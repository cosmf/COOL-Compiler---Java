package cool.compiler.ast;

import cool.parser.CoolParser;

import java.util.List;

public class Program extends ASTNode {
    private final CoolParser.ProgramContext context;
    private final List<PClass> classes;

    public Program(CoolParser.ProgramContext context, List<PClass> classes) {
        this.context = context;
        this.classes = classes;
    }

    @Override
    public CoolParser.ProgramContext getContext() {
        return context;
    }

    public List<PClass> getClasses() {
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
