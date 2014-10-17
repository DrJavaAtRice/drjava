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

package edu.rice.cs.drjava.ui;

import javax.swing.text.*;
import java.awt.print.*;
import edu.rice.cs.drjava.model.*;

/**
 * DrJava's print preview window for a definitions document
 * @version $Id: PreviewDefDocFrame.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class PreviewDefDocFrame extends PreviewFrame {
  
  private OpenDefinitionsDocument _document;
  
  /** Contructs a new PreviewDefDocFrame using a parent model and a MainFrame object. Should only be called in event 
    * thread. 
    */
  public PreviewDefDocFrame(SingleDisplayModel model, MainFrame mainFrame) throws IllegalStateException {
    super(model, mainFrame, false);
  }
  
  /** Sets up the document to be displayed and returns the Pageable object that allows display by pages
    * @param model the current display model
    * @param notUsed not used for this kind of PreviewFrame (only applies to printing the console or interaction)
    * @return a Pageable object that allows the document to be displayed by pages
    */
  protected Pageable setUpDocument(SingleDisplayModel model, boolean notUsed) {
    _document = model.getActiveDocument();
    return  _document.getPageable();
  }
  
  
  protected void _print() {
    try { _document.print(); }
    catch (FileMovedException fme) { _mainFrame._showFileMovedError(fme); }
    catch (PrinterException e) { _showError(e, "Print Error", "An error occured while printing."); }
    catch (BadLocationException e) { _showError(e, "Print Error", "An error occured while printing."); }
  }
}