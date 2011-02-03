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

/** SpecialTypeChecker is a base class for specialized type checkers such as BodyTypeChecker, ClassBodyTypeChecker, 
  * ExpressionTypeChecker, etc. SpecialTypeChecker maintains the context.
  */
public class SpecialTypeChecker extends TypeChecker {
  /** An incremental list of fields used because forward references among fields are not allowed.*/
  protected LinkedList<VariableData> _vars;
  
  /**Stores what variable datas have been newly given a value in this scope.*/
  protected LinkedList<VariableData> thingsThatHaveBeenAssigned;
  
  /**The context of this type checking--i.e. the data of the enclosing body.*/
  protected Data _data;
  
  /**The list of SymbolDatas corresponding to exceptions thrown in this body. */
  protected LinkedList<Pair<SymbolData, JExpression>> _thrown;

  /** Constructor for SpecialTypeChecker.
    * @param data  The data that represents the context.
    * @param file  The file that corresponds to the source file
    * @param packageName  The string representing the package name
    * @param importedFiles  The list of file names that have been specifically imported
    * @param importedPackages  The list of package names that have been specifically imported
    * @param vars  The list of fields that have been assigned up to the point where SpecialTypeChecker is called.
    * @param thrown  The list of exceptions that the context is declared to throw
    */
  public SpecialTypeChecker(Data data, File file, String packageName, LinkedList<String> importedFiles, 
             LinkedList<String> importedPackages, LinkedList<VariableData> vars, 
             LinkedList<Pair<SymbolData, JExpression>> thrown) {
    super(file, packageName, importedFiles, importedPackages);
    if (vars == null) throw new RuntimeException("SpecialTypeChecker called with _vars = null!");
    _data = data;
    _vars = vars;

    thingsThatHaveBeenAssigned = new LinkedList<VariableData>();
    _thrown = thrown;
  }
  
  /** @return the enclosing data*/
  protected Data _getData() { return _data; }
  
  /** @return true iff the first enclosing MethodData or SymbolData is a static method. */
  protected boolean inStaticMethod() {
    for (Data d = _data; d != null; d = d.getOuterData()) {
      if (d instanceof MethodData) { return d.hasModifier("static"); }
//      else if (d instanceof SymbolData) { return false; }
    }
    return false;
  }
  
  /** Return the symbol data corresponding to the lhs and the namePiece, if possible.
    * Otherwise, return null.
    * @param lhs  The left hand side of this complex reference, or null if this is a simple reference
    * @param namePiece  The String right hand side of this reference
    * @param jexpr  The JExpression corresponding to this class reference
    */
  protected SymbolData findClassReference(TypeData lhs, String namePiece, JExpression jexpr) {
    SymbolData result;
    if (lhs == null) {
      // Do not give an error if the SymbolData could not be found.  This is done later.
      result = getSymbolData(true, namePiece, _getData(), jexpr, false); 

    }
    
    else if (lhs instanceof PackageData) {
      // Do not give an error if the SymbolData could not be found
      result = getSymbolData(lhs.getName() + '.' + namePiece, _getData(), jexpr, false); 
    }

    else {
      // Do not give error if it could not be found, but do give an error if the reference is ambiguous.
      result = getSymbolData(true, namePiece, lhs.getSymbolData(), jexpr, false); 

      // Don't check for visibility here--check for it wherever this was called from.
    }
    
     return result;

  }

  
  /** TODO: Move this code to where it is needed?
    * Do any extra processing of this MethodInvocation, based on what level it is found at.
    * Here, check if the MethodData is declared to throw any exceptions, add them to the list of Exceptions
    * @param md  The MethodData of the method being invoked
    * @param jexpr  The jexpression corresponding to where this method is being invoked from.
    */
  protected void handleMethodInvocation(MethodData md, JExpression jexpr) {
    String[] thrown = md.getThrown();
    for (int i = 0; i<thrown.length; i++) {
      _thrown.addLast(new Pair<SymbolData, JExpression>(getSymbolData(thrown[i], _data, jexpr), jexpr));
    }
  }
  
  /**
   * Given a ParenthesizedExpressionList, extract its expression array and return
   * an array with InstanceDatas for each type of the arguments.  Throw an error if a non-instance type is
   * passed as an argument.
   * @param pel  The ParenthesizedExpressionList the arguments are stored in.
   * @return  The InstanceData[] corresponding to the types of the arguments
   */
  protected InstanceData[] getArgTypesForInvocation(ParenthesizedExpressionList pel) {
    Expression[] exprs = pel.getExpressions();
    InstanceData[] newArgs = new InstanceData[exprs.length];
    TypeData[] args = new TypeData[exprs.length];
    ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages,
                                                          _vars, _thrown);
    for (int i = 0; i < exprs.length; i++) {
      args[i] = exprs[i].visit(etc);
      if (args[i] == null || ! assertFound(args[i], exprs[i])) return null;
      if (!args[i].isInstanceType()) {
        _addError("Cannot pass a class or interface name as a constructor argument.  " + 
                  "Perhaps you meant to create an instance or use " + args[i].getName() + ".class", exprs[i]);
      }
      newArgs[i] = args[i].getInstanceData(); // getInstanceData() is used in place of a cast
    }
    thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned);
    return newArgs;
  }
  
/** Makes sure that the initializedvariable declarator is correct (the types match)
 * Also, add the Variable Data corresponding to this initializiation to the _vars list, so
 * that it can be referenced within this scope.
 * Assumes typeRes is a SymbolData.
 * @param that  The InitializedVariableDeclarator being visited
 * @param typeRes  The TypeData (should be a SymbolData) corresponding to the type on the lhs of the assignment
 * @param nameRes  Not used.
 * @param initializer_result  The type of what we are initializing the varaible with
 */
  public TypeData forInitializedVariableDeclaratorOnly(InitializedVariableDeclarator that, TypeData typeRes, 
                                                       TypeData nameRes, TypeData initializer_result) {
    if (initializer_result != null && assertFound(initializer_result, that.getInitializer())) {
      if (!initializer_result.isInstanceType()) {
        _addError("Field or variable " + that.getName().getText() + 
                  " cannot be initialized with the class or interface name " + initializer_result.getName() + 
                  ".  Perhaps you meant to create an instance or use " + initializer_result.getName() + ".class", that);
      }
      //we know typeRes is always a SymbolData.
      else if (!_isAssignableFrom(typeRes.getSymbolData(), initializer_result.getSymbolData())) {
        _addError("Type: \"" + typeRes.getName() + "\" expected, instead found type: \"" + 
                  initializer_result.getName() + '"', that);
      }
    }
    Word name = that.getName();
    String text = that.getName().getText();
    VariableData vd = _data.getVar(text);
    if (vd == null) {
      throw new RuntimeException("Internal Program Error: The field or variable " + text + 
                                 " was not found in this block.  Please report this bug.");
    }
    _vars.addLast(vd);
    return null;
  }
  
  /*This is not supported at any Language Level.  It should have been caught during the first pass.*/  
  public TypeData forInstanceInitializer(InstanceInitializer that) {
    throw new RuntimeException("Internal Program Error: Instance Initializers are not supported." + 
                               "  This should have been caught before the Type Checker Pass.  Please report this bug.");
  }

  /*This is not supported at any Language Level.  It should have been caught during the first pass.*/  
  public TypeData forStaticInitializer(StaticInitializer that) {
    throw new RuntimeException("Internal Program Error: Static Initializers are not supported." +
                               "  This should have been caught before the Type Checker Pass.  Please report this bug.");
  }
  
  /*This is not supported at any Language Level.  It should have been caught during the first pass.*/  
  public TypeData forLabeledStatement(LabeledStatement that) {
    throw new RuntimeException("Internal Program Error: Labeled Statements are not supported." + 
                               "  This should have been caught before the Type Checker Pass.  Please report this bug.");
  }
  
  /** Visits the expression with a new ExpressionTypeChecker, and return the result of that visitation.
    * Keep track of what variables get values within the expression.
    * @param that  The ExpressionStatement we are visiting.
    * @return  The result of visiting the expression with the ExpressionTypeChecker.
     */
  public TypeData forExpressionStatement(ExpressionStatement that) {
    ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages,
                                                          _vars, _thrown);
    final TypeData exprRes = that.getExpression().visit(etc);

    //do this so that we can keep track of anything that got assigned
    thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned);
    
    return exprRes;
  }
  
  /**
   * Visit the ThrowStatement's expression to determine what type of Exception is being thrown.
   * Add the corresponding SymbolData to _thrown.
   */
  public TypeData forThrowStatement(ThrowStatement that) {
    ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages,
                                                          _vars, _thrown);
    final TypeData thrown_result = that.getThrown().visit(etc);
    thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned);

    forThrowStatementOnly(that, thrown_result);
    return SymbolData.EXCEPTION.getInstanceData();
  }


  /** Make sure that what is being thrown is an instantiation of a class, not a class name, and that
    * it extends Throwable.  Otherwise, give an error.
    * @param that  The throw statement we are visiting
    * @param thrown_result  The TypeData result of visiting the throw statement.  It should be an InstanceData, 
    *                       unless there is an error in the student's code.
    */
  public TypeData forThrowStatementOnly(ThrowStatement that, TypeData thrown_result) {
    if (thrown_result == null || ! assertFound(thrown_result, that.getThrown())) return null;
  
    // add the SymbolData even if we're throwing a SymbolData, not an InstanceData
    _thrown.addLast(new Pair<SymbolData, JExpression>(thrown_result.getSymbolData(), that));
      
    //make sure they instantiated what is being thrown.
    if (!thrown_result.isInstanceType()) {
      _addError("You cannot throw a class or interface name.  Perhaps you mean to instantiate the exception class " + 
                thrown_result.getSymbolData().getName() + " that you are throwing", that);
      thrown_result = thrown_result.getInstanceData();
    }
    
//    System.err.println("getSymbolData(\"java.lang.Throwable\", that, false, true) = " + 
//                       getSymbolData("java.lang.Throwable", that, false, true));
    //make sure what is being thrown extends java.lang.Throwable.
    if (! _isAssignableFrom(getSymbolData("java.lang.Throwable", that, false, true), thrown_result.getSymbolData())) {
      _addError("You are attempting to throw " + thrown_result.getSymbolData().getName() + 
                ", which does not implement the Throwable interface", that);
    }
    return thrown_result;
  }
  
  /*This is not supported at any Language Level.  It should have been caught during the first pass.*/  
  public TypeData forSynchronizedStatement(SynchronizedStatement that) {
    throw new RuntimeException("SynchronizedStatements are not supported.");
  }


  /* Visit the declarator of this formal parameter and return its result.
   * @param that  The Formal Parameter we are visiting.
   */
  public TypeData forFormalParameter(FormalParameter that) {
    final TypeData declarator_result = that.getDeclarator().visit(this);
    return declarator_result;
  }

  /** Visit each of the declarators of this declaration.
    * @param that  The VariableDeclaration we are visiting.
    */
  public TypeData forVariableDeclaration(VariableDeclaration that) {
    final TypeData mavRes = that.getMav().visit(this);
    final TypeData[] declarators_result = makeArrayOfRetType(that.getDeclarators().length);
    for (int i = 0; i < that.getDeclarators().length; i++) {
      declarators_result[i] = that.getDeclarators()[i].visit(this);
    }
    return null;
  }

  /** If this VariableDeclarator is uninitialized, make sure its type can be resolved and visit
    * its name.
    * @param that  The UninitializedVariableDeclarator we are visiting.
    */
  public TypeData forUninitializedVariableDeclarator(UninitializedVariableDeclarator that) {
    final TypeData typeRes = getSymbolData(that.getType().getName(), _data, that.getType());
    final TypeData nameRes = that.getName().visit(this);
    return forUninitializedVariableDeclaratorOnly(that, typeRes, nameRes);
  }
  
  /** If the VariableDeclarator is initialized, things get a little bit more complicated.
    * Resolve the type and visit the name, like we do for the uninitilized case.
    * Then, check to see if the initializer is an array initializer.  If so, delegate.
    * Otherwise, just visit it with an ExpressionTypeChecker.
    * @param that  The InitializedVariableDeclarator we are visiting.
    */
  public TypeData forInitializedVariableDeclarator(InitializedVariableDeclarator that) {
    final SymbolData typeRes = getSymbolData(that.getType().getName(), _data, that.getType());
    final TypeData nameRes = that.getName().visit(this); //we think this is always null

    TypeData initializer_result;
    if (that.getInitializer() instanceof ArrayInitializer) {
      initializer_result = forArrayInitializerHelper((ArrayInitializer) that.getInitializer(), typeRes);
    }
    else {
      ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages,
                                                            _vars, _thrown);
      initializer_result = that.getInitializer().visit(etc);
      thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned); //incorporate this list here
    }
    return forInitializedVariableDeclaratorOnly(that, typeRes, nameRes, initializer_result);
  }

  /** A variable data can be assigned to if it is not final or if it does not have a value.
    * (i.e. final variables that already have a value cannot be assigned to.  Everything else can be).
    */
  protected boolean canBeAssigned(VariableData vd) {
    return !vd.isFinal() || !vd.hasValue();
  }
  
  /** Makes sure that the specified type is an array type, and then
    * examines the elements in the array initializer and makes sure each has a type assignable to
    * the elementType of the specified array type.  Returns an instance data corresponding to the type of the array.
    */
  public TypeData forArrayInitializerHelper(ArrayInitializer that, SymbolData type) {
    if (type == null) {return null;}
    if (!(type instanceof ArrayData)) {_addError("You cannot initialize the non-array type " + type.getName() + 
                                                 " with an array initializer", that); return type.getInstanceData();}
    
    SymbolData elementType = ((ArrayData) type).getElementType();
    VariableInitializerI[] elements = that.getItems();
    TypeData[] result = makeArrayOfRetType(elements.length);
    
    for (int i = 0; i<elements.length; i++) {
      if (elements[i] instanceof ArrayInitializer) {
          result[i] = forArrayInitializerHelper((ArrayInitializer) elements[i], elementType);
      }
      else {
        ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages,
                                                              _vars, _thrown);
        result[i] = elements[i].visit(etc);
        
        // Incorporate the things that were assigned in the expression here
        thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned); 

        if (result[i] != null) {
          if (assertFound(result[i], (JExpression) that.getItems()[i])) {
            if (!result[i].getSymbolData().isAssignableTo(elementType, LanguageLevelConverter.OPT.javaVersion())) {
              _addError("The elements of this initializer should have type " + elementType.getName() + " but element "
                          + i + " has type " + result[i].getSymbolData().getName(), (JExpression) that.getItems()[i]);
            }
            else {
              assertInstanceType(result[i], "The elements of this initializer should all be instances," + 
                                 " but you have specified the type name " + result[i].getName(), 
                                 (JExpression) that.getItems()[i]);
            }
          }
        }
      }
    }
    return type.getInstanceData();
  }
 
  /** Look up the SymbolData for this InnerClass within the enclosing data, check for cyclic inheritance,
    * and then visit everything inside the inner class.
    * @param that  The InnerClassDef we're visiting
    */
  public TypeData forInnerClassDef(InnerClassDef that) {
    String className = that.getName().getText();
    
    // This works because className will never be a qualified name
    SymbolData sd = _data.getInnerClassOrInterface(className);
    
    if (sd == null) throw new RuntimeException("SymbolData is null for class name = " + className);

    // Check for cyclic inheritance
    if (checkForCyclicInheritance(sd, new LinkedList<SymbolData>(), that)) { return null; }
    final TypeData mavRes = that.getMav().visit(this);
    final TypeData nameRes = that.getName().visit(this);
    final TypeData[] typeParamRes = makeArrayOfRetType(that.getTypeParameters().length);
    for (int i = 0; i < that.getTypeParameters().length; i++) {
      typeParamRes[i] = that.getTypeParameters()[i].visit(this);
    }
    final TypeData superClassRes = that.getSuperclass().visit(this);
    final TypeData[] interfacesRes = makeArrayOfRetType(that.getInterfaces().length);
    for (int i = 0; i < that.getInterfaces().length; i++) {
      interfacesRes[i] = that.getInterfaces()[i].visit(this);
    }
    final TypeData bodyRes = 
      that.getBody().visit(new ClassBodyTypeChecker(sd, _file, _package, _importedFiles, _importedPackages, _vars, 
                                                    _thrown));
    return null;
  }
  
  /** Look for the inner interface inside of the enclosing data.
    * Then, visit everything that needs to be visited.
    * @param that  The InnerInterfaceDef that is being visited.
    */
  public TypeData forInnerInterfaceDef(InnerInterfaceDef that) {
    String className = that.getName().getText();
    SymbolData sd = _data.getInnerClassOrInterface(className); // className is a relative name
//    if (sd == null) System.out.println("Tried to look up " + className + " in " + _data.getName() 
//                                           + " but I got back null");

    // Check for cyclic inheritance
    if (checkForCyclicInheritance(sd, new LinkedList<SymbolData>(), that)) {
      return null;
    }
    final TypeData mavRes = that.getMav().visit(this);
    final TypeData nameRes = that.getName().visit(this);
    final TypeData[] typeParamRes = makeArrayOfRetType(that.getTypeParameters().length);
    for (int i = 0; i < that.getTypeParameters().length; i++) {
      typeParamRes[i] = that.getTypeParameters()[i].visit(this);
    }
    final TypeData[] interfacesRes = makeArrayOfRetType(that.getInterfaces().length);
    for (int i = 0; i < that.getInterfaces().length; i++) {
      interfacesRes[i] = that.getInterfaces()[i].visit(this);
    }

    final TypeData bodyRes = 
      that.getBody().visit(new InterfaceBodyTypeChecker(sd, _file, _package, _importedFiles, _importedPackages, _vars, 
                                                        _thrown));
    return null;
  }
    
  
  /** Compare the two lists of variable datas, and if a data is in both lists, mark it as having been assigned.
    * @param l1  One of the lists of variable datas
    * @param l2  The other list of variable datas.
    */
  void reassignVariableDatas(LinkedList<VariableData> l1, LinkedList<VariableData> l2) {
    for (int i = 0; i<l1.size(); i++) { 
      if (l2.contains(l1.get(i))) {
        l1.get(i).gotValue();
        l1.get(i).gotValue();
      }
    }
  }
  
  /** Compare a list of variable datas and a list of list of variable datas.
    * If a variable data is in the list and in each list of the lists of lists, mark it as having been
    * assigned.
    * @param tryBlock  The list of variable datas.
    * @param catchBlocks  The list of list of variable datas.
    */
  void reassignLotsaVariableDatas(LinkedList<VariableData> tryBlock, LinkedList<LinkedList<VariableData>> catchBlocks) {
    for (int i = 0; i<tryBlock.size(); i++) {
      boolean seenIt = true;
      for (int j = 0; j<catchBlocks.size(); i++) {
        if (!catchBlocks.get(j).contains(tryBlock.get(i))) {seenIt = false;}
      }
      if (seenIt) {        //find the variable data in vars and give it a value!
        tryBlock.get(i).gotValue();
      }
    }
  }

  /** If an exception is thrown but not caught, throw the appropriate error, based on the JExpression.*/
  public void handleUncheckedException(SymbolData sd, JExpression j) {
//    System.err.println("***** handleUncheckedException called for " + sd + " and " + j);
    if (j instanceof MethodInvocation) {
      _addError("The method " + ((MethodInvocation)j).getName().getText() + " is declared to throw the exception " + 
                sd.getName() + " which needs to be caught or declared to be thrown", j);
      }
      else if (j instanceof ThrowStatement) {
        _addError("This statement throws the exception " + sd.getName() + 
                  " which needs to be caught or declared to be thrown", j);
      }
      else if (j instanceof ClassInstantiation) {
        _addError("The constructor for the class " + ((ClassInstantiation)j).getType().getName() + 
                  " is declared to throw the exception " + sd.getName() +
                  " which needs to be caught or declared to be thrown.", j);
      }
      else if (j instanceof SuperConstructorInvocation) {
        _addError("The constructor of this class's super class could throw the exception " + sd.getName() + 
                  ", so the enclosing constructor needs to be declared to throw it", j);
      }
      else if (j instanceof ThisConstructorInvocation) {
        _addError("This constructor could throw the exception " + sd.getName() + 
                  ", so this enclosing constructor needs to be declared to throw it", j);
      }
      
      else if (j instanceof BracedBody) { //then this is because of an implicit super constructor reference.
        _addError("There is an implicit call to the superclass's constructor here.  " + 
                  "That constructor could throw the exception " + sd.getName() + 
                  ", so the enclosing constructor needs to be declared to throw it", j);
      }
      
      else {
        throw new RuntimeException("Internal Program Error: Something besides a method invocation or throw statement" + 
                                   " threw an exception.  Please report this bug.");
      }
  }
  
  /** Returns whether the sd is a checked exception, i.e. one that needs to be caught or declared to be thrown.
    * This is defined as all subclasses of java.lang.Throwable except for subclasses of java.lang.RuntimeException
    */
  public boolean isCheckedException(SymbolData sd, JExpression that) {
    return sd.isSubClassOf(getSymbolData("java.lang.Throwable", _data, that, false)) &&
      ! sd.isSubClassOf(getSymbolData("java.lang.RuntimeException", _data, that, false)) &&
      ! sd.isSubClassOf(getSymbolData("java.lang.Error", _data, that, false));
  }
  
  /** Return true if the Exception is a checked exception yet is not caught or declared to be thrown, and false
    * otherwise.  An exception is a checked if it does not extend either java.lang.RuntimeException or java.lang.Error,
    * and is not declared to be thrown by the enclosing method.
    * @param sd  The SymbolData of the Exception we are checking.
    * @param that  The JExpression passed to getSymbolData for error purposes.
    */
  public boolean isUncaughtCheckedException(SymbolData sd, JExpression that) {
    return isCheckedException(sd, that);
  }
  
  /** Visit each of the items in the body and make sure that none throw uncaught exceptions */
  public TypeData forBody(Body that) {
    final TypeData[] items_result = makeArrayOfRetType(that.getStatements().length);
    for (int i = 0; i < that.getStatements().length; i++) {
      items_result[i] = that.getStatements()[i].visit(this);
      //walk over what has been thrown and throw an error if it contains an unchecked exception
      for (int j = 0; j < this._thrown.size(); j++) {
        if (isUncaughtCheckedException(this._thrown.get(j).getFirst(), that)) {
          handleUncheckedException(this._thrown.get(j).getFirst(), this._thrown.get(j).getSecond());
        }
      }
    }

    return forBodyOnly(that, items_result);
  }

  /** Delegate to forBody*/
  public TypeData forBracedBody(BracedBody that) { return forBody(that); }
  
  /** Delegate to forBody*/
  public TypeData forUnbracedBody(UnbracedBody that) { return forBody(that); }
  
  /** Test the methods defined in the enclosing class. */      
  public static class BobTest extends TestCase {
    
    private SpecialTypeChecker _stc;
    
    private SymbolData _sd1;
    private SymbolData _sd2;
    private SymbolData _sd3;
    private SymbolData _sd4;
    private SymbolData _sd5;
    private SymbolData _sd6;
  
    public BobTest() { this(""); }
    public BobTest(String name) { super(name); }
    
    public void setUp() {
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable.clear();
      _stc = new SpecialTypeChecker(null, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), 
                   new LinkedList<VariableData>(), new LinkedList<Pair<SymbolData, JExpression>>());
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make());
        _stc._importedPackages.addFirst("java.lang");
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");
      _stc._data = _sd1;
    }
    
    public void testForInitializedVariableDeclarator() {
      LanguageLevelVisitor llv =
        new LanguageLevelVisitor(_stc._file, 
                                 _stc._package,
                                 null, // enclosingClassName for top level traversal.
                                 _stc._importedFiles, 
                                 _stc._importedPackages, 
                                 new HashSet<String>(), 
                                 new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                 new LinkedList<Command>());
      
//      LanguageLevelConverter.symbolTable.clear();
      
      SourceInfo si = SourceInfo.NONE;
      Expression e1 = new IntegerLiteral(si, 1);
      Expression e2 = new IntegerLiteral(si, 2);
      Expression e3 = new PlusExpression(si, new IntegerLiteral(si, 3), new CharLiteral(si, 'e'));
      Expression e4 = new CharLiteral(si, 'c');

      ArrayType intArrayType = 
        new ArrayType(SourceInfo.NONE, "int[]", new PrimitiveType(SourceInfo.NONE, "int"));

      //make sure it works -- most testing done in testArrayInitializerHelper
      ArrayData intArray = new ArrayData(SymbolData.INT_TYPE, llv, si);
      intArray.setIsContinuation(false);
      symbolTable.remove("int[]");
      symbolTable.put("int[]", intArray);
      
      _stc._data.addVar(new VariableData("foozle", _publicMav, intArray, false, _stc._data));
      InitializedVariableDeclarator ivd = 
        new InitializedVariableDeclarator(SourceInfo.NONE, intArrayType,
                                          new Word(SourceInfo.NONE, "foozle"),
                                          new ArrayInitializer(si, new VariableInitializerI[] {e1, e2, e3, e4}));

      assertEquals("Should return null", null, ivd.visit(_stc));
      assertEquals("There should be no errors", 0, errors.size());
    }
    
    public void testForInitializedVariableDeclaratorOnly() {
      SymbolData sd1 = SymbolData.DOUBLE_TYPE;
      SymbolData sd2 = SymbolData.BOOLEAN_TYPE;
      SymbolData sd3 = SymbolData.INT_TYPE;
      _stc._data.addVar(new VariableData("j", _publicMav, SymbolData.DOUBLE_TYPE, false, _stc._data));
      
      InitializedVariableDeclarator ivd = 
        new InitializedVariableDeclarator(SourceInfo.NONE,
                                          JExprParser.NO_TYPE,
                                          new Word(SourceInfo.NONE, "j"),
                                          new DoubleLiteral(SourceInfo.NONE, 1.0));
      

      assertEquals("Two assignable types should not throw an error; return null.", null, 
                   _stc.forInitializedVariableDeclaratorOnly(ivd, sd1, sd1, sd3.getInstanceData()));
      assertEquals("Should be no errors", 0, errors.size());
      
      assertEquals("Two unassignable types should throw an error; return null.", null, 
                   _stc.forInitializedVariableDeclaratorOnly(ivd, sd1, sd1, sd2.getInstanceData()));
      assertEquals("Should now be one error", 1, errors.size());
      assertEquals("Error message should be correct:", "Type: \"double\" expected, instead found type: \"boolean\"", 
                   errors.getLast().getFirst());

      SymbolData foo = new SymbolData("Foo");
      assertEquals("An initialization from a SymbolData should return null", null, 
                   _stc.forInitializedVariableDeclaratorOnly(ivd, sd1, null, foo));
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct:", 
                   "Field or variable j cannot be initialized with the class or interface name Foo.  " + 
                   "Perhaps you meant to create an instance or use Foo.class", errors.getLast().getFirst());
    }
        
    public void testForThrowStatementOnly() {
      ThrowStatement s = new ThrowStatement(SourceInfo.NONE, new NullLiteral(SourceInfo.NONE));
      SymbolData exception = _stc.getSymbolData("java.lang.Throwable", s, false, true); 
      InstanceData exceptionInstance = exception.getInstanceData();
      
      SymbolData notAnException = new SymbolData("bob");
      InstanceData naeInstance = notAnException.getInstanceData();

      
      assertEquals("When a SymbolData is the thrown type, return its InstanceData", exceptionInstance, 
                   _stc.forThrowStatementOnly(s, exception));
      assertEquals("There should be 1 error", 1, errors.size());
      
      assertEquals("Error message should be correct", 
                   "You cannot throw a class or interface name.  " + 
                   "Perhaps you mean to instantiate the exception class java.lang.Throwable that you are throwing", 
                   errors.get(0).getFirst());

      assertEquals("When a thrown type does not implement Throwable, return the type anyway", naeInstance, 
                   _stc.forThrowStatementOnly(s, naeInstance));
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "You are attempting to throw bob, which does not implement the Throwable interface", 
                   errors.getLast().getFirst());
    }
      
  
    public void testForArrayInitializerHelper() {
      LanguageLevelVisitor llv =
        new LanguageLevelVisitor(_stc._file, _stc._package, 
                                 null /* enclosingClassName */, 
                                 _stc._importedFiles,  
                                 _stc._importedPackages, 
                                 new HashSet<String>(), 
                                 new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                 new LinkedList<Command>());
//      LanguageLevelConverter.symbolTable = llv.symbolTable = _stc.symbolTable;
      
      SourceInfo si = SourceInfo.NONE;
      
      Expression e1 = new IntegerLiteral(si, 1);
      Expression e2 = new IntegerLiteral(si, 2);
      Expression e3 = new PlusExpression(si, new IntegerLiteral(si, 3), new CharLiteral(si, 'e'));
      Expression e4 = new CharLiteral(si, 'c');
      Expression e5 = new DoubleLiteral(si, 5.8);
      Expression e6 = new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "int"));

      ArrayInitializer a1 = new ArrayInitializer(si, new VariableInitializerI[] {e1, e3, e4});
      ArrayInitializer a2 = new ArrayInitializer(si, new VariableInitializerI[] {e2, e3, e1});
      
      Expression nl = new NullLiteral(si);

      //it works for a one dimensional array
      ArrayData intArray = new ArrayData(SymbolData.INT_TYPE, llv, si);
      intArray.setIsContinuation(false);
      symbolTable.remove("int[]");
      symbolTable.put("int[]", intArray);
      
      ArrayInitializer ia = new ArrayInitializer(si, new VariableInitializerI[] {e1, e2, e3, e4});
      assertEquals("Should return instance of int[]", intArray.getInstanceData(), 
                   _stc.forArrayInitializerHelper(ia, intArray));
      assertEquals("There should be no errors", 0, errors.size());
      
      //it works for a 2 dimensional array
      ArrayData intArray2 = new ArrayData(intArray, llv, si);
      intArray2.setIsContinuation(false);
      symbolTable.put("int[][]", intArray2);
      
      ia = new ArrayInitializer(si, new VariableInitializerI[]{a1, a2});
      assertEquals("Should return instance of int[][]", intArray2.getInstanceData(), 
                   _stc.forArrayInitializerHelper(ia, intArray2));
      assertEquals("There should be no errors", 0, errors.size());
      
      //it works for a 2 dimensional array with null as its elements
      ia = new ArrayInitializer(si, new VariableInitializerI[] {nl, nl});
      assertEquals("Should return instance of int[][]", intArray2.getInstanceData(), 
                   _stc.forArrayInitializerHelper(ia, intArray2));
      
      //throw an error if the type passed to the helper is not an array data
      assertEquals("Should return double", SymbolData.DOUBLE_TYPE.getInstanceData(), 
                   _stc.forArrayInitializerHelper(ia, SymbolData.DOUBLE_TYPE));
      assertEquals("There should be one error message", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "You cannot initialize the non-array type double with an array initializer", 
                   errors.getLast().getFirst());

      //throw an error if the type of one of the elements doesn't match
      ia = new ArrayInitializer(si, new VariableInitializerI[] {e1, e2, e5, e4});
      assertEquals("Should return instance of int[]", intArray.getInstanceData(), 
                   _stc.forArrayInitializerHelper(ia, intArray));
      assertEquals("There should be two error messages", 2, errors.size());
      assertEquals("The error message should be correct", 
                   "The elements of this initializer should have type int but element 2 has type double", 
                   errors.getLast().getFirst());

      //throw an error if null in 1 dimensional int array
      ia = new ArrayInitializer(si, new VariableInitializerI[] {nl, nl});
      assertEquals("Should return instance of int[]", intArray.getInstanceData(),
                   _stc.forArrayInitializerHelper(ia, intArray));
      assertEquals("There should be four error messages", 4, errors.size());
      assertEquals("The error message should be correct", 
                   "The elements of this initializer should have type int but element 0 has type null", 
                   errors.get(2).getFirst());
      assertEquals("The error message should be correct", 
                   "The elements of this initializer should have type int but element 1 has type null", 
                   errors.get(3).getFirst());
      
      //should throw error if type name is passed instead of instance
      ia = new ArrayInitializer(si, new VariableInitializerI[] {e1, e2, e3, e4, e6});
      assertEquals("Should return instance of int[]", intArray.getInstanceData(), 
                   _stc.forArrayInitializerHelper(ia, intArray));
      assertEquals("Should now be 5 error messages", 5, errors.size());
      assertEquals("Error message should be correct", 
                   "The elements of this initializer should all be instances, but you have specified the type name" + 
                   " int.  Perhaps you meant to create a new instance of int",
                   errors.getLast().getFirst());
    }

    public void testFindClassReference() {
      SymbolData string = new SymbolData("java.lang.String");
      string.setIsContinuation(false);
      string.setPackage("java.lang");
      symbolTable.remove("java.lang.String");
      symbolTable.put("java.lang.String", string);
      
      //if lhs is null, just look up SymbolData
      assertEquals("Should return string", string,
                   _stc.findClassReference(null, "java.lang.String", new NullLiteral(SourceInfo.NONE)));
      assertEquals("Should not be an error", 0, errors.size());
      
      //if SymbolData cannot be found, do not add error--just return null
      assertEquals("Should return null", null, 
                   _stc.findClassReference(null, "non-existant", new NullLiteral(SourceInfo.NONE)));
      assertEquals("Should be no errors", 0, errors.size());
      
      //if LHS is package data, try to look up fully qualified name
      assertEquals("Should return string", string,
                   _stc.findClassReference(new PackageData("java.lang"), "String", new NullLiteral(SourceInfo.NONE)));
      assertEquals("Should not be an error", 0, errors.size());
      
      //if symbol data cannot be found, do not give error
      assertEquals("Should return null", null, 
                   _stc.findClassReference(new PackageData("nonsense"), "non-existant", 
                                         new NullLiteral(SourceInfo.NONE)));
      assertEquals("Should be no errors", 0, errors.size());
      
      //otherwise, try to look up symbolData from context of lhs
      SymbolData inner = new SymbolData("java.lang.String$Inner");
      inner.setIsContinuation(false);
      inner.setPackage("java.lang");
      inner.setOuterData(string);
      string.addInnerClass(inner);
      assertEquals("Should return inner", inner,
                   _stc.findClassReference(string, "Inner", new NullLiteral(SourceInfo.NONE)));
      assertEquals("Should be no errors", 0, errors.size());
      
      //do not give error if it could not be found
      assertEquals("Should return null", null, 
                   _stc.findClassReference(string, "non-existant", new NullLiteral(SourceInfo.NONE)));
      assertEquals("Should be no errors", 0, errors.size());
    }
  }
}
