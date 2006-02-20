/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.model.FileOpenSelector;
import edu.rice.cs.drjava.model.MultiThreadedTestCase;
import edu.rice.cs.drjava.model.OperationCanceledException;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.project.DocFile;
import edu.rice.cs.drjava.project.MalformedProjectFileException;
import edu.rice.cs.drjava.project.ProjectFileIR;
import edu.rice.cs.drjava.project.ProjectFileParser;
import edu.rice.cs.util.swing.Utilities;

import java.io.*;

/** Test functions of Project Facility working through the main frame and model. */
public final class ProjectMenuTest extends MultiThreadedTestCase {

  private MainFrame _frame;
  
  private SingleDisplayModel _model;
  
  /** Temporary files */
  private File _projDir;
  private File _auxFile;
  private File _projFile;
  private File _file1;
  private File _file2;
  
  private String _file1RelName;
  private String _file2RelName;
  
  /* The reader which reads the test project file */
  BufferedReader reader = null;
  
  private String _projFileText = null;
  
  /** Setup method for each JUnit test case. */
  public void setUp() throws Exception {
    super.setUp();

    // create project in a directory with an auxiliary file outside of it
    _auxFile = File.createTempFile("aux", ".java");
    File auxFileParent = _auxFile.getParentFile();
    _projDir = new File(auxFileParent, "project-dir");
    _projDir.mkdir();
    _projFile = File.createTempFile("test", ".pjt", _projDir);
    _file1 = File.createTempFile("test1",".java", _projDir);
    _file2 = File.createTempFile("test2",".java", _projDir);
    
    // generate the relative path names for the files in the project file
    String temp = _file1.getParentFile().getCanonicalPath();
    _file1RelName = _file1.getCanonicalPath().substring(temp.length()+1);
    temp = _file2.getParentFile().getCanonicalPath();
    _file2RelName = _file2.getCanonicalPath().substring(temp.length()+1);

    _projFileText =
      ";; DrJava project file.  Written with build: 20040623-1933\n" +
      "(source ;; comment\n" +
      "   (file (name \""+ _file1RelName +"\")(select 32 32)(active)))\n";
    
    reader = new BufferedReader(new FileReader(_projFile));
    BufferedWriter w = new BufferedWriter(new FileWriter(_projFile));
    w.write(_projFileText);
    w.close();

    _frame = new MainFrame();
    _frame.pack();

    _model = _frame.getModel();
  }

  public void tearDown() throws Exception {
    _projFile.deleteOnExit();
    _auxFile.delete();
    _file1.delete();
    _file2.delete();
    _projDir.delete();
    _frame.dispose();
    _projFile = null;
    _model = null;
    _frame = null;
    super.tearDown();
  }
  
  public void testSetBuildDirectory() throws MalformedProjectFileException, IOException {
    
//    Utilities.showDebug("executing testSetBuildDirectory");
    
    //test set build directory when not in project mode
    File f = new File("");
    _model.setBuildDirectory(f);
    assertEquals("Build directory should not have been set", null, _model.getBuildDirectory());
    
    _model.openProject(_projFile);
    
    assertEquals("Build directory should not have been set", null, _model.getBuildDirectory());
    
    _model.setBuildDirectory(f);
    assertEquals("Build directory should have been set", f, _model.getBuildDirectory());
    
  }
  
  public void testCloseAllClosesProject()  throws MalformedProjectFileException, IOException {
    
//    Utilities.showDebug("executing testCloseAllClosesProject");
    _model.openProject(_projFile);
    
    assertTrue("Project should have been opened", _model.isProjectActive());
    _frame.closeAll();
    assertFalse("Project should have been closed", _model.isProjectActive());
  }
  
  public void testSaveProject() throws IOException, MalformedProjectFileException {
    
//     Utilities.showDebug("executing testSaveProject");
    
    _frame.openProject(new FileOpenSelector() {
      public File[] getFiles() throws OperationCanceledException { return new File[] {_projFile}; }
    });
        
    // check to make sure it transitions from flat file mode to project mode well
    _frame.open(new FileOpenSelector() {
      public File[] getFiles() throws OperationCanceledException {
        return new File[] {_file2};
      }
    });
    _frame.open(new FileOpenSelector() {
      public File[] getFiles() throws OperationCanceledException {
        return new File[] {_auxFile};
      }
    });
    
    _frame._moveToAuxiliary();
    
    _frame.saveProject();
    _frame._closeProject();
    
    ProjectFileIR pfir = ProjectFileParser.ONLY.parse(_projFile);
    DocFile[] src = pfir.getSourceFiles();
    DocFile[] aux = pfir.getAuxiliaryFiles();
    assertEquals("Number of saved src files", 2, src.length);
    assertEquals("Number of saved aux files", 1, aux.length);
    assertEquals("Wrong file name", _file1.getCanonicalPath(), src[0].getCanonicalPath());
    assertEquals("Wrong aux file", _auxFile.getCanonicalPath(), aux[0].getCanonicalPath());
  }
  
}