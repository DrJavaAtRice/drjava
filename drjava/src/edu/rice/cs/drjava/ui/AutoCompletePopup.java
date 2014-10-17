package edu.rice.cs.drjava.ui;

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

import java.awt.event.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.ui.predictive.PredictiveInputFrame;
import edu.rice.cs.drjava.ui.predictive.PredictiveInputModel;
import edu.rice.cs.drjava.model.DummyOpenDefDoc;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.iter.*;
import edu.rice.cs.plt.collect.UnionSet;

import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.Utilities;

import static edu.rice.cs.drjava.ui.MainFrameStatics.*;
import static edu.rice.cs.drjava.ui.predictive.PredictiveInputModel.*;
import static edu.rice.cs.drjava.ui.predictive.PredictiveInputFrame.FrameState;

/** Autocomplete support.
  * @version $Id: AutoCompletePopup.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class AutoCompletePopup {
  /** Main frame. */
  final protected MainFrame _mainFrame;
    
  /** Checkbox that controls whether Java API classes are included. */
  JCheckBox _completeJavaAPICheckbox = new JCheckBox("Java API");
  
  /** Frame state. */
  protected FrameState _lastState = null;
  
  /** Complete set of entries. */
  final protected Set<AutoCompletePopupEntry> _allEntries;  
  
  /** Set of the document entries. */
  final protected Set<AutoCompletePopupEntry> _docEntries;
  
  /** Set of all the Java API classes. */
  final protected Set<AutoCompletePopupEntry> _apiEntries;
  
  /** Constructor for an auto-complete popup that uses the MainFrame for information.
    * @param mf main frame of DrJava */
  public AutoCompletePopup(MainFrame mf) { this(mf, null); }

  /** Constructor for an auto-complete popup that uses the MainFrame for information.
    * @param mf main frame of DrJava
    * @param frameState position and size of the dialog
    */
  public AutoCompletePopup(MainFrame mf, String frameState) {
    _mainFrame = mf;
    if (frameState!=null) _lastState = new FrameState(frameState);
    _docEntries = new HashSet<AutoCompletePopupEntry>();
    _apiEntries = new HashSet<AutoCompletePopupEntry>();
    _allEntries = new UnionSet<AutoCompletePopupEntry>(_apiEntries,
                                                       new UnionSet<AutoCompletePopupEntry>(mf.getCompleteClassSet(),
                                                                                            _docEntries));
  }
  
  /** Display an auto-complete popup with the specified window title centered around
    * the parent component. The initial text and caret location are specified, as well
    * as the action to be performed if the popup is cancelled or accepted.
    * @param parent parent GUI component
    * @param title window title
    * @param initial initial text
    * @param loc caret location in the initial text
    * @param canceledAction action to take if canceled
    * @param acceptedAction action to take if accepted <simple name, full name, from, to>
    */
  public void show(final Component parent,
                   final String title,
                   final String initial,
                   final int loc,
                   final Runnable canceledAction,
                   final Runnable3<AutoCompletePopupEntry,Integer,Integer> acceptedAction) {
    show(parent, title, initial, loc, IterUtil.make("OK"), 
         IterUtil.make(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)), 0, canceledAction, IterUtil.make(acceptedAction));
  }
  
  /** Display an auto-complete popup with the specified window title centered around
    * the parent component. The initial text and caret location are specified, as well
    * as the action to be performed if the popup is cancelled or accepted.
    * @param parent parent GUI component
    * @param title window title
    * @param initial initial text
    * @param loc caret location in the initial text
    * @param actionNames names for the actions
    * @param canceledAction action to take if canceled
    * @param acceptedActions actions to take if accepted <simple name, full name, from, to>
    */
  public void show(final Component parent,
                   final String title,
                   final String initial,
                   final int loc,
                   final SizedIterable<String> actionNames,
                   final Runnable canceledAction,
                   final SizedIterable<Runnable3<AutoCompletePopupEntry,Integer,Integer>> acceptedActions) {
    SizedIterable<KeyStroke> actionKeyStrokes = 
      IterUtil.compose(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                       IterUtil.copy((KeyStroke)null, acceptedActions.size()-1));
    show(parent, title, initial, loc, actionNames, actionKeyStrokes, 
         0, canceledAction, acceptedActions);
  }
    
  /** Display an auto-complete popup with the specified window title centered around
    * the parent component. The initial text and caret location are specified, as well
    * as the action to be performed if the popup is cancelled or accepted.
    * @param parent parent GUI component
    * @param title window title
    * @param initial initial text
    * @param loc caret location in the initial text
    * @param actionNames names for the actions
    * @param actionKeyStrokes keystrokes for the actions
    * @param oneMatchActionIndex the action to take when there's just one match, or -1
    * @param canceledAction action to take if canceled
    * @param acceptedActions actions to take if accepted <simple name, full name, from, to>
    */
  public void show(final Component parent,
                   final String title,
                   final String initial,
                   final int loc,
                   final SizedIterable<String> actionNames,
                   final SizedIterable<KeyStroke> actionKeyStrokes,
                   final int oneMatchActionIndex,
                   final Runnable canceledAction,
                   final SizedIterable<Runnable3<AutoCompletePopupEntry,Integer,Integer>> acceptedActions) {
    assert actionNames.size() == acceptedActions.size();
    assert actionNames.size() == actionKeyStrokes.size();
    
    _completeJavaAPICheckbox.setSelected(DrJava.getConfig().getSetting(OptionConstants.DIALOG_COMPLETE_JAVAAPI));
    _completeJavaAPICheckbox.setEnabled(true);
    
    new Thread() {
      public void run() {
        List<OpenDefinitionsDocument> docs = _mainFrame.getModel().getOpenDefinitionsDocuments();
        if ((docs == null) || (docs.size() == 0)) {
          Utilities.invokeAndWait(canceledAction);
          return; // do nothing
        }
        
        AutoCompletePopupEntry currentEntry = null;
        _docEntries.clear();
        for(OpenDefinitionsDocument d: docs) {
          if (d.isUntitled()) continue;
          String str = d.toString();
          if (str.lastIndexOf('.')>=0) {
            str = str.substring(0, str.lastIndexOf('.'));
          }
          GoToFileListEntry entry = new GoToFileListEntry(d, str);
          if (d.equals(_mainFrame.getModel().getActiveDocument())) currentEntry = entry;
          _docEntries.add(entry);
        }
        
        if (DrJava.getConfig().getSetting(OptionConstants.DIALOG_COMPLETE_JAVAAPI)) {
          addJavaAPI();
        }
        
        final PredictiveInputModel<AutoCompletePopupEntry> pim = 
          new PredictiveInputModel<AutoCompletePopupEntry>(true, new PrefixStrategy<AutoCompletePopupEntry>(), _allEntries);
        String mask = "";
        String s = initial;
        
        // check that we're at the end of a word
        if ((loc<s.length()) && (!Character.isWhitespace(s.charAt(loc))) &&
            ("()[]{}<>.,:;/*+-!~&|%".indexOf(s.charAt(loc)) == -1)) {
          // TODO: what??
          Utilities.invokeAndWait(canceledAction);
          return;
        }
        
        // find start
        int start = loc;
        while(start > 0) {
          if (!Character.isJavaIdentifierPart(s.charAt(start-1))) { break; }
          --start;
        }
        while((start<s.length()) && (!Character.isJavaIdentifierStart(s.charAt(start))) && (start < loc)) {
          ++start;
        }
        
        int end = loc-1;
        
        if ((start>=0) && (end < s.length())) {
          mask = s.substring(start, end + 1);
          pim.setMask(mask);
        }
        
        if ((pim.getMatchingItems().size() == 1) && (oneMatchActionIndex >= 0)) {
          if (pim.getCurrentItem() != null) {
            // exactly one match, auto-complete
            final int finalStart = start;
            Utilities.invokeAndWait(new Runnable() {
              public void run() {
                Iterator<Runnable3<AutoCompletePopupEntry,Integer,Integer>> actionIt =
                  acceptedActions.iterator();
                Runnable3<AutoCompletePopupEntry,Integer,Integer> action;
                int i = oneMatchActionIndex;
                do {
                  action = actionIt.next();
                } while(i<0);
                action.run(pim.getCurrentItem(), finalStart, loc);
              }
            });
            return;
          }
        }
        
        // not exactly one match
        pim.setMask(mask);
        if (pim.getMatchingItems().size() == 0) {
          // if there are no matches, shorten the mask until there is at least one
          mask = pim.getMask();
          while(mask.length() > 0) {
            mask = mask.substring(0, mask.length() - 1);
            pim.setMask(mask);
            if (pim.getMatchingItems().size() > 0) { break; }
          }
        }       
        final PredictiveInputFrame<AutoCompletePopupEntry> completeWordDialog = 
          createCompleteWordDialog(title, start, loc, actionNames, actionKeyStrokes,
                                   canceledAction, acceptedActions);
        final AutoCompletePopupEntry finalCurrentEntry = currentEntry;
        Utilities.invokeLater(new Runnable() {
          public void run() {
            completeWordDialog.setModel(true, pim); // ignore case
            completeWordDialog.selectStrategy();
            if (finalCurrentEntry != null) {
              completeWordDialog.setCurrentItem(finalCurrentEntry);
            }
            completeWordDialog.setLocationRelativeTo(parent);
            
            if (_lastState != null) {
              completeWordDialog.setFrameState(_lastState);
            }
            
            completeWordDialog.setVisible(true);
          }
        });
      }
    }.start();
  }
  
  /** Returns the last state of the frame, i.e. the location and dimension.
   *  @return frame state
   */
  public FrameState getFrameState() { return _lastState; }
  
  /** Sets state of the frame, i.e. the location and dimension of the frame for the next use.
   *  @param ds  State to update to, or {@code null} to reset
   */
  public void setFrameState(FrameState ds) {
    _lastState = ds;
  }  
  
  /** Sets state of the frame, i.e. the location and dimension of the frame for the next use.
   *  @param s  State to update to, or {@code null} to reset
   */
  public void setFrameState(String s) {
    try { _lastState = new FrameState(s); }
    catch(IllegalArgumentException e) { _lastState = null; }
  }
  
  protected PredictiveInputFrame<AutoCompletePopupEntry>
    createCompleteWordDialog(final String title,
                             final int start,
                             final int loc,
                             final SizedIterable<String> actionNames,
                             final SizedIterable<KeyStroke> actionKeyStrokes,
                             final Runnable canceledAction,
                             final SizedIterable<Runnable3<AutoCompletePopupEntry,Integer,Integer>> acceptedActions) {
    final SimpleBox<PredictiveInputFrame<AutoCompletePopupEntry>> dialogThunk =
      new SimpleBox<PredictiveInputFrame<AutoCompletePopupEntry>>();
    // checkbox whether Java API classes should be completed as well
    _completeJavaAPICheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String curMask = dialogThunk.value().getMask();
        DrJava.getConfig().setSetting(OptionConstants.DIALOG_COMPLETE_JAVAAPI, _completeJavaAPICheckbox.isSelected());
        if (_completeJavaAPICheckbox.isSelected()) addJavaAPI(); else removeJavaAPI();
        dialogThunk.value().setItems(true,_allEntries);
        dialogThunk.value().setMask(curMask);
        dialogThunk.value().resetFocus();
      }
    });
    PlatformFactory.ONLY.setMnemonic(_completeJavaAPICheckbox,'j');
    PredictiveInputFrame.InfoSupplier<AutoCompletePopupEntry> info = 
      new PredictiveInputFrame.InfoSupplier<AutoCompletePopupEntry>() {
      public String value(AutoCompletePopupEntry entry) {
        // show full class name as information
        StringBuilder sb = new StringBuilder();
        sb.append(entry.getFullPackage());
        sb.append(entry.getClassName());
        return sb.toString();
      }
    };
    
    ArrayList<PredictiveInputFrame.CloseAction<AutoCompletePopupEntry>> actions
      = new ArrayList<PredictiveInputFrame.CloseAction<AutoCompletePopupEntry>>();

    Iterator<String> nameIt = actionNames.iterator();
    Iterator<Runnable3<AutoCompletePopupEntry,Integer,Integer>> actionIt =
      acceptedActions.iterator();
    Iterator<KeyStroke> ksIt = actionKeyStrokes.iterator();
    for(int i=0; i<acceptedActions.size(); ++i) {
      final int acceptedActionIndex = i;
      final String name = nameIt.next();
      final Runnable3<AutoCompletePopupEntry,Integer,Integer> runnable = actionIt.next();
      final KeyStroke ks = ksIt.next();
      
      PredictiveInputFrame.CloseAction<AutoCompletePopupEntry> okAction =
        new PredictiveInputFrame.CloseAction<AutoCompletePopupEntry>() {
        public String getName() { return name; }
        public KeyStroke getKeyStroke() { return ks; }
        public String getToolTipText() { return "Complete the identifier"; }
        public Object value(final PredictiveInputFrame<AutoCompletePopupEntry> p) {
          _lastState = p.getFrameState();
          if (p.getItem() != null) {
            Utilities.invokeAndWait(new Runnable() {
              public void run() {
                runnable.run(p.getItem(), start, loc);
              }
            });
          }
          else {
            Utilities.invokeAndWait(canceledAction);
          }
          return null;
        }
      };
      actions.add(okAction);
    }

    PredictiveInputFrame.CloseAction<AutoCompletePopupEntry> cancelAction = 
      new PredictiveInputFrame.CloseAction<AutoCompletePopupEntry>() {
      public String getName() { return "Cancel"; }
      public KeyStroke getKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); }
      public String getToolTipText() { return null; }
      public Object value(PredictiveInputFrame<AutoCompletePopupEntry> p) {
        _lastState = p.getFrameState();
        Utilities.invokeAndWait(canceledAction);
        return null;
      }
    };    
    actions.add(cancelAction);

    // Note: PredictiveInputModel.* is statically imported
    ArrayList<MatchingStrategy<AutoCompletePopupEntry>> strategies =
      new ArrayList<MatchingStrategy<AutoCompletePopupEntry>>();
    strategies.add(new FragmentStrategy<AutoCompletePopupEntry>());
    strategies.add(new PrefixStrategy<AutoCompletePopupEntry>());
    strategies.add(new RegExStrategy<AutoCompletePopupEntry>());
    
    GoToFileListEntry entry = new GoToFileListEntry(new DummyOpenDefDoc() {
      public String getPackageNameFromDocument() { return ""; }
    }, "dummyComplete");
    dialogThunk.set(new PredictiveInputFrame<AutoCompletePopupEntry>(null,
                                                                     title,
                                                                     true, // force
                                                                     true, // ignore case
                                                                     info,
                                                                     strategies,
                                                                     actions,
                                                                     actions.size()-1, // cancel is last
                                                                     entry) {
      protected JComponent[] makeOptions() {
        return new JComponent[] { _completeJavaAPICheckbox };
      }
    });
    dialogThunk.value().setSize(dialogThunk.value().getSize().width, 500);
    dialogThunk.value().setLocationRelativeTo(_mainFrame);
    return dialogThunk.value();
  }
  
  private void addJavaAPI() {
    Set<JavaAPIListEntry> apiSet = _mainFrame.getJavaAPISet();
    if (apiSet == null) {
      DrJava.getConfig().setSetting(OptionConstants.DIALOG_COMPLETE_JAVAAPI, Boolean.FALSE);
      _completeJavaAPICheckbox.setSelected(false);
      _completeJavaAPICheckbox.setEnabled(false);
    }
    else {
      _apiEntries.clear();
      _apiEntries.addAll(apiSet);
    }
  }
  
  private void removeJavaAPI() {
    _apiEntries.clear();
  }
}
