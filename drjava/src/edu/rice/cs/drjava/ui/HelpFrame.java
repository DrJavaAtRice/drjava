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

import edu.rice.cs.drjava.CodeStatus;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * The frame for displaying the HTML help files.
 * @version $Id$
 */
public class HelpFrame extends HTMLFrame {
  private static final String HELP_PATH = "/edu/rice/cs/drjava/docs/user/";
  private static final String CONTENTS_PAGE = "index.html";
  private static final String HOME_PAGE = "intro.html";
  private static final URL INTRO_URL =
    HTMLFrame.class.getResource(HELP_PATH + HOME_PAGE);
  private static final String ICON = "DrJavaHelp.gif";

  public HelpFrame() {
    super("Help on using DrJava", INTRO_URL,
          HelpFrame.class.getResource(HELP_PATH + CONTENTS_PAGE),
          ICON);
    addHyperlinkListener(_linkListener);

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
