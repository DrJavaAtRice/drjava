/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 *
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.util.text.DocumentAdapterException;

import junit.framework.TestCase;
import java.util.LinkedList;
import java.io.*;
import javax.swing.text.*;

import koala.dynamicjava.util.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.interpreter.context.*;

/**
 * Class to test JavaDebugInterpreters by ensuring that appropriate
 * events are generated on each assignment.
 * 
 * NOTE: The tests at the bottom are disabled for now, since nothing needs to be done
 * on an assignment.  (We just copy back when the thread is resumed.)
 * 
 * @version $Id$
 */
public final class JavaDebugInterpreterTest extends DebugTestCase {
  private JavaDebugInterpreter _debugInterpreter;
  
  private String _assignedInterpreterName;
  
  protected static final String MONKEY_STUFF =
    /*1*/ "class MonkeyStuff {\n" +
    /*2*/ "  int foo = 6;\n" + 
    /*3*/ "  class MonkeyInner {\n" + 
    /*4*/ "    int innerFoo = 8;\n" +
    /*5*/ "    public class MonkeyTwoDeep {\n" +
    /*6*/ "      int twoDeepFoo = 13;\n" +
    /*7*/ "      class MonkeyThreeDeep {\n" +
    /*8*/ "        public int threeDeepFoo = 18;\n" +
    /*9*/ "          public void threeDeepMethod() {\n" +
    /*10*/"            int blah;\n" +
    /*11*/"            System.out.println(MonkeyStuff.MonkeyInner.MonkeyTwoDeep.MonkeyThreeDeep.this.threeDeepFoo);\n" +
    /*12*/"          }\n" +
    /*13*/"        }\n" +
    /*14*/"      }\n" +
    /*15*/"    }\n" +
    /*16*/"\n" + 
    /*17*/"  public static void main(String[] args) {\n" +
    /*18*/"    new MonkeyStuff().new MonkeyInner().new MonkeyTwoDeep().new MonkeyThreeDeep().threeDeepMethod();\n" +
    /*19*/"  }\n" +
    /*20*/"}";
  
  /**
   * Constructor.
   * @param  String name
   */
  public JavaDebugInterpreterTest(String name) {
    super(name);
  }
  
  public void setUp() throws IOException {
    super.setUp();
    // Creating a JavaDebugInterpreter with a custom 
    // notifyInterpreterAssignment() method
    _debugInterpreter = new JavaDebugInterpreter("test", "") {
      public EvaluationVisitorExtension makeEvaluationVisitor(Context context) {
        return new DebugEvaluationVisitor(context, _name) {
          protected void _notifyAssigned(Expression e) {
            notifyInterpreterAssignment(_name);
          }
        };
      }
    };
    _assignedInterpreterName = "";
  }
  
  public void notifyInterpreterAssignment(String name) {
    _assignedInterpreterName = name;
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
    assertEquals(message,
                 s1,
                 s2);
  }
    
  /**
   * Tests that a this expression with no classname will be correctly 
   * converted to a QualifiedName.
   */
  public void testConvertToName() {
    ThisExpression thisExp = _debugInterpreter.buildUnqualifiedThis();
    Node n = _debugInterpreter.visitThis(thisExp);        
    LinkedList thisList = new LinkedList();
    thisList.add(new Identifier("this"));
    QualifiedName expected = new QualifiedName(thisList);
    assertEqualsNodes("convertThisToName did not return the correct QualifiedName",
                      expected,
                      n);
  }
  
  /**
   * Tests that a this expression with a classname will be correctly
   * converted to an ObjectFieldAccess.
   */
  public void testConvertToObjectFieldAccess() {
    _debugInterpreter.setClassName("bar.baz.Foo$FooInner$FooInnerInner");
    LinkedList ids = new LinkedList();
    ids.add(new Identifier("Foo"));
    ThisExpression thisExp = new ThisExpression(ids, "", 0, 0, 0, 0);
    Node n = _debugInterpreter.visitThis(thisExp);
    Node expected = new ObjectFieldAccess(new ObjectFieldAccess(_debugInterpreter._convertThisToName(_debugInterpreter.buildUnqualifiedThis()), 
                                                                "this$1"), 
                                          "this$0");    
    
    assertEqualsNodes("convertThisToObjectFieldAccess did not return the correct ObjectFieldAccess",
                      expected,
                      n);
  }
  
  /**
   * Tests that the user can access fields of outer classes
   * in the debug interpreter.
   */
  public void testAccessFieldsOfOuterClasses() 
    throws DebugException, BadLocationException, DocumentAdapterException, IOException, InterruptedException {
    File file = new File(_tempDir, "MonkeyStuff.java");
    OpenDefinitionsDocument doc = doCompile(MONKEY_STUFF, file);
    BreakpointTestListener debugListener = new BreakpointTestListener();
    _debugger.addListener(debugListener);
    // Start debugger
    synchronized(_notifierLock) {
      _debugger.startup();
      _waitForNotifies(1);  // startup
      _notifierLock.wait();
    }
    
    // Set one breakpoints
    int index = MONKEY_STUFF.indexOf("System.out.println");
    _debugger.toggleBreakpoint(doc,index,11);
     
    // Run the main() method, hitting both breakpoints in different threads
    synchronized(_notifierLock) {
      interpretIgnoreResult("java MonkeyStuff");
       _waitForNotifies(3); // suspended, updated, breakpointReached
       _notifierLock.wait();
     }
    
    // Calling interpret instead of interpretIgnoreResult because we want
    // to wait until the interaction has ended.
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
              "}\n"+              
              "catch(Exception e) { System.out.println(MonkeyThreeDeep.this.threeDeepFoo);}\n" +
              "finally {System.out.println(MonkeyInner.MonkeyTwoDeep.this.twoDeepFoo);}");
    assertInteractionsDoesNotContain("18");
    assertInteractionsDoesNotContain("6");
    assertInteractionsContains("8\n13\n");
    
    // Close doc and make sure breakpoints are removed    
    _model.closeFile(doc);
    debugListener.assertBreakpointRemovedCount(1);  //fires once
      
    // Shutdown the debugger
    if (printMessages) System.out.println("Shutting down...");
    synchronized(_notifierLock) {
      _debugger.shutdown();
      _waitForNotifies(1);  // shutdown
      _notifierLock.wait();
    }
    
    debugListener.assertDebuggerShutdownCount(1);  //fires
    if (printMessages) System.out.println("Shut down.");
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
