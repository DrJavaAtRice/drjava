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

package edu.rice.cs.drjava.ui;

import java.awt.*;
import javax.swing.*;

/**
 * The row header of the DefinitionsPane which displays the line numbers
 * @version $Id$
 */
public class LineEnumRule extends JComponent {
  
  /** Width of the rule */
  public static int SIZE = 35;
  
  /** Vertical increment between line numbers */
  private int _increment;
  
  /** DefinitionsPane that this rule is displayed for */
  protected DefinitionsPane _pane;
  
  /** font metrics for the DefPane's font */
  protected FontMetrics _fm;
  /** an 8pt font derived from the DefPane's font */
  protected Font _newFont;
  /** font metrics for the new font */
  protected FontMetrics _nfm;
  
  /**
   * Create a new component to display line numbers along the left of
   * the definitions pane.
   * @param p the pane to show line numbers on
   */
  public LineEnumRule(DefinitionsPane p) {
    _pane = p;
    _fm = _pane.getFontMetrics(_pane.getFont());
    _increment = _fm.getHeight();
    _newFont = _pane.getFont().deriveFont( 8f );
    _nfm = getFontMetrics(_newFont);
    SIZE = _nfm.stringWidth("99999");
  }
  
  /**
   * Return a new Dimension using our set width, and the height of the def. pane
   * return Dimension
   */
  public Dimension getPreferredSize() {
    return new Dimension( SIZE, (int)_pane.getPreferredSize().getHeight());
  }
  
  /**
   * Updates the row header's font information, deriving it from the pane
   * once again.
   */
  public void updateFont() {
    _fm = _pane.getFontMetrics(_pane.getFont());
    _newFont = _pane.getFont().deriveFont( 8f );
    _nfm = getFontMetrics(_newFont);
  }
  
  /**
   * Paints the line enumeration component.
   */
  public void paintComponent(Graphics g) {
    Rectangle drawHere = g.getClipBounds();
    
    // Set a white background
    g.setColor(Color.white);
    g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);
    
    // Do the ruler labels in a small font that's black.
    g.setFont(_newFont); 
    g.setColor(Color.black);
    
    // Some vars we need.
    int end = 0;
    int start = 1;
    int tickLength = 0;
    String text = null;
    
    // Use clipping bounds to calculate first tick and last tick location.
    start = (drawHere.y / _increment) * _increment;
    end = (((drawHere.y + drawHere.height) / _increment) + 1) * _increment;
    
    int baseline = (int) (( _nfm.getAscent() + _fm.getHeight() - _fm.getDescent())/2.0 );
    
    // ticks and labels
    for (int i = start; i < end; i += _increment) {
      tickLength = 10;
      text = Integer.toString(i/_increment +1);
      
      int offset = SIZE - (_nfm.stringWidth(text) + 1);
      
      //g.drawLine(SIZE-1, i, SIZE-tickLength-1, i);
      if (text != null)
        g.drawString(text, offset, i + baseline);
    }
  }
}

