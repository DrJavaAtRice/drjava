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


package koala.dynamicjava.util;

import java.util.*;
import junit.framework.*;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.SourceInfo;

import java.io.StringReader;
import java.util.List;
import koala.dynamicjava.parser.wrapper.ParserFactory;
import koala.dynamicjava.parser.wrapper.JavaCCParserFactory;

import koala.dynamicjava.interpreter.throwable.WrongVersionException;


/**
 * 
 * Tests to ensure the type checker throws WrongVersionExceptions if 1.5 features are used when running 1.4
 * 
 */
public class Distinction1415Test extends DynamicJavaTestCase {
  private TreeInterpreter astInterpreter;
  private TreeInterpreter strInterpreter;
  
  private ParserFactory parserFactory;
  private String testString;
  
  public Distinction1415Test(String name) {
    super(name);
  }
  
  public void setUp(){
    parserFactory = new JavaCCParserFactory();
    astInterpreter = new TreeInterpreter(null); // No ParserFactory needed to interpret an AST
    strInterpreter = new TreeInterpreter(parserFactory); // ParserFactory is needed to interpret a string
    TigerUtilities.resetVersion();
  }
  
  public Object interpret(String testString) throws InterpreterException {
    return strInterpreter.interpret(new StringReader(testString), "Unit Test");
  }
  
  
  /**
   * Tests the ability to enable and disable the functionality of 1.5
   */
  public void testSetAndResetTigerEnabled() {
    setTigerEnabled(true);
    TigerUtilities.assertTigerEnabled("Tiger should be enabled");
    assertEquals("Tiger should be enabled",TigerUtilities.isTigerEnabled(),true);
    setTigerEnabled(false);
    assertEquals("Tiger should be disabled",TigerUtilities.isTigerEnabled(),false);
    
  }
  
  /**
   * Test that the use of generic reference types fails when the runtime environment version is set to 1.4
   */
  public void testGenericReferenceTypes14(){
    setTigerEnabled(false);
    
    try{
      testString =
        "import java.util.LinkedList;\n"+
        "LinkedList<String> l = new LinkedList<String>();\n"+
        "l.add(\"Str1Str2Str3\");\n"+
        "l.get(0);\n";
      interpret(testString);
      fail("Should have thrown WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    
    //Set the java runtime version back to the correct version
    TigerUtilities.resetVersion();
  }
  
  /**
   * Test that the use of generic reference types does not fail when the runtime environment version is set to 1.5
   */
  public void testGenericReferenceTypes15(){
    setTigerEnabled(true);
    try{
      testString =
        "import java.util.LinkedList;\n"+
        "LinkedList<String> l = new LinkedList<String>();\n"+
        "l.add(\"Str1Str2Str3\");\n"+
        "l.get(0);\n";
      interpret(testString);
    }
    catch(WrongVersionException wve) {
      fail("Should not have thrown WrongVersionException");
    }
    
    //Set the java runtime version back to the correct version
    TigerUtilities.resetVersion();
  }
  
  /**
   * Test that the use of autoboxing and auto-unboxing fails when the runtime environment version is set to 1.4
   */
  public void testAutoboxing14() {
    setTigerEnabled(false);
    
    //Auto box
    try{
      testString =
        "int i = 5;" +
        "Integer j;" +
        "j = i;";
      interpret(testString);
      fail("Should have thrown WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    
    //Auto unbox
    try{
      testString =
        "Character c = new Character('c');" +
        "char d;" +
        "d = c;";
      interpret(testString);
      fail("Should have thrown WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    
    //Auto box in declaration
    try{
      testString =
        "Integer k = 5;";
      interpret(testString);
      fail("Should have thrown WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    
    //Auto unbox in declaration
    try{
      testString =
        "int l = new Integer(5);";
      interpret(testString);
      fail("Should have thrown WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    
    //Set the java runtime version back to the correct version
    TigerUtilities.resetVersion();
  }
  
  /**
   * Test that the use of autoboxing and auto-unboxing does not fail when the runtime environment version is set to 1.5
   */
  public void testAutoboxing15() {
    setTigerEnabled(true);
    try{
      testString =
        "Character c = new Character('c');" +
        "char d;" +
        "d = c;" +
        "Integer i = 5;" +
        "int j = new Integer(6);"+
        "i = j;"+
        "j = i;";
      interpret(testString);
    }
    catch(WrongVersionException wve) {
      fail("Should not have thrown WrongVersionException");
    }
    
    //Set the java runtime version back to the correct version
    TigerUtilities.resetVersion();
  }
  
  /**
   * Test that the use of foreach statements fails when the runtime environment is set to 1.4
   */ 
  public void testForEach14() {
    setTigerEnabled(false);
    try{
      testString = 
        "double sum = 0;\n" +
        "for(double j : new double[]{3.0,4.0,5.0})\n" +
        "sum+=j;";
      interpret(testString);
      fail("Should have thrown WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    
    //Set the java runtime version back to the correct version
    TigerUtilities.resetVersion();
  }
  
  /**
   * Test that the use of foreach statements does not fail when the runtime environment is set to 1.5
   */
  public void testForEach15() {
    setTigerEnabled(true);
    try{
      testString = 
        "double sum = 0;\n" +
        "for(double j : new double[]{3.0,4.0,5.0})\n" +
        "sum+=j;";
      interpret(testString);
    }
    catch(WrongVersionException wve) {      
      fail("Should not have thrown WrongVersionException");
    }
    
    //Set the java runtime version back to the correct version
    TigerUtilities.resetVersion();
  }
  
  /**
   * Test that the use of methods with variable arguments fails when the runtime environment is set to 1.4
   */
  public void testVarArgs14(){
    setTigerEnabled(false);
    try {
      
      testString =
        "public class C {\n"+
        "  public String someMethod(String ... s){\n"+
        "    return s[3];"+
        "  }\n"+
        "}\n";
      
      interpret(testString);
      fail("Should have thrown a WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    
    //Set the java runtime version back to the correct version
    TigerUtilities.resetVersion();
  }
  
  /**
   * Test that the use of methods with variable arguments does not fail when the runtime environment is set to 1.5
   */
  public void testVarArgs15(){
    setTigerEnabled(true);
    try {
      
      testString =
        "public class C {\n"+
        "  public String someMethod(String ... s){\n"+
        "    return s[3];"+
        "  }\n"+
        "}\n"+
        "new C().someMethod(\"a\",\"b\",\"c\",\"d\");";
      
      interpret(testString);
    }
    catch(WrongVersionException wve) {
      fail("Should not have thrown a WrongVersionException");
    }
    
    //Set the java runtime version back to the correct version
    TigerUtilities.resetVersion();
  }   
  
  /**
   * Test that the use of static imports fails when the runtime environment is set to 1.4
   * Note: static importing is not yet supported. Uncomment the test case when it is supported
   */ /**/
  public void testStaticImport14() {
    setTigerEnabled(false);
    try {
      testString =
        "import static java.lang.Math.abs;\n"+
        "abs(-2);";      
      assertEquals(2,interpret(testString));
      fail("Should have thrown a WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    
    try {
      testString =
        "import static java.lang.String.*;"+
        "valueOf(1);";      
      assertEquals("1",interpret(testString));
      fail("Should have thrown a WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    
    //Set the java runtime version back to the correct version
    TigerUtilities.resetVersion();
  }
  
  /**
   * Test that the use of static imports should not fail when the runtime environment is set to 1.5
   * Note: static importing is not yet supported. Uncomment the test case when it is supported
   */ /**/
  public void testStaticImport15() {
    setTigerEnabled(true);
    try {
      testString =
        "import static java.lang.Math.abs;\n"+
        "abs(-2);";      
      assertEquals(2,interpret(testString));
      
      testString =
        "import static java.lang.String.*;"+
        "valueOf(1);";      
      assertEquals("1",interpret(testString));
    }
    catch(WrongVersionException wve) {
      fail("Should not have thrown a WrongVersionException");
    }
    
    //Set the java runtime version back to the correct version
    TigerUtilities.resetVersion();
  }
  
  
  /**
   * Test that the use of enum types fails when the runtime version is set to 1.4
   * Note: enum types are not yet supported. Uncomment the test case when it is supported
   */
  public void xtestEnumType14() {
    setTigerEnabled(false);
    try {
      testString =
        "public class C {\n"+
        "  public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }\n"+
        "  public static void m() {\n"+
        "     System.out.println(Suit.CLUBS);\n"+
        "  }\n"+
        "}\n"+
        "C.m();";      
      assertEquals("CLUBS",interpret(testString));
      fail("Should have thrown a WrongVersionException");
    }
    catch(WrongVersionException wve) {
      //Expected to throw a WrongVersionException
    }
    //Set the java runtime version back to the correct version
    TigerUtilities.resetVersion();
  }
  
  /**
   * Test that the use of enum types does not fail when the runtime version is set to 1.5
   * Note: enum types are not yet supported. Uncomment the test case when it is supported
   */
  public void xtestEnumType15() {
    setTigerEnabled(true);
    try {
      testString =
        "public class C {\n"+
        "  public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }\n"+
        "  public static void m() {\n"+
        "     System.out.println(Suit.CLUBS);\n"+
        "  }\n"+
        "}\n"+
        "C.m();";      
      assertEquals("CLUBS",interpret(testString));
    }
    catch(WrongVersionException wve) {
      fail("Should not have thrown a WrongVersionException");
    }
    //Set the java runtime version back to the correct version
    TigerUtilities.resetVersion();
  }
  
}

