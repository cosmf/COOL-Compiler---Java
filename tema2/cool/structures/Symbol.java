package cool.structures;

import cool.compiler.ast.Class_c;

public class Symbol {
    protected String name;
    protected Class_c typeClass;

    public Symbol(String name, Class_c typeClass) {
        this.name = name;
        this.typeClass = typeClass;
    }

    public String getName() {
        return name;
    }

    public Class_c getTypeClass() {
        return typeClass;
    }

    @Override
    public String toString() {
        return getName() + " " + typeClass.getName();
    }

}
