package cool.structures;

import cool.compiler.ast.expression.Expression;

public class VariableSymbol extends Symbol {
    private final ClassSymbol type;
    private final Expression initializer;

    public VariableSymbol(String name, ClassSymbol type, Expression initializer) {
        super(name);
        this.type = type;
        this.initializer = initializer;
    }

    public ClassSymbol getType() {
        return type;
    }

    public Expression getInitializer() {
        return initializer;
    }
}
