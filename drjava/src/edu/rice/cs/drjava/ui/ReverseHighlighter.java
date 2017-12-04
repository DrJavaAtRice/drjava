/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.ui;

import javax.swing.text.*;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.plaf.*;

/** Implements the Highlighter interfaces.  Implements a simple highlight painter, but stores
  * the highlights in reverse order. That means that the selection (for copying) is always
  * the foremost hightlight, and after that, the highlights are drawn from most recent
  * to oldest.
  * Based on DefaultHighlighter by Timothy Prinzing, version 1.39 12/19/03
  * Unfortunately, as the vector of highlights in DefaultHighlighter was private, there was
  * no efficient way to make use of inheritance.
  */
public class ReverseHighlighter extends DefaultHighlighter {
  
  /** Creates a new ReverseHighlighter object. */
  public ReverseHighlighter() { drawsLayeredHighlights = true; }
  
  // ---- Highlighter methods ----------------------------------------------
  
  /** Renders the highlights.
   *
   * @param g the graphics context
   */
  public void paint(Graphics g) {
    // PENDING(prinz) - should cull ranges not visible
    int len = _highlights.size();
    for (int i = 0; i < len; i++) {
      HighlightInfo info = _highlights.get(i);
      if (! (info instanceof LayeredHighlightInfo)) {
        // Avoid allocing unless we need it.
        Rectangle a = component.getBounds();
        Insets insets = component.getInsets();
        a.x = insets.left;
        a.y = insets.top;
        a.width -= insets.left + insets.right;
        a.height -= insets.top + insets.bottom;
        for (; i < len; i++) {
          info = _highlights.get(i);
          if (! (info instanceof LayeredHighlightInfo)) {
            Highlighter.HighlightPainter p = info.getPainter();
            p.paint(g, info.getStartOffset(), info.getEndOffset(),
                    a, component);
          }
        }
      }
    }
  }
  
  /** Called when the UI is being installed into the interface of a JTextComponent.  Installs the editor, and
    * removes any existing highlights.
    * @param c the editor component
    * @see Highlighter#install
    */
  public void install(JTextComponent c) {
    component = c;
    removeAllHighlights();
  }
  
  /** Called when the UI is being removed from the interface of a JTextComponent.
    * @param c the component
    * @see Highlighter#deinstall
    */
  public void deinstall(JTextComponent c) {
    component = null;
  }
  
  //static edu.rice.cs.util.Log _log = new edu.rice.cs.util.Log("highlighter.txt",true);
  
  /** Adds a highlight to the view.  Returns a tag that can be used to refer to the highlight.
    * @param p0   the start offset of the range to highlight {@literal >=} 0
    * @param p1   the end offset of the range to highlight {@literal >=} p0
    * @param p    the painter to use to actually render the highlight
    * @return     an object that can be used as a tag to refer to the highlight
    * @exception BadLocationException if the specified location is invalid
    */
  public Object addHighlight(int p0, int p1, Highlighter.HighlightPainter p) throws BadLocationException {
    Document doc = component.getDocument();
    HighlightInfo i = (getDrawsLayeredHighlights() &&
                       (p instanceof LayeredHighlighter.LayerPainter)) ?
      new LayeredHighlightInfo() : new HighlightInfo();
    i._painter = p;
    
    i.p0 = doc.createPosition(p0);
    i.p1 = doc.createPosition(p1);
    
    int insertPos = _highlights.size();

    if (p instanceof DrJavaHighlightPainter) {
      while (insertPos > 0) {
        HighlightInfo hli = _highlights.get( insertPos-1 );
        if (hli.getPainter() instanceof DrJavaHighlightPainter)
          --insertPos;
        else break;
      }
    } else if (p instanceof DefaultHighlightPainter) {
      while (insertPos > 0) {
        HighlightInfo hli = _highlights.get( insertPos-1 );
        if (hli.getPainter() instanceof DefaultHighlightPainter)
          --insertPos;
        else break;
      }
    } else if (p instanceof DefaultFrameHighlightPainter) {
      while (insertPos > 0) {
        HighlightInfo hli = _highlights.get( insertPos-1 );
        if (hli.getPainter() instanceof DefaultHighlightPainter || hli.getPainter() instanceof DefaultFrameHighlightPainter)
          --insertPos;
        else break;
      }
    } else {
      insertPos = 0;
    }
    _highlights.add(insertPos, i);
    //_log.log(p.toString() + ", pos: " + insertPos);
    safeDamageRange(p0, p1);
    return i;
  }
  
  /** Removes a highlight from the view.
   *
   * @param tag the reference to the highlight
   */
  public void removeHighlight(Object tag) {
    if (tag instanceof LayeredHighlightInfo) {
      LayeredHighlightInfo lhi = (LayeredHighlightInfo)tag;
      if (lhi.width > 0 && lhi.height > 0) {
        component.repaint(lhi.x, lhi.y, lhi.width, lhi.height);
      }
    }
    else {
      HighlightInfo info = (HighlightInfo) tag;
      safeDamageRange(info.p0, info.p1);
    }
    _highlights.remove(tag);
  }
  
  /** Removes all highlights.
   */
  public void removeAllHighlights() {
    TextUI mapper = component.getUI();
    if (getDrawsLayeredHighlights()) {
      int len = _highlights.size();
      if (len != 0) {
        int minX = 0;
        int minY = 0;
        int maxX = 0;
        int maxY = 0;
        int p0 = -1;
        int p1 = -1;
        for (int i = 0; i < len; i++) {
          HighlightInfo hi = _highlights.get(i);
          if (hi instanceof LayeredHighlightInfo) {
            LayeredHighlightInfo info = (LayeredHighlightInfo)hi;
            minX = Math.min(minX, info.x);
            minY = Math.min(minY, info.y);
            maxX = Math.max(maxX, info.x + info.width);
            maxY = Math.max(maxY, info.y + info.height);
          }
          else {
            if (p0 == -1) {
              p0 = hi.p0.getOffset();
              p1 = hi.p1.getOffset();
            }
            else {
              p0 = Math.min(p0, hi.p0.getOffset());
              p1 = Math.max(p1, hi.p1.getOffset());
            }
          }
        }
        if (minX != maxX && minY != maxY) {
          component.repaint(minX, minY, maxX - minX, maxY - minY);
        }
        if (p0 != -1) {
          try {
            safeDamageRange(p0, p1);
          } catch (BadLocationException e) { }
        }
        _highlights.clear();
      }
    }
    else if (mapper != null) {
      int len = _highlights.size();
      if (len != 0) {
        int p0 = Integer.MAX_VALUE;
        int p1 = 0;
        for (int i = 0; i < len; i++) {
          HighlightInfo info = _highlights.get(i);
          p0 = Math.min(p0, info.p0.getOffset());
          p1 = Math.max(p1, info.p1.getOffset());
        }
        try {
          safeDamageRange(p0, p1);
        } catch (BadLocationException e) { }
        
        _highlights.clear();
      }
    }
  }
  
  /** Changes a highlight.
   *
   * @param tag the highlight tag
   * @param p0 the beginning of the range {@literal >=} 0
   * @param p1 the end of the range {@literal >=} p0
   * @exception BadLocationException if the specified location is invalid
   */
  public void changeHighlight(Object tag, int p0, int p1) throws BadLocationException {
    Document doc = component.getDocument();
    if (tag instanceof LayeredHighlightInfo) {
      LayeredHighlightInfo lhi = (LayeredHighlightInfo)tag;
      if (lhi.width > 0 && lhi.height > 0) {
        component.repaint(lhi.x, lhi.y, lhi.width, lhi.height);
      }
      // Mark the highlights region as invalid, it will reset itself
      // next time asked to paint.
      lhi.width = lhi.height = 0;
      
      lhi.p0 = doc.createPosition(p0);
      lhi.p1 = doc.createPosition(p1);
      safeDamageRange(Math.min(p0, p1), Math.max(p0, p1));
    }
    else {
      HighlightInfo info = (HighlightInfo) tag;
      int oldP0 = info.p0.getOffset();
      int oldP1 = info.p1.getOffset();
      if (p0 == oldP0) safeDamageRange(Math.min(oldP1, p1), Math.max(oldP1, p1));
      else if (p1 == oldP1) safeDamageRange(Math.min(p0, oldP0), Math.max(p0, oldP0));
      else {
        safeDamageRange(oldP0, oldP1);
        safeDamageRange(p0, p1);
      }
      
      info.p0 = doc.createPosition(p0);
      info.p1 = doc.createPosition(p1);
      
      // TODO: figure out what is wrong here.  The preceding lines are dead code.
    }
  }
  
  /** Makes a copy of the highlights.  Does not actually clone each highlight,
   * but only makes references to them.
   *
   * @return the copy
   * @see Highlighter#getHighlights
   */
  public Highlighter.Highlight[] getHighlights() {
    int size = _highlights.size();
    if (size == 0) {
      return noHighlights;
    }
    Highlighter.Highlight[] h = _highlights.toArray(EMTPY_HIGHLIGHTS);
    return h;
  }
  
  private static final Highlight[] EMTPY_HIGHLIGHTS = new Highlighter.Highlight[0];
  
  /** When leaf Views (such as LabelView) are rendering they should
   * call into this method. If a highlight is in the given region it will
   * be drawn immediately.
   *
   * @param g Graphics used to draw
   * @param p0 starting offset of view
   * @param p1 ending offset of view
   * @param viewBounds Bounds of View
   * @param editor JTextComponent
   * @param view View instance being rendered
   */
  public void paintLayeredHighlights(Graphics g, int p0, int p1,
                                     Shape viewBounds,
                                     JTextComponent editor, View view) {
    for (int counter = _highlights.size() - 1; counter >= 0; counter--) {
      Object tag = _highlights.get(counter);
      if (tag instanceof LayeredHighlightInfo) {
        LayeredHighlightInfo lhi = (LayeredHighlightInfo)tag;
        int start = lhi.getStartOffset();
        int end = lhi.getEndOffset();
        if ((p0 < start && p1 > start) ||
            (p0 >= start && p0 < end)) {
          lhi.paintLayeredHighlights(g, p0, p1, viewBounds,
                                     editor, view);
        }
      }
    }
  }
  
  /** Queues damageRange() call into event dispatch thread to be sure that 
   * views are in consistent state. 
   * @param p0 first position
   * @param p1 second position
   */
  private void safeDamageRange(final Position p0, final Position p1) {
    safeDamager.damageRange(p0, p1);
  }
  
  /** Queues damageRange() call into event dispatch thread to be sure that 
   * views are in consistent state. 
   * @param a0 integer representation of first position
   * @param a1 integer representation of second position
   * @throws BadLocationException if attempts to reference an invalid location
   */
  private void safeDamageRange(int a0, int a1) throws BadLocationException {
    Document doc = component.getDocument();
    
    safeDamageRange(doc.createPosition(a0), doc.createPosition(a1));
  }
  
  /** If true, highlights are drawn as the Views draw the text. That is
   * the Views will call into <code>paintLayeredHighlight</code> which
   * will result in a rectangle being drawn before the text is drawn
   * (if the offsets are in a highlighted region that is). For this to
   * work the painter supplied must be an instance of
   * LayeredHighlightPainter.
   */
  public void setDrawsLayeredHighlights(boolean newValue) {
    drawsLayeredHighlights = newValue;
  }
  
  public boolean getDrawsLayeredHighlights() {
    return drawsLayeredHighlights;
  }
  
  // ---- member variables --------------------------------------------
  
  private final static Highlighter.Highlight[] noHighlights =
    new Highlighter.Highlight[0];
  private ArrayList<HighlightInfo> _highlights = new ArrayList<HighlightInfo>();  // Vector<HighlightInfo>
  private JTextComponent component;
  private boolean drawsLayeredHighlights;
  private SafeDamager safeDamager = new SafeDamager();
  
  /** Simple highlight painter that draws a rectangular box around text. */
  public static class DefaultFrameHighlightPainter extends LayeredHighlighter.LayerPainter {
    
    /** Constructs a new highlight painter. If c is null, the JTextComponent will be queried for its selection color.
      * @param c the color for the highlight
      * @param t the thickness in pixels
      */
    public DefaultFrameHighlightPainter(Color c, int t) {
      color = c;
      thickness = t;
    }
    
    /** @return the color of the highlight */
    public Color getColor() { return color; }
    
    /** @return thickness in pixels */
    public int getThickness() { return thickness; }
    
    // --- HighlightPainter methods ---------------------------------------
    
    private void drawRectThick(Graphics g, int x, int y, int width, int height, int thick) {
      if (thick < 2) { g.drawRect(x, y, width, height); }
      else {
        g.fillRect(x, y,              width, thick);
        g.fillRect(x, y+height-thick, width, thick);
        g.fillRect(x, y,              thick, height);
        g.fillRect(x+width-thick, y,  thick, height);
      }
    }
    
    /** Paints a highlight.
      * @param g the graphics context
      * @param offs0 the starting model offset {@literal >=} 0
      * @param offs1 the ending model offset {@literal >=} offs1
      * @param bounds the bounding box for the highlight
      * @param c the editor
      */
    public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
      Rectangle alloc = bounds.getBounds();
      try {
        // --- determine locations ---
        TextUI mapper = c.getUI();
        Rectangle p0 = mapper.modelToView(c, offs0);
        Rectangle p1 = mapper.modelToView(c, offs1);
        
        // --- render ---
        Color color = getColor();
        
        if (color == null)  g.setColor(c.getSelectionColor());
        else  g.setColor(color);

        if (p0.y == p1.y) { // same line, render a rectangle
          Rectangle r = p0.union(p1);
          drawRectThick(g, r.x, r.y, r.width, r.height, thickness);
        } 
        else { // different lines
          int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
          drawRectThick(g, p0.x, p0.y, p0ToMarginWidth, p0.height, thickness);
          if ((p0.y + p0.height) != p1.y)
            drawRectThick(g, alloc.x, p0.y + p0.height, alloc.width, p1.y - (p0.y + p0.height), thickness);
          drawRectThick(g, alloc.x, p1.y, (p1.x - alloc.x), p1.height, thickness);
        }
      } 
      catch (BadLocationException e) { /* can't render */ }
    }
    
    // --- LayerPainter methods ----------------------------
    /** Paints a portion of a highlight.
      * @param g the graphics context
      * @param offs0 the starting model offset {@literal >=} 0
      * @param offs1 the ending model offset {@literal >=} offs1
      * @param bounds the bounding box of the view, which is not necessarily the region to paint.
      * @param c the editor
      * @param view View painting for
      * @return region drawing occured in
      */
    public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
      Color color = getColor();
      
      if (color == null) g.setColor(c.getSelectionColor());
      else g.setColor(color);

      if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) { // Contained in view, can just use bounds.
        Rectangle alloc;
        if (bounds instanceof Rectangle) alloc = (Rectangle)bounds;
        else alloc = bounds.getBounds();

        drawRectThick(g, alloc.x, alloc.y, alloc.width, alloc.height, thickness);
        return alloc;
      }
      else { // Should only render part of View.
        try {
          // --- determine locations ---
          Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1,Position.Bias.Backward, bounds);
          Rectangle r = (shape instanceof Rectangle) ? (Rectangle)shape : shape.getBounds();
          
          drawRectThick(g, r.x, r.y, r.width, r.height, thickness);
          return r;
        } 
        catch (BadLocationException e) { /* can't render */ }
      }
      // Only if exception
      return null;
    }
    
    private Color color;
    private int thickness;
  }
  
  
  /** Simple highlight painter that underlines text. */
  public static class DefaultUnderlineHighlightPainter extends LayeredHighlighter.LayerPainter {
    
    /** Constructs a new highlight painter. If c is null, the JTextComponent will be queried for its selection color.
      * @param c the color for the highlight
      * @param t the thickness in pixels
      */
    public DefaultUnderlineHighlightPainter(Color c, int t) {
      color = c;
      thickness = t;
    }
    
    /** @return the color of the highlight */
    public Color getColor() { return color; }
    
    /** @return thickness in pixels */
    public int getThickness() { return thickness; }
    
    // --- HighlightPainter methods ---------------------------------------
    
    private void drawUnderline(Graphics g, int x, int y, int width, int height, int thick) {
      g.fillRect(x, y+height-thick, width, thick);
    }
    
    /** Paints a highlight.
      * @param g the graphics context
      * @param offs0 the starting model offset {@literal >=} 0
      * @param offs1 the ending model offset {@literal >=} offs1
      * @param bounds the bounding box for the highlight
      * @param c the editor
      */
    public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
      Rectangle alloc = bounds.getBounds();
      try {
        // --- determine locations ---
        TextUI mapper = c.getUI();
        Rectangle p0 = mapper.modelToView(c, offs0);
        Rectangle p1 = mapper.modelToView(c, offs1);
        
        // --- render ---
        Color color = getColor();
        
        if (color == null) g.setColor(c.getSelectionColor());
        else g.setColor(color);

        if (p0.y == p1.y) { // same line, render a rectangle
          Rectangle r = p0.union(p1);
          drawUnderline(g, r.x, r.y, r.width, r.height, thickness);
        } 
        else { // different lines
          int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
          drawUnderline(g, p0.x, p0.y, p0ToMarginWidth, p0.height, thickness);
          if ((p0.y + p0.height) != p1.y)
            drawUnderline(g, alloc.x, p0.y + p0.height, alloc.width, p1.y - (p0.y + p0.height), thickness);

          drawUnderline(g, alloc.x, p1.y, (p1.x - alloc.x), p1.height, thickness);
        }
      } 
      catch (BadLocationException e) { /* can't render */ }
    }
    
    // --- LayerPainter methods ----------------------------
    /** Paints a portion of a highlight.
      * @param g the graphics context
      * @param offs0 the starting model offset {@literal >=} 0
      * @param offs1 the ending model offset {@literal >=} offs1
      * @param bounds the bounding box of the view, which is not necessarily the region to paint.
      * @param c the editor
      * @param view View painting for
      * @return region drawing occured in
      */
    public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
      Color color = getColor();
      
      if (color == null) g.setColor(c.getSelectionColor());
      else g.setColor(color);

      if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) { // Contained in view, can just use bounds
        Rectangle alloc;
        if (bounds instanceof Rectangle) alloc = (Rectangle)bounds;
        else alloc = bounds.getBounds();

        drawUnderline(g, alloc.x, alloc.y, alloc.width, alloc.height, thickness);
        return alloc;
      }
      else { // Should only render part of View.
        try {
          // --- determine locations ---
          Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1,Position.Bias.Backward, bounds);
          Rectangle r = (shape instanceof Rectangle) ? (Rectangle)shape : shape.getBounds();
          drawUnderline(g, r.x, r.y, r.width, r.height, thickness);
          return r;
        } catch (BadLocationException e) { /* can't render */ }
      }
      // Only if exception
      return null;
    }
    
    private Color color;
    private int thickness;
  }
  
  class HighlightInfo implements Highlighter.Highlight {
    
    Position p0;
    Position p1;
    Highlighter.HighlightPainter _painter;
    
    public int getStartOffset() { return p0.getOffset(); }
    
    public int getEndOffset() { return p1.getOffset(); }
    
    public Highlighter.HighlightPainter getPainter() { return _painter; }
    

  }
  
  
  /** This class is a wrapper for the DefaultHighlightPainter that allows us to tell whether a highlight was
    * requested by DrJava or by Swing (as in selected text).
    */
  public static class DrJavaHighlightPainter extends DefaultHighlightPainter {
    
    public DrJavaHighlightPainter(Color c) { super(c); }
  }
  
  
  /** LayeredHighlightPainter is used when a drawsLayeredHighlights is
   * true. It maintains a rectangle of the region to paint.
   */
  class LayeredHighlightInfo extends HighlightInfo {
    
    void union(Shape bounds) {
      if (bounds == null)
        return;
      
      Rectangle alloc;
      if (bounds instanceof Rectangle) alloc = (Rectangle)bounds;
      else alloc = bounds.getBounds();

      if (width == 0 || height == 0) {
        x = alloc.x;
        y = alloc.y;
        width = alloc.width;
        height = alloc.height;
      }
      else {
        width = Math.max(x + width, alloc.x + alloc.width);
        height = Math.max(y + height, alloc.y + alloc.height);
        x = Math.min(x, alloc.x);
        width -= x;
        y = Math.min(y, alloc.y);
        height -= y;
      }
    }
    
    /** Restricts the region based on the receivers offsets and messages the 
     * painter to paint the region.
     * @param g the Graphics objet to use to paint
     * @param p0 lower bound on region
     * @param p1 upper bound on region
     * @param viewBounds bounds on view
     * @param editor a JTextComponent
     * @param view view in which to paint
     */
    void paintLayeredHighlights(Graphics g, int p0, int p1, Shape viewBounds, JTextComponent editor, View view) {
      int start = getStartOffset();
      int end = getEndOffset();
      // Restrict the region to what we represent
      p0 = Math.max(start, p0);
      p1 = Math.min(end, p1);
      // Paint the appropriate region using the painter and union the effected region with our bounds.
      LayeredHighlighter.LayerPainter lp = (LayeredHighlighter.LayerPainter) _painter;
      union(lp.paintLayer(g, p0, p1, viewBounds, editor, view));
    }
    
    int x;
    int y;
    int width;
    int height;
  }
  
  
  /** This class invokes <code>mapper.damageRange</code> in EventDispatchThread. The only one instance per Highlighter
    * is cretaed. When a number of ranges should be damaged it collects them into queue and damages them in consecutive
    * order in <code>run</code> call.
    */
  class SafeDamager implements Runnable {
    private ArrayList<Position> p0 = new ArrayList<Position>(10);
    private ArrayList<Position> p1 = new ArrayList<Position>(10);
    private Document lastDoc = null;
    
    /** Executes range(s) damage and cleans range queue. */
    public synchronized void run() {
      if (component != null) {
        TextUI mapper = component.getUI();
        if (mapper != null && lastDoc == component.getDocument()) { // Doc must match to properly display highlights
          int len = p0.size();
          for (int i = 0; i < len; i++) {
            mapper.damageRange(component, p0.get(i).getOffset(), p1.get(i).getOffset());
          }
        }
      }
      p0.clear();
      p1.clear();
      
      // release reference
      lastDoc = null;
    }
    
    /** Adds range to be damaged to the range queue. If the range queue is 
     * empty (the first call or run() was already invoked) then adds this 
     * class instance into EventDispatch queue. The method also tracks if the 
     * current document changed or component is null. In this case it removes 
     * all ranges added before from range queue.
     * @param pos0 lower bound on range
     * @param pos1 upper bound on range
     */
    public synchronized void damageRange(Position pos0, Position pos1) {
      if (component == null) {
        p0.clear();
        lastDoc = null;
        return;
      }
      
      boolean addToQueue = p0.isEmpty();
      Document curDoc = component.getDocument();
      if (curDoc != lastDoc) {
        if (!p0.isEmpty()) {
          p0.clear();
          p1.clear();
        }
        lastDoc = curDoc;
      }
      p0.add(pos0);
      p1.add(pos1);
      
      if (addToQueue) EventQueue.invokeLater(this);  // Why invokeLater here?  Context switches are costly.
    }
  }
}
