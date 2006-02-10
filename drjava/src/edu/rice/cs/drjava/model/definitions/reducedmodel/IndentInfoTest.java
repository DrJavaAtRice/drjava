/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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
 END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.DJDocument;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
//import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

/**
 * 
 * @version $Id$
 */
public final class IndentInfoTest extends DrJavaTestCase {
  private String _text;
  //private DefinitionsDocument _document;
  //private BraceReduction _reduced;
  private IndentInfo _info;
  //private GlobalEventNotifier _notifier;
  private DJDocument _document;
  
  
  public void setUp() throws Exception {
    super.setUp();
    //_notifier = new GlobalEventNotifier();
    // _document = new DefinitionsDocument(_notifier);
    _document = new AbstractDJDocument() {
      protected int startCompoundEdit() {
        //Do nothing
        return 0;
      }
      protected void endCompoundEdit(int key) {
        //Do nothing
      }
      protected void endLastCompoundEdit() {
        //Do nothing
      }
      protected void addUndoRedo(AbstractDocument.DefaultDocumentEvent chng, Runnable undoCommand, Runnable doCommand) {
        //Do nothing
      }
      protected void _styleChanged() {
        //Do nothing 
      }
      protected Indenter makeNewIndenter(int indentLevel) {
        return new Indenter(indentLevel);
      }
    };
  }
  
  private void _infoTestHelper(int location, String message,
                               int expDistToPrevNewline, int expDistToBrace,
                               int expDistToNewline, int expDistToBraceCurrent,
                               int expDistToNewlineCurrent)
  {
    _document.setCurrentLocation(location);
    //_reduced = _document.getReduced();
    _info = _document.getIndentInformation();
    
    assertEquals(message + " -- distToPrevNewline", expDistToPrevNewline, _info.distToPrevNewline);
    assertEquals(message + " -- distToBrace", expDistToBrace, _info.distToBrace);
    assertEquals(message + " -- distToNewline", expDistToNewline, _info.distToNewline);
    assertEquals(message + " -- distToBraceCurrent", expDistToBraceCurrent, _info.distToBraceCurrent);
    assertEquals(message + " -- distToNewlineCurrent", expDistToNewlineCurrent, _info.distToNewlineCurrent);
  }
  
  public void testFieldsForCurrentLocation() throws BadLocationException {
    
    _text = "foo {\nvoid m1(int a,\nint b) {\n}\n}";
    //       .   . ..   .  ..     . .    . . ... .
    //       |          |         |           |
    //       0          10        20          30
    
    _document.clear();
    _document.insertString(0, _text, null);
    
    _infoTestHelper(0, "DOCSTART -- no brace or newline",     -1, -1, -1, -1, -1);
    _infoTestHelper(4, "Location has no brace or newline",    -1, -1, -1, -1, -1);
    _infoTestHelper(5, "Location has a brace but no newline", -1, -1, -1,  1, -1);
    _infoTestHelper(6, "Location has a brace and a newline",   0,  2, -1,  2, -1);
    _infoTestHelper(10, "Location has a brace and a newline",  4,  6, -1,  6, -1);
    _infoTestHelper(13, "Location has a brace and a newline",  7,  9, -1,  9, -1);
    _infoTestHelper(14, "Location has a brace and a newline",  8, 10, -1,  1,  8);
    _infoTestHelper(20, "At \\n within parens",               14, 16, -1,  7, 14);
    _infoTestHelper(21, "Second line within parens",           0,  8, 15,  8, 15);
    _infoTestHelper(26, "On close paren",                      5, 13, 20, 13, 20);
    _infoTestHelper(28, "On second open brace",                7, 15, 22, 24, -1);
    _infoTestHelper(29, "On \\n in second set of braces",      8, 16, 23,  1,  8);
    _infoTestHelper(30, "Close brace of method declaration",   0,  2,  9,  2,  9);
    _infoTestHelper(31, "Last \\n",                            1,  3, 10, 27, -1);
    _infoTestHelper(32, "Final close brace",                   0, 28, -1, 28, -1);
  }
}
