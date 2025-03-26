package cool.compiler.ast.expression;

import cool.compiler.ast.ASTNode;
import cool.parser.CoolParser;
import cool.structures.*;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class MethodCall extends Expression {
    private final CoolParser.MethodCallContext context;
    private final Expression targetObject;
    private final String targetType, name;
    private final List<Expression> arguments;

    public MethodCall(CoolParser.MethodCallContext context, Expression targetObject, String targetType, String name, List<Expression> arguments) {
        this.context = context;
        this.targetObject = targetObject;
        this.targetType = targetType;
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public CoolParser.MethodCallContext getContext() {
        return context;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    public Expression getTargetObject() {
        return targetObject;
    }

    @Override
    protected void printTitle() {
        print(".");
    }

    public String getName() {
        return name;
    }

    @Override
    protected void printChildren() {
        print(targetObject);
        if (targetType != null) print(targetType);
        print(name);
        print(arguments);
    }

    @Override
    public ClassSymbol getExpressionType(Scope<VariableSymbol> scope) {
        MethodSymbol method;
        // Resolve method
        ClassSymbol cls = targetObject.getExpressionType(scope), targetCls;
        if (cls == null) return null;

        if (targetType != null) targetCls = SymbolTable.lookupClass(targetType);
        else targetCls = cls == SymbolTable.SelfType ? scope.getCurrentClass() : cls;

        if (targetCls == null) return null;
        method = targetCls.getMethodScope().lookup(name);

        if (method == null) return null;
        return method.getReturnType(cls);
    }

    @Override
    public void checkTypes(Scope<VariableSymbol> scope) {
        targetObject.checkTypes(scope);
        ClassSymbol cls = targetObject.getExpressionType(scope);
        if (cls == null) return;

        // Resolve method
        ClassSymbol targetCls;
        if (targetType != null) {
            // Static dispatch
            if (targetType.equals("SELF_TYPE")) {
                SymbolTable.error(this, context.TYPE().getSymbol(),
                        "Type of static dispatch cannot be SELF_TYPE");
                return;
            }
            targetCls = SymbolTable.lookupClass(targetType);
            if (targetCls == null) {
                SymbolTable.error(this, context.TYPE().getSymbol(),
                        "Type %s of static dispatch is undefined".formatted(targetType));
                return;
            } else if (!cls.canBeAssignedTo(targetCls, scope.getCurrentClass())) {
                SymbolTable.error(this, context.TYPE().getSymbol(),
                        "Type %s of static dispatch is not a superclass of type %s".formatted(targetType, cls.getName()));
                return;
            }
        } else {
            // Dynamic dispatch
            targetCls = cls == SymbolTable.SelfType ? scope.getCurrentClass() : cls;
        }

        checkMethodCall(scope, targetCls, name, arguments, this, context.ID().getSymbol());
    }

    static void checkMethodCall(Scope<VariableSymbol> scope, ClassSymbol cls, String name, List<Expression> arguments, ASTNode node, Token token) {
        for (var arg : arguments) arg.checkTypes(scope);

        MethodSymbol method = cls.getMethodScope().lookup(name);
        if (method == null)
            SymbolTable.error(node, token, "Undefined method %s in class %s".formatted(name, cls.getName()));
        else if (method.getFormals().size() != arguments.size())
            SymbolTable.error(node, token, "Method %s of class %s is applied to wrong number of arguments".formatted(name, cls.getName()));
        else {
            // Check types of all formals
            for (int i = 0; i < arguments.size(); ++i) {
                var arg = arguments.get(i);
                var formal = method.getFormals().get(i);
                ClassSymbol argType = arg.getExpressionType(scope);
                ClassSymbol formalType = formal.getType();
                if (argType == null || formalType == null) continue;
                if (!argType.canBeAssignedTo(formalType, scope.getCurrentClass()))
                    SymbolTable.error(node, arg.getContext().start,
                            "In call to method %s of class %s, actual type %s of formal parameter %s is incompatible with declared type %s"
                                    .formatted(name, cls.getName(), argType.getName(), formal.getName(), formalType.getName()));
            }
        }
    }
}
