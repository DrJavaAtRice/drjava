/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import junit.framework.*;
import junit.extensions.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.rice.cs.drjava.model.AbstractDJDocument;

/**
 * Test class according to the JUnit protocol. Tests the action
 * that aligns the indentation of the current line to the character
 * that opened the most recent block or expression list that contains
 * the beginning of the current line. Optional additional whitespaces 
 * can be passed through the constructor.
 * @version $Id$
 */
public final class ActionBracePlusTest extends IndentRulesTestCase 
{
  private String _text, _aligned;
  
  private IndentRuleAction _action;
  
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
    _action.indentLine(_doc, 0, Indenter.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 7, Indenter.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 8, Indenter.OTHER); // Aligns second line.
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
    _action.indentLine(_doc, 0, Indenter.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 18, Indenter.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 20, Indenter.OTHER); // Aligns second line.
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
    _action.indentLine(_doc, 0, Indenter.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 15, Indenter.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 16, Indenter.OTHER); // Aligns second line.
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
    _action.indentLine(_doc, 0, Indenter.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 15, Indenter.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 20, Indenter.OTHER); // Aligns second line.
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
    _action.indentLine(_doc, 0, Indenter.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 6, Indenter.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 10, Indenter.OTHER); // Aligns second line.
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
    _action.indentLine(_doc, 0, Indenter.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 17, Indenter.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.indentLine(_doc, 25, Indenter.OTHER); // Aligns second line.
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
    _action.indentLine(_doc, 14, Indenter.OTHER); // line 3
    _action.indentLine(_doc, 27, Indenter.OTHER); // line 4
    _action.indentLine(_doc, 43, Indenter.OTHER); // line 5
    assertEquals("Lines aligned plus one level.",
                 _aligned, _doc.getText(0, _doc.getLength()));
    
    _action.indentLine(_doc, 54, Indenter.OTHER); // after baz()
    assertEquals("Cursor after baz().",
                 _aligned, _doc.getText(0, _doc.getLength()));
  }
}
