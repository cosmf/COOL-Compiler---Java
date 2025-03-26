package cool.compiler.ast.expression;

import cool.parser.CoolParser;
import cool.structures.ClassSymbol;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import cool.structures.VariableSymbol;

public class IsVoid extends Expression {
    private final CoolParser.IsvoidContext context;
    private final Expression target;

    public IsVoid(CoolParser.IsvoidContext context, Expression target) {
        this.context = context;
        this.target = target;
    }

    @Override
    public CoolParser.IsvoidContext getContext() {
        return context;
    }

    @Override
    protected void printTitle() {
        print("isvoid");
    }

    @Override
    protected void printChildren() {
        print(target);
    }

    @Override
    public ClassSymbol getExpressionType(Scope<VariableSymbol> scope) {
        return SymbolTable.Bool;
    }

    @Override
    public void checkTypes(Scope<VariableSymbol> scope) {
        target.checkTypes(scope);
    }
}
