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

/**
 * This is an editor kit for editing Java source files.
 * It functions as the controller in the MVC arrangement.
 * It implements a factory for new documents, and it also
 * has a factory for Views (the things that render the document).
 * @version $Id$
 */
public class DefinitionsEditorKit extends DefaultEditorKit {
  private static ViewFactory _factory = new ViewFactory() {
    public View create(Element elem) {
      // The following line is for performance analysis only!
      // return new WrappedPlainView(elem, true);
      return new ColoringView(elem);
    }
  };
  
  /** Factory method to make this view create correct model objects. */
  public Document createDefaultDocument() {
    return  _createDefaultTypedDocument();
  }
  
  /**
   * Creates a new DefinitionsDocument.
   * @return a new DefinitionsDocument.
   */
  private static DefinitionsDocument _createDefaultTypedDocument() {
    return new DefinitionsDocument();
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




