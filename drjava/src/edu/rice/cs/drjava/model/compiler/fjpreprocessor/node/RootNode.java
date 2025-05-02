package edu.rice.cs.drjava.model.compiler.fjpreprocessor.node;

import java.util.ArrayList;

import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.visitor.ASTVisitor;

public class RootNode implements ASTNode {
    ArrayList<ASTNode> children = new ArrayList<ASTNode>();
    public String toString() {
        return children.toString();
    }

    public ArrayList<ASTNode> children() {
        return children;
    }

    public void addChild(ASTNode child) {
        children.add(child);
    }

    @Override
    public <T> void accept(ASTVisitor<T> visitor) {
        visitor.visit(this);
    }
}
