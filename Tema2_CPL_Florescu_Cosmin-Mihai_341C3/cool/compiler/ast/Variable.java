package cool.compiler.ast;

import cool.compiler.Constants;
import cool.parser.CoolParser;
import cool.structures.ClassSymbol;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import org.antlr.v4.runtime.Token;

public class Variable extends Expression {
    private final String id;
    private final CoolParser.VarContext ctx;

    public Variable(Token token, String id, CoolParser.VarContext ctx) {
        super(token);
        this.id = id;
        this.ctx = ctx;
    }

    @Override
    protected void printTitle() {
        print(id);
    }

    @Override
    protected void printChildren() {
    }

    @Override
    public Class_c checkExpression(Scope scope) {
        // Check if the identifier is valid
        if (!isIdentifierValid(scope)) {
            reportUndefinedIdentifier();
            return null;
        }
    
        // Fetch the class type for the identifier
        return getClassType(scope);
    }
    
    private boolean isIdentifierValid(Scope scope) {
        return id.equals("self") || scope.lookup(id) != null;
    }
    
    private void reportUndefinedIdentifier() {
        SymbolTable.error(ctx, ctx.ID().getSymbol(), String.format("Undefined identifier %s", id));
    }
    
    private Class_c getClassType(Scope scope) {
        var symbol = scope.lookup(id);
        return (symbol != null) ? symbol.getTypeClass() : null;
    }
    
}
