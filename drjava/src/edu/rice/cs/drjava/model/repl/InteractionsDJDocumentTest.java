/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.ui.MainFrame;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.util.swing.Utilities;

import java.io.File;

/** Tests the functionality of the InteractionsDJDocument. */
public final class InteractionsDJDocumentTest extends DrJavaTestCase {

  protected InteractionsDJDocument _adapter;
  protected InteractionsModel _model;
  protected InteractionsDocument _doc;
  protected MainFrame mf;
  
  /** Initialize fields for each test. */
  protected void setUp() throws Exception {
    super.setUp();
    // MainFrame creation must run in event thread because event thread is already running
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        mf = new MainFrame();
        GlobalModel gm = mf.getModel();
        _model = gm.getInteractionsModel();
        _adapter = gm.getSwingInteractionsDocument();
        _doc = gm.getInteractionsDocument();
        assert _model._pane != null;  // MainFrame creates an interactions controller which creates the pane.
      }
    });
  }
  
  private boolean _interpreterRestarted = false;
  
  public void test1() {
    try { helpTestStylesListContentAndReset(); }
    catch(Throwable t) { t.printStackTrace(); }
  }
  
  /** Tests that the styles list is updated and reset properly */
  public void helpTestStylesListContentAndReset() throws EditDocumentException, InterruptedException {
//    System.err.println("testStylesList started");
    /* The banner and the prompt are inserted in the styles list when the document is constructed; the corresponding
       offsets are computed in the tests below. 
     */
    
    final Object _restartLock = new Object();
    
    assertEquals("StylesList before insert should contain 2 pairs", 2, _adapter.getStyles().length);
//    System.err.println("Styles:\n" + Arrays.toString(_adapter.getStyles()));
    
    int blen = _model.getStartUpBanner().length();
//    System.err.println("StartUpBanner:\n'" + _model.getStartUpBanner() + "'");
//    System.err.println("length = " +  _model.getStartUpBanner().length());
//    System.err.println("Banner:\n'" + _model.getBanner() + "'");
//    System.err.println("length = " +  _model.getBanner().length());                   
    
    /** Elt1, Elt2 are first two elements pushed on the StylesList stack */
    String styleElt1 = "((0, " + blen + "), object.return.style)";
    String styleElt2 = "((" + blen + ", " + (blen + 2) + "), default)";

    assertEquals("The first element pushed on StylesList before insertion should be", styleElt1,
                 _adapter.getStyles()[1].toString());
    assertEquals("The second element pushed on StylesList before insertion should be", styleElt2,
                 _adapter.getStyles()[0].toString());
    
    // Insert some text
    _doc.append("5", InteractionsDocument.NUMBER_RETURN_STYLE);
    
    /* Third element pushed StylesList stack before reset */
    String styleElt3 = "((" + (blen + 2) + ", " + (blen + 3) + "), number.return.style)";

    assertEquals("StylesList before reset should contain 3 pairs", 3, _adapter.getStyles().length);
    
    assertEquals("The first element pushed on StylesList before reset should be", styleElt1,
                 _adapter.getStyles()[2].toString());
    assertEquals("The second element pushed on StylesList before reset should be", styleElt2,
                 _adapter.getStyles()[1].toString());
    assertEquals("The last element pushed on StylesList before reset should be", styleElt3,
                 _adapter.getStyles()[0].toString());
    
//    System.err.println("Doc text: " + _adapter.getText());
//    System.err.println("The styles list is: " + _adapter.getStylesList());
//    System.err.println("The length of the startUp banner is: " + InteractionsModel.getStartUpBanner().length());
    
    /* Reset interactions and wait until it completes */

//    System.err.println("reset interactions test reached");
    InteractionsListener restartCommand = new DummyInteractionsListener() {
      public void interpreterReady(File wd) {
        synchronized(_restartLock) {
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
    _model.resetInterpreter(f, true);
    
//    System.err.println("Interpreter reset");

    // Wait until interpreter has restarted
    synchronized(_restartLock) { while (! _interpreterRestarted) _restartLock.wait(); }
    _model.removeListener(restartCommand);
    
//    System.err.println("Doc text: " + _adapter.getText());
//    System.err.println("Text length: " + _adapter.getLength());
//    System.err.println("The styles list is: " + _adapter.getStylesList());
   
    Utilities.clearEventQueue();  // assures that pending updates to _pane have been performed
    assertEquals("StylesList after reset should contain 2 pairs", 2, _adapter.getStyles().length);
    
    assertEquals("The first element pushed on StylesList after reset should be", styleElt1,
                 _adapter.getStyles()[1].toString());
    assertEquals("The second element pushed on StylesList after reset should be", styleElt2,
                 _adapter.getStyles()[0].toString());
//    System.err.println("testStylesList complete");
  }

  public void test2() {
    try { helpTestCannotAddNullStyleToList(); }
    catch(Throwable t) { t.printStackTrace(); }
  }
  /** Tests that a null style is not added to the list. Fix for bug #995719. */
  public void helpTestCannotAddNullStyleToList() throws EditDocumentException {
//    System.err.println("testCannotAddNull started");
    // the banner and the prompt are inserted in the styles list when the document is constructed
    assertEquals("StylesList before insert should contain 2 pairs", 2, _adapter.getStyles().length);

    // Insert some text
    _doc.append("5", InteractionsDocument.NUMBER_RETURN_STYLE);
    Utilities.clearEventQueue();

    assertEquals("StylesList should contain 3 pairs", 3, _adapter.getStyles().length);

    // Insert some text with a null style
    _doc.append("6", null);
     Utilities.clearEventQueue();

    assertEquals("StylesList should still contain 3 pairs - null string should not have been inserted",
                 3, _adapter.getStyles().length);
  }
}