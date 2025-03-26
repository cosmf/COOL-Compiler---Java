package cool.compiler.ast;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

public abstract class ASTNode {
    public abstract ParserRuleContext getContext();

    // Printing stuff
    private int indent;

    public void printTree() {
        printTree(0);
    }

    protected void printTree(int indent) {
        this.indent = indent;
        printTitle();
        ++this.indent;
        printChildren();
    }

    protected abstract void printTitle();

    protected abstract void printChildren();

    // Print helpers
    protected <T extends ASTNode> void print(List<T> nodes) {
        for (var node : nodes) node.printTree(indent);
    }

    protected void print(ASTNode node) {
        node.printTree(indent);
    }

    protected void print(String message) {
        for (int i = 0; i < indent; ++i) System.out.print("  ");
        System.out.println(message);
    }
}
