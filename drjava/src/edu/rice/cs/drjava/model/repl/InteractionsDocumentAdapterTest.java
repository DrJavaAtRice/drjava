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

import edu.rice.cs.drjava.ui.*;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.repl.InteractionsDocumentTest.TestBeep;

import edu.rice.cs.util.text.DocumentAdapterException;

/**
 * Tests the functionality of the InteractionsDocumentAdapter.
 */
public final class InteractionsDocumentAdapterTest extends TestCase {


  protected InteractionsDocumentAdapter _adapter;
  protected InteractionsModel _model;
  protected InteractionsDocument _doc;
  protected InteractionsPane _pane;
  protected MainFrame mf;
  /**
   * Initialize fields for each test.
   */
  protected void setUp() {
    mf = new MainFrame();
    GlobalModel gm = mf.getModel();
    _model = gm.getInteractionsModel();
    _adapter = gm.getSwingInteractionsDocument();
    _doc = gm.getInteractionsDocument();

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
                 "((21, 22), number.return.style)", _adapter.getStylesList().get(0).toString());
    assertEquals("The second element of StylesList before reset should be",
                 "((19, 21), default)", _adapter.getStylesList().get(1).toString());
    assertEquals("The third element of StylesList before reset should be",
                 "((0, 19), object.return.style)", _adapter.getStylesList().get(2).toString());

    synchronized(_model){
      // Reset should clear
      _model.setWaitingForFirstInterpreter(false);
      //this adds the "Resetting Interactions"
      _model.resetInterpreter();
      _model.interpreterResetting();
 
      int returnNum = System.getProperty("line.separator").length();
      assertEquals("StylesList after reset should contain 1 pair",1, _adapter.getStylesList().size());
      //Resetting Interactions piece
      assertEquals("The only element of the StylesList after reset should be",
                   "(("+(47+returnNum)+", "+(72+returnNum*2)+"), error)", _adapter.getStylesList().get(0).toString());
    }
  }

  /**
   * Tests that a null style is not added to the list. Fix for bug #995719
   */
  public void testCannotAddNullStyleToList() throws DocumentAdapterException {
    // the banner and the prompt are inserted in the styles list when the document is constructed
    assertEquals("StylesList before insert should contain 2 pairs",
                 2, _adapter.getStylesList().size());

    // Insert some text
    _doc.insertText(_doc.getDocLength(), "5", InteractionsDocument.NUMBER_RETURN_STYLE);

    assertEquals("StylesList should contain 3 pairs",
                 3, _adapter.getStylesList().size());

    // Insert some text with a null style
    _doc.insertText(_doc.getDocLength(), "6", null);

    assertEquals("StylesList should still contain 3 pairs - null string should not have been inserted",
                 3, _adapter.getStylesList().size());
  }
}