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

package edu.rice.cs.drjava;

import java.io.*;
import java.util.Vector;
// TODO: Change the usage of these classes to Collections style.
// TODO: Do these need to be synchronized?
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.GlobalEventNotifier;

/** Allows users to pass filenames to a command-line indenter.  Unfortunately, this uses the Swing API (high 
  * overhead), but we attempt to run the indentation in "headless AWT" mode to prevent a Java icon from showing 
  * up on the OS X dock.
  * @version $Id: IndentFiles.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class IndentFiles {
  
  /** Command line interface to the indenter.
    * Usage:
    *   java edu.rice.cs.drjava.IndentFile [-indent N] [filenames]
    *   Where N is the number of spaces in an indentation level
    * @param args Command line arguments
    */
  public static void main(String[] args) {
    Vector<String> fileNames = new Vector<String>();
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
        else fileNames.add(arg);
      }
      indentFiles(fileNames, indentLevel, silent);
    }
  }

  /** Displays a message showing how to use this class. */
  private static void _displayUsage() {
    System.out.println(
      "Usage:" +
      "  java edu.rice.cs.drjava.IndentFile [-indent N] [-silent] [filenames]\n" +
      "  Where N is the number of spaces in an indentation level");
  }
  
  /** Applies the indent logic to each file in the list of file names, saving the new copy of each one.
    * @param fileNames Vector of filenames of files to be indented
    * @param indentLevel The number of spaces to use for a level of indentation
    * @param silent Whether to print any output to System.out
    */
  public static void indentFiles(Vector<String> fileNames, int indentLevel, boolean silent) {
    //System.setProperty("java.awt.headless", "true"); // attempt headless AWT
    //System.out.println("Using Headless AWT: " + isHeadless());
    Indenter indenter = new Indenter(indentLevel);
    
    if (! silent) System.out.println("DrJava - Indenting files:");
    for (int i = 0; i < fileNames.size(); i++) {
      String fname = fileNames.get(i);
      File file = new File(fname);
      if (!silent) { 
        System.out.print("  " + fname + " ... ");
        System.out.flush();
      }
      try {
        String fileContents = IOUtil.toString(file);
        DefinitionsDocument doc = new DefinitionsDocument(indenter, new GlobalEventNotifier());
        doc.insertString(0, fileContents, null); // (no attributes)
        int docLen = doc.getLength();
        doc.indentLines(0, docLen);
        fileContents = doc.getText();
        IOUtil.writeStringToFile(file, fileContents);
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

//  /** Java versions 1.4 or above should have this implemented.  
//    * Return false, if earlier version.
//    */
//  private static boolean isHeadless() {
//    try {
//      Method isHeadless = java.awt.GraphicsEnvironment.class.getMethod("isHeadless", new Class[0]);
//      return ((Boolean) isHeadless.invoke(null,new Object[0])).booleanValue();
//    }
//    catch(Exception e) {
//      return false;
//    }
//  }
}
