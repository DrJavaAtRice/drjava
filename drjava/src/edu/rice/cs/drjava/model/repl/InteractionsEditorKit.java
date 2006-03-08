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

import edu.rice.cs.drjava.model.definitions.ColoringGlyphPainter;

import javax.swing.text.*;
import java.awt.*;


/**
 * This is an editor kit for editing Java source files. It functions as the controller in the MVC arrangement.
 * It implements a factory for new documents, and it also has a factory for Views (the things that render the document).
 * @version $Id$
 */
public class InteractionsEditorKit extends StyledEditorKit {
  
  /**
   * Creates a new editor kit 
   */
  public InteractionsEditorKit() {
  }
  
  
  private static ViewFactory _factory = new ViewFactory() {
    
    public View create(Element elem) {
      String kind = elem.getName();
      
      if (kind != null) {
        if (kind.equals(AbstractDocument.ContentElementName)) {
          return _createColoringView(elem);
        } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
          return new ParagraphView(elem);
        } else if (kind.equals(AbstractDocument.SectionElementName)) {
          return new BoxView(elem, View.Y_AXIS);
        } else if (kind.equals(StyleConstants.ComponentElementName)) {
          return new ComponentView(elem);
        } else if (kind.equals(StyleConstants.IconElementName)) {
          return new IconView(elem);
        }
      }
      
      // default to text display
      return _createColoringView(elem);
    }
    
  };
  
  /** Get the MIME content type of the document.
   *  @return "text/java"
   */
  public String getContentType() { return "text/java"; }
  
  /** We want to use our ColoringView to render text, so here we return a factory that creates ColoringViews. */
  public final ViewFactory getViewFactory() { return _factory; }
  
  public Document createDefaultDocument() {
    return new InteractionsDJDocument();
  }
  
  /**
   * We only need to re-implement the painter for the GlyphView to modify
   * its behavior. The GlyphView delegates its paint method to the painter.
   * It also allows the painter to obtain the document to which the element
   * belongs.
   * @param elem The Element to pass to the GlyphView
   * @return A GlyphView with modified behavior
   */
  private static GlyphView _createColoringView(Element elem) {    
    final GlyphView view = new GlyphView(elem);
    view.setGlyphPainter(new ColoringGlyphPainter(new Runnable() {
      public void run() {
        if ( view.getContainer() != null) view.getContainer().repaint();
      }
    }));
    return view;
  }
}


