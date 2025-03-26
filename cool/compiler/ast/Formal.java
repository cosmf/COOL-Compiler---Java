package cool.compiler.ast;

import cool.parser.CoolParser;

public class Formal extends ASTNode {
    private final CoolParser.FormalContext context;
    private final String id;
    private final String type;

    public Formal(CoolParser.FormalContext context, String id, String type) {
        this.context = context;
        this.id = id;
        this.type = type;
    }

    @Override
    public CoolParser.FormalContext getContext() {
        return context;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @Override
    protected void printTitle() {
        print("formal");
    }

    @Override
    protected void printChildren() {
        print(id);
        print(type);
    }
}
