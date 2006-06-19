/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.javalanglevels;

import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.javalanglevels.parser.*;
import java.util.*;
import junit.framework.TestCase;

/**
 * Represents the data for a given variable.
 */
public class VariableData {
  
  /**The name of this variable*/
  private String _name;
  
  /**The modifiers and visibility*/
  private ModifiersAndVisibility _modifiersAndVisibility;
  
  /**The type of this variable, once it has been initialized*/
  private InstanceData _type;
  
  /**True if this variable has been given a value*/
  private boolean _hasBeenAssigned;
  
  /**True if this variable has an initializer*/
  private boolean _hasInitializer;
  
  /**The data that this variable belongs to*/
  private Data _enclosingData;
  
  /**True if this is a field we had generated*/
  private boolean _generated;
  
  /**
   * Constructor for VariableData.  hasInitializer and generated are set to {@code false}.
   * @param name  The name of the variable
   * @param modifiersAndVisibility  The modifiersAndVisibility
   * @param type  The SymbolData type of the variable.
   * @param hasBeenAssigned  true if this variable has a value
   * @param enclosingData  the enclosing data
   */
  public VariableData(String name, ModifiersAndVisibility modifiersAndVisibility, SymbolData type, 
                      boolean hasBeenAssigned, Data enclosingData) {
    _name = name;
    _modifiersAndVisibility = modifiersAndVisibility;
    _type = type.getInstanceData();
    _hasBeenAssigned = hasBeenAssigned;
    _enclosingData = enclosingData;
    _hasInitializer = false;
    _generated = false;
  }
  
  /**
   * This constructor is only used when reading method parameters in a class file because
   * class files only store the types of method parameters.
   */
  public VariableData(SymbolData type) {
    _name = "";
    _modifiersAndVisibility = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[0]);
    _type = type.getInstanceData();
    _hasBeenAssigned = false;
  }

  /**Check the values of the fields*/
  public boolean equals(Object obj) { 
    if (obj == null) return false;
    if ((obj.getClass() != this.getClass())) {
      return false;
    }
    
    VariableData vd = (VariableData)obj;
   
    return _name.equals(vd.getName()) &&
      _modifiersAndVisibility.equals(vd.getMav()) &&
      _type.equals(vd.getType()) &&
      (_hasBeenAssigned==vd.hasValue()) &&
      _hasInitializer==vd._hasInitializer && 
      _enclosingData == vd.getEnclosingData();
  }
  
  /**Hash on the name and enclosing data, since within each enclosing data, the variable name should be unique*/
  public int hashCode() {
    return getEnclosingData().hashCode() ^ getName().hashCode();
  }
  
  /**@return a readable representation of the Variable data*/
  public String toString() {
    return "VariableData(" + _name + ", " + _modifiersAndVisibility.getModifiers() + ", " + _type + ", " + _hasBeenAssigned + ")";
  }
  
  /**@return the name of this field or variable*/
  public String getName() { return _name; }
  
  /**Set the name of this variable to the specified string*/
  public void setName(String s) { _name = s; }
  
  /**@return the modifiers and visibility*/
  public ModifiersAndVisibility getMav() { return _modifiersAndVisibility; }
  
  /**Set the modifiers and visibility to the specified value*/
  public void setMav(ModifiersAndVisibility mav) {
    _modifiersAndVisibility = mav;
  }
  
  /**@return the InstanceData corresponding to the type of this variable.*/
  public InstanceData getType() {  return _type; }
  
  /**@return the enclosing data*/
  public Data getEnclosingData() { return _enclosingData; }
  
  /**Set the enclosing data to be the specified value*/  
  public void setEnclosingData(Data d) { _enclosingData = d; }
  
  /** Add "final" to the modifiers and visibility for this class, if it is not already there. */
  public void setFinal() {
    if (!isFinal()) {
      String[] modifiers = _modifiersAndVisibility.getModifiers();
      String[] newModifiers = new String[modifiers.length + 1];
      newModifiers[0] = "final";
      for (int i = 1; i <= modifiers.length; i++) {
        newModifiers[i] = modifiers[i-1];
      }
      _modifiersAndVisibility = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, newModifiers);
    }
  }

  /** Add "private" to the modifiers and visibility for this class, if it is not already there. */
  public void setPrivate() {
    if (!hasModifier("private")) {
      String[] modifiers = _modifiersAndVisibility.getModifiers();
      String[] newModifiers = new String[modifiers.length + 1];
      newModifiers[0] = "private";
      for (int i = 1; i <= modifiers.length; i++) {
        newModifiers[i] = modifiers[i-1];
      }
      _modifiersAndVisibility = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, newModifiers);
    }
  }
  
  /** Add the specified modifier to the modifiers and visibility for this class, if it is not already there. */
  public void addModifier(String s) {
    if (!hasModifier(s)) {
      String[] modifiers = _modifiersAndVisibility.getModifiers();
      String[] newModifiers = new String[modifiers.length + 1];
      newModifiers[0] = s;
      for (int i = 1; i <= modifiers.length; i++) {
        newModifiers[i] = modifiers[i-1];
      }
      _modifiersAndVisibility = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, newModifiers);
    }
  }

  /** Add "private" and "final" to the modifiers and visibility for this class, if it is not already there. */
  public void setPrivateAndFinal() {
   setPrivate();
   setFinal();
  }
  
  /** Add "final" and "static" to the modifiers and visibility for this class, if it is not already there. */
  public void setFinalAndStatic() {
   setFinal();
   addModifier("static");
  }
  
  /**Returns true if this VariableData is final. */
  public boolean isFinal() {
    return hasModifier("final");
  }
  
  /**Return true if this variable has the modifier specified*/
  public boolean hasModifier(String modifier) {
    String[] mavStrings = _modifiersAndVisibility.getModifiers();
    for (int i = 0; i < mavStrings.length; i++) {
      if (mavStrings[i].equals(modifier)) {
        return true;
      }
    }
    return false;
  }
  
  /**Set the generated flag to the specified value*/
  public void setGenerated(boolean value) {
    _generated = value;
  }
  
  /**@return the generated flag*/
  public boolean isGenerated() {
    return _generated;
  }
  
  /** Returns true iff this variable was declared with an initial value */
  public boolean hasInitializer() {
    return _hasInitializer;
  }
  
  /** Set the "hasInitializer" flag */
  public void setHasInitializer(boolean value) {
    _hasInitializer = value;
  }
  
  /** Returns true if this VariableData has been given a value. */
  public boolean hasValue() {
    return _hasBeenAssigned;
  }
  
  /**
   * If this VariableData has not been given a value, set _hasBeenAssigned to true, and
   * return true.  Otherwise, return false to indicate it already has a value and should not be
   * reassigned.
   */
  public boolean gotValue() {
    if (hasValue()) {
      return false;
    }
    _hasBeenAssigned = true;
    return true;
  }
  
  /**
   * If this VariableData has been given a value, set _hasBeenAssigned to false, and
   * return true.  Otherwise, return false.
   */
  public boolean lostValue() {
    if (hasValue()) {
      _hasBeenAssigned = false;
      return true;
    }
    return false;
  }
  
  
  
  /**
   * Test the methods defined in the above class.
   */
  public static class VariableDataTest extends TestCase {
    
    private VariableData _vd;
    private VariableData _vd2;
    
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"});
    private ModifiersAndVisibility _publicMav2 = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"protected"});
    
    public VariableDataTest() {
      this("");
    }
    public VariableDataTest(String name) {
      super(name);
    }
    
    public void testEquals() {
      _vd = new VariableData("v", _publicMav, SymbolData.INT_TYPE, true, null);
      
      //check variables that are equal;
      _vd2 = new VariableData("v", _publicMav2, SymbolData.INT_TYPE, true, null);
      assertTrue("Equals should return true if two VariableDatas are equal", _vd.equals(_vd2));
      assertTrue("Equals should return true in opposite direction as well", _vd2.equals(_vd));

      //comparison to null
      _vd2 = null;
      assertFalse("Equals should return false if VariableData is compared to null",_vd.equals(null));   
    
      //different names
      _vd2 = new VariableData("q", _publicMav, SymbolData.INT_TYPE, true, null);
      assertFalse("Equals should return false if variable names are different", _vd.equals(_vd2));
      
      //different MAV
      _vd2 = new VariableData("v", _protectedMav, SymbolData.INT_TYPE, true, null);
      assertFalse("Equals should return false if variable modifiers are different", _vd.equals(_vd2));
      
      //different types
      _vd2 = new VariableData("v", _publicMav, SymbolData.BOOLEAN_TYPE, true, null);
      assertFalse("Equals should return false if variable types are different", _vd.equals(_vd2));
    }
  }
}