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

package koala.dynamicjava.interpreter.context;

import java.lang.ref.*;
import java.util.*;

import koala.dynamicjava.util.ImportationManager;

/**
 * This class encapsulates the behaviour of Java scopes.
 *
 * @author  Stephane Hillion
 * @version 2.1 - 2000/01/05
 */

public class VariableContext implements SimpleContext {
  /**
   * The scopes
   */
  protected Link scopes;
  
  /**
   * The current scope
   */
  protected Scope scope;
  
  /**
   * The current scope for variables
   */
  protected Scope cscope;
  
  /**
   * The current importation manager for the context
   */
  protected ImportationManager importationManager;
  
  /**
   * Creates a new context initialized with an empty initial scope
   */
  protected VariableContext() { 
    enterScope();
  }
  
  
  /**
   * Creates a new context initialized with variables and constants defined
   * in the initial scope passed in the parameter entries
   * @param entries a set of abstract variables (constants or variables)
   */
  public VariableContext(Set<AbstractVariable> entries) {
    this();    
    Iterator<AbstractVariable> it = entries.iterator();
    while (it.hasNext()) {
      it.next().set(this, null);
    }
  }
  
  /**
   * Creates a new context initialized with an empty initial scope and 
   * sets the given Importation manager
   * @param im an importationManager for use in all child contexts
   */
  public VariableContext(ImportationManager im) {
    this();
    importationManager = im;
  }
  
  /**
   * Enters a scope.
   */
  public void enterScope() {
    // TODO: What is the correct way to handle the 
    // revert point?  If someone sets a revert point, 
    // enters a new scope, and leaves that scope, 
    // should the original revert point for this scope
    // still exist?
    scopes = LinkFactory.createLink(scopes);
    scope  = scopes.scope;
    cscope = scopes.cscope;
  }
  
  /**
   * Enters a scope and defines the given entries to null.
   * @param entries a set of string
   */
  public void enterScope(Set<AbstractVariable> entries) {
    enterScope();
    Iterator<AbstractVariable> it = entries.iterator();
    while (it.hasNext()) {
      it.next().set(this, null);
    }
  }
  
  /**
   * Defines the given variables
   */
  public void defineVariables(Set<AbstractVariable> vars) {
    Iterator<AbstractVariable> it = vars.iterator();
    while (it.hasNext()) {
      AbstractVariable v = it.next();
      
      if (v.get(this) == Scope.NO_SUCH_KEY) {
        v.set(this, null);
      }
    }
  }
  
  /**
   * Leaves the current scope
   * @return the set of variable defined in this scope
   */
  public Set<AbstractVariable> leaveScope() {
    Set<AbstractVariable> result = getCurrentScopeVariables();
    scopes = scopes.next;
    scope  = scopes.scope;
    cscope = scopes.cscope;
    return result;
  }
  
  /**
   * Returns the current scope variables in a set
   */
  public Set<AbstractVariable> getCurrentScopeVariables() {
    Set<AbstractVariable> result = new HashSet<AbstractVariable>(11);
    Iterator<String> it = scope.keySet().iterator();
    while (it.hasNext()) {
      result.add(new Variable(it.next()));
    }
    it = cscope.keySet().iterator();
    while (it.hasNext()) {
      result.add(new Constant(it.next()));
    }
    return result;
  }
  
  /**
   * Returns the current scope variables (strings) in a set
   */
  public Set<String> getCurrentScopeVariableNames() {
    Set<String> result = new HashSet<String>(11);
    Iterator<String> it = scope.keySet().iterator();
    while (it.hasNext()) {
      result.add(it.next());
    }
    it = cscope.keySet().iterator();
    while (it.hasNext()) {
      result.add(it.next());
    }
    return result;
  }
  
  /**
   * Tests whether an entry is defined in this context
   * @param name the name of the entry
   */
  public boolean isDefinedVariable(String name) {
    for (Link l = scopes; l != null; l = l.next) {
      if (l.scope.contains(name) || l.cscope.contains(name)) {
        return true;
      }
    }
    
    //return importationManager.fieldExists(name);
    return false;
  }
  
  /**
   * Tests whether a variable is final in this context
   * @param name the name of the entry
   * @return false if the variable is not final
   * @exception IllegalStateException if the variable is not defined
   */
  public boolean isFinal(String name) {
    for (Link l = scopes; l != null; l = l.next) {
      if (l.cscope.contains(name)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Defines a new variable in the current scope
   * @param name  the name of the new entry
   * @param value the value of the entry
   * @exception IllegalStateException if the variable is already defined
   */
  public void define(String name, Object value) {
    if (scope.put(name, value) != Scope.NO_SUCH_KEY) {
      throw new IllegalStateException(name);
    }
  }
  
  /**
   * Defines a new constant variable in the current scope
   * @param name  the name of the new entry
   * @param value the value of the entry
   * @exception IllegalStateException if the variable is already defined
   */
  public void defineConstant(String name, Object value) {
    if (cscope.put(name, value) != Scope.NO_SUCH_KEY ||
        scope.contains(name)) {
      throw new IllegalStateException(name);
    }
  }
  
  /**
   * Returns the value of a variable with the given name
   * @param name  the name of the value to get
   * @exception IllegalStateException if the variable is not defined
   */
  public Object get(String name) {
    for (Link l = scopes; l != null; l = l.next) {
      Object result = l.scope.get(name);
      if (result != Scope.NO_SUCH_KEY ||
          (result = l.cscope.get(name)) != Scope.NO_SUCH_KEY) {
        return result;
      }
    }
    throw new IllegalStateException(name);
  }
  
  /**
   * Sets the value of a defined variable
   * @param name  the name of the entry
   * @param value the value of the entry
   * @exception IllegalStateException if the variable is not defined or is final
   */
  public void set(String name, Object value) {
    for (Link l = scopes; l != null; l = l.next) {
      if (l.scope.contains(name)) {
        l.scope.put(name, value);
        return;
      }
    }
    throw new IllegalStateException(name);
  }
  
  /**
   * Sets the value of a constant variable in the current scope
   * @param name  the name of the entry
   * @param value the value of the entry
   */
  public void setConstant(String name, Object value) {
    cscope.put(name, value);
  }
  
  /**
   * Sets the value of a variable in the current scope
   * @param name  the name of the entry
   * @param value the value of the entry
   */
  public void setVariable(String name, Object value) {
    scope.put(name, value);
  }
  
  /**
   * Creates a map that contains the constants in this context
   */
  public Map<String,Object> getConstants() {
    Map<String,Object> result = new HashMap<String,Object>(11);
    for (Link l = scopes; l != null; l = l.next) {
      Iterator<String> it = l.cscope.keySet().iterator();
      while (it.hasNext()) {
        String s = it.next();
        result.put(s, l.cscope.get(s));
      }
    }
    return result;
  }
  
  /**
   * Sets a revert point such that calling revert will remove
   * any variable or constant bindings set after this point.
   */
  public void setRevertPoint() {
    scope.setRevertPoint();
    cscope.setRevertPoint();
  }
  
  /**
   * Removes any bindings set after the last call to setRevertPoint
   */
  public void revert() {
    scope.revert();
    cscope.revert();
  }
  
  /**
   * To store one scope
   */
  protected static class Link {
    /**
     * The current scope
     */
    public Scope scope = new Scope();
    
    /**
     * The current scope for constants
     */
    public Scope cscope = new Scope();
    
    /**
     * The next scope
     */
    public Link next;
    
    /**
     * Creates a new link
     */
    public Link(Link next) {
      this.next = next;
    }
  }
  
  /**
   * To manage the creation of scopes and links
   */
  protected static class LinkFactory {
    /**
     * The table size
     */
    protected final static int SIZE = 10;
    
    /**
     * The table used to recycle the links
     */
    protected final static WeakReference[] links = new WeakReference[SIZE];
    
    /**
     * Creates a new link
     */
    public static Link createLink(Link next) {
      /**
       for (int i = 0; i < SIZE; i++) {
       WeakReference r = links[i];
       Link l = null;
       if (r != null) {
       l = (Link)r.get();
       if (l != null) {
       links[i] = null;
       l.next = next;
       return l;
       } else {
       links[i] = null;
       }
       }
       }
       */
      return new Link(next);
    }
    
    /**
     * Notifies the factory to recycle the given link
     */
    public static void recycle(Link l) {
      /*
       l.scope.clear();
       for (int i = 0; i < SIZE; i++) {
       if (links[i] == null) {
       links[i] = new WeakReference(l);
       return;
       }
       }
       */
    }
  }
  
  /**
   * A Scope is a wrapper for a hashtable that maps variable
   * names to the corresponding value.  Previously, the scope
   * object was an implementation of a hashtable without the
   * ability to unmap a variable from it.  Now the scope wraps
   * an intance of java.util.HashMap and includes a remove 
   * method in order to allow variables to be unmapped from the
   * scope.
   */
  static class Scope {
    /**
     * The hashtable that is being wrapped by the Scope object
     */
    protected HashMap<String,Object> _table;
    
    protected LinkedList<String> _addedKeys;
    
    /**
     * The object used to notify that a key do not exists
     */
    public final static Object NO_SUCH_KEY = new Object();
    
    public Scope() { 
      _table = new HashMap<String,Object>(11,0.75f);
      _addedKeys = new LinkedList<String>();
    }
    
    /**
     * Gets the value of a variable
     * @return the value or NO_SUCH_KEY
     */
    public Object get(String key) {
      if (_table.containsKey(key))
        return _table.get(key);
      else
        return NO_SUCH_KEY;
    }
    
    /**
     * Returns whether a variable binding exists
     * in this scope
     */
    public boolean contains(String key) {
      return _table.containsKey(key);
    }
    
    /**
     * Sets a new value for the given variable
     * @return the old value or NO_SUCH_KEY
     */
    public Object put(String key, Object value) {
      boolean wasThere = _table.containsKey(key);
      Object val = _table.put(key,value);
      _addedKeys.add(key);
      return (wasThere) ? val : NO_SUCH_KEY;
    }
    
    /**
     * Returns a set that contains the keys
     */
    public Set<String> keySet() {
      return _table.keySet();
    }
    
    /**
     * Clears this scope
     */
    public void clear() {
      _table.clear();
      _addedKeys.clear();
    }
    
    /**
     * Unmaps all the variable bindings added since 
     * the last call to setRevertPoint
     */
    public void revert() {
      for(String key : _addedKeys) {
        _table.remove(key);
      }
      _addedKeys.clear();
    }
    
    /**
     * Any bindings set after this method is call may be
     * unmapped by calling the revert method.
     */
    public void setRevertPoint() {
      _addedKeys.clear();
    }
  }
  
}
