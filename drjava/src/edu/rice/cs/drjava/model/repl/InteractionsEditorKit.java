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

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.model.definitions.ColoringGlyphPainter;
import javax.swing.text.*;


/**
 * This is an editor kit for editing Java source files. It functions as the controller in the MVC arrangement.
 * It implements a factory for new documents, and it also has a factory for Views (the things that render the document).
 * @version $Id: InteractionsEditorKit.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class InteractionsEditorKit extends StyledEditorKit {
  
  /** Creates a new editor kit 
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
    * @return "text/java"
    */
  public String getContentType() { return "text/java"; }
  
  /** We want to use our ColoringView to render text, so here we return a factory that creates ColoringViews. */
  public final ViewFactory getViewFactory() { return _factory; }
  
  public InteractionsDJDocument createDefaultDocument() {
    return new InteractionsDJDocument();
  }
  
  /** We only need to re-implement the painter for the GlyphView to modify its behavior. The GlyphView delegates its 
    * paint method to the painter.  It also allows the painter to obtain the document to which the element
    * belongs.
    * @param elem The Element to pass to the GlyphView
    * @return A GlyphView with modified behavior
    */
  private static GlyphView _createColoringView(Element elem) {    
    final GlyphView view = new GlyphView(elem);
    view.setGlyphPainter(new ColoringGlyphPainter(new Runnable() {
      public void run() {
        if (view.getContainer() != null) view.getContainer().repaint();
      }
    }));
    return view;
  }
}


