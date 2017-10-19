/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.swing;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * A tree cell renderer that sets the backgrounds of cells to alternating colors.
 */
public class ShadedTreeCellRenderer implements TreeCellRenderer {
  
  /** An invisible component to return when the provided row number is incorrect. */
  private static final Component DUMMY_CELL = Box.createRigidArea(new Dimension(0, 0));

  private final TreeCellRenderer _renderer;
  private final Color _even;
  private final Color _odd;
  
  /**
   * @param renderer  A renderer to provide the contents of each cell.  For the shading
   *                  to be visible, the returned components must honor their backgroud
   *                  color setting, and must not be obscured by opaque child components.
   * @param even  Color to shade the first (at index 0) row and each subsequent even row.
   * @param odd  Color to shade the second (at index 1) row and each subsequent odd row.
   */
  public ShadedTreeCellRenderer(TreeCellRenderer renderer, Color even, Color odd) {
    _renderer = renderer;
    _even = even;
    _odd = odd;
  }
  
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                boolean expanded, boolean leaf, int row,
                                                boolean hasFocus) {
    /** Observed bug: provided row numbers will occasionally be out of the range of valid rows.
      * Sometimes this seems to be a pre-drawing step to get sizing right, and the value
      * is -1.  Other times, the value is too large, and the cell is drawn on top of the
      * correct cell.  The result is that the shading will sometimes be incorrect, depending
      * on the too-large value of row.  As a workaround, we just ignore requests to handle
      * cells with too-large row values (by returning an invisible, sizeless component).
      */
    int maxRow = tree.getRowCount() - (tree.isRootVisible() ? 0 : 1);
    if (row < maxRow) {
      Component c = _renderer.getTreeCellRendererComponent(tree, value, selected, expanded,
                                                           leaf, row, hasFocus);
      c.setBackground(row % 2 == 0 ? _even : _odd);
      return c;
    }
    else { return DUMMY_CELL; }
  }
  
  /**
   * Wrap the given tree's cell renderer in a {@code ShadedTreeCellRenderer} using the given
   * colors.
   */
  public static void shadeTree(JTree tree, Color even, Color odd) {
    tree.setCellRenderer(new ShadedTreeCellRenderer(tree.getCellRenderer(), even, odd));
  }
    

}