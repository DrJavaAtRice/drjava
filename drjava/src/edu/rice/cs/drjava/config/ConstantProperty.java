/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;

import java.util.HashSet;

/** Class representing values that are constant and that can be inserted as variables in external processes.
  * @version $Id: ConstantProperty.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class ConstantProperty extends EagerProperty {
  /** Create a constant property. */
  public ConstantProperty(String name, String value, String help) {
    super(name, help);
    if (value == null) { throw new IllegalArgumentException("DrJavaProperty value is null"); }
    _value = value;
    _isCurrent = true;
    resetAttributes();
  }
      
  /** Update the property so the value is current.
    * @param pm PropertyMaps used for substitution when replacing variables */
  public void update(PropertyMaps pm) { }
  
  /** Return the value of the property. If it is not current, update first. 
    * @param pm PropertyMaps used for substitution when replacing variables*/
  public String getCurrent(PropertyMaps pm) { return _value; }

  /** Return the value. */
  public String toString() { return _value; }
  
  /** Return true if the value is current. */
  public boolean isCurrent() { return true; }
  
  /** Mark the value as stale. */
  public void invalidate() {
    // nothing to do, but tell those who are listening
    invalidateOthers(new HashSet<DrJavaProperty>());
  }
} 
