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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.platform.PlatformFactory;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * The frame for displaying the HTML quick start files.
 */
public class QuickStartFrame extends HelpFrame {
  private static final String HELP_PATH = "/edu/rice/cs/drjava/docs/quickstart/";
  protected static final URL INTRO_URL =
    HTMLFrame.class.getResource(HELP_PATH + HOME_PAGE);
 
  
  public QuickStartFrame() {
    super("QuickStart Guide to DrJava", INTRO_URL,
          QuickStartFrame.class.getResource(HELP_PATH + CONTENTS_PAGE),
          ICON);
    addHyperlinkListener(_linkListener);
  }
  
   /** Shows the page selected by the hyperlink event.
   * (theo) changed to anonymous inner class for encapsulation purposes
   */
  private HyperlinkListener _linkListener = new HyperlinkListener() {
    public void hyperlinkUpdate(HyperlinkEvent event) {
      if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        // Only follow links within the documentation
        URL url = event.getURL();
        String protocol = url.getProtocol();

        if ((!"file".equals(protocol)) && (!"jar".equals(protocol))) {
          // try to open in the platform's web browser, since we can't
          //  view it effectively here if it isn't in the jar
          // (we only handle file/jar protocols)
          PlatformFactory.ONLY.openURL(url);
          return; 
        }

        // perform path testing
        String path = url.getPath();

        if (path.indexOf(HELP_PATH+CONTENTS_PAGE) >= 0) {
          try {
            url = new URL(url,HOME_PAGE); // redirect to home, not contents
          }
          catch(MalformedURLException murle) {
          }
        }
        else if (path.indexOf(HELP_PATH) < 0) {
          // not anywhere in the help section
          return;
        }
        if (url.sameFile(_history.contents)) {
          return; // we're already here!
        }
        jumpTo(url);
      }
    }
  };

  
}
