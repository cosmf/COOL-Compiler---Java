package cool.compiler.ast;

import cool.compiler.Constants;
import cool.parser.CoolParser;
import cool.structures.*;
import org.antlr.v4.runtime.Token;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class Comparison extends Expression {

    public enum Operation {
        LESS("<"), LESS_OR_EQUAL("<="), EQUAL("=");

        private final String symbol;

        Operation(String symbol) {
            this.symbol = symbol;
        }

        /**
         * @throws NoSuchElementException If no Operation with given symbol exists
         */
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        public static Operation findBySymbol(String symbol) {
            return Arrays.stream(values())
                    .filter(op -> op.symbol.equals(symbol))
                    .findFirst()
                    .get();
        }
    }

    private final Operation operation;
    private final Expression left, right;
    private final CoolParser.ComparisonContext ctx;

    public Comparison(Token token,
                      Operation operation,
                      Expression left,
                      Expression right,
                      CoolParser.ComparisonContext ctx) {
        super(token);
        this.operation = operation;
        this.left = left;
        this.right = right;
        this.ctx = ctx;
    }

    @Override
    protected void printTitle() {
        print(operation.symbol);
    }

    @Override
    protected void printChildren() {
        print(left);
        print(right);
    }

    /**
     * Helper to check whether we're comparing Int/Bool/String 
     * to a different type from the same set, which is disallowed.
     */
    private boolean checkEqualClasses(String leftType, String rightType) {
        // True if left is in {Int, String, Bool} AND left != right
        return (("Int".equals(leftType) || "String".equals(leftType) || "Bool".equals(leftType))
                && !leftType.equals(rightType));
    }

    /**
     * Evaluates left and right expressions, then applies 
     * the relevant comparison rules.
     */
    @Override
    public Class_c checkExpression(Scope scope) {
        // 1) Evaluate both sides
        var leftExpClass = left.checkExpression(scope);
        var rightExpClass = right.checkExpression(scope);

        // 2) If operation is '=', handle that separately
        if ("=".equals(operation.symbol)) {
            return handleEqualityCheck(leftExpClass, rightExpClass);
        }

        // 3) Otherwise, it's < or <=, so both must be Int
        return handleLessThanChecks(leftExpClass, rightExpClass);
    }

    /**
     * For the '=' operation, ensure that if either type is Int/String/Bool, 
     * the other is the same. If not, log an error.
     */
    private Class_c handleEqualityCheck(Class_c leftExpClass, Class_c rightExpClass) {
        var leftType = leftExpClass.getName();
        var rightType = rightExpClass.getName();

        // If mismatched basic types, error
        if (checkEqualClasses(leftType, rightType)
                || checkEqualClasses(rightType, leftType)) {
            SymbolTable.error(
                ctx,
                ctx.EQ().getSymbol(),
                String.format("Cannot compare %s with %s", leftType, rightType)
            );
        }
        // By COOL semantics, no well-defined type => return null
        return null;
    }

    /**
     * For < or <=, both sides must be Int. 
     * If not, log errors and return null.
     */
    private Class_c handleLessThanChecks(Class_c leftExpClass, Class_c rightExpClass) {
        if (!"Int".equals(leftExpClass.getName())) {
            SymbolTable.error(
                ctx,
                ctx.left.start,
                String.format("Operand of %s has type %s instead of Int",
                              operation.symbol, leftExpClass.getName())
            );
            return null;
        }
        if (!"Int".equals(rightExpClass.getName())) {
            SymbolTable.error(
                ctx,
                ctx.right.start,
                String.format("Operand of %s has type %s instead of Int",
                              operation.symbol, rightExpClass.getName())
            );
            return null;
        }

        // If everything checks out, we return Bool
        return Constants.BOOL_CLASS_C;
    }
}
