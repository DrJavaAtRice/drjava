/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;

import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.util.text.EditDocumentException;
import javax.swing.text.BadLocationException;
import edu.rice.cs.plt.tuple.Pair;


import edu.rice.cs.drjava.model.repl.newjvm.*;
import edu.rice.cs.drjava.model.repl.newjvm.ClassPathManager;
import edu.rice.cs.drjava.DrJavaTestCase;

import edu.rice.cs.plt.tuple.OptionVisitor;
import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.text.TextUtil;

import edu.rice.cs.drjava.model.repl.newjvm.DrScalaInterpreter;
import edu.rice.cs.dynamicjava.interpreter.InterpreterException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

import java.rmi.RemoteException;

import edu.rice.cs.drjava.model.GlobalModelTestCase;

import static edu.rice.cs.drjava.model.repl.InteractionsModelTest.TestInteractionsModel;
import static edu.rice.cs.drjava.model.repl.InteractionsModelTest.IncompleteInputInteractionsModel;

/** Tests errors in an InteractionsModel.
  * @version $Id: InteractionsModelErrorTest.java 5728 2012-09-30 19:03:27Z wdforson $
  */
public final class InteractionsModelErrorTest extends GlobalModelTestCase {
  protected static final String UNARY_FUN_NON_PUBLIC_INTERFACE_TEXT = 
    "private trait UnaryFun {\n" +
    "  def apply(arg: Object): Object\n" +
    "}";
  protected static final String UNARY_FUN_PUBLIC_INTERFACE_TEXT = 
    "trait UnaryFun {\n"+
    "  def apply(arg: Object): Object\n"+
    "}";

  protected static final String UNARY_FUN_NON_PUBLIC_CLASS_TEXT = 
    "private abstract class UnaryFun {\n" +
    "  def apply(arg: Object): Object\n" +
    "}";
  protected static final String UNARY_FUN_PUBLIC_CLASS_TEXT = 
    "abstract class UnaryFun {\n" +
    "  def apply(arg: Object): Object\n" +
    "}";
  protected static final String CLASS_IN_PACKAGE_CLASS_TEXT = 
    "package foo\n" +
    "class Bar {\n" +
    "  def run() { }\n" +
    "}";

  private volatile Interpreter _interpreter;  
  
  private static Log _log = new Log("InteractionsModelErrorTest.txt", false);
  
  public InteractionsModelErrorTest() {
    super();
    _interpreter = new DrScalaInterpreter();
    _interpreter.start();
  }
  
  protected String _name() {
    return "compiler=" + _model.getCompilerModel().getActiveCompiler().getName() + ": ";
  }

  /** Tests that we don't get an error for public classes. */
  @SuppressWarnings("unchecked")
  public void testInterpretExtendPublic()
    throws BadLocationException, IOException, InterruptedException, InterpreterException {
    _log.log("testInterpretExtendPublic started");
    
    OpenDefinitionsDocument doc = setupDocument(UNARY_FUN_PUBLIC_INTERFACE_TEXT);
    final File file = createFile("UnaryFun.scala");
    saveFile(doc, new FileSelector(file));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    listener.compile(doc);
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    _log.log("checking compile");
    listener.checkCompileOccurred();
    _log.log("compile completed");
    _model.removeListener(listener);
    assertCompileErrorsPresent(_name(), false);
    
    // Make sure .class exists
    File compiled = classForScala(file, "UnaryFun");
    assertTrue(_name() + "Class file should exist after compile", compiled.exists());    
    _log.log("found class file");

    _log.log("path to add: " + compiled.getParentFile().getPath());
    _interpreter.addCP("BuildDirectoryCP", compiled.getParentFile().getPath());
    _log.log("added CP");
    
    try {
      String res = _interpreter.interpret("val f = new UnaryFun() { " + 
              "def apply(arg: Object): Object = { (arg.asInstanceOf[Int] * " +
              "arg.asInstanceOf[Int]).asInstanceOf[java.lang.Integer] } }");
      assertTrue("extending a public class should NOT cause an error. res: " + res, 
              !res.contains("error"));
    }
    catch(Throwable t) {
      fail("testInterpretExtendPublic threw: " + t);
    }
  }
  
  /** Tests that we get the correct 'cannot access its superinterface' error for non-public classes. */
  @SuppressWarnings("unchecked")
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
    File compiled = classForScala(file, "UnaryFun");
    assertTrue(_name() + "Class file should exist after compile", compiled.exists());    
    
    _interpreter.addCP("BuildDirectoryCP", compiled.getParentFile().getPath());
    
    try {
      String res = _interpreter.interpret("val f = new UnaryFun() " + 
          "{ def apply(arg: Object) = { (arg.asInstanceOf[Int] * " +
          "arg.asInstanceOf[Int]).asInstanceOf[Object] }}");
      if (!res.contains("error: private class UnaryFun escapes its " +
                  "defining scope as part of type UnaryFun"))
        fail("Should fail with 'private class UnaryFun escapes its " +
                  "defining scope as part of type UnaryFun'");
    }
    catch (Throwable t) {
      fail("Scala interpreter should return an exception, but " +
              "it should not *throw* one itself. Threw: " + t);
    }
  }
  
  /** Tests that we don't get an error for public classes. */
  @SuppressWarnings("unchecked")
  public void testInterpretExtendPublicClass()
    throws BadLocationException, IOException, InterruptedException, InterpreterException {
    _log.log("testInterpretExtendPublic started");
    
    OpenDefinitionsDocument doc = setupDocument(UNARY_FUN_PUBLIC_CLASS_TEXT);
    final File file = createFile("UnaryFun.scala");
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
    File compiled = classForScala(file, "UnaryFun");
    assertTrue(_name() + "Class file should exist after compile", compiled.exists());    
    
    _interpreter.addCP("BuildDirectoryCP", compiled.getParentFile().getPath());
    
   try {
     String res = _interpreter.interpret("val f = new UnaryFun() { " + 
             "def apply(arg: Object) = { (arg.asInstanceOf[Int] * " +
             "arg.asInstanceOf[Int]).asInstanceOf[Object] }}");
     assertTrue("extending a public class should NOT cause an error", 
             !res.contains("error"));
   }
   catch (Throwable t) {
     fail("testInterpretExtendPublic threw: " + t);
   }
  }
  
  /** Test that we get the right package using getPackage(). */
  @SuppressWarnings("unchecked")
  public void testInterpretGetPackageClass()
    throws BadLocationException, IOException, InterruptedException, InterpreterException {
    _log.log("testInterpretGetPackageClass started");
    
    OpenDefinitionsDocument doc = setupDocument(CLASS_IN_PACKAGE_CLASS_TEXT);

    final File dir = tempDirectory();
    final File packDir = new File(dir, "foo");
    packDir.mkdir();
    final File file = new File(packDir, "Bar.scala");
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
    File compiled = classForScala(file, "Bar");
    assertTrue(_name() + "Class file should exist after compile", compiled.exists());    
    
    String classFilePath = compiled.getParentFile().getPath();
    String fooPath = classFilePath.substring(0, classFilePath.lastIndexOf("foo"));
    _interpreter.addCP("BuildDirectoryCP", fooPath);
    
    try {
      String res = _interpreter.interpret(
        "val f = new foo.Bar().getClass().getPackage().getName()");
      assertEquals("Package of foo.Bar should be foo", "f: java.lang.String = foo", res.trim());
    }
    catch (Throwable t) {
      fail("testInterpretGetPackageClass threw: " + t);
    }
  }
  
  /** Test that we get the right package using getPackage() with anonymous inner classes defined in the Interactions Pane. */
  public void testInterpretGetPackageAnonymous() {
    //throws BadLocationException, IOException, InterruptedException, InterpreterException {
    _log.log("testInterpretGetPackageAnonymous started");

    try {
      String res = _interpreter.interpret(
        "val n = new Runnable() { def run() { } }.getClass().getPackage()");
      assertEquals("package should be null", "n: java.lang.Package = null", res.trim());
    }
    catch (Throwable t) {
      fail("testInterpretGetPackageClass threw: " + t);
    }
  }
}
