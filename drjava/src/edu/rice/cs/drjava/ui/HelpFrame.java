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
import java.awt.event.*;
import java.awt.*;

import java.net.URL;
import java.io.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;

/**
 * The frame for displaying the HTML help files.
 * @version $Id$
 */ 
public class HelpFrame extends JFrame implements HyperlinkListener {
  
  private static final int FRAME_WIDTH = 750;
  private static final int FRAME_HEIGHT = 600;
  private static final int LEFT_PANEL_WIDTH = 250;
  private static final String HELP_PATH = "/edu/rice/cs/drjava/docs/user/";
  private static final String CONTENTS_PAGE = "index.html";
  private static final String HOME_PAGE = "intro.html";
  
  private JEditorPane _mainDocPane;
  private JSplitPane _splitPane;
  private JEditorPane _contentsDocPane;
  private JPanel _closePanel;
  private JButton _closeButton;
  
  private Action _closeAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent e) {        
      HelpFrame.this.hide();
    }
  };
 
  /**
   * Sets up the frame and displays it.
   */
  public HelpFrame() {
    super("Help");
    
    _contentsDocPane = new JEditorPane();
    _contentsDocPane.setEditable(false);
    _contentsDocPane.addHyperlinkListener(this);
    JScrollPane contentsScroll = new JScrollPane(_contentsDocPane);
    
    _mainDocPane = new JEditorPane();
    _mainDocPane.setEditable(false);
    _mainDocPane.addHyperlinkListener(this);
    JScrollPane mainScroll = new JScrollPane(_mainDocPane);
    
    _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                true,
                                contentsScroll, 
                                mainScroll);
    _splitPane.setDividerLocation(LEFT_PANEL_WIDTH);
    
    _closeButton = new JButton(_closeAction);
    _closePanel = new JPanel(new BorderLayout());
    _closePanel.add(_closeButton, BorderLayout.EAST);
    
    Container cp = getContentPane();
    cp.setLayout(new BorderLayout());
    cp.add(_splitPane, BorderLayout.CENTER);
    cp.add(_closePanel, BorderLayout.SOUTH);
    
    // Load contents page
    URL indexUrl = this.getClass().getResource(HELP_PATH + CONTENTS_PAGE);
    if (indexUrl != null) {
      try {
        _contentsDocPane.setPage(indexUrl);
      }
      catch (IOException ioe) {
        // Show some error page?
        _displayError();
      }
    }
    else {
      _displayError();
    }
    URL introUrl = this.getClass().getResource(HELP_PATH + HOME_PAGE);
    if (introUrl != null) {
      displayPage(introUrl);
    }
    else {
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
   * @param url URL to display
   */
  public void displayPage(URL url) {
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
    if (CodeStatus.DEVELOPMENT) {
      errorText += "\n\nTo generate the help files, run the \"ant docs\" target" +
        " after compiling DrJava.";
    }
    _mainDocPane.setText(errorText);
  }
  
  /**
   * Shows the page selected by the hyperlink event.
   */
  public void hyperlinkUpdate(HyperlinkEvent event){
    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      // Only follow links within the documentation
      URL url = event.getURL();
      String protocol = url.getProtocol();
      String path = url.getPath();
      if (("file".equals(protocol) || "jar".equals(protocol))
            && path.indexOf(HELP_PATH) >= 0) {
        displayPage(url);
      }
    }
  }
  
}

