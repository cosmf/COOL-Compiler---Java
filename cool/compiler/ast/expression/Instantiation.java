package cool.compiler.ast.expression;

import cool.parser.CoolParser;
import cool.structures.ClassSymbol;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import cool.structures.VariableSymbol;

public class Instantiation extends Expression {
    private final CoolParser.InstantiationContext context;
    private final String type;

    public Instantiation(CoolParser.InstantiationContext context, String type) {
        this.context = context;
        this.type = type;
    }

    @Override
    public CoolParser.InstantiationContext getContext() {
        return context;
    }

    @Override
    protected void printTitle() {
        print("new");
    }

    @Override
    protected void printChildren() {
        print(type);
    }

    @Override
    public ClassSymbol getExpressionType(Scope<VariableSymbol> scope) {
        return SymbolTable.lookupClass(type);
    }

    @Override
    public void checkTypes(Scope<VariableSymbol> scope) {
        if (SymbolTable.lookupClass(type) == null)
            SymbolTable.error(this, context.TYPE().getSymbol(),
                    "new is used with undefined type %s".formatted(this.type));
    }
}
