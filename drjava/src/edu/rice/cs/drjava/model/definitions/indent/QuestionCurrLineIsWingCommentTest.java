/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

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

package edu.rice.cs.drjava.model.definitions.indent;

/** Tests visitor class that determines whether the current line is a wing comment.
 * 
 * @version $Id$
 */
public final class QuestionCurrLineIsWingCommentTest extends IndentRulesTestCase {

  static IndentRuleQuestion _rule = new QuestionCurrLineIsWingComment(null, null);

  public void testWingComment() throws javax.swing.text.BadLocationException {
    _setDocText("// This is a wing comment");
    assertTrue("A valid wing comment 1", _rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("A valid wing comment 2", _rule.applyRule(_doc, 4, Indenter.IndentReason.OTHER));
    assertTrue("A valid wing comment 3", _rule.applyRule(_doc, 10, Indenter.IndentReason.OTHER));
  }
  public void testSpaces() throws javax.swing.text.BadLocationException {
    _setDocText("/*\n \n*/");
    assertFalse("A block comment 1", _rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertFalse("A block comment 2", _rule.applyRule(_doc, 3, Indenter.IndentReason.OTHER));
    assertFalse("A block comment 3", _rule.applyRule(_doc, 5, Indenter.IndentReason.OTHER));
  }
  
  static String cornerCase = " //\n";
  
  public void testCornerCase() throws javax.swing.text.BadLocationException {
    _setDocText(cornerCase);
    assertFalse("Corner Case 1", _rule.applyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertFalse("Corner Case 2", _rule.applyRule(_doc, 1, Indenter.IndentReason.OTHER));
    assertFalse("Corner Case 3", _rule.applyRule(_doc, 2, Indenter.IndentReason.OTHER));
    assertFalse("Corner Case 4", _rule.applyRule(_doc, 3, Indenter.IndentReason.OTHER));
  }
   
  public void testWingInsideBlock() throws javax.swing.text.BadLocationException {
    _setDocText("/*//\n \n */");
    assertFalse("Wing Inside BlockComment 1", _rule.applyRule(_doc, Indenter.IndentReason.OTHER));
    assertFalse("Wing Inside BlockComment 2", _rule.applyRule(_doc, 2, Indenter.IndentReason.OTHER));
  }
}
