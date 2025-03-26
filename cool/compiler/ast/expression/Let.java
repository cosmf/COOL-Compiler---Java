package cool.compiler.ast.expression;

import cool.parser.CoolParser;
import cool.structures.*;

import java.util.List;

public class Let extends Expression {
    private final CoolParser.LetContext context;
    private final List<LetLocal> locals;
    private final Expression body;

    public Let(CoolParser.LetContext context, List<LetLocal> locals, Expression body) {
        this.context = context;
        this.locals = locals;
        this.body = body;
    }

    @Override
    public CoolParser.LetContext getContext() {
        return context;
    }

    @Override
    protected void printTitle() {
        print("let");
    }

    @Override
    protected void printChildren() {
        print(locals);
        print(body);
    }

    private static Scope<VariableSymbol> createLocalScope(Scope<VariableSymbol> scope, LetLocal local) {
        if (local.getId().equals("self")) return scope;

        ClassSymbol localType = SymbolTable.lookupClass(local.getType());
        var newScope = new DefaultScope<>(scope);
        newScope.add(new VariableSymbol(local.getId(), localType, local.getInitializer()));
        return newScope;
    }

    @Override
    public ClassSymbol getExpressionType(Scope<VariableSymbol> scope) {
        for (var local : locals) scope = createLocalScope(scope, local);
        return body.getExpressionType(scope);
    }

    @Override
    public void checkTypes(Scope<VariableSymbol> scope) {
        for (var local : locals) {
            local.checkTypes(scope);
            scope = createLocalScope(scope, local);
        }
        body.checkTypes(scope);
    }
}
