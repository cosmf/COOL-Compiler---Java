package cool.compiler.ast.expression;

import cool.parser.CoolParser;
import cool.structures.ClassSymbol;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import cool.structures.VariableSymbol;

public class Negate extends Expression {
    private final CoolParser.NegateContext context;
    private final Expression target;

    public Negate(CoolParser.NegateContext context, Expression target) {
        this.context = context;
        this.target = target;
    }

    @Override
    public CoolParser.NegateContext getContext() {
        return context;
    }

    @Override
    protected void printTitle() {
        print("~");
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
        ClassSymbol type = target.getExpressionType(scope);
        if (type != null && type != SymbolTable.Bool)
            SymbolTable.error(this, context.expr().start,
                    "Operand of not has type %s instead of Bool".formatted(type.getName()));
        target.checkTypes(scope);
    }
}
