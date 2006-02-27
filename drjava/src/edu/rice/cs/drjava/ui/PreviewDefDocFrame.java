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
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import javax.swing.text.*;
import java.awt.print.*;
import edu.rice.cs.drjava.model.*;

/**
 * DrJava's print preview window for a definitions document
 * @version $Id$
 */
public class PreviewDefDocFrame extends PreviewFrame {

  private OpenDefinitionsDocument _document;

  /** Contructs a new PreviewDefDocFrame using a parent model and a MainFrame object. */
  public PreviewDefDocFrame(SingleDisplayModel model, MainFrame mainFrame) throws IllegalStateException {
    super(model, mainFrame, false);
  }
  
  /** Sets up the document to be displayed and returns the Pageable object that allows display by pages
   * 
   *  @param model the current display model
   *  @param notUsed not used for this kind of PreviewFrame (only applies to printing the console or interaction)
   *  
   *  @return a Pageable object that allows the document to be displayed by pages
   */
  protected Pageable setUpDocument(SingleDisplayModel model, boolean notUsed) {
    _document = model.getActiveDocument();
    return  _document.getPageable();
  }


  protected void _print() {
    try {
      _document.print();
    }
    catch (FileMovedException fme) {
      _mainFrame._showFileMovedError(fme);
    }
    catch (PrinterException e) {
      _showError(e, "Print Error", "An error occured while printing.");
    }
    catch (BadLocationException e) {
      _showError(e, "Print Error", "An error occured while printing.");
    }
  }
}