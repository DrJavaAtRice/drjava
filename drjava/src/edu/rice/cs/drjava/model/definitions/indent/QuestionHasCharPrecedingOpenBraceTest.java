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
 * of the class QuestionHasCharPrecedingOpenBrace.
 * @version $Id$
 */
public class QuestionHasCharPrecedingOpenBraceTest extends IndentRulesTestCase
{
  private String _text;
    
  private IndentRuleQuestion _rule;
      
  public QuestionHasCharPrecedingOpenBraceTest(String name) { super(name); }

  public void setUp() { super.setUp(); }

  public void testIsIn1DArray() throws BadLocationException
  { //01234567890123456789012345
    _text =
      "int[2][] a =            \n" + /*   0 */
      "{                       \n" + /*  25 */
      "    a, //  line comment \n" + /*  50 */
      "    int b,              \n" + /*  75 */
      "    /**                 \n" + /* 100 */
      "     * javadoc comment  \n" + /* 125 */
      "     */                 \n" + /* 150 */
      "    START               \n" + /* 175 */
      "    },                  \n" + /* 200 */
      "    {                   \n" + /* 225 */
      "    /*  {  multi line   \n" + /* 250 */
      "       comment  }       \n" + /* 275 */
      "    boolean method()    \n" + /* 300 */
      "    {                   \n" + /* 325 */
      "    }                   \n" + /* 350 */
      "    */}                 \n" + /* 375 */
      "}";                           /* 400 */

    _setDocText(_text);
    
    char [] chars = {'='};
    IndentRuleQuestion rule = new QuestionHasCharPrecedingOpenBrace(chars, null, null);

    assertTrue("At DOCSTART.", ! rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("At identifier.",  ! rule.applyRule(_doc, 10, Indenter.OTHER));
    assertTrue("At start of array.", !rule.applyRule(_doc, 25, Indenter.OTHER));
    assertTrue("START starts one-line comment.", rule.applyRule(_doc, 54, Indenter.OTHER));
    assertTrue("START starts one-line comment.", rule.applyRule(_doc, 60, Indenter.OTHER));
    assertTrue("START starts javadoc comment.", rule.applyRule(_doc, 104, Indenter.OTHER));
    assertTrue("START starts javadoc comment.", rule.applyRule(_doc, 110, Indenter.OTHER));
    assertTrue("Line inside javadoc comment.", rule.applyRule(_doc, 130, Indenter.OTHER));
    assertTrue("Line closes javadoc comment.", rule.applyRule(_doc, 150, Indenter.OTHER));
    assertTrue("START is stil in first.", rule.applyRule(_doc, 180, Indenter.OTHER));
    assertTrue("Second pseudo array element.", ! rule.applyRule(_doc, 230, Indenter.OTHER));
    assertTrue("Start of multi-line comment.", !rule.applyRule(_doc, 260, Indenter.OTHER));
    assertTrue("Line inside multi-line comment.", !rule.applyRule(_doc, 275, Indenter.OTHER));
    assertTrue("Line inside multi-line comment.", !rule.applyRule(_doc, 300, Indenter.OTHER));
    assertTrue("Line closes multi-line comment.", !rule.applyRule(_doc, 399, Indenter.OTHER));
    assertTrue("Last close brace", !rule.applyRule(_doc, 400, Indenter.OTHER));
    assertTrue("At end of document.", !rule.applyRule(_doc, 401, Indenter.OTHER));
  }
  public void testIsIn2DArray() throws BadLocationException
  { //01234567890123456789012345
    _text =
      "int[2][] a =            \n" + /*   0 */
      "{                       \n" + /*  25 */
      "  {                     \n" + /*  50 */
      "    a, //  line comment \n" + /*  75 */
      "    int b,              \n" + /* 100 */
      "    /**                 \n" + /* 125 */
      "     */                 \n" + /* 150 */
      "    START               \n" + /* 175 */
      "    },                  \n" + /* 200 */
      "    {                   \n" + /* 225 */
      "    /* = { multi line   \n" + /* 250 */
      "       comment  }       \n" + /* 275 */
      "    boolean method()    \n" + /* 300 */
      "    {                   \n" + /* 325 */
      "    }                   \n" + /* 350 */
      "    */}                 \n" + /* 375 */
      "}"                          + /* 400 */
      "";

    _setDocText(_text);
    
    char [] chars = {'='};
    IndentRuleQuestion rule = new QuestionHasCharPrecedingOpenBrace(chars, null, null);

    assertTrue("At DOCSTART.", ! rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("At identifier.",  ! rule.applyRule(_doc, 10, Indenter.OTHER));
    assertTrue("At start of outer array", !rule.applyRule(_doc, 25, Indenter.OTHER));

    assertTrue("Before start of inner array", rule.applyRule(_doc, 50, Indenter.OTHER));

    assertTrue("Same line as inner {.", rule.applyRule(_doc, 54, Indenter.OTHER));
    assertTrue("Line after inner {.", !rule.applyRule(_doc, 75, Indenter.OTHER));
    assertTrue("START is stil in first.", !rule.applyRule(_doc, 180, Indenter.OTHER));

    assertTrue("Second pseudo array element.",  rule.applyRule(_doc, 230, Indenter.OTHER));
    assertTrue("In multi-line comment.", ! rule.applyRule(_doc, 260, Indenter.OTHER));

    assertTrue("multi-line comment w/ = {.",  ! rule.applyRule(_doc, 275, Indenter.OTHER));

    assertTrue("Line inside multi-line comment.", !rule.applyRule(_doc, 300, Indenter.OTHER));
    assertTrue("Line closes multi-line comment.", !rule.applyRule(_doc, 399, Indenter.OTHER));

    assertTrue("Last close brace",  rule.applyRule(_doc, 400, Indenter.OTHER));
    assertTrue("At end of document.",  rule.applyRule(_doc, 401, Indenter.OTHER));
  }
  public void testNoEquals() throws BadLocationException
  { //01234567890123456789012345
    _text =
      "int[2][] a             \n" + /*   0 */
      "{                       \n" + /*  25 */
      "  {                     \n" + /*  50 */
      "    a, //  line comment \n" + /*  75 */
      "    int b,              \n" + /* 100 */
      "    /**                 \n" + /* 125 */
      "     */                 \n" + /* 150 */
      "    START               \n" + /* 175 */
      "    },                  \n" + /* 200 */
      "    {                   \n" + /* 225 */
      "    /* = { multi line   \n" + /* 250 */
      "       comment  }       \n" + /* 275 */
      "    boolean method()    \n" + /* 300 */
      "    {                   \n" + /* 325 */
      "    }                   \n" + /* 350 */
      "    */}                 \n" + /* 375 */
      "}"                          + /* 400 */
      "";

    _setDocText(_text);
    
    char [] chars = {'='};
    IndentRuleQuestion rule = new QuestionHasCharPrecedingOpenBrace(chars, null, null);

    assertTrue("At DOCSTART.",    ! rule.applyRule(_doc, 0, Indenter.OTHER));
    assertTrue("At identifier.",  ! rule.applyRule(_doc, 10, Indenter.OTHER));
    assertTrue("At start of outer array", !rule.applyRule(_doc, 25, Indenter.OTHER));

    assertTrue("Before start of inner array", ! rule.applyRule(_doc, 50, Indenter.OTHER));
    assertTrue("Same line as inner {.", !rule.applyRule(_doc, 54, Indenter.OTHER));
    assertTrue("Line after inner {.", !rule.applyRule(_doc, 75, Indenter.OTHER));
    assertTrue("START is stil in first.", !rule.applyRule(_doc, 180, Indenter.OTHER));

  }
}
