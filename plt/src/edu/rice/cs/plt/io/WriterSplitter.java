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

package edu.rice.cs.plt.io;

import java.io.Writer;
import java.io.IOException;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/** A writer that allows sending the same data to an arbitrary number of writers. */
public class WriterSplitter extends DirectWriter implements Composite {
  
  private final Iterable<? extends Writer> _writers;
  
  public WriterSplitter(Writer... writers) { _writers = IterUtil.asIterable(writers); }
  
  public WriterSplitter(Iterable<? extends Writer> writers) { _writers = writers; }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_writers) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_writers) + 1; }
  
  @Override public void close() throws IOException {
    for (Writer w : _writers) { w.close(); }
  }
  
  @Override public void flush() throws IOException {
    for (Writer w : _writers) { w.flush(); }
  }
  
  @Override public void write(char[] cbuf) throws IOException {
    for (Writer w : _writers) { w.write(cbuf); }
  }
  
  @Override public void write(char[] cbuf, int off, int len) throws IOException {
    for (Writer w : _writers) { w.write(cbuf, off, len); }
  }
  
  @Override public void write(int c) throws IOException {
    for (Writer w : _writers) { w.write(c); }
  }
  
  @Override public void write(String s) throws IOException {
    for (Writer w : _writers) { w.write(s); }
  }
  
  @Override public void write(String s, int off, int len) throws IOException {
    for (Writer w : _writers) { w.write(s, off, len); }
  }
  
}
