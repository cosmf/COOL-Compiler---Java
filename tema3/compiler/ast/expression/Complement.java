package cool.compiler.ast.expression;

import cool.parser.CoolParser;
import cool.structures.ClassSymbol;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import cool.structures.VariableSymbol;

public class Complement extends Expression {
    private final CoolParser.UnaryContext context;
    private final Expression operand;

    public Complement(CoolParser.UnaryContext context, Expression operand) {
        this.context = context;
        this.operand = operand;
    }

    @Override
    public CoolParser.UnaryContext getContext() {
        return context;
    }

    public Expression getOperand() {
        return operand;
    }

    @Override
    protected void printTitle() {
        print("~");
    }

    @Override
    protected void printChildren() {
        print(operand);
    }

    @Override
    public ClassSymbol getExpressionType(Scope<VariableSymbol> scope) {
        return SymbolTable.Int;
    }

    @Override
    public void checkTypes(Scope<VariableSymbol> scope) {
        ensureOperandInt(scope, operand, "~", context.expr().start);
        operand.checkTypes(scope);
    }
}
