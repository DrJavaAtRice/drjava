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

package edu.rice.cs.drjava.config;

import edu.rice.cs.drjava.DrJava;
import java.util.HashSet;
import java.util.Iterator;

/** Class representing values that are always up-to-date and that
  * can be inserted as variables in external processes.
  *
  *  @version $Id$
  */
public abstract class EagerProperty extends DrJavaProperty {
  /** Create an eager property. */
  public EagerProperty(String name) {
    super(name);
  }
  
  /** Return the value of the property. If it is not current, update first. */
  public String getCurrent() {
    update();
    if (_value==null) { throw new IllegalArgumentException("DrJavaProperty value is null"); }
    _isCurrent = true;
    return _value;
  }

  /** Return the value. */
  public String toString() {
    return getCurrent();
  }
  
  /** Return true if the value is current. */
  public boolean isCurrent() { return true; }
  
  /** Mark the value as stale. */
  public void invalidate() {
    // nothing to do, but tell those who are listening
    invalidateOthers(new HashSet<DrJavaProperty>());
  }
  
  /** @return true if the specified property is equal to this one. */
  public boolean equals(Object other) {
    if (other == null || other.getClass() != this.getClass()) return false;
    EagerProperty o = (EagerProperty)other;
    return _name.equals(o._name) && (_isCurrent == o._isCurrent) && _value.equals(o._value);
  }
  
  /** @return the hash code. */
  public int hashCode() {
    int result;
    result = _name.hashCode();
    result = 31 * result + (_value.hashCode());
    result = 31 * result + (_isCurrent?1:0);
    return result;
  }
} 
