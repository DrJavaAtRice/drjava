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

/**
 * Test the comment lines / uncomment lines functionality.
 * @version $Id$
 */
public class CommentTest extends TestCase {
  protected DefinitionsDocument doc;
  private Integer indentLevel = new Integer(2);
  private Integer configIndent;

  /**
   * put your documentation comment here
   * @param     String name
   */
  public CommentTest(String name) {
    super(name);
  }

  /**
   * Resents configuration settings and sets up the indent level so that we
   * can predict the correct behavior for indenting.
   */
  public void setUp() {
    DrJava.getConfig().resetToDefaults();
    doc = new DefinitionsDocument();
    DrJava.getConfig().setSetting(OptionConstants.INDENT_LEVEL,indentLevel);
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
      "// /* It is on multiple lines, and contains slashes // and other\n" +
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
      "// Here is some abritrary text that should be commented.\n" +
      "// /* It is on multiple lines, and contains slashes // and other\n" +
      "// various */ obnoxious characters.\n";
    
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
  
  private static void _assertContents(String msg, String expected,
                                      Document document)
    throws BadLocationException
  {
    assertEquals(msg, expected, 
                 document.getText(0, document.getLength()));
  }
  
}