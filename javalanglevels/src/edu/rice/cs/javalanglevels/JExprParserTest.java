/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.javalanglevels;

import java.io.*;

import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.javalanglevels.parser.*;

import junit.framework.TestCase;

/**
 * Test the JExprParser by trying to parse all files in the testFiles directory that end in .test,
 * writing the output to a file of the same name ending in .actual, and comparing that file to 
 * the file of the same name in the testFiles directory that ends with .expected
 */
public class JExprParserTest extends TestCase {
  
  /**
   * Takes an array of test files and feeds them into the JExprParser.  These files should
   * succeed.
   */
  public void testParseSucceeds() throws IOException, ParseException {
    File directory = new File("testFiles");

    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getAbsolutePath().endsWith(".test");
      }
    });

    for(int i = 0; i < testFiles.length; i++) {
      File currFile = testFiles[i];
      SourceFile sf = null;
      try {
        sf = new JExprParser(currFile).SourceFile();
      }
      catch (ParseException pe) {
        throw pe;
      }

      String path2 = currFile.getAbsolutePath();
      int indexOfLastDot2 = path2.lastIndexOf('.');
      String newPath2 = path2.substring(0, indexOfLastDot2) + ".actual";
      FileWriter fw = new FileWriter(newPath2);
      fw.write(sf.toString());
      fw.close();
      
      // Get the corresponding expected String value for the toString of the SourceFile.
      String path = currFile.getAbsolutePath();
      int indexOfLastDot = path.lastIndexOf('.');
      String newPath = path.substring(0, indexOfLastDot) + ".expected";
      File f = new File(newPath);
      String text = readFileAsString(f);
      assertEquals("The resulting SourceFile generated from " + currFile + " is not correct.", text, sf.toString());
    }
  }

  /**
   * Read the entire contents of a file and return them.  Copied from DrJava's FileOps.
   */
  public static String readFileAsString(final File file) throws IOException {
    FileReader reader = new FileReader(file);
    StringBuffer buf = new StringBuffer();

    while (reader.ready()) {
      char c = (char) reader.read();
      buf.append(c);
    }

    reader.close();
    return buf.toString();
  }
}
