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

package edu.rice.cs.javalanglevels;

import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.javalanglevels.parser.*;
import java.util.*;
import junit.framework.TestCase;

/** Represents the data for a given variable. */
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
  
  /** This constructor is only used when reading method parameters in a class file because class files only store the
    * types of method parameters.
    */
  public VariableData(SymbolData type) {
    _name = "";
    _modifiersAndVisibility = new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[0]);
    _type = type.getInstanceData();
    _hasBeenAssigned = false;
  }

  /** Checks the values of the fields*/
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
  public int hashCode() { return getEnclosingData().hashCode() ^ getName().hashCode(); }
  
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
  public void setMav(ModifiersAndVisibility mav) { _modifiersAndVisibility = mav; }
  
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
      _modifiersAndVisibility = new ModifiersAndVisibility(SourceInfo.NO_INFO, newModifiers);
    }
  }

  /** Add "private" to the modifiers and visibility for this class, if it is not already there. */
  public void setPrivate() {
    if (! hasModifier("private")) {
      String[] modifiers = _modifiersAndVisibility.getModifiers();
      String[] newModifiers = new String[modifiers.length + 1];
      newModifiers[0] = "private";
      for (int i = 1; i <= modifiers.length; i++) {
        newModifiers[i] = modifiers[i-1];
      }
      _modifiersAndVisibility = new ModifiersAndVisibility(SourceInfo.NO_INFO, newModifiers);
    }
  }
  
  /** Add the specified modifier to the modifiers and visibility for this class, if it is not already there. */
  public void addModifier(String s) {
    if (! hasModifier(s)) {
      String[] modifiers = _modifiersAndVisibility.getModifiers();
      String[] newModifiers = new String[modifiers.length + 1];
      newModifiers[0] = s;
      for (int i = 1; i <= modifiers.length; i++) {
        newModifiers[i] = modifiers[i-1];
      }
      _modifiersAndVisibility = new ModifiersAndVisibility(SourceInfo.NO_INFO, newModifiers);
    }
  }

  /** Adds "private" and "final" to the modifiers and visibility for this class, if it is not already there. */
  public void setPrivateAndFinal() {
   setPrivate();
   setFinal();
  }
  
  /** Adds "final" and "static" to the modifiers and visibility for this class, if it is not already there. */
  public void setFinalAndStatic() {
   setFinal();
   addModifier("static");
  }
  
  /** Returns true if this VariableData is final. */
  public boolean isFinal() { return hasModifier("final"); }
  
  /** Returns true if this variable has the modifier specified*/
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
    
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"public"});
    private ModifiersAndVisibility _publicMav2 = new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"protected"});
    
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