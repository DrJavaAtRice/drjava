

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
 * Test class according to the JUnit protocol. Tests the question
 * that determines whether or not the last opened block or expression 
 * list containing the start of the current line (opened by one of 
 * the characters '{', '(', or '[') was opened on the previous line 
 * (relative to the current position in the document). 
 * This question corresponds to rule 16 in our decision tree.
 * @version $Id$
 */
public class QuestionBraceOnPrevLineTest extends IndentRulesTestCase 
{
    // PRE: We are not inside a multiline comment.
    // PRE: The most recently opened expression list or block
    //      was opened by a '{'.                           

    private String _text;
    
    private IndentRuleQuestion _rule = new QuestionBraceOnPrevLine(null, null);

    /**
     * @param name The name of this test case.
     */
    public QuestionBraceOnPrevLineTest(String name) { super(name); }
    
    public void setUp() { super.setUp(); }    
    
    public void testWithParen() throws BadLocationException 
    {
	int i;

	/* (1) */
	
	_text = "method(\nint[] a, String b) {}";
	_setDocText(_text);

	try { _rule.applyRule(_doc, 0); fail("There is no brace."); }
	catch (UnexpectedException e) {}

	assertTrue("START's brace ('(') is on the previous line.", _rule.applyRule(_doc, 8));
	assertTrue("START's brace ('(') is on the previous line.", _rule.applyRule(_doc, _text.length() - 1));

	/* (2) */

	_text = 
	    "boolean method() {\n" +
	    "}";

	_setDocText(_text);

        try { _rule.applyRule(_doc, 18); fail("START has no brace."); }
	catch (UnexpectedException e) {}

	assertTrue("START's brace ('{') is on previous line.", _rule.applyRule(_doc, 19));

	/* (3) */

	_text = 
	    "boolean method(\n" +
	    "    int[] a, String b)\n" +
	    "{}";

	_setDocText(_text);
	
        try { _rule.applyRule(_doc, 10); fail("START has no brace."); }
	catch (UnexpectedException e) {}

	assertTrue("START's brace ('(') is on previous line.", _rule.applyRule(_doc, 16));

        try { _rule.applyRule(_doc, _text.length()-1); fail("START has no brace."); }
	catch (UnexpectedException e) {}

	/* (4) */

	_text = 
	    "boolean method(\n" +
	    "    int[] a,\n" +
	    "    String b)\n" +
	    "{}";

	_setDocText(_text);
	
        try { _rule.applyRule(_doc, 10); fail("START has no brace."); }
	catch (UnexpectedException e) {}

	assertTrue("START's brace ('(') is on previous line.", _rule.applyRule(_doc, 16));
	assertTrue("START's brace ('(') is two lines above.", !_rule.applyRule(_doc, 30));

        try { _rule.applyRule(_doc, _text.length()-1); fail("START has no brace."); }
	catch (UnexpectedException e) {}

	/* (5) */

	_text = 
	    "if (<cond>) {\n" +
	    "    if (\n" +
	    "        <cond>) { ... }}";

	_setDocText(_text);

        try { _rule.applyRule(_doc, 10); fail("START has no brace."); }
	catch (UnexpectedException e) {}

	assertTrue("START's brace ('{') is on previous line.", _rule.applyRule(_doc, 17));	    
	assertTrue("START's brace ('(') is on previous line.", _rule.applyRule(_doc, 23));	    
	
	/* (6) */

	_text = 
	    "array[\n" +
	    "    new Listener() {\n" +
	    "        method() {\n" +
	    "        }\n" +
	    "    }]";

	_setDocText(_text);

        try { _rule.applyRule(_doc, 0); fail("START has no brace."); }
	catch (UnexpectedException e) {}

	assertTrue("START's brace ('[') is on previous line.", _rule.applyRule(_doc, 7));	    
	assertTrue("START's brace ('{') is on previous line.", _rule.applyRule(_doc, 28));	    
	assertTrue("START's brace ('{') is on previous line.", _rule.applyRule(_doc, 50));	    
	assertTrue("START's brace ('{') is three lines above.", !_rule.applyRule(_doc, _text.length() - 1));	    
    }

    public void testOnlyCurly() throws BadLocationException
    {
	/* (1) */

	_text =
	    "{ /* block1 */ }\n" +
	    "{ /* block2 */ }\n" +
	    "{ /* block3 */ }";
	
	_setDocText(_text);
        try { _rule.applyRule(_doc, 0); fail("START has no brace."); }
	catch (UnexpectedException e) {}
        try { _rule.applyRule(_doc, 7); fail("START has no brace."); }
	catch (UnexpectedException e) {}
        try { _rule.applyRule(_doc, 28); fail("START has no brace."); }
	catch (UnexpectedException e) {}
        try { _rule.applyRule(_doc, 30); fail("START has no brace."); }
	catch (UnexpectedException e) {}
        try { _rule.applyRule(_doc, _text.length()-1); fail("START has no brace."); }
	catch (UnexpectedException e) {}

	/* (2) */

	_text =
	    "{\n" +
	    "    {\n" +
	    "        {}\n" +
	    "    }\n" +
	    "}";
	
	_setDocText(_text);

	assertTrue("START's brace ('{') is on previous line.", _rule.applyRule(_doc, 7));	    
	assertTrue("START's brace ('{') is on previous line.", _rule.applyRule(_doc, 8));	    
	assertTrue("START's brace ('{') is two lines above.", !_rule.applyRule(_doc, 19));	    
	assertTrue("START's brace ('{') is four lines above.", !_rule.applyRule(_doc, _text.length() - 1));	    
    }
}
  
