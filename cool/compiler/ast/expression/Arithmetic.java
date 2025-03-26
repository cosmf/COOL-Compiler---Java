package cool.compiler.ast.expression;

import cool.parser.CoolParser;
import cool.structures.ClassSymbol;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import cool.structures.VariableSymbol;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class Arithmetic extends Expression {
    public enum Op {
        ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("/");

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

    private final CoolParser.ArithmeticContext context;
    private final Op operation;

    private final Expression left, right;

    public Arithmetic(CoolParser.ArithmeticContext context, Op operation, Expression left, Expression right) {
        this.context = context;
        this.operation = operation;
        this.left = left;
        this.right = right;
    }

    @Override
    public ParserRuleContext getContext() {
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
        return SymbolTable.Int;
    }

    @Override
    public void checkTypes(Scope<VariableSymbol> scope) {
        left.checkTypes(scope);
        ensureOperandInt(scope, left, operation.symbol, context.left.start);
        ensureOperandInt(scope, right, operation.symbol, context.right.start);
        right.checkTypes(scope);
    }
}
