package cool.compiler.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;

public abstract class ASTNode {
    protected final Token token;

    public ASTNode(Token token) {
        this.token = token;
    }

    private int indent;

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
}
