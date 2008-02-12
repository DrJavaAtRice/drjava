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