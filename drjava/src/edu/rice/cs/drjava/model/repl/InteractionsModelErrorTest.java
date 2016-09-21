/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.model.*;

import edu.rice.cs.util.Log;
import javax.swing.text.BadLocationException;
import edu.rice.cs.plt.tuple.Pair;


import edu.rice.cs.drjava.model.repl.newjvm.*;

import edu.rice.cs.plt.tuple.OptionVisitor;
import edu.rice.cs.plt.reflect.ReflectUtil;

import edu.rice.cs.dynamicjava.interpreter.*;

import java.io.File;
import java.io.IOException;

import edu.rice.cs.drjava.model.GlobalModelTestCase;

/** Tests errors in an InteractionsModel.
  * @version $Id: InteractionsModelErrorTest.java 5236 2010-04-27 01:43:36Z mgricken $
  */
public final class InteractionsModelErrorTest extends GlobalModelTestCase {
  protected static final String UNARY_FUN_NON_PUBLIC_INTERFACE_TEXT = 
    "interface UnaryFun {\n"+
    "  public Object apply(final Object arg);\n"+
    "}";
  protected static final String UNARY_FUN_PUBLIC_INTERFACE_TEXT = 
    "public interface UnaryFun {\n"+
    "  public Object apply(final Object arg);\n"+
    "}";

  protected static final String UNARY_FUN_NON_PUBLIC_CLASS_TEXT = 
    "abstract class UnaryFun {\n"+
    "  public abstract Object apply(final Object arg);\n"+
    "}";
  protected static final String UNARY_FUN_PUBLIC_CLASS_TEXT = 
    "public abstract class UnaryFun {\n"+
    "  public abstract Object apply(final Object arg);\n"+
    "}";
  protected static final String CLASS_IN_PACKAGE_CLASS_TEXT = 
    "package foo;\n"+
    "public class Bar {\n"+
    "  public void run() { }\n"+
    "}";

  private volatile InteractionsPaneOptions _interpreterOptions;
  private volatile Interpreter _interpreter;  
  private volatile ClassPathManager _classPathManager;
  private volatile ClassLoader _interpreterLoader;
  
  private static Log _log = new Log("InteractionsModelErrorTest.txt", false);
  
  public InteractionsModelErrorTest() {
    super();

    _classPathManager = new ClassPathManager(ReflectUtil.SYSTEM_CLASS_PATH);
    _interpreterLoader = _classPathManager.makeClassLoader(null);
    
    // _interpreterOptions = Options.DEFAULT;
    _interpreterOptions = new InteractionsPaneOptions();
    _interpreter = new Interpreter(_interpreterOptions, _interpreterLoader);
  }
  
  /** 
   * Asserts that the results of interpreting the first of each
   * Pair is equal to the second.
   * @param cases an array of Pairs
   * @throws InterpreterException if something goes wrong during interpretation
   */
  private void tester(Pair<String,Object>[] cases) throws InterpreterException {
    for (int i = 0; i < cases.length; i++) {
      Object out = interpretDirectly(cases[i].first());
      assertEquals(cases[i].first() + " interpretation wrong!", cases[i].second(), out);
    }
  }
  
  private Object interpretDirectly(String s) throws InterpreterException {
    return _interpreter.interpret(s).apply(new OptionVisitor<Object, Object>() {
      public Object forNone() { return null; }
      public Object forSome(Object obj) { return obj; }
    });
  }
  
  protected String _name() {
    return "compiler=" + _model.getCompilerModel().getActiveCompiler().getName() + ": ";
  }

  /** 
   * Tests that we get the correct 'cannot access its superinterface' error for non-public classes. 
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution if interrupted unexpectedly
   * @throws InterpreterException if something goes wrong during interpretation
   */
  public void testInterpretExtendNonPublic()
    throws BadLocationException, IOException, InterruptedException, InterpreterException {
    _log.log("testInterpretExtendNonPublic started");
    
    OpenDefinitionsDocument doc = setupDocument(UNARY_FUN_NON_PUBLIC_INTERFACE_TEXT);
    final File file = tempFile();
    saveFile(doc, new FileSelector(file));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    listener.compile(doc);
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.checkCompileOccurred();
    _model.removeListener(listener);
    assertCompileErrorsPresent(_name(), false);
    
    // Make sure .class exists
    File compiled = classForJava(file, "UnaryFun");
    assertTrue(_name() + "Class file should exist after compile", compiled.exists());    
    
    _classPathManager.addBuildDirectoryCP(compiled.getParentFile());
    
    try {
      _interpreter.interpret("UnaryFun f = new UnaryFun() { Object apply(Object arg) { return (Integer)arg * (Integer)arg; }}");
      fail("Should fail with 'cannot access its superinterface' exception.");
    }
    catch(edu.rice.cs.dynamicjava.interpreter.CheckerException ce) {
      assertTrue(ce.getMessage().indexOf("cannot access its superinterface")>=0);
    }
  }
  
  /** Tests that we don't get an error for public classes.
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution if interrupted unexpectedly
   * @throws InterpreterException if something goes wrong during interpretation
   */
  public void testInterpretExtendPublic()
    throws BadLocationException, IOException, InterruptedException, InterpreterException {
    _log.log("testInterpretExtendPublic started");
    
    OpenDefinitionsDocument doc = setupDocument(UNARY_FUN_PUBLIC_INTERFACE_TEXT);
    final File file = createFile("UnaryFun.java");
    saveFile(doc, new FileSelector(file));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    listener.compile(doc);
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.checkCompileOccurred();
    _model.removeListener(listener);
    assertCompileErrorsPresent(_name(), false);
    
    // Make sure .class exists
    File compiled = classForJava(file, "UnaryFun");
    assertTrue(_name() + "Class file should exist after compile", compiled.exists());    
    
    _classPathManager.addBuildDirectoryCP(compiled.getParentFile());
    
    _interpreter.interpret("UnaryFun f = new UnaryFun() { Object apply(Object arg) { return (Integer)arg * (Integer)arg; }}");
  }
  
  /** Tests that we get the correct 'cannot access its superinterface' error for non-public classes.
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution if interrupted unexpectedly
   * @throws InterpreterException if something goes wrong during interpretation
   */
  public void testInterpretExtendNonPublicClass()
    throws BadLocationException, IOException, InterruptedException, InterpreterException {
    _log.log("testInterpretExtendNonPublic started");
    
    OpenDefinitionsDocument doc = setupDocument(UNARY_FUN_NON_PUBLIC_CLASS_TEXT);
    final File file = tempFile();
    saveFile(doc, new FileSelector(file));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    listener.compile(doc);
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.checkCompileOccurred();
    _model.removeListener(listener);
    assertCompileErrorsPresent(_name(), false);
    
    // Make sure .class exists
    File compiled = classForJava(file, "UnaryFun");
    assertTrue(_name() + "Class file should exist after compile", compiled.exists());    
    
    _classPathManager.addBuildDirectoryCP(compiled.getParentFile());
    
    try {
      _interpreter.interpret("UnaryFun f = new UnaryFun() { public Object apply(Object arg) { return (Integer)arg * (Integer)arg; }}");
      fail("Should fail with 'cannot access its superclass' exception.");
    }
    catch(edu.rice.cs.dynamicjava.interpreter.CheckerException ce) {
      assertTrue(ce.getMessage().indexOf("cannot access its superclass")>=0);
    }
  }
  
  /** Tests that we don't get an error for public classes.
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution if interrupted unexpectedly
   * @throws InterpreterException if something goes wrong during interpretation
   */
  public void testInterpretExtendPublicClass()
    throws BadLocationException, IOException, InterruptedException, InterpreterException {
    _log.log("testInterpretExtendPublic started");
    
    OpenDefinitionsDocument doc = setupDocument(UNARY_FUN_PUBLIC_CLASS_TEXT);
    final File file = createFile("UnaryFun.java");
    saveFile(doc, new FileSelector(file));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    listener.compile(doc);
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.checkCompileOccurred();
    _model.removeListener(listener);
    assertCompileErrorsPresent(_name(), false);
    
    // Make sure .class exists
    File compiled = classForJava(file, "UnaryFun");
    assertTrue(_name() + "Class file should exist after compile", compiled.exists());    
    
    _classPathManager.addBuildDirectoryCP(compiled.getParentFile());
    
    _interpreter.interpret("UnaryFun f = new UnaryFun() { public Object apply(Object arg) { return (Integer)arg * (Integer)arg; }}");
  }
  
  /** Test that we get the right package using getPackage().
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution if interrupted unexpectedly
   * @throws InterpreterException if something goes wrong during interpretation
   */
  public void testInterpretGetPackageClass()
    throws BadLocationException, IOException, InterruptedException, InterpreterException {
    _log.log("testInterpretGetPackageClass started");
    
    OpenDefinitionsDocument doc = setupDocument(CLASS_IN_PACKAGE_CLASS_TEXT);

    final File dir = tempDirectory();
    final File packDir = new File(dir, "foo");
    packDir.mkdir();
    final File file = new File(packDir, "Bar.java");
    saveFile(doc, new FileSelector(file));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    listener.compile(doc);
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.checkCompileOccurred();
    _model.removeListener(listener);
    assertCompileErrorsPresent(_name(), false);
    
    // Make sure .class exists
    File compiled = classForJava(file, "Bar");
    assertTrue(_name() + "Class file should exist after compile", compiled.exists());    
    
    _classPathManager.addBuildDirectoryCP(compiled.getParentFile().getParentFile());
    
    Object out = interpretDirectly("new foo.Bar().getClass().getPackage().getName()");
    assertEquals("Package of foo.Bar should be foo", "foo", out);
  }
  
  /** Test that we get the right package using getPackage() with anonymous inner classes defined in the Interactions Pane.
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution if interrupted unexpectedly
   * @throws InterpreterException if something goes wrong during interpretation
   */
  public void testInterpretGetPackageAnonymous()
    throws BadLocationException, IOException, InterruptedException, InterpreterException {
    _log.log("testInterpretGetPackageAnonymous started");

    Object out = interpretDirectly("new Runnable() { public void run() { } }.getClass().getPackage()");
    assertEquals("Package of $1 should be null", null, out);
    
    out = interpretDirectly("package foo; new Runnable() { public void run() { } }.getClass().getPackage().getName()");
    assertEquals("Package of foo.$1 should be foo", "foo", out);
  }
}
