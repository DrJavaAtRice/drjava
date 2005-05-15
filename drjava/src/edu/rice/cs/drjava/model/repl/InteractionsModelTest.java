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

package edu.rice.cs.drjava.model.repl;

import junit.framework.*;

import java.net.URL;
import java.io.File;
import java.io.IOException;

import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.model.FileOpenSelector;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.OperationCanceledException;
import edu.rice.cs.util.text.*;

import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.drjava.model.DefaultGlobalModel;

import edu.rice.cs.drjava.ui.MainFrame;

/**
 * Tests the functionality of an InteractionsModel.
 * @version $Id$
 */
public final class InteractionsModelTest extends TestCase {
  protected InteractionsDocumentAdapter _adapter;
  protected InteractionsModel _model;

  public void setUp() throws Exception {
    super.setUp();
    _adapter = new InteractionsDocumentAdapter();
    _model = new TestInteractionsModel(_adapter);
  }

  public void tearDown() throws Exception {
    _model = null;
    _adapter = null;
    super.tearDown();
  }

  /** Asserts that the given string typed by the user is processed to become the given expected string for an
   *  interpretation.
   *  @param typed A string typed by the user
   *  @param expected What the processor should return
   */
  protected void _assertProcessedContents(String typed, String expected) throws DocumentAdapterException {
    assertTrue(_model instanceof TestInteractionsModel);
    TestInteractionsModel model = (TestInteractionsModel)_model;
    InteractionsDocument doc = model.getDocument();
    doc.reset();
    doc.insertText(doc.getDocLength(), typed, InteractionsDocument.DEFAULT_STYLE);
    model.interpretCurrentInteraction();
    assertEquals("processed output should match expected", expected, model.toEval);
  }

  /** Asserts that the given string typed by the user of the form "java classname" is transformed to the given
   *  expected main method invocation.
   *  @param typed the "java classname args ..." typed by the user
   *  @param expected the expected main class call
   */
  protected void _assertMainTransformation(String typed, String expected) {
    assertEquals("main transformation should match expected",
                 expected, TestInteractionsModel._testClassCall(typed));
  }


  /** Tests that the correct text is returned when interpreting. */
  public void testInterpretCurrentInteraction() throws DocumentAdapterException {
    assertTrue(_model instanceof TestInteractionsModel);
    TestInteractionsModel model = (TestInteractionsModel)_model;
    String code = "int x = 3;";
    InteractionsDocument doc = model.getDocument();
    model.interpretCurrentInteraction();
    // pretend the call completed
    model.replReturnedVoid();
    assertEquals("string being interpreted", "", model.toEval);

    // Insert text and evaluate
    doc.insertText(doc.getDocLength(), code,
                   InteractionsDocument.DEFAULT_STYLE);
    model.interpretCurrentInteraction();
    // pretend the call completed
    model.replReturnedVoid();
    assertEquals("string being interpreted", code, model.toEval);
  }

  public void testInterpretCurrentInteractionWithIncompleteInput() throws DocumentAdapterException {
    _model = new IncompleteInputInteractionsModel(_adapter);   // override the one initialized in setUp()
    assertReplThrewContinuationException("void m() {");
    _model = new IncompleteInputInteractionsModel(_adapter);
    assertReplThrewContinuationException("void m() {;");
    _model = new IncompleteInputInteractionsModel(_adapter);
    assertReplThrewContinuationException("1+");
    _model = new IncompleteInputInteractionsModel(_adapter);
    assertReplThrewContinuationException("(1+2");
    _model = new IncompleteInputInteractionsModel(_adapter);
    assertReplThrewSyntaxException("(1+2;");
    _model = new IncompleteInputInteractionsModel(_adapter);
    assertReplThrewContinuationException("for (;;");
  }

  protected void assertReplThrewContinuationException(String code) throws DocumentAdapterException {
    assertTrue(_model instanceof IncompleteInputInteractionsModel);
    IncompleteInputInteractionsModel model = (IncompleteInputInteractionsModel)_model;
    InteractionsDocument doc = model.getDocument();
    doc.insertText(doc.getDocLength(), code, InteractionsDocument.DEFAULT_STYLE);
    model.interpretCurrentInteraction();
    try { Thread.sleep(5000); } catch(InterruptedException ie) { }; // allow for the exception to be generated!
    assertTrue("Code '"+code+"' should generate a continuation exception but not a syntax exception",
               (model.isContinuationException() == true) && (model.isSyntaxException() == false));
  }

  protected void assertReplThrewSyntaxException(String code) throws DocumentAdapterException {
    assertTrue(_model instanceof IncompleteInputInteractionsModel);
    IncompleteInputInteractionsModel model = (IncompleteInputInteractionsModel)_model;
    InteractionsDocument doc = model.getDocument();
    doc.insertText(doc.getDocLength(), code, InteractionsDocument.DEFAULT_STYLE);
    model.interpretCurrentInteraction();
    try { Thread.sleep(5000); } catch(InterruptedException ie) { }; // allow for the exception to be generated!
    assertTrue("Code '"+code+"' should generate a syntax exception but not a continuation exception",
               (model.isSyntaxException() == true) && (model.isContinuationException() == false));
  }


  /**
   * Tests that "java Classname [args]" runs the class's main method, with
   * simple delimited arguments.
   */
  public void testInterpretJavaArguments() {
    // java Foo a b c
    // Foo.main(new String[]{"a", "b", "c"});
    _assertMainTransformation("java Foo a b c",
                             "Foo.main(new String[]{\"a\",\"b\",\"c\"});");
    // java Foo "a b c"
    // Foo.main(new String[]{"a b c"});
    _assertMainTransformation("java Foo \"a b c\"",
                             "Foo.main(new String[]{\"a b c\"});");
    // java Foo "a b"c d
    // Foo.main(new String[]{"a bc", "d"});
    //  This is different behavior than Unix or DOS, but it's more
    //  intuitive to the user (and easier to implement).
    _assertMainTransformation("java Foo \"a b\"c d",
                             "Foo.main(new String[]{\"a bc\",\"d\"});");

    // java Foo c:\\file.txt
    // Foo.main("c:\\file.txt");
    _assertMainTransformation("java Foo c:\\\\file.txt",
                             "Foo.main(new String[]{\"c:\\\\file.txt\"});");

    // java Foo /home/user/file
    // Foo.main("/home/user/file");
    _assertMainTransformation("java Foo /home/user/file",
                             "Foo.main(new String[]{\"/home/user/file\"});");
  }

  /**
   * Tests that escaped characters just return the character itself.
   * Escaped whitespace is considered a character, not a delimiter.
   * (This is how Unix behaves.)
   *
   * not currently enforcing any behavior for a simple implementation
   * using a StreamTokenizer
   */
  public void testInterpretJavaEscapedArgs() {
    // java Foo \j
    // Foo.main(new String[]{"j"});
    _assertMainTransformation("java Foo \\j",
                             "Foo.main(new String[]{\"j\"});");
    // java Foo \"
    // Foo.main(new String[]{"\""});
    _assertMainTransformation("java Foo \\\"",
                             "Foo.main(new String[]{\"\\\"\"});");
    // java Foo \\
    // Foo.main(new String[]{"\\"});
    _assertMainTransformation("java Foo \\\\",
                             "Foo.main(new String[]{\"\\\\\"});");
    // java Foo a\ b
    // Foo.main(new String[]{"a b"});
    _assertMainTransformation("java Foo a\\ b",
                             "Foo.main(new String[]{\"a b\"});");
  }

  /**
   * Tests that within a quote, everything is correctly escaped.
   * (Special characters are passed to the program correctly.)
   */
  public void testInterpretJavaQuotedEscapedArgs() {
    // java Foo "a \" b"
    // Foo.main(new String[]{"a \" b"});
    _assertMainTransformation("java Foo \"a \\\" b\"",
                             "Foo.main(new String[]{\"a \\\" b\"});");
    // java Foo "\'"
    // Foo.main(new String[]{"\\'"});
    _assertMainTransformation("java Foo \"\\'\"",
                             "Foo.main(new String[]{\"\\\\'\"});");
    // java Foo "\\"
    // Foo.main(new String[]{"\\"});
    _assertMainTransformation("java Foo \"\\\\\"",
                             "Foo.main(new String[]{\"\\\\\"});");
    // java Foo "\" \d"
    // Foo.main(new String[]{"\" \\d"});
    _assertMainTransformation("java Foo \"\\\" \\d\"",
                             "Foo.main(new String[]{\"\\\" \\\\d\"});");
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

  /**
   * Tests that single quotes can be used as argument delimiters.
   */
  public void testInterpretJavaSingleQuotedArgs() {
    // java Foo 'asdf'
    _assertMainTransformation("java Foo 'asdf'",
                             "Foo.main(new String[]{\"asdf\"});");
    // java Foo 'a b c'
    _assertMainTransformation("java Foo 'a b c'",
                             "Foo.main(new String[]{\"a b c\"});");

    // java Foo 'a b'c
    _assertMainTransformation("java Foo 'a b'c",
                             "Foo.main(new String[]{\"a bc\"});");
  }



  //public void testLoadHistory();
  // TO DO: test that the correct history is returned (careful of last newline)


  /** Tests that a debug port can be generated. */
  public void testDebugPort() throws IOException {
    int port = _model.getDebugPort();
    assertTrue("generated debug port", port != -1);

    // Resetting after startup should change the port
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

  /**
   * Tests that an interactions history can be loaded in as a script.
   */
  public void testScriptLoading() throws IOException, OperationCanceledException {
    assertTrue(_model instanceof TestInteractionsModel);
    TestInteractionsModel model = (TestInteractionsModel)_model;
    // Set up a sample history
    String line1 = "System.out.println(\"hi\")";
    String line2 = "System.out.println(\"bye\")";
//    String delim = History.INTERACTION_SEPARATOR + System.getProperty("line.separator");
    final File temp = File.createTempFile("drjava-test", ".hist");
    temp.deleteOnExit();
    History history = new History(5);
    history.add(line1);
    history.add(line2);
    history.writeToFile(new FileSaveSelector() {
      public File getFile() { return temp; }
      public boolean warnFileOpen(File f) { return true; }
      public boolean verifyOverwrite() { return true; }
      public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) {
        return true;
      }
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
    assertEquals("Should have put the first line into the document.",
                 line1, doc.getCurrentInteraction());

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
    assertEquals("Should have put the second line into the document.",
                 line2, doc.getCurrentInteraction());

    // Now we should be able to get the previous interaction
    assertTrue("Should have previous", ism.hasPrevInteraction());
    ism.prevInteraction();
    assertEquals("Should have put the first line into the document.",
                 line1, doc.getCurrentInteraction());

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
    assertEquals("Should have put the second line into the document.",
                 line2, doc.getCurrentInteraction());

    // Get Previous should now return the first interaction
    assertTrue("Should have previous", ism.hasPrevInteraction());
    ism.prevInteraction();
    assertEquals("Should have put the first line into the document.",
                 line1, doc.getCurrentInteraction());

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
    assertEquals("Should have put the first line into the document.",
                 line1, doc.getCurrentInteraction());

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

  /**
   * Tests that setting and changing an input listener works correctly.
   */
  public void testSetChangeInputListener() {
    InputListener listener1 = new InputListener() {
      public String getConsoleInput() {
        return "input1";
      }
    };

    InputListener listener2 = new InputListener() {
      public String getConsoleInput() {
        return "input2";
      }
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
  public void testInteractionsHistoryStoredCorrectly() throws DocumentAdapterException {
    final Object _lock = new Object();
    String code = "public class A {\n";

    InteractionsDocument doc = _model.getDocument();

    // Insert text and evaluate
    doc.insertText(doc.getDocLength(), code, InteractionsDocument.DEFAULT_STYLE);

    _model.interpretCurrentInteraction();
    //Simulate result
    _model.replReturnedSyntaxError("Encountered Unexpected \"<EOF>\"", "public class A {\n", -1, -1, -1, -1);

    assertEquals("Current interaction should still be there - should not have interpreted", "public class A {\n" + System.getProperty("line.separator"),
                 doc.getCurrentInteraction());
    History h = doc.getHistory();
    assertEquals("History should be empty", 0, h.size());

    code = "}\n";

    doc.insertText(doc.getDocLength(), code, InteractionsDocument.DEFAULT_STYLE);

    synchronized(_lock) {
      _model.interpretCurrentInteraction();
      _model.replReturnedVoid();
    }

    synchronized(_lock) {
      assertEquals("Current interaction should not be there - should have interpreted", "", doc.getCurrentInteraction());
      assertEquals("History should contain one interaction", 1, h.size());
    }
  }

  /** A generic InteractionsModel for testing purposes. */
  public static class TestInteractionsModel extends InteractionsModel {
    String toEval = null;
    String addedClass = null;

    /**
     * Constructs a new InteractionsModel.
     */
    public TestInteractionsModel(InteractionsDocumentAdapter adapter) {
      // Adapter, history size, write delay
      super(adapter, 1000, 25);
    }

    protected void _interpret(String toEval) {
      this.toEval = toEval;
    }
    public String getVariableToString(String var) {
      fail("cannot getVariableToString in a test");
      return null;
    }
    public String getVariableClassName(String var) {
      fail("cannot getVariableClassName in a test");
      return null;
    }
    public void addProjectClassPath(URL path) {
      fail("cannot add to classpath in a test");
    }
    public void addBuildDirectoryClassPath(URL path) {
      fail("cannot add to classpath in a test");
    }
    public void addProjectFilesClassPath(URL path) {
      fail("cannot add to classpath in a test");
    }
    public void addExternalFilesClassPath(URL path) {
      fail("cannot add to classpath in a test");
    }
    public void addExtraClassPath(URL path) {
      fail("cannot add to classpath in a test");
    }

    protected void _resetInterpreter() {
      fail("cannot reset interpreter in a test");
    }
    protected void _notifyInteractionStarted() { }
    protected void _notifyInteractionEnded() { }
    protected void _notifySyntaxErrorOccurred(int offset, int length) { }
    protected void _notifyInterpreterExited(int status) { }
    protected void _notifyInterpreterResetting() { }
    protected void _notifyInterpreterResetFailed(Throwable t) { }
    protected void _notifyInterpreterReady() { }
    protected void _interpreterResetFailed(Throwable t) { }
    protected void _notifyInteractionIncomplete() { }
  }

  public static class IncompleteInputInteractionsModel extends RMIInteractionsModel {
    boolean continuationException;
    boolean syntaxException;

    /**
     * Constructs a new InteractionsModel.
     */
    public IncompleteInputInteractionsModel(InteractionsDocumentAdapter adapter) {
      // MainJVM, Adapter, history size, write delay
      super(new MainJVM(), adapter, 1000, 25);
      _interpreterControl.setInteractionsModel(this);
      _interpreterControl.startInterpreterJVM();
      continuationException = false;
      syntaxException = false;
    }

//    public String getVariableToString(String var) {
//      fail("cannot getVariableToString in a test");
//      return null;
//    }
//    public String getVariableClassName(String var) {
//      fail("cannot getVariableClassName in a test");
//      return null;
//    }
//    public void addToClassPath(String path) {
//      fail("cannot add to classpath in a test");
//    }
//    protected void _resetInterpreter() {
//      fail("cannot reset interpreter in a test");
//    }
    protected void _notifyInteractionStarted() { }
    protected void _notifyInteractionEnded() { }
    protected void _notifySyntaxErrorOccurred(int offset, int length) { }
    protected void _notifyInterpreterExited(int status) { }
    protected void _notifyInterpreterResetting() { }
    protected void _notifyInterpreterResetFailed(Throwable t) { }
    protected void _notifyInterpreterReady() { }
    protected void _interpreterResetFailed(Throwable t) { }
    protected void _notifyInteractionIncomplete() { }

    protected void _notifyInterpreterChanged(boolean inProgress) { }

    public void replThrewException(String exceptionClass, String message, String stackTrace, String shortMessage) {
      if (shortMessage!=null) {
        if (shortMessage.endsWith("<EOF>\"")) {
          continuationException = true;
          syntaxException = false;
          return;
        }
      }
      syntaxException = true;
      continuationException = false;
    }

    public void replReturnedSyntaxError(String errorMessage,
                                        String interaction,
                                        int startRow,
                                        int startCol,
                                        int endRow,
                                        int endCol ) {
      if (errorMessage!=null) {
        if (errorMessage.endsWith("<EOF>\"")) {
          continuationException = true;
          syntaxException = false;
          return;
        }
      }
      syntaxException = true;
      continuationException = false;
    }

    public boolean isContinuationException() {
      return continuationException;
    }

    public boolean isSyntaxException() {
      return syntaxException;
    }
  }
}
