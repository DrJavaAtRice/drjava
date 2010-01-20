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

package edu.rice.cs.util;

import java.io.*;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/** Runnable with extended stack trace. Catches all thrown exceptions and rethrows them
  * with the stack trace of the creation site of this RunnableEST appended.
  * This makes it possible to find out where the Runnable was created.
  * Needs to override the runEST() method now.
  */
public abstract class RunnableEST implements Runnable {
  public final RunnableEST.Exception _creation = new RunnableEST.Exception("Exception thrown in runnable.");
  public void run() {
    try {
      runEST();
    }
    catch(Throwable t) {
      _creation.initCause(t);
      throw _creation;
    }
  }
  public abstract void runEST();
  
  public static class Exception extends RuntimeException {
    public Exception(String reason) {
      super(reason);
    }
    
    public String getMessage() {
        return super.getCause().getMessage();
    }

    public String getLocalizedMessage() {
        return super.getCause().getMessage();
    }

    public Throwable getCause() {
        return super.getCause();
    }
    
    public Throwable initCause(Throwable t) {
      super.initCause(t);
      StackTraceElement[] ts = t.getStackTrace();
      StackTraceElement[] cs = super.getStackTrace();
      java.util.ArrayList<StackTraceElement> list = new java.util.ArrayList<StackTraceElement>();
      for(int i = 0; i < ts.length-9; ++i) list.add(ts[i]);
      for(int i=2; i < cs.length; ++i) list.add(cs[i]);
      setStackTrace(list.toArray(new StackTraceElement[list.size()]));
      return this;
    }

    public String toString() {
        String s = getClass().getName() + ": " + super.getCause().getClass();
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
  }
}
