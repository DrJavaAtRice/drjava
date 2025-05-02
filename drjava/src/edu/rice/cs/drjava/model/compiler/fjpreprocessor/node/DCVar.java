package edu.rice.cs.drjava.model.compiler.fjpreprocessor.node;


public class DCVar {
    String type;
    String name;

    public DCVar(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String type() {
        return type;
    }

    public String name() {
        return name;
    }
}