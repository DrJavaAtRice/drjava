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
 * Test class according to the JUnit protocol. Tests the action
 * that aligns the indentation of the current line to the character
 * that opened the most recent block or expression list that contains
 * the beginning of the current line. Optional additional whitespaces 
 * can be passed through the constructor.
 * @version $Id$
 */
public class ActionBracePlusTest extends IndentRulesTestCase 
{
  private String _text, _aligned;
  
  private IndentRuleAction _action;
  
  /** @param name The name of this test case. */
  public ActionBracePlusTest(String name) { super(name); }
  
  public void setUp() {
    super.setUp();
  }
  
  public void testNoSuffix() throws BadLocationException
  {
    _action = new ActionBracePlus("");
    
    // (1) 
    
    _text = 
      "method(\n"+
      ")\n";

    _aligned = 
      "method(\n"+
      "      )\n";
 
    _setDocText(_text);
    _action.indentLine(_doc, 0); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 7); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 8); // Aligns second line.
    assertEquals("Line aligned to open paren.", _aligned.length(), _doc.getLength());
    assertEquals("Line aligned to open paren.", _aligned, _doc.getText(0, _doc.getLength()));
  }
  

  public void testSpaceSuffix() throws BadLocationException
  {
    _action = new ActionBracePlus(" ");
    
    // (2) 
    
    _text = 
     "var = method(arg1,\n"+
     "  arg2, arg3) + 4;";

    _aligned = 
     "var = method(arg1,\n"+
     "             arg2, arg3) + 4;";
 
    _setDocText(_text);
    _action.indentLine(_doc, 0); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 18); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 20); // Aligns second line.
    assertEquals("Line aligned to open paren.", _aligned.length(), _doc.getLength());
    assertEquals("Line aligned to open paren.", _aligned, _doc.getText(0, _doc.getLength()));
    
    // (3) 
     
    _text =
     "boolean method(\n"+
     "int[] a, String b)\n"+
     "{}";
    _aligned = 
     "boolean method(\n"+
     "               int[] a, String b)\n"+
     "{}";

    _setDocText(_text);
    _action.indentLine(_doc, 0); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 15); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 16); // Aligns second line.
    assertEquals("Line aligned to open paren.", _aligned.length(), _doc.getLength());
    assertEquals("Line aligned to open paren.", _aligned, _doc.getText(0, _doc.getLength()));
 
    // (4) 
 
    _text =
     "boolean method(\n"+
     "int[] a,\n"+
     "               String b)\n"+
     "{}";
    _aligned = 
     "boolean method(\n"+
     "               int[] a,\n"+
     "               String b)\n"+
     "{}";

    _setDocText(_text);
    _action.indentLine(_doc, 0); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 15); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 20); // Aligns second line.
    assertEquals("Line aligned to open paren.", _aligned, _doc.getText(0, _doc.getLength()));
 
    // (5) 

    _text =
     "array[\n"+
     "              new Listener() {\n"+
     "           method() {\n"+
     "           }\n"+
     "      }]";
    _aligned =
     "array[\n"+
     "      new Listener() {\n"+
     "           method() {\n"+
     "           }\n"+
     "      }]";

    _setDocText(_text);
    _action.indentLine(_doc, 0); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 6); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 10); // Aligns second line.
    assertEquals("Line aligned to open bracket.", _aligned, _doc.getText(0, _doc.getLength())); 

  }
  
  public void testLargeSuffix() throws BadLocationException
  {
    _action = new ActionBracePlus(" " + "  ");
    
    // (6) 
    
    _text = 
     "var = method(foo.\n"+
     "  bar(), arg3) + 4;";

    _aligned = 
     "var = method(foo.\n"+
     "               bar(), arg3) + 4;";
 
    _setDocText(_text);
    _action.indentLine(_doc, 0); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 17); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 25); // Aligns second line.
    assertEquals("Line aligned to open paren.", _aligned.length(), _doc.getLength());
    assertEquals("Line aligned to open paren.", _aligned, _doc.getText(0, _doc.getLength()));
  }
  
  public void testComment() throws BadLocationException
  {
    _action = new ActionBracePlus(" " + "  ");
    
    // (7) 
    
    _text = 
      "foo(i,\n"+
      "    j.\n" +
      "bar().\n" +
      "// bar();\n" +
      "baz(),\n" +
      "    k);";

    _aligned = 
      "foo(i,\n"+
      "    j.\n" +
      "      bar().\n" +
      "      // bar();\n" +
      "      baz(),\n" +
      "    k);";
 
    _setDocText(_text);
    _action.indentLine(_doc, 14); // line 3
    _action.indentLine(_doc, 27); // line 4
    _action.indentLine(_doc, 43); // line 5
    assertEquals("Lines aligned plus one level.",
                 _aligned, _doc.getText(0, _doc.getLength()));
    
    _action.indentLine(_doc, 54); // after baz()
    assertEquals("Cursor after baz().",
                 _aligned, _doc.getText(0, _doc.getLength()));
  }
}
