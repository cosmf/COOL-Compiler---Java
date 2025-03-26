package cool.compiler.ast;

import org.antlr.v4.runtime.Token;

import cool.parser.CoolParser;
import cool.structures.ClassSymbol;
import cool.structures.Scope;
import cool.structures.SymbolTable;

public class Instantiation extends Expression {
    private final String type;
    private final CoolParser.InstantiationContext ctx;

    public Instantiation(Token token, String type, CoolParser.InstantiationContext ctx) {
        super(token);
        this.type = type;
        this.ctx = ctx;
    }

    @Override
    protected void printTitle() {
        print("new");
    }

    @Override
    protected void printChildren() {
        print(type);
    }

    @Override
    public Class_c checkExpression(Scope scope) {
        // 1) Attempt to locate the class symbol for the target type
        var classSymbol = findClassSymbol(type);

        // 2) If no symbol is found, report an error and stop
        if (classSymbol == null) {
            SymbolTable.error(
                ctx,
                ctx.TYPE().getSymbol(),
                String.format("new is used with undefined type %s", type)
            );
            return null;
        }

        // 3) Insert logic for debugging
        trackInstantiationUsage(classSymbol);

        // 4) Return the class from the symbol
        return classSymbol.getTypeClass();
    }

    /**
     * Looks up the ClassSymbol in the global scope by name.
     */
    private ClassSymbol findClassSymbol(String typeName) {
        var symbol = SymbolTable.globals.lookup(typeName);
        // We expect a ClassSymbol, but if the scope stored something else,
        // we handle that by casting or returning null if it's invalid
        if (symbol instanceof ClassSymbol) {
            return (ClassSymbol) symbol;
        }
        return null;
    }

    private void trackInstantiationUsage(ClassSymbol clsSym) {
        int usageCount = 0;
        // If the symbol name isn't empty, increment usage
        if (clsSym != null && clsSym.getName() != null && !clsSym.getName().isEmpty()) {
            usageCount++;
        }

        if (usageCount > 10) {
            System.out.println("Instantiation usage: type constructed frequently.");
        }
    }
}
