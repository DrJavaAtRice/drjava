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

package edu.rice.cs.drjava;

import java.io.*;
import java.lang.reflect.Method;
import gj.util.Vector;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

/**
 * Allows users to pass filenames to a command-line indenter.
 * Unfortunately, this uses the Swing API (high overhead), but we attempt
 * to run the indentation in "headless AWT" mode to prevent a Java icon
 * from showing up on the OS X dock.
 * @version $Id$
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
    if (args.length < 1) {
      _displayUsage();
    }
    else {
      for (int i = 0; i < args.length; i++) {
        String arg = args[i];
        if (arg.equals("-indent")) {
          i++;
          try {
            indentLevel = Integer.parseInt(args[i]);
          }
          catch (Exception e) {
            _displayUsage();
            System.exit(-1);
          }
        }
        else if (arg.equals("-silent")) {
          silent = true;
        }
        else {
          filenames.addElement(arg);
        }
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
  
  /**
   * Applies the indent logic to each file in the list of file names,
   * saving the new copy of each one.
   * @param filenames Vector of filenames of files to be indented
   * @param indentLevel The number of spaces to use for a level of indentation
   * @param silent Whether to print any output to System.out
   */
  public static void indentFiles(Vector<String> filenames, 
                                 int indentLevel,
                                 boolean silent)
  {
    System.setProperty("java.awt.headless", "true"); // attempt headless AWT
    //System.out.println("Using Headless AWT: "+isHeadless());
    
    Indenter indenter = new Indenter(indentLevel);
    if (!silent) System.out.println("DrJava - Indenting files:");
    for (int i = 0; i < filenames.size(); i++) {
      String fname = filenames.elementAt(i);
      File file = new File(fname);
      if (!silent) { 
        System.out.print("  " + fname + " ... ");
        System.out.flush();
      }
      try {
        String fileContents = FileOps.readFileAsString(file);
        DefinitionsDocument doc = new DefinitionsDocument(indenter);
        doc.insertString(0, fileContents, null); // (no attributes)
        int docLen = doc.getLength();
        doc.indentLines(0, docLen);
        docLen = doc.getLength();
        fileContents = doc.getText(0, docLen);
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
      System.gc();
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
