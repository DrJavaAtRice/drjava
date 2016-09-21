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

package edu.rice.cs.drjava.config;
import java.util.Vector;
import edu.rice.cs.plt.iter.IterUtil;

public class DefaultOptionMap implements OptionMap {
  
  private final Vector<OptionParser<?>> keys = new Vector<OptionParser<?>>();
  
  public <T> T getOption(OptionParser<T> o) { return o.getOption(this); }
  
  public <T> T setOption(Option<T> o, T val) {
    setOption(o);
    return o.setOption(this,val);
  }
  
  private <T> void setOption(OptionParser<T> o) { if (keys.indexOf(o) == -1) keys.add(o); }
  
  public <T> String getString(OptionParser<T> o) { return o.getString(this); }
  
  public <T> void setString(OptionParser<T> o, String s) {
    setOption(o);
    o.setString(this,s);
  }
  
  public <T> T removeOption(OptionParser<T> o) {
    keys.remove(o);
    return o.remove(this);
  }
  
  public Iterable<OptionParser<?>> keys() { return IterUtil.immutable(keys); }
  
  public String toString() {
    final StringBuilder result = new StringBuilder("\n{ ");
    
    for (OptionParser<?> key: keys) {
      result.append(key.name).append(" = ").append(getString(key)).append('\n');
    }
    
    result.append('}');
    return result.toString();
  }
}
