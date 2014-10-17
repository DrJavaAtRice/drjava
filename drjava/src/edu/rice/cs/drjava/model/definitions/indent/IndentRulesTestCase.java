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

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.AbstractDJDocument;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
//import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

/** Superclass for all test classes for the indentation decision tree.
  * @version $Id: IndentRulesTestCase.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class IndentRulesTestCase extends DrJavaTestCase {
  
  public static final int TEST_INDENT_LEVEL = 2;

  protected volatile AbstractDJDocument _doc;
//  private String _indent;
 // private GlobalEventNotifier _notifier;

  /** Sets up the test environment. */
  public void setUp() throws Exception {
    super.setUp();
    //_notifier = new GlobalEventNotifier();
    //_doc = new DefinitionsDocument(_notifier);
    _doc = new AbstractDJDocument(TEST_INDENT_LEVEL) {
      protected int startCompoundEdit() { return 0; /* Do nothing. */ }
      protected void endCompoundEdit(int key) { /* Do nothing. */ }
      protected void endLastCompoundEdit() { /* Do nothing. */ }
      protected void addUndoRedo(AbstractDocument.DefaultDocumentEvent chng, Runnable undoCommand, Runnable doCommand) {
        /* Do nothing. */ 
      }
      protected void _styleChanged() { /* Do nothing. */ }
    };
  }
  
  public void tearDown() throws Exception {
    _doc = null;
//    _notifier = null;
//    System.gc();
    super.tearDown();
  }
  
  /** Clears the text of the _doc field and sets it to the given string. */
  protected final void _setDocText(String text) throws BadLocationException {
    setDocText(_doc, text);
  }
  
  /** Returns text of _doc. */
  protected final String _getDocText() { return _doc.getText(); }
  
//    _doc.clear();
//    _doc._insertString(0, text, null);
//    Utilities.clearEventQueue();  // make sure that all listener actions triggered by this document update have completed
//    Utilities.clearEventQueue();
//  }
  
  /** Sets the number of spaces to include in the indent string.
   *
  protected final void _setIndentSize(int size) {
    _indent = "";
    for (int i=0; i < size; i++) {
      _indent = _indent + " ";
    }
  }*/
  
  /** Gets the length of the indent string.
   * @return Number of spaces in the indent string.
   *
  protected final int _getIndentSize() {
    return _indent.length();
  }*/
  
  /** Get a string containing the specified number of indents.
   * @param numLevels Number of indent strings to return
   *
  protected String _getIndentString(int numLevels) {
    String indent = "";
    for (int i=0; i < numLevels; i++) {
      indent += _indent;
    }
    return indent;
  }*/

  /** Inserts an indent of the specificed number of levels at the given
   * index in the string.
   * @param text String to insert indent into
   * @param index Position in string to add indent
   * @param numLevels Number of indents to insert
   *
  protected String _addIndent(String text, int index, int numLevels) {
    String start = text.substring(0, index);
    String end = text.substring(index);
    String indent = _getIndentString(numLevels);
    return start.concat(indent).concat(end);
  }*/
  
  /** Asserts that the document contains the expected text.
    * @param expected what text of document should be
    */
  protected void _assertContents(String expected) throws BadLocationException {
    assertEquals("document contents", expected, _doc.getText());
  }

}
