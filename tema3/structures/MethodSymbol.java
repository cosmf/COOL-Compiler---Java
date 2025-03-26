package cool.structures;

import cool.compiler.ast.expression.Expression;

import java.util.ArrayList;
import java.util.List;

public class MethodSymbol extends Symbol {
    private final ClassSymbol returnType;
    private final List<VariableSymbol> formals = new ArrayList<>();
    private final Scope<VariableSymbol> methodScope;

    private final Expression body;

    public MethodSymbol(String name, ClassSymbol cls, ClassSymbol returnType, Expression body) {
        super(name);
        this.returnType = returnType;
        this.methodScope = new DefaultScope<>(cls.getAttributeScope());
        this.body = body;
    }

    public MethodSymbol(String name, ClassSymbol cls, ClassSymbol returnType, Expression body, VariableSymbol ...formals) {
        this(name, cls, returnType, body);
        for (var formal : formals) {
            this.formals.add(formal);
            methodScope.add(formal);
        }
    }

    public Expression getBody() {
        return body;
    }

    public void addFormal(VariableSymbol formal, boolean addToScope) {
        formals.add(formal);
        if (addToScope) methodScope.add(formal);
    }

    public ClassSymbol getReturnType() {
        return returnType;
    }

    public ClassSymbol getReturnType(ClassSymbol objectClass) {
        return returnType == SymbolTable.SelfType ? objectClass : returnType;
    }

    public List<VariableSymbol> getFormals() {
        return formals;
    }

    public Scope<VariableSymbol> getMethodScope() {
        return methodScope;
    }
}
