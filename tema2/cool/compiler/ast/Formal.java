package cool.compiler.ast;

import cool.parser.CoolParser;
import org.antlr.v4.runtime.Token;

public class Formal extends ASTNode {
    private final String id, type;
    private final CoolParser.FormalContext ctx;

    public Formal(Token token, String id, String type, CoolParser.FormalContext ctx) {
        super(token);
        this.id = id;
        this.ctx = ctx;
        this.type = type;
    }

    @Override
    protected void printTitle() {
        print("formal");
    }

    public CoolParser.FormalContext getCtx() {
        return ctx;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @Override
    protected void printChildren() {
        print(id);
        print(type);
    }
}
