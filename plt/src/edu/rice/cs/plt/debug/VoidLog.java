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

/** A log that ignores all logging requests.  All method invocations return immediately. */
public class VoidLog implements Log {
  
  public static final VoidLog INSTANCE = new VoidLog();
  
  protected VoidLog() {} // allow for subclassing, if desired
  
  public void log() {}
  public void log(String message) {}
  public void log(Throwable t) {}
  public void log(String message, Throwable t) {}
  
  public void logStart() {}
  public void logStart(String message) {}
  public void logStart(String name, Object value) {}
  public void logStart(String message, String name, Object value) {}
  public void logStart(String[] names, Object... values) {}
  public void logStart(String message, String[] names, Object... values) {}
  
  
  public void logEnd() {}
  public void logEnd(String message) {}
  public void logEnd(String name, Object value) {}
  public void logEnd(String message, String name, Object value) {}
  public void logEnd(String[] names, Object... values) {}
  public void logEnd(String message, String[] names, Object... values) {}
  
  public void logStack() {}
  public void logStack(String message) {}
  
  public void logValue(String name, Object value) {}
  public void logValue(String message, String name, Object value) {}
  public void logValues(String[] names, Object... values) {}
  public void logValues(String message, String[] names, Object... values) {}
}
