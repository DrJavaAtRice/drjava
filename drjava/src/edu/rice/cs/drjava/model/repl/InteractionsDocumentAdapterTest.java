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

package edu.rice.cs.drjava.model.repl;

import junit.framework.*;

import edu.rice.cs.util.text.DocumentAdapterException;

/**
 * Tests the functionality of the InteractionsDocumentAdapter.
 */
public final class InteractionsDocumentAdapterTest extends TestCase {
  protected InteractionsDocument _doc;
  protected InteractionsDocumentAdapter _adapter;
  protected InteractionsModel _model;
  /**
   * Initialize fields for each test.
   */
  protected void setUp() {
    
    _adapter = new InteractionsDocumentAdapter();
    _model = new TestInteractionsModel(_adapter);
    _doc = _model.getDocument();
  }
  
  /**
   * Tests that the styles list is updated and reset properly
   */
  public void testStylesListContentAndReset() throws DocumentAdapterException {
    // the banner and the prompt are inserted in the styles list when the document is constructed
    assertEquals("StylesList before insert should contain 2 pairs",
                 2, _adapter.getStylesList().size());
                   
    // Insert some text                   
    _doc.insertText(_doc.getDocLength(), "5", InteractionsDocument.NUMBER_RETURN_STYLE);
    
    assertEquals("StylesList before reset should contain 3 pairs", 
                 3, _adapter.getStylesList().size());
    
    assertEquals("The first element of StylesList before reset should be", 
                 "((0, 19), default)", _adapter.getStylesList().get(0).toString());
    assertEquals("The second element of StylesList before reset should be", 
                 "((19, 21), default)", _adapter.getStylesList().get(1).toString());
    assertEquals("The third element of StylesList before reset should be", 
                 "((21, 22), number.return.style)", _adapter.getStylesList().get(2).toString());
    
    // Reset should clear
    _model.setWaitingForFirstInterpreter(false);
    //this adds the "Resetting Interactions" 
    _model.interpreterResetting();
    
    assertEquals("StylesList before reset should contain 4 pairs",
                 4, _adapter.getStylesList().size());
    
    assertEquals("The fourth element of the StylesLIst before reset should be",
                 "((19, 45), error)", _adapter.getStylesList().get(3).toString());
    
    _doc.reset();
    //_doc.insertText(_doc.getDocLength(), "\"yes\"", InteractionsDocument.STRING_RETURN_STYLE);
    assertEquals("StylesList after rest should only contain 2 pairs",
                 2, _adapter.getStylesList().size());
    assertEquals("The first element of StylesList after reset should be", 
                 "((0, 19), default)", _adapter.getStylesList().get(0).toString());
    assertEquals("The second element of StylesList after reset should be", 
                 "((19, 21), default)", _adapter.getStylesList().get(1).toString());
  }
  
  
  
  /**
   * A generic InteractionsModel for testing purposes.
   */
  public static class TestInteractionsModel extends InteractionsModel {
    String toEval = null;
    String addedClass = null;

    /**
     * Constructs a new InteractionsModel.
     */
    public TestInteractionsModel(InteractionsDocumentAdapter adapter) {
      // Adapter, history size, write delay
      super(adapter, 1000, 25);
    }

    protected void _interpret(String toEval) {
      this.toEval = toEval;
    }
    public String getVariableToString(String var) {
      fail("cannot getVariableToString in a test");
      return null;
    }
    public String getVariableClassName(String var) {
      fail("cannot getVariableClassName in a test");
      return null;
    }
    public void addToClassPath(String path) {
      fail("cannot add to classpath in a test");
    }
    protected void _resetInterpreter() {
      fail("cannot reset interpreter in a test");
    }
    protected void _notifyInteractionStarted() {}
    protected void _notifyInteractionEnded() {}
    protected void _notifySyntaxErrorOccurred(int offset, int length) {}
    protected void _notifyInterpreterExited(int status) {}
    protected void _notifyInterpreterResetting() {}
    protected void _notifyInterpreterResetFailed(Throwable t) {}
    protected void _notifyInterpreterReady() {}
    protected void _interpreterResetFailed(Throwable t) {}
    protected void _notifyInteractionIncomplete() {}
  }
}