package cool.structures;

import java.util.List;

public class ClassSymbol extends Symbol {
    private ClassSymbol parent;
    private final Scope<VariableSymbol> attributeScope = new DefaultScope<>(this);
    private final Scope<MethodSymbol> methodScope = new DefaultScope<>(this);

    public ClassSymbol(String name) {
        this(name, SymbolTable.Object);
        attributeScope.add(new VariableSymbol("self", SymbolTable.SelfType, null));
    }

    ClassSymbol(String name, ClassSymbol parent) {
        super(name);
        setParent(parent);
    }

    public Scope<VariableSymbol> getAttributeScope() {
        return attributeScope;
    }

    public Scope<MethodSymbol> getMethodScope() {
        return methodScope;
    }

    public ClassSymbol getParent() {
        return parent;
    }

    public void setParent(ClassSymbol parent) {
        this.parent = parent;
        if (parent != null) {
            attributeScope.setParent(parent.getAttributeScope());
            methodScope.setParent(parent.getMethodScope());
        } else {
            attributeScope.setParent(null);
            methodScope.setParent(null);
        }
    }

    public boolean isSuperTypeOf(ClassSymbol cls) {
        ClassSymbol c = cls;
        while (c != null) {
            if (c == this) return true;
            c = c.parent;
        }
        return false;
    }

    public boolean canBeAssignedTo(ClassSymbol varType, ClassSymbol currentClass) {
        if (this == varType) return true;
        // SELF_TYPE can be assigned to variables of the current class type
        // Nothing other than SELF_TYPE can be assigned to SELF_TYPE (this is covered by else condition)
        return this == SymbolTable.SelfType ? varType.isSuperTypeOf(currentClass) : varType.isSuperTypeOf(this);
    }

    public static ClassSymbol joinTypes(ClassSymbol first, ClassSymbol second, Scope<?> scope) {
        // Handle SELF_TYPE
        if (first == second) return first;
        if (first == SymbolTable.SelfType) first = scope.getCurrentClass();
        else if (second == SymbolTable.SelfType) first = scope.getCurrentClass();
        // Find common ancestor
        while (!first.isSuperTypeOf(second)) first = first.parent;
        return first;
    }

    public static ClassSymbol joinTypes(List<ClassSymbol> classes, Scope<?> scope) {
        if (classes.isEmpty()) return SymbolTable.Object;
        ClassSymbol type = classes.get(0);
        for (int i = 1; i < classes.size(); ++i) type = joinTypes(type, classes.get(i), scope);
        return type;
    }
}
