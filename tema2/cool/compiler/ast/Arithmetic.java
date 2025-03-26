package cool.compiler.ast;

import cool.compiler.Constants;
import cool.parser.CoolParser;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import org.antlr.v4.runtime.Token;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class Arithmetic extends Expression {

    public enum Operation {
        ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("/");

        private final String symbol;

        Operation(String symbol) {
            this.symbol = symbol;
        }

        /**
         * @throws NoSuchElementException If no Operation with the given symbol exists
         */
        public static Operation findBySymbol(String symbol) {
            for (Operation op : values()) {
                if (op.symbol.equals(symbol)) {
                    return op;
                }
            }
            throw new NoSuchElementException("No Operation with symbol: " + symbol);
        }
    }

    private final Operation operation;
    private final Expression left, right;

    private final CoolParser.ArithmeticContext ctx;

    public Arithmetic(Token token, Operation operation, Expression left, Expression right, CoolParser.ArithmeticContext ctx) {
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
    
    @Override
    public Class_c checkExpression(Scope scope) {
        // Evaluate the right operand
        if (!validateOperand(right, scope, true)) {
            // Evaluate left to catch additional errors
            left.checkExpression(scope);
            return null;
        }

        // Evaluate the left operand
        if (!validateOperand(left, scope, false)) {
            return null;
        }

        // If both operands are valid, return Int
        return getIntType();
    }

    // Helper function to validate an operand
    private boolean validateOperand(Expression operand, Scope scope, boolean isRight) {
        Class_c operandType = operand.checkExpression(scope);

        // Check for non-null and non-Int types
        if (operandType != null && !"Int".equals(operandType.getName())) {
            reportOperandError(operandType, isRight);
            return false;
        }
        return true;
    }

    // Helper function to report operand errors
    private void reportOperandError(Class_c type, boolean isRight) {
        Token token = isRight ? ctx.right.start : ctx.left.start;
        String operandPosition = isRight ? "right" : "left";

        SymbolTable.error(
            ctx,
            token,
            "Operand of %s has type %s instead of Int".formatted(operation.symbol, type.getName())
        );
    }

    // Helper function to get the Int type
    private Class_c getIntType() {
        return Constants.INT_CLASS_C;
    }
}
