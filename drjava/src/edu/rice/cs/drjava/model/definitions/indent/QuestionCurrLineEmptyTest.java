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
 * Tests Rule #3: Is the current line empty?
 * (see http://www.owlnet.rice.edu/~creis/comp312/indentrules.html)
 */

package edu.rice.cs.drjava.model.definitions.indent;

import junit.framework.*;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

// Rule #3: Is the current line empty?
public class QuestionCurrLineEmptyTest extends IndentRulesTestCase {
  public QuestionCurrLineEmptyTest(String name) {
    super(name);
  }
  static IndentRuleQuestion _rule = new QuestionCurrLineEmpty(null, null);

   
  public void testEmpty() throws javax.swing.text.BadLocationException {
    // /*
    // 
    // */
    _setDocText("/*\n\n*/");
    //               .
    assertTrue("nothing on line", _rule.applyRule(_doc, 3));
  }
  public void testSpaces() throws javax.swing.text.BadLocationException {
    // /*
    //                         [some spaces]
    // */
    _setDocText("/*\n        \n*/");
    //                  .
    assertTrue("only spaces", _rule.applyRule(_doc, 6));
  }
  
  static String stuffExample = "/*\n   foo   \n*/";
  //                                .       .
  // /*
  //     foo   
  // */
  
  public void testStuffBefore() throws javax.swing.text.BadLocationException {
    _setDocText(stuffExample);
    assertTrue("text before the cursor", !_rule.applyRule(_doc, 3));
  }
  public void testStuffAfter() throws javax.swing.text.BadLocationException {
    _setDocText(stuffExample);
    assertTrue("text after the cursor", !_rule.applyRule(_doc, 11));
  }
  
  public void testLineWithStar() throws javax.swing.text.BadLocationException {
    _setDocText("/*\n * foo\n */");
    assertTrue("line with a star", !_rule.applyRule(_doc, 5));
  }
}
