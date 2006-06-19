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
import junit.framework.*;
import java.io.File;

/**
 * Represents the data for an array class.  There are two states of SymbolData.  One is a continuation which
 * is created when a type is referenced, but the corresponding class has not been read for its members.  The
 * other is a complete SymbolData containing all of the member data.
 * This ArrayData stores the SymbolData of its element type inside of it.
 */

public class ArrayData extends SymbolData {
  /**The type of the elements of this array.  For example, int[] has an _elementType of int.*/
  private SymbolData _elementType;
  
  /**
   * Creates a new ArrayData corresponding to the elementType sd.
   * @param sd  The SymbolData element type.
   * @param llv  The LanguageLevelVisitor who created this ArrayData.
   * @param si  The SourceInfo corresponding to this ArrayData.
   */
  public ArrayData(SymbolData sd, LanguageLevelVisitor llv, SourceInfo si) {
    super(sd.getName() + "[]");
    
    _elementType = sd;
    
    // Arrays only have one field called length, and it is automatically given a value
    addVar(new VariableData("length", 
                            new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, 
                                                       new String[] {"public", "final"}),
                            SymbolData.INT_TYPE, true, this));
    
    
    //All arrays are a subclass of Object
    SymbolData object = llv.getSymbolData("java.lang.Object", si);
    setSuperClass(object);
    
    //All arrays implement java.lang.Cloneable and java.io.Serializable
    SymbolData result = llv.getSymbolData("java.lang.Cloneable", si);
    if (result != null) {
      addInterface(result);
    }
    
    result = llv.getSymbolData("java.io.Serializable", si);
    if (result != null) {
      addInterface(result);
    }
    
    //And, since they implement Cloneable, all arrays overwrite the clone method so that it does not throw exceptions
    addMethod(new MethodData("clone", 
                             new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"}), 
                             new TypeParameter[0],
                             object,
                             new VariableData[0],
                             new String[0], //Doesn't throw any exceptions!
                             this,
                             null));
                             
    setIsContinuation(false);

  }

  /*@return the package of the element type*/
  public String getPackage() {
    return _elementType.getPackage();
  }
  
  /*Set the package of the element type to be the specified package:
   * @param s  The package to use*/
  public void setPackage(String s) {
    _elementType.setPackage(s);
  }
  
  
  /* @return the ModifiersAndVisibility of the element type*/
  public ModifiersAndVisibility getMav() {
    if (_elementType.hasModifier("final")) { return _elementType.getMav(); }
    else {
      String[] elementMavs = _elementType.getMav().getModifiers();
      String[] newMavs = new String[elementMavs.length + 1];
      for (int i = 0; i < elementMavs.length; i++) { newMavs[i] = elementMavs[i]; }
      newMavs[elementMavs.length] = "final";
      
      return new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, newMavs);
    }
  }
  
  /*Set the ModifiersAndVisibility of the element type to the specified value.
   * @param mv  The ModifiersAndVisibility to use.*/
  public void setMav(ModifiersAndVisibility mv) {
    _elementType.setMav(mv);
  }
  
  /*@return the SymbolData element type corresponding to the elements of this array.*/
  public SymbolData getElementType() {
    return _elementType;
  }
  
  /**Delegate to the outer data of your element type*/
  public Data getOuterData() {
    return _elementType.getOuterData();
  }
  
  /**
   * A Noop, because arrays shouldn't have outer data
   */
  public void setOuterData(Data outerData) {
//    _elementType.setOuterData(outerData);
  }
  
  
  public boolean equals(Object obj) {
    if (this == obj) {return true;}
    if (obj == null) return false;
    if ((obj.getClass() != this.getClass())) { //|| (obj.hashCode() != this.hashCode())) {
      return false;
    }
    ArrayData ad = (ArrayData) obj;    
    
    //For 2 array datas to be equal, all their symbolData fields must be equal, and their element types must be equal
    return super.equals(obj) && getElementType().equals(ad.getElementType());
    
    
  }
  
  /**
   * Provide a hashcode method that distinguishes between array datas based on name
   */
  public int hashCode() {
    return getName().hashCode();
  }
  
  /**
   *      Returns true only under the following conditions: 
   *      -if assignTo is a class, assignTo must be java.lang.Object.
   *      -if assignTo is an interferface, then it must be Serializable or Clonable.
   *      -if assignTo is an array, then if this's element type is a primitive assignTo must have the same primitive element type and
   *          if this's element type is a reference type, this's reference type must be assignable to assignTo's element type.
   */
  public boolean isAssignableTo(SymbolData assignTo, String version) {
    if (assignTo instanceof ArrayData) {
      if (this.getElementType().isPrimitiveType()) {
        return this.getElementType() == ((ArrayData)assignTo).getElementType();
      }
      else if (((ArrayData)assignTo).getElementType().isPrimitiveType()) {
        return false;
      }
      else {
        return this.getElementType().isAssignableTo(((ArrayData)assignTo).getElementType(), version);
      }
    }
    else {
      return this.isSubClassOf(assignTo);
    }
  }
  
  /**
   * Return true iff
   *      - castTo is a class type and is Object
   *      - castTo is an interface type that is Serializable or Clonable
   *      - castTo is an array type then this and castTo must have element types that are either the same primitive type or (both reference types and this's element type must be castable to castTo's element type)
   */
  public boolean isCastableTo(SymbolData castTo, String version) {
    if (castTo instanceof ArrayData) {
      if (this.getElementType().isPrimitiveType()) {
        return this.getElementType() == ((ArrayData)castTo).getElementType();
      }
      else if (((ArrayData)castTo).getElementType().isPrimitiveType()) {
        return false;
      }
      else {
        return this.getElementType().isCastableTo(((ArrayData)castTo).getElementType(), version);
      }
    }
    else if (this.isSubClassOf(castTo)) {
      return true;
    }
    else {
      return this.isSubClassOf(castTo);
    }
  }
  
  /**
   * Return the dimensions of this array (the level of nesting until a non-array element type is found).
   */
  public int getDimensions() {
    int dim = 1;
    SymbolData curData = this.getElementType();
    while(curData instanceof ArrayData) {
      dim ++;
      curData = ((ArrayData) curData).getElementType();
    }
    return dim;
  }
  

   /**
    * Test the methods in the enclosing class.  There is a test method corresponding to almost every method defined above.
    */
  public static class ArrayDataTest extends TestCase {
    
    private ArrayData _ad;
    
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[0]);
    private ModifiersAndVisibility _abstractMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"final"});
    private ModifiersAndVisibility _publicFinalMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[]{"public", "final"});
    private ModifiersAndVisibility _privateFinalMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"private", "final"});
    
    private LanguageLevelVisitor llv;
    private SourceInfo si;
    
    public ArrayDataTest() {
      this("");
    }
    public ArrayDataTest(String name) {
      super(name);
    }
    
    public void setUp() {
      llv = new LanguageLevelVisitor(new File(""), "", new LinkedList<String>(), new LinkedList<String>(),
                                                          new LinkedList<String>(), new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>(), 
                                                          new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
      
      llv.symbolTable = new Symboltable();
      si = JExprParser.NO_SOURCE_INFO;
      SymbolData e = new SymbolData("elementType");
      e.setIsContinuation(false);
      _ad = new ArrayData(e, llv, si);
      LanguageLevelVisitor.errors = new LinkedList<Pair<String, JExpressionIF>>();
    }
    
    public void testGetDimensions() {
      ArrayData intArray = new ArrayData(SymbolData.INT_TYPE, llv, si);
      intArray.setIsContinuation(false);
      llv.symbolTable.remove("int[]");
      llv.symbolTable.put("int[]", intArray);
      
      ArrayData intArrayArray = new ArrayData(intArray, llv, si);
      intArrayArray.setIsContinuation(false);
      llv.symbolTable.put("int[][]", intArrayArray);

      ArrayData intArray3 = new ArrayData(intArrayArray, llv, si);
      intArray3.setIsContinuation(false);
      llv.symbolTable.put("int[][][]", intArray3);
      
      assertEquals("Should return 1", 1, intArray.getDimensions());
      assertEquals("Should return 2", 2, intArrayArray.getDimensions());
      assertEquals("Should return 3", 3, intArray3.getDimensions());
    }
    
    public void testGetPackage() {
      SymbolData sd = _ad.getElementType();
      sd.setPackage("a.b");
      assertEquals("Should return a.b", "a.b", _ad.getPackage());
      
      sd.setPackage("");
      assertEquals("Should return empty string", "", _ad.getPackage());
      
      sd.setPackage("who.let");
      assertEquals("Should return who.let", "who.let", _ad.getPackage());
    }
    
    public void testSetPackage() {
      SymbolData sd = _ad.getElementType();
      _ad.setPackage("a.b");
      assertEquals("Should return a.b", "a.b", sd.getPackage());
      
      _ad.setPackage("");
      assertEquals("Should return empty string", "", sd.getPackage());
      
      _ad.setPackage("who.let");
      assertEquals("Should return who.let", "who.let", sd.getPackage());
      
    }

    public void testGetMav() {
      SymbolData sd = _ad.getElementType();

      sd.setMav(_publicMav);
      assertEquals("Should return _publicFinal mav", _publicFinalMav, _ad.getMav());
      
      sd.setMav(_privateMav);
      assertEquals("Should return _privateFinal mav", _privateFinalMav, _ad.getMav());
      
      sd.setMav(_packageMav);
      assertEquals("Should return _finalMav", _finalMav, _ad.getMav());
      
      sd.setMav(_publicFinalMav);
      assertEquals("Should return _publicFinalMav", _publicFinalMav, _ad.getMav());
    }

    public void testSetMav() {
      SymbolData sd = _ad.getElementType();
      
      _ad.setMav(_publicMav);
      assertEquals("Should return _publicMav", _publicMav, sd.getMav());
      
      _ad.setMav(_privateMav);
      assertEquals("Should return _privateMav", _privateMav, sd.getMav());
      
      _ad.setMav(_publicFinalMav);
      assertEquals("Should return _publicFinalMav", _publicFinalMav, _ad.getMav());
      
    }
    
    
    public void testIsAssignableTo() {
      //if assignTo is a class, it must be java.lang.Object
      SymbolData object = llv.symbolTable.get("java.lang.Object");
      assertTrue(_ad.isAssignableTo(object, "1.5"));
      assertTrue(_ad.isAssignableTo(object, "1.4"));
      
      SymbolData notObject = new SymbolData("somethingRandom");
      assertFalse(_ad.isAssignableTo(notObject, "1.5"));
      assertFalse(_ad.isAssignableTo(notObject, "1.4"));
      
      //if assignTo is an interface, then it must be Serializable or Clonable
      SymbolData serializable = _ad.getInterfaces().get(0);
      SymbolData clonable = _ad.getInterfaces().get(1);
      notObject.setInterface(true);
      
      assertTrue(_ad.isAssignableTo(serializable, "1.5"));
      assertTrue(_ad.isAssignableTo(serializable, "1.4"));
      assertTrue(_ad.isAssignableTo(clonable, "1.5"));
      assertTrue(_ad.isAssignableTo(clonable, "1.4"));
      assertFalse(_ad.isAssignableTo(notObject, "1.5"));
      assertFalse(_ad.isAssignableTo(notObject, "1.4"));

      //if array is an array of primatives, then assign to must have primitive types that must match exactly
      _ad = new ArrayData(SymbolData.INT_TYPE, llv, si);
      ArrayData intArray = new ArrayData(SymbolData.INT_TYPE, llv, si);
      ArrayData doubleArray = new ArrayData(SymbolData.DOUBLE_TYPE, llv, si);
      ArrayData charArray = new ArrayData(SymbolData.CHAR_TYPE, llv, si);
      ArrayData objArray = new ArrayData(object, llv, si);
      
      assertTrue(_ad.isAssignableTo(intArray, "1.5"));
      assertTrue(_ad.isAssignableTo(intArray, "1.4"));
      assertFalse(_ad.isAssignableTo(charArray, "1.5"));
      assertFalse(_ad.isAssignableTo(charArray, "1.4"));
      assertFalse(_ad.isAssignableTo(doubleArray, "1.5"));
      assertFalse(_ad.isAssignableTo(doubleArray, "1.4"));
      assertFalse(_ad.isAssignableTo(objArray, "1.5"));
      assertFalse(_ad.isAssignableTo(objArray, "1.4"));

      
      //if array is an array of reference types, then reference types must be assignable to element type
      SymbolData integerSd = new SymbolData("java.lang.Integer");
      integerSd.setSuperClass(object);
      _ad = new ArrayData(integerSd, llv, si);
      notObject.setInterface(false);
      ArrayData randomArray = new ArrayData(notObject, llv, si);

      assertTrue(_ad.isAssignableTo(objArray, "1.5"));
      assertTrue(_ad.isAssignableTo(objArray, "1.4"));
      assertTrue(_ad.isAssignableTo(_ad, "1.5"));
      assertTrue(_ad.isAssignableTo(_ad, "1.4"));
      assertFalse(_ad.isAssignableTo(randomArray, "1.5"));
      assertFalse(_ad.isAssignableTo(randomArray, "1.4"));
      assertFalse(_ad.isAssignableTo(intArray, "1.5"));
      assertFalse(_ad.isAssignableTo(intArray, "1.4"));
    }
   
    
    public void testIsCastableTo() {
      //if castTo is a class type and Object, should work
      SymbolData object = llv.symbolTable.get("java.lang.Object");
      assertTrue(_ad.isCastableTo(object, "1.5"));
      assertTrue(_ad.isCastableTo(object, "1.4"));
      
      //if castTo is an interface, then it must be Serializable or Clonable
      SymbolData serializable = _ad.getInterfaces().get(0);
      SymbolData clonable = _ad.getInterfaces().get(1);
      
      assertTrue(_ad.isAssignableTo(serializable, "1.5"));
      assertTrue(_ad.isAssignableTo(serializable, "1.4"));
      assertTrue(_ad.isAssignableTo(clonable, "1.5"));
      assertTrue(_ad.isAssignableTo(clonable, "1.4"));

      //anything non-array type should break
      SymbolData notObject = new SymbolData("somethingRandom");
      assertFalse(_ad.isCastableTo(notObject, "1.5"));
      assertFalse(_ad.isCastableTo(notObject, "1.4"));

      
     
      //if castTo is an array and the array elements can be cast to its elements, should work
      
      //if array is an array of primatives, then assign to must have primitive types that must match exactly
      _ad = new ArrayData(SymbolData.INT_TYPE, llv, si);
      ArrayData intArray = new ArrayData(SymbolData.INT_TYPE, llv, si);
      ArrayData doubleArray = new ArrayData(SymbolData.DOUBLE_TYPE, llv, si);
      ArrayData charArray = new ArrayData(SymbolData.CHAR_TYPE, llv, si);
      ArrayData objArray = new ArrayData(object, llv, si);
      
      assertTrue(_ad.isCastableTo(intArray, "1.5"));
      assertTrue(_ad.isCastableTo(intArray, "1.4"));
      assertFalse(_ad.isCastableTo(charArray, "1.5"));
      assertFalse(_ad.isCastableTo(charArray, "1.4"));
      assertFalse(_ad.isCastableTo(doubleArray, "1.5"));
      assertFalse(_ad.isCastableTo(doubleArray, "1.4"));
      assertFalse(_ad.isCastableTo(objArray, "1.5"));
      assertFalse(_ad.isCastableTo(objArray, "1.4"));

      
      //if array is an array of reference types, then reference types must be castable to element type
      SymbolData integerSd = new SymbolData("java.lang.Integer");
      integerSd.setSuperClass(object);
      _ad = new ArrayData(integerSd, llv, si);
      notObject.setInterface(false);
      ArrayData randomArray = new ArrayData(notObject, llv, si);

      assertTrue(_ad.isCastableTo(objArray, "1.5"));
      assertTrue(_ad.isCastableTo(objArray, "1.4"));
      assertTrue(_ad.isCastableTo(_ad, "1.5"));
      assertTrue(_ad.isCastableTo(_ad, "1.4"));
      assertFalse(_ad.isCastableTo(randomArray, "1.5"));
      assertFalse(_ad.isCastableTo(randomArray, "1.4"));
      assertFalse(_ad.isCastableTo(intArray, "1.5"));
      assertFalse(_ad.isCastableTo(intArray, "1.4"));

      _ad = new ArrayData(object, llv, si);
      assertTrue(_ad.isCastableTo(new ArrayData(integerSd, llv, si), "1.5"));
      assertTrue(_ad.isCastableTo(new ArrayData(integerSd, llv, si), "1.4"));
      assertFalse(_ad.isCastableTo(new ArrayData(SymbolData.INT_TYPE, llv, si), "1.5"));
      assertFalse(_ad.isCastableTo(new ArrayData(SymbolData.INT_TYPE, llv, si), "1.4"));
    }
  }
}