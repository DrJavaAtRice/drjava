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
import javax.swing.tree.*;
import javax.swing.text.BadLocationException;
import java.awt.event.*;
import java.awt.*;

import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.drjava.model.RegionManager;
import edu.rice.cs.drjava.model.RegionManagerListener;
import edu.rice.cs.drjava.model.debug.*;

/** Panel for displaying the breakpoints.  This class is a swing view class and hence should only be accessed from the 
  * event-handling thread.
  * @version $Id: BreakpointsPanel.java 5594 2012-06-21 11:23:40Z rcartwright $
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
    */
  public BreakpointsPanel(MainFrame frame, RegionManager<Breakpoint> breakpointManager) {
    super(frame, "Breakpoints", breakpointManager);
    // TODO: consolidate the following listener with the MainFrame Breakpoint listener
    getRegionManager().addListener(new RegionManagerListener<Breakpoint>() {
      /** Called when a breakpoint is set in a document. Adds the breakpoint to the tree of breakpoints.
        * Must be executed in event thread.
        * @param bp the breakpoint
        */
      public void regionAdded(final Breakpoint bp) { 
        assert EventQueue.isDispatchThread();
        addRegion(bp); 
      }
      
      /** Called when a breakpoint is changed.
        * Removes the breakpoint from the tree of breakpoints.
        * @param bp the breakpoint
        */
      public void regionChanged(final Breakpoint bp) {
        assert EventQueue.isDispatchThread();

        DefaultMutableTreeNode regNode = _regionToTreeNode.get(bp);
        getRegTreeModel().nodeChanged(regNode);
      }
      
      /** Called when a breakpoint is removed from a document.
        * Removes the breakpoint from the tree of breakpoints.
        * @param bp the breakpoint
        */
      public void regionRemoved(final Breakpoint bp) { removeRegion(bp); }
    });
    _debugger = getGlobalModel().getDebugger();
  }
  
  /** Action performed when the Enter key is pressed. Should be overridden. */
  protected void performDefaultAction() { goToRegion(); }
  
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
//        startChanging();
        for (Breakpoint bp: getSelectedRegions()) getRegionManager().removeRegion(bp);
//        finishChanging();
      }
    };
    _removeButton = new JButton(removeAction);
    
    Action removeAllAction = new AbstractAction("Remove All") {
      public void actionPerformed(ActionEvent ae) {
//        startChanging();
        getRegionManager().clearRegions();
//        finishChanging();
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
  protected void _updateButtons() {
    ArrayList<Breakpoint> regs = getSelectedRegions();
    _goToButton.setEnabled(regs.size() == 1);
    _removeButton.setEnabled(regs.size() > 0);
    _removeAllButton.setEnabled(getRootNode() != null && getRootNode().getDepth() > 0);
    _enableDisableButton.setEnabled(regs.size() > 0);
    if (regs.size() > 0) {
      if (regs.get(0).isEnabled()) _enableDisableButton.setText("Disable");
      else _enableDisableButton.setText("Enable");
    }
    _removeAllButton.setEnabled(getRootNode() != null && getRootNode().getDepth() > 0);
  }
  
  /** Overloaded for BreakpointsPanel, do not close the panel if the tree becomes empty. */
  protected void closeIfEmpty() {
    // do not close the panel if the tree becomes empty
  }
  
  /** Makes the popup menu actions. Should be overridden if additional actions besides "Go to" and "Remove" are added. */
  protected AbstractAction[] makePopupMenuActions() {
    AbstractAction[] acts = new AbstractAction[] {
      new AbstractAction("Go to") {
        public void actionPerformed(ActionEvent e) { goToRegion(); }
      },
        
        new AbstractAction("Remove") {
          public void actionPerformed(ActionEvent e) {
            for (Breakpoint bp: getSelectedRegions()) getRegionManager() .removeRegion(bp);
          }
        }
    };
    return acts;
  }
  
  /** Go to region. */
  protected void goToRegion() {
    ArrayList<Breakpoint> bps = getSelectedRegions();
    if (bps.size() == 1) _debugger.scrollToSource(bps.get(0));
  }
  
  /** Toggle breakpoint's enable/disable flag. */
  protected void enableDisableBreakpoint() {
    final ArrayList<Breakpoint> bps = getSelectedRegions();
    if (bps.size() > 0) {
      final boolean newState = !bps.get(0).isEnabled();
      for (Breakpoint bp: bps) {
        getRegionManager().changeRegion(bp, new Lambda<Breakpoint,Object>() {
          public Object value(Breakpoint bp) {
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
      final StringBuilder sb = new StringBuilder();
      sb.append(lineNumber());
      try {
        if (!_region.isEnabled()) { sb.append(" (disabled)"); }
        sb.append(": ");
        int length = Math.min(120, _region.getEndOffset()-_region.getStartOffset());
        sb.append(_region.getDocument().getText(_region.getStartOffset(), length).trim());
      } catch(BadLocationException bpe) { /* ignore, just don't display line */ }        
      return sb.toString();
    }
  }
}
