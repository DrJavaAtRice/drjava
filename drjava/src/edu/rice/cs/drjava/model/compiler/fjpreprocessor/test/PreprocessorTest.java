/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2019, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the names of its contributors may be used 
 *      to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software. Open Source Initative Approved is a trademark
 * of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/ or 
 * http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/
package edu.rice.cs.drjava.model;

import javax.swing.text.BadLocationException;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;

import edu.rice.cs.drjava.model.compiler.fjpreprocessor.Preprocessor;
import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.util.FileOps;

/**
 * Tests the indenting functionality on the level of the GlobalModel.
 * Not only are we testing that the document turns out right, but also
 * that the cursor position in the document is consistent with a standard.
 * 
 * @version $Id$
 */
public final class PreprocessorTest extends GlobalModelTestCase {

  /** Test the Preprocessor class creates the correct output files. */
  public void testCreatesJavaFile() {
    // Create a list of files to preprocess
    File fjavaFile;
    try {
      fjavaFile = tempFjavaFile(0);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create temp file: " + e.getMessage(), e);
    }

    // make sure the file exists
    if (!fjavaFile.exists()) {
      throw new RuntimeException("File " + fjavaFile.getAbsolutePath() + " does not exist.");
    }

    // Create a list of files to preprocess
    List<File> files = new ArrayList<File>();
    files.add(fjavaFile);

    // Preprocess the files
    LinkedList<DJError> errors = Preprocessor.preprocessList(files);
    // Check if there are any errors
    if (errors.size() > 0) {
      throw new RuntimeException("Preprocessing failed with errors: " + errors);
    }

    // Check if the output file is created
    File outputFile = new File(fjavaFile.getAbsolutePath().replace(".fjava", ".java"));
    if (!outputFile.exists()) {
      throw new RuntimeException("Output file " + outputFile.getAbsolutePath() + " was not created.");
    }
  }

  public void testNoFjavaCode() {

    // Write some java8 content to the file
    String java8Content = "public class Test {\n" +
        "  public static void main(String[] args) {\n" +
        "    System.out.println(\"Hello, World!\");\n" +
        "  }\n" +
        "}\n";

    testPreprocessorExpandsCorrectly(java8Content, java8Content);
  }

  public void testSimpleDataClass() {

    // Write some fjava content to the file
    String fjavaContent = "data-class Pair {\n" +
        "  int x;\n" +
        "  int y;\n" +
        "}\n";

    // Check if the output file has the correct content
    String expectedContent = "class Pair {\n" +
        "    private int x;\n" +
        "    private int y;\n" +
        "\n" +
        "/******************************************\\\n" +
        "| PREPROCESSOR NOTE: Begin code generation |\n" +
        "\\******************************************/\n" +
        "\n" +
        "    public Pair(int x, int y) {\n" +
        "        this.x = x;\n" +
        "        this.y = y;\n" +
        "    }\n" +
        "\n" +
        "    public int x() {\n" +
        "        return x;\n" +
        "    }\n" +
        "\n" +
        "    public int y() {\n" +
        "        return y;\n" +
        "    }\n" +
        "\n" +
        "    // PREPROCESSOR NOTE: toString method generated\n" +
        "    @Override\n" +
        "    public String toString() {\n" +
        "        return \"Pair(\" + x + \", \" + y + \")\";\n" +
        "    }\n" +
        "\n" +
        "    // PREPROCESSOR NOTE: hashCode method generated\n" +
        "    @Override\n" +
        "    public int hashCode() {\n" +
        "        return java.util.Objects.hash(x, y);\n" +
        "    }\n" +
        "\n" +
        "    // PREPROCESSOR NOTE: equals method generated\n" +
        "    @Override\n" +
        "    public boolean equals(Object obj) {\n" +
        "        if (this == obj) return true;\n" +
        "        if (obj == null || getClass() != obj.getClass()) return false;\n" +
        "        Pair other = (Pair) obj;\n" +
        "        return x.equals(other.x) && y.equals(other.y);\n" +
        "    }\n" +
        "\n" +
        "/****************************************\\\n" +
        "| PREPROCESSOR NOTE: End code generation |\n" +
        "\\****************************************/\n" +
        "\n" +
        "}\n" +
        "\n" +
        "\n";

    testPreprocessorExpandsCorrectly(fjavaContent, expectedContent);
  }

  public void testGeneric() {
    // Write some fjava content to the file
    String fjavaContent = "data-class Pair<T extends Numeric, U extends Numeric> {\n" +
        "  T x;\n" +
        "  U y;\n" +
        "}\n";

    // Check if the output file has the correct content
    String expectedContent = "class Pair<T extends Numeric, U extends Numeric> {\n" +
        "    private T x;\n" +
        "    private U y;\n" +
        "\n" +
        "/******************************************\\\n" +
        "| PREPROCESSOR NOTE: Begin code generation |\n" +
        "\\******************************************/\n" +
        "\n" +
        "    public Pair(T x, U y) {\n" +
        "        this.x = x;\n" +
        "        this.y = y;\n" +
        "    }\n" +
        "\n" +
        "    public T x() {\n" +
        "        return x;\n" +
        "    }\n" +
        "\n" +
        "    public U y() {\n" +
        "        return y;\n" +
        "    }\n" +
        "\n" +
        "    // PREPROCESSOR NOTE: toString method generated\n" +
        "    @Override\n" +
        "    public String toString() {\n" +
        "        return \"Pair(\" + x + \", \" + y + \")\";\n" +
        "    }\n" +
        "\n" +
        "    // PREPROCESSOR NOTE: hashCode method generated\n" +
        "    @Override\n" +
        "    public int hashCode() {\n" +
        "        return java.util.Objects.hash(x, y);\n" +
        "    }\n" +
        "\n" +
        "    // PREPROCESSOR NOTE: equals method generated\n" +
        "    @Override\n" +
        "    public boolean equals(Object obj) {\n" +
        "        if (this == obj) return true;\n" +
        "        if (obj == null || getClass() != obj.getClass()) return false;\n" +
        "        Pair other = (Pair) obj;\n" +
        "        return x.equals(other.x) && y.equals(other.y);\n" +
        "    }\n" +
        "\n" +
        "/****************************************\\\n" +
        "| PREPROCESSOR NOTE: End code generation |\n" +
        "\\****************************************/\n" +
        "\n" +
        "}";
    testPreprocessorExpandsCorrectly(fjavaContent, expectedContent);

  }

  public void testExtraMethod() {
    // Write some fjava content to the file
    String fjavaContent = "data-class Pair<T extends Numeric, U extends Numeric> {\n" +
        "    T x;\n" +
        "    U y;\n" +
        "    double sum() {\n" +
        "        return x.doubleValue() + y.doubleValue();\n" +
        "    }\n" +
        "}\n";
      
    // Check if the output file has the correct content
    String expectedContent = "class Pair<T extends Numeric, U extends Numeric> {\n" +
        "    private T x;\n" +
        "    private U y;\n" +
        "\n" +
        "/******************************************\\\n" +
        "| PREPROCESSOR NOTE: Begin code generation |\n" +
        "\\******************************************/\n" +
        "\n" +
        "    public Pair(T x, U y) {\n" +
        "        this.x = x;\n" +
        "        this.y = y;\n" +
        "    }\n" +
        "\n" +
        "    public T x() {\n" +
        "        return x;\n" +
        "    }\n" +
        "\n" +
        "    public U y() {\n" +
        "        return y;\n" +
        "    }\n" +
        "\n" +
        "    // PREPROCESSOR NOTE: toString method generated\n" +
        "    @Override\n" +
        "    public String toString() {\n" +
        "        return \"Pair(\" + x + \", \" + y + \")\";\n" +
        "    }\n" +
        "\n" +
        "    // PREPROCESSOR NOTE: hashCode method generated\n" +
        "    @Override\n" +
        "    public int hashCode() {\n" +
        "        return java.util.Objects.hash(x, y);\n" +
        "    }\n" +
        "\n" +
        "    // PREPROCESSOR NOTE: equals method generated\n" +
        "    @Override\n" +
        "    public boolean equals(Object obj) {\n" +
        "        if (this == obj) return true;\n" +
        "        if (obj == null || getClass() != obj.getClass()) return false;\n" +
        "        Pair other = (Pair) obj;\n" +
        "        return x.equals(other.x) && y.equals(other.y);\n" +
        "    }\n" +
        "\n" +
        "/****************************************\\\n" +
        "| PREPROCESSOR NOTE: End code generation |\n" +
        "\\****************************************/\n" +
        "\n" +
        "double sum() {\n" +
        "        return x.doubleValue() + y.doubleValue();\n" +
        "    }" +
        "\n" +
        "}";

    testPreprocessorExpandsCorrectly(fjavaContent, expectedContent);
  }

  public void testCustomEquals() {
    String fjavaContent = "data-class FunkyInt {\n" +
        "    int x;\n" +
        "    public boolean equals(Object obj) {\n" +
        "        if (obj instanceof FunkyInt) {\n" +
        "            FunkyInt other = (FunkyInt) obj;\n" +
        "            return this.x == other.x + 2;\n" +
        "        }\n" +
        "        return false;\n" +
        "    }\n" +
        "}\n";

    String expectedContent = "class FunkyInt {\n" +
        "    private int x;\n" +
        "\n" +
        "/******************************************\\\n" +
        "| PREPROCESSOR NOTE: Begin code generation |\n" +
        "\\******************************************/\n" +
        "\n" +
        "    public FunkyInt(int x) {\n" +
        "        this.x = x;\n" +
        "    }\n" +
        "\n" +
        "    public int x() {\n" +
        "        return x;\n" +
        "    }\n" +
        "\n" +
        "    // PREPROCESSOR NOTE: toString method generated\n" +
        "    @Override\n" +
        "    public String toString() {\n" +
        "        return \"FunkyInt(\" + x + \")\";\n" +
        "    }\n" +
        "\n" +
        "    // PREPROCESSOR NOTE: hashCode method generated\n" +
        "    @Override\n" +
        "    public int hashCode() {\n" +
        "        return java.util.Objects.hash(x);\n" +
        "    }\n" +
        "\n" +
        "    // PREPROCESSOR NOTE: equals method already defined\n" +
        "\n" +
        "/****************************************\\\n" +
        "| PREPROCESSOR NOTE: End code generation |\n" +
        "\\****************************************/\n" +
        "\n" +
        "public boolean equals(Object obj) {\n" +
        "        if (obj instanceof FunkyInt) {\n" +
        "            FunkyInt other = (FunkyInt) obj;\n" +
        "            return this.x == other.x + 2;\n" +
        "        }\n" +
        "        return false;\n" +
        "    }\n" +
        "}";

        testPreprocessorExpandsCorrectly(fjavaContent, expectedContent);
  }

  private void testPreprocessorExpandsCorrectly(String fjavacontent, String expected) {
    // Create a list of files to preprocess
    File fjavaFile;
    try {
      fjavaFile = tempFjavaFile(0);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create temp file: " + e.getMessage(), e);
    }

    try (FileWriter writer = new FileWriter(fjavaFile)) {
      writer.write(fjavacontent);
    } catch (Exception e) {
      throw new RuntimeException("Failed to write to file: " + fjavaFile.getAbsolutePath(), e);
    }

    // Create a list of files to preprocess
    List<File> files = new ArrayList<File>();
    files.add(fjavaFile);

    // Preprocess the files
    LinkedList<DJError> errors = Preprocessor.preprocessList(files);
    // Check if there are any errors
    if (errors.size() > 0) {
      throw new RuntimeException("Preprocessing failed with errors: " + errors);
    }

    // Check if the output file is created
    File outputFile = new File(fjavaFile.getAbsolutePath().replace(".fjava", ".java"));
    if (!outputFile.exists()) {
      throw new RuntimeException("Output file " + outputFile.getAbsolutePath() + " was not created.");
    }

    String actualContent = "";
    try (FileReader reader = new FileReader(outputFile)) {
      char[] buffer = new char[1024];
      int bytesRead;
      while ((bytesRead = reader.read(buffer)) != -1) {
        actualContent += new String(buffer, 0, bytesRead);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to read from file: " + outputFile.getAbsolutePath(), e);
    }

    assertEquals("Preprocessor output does not match expected output", expected.trim(), actualContent.trim());
  }

}