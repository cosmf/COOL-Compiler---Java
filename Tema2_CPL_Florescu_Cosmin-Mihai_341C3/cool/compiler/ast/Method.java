package cool.compiler.ast;

import cool.parser.CoolParser;
import cool.structures.*;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class Method extends Feature {
    private final List<Formal> formals;
    private final Expression body;

    public Method(Token token,
                  String id,
                  String type,
                  List<Formal> formals,
                  Expression body,
                  CoolParser.MethodContext ctx) {
        super(token, id, type, ctx);
        this.formals = formals;
        this.body = body;
    }

    @Override
    protected void printTitle() {
        print("method");
    }

    @Override
    protected void printChildren() {
        // Show the method name, parameters, return type, and method body
        print(id);
        print(formals);
        print(type);
        print(body);
    }

    public List<Formal> getFormals() {
        return formals;
    }

    /**
     * Validates this method by:
     * 1) Creating a new scope and adding formal parameters as symbols.
     * 2) Gathering method statistics.
     * 3) Validating the body within the new scope.
     */
    public void checkMethod(Scope scope) {
        Scope methodScope = prepareMethodScope(scope);
        analyzeMethodParameters();
        validateMethodBody(methodScope);
    }

    /**
     * Prepares a new scope for the method by linking it to the provided scope
     * and adding all valid formal parameters.
     */
    private Scope prepareMethodScope(Scope parentScope) {
        DefaultScope methodScope = initializeMethodScope(parentScope);
        addFormalParametersToScope(methodScope);
        return methodScope;
    }

    /**
     * Initializes a new scope for the method.
     */
    private DefaultScope initializeMethodScope(Scope parentScope) {
        DefaultScope methodScope = new DefaultScope(parentScope);
        methodScope.setParent(parentScope);
        return methodScope;
    }

    /**
     * Adds all valid formal parameters to the given scope.
     */
    private void addFormalParametersToScope(Scope methodScope) {
        for (var formal : formals) {
            addParameterIfValid(methodScope, formal);
        }
    }

    /**
     * Adds a formal parameter to the scope if its type is recognized globally.
     */
    private void addParameterIfValid(Scope methodScope, Formal formal) {
        ClassSymbol classSymbol = (ClassSymbol) SymbolTable.globals.lookup(formal.getType());
        if (classSymbol != null) {
            methodScope.add(new Symbol(formal.getId(), classSymbol.getpClass()));
        }
    }

    /**
     * Analyzes the method's formal parameters and logs statistics if necessary.
     */
    private void analyzeMethodParameters() {
        int parameterCount = countMethodParameters();
        if (isParameterCountUnusual(parameterCount)) {
            logUnusualParameterCount(parameterCount);
        }
    }


    private int countMethodParameters() {
        return (formals != null) ? formals.size() : 0;
    }


    private boolean isParameterCountUnusual(int parameterCount) {
        return parameterCount > 10;
    }

    /**
     * Logs a warning for methods with an unusual number of parameters.
     */
    private void logUnusualParameterCount(int parameterCount) {
        System.out.println("Method contains " + parameterCount + " parameters, which is unusual.");
    }

    /**
     * Validates the method's body within the provided scope.
     */
    private void validateMethodBody(Scope methodScope) {
        body.checkExpression(methodScope);
    }

}
