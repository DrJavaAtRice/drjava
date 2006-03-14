/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.util.swing.Utilities;

import java.util.LinkedList;
import java.io.*;
import javax.swing.text.*;

import koala.dynamicjava.util.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.interpreter.context.*;

/** Class to test JavaDebugInterpreters by ensuring that appropriate events are generated on each assignment.
 *
 * NOTE: The tests at the bottom are disabled for now, since nothing needs to be done on an assignment.  (We 
 * just copy back when the thread is resumed.)
 *
 * @version $Id$
 */
public final class JavaDebugInterpreterTest extends DebugTestCase {
  private static final String _newLine = System.getProperty("line.separator");
  private JavaDebugInterpreter _debugInterpreter;

//  private String _assignedInterpreterName;

  protected static final String MONKEY_STUFF =
    /*1*/ "class MonkeyStuff {\n" +
    /*2*/ "  int foo = 6;\n" +
    /*3*/ "  class MonkeyInner {\n" +
    /*4*/ "    int innerFoo = 8;\n" +
    /*5*/ "    public class MonkeyTwoDeep {\n" +
    /*6*/ "      int twoDeepFoo = 13;\n" +
    /*7*/ "      class MonkeyThreeDeep {\n" +
    /*8*/ "        public int threeDeepFoo = 18;\n" +
    /*9*/ "        public void threeDeepMethod() {\n" +
    /*10*/"          int blah;\n" +
    /*11*/"          System.out.println(MonkeyStuff.MonkeyInner.MonkeyTwoDeep.MonkeyThreeDeep.this.threeDeepFoo);\n" +
    /*12*/"        }\n" +
    /*13*/"      }\n" +
    /*14*/"      int getNegativeTwo() { return -2; }\n" +
    /*15*/"    }\n" +
    /*16*/"  }\n" +
    /*17*/"\n" +
    /*18*/"  public static void main(String[] args) {\n" +
    /*19*/"    new MonkeyStuff().new MonkeyInner().new MonkeyTwoDeep().new MonkeyThreeDeep().threeDeepMethod();\n" +
    /*20*/"  }\n" +
    /*21*/"}";

  protected static final String MONKEY_STATIC_STUFF =
    /*1*/ "package monkey;\n" +
    /*2*/ "public class MonkeyStaticStuff {\n" +
    /*3*/ "  static int foo = 6;\n" +
    /*4*/ "  static class MonkeyInner {\n" +
    /*5*/ "    static int innerFoo = 8;\n" +
    /*6*/ "    static public class MonkeyTwoDeep {\n" +
    /*7*/ "      static int twoDeepFoo = 13;\n" +
    /*8*/ "      public static class MonkeyThreeDeep {\n" +
    /*9*/ "        public static int threeDeepFoo = 18;\n" +
    /*10*/ "        public static void threeDeepMethod() {\n" +
    /*11*/"          System.out.println(MonkeyStaticStuff.MonkeyInner.MonkeyTwoDeep.MonkeyThreeDeep.threeDeepFoo);\n" +
    /*12*/"          System.out.println(MonkeyTwoDeep.twoDeepFoo);\n" +
    /*13*/"          System.out.println(twoDeepFoo);\n" +
    /*14*/"        }\n" +
    /*15*/"      }\n" +
    /*16*/"      static int getNegativeTwo() { return -2; }\n" +
    /*17*/"    }\n" +
    /*18*/"  }\n" +
    /*19*/"}";

  protected static final String MONKEY_WITH_INNER_CLASS =
    /* 1 */    "class Monkey {\n" +
    /* 2 */    "  static int foo = 6; \n" +
    /* 3 */    "  class MonkeyInner { \n" +
    /* 4 */    "    int innerFoo = 8;\n" +
    /* 5 */    "    class MonkeyInnerInner { \n" +
    /* 6 */    "      int innerInnerFoo = 10;\n" +
    /* 7 */    "      public void innerMethod() { \n" +
    /* 8 */    "        int innerMethodFoo;\n" +
    /* 9 */    "        String nullString = null;\n" +
    /* 10 */   "        innerMethodFoo = 12;\n" +
    /* 11 */   "        foo++;\n" +
    /* 12 */   "        innerFoo++;\n" +
    /* 13 */   "        innerInnerFoo++;\n" +
    /* 14 */   "        innerMethodFoo++;\n" +
    /* 15 */   "        staticMethod();\n" +
    /* 16 */   "        System.out.println(\"innerMethodFoo: \" + innerMethodFoo);\n" +
    /* 17 */   "      }\n" +
    /* 18 */   "    }\n" +
    /* 19 */   "  }\n" +
    /* 20 */   "  public void bar() {\n" +
    /* 21 */   "    final MonkeyInner.MonkeyInnerInner mi = \n" +
    /* 22 */   "      new MonkeyInner().new MonkeyInnerInner();\n" +
    /* 23 */   "    mi.innerMethod();\n" +
    /* 24 */   "    final int localVar = 99;\n" +
    /* 25 */   "    new Thread() {\n" +
    /* 26 */   "      public void run() {\n" +
    /* 27 */   "        final int localVar = mi.innerInnerFoo;\n" +
    /* 28 */   "        new Thread() {\n" +
    /* 29 */   "          public void run() {\n" +
    /* 30 */   "            new Thread() {\n" +
    /* 31 */   "              public void run() {\n" +
    /* 32 */   "                System.out.println(\"localVar = \" + localVar);\n" +
    /* 33 */   "              }\n" +
    /* 34 */   "            }.run();\n" +
    /* 35 */   "          }\n" +
    /* 36 */   "        }.run();\n" +
    /* 37 */   "      }\n" +
    /* 38 */   "    }.run();\n" +
    /* 39 */   "  }\n" +
    /* 40 */   "  public static void staticMethod() {\n" +
    /* 41 */   "    int z = 3;\n" +
    /* 42 */   "  }\n" +
    /* 43 */   "}\n";


  public void setUp() throws Exception {
    super.setUp();
    // Creating a JavaDebugInterpreter with a custom notifyInterpreterAssignment() method
    _debugInterpreter = new JavaDebugInterpreter("test", "") {
      public EvaluationVisitorExtension makeEvaluationVisitor(Context context) {
        return new DebugEvaluationVisitor(context, _name);
//        return new DebugEvaluationVisitor(context, _name) {
//          protected void _notifyAssigned(Expression e) {
//            notifyInterpreterAssignment(_name);
//          }
//        };
      }
    };
//    _assignedInterpreterName = "";
  }

  public void notifyInterpreterAssignment(String name) {
//    _assignedInterpreterName = name;
  }

  public void testVerifyClassName() {
    _debugInterpreter.setClassName("bar.baz.Foo$FooInner$FooInnerInner");
    assertEquals("verify failed", 0, _debugInterpreter.verifyClassName("bar.baz.Foo.FooInner.FooInnerInner"));
    assertEquals("verify failed", 1, _debugInterpreter.verifyClassName("bar.baz.Foo.FooInner"));
    assertEquals("verify failed", 2, _debugInterpreter.verifyClassName("bar.baz.Foo"));
    assertEquals("verify failed", -1, _debugInterpreter.verifyClassName("bar.baz"));
    assertEquals("verify failed", 0, _debugInterpreter.verifyClassName("Foo.FooInner.FooInnerInner"));
    assertEquals("verify failed", 2, _debugInterpreter.verifyClassName("Foo"));
    assertEquals("verify failed", 1, _debugInterpreter.verifyClassName("FooInner"));
    assertEquals("verify failed", 0, _debugInterpreter.verifyClassName("FooInnerInner"));
    assertEquals("verify failed", 1, _debugInterpreter.verifyClassName("Foo.FooInner"));
    assertEquals("verify failed", 0, _debugInterpreter.verifyClassName("FooInner.FooInnerInner"));
    assertEquals("verify failed", -1, _debugInterpreter.verifyClassName("FooInner.FooInnerInner.Foo"));
    assertEquals("verify failed", -1, _debugInterpreter.verifyClassName("FooInner.FooInnerInner.foo"));
    assertEquals("verify failed", -1, _debugInterpreter.verifyClassName("o.FooInner"));
    _debugInterpreter.setClassName("Foo$FooInner$FooInnerInner");
    assertEquals("verify failed", 0, _debugInterpreter.verifyClassName("Foo.FooInner.FooInnerInner"));
    assertEquals("verify failed", 2, _debugInterpreter.verifyClassName("Foo"));
    assertEquals("verify failed", 1, _debugInterpreter.verifyClassName("FooInner"));
    assertEquals("verify failed", 0, _debugInterpreter.verifyClassName("FooInnerInner"));
    assertEquals("verify failed", 1, _debugInterpreter.verifyClassName("Foo.FooInner"));
    assertEquals("verify failed", 0, _debugInterpreter.verifyClassName("FooInner.FooInnerInner"));
    assertEquals("verify failed", -1, _debugInterpreter.verifyClassName("FooInner.FooInnerInner.Foo"));
    assertEquals("verify failed", -1, _debugInterpreter.verifyClassName("FooInner.FooInnerInner.foo"));
    assertEquals("verify failed", -1, _debugInterpreter.verifyClassName("o.FooInner"));
  }

  private void assertEqualsNodes(String message, Node expected, Node actual) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DisplayVisitor dve = new DisplayVisitor(baos);
    expected.acceptVisitor(dve);
    String s1 = baos.toString();
    baos.reset();
    actual.acceptVisitor(dve);
    String s2 = baos.toString();
    //System.out.println("s1 = " + s1 + "\ns2 = " + s2);
    assertEquals(message, s1, s2);
  }

  /** Tests that a this expression with no classname will be correctly converted to a QualifiedName. */
  public void testConvertToName() {
    ThisExpression thisExp = _debugInterpreter.buildUnqualifiedThis();
    Node n = _debugInterpreter.visitThis(thisExp);
    LinkedList<IdentifierToken> thisList = new LinkedList<IdentifierToken>(); // Add parameterization <Identifier>.
    thisList.add(new Identifier("this"));
    QualifiedName expected = new QualifiedName(thisList);
    assertEqualsNodes("convertThisToName did not return the correct QualifiedName", expected, n);
  }

  /** Tests that a this expression with a classname will be correctly converted to an ObjectFieldAccess. */
  public void testConvertToObjectFieldAccess() {
    _debugInterpreter.setClassName("bar.baz.Foo$FooInner$FooInnerInner");
    LinkedList<IdentifierToken> ids = new LinkedList<IdentifierToken>(); // Add parameterization <Identifier>.
    ids.add(new Identifier("Foo"));
    ThisExpression thisExp = new ThisExpression(ids, "", 0, 0, 0, 0);
    Node n = _debugInterpreter.visitThis(thisExp);
    Node expected = 
      new ObjectFieldAccess(
        new ObjectFieldAccess(_debugInterpreter._convertThisToName(_debugInterpreter.buildUnqualifiedThis()), "this$1"),
          "this$0");

    assertEqualsNodes("convertThisToObjectFieldAccess did not return the correct ObjectFieldAccess",
                      expected,
                      n);
  }

  /**
   * Tests that the user can access fields of outer classes
   * in the debug interpreter.
   */
  public void testAccessFieldsAndMethodsOfOuterClasses()
    throws DebugException, BadLocationException, EditDocumentException, IOException, InterruptedException {
    File file = new File(_tempDir, "MonkeyStuff.java");
    OpenDefinitionsDocument doc = doCompile(MONKEY_STUFF, file);
    BreakpointTestListener debugListener = new BreakpointTestListener();
    _debugger.addListener(debugListener);
    // Start debugger
    synchronized(_notifierLock) {
      _debugger.startup();
      _setPendingNotifies(1);  // startup
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    // Set one breakpoint
    int index = MONKEY_STUFF.indexOf("System.out.println");
    _debugger.toggleBreakpoint(doc, index, 11);

    // Run the main() method, hitting the breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("java MonkeyStuff");
       _setPendingNotifies(3); // suspended, updated, breakpointReached
       while (_pendingNotifies > 0) _notifierLock.wait();
     }

    // Calling interpret instead of interpretIgnoreResult because we want to wait until the interaction has ended.

    // Test that IdentityVisitor really does visit all nodes and their subnodes
    // by giving it a statement consisting of lots of different syntax components.
    interpret("try {\n" +
              "  for (int i = MonkeyStuff.this.foo; i < 7; i++) {\n"+
              "    do{System.out.println(MonkeyInner.this.innerFoo);}\n" +
              "    while(MonkeyStuff.MonkeyInner.this.innerFoo == MonkeyThreeDeep.this.threeDeepFoo);\n" +
              "    switch(MonkeyStuff.MonkeyInner.MonkeyTwoDeep.this.twoDeepFoo) {\n" +
              "      case 13: if (this.threeDeepFoo == 5) {\n" +
              "                  System.out.println(MonkeyThreeDeep.this.threeDeepFoo);\n" +
              "               }\n" +
              "               else {\n" +
              "                  MonkeyThreeDeep.this.threeDeepFoo = MonkeyThreeDeep.this.threeDeepFoo + MonkeyStuff.this.foo;\n" +
              "               }\n" +
              "    }\n" +
              "  }\n" +
              "}\n" +
              "catch(Exception e) { System.out.println(MonkeyThreeDeep.this.threeDeepFoo);}\n" +
              "finally {System.out.println(MonkeyInner.MonkeyTwoDeep.this.twoDeepFoo);}");
    assertInteractionsDoesNotMatch(".*^18$.*");
    assertInteractionsDoesNotMatch(".*^6$.*");
    assertInteractionsMatches(".*^8" + _newLine + "13$.*");
    
    // Tests that the debugger has the correct notion of
    interpret("foo");
    
//    System.err.println(getInteractionsText());

    assertInteractionsMatches(".*^6$.*");
    
    interpret("foo = 123");
    assertEquals("foo should have been modified" , "123", interpret("MonkeyStuff.this.foo"));
    interpret("int foo = 999;");
    assertEquals("foo should refer to defined foo", "999", interpret("foo"));
    assertEquals("declaring foo should not change MonkeyStuff.this.foo", "123", interpret("MonkeyStuff.this.foo"));

    assertEquals("call method of outer class #1", "-2", interpret("getNegativeTwo()"));
    assertEquals("call method of outer class #2", "-2", interpret("MonkeyTwoDeep.this.getNegativeTwo()"));
    assertEquals("call method of outer class #3", "-2",
                 interpret("MonkeyInner.MonkeyTwoDeep.this.getNegativeTwo()"));
    assertEquals("call method of outer class #4", "-2",
                 interpret("MonkeyStuff.MonkeyInner.MonkeyTwoDeep.this.getNegativeTwo()"));

    // Close doc and make sure breakpoints are removed
    _model.closeFile(doc);
    
    Utilities.clearEventQueue();
    debugListener.assertBreakpointRemovedCount(1);  //fires once

    // Shutdown the debugger
    if (printMessages) System.out.println("Shutting down...");
    synchronized(_notifierLock) {
      _debugger.shutdown();
      _setPendingNotifies(1);  // shutdown
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    debugListener.assertDebuggerShutdownCount(1);  //fires
    if (printMessages) System.out.println("Shut down.");
    _debugger.removeListener(debugListener);
  }

  /** Tests that the user can access static fields of outer classes in the debug interpreter. */
  public void testAccessStaticFieldsAndMethodsOfOuterClasses()
    throws DebugException, BadLocationException, EditDocumentException, IOException, InterruptedException {
    File dir = new File(_tempDir, "monkey");
    dir.mkdir();
    File file = new File(dir, "MonkeyStaticStuff.java");
    OpenDefinitionsDocument doc = doCompile(MONKEY_STATIC_STUFF, file);
    BreakpointTestListener debugListener = new BreakpointTestListener();
    _debugger.addListener(debugListener);
    // Start debugger
    synchronized(_notifierLock) {
      _debugger.startup();
      _setPendingNotifies(1);  // startup
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    // Set one breakpoint
    int index = MONKEY_STATIC_STUFF.indexOf("System.out.println");
    _debugger.toggleBreakpoint(doc,index,11);

    // Run the main() method, hitting both breakpoints in different threads
    synchronized(_notifierLock) {
      //interpret("package monkey;");
      interpretIgnoreResult("monkey.MonkeyStaticStuff.MonkeyInner.MonkeyTwoDeep.MonkeyThreeDeep.threeDeepMethod();");
      _setPendingNotifies(3); // suspended, updated, breakpointReached
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    assertEquals("should find field of static outer class",
                 "13",
                 interpret("twoDeepFoo"));
    assertEquals("should find field of static outer class",
                 "13",
                 interpret("MonkeyInner.MonkeyTwoDeep.twoDeepFoo"));

    interpret("twoDeepFoo = 100;");
    assertEquals("should have assigned field of static outer class",
                 "100",
                 interpret("twoDeepFoo"));
    assertEquals("should have assigned the field of static outer class",
                 "100",
                 interpret("MonkeyStaticStuff.MonkeyInner.MonkeyTwoDeep.twoDeepFoo"));
    assertEquals("should have assigned the field of static outer class",
                 "100",
                 interpret("monkey.MonkeyStaticStuff.MonkeyInner.MonkeyTwoDeep.twoDeepFoo"));

    interpret("int twoDeepFoo = -10;");
    assertEquals("Should have successfully shadowed field of static outer class", "-10", interpret("twoDeepFoo"));
    
    assertEquals("should have assigned the field of static outer class", "100",
                 interpret("MonkeyTwoDeep.twoDeepFoo"));
    
    assertEquals("should have assigned the field of static outer class", "100",
                 interpret("MonkeyStaticStuff.MonkeyInner.MonkeyTwoDeep.twoDeepFoo"));

    assertEquals("Should be able to access a static field of a non-static outer class", "6", interpret("foo"));
    assertEquals("Should be able to access a static field of a non-static outer class", "6",
                 interpret("MonkeyStaticStuff.foo"));

    interpret("foo = 987;");
    assertEquals("Should have changed the value of a static field of a non-static outer class", "987",
                 interpret("foo"));
    
    assertEquals("Should have changed the value of a static field of a non-static outer class", "987",
                 interpret("MonkeyStaticStuff.foo"));

    interpret("int foo = 56;");
    assertEquals("Should have defined a new variable", "56", interpret("foo"));
    assertEquals("Should have shadowed the value of a static field of a non-static outer class", "987",
                 interpret("MonkeyStaticStuff.foo"));

    assertEquals("should be able to call method of outer class", "-2", interpret("getNegativeTwo()"));
    assertEquals("should be able to call method of outer class", "-2", interpret("MonkeyTwoDeep.getNegativeTwo()"));
    assertEquals("should be able to call method of outer class", "-2",
                 interpret("MonkeyInner.MonkeyTwoDeep.getNegativeTwo()"));
    assertEquals("should be able to call method of outer class", "-2",
                 interpret("MonkeyStaticStuff.MonkeyInner.MonkeyTwoDeep.getNegativeTwo()"));

    // Shutdown the debugger
    if (printMessages) System.out.println("Shutting down...");

    synchronized(_notifierLock) {
      _debugger.shutdown();
      _setPendingNotifies(1);  // shutdown
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    debugListener.assertDebuggerShutdownCount(1);  //fires
    if (printMessages) System.out.println("Shut down.");

    _debugger.removeListener(debugListener);
  }

  public void testAccessNullFieldsAndFinalLocalVariables()
    throws DebugException, BadLocationException, EditDocumentException, IOException, InterruptedException {
    File file = new File(_tempDir, "Monkey.java");
    OpenDefinitionsDocument doc = doCompile(MONKEY_WITH_INNER_CLASS, file);
    BreakpointTestListener debugListener = new BreakpointTestListener();
    _debugger.addListener(debugListener);
    // Start debugger
    synchronized(_notifierLock) {
      _debugger.startup();
      _setPendingNotifies(1);  // startup
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    // Set one breakpoint
    int index = MONKEY_WITH_INNER_CLASS.indexOf("innerMethodFoo = 12;");
    _debugger.toggleBreakpoint(doc,index,10);
    index = MONKEY_WITH_INNER_CLASS.indexOf("System.out.println(\"localVar = \" + localVar);");
    _debugger.toggleBreakpoint(doc,index,32);

    // Run the main() method, hitting both breakpoints in different threads
    synchronized(_notifierLock) {
      interpretIgnoreResult("new Monkey().bar()");
       _setPendingNotifies(3); // suspended, updated, breakpointReached
       while (_pendingNotifies > 0) _notifierLock.wait();
     }

    // Test accessing a field initialized to null
    assertEquals("nullString should be null", "null", interpret("nullString"));
    interpret("nullString = new Integer(3)");
    assertInteractionsContains("Error: Bad types in assignment");
    assertEquals("nullString should still be null", "null", interpret("nullString"));
    assertEquals("Should be able to assign a string to nullString", "\"asdf\"", interpret("nullString = \"asdf\""));
    assertEquals("Should equal \"asdf\"", "true", interpret("nullString.equals(\"asdf\")"));

    // Resumes this thread, switching to the next break point
    synchronized(_notifierLock) {
      _asyncResume();
      _setPendingNotifies(3);  // breakpointReached, suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    // Test accessing final local variables
    assertEquals("Should be able to access localVar", "11", interpret("localVar"));
    interpret("localVar = 5");
    /* The Following test is commented out TEMPORARILY to work around bug in JDK 1.5 Beta2 JVM */
    /* Update (10/12/2005): The test still fails under a current JDK 1.5 JVM.  I'm not sure it's the JDK's fault */
    // assertEquals("The value of localVar should not have changed", "11", interpret("localVar"));

    // Shutdown the debugger
    if (printMessages) {
      System.out.println("Shutting down...");
    }
    synchronized(_notifierLock) {
      _debugger.shutdown();
      _setPendingNotifies(1);  // shutdown
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    debugListener.assertDebuggerShutdownCount(1);  //fires
    if (printMessages) {
      System.out.println("Shut down.");
    }
    _debugger.removeListener(debugListener);
  }

  /**
   * Disabled...
   *
  public void testNoAssignment() throws ExceptionReturnedException {

    // 1
    _debugInterpreter.interpret("1 + 1");
    assertEquals("Should not have made an assignment.", "", _assignedInterpreterName);

    // 2
    _debugInterpreter.interpret("public void foo() {}; foo()");
    assertEquals("Should not have made an assignment.", "", _assignedInterpreterName);

    // 3
    _debugInterpreter.interpret("int x");
    assertEquals("Should not have made an assignment.", "", _assignedInterpreterName);

  }*/

  /**
   * Disabled...
   *
  public void testWithAssignment() throws ExceptionReturnedException {
    // 1
    _debugInterpreter.interpret("x = 0");
    assertEquals("Should have made an assignment.", "test", _assignedInterpreterName);
    _assignedInterpreterName = "";

    // 2
    _debugInterpreter.interpret("y = null");
    assertEquals("Should have made an assignment.", "test", _assignedInterpreterName);
    _assignedInterpreterName = "";

    // 3
    _debugInterpreter.interpret("int z; z = 2");
    assertEquals("Should have made an assignment.", "test", _assignedInterpreterName);
    _assignedInterpreterName = "";
  } */
}
