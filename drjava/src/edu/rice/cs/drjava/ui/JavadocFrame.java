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

import javax.swing.text.BadLocationException;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import edu.rice.cs.util.UnexpectedException;

/**
 * DrJava's Javadoc viewing frame
 * @version $Id$
 */
public class JavadocFrame extends HTMLFrame {
  
  private static final int MAX_READ_PACKAGES_LINES = 100;
  private static final int MAX_READ_FOR_LINK_LINES = 100;
  private static final String[] INTRO_PAGE= {
    "overview-summary.html",
    "packages.html"
  };
  private static final String INDEX_PAGE= "allclasses-frame.html";
  
  private static String introPagePath(File destDir, String curClass) {
    // Iterate through possible intro pages, looking for one that exists.
    File test = new File(destDir, curClass + ".html");
    int i = -1;
    while (!test.exists() && (i < INTRO_PAGE.length)) {
      i++;
      test = new File(destDir, INTRO_PAGE[i]);
    }
    
    // Packages.html might just be a pointer to another file
    if (test.getName().equals("packages.html")) {
      test = _parsePackagesFile(test, destDir);
    }
    return test.getAbsolutePath();
  }
  
  /**
   * Reads through the beginning of the packages.html file to determine
   * if it is just a pointer to another file.  Returns either the same
   * file (if it's not a pointer), or the file used for "No frames" (if
   * it is a pointer).
   * @param packages Full path to the packages.html file
   */
  private static File _parsePackagesFile(File packages, File destDir) {
    try {
      FileReader fr = new FileReader(packages);
      BufferedReader br = new BufferedReader(fr);
      boolean found = false;
      String line = br.readLine();
      int numLinesRead = 1;
      while ((!found) &&
             (numLinesRead < MAX_READ_PACKAGES_LINES) &&
             (line != null)) {
        found = (line.indexOf("The front page has been relocated") != -1);
        if (!found) {
          line = br.readLine();
          numLinesRead++;
        }
      }
      
      // Replace packages.html with the No Frames link.
      if (found) {
        boolean foundLink = false;
        while ((!foundLink) &&
               (numLinesRead < MAX_READ_FOR_LINK_LINES) &&
               (line != null)) {
          foundLink = (line.indexOf("Non-frame version") != -1);
          if (!foundLink) {
            line = br.readLine();
            numLinesRead++;
          }
        }
        
        if (foundLink) {
          String start = "HREF=\"";
          int startIndex = line.indexOf(start) + start.length();
          int endIndex = line.indexOf("\">");
          if ((startIndex != -1) && (endIndex != -1)) {
            String filename = line.substring(startIndex, endIndex);
            return new File(destDir, filename);
          }
        }
      }
    }
    catch (IOException ioe) {
      throw new UnexpectedException(ioe);
    }
    
    return packages;
  }
  
  public JavadocFrame(File destDir, String curClass) throws MalformedURLException {
    // This call has to happen first!
    super("Javadoc Viewer",
          new URL("file", "", introPagePath(destDir, curClass)),
          new URL("file", "", (new File(destDir, INDEX_PAGE)).getAbsolutePath()),
           "DrJavadoc.png", destDir);

    addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent event){
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          URL url = event.getURL();
          jumpTo(url);
        }
      }
    });
  }
}
