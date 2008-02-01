/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.util;

import junit.framework.TestCase;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.Random;

import edu.rice.cs.drjava.model.MultiThreadedTestCase;
import edu.rice.cs.plt.io.IOUtil;

/** Test cases for {@link Log}.
 *  @version $Id$
 */
public class LogTest extends MultiThreadedTestCase {
  
  static final int SHORT_TIME = 10000;  // few seconds in milliseconds
  
  static final int DATE_END = 25;  // the ending index of the date field in a log entry
  
  /** A thread class that adds a log message after sleeping a given number of milliseconds */
  private class LogTestThread extends Thread {
    
    Log _log;
    int _millis;
    
    public LogTestThread(Log log, int millis) {
      _log = log;
      _millis = millis;
    }
    
    public void run() {
      try { sleep(_millis); }
      catch (InterruptedException e ) {
        e.printStackTrace();
        fail("testConcurrent failed: sleep interrupted");
      }
      _log.log( "Test message" );
    }
    
  }
  
  /** Parses a date printed by Date.toString(); returns null if there is a parse error. */
  @SuppressWarnings("deprecation")
  private static Date parse(String s) {
    try { return new Date(Date.parse(datePrefix(s)));  }  // the undeprecated version of parse DOES NOT WORK
    catch(RuntimeException e) { return null; }  // either IllegalArgument or StringIndexOutOfBounds
  }
  
  private static int dateEnd(String s) { return s.indexOf("GMT: ") + 5; }
    
  private static String datePrefix(String s) { return s.substring(0, dateEnd(s)); }
  
  /** Adds a couple of generic messages to a log, and then tests to make sure they are all correct, in the correct order,
    * and their timestamps are within the past few seconds.
    */
  public void testLog() throws IOException {
    File file1 = IOUtil.createAndMarkTempFile("logtest001",".txt");
    Log log1 = new Log(file1, true);
    log1.log("Message 1");
    log1.log("Message 2");
    log1.log("Message 3");
    
    BufferedReader fin = new BufferedReader(new FileReader(file1));
    Date earlier = new Date(new Date().getTime() - SHORT_TIME);
    Date now = new Date();
    
    String s0 = fin.readLine();
//    System.err.println("s0 = " + s0);
//    System.err.println("s0 converted to millis " + parse(s0));
//    System.err.println("Current time in millis is: " + System.currentTimeMillis());

    Date time0 = parse(s0);
//    System.err.println("s0 = '" + s0 + "'");
//    System.err.println("time0 = " + time0);
    assertTrue("Log opened within last few seconds", time0.compareTo(earlier) >= 0 && time0.compareTo(now) <= 0);
    String log1OpenMsg = "Log '" + file1.getName() + "' opened";
    int offset = dateEnd(s0);
    
    assertEquals("Log open message", log1OpenMsg , s0.substring(offset, offset + log1OpenMsg.length()));
    
    String s1 = fin.readLine();
//    System.err.println("s1 = '" + s1 + "'");
    Date time1 = parse(s1);
    assertTrue("Date of message 1 within last few seconds", time1.compareTo(earlier) >= 0 && time1.compareTo(now) <= 0);
    assertEquals("Log message 1", "Message 1", s1.substring(dateEnd(s1)));
    
    String s2 = fin.readLine();
    Date time2 = parse(s2);
    assertTrue("Date of message 2 within last few seconds", time2.compareTo(earlier) >= 0 && time2.compareTo(now) <= 0);
    assertEquals("Log message 2", "Message 2", s2.substring(dateEnd(s2)));
    
    String s3 = fin.readLine();
    Date time3 = parse(s3);
    assertTrue("Date of message 3 within last few seconds", time3.compareTo(earlier) >= 0 && time3.compareTo(now) <= 0);
    assertEquals("Log message 3", "Message 3", s3.substring(dateEnd(s3)));
  
    fin.close();
  }
  
  /** Tests the Exception printing methods in the Log file by throwing two exceptions and using the two types of log 
    * methods (one with the Throwable itself and the other with the the StackTraceElement[])
    */
  public void testExceptionPrinting() throws IOException {
    File file2 = IOUtil.createAndMarkTempFile("logtest002",".txt");
    Log log2 = new Log(file2, true);
//    System.err.println("Starting testExceptionPrinting");
    
    // Throw a couple of exceptions and log them
    try { throw new ArrayIndexOutOfBoundsException(); }
    catch (ArrayIndexOutOfBoundsException e) {
      //e.printStackTrace();
      log2.log("Message 1", e);
    }
    
    try { throw new NullPointerException(); }
    catch (NullPointerException e) {
      //e.printStackTrace();
      log2.log("Message 2", e.getStackTrace());
    }
    
    BufferedReader fin = new BufferedReader(new FileReader(file2));
    Date earlier = new Date(new Date().getTime() - SHORT_TIME);
    Date now = new Date();
    
    String s0 = fin.readLine();
    Date time0 = parse(s0);
    assertTrue("Log opened within last few seconds", time0.compareTo(earlier) >= 0 && time0.compareTo(now) <= 0);
    String log2OpenMsg = "Log '" + file2.getName() + "' opened";
    int offset = dateEnd(s0);
    assertEquals("Log open message", log2OpenMsg, s0.substring(offset, offset + log2OpenMsg.length()));
    
    String s1 = fin.readLine();
    Date time1 = parse(s1);
    assertTrue("Date of message 1 within last few seconds", time1.compareTo(earlier) >= 0 && time1.compareTo(now) <= 0);
    assertEquals("Log message 1", "Message 1", s1.substring(dateEnd(s1)));
    assertEquals("Log exception 1", "java.lang.ArrayIndexOutOfBoundsException", fin.readLine());
    
    // Since it's difficult to test the rest of the stack trace, just skip over it
    String s2;
    Date time2;
    do {
      s2 = fin.readLine();
//      System.err.println("traceback line = " + s2);
      time2 = parse(s2);  // returns null if there is a parse error
    }
    while (time2 == null); 
    
//    System.err.println("Skipped over traceback");
    
    assertTrue("Date of message 2 within last few seconds", time2.compareTo(earlier) >= 0 && time2.compareTo(now) <= 0);
    assertEquals("Log message 2", "Message 2", s2.substring(dateEnd(s2)));
    String method = "edu.rice.cs.util.LogTest.testExceptionPrinting";
    assertEquals("Log exception 2 (trace line 1)", method, fin.readLine().substring(0, method.length()));

    fin.close();
  }
  
  private static final int NUM_THREADS = 50;
  private static final int DELAY = 100;
  
  /** Attempts to test Log's behavior when called concurrently from several sources.  Spawns NUM_THREADS LogTestThreads 
    * (see above)that wait a random number between 0 and DELAY milliseconds and then log a message.  The function tests
    * to make sure that the messages and dates are all intact (if the Log was not handling concurrent requests properly,
    * the entries in the log may be corrupted).
    */
  public void testConcurrentWrites() throws IOException, InterruptedException {
    File file3 = IOUtil.createAndMarkTempFile("logtest003",".txt");
    Log log3 = new Log(file3, true);
    Random r = new Random();
    Thread[] threads = new Thread[NUM_THREADS];
    for (int i = 0; i < NUM_THREADS; i++) threads[i] = new LogTestThread(log3, r.nextInt(DELAY));
    for (int i = 0; i < NUM_THREADS; i++) threads[i].start();
    for (int i = 0; i < NUM_THREADS; i++) threads[i].join();
   
    BufferedReader fin = new BufferedReader(new FileReader(file3));
    Date earlier = new Date(new Date().getTime() - SHORT_TIME);
    Date now = new Date();
    String s0 = fin.readLine();
    Date time0 = parse(s0);
    assertTrue("Log opened within last 10 seconds", time0.compareTo(earlier) >= 0 && time0.compareTo(now) <= 0);
    String log3OpenMsg = "Log '" + file3.getName() + "' opened";
    int offset = dateEnd(s0);
    assertEquals("Log open message", log3OpenMsg, s0.substring(offset, offset + log3OpenMsg.length()));
    
    for (int i = 0; i < NUM_THREADS; i++) {
      String s1 = fin.readLine();
      Date time1 = parse(s1);
      assertTrue("Date of message within last 10 seconds", time1.compareTo(earlier) >= 0 && time1.compareTo(now) <= 0);
      assertEquals("Log message", "Test message", s1.substring(dateEnd(s1)));
    } 
    
    fin.close();
  }
  
}

