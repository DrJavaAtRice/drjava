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

package edu.rice.cs.util;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

/** A JUnit test case for the ClasspathVector class. */
public class ClassPathVectorTest extends DrJavaTestCase {
  
  /** Verifies the correctness of the formatting of the toString method of ClasspathVector. */
  public void test_toString() {
    ClassPathVector v = new ClassPathVector();
    assertEquals("Empty classpath", "", v.toString());
    addElement(v, "file:///jsr14.jar");
    assertEquals("One element classpath", File.separator+"jsr14.jar"+File.pathSeparator,v.toString());
    addElement(v, "file:///wherever/supercool.jar");
    String fileName = File.separator + "wherever" + File.separator + "supercool.jar";
    assertEquals("Multiple element classpath", File.separator+"jsr14.jar" + File.pathSeparator + fileName + File.pathSeparator, v.toString());
    addElement(v, "http://www.drjava.org/hosted.jar");
    assertEquals("Multiple element classpath", File.separator+"jsr14.jar" + File.pathSeparator + fileName + File.pathSeparator + File.separator + "hosted.jar" + File.pathSeparator, v.toString());
  }
  
  /** Tests the overloaded methods for translating other inputs to URLs on the fly.*/
  public void test_OverloadedAdds() {
    ClassPathVector v = new ClassPathVector();
      v.add("asdf");  // illegal path should be ignored 
      assertEquals("Nothing should be added", "", v.toString());
  }
  
  /** Tests to make sure the conversion to files is correct */
  public void test_asFileVector() throws IOException {
    ClassPathVector vu = new ClassPathVector();
    File[] files = new File[]{
      new File("folder1/folder2/file1.ext"),
      new File("folder1/folder2/file2.ext"),
      new File("folder1/folder2/file3.ext")
    };
    for (File f : files) vu.add(f);
    
    Vector<File> vf = vu.asFileVector();
    assertEquals("Size of vectors should agree", vu.size(), vf.size());
    for(int i=0; i<files.length; i++)
      assertEquals(files[i].getCanonicalFile(), vf.get(i));
  }
  
  private void addElement(ClassPathVector v, String element) {
    try {
      v.add(new URL(element));
    } catch(MalformedURLException e) {
      fail("Mysterious MalformedURLException. Probably not our fault.");
    }
  }
  
}
