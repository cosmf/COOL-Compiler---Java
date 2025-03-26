package cool.compiler.ast;

import cool.structures.Scope;
import org.antlr.v4.runtime.Token;

public abstract class Expression extends ASTNode {
    protected Expression(Token token) {
        super(token);
    }

    public abstract Class_c checkExpression(Scope scope);
}
