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

/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.tree;

import java.beans.*;
import java.util.*;

import edu.rice.cs.plt.object.ObjectUtil;

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents the nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.1 - 1999/11/12
 */

public abstract class Node implements SourceInfo.Wrapper {
  /**
   * The sourceInfo property name
   */
  public final static String SOURCE_INFO = "sourceInfo";
  
  private final Map<String,Object> properties;
  private SourceInfo sourceInfo;
  
  
  /**
   * The support for the property change mechanism
   */
  private PropertyChangeSupport propertyChangeSupport;
  
  protected Node(SourceInfo si) {
    assert si != null;
    sourceInfo = si;
    propertyChangeSupport = new PropertyChangeSupport(this);
    properties = new HashMap<String, Object>(11);
  } 
  
  /** Returns the sourceInfo. */
  public SourceInfo getSourceInfo() {
    return sourceInfo;
  }
  
  /**
   * Sets the filename
   */
  public void setSourceInfo(SourceInfo si) {
    assert si != null;
    firePropertyChange(SOURCE_INFO, sourceInfo, sourceInfo = si);
  }
  
  
  
  // Properties support //////////////////////////////////////////////////
  
  /**
   * Sets the value of a property
   * @param name  the property name
   * @param value the new value to set
   */
  public void setProperty(String name, Object value) {
    firePropertyChange(name, properties.put(name, value), value);
  }
  
  /**
   * Returns the value of a property
   * @param name  the property name
   * @return null if the property was not previously set
   */
  public Object getProperty(String name) {
    if (!properties.containsKey(name)) { 
      throw new IllegalStateException("Property '" + name + "' is not initialized");
    }
    return properties.get(name);
  }
  
  /**
   * Returns the defined properties for this node.
   * @return a set of string
   */
  public Set<String> getProperties() {
    return properties.keySet();
  }
  
  /**
   * Returns true if a property is defined for this node
   * @param name the name of the property
   */
  public boolean hasProperty(String name) {
    return properties.containsKey(name);
  }
  
  /** Change the names of all properties by prefixing each name with the given string. */
  public void archiveProperties(String prefix) {
    Map<String, Object> newProps = new HashMap<String, Object>();
    for (Map.Entry<String, Object> e : properties.entrySet()) { newProps.put(prefix + e.getKey(), e.getValue()); }
    properties.clear();
    properties.putAll(newProps);
  }
  
  /**
   * Adds a PropertyChangeListener to the listener list.
   * The listener is registered for all properties.
   * @param listener  The PropertyChangeListener to be added
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }
  
  /**
   * Removes a PropertyChangeListener from the listener list.
   * This removes a PropertyChangeListener that was registered
   * for all properties.
   * @param listener  The PropertyChangeListener to be removed
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }
  
  /**
   * Adds a PropertyChangeListener for a specific property.  The listener
   * will be invoked only when a call on firePropertyChange names that
   * specific property.
   * @param propertyName  The name of the property to listen on.
   * @param listener  The PropertyChangeListener to be added
   */
  public void addPropertyChangeListener(String propertyName,
                                        PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }
  
  /**
   * Removes a PropertyChangeListener for a specific property.
   * @param propertyName  The name of the property that was listened on.
   * @param listener  The PropertyChangeListener to be removed
   */
  public void removePropertyChangeListener(String propertyName,
                                           PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }
  
  /**
   * Report a bound property update to any registered listeners.
   * No event is fired if old and new are equal and non-null.
   * @param propertyName  The programmatic name of the property that was changed.
   * @param oldValue  The old value of the property.
   * @param newValue  The new value of the property.
   */
  protected void firePropertyChange(String propertyName,
                                    boolean oldValue, boolean newValue) {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }
  
  /**
   * Report a bound property update to any registered listeners.
   * No event is fired if old and new are equal and non-null.
   * @param propertyName  The programmatic name of the property that was changed.
   * @param oldValue  The old value of the property.
   * @param newValue  The new value of the property.
   */
  protected void firePropertyChange(String propertyName,
                                    int oldValue, int newValue) {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }
  
  /**
   * Report a bound property update to any registered listeners.
   * No event is fired if old and new are equal and non-null.
   * @param propertyName  The programmatic name of the property that was changed.
   * @param oldValue  The old value of the property.
   * @param newValue  The new value of the property.
   */
  protected void firePropertyChange(String propertyName,
                                    Object oldValue, Object newValue) {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }
  
  // Visitors support ///////////////////////////////////////////////////////////
  
  /**
   * Allows a visitor to traverse the tree
   * @param visitor the visitor to accept
   */
  public abstract <T> T acceptVisitor(Visitor<T> visitor);
  
}
