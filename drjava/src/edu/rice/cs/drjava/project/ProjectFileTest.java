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

package edu.rice.cs.drjava.project;

import junit.framework.*;
import java.io.*;
import java.util.*;

public class ProjectFileTest extends TestCase {
  private static final String SOURCE_FILE_1 =  "Test1.java";
  private static final String SOURCE_FILE_2 = "Test2.java";
  private static final String SOURCE_ENTRY_1 = "(test/" + SOURCE_FILE_1  + ")\n";
  private static final String SOURCE_ENTRY_2 = "(test/" + SOURCE_FILE_2 + ")\n";
  private static final String RESOURCE_FILE_1 = "resource1.gif";
  private static final String RESOURCE_FILE_2 = "resource2.gif";    
  private static final String RESOURCES_ENTRY_1 = "(test/" + RESOURCE_FILE_1 + ")\n";
  private static final String RESOURCES_ENTRY_2 = "(test/" + RESOURCE_FILE_2 + ")\n";
  private static final String MISC_FILE_1 = "misc1.txt";
  private static final String MISC_FILE_2 = "misc2.txt";    
  private static final String MISC_ENTRY_1 = "(test/" + MISC_FILE_1  + ")\n";
  private static final String MISC_ENTRY_2 = "(test/" + MISC_FILE_2  + ")\n";
  
  /* classpath is not relative to project file location because if you change computers you ought to
   * have to reedit the classpath, it's impossible to guess what it should be */
  private static final String CLASSPATH_1 = "/home/ef/drjava/built";
  private static final String CLASSPATH_2 = "/usr/site/jdk/lib/tools.jar";
  private static final String CLASSPATH_3 = "/home/comp312/bin/gj/gcjrt.jar";
  private static final String CLASSPATH_ENTRY_1 = "(" + CLASSPATH_1 + ")\n";
  private static final String CLASSPATH_ENTRY_2 = "(" + CLASSPATH_2 + ")\n";
  private static final String CLASSPATH_ENTRY_3 = "(" + CLASSPATH_3 + ")\n";
  private static final String JAR_MAIN_CLASS = "edu.rice.cs.drjava.DrJava";
  private static final String JAR_ENTRY = "(" + JAR_MAIN_CLASS + ")\n";
  
  /** the temporary file used to read from */
  private File testFile = null;
  
  private static final String TEST_FILE_TEXT =
    "  \t(Source  \n" +
    "\t     " + SOURCE_ENTRY_1 +
    "\t" + SOURCE_ENTRY_2 +
    ")    \n" +
    "   \t(Resources\t\t \t \n" +
    "\t" + RESOURCES_ENTRY_1 +
    "\t\t\t" + RESOURCES_ENTRY_2 +
    ")\n" +
    "(Misc\n" +
    "\t" + MISC_ENTRY_1 +
    "\t" + MISC_ENTRY_2 +
    ")\n" + 
    "(Classpath\n" +
    "\t" + CLASSPATH_ENTRY_1 +
    "\t" + CLASSPATH_ENTRY_2 +
    "\t" + CLASSPATH_ENTRY_3 +
    ")\n" + 
    "(Jar\n" +
    "\t" + JAR_ENTRY +
    ")\n";
  
  /* the reader which reads the test project file */
  BufferedReader reader = null;
  
  public ProjectFileTest(String s) {
    super(s);
  }
  
  /* creates the temporary project file and creates a reader for it */
  public void setUp() throws Exception {
    testFile = File.createTempFile("_test", "pjt");
    reader = new BufferedReader(new FileReader(testFile));
    BufferedWriter w = new BufferedWriter(new FileWriter(testFile));
    w.write(TEST_FILE_TEXT);
    w.close();
  }
  
  /* removes the temporary project file and sets the reader to null */
  public void tearDown() throws Exception {
    if( !testFile.delete() ) {
      fail("Couldn't delete temporary file " + testFile.toString());
    }
    reader = null;    
  }
  
  public void testSourceTag() throws Exception {
    SourceTag tag = TagFactory.makeSourceTag(testFile, reader);
    assertTrue( tag.entries().length == 2 );
    assertTrue(tag.entries()[0].getName().equals(SOURCE_FILE_1));
    assertTrue(tag.entries()[1].getName().equals(SOURCE_FILE_2));
  }
  
  public void testResourceTag() throws Exception {
    /* munch the source tag */
    TagFactory.makeSourceTag(testFile, reader);
    
    ResourceTag tag = TagFactory.makeResourceTag(testFile, reader);
    assertTrue( tag.entries().length == 2 );
    assertTrue(tag.entries()[0].getName().equals(RESOURCE_FILE_1));
    assertTrue(tag.entries()[1].getName().equals(RESOURCE_FILE_2));
  }
  
  public void testMiscTag() throws Exception {
    /* munch the source tag */
    TagFactory.makeSourceTag(testFile, reader);
    /* munch the resource tag */
    TagFactory.makeResourceTag(testFile, reader);
    
    MiscTag tag = TagFactory.makeMiscTag(testFile, reader);
    assertTrue( tag.entries().length == 2 );
    assertTrue(tag.entries()[0].getName().equals(MISC_FILE_1));
    assertTrue(tag.entries()[1].getName().equals(MISC_FILE_2));
  }
  
  public void testClasspathTag() throws Exception {
    /* munch the source tag */
    TagFactory.makeSourceTag(testFile, reader);
    /* munch the resource tag */
    TagFactory.makeResourceTag(testFile, reader);
    /* munch the misc tag */
    TagFactory.makeMiscTag(testFile, reader);
    
    ClasspathTag tag = TagFactory.makeClasspathTag(testFile, reader);
    assertTrue( tag.entries().length == 3 );
    assertTrue(tag.entries()[0].getAbsolutePath().equals(CLASSPATH_1));
    assertTrue(tag.entries()[1].getAbsolutePath().equals(CLASSPATH_2));
    assertTrue(tag.entries()[2].getAbsolutePath().equals(CLASSPATH_3));
  }
  
  public void testJarTag() throws Exception {
    /* munch the source tag */
    TagFactory.makeSourceTag(testFile, reader);
    /* munch the resource tag */
    TagFactory.makeResourceTag(testFile, reader);
    /* munch the misc tag */
    TagFactory.makeMiscTag(testFile, reader);
    /* munch the classpath tag */
    TagFactory.makeClasspathTag(testFile, reader);
    
    JarTag tag = TagFactory.makeJarTag(testFile, reader);
    
    assertTrue( tag.entries().length == 1 );
    assertTrue(tag.entries()[0].getName().equals(JAR_MAIN_CLASS));
  }
  
  public void testParser() throws Exception {
    ProjectFileIR ir = ProjectFileParser.ONLY.parse(testFile);
    
    assertTrue( ir.getSourceFiles().length == 2 );
    assertTrue(ir.getSourceFiles()[0].getName().equals(SOURCE_FILE_1));
    assertTrue(ir.getSourceFiles()[1].getName().equals(SOURCE_FILE_2));
    
    assertTrue( ir.getResourceFiles().length == 2 );
    assertTrue(ir.getResourceFiles()[0].getName().equals(RESOURCE_FILE_1));
    assertTrue(ir.getResourceFiles()[1].getName().equals(RESOURCE_FILE_2));
    
    assertTrue( ir.getMiscFiles().length == 2 );
    assertTrue(ir.getMiscFiles()[0].getName().equals(MISC_FILE_1));
    assertTrue(ir.getMiscFiles()[1].getName().equals(MISC_FILE_2));
    
    assertTrue( ir.getClasspath().length == 3 );
    assertTrue(ir.getClasspath()[0].getAbsolutePath().equals(CLASSPATH_1));
    assertTrue(ir.getClasspath()[1].getAbsolutePath().equals(CLASSPATH_2));
    assertTrue(ir.getClasspath()[2].getAbsolutePath().equals(CLASSPATH_3));
    
    assertTrue(ir.getJarMainClass().equals(JAR_MAIN_CLASS));
  }
}
