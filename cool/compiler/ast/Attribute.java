package cool.compiler.ast;

import cool.compiler.ast.expression.Expression;
import cool.parser.CoolParser;

public class Attribute extends Feature {
    private final CoolParser.AttributeContext context;
    private final Expression initializer;

    public Attribute(CoolParser.AttributeContext context, String id, String type, Expression initializer) {
        super(id, type);
        this.context = context;
        this.initializer = initializer;
    }

    @Override
    public CoolParser.AttributeContext getContext() {
        return context;
    }

    public Expression getInitializer() {
        return initializer;
    }

    @Override
    protected void printTitle() {
        print("attribute");
    }

    @Override
    protected void printChildren() {
        print(id);
        print(type);
        if (initializer != null) print(initializer);
    }
}
