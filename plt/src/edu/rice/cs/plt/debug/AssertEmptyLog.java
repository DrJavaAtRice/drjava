/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007 JavaPLT group at Rice University
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

import java.util.Date;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.lambda.Predicate2;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * A log that triggers an assertion failure whenever it is written to.  If necessary, a filter can be used so that
 * only logging that occurs in a certain location or thread leads to a failure.  (If assertions are disabled, no 
 * failures will occur, and this degenerates into a slightly more-expensive {@link VoidLog}.)
 */
public class AssertEmptyLog extends AbstractLog {
  
  public AssertEmptyLog() { super(); }
  
  public AssertEmptyLog(Predicate2<? super Thread, ? super StackTraceElement> filter) {
    super(filter);
  }

  /** Trigger an assertion failure with a descriptive message */
  protected void write(Date time, Thread thread, StackTraceElement location, SizedIterable<? extends String> messages) {
    assert false : makeMessage(time, thread, location, messages);
  }
  
  private String makeMessage(Date time, Thread thread, StackTraceElement location, 
                             SizedIterable<? extends String> messages) {
    String first = "[" + formatLocation(location) + " - " + formatThread(thread) + " - " + formatTime(time) + "]";
    return IterUtil.multilineToString(IterUtil.compose(first, messages));
  }
  
  /** Do nothing */
  protected void push() {}
  
  /** Do nothing */
  protected void pop() {}

}
