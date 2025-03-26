package cool.compiler.ast;

import cool.parser.CoolParser;
import cool.structures.Scope;
import cool.structures.Symbol;
import cool.structures.SymbolTable;
import org.antlr.v4.runtime.Token;

public class Assign extends Expression {
    private final String id;
    private final Expression expression;

    private final CoolParser.VarAssignContext ctx;

    public Assign(Token token, String id, Expression expression, CoolParser.VarAssignContext ctx) {
        super(token);
        this.id = id;
        this.expression = expression;
        this.ctx = ctx;
    }

    @Override
    protected void printTitle() {
        print("<-");
    }

    @Override
    protected void printChildren() {
        print(id);
        print(expression);
    }


    @Override
    public Class_c checkExpression(Scope scope) {
        // Disallow assigning to 'self'
        checkAssignToSelf();

        // Infer the expression's type
        var typeClass = inferExpressionType(scope);

        // Lookup the declared type in the current scope
        var idClass = lookupDeclaredType(scope);

        // Validate the types
        if (!validateTypes(typeClass, idClass)) {
            return null;
        }

        // Return the declared type of the identifier
        return idClass.getTypeClass();
    }

    private void checkAssignToSelf() {
        if ("self".equals(id)) {
            SymbolTable.error(ctx, ctx.ID().getSymbol(), "Cannot assign to self");
        }
    }

    private Class_c inferExpressionType(Scope scope) {
        return expression.checkExpression(scope);
    }

    private Symbol lookupDeclaredType(Scope scope) {
        return scope.lookup(id);
    }

    private boolean validateTypes(Class_c typeClass, Symbol idClass) {
        if (idClass == null || typeClass == null) {
            return false;
        }
        if (!typeClass.checkIfParent(idClass.getTypeClass().getName())) {
            SymbolTable.error(ctx, ctx.expr().start,
                    String.format("Type %s of assigned expression is incompatible with declared type %s of identifier %s",
                            typeClass.getName(), idClass.getTypeClass().getName(), id));
            return false;
        }
        return true;
    }
}
