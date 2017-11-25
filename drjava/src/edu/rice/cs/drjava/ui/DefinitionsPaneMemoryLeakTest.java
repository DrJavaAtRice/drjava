/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.FileOps;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.lang.ref.WeakReference;

import static org.netbeans.test.MemoryTestUtils.*;

/** Tests the Definitions Pane.  The MultiThreadedTestCase frameworks ensures that runtime errors in auxiliary threads
  * are detected.
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
  
  @SuppressWarnings("unused")
  private volatile DefinitionsDocument preventFinalization;

  private volatile int _finalPaneCt;
  private volatile int _finalDocCt;
  private volatile StringBuilder sbIdHashCodes;
  
  public static final int PANE_COUNT = 6;
  public static final boolean DUMP_STACK = false;
  
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
    
    OpenDefinitionsDocument[] d = new OpenDefinitionsDocument[PANE_COUNT];
    DefinitionsPane[] p = new DefinitionsPane[PANE_COUNT];
    
    for(int i = 0; i < PANE_COUNT; ++i) {
      listener.reset();
      d[i] = _model.newFile();
      try {
        java.lang.reflect.Field fTimeStamp = d[i].getClass().getSuperclass().getDeclaredField("_timestamp");
        fTimeStamp.setAccessible(true);
        fTimeStamp.setLong(d[i],System.identityHashCode(d[i]));
      } catch(Exception e) {
        println("Couldn't set _timestamp field of Document "+i+" to identity hashcode ");
        throw new RuntimeException(e);
      }
      d[i].addFinalizationListener(fldoc);
      listener.waitDocChanged();
      p[i] = _frame.getCurrentDefPane();
      p[i].addFinalizationListener(fl);
      println("Listener attached to DefinitionsPane "+i+" 0x" + hexIdentityHashCode(p[i]));
      println("\tDocument is 0x" + hexIdentityHashCode(d[i]));
      try {
        java.lang.reflect.Field fTimeStamp = d[i].getClass().getSuperclass().getDeclaredField("_timestamp");
        fTimeStamp.setAccessible(true);
        println("\tDocument's _timestamp is set to " + Long.toHexString(fTimeStamp.getLong(d[i])));
      } catch(Exception e) {
        println("Couldn't get _timestamp field of Document "+i);
        throw new RuntimeException(e);
      }
      assertEquals("Doc "+i+" set up correctly", d[i], p[i].getOpenDefDocument());
    }

    // print identity hash codes into a StringBuilder in case we need them later;
    // this does not create any references
    sbIdHashCodes = new StringBuilder();
    sbIdHashCodes.append("_frame = "+_frame.getClass().getName()+"@0x"+hexIdentityHashCode(_frame)+"\n");
    sbIdHashCodes.append("_model = "+_model.getClass().getName()+"@0x"+hexIdentityHashCode(_frame)+"\n");
    for(int i = 0; i < PANE_COUNT;++i) {
      sbIdHashCodes.append("p["+i+"]   = "+p[i].getClass().getName()+"@0x"+hexIdentityHashCode(p[i])+"\n");
    }
    for(int i = 0; i < PANE_COUNT;++i) {
      sbIdHashCodes.append("d["+i+"]   = "+d[i].getClass().getName()+"@0x"+hexIdentityHashCode(d[i])+"\n");
    }

    List<WeakReference<OpenDefinitionsDocument>> wd = new ArrayList<WeakReference<OpenDefinitionsDocument>>(PANE_COUNT);
    List<WeakReference<DefinitionsPane>> wp = new ArrayList<WeakReference<DefinitionsPane>>(PANE_COUNT);
    for(int i = 0; i < PANE_COUNT;++i) {
      wd.add(new WeakReference<OpenDefinitionsDocument>(d[i]));
      wp.add(new WeakReference<DefinitionsPane>(p[i]));
    }
    
    /* All the panes have a listener, so lets close all files */
//    Utilities.show("Waiting to start");
    for(int i = 0; i < PANE_COUNT; ++i) {
      p[i] = null;
      d[i] = null;
    }
    
    Utilities.invokeAndWait(new Runnable() { public void run() { _model.closeAllFiles(); } });
    Utilities.clearEventQueue();
    
    assertEquals("All files closed", PANE_COUNT+1, listener.getClosedCt()); // includes for initial open file
    
    _model.newFile();  // create a new document and pane for the model to hold as active.
    Utilities.invokeAndWait(new Runnable() { public void run() { _model.closeAllFiles(); } });
    Utilities.clearEventQueue();
    
    // The following is probably overkill because it presumably only closes the scratch document created by
    // when all open files were closed.  But we were getting occasional test failures with one or9ginal document
    // remaining uncollected.
    Utilities.invokeAndWait(new Runnable() { public void run() { _model.closeAllFiles(); } });
    Utilities.clearEventQueue();
    
    for(int i = 0; i < PANE_COUNT; ++i) {
      assertGC("Document "+i+" leaked", wd.get(i));
      assertGC("Pane "+i+" leaked", wp.get(i));
    }
  }
    
  
  @SuppressWarnings("unused")
public void testDocumentPaneMemoryLeak() throws InterruptedException, IOException {
    println("---- testDocumentPaneMemoryLeak ----");

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
    while (ct < 10 && (_finalDocCt < PANE_COUNT || _finalPaneCt < PANE_COUNT));

    if (DUMP_STACK && (ct == 10)) {
      // if we fail with a garbage collection problem, dump heap
      boolean isEnabled = LOG.isEnabled();
      LOG.setEnabled(true);
      LOG.log(sbIdHashCodes.toString());
      try { LOG.log("heap dump in "+dumpHeap()); }
      catch(Exception e) {
        println("Could not dump heap.");
        e.printStackTrace(System.err);
      }
      LOG.setEnabled(isEnabled);
      
      // Note: see http://www.concurrentaffair.org/2010/06/03/not-a-memory-leak-but-not-finalized/
      //
      // We are not using finalization to check for memory leaks anymore. We are using the assertGC
      // method instead. Finalization is a fundamentally flawed way of checking for garbage collection.
      // 
      // The creation context stores the stack trace when the instance is created, and I can tell
      // that one of these documents was created when DrJava was initially started, and the other after
      // all documents have been closed. These aren't the documents that need to be garbage-collected.
      //
      // --Mathias
      
//      assertEquals("Failed to reclaim all panes; panes left = " + (PANE_COUNT - _finalPaneCt) + "; docs left = " + 
//                   (PANE_COUNT - _finalDocCt), PANE_COUNT, _finalPaneCt);
//      assertEquals("Failed to reclaim all documents; panes left = " + (PANE_COUNT - _finalPaneCt) + "; docs left = " + 
//                   (PANE_COUNT - _finalDocCt), PANE_COUNT, _finalDocCt);
    }

    if (ct > 1) println("testDocumentPaneMemoryLeak required " + ct + " iterations");

//    assertEquals("all the defdocs should have been garbage collected", PANE_COUNT, _finalDocCt);
//    assertEquals("all the defpanes should have been garbage collected", PANE_COUNT,  _finalPaneCt);    
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
  
  /** Dumps the current heap to a file. 
   * @return the newly-created file
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
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
  
  /** @param o object for which to get the hash code
   * @return the identity hash code in hex. 
   */
  public static String hexIdentityHashCode(Object o) {
    return Integer.toHexString(System.identityHashCode(o));
  }
  
  public static void println(String s) {
    if (DUMP_STACK) System.err.println(s);
    LOG.log(s);
  }
}

