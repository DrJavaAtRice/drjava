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

import java.awt.event.*;
import java.awt.*;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;

import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.config.OptionParser;
import edu.rice.cs.drjava.config.StringOption;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.ui.config.*;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.concurrent.CompletionMonitor;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.Utilities;

import edu.rice.cs.drjava.ui.predictive.*;
import static edu.rice.cs.drjava.ui.predictive.PredictiveInputModel.*;

/** A frame for setting Project Advanced Preferences */
public class ProjectAdvancedPropertiesFrame extends SwingFrame {

  private static final int FRAME_WIDTH = 503;
  private static final int FRAME_HEIGHT = 500;

  private MainFrame _mainFrame;
  private SwingFrame _parentFrame;
  private SingleDisplayModel _model; 
  private VectorOptionComponent<PreferencesRecord> _preferencesList;
  private volatile Map<OptionParser<?>,String> _unmodifiedStoredPreferences = new HashMap<OptionParser<?>,String>();
  
  protected static class PreferencesRecord implements Comparable<PreferencesRecord> {
    public final OptionParser<?> option;
    public final String shortDesc;
    public final String longDesc;
    public PreferencesRecord(OptionParser<?> o, String s, String l) {
      option = o;
      shortDesc = s;
      longDesc = StringOps.removeHTML(l);
    }
    public int compareTo(PreferencesRecord other) {
      return option.getName().compareTo(other.option.getName());
    }
    public boolean equals(Object other) {
      if (other == null || ! (other instanceof PreferencesRecord)) return false;
      PreferencesRecord o = (PreferencesRecord) other;
      return option.getName().equals(o.option.getName());
    }
    public int hashCode() { return option.getName().hashCode(); }
    public String toString() { return shortDesc; }
  }
  
  public static final PredictiveInputModel<PreferencesRecord> STORED_PROPERTIES_PIM =
    new PredictiveInputModel<PreferencesRecord>(true, 
                                                new PredictiveInputModel.FragmentStrategy<PreferencesRecord>(), 
                                                new ArrayList<PreferencesRecord>());

  static {
    ArrayList<PreferencesRecord> list = new ArrayList<PreferencesRecord>();
    for(Map.Entry<OptionParser<?>,String> e: edu.rice.cs.drjava.ui.config.ConfigDescriptions.CONFIG_DESCRIPTIONS.entrySet()) {
      OptionParser<?> o = e.getKey();
      String shortDesc = e.getValue();
      String longDesc = edu.rice.cs.drjava.ui.config.ConfigDescriptions.CONFIG_LONG_DESCRIPTIONS.get(o);
      list.add(new PreferencesRecord(o, shortDesc, longDesc));
    }
    STORED_PROPERTIES_PIM.setItems(list.toArray(new PreferencesRecord[list.size()]));
  }
  
  private final JButton _okButton;
  private final JButton _cancelButton;
  //  private JButton _saveSettingsButton;
  private JPanel _mainPanel;
  
  /** Constructs project properties frame for a new project and displays it.  Assumes that a project is active. */
  public ProjectAdvancedPropertiesFrame(MainFrame mf, SwingFrame parentFrame) {
    super("Advanced Project Properties");

    //  Utilities.show("ProjectPropertiesFrame(" + mf + ", " + projFile + ")");

    _parentFrame = parentFrame;
    _mainFrame = mf;
    _model = _mainFrame.getModel();
    _mainPanel= new JPanel();
    
    Action okAction = new AbstractAction("OK") {
      public void actionPerformed(ActionEvent e) {
        // Always apply and save settings
        boolean successful = true;
        ProjectAdvancedPropertiesFrame.this.setVisible(false);
      }
    };
    _okButton = new JButton(okAction);

    Action cancelAction = new AbstractAction("Cancel") {
      public void actionPerformed(ActionEvent e) { cancel(); }
    };
    _cancelButton = new JButton(cancelAction);
    
    init();
    initDone(); // call mandated by SwingFrame contract
  }

  /** Initializes the components in this frame. */
  private void init() {
    _setupPanel(_mainPanel);
    JScrollPane scrollPane = new JScrollPane(_mainPanel);
    Container cp = getContentPane();
    
    GridBagLayout cpLayout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    cp.setLayout(cpLayout);
    
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = GridBagConstraints.RELATIVE;
    c.weightx = 1.0;
    c.weighty = 1.0;
    cpLayout.setConstraints(scrollPane, c);
    cp.add(scrollPane);
    
    // Add buttons
    JPanel bottom = new JPanel();
    bottom.setBorder(new EmptyBorder(5,5,5,5));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
    bottom.add(_okButton);
    bottom.add(_cancelButton);
    bottom.add(Box.createHorizontalGlue());

    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.SOUTH;
    c.gridheight = GridBagConstraints.REMAINDER;
    c.weighty = 0.0;
    cpLayout.setConstraints(bottom, c);
    cp.add(bottom);

    // Set all dimensions ----
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    if (dim.width>FRAME_WIDTH) { dim.width = FRAME_WIDTH; }
    else { dim.width -= 80; }
    if (dim.height>FRAME_HEIGHT) { dim.height = FRAME_HEIGHT; }
    else { dim.height -= 80; }
    setSize(dim);
    Utilities.setPopupLoc(this, _parentFrame);
  }

  /** Resets the frame and hides it. */
  public void cancel() {
    reset();
    setVisible(false);
  }

  public void reset() {
    setPreferencesStoredInProject(_unmodifiedStoredPreferences);
  }
  
  public void reset(Map<OptionParser<?>,String> sp) {
    _unmodifiedStoredPreferences = new HashMap<OptionParser<?>,String>(sp);
    setPreferencesStoredInProject(_unmodifiedStoredPreferences);
  }
  
  @SuppressWarnings("unchecked")
  public Map<OptionParser<?>,String> getPreferencesStoredInProject() {
    Map<OptionParser<?>,String> sp = new HashMap<OptionParser<?>,String>();
    for(PreferencesRecord pr: _preferencesList.getValue()) {
      sp.put(pr.option, DrJava.getConfig().getOptionMap().getString(pr.option));
    }
    return sp;
  }

  @SuppressWarnings("unchecked")
  public void setPreferencesStoredInProject(Map<OptionParser<?>,String> sp) {
    ArrayList<PreferencesRecord> list = new ArrayList<PreferencesRecord>();
    for(OptionParser<?> o: sp.keySet()) {
      list.add(new PreferencesRecord
                 (o,
                  edu.rice.cs.drjava.ui.config.ConfigDescriptions.CONFIG_DESCRIPTIONS.get(o),
                  edu.rice.cs.drjava.ui.config.ConfigDescriptions.CONFIG_LONG_DESCRIPTIONS.get(o)));
    }
    _preferencesList.setValue(list);
  }
  
  private void _setupPanel(JPanel panel) {
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridbag);
    c.fill = GridBagConstraints.HORIZONTAL;
    Insets labelInsets = new Insets(5, 10, 0, 0);
    Insets compInsets  = new Insets(5, 5, 0, 10);

    c.weightx = 0.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = labelInsets;

    JLabel descriptionLabel = new JLabel("<html>The current values of the preferences listed here will be<br>"+
                                         "stored in the project file when the project is saved and<br>"+
                                         " restored when the project is loaded again.<br>"+
                                         "Note that the previous values of preferences stored in a project<br>"+
                                         "file will be overwritten when a project is loaded.<br>&nbsp;</html>");
    gridbag.setConstraints(descriptionLabel, c);
    panel.add(descriptionLabel);

    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    JLabel preferencesLabel = new JLabel("Stored Preferences");
    preferencesLabel.setToolTipText("<html>The list of preferences that are stored and restored with the project.</html>");
    gridbag.setConstraints(preferencesLabel, c);
    panel.add(preferencesLabel);

    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    Component preferencesComponent = _preferencesComponent();
    gridbag.setConstraints(preferencesComponent, c);
    panel.add(preferencesComponent);    

    c.weightx = 0.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = labelInsets;

    descriptionLabel = new JLabel("<html>To change the preferences, please use the 'Preferences' window<br>"+
                                  "in the 'Edit' menu.</html>");
    gridbag.setConstraints(descriptionLabel, c);
    panel.add(descriptionLabel);
//    
//    c.weightx = 0.0;
//    c.gridwidth = 1;
//    c.insets = labelInsets;
//
//    descriptionLabel = new JLabel("<html>&nbsp;</html>");
//    gridbag.setConstraints(descriptionLabel, c);
//    panel.add(descriptionLabel);
//    
//    c.weightx = 0.0;
//    c.gridwidth = GridBagConstraints.REMAINDER;
//    c.insets = labelInsets;
//
//    JButton prefWindowButton = new JButton("Preferences");
//    prefWindowButton.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        _mainFrame.removeModalWindowAdapter(ProjectAdvancedPropertiesFrame.this);
//        _mainFrame.editPreferences();
//      }
//    });
//    gridbag.setConstraints(prefWindowButton, c);
//    panel.add(prefWindowButton);    
  }
  
  public Component _preferencesComponent() {
    _preferencesList = new VectorOptionComponent<PreferencesRecord>
      (null, "Stored Preferences", this, new String[0],
       "The list of preferences that are stored and restored with the project.",false) {
      protected Action _getAddAction() {
        return new AbstractAction("Add") {
          public void actionPerformed(ActionEvent ae) {
            _mainFrame.removeModalWindowAdapter(ProjectAdvancedPropertiesFrame.this);
            chooseString();
          }
        };
      }
      public void chooseString() {
        new Thread() {
          public void run() {
            final CompletionMonitor cm = new CompletionMonitor();
            // predictive input frame for preferences
            PredictiveInputFrame.InfoSupplier<PreferencesRecord> info = 
              new PredictiveInputFrame.InfoSupplier<PreferencesRecord>() {
              public String value(PreferencesRecord entry) { // show full class name as information
                return entry.longDesc;
              }
            };
            PredictiveInputFrame.CloseAction<PreferencesRecord> okAction = 
              new PredictiveInputFrame.CloseAction<PreferencesRecord>() {
              public String getName() { return "OK"; }
              public KeyStroke getKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0); }
              public String getToolTipText() { return null; }
              public Object value(PredictiveInputFrame<PreferencesRecord> p) {
                if (p.getItem() != null) { // if an item was selected...
                  _addValue(p.getItem());
                }
                cm.signal();
                return null;
              }
            };
            PredictiveInputFrame.CloseAction<PreferencesRecord> cancelAction = 
              new PredictiveInputFrame.CloseAction<PreferencesRecord>() {
              public String getName() { return "Cancel"; }
              public KeyStroke getKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); }
              public String getToolTipText() { return null; }
              public Object value(PredictiveInputFrame<PreferencesRecord> p) {
                cm.signal();
                return null;
              }
            };
            
            ArrayList<MatchingStrategy<PreferencesRecord>> strategies =
              new ArrayList<MatchingStrategy<PreferencesRecord>>();
            strategies.add(new FragmentStrategy<PreferencesRecord>());
            strategies.add(new PrefixStrategy<PreferencesRecord>());
            strategies.add(new RegExStrategy<PreferencesRecord>());
            ArrayList<PredictiveInputFrame.CloseAction<PreferencesRecord>> actions
              = new ArrayList<PredictiveInputFrame.CloseAction<PreferencesRecord>>();
            actions.add(okAction);
            actions.add(cancelAction);
            final PredictiveInputFrame<PreferencesRecord> pif = new PredictiveInputFrame<PreferencesRecord>
              (ProjectAdvancedPropertiesFrame.this,
               "Preferences Stored with Project",
               true,
               true,
               info,
               strategies,
               actions, 1,
               new PreferencesRecord(new StringOption("dummy","dummy"), "dummy", "dummy"));
            pif.setModel(true, STORED_PROPERTIES_PIM);
            Utilities.invokeLater(new Runnable() {
              public void run() {
                pif.setVisible(true);
              }
            });
            cm.attemptEnsureSignaled();
            Utilities.invokeLater(new Runnable() {
              public void run() {
                _mainFrame.installModalWindowAdapter(ProjectAdvancedPropertiesFrame.this, LambdaUtil.NO_OP, CANCEL);
              }
            });
          }
        }.start();
      }
    };
    _preferencesList.setRows(15,15);
    return _preferencesList.getComponent();
  }

  /** Runnable that calls _cancel. */
  protected final Runnable1<WindowEvent> CANCEL = new Runnable1<WindowEvent>() {
    public void run(WindowEvent e) { cancel(); }
  };
  
  /** Validates before changing visibility.  Only runs in the event thread.
    * @param vis true if frame should be shown, false if it should be hidden.
    */
  public void setVisible(boolean vis) {
    assert EventQueue.isDispatchThread();
    validate();
    if (vis) {
      _mainFrame.hourglassOn();
      _mainFrame.installModalWindowAdapter(this, LambdaUtil.NO_OP, CANCEL);
      toFront();
    }
    else {
      _mainFrame.removeModalWindowAdapter(this);
      _mainFrame.hourglassOff();
      _parentFrame.toFront();
    }
    super.setVisible(vis);
  }
}
