/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava;

import java.io.*;
import java.util.Vector;
// TODO: Change the usage of these classes to Collections style.
// TODO: Do these need to be synchronized?
import edu.rice.cs.util.FileOps;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.GlobalEventNotifier;

/** Allows users to pass filenames to a command-line indenter.  Unfortunately, this uses the Swing API (high 
 *  overhead), but we attempt to run the indentation in "headless AWT" mode to prevent a Java icon from showing 
 *  up on the OS X dock.
 *  @version $Id$
 */
public class IndentFiles {
  
  /**
   * Command line interface to the indenter.
   * Usage:
   *   java edu.rice.cs.drjava.IndentFile [-indent N] [filenames]
   *   Where N is the number of spaces in an indentation level
   * 
   * @param args Command line arguments
   */
  public static void main(String[] args) {
    Vector<String> filenames = new Vector<String>();
    int indentLevel = 2;
    boolean silent = false;
    if (args.length < 1) _displayUsage();
    else {
      for (int i = 0; i < args.length; i++) {
        String arg = args[i];
        if (arg.equals("-indent")) {
          i++;
          try { indentLevel = Integer.parseInt(args[i]); }
          catch (Exception e) {
            _displayUsage();
            System.exit(-1);
          }
        }
        else if (arg.equals("-silent")) silent = true;
        else filenames.add(arg);
      }
      indentFiles(filenames, indentLevel, silent);
    }
  }

  /**
   * Displays a message showing how to use this class.
   */
  private static void _displayUsage() {
    System.out.println(
      "Usage:" +
      "  java edu.rice.cs.drjava.IndentFile [-indent N] [-silent] [filenames]\n" +
      "  Where N is the number of spaces in an indentation level");
  }
  
  /** Applies the indent logic to each file in the list of file names, saving the new copy of each one.
   * @param filenames Vector of filenames of files to be indented
   * @param indentLevel The number of spaces to use for a level of indentation
   * @param silent Whether to print any output to System.out
   */
  public static void indentFiles(Vector<String> filenames, int indentLevel, boolean silent) {
    //System.setProperty("java.awt.headless", "true"); // attempt headless AWT
    //System.out.println("Using Headless AWT: "+isHeadless());
    Indenter indenter = new Indenter(indentLevel);
    
    if (!silent) System.out.println("DrJava - Indenting files:");
    for (int i = 0; i < filenames.size(); i++) {
      String fname = filenames.get(i);
      File file = new File(fname);
      if (!silent) { 
        System.out.print("  " + fname + " ... ");
        System.out.flush();
      }
      try {
        String fileContents = FileOps.readFileAsString(file);
        DefinitionsDocument doc = new DefinitionsDocument(indenter, new GlobalEventNotifier());
        doc.insertString(0, fileContents, null); // (no attributes)
        int docLen = doc.getLength();
        doc.indentLines(0, docLen);
        fileContents = doc.getText();
        FileOps.writeStringToFile(file, fileContents);
        if (!silent) System.out.println("done.");
      }
      catch (Exception e) {
        if (!silent) {
          System.out.println("ERROR!");
          System.out.println("  Exception: " + e.toString());
          e.printStackTrace(System.out);
          System.out.println();
        }
      }
      // System.gc();
    }
    if (!silent) System.out.println();
  }

  /**
   * Java versions 1.4 or above should have this implemented.  
   * Return false, if earlier version.
   *
  private static boolean isHeadless() {
    try {
      Method isHeadless = java.awt.GraphicsEnvironment.class.getMethod("isHeadless", new Class[0]);
      return ((Boolean) isHeadless.invoke(null,new Object[0])).booleanValue();
    }
    catch(Exception e) {
      return false;
    }
  }*/
}
