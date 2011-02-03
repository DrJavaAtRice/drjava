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
import java.util.*;
import junit.framework.TestCase;
import edu.rice.cs.javalanglevels.parser.JExprParser;

/** Abstract class epresenting the data for a given braced body: a class, interface, method, or just a body. */
public abstract class Data {
  
  /** The fully qualified name of this data. */
  protected String _name;
  
  /** The vars defined in the lexical scope of this data.*/
  protected LinkedList<VariableData> _vars;
  
  /** All enclosing data are in this list. */
  protected LinkedList<Data> _enclosingData;
  
  /** The modifiers and visibility of this data.*/
  protected ModifiersAndVisibility _modifiersAndVisibility;
  
  /** The outer data--what directly encloses this data.*/
  protected Data _outerData;
  
  /** Any inner classes that are defined in this data.*/
  protected LinkedList<SymbolData> _innerClasses;
  
  /** All blocks defined within this data, in lexical order.*/
  protected LinkedList<BlockData> _blocks;
  
  /** Iterator over _blocks*/
  protected Iterator<BlockData> _blockIterator;

  /** The default constructor for a Data.  It takes in the outerData, and sets all lists and the name to empty, except
    * that the outer data is asdded to the enclosing data list. */
  public Data(Data outerData) {
    _name = "";
    _modifiersAndVisibility = null;
    _vars = new LinkedList<VariableData>();
    _enclosingData = new LinkedList<Data>();
    _outerData = outerData;
    if (outerData != null) {
      _enclosingData.addLast(_outerData); // We add superclasses and interfaces to the front of this _enclosingData.
    }
    _innerClasses = new LinkedList<SymbolData>();
    _blocks = new LinkedList<BlockData>();
    _blockIterator = null;
  }
  
  /** @return the name of this data.*/
  public String getName() { return _name; }
  
  /** Set the fully qualified name of this data.
    * @param name  The new fully qualified name of this data.
    */
  void setName(String name) { _name = name; }
  
  public Boolean isAnonymousClass() {
    int lastIndex = _name.lastIndexOf('$');
    try { return (lastIndex < 0) && Integer.parseInt(_name.substring(lastIndex+1)) >= 0; }
    catch(NumberFormatException e) { return false; /* suffix is not an anonymous class index */ }
  }
  
  public Boolean isDoublyAnonymous() {
    if (! isAnonymousClass()) return false;
    for (Data d: getEnclosingData()) {
      if (d.isAnonymousClass()) return true;
    }
    return false;
  }
     
  /** Set vars to the specified linked list of vars, the variables that are defined in the scope of this data. */
  void setVars(LinkedList<VariableData> vars) { _vars = vars; }
  
  /**  Finds and returns the particular VariableData declared in this Data's context.
    * @param name  Name of the variable
    * @return  The VariableData with the matching name or null if it was not found.
    */
  public VariableData getVar(String name) {
    Iterator<VariableData> iter = _vars.iterator();
    while (iter.hasNext()) {
      VariableData vd = iter.next();
      if (vd.getName().equals(name)) {
        return vd;
      }
    }
    return null;
  }
  
  /**@return the list of variables declared in the scope of this data.*/
  public LinkedList<VariableData> getVars() { return _vars; }
  
  /** @return the list of enclosing data. */
  public LinkedList<Data> getEnclosingData() { return _enclosingData; }
  
  /** Add to the front because we want the outer data to be the last thing in the list. */
  public void addEnclosingData(Data enclosingData) {
    assert enclosingData != null;
    if (!_enclosingData.contains(enclosingData)) {
      _enclosingData.addFirst(enclosingData);
    }
  }
  
  //Used during testing
  public void setEnclosingData(LinkedList<Data> d) { _enclosingData = d; }
  
  /** Check to see if a variable with the same name as vr has already been defined in the scope of this data.  If so, 
    * return true.  Otherwise, return false.
    * @param vr  The VariableData whose name we are searching for.
    * @return  true if that name has already been used in this scope, false otherwise.
    */
  private boolean _repeatedName (VariableData vr) {
    Iterator<VariableData> iter = _vars.iterator();
    while (iter.hasNext()) {
      VariableData next = iter.next();
      if (vr.getName().equals(next.getName())) {
        return true;
      }
    }
    return false;
  }
  
  /** Add the specified Variable Data to the list of variables defined in this scope, unless its name has already been 
    * used.  Return true if it was successfully added, and false otherwise.
    * @param var  The variable we want to add to this scope.
    * @return  true if it was successfully added, false otherwise.
    */
  public boolean addVar(VariableData var) {
    if (! _repeatedName(var)) {
      _vars.addLast(var);
      return true;
    }
    else return false;
  }
  
  /** Add the array of variable datas to the list of variables defined in this scope, unless a name has already been
    * used.  Return true if all variables were added successfully, false otherwise.
    * @param vars  The VariableData[] that we want to add.
    * @return  true if all VariableDatas were added successfully, false otherwise.
    */
  public boolean addVars(VariableData[] vars) {
    boolean success = true;
    for (int i = 0; i < vars.length; i++) {
//      if (vars[i] == null) { System.out.println("Var " + i + " was null!"); }
      if (! _repeatedName(vars[i])) {
        _vars.addLast(vars[i]);
      }
      else success = false;
    }
    return success;
  }
  
  /** Add the array of variable datas to the list of variables defined in this scope, unless a name has already been 
    * used.  Return true if all variables were added successfully, false otherwise.  Set each of the variable datas in 
    * the array to be final before adding them.
    * @param vars the VariableData[] that we want to add.
    * @return true if all VariableDatas were added successfully, false otherwise.
   */
  public boolean addFinalVars(VariableData[] vars) {
    boolean success = true;
    for (int i = 0; i < vars.length; i++) {
      if (! _repeatedName(vars[i])) {
        if (! vars[i].isFinal()) vars[i].setFinal();
        _vars.addLast(vars[i]);
      }
      else { success = false; }
    }
    return success;
  }
  
  /** @return the modifiersAndVisibility for this data. */
  public ModifiersAndVisibility getMav() { return _modifiersAndVisibility; }
  
  /** Assigns the specified modifiersAndVisiblity to this data.
    * @param modifiersAndVisibility  The ModifiersAndVisibility to assign to this data.
    */
  public void setMav(ModifiersAndVisibility modifiersAndVisibility) {
    _modifiersAndVisibility = modifiersAndVisibility;
  }
  
  /**Return the enclosing getSymbolData()*/
  public abstract SymbolData getSymbolData();

  /** @return the directly enclosing outer data. */
  public Data getOuterData() { return _outerData; }
  
  /** Sets the outer data to the specified value--throw an exception if the data already has an outer data.
    * @param outerData  The Data that encloses this data.
    */
  public void setOuterData(Data outerData) {
    if (outerData == null) {
      assert _outerData == null; // Client code should not try to nullify a defined outerData value
      return;
    }
    if (_outerData == null || _outerData.equals(outerData)) {
      _outerData = outerData;
      if (! _enclosingData.contains(outerData)) _enclosingData.addLast(outerData);
    }
    else {
      throw new RuntimeException("Internal Program Error: Trying to reset an outer data to " + outerData.getName() +  
                                 " for " + getName() + " that has already been set to " + _outerData.getName() + 
                                 ".  Please report this bug.");
    }
  }
  
  /** @return true if d is an outer data of this data. TODO: What if d is a library class? */
  public boolean isOuterData(Data d) {
    Data outerData = _outerData;
    while ((outerData != null) && ! LanguageLevelVisitor.isJavaLibraryClass(outerData.getName())) {
      if (outerData == d) return true;
      outerData = outerData.getOuterData();
    }
    return false;
  }
  
  /** @return the enclosing class of this. */
  public SymbolData getEnclosingClass() {
    Data next = _outerData;
    
    while (next != null) {
      if (next instanceof SymbolData) return (SymbolData) next;
      next = next.getOuterData();
    }
    return null;
  }

  /** Loop over the specified string, and replace any '$' with '.'  This is used to change an inner class name to a 
    * standard format.  It fails if the inner class is local or anonymous!  
    * @param s  The String to change.
    * @return   The converted string.
    */
  public static String dollarSignsToDots(String s) { return s.replace('$', '.'); }
  
  /** Loop over the specified string, and replace any '.' with '$'  This is used to change an inner class name from 
    * external (as in Java source) to internal (as in class files) format.
    * @param s  The String to change.
    * @return   The converted string.
    */
  public static String dotsToDollarSigns(String s) { return s.replace('.', '$'); }
  
  /** Determines the name of the next anonymous inner class (enclosing class name + '$' + sequence number). Looks 
    * through the list of inner classes of this data to see if there is a match.  (It should succeed).  
    * @return the SymbolData for next anonymous inner class of this data; null if it cannot be found
    */
  public SymbolData getNextAnonymousInnerClass() {
    String name = getSymbolData().getName() + '$' + getSymbolData().preincrementAnonymousInnerClassNum();
//    System.err.println("**** Looking up anonymous inner class " + name);
    LinkedList<SymbolData> myDatas = getInnerClasses();
    SymbolData myData = null;
    //look through the inner classes for the data
    for (int i = 0; i < myDatas.size(); i++) {
      if (myDatas.get(i).getName().equals(name)) {
        myData = myDatas.get(i);
        break;
      }
    }
    return myData;
  }
  
  /** Reset the block iterator to the beginning of the list of blocks. */
  public void resetBlockIterator() { _blockIterator = null; }
  
  /** Returns the next block contained within this data.
   * @return a BlockData, or null if none exists.
   */
  public BlockData getNextBlock() {
    if (_blockIterator == null) { _blockIterator = _blocks.iterator(); }

    if (_blockIterator.hasNext()) { return _blockIterator.next(); }
    else { return null; }
  }
  
  /** Add a BlockData to this Data's list of blocks. */
  public void addBlock(BlockData b) { _blocks.add(b); }
  
  /** Remove all blocks from this data's list of enclosed blocks.  (Used to simplify testing.) */
  public void removeAllBlocks() { _blocks.clear(); }
  
  /** Takes in a relative name and tries to match it with one of this Data's inner classes or inner interfaces.  The 
    * relName argument is a name relative to this SymbolData (such as B to request the the class with this
    * relative name within some enclosing symbol data or B$C to request the class A.B.C from class A) and
    * may be delimited by '.' or '$' (??).  If the name is not found in this Data, checks the outer data (if there is 
    * one), which will recursively search up the chain of enclosing Datas. If no matching visible inner classes or 
    * interfaces are found, but one or more that are not visible are found, one of the non-visible ones will be 
    * returned. This means that checkAccess should be called after this method.
    * TODO: Is support for '$' delimiter required to process inner classes in class files?  Yes. 
    * !!! Eliminate the kludge in this method.
    * @param relName  The name of the inner class or interface to find RELATIVE to this SymbolData
    * @return  The SymbolData for the matching inner class or interface is null if there isn't one.
    */
  public SymbolData getInnerClassOrInterface(String relName) {
//    if (relName.equals("MyInner")) System.err.println("getInnerClass('" + relName + "') called on '" + this + "'");
    int firstIndexOfDot = relName.indexOf('.');
    int firstIndexOfDollar = relName.indexOf("$");
    if (firstIndexOfDot == -1) firstIndexOfDot = firstIndexOfDollar;
    else if (firstIndexOfDollar >= 0 && firstIndexOfDollar < firstIndexOfDot) firstIndexOfDot = firstIndexOfDollar;

    // First, look through the inner classes/interfaces of this class
    SymbolData privateResult = null;
    SymbolData result = getInnerClassOrInterfaceHelper(relName, firstIndexOfDot);
    if (relName.equals("MyInner")) {
//      System.err.println("getInnerClassOrInterfaceHelper('" + relName + "', " + firstIndexOfDot + ")");
//      System.err.println("_innerClasses = " + _innerClasses);
//      System.err.println("Result is: '" + result + "'");
    }
    if (result != null) {
//      System.err.println("Result is: '" + result + "'");
      SymbolData outerPiece;
      if (firstIndexOfDot > 0) {
        outerPiece = getInnerClassOrInterfaceHelper(relName.substring(0, firstIndexOfDot), -1);
      }
      else { outerPiece = result; }
      if (TypeChecker.checkAccess(outerPiece.getMav(), outerPiece, getSymbolData())) return result;
      else {
        privateResult = result; 
        result = null;
      }
    }
    
    // Call this method recursively on the outer data; anything our outer class can see we can see, so there is no 
    // eason to check accessibility here
    if (_outerData != null) {
      result = _outerData.getInnerClassOrInterface(relName);
//      if (relName.equals("MyInner")) System.err.println("outerResult = " + result);
      if (result != null) return result;
    }
    
    return privateResult;
  }
  
  /** Takes in a relative name and tries to match it with one of this Data's inner classes or inner interfaces.  The 
    * relName argument is a name relative to this SymbolData (such as B.C to request the class A.B.C from class A) and 
    * may be delimited by '.' or '$'. This method is overridden in SymbolData (but not other concrete Data classes) to 
    * handle the fact that classes must check their super classes and interfaces and interfaces must check their super 
    * interfaces.
    * TODO: Kludge!  Only use dots to separate segments!!!
    * @return  The SymbolData for the matching inner class or interface or null if there isn't one.
    */
  protected SymbolData getInnerClassOrInterfaceHelper(String relName, int firstIndexOfDot) {
    Iterator<SymbolData> iter = innerClassesAndInterfacesIterator();
    while (iter.hasNext()) {
      SymbolData sd = iter.next();
      String sdName = sd.getName();

      sdName = LanguageLevelVisitor.getUnqualifiedClassName(sdName);
//      if (sdName.equals("MyInner")) System.err.println("In getInnerClass, sdName = '" + sdName + "'; relName = '"
//                                                         + relName +"'");
      if (firstIndexOfDot == -1) {
        if (sdName.equals(relName)) return sd;
      }
      else {
        if (sdName.equals(relName.substring(0, firstIndexOfDot))) {
          return sd.getInnerClassOrInterface(relName.substring(firstIndexOfDot + 1));
        }
      }
    }
    return null;
  }
  
  public Iterator<SymbolData> innerClassesAndInterfacesIterator() { return _innerClasses.iterator(); }
  
  /** @return  The inner classes of this Data. */
  public LinkedList<SymbolData> getInnerClasses() { return _innerClasses; }
  
  /** Sets the inner classes of this Data. */
  public void setInnerClasses(LinkedList<SymbolData> innerClasses) { _innerClasses = innerClasses; }  
  
  /** Add the specified SymbolData to the end of the list of inner classes.
    * @param innerClass  The SymbolData to add.
    */
  public void addInnerClass(SymbolData innerClass) { _innerClasses.addLast(innerClass); }
  
  /** @return  true if this data has the specified String modifier, and false otherwise. */
  public boolean hasModifier(String modifier) {
    if (getMav() == null) {return false;}
    String[] mavStrings = _modifiersAndVisibility.getModifiers();
    for (int i = 0; i < mavStrings.length; i++) {
      if (mavStrings[i].equals(modifier)) {
        return true;
      }
    }
    return false;
  }
  
  //TODO: now that we have this, can we factor out some code in VariableData?
  /** Add the specified modifier to the modifiers and visibility for this data, if it is not already present.
    * @param modifier  The String to add.
    */
  public void addModifier(String modifier) {
    if (! hasModifier(modifier)) {
      if (_modifiersAndVisibility == null) { setMav(new ModifiersAndVisibility(SourceInfo.NONE, new String[0])); }
      String[] modifiers = _modifiersAndVisibility.getModifiers();
      String[] newModifiers = new String[modifiers.length + 1];
      newModifiers[0] = modifier;
      for (int i = 1; i <= modifiers.length; i++) {
        newModifiers[i] = modifiers[i-1];
      }
      _modifiersAndVisibility = new ModifiersAndVisibility(_modifiersAndVisibility.getSourceInfo(), newModifiers);
    }    
  }
  
  /** Check if varName is used in this Data's scope.  If so, find a new name for the variable by appending a counter to 
    * its name until an unused variable name results.  Return the new name.
    * @param varName  The initial String name of the variable we are creating.
    * @return  The new variable name which does not shadow anything in vars.
    */
  public String createUniqueName(String varName) {
    VariableData vd = 
      TypeChecker.getFieldOrVariable(varName, this, getSymbolData(), new NullLiteral(SourceInfo.NONE), getVars(), 
                                     true, false);
    String newName = varName;
    int counter = 0;  // Note: counter overflow is effectively impossible; 2G anonymous classes would blow memory
    while (vd != null && counter != -1) {
      newName = varName + counter; counter++;
      vd = TypeChecker.getFieldOrVariable(newName, this, getSymbolData(), new NullLiteral(SourceInfo.NONE), 
                                          getVars(), true, false);
    }
    if (counter == -1) { throw new RuntimeException("Internal Program Error: Unable to rename variable " + varName
                                                      + ".  All possible names were taken.  Please report this bug");}
    return newName; 
  }

  
  /** Test the methods in the above class. */
  public static class DataTest extends TestCase {
    
    private Data _d;
    
    private ModifiersAndVisibility _publicMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] { "public" });
    private ModifiersAndVisibility _staticMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] { "static" });
    private ModifiersAndVisibility _lotsaMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] { "public", "final", "abstract" });
    private ModifiersAndVisibility _protectedMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] { "protected" });
    private ModifiersAndVisibility _privateMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] { "private" });
    
    public DataTest() { this("");}
    public DataTest(String name) { super(name); }

    public void testRepeatedName() {
      _d = new SymbolData("myname");
      
      VariableData vd = new VariableData("v1", _publicMav, SymbolData.INT_TYPE, false, _d);

      // Compare a vd to an symbol data with no vars
      assertFalse("No variables to repeat name", _d._repeatedName(vd));
      
      // Compare a vd to a symbol data with 1 var with a different name
      _d.addVar(new VariableData("v2", _protectedMav, SymbolData.BOOLEAN_TYPE, true, _d));
      assertFalse("No repeated name", _d._repeatedName(vd));
      
      // Compare a vd to a symbol data who has a var with the same name
      _d.addVar(vd);
      assertTrue("Should be repeated name", _d._repeatedName(vd));
//      System.err.println("testRepeatedName finished");
    }
    
    public void testIsAbstract() {
      _d = new SymbolData("myName");
      _d.setMav(_publicMav);
      
      // not abstract
      assertFalse("Should not be abstract", _d.hasModifier("abstract"));
      
      _d.setMav(_lotsaMav);

      // abstract
      assertTrue("Should be abstract", _d.hasModifier("abstract"));
      
//      System.err.println("testIsAbstract finished");
    }
   
    public void testAddVar() {
      _d = new SymbolData("myName");
      VariableData vd = new VariableData("v1", _publicMav, SymbolData.INT_TYPE, true, _d);
      VariableData vd2 = new VariableData("v2", _publicMav, SymbolData.CHAR_TYPE, true, _d);
      LinkedList<VariableData> myVds = new LinkedList<VariableData>();
      myVds.addLast(vd);
      
      //first variable added
      assertTrue("Should be able to add first variable", _d.addVar(vd));
      assertEquals("Variable list should have 1 variable, vd", myVds, _d.getVars());

      //duplicate variable
      assertFalse("Should not be able to add same variable again", _d.addVar(vd));
      assertEquals("Variable list should not have changed", myVds, _d.getVars());
      
      //different variable
      myVds.addLast(vd2);
      assertTrue("Should be able to add a different variable", _d.addVar(vd2));
      assertEquals("Variable list should have 2 variables, vd, vd2", myVds, _d.getVars());
      
//      System.err.println("testAddVar finished");
      
    }
    
    public void testAddVars() {
      _d = new SymbolData("genius");
      VariableData vd = new VariableData("v1", _publicMav, SymbolData.INT_TYPE, true, _d);
      VariableData vd2 = new VariableData("v2", _publicMav, SymbolData.CHAR_TYPE, true, _d);
      VariableData[] toAdd = new VariableData[] {vd, vd2};
      LinkedList<VariableData> myVds = new LinkedList<VariableData>();
      
      //first adding array
      myVds.addLast(vd);
      myVds.addLast(vd2);
      assertTrue("Should be able to add new vars array", _d.addVars(toAdd));
      assertEquals("Variable list should have 2 variables", myVds, _d.getVars());
      
      //trying to read array whose variables are already there.
      assertFalse("Should not be able to add same variables again", _d.addVars(toAdd));
      assertEquals("Variable list should not have changed", myVds, _d.getVars());
      
      //trying to add a different array
      VariableData vd3 = new VariableData("v3", _publicMav, SymbolData.INT_TYPE, true, _d);
      VariableData[] toAdd2 = new VariableData[] {vd3};
      myVds.addLast(vd3);
      
      assertTrue("Should be able to add new variable array", _d.addVars(toAdd2));
      assertEquals("Variable list should now have 3 variables", myVds, _d.getVars());
       
      //try adding an empty array of variable datas
      assertTrue("Should be able to add an empty array", _d.addVars(new VariableData[0]));
      assertEquals("Variable list should not have changed by adding empty array", myVds, _d.getVars());
      
//      System.err.println("testAddVars finished");
    }
    
    public void testGetVar() {
     _d = new SymbolData("woah");
     VariableData vd = new VariableData("v1", _publicMav, SymbolData.INT_TYPE, false, _d);
     VariableData vd2 = new VariableData("v2", _publicMav, SymbolData.CHAR_TYPE, true, _d);
     VariableData[] toAdd = new VariableData[] {vd, vd2};
     _d.addVars(toAdd);
     
     // Lookup a name that should be there
     assertEquals("Should return vd", vd, _d.getVar("v1"));
     
     // Lookup a name that should not be there
      assertEquals("Should return null--no variable with that name", null, _d.getVar("whatever"));
//      System.err.println("testGetVar finished");
    }
    
    public void testIsOuterData() {
      _d = new SymbolData("asdf");      
      SymbolData d2 = new SymbolData("qwer");
      SymbolData d246 = new SymbolData("fdsa");
      d2.setOuterData(_d);
      _d.setOuterData(d246);
      assertTrue("d246 should be outer data of d2", d2.isOuterData(d246));
      assertTrue("d246 should be outer data of _d", _d.isOuterData(d246));
      assertFalse("d2 should not be outer data of d246", d246.isOuterData(d2));
//      System.err.println("testIsOuterData finished");
    }
    
    public void testGetInnerClassOrInterface() {
      SymbolData sd1 = new SymbolData("testing");
      SymbolData sd2 = new SymbolData("testing$test123");
      SymbolData sd3 = new SymbolData("testing$test123$test1234");
      sd1.addInnerClass(sd2);
      sd2.addInnerClass(sd3);
      
      // One level can be found
      SymbolData result = sd1.getInnerClassOrInterface("test123");
      assertEquals("The correct inner SymbolData should be returned", sd2, result);
      
      // Dollars or dots are okay, and nested inner classes can be found
      result = sd2.getInnerClassOrInterface("test1234");
      assertEquals("The correct nested inner SymbolData should be returned", sd3, result);
      
      //dollars or dots are okay, and nested inner classes can be found
      result = sd1.getInnerClassOrInterface("test123.test1234");
      assertEquals("The correct nested inner SymbolData should be returned", sd3, result);

      // Dollars or dots are okay, and nested inner classes can be found
      result = sd1.getInnerClassOrInterface("test123$test1234");
      assertEquals("The correct nested inner SymbolData should be returned", sd3, result);

      // null is returned when a non-present inner class is looked for.
      result = sd1.getInnerClassOrInterface("testing.notYourInnerClass");
      assertEquals("null should be returned", null, result);

      SymbolData sd4 = new SymbolData("testing");
      SymbolData sd5 = new SymbolData("testing$test123");
      sd5.setInterface(true);
      SymbolData sd6 = new SymbolData("testing$test123$2test1234");
      sd4.addInnerInterface(sd5);
      sd5.addInnerClass(sd6);
      
      // One level can be found
      result = sd4.getInnerClassOrInterface("test123");
      assertEquals("The correct inner SymbolData should be returned", sd5, result);
      
      //dollars or dots are okay, and nested inner classes can be found
      result = sd5.getInnerClassOrInterface("test1234");
      assertEquals("The correct nested inner SymbolData should be returned", sd6, result);
      
      //dollars or dots are okay, and nested inner classes can be found
      result = sd4.getInnerClassOrInterface("test123.test1234");
      assertEquals("The correct nested inner SymbolData should be returned", sd6, result);

      //null is returned when a non-present inner class is looked for.
      result = sd4.getInnerClassOrInterface("testing.notYourInnerClass");
      assertEquals("null should be returned", null, result);
      
      //Test a class defined in the context of a method
      SymbolData sd7 = new SymbolData("test123.myMethod$bob");
      sd7.setIsContinuation(false);
      MethodData md = new MethodData("myMethod", _publicMav, new TypeParameter[0], 
                    SymbolData.INT_TYPE, new VariableData[0], new String[0], sd1, 
                    new NullLiteral(SourceInfo.NONE));
      md.addInnerClass(sd7);
      assertEquals("Should return sd7", sd7, md.getInnerClassOrInterface("bob"));
      
      //Test an ambiguous case, where the inner class is in both the super interface and the super class.
      SymbolData interfaceInner = new SymbolData("MyInterface$MyInner");
      interfaceInner.setIsContinuation(false);
      interfaceInner.setInterface(true);

      SymbolData superInner = new SymbolData("MySuper$MyInner");
      superInner.setIsContinuation(false);
      
      SymbolData myInterface = new SymbolData("MyInterface");
      myInterface.setInterface(true);
      myInterface.setIsContinuation(false);
      myInterface.addInnerClass(interfaceInner);
      interfaceInner.setOuterData(myInterface);
      
      SymbolData mySuper = new SymbolData("MySuper");
      mySuper.addInnerClass(superInner);
      superInner.setOuterData(mySuper);
      
      SymbolData me = new SymbolData("Me");
      me.setSuperClass(mySuper);
      me.addInterface(myInterface);
      
      assertEquals("Should return SymbolData.AMBIGUOUS_REFERENCE", SymbolData.AMBIGUOUS_REFERENCE, 
                   me.getInnerClassOrInterface("MyInner"));
      
      // Test a case where the inner class is private in one, but not the other
      superInner.setMav(_privateMav);
      assertEquals("Should return interfaceInner", interfaceInner, me.getInnerClassOrInterface("MyInner"));
      
      // Test a case where the inner class is private in both--returns one of them
      interfaceInner.setMav(_privateMav);
      assertEquals("Should return interfaceInner", interfaceInner, me.getInnerClassOrInterface("MyInner"));
 
      // Test a case where the inner most class is private, but one layer is public
      interfaceInner.setMav(_publicMav);
      SymbolData innerInterfaceInner = new SymbolData("MyInterface$MyInner$Inner");
      innerInterfaceInner.setMav(_privateMav);
      interfaceInner.addInnerClass(innerInterfaceInner);
      innerInterfaceInner.setOuterData(interfaceInner);
      assertEquals("Should return innerInterfaceInner", innerInterfaceInner, 
                   me.getInnerClassOrInterface("MyInner.Inner"));
      
//      System.err.println("testGetInnerClassOrInterface finished");
    }
    
    public void testCreateUniqueName() {
      // where varName is not defined
      MethodData md = new MethodData("foozle", new VariableData[0]);
      md.addVars(md.getParams());
      md.setOuterData(new SymbolData("Fooz"));
      String result = md.createUniqueName("avar");
      assertEquals("the result is correct", "avar", result);
        
      // where varName is defined in a method signature
      VariableData vd = new VariableData("avar", _publicMav, SymbolData.INT_TYPE, true, null);
      md = new MethodData("foozleWithAvar", new VariableData[] {vd});
      vd.setEnclosingData(md);
      md.addVars(md.getParams());
      md.setOuterData(new SymbolData("Fooz"));
      result = md.createUniqueName("avar");
      assertEquals("the result is correct", "avar0", result);
      
      // where varName & varName0 are defined in the class
      SymbolData sd = new SymbolData("RandomClass");
      VariableData vd0 = new VariableData("avar0", _publicMav, SymbolData.DOUBLE_TYPE, true, sd);
      vd.setEnclosingData(sd);
      sd.addVars(new VariableData[] {vd, vd0});
      result = sd.createUniqueName("avar");
      assertEquals("the result is correct", "avar1", result);
      
      // where varName is defined in an enclosing class
      sd = new SymbolData("RandomClass");
      sd.setMav(_staticMav);
      SymbolData sd2 = new SymbolData("IAteRandomClass");
      sd2.addInnerClass(sd);
      sd.setOuterData(sd2);
      sd.addVar(vd);
      result = sd.createUniqueName("avar");
      assertEquals("the result is correct", "avar0", result);
      
      // where varName is defined in a super class and enclosing class
      SymbolData sd3 = new SymbolData("RandomsMama");
      sd.setSuperClass(sd3);
      sd3.addVar(vd0);
      result = sd.createUniqueName("avar");
      assertEquals("the result is correct", "avar1", result);
//      System.err.println("testCreateName finished");
    }

    public void testGetNextAnonymousInnerClass() {
      SymbolData sd1 = new SymbolData("silly");
      sd1.setIsContinuation(false);
      
      _d = new BlockData(sd1);
      
      SymbolData anon1 = new SymbolData("silly$1");
      anon1.setIsContinuation(false);
      SymbolData anon2 = new SymbolData("silly$2");
      anon2.setIsContinuation(false);
      sd1.addInnerClass(anon1);
      anon1.setOuterData(sd1);
      _d.addInnerClass(anon2);
      anon2.setOuterData(_d);
      
      assertEquals("Should return anon1", anon1, sd1.getNextAnonymousInnerClass());
      assertEquals("Should return anon2", anon2, _d.getNextAnonymousInnerClass());
      assertEquals("Should return null", null, _d.getNextAnonymousInnerClass());
      assertEquals("Should return null", null, sd1.getNextAnonymousInnerClass());
      
//      System.err.println("testGetNextAnonymousInnerClass finished");
    } 
  }
}
