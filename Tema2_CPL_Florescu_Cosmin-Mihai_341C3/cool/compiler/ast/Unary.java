package cool.compiler.ast;

import cool.parser.CoolParser;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import org.antlr.v4.runtime.Token;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class Unary extends Expression {

    public enum Operation {
        COMPLEMENT("~"), NOT("not");

        private final String symbol;

        Operation(String symbol) {
            this.symbol = symbol;
        }

        /**
         * Finds the matching unary operation by symbol.
         * @throws NoSuchElementException if no operation matches.
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
    private final Expression target;
    private final CoolParser.UnaryContext ctxu;   // for '~'
    private final CoolParser.NegateContext ctxn;  // for 'not'

    public Unary(Token token,
                 Operation operation,
                 Expression target,
                 CoolParser.UnaryContext ctx) {
        super(token);
        this.operation = operation;
        this.target = target;
        this.ctxu = ctx;
        this.ctxn = null;
    }

    public Unary(Token token,
                 Operation operation,
                 Expression target,
                 CoolParser.NegateContext ctx) {
        super(token);
        this.operation = operation;
        this.target = target;
        this.ctxn = ctx;
        this.ctxu = null;
    }

    @Override
    protected void printTitle() {
        print(operation.symbol);
    }

    @Override
    protected void printChildren() {
        print(target);
    }

    @Override
    public Class_c checkExpression(Scope scope) {
        // Evaluate the target's type.
        var targetClass = target.checkExpression(scope);
        if (targetClass == null) {
            return null;
        }

        // Branch logic based on which unary op we have.
        if (operation == Operation.COMPLEMENT) {
            return handleComplement(targetClass);
        } else {
            return handleNot(targetClass);
        }
    }

    /**
     * Handles the '~' (COMPLEMENT) operation, which must have Int as operand.
     */
    private Class_c handleComplement(Class_c targetClass) {
        // If we have a unary context ctxu, check that type is "Int"
        if (!"Int".equals(targetClass.getName()) && ctxu != null) {
            SymbolTable.error(
                ctxu,
                ctxu.expr().start,
                String.format("Operand of %s has type %s instead of Int",
                              operation.symbol, targetClass.getName())
            );
            return null;
        }
        // If type is Int, we just return the same type.
        return targetClass;
    }

    /**
     * Handles the 'not' operation, which must have Bool as operand.
     */
    private Class_c handleNot(Class_c targetClass) {
        // If this is a negate context ctxn, check that type is "Bool"
        if (!"Bool".equals(targetClass.getName()) && ctxn != null) {
            SymbolTable.error(
                ctxn,
                ctxn.expr().start,
                String.format("Operand of not has type %s instead of Bool",
                              targetClass.getName())
            );
            return null;
        }
        // If type is Bool, we return the same type.
        return targetClass;
    }
}
