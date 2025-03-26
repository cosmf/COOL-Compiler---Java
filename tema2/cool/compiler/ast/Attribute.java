package cool.compiler.ast;

import cool.parser.CoolParser;
import cool.structures.Scope;
import org.antlr.v4.runtime.Token;

public class Attribute extends Feature {
    private final Expression initializer;

    public Attribute(Token token, String id, String type, Expression initializer, CoolParser.AttributeContext ctx) {
        super(token, id, type,ctx);
        this.initializer = initializer;
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

    public void checkAttribute(Scope scope){
        if(initializer != null) initializer.checkExpression(scope);
    }
}
