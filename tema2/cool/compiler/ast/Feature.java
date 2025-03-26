package cool.compiler.ast;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

public abstract class Feature extends ASTNode {
    protected final String id, type;
    protected final CoolParser.FeatureContext ctx;

    public Feature(Token token, String id, String type, CoolParser.FeatureContext ctx) {
        super(token);
        this.id = id;
        this.type = type;
        this.ctx = ctx;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public CoolParser.FeatureContext getCtx() {
        return ctx;
    }

    @Override
    public String toString() {
        return "Feature{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
