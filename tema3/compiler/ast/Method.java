package cool.compiler.ast;

import cool.compiler.ast.expression.Expression;
import cool.parser.CoolParser;

import java.util.List;

public class Method extends Feature {
    private final CoolParser.MethodContext context;
    private final List<Formal> formals;
    private final Expression body;

    public Method(CoolParser.MethodContext context, String id, String type, List<Formal> formals, Expression body) {
        super(id, type);
        this.context = context;
        this.formals = formals;
        this.body = body;
    }

    @Override
    public CoolParser.MethodContext getContext() {
        return context;
    }

    public List<Formal> getFormals() {
        return formals;
    }

    public Expression getBody() {
        return body;
    }

    @Override
    protected void printTitle() {
        print("method");
    }

    @Override
    protected void printChildren() {
        print(id);
        print(formals);
        print(type);
        print(body);
    }
}
