package cool.compiler;

import cool.compiler.ast.Program;
import cool.lexer.CoolLexer;
import cool.parser.CoolParser;
import cool.structures.ClassSymbol;
import cool.structures.Symbol;
import cool.structures.SymbolTable;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.io.File;
import java.io.IOException;

/**
 * Main entry point for the COOL compiler.
 * Manages multiple input files, merges parse trees, then performs semantic checks.
 */
public class Compiler {
    // Associates parse-tree nodes for classes with their source file names
    public static ParseTreeProperty<String> fileNames = new ParseTreeProperty<>();

    public static void main(String[] args) throws IOException {
        // 1) Check for input files
        if (args.length == 0) {
            System.err.println("No file(s) given");
            return;
        }

        // 2) Parse all input files into a single merged parse tree
        var globalTree = parseAllFiles(args);
        if (globalTree == null) {
            // We had lexical or syntax errors, so we halt
            System.err.println("Compilation halted");
            return;
        }

        // 3) Convert the parse tree into an AST
        Program program = new CoolVisitor().visitProgram(globalTree);

        // 4) Populate the global scope with built-in classes
        SymbolTable.defineBasicClasses();

        // 5) Perform semantic checks
        if (!runSemanticChecks(program)) {
            // If errors were detected, stop here
            System.err.println("Compilation halted");
        }
    }

    /**
     * Parses each file from 'args', merges parse trees, and returns the
     * combined result. If lexical/syntax errors occur, returns null.
     */
    private static CoolParser.ProgramContext parseAllFiles(String[] args) throws IOException {
        CoolLexer lexer = null;
        CommonTokenStream tokenStream = null;
        CoolParser parser = null;
        CoolParser.ProgramContext globalTree = null;

        // Track if any lexical or syntax error was found
        boolean encounteredLexSyntaxError = false;

        // Process each file, merge into a single parse tree
        for (var fileName : args) {
            var partialTree = parseSingleFile(fileName, lexer, tokenStream, parser);

            // If partialTree is null, it means we had an unrecoverable lexical/syntax error
            if (partialTree.tree == null) {
                encounteredLexSyntaxError |= partialTree.errorFlag;
                continue;
            }

            // Save references to the reused lexer/parser across iterations
            lexer = partialTree.lexer;
            tokenStream = partialTree.tokenStream;
            parser = partialTree.parser;

            // Merge partial tree into global
            globalTree = mergeParseTrees(globalTree, partialTree.tree);

            // Annotate the partial parse tree with file names
            annotateParseTreeWithFileNames(partialTree.tree, fileName);

            // Update error status
            encounteredLexSyntaxError |= partialTree.errorFlag;
        }

        // If lexical/syntax errors, return null
        if (encounteredLexSyntaxError) {
            return null;
        }
        return globalTree;
    }

    /**
     * Parses a single file into a partial ProgramContext, returning
     * the parse tree plus references to updated lexer/parser objects.
     */
    private static ParsedFile parseSingleFile(String fileName,
                                              CoolLexer existingLexer,
                                              CommonTokenStream existingTokenStream,
                                              CoolParser existingParser)
            throws IOException {

        var result = new ParsedFile();

        // Load file as CharStream
        var input = CharStreams.fromFileName(fileName);

        // Configure or reuse the lexer
        if (existingLexer == null) {
            result.lexer = new CoolLexer(input);
        } else {
            existingLexer.setInputStream(input);
            result.lexer = existingLexer;
        }

        // Manage token stream
        if (existingTokenStream == null) {
            result.tokenStream = new CommonTokenStream(result.lexer);
        } else {
            existingTokenStream.setTokenSource(result.lexer);
            result.tokenStream = existingTokenStream;
        }

        // Create or reuse parser
        if (existingParser == null) {
            result.parser = new CoolParser(result.tokenStream);
        } else {
            existingParser.setTokenStream(result.tokenStream);
            result.parser = existingParser;
        }

        // Attach our error listener
        var errorListener = new FileAwareErrorListener(fileName);
        result.parser.removeErrorListeners();
        result.parser.addErrorListener(errorListener);

        // Attempt to parse
        var partialTree = result.parser.program();
        result.tree = partialTree;
        result.errorFlag = errorListener.errors;

        return result;
    }

    /**
     * Merges two parse trees by appending the children of 'newTree'
     * to 'accumulatedTree' if the latter is non-null. Returns the
     * updated parse tree or the newTree if accumulatedTree was null.
     */
    private static CoolParser.ProgramContext mergeParseTrees(
            CoolParser.ProgramContext accumulatedTree,
            CoolParser.ProgramContext newTree
    ) {
        if (accumulatedTree == null) {
            return newTree;
        }
        for (int i = 0; i < newTree.getChildCount(); i++) {
            accumulatedTree.addAnyChild(newTree.getChild(i));
        }
        return accumulatedTree;
    }

    /**
     * Annotates each class node in the parse tree with a fileName
     * for error reporting.
     */
    private static void annotateParseTreeWithFileNames(
            CoolParser.ProgramContext tree,
            String fileName
    ) {
        for (int i = 0; i < tree.getChildCount(); i++) {
            var child = tree.getChild(i);
            if (child instanceof ParserRuleContext) {
                fileNames.put(child, fileName);
            }
        }
    }

    /**
     * Runs all semantic checks on the AST and returns true if no errors were found.
     */
    private static boolean runSemanticChecks(Program program) {
        // 1) Class name checks, add to global if valid
        for (var cls : program.getClasses()) {
            if (!cls.checkClassName()) {
                SymbolTable.globals.add(new ClassSymbol(cls.getName(), cls));
            }
        }

        // 2) Check inheritance parents
        for (var cls : program.getClasses()) {
            cls.checkParent();
        }

        // 3) Detect inheritance cycles
        for (var cls : program.getClasses()) {
            cls.checkCycle();
        }

        // Halt if errors
        if (SymbolTable.hasSemanticErrors()) {
            return false;
        }

        // 4) Check attributes
        for (var cls : program.getClasses()) {
            cls.checkAttributes();
        }
        if (SymbolTable.hasSemanticErrors()) {
            return false;
        }

        // 5) Check methods & parameters
        for (var cls : program.getClasses()) {
            cls.checkMethods();
        }
        for (var cls : program.getClasses()) {
            cls.checkParametersMethods();
        }

        // 6) Check bodies, then re-check attributes
        for (var cls : program.getClasses()) {
            cls.checkMethodBody();
        }
        for (var cls : program.getClasses()) {
            cls.checkAttribute2();
        }

        // Return inverse of whether errors occurred
        return !SymbolTable.hasSemanticErrors();
    }

    /**
     * Helper class that captures partial parse results for a single file,
     * including the resulting ProgramContext, updated lexer, parser, and
     * an error flag.
     */
    private static class ParsedFile {
        CoolLexer lexer;
        CommonTokenStream tokenStream;
        CoolParser parser;
        CoolParser.ProgramContext tree;
        boolean errorFlag;
    }

    /**
     * Custom error listener that includes file information in error messages.
     */
    private static class FileAwareErrorListener extends BaseErrorListener {
        private final String fileName;
        public boolean errors = false;

        public FileAwareErrorListener(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e
        ) {
            String formattedError = "\"" + new File(fileName).getName()
                    + "\", line " + line + ":" + (charPositionInLine + 1) + ", ";

            Token token = (Token) offendingSymbol;
            if (token != null && token.getType() == CoolLexer.ERROR) {
                formattedError += "Lexical error: " + token.getText();
            } else {
                formattedError += "Syntax error: " + msg;
            }

            System.err.println(formattedError);
            errors = true;
        }
    }
}
