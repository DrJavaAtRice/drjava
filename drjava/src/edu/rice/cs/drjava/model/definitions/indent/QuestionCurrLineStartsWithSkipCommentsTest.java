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

import junit.framework.*;
import junit.extensions.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

/**
 * Test class according to the JUnit protocol. Tests the proper functionality
 * of the class QuestionCurrLineStartsWithSkipComments.
 * @version $Id$
 */
public class QuestionCurrLineStartsWithSkipCommentsTest extends IndentRulesTestCase
{
  private String _text;
    
  private IndentRuleQuestion _rule;
      
  public QuestionCurrLineStartsWithSkipCommentsTest(String name) { super(name); }

  public void setUp() { super.setUp(); }

  public void testNoPrefix() throws BadLocationException
  {
    _text =
      "class A                 \n" + /*   0 */
      "{                       \n" + /*  25 */
      "    // one line comment \n" + /*  50 */
      "    int method1         \n" + /*  75 */
      "    /**                 \n" + /* 100 */
      "     * javadoc comment  \n" + /* 125 */
      "     */                 \n" + /* 150 */
      "    int method()        \n" + /* 175 */
      "    {                   \n" + /* 200 */
      "    }                   \n" + /* 225 */
      "    /* multi line       \n" + /* 250 */
      "       comment          \n" + /* 275 */
      "    boolean method()    \n" + /* 300 */
      "    {                   \n" + /* 325 */
      "    }                   \n" + /* 350 */
      "    */                  \n" + /* 375 */
      "}";                           /* 400 */

    _setDocText(_text);

    IndentRuleQuestion rule = new QuestionCurrLineStartsWithSkipComments("", null, null);

    // This rule should always apply, unless the entire line is inside a comment.
    
    assertTrue("At DOCSTART.", rule.applyRule(_doc, 0));
    assertTrue("At start of block.", rule.applyRule(_doc, 25));
    assertTrue("START starts one-line comment.", rule.applyRule(_doc, 54));
    assertTrue("START starts one-line comment.", rule.applyRule(_doc, 60));
    assertTrue("START starts javadoc comment.", rule.applyRule(_doc, 104));
    assertTrue("START starts javadoc comment.", rule.applyRule(_doc, 110));
    assertTrue("Line inside javadoc comment.", !rule.applyRule(_doc, 130));
    assertTrue("Line closes javadoc comment.", rule.applyRule(_doc, 150));
    assertTrue("START is free.", rule.applyRule(_doc, 180));
    assertTrue("START is free.", rule.applyRule(_doc, 230));
    assertTrue("START starts multi-line comment.", rule.applyRule(_doc, 260));
    assertTrue("Line inside multi-line comment.", !rule.applyRule(_doc, 275));
    assertTrue("Line inside multi-line comment.", !rule.applyRule(_doc, 300));
    assertTrue("Line closes multi-line comment.", rule.applyRule(_doc, 399));
    assertTrue("START is free.", rule.applyRule(_doc, 400));
    assertTrue("At end of document.", rule.applyRule(_doc, 401));
  }

  public void testOpenBracePrefix() throws BadLocationException
  {
    _text =
      "class A extends         \n" + /*   0 */
      "B {                     \n" + /*  25 */
      "    // {        }       \n" + /*  50 */
      "    int field;          \n" + /*  75 */
      "    /**                 \n" + /* 100 */
      "     * {        }       \n" + /* 125 */
      "     */                 \n" + /* 150 */
      "    int method() /*     \n" + /* 175 */
      " */ {                   \n" + /* 200 */
      "    }                   \n" + /* 225 */
      "    /* multi line       \n" + /* 250 */
      "       comment          \n" + /* 275 */
      "    boolean method()    \n" + /* 300 */
      "/**stuff*/   {  // stuff\n" + /* 325 */
      "             }          \n" + /* 350 */
      "                        \n" + /* 375 */
      "}";                           /* 400 */

    _setDocText(_text);

    _rule = new QuestionCurrLineStartsWithSkipComments("{", null, null);

    assertTrue("At DOCSTART - line doesn't start with an open brace.",      !_rule.applyRule(_doc,   0));
    assertTrue("Line starts a block, but not the start of the line.",       !_rule.applyRule(_doc,  25));
    assertTrue("Inside block - line starts with an alphanumeric character.",!_rule.applyRule(_doc,  30));
    assertTrue("Line starts a one-line comment.",                           !_rule.applyRule(_doc,  54));
    assertTrue("Line starts a one-line comment.",                           !_rule.applyRule(_doc,  60));
    assertTrue("Line starts with alphanumeric character.",                  !_rule.applyRule(_doc,  80));
    assertTrue("Line starts a javadoc comment.",                            !_rule.applyRule(_doc, 104));
    assertTrue("Line starts a javadoc comment.",                            !_rule.applyRule(_doc, 110));
    assertTrue("Line inside javadoc comment.",                              !_rule.applyRule(_doc, 130));
    assertTrue("Line starts with alphanumeric character.",                  !_rule.applyRule(_doc, 180));
    assertTrue("Line closes comment. It follows an open brace.",             _rule.applyRule(_doc, 201));
    assertTrue("Line closes comment. It follows an open brace.",             _rule.applyRule(_doc, 221));
    assertTrue("At end of block - line starts with a close brace.",         !_rule.applyRule(_doc, 225));
    assertTrue("Line starts a multi-line comment.",                         !_rule.applyRule(_doc, 260));
    assertTrue("Line inside multi-line comment.",                           !_rule.applyRule(_doc, 275));
    assertTrue("Line inside multi-line comment.",                           !_rule.applyRule(_doc, 300));
    assertTrue("Line closes comment. It follows an open brace.",             _rule.applyRule(_doc, 325));
    assertTrue("Line starts with a close brace.",                           !_rule.applyRule(_doc, 355));
    assertTrue("Empty line.",                                               !_rule.applyRule(_doc, 390));
    assertTrue("At last character - line starts with a close brace.",       !_rule.applyRule(_doc, 400));
    assertTrue("At end of document - line starts with a close brace.",      !_rule.applyRule(_doc, 401));
  }
    
  public void testCloseBracePrefix() throws BadLocationException
  {
    _text =
      "class A                 \n" + /*   0 */
      "{                       \n" + /*  25 */
      "    // }         }      \n" + /*  50 */
      "    int field;          \n" + /*  75 */
      "    /**                 \n" + /* 100 */
      "     * javadoc comment  \n" + /* 125 */
      "     */   }             \n" + /* 150 */
      "    int method()        \n" + /* 175 */
      "/**/}                   \n" + /* 200 */
      "/ * }                   \n" + /* 225 */
      "    /* multi line       \n" + /* 250 */
      "       comment          \n" + /* 275 */
      "    boolean method()    \n" + /* 300 */
      "    {                   \n" + /* 325 */
      "*/ / }                  \n" + /* 350 */
      "   * }                  \n" + /* 375 */
      "}";                           /* 400 */

    _setDocText(_text);
    
    _rule = new QuestionCurrLineStartsWithSkipComments("}", null, null);

    assertTrue("At DOCSTART - line doesn't start with a close brace.",      !_rule.applyRule(_doc,   0));
    assertTrue("At start of block - line starts with an open brace.",       !_rule.applyRule(_doc,  25));
    assertTrue("Inside block - line starts with an open brace.",            !_rule.applyRule(_doc,  30));
    assertTrue("Line starts a one-line comment.",                           !_rule.applyRule(_doc,  54));
    assertTrue("Line starts a one-line comment.",                           !_rule.applyRule(_doc,  60));
    assertTrue("Line starts with alphanumeric character.",                  !_rule.applyRule(_doc,  80));
    assertTrue("Line starts a javadoc comment.",                            !_rule.applyRule(_doc, 104));
    assertTrue("Line starts a javadoc comment.",                            !_rule.applyRule(_doc, 110));
    assertTrue("Line inside javadoc comment.",                              !_rule.applyRule(_doc, 130));
    assertTrue("Line closes multi-line comment, it follows a close brace.",  _rule.applyRule(_doc, 150));
    assertTrue("Line starts with alphanumeric character.",                  !_rule.applyRule(_doc, 180));
    assertTrue("Line starts with a comment, it follows a close brace.",      _rule.applyRule(_doc, 221));
    assertTrue("At end of block - line starts with a slash.",               !_rule.applyRule(_doc, 225));
    assertTrue("Line starts a multi-line comment.",                         !_rule.applyRule(_doc, 260));
    assertTrue("Line inside multi-line comment.",                           !_rule.applyRule(_doc, 275));
    assertTrue("Line inside multi-line comment.",                           !_rule.applyRule(_doc, 300));
    assertTrue("Line inside multi-line comment.",                           !_rule.applyRule(_doc, 325));
    assertTrue("Line closes multi-line comment, it follows a slash.",       !_rule.applyRule(_doc, 355));
    assertTrue("Line starts with a star.",                                  !_rule.applyRule(_doc, 376));
    assertTrue("At last character - line starts with a close brace.",        _rule.applyRule(_doc, 400));
    assertTrue("At end of document - line starts with a close brace.",       _rule.applyRule(_doc, 401));
  }
}
