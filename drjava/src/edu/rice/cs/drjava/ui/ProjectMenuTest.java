/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.project.DocFile;
import edu.rice.cs.drjava.project.MalformedProjectFileException;
import edu.rice.cs.drjava.project.ProjectFileIR;
import edu.rice.cs.drjava.project.ProjectFileParserFacade;

import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.*;
import java.util.List;

/** Test functions of Project Facility working through the main frame and model. */
public final class ProjectMenuTest extends DrJavaTestCase {
  
  private volatile MainFrame _frame;
  
  private volatile SingleDisplayModel _model;
  
  /** Temporary files */
  private volatile File _base;
  private volatile File _parent;
  private volatile File _srcDir;
  private volatile File _auxFile;
  private volatile File _projFile;
  private volatile File _file1;
  private volatile File _file2;
  
  /* The reader which reads the test project file */
  volatile BufferedReader reader = null;
  
  private volatile String _projFileText = null;
  
  /** Invokes setUp() in MultiThreadedTestCase.  Accessible from anonymous inner classes. */
  private void superSetUp() throws Exception { super.setUp(); }
  
  /** Setup method for each JUnit test case in this Test class.
    * @throws Exception  This convention is mandated by the JUnit TestClass which is an ancestor of this class. 
    */
  public void setUp() throws Exception {
    // Perform Swing setup in event thread because the event thread is ALREADY running
    superSetUp(); // super.setUp() should be called first; contains an embedded invokeAndWait
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          // create temp directory for this test
          _base = new File(System.getProperty("java.io.tmpdir")).getCanonicalFile();
          _parent = IOUtil.createAndMarkTempDirectory("proj", "", _base);
          _srcDir = new File(_parent, "src");
          _srcDir.mkdir(); // create the src directory
          
          // create project in a directory with an auxiliary file outside of it
          _auxFile = File.createTempFile("aux", ".scala").getCanonicalFile();
          _projFile = new File(_parent, "test.pjt");
          
          _file1 = new File(_srcDir, "test1.scala");
          IOUtil.writeStringToFile(_file1, "");  // create dummy file
          _file2 = new File(_srcDir, "test2.scala");
          IOUtil.writeStringToFile(_file2, "");// create dummy file
          
//    System.err.println("test1.java and test1.java created");
          
          _projFileText =
            ";; DrJava project file.  Written with build: 20040623-1933\n" +
            "(source ;; comment\n" +
            "   (file (name \"src/test1.scala\")(select 32 32))" +
            "   (file (name \"src/test2.scala\")(select 32 32)))";
          
          IOUtil.writeStringToFile(_projFile, _projFileText);
          
          _frame = new MainFrame();
          _frame.pack();
          _model = _frame.getModel();
          _model.ensureJVMStarterFinished();
//          superSetUp();
        }
        // Exception e is either an IOException from a file operation or an Exception thrown by superSetUp(). 
        catch(Exception e) { throw new UnexpectedException(e); }
      }
    });
  }
  
  public void tearDown() throws Exception {
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        IOUtil.deleteOnExitRecursively(_parent);
        _auxFile.delete();
        //FileOps.deleteDirectory(_parent);
      }
    });
    
    /* Ensure that all pending actions in the event queue have completed. */  
    EventQueue current = java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue();
    while (current.peekEvent() != null) { Thread.sleep(200); }
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {   
        _frame.dispose();
        _projFile = FileOps.NULL_FILE;
        _model = null;
        _frame = null;
      }
    });
    super.tearDown();
  }
  
  public void testSetBuildDirectory() throws MalformedProjectFileException, IOException {
    
//    Utilities.showDebug("executing testSetBuildDirectory");
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
//        Test set build directory when not in project mode
          File f = FileOps.NULL_FILE;
          _model.setBuildDirectory(f);
          assertEquals("Build directory should not have been set", FileOps.NULL_FILE, _model.getBuildDirectory());
          
//          System.err.println("Opening Project File");
          _model.openProject(_projFile);

//          System.err.println("Completed Opening Project File");
//          System.err.println("Project documents are: " + _model.getProjectDocuments());
          
          assertEquals("Build directory should not have been set", FileOps.NULL_FILE, _model.getBuildDirectory());
          
          _model.setBuildDirectory(f);
          assertEquals("Build directory should have been set", f, _model.getBuildDirectory());
        }
        catch(Exception e) { throw new UnexpectedException(e); }
      }
    });
  }
  
  public void testCloseAllClosesProject() {
//    Utilities.showDebug("executing testCloseAllClosesProject");
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        try { 
          _model.openProject(_projFile);
          assertTrue("Project should have been opened", _model.isProjectActive());
          
          _frame.closeAll(); 
          assertFalse("Project should have been closed", _model.isProjectActive());
        }
        catch(Exception e) { throw new UnexpectedException(e); }
      }
    });
  }
  
  public void testSaveProject() {  
//     Utilities.showDebug("executing testSaveProject");
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        try {
          _frame.openProject(new FileOpenSelector() {
            public File[] getFiles() throws OperationCanceledException { return new File[] { _projFile}; }
          });
          
          // open a new file and make it an auxiliary file
          _frame.open(new FileOpenSelector() {
            public File[] getFiles() throws OperationCanceledException { return new File[] { _auxFile}; }
          });
          _frame._moveToAuxiliary();
          
          List<OpenDefinitionsDocument> auxDocs = _model.getAuxiliaryDocuments();
          assertEquals("One auxiliary document", 1, auxDocs.size());
          _frame.saveProject();
          _frame._closeProject();
          
          List<OpenDefinitionsDocument> docs = _model.getOpenDefinitionsDocuments();
          assertEquals("One empty document remaining", 1, docs.size());
          assertEquals("Name is (Untitled)", "(Untitled)", _model.getActiveDocument().toString());
          
          ProjectFileIR pfir = ProjectFileParserFacade.ONLY.parse(_projFile);
          DocFile[] src = pfir.getSourceFiles();
//          System.err.println(Arrays.toString(src));
          DocFile[] aux = pfir.getAuxiliaryFiles();
//          System.err.println(Arrays.toString(aux));
          assertEquals("Number of saved src files", 2, src.length);
          assertEquals("Number of saved aux files", 1, aux.length);
          assertEquals("wrong name for _file2", _file2.getCanonicalPath(), src[1].getCanonicalPath()); // assumes same (not reverse) order
          assertEquals("Wrong name for _file1", _file1.getCanonicalPath(), src[0].getCanonicalPath());
          assertEquals("Wrong aux file", _auxFile.getCanonicalPath(), aux[0].getCanonicalPath());
        }
        catch(Exception e) { throw new UnexpectedException(e); }
      }
    });
  }
}
