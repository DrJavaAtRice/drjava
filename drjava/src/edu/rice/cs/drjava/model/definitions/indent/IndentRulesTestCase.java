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
import javax.swing.text.*;

import edu.rice.cs.drjava.model.AbstractDJDocument;
//import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

/**
 * Superclass for all test classes for the indentation decision tree.
 * @version $Id$
 */
public abstract class IndentRulesTestCase extends TestCase {

  protected AbstractDJDocument _doc;
//  private String _indent;
 // private GlobalEventNotifier _notifier;

  /** Sets up the test environment. */
  public void setUp() {
    //_notifier = new GlobalEventNotifier();
    //_doc = new DefinitionsDocument(_notifier);
    _doc = new AbstractDJDocument() {
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
  
  public void tearDown() {
    _doc = null;
    //_notifier = null;
    System.gc();
  }
  
  /** Clears the text of the _doc field and sets it to the given string.
   */
  protected final void _setDocText(String text)
    throws BadLocationException {
    _doc.clear();
    _doc.insertString(0, text, null);
  }
  
  /**
   * Sets the number of spaces to include in the indent string.
   *
  protected final void _setIndentSize(int size) {
    _indent = "";
    for (int i=0; i < size; i++) {
      _indent = _indent + " ";
    }
  }*/
  
  /**
   * Gets the length of the indent string.
   * @return Number of spaces in the indent string.
   *
  protected final int _getIndentSize() {
    return _indent.length();
  }*/
  
  /**
   * Get a string containing the specified number of indents.
   * @param numLevels Number of indent strings to return
   *
  protected String _getIndentString(int numLevels) {
    String indent = "";
    for (int i=0; i < numLevels; i++) {
      indent += _indent;
    }
    return indent;
  }*/

  /**
   * Inserts an indent of the specificed number of levels at the given
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
  
  /**
   * Asserts that the document contains the expected text.
   * @param expected what text of document should be
   */
  protected void _assertContents(String expected) throws BadLocationException {
    assertEquals("document contents", 
                 expected, 
                 _doc.getText());
  }

}
