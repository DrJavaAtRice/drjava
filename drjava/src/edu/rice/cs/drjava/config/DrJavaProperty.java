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

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import edu.rice.cs.plt.lambda.Lambda;

/** Class representing values that can be inserted as variables in external processes.
 *  @version $Id: DrJavaProperty.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public abstract class DrJavaProperty implements Cloneable {
  /** Whether the invalidation listening mechanism has been deactivated due to an error. */
  public volatile boolean DEACTIVATED_DUE_TO_ERROR = false;
  
  /** Name of the property. Must be unique. */
  protected String _name;
  /** Value of the property. */
  protected String _value = "--uninitialized--";
  /** Is the value current? */
  protected boolean _isCurrent = false;
  /** Help page for this property. */
  protected String _help = "Help unavailable.";
  /** Map of attributes. */
  protected HashMap<String,String> _attributes = new HashMap<String,String>();
  /** Set of other properties that are listening to this property, i.e.
    * when this property is invalidated, the other properties are too. */
  protected Set<DrJavaProperty> _listening = new HashSet<DrJavaProperty>();
  
  /** Create a property. */
  public DrJavaProperty(String name, String help) {
    if (name == null) { throw new IllegalArgumentException("DrJavaProperty name is null"); }
    _name = name;
    if (help != null) { _help = help; } 
    resetAttributes();
  }

  /** Create a property. */
  public DrJavaProperty(String name, String value, String help) {
    this(name, help);
    if (value == null) { throw new IllegalArgumentException("DrJavaProperty value is null"); }
    if (help != null) { _help = help; } 
    _value = value;
    _isCurrent = true;
  }
  
  /** Return the name of the property. */
  public String getName() { return _name; }
  
  /** Return the value of the property. If it is not current, update first.
    * @param pm PropertyMaps used for substitution when replacing variables */
  public String getCurrent(PropertyMaps pm) {
    if (!isCurrent()) {
      update(pm);
      if (_value == null) { throw new IllegalArgumentException("DrJavaProperty value is null"); }
      _isCurrent = true;
    }
    return _value;
  }

  /** Return the value of the property lazily. The value may be stale.
    * @param pm PropertyMaps used for substitution when replacing variables */
  public String getLazy(PropertyMaps pm) {
    if (_value == null) { throw new IllegalArgumentException("DrJavaProperty value is null"); }
    return _value;
  }
  
  /** Update the property so the value is current. 
    * @param pm PropertyMaps used for substitution when replacing variables */
  public abstract void update(PropertyMaps pm);
  
  /** Reset attributes to their defaults. Should be overridden by properties that use attributes. */
  public void resetAttributes() { _attributes.clear(); }
  
  /** Set an attribute's value. The attribute must already exist in the table.
    * @param key name of the attribute
    * @param value new value of the attribute
    * @throws IllegalArgumentException if attribute with specified key does not already exist in table
    */
  public void setAttribute(String key, String value) {
    if (!_attributes.containsKey(key)) {
      throw new IllegalArgumentException("Attribute " + key + " not known to property " + _name);
    }
    _attributes.put(key, value);
  }
  
  /** Set all attribute values. The attributes must already exist in the table.
    * @param attrs attribute key-value pairs
    * @param replaceLambda lambda that can be used to replace the variables in a value
    * @throws IllegalArgumentException if an attribute with a specified key does not already exist in table
    */
  public void setAttributes(HashMap<String,String> attrs, Lambda<String,String> replaceLambda) {
    for(Map.Entry<String,String> e: attrs.entrySet()) {
      setAttribute(e.getKey(), replaceLambda.value(e.getValue()));
    }
  }
  
  /** Return an attribute's value.
    * @param key name of the attribute
    * @throws IllegalArgumentException if attribute with specified key does not already exist in table
    */
  public String getAttribute(String key) {
    if (!_attributes.containsKey(key)) {
      throw new IllegalArgumentException("Attribute " + key + " not known to property " + _name);
    }
    return _attributes.get(key);
  }
  
  /** Return the value, which might be stale or null. */
  public String toString() { return _value; }
  
  /** Return the value, which might be stale. */
  public String getHelp() { return _help; }

  /** Return true if the value is current. */
  public boolean isCurrent() { return _isCurrent; }
    
  /** Mark the value as stale and invalidate other properties that are listening. */
  public void invalidate() {
    _invalidate();
    invalidateOthers(new HashSet<DrJavaProperty>());
  }
  
  /** Just invalidate. */
  protected void _invalidate() { _isCurrent = false; }
  
  public DrJavaProperty listenToInvalidatesOf(DrJavaProperty other) {
    if (other == this) {
      DEACTIVATED_DUE_TO_ERROR = true;
      RuntimeException e = 
        new IllegalArgumentException("Property cannot listen for invalidation of itself. " + 
                                     "Variables for external processes will not function correctly anymore. " + 
                                     "This is a SERIOUS programming error. Please notify the DrJava team.");
      edu.rice.cs.drjava.ui.DrJavaErrorHandler.record(e);
      throw e;
    }
    other._listening.add(this);
    return this;
  }
  
  /** @return true if the specified property is equal to this one. */
  public boolean equals(Object other) {
    if (other == null || other.getClass() != this.getClass()) return false;

    DrJavaProperty o = (DrJavaProperty) other;
    return _name.equals(o._name);

  }

  /** @return the hash code. name is never mutated remains constant, so its hash code can be used. */
  public int hashCode() { return _name.hashCode(); }
  
  /** Invalidate those properties that are listening to this property.
    * @param alreadyVisited set of properties already visited, to avoid cycles. */
  protected void invalidateOthers(Set<DrJavaProperty> alreadyVisited) {
    if (DEACTIVATED_DUE_TO_ERROR) { return; }          
    if (alreadyVisited.contains(this)) {
      Iterator<DrJavaProperty> it = alreadyVisited.iterator();
      StringBuilder sb = new StringBuilder("Invalidating ");
      sb.append(getName());
      sb.append(" after already having invalidated ");
      boolean first = true;
      while (it.hasNext()) {
        if (first) { first = false; } 
        else { sb.append(", "); }
        sb.append(it.next().getName());
      }
      sb.append(". Variables for external processes will not function correctly anymore. " + 
                "This is a SERIOUS programming error. Please notify the DrJava team.");
      DEACTIVATED_DUE_TO_ERROR = true;
      RuntimeException e = new InfiniteLoopException(sb.toString());
      edu.rice.cs.drjava.ui.DrJavaErrorHandler.record(e);
      throw e;
    }
    alreadyVisited.add(this);
    Iterator<DrJavaProperty> it = _listening.iterator();
    while(it.hasNext()) {
      DrJavaProperty prop = it.next();
      prop._invalidate();
      prop.invalidateOthers(alreadyVisited);
    }
  }
  
  /** Exception thrown if an infinite loop of invalidation listening is detected. */
  public static class InfiniteLoopException extends RuntimeException {
    public InfiniteLoopException(String s) { super(s); } 
  }
} 
