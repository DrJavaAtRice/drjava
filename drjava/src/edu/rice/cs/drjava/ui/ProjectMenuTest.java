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

package edu.rice.cs.drjava.ui;

import java.io.*;
import java.util.*;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.ui.*;
/**
 * Test functions of Project Facility working through the main frame and model.
 *
 */
public final class ProjectMenuTest extends MultiThreadedTestCase {

  private MainFrame _frame;
  
  private SingleDisplayModel _model;
  
  /**
   * A temporary file
   */
  private File _testFile = null;
  
   /* the reader which reads the test project file */
  BufferedReader reader = null;
  
  private static final String TEST_FILE_TEXT =
    "(Source\n" +
    " (/foo/subfoo/bar)\n"+
    " (/foo2/subfoo/bar)\n"+
    ")\n" +
    "(Resources\n" +
    ")\n" +
    "(BuildDir\n" +
    ")\n" + 
    "(Classpath\n" +
    ")\n" + 
    "(Jar\n" +
    ")\n";
  
  
  /**
   * Setup method for each JUnit test case.
   */
  public void setUp() throws IOException {
    _testFile = File.createTempFile("_test", "pjt");
    
    reader = new BufferedReader(new FileReader(_testFile));
    BufferedWriter w = new BufferedWriter(new FileWriter(_testFile));
    w.write(TEST_FILE_TEXT);
    w.close();
    
    _frame = new MainFrame();
    _frame.pack();
    
    _model = _frame.getModel();
    super.setUp();
  }

  public void tearDown() throws IOException {
    super.tearDown();
    _testFile.delete();
    _frame.dispose();
    _testFile = null;
    _model = null;
    _frame = null;
    System.gc();
  }
  
  public void testSetBuildDirectory() {
    //test set build directory when not in project mode
    File f = new File("");
    _model.setBuildDirectory(f);
    assertEquals("Build directory should not have been set",null,_model.getBuildDirectory());
    
    try {
      _model.openProject(_testFile);
    }
    catch(IOException ioe) {
      fail("Should not have thrown an IOException when opening a temporary file");
    }
    
    assertEquals("Build directory should not have been set",null,_model.getBuildDirectory());
    
    _model.setBuildDirectory(f);
    assertEquals("Build directory should have been set",f,_model.getBuildDirectory());
    
  }
  
  public void testCloseAllClosesProject() {
    try {
      _model.openProject(_testFile);
    }
    catch(IOException ioe) {
      fail("Should not have thrown an IOException when opening a temporary file");
    }
    assertTrue("Project should have been opened",_model.isProjectActive());
    _frame.closeAll();
    assertFalse("Project should have been closed",_model.isProjectActive());
    
  }
  
}