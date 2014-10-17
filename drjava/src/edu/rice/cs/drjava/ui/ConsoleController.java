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

import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.util.text.ConsoleDocument;

/** A Swing specific console controller class that includes both a Swing InteractionsDJDocument and a GUI agnostic
  * ConsoleDocument (wrapping the former).
  * @version $Id: ConsoleController.java 5594 2012-06-21 11:23:40Z rcartwright $ */
public class ConsoleController extends AbstractConsoleController /* implements Serializable */ {
  
  /** GUI-neutral formulation of console document.  In this case, is a wrapper around _swingConsoleDocument. */
  protected volatile ConsoleDocument _consoleDoc;

  public ConsoleController(final ConsoleDocument consoleDoc, InteractionsDJDocument swingDoc) {
    super(swingDoc, new InteractionsPane("CONSOLE_KEYMAP", swingDoc) {  // creates an InteractionsPane
      public int getPromptPos() { return consoleDoc.getPromptPos(); }
    });
    _consoleDoc = consoleDoc;
    _pane.setEditable(false);
    _init();
  }

  /** Gets the ConsoleDocument. */
  public ConsoleDocument getConsoleDoc() { return _consoleDoc; }

  protected void _setupModel() {
    _consoleDoc.setBeep(_pane.getBeep());  // Beep support is embedded in the wrapper.
  }
}

