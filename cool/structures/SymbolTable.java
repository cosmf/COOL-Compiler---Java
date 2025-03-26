package cool.structures;

import cool.compiler.Compiler;
import cool.compiler.ast.ASTNode;
import cool.parser.CoolParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.io.File;

public class SymbolTable {
    private static Scope<ClassSymbol> globals;

    private static boolean semanticErrors = false;

    public static final ClassSymbol Object = new ClassSymbol("Object", null);
    public static final ClassSymbol IO = new ClassSymbol("IO");
    public static final ClassSymbol Int = new ClassSymbol("Int");
    public static final ClassSymbol String = new ClassSymbol("String");
    public static final ClassSymbol Bool = new ClassSymbol("Bool");
    public static final ClassSymbol SelfType = new ClassSymbol("SELF_TYPE", null);

    private static boolean initialized = false;

    public static void init() {
        // Populate global scope
        semanticErrors = false;
        globals = new DefaultScope<>();
        globals.add(Object);
        globals.add(IO);
        globals.add(Int);
        globals.add(String);
        globals.add(Bool);
        globals.add(SelfType);

        if (initialized) return;
        initialized = true;

        // Object: Define methods
        var methods = Object.getMethodScope();
        methods.add(new MethodSymbol("abort", Object, Object, null));
        methods.add(new MethodSymbol("type_name", Object, String, null));
        methods.add(new MethodSymbol("copy", Object, SelfType, null));

        // IO: Define methods
        methods = IO.getMethodScope();
        methods.add(new MethodSymbol("out_string", IO, SelfType, null, new VariableSymbol("x", String, null)));
        methods.add(new MethodSymbol("out_int", IO, SelfType,null,  new VariableSymbol("x", Int, null)));
        methods.add(new MethodSymbol("in_string", IO, String, null));
        methods.add(new MethodSymbol("in_int", IO, Int, null));

        // String: Define methods
        methods = String.getMethodScope();
        methods.add(new MethodSymbol("length", String, Int, null));
        methods.add(new MethodSymbol("concat", String, String, null, new VariableSymbol("s", String, null)));
        methods.add(new MethodSymbol("substr", String, String, null, new VariableSymbol("i", Int, null), new VariableSymbol("l", Int, null)));
    }

    public static boolean defineClass(String name) {
        return globals.add(new ClassSymbol(name));
    }

    public static ClassSymbol lookupClass(String name) {
        return globals.lookup(name);
    }

    /**
     * Displays a semantic error message.
     *
     * @param node Used to determine the enclosing class context of this error,
     *             which knows the file name in which the class was defined,
     *             as well as for line and column information.
     * @param str  The error message.
     */
    public static void error(ASTNode node, Token info, String str) {
        ParserRuleContext ctx = node.getContext();
        while (!(ctx.getParent() instanceof CoolParser.ProgramContext))
            ctx = ctx.getParent();

        String message = "\"" + new File(Compiler.fileNames.get(ctx)).getName()
                + "\", line " + info.getLine()
                + ":" + (info.getCharPositionInLine() + 1)
                + ", Semantic error: " + str;

        System.err.println(message);

        semanticErrors = true;
    }

    public static String getFilePath(ParserRuleContext ctx){
        while (!(ctx.getParent() instanceof CoolParser.ProgramContext))
            ctx = ctx.getParent();
        return new File(Compiler.fileNames.get(ctx)).getName();
    }

    public static void error(String str) {
        String message = "Semantic error: " + str;

        System.err.println(message);

        semanticErrors = true;
    }

    public static boolean hasSemanticErrors() {
        return semanticErrors;
    }
}
