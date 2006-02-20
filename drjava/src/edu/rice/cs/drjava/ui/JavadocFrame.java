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
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

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
    for (int i = 0; !test.exists() && (i < INTRO_PAGE.length); i++) {
      test = new File(destDir, INTRO_PAGE[i]);
    }

    // Packages.html might just be a pointer to another file
    if (test.exists()) {
      if (test.getName().equals("packages.html")) {
      test = _parsePackagesFile(test, destDir);
      }
    }
    else {
      throw new IllegalStateException("No Javadoc HTML output files found!");
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
      try { // process the opened file
        String line = br.readLine();
        int numLinesRead = 1;
        boolean found = false;
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
      finally { br.close(); }
    }
    catch (IOException ioe) { throw new UnexpectedException(ioe); }
    return packages;
  }

  /**
   * Constructor.
   * @param destDir Directory holding the Javadoc
   * @param curClass Name of the class to try to show by default
   * @param allDocs Whether Javadoc was run for all open documents
   */
  public JavadocFrame(File destDir, String curClass, boolean allDocs)
    throws MalformedURLException
  {
    // This call has to happen first!
    super("Javadoc Viewer",
          new URL("file", "", introPagePath(destDir, curClass)),
          new URL("file", "", (new File(destDir, INDEX_PAGE)).getAbsolutePath()),
           "DrJavadoc.png", destDir);

    addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          URL url = event.getURL();
          jumpTo(url);
        }
      }
    });

    if (!allDocs) {
      _hideNavigationPane();
    }
  }
}
