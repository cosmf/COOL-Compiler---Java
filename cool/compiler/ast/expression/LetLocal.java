package cool.compiler.ast.expression;

import cool.compiler.ast.ASTNode;
import cool.parser.CoolParser;
import cool.structures.ClassSymbol;
import cool.structures.Scope;
import cool.structures.SymbolTable;
import cool.structures.VariableSymbol;

public class LetLocal extends ASTNode {
    private final CoolParser.LocalContext context;
    private final String id;
    private final String type;
    private final Expression initializer;

    public LetLocal(CoolParser.LocalContext context, String id, String type, Expression initializer) {
        this.context = context;
        this.id = id;
        this.type = type;
        this.initializer = initializer;
    }

    @Override
    public CoolParser.LocalContext getContext() {
        return context;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Expression getInitializer() {
        return initializer;
    }

    @Override
    protected void printTitle() {
        print("local");
    }

    @Override
    protected void printChildren() {
        print(id);
        print(type);
        if (initializer != null) print(initializer);
    }

    public void checkTypes(Scope<VariableSymbol> scope) {
        if (id.equals("self")) {
            SymbolTable.error(this, context.ID().getSymbol(), "Let variable has illegal name self");
            return;
        }

        ClassSymbol declaredType = SymbolTable.lookupClass(type);

        if (declaredType == null) {
            SymbolTable.error(this, context.TYPE().getSymbol(),
                    "Let variable %s has undefined type %s".formatted(id, type));
        }

        if (initializer == null) return;
        initializer.checkTypes(scope);

        ClassSymbol expressionType = initializer.getExpressionType(scope);
        if (declaredType == null || expressionType == null) return;

        if (!expressionType.canBeAssignedTo(declaredType, scope.getCurrentClass()))
            SymbolTable.error(this, context.expr().start,
                    "Type %s of initialization expression of identifier %s is incompatible with declared type %s"
                            .formatted(expressionType.getName(), id, declaredType.getName()));
    }
}
