package edu.rice.cs.drjava.model.compiler.fjpreprocessor.node;

import java.util.ArrayList;

import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.visitor.ASTVisitor;
import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.DCVar;

public class DCNode implements ASTNode {
    String name;
    ArrayList<String> typeVars = new ArrayList<String>();
    ArrayList<DCVar> vars = new ArrayList<DCVar>();
    StringBuilder sb = new StringBuilder();

    public DCNode(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public ArrayList<DCVar> vars() {
        return vars;
    }

    public ArrayList<String> typeVars() {
        return typeVars;
    }

    public void addTypeVar(String typeVar) {
        typeVars.add(typeVar);
    }

    public String otherCode() {
        return sb.toString();
    }

    public void addVar(DCVar var) {
        vars.add(var);
    }

    public void addOther(String other) {
        sb.append(other);
    }

    public String toString() {
        return "data-class " + name + " " + vars.toString();
    }
    public <T> void accept(ASTVisitor<T> visitor) {
        visitor.visit(this);
    }
}
