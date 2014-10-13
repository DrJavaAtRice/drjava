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

package  edu.rice.cs.drjava.model.definitions;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.DJDocument;
import edu.rice.cs.drjava.model.GlobalEventNotifier;

import javax.swing.text.BadLocationException;

/** Test the comment lines / uncomment lines functionality.
  * @version $Id: CommentTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class CommentTest extends DrJavaTestCase {
  protected DefinitionsDocument doc;
  private Integer _indentLevel = Integer.valueOf(2);
  private GlobalEventNotifier _notifier;

  /** Resents configuration settings and sets up the indent level so that we
    * can predict the correct behavior for indenting.
    */
  public void setUp() throws Exception {
    super.setUp();
    DrJava.getConfig().resetToDefaults();
    _notifier = new GlobalEventNotifier();
    doc = new DefinitionsDocument(_notifier);
    DrJava.getConfig().setSetting(OptionConstants.INDENT_LEVEL,_indentLevel);
  }

  /** Tests the Comment Out Line(s) command with a single line. */
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

  /** Tests the Comment Out Line(s) command with multiple lines. */
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

  /** Tests the Uncomment Line(s) command with a single line.
    * These sample lines should be ignored by the algorithm.
    */
  public void testUncommentIgnoreSingleLine() throws BadLocationException {
    doc.uncommentLines(0,0);
    _assertContents("Uncommenting an empty document should not cause an error", "", doc);
    
    String text = 
      "Here is some abritrary text that should not be uncommented.\n" +
      "/* It is on multiple lines, and contains slashes // and other\n" +
      "* various */ obnoxious characters,\n" +
      "sometimes // in block comments and sometimes not.";

    doc.insertString(0, text, null);
    _assertContents("Sample text is inserted improperly.", text, doc);
    doc.uncommentLines(0,0);
    _assertContents("Uncommenting an uncommented line should not cause an error or modify the text", text, doc);
    doc.uncommentLines(70, 75);
    _assertContents("These lines should be unchanged by uncomment!", text, doc);
  }

  /** Tests the Uncomment Line(s) command with multiple lines.
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

  /** Tests the Uncomment Line(s) command with a single line.
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
      "         System.out.println(\"Aren't comments fun? // (yeah!)\")";

    doc.insertString(0, text, null);
    _assertContents("Sample text is inserted improperly.", text, doc);
    doc.uncommentLines(doc.getLength()-1, doc.getLength());
    _assertContents("The last line should have no commenting!",
                    uncommented, doc);
  }

  /** Tests the Uncomment Line(s) command with multiple lines.
   * These sample lines should be uncommented and indented by the algorithm.
   */
  public void testUncommentMultipleLines() throws BadLocationException {
    String text =
      "//// Here is some abritrary text that should be uncommented.\n" +
      "// /* along with a little bit of code, just to spice\n" +
      "//  * things up.\n" +
      "//  */ \n" +
      "// System.out.println(\"Aren't comments fun? // (yeah!)\")";

    String uncommented =
      "// Here is some abritrary text that should be uncommented.\n" +
      " /* along with a little bit of code, just to spice\n" +
      "  * things up.\n" +
      "  */ \n" +
      " System.out.println(\"Aren't comments fun? // (yeah!)\")";

    doc.insertString(0, text, null);
    _assertContents("Sample text is inserted improperly.", text, doc);
    doc.uncommentLines(0, doc.getLength());
    _assertContents("These lines should have at most one level of commenting!", uncommented, doc);
  }

  private static void _assertContents(String msg, String expected, DJDocument document)
    throws BadLocationException {
    assertEquals(msg, expected, document.getText());
  }
}
