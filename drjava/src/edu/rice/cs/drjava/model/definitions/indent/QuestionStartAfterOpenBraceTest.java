

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
import edu.rice.cs.drjava.model.definitions.reducedmodel.IndentInfo;
import edu.rice.cs.util.UnexpectedException;

/**
 * Test class according to the JUnit protocol.
 * Tests the question determining whether or not the closest non-whitespace character
 * previous to the start of the current line (excluding any characters
 * inside comments or strings) is an open brace.
 *
 * @version $Id$
 */
public class QuestionStartAfterOpenBraceTest extends IndentRulesTestCase 
{
    private String _text;
    
    private IndentRuleQuestion _rule = new QuestionStartAfterOpenBrace(null, null);

    /**
     * @param name The name of this test case.
     */
    public QuestionStartAfterOpenBraceTest(String name) { super(name); }
    
    public void setUp() { super.setUp(); }    
    
    public void testWithFree() throws BadLocationException 
    {
	int i;

	/* (1) */
	
	_text = "method(\nint[] a, String b) {}";
	_setDocText(_text);
	assertTrue("START has no preceding brace.", !_rule.applyRule(_doc, 0));
	assertTrue("START immediately follows an open paren, not a brace.", !_rule.applyRule(_doc, 8));
	assertTrue("START immediately follows an open paren, not a brace.", !_rule.applyRule(_doc, _text.length()-1));

	/* (2) */

	_text = 
	    "boolean method() {\n" +
	    "}";

	_setDocText(_text);
	assertTrue("START immediately follows an open brace.", _rule.applyRule(_doc, 19));

	/* (3) */

	_text = 
	    "boolean method(\n" +
	    "    int[] a, String b)\n" +
	    "{\n" +
	    "}";

	_setDocText(_text);	
	assertTrue("START immediately follows an open paren.", !_rule.applyRule(_doc, 40));
	assertTrue("START immediately follows an open brace.", _rule.applyRule(_doc, 41));

	/* (5) */

	_text = 
	    "if (<cond>) {\n" +
	    "\n" +
	    "    if (\n" +
	    "        <cond>) { ... }}";

	_setDocText(_text);

	assertTrue("START immediatly follows an open brace.", _rule.applyRule(_doc, 14));	    
	assertTrue("Only WS between open brace and START.", _rule.applyRule(_doc, 15));	    
	assertTrue("Only WS between open brace and START.", _rule.applyRule(_doc, 23));	    
	assertTrue("START immediatly follows an open paren.", !_rule.applyRule(_doc, 25));	    
	
	/* (6) */

	_text = 
	    "class Foo {   \n" +
	    "              \n" +
	    "  /*          \n" +
	    "   *          \n" +
	    "   */         \n" +
	    "  int field;  \n" +
	    "}";
 
	_setDocText(_text);

	assertTrue("START = DOCSTART.", !_rule.applyRule(_doc, 0));
	assertTrue("START = DOCSTART.", !_rule.applyRule(_doc, 14));
	assertTrue("Only WS between START and open brace.", _rule.applyRule(_doc, 15));
	assertTrue("Only WS between START and open brace.", _rule.applyRule(_doc, 30));
	assertTrue("Only WS between START and open brace.", _rule.applyRule(_doc, 44));
	assertTrue("Only comment and WS between START and open brace.", _rule.applyRule(_doc, 45));
	assertTrue("Only comment and WS between START and open brace.", _rule.applyRule(_doc, 60));
	assertTrue("Only comment and WS between START and open brace.", _rule.applyRule(_doc, 77));
    }
}
  
