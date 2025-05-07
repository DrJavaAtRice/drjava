package edu.rice.cs.drjava.model.compiler.fjpreprocessor;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import org.antlr.v4.runtime.*;

import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.ASTNode;
import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.DCNode;
import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.DCVar;
import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.RawJavaNode;
import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.RootNode;
import edu.rice.cs.drjava.model.compiler.fjpreprocessor.node.visitor.CodeGenVisitor;

import edu.rice.cs.drjava.model.compiler.fjpreprocessor.grammar.JavaLexer;

import edu.rice.cs.drjava.model.DJError;

public class Preprocessor {

    /**
     * Preprocess a list of files, for each .fjava file, create a .java file with
     * the same name.
     * The .java file will be created in the same directory as the .fjava file.
     * The .java file will contain the preprocessed code.
     * The .fjava file will not be modified.
     * 
     * @param files
     * @throws Exception
     */
    public static LinkedList<DJError> preprocessList(List<File> files) {
        LinkedList<DJError> errors = new LinkedList<DJError>();
        for (File file : files) {
            if (file.getName().endsWith(".fjava")) {
                File outputFile = new File(file.getAbsolutePath().replace(".fjava", ".java"));
                DJError e = doPreprocess(file, outputFile);
                if (e != null) {
                    errors.add(e);
                }
                continue;
            }
            if (file.getName().endsWith(".java")) {
                // Check if the file is a .java file and not a .fjava file
                // If it is a .java file, we can ignore it
                continue;
            }

            errors.add(new DJError(
                    "File " + file.getAbsolutePath() + " is a languagle level file which is no longer supported.",
                    false));
        }
        return errors;
    }

    static DJError doPreprocess(File inputFile, File outputFile) {
        CharStream input;
        try {
            input = CharStreams.fromPath(inputFile.toPath());
        } catch (Exception e) {
            return new DJError("Error reading file: " + inputFile.getAbsolutePath() + "e: " + e, false);
        }
        ASTNode ast = FJPreprocessor.process(input);
        String output = CodeGenVisitor.generate(ast);
        try (Writer writer = new FileWriter(outputFile)) {
            writer.write(output.toString());
        } catch (Exception e) {
            return new DJError("Error writing to file: " + outputFile.getAbsolutePath(), false);
        }
        return null; // No errors
    }

}

class FJPreprocessor {

    private final BufferedLexer lexer;
    private final StringBuilder result;
    private final ArrayList<String> lastThree;
    private final RootNode root;

    private FJPreprocessor(CharStream input) {
        this.lexer = new BufferedLexer(input);
        this.result = new StringBuilder();
        this.lastThree = new ArrayList<>();
        this.root = new RootNode();
    }

    public static ASTNode process(CharStream input) {
        FJPreprocessor pre = new FJPreprocessor(input);
        pre.run();
        return pre.root;
    }

    private void run() {
        Token token = lexer.nextToken();
        while (token.getType() != Token.EOF) {
            String text = token.getText();

            // Update the rolling buffer of the last three token texts
            lastThree.add(text);
            if (lastThree.size() > 3) {
                lastThree.remove(0);
            }

            if (isDataClassStart()) {
                // Parse a data class
                result.setLength(result.length() - 5); // Clear the buffer
                root.addChild(new RawJavaNode(result.toString()));
                result.setLength(0); // Clear the buffer
                DCNode dcNode = parseDataClass();
                root.addChild(dcNode);

                // advance the loop
                token = lexer.nextToken();
                // Reset the rolling buffer
                lastThree.clear();
                continue;
            }

            // Otherwise, just append it to a raw node buffer
            result.append(text);
            if (text.matches("[;{}]")) {
                // Finalize current raw node at structural break
                root.addChild(new RawJavaNode(result.toString()));
                result.setLength(0);
            }

            token = lexer.nextToken();
        }

        // Flush any remaining text
        if (result.length() > 0) {
            root.addChild(new RawJavaNode(result.toString()));
        }
    }

    private boolean isDataClassStart() {
        if (lastThree.size() != 3)
            return false;
        return lastThree.get(0).equals("data") &&
                lastThree.get(1).equals("-") &&
                lastThree.get(2).equals("class");
    }

    private Boolean isField() {
        Token[] nextFour = lexer.peekN(3, false);
        if (nextFour[2].getType() != JavaLexer.SEMI)
            return false;
        return true;
    }

    private DCNode parseDataClass() {
        // Skip the "data - class" tokens
        Token nameToken = lexer.nextTokenSkipWS();
        String className = nameToken.getText();
        DCNode node = new DCNode(className);

        // Expect opening brace
        Token brace = lexer.nextTokenSkipWS();

        if (brace.getText().equals("<")) {
            // Parse type variables
            while (true) {
                String typeVar = lexer.nextTokenSkipWS().getText();

                Token next = lexer.nextTokenSkipWS();

                if (next.getText().equals("extends")) {
                    String type = lexer.nextTokenSkipWS().getText();
                    node.addTypeVar(typeVar + " extends " + type);
                    next = lexer.nextTokenSkipWS();
                } else if (next.getText().equals("super")) {
                    String type = lexer.nextTokenSkipWS().getText();
                    node.addTypeVar(typeVar + " super " + type);
                    next = lexer.nextTokenSkipWS();
                } else {
                    node.addTypeVar(typeVar);
                }

                if (next.getText().equals(">"))
                    break;

                if (!next.getText().equals(",")) {
                    throw new RuntimeException("Expected ',' or '>' after type variable");
                }

            }
            brace = lexer.nextTokenSkipWS(); // Expecting '{' after type variables
        }

        if (!brace.getText().equals("{")) {
            throw new RuntimeException("Expected '{' after data-class name");
        }

        // Parse inside the data-class block
        while (true) {
            Token next = lexer.nextTokenSkipWS();
            if (next.getType() == Token.EOF) {
                throw new RuntimeException("Unexpected EOF inside data-class block");
            }
            if (next.getText().equals("}"))
                break;

            if (!isField()) {
                int braceId = 0;
                Token nextToken = next;
                while (true) {
                    node.addOther(nextToken.getText());
                    if (nextToken.getText().equals("{")) {
                        braceId++;
                    } else if (nextToken.getText().equals("}")) {
                        braceId--;
                        if (braceId == 0) {
                            node.addOther("\n");
                            break;
                        }
                    }

                    nextToken = lexer.nextToken();
                }
                continue;
            }

            String type = next.getText();
            Token varNameToken = lexer.nextTokenSkipWS();
            String varName = varNameToken.getText();

            // Expect semicolon
            Token semi = lexer.nextTokenSkipWS();
            if (!semi.getText().equals(";")) {
                throw new RuntimeException("Expected ';' after data-class field");
            }

            node.addVar(new DCVar(type, varName));
        }

        return node;
    }
}
