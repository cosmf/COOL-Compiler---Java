package cool.structures;

import cool.compiler.Compiler;
import cool.compiler.Constants;
import cool.parser.CoolParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.io.File;

public class SymbolTable {
    public static Scope globals;

    private static boolean semanticErrors;
    
    private static final ClassSymbol ObjectSymbol =
        new ClassSymbol(Constants.OBJECT, Constants.OBJECT_CLASS_C);
    private static final ClassSymbol IntSymbol =
        new ClassSymbol(Constants.INT, Constants.INT_CLASS_C);
    private static final ClassSymbol StringSymbol =
        new ClassSymbol(Constants.STRING, Constants.STRING_CLASS_C);
    private static final ClassSymbol IOSymbol =
        new ClassSymbol(Constants.IO, Constants.IO_CLASS_C);
    private static final ClassSymbol SelfTypeSymbol =
        new ClassSymbol(Constants.SELF_TYPE, Constants.SELF_TYPE_CLASS_C);
    private static final ClassSymbol BoolSymbol =
        new ClassSymbol(Constants.BOOL, Constants.BOOL_CLASS_C);

    public static void defineBasicClasses() {
        globals = new DefaultScope(null);
        semanticErrors = false;

        globals.add(IntSymbol);
        globals.add(StringSymbol);
        globals.add(ObjectSymbol);
        globals.add(IOSymbol);
        globals.add(SelfTypeSymbol);
        globals.add(BoolSymbol);

        // Setting up Object methods in the global scope
        Constants.OBJECT_CLASS_C.getMethodeScope().add(new Symbol("abort", Constants.OBJECT_CLASS_C));
        Constants.OBJECT_CLASS_C.getMethodeScope().add(new Symbol("type_name", Constants.STRING_CLASS_C));
        Constants.OBJECT_CLASS_C.getMethodeScope().add(new Symbol("copy", Constants.SELF_TYPE_CLASS_C));

        // Setting up IO methods
        Constants.IO_CLASS_C.getMethodeScope().add(new Symbol("out_string", Constants.SELF_TYPE_CLASS_C));
        Constants.IO_CLASS_C.getMethodeScope().add(new Symbol("out_int", Constants.SELF_TYPE_CLASS_C));
        Constants.IO_CLASS_C.getMethodeScope().add(new Symbol("in_string", Constants.STRING_CLASS_C));
        Constants.IO_CLASS_C.getMethodeScope().add(new Symbol("in_int", Constants.INT_CLASS_C));
        Constants.IO_CLASS_C.setParentScope();

        // Int inherits Object; set parent scope
        Constants.INT_CLASS_C.setParentScope();

        // Setting up String methods
        Constants.STRING_CLASS_C.getMethodeScope().add(new Symbol("length", Constants.INT_CLASS_C));
        Constants.STRING_CLASS_C.getMethodeScope().add(new Symbol("concat", Constants.STRING_CLASS_C));
        Constants.STRING_CLASS_C.getMethodeScope().add(new Symbol("substr", Constants.STRING_CLASS_C));
        Constants.STRING_CLASS_C.setParentScope();

        // Bool also inherits Object
        Constants.BOOL_CLASS_C.setParentScope();
    }

    /**
     * Displays a semantic error message.
     *
     * @param ctx  Used to determine the enclosing class context of this error,
     *             which knows the file name in which the class was defined.
     * @param info Used for line and column information.
     * @param str  The error message.
     */
    public static void error(ParserRuleContext ctx, Token info, String str) {
        while (!(ctx.getParent() instanceof CoolParser.ProgramContext))
            ctx = ctx.getParent();

        String message = "\"" + new File(Compiler.fileNames.get(ctx)).getName()
                + "\", line " + info.getLine()
                + ":" + (info.getCharPositionInLine() + 1)
                + ", Semantic error: " + str;

        System.err.println(message);

        semanticErrors = true;
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
