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
import edu.rice.cs.util.UnexpectedException;

/** Test cases for {@link Log}.
 *  @version $Id$
 */
public class LogTest extends MultiThreadedTestCase {
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

  /** Parses a date printed by Date.toString(); returns null if there is a parse error or if there is no date. */
  private static Date parse(String s) {
    int pos = s.indexOf("GMT: ");
    if (pos==-1) { return null; }
    try {
      return Log.DATE_FORMAT.parse(s.substring(0,pos+3));
    }
    catch(ParseException pe) { return null; }
  }
  
  /** Returns the string after the date; returns null if there is no date. */
  private static String getStringAfterDate(String s) {
    int pos = s.indexOf("GMT: ");
    if (pos==-1) { return null; }
    return s.substring(pos + 5);
  }
  
  /** Returns true if time0 is less than 1000 ms earlier than 'earlier',
    * and now is less than 1000 ms earlier than time0.
    * This is necessary because when we parse dates back, the millisecond part gets
    * dropped, so a date later during the same second interval might appear earlier. */
  private static boolean withinASecond(Date earlier, Date time0, Date now) {
     return (time0.getTime()-earlier.getTime()<1000) && (now.getTime()-time0.getTime()<1000);
  }
  
  /** Adds a couple of generic messages to a log, and then tests to make sure they are all correct, in the correct order,
    * and their timestamps are within the past few seconds.
    */
  public void testLog() throws IOException {
    File file1 = IOUtil.createAndMarkTempFile("logtest001",".txt");
    //File file1 = new File("logtest001.txt");

    Date earlier = new Date();

    Log log1 = new Log(file1, true);
    log1.log("Message 1");
    log1.log("Message 2");
    log1.log("Message 3");
   
    BufferedReader fin = new BufferedReader(new FileReader(file1));
    Date now = new Date();

    String s0 = fin.readLine();
    Date time0 = parse(s0);
    assertTrue("Log not opened after 'earlier' and before 'now'", withinASecond(earlier, time0, now));

    String log1OpenMsg = "Log '" + file1.getName() + "' opened: ";
    assertEquals("Incorrect log open message", log1OpenMsg , getStringAfterDate(s0).substring(0, log1OpenMsg.length()));
    
    String s1 = fin.readLine();
    Date time1 = parse(s1);
    assertTrue("Date of message 1 not after 'earlier' and before 'now'", withinASecond(earlier, time1, now));
    assertTrue("Date of message 1 not after 'log opened' and before 'now'", withinASecond(time0, time1, now));
    assertEquals("Log message 1", "Message 1", getStringAfterDate(s1));
    
    String s2 = fin.readLine();
    Date time2 = parse(s2);
    assertTrue("Date of message 2 not after 'earlier' and before 'now'", withinASecond(earlier, time2, now));
    assertTrue("Date of message 2 not after 'message 1' and before 'now'", withinASecond(time1, time2, now));
    assertEquals("Log message 2", "Message 2", getStringAfterDate(s2));
    
    String s3 = fin.readLine();
    Date time3 = parse(s3);
    assertTrue("Date of message 3 not after 'earlier' and before 'now'", withinASecond(earlier, time3, now));
    assertTrue("Date of message 3 not after 'message 2' and before 'now'", withinASecond(time2, time3, now));
    assertEquals("Log message 3", "Message 3", getStringAfterDate(s3));
  
    assertEquals("End of log expected", null, fin.readLine());
    fin.close();
  }

  /** Tests the Exception printing methods in the Log file by throwing two exceptions and using the two types of log 
    * methods (one with the Throwable itself and the other with the the StackTraceElement[])
    */
  public void testExceptionPrinting() throws IOException {
    File file2 = IOUtil.createAndMarkTempFile("logtest002",".txt");
    //File file2 = new File("logtest002.txt");

    Date earlier = new Date();

    Log log2 = new Log(file2, true);

//    System.err.println("Starting testExceptionPrinting");
    
    // Throw a couple of exceptions and log them
    try { throw new ArrayIndexOutOfBoundsException(); }
    catch (ArrayIndexOutOfBoundsException e) {
      //e.printStackTrace();
      log2.log("Message 1", e);
    }
    
    String method = null;
    try { throw new NullPointerException(); }
    catch (NullPointerException e) {
      //e.printStackTrace();
      StackTraceElement[] stes = e.getStackTrace();
      method = "\tat "+stes[0].toString();
      log2.log("Message 2", stes);
    }
    
    BufferedReader fin = new BufferedReader(new FileReader(file2));
    Date now = new Date();
    
    String s0 = fin.readLine();
    Date time0 = parse(s0);
    assertTrue("Log not opened after 'earlier' and before 'now'", withinASecond(earlier, time0, now));

    String log2OpenMsg = "Log '" + file2.getName() + "' opened: ";
    assertEquals("Incorrect log open message", log2OpenMsg , getStringAfterDate(s0).substring(0, log2OpenMsg.length()));
   
    String s1 = fin.readLine();
    Date time1 = parse(s1);
    assertTrue("Date of message 1 not after 'earlier' and before 'now'", withinASecond(earlier, time1, now));
    assertTrue("Date of message 1 not after 'log opened' and before 'now'", withinASecond(time0, time1, now));
    assertEquals("Log exception 1", "java.lang.ArrayIndexOutOfBoundsException", fin.readLine());
    
    // Since it's difficult to test the rest of the stack trace, just skip over it
    String s2;
    Date time2;
    do {
      s2 = fin.readLine();
      time2 = parse(s2);  // returns null if there is a parse error
    }
    while (time2 == null); 
    
//    System.err.println("Skipped over traceback");
    
    assertTrue("Date of message 2 not after 'earlier' and before 'now'", withinASecond(earlier, time2, now));
    assertTrue("Date of message 2 not after 'message 1' and before 'now'", withinASecond(time1, time2, now)); 
    assertEquals("Log message 2", "Message 2", getStringAfterDate(s2));
    assertEquals("Log exception 2 (trace line 1)", method, fin.readLine());

    fin.close();
  }
  
  private static final int NUM_THREADS = 50;
  private static final int DELAY = 100;
  // private static final Log ltl = new Log("logtest.txt", true);
  
  /** Attempts to test Log's behavior when called concurrently from several sources.  Spawns NUM_THREADS LogTestThreads 
    * (see above)that wait a random number between 0 and DELAY milliseconds and then log a message.  The function tests
    * to make sure that the messages and dates are all intact (if the Log was not handling concurrent requests properly,
    * the entries in the log may be corrupted).
    */
  public void testConcurrentWrites() throws IOException, InterruptedException {
    File file3 = IOUtil.createAndMarkTempFile("logtest003",".txt");
    // File file3 = new File("logtest003.txt");

    Date earlier = new Date();

    Log log3 = new Log(file3, true);
    Random r = new Random();
    Thread[] threads = new Thread[NUM_THREADS];
    for (int i = 0; i < NUM_THREADS; i++) threads[i] = new LogTestThread(log3, r.nextInt(DELAY));
    for (int i = 0; i < NUM_THREADS; i++) threads[i].start();
    for (int i = 0; i < NUM_THREADS; i++) threads[i].join();
   
    BufferedReader fin = new BufferedReader(new FileReader(file3));
    Date now = new Date();
    String s0 = fin.readLine();
    Date time0 = parse(s0);
    //ltl.log("earlier = "+earlier);
    //ltl.log("now     = "+now);
    //ltl.log("time0   = "+time0);
    assertTrue("Log not opened after 'earlier' and before 'now'", withinASecond(earlier, time0, now));

    String log3OpenMsg = "Log '" + file3.getName() + "' opened: ";
    assertEquals("Incorrect log open message", log3OpenMsg , getStringAfterDate(s0).substring(0, log3OpenMsg.length()));
    
    for (int i = 0; i < NUM_THREADS; i++) {
      String s1 = fin.readLine();
      Date time1 = parse(s1);
      assertTrue("Date of message not after 'earlier' and before 'now'", withinASecond(earlier, time1, now));
      assertTrue("Date of message not after 'previous time' and before 'now'", withinASecond(time0, time1, now));
      assertEquals("Log message", "Test message", getStringAfterDate(s1));
      time0 = time1;
    } 
    
    fin.close();
  }
  
}

