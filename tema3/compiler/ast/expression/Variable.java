package cool.compiler.ast.expression;

import cool.parser.CoolParser;
import cool.structures.ClassSymbol;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import cool.structures.VariableSymbol;

public class Variable extends Expression {
    private final CoolParser.VarContext context;
    private final String id;

    public Variable(CoolParser.VarContext context, String id) {
        this.context = context;
        this.id = id;
    }

    @Override
    public CoolParser.VarContext getContext() {
        return context;
    }

    @Override
    protected void printTitle() {
        print(id);
    }

    @Override
    protected void printChildren() {
    }

    public String getId() {
        return id;
    }

    @Override
    public ClassSymbol getExpressionType(Scope<VariableSymbol> scope) {
        VariableSymbol var = scope.lookup(id);
        return var == null ? null : var.getType();
    }

    @Override
    public void checkTypes(Scope<VariableSymbol> scope) {
        if (scope.lookup(id) == null)
            SymbolTable.error(this, context.start, "Undefined identifier %s".formatted(id));
    }
}
