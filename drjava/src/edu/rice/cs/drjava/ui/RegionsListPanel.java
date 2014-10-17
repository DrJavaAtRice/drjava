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

import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;

import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.IDocumentRegion;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.RightClickMouseAdapter;

/** Panel for displaying regions in a list in the order specified by indices passes to addRegion.
  * This class is a swing view class and hence should only be accessed from the event-handling thread.
  * The volatile declarations are included because the event-thread-only invariant is not enforced. TODO: fix this.
  * Not currently used because BrowserHistoryPanel is not used.
  * @version $Id: RegionsListPanel.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class RegionsListPanel<R extends IDocumentRegion> extends TabbedPanel {
  protected final JPanel _leftPane;
  
  protected volatile JList<RegionListUserObj<R>> _list;
  protected volatile DefaultListModel<RegionListUserObj<R>> _listModel;
  protected volatile String _title;
  
  protected final SingleDisplayModel _model;
  protected final MainFrame _frame;
  
  protected volatile JPanel _buttonPanel;
  
  /** Constructs a new panel to display regions in a list.
    * This is swing view class and hence should only be accessed from the event-handling thread.
    * @param frame the MainFrame
    * @param title title of the pane
    */
  public RegionsListPanel(MainFrame frame, String title) {
    super(frame, title);
    _title = title;
    this.setLayout(new BorderLayout());
    
    _frame = frame;
    _model = frame.getModel();
    
    this.removeAll(); // override the behavior of TabbedPanel
    
    // remake closePanel
    _closePanel = new JPanel(new BorderLayout());
    _closePanel.add(_closeButton, BorderLayout.NORTH);
    
    _leftPane = new JPanel(new BorderLayout());
    _setupRegionList();
    
    this.add(_leftPane, BorderLayout.CENTER);
    
    _buttonPanel = new JPanel(new BorderLayout());
    _setupButtonPanel();
    this.add(_buttonPanel, BorderLayout.EAST);
    updateButtons();
    
    // Setup the color listeners.
    _setColors(_list);
    
    _list.addMouseListener(new RegionMouseAdapter());
  }
  
  /** Quick helper for setting up color listeners. */
  private static void _setColors(Component c) {
    new ForegroundColorListener(c);
    new BackgroundColorListener(c);
  }
  
  /** Close the panel and update buttons. */
  @Override
  protected void _close() {
    super._close();
    updateButtons();
  }
  
  /** Creates the region list. TODO: fold this into constructor so related fields can be declared final. */
  private void _setupRegionList() {
    _listModel = new DefaultListModel<RegionListUserObj<R>>();
    _list = new JList<RegionListUserObj<R>>(_listModel) {
      public String getToolTipText(MouseEvent evt) {
        // Get item
        int index = locationToIndex(evt.getPoint());
        
        RegionListUserObj<R> node = getModel().getElementAt(index);
        R r = node.region();
        String tooltip = null;
        
        OpenDefinitionsDocument doc = r.getDocument();
        try {
          int lnr = doc.getLineOfOffset(r.getStartOffset())+1;
          int startOffset = doc._getOffset(lnr - 3);
          if (startOffset < 0) { startOffset = 0; }
          int endOffset = doc._getOffset(lnr + 3);
          if (endOffset < 0) { endOffset = doc.getLength()-1; }
          
          // convert to HTML (i.e. < to &lt; and > to &gt; and newlines to <br>)
          String s = doc.getText(startOffset, endOffset-startOffset);
          
          // this highlights the actual region in red
          int rStart = r.getStartOffset() - startOffset;
          if (rStart < 0) { rStart = 0; }
          int rEnd = r.getEndOffset() - startOffset;
          if (rEnd>s.length()) { rEnd = s.length(); }
          if ((rStart <= s.length()) && (rEnd >= rStart)) {
            String t1 = StringOps.encodeHTML(s.substring(0, rStart));
            String t2 = StringOps.encodeHTML(s.substring(rStart, rEnd));
            String t3 = StringOps.encodeHTML(s.substring(rEnd));
            s = t1 + "<font color=#ff0000>" + t2 + "</font>" + t3;
          }
          else {
            s = StringOps.encodeHTML(s);
          }
          tooltip = "<html><pre>" + s + "</pre></html>";
        }
        catch(javax.swing.text.BadLocationException ble) { tooltip = null; /* just don't give a tool tip */ }
        return tooltip;
      }
    };
    _list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    _list.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) { updateButtons(); }
    });            
    _list.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) performDefaultAction(); } 
    });
    _list.setFont(DrJava.getConfig().getSetting(OptionConstants.FONT_DOCLIST));
    
    _leftPane.add(new JScrollPane(_list));
    ToolTipManager.sharedInstance().registerComponent(_list);
  }
  
  /** Update button state and text. Should be overridden if additional buttons are added besides "Go To", "Remove" and "Remove All". */
  protected void updateButtons() {
  }
  
  /** Action performed when the Enter key is pressed. Should be overridden. */
  protected void performDefaultAction() {
  }
  
  /** Creates the buttons for controlling the regions. Should be overridden. */
  protected JComponent[] makeButtons() {        
    return new JComponent[0];    
  }
  
  /** Creates the buttons for controlling the regions. 
    * TODO: fold this into constructor so related fields can be declared final. */
  private void _setupButtonPanel() {
    JPanel mainButtons = new JPanel();
    JPanel emptyPanel = new JPanel();
    JPanel closeButtonPanel = new JPanel(new BorderLayout());
    GridBagLayout gbLayout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    mainButtons.setLayout(gbLayout);
    
    JComponent[] buts = makeButtons();
    
    closeButtonPanel.add(_closeButton, BorderLayout.NORTH);    
    for (JComponent b: buts) { mainButtons.add(b); }
    mainButtons.add(emptyPanel);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.weightx = 1.0;
    
    for (JComponent b: buts) { gbLayout.setConstraints(b, c); }
    
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.SOUTH;
    c.gridheight = GridBagConstraints.REMAINDER;
    c.weighty = 1.0;
    
    gbLayout.setConstraints(emptyPanel, c);
    
    _buttonPanel.add(mainButtons, BorderLayout.CENTER);
    _buttonPanel.add(closeButtonPanel, BorderLayout.EAST);
  }
  
  /** Gets the currently selected regions in the region list, or an empty array if no regions are selected.
    * @return list of selected regions in the list
    */
  protected ArrayList<R> getSelectedRegions() {
    ArrayList<R> regs = new ArrayList<R>();
    int[] indices = _list.getSelectedIndices();
    if (indices != null) {
      for (int index: indices) {
        RegionListUserObj<R> userObj = _listModel.elementAt(index);
        R r = userObj.region();
        regs.add(r);
      }
    }
    return regs;
  }
  
  /** Go to region. */
  protected void goToRegion() {
    ArrayList<R> r = getSelectedRegions();
    if (r.size() == 1) {
      RegionListUserObj<R> userObj = getUserObjForRegion(r.get(0));
      if (userObj != null) { _list.ensureIndexIsVisible(_listModel.indexOf(userObj)); }
      _frame.scrollToDocumentAndOffset(r.get(0).getDocument(), r.get(0).getStartOffset(), false);
    }
  }
  
  /** @return the usser object in the list associated with the region, or null if not found */
  protected RegionListUserObj<R> getUserObjForRegion(R r) {
    for(int i = 0; i < _listModel.size(); ++i) {
      RegionListUserObj<R> userObj = _listModel.get(i);
      if ((userObj.region().getStartOffset() == r.getStartOffset()) &&
          (userObj.region().getEndOffset() == r.getEndOffset()) &&
          (userObj.region().getDocument().equals(r.getDocument()))) {
        return userObj;
      }
    }
    return null;
  }
  
  /** Add a region to the list. Must be executed in event thread.
    * @param r the region
    * @param index the index where the region should be inserted
    */
  public void addRegion(final R r, final int index) {
    assert EventQueue.isDispatchThread();
    // Only change GUI from event-dispatching thread
//    Runnable doCommand = new Runnable() {
//      public void run() {
//        edu.rice.cs.drjava.model.AbstractGlobalModel.log.log("RegionsListPanel.addRegion: in list were...");
//        for(int i = 0; i < _listModel.getSize();++i) { edu.rice.cs.drjava.model.AbstractGlobalModel.log.log("\t" + _listModel.elementAt(i)); }
        
//        String name = "";
//        try { name = r.getDocument().getQualifiedClassName(); }
//        catch (ClassNameNotFoundException cnnfe) { name = r.getDocument().toString(); }
        
        RegionListUserObj<R> userObj = makeRegionListUserObj(r);
        _listModel.add(index, userObj);
        _list.ensureIndexIsVisible(_listModel.indexOf(userObj));
        
        updateButtons();
//      }
//    };
//    Utilities.invokeLater(doCommand);
  }
  
  /** Remove a region from the tree. Must be executed in event thread.
    * @param r the region
    */
  public void removeRegion(final R r) {
    // Only change GUI from event-dispatching thread
//    Runnable doCommand = new Runnable() {
//      public void run() {
//        String name = "";
//        try {
//          name = r.getDocument().getQualifiedClassName();
//        }
//        catch (ClassNameNotFoundException cnnfe) {
//          name = r.getDocument().toString();
//        }
    
    for (int i = 0; i < _listModel.size(); ++i) {
      RegionListUserObj<R> userObj = _listModel.get(i);
      if (userObj.region() == r) {
        _listModel.removeElementAt(i);
        break;
      }
    }
    
    updateButtons();
//      }
//    };
//    Utilities.invokeLater(doCommand);
  }
  
//  /** Remove all regions for this document from the tree. Must be executed in event thread. */
//  public void removeRegions(final OpenDefinitionsDocument odd) {
//    // Only change GUI from event-dispatching thread
//    Runnable doCommand = new Runnable() {
//      public void run() {
////        String name = "";
////        try { name = odd.getQualifiedClassName(); }
////        catch (ClassNameNotFoundException cnnfe) { name = odd.toString(); }
//        
//        for (int i = 0; i < _listModel.size(); ++i) {
//          RegionListUserObj<R> userObj = _listModel.get(i);
//          
//          if (userObj.region().getDocument().equals(odd)) {
//            _listModel.removeElementAt(i);
//          }
//        }
//        
//        updateButtons();
//      }
//    };
//    Utilities.invokeLater(doCommand);
//  }
  
  /** Factory method to create user objects put in the list.
    * If subclasses extend RegionListUserObj, they need to override this method. */
  protected RegionListUserObj<R> makeRegionListUserObj(R r) {
    return new RegionListUserObj<R>(r);
  }
  
  /** Class that gets put into the list. The toString() method determines what's displayed in the three. */
  protected static class RegionListUserObj<R extends IDocumentRegion> {
    protected final R _region;
    public int lineNumber() { return _region.getDocument().getLineOfOffset(_region.getStartOffset())+1; }
    public R region() { return _region; }
    public RegionListUserObj(R r) { _region = r; }
    public String toString() {
      final StringBuilder sb = new StringBuilder();
        sb.append(_region.getDocument().toString());
        sb.append(':');
        sb.append(lineNumber());
        try {
          sb.append(": ");
          int length = Math.min(120, _region.getEndOffset() - _region.getStartOffset());
          sb.append(_region.getDocument().getText(_region.getStartOffset(), length).trim());
        } catch(BadLocationException bpe) { /* ignore, just don't display line */ }        
      return sb.toString();
    }
//    public boolean equals(Object other) {
//      if ((other == null) || ! (other instanceof RegionListUserObj)) { return false; }
//      RegionListUserObj<R> o = other;
//      return (o.region().getDocument().equals(region().getDocument())) &&
//        (o.region().getStartOffset() == region().getStartOffset()) &&
//        (o.region().getEndOffset() == region().getEndOffset());
//    }
//    public int hashCode() { return (_region != null ? _region.hashCode() : 0); }
  }
  
  
  /** Mouse adapter for the region tree. */
  protected class RegionMouseAdapter extends RightClickMouseAdapter {
    protected void _popupAction(MouseEvent e) {
      // TODO: add popup
    }
    
    public void mousePressed(MouseEvent e) {
      super.mousePressed(e);
      if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
        performDefaultAction();
      }
    }
  }
}
