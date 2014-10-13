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

package edu.rice.cs.drjava.model.definitions;

import javax.swing.text.*;
import java.awt.*;
import javax.swing.event.DocumentEvent;
import java.util.ArrayList;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.repl.InteractionsDJDocument;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.EditDocumentInterface;

/** This view class renders text on the screen using the reduced model info.  By extending WrappedPlainView, we only
  * have to override the parts we want to. Here we only override drawUnselectedText. We may want to override
  * drawSelectedText at some point. As of 2002/06/17, we now extend PlainView because WrappedPlainView was causing 
  * bugs related to resizing the viewport of the definitions scroll pane.
  *
  * @version $Id: ColoringView.java 5711 2012-09-11 19:42:33Z rcartwright $
  */
public class ColoringView extends PlainView implements OptionConstants {
  
  public static Color COMMENTED_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_COMMENT_COLOR);
  public static Color DOUBLE_QUOTED_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_DOUBLE_QUOTED_COLOR);
  public static Color SINGLE_QUOTED_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_SINGLE_QUOTED_COLOR);
  public static Color NORMAL_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR);
  public static Color KEYWORD_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_KEYWORD_COLOR);
  public static Color NUMBER_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_NUMBER_COLOR);
  public static Color TYPE_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_TYPE_COLOR);
  public static Font MAIN_FONT = DrJava.getConfig().getSetting(FONT_MAIN);
  
  //Interactions only colors
  public static Color INTERACTIONS_SYSTEM_ERR_COLOR = DrJava.getConfig().getSetting(SYSTEM_ERR_COLOR);
  public static Color INTERACTIONS_SYSTEM_IN_COLOR = DrJava.getConfig().getSetting(SYSTEM_IN_COLOR);
  public static Color INTERACTIONS_SYSTEM_OUT_COLOR = DrJava.getConfig().getSetting(SYSTEM_OUT_COLOR);
  //Renamed as to avoid confusion with the one in option constants
  public static Color ERROR_COLOR = DrJava.getConfig().getSetting(INTERACTIONS_ERROR_COLOR);
  public static Color DEBUGGER_COLOR = DrJava.getConfig().getSetting(DEBUG_MESSAGE_COLOR);
  
  /** Constructs a new coloring view.
    * @param elem the element
    */
  public ColoringView(Element elem) {
    super(elem);
    
    // Listen for updates to configurable colors
    final ColorOptionListener col = new ColorOptionListener();
    final FontOptionListener fol = new FontOptionListener();
    
    Document doc = getDocument();
    if (doc instanceof AbstractDJDocument) {
      // delete the old color listeners, because they're hanging onto the wrong coloringview
      // add color listeners to highlight keywords etc
      DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_COMMENT_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_DOUBLE_QUOTED_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_SINGLE_QUOTED_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_NORMAL_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_KEYWORD_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_NUMBER_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_TYPE_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.FONT_MAIN, fol);
      
      DrJava.getConfig().addOptionListener( OptionConstants.SYSTEM_ERR_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.SYSTEM_IN_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.SYSTEM_OUT_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.INTERACTIONS_ERROR_COLOR, col);
      DrJava.getConfig().addOptionListener( OptionConstants.DEBUG_MESSAGE_COLOR, col);
      
    }
    
    if (doc instanceof DefinitionsDocument) {
      // remove the listeners when the document closes
      ((DefinitionsDocument)doc).addDocumentClosedListener(new DocumentClosedListener() {
        public void close() {
          DrJava.getConfig().removeOptionListener( OptionConstants.DEFINITIONS_COMMENT_COLOR, col);
          DrJava.getConfig().removeOptionListener( OptionConstants.DEFINITIONS_DOUBLE_QUOTED_COLOR, col);
          DrJava.getConfig().removeOptionListener( OptionConstants.DEFINITIONS_SINGLE_QUOTED_COLOR, col);
          DrJava.getConfig().removeOptionListener( OptionConstants.DEFINITIONS_NORMAL_COLOR, col);
          DrJava.getConfig().removeOptionListener( OptionConstants.DEFINITIONS_KEYWORD_COLOR, col);
          DrJava.getConfig().removeOptionListener( OptionConstants.DEFINITIONS_NUMBER_COLOR, col);
          DrJava.getConfig().removeOptionListener( OptionConstants.DEFINITIONS_TYPE_COLOR, col);
          DrJava.getConfig().removeOptionListener( OptionConstants.FONT_MAIN, fol);
          DrJava.getConfig().removeOptionListener( OptionConstants.SYSTEM_ERR_COLOR, col);
          DrJava.getConfig().removeOptionListener( OptionConstants.SYSTEM_IN_COLOR, col);
          DrJava.getConfig().removeOptionListener( OptionConstants.SYSTEM_OUT_COLOR, col);
          DrJava.getConfig().removeOptionListener( OptionConstants.INTERACTIONS_ERROR_COLOR, col);
          DrJava.getConfig().removeOptionListener( OptionConstants.DEBUG_MESSAGE_COLOR, col); 
        }
      });
    }
  }
  
  /** Renders the given range in the model as normal unselected text. Note that this text is all on one line.
    * The superclass deals with breaking lines and such. So all we have to do here is draw the text on [p0,p1) in the
    * model. We have to start drawing at (x,y), and the function returns the x coordinate when we're done.
    * @param g  The graphics context
    * @param x  The starting X coordinate
    * @param y  The starting Y coordinate
    * @param start  The beginning position in the model
    * @param end  The ending position in the model
    * @return  The x coordinate at the end of the range
    * @throws BadLocationException  If the range is invalid
    */
  protected int drawUnselectedText(Graphics g, int x, int y, int start, int end) throws BadLocationException {
    // If there's nothing to show, don't do anything!
    // For some reason I don't understand we tend to get called sometimes to render a zero-length area.
    if (start == end) return x;
    
    // doc might be a PlainDocument (when AbstractDJPane is first constructed).
    // See comments for DefinitionsEditorKit.createNewDocument() for details.
    Document doc = getDocument();
    if (! (doc instanceof AbstractDJDocument)) return x; // return if there is no AbstracDJDocument
    
    final AbstractDJDocument _doc = (AbstractDJDocument) doc;
    
    ArrayList<HighlightStatus> stats = _doc.getHighlightStatus(start, end);
    if (stats.size() < 1) throw new UnexpectedException("GetHighlightStatus returned nothing!");
    
    for (HighlightStatus stat: stats) {
      int location = stat.getLocation();
      int length = stat.getLength();
      
      // If this highlight status extends past p1, end at p1
      if (location + length > end) length = end - stat.getLocation();
      
      if (! (_doc instanceof InteractionsDJDocument) || ! ((InteractionsDJDocument)_doc).setColoring((start + end)/2, g))      
        setFormattingForState(g, stat.getState());
      Segment text = getLineBuffer(); 
      _doc.getText(location, length, text);
      x = Utilities.drawTabbedText(text, x, y, g, this, location);  // updates x on each iteration
    }
    return  x;
  }
  
  /** Draws the selected text image at the specified location.
    * @param g  The text image
    * @param x  The x coordinate for the drawn text
    * @param y  The y coordinate for the drawn text
    * @param start  The beginning position in the model
    * @param end  The end position in the model
    * @return  The location of the end of the image (range)
    * @throws BadLocationException
    */
  protected int drawSelectedText(Graphics g, int x, int y, int start, int end) throws BadLocationException {

//    DrJava.consoleErr().
//      println("drawSelected: " + p0 + "-" + p1 + " doclen=" + _doc.getLength() + " x=" + x + " y=" + y);

    EditDocumentInterface doc = (EditDocumentInterface) getDocument();
    if (doc instanceof InteractionsDJDocument) ((InteractionsDJDocument)doc).setBoldFonts(end, g);
    
    return  super.drawSelectedText(g, x, y, start, end);
  }
  
  /** Given a particular state, assign it a color.
    * @param g Graphics object
    * @param state a given state
    */
  private void setFormattingForState(Graphics g, int state) {
    switch (state) {
      case HighlightStatus.NORMAL:
        g.setColor(NORMAL_COLOR);
        break;
      case HighlightStatus.COMMENTED:
        g.setColor(COMMENTED_COLOR);
        break;
      case HighlightStatus.SINGLE_QUOTED:
        /* In Scala, SINGLE_QUOTE escapes have different termination criteria */
        g.setColor(/* SINGLE_QUOTED_COLOR */ NORMAL_COLOR);
        break;
      case HighlightStatus.DOUBLE_QUOTED:
        g.setColor(DOUBLE_QUOTED_COLOR);
        break;
      case HighlightStatus.KEYWORD:
        g.setColor(KEYWORD_COLOR);
        break;
      case HighlightStatus.NUMBER:
        g.setColor(NUMBER_COLOR);
        break;
      case HighlightStatus.TYPE:
        g.setColor(TYPE_COLOR);
        break;
      default:
        throw  new RuntimeException("Can't get color for invalid state: " + state);
    }
    g.setFont(MAIN_FONT);
  }
  
  
  /** Repaints the container associated with this view, if such container exists. */
  private void repaintContainer() {
    Container c = getContainer();
    if (c != null) c.repaint();
  }
    
  /** Called when a change occurs.
    * @param changes document changes
    * @param a a Shape
    * @param f a ViewFactory
    */
  public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
    super.changedUpdate(changes, a, f);
    // Make sure we redraw since something changed in the formatting
    repaintContainer();
  }
  
  /** Called when an OptionListener perceives a change in any of the colors */
  public void updateColors() {
    
    COMMENTED_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_COMMENT_COLOR);
    DOUBLE_QUOTED_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_DOUBLE_QUOTED_COLOR);
    SINGLE_QUOTED_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_SINGLE_QUOTED_COLOR);
    NORMAL_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR);
    KEYWORD_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_KEYWORD_COLOR);
    NUMBER_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_NUMBER_COLOR);
    TYPE_COLOR = DrJava.getConfig().getSetting(DEFINITIONS_TYPE_COLOR);
    
    INTERACTIONS_SYSTEM_ERR_COLOR = DrJava.getConfig().getSetting(SYSTEM_ERR_COLOR);
    INTERACTIONS_SYSTEM_IN_COLOR = DrJava.getConfig().getSetting(SYSTEM_IN_COLOR);
    INTERACTIONS_SYSTEM_OUT_COLOR = DrJava.getConfig().getSetting(SYSTEM_OUT_COLOR);
    ERROR_COLOR = DrJava.getConfig().getSetting(INTERACTIONS_ERROR_COLOR);
    DEBUGGER_COLOR = DrJava.getConfig().getSetting(DEBUG_MESSAGE_COLOR);
    
    // Avoid the ColoringView that does not have a container.
    repaintContainer();
  }
  
  /** The OptionListeners for DEFINITIONS COLORs */
  private class ColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) { updateColors(); }
  }
  
  private static class FontOptionListener implements OptionListener<Font> {
    public void optionChanged(OptionEvent<Font> oce) {
      MAIN_FONT = DrJava.getConfig().getSetting(FONT_MAIN);
    }
  }
}