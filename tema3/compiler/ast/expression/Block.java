package cool.compiler.ast.expression;

import cool.parser.CoolParser;
import cool.structures.ClassSymbol;
import cool.structures.Scope;
import cool.structures.VariableSymbol;

import java.util.List;

public class Block extends Expression {
    private final CoolParser.BlockContext context;
    private final List<Expression> expressions;

    public Block(CoolParser.BlockContext context, List<Expression> expressions) {
        this.context = context;
        this.expressions = expressions;
    }

    @Override
    public CoolParser.BlockContext getContext() {
        return context;
    }

    @Override
    protected void printTitle() {
        print("block");
    }

    @Override
    protected void printChildren() {
        print(expressions);
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public ClassSymbol getExpressionType(Scope<VariableSymbol> scope) {
        return expressions.get(expressions.size() - 1).getExpressionType(scope);
    }

    @Override
    public void checkTypes(Scope<VariableSymbol> scope) {
        for (var expr : expressions) expr.checkTypes(scope);
    }
}
