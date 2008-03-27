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
import edu.rice.cs.util.Lambda;
import java.util.*;

/** Class representing all the variables that
  * can be inserted as variables in external processes.
  * @version $Id$
  */
public class PropertyMaps {
  /**
   * Map of property sets.
   */
  protected Map<String,Map<String,DrJavaProperty>> _props = new TreeMap<String,Map<String,DrJavaProperty>>();
  
  /** Singleton instance. */
  public static final PropertyMaps ONLY = new PropertyMaps();
  
  static {
    for(Map.Entry<Object,Object> es: System.getProperties().entrySet()) {
      ONLY.setProperty("Java", new JavaSystemProperty(es.getKey().toString()));
    }
    
    OptionMap om = DrJava.getConfig().getOptionMap();
    Iterator<OptionParser<?>> it = om.keys();
    while(it.hasNext()) {
      OptionParser<?> op = it.next();
      String key = "config."+op.getName();
      String value = om.getString(op);
      ONLY.setProperty("Config", new ConfigProperty(key));
    }
  }
  
  /** Create the basic property maps.
    * One is named "Java" and contains the Java system properties.
    * A second one is named "Config" and contains the DrJava configuration items. */
  public PropertyMaps() {
  }

  /** Return the property requested, or null if not found.
    * @throws IllegalArgumentException if category is not known. */
  public DrJavaProperty getProperty(String category, String name) {
    Map<String,DrJavaProperty> m = _props.get(category);
    if (m==null) { throw new IllegalArgumentException("DrJavaProperty category unknown."); }
    return m.get(name);
  }
  
  /** Add a property. */
  public DrJavaProperty setProperty(String category, DrJavaProperty p) {
    Map<String,DrJavaProperty> m = _props.get(category);
    if (m==null) { m = new HashMap<String,DrJavaProperty>(); _props.put(category,m); }
    m.put(p.getName(), p);
    return p;
  }
  
  /** Return the set of categories. */
  public Set<String> getCategories() { return _props.keySet(); }

  /** Return the properties in a category.
    * @throws IllegalArgumentException if category is not known. */
  public Map<String, DrJavaProperty> getProperties(String category) {
    Map<String,DrJavaProperty> m = _props.get(category);
    if (m==null) { throw new IllegalArgumentException("DrJavaProperty category unknown."); }
    return m;
  }
  
  /** A lambda to use the toString() method, which does not force an update and might be stale. */
  public static final Lambda<String,DrJavaProperty> TO_STRING = new Lambda<String,DrJavaProperty>() {
    public String apply(DrJavaProperty p) { return p.toString(); /* might be stale */ }
  };
  
  /** A lambda to use the getCurrent() method, which forces an update. */
  public static final Lambda<String,DrJavaProperty> GET_CURRENT = new Lambda<String,DrJavaProperty>() {
    public String apply(DrJavaProperty p) { return p.getCurrent(); /* might be stale */ }
  };
} 
