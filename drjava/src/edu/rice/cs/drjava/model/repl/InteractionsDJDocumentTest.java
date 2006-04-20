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

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.DummyGlobalModelListener;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.ui.InteractionsPane;
import edu.rice.cs.drjava.ui.MainFrame;
import edu.rice.cs.util.text.EditDocumentException;

import java.io.File;

/** Tests the functionality of the InteractionsDJDocument. */
public final class InteractionsDJDocumentTest extends DrJavaTestCase {

  protected InteractionsDJDocument _adapter;
  protected InteractionsModel _model;
  protected InteractionsDocument _doc;
  protected InteractionsPane _pane;
  protected MainFrame mf;
  
  /** Initialize fields for each test. */
  protected void setUp() throws Exception {
    super.setUp();
    mf = new MainFrame();
    GlobalModel gm = mf.getModel();
    _model = gm.getInteractionsModel();
    _adapter = gm.getSwingInteractionsDocument();
    _doc = gm.getInteractionsDocument();
  }
  
  private boolean _interpreterRestarted = false;
  
  /** Tests that the styles list is updated and reset properly */
  public void testStylesListContentAndReset() throws EditDocumentException, InterruptedException {
    /* The banner and the prompt are inserted in the styles list when the document is constructed; the corresponding
       offsets are computed in the tests below. 
     */
    
    final Object _restartLock = new Object();
    
    assertEquals("StylesList before insert should contain 2 pairs", 2, _adapter.getStylesList().size());
    
    int blen = _model.getStartUpBanner().length();
    
    /** Elt1, Elt2 are first two elements pushed on the StylesList stack */
    String styleElt1 = "((0, " + blen + "), object.return.style)";
    String styleElt2 = "((" + blen + ", " + (blen + 2) + "), default)";

    assertEquals("The first element pushed on StylesList before insertion should be", styleElt1,
                 _adapter.getStylesList().get(1).toString());
    assertEquals("The second element pushed on StylesList before insertion should be", styleElt2,
                 _adapter.getStylesList().get(0).toString());
    
    // Insert some text
    _doc.insertText(_doc.getLength(), "5", InteractionsDocument.NUMBER_RETURN_STYLE);
    
    /* Third element pushed StylesList stack before reset */
    String styleElt3 = "((" + (blen + 2) + ", " + (blen + 3) + "), number.return.style)";

    assertEquals("StylesList before reset should contain 3 pairs", 3, _adapter.getStylesList().size());
    
    assertEquals("The first element pushed on StylesList before reset should be", styleElt1,
                 _adapter.getStylesList().get(2).toString());
    assertEquals("The second element pushed on StylesList before reset should be", styleElt2,
                 _adapter.getStylesList().get(1).toString());
    assertEquals("The last element pushed on StylesList before reset should be", styleElt3,
                 _adapter.getStylesList().get(0).toString());
    
//    System.err.println("Doc text: " + _adapter.getText());
//    System.err.println("The styles list is: " + _adapter.getStylesList());
//    System.err.println("The length of the startup banner is: " + InteractionsModel.getStartUpBanner().length());
    
    /* Reset interactions and wait until it completes */

    InteractionsListener restartCommand = new DummyGlobalModelListener() {
      public void interpreterReady(File wd) {
        synchronized (_restartLock) {
          _interpreterRestarted = true;
          _restartLock.notify();
        }
      }};
    _model.addListener(restartCommand);
                                   
    // Reset should clear
    _model.setWaitingForFirstInterpreter(false);
    
    synchronized(_restartLock) { _interpreterRestarted = false; }
      
    // Reset the interactions pane, restarting the interpreter
    File f = _model.getWorkingDirectory();
    _model.resetInterpreter(f);  

    //. Wait until interpreter has restarted
    synchronized(_restartLock) { while (! _interpreterRestarted) _restartLock.wait(); }
    _model.removeListener(restartCommand);
    
//    System.err.println("Doc text: " + _adapter.getText());
//    System.err.println("Text length: " + _adapter.getLength());
//    System.err.println("The styles list is: " + _adapter.getStylesList());
   
    assertEquals("StylesList after reset should contain 2 pairs", 2, _adapter.getStylesList().size());
    
    assertEquals("The first element pushed on StylesList after reset should be", styleElt1,
                 _adapter.getStylesList().get(1).toString());
    assertEquals("The second element pushed on StylesList after reset should be", styleElt2,
                 _adapter.getStylesList().get(0).toString());
    
    

  }

  /**
   * Tests that a null style is not added to the list. Fix for bug #995719
   */
  public void testCannotAddNullStyleToList() throws EditDocumentException {
    // the banner and the prompt are inserted in the styles list when the document is constructed
    assertEquals("StylesList before insert should contain 2 pairs",
                 2, _adapter.getStylesList().size());

    // Insert some text
    _doc.insertText(_doc.getLength(), "5", InteractionsDocument.NUMBER_RETURN_STYLE);

    assertEquals("StylesList should contain 3 pairs",
                 3, _adapter.getStylesList().size());

    // Insert some text with a null style
    _doc.insertText(_doc.getLength(), "6", null);

    assertEquals("StylesList should still contain 3 pairs - null string should not have been inserted",
                 3, _adapter.getStylesList().size());
  }
}