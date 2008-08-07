/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.Version;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.platform.*;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.lambda.Box;
import edu.rice.cs.plt.lambda.SimpleBox;

/** Displays whether a new version of DrJava is available.
 *  @version $Id$
 */
public class NewVersionPopup extends JDialog {
  /** whether to keep displaying this dialog, and for which releases */
  private JComboBox _modeBox;
  /** the button that closes this window */
  private JButton _closeButton;
  /** the button that downloads the new version */
  private JButton _downloadButton;
  /** the parent frame */
  private MainFrame _mainFrame;
  /** the version information pane */
  private JOptionPane _versionPanel;
  /** the panel with the buttons and combobox */
  private JPanel _bottomPanel;
  /** the build time of this version */
  private static Date BUILD_TIME = Version.getBuildTime();
  /** the message for the user */
  private String[] _msg = null;
  /** the version string of the new version found, or "" */
  private String _newestVersionString = "";
  
  /** Creates a window to display whether a new version of DrJava is available. */
  public NewVersionPopup(MainFrame parent) {
    super(parent, "Check for New Version of DrJava");
    setResizable(false);
    
    _mainFrame = parent;
    _mainFrame.setPopupLoc(this);
    this.setSize(500,150);

    _modeBox = new JComboBox(OptionConstants.NEW_VERSION_NOTIFICATION_CHOICES.toArray());
    for(int i = 0; i < OptionConstants.NEW_VERSION_NOTIFICATION_CHOICES.size(); ++i) {
      if (DrJava.getConfig().getSetting(OptionConstants.NEW_VERSION_NOTIFICATION)
            .equals(OptionConstants.NEW_VERSION_NOTIFICATION_CHOICES.get(i))) {
        _modeBox.setSelectedIndex(i);
        break;
      }
    }
    _modeBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        DrJava.getConfig().setSetting(OptionConstants.NEW_VERSION_NOTIFICATION,
                                      OptionConstants.NEW_VERSION_NOTIFICATION_CHOICES.get(_modeBox.getSelectedIndex()));
        _msg = null;
        updateText();
      }
    });

    _downloadButton = new JButton(_downloadAction);
    _closeButton = new JButton(_closeAction);
    _downloadButton.setEnabled(false);

    _bottomPanel = new JPanel(new BorderLayout());
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(_downloadButton);
    buttonPanel.add(_closeButton);
    _bottomPanel.add(buttonPanel, BorderLayout.CENTER);
    JPanel comboPanel = new JPanel();
    comboPanel.add(new JLabel("Check for: "));
    comboPanel.add(_modeBox);
    _bottomPanel.add(comboPanel, BorderLayout.WEST);
    
    updateText();
  }
  
  private void updateText() {
    if (_msg!=null) {
      _versionPanel = new JOptionPane(_msg,JOptionPane.INFORMATION_MESSAGE,
                                      JOptionPane.DEFAULT_OPTION,null,
                                      new Object[0]);   
        
        JPanel cp = new JPanel(new BorderLayout(5,5));
        cp.setBorder(new EmptyBorder(5,5,5,5));
        setContentPane(cp);
        cp.add(_versionPanel, BorderLayout.CENTER);
        cp.add(_bottomPanel, BorderLayout.SOUTH);    
        getRootPane().setDefaultButton(_closeButton);
        setTitle("Check for New Version of DrJava");
        pack();
        return;
    }
    setTitle("Checking for new versions, please wait...");
    String[] msg = new String[] {"Checking drjava.org for new versions.", "Please wait..."};
    _versionPanel = new JOptionPane(msg,JOptionPane.INFORMATION_MESSAGE,
                                    JOptionPane.DEFAULT_OPTION,null,
                                    new Object[0]);   
    JPanel cp = new JPanel(new BorderLayout(5,5));
    cp.setBorder(new EmptyBorder(5,5,5,5));
    setContentPane(cp);
    cp.add(_versionPanel, BorderLayout.CENTER);
    cp.add(_bottomPanel, BorderLayout.SOUTH);    
    getRootPane().setDefaultButton(_closeButton);
    pack();
    Utilities.clearEventQueue();  // Why? In principle, its inclusion does not change the semantics of the program
    
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        _msg = getMessage(null);
        _versionPanel = new JOptionPane(_msg,JOptionPane.INFORMATION_MESSAGE,
                                        JOptionPane.DEFAULT_OPTION,null,
                                        new Object[0]);   
        
        JPanel cp = new JPanel(new BorderLayout(5,5));
        cp.setBorder(new EmptyBorder(5,5,5,5));
        setContentPane(cp);
        cp.add(_versionPanel, BorderLayout.CENTER);
        cp.add(_bottomPanel, BorderLayout.SOUTH);    
        getRootPane().setDefaultButton(_closeButton);
        setTitle("Check for New Version of DrJava");
        pack();
      }
    });
  }
  
  /* Close the window. */
  private Action _closeAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent e) { closeAction(); }
  };

  /** Close this window, but display the full DrJava Errors window. */
  private Action _downloadAction = new AbstractAction("Download") {
    public void actionPerformed(ActionEvent e) { downloadAction(); }
  };

  protected void closeAction() {
    NewVersionPopup.this.setVisible(false);
    NewVersionPopup.this.dispose();
  }
  protected void downloadAction() {
    closeAction();
    final String DRJAVA_FILES_PAGE = "http://sourceforge.net/project/showfiles.php?group_id=44253";
    final String LINK_PREFIX = "<a href=\"/project/showfiles.php?group_id=44253";
    final String LINK_SUFFIX = "\">";
    BufferedReader br = null;
    try {
      URL url = new URL(DRJAVA_FILES_PAGE);
      InputStream urls = url.openStream();
      InputStreamReader is = new InputStreamReader(urls);
      br = new BufferedReader(is);
      String line;
      int pos;
      // search for the link to the version
      while((line = br.readLine()) != null) {
        if ((pos = line.indexOf(_newestVersionString)) >= 0) {
          int prePos = line.indexOf(LINK_PREFIX);
          if ((prePos >= 0) && (prePos < pos)) {
            int suffixPos = line.indexOf(LINK_SUFFIX, prePos);
            if ((suffixPos >= 0) && (suffixPos + LINK_SUFFIX.length() == pos)) {
              String versionLink = 
                edu.rice.cs.plt.text.TextUtil.xmlUnescape(line.substring(prePos + LINK_PREFIX.length(), suffixPos));
              PlatformFactory.ONLY.openURL(new URL(DRJAVA_FILES_PAGE + versionLink));
              return;
            }
          }
        }
      };
    }
    catch(IOException e) { _openFileDownloadPage(DRJAVA_FILES_PAGE); }
    finally { // close open input stream
      try { if (br!=null) br.close(); }
      catch(IOException e) { /* ignore */ }
    }
  }
  
  /** Opens the specified page. */
  private void _openFileDownloadPage(String page) {
    try { PlatformFactory.ONLY.openURL(new URL(page)); }
    catch(Exception ex) { /* ignore */ }
  }
  
  /** Returns true if there is a new version available that matches the users criterion. */
  public boolean checkNewVersion() {
    Box<Boolean> availableRef = new SimpleBox<Boolean>(false);
    getMessage(availableRef);
    return availableRef.value();
  }
  
  @SuppressWarnings("fallthrough")
  protected String[] getMessage(Box<Boolean> availableRef) {
    Box<String> stableString = new SimpleBox<String>("");
    Box<String> betaString = new SimpleBox<String>("");
    Box<String> devString = new SimpleBox<String>("");
    Box<Date> stableTime = new SimpleBox<Date>(new Date(0));
    Box<Date> betaTime = new SimpleBox<Date>(new Date(0));
    Box<Date> devTime = new SimpleBox<Date>(new Date(0));
    boolean newVersion = false;
    _newestVersionString = "";
    if (availableRef!=null) { availableRef.set(false); }
    switch(_modeBox.getSelectedIndex()) {
      case 2:
        newVersion |= checkNewDevVersion(devString,devTime); // fall-through required, not a mistake
      case 1:
        newVersion |= checkNewBetaVersion(betaString,betaTime); // fall-through required, not a mistake
      case 0:
        newVersion |= checkNewStableVersion(stableString,stableTime);
        _downloadButton.setEnabled(newVersion);
        DrJava.getConfig().setSetting(OptionConstants.LAST_NEW_VERSION_NOTIFICATION, new Date().getTime());
        if (availableRef!=null) { availableRef.set(newVersion); }
        if (newVersion) {
          String newestType = "";
          if (stableTime.value().after(betaTime.value())) {
            // stable newer than beta
            if (stableTime.value().after(devTime.value())) {
              // stable newer than beta and dev
              _newestVersionString = stableString.value();
              newestType = "stable ";
            }
            else {
              // stable newer than beta, but dev is even newer
              _newestVersionString = devString.value();
              newestType = "development ";              
            }
          }
          else {
            // beta newer than stable
            if (betaTime.value().after(devTime.value())) {
              // beta newer than stable and dev
              _newestVersionString = betaString.value();
              newestType = "beta ";
            }
            else {
              // beta newer than stable, but dev is even newer
              _newestVersionString = devString.value();
              newestType = "development ";              
            }
          }
            
          return new String[] {
            "A new "+newestType+"version has been found.",
              "The new version is: "+_newestVersionString,
              "Do you want to download this new version?"};
        }
        else {
          if (availableRef!=null) { availableRef.set(false); }
          return new String[] {
            "No new version of DrJava has been found.", "You are using the newest version that matches your criterion."};
        }
      default:
        _downloadButton.setEnabled(false);
        return new String[] { "Checking for new versions has been disabled.", "You can change this setting below." };
    }
  }
  
  /** Return true if there is a stable release available that's newer than this version.
    * @param versionStringRef a reference that will be filled with the version string, or null if not desired
    * @param buildTimeRef a reference that will be filled with the build time, or null if not desired
    * @return true if newer stable version is available */
  public static boolean checkNewStableVersion(Box<String> versionStringRef,
                                              Box<Date> buildTimeRef) {
    try {
      Date newestTime = getBuildTime(new URL("http://www.drjava.org/LATEST_VERSION.TXT"), versionStringRef);
      if (newestTime==null) { return false; }
      if (buildTimeRef!=null) { buildTimeRef.set(newestTime); }
      return BUILD_TIME.before(newestTime);
    }
    catch(MalformedURLException e) { return false; }
  }
  /** Return true if there is a beta release available that's newer than this version.
    * @param versionStringRef a reference that will be filled with the version string, or null if not desired
    * @param buildTimeRef a reference that will be filled with the build time, or null if not desired
    * @return true if newer beta version is available */
  public static boolean checkNewBetaVersion(Box<String> versionStringRef,
                                            Box<Date> buildTimeRef) {
    try {
      Date newestTime = getBuildTime(new URL("http://www.drjava.org/LATEST_BETA_VERSION.TXT"), versionStringRef);
      if (newestTime==null) { return false; }
      if (buildTimeRef!=null) { buildTimeRef.set(newestTime); }
      return BUILD_TIME.before(newestTime);
    }
    catch(MalformedURLException e) { return false; }
  }
  /** Return true if there is a development release available that's newer than this version.
    * @param versionStringRef a reference that will be filled with the version string, or null if not desired
    * @param buildTimeRef a reference that will be filled with the build time, or null if not desired
    * @return true if newer development version is available */
  public static boolean checkNewDevVersion(Box<String> versionStringRef,
                                           Box<Date> buildTimeRef) {
    try {
      Date newestTime = getBuildTime(new URL("http://www.drjava.org/LATEST_DEV_VERSION.TXT"), versionStringRef);
      if (newestTime==null) { return false; }
      if (buildTimeRef!=null) { buildTimeRef.set(newestTime); }
      return BUILD_TIME.before(newestTime);
    }
    catch(MalformedURLException e) { return false; }
  }

  /** Returns the build time for the URL, or null if it could not be read. */
  public static Date getBuildTime(URL url) {
    return getBuildTime(url, null);
  }

  /** Returns the build time for the URL, or null if it could not be read.
    * @param url the URL that contains the version string
    * @param versionStringRef a reference that will be filled with the version string, or null if not desired
    * @return build time, or null if there was an error */
  public static Date getBuildTime(URL url, Box<String> versionStringRef) {
    try {
      InputStream urls = url.openStream();
      InputStreamReader is = null;
      BufferedReader br = null;
      is = new InputStreamReader(urls);
      br = new BufferedReader(is);
      String line = br.readLine();
      if (versionStringRef!=null) { versionStringRef.set(line); }
      br.close();
      
      // remove "drjava-" prefix
      final String DRJAVA_PREFIX = "drjava-";
      if (!line.startsWith(DRJAVA_PREFIX)) { return null; }
      line = line.substring(DRJAVA_PREFIX.length());
      // remove "stable-" prefix
      final String STABLE_PREFIX = "stable-";
      if (line.startsWith(STABLE_PREFIX)) { line = line.substring(STABLE_PREFIX.length()); }
      // remove "beta-" prefix
      final String BETA_PREFIX = "beta-";
      if (line.startsWith(BETA_PREFIX)) { line = line.substring(BETA_PREFIX.length()); }
      // if this version string uses the new format with the release at the end, remove it
      int releasePos = line.indexOf("-r");
      if (releasePos>=0) { line = line.substring(0, releasePos); }
      
      return new SimpleDateFormat("yyyyMMdd-HHmm z").parse(line + " GMT");
    }
    catch (Exception e) { // parse format or whatever problem
      return null;
    }
  }
  
  /** Runnable that calls _cancel. */
  protected final Runnable1<WindowEvent> CANCEL = new Runnable1<WindowEvent>() {
    public void run(WindowEvent e) { closeAction(); }
  };
  
  /** Toggle visibility of this frame. Warning, it behaves like a modal dialog. */
  public void setVisible(boolean vis) {
    assert EventQueue.isDispatchThread();
    validate();
    if (vis) {
      _mainFrame.hourglassOn();
      _mainFrame.installModalWindowAdapter(this, LambdaUtil.NO_OP, CANCEL);
    }
    else {
      _mainFrame.removeModalWindowAdapter(this);
      _mainFrame.hourglassOff();
      _mainFrame.toFront();
    }
    super.setVisible(vis);
  }
}