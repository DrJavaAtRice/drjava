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

/** Test cases for {@link Log}.
 *  @version $Id: FileOpsTest.java 3896 2006-06-26 21:19:25Z rcartwright $
 */
public class LogTest extends MultiThreadedTestCase {
  
  static final String FILENAME_PREFIX = "$#%#$";
  static final File FILE1 = new File(FILENAME_PREFIX + "logtest1.txt");
  static final File FILE2 = new File(FILENAME_PREFIX + "logtest2.txt");
  static final File FILE3 = new File(FILENAME_PREFIX + "logtest3.txt");
  
  static final int SHORT_TIME = 10000;  // few seconds in milliseconds
  
  static final int DATE_END = 28;  // the ending index of the date field in a log entry
  
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
  
  public void setUp() throws Exception {
    super.setUp();
    
    // Clear log files if they exist, so beginning of files are written by these tests
    new FileWriter(FILE1).close();
    new FileWriter(FILE2).close();
    new FileWriter(FILE3).close();
  }
  
  public void tearDown() throws Exception {
    // Delete log files
    /* Note: this code does not currently delete the files.
     * TO DO: Find a way to delete these files so the build directory does not contain more clutter.
     */
    FILE1.deleteOnExit();
    FILE2.deleteOnExit();
    FILE3.deleteOnExit();
    
    super.tearDown();
  }
  
  /** Parses a date printed by Date.toString(); returns null if there is a parse error. */
  @SuppressWarnings("deprecation")
  private static Date parse(String s) {
    try { return new Date(Date.parse(s.substring(0, DATE_END)));  }  // the undeprecated version of parse DOES NOT WORK
    catch(RuntimeException e) { return null; }  // either IllegalArgument or StringIndexOutOfBounds
  }
  
  /** Adds a couple of generic messages to a log, and then tests to make sure they are all correct, in the correct order,
    * and their timestamps are within the past few seconds.
    */
  public void testLog() throws IOException {
    Log log1 = new Log(FILE1, true);
    log1.log("Message 1");
    log1.log("Message 2");
    log1.log("Message 3");
    
    BufferedReader fin = new BufferedReader(new FileReader(FILE1));
    Date earlier = new Date(new Date().getTime() - SHORT_TIME);
    Date now = new Date();
    
    String s0 = fin.readLine();
//    System.err.println("s0 = " + s0);
//    System.err.println("s0 converted to millis " + parse(s0));
//    System.err.println("Current time in millis is: " + System.currentTimeMillis());
    Date time0 = parse(s0);
    assertTrue("Log opened within last few seconds", time0.compareTo(earlier) >= 0 && time0.compareTo(now) <= 0);
    assertEquals("Log open message", "Log '" + FILE1.getName() + "' opened", s0.substring(30, 60));
    
    String s1 = fin.readLine();
    Date time1 = parse(s1);
    assertTrue("Date of message 1 within last few seconds", time1.compareTo(earlier) >= 0 && time1.compareTo(now) <= 0);
    assertEquals("Log message 1", "Message 1", s1.substring(30));
    
    String s2 = fin.readLine();
    Date time2 = parse(s2);
    assertTrue("Date of message 2 within last few seconds", time2.compareTo(earlier) >= 0 && time2.compareTo(now) <= 0);
    assertEquals("Log message 2", "Message 2", s2.substring(30));
    
    String s3 = fin.readLine();
    Date time3 = parse(s3);
    assertTrue("Date of message 3 within last few seconds", time3.compareTo(earlier) >= 0 && time3.compareTo(now) <= 0);
    assertEquals("Log message 3", "Message 3", s3.substring(30));
  
    fin.close();
  }
  
  /** Tests the Exception printing methods in the Log file by throwing two exceptions and using the two types of log 
    * methods (one with the Throwable itself and the other with the the StackTraceElement[])
    */
  public void testExceptionPrinting() throws IOException {
    Log log2 = new Log(FILE2, true);
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
    
    BufferedReader fin = new BufferedReader(new FileReader(FILE2));
    Date earlier = new Date(new Date().getTime() - SHORT_TIME);
    Date now = new Date();
    
    String s0 = fin.readLine();
    Date time0 = parse(s0);
    assertTrue("Log opened within last few seconds", time0.compareTo(earlier) >= 0 && time0.compareTo(now) <= 0);
    assertEquals("Log open message", "Log '" + FILE2.getName() + "' opened", s0.substring(30, 60));
    
    String s1 = fin.readLine();
    Date time1 = parse(s1);
    assertTrue("Date of message 1 within last few seconds", time1.compareTo(earlier) >= 0 && time1.compareTo(now) <= 0);
    assertEquals("Log message 1", "Message 1", s1.substring(30));
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
    assertEquals("Log message 2", "Message 2", s2.substring(30));
    assertEquals("Log exception 2 (trace line 1)", 
                 "edu.rice.cs.util.LogTest.testExceptionPrinting", fin.readLine().substring(0,46));

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

    Log log3 = new Log(FILE3, true);
    Random r = new Random();
    Thread[] threads = new Thread[NUM_THREADS];
    for (int i = 0; i < NUM_THREADS; i++) threads[i] = new LogTestThread(log3, r.nextInt(DELAY));
    for (int i = 0; i < NUM_THREADS; i++) threads[i].start();
    for (int i = 0; i < NUM_THREADS; i++) threads[i].join();
   
    BufferedReader fin = new BufferedReader(new FileReader(FILE3));
    Date earlier = new Date(new Date().getTime() - SHORT_TIME);
    Date now = new Date();
    
    String s0 = fin.readLine();
    Date time0 = parse(s0);
    assertTrue("Log opened within last 10 seconds", time0.compareTo(earlier) >= 0 && time0.compareTo(now) <= 0);
    assertEquals("Log open message", "Log '" + FILE3.getName() + "' opened", s0.substring(30, 60));
    
    for (int i = 0; i < NUM_THREADS; i++) {
      String s1 = fin.readLine();
      Date time1 = parse(s1);
      assertTrue("Date of message within last 10 seconds", time1.compareTo(earlier) >= 0 && time1.compareTo(now) <= 0);
      assertEquals("Log message", "Test message", s1.substring(30));
    } 
    
    fin.close();
  }
  
}

