/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
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

package edu.rice.cs.plt.debug;

/** 
 * A log allows messages and other information to be recorded during program execution.  Implementations should
 * be thread-safe.
 */
public interface Log {
  
  /** Record the current execution point (may include the current time, thread, code location, etc.) */
  public void log();
  
  /** Record the given message */
  public void log(String message);
  
  /** Record the given exception (or other throwable); may be {@code null} */
  public void log(Throwable t);
  
  /** Record the given exception (or other throwable) with a descriptive message; {@code t} may be {@code null} */
  public void log(String message, Throwable t);
  
  /**
   * Record the beginning of an execution phase.  This is useful, for example, when a method's execution
   * begins.  This call should always be followed by a corresponding invocation of {@link #logEnd()} or 
   * {@link #logEnd(String)}.
   */
  public void logStart();
  
  /**
   * Record the beginning of an execution phase with a descriptive message.  This is useful, for example, when 
   * a method's execution begins.  This call should always be followed by a corresponding invocation of 
   * {@link #logEnd()} or {@link #logEnd(String)}.
   */
  public void logStart(String message);
  
  /**
   * <p>Record the beginning of an execution phase.  This is useful, for example, when a method's execution
   * begins.  This call should always be followed by a corresponding invocation of {@link #logEnd()} or 
   * {@link #logEnd(String)}.</p>
   * <p>This version also records the name and value of a variable.  {@code value} may be {@code null}.</p>
   */
  public void logStart(String name, Object value);
  
  /**
   * <p>Record the beginning of an execution phase with a descriptive message.  This is useful, for example,
   * when a method's execution begins.  This call should always be followed by a corresponding invocation 
   * of {@link #logEnd()} or {@link #logEnd(String)}.</p>
   * <p>This version also records the name and value of a variable.  {@code value} may be {@code null}.</p>
   */
  public void logStart(String message, String name, Object value);
  
  /**
   * <p>Record the beginning of an execution phase.  This is useful, for example, when a method's execution
   * begins.  This call should always be followed by a corresponding invocation of {@link #logEnd()} or 
   * {@link #logEnd(String)}.</p>
   * <p>This version also records the name and value of a set of variables.  Any of {@code values}
   * may be {@code null}.</p>
   */
  public void logStart(String[] names, Object... values);
  
  /**
   * <p>Record the beginning of an execution phase with a descriptive message.  This is useful, for example,
   * when a method's execution begins.  This call should always be followed by a corresponding invocation
   * of {@link #logEnd()} or {@link #logEnd(String)}.</p>
   * <p>This version also records the name and value of a set of variables.  Any of {@code values}
   * may be {@code null}.</p>
   */
  public void logStart(String message, String[] names, Object... values);
  
  /**
   * Record the end of an execution phase.  This is useful, for example, when a method's execution ends.  This
   * call should always be preceded by a corresponding invocation of {@link #logStart()} or {@link #logStart(String)}.
   */
  public void logEnd();

  /**
   * Record the end of an execution phase with a descriptive message.  This is useful, for example, when 
   * a method's execution ends.  This call should always be preceded by a corresponding invocation of 
   * {@link #logStart()} or {@link #logStart(String)}.
   */
  public void logEnd(String message);
  
  /**
   * <p>Record the end of an execution phase.  This is useful, for example, when a method's execution ends.  This
   * call should always be preceded by a corresponding invocation of {@link #logStart()} or
   * {@link #logStart(String)}.</p>
   * <p>This version also records the name and value of a variable.  {@code value} may be {@code null}.</p>
   */
  public void logEnd(String name, Object value);

  /**
   * <p>Record the end of an execution phase with a descriptive message.  This is useful, for example, when 
   * a method's execution ends.  This call should always be preceded by a corresponding invocation of 
   * {@link #logStart()} or {@link #logStart(String)}.</p>
   * <p>This version also records the name and value of a variable.  {@code value} may be {@code null}.</p>
   */
  public void logEnd(String message, String name, Object value);
  
  /**
   * <p>Record the end of an execution phase.  This is useful, for example, when a method's execution ends.  This
   * call should always be preceded by a corresponding invocation of {@link #logStart()} or
   * {@link #logStart(String)}.</p>
   * <p>This version also records the name and value of a set of variables.  Any of {@code values}
   * may be {@code null}.</p>
   */
  public void logEnd(String[] names, Object... values);

  /**
   * <p>Record the end of an execution phase with a descriptive message.  This is useful, for example, when 
   * a method's execution ends.  This call should always be preceded by a corresponding invocation of 
   * {@link #logStart()} or {@link #logStart(String)}.</p>
   * <p>This version also records the name and value of a set of variables.  Any of {@code values}
   * may be {@code null}.</p>
   */
  public void logEnd(String message, String[] names, Object... values);
  
  /** Record the current thread's stack trace */
  public void logStack();
  
  /** Record the current thread's stack trace with a descriptive message */
  public void logStack(String message);
  
  /** Record the name and value of some variable or expression; {@code value} may be {@code null} */
  public void logValue(String name, Object value);

  /**
   * Record the name and value of some variable or expression with a descriptive message; {@code value}
   * may be {@code null}
   */
  public void logValue(String message, String name, Object value);

  /**
   * Record the names and values of a list of variables or expressions.  The two arrays are assumed
   * to have the same length, although implementations are encouraged to handle violations of this 
   * assumption cleanly by, for example, logging an error message.  Any member of {@code values}
   * may be {@code null}.
   */
  public void logValues(String[] names, Object... values);

  /**
   * Record the names and values of a list of variables or expressions with a descriptive message.
   * The two arrays are assumed to have the same length, although implementations are encouraged to 
   * handle violations of this assumption cleanly by, for example, logging an error message.  Any member
   * of {@code values} may be {@code null}.
   */
  public void logValues(String message, String[] names, Object... values);

}
