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
 * @version $Id: JavadocFrame.java 5594 2012-06-21 11:23:40Z rcartwright $
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

  /** Reads through the beginning of the packages.html file to determine
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
              String fileName = line.substring(startIndex, endIndex);
              return new File(destDir, fileName);
            }
          }
        }
      }
      finally { br.close(); }
    }
    catch (IOException ioe) { throw new UnexpectedException(ioe); }
    return packages;
  }

  /** Constructor.
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
