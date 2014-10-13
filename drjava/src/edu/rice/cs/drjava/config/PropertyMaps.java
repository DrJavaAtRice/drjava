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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.plt.lambda.Lambda2;
import java.util.*;

/** Class representing all the variables that
  * can be inserted as variables in external processes.
  * @version $Id: PropertyMaps.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class PropertyMaps implements Cloneable {
  /** Map of property sets. */
  protected Map<String,Map<String,DrJavaProperty>> _props = new TreeMap<String,Map<String,DrJavaProperty>>();
  
  /** Template instance. */
  public static final PropertyMaps TEMPLATE = new PropertyMaps();
  
  static {
    for(Map.Entry<Object,Object> es: System.getProperties().entrySet()) {
      TEMPLATE.setProperty("Java", new JavaSystemProperty(es.getKey().toString()));
    }
    
    OptionMap om = DrJava.getConfig().getOptionMap();
    for (OptionParser<?> op : om.keys()) {
      String key = "config." + op.getName();
      TEMPLATE.setProperty("Config", new ConfigProperty(key));
    }
  }
  
  /** Create the basic property maps.
    * One is named "Java" and contains the Java system properties.
    * A second one is named "Config" and contains the DrJava configuration items. */
  public PropertyMaps() { }

  /** Return the property requested, or null if not found.
    * @param category name of the category
    * @param name name of the property
    * @return property, or null if not found
    * @throws IllegalArgumentException if category is not known. */
  public DrJavaProperty getProperty(String category, String name) {
    Map<String,DrJavaProperty> m = _props.get(category);
    if (m == null) { throw new IllegalArgumentException("DrScalaProperty category unknown."); }
    return m.get(name);
  }
  
  /** Search through all categories and return the property requested, or null if not found.
    * @param key key of the property
    * @return property, or null if not found */
  public DrJavaProperty getProperty(String key) {
    for(String category: _props.keySet()) {
      DrJavaProperty p = getProperty(category, key);
      if (p != null) { return p; }
    }
    return null;
  }
  
  /** Remove the specified property.
    * @param p property to remove */
  public void removeProperty(DrJavaProperty p) {
    for(String category: _props.keySet()) {
      _props.get(category).remove(p);
    }
  }
  
  /** Add a property. */
  public DrJavaProperty setProperty(String category, DrJavaProperty p) {
    Map<String,DrJavaProperty> m = _props.get(category);
    if (m == null) { m = new HashMap<String,DrJavaProperty>(); _props.put(category,m); }
    m.put(p.getName(), p);
    return p;
  }
  
  /** Clear the specified category. */
  public void clearCategory(String category) {
    _props.remove(category);
  }
  
  /** Return the set of categories. */
  public Set<String> getCategories() { return _props.keySet(); }

  /** Return the properties in a category.
    * @throws IllegalArgumentException if category is not known. */
  public Map<String, DrJavaProperty> getProperties(String category) {
    Map<String,DrJavaProperty> m = _props.get(category);
    if (m == null) { throw new IllegalArgumentException("DrScalaProperty category unknown."); }
    return m;
  }
  
  /** A lambda to use the getLazy() method, which does not force an update and might be stale. */
  public static final Lambda2<DrJavaProperty,PropertyMaps,String> GET_LAZY = new Lambda2<DrJavaProperty,PropertyMaps,String>() {
    public String value(DrJavaProperty p, PropertyMaps pm) { return p.getLazy(pm); /* might be stale */ }
  };
  
  /** A lambda to use the getCurrent() method, which forces an update. */
  public static final Lambda2<DrJavaProperty,PropertyMaps,String> GET_CURRENT = new Lambda2<DrJavaProperty,PropertyMaps,String>() {
    public String value(DrJavaProperty p, PropertyMaps pm) { return p.getCurrent(pm); }
  };
  
  protected HashMap<String,Stack<VariableProperty>> _variables = new HashMap<String,Stack<VariableProperty>>();
  
  // name of the category for variables
  protected static final String VARIABLES_CATEGORY = "$Variables$";
  
  /** Clear all user-defined variables. */
  public void clearVariables() {
    _props.remove(VARIABLES_CATEGORY);
  }
  
  /** Add a variable with the specified name and value, shadowing previous definitions of the variable.
    * @param name name of the variable
    * @param value value of the variable
    * @throws IllegalArgumentException if the name is already used for a built-in property */
  public void addVariable(String name, String value) {
    for(String category: _props.keySet()) {
      if (category.equals(VARIABLES_CATEGORY)) continue;
      if (getProperty(category, name) != null) {
        throw new IllegalArgumentException("Variable " + name + " already used for a built-in property");
      }
    }
    // name not used by built-in
    VariableProperty p = new VariableProperty(name, value);
    setProperty(VARIABLES_CATEGORY, p);
    Stack<VariableProperty> varStack = _variables.get(name);
    if (varStack == null) { varStack = new Stack<VariableProperty>(); _variables.put(name,varStack); }
    varStack.push(p);
  }
  
  /** Mutate the value of a variable with the specified name.
    * @param name name of the variable
    * @param value new value of the variable
    * @throws IllegalArgumentException if a variable with name does not exist */
  public void setVariable(String name, String value) {
    Stack<VariableProperty> varStack = _variables.get(name);
    if ((varStack == null) ||
        (varStack.empty())) { throw new IllegalArgumentException("Variable " + name + " does not exist."); }
    VariableProperty p = varStack.peek();
    p.setValue(value);
  }
  
  /** Remove the variable with the specified name, unshadowing previous definitions of the variable.
    * @param name of the variable
    * @throws IllegalArgumentException if no variable with that name exists */
  public void removeVariable(String name) {
    Stack<VariableProperty> varStack = _variables.get(name);
    if ((varStack == null) ||
        (varStack.empty())) { throw new IllegalArgumentException("Variable " + name + " does not exist."); }
    VariableProperty p = varStack.pop();
    if (varStack.empty()) {
      // no shadowed variables
      // remove the stack from the hash map of variables
      _variables.remove(name);
      // remove the property
      removeProperty(p);
    }
    else {
      // previously shadowed variable(s) exist
      // set unshadowed variable as new value of property
      setProperty(VARIABLES_CATEGORY, varStack.peek());
    }
  }
  
  /** Clone this PropertyMaps object.
    * @return cloned object */
  public PropertyMaps clone() throws CloneNotSupportedException {
    PropertyMaps clone = new PropertyMaps();
    clone._props.clear();
    for(String category: _props.keySet()) {
      for (String key: _props.get(category).keySet()) {
        clone.setProperty(category, getProperty(key));
      }
    }
    clone._variables.clear();
    for(String name: _variables.keySet()) {
      Stack<VariableProperty> stack = new Stack<VariableProperty>();
      for (VariableProperty v: _variables.get(name)) {
        stack.add(new VariableProperty(v.getName(),v.getCurrent(this)));
      }
      clone._variables.put(name, stack);
    }
    return clone;
  }
} 
