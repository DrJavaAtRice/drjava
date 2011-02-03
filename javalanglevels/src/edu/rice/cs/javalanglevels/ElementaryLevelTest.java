/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.javalanglevels;
import edu.rice.cs.javalanglevels.parser.*;
import edu.rice.cs.javalanglevels.tree.*;
import junit.framework.TestCase;
import java.util.*;
import java.io.*;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.*;
import edu.rice.cs.plt.io.IOUtil;

/** This is a high-level test to make sure that taking an Elementary Level file from
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
  
//  public void assertEquals(String s, Data answer, Data testValue) {
//    if (! answer.equals(testValue)) 
//      System.err.println("Unit test '" + s + "' failed. Expected '" + 
//                         answer.getName() + "'.  Found '" + testValue.getName() + "'.");
//  }
  
  /** Tests some files that should be handled without errors, and ensures the resulting augmented
    * file is correct.*/
  public void testSuccessful() {
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) { return pathName.getAbsolutePath().endsWith(".dj0"); }
    });
//    System.err.println("testFiles for testSuccessful = " + Arrays.toString(testFiles));
    LanguageLevelConverter llc = new LanguageLevelConverter();
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    result = llc.convert(testFiles, new Options(JavaVersion.JAVA_5,
                                                IterUtil.make(new File("lib/buildlib/junit.jar"))));
//    System.err.println("Visitor exception #1 = " + result.getSecond().getFirst());
//    fail("Dumping System.err");
    assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
    assertEquals("should be no visitor exceptions", new LinkedList<Pair<String, JExpressionIF>>(), result.getSecond());
    
    /** Now make sure that the resulting java files are correct.*/
    for (int i = 0; i < testFiles.length; i++) {
      File currFile = testFiles[i];
      String fileName = currFile.getAbsolutePath();
      fileName = fileName.substring(0, fileName.length() -4);
      File resultingFile = new File(fileName + ".java");
      File correctFile = new File(fileName + ".expected");
      
//      System.err.println("Testing file: " + fileName);
      
      if (correctFile.exists()) {
        try {
          assertEquals("File " + currFile.getName() + " should have been parsed and augmented correctly.",
                       lf(IOUtil.toString(correctFile)),
                       lf(IOUtil.toString(resultingFile)));
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
    
//    System.err.println("testFiles for testShouldBeErrors = " + Arrays.toString(testFiles));
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    for (int i = 0; i < testFiles.length; i++) {
      LanguageLevelConverter llc = new LanguageLevelConverter();
      result = llc.convert(new File[]{ testFiles[i] }, new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make()));
//      System.err.println("Test result = " + result);
      assertTrue("should be parse exceptions or visitor exceptions in file " + testFiles[i].getName(),
                 ! result.getFirst().isEmpty() || ! result.getSecond().isEmpty());
    }
  }
  
  // TODO: !!! Reinstate this test
  /** Tests that when some files have already been compiled, the .java files are not generated for those files,
    * and files that reference those files are augmented correctly.
    */
  public void xtestSomeFilesCompiled() {
    directory = new File(directory, "someCompiled");
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getName().equals("UseOtherClassAsField.dj0") || pathName.getName().equals("SubClass.dj0");
      }});
    
    LanguageLevelConverter llc = new LanguageLevelConverter();
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    result = llc.convert(testFiles, new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make()));
    
    assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
    assertEquals("should be no visitor exceptions", new LinkedList<Pair<String, JExpressionIF>>(), result.getSecond());
    
    /**Now make sure that the resulting java files are correct.*/
    for (int i = 0; i < testFiles.length; i++) {
      File currFile = testFiles[i];
      String fileName = currFile.getAbsolutePath();
      fileName = fileName.substring(0, fileName.length() -4);
      File resultingFile = new File(fileName + ".java");
      File correctFile = new File(fileName + ".expected");
      
      try {
        assertEquals("File " + currFile.getName() + " should have been handled correctly",
                     lf(IOUtil.toString(correctFile)),
                     lf(IOUtil.toString(resultingFile)));
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
  
  /* Make sure that successful compilation is not dependent on visiting the file with no dependencies first.*/
  public void testOrderMatters() {
    directory = new File(directory, "orderMatters");
    File[] files = new File[] { 
      new File(directory, "Empty.dj0"), 
      new File(directory, "List.dj0"), 
      new File(directory, "NonEmpty.dj0") 
    };
    LanguageLevelConverter llc = new LanguageLevelConverter();
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
//    System.err.println("files for testOrderMatters = " + Arrays.toString(files));
    result = llc.convert(files, new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make()));
    
    assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
    assertEquals("should be 1 visitor exception", 1, result.getSecond().size());
    assertEquals("the error message should be correct", "Could not resolve symbol f", 
                 result.getSecond().getFirst().getFirst());
    
//    System.err.println("testOrderMatters finished");
  }
  
  /** An empty file should not get converted to a .java file. */
  public void testEmptyFileNoAction() {
    directory = new File(directory, "emptyFile");
    File[] files = new File[]{ new File(directory, "EmptyFile.dj0")};
    LanguageLevelConverter llc = new LanguageLevelConverter();
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    result = llc.convert(files, new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make()));
    
    assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
    assertEquals("should be no visitor exceptions", 0, result.getSecond().size());
    assertFalse("Should be no .java file", (new File(directory, "EmptyFile.java")).exists());
  }
  
  /** Makes sure that autoboxing is done appropriately*/
  public void testRequiresAutoboxing() {
    directory = new File(directory, "requiresAutoboxing");
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getAbsolutePath().endsWith(".dj0");
      }});
    
    LanguageLevelConverter llc = new LanguageLevelConverter();
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    result = llc.convert(testFiles, new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make()));
    
    assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
    assertEquals("should be no visitor exceptions", new LinkedList<Pair<String, JExpressionIF>>(), result.getSecond());
    
//    for (int i = 0; i <testFiles.length; i++) {
//      LanguageLevelConverter llc5 = new LanguageLevelConverter();
//      result = llc5.convert(new File[]{testFiles[i]}, new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make()));
//      
//      assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
//      assertEquals("should be no visitor exceptions", new LinkedList<Pair<String, JExpressionIF>>(), result.getSecond());
//    }

    
    /* Now make sure that the resulting java files are correct.*/
    for (int i = 0; i < testFiles.length; i++) {
      File currFile = testFiles[i];
      String fileName = currFile.getAbsolutePath();
      fileName = fileName.substring(0, fileName.length() -4);
      File resultingFile = new File(fileName + ".java");
      File correctFile = new File(fileName + ".expected");
      
      try {
        assertEquals("File " + currFile.getName() + " should have been parsed and augmented correctly.",
                     lf(IOUtil.toString(correctFile)),
                     lf(IOUtil.toString(resultingFile)));
      }
      catch (IOException ioe) {
        fail(ioe.getMessage());
        // let JUnit throw the exception
      }
    }
  }

  /** Convert whatever line feeds (\n, \r, \r\n) are in the string to just \n, ignoring trailing whitespace. */
  public static String lf(String s) {
      return s.trim().replaceAll(edu.rice.cs.plt.text.TextUtil.NEWLINE_PATTERN,"\n");
  }
}
