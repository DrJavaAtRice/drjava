/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.autocomplete.parser;

import junit.framework.TestCase;

import edu.rice.cs.javalanglevels.*;
import edu.rice.cs.javalanglevels.tree.*;

/**
 * test the auto complete parser
 */
public class ACParserTest extends TestCase {
  
  /**
   * parses a string and returns it's sourcefile
   */
  private SourceFile _parseString(String txt) throws ParseException {
    ACParser p = new ACParser(txt);
    return p.SourceFile();
  }
  
  
  
  /**
   * test import/package out of order.
   * also tests package/imports not terminated by ;'s
   */
  public void testPackageMissingSemicolon() throws ParseException {
    SourceFile s = _parseString("package edu.rice.cs.drjava\n"+
                                "import java.io.*\n"+
                                "import java.util.List\n"+
                                "package edu.rice.cs.drjava\n"+
                                "public class A {\n"+
                                "}");
    assertEquals("Number of packages",2,s.getPackageStatements().length);
    assertEquals("Number of imports",2,s.getImportStatements().length);
  }
  
  /**
   * also tests package/imports terminated by trash
   */
  public void testPackageEndInTrash() throws ParseException {
    SourceFile s = _parseString("package edu.rice.cs.drjava;\n"+
                                "import java.io.*asdf\n"+
                                "import java.util.List#$\n"+
                                "package edu.rice.cs.drjava\n"+
                                "public class A {\n"+
                                "}");
    assertEquals("Number of packages",2,s.getPackageStatements().length);
    assertEquals("Number of imports",2,s.getImportStatements().length);
  }

  /**
   * also tests package/imports terminated by trash
   */
  public void testTrashInClassDef() throws ParseException {
    SourceFile s = _parseString("fdsa public sad static class A {\n"+
                                "}");
    assertEquals("Number of classes",1,s.getClasses().length);
    assertEquals("the class name is A", "A", s.getClasses()[0].getName().getText());
    assertEquals("the class has 2 modifiers", 1, s.getClasses()[0].getMav().getModifiers().length);
  }

  /**
   * also tests package/imports terminated by trash
   */
  public void testTrashInInterfaceDef() throws ParseException {
    SourceFile s = _parseString("fdsa public static interface A {\n"+
                                "}");
    assertEquals("Number of interfaces",1,s.getInterfaces().length);
    assertEquals("the class name is A", "A", s.getInterfaces()[0].getName().getText());
    assertEquals("the class has 2 modifiers", 2, s.getInterfaces()[0].getMav().getModifiers().length);
  }
  
  
  /**
   * test class declaration with optional {'s and }'s
   */
  public void testClassDefWithoutBraces() throws ParseException {
    /* normal case */
    SourceFile s = _parseString("public class A {\n" +
                                "public void foo(){}" +
                                "}");
    assertEquals("Number of classes",1,s.getClasses().length);
    assertEquals("the class name is A", "A", s.getClasses()[0].getName().getText());
    assertEquals("the class has 1 modifiers", 1, s.getClasses()[0].getMav().getModifiers().length);
    assertEquals("there is 1 function in the body", 1, s.getClasses()[0].getBody().getItems().length);

    /* missing } */
    s = _parseString("public class A {\n" +
                                "public void foo(){}");
    assertEquals("Number of classes",1,s.getClasses().length);
    assertEquals("the class name is A", "A", s.getClasses()[0].getName().getText());
    assertEquals("the class has 1 modifiers", 1, s.getClasses()[0].getMav().getModifiers().length);
    assertEquals("there is 1 function in the body", 1, s.getClasses()[0].getBody().getItems().length);


    /* missing { */
    s = _parseString("public class A \n" +
                                "public void foo(){}" +
                                "}");
    assertEquals("Number of classes",1,s.getClasses().length);
    assertEquals("the class name is A", "A", s.getClasses()[0].getName().getText());
    assertEquals("the class has 1 modifiers", 1, s.getClasses()[0].getMav().getModifiers().length);
    assertEquals("there is 1 function in the body", 1, s.getClasses()[0].getBody().getItems().length);

    /* missing both { and } */
    s = _parseString("public class A \n" +
                     "  public void foo(){}");
    assertEquals("Number of classes",1,s.getClasses().length);
    assertEquals("the class name is A", "A", s.getClasses()[0].getName().getText());
    assertEquals("the class has 1 modifiers", 1, s.getClasses()[0].getMav().getModifiers().length);
    assertEquals("there is 1 function in the body", 1, s.getClasses()[0].getBody().getItems().length);
  }
  
  
  /**
   * test interface definition with optional {'s and }'
   */
  public void testInterfaceDefWithoutBraces() throws ParseException{
    /* normal case */
    SourceFile s = _parseString("public interface A {\n" +
                                "public void foo();" +
                                "}");
    assertEquals("Number of interfacees",1,s.getInterfaces().length);
    assertEquals("the interface name is A", "A", s.getInterfaces()[0].getName().getText());
    assertEquals("the interface has 1 modifiers", 1, s.getInterfaces()[0].getMav().getModifiers().length);
    assertEquals("there is 1 function in the body", 1, s.getInterfaces()[0].getBody().getItems().length);

    /* missing } */
    s = _parseString("public interface A {\n" +
                     "  public void foo();");
    assertEquals("Number of interfacees",1,s.getInterfaces().length);
    assertEquals("the interface name is A", "A", s.getInterfaces()[0].getName().getText());
    assertEquals("the interface has 1 modifiers", 1, s.getInterfaces()[0].getMav().getModifiers().length);
    assertEquals("there is 1 function in the body", 1, s.getInterfaces()[0].getBody().getItems().length);


    /* missing { */
    s = _parseString("public interface A \n" +
                     "  public void foo();" +
                     "}");
    assertEquals("Number of interfacees",1,s.getInterfaces().length);
    assertEquals("the interface name is A", "A", s.getInterfaces()[0].getName().getText());
    assertEquals("the interface has 1 modifiers", 1, s.getInterfaces()[0].getMav().getModifiers().length);
    assertEquals("there is 1 function in the body", 1, s.getInterfaces()[0].getBody().getItems().length);

    /* missing both { and } */
    s = _parseString("public interface A \n" +
                     "  public void foo();");
    assertEquals("Number of interfacees",1,s.getInterfaces().length);
    assertEquals("the interface name is A", "A", s.getInterfaces()[0].getName().getText());
    assertEquals("the interface has 1 modifiers", 1, s.getInterfaces()[0].getMav().getModifiers().length);
    assertEquals("there is 1 function in the body", 1, s.getInterfaces()[0].getBody().getItems().length);
  }
  
  public void testNoCommaInGenerics() throws ParseException{
    SourceFile s = _parseString("public class A<T H>\n");
    assertEquals("Number of classes",1,s.getClasses().length);
    assertEquals("the class name is A", "A", s.getClasses()[0].getName().getText());
  }
}
