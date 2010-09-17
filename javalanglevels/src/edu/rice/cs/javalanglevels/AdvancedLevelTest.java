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
import edu.rice.cs.javalanglevels.util.Log;
import junit.framework.TestCase;
import java.util.*;
import java.io.*;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.io.IOUtil;

import static edu.rice.cs.javalanglevels.ElementaryLevelTest.lf;

/** This is a high-level test to make sure that taking an Advanced Level file from
  * source file to augmented file has the correct behavior, does not throw errors when
  * it should not, throws errors when it should, and results in the correct augmented code.
  * Files that should be successfully tested are placed in the testFiles/forAdvancedLevelTest folder 
  * as .dj2 files, and the expected augmented files asre also placed in the testFiles/forAdvancedLevelTest
  * folder with the same name, but a .expected extension.  Files that are expected to generate errors are
  * placed in the testFiles/forAdvancedLevelTest/shouldBreak folder, as .dj2 files.
  * Other subdirectories are used for other tests.
  */
import java.util.*;
  
public class AdvancedLevelTest extends TestCase {
    
  public static final Log _log = new Log("LLConverter.txt", false);
  
  File dir1, dir2, dir3;
  FileFilter dj2Filter, dj2JavaFilter;
  
  public void setUp() { 
    dir1 = new File("testFiles/forAdvancedLevelTest"); 
    dir2 = new File("testFiles/forAdvancedLevelTest/importedFiles"); 
    dir3 = new File("testFiles/forAdvancedLevelTest/importedFiles2"); 
    
    dj2Filter = new FileFilter() {
      public boolean accept(File pathName) {
        String name = pathName.getAbsolutePath();
        return (name.endsWith(".dj2"));
      }
    };
    dj2JavaFilter = new FileFilter() {
      public boolean accept(File pathName) {
        String name = pathName.getAbsolutePath();
        return (name.endsWith(".dj2") || name.endsWith(".java"));
      }
    };
  }
  
  /** This ensures that setUp() is not failing. */
  public void testNothing() {
    LanguageLevelVisitor llv = 
      new LanguageLevelVisitor(new File(""), 
                               "",
                               null,
                               new LinkedList<String>(), 
                               new LinkedList<String>(),
                               new HashSet<String>(), 
                               new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                               new LinkedList<Command>());
  }
  
  /** Try some example files and make sure they can be converted without errors and that the resulting conversions are 
    * correct. */
  public void testSuccessful() {

    _log.log("Running testSuccessful");
    File[] files1 = dir1.listFiles(dj2Filter);
    File[] files2 = dir2.listFiles(dj2Filter);
    File[] files3 = dir3.listFiles(dj2Filter);

    LanguageLevelConverter llc = new LanguageLevelConverter();
    // testFiles = files1 || files2 || files3; this computation is ugly in Java because Java has no functional lists
    int len1 = files1.length;
    int len2 = files2.length;
    int len3 = files3.length;
    File[] testFiles = new File[len1 + len2 + len3];
    for (int i = 0; i < len1; i++) { testFiles[i] = files1[i]; }
    for (int i = 0; i < len2; i++) { testFiles[len1 + i] = files2[i]; }
    for (int i = 0; i < len3; i++) { testFiles[len1 + len2 + i] = files3[i]; }
        
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    result = llc.convert(testFiles, new Options(JavaVersion.JAVA_5, IterUtil.<File>empty()));
    
//    System.err.println("testFiles = " + Arrays.toString(testFiles));
    
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
                       lf(IOUtil.toString(correctFile)),
                       lf(IOUtil.toString(resultingFile)));
        }
        catch (IOException ioe) {
          fail(ioe.getMessage());
          // let JUnit throw the exception
        }
      }
    }
    
    //test the subdirectory files as well.
    File dir2 = new File(dir1.getAbsolutePath() + System.getProperty("file.separator") + "importedFiles");
    testFiles = dir2.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        String name = pathName.getAbsolutePath();
        return name.endsWith("IsItPackageAndImport.dj1") || name.endsWith("ToReference.dj1");
      }});
      
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
                         lf(IOUtil.toString(correctFile)),
                         lf(IOUtil.toString(resultingFile)));
          }
          catch (IOException ioe) {
            fail(ioe.getMessage());
            // let JUnit throw the exception
          }
        }
      }
      
//      //And make sure that no java file was generated for ToReference2.dj1
//      //(This is testing that we correctly handled what could have been an ambiguous name reference, but wasn't)
//      File f = new File(dir2, "ToReference2.java");
//      assertFalse("ToReference2.java should not exist", f.exists());
      
      File f;
      dir2 = new File(dir1.getAbsolutePath() + System.getProperty("file.separator") + "importedFiles2");
      testFiles = dir2.listFiles(new FileFilter() {
        public boolean accept(File pathName) {
          return pathName.getAbsolutePath().endsWith("AlsoReferenced.dj1");
        }});
        
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
                           lf(IOUtil.toString(correctFile)),
                           lf(IOUtil.toString(resultingFile)));
            }
            catch (IOException ioe) {
              fail(ioe.getMessage());
              // let JUnit throw the exception
            }
          }
        }
        
//        //And make sure that no java file was generated for ToReference.dj1
//        f = new File(dir2, "ToReference.java");
//        assertFalse("ToReference.java should not exist", f.exists());
//        fail("Ensure that System.err is dumped");
        
  }
  
  /** This file used to have a NullPointer Exception in it because of a bug in the code.  Leave the test in so that the bug
    * never gets reintroduced.
    */
  public void testNoNullPointer() { 
    _log.log("Running testNoNullPointer");
    dir1 = new File(dir1.getAbsolutePath() + "/shouldBreak");
    File[] testFiles = dir1.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getAbsolutePath().endsWith("SwitchDoesntAssign.dj2");
      }});
    _log.log("testFiles = " + testFiles);
    LanguageLevelConverter llc = new LanguageLevelConverter();
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    for (int i = 0; i<testFiles.length; i++) {
      result = llc.convert(new File[]{testFiles[i]}, new Options(JavaVersion.JAVA_5, IterUtil.<File>empty()));
      assertTrue("should be parse exceptions or visitor exceptions", !result.getFirst().isEmpty() || !result.getSecond().isEmpty());
      }
  }  
    
  /** Make sure that the order packaged files are compiled in does not matter.
    * This is to make sure that a bug that was fixed stays fixed.
    */
  public void testPackagedOrderMatters() {
    _log.log("Running testPackagedOrderMatters");
    dir2 = new File(dir1.getAbsolutePath() + System.getProperty("file.separator") + "lists-dj2" + 
                    System.getProperty("file.separator") + "src" + System.getProperty("file.separator") + "listFW");
    File[] testFiles = 
      new File[]{ new File(dir2, "NEList.dj2"), new File(dir2, "MTList.dj2"), new File(dir2, "IList.dj2")};
    
//    System.err.println("testfiles = " + Arrays.toString(testFiles));

    LanguageLevelConverter llc = new LanguageLevelConverter();
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result;
    result = llc.convert(testFiles, new Options(JavaVersion.JAVA_5, IterUtil.<File>empty()));
//    System.err.println("ParseExceptions: " + result.getFirst());
//    System.err.println("VisitorExceptions: " + result.getSecond());
    
    assertEquals("should be no parse exceptions", new LinkedList<JExprParseException>(), result.getFirst());
    
    assertEquals("should be no visitor exceptions", new LinkedList<Pair<String, JExpressionIF>>(), result.getSecond());
    
    //don't worry about checking the .java files for correctness...just make sure there weren't any exceptions
  }
}
