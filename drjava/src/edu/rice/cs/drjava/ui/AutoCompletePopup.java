package edu.rice.cs.drjava.ui;

/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.Utilities;

import static edu.rice.cs.drjava.ui.MainFrameStatics.*;
import static edu.rice.cs.drjava.ui.predictive.PredictiveInputModel.*;

/** Autocomplete support.
  * @version $Id$
  */
public class AutoCompletePopup {
  JCheckBox _completeJavaAPICheckbox = new JCheckBox("Java API");
  /** Main frame. */
  final MainFrame _mainFrame;
    
  /** Constructor for an auto-complete popup that uses the MainFrame for information.
    * @param mf main frame of DrJava */
  public AutoCompletePopup(MainFrame mf) {
    _mainFrame = mf;
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
                   final Runnable4<String,String,Integer,Integer> acceptedAction) {
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
                   final SizedIterable<Runnable4<String,String,Integer,Integer>> acceptedActions) {
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
                   final SizedIterable<Runnable4<String,String,Integer,Integer>> acceptedActions) {
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
        
        ClassNameAndPackageEntry currentEntry = null;
        HashSet<ClassNameAndPackageEntry> set;
        if ((DrJava.getConfig().getSetting(OptionConstants.DIALOG_COMPLETE_SCAN_CLASS_FILES).booleanValue()) &&
            (_mainFrame.getCompleteClassSet().size() > 0)) {
          set = new HashSet<ClassNameAndPackageEntry>(_mainFrame.getCompleteClassSet());
        }
        else {
          set = new HashSet<ClassNameAndPackageEntry>(docs.size());
          for(OpenDefinitionsDocument d: docs) {
            if (d.isUntitled()) continue;
            String str = d.toString();
            if (str.lastIndexOf('.')>=0) {
              str = str.substring(0, str.lastIndexOf('.'));
            }
            GoToFileListEntry entry = new GoToFileListEntry(d, str);
            if (d.equals(_mainFrame.getModel().getActiveDocument())) currentEntry = entry;
            set.add(entry);
          }
        }
        
        if (DrJava.getConfig().getSetting(OptionConstants.DIALOG_COMPLETE_JAVAAPI)) {
          addJavaAPIToSet(set);
        }
        
        final PredictiveInputModel<ClassNameAndPackageEntry> pim = 
          new PredictiveInputModel<ClassNameAndPackageEntry>(true, new PrefixStrategy<ClassNameAndPackageEntry>(), set);
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
                Iterator<Runnable4<String,String,Integer,Integer>> actionIt =
                  acceptedActions.iterator();
                Runnable4<String,String,Integer,Integer> action;
                int i = oneMatchActionIndex;
                do {
                  action = actionIt.next();
                } while(i<0);
                action.run(pim.getCurrentItem().getClassName(),
                           pim.getCurrentItem().getFullPackage()+pim.getCurrentItem().getClassName(),
                           finalStart, loc);
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
        final PredictiveInputFrame<ClassNameAndPackageEntry> completeWordDialog = 
          createCompleteWordDialog(title, start, loc, actionNames, actionKeyStrokes,
                                   canceledAction, acceptedActions);
        final ClassNameAndPackageEntry finalCurrentEntry = currentEntry;
        Utilities.invokeLater(new Runnable() {
          public void run() {
            completeWordDialog.setModel(true, pim); // ignore case
            completeWordDialog.selectStrategy();
            if (finalCurrentEntry != null) {
              completeWordDialog.setCurrentItem(finalCurrentEntry);
            }
            completeWordDialog.setLocationRelativeTo(parent);
            completeWordDialog.setVisible(true);
          }
        });
      }
    }.start();
  }
  
  PredictiveInputFrame<ClassNameAndPackageEntry>
    createCompleteWordDialog(final String title,
                             final int start,
                             final int loc,
                             final SizedIterable<String> actionNames,
                             final SizedIterable<KeyStroke> actionKeyStrokes,
                             final Runnable canceledAction,
                             final SizedIterable<Runnable4<String,String,Integer,Integer>> acceptedActions) {
    final SimpleBox<PredictiveInputFrame<ClassNameAndPackageEntry>> dialogThunk =
      new SimpleBox<PredictiveInputFrame<ClassNameAndPackageEntry>>();
    // checkbox whether Java API classes should be completed as well
    _completeJavaAPICheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String curMask = dialogThunk.value().getMask();
        Set<ClassNameAndPackageEntry> s = new HashSet<ClassNameAndPackageEntry>(dialogThunk.value().getItems());
        if (_completeJavaAPICheckbox.isSelected()) {
          DrJava.getConfig().setSetting(OptionConstants.DIALOG_COMPLETE_JAVAAPI, Boolean.TRUE);
          addJavaAPIToSet(s);
          dialogThunk.value().setItems(true,s);
        }
        else {
          // unselected, remove Java API classes from list
          DrJava.getConfig().setSetting(OptionConstants.DIALOG_COMPLETE_JAVAAPI, Boolean.FALSE);
          removeJavaAPIFromSet(s);
          dialogThunk.value().setItems(true,s);
        }
        dialogThunk.value().setMask(curMask);
        dialogThunk.value().resetFocus();
      }
    });
    PlatformFactory.ONLY.setMnemonic(_completeJavaAPICheckbox,'j');
    PredictiveInputFrame.InfoSupplier<ClassNameAndPackageEntry> info = 
      new PredictiveInputFrame.InfoSupplier<ClassNameAndPackageEntry>() {
      public String value(ClassNameAndPackageEntry entry) {
        // show full class name as information
        StringBuilder sb = new StringBuilder();
        sb.append(entry.getFullPackage());
        sb.append(entry.getClassName());
        return sb.toString();
      }
    };
    
    List<PredictiveInputFrame.CloseAction<ClassNameAndPackageEntry>> actions
      = new ArrayList<PredictiveInputFrame.CloseAction<ClassNameAndPackageEntry>>();

    Iterator<String> nameIt = actionNames.iterator();
    Iterator<Runnable4<String,String,Integer,Integer>> actionIt =
      acceptedActions.iterator();
    Iterator<KeyStroke> ksIt = actionKeyStrokes.iterator();
    for(int i=0; i<acceptedActions.size(); ++i) {
      final int acceptedActionIndex = i;
      final String name = nameIt.next();
      final Runnable4<String,String,Integer,Integer> runnable = actionIt.next();
      final KeyStroke ks = ksIt.next();
      
      PredictiveInputFrame.CloseAction<ClassNameAndPackageEntry> okAction =
        new PredictiveInputFrame.CloseAction<ClassNameAndPackageEntry>() {
        public String getName() { return name; }
        public KeyStroke getKeyStroke() { return ks; }
        public String getToolTipText() { return "Complete the identifier"; }
        public Object value(final PredictiveInputFrame<ClassNameAndPackageEntry> p) {
          if (p.getItem() != null) {
            Utilities.invokeAndWait(new Runnable() {
              public void run() {
                runnable.run(p.getItem().getClassName(),
                             p.getItem().getFullPackage()+p.getItem().getClassName(),
                             start, loc);
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

    PredictiveInputFrame.CloseAction<ClassNameAndPackageEntry> cancelAction = 
      new PredictiveInputFrame.CloseAction<ClassNameAndPackageEntry>() {
      public String getName() { return "Cancel"; }
      public KeyStroke getKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); }
      public String getToolTipText() { return null; }
      public Object value(PredictiveInputFrame<ClassNameAndPackageEntry> p) {
        Utilities.invokeAndWait(canceledAction);
        return null;
      }
    };    
    actions.add(cancelAction);

    // Note: PredictiveInputModel.* is statically imported
    java.util.ArrayList<MatchingStrategy<ClassNameAndPackageEntry>> strategies =
      new java.util.ArrayList<MatchingStrategy<ClassNameAndPackageEntry>>();
    strategies.add(new FragmentStrategy<ClassNameAndPackageEntry>());
    strategies.add(new PrefixStrategy<ClassNameAndPackageEntry>());
    strategies.add(new RegExStrategy<ClassNameAndPackageEntry>());
    
    GoToFileListEntry entry = new GoToFileListEntry(new DummyOpenDefDoc() {
      public String getPackageNameFromDocument() { return ""; }
    }, "dummyComplete");
    dialogThunk.set(new PredictiveInputFrame<ClassNameAndPackageEntry>(null,
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
  
  private void addJavaAPIToSet(Set<ClassNameAndPackageEntry> s) {
    Set<JavaAPIListEntry> apiSet = _mainFrame.getJavaAPISet();
    if (apiSet == null) {
      DrJava.getConfig().setSetting(OptionConstants.DIALOG_COMPLETE_JAVAAPI, Boolean.FALSE);
      _completeJavaAPICheckbox.setSelected(false);
      _completeJavaAPICheckbox.setEnabled(false);
    }
    else {
      s.addAll(apiSet);
    }
  }
  
  private void removeJavaAPIFromSet(Set<ClassNameAndPackageEntry> s) {
    Set<JavaAPIListEntry> apiSet = _mainFrame.getJavaAPISet();
    if (apiSet == null) {
      DrJava.getConfig().setSetting(OptionConstants.DIALOG_COMPLETE_JAVAAPI, Boolean.FALSE);
      _completeJavaAPICheckbox.setSelected(false);
      _completeJavaAPICheckbox.setEnabled(false);
      Set<ClassNameAndPackageEntry> n = new HashSet<ClassNameAndPackageEntry>();
      for(ClassNameAndPackageEntry entry: s) {
        if (entry instanceof JavaAPIListEntry) { n.add(entry); }
      }
      s.removeAll(n);
    }
    else {
      for(JavaAPIListEntry entry: apiSet) { s.remove(entry); }
    }
  }
}
