package edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.visitor;

import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.DCNode;
import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.RawJavaNode;
import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.RootNode;

public interface ASTVisitor<T> {
    T visit(RawJavaNode node);
    T visit(DCNode node); // Add this line to the interface
    T visit(RootNode node); // Add this line to the interface
}   
