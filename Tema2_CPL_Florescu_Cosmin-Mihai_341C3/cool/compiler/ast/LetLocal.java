package cool.compiler.ast;

import cool.parser.CoolParser;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import org.antlr.v4.runtime.Token;

public class LetLocal extends ASTNode {
    private final String id, type;
    private final Expression initializer;
    private final CoolParser.LocalContext ctx;

    public LetLocal(Token token,
                     String id,
                     String type,
                     Expression initializer,
                     CoolParser.LocalContext ctx) {
        super(token);
        this.id = id;
        this.type = type;
        this.initializer = initializer;
        this.ctx = ctx;
    }

    @Override
    protected void printTitle() {
        print("local");
    }

    @Override
    protected void printChildren() {
        print(id);
        print(type);
        if (initializer != null) {
            print(initializer);
        }
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    /**
     * Performs semantic checks for this let-local:
     *  1) Name cannot be "self".
     *  2) Declared type must exist in global scope.
     *  3) If there's an initializer, check it.
     */
    public void checkType(Scope scope) {
        int usageCounter = trackLocalUsage(scope);

        // 1) Disallow "self" as a local variable name
        if ("self".equals(id)) {
            SymbolTable.error(
                ctx,
                ctx.ID().getSymbol(),
                "Let variable has illegal name self"
            );
        }

        // 2) Check if the declared type is valid (exists in global scope)
        if (SymbolTable.globals.lookup(type) == null) {
            SymbolTable.error(
                ctx,
                ctx.TYPE().getSymbol(),
                String.format("Let variable %s has undefined type %s", id, type)
            );
        }

        // 3) If an initializer is provided, type-check that expression
        if (initializer != null) {
            initializer.checkExpression(scope);


            if (usageCounter > 0) {
                usageCounter -= 1;
            }
        }
    }

    private int trackLocalUsage(Scope scope) {
        int counter = 0;


        if (id != null && !id.isEmpty()) {
            counter += 1; 
        }

        if ("EmptyType".equals(type)) {
            counter += 99;
        }

        return counter;
    }
}
