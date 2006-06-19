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
import edu.rice.cs.javalanglevels.parser.*;
import edu.rice.cs.javalanglevels.tree.*;
import junit.framework.TestCase;
import java.util.*;
import java.io.*;

/**
 * This is a high-level test to make sure that taking an Intermediate Level file from
 * source file to augmented file has the correct behavior, does not throw errors when
 * it should not, throws errors when it should, and results in the correct augmented code.
 * Files that should be successfully tested are placed in the testFiles/forIntermediateLevelTest folder 
 * as .dj1 files, and the expected augmented files asre also placed in the testFiles/forIntermediateLevelTest
 * folder with the same name, but a .expected extension.  Files that are expected to generate errors are
 * placed in the testFiles/forIntermediateLevelTest/shouldBreak folder, as .dj1 files.
 * Other subdirectories are used for other tests.
 */
public class IntermediateLevelTest extends TestCase {
  File directory;
  
  public void setUp() {
    directory = new File("testFiles/forIntermediateLevelTest");
    
  }

  /*Test that files that are correct can be processed with no errors and result in the expected augmented file.
   * Yay.dj1 is designed to be handled as a 1.4 file, so ignore it here.
   */
  public void testSuccessful() {
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getAbsolutePath().endsWith(".dj1") && !pathName.getAbsolutePath().endsWith("Yay.dj1");  //we will check Yay.dj1 for 1.4 augmentation
      }
    });

    System.out.flush();
    
    LanguageLevelConverter llc = new LanguageLevelConverter("1.5");
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    result = llc.convert(testFiles);
    
    assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
    
    assertEquals("should be no visitor exceptions", new LinkedList<Pair<String, JExpressionIF>>(), result.getSecond());
    
    /**Now make sure that the resulting java files are correct.*/
    for(int i = 0; i < testFiles.length; i++) {
      File currFile = testFiles[i];
      String fileName = currFile.getAbsolutePath();
      fileName = fileName.substring(0, fileName.length() -4);
      File resultingFile = new File(fileName + ".java");
      File correctFile = new File(fileName + ".expected");
      if (correctFile.exists()) {
        try {
          assertEquals("File " + currFile.getName() + " should have been parsed and augmented correctly.",
                       readFileAsString(correctFile),
                       readFileAsString(resultingFile));
        }
        catch (IOException ioe) {
          fail(ioe.getMessage());
          // let JUnit throw the exception
        }
      }
    }
  }
  

  /** Ensure that files that are incorrect do actually throw errors.*/
  public void testShouldBeErrors() {
    directory = new File(directory.getAbsolutePath() + System.getProperty("file.separator") + "shouldBreak");
    
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getAbsolutePath().endsWith(".dj1");
      }});
      
    LanguageLevelConverter llc;
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    for (int i = 0; i<testFiles.length; i++) {
      llc = new LanguageLevelConverter("1.5");
      result = llc.convert(new File[] {testFiles[i]});
      assertTrue("should be parse exceptions or visitor exceptions in file " + testFiles[i].getName(), !result.getFirst().isEmpty() || !result.getSecond().isEmpty());
    }
    
    /* Take care of the "references" directory */
    llc = new LanguageLevelConverter("1.5");
    File f = new File(new File(directory, "references"), "ReferencingClass.dj1");
    result = llc.convert(new File[] { f });
    assertTrue("should be parse exceptions or visitor exceptions in file " + f.getName(), !result.getFirst().isEmpty() || !result.getSecond().isEmpty());
  }
  
  
  /*Make sure that 1.4 augmentation rules are correctly followed for Yay.dj1*/
  public void test14Augmentation() {
        File[] arrayF = new File[]{ new File("testFiles/forIntermediateLevelTest/Yay.dj1")};
      LanguageLevelConverter llc = new LanguageLevelConverter("1.4");
      Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
      result = llc.convert(arrayF);
      assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
      
      assertEquals("should be no visitor exceptions", new LinkedList<Pair<String, JExpressionIF>>(), result.getSecond());
      
      
      File currFile = new File("testFiles/forIntermediateLevelTest/Yay.dj1");
      String fileName = currFile.getAbsolutePath();
      fileName = fileName.substring(0, fileName.length() -4);
      File resultingFile = new File(fileName + ".java");
      File correctFile = new File(fileName + ".expected");
        
      try {
        assertEquals("File " + currFile.getName() + " should have been parsed and augmented correctly.",
                     readFileAsString(correctFile),
                     readFileAsString(resultingFile));
        }
      catch (IOException ioe) {
        fail(ioe.getMessage());
        // let JUnit throw the exception
      }
  }
  

  
  
  /**
   * Read the entire contents of a file and return them.  Copied from DrJava's FileOps.
   */
  public static String readFileAsString(final File file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    StringBuffer buf = new StringBuffer();

    while (reader.ready()) {
//      char c = (char) reader.read();
//      buf.append(c);
      String s = reader.readLine();
      buf.append(s);
    }
    

    reader.close();
    return buf.toString();
  }

  
  
}
