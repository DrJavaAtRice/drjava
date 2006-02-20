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
  public Document createNewDocument() { return  _createDefaultTypedDocument(); }

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




