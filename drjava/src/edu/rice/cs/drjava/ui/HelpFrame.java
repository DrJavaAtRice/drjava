/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;

/**
 * The frame for displaying the HTML help files.
 * @version $Id$
 */ 
public class HelpFrame extends JFrame {
  
  private static final int FRAME_WIDTH = 750;
  private static final int FRAME_HEIGHT = 600;
  private static final int LEFT_PANEL_WIDTH = 250;
  private static final String HELP_PATH = "/edu/rice/cs/drjava/docs/user/";
  private static final String CONTENTS_PAGE = "index.html";
  private static final String HOME_PAGE = "intro.html";
  private static final URL INTRO_URL = 
    HelpFrame.class.getResource(HELP_PATH + HOME_PAGE);
  private JEditorPane _mainDocPane;
  private JSplitPane _splitPane;
  private JEditorPane _contentsDocPane;
  private JPanel _closePanel;
  private JButton _closeButton;
  private JButton _backButton;
  private JButton _forwardButton;
  
  private JPanel _navPane;

  private HistoryList _history;
  
  private static class HistoryList {
    private HistoryList next = null;
    private final HistoryList prev;
    private final URL contents;
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
      super(name,name+"16.gif");
    }
  }
  
  private Action _forwardAction = new ConsolidatedAction("Forward") {
    public void actionPerformed(ActionEvent e) {
      _history = _history.next;

      // user is always allowed to move back after a forward.
      _backAction.setEnabled(true); 

      if(_history.next == null) {
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
      
      if(_history.prev == null) {
        // no more backing up
        _backAction.setEnabled(false);
      }
      _displayPage(_history.contents);
    }
  };
  
  private Action _closeAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent e) {        
      HelpFrame.this.hide();
    }
  };
 
  private static JButton makeButton(Action a, int horTextPos,
                                    int left, int right) {
    JButton j = new JButton(a);
    j.setHorizontalTextPosition(horTextPos);
    j.setVerticalTextPosition(JButton.CENTER);
    //Insets i = j.getMargin();
    //j.setMargin(new Insets(i.top,left,i.bottom,right));
    j.setMargin(new Insets(3,left+3,3,right+3));
    return j;
  }
  
  /**
   * Sets up the frame and displays it.
   */
  public HelpFrame() {
    super("Help on using DrJava");
    
    _contentsDocPane = new JEditorPane();
    _contentsDocPane.setEditable(false);
    _contentsDocPane.addHyperlinkListener(_linkListener);
    JScrollPane contentsScroll = new BorderlessScrollPane(_contentsDocPane);
    
    _mainDocPane = new JEditorPane();
    _mainDocPane.setEditable(false);
    _mainDocPane.addHyperlinkListener(_linkListener);
    JScrollPane mainScroll = new BorderlessScrollPane(_mainDocPane);
    
    _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                true,
                                contentsScroll, 
                                mainScroll);
    _splitPane.setDividerLocation(LEFT_PANEL_WIDTH);
    JPanel tempPanel = new JPanel(new GridLayout(1,1));
    tempPanel.setBorder(new EmptyBorder(0,5,0,5));
    tempPanel.add(_splitPane);
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
    JLabel icon = new JLabel(MainFrame.getIcon("DrJavaHelp.gif"));
    _navPane.add(icon);
    _navPane.add(Box.createHorizontalStrut(8));
    _navPane.add(Box.createHorizontalGlue());
    _navPane.add(_backButton);
    _navPane.add(Box.createHorizontalStrut(8));
    _navPane.add(_forwardButton);
    _navPane.add(Box.createHorizontalStrut(3));
    _navPane.setBorder(new EmptyBorder(5,5,5,5));
    JPanel navContainer = new JPanel(new GridLayout(1,1));
    navContainer.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5),
                                              new EtchedBorder()));
                                              //new BevelBorder(BevelBorder.LOWERED)));
    navContainer.add(_navPane);
    Container cp = getContentPane();
    cp.setLayout(new BorderLayout());
    cp.add(navContainer, BorderLayout.NORTH);
    cp.add(tempPanel, BorderLayout.CENTER);
    cp.add(_closePanel, BorderLayout.SOUTH);
    
    // Load contents page
    URL indexUrl = HelpFrame.class.getResource(HELP_PATH + CONTENTS_PAGE);
    if (indexUrl != null) {
      try {
        _contentsDocPane.setPage(indexUrl);
      }
      catch (IOException ioe) {
        // Show some error page?
        _displayError();
      }
    } else {
      _displayError();
    }

    if (INTRO_URL != null) {
      _history = new HistoryList(INTRO_URL);
      _displayPage(INTRO_URL);
    } else {
      _displayError();
    }
    
    // Set all dimensions ----
    setSize( FRAME_WIDTH, FRAME_HEIGHT);
    // suggested from zaq@nosi.com, to keep the frame on the screen!
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = this.getSize();

    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }

    this.setSize(frameSize);
    this.setLocation((screenSize.width - frameSize.width) / 2,
                     (screenSize.height - frameSize.height) / 2);
  }

  /**
   * Displays the given URL in the main pane.
   * changed to private, because of history system.
   * @param url URL to display
   */
  public void _displayPage(URL url) {
    try {
      _mainDocPane.setPage(url);
    }
    catch (IOException ioe) {
      // Show some error page?
      _displayError();
      //System.err.println("couldn't find url: " + url);
    }
  }
  
  /**
   * Prints an error indicating that the help files couldn't be found.
   */
  private void _displayError() {
    // The help files are made available by running "ant docs"
    String errorText = "The Help files are currently unavailable.";
    if (CodeStatus.DEVELOPMENT) {  // don't show this message in stable
      errorText += "\n\nTo generate the help files, run the \"ant docs\" target" +
        " after compiling DrJava.";
    }
    _mainDocPane.setText(errorText);
  }
  
  public void jumpTo(URL url) {
    _history = new HistoryList(url,_history); // current history is prev for this node
    
    _backAction.setEnabled(true); // now we can back up.
    _forwardAction.setEnabled(false); // can't go any more forward
    // (any applicable previous forward info is lost) because you nuked the forward list
    _displayPage(url);
  }
  
  /**
   * Shows the page selected by the hyperlink event.
   * (theo) changed to anonymous inner class for encapsulation purposes
   */
  private final HyperlinkListener _linkListener = new HyperlinkListener() {
    public void hyperlinkUpdate(HyperlinkEvent event){
      if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        // Only follow links within the documentation
        URL url = event.getURL();
        String protocol = url.getProtocol();
        
        if ((!"file".equals(protocol)) && (!"jar".equals(protocol))) {
          return; // we only handle file/jar protocols
        }
        
        // perform path testing
        String path = url.getPath();
        
        if(path.indexOf(HELP_PATH+CONTENTS_PAGE) >= 0) {
          try {
            url = new URL(url,HOME_PAGE); // redirect to home, not contents
          } catch(MalformedURLException murle) {
          }
        } else if(path.indexOf(HELP_PATH) < 0) {
          // not anywhere in the help section
          return;
        }
        if(url.sameFile(_history.contents)) {
          return; // we're already here!
        }
        jumpTo(url);
      }
    }
  };
}

