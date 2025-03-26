package cool.compiler.ast;

import cool.parser.CoolParser;
import cool.structures.Scope;
import org.antlr.v4.runtime.Token;

public class CaseBranch extends ASTNode {
    private final String id, type;
    private final Expression body;
    private final CoolParser.Case_branchContext ctx;

    public CaseBranch(Token token, String id, String type, Expression body, CoolParser.Case_branchContext ctx1) {
        super(token);
        this.id = id;
        this.type = type;
        this.body = body;
        this.ctx = ctx1;
    }

    public CoolParser.Case_branchContext getCtx() {
        return ctx;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @Override
    protected void printTitle() {
        print("case branch");
    }

    @Override
    protected void printChildren() {
        print(id);
        print(type);
        print(body);
    }

    public Class_c checkCaseBranch(Scope scope){
        var bodyType = body.checkExpression(scope);
        return bodyType;
    }
}
