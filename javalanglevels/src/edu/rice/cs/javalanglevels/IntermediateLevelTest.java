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

import static edu.rice.cs.javalanglevels.ElementaryLevelTest.lf;

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
  
  public void assertEquals(String s, Data answer, Data testValue) {
//    if (! answer.equals(testValue)) 
//      System.err.println("Unit test '" + s + "' failed. Expected '" + 
//                         answer.getName() + "'.  Found '" + testValue.getName() + "'.");
  }
  
  /** Test that files that are correct can be processed with no errors and result in the expected augmented file.
    * Yay.dj1 is designed to be handled as a 1.4 file, so ignore it here.
    */
  public void testSuccessful() {
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
//        System.err.println("Testing " + pathName + " ; should be successful");
        return pathName.getAbsolutePath().endsWith(".dj1") && ! pathName.getAbsolutePath().endsWith("Yay.dj1");  
        // we will check Yay.dj1 for 1.4 augmentation
      }
    });

//     System.err.flush();
    
    LanguageLevelConverter llc = new LanguageLevelConverter();
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    result = llc.convert(testFiles, new Options(JavaVersion.JAVA_5, IterUtil.make(new File("lib/buildlib/junit.jar"))));
    
    assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
    
    assertEquals("should be no visitor exceptions", new LinkedList<Pair<String, JExpressionIF>>(), result.getSecond());
    
    /**Now make sure that the resulting java files are correct.*/
//    System.err.println("Ensuring that generated .java files are correct.");
    
    for(int i = 0; i < testFiles.length; i++) {
      File currFile = testFiles[i];
      String fileName = currFile.getAbsolutePath();
      fileName = fileName.substring(0, fileName.length() -4);
      File resultingFile = new File(fileName + ".java");
      File correctFile = new File(fileName + ".expected");
      if (correctFile.exists()) {
        try {
//          System.err.println("Checking " + currFile);
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
  

  /** Ensure that files that are incorrect do actually throw errors.*/
  public void testShouldBeErrors() {
    directory = new File(directory.getAbsolutePath() + System.getProperty("file.separator") + "shouldBreak");
    
    File[] testFiles = directory.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
//        System.err.println("Testing " + pathName + " should break");
//        return pathName.getAbsolutePath().endsWith(".dj1");
        return pathName.getAbsolutePath().endsWith("BadClass.dj1");
      }});
//    System.err.println("In testShouldBeErrors, testFiles = " + Arrays.toString(testFiles));
    LanguageLevelConverter llc;
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    for (int i = 0; i < testFiles.length; i++) {
//      System.err.println("TESTING " + testFiles[i]);
      LanguageLevelConverter llc1 = new LanguageLevelConverter();
//      System.err.println("Checking " + testFiles[i]);
      result = llc1.convert(new File[] {testFiles[i]}, new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make()));
      assertTrue("should be parse exceptions or visitor exceptions in file " + testFiles[i].getName(), 
                 ! result.getFirst().isEmpty() || ! result.getSecond().isEmpty());
    }
    
    /* Take care of the "references" directory */
    LanguageLevelConverter llc2 = new LanguageLevelConverter();
    File f = new File(new File(directory, "references"), "ReferencingClass.dj1");
    result = llc2.convert(new File[] { f }, new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make()));
    assertTrue("should be parse exceptions or visitor exceptions in file " + f.getName(), 
               ! result.getFirst().isEmpty() || ! result.getSecond().isEmpty());
  }
  
  
  /** Make sure that 1.4 augmentation rules are correctly followed for Yay.dj1*/
  public void test14Augmentation() {
    File[] arrayF = new File[]{ new File("testFiles/forIntermediateLevelTest/Yay.dj1")};
    LanguageLevelConverter llc = new LanguageLevelConverter();
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    assert llc._newSDs != null;
    result = llc.convert(arrayF, new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make()));
    assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
    
    assertEquals("should be no visitor exceptions", new LinkedList<Pair<String, JExpressionIF>>(), result.getSecond());
    
    
    File currFile = new File("testFiles/forIntermediateLevelTest/Yay.dj1");
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
