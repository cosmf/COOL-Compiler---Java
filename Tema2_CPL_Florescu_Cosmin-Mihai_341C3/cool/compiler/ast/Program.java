package cool.compiler.ast;

import org.antlr.v4.runtime.Token;

import java.util.List;

public class Program extends ASTNode {
    private final List<Class_c> classes;

    public Program(Token token, List<Class_c> classes) {
        super(token);
        this.classes = classes;
    }

    public List<Class_c> getClasses() {
        return classes;
    }

    @Override
    protected void printTitle() {
        print("program");
    }

    @Override
    protected void printChildren() {
        print(classes);
    }
}
