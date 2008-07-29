/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.io.File;
import java.lang.ref.WeakReference;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.LayeredHighlighter;
import java.awt.event.*;
import java.awt.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.DocumentRegion;
import edu.rice.cs.drjava.model.FileMovedException;
import edu.rice.cs.drjava.model.MovingDocumentRegion;
import edu.rice.cs.drjava.model.OrderedDocumentRegion;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.RegionManager;
import edu.rice.cs.drjava.model.RegionManagerListener;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.drjava.config.OptionConstants;

/** Panel for displaying find results. This class is a swing class which should only be accessed from the event thread.
  * @version $Id$
  */
public class FindResultsPanel extends RegionsTreePanel<MovingDocumentRegion> {

  // The following field has been hoisted into RegionsTreePanel
//  protected final RegionManager<MovingDocumentRegion> _regionManager;
  protected final String _searchString;
  protected final boolean _searchAll;
  protected final boolean _matchCase;
  protected final boolean _wholeWord;
  protected final boolean _noComments;
  protected final boolean _noTestCases;
  protected final WeakReference<OpenDefinitionsDocument> _doc;
  protected final FindReplacePanel _findReplace;
  
  protected JButton _findAgainButton;
  protected JButton _goToButton;
  protected JButton _bookmarkButton;
  protected JButton _removeButton;
  protected JComboBox _colorBox;
  protected int _lastIndex;
  
  /** Saved option listeners kept in this field so they can be removed for garbage collection  */
  private LinkedList<Pair<Option<Color>, OptionListener<Color>>> _colorOptionListeners = 
    new LinkedList<Pair<Option<Color>, OptionListener<Color>>>();
  
  /** Constructs a new find results panel. This is swing class which should only be accessed from the event thread.
    * @param frame the MainFrame
    * @param rm the region manager associated with this panel
    * @param title for the panel
    * @param searchString string that was searched for
    * @param searchAll whether all files were searched
    * @param doc weak reference to the document in which the search occurred (or started, if all documents were searched)
    * @param findReplace the FindReplacePanel that created this FindResultsPanel
    */
  public FindResultsPanel(MainFrame frame, RegionManager<MovingDocumentRegion> regionManager, String title, 
                          String searchString, boolean searchAll, boolean matchCase, boolean wholeWord, 
                          boolean noComments, boolean noTestCases, WeakReference<OpenDefinitionsDocument> doc, 
                          FindReplacePanel findReplace) {
    super(frame, title, regionManager);
//    _regionManager = regionManager;
    _searchString = searchString;
    _searchAll    = searchAll;
    _matchCase    = matchCase;
    _wholeWord    = wholeWord;
    _noComments   = noComments;
    _noTestCases  = noTestCases;
    _doc          = doc;
    _findReplace  = findReplace;
    
    for(int i = 0; i < OptionConstants.FIND_RESULTS_COLORS.length; ++i) {
      final OptionListener<Color> listener = new FindResultsColorOptionListener(i);
      final Pair<Option<Color>, OptionListener<Color>> pair = 
        new Pair<Option<Color>, OptionListener<Color>>(OptionConstants.FIND_RESULTS_COLORS[i], listener);
      _colorOptionListeners.add(pair);
      DrJava.getConfig().addOptionListener(OptionConstants.FIND_RESULTS_COLORS[i], listener);
    }
  }
  
  class ColorComboRenderer extends JPanel implements ListCellRenderer {
    private Color _color = DrJava.getConfig().getSetting(OptionConstants.FIND_RESULTS_COLORS[_colorBox.getSelectedIndex()]);
    private DefaultListCellRenderer _defaultRenderer = new DefaultListCellRenderer();
    private final Dimension _size = new Dimension(0, 20);  
    private final CompoundBorder _compoundBorder = 
      new CompoundBorder(new MatteBorder(2, 10, 2, 10, Color.white), new LineBorder(Color.black));
    
    public ColorComboRenderer() {
      super();
      setBackground(_color);
      setBorder(_compoundBorder);
    }
    
    public Component getListCellRendererComponent(JList list, Object value, int row, boolean sel, boolean hasFocus) {
      JComponent renderer;
      if (value instanceof Color) {
        _color = (Color) value;
        renderer = this;
      }
      else {
        JLabel l = (JLabel) _defaultRenderer.getListCellRendererComponent(list, value, row, sel, hasFocus);
        l.setHorizontalAlignment(JLabel.CENTER);
        renderer = l;
      }
      // Taken out because this is a 1.5 method; not sure if it's necessary
      renderer.setPreferredSize(_size);
      return renderer;
    }
    
    public void paint(Graphics g) {
      setBackground(_color);
      setBorder(_compoundBorder);
      super.paint(g);
    }
  }
  
  /** Creates the buttons for controlling the regions. Should be overridden. */
  protected JComponent[] makeButtons() {    
    Action findAgainAction = new AbstractAction("Find Again") {
      public void actionPerformed(ActionEvent ae) { _findAgain(); }
    };
    _findAgainButton = new JButton(findAgainAction);

    Action goToAction = new AbstractAction("Go to") {
      public void actionPerformed(ActionEvent ae) { goToRegion(); }
    };
    _goToButton = new JButton(goToAction);
    
    Action bookmarkAction = new AbstractAction("Bookmark") {
      public void actionPerformed(ActionEvent ae) { _bookmark(); }
    };
    _bookmarkButton = new JButton(bookmarkAction);
    
    Action removeAction = new AbstractAction("Remove") {
      public void actionPerformed(ActionEvent ae) { _remove(); }
    };
    _removeButton = new JButton(removeAction);
    
    // "Highlight" label panel
    final JPanel highlightPanel = new JPanel();
    final Color normalColor = highlightPanel.getBackground();
    highlightPanel.add(new JLabel("Highlight:"));
    
    // find the first available color, or choose "None"
    int smallestIndex = 0;
    int smallestUsage = DefinitionsPane.FIND_RESULTS_PAINTERS_USAGE[smallestIndex];
    for(_lastIndex = 0; _lastIndex < OptionConstants.FIND_RESULTS_COLORS.length; ++_lastIndex) {
      if (DefinitionsPane.FIND_RESULTS_PAINTERS_USAGE[_lastIndex] < smallestUsage) {
        smallestIndex = _lastIndex;
        smallestUsage = DefinitionsPane.FIND_RESULTS_PAINTERS_USAGE[smallestIndex];
      }
    }
    _lastIndex = smallestIndex;
    ++DefinitionsPane.FIND_RESULTS_PAINTERS_USAGE[_lastIndex];
    _colorBox = new JComboBox();    
    for (int i=0; i < OptionConstants.FIND_RESULTS_COLORS.length; ++i) {
      _colorBox.addItem(DrJava.getConfig().getSetting(OptionConstants.FIND_RESULTS_COLORS[i]));
    }
    _colorBox.addItem("None");
    _colorBox.setRenderer(new ColorComboRenderer());
    _colorBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (_lastIndex<OptionConstants.FIND_RESULTS_COLORS.length) {
          --DefinitionsPane.FIND_RESULTS_PAINTERS_USAGE[_lastIndex];
        }
        _lastIndex = _colorBox.getSelectedIndex();
        if (_lastIndex<OptionConstants.FIND_RESULTS_COLORS.length) {
          ++DefinitionsPane.FIND_RESULTS_PAINTERS_USAGE[_lastIndex];
          highlightPanel.setBackground(DrJava.getConfig().getSetting(OptionConstants.FIND_RESULTS_COLORS[_lastIndex]));
        }
        else highlightPanel.setBackground(normalColor);
        
        _frame.refreshFindResultsHighlightPainter(FindResultsPanel.this, 
                                                  DefinitionsPane.FIND_RESULTS_PAINTERS[_lastIndex]);
      }
    });
    _colorBox.setMaximumRowCount(OptionConstants.FIND_RESULTS_COLORS.length + 1);
    _colorBox.addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuCanceled(PopupMenuEvent e) { _colorBox.revalidate(); }
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { _colorBox.revalidate(); }
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) { _colorBox.revalidate(); }
    });
    _colorBox.setSelectedIndex(_lastIndex);
    _frame.refreshFindResultsHighlightPainter(FindResultsPanel.this, 
                                              DefinitionsPane.FIND_RESULTS_PAINTERS[_lastIndex]);
    
    updateButtons();
    return new JComponent[] { _findAgainButton, _goToButton, _bookmarkButton, _removeButton, highlightPanel, _colorBox};
  }
  
  /** @return the selected painter for these find results. */
  public LayeredHighlighter.LayerPainter getSelectedPainter() {
    return DefinitionsPane.FIND_RESULTS_PAINTERS[_lastIndex];
  }
  
  /** Find again. */
  private void _findAgain() {
    _updateButtons();   // force an update buttons operation
    OpenDefinitionsDocument odd = null;
    if (_searchAll) odd = _model.getActiveDocument();
    else if (_doc != null) { odd = _doc.get(); }
    if (odd != null) {

      _regionManager.clearRegions();
      assert _rootNode == _regTreeModel.getRoot();
      _rootNode.removeAllChildren();
      _docToTreeNode.clear();
      _regionToTreeNode.clear();
      _regTreeModel.nodeStructureChanged(_rootNode);
      _requestFocusInWindow();
//      System.err.println("Root has been cleared; child count = " + _rootNode.getChildCount());
      _findReplace.findAll(_searchString, _searchAll, _matchCase, _wholeWord, _noComments, _noTestCases, odd, 
                           _regionManager, this);
      _regTree.scrollRowToVisible(0);  // Scroll to the first line in the new panel
      _requestFocusInWindow();
    }
  }
  
  /** Turn the selected regions into bookmarks. */
  private void _bookmark() {
    updateButtons();
//    _frame._bookmarksPanel.startChanging();
    for (final MovingDocumentRegion r: getSelectedRegions()) {
      if (! _model.getBookmarkManager().contains(r)) {
        OrderedDocumentRegion newR = 
          new DocumentRegion(r.getDocument(), r.getStartPosition(), r.getEndPosition());
        _model.getBookmarkManager().addRegion(newR);
      }
    }
//    _frame._bookmarksPanel.finishChanging();
  }
  
  /** Action performed when the Enter key is pressed. Should be overridden. */
  protected void performDefaultAction() { goToRegion(); }
  
  /** Remove the selected regions. */
  private void _remove() {

    int[] rows = _regTree.getSelectionRows();
//    System.err.println("_remove() called with rows " + Arrays.toString(rows));
    int len = rows.length;
    int row = (len > 0) ? rows[len - 1] : 0;
//    _regTree.setSelectionRow(row);
    _frame.removeCurrentLocationHighlight();
//    startChanging();
    for (MovingDocumentRegion r: getSelectedRegions()) {
      _regionManager.removeRegion(r); // removes r from region manager and the panel node for r from the tree model
    }
    _regTree.setSelectionRow(row);
    _requestFocusInWindow();
    _regTree.scrollRowToVisible(row);

  }
  
  /** Remove a region from this panel. If this panel is emptied, remove this.  Must be executed in event thread.
    * @param r the region
    */
  public void removeRegion(final MovingDocumentRegion r) {
    super.removeRegion(r);
    if (_regionManager.getDocuments().isEmpty()) _close();
  }
  
  /** Update button state and text. */
  protected void _updateButtons() {
    ArrayList<MovingDocumentRegion> regs = getSelectedRegions();
    OpenDefinitionsDocument odd = null;
    if (_doc != null) { odd = _doc.get(); }
    _findAgainButton.setEnabled(odd != null || _searchAll);
    _goToButton.setEnabled(regs.size() == 1);
    _bookmarkButton.setEnabled(regs.size() > 0);
    _removeButton.setEnabled(regs.size() > 0);
  }
  
  /** Makes popup menu actions. Should be overridden if additional actions besides "Go to" and "Remove" are added. */
  protected AbstractAction[] makePopupMenuActions() {
    AbstractAction[] acts = new AbstractAction[] {
      new AbstractAction("Go to") { public void actionPerformed(ActionEvent e) { goToRegion(); } },
        new AbstractAction("Bookmark") { public void actionPerformed(ActionEvent e) { _bookmark(); } },
          new AbstractAction("Remove") { public void actionPerformed(ActionEvent e) { _remove(); } }
    };
    return acts;
  }
  
  /** Go to region. */
  protected void goToRegion() {
    ArrayList<MovingDocumentRegion> r = getSelectedRegions();
    // we highlight the current location using the teal band
    if (r.size() == 1) {
      _frame.removeCurrentLocationHighlight();
      _frame.goToRegionAndHighlight(r.get(0));
    }
  }
  
  /** Close the panel. */
  public void _close() {
    super._close();
    _frame.removeCurrentLocationHighlight();
    freeResources();
  }
  
  /** Free the resources; this can be used if the panel was never actually displayed. */
  public void freeResources() {
    _regionManager.clearRegions();
    _model.disposeFindResultsManager(_regionManager);
    _docToTreeNode.clear();
    _regionToTreeNode.clear();
    for (Pair<Option<Color>, OptionListener<Color>> p: _colorOptionListeners) {
      DrJava.getConfig().removeOptionListener(p.first(), p.second());
    }
    if (_lastIndex < OptionConstants.FIND_RESULTS_COLORS.length) {
      --DefinitionsPane.FIND_RESULTS_PAINTERS_USAGE[_lastIndex];
    }
  }
  
  /** Factory method to create user objects in tree nodes.  If subclasses extend RegionTreeUserObj, they must override
    * this method. */
  protected RegionTreeUserObj<MovingDocumentRegion> makeRegionTreeUserObj(MovingDocumentRegion r) {
    return new FindResultsRegionTreeUserObj(r);
  }
  
  /** Return true if all documents were searched. */
  public boolean isSearchAll() { return _searchAll; }
  
  /** Return the document which was searched (or where the search started, if _searchAll is true).
    * May return null if the weak reference to the document was severed. */
  public OpenDefinitionsDocument getDocument() { return _doc.get(); }

  /** Disables "Find Again", e.g. because the document was closed. */
  public void disableFindAgain() {_doc.clear(); updateButtons(); }
  
  /** Class that gets put into the tree. The toString() method determines what's displayed in the tree. */
  protected static class FindResultsRegionTreeUserObj extends RegionTreeUserObj<MovingDocumentRegion> {
    protected int _lineNumber;
    public FindResultsRegionTreeUserObj(MovingDocumentRegion r) {
      super(r);
      _lineNumber = _region.getDocument().getLineOfOffset(_region.getStartOffset()) + 1;
    }

    public String toString() {
      final String textFont = DrJava.getConfig().getSetting(OptionConstants.FONT_MAIN).toString();
      final String numFont = DrJava.getConfig().getSetting(OptionConstants.FONT_LINE_NUMBERS).toString();
      final StringBuilder sb = new StringBuilder(120);
//      sb.append("<html><pre><font face=\"sanserif\">");
      sb.append(lineNumber());
      sb.append(": ");
      String text = _region.getString();
      sb.append(text);
//      sb.append(StringOps.getBlankString(120 - text.length()));
//      sb.append("</pre></html>");
      return sb.toString();
    }
  }
  
  /** The OptionListener for FIND_RESULTS_COLOR. */
  private class FindResultsColorOptionListener implements OptionListener<Color> {
    private int _index;
    public FindResultsColorOptionListener(int i) { _index = i; }
    public void optionChanged(OptionEvent<Color> oce) {
      int pos = _colorBox.getSelectedIndex();
      _colorBox.removeItemAt(_index);
      _colorBox.insertItemAt(oce.value, _index);
      _colorBox.setSelectedIndex(pos);
      if (pos == _index) {
        _frame.refreshFindResultsHighlightPainter(FindResultsPanel.this, DefinitionsPane.FIND_RESULTS_PAINTERS[_index]);
      }
    }
  }
}
