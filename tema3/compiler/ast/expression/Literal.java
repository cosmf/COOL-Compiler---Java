package cool.compiler.ast.expression;

import cool.structures.ClassSymbol;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import cool.structures.VariableSymbol;
import org.antlr.v4.runtime.ParserRuleContext;

public class Literal extends Expression {
    public enum Type {
        INTEGER, STRING, BOOLEAN
    }

    private final ParserRuleContext context;
    private final Type type;
    private final String content;

    public Literal(ParserRuleContext context, Type type, String content) {
        this.context = context;
        this.type = type;
        this.content = content;
    }

    @Override
    public ParserRuleContext getContext() {
        return context;
    }

    public Type getType() {
        return type;
    }

    @Override
    protected void printTitle() {
        print(content);
    }

    @Override
    protected void printChildren() {
    }

    public String getContent() {
        return content;
    }

    @Override
    public ClassSymbol getExpressionType(Scope<VariableSymbol> scope) {
        return switch (type) {
            case INTEGER -> SymbolTable.Int;
            case STRING -> SymbolTable.String;
            case BOOLEAN -> SymbolTable.Bool;
        };
    }

    @Override
    public void checkTypes(Scope<VariableSymbol> scope) {
    }
}
