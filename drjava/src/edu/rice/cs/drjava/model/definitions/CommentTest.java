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

package  edu.rice.cs.drjava.model.definitions;

import  junit.framework.*;
import  junit.extensions.*;
import  javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.definitions.indent.*;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.model.GlobalEventNotifier;

/**
 * Test the comment lines / uncomment lines functionality.
 * @version $Id$
 */
public final class CommentTest extends TestCase {
  protected DefinitionsDocument doc;
  private Integer _indentLevel = new Integer(2);
//  private Integer configIndent;
  private GlobalEventNotifier _notifier;

  /**
   * Resents configuration settings and sets up the indent level so that we
   * can predict the correct behavior for indenting.
   */
  public void setUp() {
    DrJava.getConfig().resetToDefaults();
    _notifier = new GlobalEventNotifier();
    doc = new DefinitionsDocument(_notifier);
    DrJava.getConfig().setSetting(OptionConstants.INDENT_LEVEL,_indentLevel);
  }

  /**
   * Tests the Comment Out Line(s) command with a single line.
   */
  public void testCommentOutSingleLine() throws BadLocationException {
    String text =
      "Here is some abritrary text that should be commented.\n" +
      "/* It is on multiple lines, and contains slashes // and other\n" +
      "various */ obnoxious characters.\n";

    String commented =
      "Here is some abritrary text that should be commented.\n" +
      "///* It is on multiple lines, and contains slashes // and other\n" +
      "various */ obnoxious characters.\n";

    doc.insertString(0, text, null);
    _assertContents("Sample text is inserted improperly.", text, doc);
    doc.commentLines(70, 75);
    _assertContents("Only the second line should be wing-commented!", commented, doc);
  }

  /**
   * Tests the Comment Out Line(s) command with multiple lines.
   */
  public void testCommentOutMultipleLines() throws BadLocationException {
    String text =
      "Here is some abritrary text that should be commented.\n" +
      "/* It is on multiple lines, and contains slashes // and other\n" +
      "various */ obnoxious characters.\n";

    String commented =
      "//Here is some abritrary text that should be commented.\n" +
      "///* It is on multiple lines, and contains slashes // and other\n" +
      "//various */ obnoxious characters.\n";

    doc.insertString(0, text, null);
    _assertContents("Sample text is inserted improperly.", text, doc);
    doc.commentLines(0, doc.getLength());
    _assertContents("These lines should be wing-commented!", commented, doc);
  }

  /**
   * Tests the Uncomment Line(s) command with a single line.
   * These sample lines should be ignored by the algorithm.
   */
  public void testUncommentIgnoreSingleLine() throws BadLocationException {
    String text =
      "Here is some abritrary text that should not be uncommented.\n" +
      "/* It is on multiple lines, and contains slashes // and other\n" +
      "* various */ obnoxious characters,\n" +
      "sometimes // in block comments and sometimes not.";

    doc.insertString(0, text, null);
    _assertContents("Sample text is inserted improperly.", text, doc);
    doc.uncommentLines(70, 75);
    _assertContents("These lines should be unchanged by uncomment!", text, doc);
  }

  /**
   * Tests the Uncomment Line(s) command with multiple lines.
   * These sample lines should be ignored by the algorithm.
   */
  public void testUncommentIgnoreMultipleLines() throws BadLocationException {
    String text =
      "Here is some abritrary text that should not be uncommented.\n" +
      "/* It is on multiple lines, and contains slashes // and other\n" +
      "* various */ obnoxious characters,\n" +
      "sometimes // in block comments and sometimes not.";

    doc.insertString(0, text, null);
    _assertContents("Sample text is inserted improperly.", text, doc);
    doc.uncommentLines(0, doc.getLength());
    _assertContents("These lines should be unchanged by uncomment!", text, doc);
  }

  /**
   * Tests the Uncomment Line(s) command with a single line.
   * One of these sample lines should be uncommented and indented by the algorithm.
   */
  public void testUncommentSingleLine() throws BadLocationException {
    String text =
      "// // Here is some abritrary text that should be uncommented.\n" +
      "// /* along with a little bit of code, just to spice\n" +
      "      //* things up.\n" +
      "//                    */ \n" +
      "//         System.out.println(\"Aren't comments fun? // (yeah!)\")";

    String uncommented =
      "// // Here is some abritrary text that should be uncommented.\n" +
      "// /* along with a little bit of code, just to spice\n" +
      "      //* things up.\n" +
      "//                    */ \n" +
      "System.out.println(\"Aren't comments fun? // (yeah!)\")";

    doc.insertString(0, text, null);
    _assertContents("Sample text is inserted improperly.", text, doc);
    doc.uncommentLines(doc.getLength()-1, doc.getLength());
    _assertContents("The last line should be indented and have no commenting!",
                    uncommented, doc);
  }

  /**
   * Tests the Uncomment Line(s) command with multiple lines.
   * These sample lines should be uncommented and indented by the algorithm.
   */
  public void testUncommentMultipleLines() throws BadLocationException {
    String text =
      "// // Here is some abritrary text that should be uncommented.\n" +
      "// /* along with a little bit of code, just to spice\n" +
      "      //* things up.\n" +
      "//                    */ \n" +
      "//         System.out.println(\"Aren't comments fun? // (yeah!)\")";

    String uncommented =
      "// Here is some abritrary text that should be uncommented.\n" +
      "/* along with a little bit of code, just to spice\n" +
      " * things up.\n" +
      " */ \n" +
      "System.out.println(\"Aren't comments fun? // (yeah!)\")";

    doc.insertString(0, text, null);
    _assertContents("Sample text is inserted improperly.", text, doc);
    doc.uncommentLines(0, doc.getLength());
    _assertContents("These lines should be indented and have at most"+
                    "one level of commenting!", uncommented, doc);
  }

  private static void _assertContents(String msg, String expected, Document document)
    throws BadLocationException
  {
    assertEquals(msg, expected,
                 document.getText(0, document.getLength()));
  }

}
