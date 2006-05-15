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
import java.io.File;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.text.BadLocationException;
import java.awt.event.*;
import java.awt.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import edu.rice.cs.drjava.model.RegionManagerListener;
import edu.rice.cs.drjava.model.DocumentRegion;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.FileMovedException;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.UnexpectedException;

/**
 * Panel for displaying find results.
 * This class is a swing view class and hence should only be accessed from the event-handling thread.
 * @version $Id$
 */
public class FindResultsPanel extends RegionsTreePanel<DocumentRegion> {
  protected JButton _goToButton;
  protected JButton _bookmarkButton;
  protected JButton _removeButton;
  protected JButton _removeAllButton;
  
  /** Constructs a new find results panel.
   *  This is swing view class and hence should only be accessed from the event-handling thread.
   *  @param frame the MainFrame
   */
  public FindResultsPanel(MainFrame frame) {
    super(frame, "Find Results");
    _model.getFindResultsManager().addListener(new RegionManagerListener<DocumentRegion>() {      
      public void regionAdded(DocumentRegion r) {
        addRegion(r);
      }
      public void regionChanged(DocumentRegion r) { 
        regionRemoved(r);
        regionAdded(r);
      }
      public void regionRemoved(DocumentRegion r) {
        removeRegion(r);
      }
    });
  }
  
  /** Creates the buttons for controlling the regions. Should be overridden. */
  protected JButton[] makeButtons() {    
    Action goToAction = new AbstractAction("Go to") {
      public void actionPerformed(ActionEvent ae) {
        goToRegion();
      }
    };
    _goToButton = new JButton(goToAction);

    Action bookmarkAction = new AbstractAction("Bookmark") {
      public void actionPerformed(ActionEvent ae) {
        _bookmark();
      }
    };
    _bookmarkButton = new JButton(bookmarkAction);

    Action removeAction = new AbstractAction("Remove") {
      public void actionPerformed(ActionEvent ae) {
        _remove();
      }
    };
    _removeButton = new JButton(removeAction);
    
    Action removeAllAction = new AbstractAction("Remove All") {
      public void actionPerformed(ActionEvent ae) {
        _model.getFindResultsManager().clearRegions();
      }
    };
    _removeAllButton = new JButton(removeAllAction);
    
    JButton[] buts = new JButton[] { 
      _goToButton, 
        _bookmarkButton,
        _removeButton,
        _removeAllButton
    };
    
    return buts;
  }
  
  /** Turn the selected regions into bookmarks. */
  private void _bookmark() {
    for (final DocumentRegion r: getSelectedRegions()) {
      DocumentRegion bookmark = _model.getBookmarkManager().getRegionOverlapping(r.getDocument(),
                                                                                 r.getStartOffset(),
                                                                                 r.getEndOffset());
      if (bookmark==null) {
        _model.getBookmarkManager().addRegion(new DocumentRegion() {
          public OpenDefinitionsDocument getDocument() { return r.getDocument(); }
          public File getFile() throws FileMovedException { return r.getDocument().getFile(); }
          public int getStartOffset() { return r.getStartOffset(); }
          public int getEndOffset() { return r.getEndOffset(); }
        });
      }
    }
  }
  
  /** Remove the selected regions. */
  private void _remove() {
    for (DocumentRegion r: getSelectedRegions()) {
      _model.getFindResultsManager().removeRegion(r);
    }
  }

  /** Update button state and text. */
  protected void updateButtons() {
    ArrayList<DocumentRegion> regs = getSelectedRegions();
    _goToButton.setEnabled(regs.size()==1);
    _bookmarkButton.setEnabled(regs.size()>0);
    _removeButton.setEnabled(regs.size()>0);
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
        
        new AbstractAction("Bookmark") {
          public void actionPerformed(ActionEvent e) {
            _bookmark();
          }
        },
        
        new AbstractAction("Remove") {
          public void actionPerformed(ActionEvent e) {
            _remove();
          }
        }
    };
    return acts;
  }
  
  
  /** Factory method to create user objects put in the tree.
   *  If subclasses extend RegionTreeUserObj, they need to override this method. */
  protected RegionTreeUserObj<DocumentRegion> makeRegionTreeUserObj(DocumentRegion r) {
    return new FindResultsRegionTreeUserObj(r);
  }

  /** Class that gets put into the tree. The toString() method determines what's displayed in the three. */
  protected static class FindResultsRegionTreeUserObj extends RegionTreeUserObj<DocumentRegion> {
    public FindResultsRegionTreeUserObj(DocumentRegion r) { super(r); }
    public String toString() {
      StringBuilder sb = new StringBuilder();
      _region.getDocument().acquireReadLock();
      try {
        sb.append(lineNumber());
        try {
          sb.append(": ");
          int endSel = _region.getDocument().getLineEndPos(_region.getEndOffset());
          int startSel = _region.getDocument().getLineStartPos(_region.getStartOffset());
          
          int length = Math.min(120, endSel-startSel);
          sb.append(_region.getDocument().getText(startSel, length).trim());
        } catch(BadLocationException bpe) { /* ignore, just don't display line */ }        
      } finally { _region.getDocument().releaseReadLock(); }
      return sb.toString();
    }
  }
}
