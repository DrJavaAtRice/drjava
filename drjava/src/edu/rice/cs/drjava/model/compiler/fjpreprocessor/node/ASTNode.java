package edu.rice.cs.drjava.model.compiler.fjpreprocessor.node;

import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.visitor.ASTVisitor;

public interface ASTNode {
    <T> T accept(ASTVisitor<T> visitor);  
} 
