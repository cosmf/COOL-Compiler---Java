package cool.compiler.ast.expression;

import cool.parser.CoolParser;
import cool.structures.ClassSymbol;
import cool.structures.Scope;
import cool.structures.VariableSymbol;

import java.util.List;

public class SelfMethodCall extends Expression {
    private final CoolParser.SelfMethodCallContext context;
    private final String name;
    private final List<Expression> arguments;

    public SelfMethodCall(CoolParser.SelfMethodCallContext context, String name, List<Expression> arguments) {
        this.context = context;
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public CoolParser.SelfMethodCallContext getContext() {
        return context;
    }

    @Override
    protected void printTitle() {
        print("implicit dispatch");
    }

    @Override
    protected void printChildren() {
        print(name);
        print(arguments);
    }


    public List<Expression> getArguments() {
        return arguments;
    }

    public CoolParser.SelfMethodCallContext getCtx(){
        return context;
    }

    public String getName() {
        return name;
    }

    @Override
    public ClassSymbol getExpressionType(Scope<VariableSymbol> scope) {
        var method = scope.getCurrentClass().getMethodScope().lookup(name);
        return method == null ? null : method.getReturnType();
    }

    @Override
    public void checkTypes(Scope<VariableSymbol> scope) {
        MethodCall.checkMethodCall(scope, scope.getCurrentClass(), name, arguments, this, context.ID().getSymbol());
    }
}
