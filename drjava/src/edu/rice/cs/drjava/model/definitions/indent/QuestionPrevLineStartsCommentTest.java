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
/**
 * @version $Id$
 *
 * Tests Rule #2: Does the previous line start the comment?
 * (see http://www.owlnet.rice.edu/~creis/comp312/indentrules.html)
 */

package edu.rice.cs.drjava.model.definitions.indent;

import junit.framework.*;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

// Rule #2: Does the previous line start the comment?
public class QuestionPrevLineStartsCommentTest extends IndentRulesTestCase {
  public QuestionPrevLineStartsCommentTest(String name) {
    super(name);
  }
  // generic rule always returns false...
  // to use until we get the actual implementation
  static IndentRuleQuestion rule2 = new QuestionPrevLineStartsComment(null,
								      null);
  private static String example1 = "/*\nfoo\nbar\n*/";
  //                                    .    .
  // /* 
  // foo
  // bar
  // */
  private static String example2 = "foo /* bar\nblah\nmoo\n*/";
  //                                            .     .
  // foo /* bar
  // blah
  // moo
  // */
  private static String example3 = "/*\nfoo\n// /*\nbar\n*/";
  //                                    .    .      .
  // /*
  // foo
  // // /*
  // bar
  // */

  public void testSimpleFirstLine() throws javax.swing.text.BadLocationException {
    _setDocText(example1);
    assertEquals(true, rule2.applyRule(_doc, 3));
  }
  
  public void testSimpleSecondLine() throws javax.swing.text.BadLocationException {
    _setDocText(example1);
    assertEquals(false, rule2.applyRule(_doc, 7));
  }
  
  public void testSlashStarMidLineFirstLine() throws javax.swing.text.BadLocationException {
    _setDocText(example2);
    //_doc.setCurrentLocation(11);
    assertEquals(true, rule2.applyRule(_doc, 11));
  }
  public void testSlashStarMidLineSecondLine() throws javax.swing.text.BadLocationException {
    _setDocText(example2);
    //_doc.setCurrentLocation(16);
    assertEquals(false, rule2.applyRule(_doc, 16));
  }
  public void testCommentedOutSlashStarBefore() throws javax.swing.text.BadLocationException {
    _setDocText(example3);
    //_doc.setCurrentLocation(3);
    assertEquals(true, rule2.applyRule(_doc, 3));
  }
  public void testCommentedOutSlashStarAt() throws javax.swing.text.BadLocationException {
    _setDocText(example3);
    //_doc.setCurrentLocation(7);
    assertEquals(false, rule2.applyRule(_doc, 7));
  }
  public void testCommentedOutSlashStarAfter() throws javax.swing.text.BadLocationException {
    _setDocText(example3);
    //_doc.setCurrentLocation(13);
    assertEquals(false, rule2.applyRule(_doc, 13));
  }
}


