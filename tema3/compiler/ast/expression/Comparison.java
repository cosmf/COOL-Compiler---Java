package cool.compiler.ast.expression;

import cool.parser.CoolParser;
import cool.structures.ClassSymbol;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import cool.structures.VariableSymbol;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class Comparison extends Expression {
    public enum Op {
        LESS("<"), LESS_OR_EQUAL("<="), EQUAL("=");

        private final String symbol;

        Op(String symbol) {
            this.symbol = symbol;
        }

        /**
         * @throws NoSuchElementException If no Op with given symbol exists
         */
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        public static Op findBySymbol(String symbol) {
            return Arrays.stream(values()).filter(op -> op.symbol.equals(symbol)).findFirst().get();
        }
    }

    private final CoolParser.ComparisonContext context;
    private final Op operation;

    private final Expression left, right;

    public Comparison(CoolParser.ComparisonContext context, Op operation, Expression left, Expression right) {
        this.context = context;
        this.operation = operation;
        this.left = left;
        this.right = right;
    }

    @Override
    public CoolParser.ComparisonContext getContext() {
        return context;
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
    public ClassSymbol getExpressionType(Scope<VariableSymbol> scope) {
        return SymbolTable.Bool;
    }

    @Override
    public void checkTypes(Scope<VariableSymbol> scope) {
        left.checkTypes(scope);
        if (operation != Op.EQUAL) {
            ensureOperandInt(scope, left, operation.symbol, context.left.start);
            ensureOperandInt(scope, right, operation.symbol, context.right.start);
        } else {
            ClassSymbol leftType = left.getExpressionType(scope), rightType = right.getExpressionType(scope);
            if (leftType != null && rightType != null && leftType != rightType &&
                    (leftType == SymbolTable.Int || leftType == SymbolTable.Bool || leftType == SymbolTable.String ||
                    rightType == SymbolTable.Int || rightType == SymbolTable.Bool || rightType == SymbolTable.String)) {
                SymbolTable.error(this, context.op, "Cannot compare %s with %s".formatted(leftType.getName(), rightType.getName()));
            }
        }
        right.checkTypes(scope);
    }
}
