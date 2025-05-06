package edu.rice.cs.drjava.model.compiler.fjpreprocessor.node;
import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.visitor.ASTVisitor;

public class RawJavaNode implements ASTNode {
    String text;
    public RawJavaNode(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

    public String toString() {
        return text;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}