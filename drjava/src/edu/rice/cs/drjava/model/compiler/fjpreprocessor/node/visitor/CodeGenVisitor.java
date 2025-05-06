package edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.visitor;

import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.ASTNode;
import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.DCNode;
import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.DCVar;
import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.RawJavaNode;
import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.RootNode;

public class CodeGenVisitor implements ASTVisitor<StringBuilder> {


    private static final CodeGenVisitor INSTANCE = new CodeGenVisitor();
    private CodeGenVisitor() {
        // Private constructor to prevent instantiation
    }

    public static String generate(ASTNode node) {
        StringBuilder output = node.accept(INSTANCE);
        return output.toString();      
    }

    @Override
    public StringBuilder visit(RawJavaNode node) {
        StringBuilder output = new StringBuilder();
        output.append(node.text());
        return output;
    }

    @Override
    public StringBuilder visit(DCNode node) {
        StringBuilder output = new StringBuilder();
        output.append("class ").append(node.name());

        if (!node.typeVars().isEmpty()) {
            output.append("<");
            for (int i = 0; i < node.typeVars().size(); i++) {
                output.append(node.typeVars().get(i));
                if (i < node.typeVars().size() - 1)
                    output.append(", ");
            }
            output.append(">");
        }
        output.append(" {\n");

        // Fields
        for (DCVar var : node.vars()) {
            output.append("    private ").append(var.type()).append(" ")
                    .append(var.name()).append(";\n");
        }
        output.append("\n");

        output.append("/******************************************\\\n");
        output.append("| PREPROCESSOR NOTE: Begin code generation |\n");
        output.append("\\******************************************/\n\n");

        // Constructor
        output.append("    public ").append(node.name()).append("(");
        for (int i = 0; i < node.vars().size(); i++) {
            DCVar var = node.vars().get(i);
            output.append(var.type()).append(" ").append(var.name());
            if (i < node.vars().size() - 1)
                output.append(", ");
        }
        output.append(") {\n");
        for (DCVar var : node.vars()) {
            output.append("        this.").append(var.name())
                    .append(" = ").append(var.name()).append(";\n");
        }
        output.append("    }\n\n");

        // Accessors
        for (DCVar var : node.vars()) {
            output.append("    public ").append(var.type()).append(" ")
                    .append(var.name()).append("() {\n")
                    .append("        return ").append(var.name()).append(";\n")
                    .append("    }\n\n");
        }

        // ToString method
        // if othercode already contains a toString method, we should not generate one
        if (node.otherCode().contains("public String toString()")) {
            output.append("    // PREPROCESSOR NOTE: toString method already defined\n\n");
        } else {
            output.append("    // PREPROCESSOR NOTE: toString method generated\n");
            output.append(generateToStringMethod(node));
        }

        // HashCode method
        // if othercode already contains a hashCode method, we should not generate one
        if (node.otherCode().contains("public int hashCode()")) {
            output.append("    // PREPROCESSOR NOTE: hashCode method already defined\n\n");
        } else {
            output.append("    // PREPROCESSOR NOTE: hashCode method generated\n");
            output.append(generateHashCodeMethod(node));
        }

        // Equals method
        // if othercode already contains a equals method, we should not generate one
        if (node.otherCode().contains("public boolean equals(Object obj)")) {
            output.append("    // PREPROCESSOR NOTE: equals method already defined\n\n");
        } else {
            output.append("    // PREPROCESSOR NOTE: equals method generated\n");
            output.append(generateEqualsMethod(node));
        }

        output.append("/****************************************\\\n");
        output.append("| PREPROCESSOR NOTE: End code generation |\n");
        output.append("\\****************************************/\n\n");

        // Other code

        output.append(node.otherCode());

        output.append("}\n\n");
        return output;
    }

    private StringBuilder generateHashCodeMethod(DCNode node) {
        StringBuilder output = new StringBuilder();
        output.append("    @Override\n")
                .append("    public int hashCode() {\n")
                .append("        return java.util.Objects.hash(");
        for (int i = 0; i < node.vars().size(); i++) {
            DCVar var = node.vars().get(i);
            output.append(var.name());
            if (i < node.vars().size() - 1)
                output.append(", ");
        }
        output.append(");\n")
                .append("    }\n\n");
        return output;
    }

    private StringBuilder generateEqualsMethod(DCNode node) {
        StringBuilder output = new StringBuilder();
        output.append("    @Override\n")
                .append("    public boolean equals(Object obj) {\n")
                .append("        if (this == obj) return true;\n")
                .append("        if (obj == null || getClass() != obj.getClass()) return false;\n")
                .append("        ").append(node.name()).append(" other = (").append(node.name()).append(") obj;\n")
                .append("        return ");

        for (int i = 0; i < node.vars().size(); i++) {
            DCVar var = node.vars().get(i);
            output.append(var.name()).append(".equals(other.").append(var.name()).append(")");
            if (i < node.vars().size() - 1)
                output.append(" && ");
        }

        output.append(";\n    }\n\n");
        return output;
    }

    private StringBuilder generateToStringMethod(DCNode node) {
        StringBuilder output = new StringBuilder();
        output.append("    @Override\n")
                .append("    public String toString() {\n")
                .append("        return \"").append(node.name()).append("(\" + ");
        for (int i = 0; i < node.vars().size(); i++) {
            DCVar var = node.vars().get(i);
            output.append(var.name());
            if (i < node.vars().size() - 1)
                output.append(" + \", \" + ");
        }
        output.append(" + \")\";\n")
                .append("    }\n\n");
        return output;
    }

    @Override
    public StringBuilder visit(RootNode node) {
        StringBuilder output = new StringBuilder();
        for (ASTNode child : node.children()) {
            output.append(child.accept(this));
        }
        return output;
    }
}
