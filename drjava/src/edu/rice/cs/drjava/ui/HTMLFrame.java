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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.text.html.*;
import java.awt.event.*;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.BorderlessScrollPane;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.Utilities;

/** The frame for displaying the HTML help files.
  * @version $Id: HTMLFrame.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class HTMLFrame extends SwingFrame {
  
  private static final int FRAME_WIDTH = 750;
  private static final int FRAME_HEIGHT = 600;
  private static final int LEFT_PANEL_WIDTH = 250;
  private JEditorPane _mainDocPane;
  private JScrollPane _mainScroll;
  private JSplitPane _splitPane;
  private JPanel _splitPaneHolder;
  private JEditorPane _contentsDocPane;
  private JPanel _closePanel;
  private JButton _closeButton;
  private JButton _backButton;
  private JButton _forwardButton;
  protected URL _baseURL;
  private ArrayList<HyperlinkListener> _hyperlinkListeners;
  private boolean _linkError;
  private URL _lastURL;
  
  private JPanel _navPane;
  
  protected HistoryList _history;
  
  private HyperlinkListener _resetListener = new HyperlinkListener() {
    public void hyperlinkUpdate(HyperlinkEvent e) {
      if (_linkError && e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        _resetMainPane();
      }
    }
  };
  
  protected static class HistoryList {
    private HistoryList next = null;
    private final HistoryList prev;
    protected final URL contents;
    private HistoryList(URL contents) {
      this.contents = contents;
      this.prev = null;
    }
    private HistoryList(URL contents, HistoryList prev) {
      this.contents = contents;
      this.prev = prev;
      prev.next = this;
    }
  }
  
  public static abstract class ResourceAction extends AbstractAction {
    public ResourceAction(String name, String iconName) {
      super(name,MainFrame.getIcon(iconName));
    }
  }
  
  private static abstract class ConsolidatedAction extends ResourceAction {
    private ConsolidatedAction(String name) {
      super(name,name + "16.gif");
    }
  }
  
  private Action _forwardAction = new ConsolidatedAction("Forward") {
    public void actionPerformed(ActionEvent e) {
      _history = _history.next;
      
      // user is always allowed to move back after a forward.
      _backAction.setEnabled(true);
      
      if (_history.next == null) {
        // no more forwards after this
        _forwardAction.setEnabled(false);
      }
      _displayPage(_history.contents);
    }
  };
  
  private Action _backAction = new ConsolidatedAction("Back") {
    public void actionPerformed(ActionEvent e) {
      _history = _history.prev;
      
      // user is always allowed to move forward after backing up
      _forwardAction.setEnabled(true);
      
      if (_history.prev == null) {
        // no more backing up
        _backAction.setEnabled(false);
      }
      _displayPage(_history.contents);
    }
  };
  
  private Action _closeAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent e) {
      HTMLFrame.this.setVisible(false);
    }
  };
  
  private static JButton makeButton(Action a, int horTextPos, int left, int right) {
    JButton j = new JButton(a);
    j.setHorizontalTextPosition(horTextPos);
    j.setVerticalTextPosition(JButton.CENTER);
    //Insets i = j.getMargin();
    //j.setMargin(new Insets(i.top,left,i.bottom,right));
    j.setMargin(new Insets(3,left+3,3,right+3));
    return j;
  }
  
  public void addHyperlinkListener(HyperlinkListener linkListener) {
    _hyperlinkListeners.add(linkListener);
    _contentsDocPane.addHyperlinkListener(linkListener);
    _mainDocPane.addHyperlinkListener(linkListener);
  }
  
  /** Sets up the frame and displays it. */
  public HTMLFrame(String frameName, URL introUrl, URL indexUrl, String iconString) {
    this(frameName, introUrl, indexUrl, iconString, null);
  }
  
  /** Sets up the frame and displays it. */
  public HTMLFrame(String frameName, URL introUrl, URL indexUrl, String iconString, File baseDir) {
    super(frameName);
    
    _contentsDocPane = new JEditorPane();
    _contentsDocPane.setEditable(false);
    JScrollPane contentsScroll = new BorderlessScrollPane(_contentsDocPane);
    
    _mainDocPane = new JEditorPane();
    _mainDocPane.setEditable(false);
    _mainScroll = new BorderlessScrollPane(_mainDocPane);
    
    _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, contentsScroll, _mainScroll);
    _splitPane.setDividerLocation(LEFT_PANEL_WIDTH);
    _splitPaneHolder = new JPanel(new GridLayout(1,1));
    _splitPaneHolder.setBorder(new EmptyBorder(0,5,0,5));
    _splitPaneHolder.add(_splitPane);
    // _splitPane.setBorder(new CompoundBorder(new EmptyBorder(0,5,0,5),_splitPane.getBorder()));
    _closeButton = new JButton(_closeAction);
    _backButton = makeButton(_backAction,JButton.RIGHT,0,3);
    _forwardButton = makeButton(_forwardAction,JButton.LEFT,3,0);
    _backAction.setEnabled(false);
    _forwardAction.setEnabled(false);
    _closePanel = new JPanel(new BorderLayout());
    _closePanel.add(_closeButton, BorderLayout.EAST);
    _closePanel.setBorder(new EmptyBorder(5,5,5,5)); // padding
    _navPane = new JPanel();
    _navPane.setBackground(new Color(0xCCCCFF));
    _navPane.setLayout(new BoxLayout(_navPane,BoxLayout.X_AXIS));
    JLabel icon = new JLabel(MainFrame.getIcon(iconString));
    _navPane.add(icon);
    _navPane.add(Box.createHorizontalStrut(8));
    _navPane.add(Box.createHorizontalGlue());
    _navPane.add(_backButton);
    _navPane.add(Box.createHorizontalStrut(8));
    _navPane.add(_forwardButton);
    _navPane.add(Box.createHorizontalStrut(3));
    _navPane.setBorder(new EmptyBorder(0,0,0,5));
    JPanel navContainer = new JPanel(new GridLayout(1,1));
    navContainer.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5), new EtchedBorder()));
    //new BevelBorder(BevelBorder.LOWERED)));
    navContainer.add(_navPane);
    Container cp = getContentPane();
    cp.setLayout(new BorderLayout());
    cp.add(navContainer, BorderLayout.NORTH);
    cp.add(_splitPaneHolder, BorderLayout.CENTER);
    cp.add(_closePanel, BorderLayout.SOUTH);
    
    _linkError = false;
    _hyperlinkListeners = new ArrayList<HyperlinkListener>();
    _hyperlinkListeners.add(_resetListener);
    _mainDocPane.addHyperlinkListener(_resetListener);
    
    if (baseDir == null) _baseURL = null;
    else
      try { _baseURL = FileOps.toURL(baseDir); }
    catch(MalformedURLException ex) {
      throw new UnexpectedException(ex);
    }
    
    // Load contents page
    if (indexUrl == null) _displayContentsError(null);
    else
      try {
      _contentsDocPane.setPage(indexUrl);
      if (_baseURL != null) ((HTMLDocument)_contentsDocPane.getDocument()).setBase(_baseURL);
    }
    catch (IOException ioe) {
      // Show some error page?
      _displayContentsError(indexUrl, ioe);
    }
    
    if (introUrl == null) _displayMainError(null);
    else {
      _history = new HistoryList(introUrl);
      _displayPage(introUrl);
      _displayPage(introUrl);
    }
    
    // Set all dimensions ----
    setSize(FRAME_WIDTH, FRAME_HEIGHT);
    Utilities.setPopupLoc(this, null);
    
    initDone(); // call mandated by SwingFrame contract
  }
  
  /** Hides the navigation panel on the left.  Cannot currently be undone. */
  protected void _hideNavigationPane() {
    _splitPaneHolder.remove(_splitPane);
    _splitPaneHolder.add(_mainScroll);
  }
  
  private void _resetMainPane() {
    _linkError = false;
    
    _mainDocPane = new JEditorPane();
    _mainDocPane.setEditable(false);
    for (int i = 0; i < _hyperlinkListeners.size(); i++) {
      _mainDocPane.addHyperlinkListener(_hyperlinkListeners.get(i));
    }
    _displayPage(_lastURL);
    
    _splitPane.setRightComponent(new BorderlessScrollPane(_mainDocPane));
    _splitPane.setDividerLocation(LEFT_PANEL_WIDTH);
  }
  
  /** Displays the given URL in the main pane. Changed to private, because of history system.
    * @param url URL to display
    */
  private void _displayPage(URL url) {
    if (url == null) return;
    try {
      _mainDocPane.setPage(url);
      if (_baseURL != null) {
        ((HTMLDocument)_contentsDocPane.getDocument()).setBase(_baseURL);
      }
      _lastURL = url;
    }
    catch (IOException ioe) {
      String path = url.getPath();
      try {
        URL newURL = new URL(_baseURL + path);
        _mainDocPane.setPage(newURL);
        if (_baseURL != null) {
          ((HTMLDocument)_contentsDocPane.getDocument()).setBase(_baseURL);
        }
        _lastURL = newURL;
      }
      catch (IOException ioe2) {
        // Show some error page?
        _displayMainError(url, ioe2);
        //System.err.println("couldn't find url: " + url);
      }
    }
  }
  
  /** Prints an error indicating that the HTML file to load in the main pane could not be found. */
  private void _displayMainError(URL url) {
    if (!_linkError) {
      _linkError = true;
      _mainDocPane.setText(getErrorText(url));
    }
    else _resetMainPane();
  }
  
  /** Prints an error indicating that the HTML file to load in the main pane could not be found
    */
  private void _displayMainError(URL url, Exception ex) {
    if (!_linkError) {
      _linkError = true;
      _mainDocPane.setText(getErrorText(url) + "\n" + ex);
    }
    else _resetMainPane();
  }
  
  /** Prints an error indicating that the HTML file to load in the contentes pane
   * could not be found
   */
  private void _displayContentsError(URL url) {
    _contentsDocPane.setText(getErrorText(url));
  }
  
  /** Prints an error indicating that the HTML file to load in the contentes pane could not be found. */
  private void _displayContentsError(URL url, Exception ex) {
    _contentsDocPane.setText(getErrorText(url) + "\n" + ex);
  }
  
  /** This method returns the error text to display when something goes wrong. */
  protected String getErrorText(URL url) {
    return "Could not load the specified URL: " + url;
  }
  
  public void jumpTo(URL url) {
    _history = new HistoryList(url,_history); // current history is prev for this node
    
    _backAction.setEnabled(true); // now we can back up.
    _forwardAction.setEnabled(false); // can't go any more forward
    // (any applicable previous forward info is lost) because you nuked the forward list
    _displayPage(url);
  }
}
