package cool.compiler.ast;

import cool.compiler.Constants;
import cool.parser.CoolParser;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import org.antlr.v4.runtime.Token;

public class If extends Expression {
    private final Expression condition, thenBranch, elseBranch;
    private final CoolParser.IfContext ctx;

    public If(Token token,
              Expression condition,
              Expression thenBranch,
              Expression elseBranch,
              CoolParser.IfContext ctx) {
        super(token);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
        this.ctx = ctx;
    }

    @Override
    protected void printTitle() {
        print("if");
    }

    @Override
    protected void printChildren() {
        print(condition);
        print(thenBranch);
        print(elseBranch);
    }

    @Override
    public Class_c checkExpression(Scope scope) {
        // First, we check the condition expression
        var condType = condition.checkExpression(scope);

        // If condition isn't Bool, report an error
        if (condType != null && !"Bool".equals(condType.getName())) {
            SymbolTable.error(
                ctx,
                ctx.expr(0).start,
                String.format("If condition has type %s instead of Bool", condType.getName())
            );
        }

        // Evaluate both branches so we catch any further errors
        var thenType = thenBranch.checkExpression(scope);
        var elseType = elseBranch.checkExpression(scope);

        // If one of them is null, can't unify
        if (thenType == null || elseType == null) {
            return null;
        }

        // The result is the nearest common parent of thenType and elseType
        return elseType.findCommonParent(thenType);
    }
}
