package cool.compiler;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import cool.compiler.ast.PClass;
import cool.compiler.ast.Program;
import cool.compiler.generator.GenerateCode;
import cool.lexer.CoolLexer;
import cool.parser.CoolParser;
import cool.structures.ClassSymbol;
import cool.structures.MethodSymbol;
import cool.structures.SymbolTable;
import cool.structures.VariableSymbol;


public class Compiler {
    // Annotates class nodes with the names of files where they are defined.
    public static ParseTreeProperty<String> fileNames = new ParseTreeProperty<>();
    public static Set<String> filePathNames = new HashSet<>();

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("No file(s) given");
            return;
        }

        CoolLexer lexer = null;
        CommonTokenStream tokenStream = null;
        CoolParser parser = null;
        CoolParser.ProgramContext globalTree = null;

        // True if any lexical or syntax errors occur.
        boolean lexicalSyntaxErrors = false;

        // Parse each input file and build one big parse tree out of
        // individual parse trees.
        for (var fileName : args) {
            String fileNameSimple = new File(fileName).getName();
            var input = CharStreams.fromFileName(fileName);

            // Lexer
            if (lexer == null)
                lexer = new CoolLexer(input);
            else
                lexer.setInputStream(input);

            // Token stream
            if (tokenStream == null)
                tokenStream = new CommonTokenStream(lexer);
            else
                tokenStream.setTokenSource(lexer);
                
            /*
            // Test lexer only.
            tokenStream.fill();
            List<Token> tokens = tokenStream.getTokens();
            tokens.stream().forEach(token -> {
                var text = token.getText();
                var name = CoolLexer.VOCABULARY.getSymbolicName(token.getType());
                
                System.out.println(text + " : " + name);
                //System.out.println(token);
            });
            */

            // Parser
            if (parser == null)
                parser = new CoolParser(tokenStream);
            else
                parser.setTokenStream(tokenStream);

            // Customized error listener, for including file names in error
            // messages.
            var errorListener = new BaseErrorListener() {
                public boolean errors = false;

                @Override
                public void syntaxError(Recognizer<?, ?> recognizer,
                                        Object offendingSymbol,
                                        int line, int charPositionInLine,
                                        String msg,
                                        RecognitionException e) {
                    String newMsg = "\"" + new File(fileName).getName() + "\", line " +
                            line + ":" + (charPositionInLine + 1) + ", ";

                    Token token = (Token) offendingSymbol;
                    if (token.getType() == CoolLexer.ERROR)
                        newMsg += "Lexical error: " + token.getText();
                    else
                        newMsg += "Syntax error: " + msg;

                    System.err.println(newMsg);
                    errors = true;
                }
            };

            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // Actual parsing
            var tree = parser.program();
            if (globalTree == null)
                globalTree = tree;
            else
                // Add the current parse tree's children to the global tree.
                for (int i = 0; i < tree.getChildCount(); i++)
                    globalTree.addAnyChild(tree.getChild(i));

            // Annotate class nodes with file names, to be used later
            // in semantic error messages.
            for (int i = 0; i < tree.getChildCount(); i++) {
                var child = tree.getChild(i);
                // The only ParserRuleContext children of the program node
                // are class nodes.
                if (child instanceof ParserRuleContext) {
                    fileNames.put(child, fileName);
                    filePathNames.add(fileNameSimple);
                }
            }

            // Record any lexical or syntax errors.
            lexicalSyntaxErrors |= errorListener.errors;
        }

        // Stop before semantic analysis phase, in case errors occurred.
        if (lexicalSyntaxErrors) {
            System.err.println("Compilation halted");
            return;
        }

        // Populate global scope
        SymbolTable.init();

        // Create AST
        Program program = new CoolVisitor().visitProgram(globalTree);
        // Create class symbols
        for (var cls : program.getClasses()) {
            if (cls.getName().equals("SELF_TYPE"))
                SymbolTable.error(cls, cls.getContext().name, "Class has illegal name SELF_TYPE");
            else if (!SymbolTable.defineClass(cls.getName()))
                SymbolTable.error(cls, cls.getContext().name, "Class %s is redefined".formatted(cls.getName()));
        }
        // Set parents


        for (var cls : program.getClasses()) {
            String parent = cls.getParent();
            if (parent == null) continue;
            if (parent.equals("Int") || parent.equals("String") || parent.equals("Bool") || parent.equals("SELF_TYPE")) {
                SymbolTable.error(cls, cls.getContext().parent, "Class %s has illegal parent %s".formatted(cls.getName(), parent));
                continue;
            }
            // Ensure parent exists
            ClassSymbol parentSymbol = SymbolTable.lookupClass(parent);
            if (parentSymbol == null)
                SymbolTable.error(cls, cls.getContext().parent, "Class %s has undefined parent %s".formatted(cls.getName(), parent));
                // Set the parent on the class
            else SymbolTable.lookupClass(cls.getName()).setParent(parentSymbol);
        }
        // Check for inheritance cycles
        for (var cls : program.getClasses()) {
            ClassSymbol classSymbol = SymbolTable.lookupClass(cls.getName());
            if (classSymbol == null) continue;
            ClassSymbol parent = classSymbol.getParent();
            while (parent != null) {
                if (parent == classSymbol) {
                    SymbolTable.error(cls, cls.getContext().name, "Inheritance cycle for class %s".formatted(cls.getName()));
                    break;
                } else parent = parent.getParent();
            }
        }

        if (SymbolTable.hasSemanticErrors()) {
            System.err.println("Compilation halted");
            return;
        }

        // Define attributes & methods and check names
        List<PClass> classes = new LinkedList<>(program.getClasses());
        // Start with basic classes marked as processed
        Set<ClassSymbol> processedClasses = new HashSet<>();
        processedClasses.add(SymbolTable.Object);
        processedClasses.add(SymbolTable.Int);
        processedClasses.add(SymbolTable.Bool);
        processedClasses.add(SymbolTable.String);
        processedClasses.add(SymbolTable.IO);

        // Go through classes in parent -> child order
        while (!classes.isEmpty()) {
            var it = classes.iterator();
            while (it.hasNext()) {
                var cls = it.next();
                // Skip class if we haven't processed its parent yet
                String c = cls.getName();
                ClassSymbol classSymbol = SymbolTable.lookupClass(c);
                ClassSymbol parent = SymbolTable.lookupClass(cls.getParent());
                if (parent != null && !processedClasses.contains(parent)) continue;
                it.remove();
                processedClasses.add(classSymbol);

                for (var attribute : cls.getAttributes()) {
                    String a = attribute.getId();
                    if (a.equals("self"))
                        SymbolTable.error(attribute, attribute.getContext().start, "Class %s has attribute with illegal name self".formatted(c));
                    else if (classSymbol.getAttributeScope().lookup(a, false) != null)
                        SymbolTable.error(attribute, attribute.getContext().start, "Class " + c + " redefines attribute " + a);
                    else if (classSymbol.getAttributeScope().getParent() != null && classSymbol.getAttributeScope().getParent().lookup(a) != null)
                        SymbolTable.error(attribute, attribute.getContext().start, "Class %s redefines inherited attribute %s".formatted(c, a));
                    else {
                        ClassSymbol type = SymbolTable.lookupClass(attribute.getType());
                        if (type == null && !attribute.getType().equals("SELF_TYPE"))
                            SymbolTable.error(attribute, attribute.getContext().TYPE().getSymbol(),
                                    "Class " + c + " has attribute " + a + " with undefined type " + attribute.getType());
                            // All checks passed
                        else classSymbol.getAttributeScope().add(new VariableSymbol(a, type, attribute.getInitializer()));
                    }
                }

                for (var method : cls.getMethods()) {
                    String m = method.getId();
                    if (classSymbol.getMethodScope().lookup(m, false) != null) {
                        SymbolTable.error(method, method.getContext().ID().getSymbol(), "Class %s redefines method %s".formatted(c, m));
                        continue;
                    }

                    ClassSymbol returnType = SymbolTable.lookupClass(method.getType());
                    if (returnType == null) {
                        SymbolTable.error(method, method.getContext().TYPE().getSymbol(),
                                "Class %s has method %s with undefined return type %s".formatted(c, m, method.getType()));
                    }

                    MethodSymbol methodSymbol = new MethodSymbol(m, classSymbol, returnType, method.getBody());
                    for (var formal : method.getFormals()) {
                        boolean addToScope = true;
                        if (formal.getId().equals("self")) {
                            SymbolTable.error(formal, formal.getContext().ID().getSymbol(),
                                    "Method %s of class %s has formal parameter with illegal name self".formatted(m, c));
                            addToScope = false;
                        }
                        if (methodSymbol.getMethodScope().lookup(formal.getId(), false) != null) {
                            SymbolTable.error(formal, formal.getContext().ID().getSymbol(),
                                    "Method %s of class %s redefines formal parameter %s".formatted(m, c, formal.getId()));
                            addToScope = false;
                        }

                        ClassSymbol formalType;
                        if (formal.getType().equals("SELF_TYPE")) {
                            SymbolTable.error(formal, formal.getContext().TYPE().getSymbol(),
                                    "Method %s of class %s has formal parameter %s with illegal type SELF_TYPE".formatted(m, c, formal.getId()));
                            formalType = null;
                        } else {
                            formalType = SymbolTable.lookupClass(formal.getType());
                            if (formalType == null)
                                SymbolTable.error(formal, formal.getContext().TYPE().getSymbol(),
                                        "Method %s of class %s has formal parameter %s with undefined type %s"
                                                .formatted(m, c, formal.getId(), formal.getType()));
                        }

                        methodSymbol.addFormal(new VariableSymbol(formal.getId(), formalType, null), addToScope);
                    }

                    MethodSymbol superMethod = classSymbol.getParent() == null ? null : classSymbol.getParent().getMethodScope().lookup(m);
                    if (superMethod != null) {
                        List<VariableSymbol> methodFormals = methodSymbol.getFormals();
                        List<VariableSymbol> superFormals = superMethod.getFormals();

                        if (superFormals.size() != methodFormals.size())
                            SymbolTable.error(method, method.getContext().ID().getSymbol(),
                                    "Class %s overrides method %s with different number of formal parameters".formatted(c, m));
                        else for (int i = 0; i < methodFormals.size(); ++i) {
                            var mft = methodFormals.get(i).getType();
                            var sft = superFormals.get(i).getType();
                            if (mft != null && sft != null && mft != sft) {
                                var formal = method.getFormals().get(i);
                                SymbolTable.error(formal, formal.getContext().TYPE().getSymbol(),
                                        "Class %s overrides method %s but changes type of formal parameter %s from %s to %s"
                                                .formatted(c, m, formal.getId(), sft.getName(), mft.getName()));
                            }
                        }

                        var superReturnType = superMethod.getReturnType();
                        if (returnType != null && superReturnType != null && returnType != superReturnType) {
                            SymbolTable.error(method, method.getContext().TYPE().getSymbol(),
                                    "Class %s overrides method %s but changes return type from %s to %s"
                                            .formatted(c, m, superReturnType.getName(), returnType.getName()));
                        }
                    }

                    // All checks passed
                    classSymbol.getMethodScope().add(methodSymbol);
                }
            }
        }

        // Perform type-checking on all attributes
        for (var cls : program.getClasses()) {
            ClassSymbol classSymbol = SymbolTable.lookupClass(cls.getName());
            var scope = classSymbol.getAttributeScope();

            for (var attribute : cls.getAttributes()) {
                VariableSymbol attrSymbol = scope.lookup(attribute.getId());
                if (attrSymbol == null || attribute.getInitializer() == null) continue;
                // Register attribute
                var declaredType = attrSymbol.getType();
                attribute.getInitializer().checkTypes(scope);
                scope.add(new VariableSymbol(attribute.getId(), declaredType, attribute.getInitializer()));
                // Check types
                var expressionType = attribute.getInitializer().getExpressionType(scope);
                if (expressionType == null || declaredType == null) continue;
                if (!expressionType.canBeAssignedTo(declaredType, classSymbol))
                    SymbolTable.error(attribute, attribute.getContext().expr().start,
                            "Type %s of initialization expression of attribute %s is incompatible with declared type %s"
                                    .formatted(expressionType.getName(), attribute.getId(), attribute.getType()));
            }
        }
        // Perform type-checking on all method bodies
        for (var cls : program.getClasses()) {
            ClassSymbol classSymbol = SymbolTable.lookupClass(cls.getName());
            for (var method : cls.getMethods()) {
                MethodSymbol methodSymbol = classSymbol.getMethodScope().lookup(method.getId());
                if (methodSymbol == null) continue;
                // Type-check the method body
                var scope = methodSymbol.getMethodScope();
                method.getBody().checkTypes(scope);
                // Check return type
                var declaredType = methodSymbol.getReturnType();
                var expressionType = method.getBody().getExpressionType(scope);
                if (declaredType == null || expressionType == null) continue;
                if (!expressionType.canBeAssignedTo(declaredType, classSymbol))
                    SymbolTable.error(method, method.getContext().expr().start,
                            "Type %s of the body of method %s is incompatible with declared return type %s"
                                    .formatted(expressionType.getName(), method.getId(), method.getType()));
            }
        }

        if (SymbolTable.hasSemanticErrors()) {
            System.err.println("Compilation halted");
            return;
        }
        GenerateCode generator = new GenerateCode();
        String code = generator.generateCode(program);
        System.out.println(code);
    }
}
