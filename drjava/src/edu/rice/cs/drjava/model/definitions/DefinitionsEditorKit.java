/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions;

import javax.swing.text.*;
import java.awt.*;
import javax.swing.event.DocumentEvent;

import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.drjava.model.EventNotifier;

/**
 * This is an editor kit for editing Java source files.
 * It functions as the controller in the MVC arrangement.
 * It implements a factory for new documents, and it also
 * has a factory for Views (the things that render the document).
 * @version $Id$
 */
public class DefinitionsEditorKit extends DefaultEditorKit {
  
  private EventNotifier _notifier;
  
  /**
   * Creates a new editor kit with the given listeners.
   * @param notifier Keeps track of the listeners to the model
   */
  public DefinitionsEditorKit(EventNotifier notifier) {
    _notifier = notifier;
  }
  
  
  private static ViewFactory _factory = new ViewFactory() {
    public View create(Element elem) {
      // The following line is for performance analysis only!
      // return new WrappedPlainView(elem, true);
      return new ColoringView(elem);
    }
  };
  
  /**
   * Creates a new DefinitionsDocument.
   * This used to be named createDefaultDocument() so that the view
   * (DefinitionsPane) would create a DefinitionsDocument by default
   * when it was constructed.  However, we already have an existing
   * DefinitionsDocument we want to use when the DefinitionsPane is
   * constructed, so this default one was being created and thrown
   * away (very expensive).  Ideally, we would have the DefinitionsPane
   * use our existing document from the beginning, but the JEditorPane
   * constructor does not take in a Document.  The only possible
   * approach would be to have this EditorKit return the desired 
   * existing document when the JEditorPane requests a new one, but
   * since the EditorKit must be kept as a static field on DefinitionsPane
   * (since we can't set one until after JEditorPane's constructor is
   * finished), there's no clean way to tell the EditorKit which document
   * to return at which time.  (It would require a large synchronization
   * effort each time a DefinitionsPane is constructed.)
   * 
   * As an easier alternative, we just let the DefaultEditorKit return
   * a PlainDocument (much lighter weight), which can then be thrown
   * away when the true DefinitionsDocument is assigned.
   * 
   * Improvements to this approach are welcome...  :)
   */
  public Document createNewDocument() {
    return  _createDefaultTypedDocument();
  }
  
  /**
   * Creates a new DefinitionsDocument.
   * @return a new DefinitionsDocument.
   */
  private DefinitionsDocument _createDefaultTypedDocument() {
    return new DefinitionsDocument(_notifier);
  }
  
  /**
   * Get the MIME content type of the document.
   * @return "text/java"
   */
  public String getContentType() {
    return "text/java";
  }
  
  /**
   * We want to use our ColoringView to render text, so here we return
   * a factory that creates ColoringViews.
   */
  public final ViewFactory getViewFactory() {
    return _factory;
  }
}




