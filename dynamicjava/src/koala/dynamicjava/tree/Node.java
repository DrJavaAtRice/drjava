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

import java.util.*;

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents the nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.1 - 1999/11/12
 */

public abstract class Node implements SourceInfo.Wrapper {
  private final Map<String,Object> properties;
  private SourceInfo sourceInfo;
  
  
  protected Node(SourceInfo si) {
    assert si != null;
    sourceInfo = si;
    properties = new HashMap<String, Object>();
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
    sourceInfo = si;
  }
  
  
  
  // Properties support //////////////////////////////////////////////////
  
  /**
   * Sets the value of a property
   * @param name  the property name
   * @param value the new value to set
   */
  public void setProperty(String name, Object value) {
    properties.put(name, value);
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
   * Allows a visitor to traverse the tree
   * @param visitor the visitor to accept
   */
  public abstract <T> T acceptVisitor(Visitor<T> visitor);
  
}
