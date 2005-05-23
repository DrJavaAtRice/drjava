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
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.platform.PlatformFactory;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * The frame for displaying the HTML help files.
 * @version $Id$
 */
public class HelpFrame extends HTMLFrame {
  private static final String HELP_PATH =  "/edu/rice/cs/drjava/docs/user/";
  protected static final String CONTENTS_PAGE = "index.html";
  protected static final String HOME_PAGE = "intro.html";
  private static final URL INTRO_URL =
    HTMLFrame.class.getResource(HELP_PATH + HOME_PAGE);
  protected static final String ICON = "DrJavaHelp.png";

  public HelpFrame() {
    super("Help on using DrJava", INTRO_URL, HelpFrame.class.getResource(HELP_PATH + CONTENTS_PAGE), ICON);
    addHyperlinkListener(_linkListener);

  }
  
  /**
   * Used by subclass QuickStartFrame to instantiate fields of frame.
   */
  public HelpFrame(String frameName, URL introUrl, URL indexUrl, String iconString) {
    super(frameName, introUrl, indexUrl, iconString);
  }
  

  protected String getErrorText(URL url) {
    // The help files are made available by running "ant docs"
    String errorText = "The Help files are currently unavailable.";
    if (CodeStatus.DEVELOPMENT) {  // don't show this message in stable
      errorText += "\n\nTo generate the help files, run the \"ant docs\" target" +
        " after compiling DrJava.";
    }
    return errorText;
  }

  /** Shows the page selected by the hyperlink event.  Changed to anonymous inner class for 
   * encapsulation purposes */
  private HyperlinkListener _linkListener = new HyperlinkListener() {
    public void hyperlinkUpdate(HyperlinkEvent event) {
      if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        // Only follow links within the documentation
        URL url = event.getURL();
        String protocol = url.getProtocol();

        if (!"file".equals(protocol) && !"jar".equals(protocol)) {
          // try to open in the platform's web browser, since we can't
          //  view it effectively here if it isn't in the jar
          // (we only handle file/jar protocols)
          PlatformFactory.ONLY.openURL(url);
          return; 
        }

        // perform path testing
        String path = url.getPath();

        if (path.indexOf(HELP_PATH+CONTENTS_PAGE) >= 0) {
          try { url = new URL(url,HOME_PAGE); } // redirect to home, not content
          catch(MalformedURLException murle) {
            /* do nothing */
          }
        }
        else if (path.indexOf(HELP_PATH) < 0) return; // not anywhere in the help section
          
        if (url.sameFile(_history.contents)) return; // we're already here!
        jumpTo(url);
      }
    }
  };
}
