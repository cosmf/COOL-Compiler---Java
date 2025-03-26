package cool.compiler.ast;

import cool.compiler.Constants;
import cool.parser.CoolParser;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import org.antlr.v4.runtime.Token;

/**
 * Represents a 'while' expression in Cool.
 * The condition must be Bool, and the loop's overall type is Object.
 */
public class While extends Expression {
    private final Expression condition, body;
    private final CoolParser.WhileContext ctx;

    public While(Token token,
                 Expression condition,
                 Expression body,
                 CoolParser.WhileContext ctx) {
        super(token);
        this.condition = condition;
        this.body = body;
        this.ctx = ctx;
    }

    @Override
    protected void printTitle() {
        print("while");
    }

    @Override
    protected void printChildren() {
        print(condition);
        print(body);
    }

    /**
     * Checks the condition for Bool, then returns Object per Cool semantics.
     * We do not evaluate the body in this snippet, matching the original code.
     */
    @Override
    public Class_c checkExpression(Scope scope) {
        var conditionType = evaluateWhileCondition(scope);

        return Constants.OBJECT_CLASS_C;
    }

    /**
     * A helper method that checks the while condition's type
     * and reports an error if it's not Bool.
     */
    private Class_c evaluateWhileCondition(Scope scope) {
        // Evaluate the condition expression
        var condClass = condition.checkExpression(scope);

        // If condition is not Bool, emit an error
        if (!"Bool".equals(condClass.getName())) {
            SymbolTable.error(
                ctx,
                ctx.expr(0).start,
                String.format(
                    "While condition has type %s instead of Bool",
                    condClass.getName()
                )
            );
        }
        return condClass;
    }
}
