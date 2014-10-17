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
import java.awt.event.*;

import edu.rice.cs.drjava.model.RegionManager;
import edu.rice.cs.drjava.model.RegionManagerListener;
import edu.rice.cs.drjava.model.MovingDocumentRegion;

/** Panel for displaying bookmarks. Only runs in the event thread.
  * @version $Id: BookmarksPanel.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class BookmarksPanel extends RegionsTreePanel<MovingDocumentRegion> {
  protected JButton _goToButton;
  protected JButton _removeButton;
  protected JButton _removeAllButton;
  
  /** Constructs a new bookmarks panel.
   *  This is swing view class and hence should only be accessed from the event-handling thread.
   *  @param frame the MainFrame
   */
  public BookmarksPanel(MainFrame frame, RegionManager<MovingDocumentRegion> bookmarkManager) {
    super(frame, "Bookmarks", bookmarkManager);
    getRegionManager().addListener(new RegionManagerListener<MovingDocumentRegion>() {      
      public void regionAdded(MovingDocumentRegion r) { addRegion(r); }
      public void regionChanged(MovingDocumentRegion r) { 
        regionRemoved(r);
        regionAdded(r);
      }
      public void regionRemoved(MovingDocumentRegion r) { removeRegion(r); }
    });
  }
  
  /** Action performed when the Enter key is pressed. Should be overridden. */
  protected void performDefaultAction() {
    goToRegion();
  }
  
  /** Creates the buttons for controlling the regions. Should be overridden. */
  protected JComponent[] makeButtons() {    
    Action goToAction = new AbstractAction("Go to") {
      public void actionPerformed(ActionEvent ae) {
        goToRegion();
      }
    };
    _goToButton = new JButton(goToAction);

    Action removeAction = new AbstractAction("Remove") {
      public void actionPerformed(ActionEvent ae) { _remove(); }  // remove is inherited from RegionsTreePanel
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
      _goToButton, 
        _removeButton,
        _removeAllButton
    };
    
    return buts;
  }

  /** Update button state and text. */
  protected void _updateButtons() {
    ArrayList<MovingDocumentRegion> regs = getSelectedRegions();
    _goToButton.setEnabled(regs.size() == 1);
    _removeButton.setEnabled(regs.size() > 0);
    _removeAllButton.setEnabled(getRootNode() != null && getRootNode().getDepth() > 0);
  }
  
  /** Makes the popup menu actions. Should be overridden if additional actions besides "Go to" and "Remove" are added. */
  protected AbstractAction[] makePopupMenuActions() {
    AbstractAction[] acts = new AbstractAction[] {
      new AbstractAction("Go to") { public void actionPerformed(ActionEvent e) { goToRegion(); } },
        
      new AbstractAction("Remove") {
        public void actionPerformed(ActionEvent e) {
          for (MovingDocumentRegion r: getSelectedRegions()) getRegionManager().removeRegion(r);
        }
      }
    };
    return acts;
  }
}
