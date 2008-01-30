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

package edu.rice.cs.drjava.model.definitions;


import javax.swing.text.*;
import edu.rice.cs.drjava.model.GlobalEventNotifier;
/**
 * This is an editor kit for editing Java source files.
 * It functions as the controller in the MVC arrangement.
 * It implements a factory for new documents, and it also
 * has a factory for Views (the things that render the document).
 * @version $Id$
 */
public class DefinitionsEditorKit extends StyledEditorKit {

  private GlobalEventNotifier _notifier;

  /** Creates a new editor kit with the given listeners.
   *  @param notifier Keeps track of the listeners to the model
   */
  public DefinitionsEditorKit(GlobalEventNotifier notifier) { _notifier = notifier; }

  private static ViewFactory _factory = new ViewFactory() {
    public View create(Element elem) {
      // The following line is for performance analysis only!
      // return new WrappedPlainView(elem, true);
      return new ColoringView(elem);
    }
  };

  /** Creates a new DefinitionsDocument. This used to be named createDefaultDocument() so that the view
   *  (DefinitionsPane) would create a DefinitionsDocument by default when it was constructed.  However, 
   *  we already have an existing DefinitionsDocument we want to use when the DefinitionsPane is constructed, 
   *  so this default one was being created and thrown away (very expensive).  Ideally, we would have the
   *  DefinitionsPane use our existing document from the beginning, but the JEditorPane constructor does 
   *  not take in a Document.  The only possible approach would be to have this EditorKit return the desired
   *  existing document when the JEditorPane requests a new one, but since the EditorKit must be kept as a 
   *  static field on DefinitionsPane since we can't set one until after JEditorPane's constructor is
   *  finished), there's no clean way to tell the EditorKit which document to return at which time.  (It 
   *  would require a large synchronization effort each time a DefinitionsPane is constructed.)
   *
   *  As an easier alternative, we just let the DefaultEditorKit return a PlainDocument (much lighter weight),
   *  which can then be thrown away when the true DefinitionsDocument is assigned.
   *
   *  Improvements to this approach are welcome...  :)
   */
  public DefinitionsDocument createNewDocument() { return  _createDefaultTypedDocument(); }

  /** Creates a new DefinitionsDocument.
   *  @return a new DefinitionsDocument.
   */
  private DefinitionsDocument _createDefaultTypedDocument() { return new DefinitionsDocument(_notifier); }

  /** Get the MIME content type of the document.
   *  @return "text/java"
   */
  public String getContentType() { return "text/java"; }

  /** We want to use our ColoringView to render text, so here we return
   *  a factory that creates ColoringViews.
   */
  public final ViewFactory getViewFactory() { return _factory; }
}




