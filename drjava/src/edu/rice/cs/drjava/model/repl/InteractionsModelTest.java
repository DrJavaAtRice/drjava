/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.drjava.model.FileSaveSelector;

import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.util.text.EditDocumentException;

import java.io.File;
import java.io.IOException;

import java.rmi.RemoteException;

/** Tests the functionality of an InteractionsModel.
 *  @version $Id$
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
   *  interpretation.
   *  @param typed A string typed by the user
   *  @param expected What the processor should return
   */
  protected void _assertProcessedContents(String typed, String expected) throws EditDocumentException {
    assertTrue(_model instanceof TestInteractionsModel);
    TestInteractionsModel model = (TestInteractionsModel)_model;
    InteractionsDocument doc = model.getDocument();
    doc.reset("This is a test");
    doc.append(typed, InteractionsDocument.DEFAULT_STYLE);
    model.interpretCurrentInteraction();
    assertEquals("processed output should match expected", expected, model.toEval);
  }

  /** Asserts that the given string typed by the user of the form "java classname" is transformed to the given
   *  expected main method invocation.
   *  @param typed the "java classname args ..." typed by the user
   *  @param expected the expected main class call
   */
  protected void _assertMainTransformation(String typed, String expected) {
    assertEquals("main transformation should match expected", expected, TestInteractionsModel._testClassCall(typed));
  }

  /** Tests that the correct text is returned when interpreting. */
  public void testInterpretCurrentInteraction() throws EditDocumentException {
    assertTrue(_model instanceof TestInteractionsModel);
    TestInteractionsModel model = (TestInteractionsModel) _model;
    String code = "int x = 3;";
    InteractionsDocument doc = model.getDocument();
    model.interpretCurrentInteraction();
    // pretend the call completed
    model.replReturnedVoid();
    assertEquals("string being interpreted", "", model.toEval);

    // Insert text and evaluate
    doc.append(code, InteractionsDocument.DEFAULT_STYLE);
    model.interpretCurrentInteraction();
    // pretend the call completed
    model.replReturnedVoid();
    assertEquals("string being interpreted", code, model.toEval);
  }

  // Why do we create a new model (and slave JVM) for each of this trivial tests?
  public void testInterpretCurrentInteractionWithIncompleteInput() throws EditDocumentException, InterruptedException,
    RemoteException {
    _log.log("testInterpretCurrentInteractionWithIncompleteInput started");
    _model = new IncompleteInputInteractionsModel(_adapter);   // override the one initialized in setUp()
    assertReplThrewContinuationException("void m() {");
    assertReplThrewContinuationException("void m() {;");
    assertReplThrewContinuationException("1+");
    assertReplThrewContinuationException("(1+2");
    assertReplThrewSyntaxException("(1+2;");
    assertReplThrewContinuationException("for (;;");
  }

  protected void assertReplThrewContinuationException(String code) throws EditDocumentException, InterruptedException {
    assertTrue(_model instanceof IncompleteInputInteractionsModel);
    IncompleteInputInteractionsModel model = (IncompleteInputInteractionsModel) _model;
    InteractionsDocument doc = model.getDocument();
    doc.reset("This is a test");
    doc.append(code, InteractionsDocument.DEFAULT_STYLE);
    model._logInteractionStart();
    model.interpretCurrentInteraction();
    _log.log("Waiting for InteractionDone()");
    model._waitInteractionDone();
    assertTrue("Code '"+code+"' should generate a continuation exception but not a syntax exception",
               (model.isContinuationException() == true) && (model.isSyntaxException() == false));
  }

  protected void assertReplThrewSyntaxException(String code) throws EditDocumentException, InterruptedException {
    assertTrue(_model instanceof IncompleteInputInteractionsModel);
    IncompleteInputInteractionsModel model = (IncompleteInputInteractionsModel)_model;
    InteractionsDocument doc = model.getDocument();
    doc.reset("This is a test");
    doc.append(code, InteractionsDocument.DEFAULT_STYLE);
    model._logInteractionStart();
    model.interpretCurrentInteraction();
    model._waitInteractionDone();
    assertTrue("Code '" + code +  "' should generate a syntax exception but not a continuation exception",
               (model.isSyntaxException() == true) && (model.isContinuationException() == false));
  }


  /** Tests that "java Classname [args]" runs the class's main method, with simple delimited arguments. */
  public void testInterpretJavaArguments() {
    // java Foo a b c
    // Foo.main(new String[]{"a", "b", "c"});
    _assertMainTransformation("java Foo a b c", "Foo.main(new String[]{\"a\",\"b\",\"c\"});");
    // java Foo "a b c"
    // Foo.main(new String[]{"a b c"});
    _assertMainTransformation("java Foo \"a b c\"", "Foo.main(new String[]{\"a b c\"});");
    // java Foo "a b"c d
    // Foo.main(new String[]{"a bc", "d"});
    //  This is different behavior than Unix or DOS, but it's more
    //  intuitive to the user (and easier to implement).
    _assertMainTransformation("java Foo \"a b\"c d", "Foo.main(new String[]{\"a bc\",\"d\"});");

    // java Foo c:\\file.txt
    // Foo.main("c:\\file.txt");
    _assertMainTransformation("java Foo c:\\\\file.txt", "Foo.main(new String[]{\"c:\\\\file.txt\"});");

    // java Foo /home/user/file
    // Foo.main("/home/user/file");
    _assertMainTransformation("java Foo /home/user/file", "Foo.main(new String[]{\"/home/user/file\"});");
  }

  /** Tests that escaped characters just return the character itself.  Escaped whitespace is considered a character, 
   *  not a delimiter. (This is how Unix behaves.)
   *
   *  not currently enforcing any behavior for a simple implementation using a StreamTokenizer
   */
  public void testInterpretJavaEscapedArgs() {
    // java Foo \j
    // Foo.main(new String[]{"j"});
    _assertMainTransformation("java Foo \\j", "Foo.main(new String[]{\"j\"});");
    // java Foo \"
    // Foo.main(new String[]{"\""});
    _assertMainTransformation("java Foo \\\"", "Foo.main(new String[]{\"\\\"\"});");
    // java Foo \\
    // Foo.main(new String[]{"\\"});
    _assertMainTransformation("java Foo \\\\", "Foo.main(new String[]{\"\\\\\"});");
    // java Foo a\ b
    // Foo.main(new String[]{"a b"});
    _assertMainTransformation("java Foo a\\ b", "Foo.main(new String[]{\"a b\"});");
  }

  /** Tests that within a quote, everything is correctly escaped.
   *  (Special characters are passed to the program correctly.)
   */
  public void testInterpretJavaQuotedEscapedArgs() {
    // java Foo "a \" b"
    // Foo.main(new String[]{"a \" b"});
    _assertMainTransformation("java Foo \"a \\\" b\"", "Foo.main(new String[]{\"a \\\" b\"});");
    // java Foo "\'"
    // Foo.main(new String[]{"\\'"});
    _assertMainTransformation("java Foo \"\\'\"", "Foo.main(new String[]{\"\\\\'\"});");
    // java Foo "\\"
    // Foo.main(new String[]{"\\"});
    _assertMainTransformation("java Foo \"\\\\\"", "Foo.main(new String[]{\"\\\\\"});");
    // java Foo "\" \d"
    // Foo.main(new String[]{"\" \\d"});
    _assertMainTransformation("java Foo \"\\\" \\d\"", "Foo.main(new String[]{\"\\\" \\\\d\"});");
    // java Foo "\n"
    // Foo.main(new String[]{"\n"});
/*    _assertMainTransformation("java Foo \"\\n\"",
                             "Foo.main(new String[]{\"\\n\"});");
    // java Foo "\t"
    // Foo.main(new String[]{"\t"});
    _assertMainTransformation("java Foo \"\\t\"",
                             "Foo.main(new String[]{\"\\t\"});");
    // java Foo "\r"
    // Foo.main(new String[]{"\r"});
    _assertMainTransformation("java Foo \"\\r\"",
                             "Foo.main(new String[]{\"\\r\"});");
    // java Foo "\f"
    // Foo.main(new String[]{"\f"});
    _assertMainTransformation("java Foo \"\\f\"",
                             "Foo.main(new String[]{\"\\f\"});");
    // java Foo "\b"
    // Foo.main(new String[]{"\b"});
    _assertMainTransformation("java Foo \"\\b\"",
                             "Foo.main(new String[]{\"\\b\"});"); */
  }

  /** Tests that single quotes can be used as argument delimiters. */
  public void testInterpretJavaSingleQuotedArgs() {
    
    // java Foo 'asdf'
    _assertMainTransformation("java Foo 'asdf'", "Foo.main(new String[]{\"asdf\"});");
    
    // java Foo 'a b c'
    _assertMainTransformation("java Foo 'a b c'", "Foo.main(new String[]{\"a b c\"});");

    // java Foo 'a b'c
    _assertMainTransformation("java Foo 'a b'c", "Foo.main(new String[]{\"a bc\"});");
  }

  //public void testLoadHistory();
  // TO DO: test that the correct history is returned (careful of last newline)


  /** Tests that a debug port can be generated. */
  public void testDebugPort() throws IOException {
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
  }

  /** Tests that an interactions history can be loaded in as a script. */
  public void testScriptLoading() throws IOException, OperationCanceledException {
    assertTrue(_model instanceof TestInteractionsModel);
    TestInteractionsModel model = (TestInteractionsModel)_model;
    // Set up a sample history
    String line1 = "System.out.println(\"hi\")";
    String line2 = "System.out.println(\"bye\")";
//    String delim = History.INTERACTION_SEPARATOR + StringOps.EOL;
    final File temp = File.createTempFile("drjava-test", ".hist").getCanonicalFile();
    temp.deleteOnExit();
    History history = new History(5);
    history.add(line1);
    history.add(line2);
    history.writeToFile(new FileSaveSelector() {
      public File getFile() { return temp; }
      public boolean warnFileOpen(File f) { return true; }
      public boolean verifyOverwrite() { return true; }
      public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) { return true; }
    });

    // Load the history as a script
    InteractionsScriptModel ism = model.loadHistoryAsScript(new FileOpenSelector() {
      public File[] getFiles() {
        return new File[] {temp};
      }
    });
    InteractionsDocument doc = model.getDocument();

    // Should not be able to get the previous interaction
    assertTrue("Should have no previous", !ism.hasPrevInteraction());
    try {
      ism.prevInteraction();
      fail("Should not have been able to get previous interaction!");
    }
    catch (IllegalStateException ise) {
      // good, continue
    }

    // Get the next (first) interaction
    assertTrue("Should have next", ism.hasNextInteraction());
    ism.nextInteraction();
    assertEquals("Should have put the first line into the document.", line1, doc.getCurrentInteraction());

    // Still should not be able to get the previous interaction
    assertTrue("Should have no previous", !ism.hasPrevInteraction());
    try {
      ism.prevInteraction();
      fail("Should not have been able to get previous interaction!");
    }
    catch (IllegalStateException ise) {
      // good, continue
    }

    // Skip it; get the next (second) interaction
    assertTrue("Should have next", ism.hasNextInteraction());
    ism.nextInteraction();
    assertEquals("Should have put the second line into the document.", line2, doc.getCurrentInteraction());

    // Now we should be able to get the previous interaction
    assertTrue("Should have previous", ism.hasPrevInteraction());
    ism.prevInteraction();
    assertEquals("Should have put the first line into the document.", line1, doc.getCurrentInteraction());

    // Go back to the second line and execute it
    ism.nextInteraction();
    ism.executeInteraction();
    assertEquals("Should have \"executed\" the second interaction.", line2, model.toEval);
    // pretend the call completed
    model.replReturnedVoid();

    // Should not be able to get the next interaction, since we're at the end
    assertTrue("Should have no next", !ism.hasNextInteraction());
    try {
      ism.nextInteraction();
      fail("Should not have been able to get next interaction!");
    }
    catch (IllegalStateException ise) {
      // good, continue
    }

    // Get Previous should return the most recently executed interaction
    assertTrue("Should have previous", ism.hasPrevInteraction());
    ism.prevInteraction();
    assertEquals("Should have put the second line into the document.", line2, doc.getCurrentInteraction());

    // Get Previous should now return the first interaction
    assertTrue("Should have previous", ism.hasPrevInteraction());
    ism.prevInteraction();
    assertEquals("Should have put the first line into the document.", line1, doc.getCurrentInteraction());

    // Should have no more previous
    assertTrue("Should have no previous", !ism.hasPrevInteraction());

    // Now execute the first interaction
    ism.executeInteraction();
    assertEquals("Should have \"executed\" the first interaction.", line1, model.toEval);
    // pretend the call completed
    model.replReturnedVoid();

    // Get Previous should return the most recent (first) interaction
    assertTrue("Should have previous", ism.hasPrevInteraction());
    ism.prevInteraction();
    assertEquals("Should have put the first line into the document.", line1, doc.getCurrentInteraction());

    // Should not be able to get the previous interaction this time
    assertTrue("Should have no previous", !ism.hasPrevInteraction());
    try {
      ism.prevInteraction();
      fail("Should not have been able to get previous interaction!");
    }
    catch (IllegalStateException ise) {
      // good, continue
    }
  }

  /** Tests that setting and changing an input listener works correctly. */
  public void testSetChangeInputListener() {
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
  }

  /** Tests that the interactions history is stored correctly. See bug # 992455 */
  public void testInteractionsHistoryStoredCorrectly() throws EditDocumentException {
    final Object _lock = new Object();
    String code = "public class A {\n";

    InteractionsDocument doc = _model.getDocument();

    // Insert text and evaluate
    doc.insertText(doc.getLength(), code, InteractionsDocument.DEFAULT_STYLE);

    _model.interpretCurrentInteraction();
    //Simulate result
    _model.replReturnedSyntaxError("Encountered Unexpected \"<EOF>\"", "public class A {\n", -1, -1, -1, -1);

    String expected = "public class A {\n" + "\n";  // last term was StringOps.EOL but Swing uses '\n' for newLIne
    String result = doc.getCurrentInteraction();
//    System.err.println("expected = '" + expected + "' length = " + expected.length());
//    System.err.println("result = '" + result + "' length = " + result.length());
    assertEquals("Current interaction should still be there - should not have interpreted", expected, result);
    History h = doc.getHistory();
    assertEquals("History should be empty", 0, h.size());

    code = "}\n";

    doc.insertText(doc.getLength(), code, InteractionsDocument.DEFAULT_STYLE);

    synchronized(_lock) {
      _model.interpretCurrentInteraction();
      _model.replReturnedVoid();
    }

    synchronized(_lock) {
      assertEquals("Current interaction should not be there - should have interpreted", "", doc.getCurrentInteraction());
      assertEquals("History should contain one interaction", 1, h.size());
    }
  }

  /** A generic InteractionsModel for testing purposes.  (Used here and in InteractionsPaneTest.) */
  public static class TestInteractionsModel extends InteractionsModel {
    String toEval = null;
    String addedClass = null;

    /** Constructs a new InteractionsModel. */
    public TestInteractionsModel(InteractionsDJDocument adapter) {
      // Adapter, history size, write delay
      super(adapter, new File(System.getProperty("user.dir")), 1000, 25);
    }

    protected void _interpret(String toEval) { this.toEval = toEval; }
    
    public String getVariableToString(String var) {
      fail("cannot getVariableToString in a test");
      return null;
    }
    public String getVariableClassName(String var) {
      fail("cannot getVariableClassName in a test");
      return null;
    }
    
    public void addProjectClassPath(File path) { fail("cannot add to classpath in a test"); }
    public void addBuildDirectoryClassPath(File path) { fail("cannot add to classpath in a test"); }
    public void addProjectFilesClassPath(File path) { fail("cannot add to classpath in a test"); }
    public void addExternalFilesClassPath(File path) { fail("cannot add to classpath in a test"); }
    public void addExtraClassPath(File path) { fail("cannot add to classpath in a test"); }
    protected void _resetInterpreter(File wd) { fail("cannot reset interpreter in a test"); }
    
    protected void _notifyInteractionStarted() { }
    protected void _notifyInteractionEnded() { }
    protected void _notifySyntaxErrorOccurred(int offset, int length) { }
    protected void _notifyInterpreterExited(int status) { }
    protected void _notifyInterpreterResetting() { }
    protected void _notifyInterpreterResetFailed(Throwable t) { }
    public void _notifyInterpreterReady(File wd) { }
    protected void _interpreterResetFailed(Throwable t) { }
    protected void _notifyInteractionIncomplete() { }
    protected void _notifySlaveJVMUsed() { }
    public ConsoleDocument getConsoleDocument() { return null; }
  }
  
  /** This test model includes a slave JVM, just like a DefaultGlobalModel.  It must be disposed before it is
   *  deallocated to kill the slave JVM.   TODO: the mutation in this class is disgusting -- Corky  2 June 06.
   */
  private static class IncompleteInputInteractionsModel extends RMIInteractionsModel {
    boolean continuationException;  // This appears to be the negation of syntaxException making it redundant!
    boolean syntaxException;
    
    private volatile boolean _interactionDone = false;
    private final Object _interactionLock = new Object();
    
    public void _logInteractionStart() { _interactionDone = false; }
    
    public void _waitInteractionDone() throws InterruptedException { 
      synchronized(_interactionLock) { while (! _interactionDone) _interactionLock.wait(); }
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

    protected void _notifyInteractionStarted() { }
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
    protected void _notifyInteractionIncomplete() { _notifyInteractionEnded(); }
    protected void _notifyInterpreterChanged(boolean inProgress) { }
    protected void _notifySlaveJVMUsed() { }
    
    public void dispose() throws RemoteException { _jvm.dispose(); }
    
    public ConsoleDocument getConsoleDocument() { return null; }

    public void replThrewException(String exceptionClass, String message, String stackTrace, String shortMessage) {
      _log.log("replThrewException called");
      if (shortMessage != null) {
        if (shortMessage.endsWith("<EOF>\"")) {
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

    public void replReturnedSyntaxError(String errorMessage, String interaction, int startRow, int startCol, int endRow,
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
