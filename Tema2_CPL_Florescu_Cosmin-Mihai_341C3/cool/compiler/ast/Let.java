package cool.compiler.ast;

import cool.structures.*;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class Let extends Expression {
    private final List<LetLocal> locals;
    private final Expression body;

    public Let(Token token, List<LetLocal> locals, Expression body) {
        super(token);
        this.locals = locals;
        this.body = body;
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

    /**
     * Creates a new scope for a single let-local definition.
     * Introduces a symbol if the type is found in the global table.
     */
    private Scope createNewScope(Scope scope, LetLocal local) {
        Scope freshScope = initializeScope(scope);
        ClassSymbol localClass = fetchClassSymbol(local);

        if (localClass != null) {
            addSymbolToScope(freshScope, local, localClass);
        }

        return freshScope;
    }

    private Scope initializeScope(Scope parentScope) {
        return new DefaultScope(parentScope);
    }

    // Helper function to fetch the class symbol from the global table
    private ClassSymbol fetchClassSymbol(LetLocal local) {
        return (ClassSymbol) SymbolTable.globals.lookup(local.getType());
    }

    // Helper function to add a symbol to the scope
    private void addSymbolToScope(Scope freshScope, LetLocal local, ClassSymbol localClass) {
        freshScope.add(new Symbol(local.getId(), localClass.getpClass()));
    }

    @Override
    public Class_c checkExpression(Scope scope) {
        // We accumulate new scopes for each let-local definition.
        var currentScope = scope;

        // For each local declaration, verify its type and update scope.
        for (var local : locals) {
            local.checkType(currentScope);
            currentScope = createNewScope(currentScope, local);
        }

        // After defining all locals, check the expression in the final scope.
        // In many COOL semantics, the type of the 'let' is the type of the body.
        var bodyType = body.checkExpression(currentScope);

        return bodyType;
    }
}
