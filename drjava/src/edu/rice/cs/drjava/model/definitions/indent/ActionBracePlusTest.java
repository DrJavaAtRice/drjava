/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import javax.swing.text.BadLocationException;

/**
 * Test class according to the JUnit protocol. Tests the action
 * that aligns the indentation of the current line to the character
 * that opened the most recent block or expression list that contains
 * the beginning of the current line. Optional additional whitespaces 
 * can be passed through the constructor.
 * @version $Id: ActionBracePlusTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class ActionBracePlusTest extends IndentRulesTestCase {
  private String _text, _aligned;
  
  private IndentRuleAction _action;
  
  public void testNoSuffix() throws BadLocationException {
    _action = new ActionBracePlus(0);
    
    // (1) 
    
    _text = 
      "method(\n" + 
      ")\n";

    _aligned = 
      "method(\n" + 
      "      )\n";
 
    _setDocText(_text);
    _action.testIndentLine(_doc, 0, Indenter.IndentReason.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.testIndentLine(_doc, 7, Indenter.IndentReason.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.testIndentLine(_doc, 8, Indenter.IndentReason.OTHER); // Aligns second line.
    assertEquals("Line aligned to open paren.", _aligned.length(), _doc.getLength());
    assertEquals("Line aligned to open paren.", _aligned, _doc.getText());
  }
  

  public void testSpaceSuffix() throws BadLocationException
  {
    _action = new ActionBracePlus(1);
    
    // (2) 
    
    _text = 
     "var = method(arg1,\n" + 
     "  arg2, arg3) + 4;";

    _aligned = 
     "var = method(arg1,\n" + 
     "             arg2, arg3) + 4;";
 
    _setDocText(_text);
    _action.testIndentLine(_doc, 0, Indenter.IndentReason.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.testIndentLine(_doc, 18, Indenter.IndentReason.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.testIndentLine(_doc, 20, Indenter.IndentReason.OTHER); // Aligns second line.
    assertEquals("Line aligned to open paren.", _aligned.length(), _doc.getLength());
    assertEquals("Line aligned to open paren.", _aligned, _doc.getText());
    
    // (3) 
     
    _text =
     "boolean method(\n" + 
     "int[] a, String b)\n" + 
     "{}";
    _aligned = 
     "boolean method(\n" + 
     "               int[] a, String b)\n" + 
     "{}";

    _setDocText(_text);
    _action.testIndentLine(_doc, 0, Indenter.IndentReason.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.testIndentLine(_doc, 15, Indenter.IndentReason.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.testIndentLine(_doc, 16, Indenter.IndentReason.OTHER); // Aligns second line.
    assertEquals("Line aligned to open paren.", _aligned.length(), _doc.getLength());
    assertEquals("Line aligned to open paren.", _aligned, _doc.getText());
 
    // (4) 
 
    _text =
     "boolean method(\n" + 
     "int[] a,\n" + 
     "               String b)\n" + 
     "{}";
    _aligned = 
     "boolean method(\n" + 
     "               int[] a,\n" + 
     "               String b)\n" + 
     "{}";

    _setDocText(_text);
    _action.testIndentLine(_doc, 0, Indenter.IndentReason.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.testIndentLine(_doc, 15, Indenter.IndentReason.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.testIndentLine(_doc, 20, Indenter.IndentReason.OTHER); // Aligns second line.
    assertEquals("Line aligned to open paren.", _aligned, _doc.getText());
 
    // (5) 

    _text =
     "array[\n" + 
     "              new Listener() {\n" + 
     "           method() {\n" + 
     "           }\n" + 
     "      }]";
    _aligned =
     "array[\n" + 
     "      new Listener() {\n" + 
     "           method() {\n" + 
     "           }\n" + 
     "      }]";

    _setDocText(_text);
    _action.testIndentLine(_doc, 0, Indenter.IndentReason.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.testIndentLine(_doc, 6, Indenter.IndentReason.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.testIndentLine(_doc, 10, Indenter.IndentReason.OTHER); // Aligns second line.
    assertEquals("Line aligned to open bracket.", _aligned, _doc.getText()); 

  }
  
  public void testLargeSuffix() throws BadLocationException
  {
    _action = new ActionBracePlus(3);
    
    // (6) 
    
    _text = 
     "var = method(foo.\n" + 
     "  bar(), arg3) + 4;";

    _aligned = 
     "var = method(foo.\n" + 
     "               bar(), arg3) + 4;";
 
    _setDocText(_text);
    _action.testIndentLine(_doc, 0, Indenter.IndentReason.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.testIndentLine(_doc, 17, Indenter.IndentReason.OTHER); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    _action.testIndentLine(_doc, 25, Indenter.IndentReason.OTHER); // Aligns second line.
    assertEquals("Line aligned to open paren.", _aligned.length(), _doc.getLength());
    assertEquals("Line aligned to open paren.", _aligned, _doc.getText());
  }
  
  public void testComment() throws BadLocationException
  {
    _action = new ActionBracePlus(3);
    
    // (7) 
    
    _text = 
      "foo(i,\n" + 
      "    j.\n" +
      "bar().\n" +
      "// bar();\n" +
      "baz(),\n" +
      "    k);";

    _aligned = 
      "foo(i,\n" + 
      "    j.\n" +
      "      bar().\n" +
      "      // bar();\n" +
      "      baz(),\n" +
      "    k);";
 
    _setDocText(_text);
    _action.testIndentLine(_doc, 14, Indenter.IndentReason.OTHER); // line 3
    _action.testIndentLine(_doc, 27, Indenter.IndentReason.OTHER); // line 4
    _action.testIndentLine(_doc, 43, Indenter.IndentReason.OTHER); // line 5
    assertEquals("Lines aligned plus one level.",
                 _aligned, _doc.getText());
    
    _action.testIndentLine(_doc, 54, Indenter.IndentReason.OTHER); // after baz()
    assertEquals("Cursor after baz().", _aligned, _doc.getText());
  }
}
