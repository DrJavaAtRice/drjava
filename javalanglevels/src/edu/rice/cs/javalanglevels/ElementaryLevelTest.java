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
 * This is a high-level test to make sure that taking an Elementary Level file from
 * source file to augmented file has the correct behavior, does not throw errors when
 * it should not, throws errors when it should, and results in the correct augmented code.
 * Files that should be successfully tested are placed in the testFiles/forElementaryLevelTest folder 
 * as .dj0 files, and the expected augmented files asre also placed in the testFiles/forElementaryLevelTest
 * folder with the same name, but a .expected extension.  Files that are expected to generate errors are
 * placed in the testFiles/forElementaryLevelTest/shouldBreak folder, as .dj0 files.
 * Other subdirectories are used for other tests.
 */
public class ElementaryLevelTest extends TestCase {
  File directory;
  
  public void setUp() {
    directory = new File("testFiles" + File.separatorChar + "forElementaryLevelTest");
    
  }

  /*Test some files that should be handled without errors, and make sure the resulting augmented
   * file is correct.*/
  public void testSuccessful() {
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getAbsolutePath().endsWith(".dj0");
      }
    });
    
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
  
  /*Test some files that should break*/
  public void testShouldBeErrors() {
    directory = new File(directory, "shouldBreak");
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getAbsolutePath().endsWith(".dj0");
      }});

      LanguageLevelConverter llc = new LanguageLevelConverter("1.5");
      Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
      for (int i = 0; i<testFiles.length; i++) {
        result = llc.convert(new File[]{testFiles[i]});
        assertTrue("should be parse exceptions or visitor exceptions in file " + testFiles[i].getName(), !result.getFirst().isEmpty() || !result.getSecond().isEmpty());
      }
  }
  
  /**
   * Test that when some files have already been compiled, the .java files are not generated for those files,
   * and files that reference those files are augmented correctly.
   */
  public void testSomeFilesCompiled() {
    directory = new File(directory, "someCompiled");
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getName().equals("UseOtherClassAsField.dj0") || pathName.getName().equals("SubClass.dj0");
      }});
      
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
        
        try {
        assertEquals("File " + currFile.getName() + " should have been handled correctly",
                     readFileAsString(correctFile),
                     readFileAsString(resultingFile));
        }
        catch (IOException ioe) {
          fail(ioe.getMessage());
          // let JUnit throw the exception
        }
      }
      
      /**And make sure that .java files did not get generated for the classes referenced from our test class.*/
      File superClass = new File(directory.getAbsolutePath() + "SuperClass.java");
      File classAsField = new File(directory.getAbsolutePath() + "ClassAsField.java");
      assertFalse("superClass.java should not exist", superClass.exists());
      assertFalse("ClassAsField.java should not exist", classAsField.exists());
      
  }
  
  /*Make sure that successful compilation is not dependant on visiting the file with no dependencies first.*/
  public void testOrderMatters() {
    directory = new File(directory, "orderMatters");
    File[] files = new File[]{ new File(directory, "Empty.dj0"), new File(directory, "List.dj0"), new File(directory, "NonEmpty.dj0") };
    LanguageLevelConverter llc = new LanguageLevelConverter("1.5");
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    result = llc.convert(files);
    
      assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
      assertEquals("should be 1 visitor exception", 1, result.getSecond().size());
      assertEquals("the error message should be correct", "Could not resolve symbol f", result.getSecond().getFirst().getFirst());
    
  }
    
  /**
   * An empty file should not get converted to a .java file.
   */
  public void testEmptyFileNoAction() {
    directory = new File(directory, "emptyFile");
    File[] files = new File[]{ new File(directory, "EmptyFile.dj0")};
    LanguageLevelConverter llc = new LanguageLevelConverter("1.5");
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    result = llc.convert(files);
    
    assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
    assertEquals("should be no visitor exceptions", 0, result.getSecond().size());
    
    assertFalse("Should be no .java file", (new File(directory, "EmptyFile.java")).exists());
    
    
    
  }
  
  /* Make sure that autoboxing is done appropriately*/
  public void testRequiresAutoboxing() {
    directory = new File(directory, "requiresAutoboxing");
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getAbsolutePath().endsWith(".dj0");
      }});

      LanguageLevelConverter llc14 = new LanguageLevelConverter("JDK1.4.0");
      LanguageLevelConverter llcJSR14 = new LanguageLevelConverter("JSR-14 2.4");
      Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
      
      for (int i = 0; i<testFiles.length; i++) {
        result = llc14.convert(new File[]{testFiles[i]});
        assertTrue("should be parse exceptions or visitor exceptions", !result.getFirst().isEmpty() || !result.getSecond().isEmpty());
      }
      
      result = llcJSR14.convert(testFiles);

      assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
      assertEquals("should be no visitor exceptions", new LinkedList<Pair<String, JExpressionIF>>(), result.getSecond());

      /**Now make sure that the resulting java files are correct.*/
      for(int i = 0; i < testFiles.length; i++) {
        File currFile = testFiles[i];
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
      
  }
  
  /**
   * Read the entire contents of a file and return them.  Copied from DrJava's FileOps.
   */
  public static String readFileAsString(final File file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    StringBuffer buf = new StringBuffer();

    while (reader.ready()) {
      String s = reader.readLine();
      buf.append(s);
    }

    reader.close();
    return buf.toString();
  }

}
