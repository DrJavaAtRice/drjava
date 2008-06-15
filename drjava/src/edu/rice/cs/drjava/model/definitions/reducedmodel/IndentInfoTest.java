/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.DJDocument;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.definitions.indent.IndentRulesTestCase;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
//import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

/** Test class for the IndentInfo class.
  * @version $Id$
  */
public final class IndentInfoTest extends DrJavaTestCase {
  private String _text;
//  private DefinitionsDocument _document;
//  private BraceReduction _reduced;
//  private IndentInfo _info;
//  private GlobalEventNotifier _notifier;
  private DJDocument _document;
  
  
  public void setUp() throws Exception {
    super.setUp();
    //_notifier = new GlobalEventNotifier();
    // _document = new DefinitionsDocument(_notifier);
    _document = new AbstractDJDocument(IndentRulesTestCase.TEST_INDENT_LEVEL) {
      protected int startCompoundEdit() {
        //Do nothing
        return 0;
      }
      protected void endCompoundEdit(int key) { /* Do nothing. */ }
      protected void endLastCompoundEdit() { /* Do nothing. */ }
      protected void addUndoRedo(AbstractDocument.DefaultDocumentEvent chng, Runnable undoCommand, Runnable doCommand) {
        /* Do nothing */
      }
      protected void _styleChanged() { /* Do nothing. */ }
      protected Indenter makeNewIndenter(int indentLevel) { return new Indenter(indentLevel); }
    };
  }
  
  private void _infoTestHelper(int location, String msg,
                               int expDistToStart, int expDistToLineEnclosingBrace,
                               int expDistToLineEnclosingBraceStart, int expDistToEnclosingBrace,
                               int expDistToEnclosingBraceStart)
  {
    _document.setCurrentLocation(location);
    //_reduced = _document.getReduced();
    IndentInfo info = _document.getIndentInformation();
    
    assertEquals(msg + ": distToStart", expDistToStart, info.distToStart());
    assertEquals(msg + ": distToLineEnclosingBrace", expDistToLineEnclosingBrace, info.distToLineEnclosingBrace());
    assertEquals(msg + ": distToLineEnclosingBraceStart", expDistToLineEnclosingBraceStart, 
                 info.distToLineEnclosingBraceStart());
    assertEquals(msg + ": distToBraceCurrent", expDistToEnclosingBrace, info.distToEnclosingBrace());
    assertEquals(msg + ": distToEnclosingBraceStart", expDistToEnclosingBraceStart, info.distToEnclosingBraceStart());
  }
  
  public void testFieldsForCurrentLocation() throws BadLocationException {
    
    _text = "foo {\nvoid m1(int a,\nint b) {\n}\n}";
    //       .   . ..   .  ..     . .    . . ... .
    //       |          |         |           |
    //       0          10        20          30
    
    _document.clear();
    _document.insertString(0, _text, null);
    
    _infoTestHelper(0, "0 -- no brace or newline",     -1, -1, -1, -1, -1);
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
