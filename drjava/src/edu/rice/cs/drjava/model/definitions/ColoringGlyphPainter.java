/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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
import java.awt.*;
// TODO: Check synchronization.
import java.util.ArrayList;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.repl.InteractionsDJDocument;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

// import edu.rice.cs.util.swing.Utilities;  // conflicts with javax.swing.text.Utilities


public class ColoringGlyphPainter extends GlyphView.GlyphPainter implements OptionConstants {
  
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
  
  private boolean _listenersAttached;
  private Runnable _lambdaRepaint;
  private FontMetrics _metrics;
  
  public ColoringGlyphPainter(Runnable lambdaRepaint) {
    _listenersAttached = false;
    _lambdaRepaint = lambdaRepaint;
    // _metrics is initialized by sync(), which thus must be called before any use of _metrics
  }
  
  /** Paints the glyphs representing the given range. Only runs in the event thread. */
  public void paint(GlyphView v, Graphics g, Shape a, int start, int end) {
    
    // If there's nothing to show, don't do anything!
    // For some reason I don't understand we tend to get called sometimes to render a zero-length area.
    if (start == end) return;
    
    sync(v);
    
    // Might be a PlainDocument (when AbstractDJPane is first constructed).
    //   See comments for DefinitionsEditorKit.createNewDocument() for details.
    Document doc = v.getDocument();
    AbstractDJDocument djdoc = null;
    if (doc instanceof AbstractDJDocument)
      djdoc = (AbstractDJDocument) doc;
    else
      return; // return if there is no AbstracDJDocument

    Segment text;
    TabExpander expander = v.getTabExpander();
    Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
    
    // determine the x coordinate to render the glyphs
    int x = alloc.x;
    int p = v.getStartOffset();
    if (p != start) {
      text = v.getText(p, start);
      int width = Utilities.getTabbedTextWidth(text, _metrics, x, expander, p);
      x += width;
    }
    
    // determine the y coordinate to render the glyphs
    int y = alloc.y + _metrics.getHeight() - _metrics.getDescent();
    
    text = v.getText(start, end);
    
    ArrayList<HighlightStatus> stats = djdoc.getHighlightStatus(start, end);
    if (stats.size() < 1) throw  new RuntimeException("GetHighlightStatus returned nothing!");
    try {
      for (HighlightStatus stat: stats) {
        int length = stat.getLength();
        int location = stat.getLocation();
        
        if (location < end && location + length > start) {
          
          // Adjust the length and location to fit within the bounds of the element we're about to render
          if (location < start) {
            length -= (start-location);
            location = start;
          }        
          if (location + length > end) length = end - location;
          
          if (! (djdoc instanceof InteractionsDJDocument) || 
              ! ((InteractionsDJDocument)djdoc).setColoring((start+end)/2,g))      
            setFormattingForState(g, stat.getState());
          
          djdoc.getText(location, length, text);
          x = Utilities.drawTabbedText(text, x, y, g, v.getTabExpander(), location);
        }
      }
    }
    catch(BadLocationException ble) { /* don't continue rendering if such an exception is found */ }
  }
  
  /** Determines the span the glyphs given a start location (for tab expansion).  Only runs in event thread. */
  public float getSpan(GlyphView v, int start, int end, TabExpander e, float x) {
    sync(v);
    Segment text = v.getText(start, end);
    int width = Utilities.getTabbedTextWidth(text, _metrics, (int) x, e, start);
    return width;
  }
  
  /** Only runs in event thread. */
  public float getHeight(GlyphView v) {
    sync(v);
    return _metrics.getHeight();
  }
  
  /** Fetches the ascent above the baseline for the glyphs corresponding to the given range in the model.  Only runs
    * in event thread.
    */
  public float getAscent(GlyphView v) {
    sync(v);
    return _metrics.getAscent();
  }
  
  /** Fetches the descent below the baseline for the glyphs corresponding to the given range in the model.  Assumes 
    * ReadLocak is held.
    */
  public float getDescent(GlyphView v) {
    sync(v);
    return _metrics.getDescent();
  }
  
  /** Only runs in event thread. */
  public Shape modelToView(GlyphView v, int pos, Position.Bias bias, Shape a) throws BadLocationException {
    
    sync(v);
    Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
    int start = v.getStartOffset();
    int end = v.getEndOffset();
    TabExpander expander = v.getTabExpander();
    Segment text;
    
    if(pos == end) {
      // The caller of this is left to right and borders a right to
      // left view, return our end location.
      return new Rectangle(alloc.x + alloc.width, alloc.y, 0,
                           _metrics.getHeight());
    }
    if ((pos >= start) && (pos <= end)) {
      // determine range to the left of the position
      text = v.getText(start, pos);
      int width = Utilities.getTabbedTextWidth(text, _metrics, alloc.x, expander, start);
      return new Rectangle(alloc.x + width, alloc.y, 0, _metrics.getHeight());
    }
    throw new BadLocationException("modelToView - can't convert", end);
  }
  
  /** Provides a mapping from the view coordinate space to the logical coordinate space of the model.  Only runs in
    * event thread.
    * @param v the view containing the view coordinates
    * @param x the X coordinate
    * @param y the Y coordinate
    * @param a the allocated region to render into
    * @param biasReturn always returns <code>Position.Bias.Forward</code> as the zero-th element of this array
    * @return the location within the model that best represents the given point in the view
    * @see View#viewToModel
    */
  public int viewToModel(GlyphView v, float x, float y, Shape a, Position.Bias[] biasReturn) {
    sync(v);
    Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
    int start = v.getStartOffset();
    int end = v.getEndOffset();
    TabExpander expander = v.getTabExpander();
    Segment text = v.getText(start, end);
    
    int offs = Utilities.getTabbedTextOffset(text, _metrics, 
                                             alloc.x, (int) x, expander, start);
    int retValue = start + offs;
    if(retValue == end) {
      // No need to return backward bias as GlyphPainter1 is used for
      // ltr text only.
      retValue--;
    }
    biasReturn[0] = Position.Bias.Forward;
    return retValue;
  }

  /** Determines the best location (in the model) to break the given view.  This method attempts to break on a 
    * whitespace location.  If a whitespace location can't be found, the nearest character location is returned.
    * Only runs in event thread.
    *
    * @param v  The view 
    * @param start  The location in the model where the fragment should start its representation >= 0
    * @param x  The graphic location along the axis that the broken view would occupy >= 0; this may be useful for
    *           things like tab calculations
    * @param len  Specifies the distance into the view where a potential break is desired >= 0  
    * @return  The model location desired for a break
    * @see View#breakView
    */
  public int getBoundedPosition(GlyphView v, int start, float x, float len) {
    sync(v);
    TabExpander expander = v.getTabExpander();
    Segment s = v.getText(start, v.getEndOffset());
    int index = Utilities.getTabbedTextOffset(s, _metrics, (int)x, (int)(x+len), expander, start, false);
    int end = start + index;
    return end;
  }
  
  /** Only runs in event thread. */
  void sync(GlyphView v) {
    Font f = v.getFont();
    if ((_metrics == null) || (! f.equals(_metrics.getFont()))) {
      // fetch a new FontMetrics
      Toolkit kit;
      Component c = v.getContainer();
      if (c != null) {
        kit = c.getToolkit();
      } else {
        kit = Toolkit.getDefaultToolkit();
      }
      /* The deprecated method here is necessary to get a handle on a FontMetrics object.  This is required by our 
       * dependence on the javax.swing.text.Utilities class, which does a lot of Java 1.1-style calculation (presumably
       * these methods should be deprecated, too). The deprecated use can't be fixed without an in-depth understanding
       * of fonts, glyphs, and font rendering.  Where _metrics is currently used, the Font methods getLineMetrics, 
       * getStringBounds, getHeight, getAscent, and getDescent will probably be helpful.
       */
      @SuppressWarnings("deprecation") 
      FontMetrics newMetrics = kit.getFontMetrics(f);
      _metrics = newMetrics;
    }
    
    Document doc = v.getDocument();
    if (! _listenersAttached && (doc instanceof AbstractDJDocument)) {
      attachOptionListeners((AbstractDJDocument)doc);
    }
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
        g.setColor(SINGLE_QUOTED_COLOR);
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
  
  
//  /** Called when a change occurs.
//    * @param changes document changes
//    * @param a a Shape
//    * @param f a ViewFactory
//    */
//  public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
//    super.changedUpdate(changes, a, f);
//    // Make sure we redraw since something changed in the formatting
//    Container c = getContainer();
//    if (c != null) c.repaint();
//  }
  
  private void attachOptionListeners(AbstractDJDocument doc) {
    // Listen for updates to configurable colors
    final ColorOptionListener col = new ColorOptionListener();
    final FontOptionListener fol = new FontOptionListener();
    
    // These events can occur in the event thread, and so can't clear the event queue

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
    
    // The listeners that were added in the above lines need to be removed from
    // the config framework when the document corresponding to this painter is
    // kicked out of the DocumentCache. Otherwise, this painter will remain 
    // un-garbage-collected and unused.
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
    _listenersAttached = true;
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
    
    EventQueue.invokeLater(_lambdaRepaint);
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
