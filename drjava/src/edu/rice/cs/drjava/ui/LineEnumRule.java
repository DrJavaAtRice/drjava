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

import java.awt.*;
import javax.swing.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;

/**
 * The row header of the DefinitionsPane which displays the line numbers
 * @version $Id: LineEnumRule.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class LineEnumRule extends JComponent {
  /** The magic number for Swing's JTextPane border padding. */
  private static final int BORDER_PADDING = 3;

  /** Width of the rule */
  static int SIZE = 35;

  /** White space between numbers and code*/
  static int WHITE_SPACE = 7;
  
  /** Vertical increment between line numbers */
  private int _increment;

  /** DefinitionsPane that this rule is displayed for */
  protected DefinitionsPane _pane;

  /** font metrics for the DefPane's font */
  protected FontMetrics _fm;
  /** custom font for the line numbers */
  protected Font _newFont;
  /** font metrics for the new font */
  protected FontMetrics _nfm;

  /** Create a new component to display line numbers along the left of
   * the definitions pane.
   * @param p the pane to show line numbers on
   */
  public LineEnumRule(DefinitionsPane p) {
    _pane = p;
    _fm = _pane.getFontMetrics(_pane.getFont());
    _increment = _fm.getHeight();

    _newFont = _getLineNumFont();
    _nfm = getFontMetrics(_newFont);
    // XXX: 3 is the magic number for Swing's JTextPane border padding.
    SIZE = (int) _nfm.getStringBounds("99999", getGraphics()).getWidth() + 3 +10;
  }

  /** Return a new Dimension using our set width, and the height of the definitions pane */
  public Dimension getPreferredSize() {
    return new Dimension(SIZE, (int)_pane.getPreferredSize().getHeight());
  }

  /** Updates the row header's font information.
   * Uses a custom config setting for this purpose.
   */
  public void updateFont() {
    _fm = _pane.getFontMetrics(_pane.getFont());
    _newFont = _getLineNumFont();
      //_pane.getFont().deriveFont( 8f );
    _nfm = getFontMetrics(_newFont);
    // XXX: 3 is the magic number for Swing's JTextPane border padding.
    SIZE = (int) _nfm.getStringBounds("99999", getGraphics()).getWidth() + 3 + WHITE_SPACE;
  }

  /** Paints the line enumeration component.*/
  public void paintComponent(Graphics g) {
    Rectangle drawHere = g.getClipBounds();

    // Set a white background
    Color backg = DrJava.getConfig().getSetting
      (OptionConstants.DEFINITIONS_LINE_NUMBER_BACKGROUND_COLOR);
    g.setColor(backg);
    g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);

    // Do the ruler labels in a small font that's black.
    g.setFont(_newFont);
    Color foreg = DrJava.getConfig().getSetting
      (OptionConstants.DEFINITIONS_LINE_NUMBER_COLOR);
    g.setColor(foreg);

    // Use clipping bounds to calculate first tick and last tick location.
    int start = (drawHere.y / _increment) * _increment;
    int end = (((drawHere.y + drawHere.height) / _increment) + 1) * _increment;


    int baseline = (int) (( _nfm.getAscent() + _fm.getHeight() - _fm.getDescent())/2.0 );

    // ticks and labels
//    final OpenDefinitionsDocument odd = _pane.getOpenDefDocument();
//    final int endOffset = odd.getEndPosition().getOffset()-1;
//    int lastLine = odd.getDefaultRootElement().getElementIndex(endOffset);
//    
//    if (odd.getLineStartPos(endOffset) != odd.getLineEndPos(endOffset)) { ++lastLine; }
    for (int i = start; i < end; i += _increment) {
//      final int lineNo = i/_increment +1;
//      if (lineNo>lastLine) break;
//      String text = Integer.toString(lineNo);
      String text = Integer.toString(i/_increment +1);

      // When we paint, we get a good look at the Graphics hints.
      // Use them to update our estimate of total width.
      SIZE = (int) _nfm.getStringBounds("99999", g).getWidth() + BORDER_PADDING + WHITE_SPACE;
      int offset = SIZE - ((int) (_nfm.getStringBounds(text, g).getWidth() + 3)) - WHITE_SPACE;

      //g.drawLine(SIZE-1, i, SIZE-tickLength-1, i);
      if (text != null) {
        // Add an arbitrary 3 pixels to line up the text properly with the
        // def pane text baseline.
        g.drawString(text, offset, i + baseline + 3);
      }
    }
  }

  /** Get the font for line numbers, making sure that it is vertically smaller
   * than the definitions pane font.
   * @return a valid font for displaying line numbers
   */
  private Font _getLineNumFont() {
    Font lnf = DrJava.getConfig().getSetting(OptionConstants.FONT_LINE_NUMBERS);
    FontMetrics mets = getFontMetrics(lnf);
    Font mainFont = _pane.getFont();

    // Check the height of the line num font against the def pane font.
    if (mets.getHeight() > _fm.getHeight()) {
      // If the line num font has a larger size than the main font, try deriving
      // a new version with the same size.  (This may or may not produce a height
      // smaller than the main font.)
      float newSize;
      if (lnf.getSize() > mainFont.getSize()) {
        newSize = mainFont.getSize2D();
      }
      // Otherwise, just reduce the current size by one and try that.
      else {
        newSize = lnf.getSize2D() - 1f;
      }

      // If that doesn't work, try reducing the size by one until it fits.
      do {
        lnf = lnf.deriveFont(newSize);
        mets = getFontMetrics(lnf);
        newSize -= 1f;
      } while (mets.getHeight() > _fm.getHeight());
    }

    return lnf;
  }
}
