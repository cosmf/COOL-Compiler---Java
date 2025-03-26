package cool.compiler.ast.expression;

import cool.compiler.ast.ASTNode;
import cool.parser.CoolParser;
import cool.structures.DefaultScope;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import cool.structures.VariableSymbol;

public class CaseBranch extends ASTNode {
    private final CoolParser.Case_branchContext context;
    private final String id;
    private final String type;
    private final Expression body;

    public CaseBranch(CoolParser.Case_branchContext context, String id, String type, Expression body) {
        this.context = context;
        this.id = id;
        this.type = type;
        this.body = body;
    }

    @Override
    public CoolParser.Case_branchContext getContext() {
        return context;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Expression getBody() {
        return body;
    }

    public void checkTypes(Scope<VariableSymbol> scope) {
        if (id.equals("self"))
            SymbolTable.error(this, context.ID().getSymbol(), "Case variable has illegal name self");
        else if (type.equals("SELF_TYPE"))
            SymbolTable.error(this, context.TYPE().getSymbol(), "Case variable %s has illegal type SELF_TYPE".formatted(id));
        else if (SymbolTable.lookupClass(type) == null)
            SymbolTable.error(this, context.TYPE().getSymbol(), "Case variable %s has undefined type %s".formatted(id, type));
        var branchScope = new DefaultScope<>(scope);
        branchScope.add(new VariableSymbol(id, SymbolTable.lookupClass(type), null));
        body.checkTypes(branchScope);
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
}
