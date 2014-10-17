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
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.drjava.model.FileSaveSelector;

import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.plt.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

import java.rmi.RemoteException;

/** Tests the functionality of an InteractionsModel.  The synchronization in this class is abominable; many interactions
  * document locking and "event queue only" invariants are violated.  The ubiquitous workaround is to call 
  * Utilities.clearEventQueue().
  * TODO: completely revise this class.
  * @version $Id: InteractionsModelTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class InteractionsModelTest extends DrJavaTestCase {
  
  private static Log _log = new Log("InteractionsModelTest.txt", false);
  protected InteractionsDJDocument _adapter;
  protected InteractionsModel _model;
  
  public InteractionsModelTest(String name) {
    super(name);
    _adapter = new InteractionsDJDocument();
    _model = new TestInteractionsModel(_adapter);
  }
  
  public void tearDown() throws Exception {
    // dispose the the AbstractMasterJVM supervising the MainJVM if it exists
    if (_model instanceof IncompleteInputInteractionsModel) ((IncompleteInputInteractionsModel) _model).dispose();
    _model = null;
    _adapter = null;
    super.tearDown();
  }
  
  /** Asserts that the given string typed by the user is processed to become the given expected string for an
    * interpretation.
    * @param typed A string typed by the user
    * @param expected What the processor should return
    */
  protected void _assertProcessedContents(final String typed, final String expected) throws Exception {
    assertTrue(_model instanceof TestInteractionsModel);
    final TestInteractionsModel model = (TestInteractionsModel)_model;
    final InteractionsDocument doc = model.getDocument();
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        doc.reset("This is a test"); 
        doc.append(typed, InteractionsDocument.DEFAULT_STYLE); 
      }
    });
    
//    Utilities.clearEventQueue();
    model._logInteractionStart();
    model.interpretCurrentInteraction();
    model._waitInteractionDone();
    
//    Utilities.clearEventQueue();
    assertEquals("processed output should match expected", expected, model.toEval);
  }
  
  /** Asserts that the given string typed by the user of the form "java classname" is transformed to the given
    * expected main method invocation.  An arbitrary prefix may precede the expected string.
    * @param typed the "java classname args ..." typed by the user
    * @param expected the expected main class call
    */
  protected void _assertJavaTransformationTail(final String typed, final String expected) {
    assertTrue("main transformation should match expected",
               edu.rice.cs.drjava.model.compiler.JavacCompiler.transformJavaCommand(typed).endsWith(expected));
  }

  /** Asserts that the given string typed by the user of the form "applet classname" is transformed to the given
    * expected applet invocation.
    * @param typed the "applet classname args ..." typed by the user
    * @param expected the expected applet invocation
    */
  protected void _assertAppletTransformation(final String typed, final String expected) {
    assertEquals("applet transformation should match expected",
                 expected, 
                 edu.rice.cs.drjava.model.compiler.JavacCompiler.transformAppletCommand(typed));
  }
  
  /** Tests that the correct text is returned when interpreting. */
  public void testInterpretCurrentInteraction() throws Exception {
    _log.log("testInterpretCurrentInteraction started");
    assertTrue(_model instanceof TestInteractionsModel);
    final TestInteractionsModel model = (TestInteractionsModel) _model;
    final InteractionsDocument doc = model.getDocument();
    model._logInteractionStart();
    model.interpretCurrentInteraction();
    model._waitInteractionDone();
    
//    Utilities.clearEventQueue();
    assertEquals("string being interpreted", "", model.toEval);
    
    final String code = "int x = 3;";
    // Insert text and evaluate
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        doc.append(code, InteractionsDocument.DEFAULT_STYLE); // spawns an event queue task
      } 
    });
    
//    Utilities.clearEventQueue();
//    System.err.println("doc = '" + doc.getText() + "'");
    assertTrue("Code appended correctly to interactions document", doc.getText().endsWith(code));
//    System.err.println("currentInteraction = '" + doc.getCurrentInteraction() + "'");
    
//    Utilities.clearEventQueue();
    assertTrue("Current interaction text is correct", doc.getCurrentInteraction().equals(code));
    
    model._logInteractionStart();
    model.interpretCurrentInteraction(); // runs in the event queue 
    model._waitInteractionDone();

//    Utilities.clearEventQueue();
    assertEquals("string being interpreted", code, model.toEval);
    _log.log("testInterpretCurrentInteraction ended");
  }
  
  // this test uses thread pools and starts a THRAD_EXECUTOR-n thread that we cannot join
  // NOTE: this test, as written, is not applicable for drscala, as ContinuationExceptions and SyntaxExceptions are not used
  public void deleteThisVersion_testInterpretCurrentInteractionWithIncompleteInput_NOJOIN() throws EditDocumentException, InterruptedException,
    RemoteException {
    _log.log("testInterpretCurrentInteractionWithIncompleteInput started");
    _model = new IncompleteInputInteractionsModel(_adapter);   // override the one initialized in setUp()
    assertReplThrewContinuationException("def m() = {");
    assertReplThrewContinuationException("def m() {");
    assertReplThrewContinuationException("1 + ");
    assertReplThrewContinuationException("(1+2");
    assertReplThrewSyntaxException("(1+2;");
    assertReplThrewContinuationException("val x = ");
    _log.log("testInterpretCurrentInteractionWithIncompleteInput ended");
  }
  
  /** Not a test method,  Assumes that _model is an IncompleteInputInteractionsModel. */
  protected void assertReplThrewContinuationException(final String code) throws EditDocumentException, InterruptedException {
    assertTrue(_model instanceof IncompleteInputInteractionsModel);
    final IncompleteInputInteractionsModel model = (IncompleteInputInteractionsModel) _model;
    final InteractionsDocument doc = model.getDocument();
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        doc.reset("This is a test");
        doc.append(code, InteractionsDocument.DEFAULT_STYLE);
      }
    });
    
//    Utilities.clearEventQueue();
    model._logInteractionStart();
    model.interpretCurrentInteraction();
    
//    Utilities.clearEventQueue();
    _log.log("Waiting for InteractionDone()");
    model._waitInteractionDone();

//    Utilities.clearEventQueue();
    assertTrue("Code '" + code + "' should generate a continuation exception but not a syntax exception",
               (model.isContinuationException() == true) && (model.isSyntaxException() == false));
  }
  
  /** Not a test method,  Assumes that _model is an IncompleteInputInteractionsModel. */
  protected void assertReplThrewSyntaxException(final String code) throws EditDocumentException, InterruptedException {
    assertTrue(_model instanceof IncompleteInputInteractionsModel);
    final IncompleteInputInteractionsModel model = (IncompleteInputInteractionsModel)_model;
    final InteractionsDocument doc = model.getDocument();
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        doc.reset("This is a test");
        doc.append(code, InteractionsDocument.DEFAULT_STYLE);
      }
    });
//    Utilities.clearEventQueue();

    model._logInteractionStart();
    model.interpretCurrentInteraction();
    model._waitInteractionDone();

//    Utilities.clearEventQueue();
    assertTrue("Code '" + code +  "' should generate a syntax exception but not a continuation exception",
               (model.isSyntaxException() == true) && (model.isContinuationException() == false));
  }
    
  /** Tests that "java Classname [args]" runs the class's main method, with simple delimited arguments. */
  public void testInterpretJavaArguments() {
    _log.log("testInterpretJavaArguments started");
    // java Foo a b c
    // Foo.main(new String[]{"a", "b", "c"});
    _assertJavaTransformationTail("java Foo a b c", "Foo.main(new String[]{\"a\",\"b\",\"c\"});");
    // java Foo "a b c"
    // Foo.main(new String[]{"a b c"});
    _assertJavaTransformationTail("java Foo \"a b c\"", "Foo.main(new String[]{\"a b c\"});");
    // java Foo "a b"c d
    // Foo.main(new String[]{"a bc", "d"});
    //  This is different behavior than Unix or DOS, but it's more
    //  intuitive to the user (and easier to implement).
    _assertJavaTransformationTail("java Foo \"a b\"c d", "Foo.main(new String[]{\"a bc\",\"d\"});");
    
    // java Foo c:\\file.txt
    // Foo.main("c:\\file.txt");
    _assertJavaTransformationTail("java Foo c:\\\\file.txt", "Foo.main(new String[]{\"c:\\\\file.txt\"});");
    
    // java Foo /home/user/file
    // Foo.main("/home/user/file");
    _assertJavaTransformationTail("java Foo /home/user/file", "Foo.main(new String[]{\"/home/user/file\"});");
    _log.log("testInterpretJavaArguments ended");
  }
  
  /** Tests that escaped characters just return the character itself.  Escaped whitespace is considered a character, 
    * not a delimiter. (This is how Unix behaves.)
    *
    * not currently enforcing any behavior for a simple implementation using a StreamTokenizer
    */
  public void testInterpretJavaEscapedArgs() {
    _log.log("testInterpretJavaEscapedArgs started");
    // java Foo \j
    // Foo.main(new String[]{"j"});
    _assertJavaTransformationTail("java Foo \\j", "Foo.main(new String[]{\"j\"});");
    // java Foo \"
    // Foo.main(new String[]{"\""});
    _assertJavaTransformationTail("java Foo \\\"", "Foo.main(new String[]{\"\\\"\"});");
    // java Foo \\
    // Foo.main(new String[]{"\\"});
    _assertJavaTransformationTail("java Foo \\\\", "Foo.main(new String[]{\"\\\\\"});");
    // java Foo a\ b
    // Foo.main(new String[]{"a b"});
    _assertJavaTransformationTail("java Foo a\\ b", "Foo.main(new String[]{\"a b\"});");
    _log.log("testInterpretJavaEscapedArgs ended");
  }
  
  /** Tests that within a quote, everything is correctly escaped.
    * (Special characters are passed to the program correctly.)
    */
  public void testInterpretJavaQuotedEscapedArgs() {
    _log.log("testInterpretJavaQuotedEscapedArgs started");
    // java Foo "a \" b"
    // Foo.main(new String[]{"a \" b"});
    _assertJavaTransformationTail("java Foo \"a \\\" b\"", "Foo.main(new String[]{\"a \\\" b\"});");
    // java Foo "\'"
    // Foo.main(new String[]{"\\'"});
    _assertJavaTransformationTail("java Foo \"\\'\"", "Foo.main(new String[]{\"\\\\'\"});");
    // java Foo "\\"
    // Foo.main(new String[]{"\\"});
    _assertJavaTransformationTail("java Foo \"\\\\\"", "Foo.main(new String[]{\"\\\\\"});");
    // java Foo "\" \d"
    // Foo.main(new String[]{"\" \\d"});
    _assertJavaTransformationTail("java Foo \"\\\" \\d\"", "Foo.main(new String[]{\"\\\" \\\\d\"});");
    // java Foo "\n"
    // Foo.main(new String[]{"\n"});
    /*    _assertJavaTransformation("java Foo \"\\n\"", "Foo.main(new String[]{\"\\n\"});");
     // java Foo "\t"
     // Foo.main(new String[]{"\t"});
     _assertJavaTransformation("java Foo \"\\t\"", "Foo.main(new String[]{\"\\t\"});");
     // java Foo "\r"
     // Foo.main(new String[]{"\r"});
     _assertJavaTransformation("java Foo \"\\r\"", "Foo.main(new String[]{\"\\r\"});");
     // java Foo "\f"
     // Foo.main(new String[]{"\f"});
     _assertJavaTransformation("java Foo \"\\f\"", "Foo.main(new String[]{\"\\f\"});");
     // java Foo "\b"
     // Foo.main(new String[]{"\b"});
     _assertJavaTransformation("java Foo \"\\b\"", "Foo.main(new String[]{\"\\b\"});"); */
    _log.log("testInterpretJavaQuotedEscapedArgs started");
  }
  
  /** Tests that single quotes can be used as argument delimiters. */
  public void testInterpretJavaSingleQuotedArgs() {
    _log.log("testInterpretJavaSingleQuotedArgs started");
    // java Foo 'asdf'
    _assertJavaTransformationTail("java Foo 'asdf'", "Foo.main(new String[]{\"asdf\"});");
    
    // java Foo 'a b c'
    _assertJavaTransformationTail("java Foo 'a b c'", "Foo.main(new String[]{\"a b c\"});");
    
    // java Foo 'a b'c
    _assertJavaTransformationTail("java Foo 'a b'c", "Foo.main(new String[]{\"a bc\"});");
     _log.log("testInterpretJavaSingleQuotedArgs ended");
  }
  
  /** Tests that "applet Classname [args]" runs the class's main method, with simple delimited arguments. */
  public void testInterpretAppletArguments() {
    _log.log("testInterpretAppletArguments started");
    // applet Foo a b c
    // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("a","b","c"), 400, 300);
    _assertAppletTransformation("applet Foo a b c", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"a\",\"b\",\"c\"), 400, 300);");
    // applet Foo "a b c"
    // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("a b c"), 400, 300);
    _assertAppletTransformation("applet Foo \"a b c\"", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"a b c\"), 400, 300);");
    // applet Foo "a b"c d
    // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("a bc","d"), 400, 300);
    //  This is different behavior than Unix or DOS, but it's more
    //  intuitive to the user (and easier to implement).
    _assertAppletTransformation("applet Foo \"a b\"c d", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"a bc\",\"d\"), 400, 300);");
    
    // applet Foo c:\\file.txt
    // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("c:\\file.txt"), 400, 300);
    _assertAppletTransformation("applet Foo c:\\\\file.txt", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"c:\\\\file.txt\"), 400, 300);");
    
    // applet Foo /home/user/file
    // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("/home/user/file"), 400, 300);
    _assertAppletTransformation("applet Foo /home/user/file", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"/home/user/file\"), 400, 300);");
    _log.log("testInterpretAppletArguments ended");
  }
  
  /** Tests that escaped characters just return the character itself.  Escaped whitespace is considered a character, 
    * not a delimiter. (This is how Unix behaves.)
    *
    * not currently enforcing any behavior for a simple implementation using a StreamTokenizer
    */
  public void testInterpretAppletEscapedArgs() {
    _log.log("testInterpretAppletEscapedArgs started");
    // applet Foo \j
    // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("j"), 400, 300);
    _assertAppletTransformation("applet Foo \\j", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"j\"), 400, 300);");
    // applet Foo \"
    // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("\""), 400, 300);
    _assertAppletTransformation("applet Foo \\\"", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"\\\"\"), 400, 300);");
    // applet Foo \\
    // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("\\"), 400, 300);
    _assertAppletTransformation("applet Foo \\\\", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"\\\\\"), 400, 300);");
    // applet Foo a\ b
    // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("a b"), 400, 300);
    _assertAppletTransformation("applet Foo a\\ b", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"a b\"), 400, 300);");
    _log.log("testInterpretAppletEscapedArgs ended");
  }
  
  /** Tests that within a quote, everything is correctly escaped.
    * (Special characters are passed to the program correctly.)
    */
  public void testInterpretAppletQuotedEscapedArgs() {
    _log.log("testInterpretAppletQuotedEscapedArgs started");
    // applet Foo "a \" b"
    // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("a \" b"), 400, 300);
    _assertAppletTransformation("applet Foo \"a \\\" b\"", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"a \\\" b\"), 400, 300);");
    // applet Foo "\'"
    // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("\\'"), 400, 300);
    _assertAppletTransformation("applet Foo \"\\'\"", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"\\\\'\"), 400, 300);");
    // applet Foo "\\"
    // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("\\"), 400, 300);
    _assertAppletTransformation("applet Foo \"\\\\\"", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"\\\\\"), 400, 300);");
    // applet Foo "\" \d"
    // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("\" \\d"), 400, 300);
    _assertAppletTransformation("applet Foo \"\\\" \\d\"", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"\\\" \\\\d\"), 400, 300);");
    // applet Foo "\n"
    // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("\n"), 400, 300);
    /*    _assertAppletTransformation("applet Foo \"\\n\"", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"\\n\"), 400, 300);");
     // applet Foo "\t"
     // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("\t"), 400, 300);
     _assertAppletTransformation("applet Foo \"\\t\"", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"\\t\"), 400, 300);");
     // applet Foo "\r"
     // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("\r"), 400, 300);
     _assertAppletTransformation("applet Foo \"\\r\"", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"\\r\"), 400, 300);");
     // applet Foo "\f"
     // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("\f"), 400, 300);
     _assertAppletTransformation("applet Foo \"\\f\"", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"\\f\"), 400, 300);");
     // applet Foo "\b"
     // edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo("\b"), 400, 300);
     _assertAppletTransformation("applet Foo \"\\b\"", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"\\b\"), 400, 300);"); */
    _log.log("testInterpretAppletQuotedEscapedArgs started");
  }
  
  /** Tests that single quotes can be used as argument delimiters. */
  public void testInterpretAppletSingleQuotedArgs() {
    _log.log("testInterpretAppletSingleQuotedArgs started");
    // applet Foo 'asdf'
    _assertAppletTransformation("applet Foo 'asdf'", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"asdf\"), 400, 300);");
    
    // applet Foo 'a b c'
    _assertAppletTransformation("applet Foo 'a b c'", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"a b c\"), 400, 300);");
    
    // applet Foo 'a b'c
    _assertAppletTransformation("applet Foo 'a b'c", "edu.rice.cs.plt.swing.SwingUtil.showApplet(new Foo(\"a bc\"), 400, 300);");
     _log.log("testInterpretAppletSingleQuotedArgs ended");
  }
  
  //public void testLoadHistory();
  // TO DO: test that the correct history is returned (careful of last newline)
  
  
  /** Tests that a debug port can be generated. */
  public void testDebugPort() throws IOException {
     _log.log("testDebugPort started");
    int port = _model.getDebugPort();
    assertTrue("generated debug port", port != -1);
    
    // Resetting after startUp should change the port
    _model.setWaitingForFirstInterpreter(false);
    _model.interpreterResetting();
    int newPort = _model.getDebugPort();
    assertTrue("debug port should change", newPort != port);
    
    // Set port
    _model.setDebugPort(5);
    assertEquals("manually set debug port", 5, _model.getDebugPort());
    
    // Port should stay -1 after setting it
    _model.setDebugPort(-1);
    assertEquals("debug port should be -1", -1, _model.getDebugPort());
    _log.log("testDebugPort ended");
  }
  
  /** Tests that an interactions history can be loaded in as a script. */
  public void testScriptLoading() throws Exception {
    _log.log("testScriptLoading started");
    assertTrue(_model instanceof TestInteractionsModel);
    final TestInteractionsModel model = (TestInteractionsModel)_model;
    // Set up a sample history
    String line1 = "System.out.println(\"hi\")";
    String line2 = "System.out.println(\"bye\")";
//    String delim = History.INTERACTION_SEPARATOR + StringOps.EOL;
    final File temp = File.createTempFile("drscala-test", ".hist").getCanonicalFile();
    temp.deleteOnExit();
    History history = new History(5);
    history.add(line1);
    history.add(line2);
    
    history.writeToFile(new FileSaveSelector() {
      public File getFile() { return temp; }
      public boolean warnFileOpen(File f) { return true; }
      public boolean verifyOverwrite(File f) { return true; }
      public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) { return true; }
      public boolean shouldUpdateDocumentState() { return true; }
    });
    
    // Load the history as a script
    final InteractionsScriptModel ism = model.loadHistoryAsScript(new FileOpenSelector() {
      public File[] getFiles() {
        return new File[] { temp };
      }
    });
    final InteractionsDocument doc = model.getDocument();
    
    // Should not be able to get the previous interaction
    assertTrue("Should have no previous", !ism.hasPrevInteraction());
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        try {
          ism.prevInteraction();
          fail("Should not have been able to get previous interaction!");
        }
        catch (IllegalStateException ise) { /* good, continue */ }
      }
    });
    
    // Get the next (first) interaction
    assertTrue("Should have next", ism.hasNextInteraction());
    Utilities.invokeAndWait(new Runnable() { public void run() { ism.nextInteraction(); } });
    
//    Utilities.clearEventQueue();
    assertEquals("Should have put the first line into the document.", line1, doc.getCurrentInteraction());
    
    // Still should not be able to get the previous interaction
    assertTrue("Should have no previous", !ism.hasPrevInteraction());
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        try {
          ism.prevInteraction();
          fail("Should not have been able to get previous interaction!");
        }
        catch (IllegalStateException ise) { /* good, continue */ }
      }
    });
    // Skip it; get the next (second) interaction
    assertTrue("Should have next", ism.hasNextInteraction());
    Utilities.invokeAndWait(new Runnable() { public void run() { ism.nextInteraction(); } });
//    Utilities.clearEventQueue();
    assertEquals("Should have put the second line into the document.", line2, doc.getCurrentInteraction());
    
    // Now we should be able to get the previous interaction
    assertTrue("Should have previous", ism.hasPrevInteraction());
    Utilities.invokeAndWait(new Runnable() { public void run() { ism.prevInteraction(); } });
//    Utilities.clearEventQueue();
    assertEquals("Should have put the first line into the document.", line1, doc.getCurrentInteraction());
    
    // Go back to the second line and execute it
    Utilities.invokeAndWait(new Runnable() { public void run() { ism.nextInteraction(); } });
//    Utilities.clearEventQueue();
    
    model._logInteractionStart();
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { ism.executeInteraction(); } 
    });
    model._waitInteractionDone();

    assertEquals("Should have \"executed\" the second interaction.", line2, model.toEval);
    
    // Should not be able to get the next interaction, since we're at the end
    assertTrue("Should have no next", !ism.hasNextInteraction());
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        try {
          ism.nextInteraction();
          fail("Should not have been able to get next interaction!");
        }
        catch (IllegalStateException ise) { /* good, continue */ }
      }
    });
    
//    Utilities.clearEventQueue();
    // Get Previous should return the most recently executed interaction
    assertTrue("Should have previous", ism.hasPrevInteraction());
    
    Utilities.invokeAndWait(new Runnable() { public void run() { ism.prevInteraction(); } });
//    Utilities.clearEventQueue();
    assertEquals("Should have put the second line into the document.", line2, doc.getCurrentInteraction());
    
    // Get Previous should now return the first interaction
    assertTrue("Should have previous", ism.hasPrevInteraction());
    
    Utilities.invokeAndWait(new Runnable() { public void run() { ism.prevInteraction(); } });
//    Utilities.clearEventQueue();
//    System.err.println("Interaction is '" + doc.getCurrentInteraction() + "'");
    assertEquals("Should have put the first line into the document.", line1, doc.getCurrentInteraction());
    
    // Should have no more previous
    assertFalse("Should have no previous", ism.hasPrevInteraction());
    
//    System.err.println("Current interaction for line 428 is " + doc.getCurrentInteraction());
//    System.err.println("line1 = '" + line1 + "'");
    model._logInteractionStart();
    // Now execute the first interaction
    Utilities.invokeAndWait(new Runnable() { public void run() { ism.executeInteraction();  } });
    model._waitInteractionDone();

//    System.err.println("model.toEval = '" + model.toEval + "'");  
    assertEquals("Should have \"executed\" the first interaction.", line1, model.toEval);
    
    // Get Previous should return the most recent (first) interaction
    assertTrue("Should have previous", ism.hasPrevInteraction());
    
    Utilities.invokeAndWait(new Runnable() { public void run() { ism.prevInteraction(); } });
//    Utilities.clearEventQueue();
    assertEquals("Should have put the first line into the document.", line1, doc.getCurrentInteraction());
    
    // Should not be able to get the previous interaction this time
    assertTrue("Should have no previous", !ism.hasPrevInteraction());
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        try {
          ism.prevInteraction();
          fail("Should not have been able to get previous interaction!");
        }
        catch (IllegalStateException ise) { /* good, continue */ }
      }
    });
    
    _log.log("testScriptLoading ended");
  }
  
  /** Tests that setting and changing an input listener works correctly. Many actions should be moved to the
    * event thread. */
  public void testSetChangeInputListener() {
    _log.log("testSetChangeInputListener started");
    InputListener listener1 = new InputListener() {
      public String getConsoleInput() { return "input1"; }
    };
    
    InputListener listener2 = new InputListener() {
      public String getConsoleInput() { return "input2"; }
    };
    
    try {
      _model.getConsoleInput();
      fail("Should not have allowed getting input before a listener is installed!");
    }
    catch (IllegalStateException ise) {
      assertEquals("Should have thrown the correct exception.",
                   "No input listener installed!", ise.getMessage());
    }
    
    _model.setInputListener(listener1);
    assertEquals("First input listener should return correct input", "input1", _model.getConsoleInput());
    _model.changeInputListener(listener1, listener2);
    assertEquals("Second input listener should return correct input", "input2", _model.getConsoleInput());
    _log.log("testSetChangeInputListener ended");
  }
  
  /** Tests that the interactions history is stored correctly. See bug # 992455 */
  public void testInteractionsHistoryStoredCorrectly() throws Exception {
    _log.log("testInteractionsHistoryStoredCorrectly started");
    final String code = "public class A {\n";
    
    _model = new BadSyntaxInteractionsModel(_adapter);  // replaces model created by setUp()
    final BadSyntaxInteractionsModel model = (BadSyntaxInteractionsModel) _model;
    
    final InteractionsDocument doc = model.getDocument();
    
    // Insert text and evaluate
    model._logInteractionStart();
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        doc.insertText(doc.getLength(), code, InteractionsDocument.DEFAULT_STYLE);
        model.setSyntaxErrorStrings("Encountered Unexpected \"<EOF>\"", "public class A {\n");
      }
    });
//    Utilities.clearEventQueue();
    model.interpretCurrentInteraction();
    model._waitInteractionDone();
    
    //Simulate result
//    _model.replReturnedSyntaxError("Encountered Unexpected \"<EOF>\"", "public class A {\n", -1, -1, -1, -1);
    
    String expected = "public class A {\n" + "\n";  // last term was StringOps.EOL but Swing uses '\n' for newLIne
    String result = doc.getCurrentInteraction();
//    Utilities.clearEventQueue();
//    System.err.println("expected = '" + expected + "' length = " + expected.length());
//    System.err.println("result = '" + result + "' length = " + result.length());
    assertEquals("Current interaction should still be there - should not have interpreted", expected, result);
    History h = doc.getHistory();
    assertEquals("History should be empty", 0, h.size());
    
    final String code1 = "}\n";
    model.disableSyntaxError();
    model._logInteractionStart();
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { doc.insertText(doc.getLength(), code1, InteractionsDocument.DEFAULT_STYLE); }
    });
//    Utilities.clearEventQueue();
    _model.interpretCurrentInteraction();
    model._waitInteractionDone();
    
    assertEquals("Current interaction should not be there - should have interpreted", "", doc.getCurrentInteraction());
    assertEquals("History should contain one interaction", 1, h.size());
    _log.log("testInteractionsHistoryStoredCorrectly ended");
  }
  
  /** A generic InteractionsModel for testing purposes.  (Used here, in InteractionsPaneTest,
    * and in InteractionsModelErrorTest.) */
  public static class TestInteractionsModel extends InteractionsModel {
    String toEval = null;
    String addedClass = null;
    
    private volatile boolean _interactionDone = false;
    private final Object _interactionLock = new Object();
    
    public void _logInteractionStart() { _interactionDone = false; }
    
    public void _waitInteractionDone() throws InterruptedException { 
      synchronized(_interactionLock) { 
        while (! _interactionDone) _interactionLock.wait(); }
    }
    
    /** Constructs a new InteractionsModel. */
    public TestInteractionsModel(InteractionsDJDocument adapter) {
      // Adapter, history size, write delay
      super(adapter, new File(System.getProperty("user.dir")), 1000, 25);
    }
    
    /** Sets toEval field and simulates successful interpretation. */
    protected void _interpret(String toEval) {
//      System.err.println("interpret setting toEval to " + toEval);
      this.toEval = toEval; 
      replReturnedVoid(); // imitate completed call
    }
    
    protected void _notifyInteractionEnded() { 
      _log.log("_notifyInteractionEnded called.");
      synchronized(_interactionLock) {
        _interactionDone = true;
        _interactionLock.notify();
      }
    }
        
    public Pair<String,String> getVariableToString(String var) {
      fail("cannot getVariableToString in a test");
      return null;
    }
    
    public void addProjectClassPath(File path) { fail("cannot add to classpath in a test"); }
    public void addBuildDirectoryClassPath(File path) { fail("cannot add to classpath in a test"); }
    public void addProjectFilesClassPath(File path) { fail("cannot add to classpath in a test"); }
    public void addExternalFilesClassPath(File path) { fail("cannot add to classpath in a test"); }
    public void addExtraClassPath(File path) { fail("cannot add to classpath in a test"); }
    protected void _resetInterpreter(File wd, boolean force) { fail("cannot reset interpreter in a test"); }
    public List<File> getCompilerBootClassPath() {
      // TODO: figure out what to do here
      return new ArrayList<File>();
    }
    public String transformCommands(String interactionsString) {
      // TODO: figure out what to do here
      return interactionsString;
    }
    
    public void _notifyInteractionStarted() { }
    protected void _notifySyntaxErrorOccurred(int offset, int length) { }
    protected void _notifyInterpreterExited(int status) { }
    protected void _notifyInterpreterResetting() { }
    protected void _notifyInterpreterResetFailed(Throwable t) { }
    public void _notifyInterpreterReady(File wd) { }
    protected void _interpreterResetFailed(Throwable t) { }
    protected void _interpreterWontStart(Exception e) { }
    protected void _notifyInteractionIncomplete() { }
    public ConsoleDocument getConsoleDocument() { return null; }
  }
  
  /** This test model can simulate a syntax error in interpretation. */
  private static class BadSyntaxInteractionsModel extends TestInteractionsModel {
    
    private String errorString1, errorString2;
    private boolean errorPresent = false;
    
    BadSyntaxInteractionsModel(InteractionsDJDocument adapter) { super(adapter); }
    
    protected void setSyntaxErrorStrings(String s1, String s2) { 
      errorString1 = s1; 
      errorString2 = s2; 
      errorPresent = true;
    }
    
    protected void disableSyntaxError() { errorPresent = false; }
    
    /** Simulates a syntax error in interpretation. */
    protected void _interpret(String toEval) {
//      System.err.println("interpret setting toEval to " + toEval);
      this.toEval = toEval; 
      if (errorPresent) replReturnedSyntaxError(errorString1, errorString2, -1, -1, -1, -1); // imitate return with syntax error
      else replReturnedVoid();  // imitate successful return
    }
  }

  
  /** This test model includes a slave JVM, just like a DefaultGlobalModel.  It must be disposed before it is
    * deallocated to kill the slave JVM.   TODO: the mutation in this class is disgusting -- Corky  2 June 06.
    */
  public static class IncompleteInputInteractionsModel extends RMIInteractionsModel {
    boolean continuationException;  // This appears to be the negation of syntaxException making it redundant!
    boolean syntaxException;
    
    private volatile boolean _interactionDone = false;
    private final Object _interactionLock = new Object();  // distinct from _interactionLock in InteractionsModel
    
    public void _logInteractionStart() { _interactionDone = false; }
    
    public void _waitInteractionDone() throws InterruptedException { 
      synchronized(_interactionLock) { 
        while (! _interactionDone) _interactionLock.wait(); }
    }
    
    /** Constructs a new IncompleteInputInteractionsModel. */
    public IncompleteInputInteractionsModel(InteractionsDJDocument adapter) throws RemoteException {
      // MainJVM, Adapter, history size, write delay
      super(new MainJVM(null), adapter, new File(System.getProperty("user.dir")), 1000, 25);
      _jvm.setInteractionsModel(this); // _jvm is set to MainJVM(null) by super call;
      _jvm.startInterpreterJVM();
      continuationException = false;
      syntaxException = false;
    }
    
    public void _notifyInteractionStarted() { }
    protected void _notifyInteractionEnded() { 
      _log.log("_notifyInteractionEnded called.");
      synchronized(_interactionLock) {
        _interactionDone = true;
        _interactionLock.notify();
      }
    }
    protected void _notifySyntaxErrorOccurred(int offset, int length) { }
    protected void _notifyInterpreterExited(int status) { }
    protected void _notifyInterpreterResetting() { }
    protected void _notifyInterpreterResetFailed(Throwable t) { }
    public void _notifyInterpreterReady(File wd) { }
    protected void _interpreterResetFailed(Throwable t) { }
    protected void _interpreterWontStart(Exception e) { }
    protected void _notifyInteractionIncomplete() { _notifyInteractionEnded(); }
    protected void _notifyInterpreterChanged(boolean inProgress) { }
    
    public void dispose() throws RemoteException { _jvm.dispose(); }
    
    public ConsoleDocument getConsoleDocument() { return null; }
    
    @Override public void replThrewException(String message, StackTraceElement[] stackTrace) {
      StringBuilder sb = new StringBuilder(message);
      for(StackTraceElement ste: stackTrace) {
        sb.append("\n\tat ");
        sb.append(ste);
      }
      replThrewException(sb.toString().trim());
    }
    
    @Override public void replThrewException(String message) {
      _log.log("replThrewException called");
      if (message != null) {
        if (message.endsWith("<EOF>\"")) {
          continuationException = true;
          syntaxException = false;
          _interactionIsOver();
          return;
        }
      }
      syntaxException = true; // ? -- Why is this a syntaxException?
      continuationException = false;
      _interactionIsOver();
    }
    
    @Override public void replReturnedSyntaxError(String errorMessage, String interaction, int startRow, int startCol, int endRow,
                                                  int endCol) {
      _log.log("replReturnedSyntaxError called");
      if (errorMessage != null) {
        if (errorMessage.endsWith("<EOF>\"")) {
          continuationException = true;
          syntaxException = false;
          _interactionIsOver();
          return;
        }
      }
      syntaxException = true;
      continuationException = false;
      _interactionIsOver();
    }
    
    public boolean isContinuationException() { return continuationException; }
    public boolean isSyntaxException() { return syntaxException; }
    
    public List<File> getCompilerBootClassPath() {
      // TODO: figure out what to do here      
      return new ArrayList<File>();
    }
    
    public String transformCommands(String interactionsString) {
      // TODO: figure out what to do here
      return interactionsString;
    }
  }
  
//  public class TestInteractionsListener extends DummyInteractionsListener {
//    private volatile boolean _interactionDone = false;       // records when the interaction is done
//    private final Object _interactionLock = new Object();    // lock for _interactionDone
//    
//    /** Relying on the default constructor. */
//    
//    public void interactionEnded() {
//      synchronized(_interactionLock) { 
//        _interactionDone = true;
//        _interactionLock.notify(); 
//      }
//    }
//    public void logInteractionStart() { _interactionDone = false; }
//    
//    public void waitInteractionDone() throws InterruptedException {
//      synchronized(_interactionLock) { while (! _interactionDone) _interactionLock.wait(); }
//    }
//  }
}
