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

package edu.rice.cs.javalanglevels;

import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.javalanglevels.parser.JExprParser;
import java.util.*;
import java.io.File;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.*;

import junit.framework.TestCase;


/** Used to type check the LHS of an assignment expression such as += or -=, where the left hand side needs to not be final
  * and already have a value.
  */
public class LValueWithValueTypeChecker extends JExpressionIFAbstractVisitor<TypeData> {
  
  /** Instance of the testAssignable visitor that will be used to make sure that if the lhs
    * is something of the type that could be assigned to, it can actually be assigned to*/
  private final TestAssignable _testAssignableInstance;

  // The visitor that invoked this: holds the error list
  private final SpecialTypeChecker _bob;

  /* Constructor for LValueTypeChecker.  Initializes _testAssignableInstance.
   * @param bob  The visitor that invoked this visitor.
   */
  public LValueWithValueTypeChecker(SpecialTypeChecker bob) {
    _testAssignableInstance = new TestAssignable(bob._data, bob._file, bob._package, bob._importedFiles, bob._importedPackages, bob._vars, bob._thrown);
    _bob = bob;
  }
  
  /** Most expressions cannot appear on the lhs of an assignment: give an appropriate error */
  public TypeData defaultCase(JExpressionIF that) {
    _bob._addError("You cannot assign a value to an expression of this kind.  Values can only be assigned to fields or variables", that);
    return null;
  }
  
   /**An increment expression is a special case that cannot appear on the lhs*/
  public TypeData forIncrementExpression(IncrementExpression that) {
    _bob._addError("You cannot assign a value to an increment expression", that);
    return null;
  }

  /* Names can appear on the lhs, so check to see if the name can be assigned to*/
  public TypeData forNameReference(NameReference that) {
    return that.visit(_testAssignableInstance);
  }

  /** Array accesses can appear on the lhs, so check to see if the array can be assigned to*/
  public TypeData forArrayAccess(ArrayAccess that) {
    return that.visit(_testAssignableInstance);
  }
      
  /** Recur on the value stored in the parentheses*/
  public TypeData forParenthesized(Parenthesized that) {
    return that.getValue().visit(this);
  }
  
  /** Checks to see if what is on the lhs is assignable to and already has a value*/
  private class TestAssignable extends ExpressionTypeChecker {
  
    public TestAssignable(Data data, File file, String packageName, LinkedList<String> importedFiles, LinkedList<String> importedPackages, LinkedList<VariableData> vars, LinkedList<Pair<SymbolData, JExpression>> thrown) {
      super(data, file, packageName, importedFiles, importedPackages, vars, thrown);
    }

    /** The variable referenced here should already have a value and should not be final. */
    public TypeData forSimpleNameReference(SimpleNameReference that) {
      Word myWord = that.getName();
      myWord.visit(this);
      
      VariableData reference = getFieldOrVariable(myWord.getText(), _data, _data.getSymbolData(), that, _vars, true, true);
      if (reference != null) {
        if (!reference.hasValue()) {
          _addError("You cannot use " + reference.getName() + " here, because it may not have been given a value", that.getName());
        }
        else if (reference.isFinal()) {
          _addError("You cannot assign a new value to " + reference.getName() + " because it is immutable and has already been given a value", that.getName());
        }
        
        //if reference is non-static, but context is static, give error
        else if (! reference.hasModifier("static") && inStaticMethod()) {
          _addError("Non static field or variable " + reference.getName() + " cannot be referenced from a static context", that);
        }
        
        return reference.getType().getInstanceData();
        
      }
      
      SymbolData classR = findClassReference(null, myWord.getText(), that);
      if (classR == SymbolData.AMBIGUOUS_REFERENCE) {return null;}
      if (classR != null) {
        if (checkAccess(that, classR.getMav(), classR.getName(), classR, _data.getSymbolData(), "class or interface", false)) {
          return classR;
        }
      }
      PackageData packageD = new PackageData(myWord.getText());
      return packageD;
    }
    
    /**
     * Here is a table that explains what is allowed:
     *              result:
     * left:        package |        symbol            | instance
     * package  |     yes      yes(if class exists)         no
     * symbol   |     no       yes, if static inner class   yes if field is static and assignable
     * instance |     no       ERROR                        yes, if field is assignable
     */
    public TypeData forComplexNameReference(ComplexNameReference that) {
      ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages, _vars, _thrown);
      TypeData lhs = that.getEnclosing().visit(etc);
      _bob.thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned);
      Word myWord = that.getName();
      
      //if lhs is a package data, either we found a class reference or this piece is still part of the package
      if (lhs instanceof PackageData) {
        SymbolData classRef =  findClassReference(lhs, myWord.getText(), that);
        if (classRef != null) {return classRef;}
        return new PackageData((PackageData) lhs, myWord.getText());
      }
      
      //if the word is a variable reference, make sure it can be seen from this context
      VariableData reference = getFieldOrVariable(myWord.getText(), lhs.getSymbolData(), _data.getSymbolData(), that);
      if (reference != null) {
        if (lhs instanceof SymbolData) {
          //does this reference a field? if so, it must be static
          if (!reference.hasModifier("static")) {
            _addError("Non-static variable " + reference.getName() + " cannot be accessed from the static context " + Data.dollarSignsToDots(lhs.getName()) + ".  Perhaps you meant to instantiate an instance of " + Data.dollarSignsToDots(lhs.getName()), that);
            return reference.getType().getInstanceData();
          }
        }
        
        //make sure it already had a value
        if (!reference.hasValue()) {
          _addError("You cannot use " + reference.getName() + " here, because it may not have been given a value", that.getName());
        }
        
        //make sure it can be assigned
        if (!canBeAssigned(reference)) {
          _addError("You cannot assign a new value to " + reference.getName() + " because it is immutable and has already been given a value", that.getName());
        }
        return reference.getType().getInstanceData();
      }
      
      //does this reference an inner class? if so, it must be static
      SymbolData sd = getSymbolData(true, myWord.getText(), lhs.getSymbolData(), that, false);
      if (sd != null && sd != SymbolData.AMBIGUOUS_REFERENCE) {
        
        if (!checkAccess(that, sd.getMav(), sd.getName(), sd, _data.getSymbolData(), "class or interface")) {return null;}

        
        if (!sd.hasModifier("static")) {
          _addError("Non-static inner class " +Data.dollarSignsToDots(sd.getName()) + " cannot be accessed from this context.  Perhaps you meant to instantiate it", that);
        }
        
        //you cannot reference static inner classes from the context of an instantiation of their outer class
        else if (lhs instanceof InstanceData) {
          _addError("You cannot reference the static inner class " + Data.dollarSignsToDots(sd.getName()) + " from an instance of " + Data.dollarSignsToDots(lhs.getName()), that);
        }
        return sd;
      }
      
      if (sd != SymbolData.AMBIGUOUS_REFERENCE) {_addError("Could not resolve " + myWord.getText() + 
                                                           " from the context of " + 
                                                           Data.dollarSignsToDots(lhs.getName()), that);}
      return null;
    }
    
    /** Type-check the lhs and the index. */
    public TypeData forArrayAccess(ArrayAccess that) {
      ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages, _vars, _thrown);
      TypeData lhs = that.getArray().visit(etc);
      _bob.thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned); //update internal SpecialTypeChecker's list of what got assigned
      
      ExpressionTypeChecker indexTC = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages, _vars, _thrown);
      TypeData index = that.getIndex().visit(indexTC);
      _bob.thingsThatHaveBeenAssigned.addAll(indexTC.thingsThatHaveBeenAssigned); //update internal SpecialTypeChecker's list of what got assigned
      
      return forArrayAccessOnly(that, lhs, index);
    }
    
  }
  

  /**Test the methods defined in the above class*/
  public static class LValueWithValueTypeCheckerTest extends TestCase {
    
    private LValueWithValueTypeChecker _lvtc;
    LValueWithValueTypeChecker.TestAssignable _ta;
    
    
    private SymbolData _sd1;
    private SymbolData _sd2;
    private SymbolData _sd3;
    private SymbolData _sd4;
    private SymbolData _sd5;
    private SymbolData _sd6;
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[0]);
    private ModifiersAndVisibility _abstractMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final"});
    private ModifiersAndVisibility _finalPublicMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final", "public"});
    private ModifiersAndVisibility _publicAbstractMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public", "abstract"});
    private ModifiersAndVisibility _publicStaticMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public", "static"});
    
    
    public LValueWithValueTypeCheckerTest() {
      this("");
    }
    public LValueWithValueTypeCheckerTest(String name) {
      super(name);
    }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");
      _lvtc = 
        new LValueWithValueTypeChecker(new SpecialTypeChecker(_sd1, new File(""), "", new LinkedList<String>(), 
                                                              new LinkedList<String>(), new LinkedList<VariableData>(), 
                                                              new LinkedList<Pair<SymbolData, JExpression>>()));
      _ta = _lvtc._testAssignableInstance;
      _lvtc._bob.errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable.clear();
//      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, IterUtil.<File>empty());
      _lvtc._bob._importedPackages.addFirst("java.lang");
    }
    
    //Test methods of LeftValueTypeChecker:

    public void testDefaultCase() {
      //should add an error
      new NullLiteral(SourceInfo.NONE).visit(_lvtc);
      assertEquals("Should be 1 error", 1, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "You cannot assign a value to an expression of this kind.  Values can only be assigned to fields or variables",
                   _lvtc._bob.errors.getLast().getFirst());

      //should add an error
      new PlusExpression(SourceInfo.NONE, new IntegerLiteral(SourceInfo.NONE, 21), new IntegerLiteral(SourceInfo.NONE, 22)).visit(_lvtc);
      assertEquals("Should be 2 errors", 2, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "You cannot assign a value to an expression of this kind.  Values can only be assigned to fields or variables",
                   _lvtc._bob.errors.getLast().getFirst());
    }
    
    public void testForIncrementExpression() {
      //should add an error
      PositivePrefixIncrementExpression p = new PositivePrefixIncrementExpression(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "bob")));
      assertEquals("Should return null", null, p.visit(_lvtc));
      assertEquals("Should be 1 error", 1, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "You cannot assign a value to an increment expression", _lvtc._bob.errors.getLast().getFirst());
    }
  

    public void testForSimpleNameReference() {
      //first, consider the case where what we have is a variable reference:
      SimpleNameReference var = new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "variable1"));
      VariableData varData = new VariableData("variable1", _publicMav, SymbolData.INT_TYPE, false, _ta._data);
      _ta._vars.add(varData);
      
      //in this case, it has not been initialized--should throw error because it needs to have already had a value
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), var.visit(_lvtc));
      assertEquals("Should be 1 error", 1, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "You cannot use variable1 here, because it may not have been given a value", _lvtc._bob.errors.getLast().getFirst());
      
      //if it has been initialized but is not final, do not give an error
      varData.gotValue();
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), var.visit(_lvtc));
      assertEquals("Should still be 1 error", 1, _lvtc._bob.errors.size());

      //if it has been initialized and is final, give an error
      varData.setMav(_finalMav);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), var.visit(_lvtc));
      assertEquals("Should be 2 errors", 2, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "You cannot assign a new value to variable1 because it is immutable and has already been given a value", _lvtc._bob.errors.getLast().getFirst());
      varData.setMav(_publicMav);
      
      //if variable is non-static, but you are in static context, cannot reference it. Should give error
      MethodData newContext = new MethodData("method", _publicStaticMav, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[0], new String[0], _sd1, new NullLiteral(SourceInfo.NONE)); 
      _ta._data = newContext;
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), var.visit(_lvtc));
      assertEquals("Should be 3 errors", 3, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "Non static field or variable variable1 cannot be referenced from a static context", _lvtc._bob.errors.getLast().getFirst());
      _ta._data = _sd1;
      
      //if it is a variable of your super class, it won't be in _vars.  Check this case.
      _ta._vars = new LinkedList<VariableData>();
      _sd1.setSuperClass(_sd2);
      _sd2.addVar(varData);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), var.visit(_lvtc));
      assertEquals("Should still be 3 errors", 3, _lvtc._bob.errors.size());

      //now, consider the case where what we have is a class reference:
      SimpleNameReference className = new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "Frog"));
      SymbolData frog = new SymbolData("Frog");
      frog.setIsContinuation(false);
      _lvtc._bob.symbolTable.put("Frog", frog);
      
      //if it is not visibile from this context, return package data
      TypeData result = className.visit(_lvtc);
      assertTrue("Result should be a PackageData since Frog is not accessible", result instanceof PackageData);
      assertEquals("Should have correct name", "Frog", result.getName());
      assertEquals("Should still be 3 errors", 3, _lvtc._bob.errors.size());
      
      //if it is visibile from this context, no error
      frog.setMav(_publicMav);
      assertEquals("Should return Frog", frog, className.visit(_lvtc));
      assertEquals("Should still be 3 errors", 3, _lvtc._bob.errors.size());
      
      //If the name cannot be resolved, simply return a packageData.
      SimpleNameReference fake = new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "notRealReference"));
      assertEquals("Should return package data", "notRealReference", (fake.visit(_lvtc)).getName());
      assertEquals("Should still be just 3 errors", 3, _lvtc._bob.errors.size());
      
      //if the reference is ambiguous (matches both an interface and a class) give an error
      SimpleNameReference ambigRef = new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "ambigThing"));

      SymbolData interfaceD = new SymbolData("interface");
      interfaceD.setIsContinuation(false);
      interfaceD.setInterface(true);
      interfaceD.setMav(_publicMav);
      
      SymbolData classD = new SymbolData("superClass");
      classD.setIsContinuation(false);
      classD.setMav(_publicMav);
      
      SymbolData ambigThingI = new SymbolData("ambigThing");
      ambigThingI.setIsContinuation(false);
      ambigThingI.setInterface(true);
      interfaceD.addInnerInterface(ambigThingI);
      ambigThingI.setOuterData(interfaceD);
      ambigThingI.setMav(_publicStaticMav);
      
      SymbolData ambigThingC = new SymbolData("ambigThing");
      ambigThingC.setIsContinuation(false);
      classD.addInnerClass(ambigThingC);
      ambigThingC.setOuterData(classD);
      ambigThingC.setMav(_publicStaticMav);
      
      _sd6.addInterface(interfaceD);
      _sd6.setSuperClass(classD);
      
      _sd6.setMav(_publicMav);
      _sd6.setIsContinuation(false);
      
      _ta._data = _sd6;

      assertEquals("Should return null", null, ambigRef.visit(_lvtc));
      assertEquals("Should be 4 errors", 4, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "Ambiguous reference to class or interface ambigThing", 
                   _lvtc._bob.errors.getLast().getFirst());    
    }
    
    
    public void testForComplexNameReference() {
      //if lhs is a package data, we want to keep building it:
      
      //if whole reference is just package reference, return package data
      ComplexNameReference ref1 = new ComplexNameReference(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "java")), new Word(SourceInfo.NONE, "lang"));
      assertEquals("Should return correct package data", "java.lang", ref1.visit(_lvtc).getName());
      assertEquals("Should be no errors", 0, _lvtc._bob.errors.size());
      
      //if reference builds to a class in the symbol table, return that class
      ComplexNameReference ref2 = new ComplexNameReference(SourceInfo.NONE, ref1, new Word(SourceInfo.NONE, "String"));
      SymbolData string = new SymbolData("java.lang.String");
      string.setPackage("java.lang");
      string.setMav(_publicMav);
      string.setIsContinuation(false);
      _lvtc._bob.symbolTable.put("java.lang.String", string);
      
      assertEquals("Should return string", string, ref2.visit(_lvtc));

      assertEquals("Should still be no errors", 0, _lvtc._bob.errors.size());
      

      //if lhs is not a package data, it gets more complicated:
      
      //we're referencing a variable inside of symbol data lhs:
      VariableData myVar = new VariableData("myVar", _publicStaticMav, SymbolData.DOUBLE_TYPE, true, string);
      string.addVar(myVar);
      ComplexNameReference varRef1 = new ComplexNameReference(SourceInfo.NONE, ref2, new Word(SourceInfo.NONE, "myVar"));
      
      //static var from static context
      assertEquals("Should return Double_Type instance", SymbolData.DOUBLE_TYPE.getInstanceData(), varRef1.visit(_lvtc));
      assertEquals("There should still be no errors", 0, _lvtc._bob.errors.size());
      
      //static uninitialized var from static context--give error
      myVar.lostValue();
      assertEquals("Should return Double_Type instance", SymbolData.DOUBLE_TYPE.getInstanceData(), varRef1.visit(_lvtc));
      assertEquals("There should be one error", 1, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "You cannot use myVar here, because it may not have been given a value", _lvtc._bob.errors.getLast().getFirst());

     
      //non-static var--this is a static context
      myVar.setMav(_publicMav);
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), varRef1.visit(_lvtc));
      assertEquals("Should be 2 errors", 2, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "Non-static variable myVar cannot be accessed from the static context java.lang.String.  Perhaps you meant to instantiate an instance of java.lang.String", _lvtc._bob.errors.getLast().getFirst());
      
      
      //non-static context, okay to reference non-static var
      VariableData stringVar = new VariableData("s", _publicMav, string, true, _lvtc._bob._data);
      _ta._vars.add(stringVar);
      myVar.gotValue();
      ComplexNameReference varRef2 = new ComplexNameReference(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "s")), new Word(SourceInfo.NONE, "myVar"));
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), varRef2.visit(_lvtc));
      assertEquals("Should still just be 2 errors", 2, _lvtc._bob.errors.size());
      
      //if it has been initialized and is final, give an error
      myVar.setMav(_finalPublicMav);
      myVar.gotValue();
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), varRef2.visit(_lvtc));
      assertEquals("Should be 3 errors", 3, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "You cannot assign a new value to myVar because it is immutable and has already been given a value", _lvtc._bob.errors.getLast().getFirst());
      myVar.setMav(_publicMav);

      //if it is a variable of the super class, you should still be able to see it.  Check this case.
      myVar.setMav(_publicMav);
      string.setVars(new LinkedList<VariableData>());
      string.setSuperClass(_sd2);
      _sd2.addVar(myVar);
      assertEquals("Should return double instance", SymbolData.DOUBLE_TYPE.getInstanceData(), varRef2.visit(_lvtc));
      assertEquals("Should still be 3 errors", 3, _lvtc._bob.errors.size());

      //here's a complex multiple variable reference case:
      VariableData vd1 = new VariableData("Mojo", _publicMav, SymbolData.INT_TYPE, true, _sd1);
      VariableData vd2 = new VariableData("Santa's Little Helper", _publicMav, _sd1, true, _sd2);
      VariableData vd3 = new VariableData("Snowball1", _publicMav, _sd2, true, _sd3);
      _sd3.addVar(vd3);
      _sd2.addVar(vd2);
      _sd1.addVar(vd1);
      
      ComplexNameReference varRef3 = new ComplexNameReference(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "Snowball1")),
                                                        new Word(SourceInfo.NONE, "Santa's Little Helper"));
      ComplexNameReference varRef4 = new ComplexNameReference(SourceInfo.NONE, varRef3, new Word(SourceInfo.NONE, "Mojo"));
      
      Data oldData = _lvtc._bob._data;
      _lvtc._bob._data = _sd3;
      _lvtc._bob._vars.add(vd3);

      TypeData result = varRef4.visit(_lvtc);
      assertEquals("Should return int instance", SymbolData.INT_TYPE.getInstanceData(), result);
      assertEquals("Should still be 3 errors", 3, _lvtc._bob.errors.size());
      

      _lvtc._bob._data = oldData;
      

      //what if what we have is an inner class?
      SymbolData inner = new SymbolData("java.lang.String$Inner");
      inner.setPackage("java.lang");
      inner.setIsContinuation(false);
      inner.setOuterData(string);
      string.addInnerClass(inner);


      //if inner is not visible, throw error
      ComplexNameReference innerRef0 = new ComplexNameReference(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "s")), new Word(SourceInfo.NONE, "Inner"));
      assertEquals("Should return null", null, innerRef0.visit(_lvtc));
      assertEquals("Should be 4 errors", 4, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "The class or interface java.lang.String.Inner is package protected because there is no access specifier and cannot be accessed from i.like.monkey", _lvtc._bob.errors.getLast().getFirst());

      inner.setMav(_publicMav);
      
      //if inner is not static, give error:
      ComplexNameReference innerRef1 = new ComplexNameReference(SourceInfo.NONE, ref2, new Word(SourceInfo.NONE, "Inner"));
      assertEquals("Should return inner", inner, innerRef1.visit(_lvtc));
      assertEquals("Should be 5 errors", 5, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "Non-static inner class java.lang.String.Inner cannot be accessed from this context.  Perhaps you meant to instantiate it", _lvtc._bob.errors.getLast().getFirst());
  
      //if inner is not static and outer is not static, it's okay...
      ComplexNameReference innerRef2 = new ComplexNameReference(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "s")), new Word(SourceInfo.NONE, "Inner"));
      assertEquals("Should return inner", inner, innerRef2.visit(_lvtc));
      assertEquals("Should be 6 errors", 6, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "Non-static inner class java.lang.String.Inner cannot be accessed from this context.  Perhaps you meant to instantiate it", _lvtc._bob.errors.getLast().getFirst());

      //if inner is static and outer is not static, throw error
      inner.setMav(_publicStaticMav);
      assertEquals("Should return inner", inner, innerRef2.visit(_lvtc));
      assertEquals("Should be 7 errors", 7, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "You cannot reference the static inner class java.lang.String.Inner from an instance of java.lang.String", _lvtc._bob.errors.getLast().getFirst());
      
      
      //if the symbol could not be matched, give an error and return null
      ComplexNameReference noSense = new ComplexNameReference(SourceInfo.NONE, ref2, new Word(SourceInfo.NONE, "nonsense"));
      assertEquals("Should return null", null, noSense.visit(_lvtc));
      assertEquals("Should be 8 errors", 8, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "Could not resolve nonsense from the context of java.lang.String", _lvtc._bob.errors.getLast().getFirst());
    
      //if the reference is ambiguous (matches both an interface and a class) give an error
      ComplexNameReference ambigRef = new ComplexNameReference(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "cebu")), new Word(SourceInfo.NONE, "ambigThing"));

      SymbolData interfaceD = new SymbolData("interface");
      interfaceD.setIsContinuation(false);
      interfaceD.setInterface(true);
      interfaceD.setMav(_publicMav);
      
      SymbolData classD = new SymbolData("superClass");
      classD.setIsContinuation(false);
      classD.setMav(_publicMav);
      
      SymbolData ambigThingI = new SymbolData("ambigThing");
      ambigThingI.setIsContinuation(false);
      ambigThingI.setInterface(true);
      interfaceD.addInnerInterface(ambigThingI);
      ambigThingI.setOuterData(interfaceD);
      ambigThingI.setMav(_publicStaticMav);
      
      SymbolData ambigThingC = new SymbolData("ambigThing");
      ambigThingC.setIsContinuation(false);
      classD.addInnerClass(ambigThingC);
      ambigThingC.setOuterData(classD);
      ambigThingC.setMav(_publicStaticMav);
      
      _sd6.addInterface(interfaceD);
      _sd6.setSuperClass(classD);
      
      _lvtc._bob.symbolTable.put("cebu", _sd6);
      _sd6.setMav(_publicMav);
      _sd6.setIsContinuation(false);

      assertEquals("Should return null", null, ambigRef.visit(_lvtc));
      assertEquals("Should be 9 errors", 9, _lvtc._bob.errors.size());
      // TODO: should following error message mention the context 'cebu'?
      assertEquals("Error message should be correct", "Ambiguous reference to class or interface ambigThing", 
                   _lvtc._bob.errors.getLast().getFirst());    
    
    }
      
    
    public void testForArrayAccess() {
      ArrayData intArray = 
        new ArrayData(SymbolData.INT_TYPE, 
                      new LanguageLevelVisitor(_lvtc._bob._file,
                                               _lvtc._bob._package,
                                               null, // enclosingClassName for top level traversal
                                               _lvtc._bob._importedFiles, 
                                               _lvtc._bob._importedPackages, 
                                               new HashSet<String>(), 
                                               new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                               new LinkedList<Command>()),
                      SourceInfo.NONE);
      VariableData variable1 = new VariableData("variable1", _publicMav, intArray, true, _ta._data);
      _ta._vars.add(variable1);
      
      VariableData intVar = new VariableData("intVar", _publicMav, SymbolData.INT_TYPE, true, _ta._data);
      _ta._vars.add(intVar);

      MethodData makeArray = new MethodData("makeArray", _privateMav, new TypeParameter[0], intArray, new VariableData[0], new String[0], _ta._data.getSymbolData(), new NullLiteral(SourceInfo.NONE));
      _ta._data.getSymbolData().addMethod(makeArray);

      //first, a simple index into an int[]
      
      ArrayAccess a1 = new ArrayAccess(SourceInfo.NONE, 
                                       new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "variable1")),
                                       new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "intVar")));

      assertEquals("should return int", SymbolData.INT_TYPE.getInstanceData(), a1.visit(_lvtc));
      assertEquals("Should be 0 errors", 0, _lvtc._bob.errors.size());
      
      
      //make sure that an arbitrary expression can occur in the index
      
      ArrayAccess a2 = new ArrayAccess(SourceInfo.NONE,
                                       new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "variable1")),
                                       new PlusExpression(SourceInfo.NONE,
                                                          new IntegerLiteral(SourceInfo.NONE, 12),
                                                          new IntegerLiteral(SourceInfo.NONE, 22)));
                                                               
      assertEquals("should return int", SymbolData.INT_TYPE.getInstanceData(), a2.visit(_lvtc));
      assertEquals("Should be 0 errors", 0, _lvtc._bob.errors.size());
      
      //make sure that an arbitrary expression can occur in the array
      
      ArrayAccess a3 = new ArrayAccess(SourceInfo.NONE,
                                       new SimpleMethodInvocation(SourceInfo.NONE,
                                                                  new Word(SourceInfo.NONE, "makeArray"),
                                                                  new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0])),
                                       new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "intVar")));
      assertEquals("should return int", SymbolData.INT_TYPE.getInstanceData(), a3.visit(_lvtc));
      assertEquals("Should be 0 errors", 0, _lvtc._bob.errors.size());
    }
      
    public void testForParenthesized() {
      
      VariableData x = new VariableData("x", _publicMav, SymbolData.INT_TYPE, true, _ta._data);
      _ta._vars.add(x);
      
      // make sure (((x))) is okay
      Parenthesized p1 = new Parenthesized(SourceInfo.NONE,
                                           new Parenthesized(SourceInfo.NONE,
                                                             new Parenthesized(SourceInfo.NONE,
                                                                              new SimpleNameReference(SourceInfo.NONE,
                                                                                                      new Word(SourceInfo.NONE, "x")))));
      
      assertEquals("should return int", SymbolData.INT_TYPE.getInstanceData(), p1.visit(_lvtc));
      assertEquals("Should be 0 errors", 0, _lvtc._bob.errors.size());
      
      // make sure ((1)) breaks
      Parenthesized p2 = new Parenthesized(SourceInfo.NONE,
                                           new Parenthesized(SourceInfo.NONE,
                                                             new IntegerLiteral(SourceInfo.NONE, 1)));
      assertEquals("should return null", null, p2.visit(_lvtc));
      assertEquals("Should be 1 error", 1, _lvtc._bob.errors.size());
      assertEquals("Error message should be correct", "You cannot assign a value to an expression of this kind.  Values can only be assigned to fields or variables", 
                   _lvtc._bob.errors.getLast().getFirst());
      
    }
    
    
    
    //methods of TestAssignable are implicitly tested above.
    
  }
}
