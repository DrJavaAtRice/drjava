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

import junit.framework.*;

import java.io.IOException;

import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.util.text.*;

/**
 * Tests the functionality of an InteractionsModel.
 * @version $Id$
 */
public final class InteractionsModelTest extends TestCase {
  protected DocumentAdapter _adapter;
  protected TestInteractionsModel _model;
  
  public void setUp() {
    _adapter = new SwingDocumentAdapter();
    _model = new TestInteractionsModel(_adapter);
  }
  
  public void tearDown() {
    _model = null;
    _adapter = null;
    System.gc();
  }
  
  /**
   * Return a new TestSuite for this class.
   * @return Test
   */
  public static Test suite() {
    return new TestSuite(InteractionsModelTest.class);
  }
  
  /**
   * Asserts that the given string typed by the user is processed
   * to become the given expected string for an interpretation.
   * @param typed A string typed by the user
   * @param expected What the processor should return
   */
  protected void _assertProcessedContents(String typed, String expected)
    throws DocumentAdapterException
  {
    InteractionsDocument doc = _model.getDocument();
    doc.reset();
    doc.insertText(doc.getDocLength(), typed,
                   InteractionsDocument.DEFAULT_STYLE);
    _model.interpretCurrentInteraction();
    assertEquals("processed output should match expected",
                 expected, _model.toEval);
  }
  
  
  /**
   * Tests that the correct text is returned when interpreting.
   */
  public void testInterpretCurrentInteraction() throws DocumentAdapterException {
    String code = "int x = 3;";
    InteractionsDocument doc = _model.getDocument();
    _model.interpretCurrentInteraction();
    // pretend the call completed
    _model.replReturnedVoid();
    assertEquals("string being interpreted", "", _model.toEval);
    
    // Insert text and evaluate
    doc.insertText(doc.getDocLength(), code,
                   InteractionsDocument.DEFAULT_STYLE);
    _model.interpretCurrentInteraction();
    // pretend the call completed
    _model.replReturnedVoid();
    assertEquals("string being interpreted", code, _model.toEval);
  }
  
  /**
   * Tests that "java Classname [args]" runs the class's main method, with
   * simple delimited arguments.
   */
  public void testInterpretJavaArguments() throws DocumentAdapterException {
    // java Foo a b c
    // Foo.main(new String[]{"a", "b", "c"});
    _assertProcessedContents("java Foo a b c",
                             "Foo.main(new String[]{\"a\",\"b\",\"c\"});");
    // java Foo "a b c"
    // Foo.main(new String[]{"a b c"});
    _assertProcessedContents("java Foo \"a b c\"",
                             "Foo.main(new String[]{\"a b c\"});");
    // java Foo "a b"c d
    // Foo.main(new String[]{"a bc", "d"});
    //  This is different behavior than Unix or DOS, but it's more
    //  intuitive to the user (and easier to implement).
    _assertProcessedContents("java Foo \"a b\"c d",
                             "Foo.main(new String[]{\"a bc\",\"d\"});");

    // java Foo c:\\file.txt
    // Foo.main("c:\\file.txt");
    _assertProcessedContents("java Foo c:\\\\file.txt",
                             "Foo.main(new String[]{\"c:\\\\file.txt\"});");

    // java Foo /home/user/file
    // Foo.main("/home/user/file");
    _assertProcessedContents("java Foo /home/user/file",
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
  public void testInterpretJavaEscapedArgs() throws DocumentAdapterException {
    // java Foo \j
    // Foo.main(new String[]{"j"});
    _assertProcessedContents("java Foo \\j",
                             "Foo.main(new String[]{\"j\"});");
    // java Foo \"
    // Foo.main(new String[]{"\""});
    _assertProcessedContents("java Foo \\\"",
                             "Foo.main(new String[]{\"\\\"\"});");
    // java Foo \\
    // Foo.main(new String[]{"\\"});
    _assertProcessedContents("java Foo \\\\",
                             "Foo.main(new String[]{\"\\\\\"});");
    // java Foo a\ b
    // Foo.main(new String[]{"a b"});
    _assertProcessedContents("java Foo a\\ b",
                             "Foo.main(new String[]{\"a b\"});");
  }
  
  /**
   * Tests that within a quote, everything is correctly escaped.
   * (Special characters are passed to the program correctly.)
   */
  public void testInterpretJavaQuotedEscapedArgs() throws DocumentAdapterException {
    // java Foo "a \" b"
    // Foo.main(new String[]{"a \" b"});
    _assertProcessedContents("java Foo \"a \\\" b\"",
                             "Foo.main(new String[]{\"a \\\" b\"});");
    // java Foo "\'"
    // Foo.main(new String[]{"\\'"});
    _assertProcessedContents("java Foo \"\\'\"",
                             "Foo.main(new String[]{\"\\\\'\"});");
    // java Foo "\\"
    // Foo.main(new String[]{"\\"});
    _assertProcessedContents("java Foo \"\\\\\"",
                             "Foo.main(new String[]{\"\\\\\"});");
    // java Foo "\" \d"
    // Foo.main(new String[]{"\" \\d"});
    _assertProcessedContents("java Foo \"\\\" \\d\"",
                             "Foo.main(new String[]{\"\\\" \\\\d\"});");
    // java Foo "\n"
    // Foo.main(new String[]{"\n"});
/*    _assertProcessedContents("java Foo \"\\n\"",
                             "Foo.main(new String[]{\"\\n\"});");
    // java Foo "\t"
    // Foo.main(new String[]{"\t"});
    _assertProcessedContents("java Foo \"\\t\"",
                             "Foo.main(new String[]{\"\\t\"});");
    // java Foo "\r"
    // Foo.main(new String[]{"\r"});
    _assertProcessedContents("java Foo \"\\r\"",
                             "Foo.main(new String[]{\"\\r\"});");
    // java Foo "\f"
    // Foo.main(new String[]{"\f"});
    _assertProcessedContents("java Foo \"\\f\"",
                             "Foo.main(new String[]{\"\\f\"});");
    // java Foo "\b"
    // Foo.main(new String[]{"\b"});
    _assertProcessedContents("java Foo \"\\b\"",
                             "Foo.main(new String[]{\"\\b\"});"); */
  }

  /**
   * Tests that single quotes cannot be used as argument delimiters.
   * This is consistent with DOS, not with Unix.
   */
  public void testInterpretJavaSingleQuotedArgs() throws DocumentAdapterException {
    // java Foo 'asdf'
    _assertProcessedContents("java Foo 'asdf'",
                             "Foo.main(new String[]{\"asdf\"});");
    // java Foo 'a b c'
    _assertProcessedContents("java Foo 'a b c'",
                             "Foo.main(new String[]{\"a b c\"});");
  }
  
  
  
  //public void testLoadHistory();
  // TO DO: test that the correct history is returned (careful of last newline)
  
  
  /**
   * Tests that a debug port can be generated.
   */
  public void testDebugPort() throws IOException {
    assertTrue("generated debug port", _model.getDebugPort() != -1);
    
    // Set port
    _model.setDebugPort(5);
    assertEquals("manually set debug port", 5, _model.getDebugPort());
    
    // Port should stay -1 after setting it
    _model.setDebugPort(-1);
    assertEquals("debug port should be -1", -1, _model.getDebugPort());
  }
  
  /**
   * A generic InteractionsModel.
   */
  public static class TestInteractionsModel extends InteractionsModel {
    String toEval = null;
    String addedClass = null;

    /**
     * Constructs a new InteractionsModel.
     */
    public TestInteractionsModel(DocumentAdapter adapter) {
      // Adapter, history size, write delay
      super(adapter, 1000, 25);
    }
    
    protected void _interpret(String toEval) {
      this.toEval = toEval;
    }
    public void addToClassPath(String path) {
      fail("cannot add to classpath in a test");
    }
    protected void _resetInterpreter() {
      fail("cannot reset interpreter in a test");
    }
    protected void _notifyInteractionStarted() {}
    protected void _notifyInteractionEnded() {}
    protected void _notifySyntaxErrorOccurred(int offset, int length) {}
    protected void _notifyInterpreterExited(int status) {}
    protected void _notifyInterpreterResetting() {}
    protected void _notifyInterpreterResetFailed(Throwable t) {}
    protected void _notifyInterpreterReady() {}
    public String getConsoleInput() {
      fail("cannot get input from System.in in a test");
      return null;
    }
  }
}