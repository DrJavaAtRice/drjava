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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.MultiThreadedTestCase;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.FileOps;
import static edu.rice.cs.drjava.model.GlobalModelTestCase.FileSelector;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import java.util.Date;

/** Tests the Definitions Pane
  * @version $Id$
  */
public final class DefinitionsPaneMemoryLeakTest extends MultiThreadedTestCase {
  File _tempDir;

  private volatile MainFrame _frame;    
  /** Setup method for each JUnit test case. */
  public void setUp() throws Exception {
    super.setUp();
    
    String user = System.getProperty("user.name");
    _tempDir = /* IOUtil.createAndMarkTempDirectory */ FileOps.createTempDirectory("DrJava-test-" + user /*, ""*/);
    
    /* The following use of invokeAndWait has been motivated by occasional test failures in set up (particularly in
     * MainFrame creation and packing) among different test methods in this test class. */
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        DrJava.getConfig().resetToDefaults();
        _frame = new MainFrame();
        _frame.pack(); 
      }
    });
  }
  
  public void tearDown() throws Exception {
    Utilities.invokeLater(new Runnable() {
      public void run() {
        _frame.dispose();
        _frame = null;
      }
    });
    Utilities.clearEventQueue();
    super.tearDown();
  }
  
  private volatile DefinitionsDocument preventFinalization;

  private volatile int _finalPaneCt;
  private volatile int _finalDocCt;
  
  /* Isolates the creation and destruction of the simulated DrJava session from the code that monitors
   * leaks.  Any cached references created for this stack frame are flushed on exit.  We don't know that
   * such references potentially exist but we could not rule it out given occasional failures of this
   * the leak test. */
  private void runIsolatedDrJavaSession() throws InterruptedException, IOException {
    DocChangeListener listener = new DocChangeListener();
    
    _finalPaneCt = 0;
    _finalDocCt = 0;
    
    FinalizationListener<DefinitionsPane> fl = new FinalizationListener<DefinitionsPane>() {
      public void finalized(FinalizationEvent<DefinitionsPane> e) { _finalPaneCt++; }
    };
    
    FinalizationListener<DefinitionsDocument> fldoc = new FinalizationListener<DefinitionsDocument>() {
      public void finalized(FinalizationEvent<DefinitionsDocument> e) { _finalDocCt++; }
    };
    
    final SingleDisplayModel _model = _frame.getModel();
    _model.addListener(listener);
    
    listener.reset();
    OpenDefinitionsDocument d1 = _model.newFile();
//    try {
//      java.lang.reflect.Field fTimeStamp = d1.getClass().getSuperclass().getDeclaredField("_timestamp");
//      fTimeStamp.setAccessible(true);
//      fTimeStamp.setLong(d1,System.identityHashCode(d1));
//    } catch(Exception e) { throw new RuntimeException(e); }
    d1.addFinalizationListener(fldoc);
    listener.waitDocChanged();
    DefinitionsPane p1 = _frame.getCurrentDefPane();
    p1.addFinalizationListener(fl);
//    System.err.println("Listener attached to DefintionsPane@" + p1.hashCode());
    assertEquals("Doc1 setup correctly", d1, p1.getOpenDefDocument());

    listener.reset();
    OpenDefinitionsDocument d2 = _model.newFile();
//    try {
//      java.lang.reflect.Field fTimeStamp = d2.getClass().getSuperclass().getDeclaredField("_timestamp");
//      fTimeStamp.setAccessible(true);
//      fTimeStamp.setLong(d2,System.identityHashCode(d1));
//    } catch(Exception e) { throw new RuntimeException(e); }
    d2.addFinalizationListener(fldoc);
    listener.waitDocChanged();
    DefinitionsPane p2 = _frame.getCurrentDefPane();
    p2.addFinalizationListener(fl);
//    System.err.println("Listener attached to DefintionsPane@" + p2.hashCode());
    assertEquals("Doc2 setup correctly", d2, p2.getOpenDefDocument());
    
    listener.reset();
    OpenDefinitionsDocument d3 = _model.newFile();
//    try {
//      java.lang.reflect.Field fTimeStamp = d3.getClass().getSuperclass().getDeclaredField("_timestamp");
//      fTimeStamp.setAccessible(true);
//      fTimeStamp.setLong(d3,System.identityHashCode(d1));
//    } catch(Exception e) { throw new RuntimeException(e); }
    d3.addFinalizationListener(fldoc);
    listener.waitDocChanged();
    DefinitionsPane p3 = _frame.getCurrentDefPane();
    p3.addFinalizationListener(fl);
//    System.err.println("Listener attached to DefintionsPane@" + p3.hashCode()); 
    assertEquals("Doc3 setup correctly", d3, p3.getOpenDefDocument());
       
    listener.reset();
    OpenDefinitionsDocument d4 = _model.newFile();
//    try {
//      java.lang.reflect.Field fTimeStamp = d4.getClass().getSuperclass().getDeclaredField("_timestamp");
//      fTimeStamp.setAccessible(true);
//      fTimeStamp.setLong(d4,System.identityHashCode(d1));
//    } catch(Exception e) { throw new RuntimeException(e); }
    d4.addFinalizationListener(fldoc);
    listener.waitDocChanged();
    DefinitionsPane p4 = _frame.getCurrentDefPane();
    p4.addFinalizationListener(fl);
//    System.err.println("Listener attached to DefintionsPane@" + p4.hashCode());
    assertEquals("Doc4 setup correctly", d4, p4.getOpenDefDocument());
        
    listener.reset();
    OpenDefinitionsDocument d5 = _model.newFile();
//    try {
//      java.lang.reflect.Field fTimeStamp = d5.getClass().getSuperclass().getDeclaredField("_timestamp");
//      fTimeStamp.setAccessible(true);
//      fTimeStamp.setLong(d5,System.identityHashCode(d1));
//    } catch(Exception e) { throw new RuntimeException(e); }
    d5.addFinalizationListener(fldoc);
    listener.waitDocChanged();
    DefinitionsPane p5 = _frame.getCurrentDefPane();
    p5.addFinalizationListener(fl);
//    System.err.println("Listener attached to DefintionsPane@" + p5.hashCode()); 
    assertEquals("Doc5 setup correctly", d5, p5.getOpenDefDocument());   
    
    listener.reset();
    OpenDefinitionsDocument d6 = _model.newFile();
//    try {
//      java.lang.reflect.Field fTimeStamp = d6.getClass().getSuperclass().getDeclaredField("_timestamp");
//      fTimeStamp.setAccessible(true);
//      fTimeStamp.setLong(d6,System.identityHashCode(d1));
//    } catch(Exception e) { throw new RuntimeException(e); }
    d6.addFinalizationListener(fldoc);
    listener.waitDocChanged();
    DefinitionsPane p6 = _frame.getCurrentDefPane();
    p6.addFinalizationListener(fl);
//    System.err.println("Listener attached to DefintionsPane@" + p6.hashCode()); 
    assertEquals("Doc6 setup correctly", d6, p6.getOpenDefDocument()); 

    // print identity hash codes into a StringBuilder in case we need them later;
    // this does not create any references
//    StringBuilder sbIdHashCodes = new StringBuilder();
//    sbIdHashCodes.append("_frame = 
//      "+_frame.getClass().getName()+"@0x"+Integer.toHexString(System.identityHashCode(_frame))+"\n");
//    sbIdHashCodes.append("_model = 
//      "+_model.getClass().getName()+"@0x"+Integer.toHexString(System.identityHashCode(_frame))+"\n");
//    sbIdHashCodes.append("p1     = "+p1.getClass().getName()+"@0x"+Integer.toHexString(System.identityHashCode(p1))+"\n");
//    sbIdHashCodes.append("p2     = "+p2.getClass().getName()+"@0x"+Integer.toHexString(System.identityHashCode(p2))+"\n");
//    sbIdHashCodes.append("p3     = "+p3.getClass().getName()+"@0x"+Integer.toHexString(System.identityHashCode(p3))+"\n");
//    sbIdHashCodes.append("p4     = "+p4.getClass().getName()+"@0x"+Integer.toHexString(System.identityHashCode(p4))+"\n");
//    sbIdHashCodes.append("p5     = "+p5.getClass().getName()+"@0x"+Integer.toHexString(System.identityHashCode(p5))+"\n");
//    sbIdHashCodes.append("p6     = "+p6.getClass().getName()+"@0x"+Integer.toHexString(System.identityHashCode(p6))+"\n");
//    sbIdHashCodes.append("d1     = "+d1.getClass().getName()+"@0x"+Integer.toHexString(System.identityHashCode(d1))+"\n");
//    sbIdHashCodes.append("d2     = "+d2.getClass().getName()+"@0x"+Integer.toHexString(System.identityHashCode(d2))+"\n");
//    sbIdHashCodes.append("d3     = "+d3.getClass().getName()+"@0x"+Integer.toHexString(System.identityHashCode(d3))+"\n");
//    sbIdHashCodes.append("d4     = "+d4.getClass().getName()+"@0x"+Integer.toHexString(System.identityHashCode(d4))+"\n");
//    sbIdHashCodes.append("d5     = "+d5.getClass().getName()+"@0x"+Integer.toHexString(System.identityHashCode(d5))+"\n");
//    sbIdHashCodes.append("d6     = "+d6.getClass().getName()+"@0x"+Integer.toHexString(System.identityHashCode(d6)));
    
    // all the panes have a listener, so lets close all files
//    Utilities.show("Waiting to start");
    p1 = p2 = p3 = p4 = p5 = p6 = null;
    d1 = d2 = d3 = d4 = d5 = d6 = null;
//    _model.newFile();  // create a new document and pane for the model to hold as active.
    
    Utilities.invokeAndWait(new Runnable() { public void run() { _model.closeAllFiles(); } });
    Utilities.clearEventQueue();
    
    assertEquals("All files closed", 7, listener.getClosedCt());  // 7 includes for initial open file
    // The following is probably overkill because it presumably only closes the scratch document created by
    // when all open files were closed.  But we were getting occasional test failures with one or9ginal document
    // remaining uncollected.
    Utilities.invokeAndWait(new Runnable() { public void run() { _model.closeAllFiles(); } });
    Utilities.clearEventQueue();
  }
    
  
  public void testDocumentPaneMemoryLeak() throws InterruptedException, IOException {
    // _model has been setUp
    runIsolatedDrJavaSession();
    
    int ct = 0;
    do {  
      // make sure that the event queue is empty
      Utilities.clearEventQueue();
      Utilities.clearEventQueue();
      
      System.gc();
      System.runFinalization();
      System.gc();
      ct++; 
    }
    while (ct < 10 && (_finalDocCt < 6 || _finalPaneCt < 6));

//    if (ct == 10) {
//      // if we fail with a garbage collection problem, dump heap
//      LOG.setEnabled(true);
////      LOG.log(sbIdHashCodes.toString());
//      try { LOG.log("heap dump in "+dumpHeap()); }
//      catch(Exception e) {
//        System.err.println("Could not dump heap.");
//        e.printStackTrace(System.err);
//      }
//      
//      fail("Failed to reclaim all documents; panes left = " + (6 - _finalPaneCt) + "; docs left = " + 
//           (6 - _finalDocCt));
//    }

    if (ct > 1) System.out.println("testDocumentPaneMemoryLeak required " + ct + " iterations");

    assertEquals("all the defdocs should have been garbage collected", 6, _finalDocCt);
    assertEquals("all the defpanes should have been garbage collected", 6,  _finalPaneCt);    
  }
  
  static class DocChangeListener extends DummyGlobalModelListener {
    private Object lock = new Object();
    private boolean docChanged = false;
    private int closedCt = 0;
    
    @Override public void activeDocumentChanged(OpenDefinitionsDocument active) {
      synchronized(lock) { 
        docChanged = true;
        lock.notifyAll();
      }
    }
    public void waitDocChanged() throws InterruptedException {
      synchronized(lock) {
        while (! docChanged) lock.wait();
      }
    }
    public void fileClosed(OpenDefinitionsDocument d) { closedCt++; }
    public void reset() { 
      docChanged = false; 
      closedCt = 0;
    }
    public int getClosedCt() { return closedCt; }
  }
  
  public static final edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("heap.log",false);
  
  /** Dumps the current heap to a file. */
  public static File dumpHeap() throws IOException, InterruptedException {
    String javaHome = System.getenv("JAVA_HOME");
    char SEP = File.separatorChar;
    
    // try jps first
    File jps = new File(javaHome+SEP+"bin"+SEP+"jps");
    // if that doesn't work, try jps.exe
    if (!jps.exists()) jps = new File(javaHome+SEP+"bin"+SEP+"jps.exe");
    
    // execute jps
    ProcessBuilder pb = new ProcessBuilder(jps.getAbsolutePath());
    LOG.log(java.util.Arrays.toString(pb.command().toArray()));
    Process jpsProc = pb.start();
    jpsProc.waitFor();
    LOG.log("jps returned "+jpsProc.exitValue());
    
    // read the output of jps
    BufferedReader br = new BufferedReader(new InputStreamReader(jpsProc.getInputStream()));
    Integer pid = null;
    String line = null;
    while((pid == null) && (line=br.readLine()) != null) {
      LOG.log(line);
      // find the PID of JUnitTestRunner, i.e. the PID of the current process
      if (line.indexOf("JUnitTestRunner")>=0) {
        pid = new Integer(line.substring(0,line.indexOf(' ')));
      }
    }
    if (pid == null) throw new FileNotFoundException("Could not detect PID");
    LOG.log("PID is "+pid);
    
    // try jmap first
    File jmap = new File(javaHome+SEP+"bin"+SEP+"jmap");
    // if that doesn't work, try jmap.exe
    if (! jmap.exists()) jmap = new File(javaHome+SEP+"bin"+SEP+"jmap.exe");
    
    // execute jmap -heap:format=b PID
    pb = new ProcessBuilder(jmap.getAbsolutePath(),
                            "-heap:format=b",
                            pid.toString());
    LOG.log(java.util.Arrays.toString(pb.command().toArray()));
    Process jmapProc = pb.start();
    jmapProc.waitFor();
    LOG.log("jmap returned "+jmapProc.exitValue());
    
    // read the output of jmap
    br = new BufferedReader(new InputStreamReader(jmapProc.getInputStream()));
    while((line=br.readLine()) != null) {
      LOG.log(line);
    }
    
    // rename the file 
    File dump = new File("heap.bin");
    if (!dump.exists()) { throw new FileNotFoundException("heap.bin not found"); }
    File newDump = new File("heap-DefinitionsPaneTest-" + pid + "-" + System.currentTimeMillis() + ".bin");
    dump.renameTo(newDump);
    return newDump;
  }
}

