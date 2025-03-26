package cool.structures;

import cool.compiler.ast.Class_c;

public class ClassSymbol extends Symbol{
    private Class_c class_c;

    public ClassSymbol(String name, Class_c class_c) {
        super(name, class_c);
        this.class_c = class_c;
    }

    public Class_c getpClass() {
        return class_c;
    }
}
