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

import java.util.Vector;
import java.util.LinkedList;
import java.util.ArrayList;
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

import edu.rice.cs.drjava.model.RegionManagerListener;
import edu.rice.cs.drjava.model.DocumentRegion;
import edu.rice.cs.drjava.model.MovingDocumentRegion;
import edu.rice.cs.drjava.model.OrderedDocumentRegion;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.FileMovedException;
import edu.rice.cs.drjava.model.RegionManager;
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
  protected JButton _findAgainButton;
  protected JButton _goToButton;
  protected JButton _bookmarkButton;
  protected JButton _removeButton;
  protected JComboBox _colorBox;
  protected RegionManager<MovingDocumentRegion> _regionManager;
  protected int _lastIndex;
  protected String _searchString;
  protected boolean _searchAll;
  protected boolean _matchCase;
  protected boolean _wholeWord;
  protected boolean _noComments;
  protected boolean _noTestCases;
  protected WeakReference<OpenDefinitionsDocument> _doc;
  protected FindReplacePanel _findReplace;
  
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
  public FindResultsPanel(MainFrame frame, RegionManager<MovingDocumentRegion> rm, String title,
                          String searchString, boolean searchAll, final boolean matchCase,
                          final boolean wholeWord, final boolean noComments, final boolean noTestCases,
                          WeakReference<OpenDefinitionsDocument> doc, FindReplacePanel findReplace) {
    super(frame, title);
    _regionManager = rm;
    _regionManager.addListener(new RegionManagerListener<MovingDocumentRegion>() {      
      public void regionAdded(MovingDocumentRegion r) { addRegion(r); }
      public void regionChanged(MovingDocumentRegion r) { 
        regionRemoved(r);
        regionAdded(r);
      }
      public void regionRemoved(MovingDocumentRegion r) { removeRegion(r); }
    });
    _searchString = searchString;
    _searchAll = searchAll;
    _matchCase = matchCase;
    _wholeWord = wholeWord;
    _noComments = noComments;
    _noTestCases = noTestCases;
    _doc = doc;
    _findReplace = findReplace;
    
    OptionListener<Color> temp;
    Pair<Option<Color>, OptionListener<Color>> pair;
    for(int i = 0; i < OptionConstants.FIND_RESULTS_COLORS.length; ++i) {
      temp = new FindResultsColorOptionListener(i);
      pair = new Pair<Option<Color>, OptionListener<Color>>(OptionConstants.FIND_RESULTS_COLORS[i], temp);
      _colorOptionListeners.add(pair);
      DrJava.getConfig().addOptionListener(OptionConstants.FIND_RESULTS_COLORS[i], temp);
    }
  }
  
  class ColorComboRenderer extends JPanel implements ListCellRenderer {
    private Color m_c = DrJava.getConfig().getSetting(OptionConstants.FIND_RESULTS_COLORS[_colorBox.getSelectedIndex()]);
    private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
    private final Dimension preferredSize = new Dimension(0, 20);    
    
    public ColorComboRenderer() {
      super();
      setBackground(m_c);
      setBorder(new CompoundBorder(new MatteBorder(2, 10, 2, 10, Color.white), new LineBorder(Color.black)));
    }
    
    public Component getListCellRendererComponent(JList list, Object value, int row, boolean sel, boolean hasFocus) {
      JComponent renderer;
      if (value instanceof Color) {
        m_c = (Color) value;
        renderer = this;
      }
      else {
        JLabel l = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, row, sel, hasFocus);
        l.setHorizontalAlignment(JLabel.CENTER);
        renderer = l;
      }
      // Taken out because this is a 1.5 method; not sure if it's necessary
      renderer.setPreferredSize(preferredSize);
      return renderer;
    }
    
    public void paint(Graphics g) {
      setBackground(m_c);
      setBorder(new CompoundBorder(new MatteBorder(2, 10, 2, 10, Color.white), new LineBorder(Color.black)));
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
    for(_lastIndex=0; _lastIndex<OptionConstants.FIND_RESULTS_COLORS.length; ++_lastIndex) {
      if (DefinitionsPane.FIND_RESULTS_PAINTERS_USAGE[_lastIndex]<smallestUsage) {
        smallestIndex = _lastIndex;
        smallestUsage = DefinitionsPane.FIND_RESULTS_PAINTERS_USAGE[smallestIndex];
      }
    }
    _lastIndex = smallestIndex;
    ++DefinitionsPane.FIND_RESULTS_PAINTERS_USAGE[_lastIndex];
    _colorBox = new JComboBox();    
    for(int i=0; i<OptionConstants.FIND_RESULTS_COLORS.length; ++i) {
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
    _colorBox.setMaximumRowCount(OptionConstants.FIND_RESULTS_COLORS.length+1);
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
    updateButtons();
    OpenDefinitionsDocument odd = null;
    if (_searchAll) odd = _model.getActiveDocument();
    else if (_doc != null) { odd = _doc.get(); }
    if (odd != null) {
      _regionManager.clearRegions();
      _findReplace.findAll(_searchString, _searchAll, _matchCase, _wholeWord,
                           _noComments, _noTestCases, odd, _regionManager, this);
    }
  }
  
  /** Turn the selected regions into bookmarks. */
  private void _bookmark() {
    updateButtons();
    setChanging(true);
    _frame._bookmarksPanel.setChanging(true);
    for (final MovingDocumentRegion r: getSelectedRegions()) {
      if (! _model.getBookmarkManager().contains(r)) {
//        try {
          OrderedDocumentRegion newR = 
            new DocumentRegion(r.getDocument(), r.getStartPosition(), r.getEndPosition());
          _model.getBookmarkManager().addRegion(newR);
//        }
//        catch (FileMovedException fme) { throw new UnexpectedException(fme); }
      }
    }
    setChanging(false);
    _frame._bookmarksPanel.setChanging(false);
  }
  
  /** Action performed when the Enter key is pressed. Should be overridden. */
  protected void performDefaultAction() { goToRegion(); }
  
  /** Remove the selected regions. */
  private void _remove() {
    updateButtons();
    setChanging(true);
    for (MovingDocumentRegion r: getSelectedRegions()) _regionManager.removeRegion(r);
    setChanging(false);
    if (_regionManager.getDocuments().size() == 0) { _close(); }
  }
  
  /** Update button state and text. */
  protected void updateButtons() {
    ArrayList<MovingDocumentRegion> regs = getSelectedRegions();
    OpenDefinitionsDocument odd = null;
    if (_doc!=null) { odd = _doc.get(); }
    _findAgainButton.setEnabled((odd!=null) || _searchAll);
    _goToButton.setEnabled(regs.size()==1);
    _bookmarkButton.setEnabled(regs.size()>0);
    _removeButton.setEnabled(regs.size()>0);
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
  
  /** Close the pane. */
  public void _close() {
    super._close();
    freeResources();
  }
  
  /** Free the resources; this can be used if the panel was never actually displayed. */
  public void freeResources() {
    _regionManager.clearRegions();
    _model.disposeFindResultsManager(_regionManager);
    for (Pair<Option<Color>, OptionListener<Color>> p: _colorOptionListeners) {
      DrJava.getConfig().removeOptionListener(p.first(), p.second());
    }
    if (_lastIndex<OptionConstants.FIND_RESULTS_COLORS.length) {
      --DefinitionsPane.FIND_RESULTS_PAINTERS_USAGE[_lastIndex];
    }
  }
  
  /** Factory method to create user objects put in the tree.
    * If subclasses extend RegionTreeUserObj, they need to override this method. */
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
    public boolean equals(Object other) {
      if (other == null || other.getClass() != this.getClass()) return false;
      @SuppressWarnings("unchecked") FindResultsRegionTreeUserObj o = (FindResultsRegionTreeUserObj)other;
      return (o.region().getDocument().equals(region().getDocument()) &&
              (o.region().getStartOffset() == region().getStartOffset()) &&
              (o.region().getEndOffset() == region().getEndOffset()) &&
              (o._lineNumber == this._lineNumber));
    }
    public int hashCode() {
      int result;
      result = (_region != null ? _region.hashCode() : 0);
      return result;
    }
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append("<html>");
      sb.append(lineNumber());
      sb.append(": ");
      sb.append(_region.getString());
      sb.append("</html>");
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
      if (pos==_index) {
        _frame.refreshFindResultsHighlightPainter(FindResultsPanel.this, DefinitionsPane.FIND_RESULTS_PAINTERS[_index]);
      }
    }
  }
}
