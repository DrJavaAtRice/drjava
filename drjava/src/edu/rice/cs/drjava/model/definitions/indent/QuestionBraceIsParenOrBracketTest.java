

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
 * Test class according to the JUnit protocol. Tests the question
 * that determines whether or not the last block or expression list 
 * opened previous to the start of the current line was opened 
 * by one of the characters '(' or '['. 
 * This questions corresponds to rule 11 in our decision tree.
 * @version $Id$
 */
public class QuestionBraceIsParenOrBracketTest extends IndentRulesTestCase 
{
    // PRE: We are not inside a multiline comment.
	
    private String _text;

    private final IndentRuleQuestion _rule = new QuestionBraceIsParenOrBracket(null, null);

    /**
     * @param name The name of this test case.
     */
    public QuestionBraceIsParenOrBracketTest(String name) { super(name); }
    
    public void setUp() { super.setUp(); }
    
    public void testParen() throws BadLocationException 
    {
	int i;

	/* (1) */ 
	
	_text = "boolean method(int[] a, String b)";
	_setDocText(_text);

	for (i = 0; i < _text.length(); i++)
	    assertTrue("START has no brace.", !_rule.applyRule(_doc, i));

	/* (2) */ 

	_text = 
	    "boolean method\n" +
	    "    (int[] a, String b)";

	_setDocText(_text);

	for (i = 0; i < _text.length(); i++)
	    assertTrue("START has no brace.", !_rule.applyRule(_doc, i));

	/* (3) */ 

	_text = 
	    "boolean method(\n" +
	    "    int[] a, String b)";

	_setDocText(_text);

	for (i = 0; i < 16; i++)
	    assertTrue("START has no brace.", !_rule.applyRule(_doc, i));
	
	// For any position past the '\n' character, the rule applies:
 
	for (i = 16; i < _text.length(); i++)
	    assertTrue("START's brace is an open paren.", _rule.applyRule(_doc, i));	    

	/* (4) */ 

	_text = 
	    "if (<cond>) {\n" +
	    "    if (\n" +
	    "        <cond>) { ... }}";

	_setDocText(_text);

	for (i = 0; i < 23; i++)
	    assertTrue("START has no brace.", !_rule.applyRule(_doc, i));	    

	// For any position past the second '\n' character, the rule applies:

	for (i = 23; i < _text.length(); i++)
	    assertTrue("START's brace is an open paren.", _rule.applyRule(_doc, i));	    

	/* (5) */ 

	_text = 
	    "method(\n" +
	    "       array1, foo(array1[x]))\n" +
	    " <other stuff>";

	_setDocText(_text);

	assertTrue("START has no brace.", !_rule.applyRule(_doc, 0));	    
	assertTrue("START has no brace", !_rule.applyRule(_doc, 7));	    
	assertTrue("START's brace is an open paren.", _rule.applyRule(_doc, 8));	    
	assertTrue("START's brace is an open paren.", _rule.applyRule(_doc, 30));	    
	assertTrue("START has no brace.", !_rule.applyRule(_doc, _text.length() - 1));	    
    }

    public void testBracket() throws BadLocationException
    {
	int i;

	/* (1) */ 

	_text = 
	    "boolean method(int[\n" +
	    "                   ELTS]\n" +
	    "               a, String b)";

	_setDocText(_text);

	for (i = 0; i < 20; i++)
	    assertTrue("START has no brace.", !_rule.applyRule(_doc, i));
	
	// For any position past the first '\n' character, the rule applies:
 
	for (i = 20; i < 29; i++)
	    assertTrue("START's brace is an open bracket.", _rule.applyRule(_doc, i));	    

	for (i = 29; i < _text.length(); i++)
	    assertTrue("START's brace is an open paren.", _rule.applyRule(_doc, i));	    

	/* (2) */ 

	_text = "array1[i]\n" +
	    "       [j]";
	
	_setDocText(_text);

	for (i = 0; i < _text.length(); i++)
	    assertTrue("START has no brace.", !_rule.applyRule(_doc, i));

	/* (3) */ 

	_text = 
	    "array1[\n" +
	    "           i][\n" +
	    "              j]";

	_setDocText(_text);	

	assertTrue("START's paren is an open bracket.", _rule.applyRule(_doc, 8));
	assertTrue("START's paren is an open bracket.", _rule.applyRule(_doc, 22));
	assertTrue("START's paren is an open bracket.", _rule.applyRule(_doc, 23));
    }

    public void testCurly() throws BadLocationException
    {
	int i;

	/* (1) */ 
	
	_text = 
	    "class X extends Base\n" +
	    "{\n" +
	    "}";

	_setDocText(_text);

	assertTrue("START has no brace.", !_rule.applyRule(_doc, 0));
	assertTrue("START has no brace.", !_rule.applyRule(_doc, 20));
	assertTrue("START is curly brace.", !_rule.applyRule(_doc, 21));
	assertTrue("START is close brace.", !_rule.applyRule(_doc, 23));
	
	/* (2) */ 
	
	_text = 
	    "class X extends Base\n" +
	    "{\n" +
	    "    int bla() { return 44; }\n" +
	    "}";

	_setDocText(_text);

	assertTrue("START has no brace.", !_rule.applyRule(_doc, 0));
	assertTrue("START has no brace.", !_rule.applyRule(_doc, 20));
	assertTrue("START is curly brace.", !_rule.applyRule(_doc, 21));
	assertTrue("START's brace is curly brace.", !_rule.applyRule(_doc, 23));
	assertTrue("START is close curly brace.", !_rule.applyRule(_doc, _text.length() - 1));

	/* (3) */ 
	
	_text = 
	    "class X extends Base\n" +
	    "{}\n" +
	    "class Y extends Base\n" +
	    "{}";

	_setDocText(_text);

	assertTrue("START has no brace.", !_rule.applyRule(_doc, 0));
	assertTrue("START has no brace.", !_rule.applyRule(_doc, 20));
	assertTrue("START is open curly brace.", !_rule.applyRule(_doc, 21));
	assertTrue("START has no brace.", !_rule.applyRule(_doc, 24));
	assertTrue("START's brace is open curly brace.", !_rule.applyRule(_doc, _text.length() - 1));	
    }
}

  




