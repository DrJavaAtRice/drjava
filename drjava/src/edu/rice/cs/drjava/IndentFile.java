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
import java.util.Vector;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
/**
 * Allows users to pass filenames to a command-line indenter.  The stupid part is that
 * it uses the swing document API.  Attempts to run with headless AWT though.
 * $Id$
 */
public class IndentFile {
  
  private static final int BUF_SIZE = 0x2800; // 10Kb
  
  public static void main(String[] args) {
    Vector filenames = new Vector();
    int arglen = args.length;
    int indentLevel = 2;
    for(int i = 0; i < arglen; i++) {
      String arg = args[i];
      if(arg.equals("-indent")) {
        i++;
        try {
          indentLevel = Integer.parseInt(args[i]);
        } catch(Exception e) {
          System.err.println("java edu.rice.cs.drjava.IndentFile [-indent level] [filenames]");
          System.exit(-1);
        }
      } else {
        filenames.addElement(arg);
      }
    }
    indentFiles((String[])filenames.toArray(new String[filenames.size()]),indentLevel);
  }

  public static void indentFiles(String[] filenames, int indentLevel) {
    System.setProperty("java.awt.headless","true"); // attempt headless AWT
    System.out.println("Using Headless AWT: "+isHeadless());
    int numFiles = filenames.length;
    char[] buf = new char[BUF_SIZE];
    Indenter INDENTER = new Indenter(indentLevel);
    for(int i = 0; i < numFiles; i++) {
      String fname = filenames[i];
      System.out.print("Indenting file: "+fname+" ... ");
      System.out.flush();
      try {
        String fileContents = readFileAsString(fname,buf);
        DefinitionsDocument doc = new DefinitionsDocument(INDENTER);
        doc.insertString(0,fileContents,null); // null attributes (no attributes)
        int docLen = doc.getLength();
        doc.indentLines(0,docLen);
        docLen = doc.getLength();
        fileContents = doc.getText(0,docLen);
        writeFileFromString(fname, fileContents);
        System.out.println("done.");
      } catch (Exception e) {
        System.out.println("ERROR!");
        System.out.println("Exception: "+e.toString());
        e.printStackTrace(System.out);
        System.out.println();
      }
      System.gc();
    }
    System.out.println();
    System.exit(0);
  }
  
  private static String readFileAsString(String filename, char[] buf) 
    throws IOException {
    StringBuffer sb = new StringBuffer(buf.length);
    FileReader reader = new FileReader(filename);
    for(int c = reader.read(buf); c != -1; c = reader.read(buf)) {
      sb.append(buf,0,c);
    }
    reader.close();
    return sb.toString();
  }
  
  private static void writeFileFromString(String filename, String value) 
    throws IOException {
    FileWriter writer = new FileWriter(filename);
    writer.write(value);
    writer.close();
  }
  

  /**
   * Java versions 1.4 or above should have this implemented.  Return false, if earlier version.
   */
  private static boolean isHeadless() {
    try {
      Method isHeadless = java.awt.GraphicsEnvironment.class.getMethod("isHeadless", new Class[0]);
      return ((Boolean) isHeadless.invoke(null,new Object[0])).booleanValue();
    } catch(Exception e) {
      return false;
    }
  }
}
