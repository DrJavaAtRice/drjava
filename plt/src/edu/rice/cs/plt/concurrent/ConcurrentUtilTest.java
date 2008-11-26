/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.concurrent;

import java.io.Serializable;
import java.io.NotSerializableException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.util.Iterator;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import edu.rice.cs.plt.debug.Stopwatch;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.WrappedException;

import static edu.rice.cs.plt.concurrent.ConcurrentUtil.*;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

public class ConcurrentUtilTest extends TestCase {
//  static { edu.rice.cs.plt.debug.DebugUtil.debug = new edu.rice.cs.plt.debug.FileLog(new java.io.File("debug-log.txt")); }
  
  private static volatile Object _obj; // used to communicate between threads
  
  private void assertInRange(long lower, long higher, long actual) {
    assertTrue(lower <= actual);
    assertTrue(higher >= actual);
  }
  
  public void testSleep() {
    Stopwatch w = new Stopwatch(true);
    sleep(500); // has to be long enough to minimize variation
    long time = w.stop();
    assertInRange(300, 700, time);
    
    new DelayedInterrupter(500);
    w.start();
    sleep(1000);
    time = w.stop();
    assertInRange(300, 700, time);
  }

  public void testWork() {
    Stopwatch w = new Stopwatch(true);
    work(500);
    long time = w.stop();
    assertInRange(300, 700, time);
    
    new DelayedInterrupter(500);
    w.start();
    work(1000);
    time = w.stop();
    assertInRange(300, 700, time);
  }
  
  
  public void testRunInThread() {
    _obj = null;
    TaskController<Void> c = runInThread(new Runnable() {
      // sleep is to allow c.value() to (hopefully) be called before this completes
      public void run() { sleep(20); _obj = Thread.currentThread(); }
    });
    c.value();
    assertNotNull(_obj);
    assertNotSame(Thread.currentThread(), _obj);
  }

  
  public void testComputeInThread() {
    _obj = null;
    final CompletionMonitor monitor = new CompletionMonitor();
    Thunk<Thread> task = new Thunk<Thread>() {
      public Thread value() {
        monitor.attemptEnsureSignalled();
        _obj = Thread.currentThread();
        return Thread.currentThread();
      }
    };
    TaskController<Thread> c = computeInThread(task, false);
    assertSame(TaskController.Status.PAUSED, c.status());
    c.start();
    assertSame(TaskController.Status.RUNNING, c.status());
    monitor.signal();
    Thread t = c.value();
    assertNotNull(_obj);
    assertNotSame(Thread.currentThread(), _obj);
    assertSame(t, _obj);
  }
  
  public void testComputeInThreadWithException() {
    TaskController<String> c = computeInThread(new Thunk<String>() {
      public String value() { throw new RuntimeException("exception in task"); }
    });
    try { c.value(); fail("expected exception"); }
    catch (WrappedException e) {
      assertTrue(e.getCause() instanceof InvocationTargetException);
      assertTrue(e.getCause().getCause() instanceof RuntimeException);
      assertEquals("exception in task", e.getCause().getCause().getMessage());
    }
  }
  
  public void testComputeIncrementalInThread() {
    CompletionMonitor monitor = new CompletionMonitor();
    TrivialIncrementalTask<Integer, String> task =
      new TrivialIncrementalTask<Integer, String>(IterUtil.make(1, 2, 3, 4, 5), "done", monitor);
    IncrementalTaskController<Integer, String> c = computeInThread(task);
    
    monitor.signal();
    assertTrue(IterUtil.isEqual(IterUtil.make(1), c.intermediateValues()));
    assertSame(TaskController.Status.RUNNING, c.status());
    monitor.signal();
    assertTrue(IterUtil.isEqual(IterUtil.make(2), c.intermediateValues()));
    assertSame(TaskController.Status.RUNNING, c.status());
    c.pause();
    task.ignoreMonitor();
    sleep(100); // can't *guarantee* that the status will update, but we expect it to be reasonably responsive
    assertSame(TaskController.Status.PAUSED, c.status());
    c.start();
    assertEquals("done", c.value());
    assertSame(TaskController.Status.FINISHED, c.status());
    assertTrue(IterUtil.isEqual(IterUtil.make(3, 4, 5), c.intermediateValues()));
    assertTrue(c.intermediateValues().isEmpty());
  }
  
  private static class TrivialIncrementalTask<I, R> implements IncrementalTask<I, R> {
    private final Iterator<? extends I> _intermediates;
    private final R _finalResult;
    private final CompletionMonitor _monitor;
    private volatile boolean _useMonitor;
    public TrivialIncrementalTask(Iterable<? extends I> intermediates, R finalResult,
                                  CompletionMonitor monitor) {
      _intermediates = intermediates.iterator();
      _finalResult = finalResult;
      _monitor = monitor;
      _useMonitor = true;
    }
    public void ignoreMonitor() { _useMonitor = false; _monitor.signal(); }
    public void useMonitor() { _monitor.reset(); _useMonitor = true; }
    public boolean isFinished() { return !_intermediates.hasNext(); }
    public I step() {
      if (_useMonitor) { _monitor.attemptEnsureSignalled(); _monitor.reset(); }
      return _intermediates.next();
    }
    public R value() {
      if (_useMonitor) { _monitor.attemptEnsureSignalled(); _monitor.reset(); }
      return _finalResult;
    }
  }
  
  
  public void testComputeInProcess() {
    _obj = null;
    TaskController<String> c1 = computeInProcess(new ProcessTask1());
    assertEquals("done", c1.value());
    assertNull(_obj);
    
    _obj = null;
    TaskController<String> c2 = computeInProcess(new ProcessTask2());
    try { c2.value(); fail("expected exception"); }
    catch (WrappedException e) {
      if (e.getCause() instanceof NotSerializableException) { /* expected */ }
      else { throw e; }
    }
    assertNull(_obj);
    
    _obj = null;
    TaskController<String> c3 = computeInProcess(new ProcessTask3());
    try { c3.value(); fail("expected exception"); }
    catch (WrappedException e) {
      if (e.getCause() instanceof InvocationTargetException && e.getCause().getCause() instanceof RuntimeException) {
        assertEquals("done", e.getCause().getCause().getMessage());
      }
      else { throw e; }
    }
    assertNull(_obj);
  }
  
  private static final class ProcessTask1 implements Thunk<String>, Serializable {
    public String value() {
      _obj = "should not be visible in parent process";
      return "done";
    }
  }
  
  private static final class ProcessTask2 implements Thunk<String> /* Not serializable! */ {
    public String value() {
      _obj = "should not be visible in parent process";
      return "done";
    }
  }
  
  private static final class ProcessTask3 implements Thunk<String>, Serializable {
    public String value() {
      _obj = "should not be visible in parent process";
      throw new RuntimeException("done");
    }
  }
  
  
  public void testRunJavaProcess() throws IOException {
    String currentCP = System.getProperty("java.class.path");
    Iterable<File> currentCPFiles = IOUtil.parsePath(currentCP);
    Process p = runJavaProcess(TestProcess.class.getName(), "a", "b", "c");
    checkProcessOutput(p, currentCP, System.getProperty("user.dir"), IterUtil.make("a", "b", "c"));
    
    Process p2 = runJavaProcess(TestProcess.class.getName(), IterUtil.make("d", "e"),
                                IterUtil.compose(currentCPFiles, new File("xx")));
    checkProcessOutput(p2, currentCP + ":xx", System.getProperty("user.dir"), IterUtil.make("d", "e"));
    
    Process p3 = runJavaProcess(TestProcess.class.getName(), IterUtil.<String>empty(), File.listRoots()[0]);
    checkProcessOutput(p3, currentCP, File.listRoots()[0].getPath(), IterUtil.<String>empty());
  }

  private void checkProcessOutput(Process p, String classPath, String workingDir, Iterable<String> args) 
    throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
    try {
      assertEquals("Test process", in.readLine());
      assertTrue(TextUtil.contains(in.readLine(), classPath));
      assertEquals(workingDir, in.readLine());
      assertEquals(IterUtil.toString(args), in.readLine());
    }
    finally { in.close(); }
  }
  
  private static final class TestProcess {
    public static void main(String... args) {
      System.out.println("Test process");
      System.out.println(System.getProperty("java.class.path"));
      System.out.println(IOUtil.attemptAbsoluteFile(new File(""))); // demonstrates working dir
      System.out.println(IterUtil.asIterable(args));
    }
  }
  
}
