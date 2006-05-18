/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import java.util.Vector;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.text.BadLocationException;
import java.awt.event.*;
import java.awt.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import edu.rice.cs.util.Lambda;
import edu.rice.cs.drjava.model.RegionManagerListener;
import edu.rice.cs.drjava.model.DocumentRegion;
import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.UnexpectedException;

/**
 * Panel for displaying the breakpoints.  This class is a swing view class and hence should only be accessed from the 
 * event-handling thread.
 * @version $Id$
 */
public class BreakpointsPanel extends RegionsTreePanel<Breakpoint> {
  protected JButton _goToButton;
  protected JButton _enableDisableButton;
  protected JButton _removeButton;
  protected JButton _removeAllButton;
  protected final Debugger _debugger;
  
  /** Constructs a new breakpoints panel.
   *  This is swing view class and hence should only be accessed from the event-handling thread.
   *  @param frame the MainFrame
   *  @param title title of the pane
   */
  public BreakpointsPanel(MainFrame frame) {
    super(frame, "Breakpoints");
    _model.getBreakpointManager().addListener(new RegionManagerListener<Breakpoint>() {
      /** Called when a breakpoint is set in a document. Adds the breakpoint to the tree of breakpoints.
       *  Must be executed in event thread.
       *  @param bp the breakpoint
       */
      public void regionAdded(final Breakpoint bp) { addRegion(bp); }
      
      /**
       * Called when a breakpoint is changed.
       * Removes the breakpoint from the tree of breakpoints.
       * @param bp the breakpoint
       */
      public void regionChanged(final Breakpoint bp) {
        // Only change GUI from event-dispatching thread
        Runnable doCommand = new Runnable() {
          public void run() {
            String name = "";
            try {
              name = bp.getDocument().getQualifiedClassName();
            }
            catch (ClassNameNotFoundException cnnfe) {
              name = bp.getDocument().toString();
            }
            
            DefaultMutableTreeNode regDocNode = new DefaultMutableTreeNode(name);
            
            // Find the document node for this region
            Enumeration documents = _regionRootNode.children();
            boolean found = false;
            while ((!found) && (documents.hasMoreElements())) {
              DefaultMutableTreeNode doc = (DefaultMutableTreeNode)documents.nextElement();
              if (doc.getUserObject().equals(regDocNode.getUserObject())) {
                // Find the correct line start offset node for this breakpoint
                Enumeration existingRegions = doc.children();
                while (existingRegions.hasMoreElements()) {
                  DefaultMutableTreeNode existing = (DefaultMutableTreeNode)existingRegions.nextElement();
                  @SuppressWarnings("unchecked") RegionTreeUserObj<Breakpoint> uo =
                    (RegionTreeUserObj<Breakpoint>)existing.getUserObject();
                  if (uo.region().getStartOffset()==bp.getStartOffset()) {
                    Breakpoint r = (Breakpoint) uo.region();
                    if (r instanceof Breakpoint) {
                      ((Breakpoint)r).setEnabled(bp.isEnabled());
                      ((DefaultTreeModel)_regTree.getModel()).nodeChanged(existing);
                      found = true;
                      break;
                    }
                  }
                }
              }
            }
            updateButtons();
          }
        };
        Utilities.invokeLater(doCommand);
      }
      
      /**
       * Called when a breakpoint is removed from a document.
       * Removes the breakpoint from the tree of breakpoints.
       * @param bp the breakpoint
       */
      public void regionRemoved(final Breakpoint bp) {
        removeRegion(bp);
      }
    });
    _debugger = _model.getDebugger();
  }
  
  /** Creates the buttons for controlling the regions. Should be overridden. */
  protected JComponent[] makeButtons() {    
    Action goToAction = new AbstractAction("Go to") {
      public void actionPerformed(ActionEvent ae) {
        goToRegion();
      }
    };
    _goToButton = new JButton(goToAction);

    Action enableDisableAction = new AbstractAction("Disable") {
      public void actionPerformed(ActionEvent ae) {
        enableDisableBreakpoint();
      }
    };
    _enableDisableButton = new JButton(enableDisableAction);

    Action removeAction = new AbstractAction("Remove") {
      public void actionPerformed(ActionEvent ae) {
        for (Breakpoint bp: getSelectedRegions()) {
          _model.getBreakpointManager().removeRegion(bp);
        }
      }
    };
    _removeButton = new JButton(removeAction);
    
    Action removeAllAction = new AbstractAction("Remove All") {
      public void actionPerformed(ActionEvent ae) {
        _model.getBreakpointManager().clearRegions();
      }
    };
    _removeAllButton = new JButton(removeAllAction);
    
    JComponent[] buts = new JComponent[] { 
      _enableDisableButton,
        _goToButton, 
        _removeButton,
        _removeAllButton
    };
    
    return buts;
  }

  /** Update button state and text. */
  protected void updateButtons() {
    ArrayList<Breakpoint> regs = getSelectedRegions();
    _goToButton.setEnabled(regs.size()==1);
    _removeButton.setEnabled(regs.size()>0);
    _removeAllButton.setEnabled((_regionRootNode!=null) && (_regionRootNode.getDepth()>0));
    _enableDisableButton.setEnabled(regs.size()>0);
    if ((regs.size()>0) && (regs.get(0) instanceof Breakpoint)) {
      if (((Breakpoint)regs.get(0)).isEnabled()) {
        _enableDisableButton.setText("Disable");
      }
      else {
        _enableDisableButton.setText("Enable");
      }
    }
    _removeAllButton.setEnabled((_regionRootNode!=null) && (_regionRootNode.getDepth()>0));
  }
  
  /** Makes the popup menu actions. Should be overridden if additional actions besides "Go to" and "Remove" are added. */
  protected AbstractAction[] makePopupMenuActions() {
    AbstractAction[] acts = new AbstractAction[] {
      new AbstractAction("Go to") {
        public void actionPerformed(ActionEvent e) {
          goToRegion();
        }
      },
        
        new AbstractAction("Remove") {
          public void actionPerformed(ActionEvent e) {
            for (Breakpoint bp: getSelectedRegions()) {
              _model.getBreakpointManager().removeRegion(bp);
            }
          }
        }
    };
    return acts;
  }
  
  /** Go to region. */
  protected void goToRegion() {
    ArrayList<Breakpoint> bps = getSelectedRegions();
    if (bps.size() == 1) {
      _debugger.scrollToSource(bps.get(0));
    }
  }
  
  /** Toggle breakpoint's enable/disable flag. */
  protected void enableDisableBreakpoint() {
    final ArrayList<Breakpoint> bps = getSelectedRegions();
    if (bps.size()>0) {
      final boolean newState = !bps.get(0).isEnabled();
      for (Breakpoint bp: bps) {
        _model.getBreakpointManager().changeRegion(bp, new Lambda<Object, Breakpoint>() {
          public Object apply(Breakpoint bp) {
            bp.setEnabled(newState);
            return null;
          }
        });
      }
    }
  }
  
  
  /** Factory method to create user objects put in the tree.
   *  If subclasses extend RegionTreeUserObj, they need to override this method. */
  protected RegionTreeUserObj<Breakpoint> makeRegionTreeUserObj(Breakpoint bp) {
    return new BreakpointRegionTreeUserObj(bp);
  }

  /** Class that gets put into the tree. The toString() method determines what's displayed in the three. */
  protected static class BreakpointRegionTreeUserObj extends RegionTreeUserObj<Breakpoint> {
    public BreakpointRegionTreeUserObj (Breakpoint bp) { super(bp); }
    public String toString() {
      StringBuilder sb = new StringBuilder();
      _region.getDocument().acquireReadLock();
      try {
        sb.append(lineNumber());
        try {
          if (!_region.isEnabled()) { sb.append(" (disabled)"); }
          sb.append(": ");
          int length = Math.min(120, _region.getEndOffset()-_region.getStartOffset());
          sb.append(_region.getDocument().getText(_region.getStartOffset(), length).trim());
        } catch(BadLocationException bpe) { /* ignore, just don't display line */ }        
      } finally { _region.getDocument().releaseReadLock(); }
      return sb.toString();
    }
    public boolean equals(Object other) {
      BreakpointRegionTreeUserObj o = (BreakpointRegionTreeUserObj)other;
      return (o.region().getDocument().equals(region().getDocument())) &&
        (o.region().getStartOffset()==region().getStartOffset()) &&
        (o.region().getEndOffset()==region().getEndOffset()) &&
        (o.region().isEnabled()==region().isEnabled());
    }
  }
}
