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
import java.io.*;

import junit.framework.TestCase;

/** Do the TypeChecking appropriate to the context of a constructor body.  Common to all Language Levels. */
public class ConstructorBodyTypeChecker extends BodyTypeChecker {

  /** Constructor for ConstructorBodyTypeChecker.
    * @param bodyData  The bodyData corresponding to the constructor we are visiting
    * @param file  The File corresponding to the source file we are checking.
    * @param packageName  The package of the source file.
    * @param importedFiles  A list of the names of the classes that are specifically imported in the source file
    * @param importedPackages  A list of the names of the packages that are imported in the source file.
    * @param vars  A list of the variable datas that can be seen and have been given a value before this context
    * @param thrown  The exceptions that are thrown
    */
  public ConstructorBodyTypeChecker(BodyData bodyData, File file, String packageName, LinkedList<String> importedFiles,
                                    LinkedList<String> importedPackages, LinkedList<VariableData> vars, 
                                    LinkedList<Pair<SymbolData, JExpression>> thrown) {
    super(bodyData, file, packageName, importedFiles, importedPackages, vars, thrown);
  }
 
  
  /** Creates a new instance of this class for visiting inner bodies. */
  protected BodyTypeChecker 
    createANewInstanceOfMe(BodyData bodyData, File file, String pakage, LinkedList<String> importedFiles, 
                           LinkedList<String> importedPackages, LinkedList<VariableData> vars, 
                           LinkedList<Pair<SymbolData, JExpression>> thrown) {
    return new ConstructorBodyTypeChecker(bodyData, file, pakage, importedFiles, importedPackages, vars, thrown);
  }
  
  
  /** This is used in the case where a simple this constructor invocation is allowed.  i.e. when it is the first 
    * statement of a constructor body.
   */
  public TypeData simpleThisConstructorInvocationAllowed(SimpleThisConstructorInvocation that) {
    //Verify that a constructor of this form is in this--if not, an error will be thrown by this method invocation.
    //we continue with this method regardless.
    String name = LanguageLevelVisitor.getUnqualifiedClassName(_data.getSymbolData().getName());
    InstanceData[] args = getArgTypesForInvocation(that.getArguments());
    if (args == null) {return null;}
    MethodData cd = _lookupMethod(name, _data.getSymbolData(), args, that, 
                           "No constructor found in class " + _data.getSymbolData().getName() + " with signature: ", 
                           true, _data.getSymbolData());
    
    if (cd==null) {return null;}
    
    //set all final fields to have a value--they should have gotten caught.
    LinkedList<VariableData> myFields = _data.getSymbolData().getVars();
    for (int i = 0; i<myFields.size(); i++) {
      if (myFields.get(i).hasModifier("final")) {
        _vars.get(_vars.indexOf(myFields.get(i))).gotValue();
        thingsThatHaveBeenAssigned.addLast(_vars.get(_vars.indexOf(myFields.get(i))));
      }
    }
    
    // if constructor is declared to throw exceptions, add them to thrown list:
    String[] thrown = cd.getThrown();
    for (int i = 0; i < thrown.length; i++) {
      _thrown.addLast(new Pair<SymbolData, JExpression>(getSymbolData(thrown[i], _getData(), that), that));
    }  
    return null;
  }
  
  /** ComplexThisConstructorInvocations are not ever allowed--throw an appropriate error. */
  public TypeData complexThisConstructorInvocationNotAllowed(ComplexThisConstructorInvocation that) {
    _addError("Constructor Invocations of this form are never allowed.  A constructor invocation can appear here, "
                + "but it must either be a super constructor invocation or have the form this(...)", that);
    return null;
  }
  
  /** This is used in the case where a simple super constructor invocation is allowed i.e. when it is the first statement
    * of a constructor body.
    */
  public TypeData simpleSuperConstructorInvocationAllowed(SimpleSuperConstructorInvocation that) {
    
    SymbolData superClass = _data.getSymbolData().getSuperClass();
    
    if (superClass == null) {  //This should never happen.
      _addError("The class " + _data.getSymbolData().getName() + " does not have a super class", that);
      return null;
    }

    //If the super class is an inner class, a constructor of this form can only be used if it is static.
    if (superClass.getOuterData() != null && !(superClass.hasModifier("static"))) {
      _addError(superClass.getName() + " is a non-static inner class of " + superClass.getOuterData().getName() + ".  Its constructor must be invoked from an instance of its outer class", that);
      return null;
    }

    
    //Look in this's super class and try to match the invocation.  If no match is found, the method invocation will add an error.
    String name = LanguageLevelVisitor.getUnqualifiedClassName(superClass.getName());
    InstanceData[] args = getArgTypesForInvocation(that.getArguments());
    if (args == null) return null;
    MethodData cd = _lookupMethod(name, superClass, args, that, 
                           "No constructor found in class " + superClass.getName() + " with signature: ", 
                           true, superClass);

    if (cd == null) return null;
    
    // if constructor is declared to throw exceptions, add them to thrown list:
    String[] thrown = cd.getThrown();
    for (int i = 0; i < thrown.length; i++) {
      _thrown.addLast(new Pair<SymbolData, JExpression>(getSymbolData(thrown[i], _getData(), that), that));
    }
    return null;
  }
  
  /** This is used in the case where a complex super constructor invocation is allowed i.e. when it is the first 
    * statement of a constructor body.
    */
  public TypeData complexSuperConstructorInvocationAllowed(ComplexSuperConstructorInvocation that) {
    //resolve the first part of the super invocation using an ETC and super class and try to match the invocation.
    ExpressionTypeChecker etc = new ExpressionTypeChecker(_data, _file, _package, _importedFiles, _importedPackages, 
                                                          _vars, _thrown);
    TypeData enclosingResult = that.getEnclosing().visit(etc);
    thingsThatHaveBeenAssigned.addAll(etc.thingsThatHaveBeenAssigned);

    if (! assertFound(enclosingResult, that)) return null;
    SymbolData superClass = _data.getSymbolData().getSuperClass();
    
    //enclosingResult must be the outerclass of the super class of this class.
    if (superClass == null) { 
      _addError("A qualified super constructor invocation can only be used to invoke the constructor of your super "
                  + "class from the context of its outer class.  The class " + _data.getSymbolData().getName() + 
                " does not have a super class, so you cannot do this here", 
                that);
      return null;
    }
    else if (superClass.getOuterData() == null) { 
      _addError("A qualified super constructor invocation can only be used to invoke the constructor of your super "
                  + "class from the context of its outer class.  The super class " + superClass.getName() 
                  + " does not have an outer class, so you cannot do this here", 
                that);
      return null;
    }
    else if (enclosingResult == null) {
      _addError("A qualified super constructor invocation can only be used to invoke the constructor of your super "
                  + "class from the context of its outer class.", 
                that);
      return null;
    }
    else if (superClass.getOuterData() != enclosingResult.getSymbolData()) {
      _addError("A qualified super constructor invocation can only be used to invoke the constructor of your super "
                  + "class from the context of its outer class.  The class or interface " 
                  + enclosingResult.getSymbolData().getName() + " is not the outer class of the super class " 
                  + superClass.getName(), 
                that);
      return null;
    }
    else if (!enclosingResult.isInstanceType()) {
      _addError("A qualified super constructor invocation can only be made from the context of an instance of the "
                  + "outer class of the super class.  You have specified a type name", 
                that);
      return null;
    }
    else if (superClass.hasModifier("static")) {
      _addError("A qualified super constructor invocation can only be used to invoke the constructor of a non-static "
                  + "super class from the context of its outer class.  The super class " + superClass.getName() 
                  + " is a static inner class",
                that);
      return null;
    }
    String name = LanguageLevelVisitor.getUnqualifiedClassName(superClass.getName());
    InstanceData[] args = getArgTypesForInvocation(that.getArguments());
    if (args == null) {return null;}
    MethodData cd = _lookupMethod(name, superClass, args, that, 
                                  "No constructor found in class " + superClass.getName() + " with signature: ", 
                                  true, superClass);
    if (cd == null) {return null;}
    //if constructor is declared to throw exceptions, add them to thrown list:
    String[] thrown = cd.getThrown();
    for (int i = 0; i<thrown.length; i++) {
      _thrown.addLast(new Pair<SymbolData, JExpression>(getSymbolData(thrown[i], _getData(), that), that));
    }
    return null;
  }
  
  /** Called when the first line of a constructor is not an explicit constructor invocation.
    * In this case, if the class has a super class (which it should), we assume there is an implicit call
    * to the super constructor, passing no arguments.
    */
  private void implicitSuperConstructor(BracedBody that) {
//    System.err.println("implicitSuperConstructor called for " + _data.getSymbolData());
    SymbolData superClass = _data.getSymbolData().getSuperClass();
    
    if (superClass == null) {  //This should never happen, but if it does, no reason to throw an error.
      return;
    }

    //If the super class is an inner class, there cannot be an implicit constructor call
    if (superClass.getOuterData() != null && !(superClass.hasModifier("static"))) {
      _addError("There is an implicit call to the constructor of " + superClass.getName() + " here, but " + 
                superClass.getName() + " is a non-static inner class of " + superClass.getOuterData().getName() + 
                ".  Thus, you must explicitly invoke its constructor from an instance of its outer class", 
                that);
      return;
    }

    
    // Look in this's super class and try to match the invocation.  If no match is found, the method invocation will 
    // add an error.
    String name = LanguageLevelVisitor.getUnqualifiedClassName(superClass.getName());
    
    // There must be a default constructor with no arguments, or an error is thrown.
    // (Note--if there were no constructors in the super class at all, the default no arguments constructor would exist.  
    // However, a constructor is always generated for a LanguageLevel file, and any class file that relied on the 
    // default constructor would already have it.  Therefore, we can assume that all classes have at least one constructor).
    
    MethodData cd = 
      _lookupMethod(name, 
                    superClass, 
                    new InstanceData[0], 
                    that, 
                    "You must invoke one of " + superClass.getName() + 
                    "'s constructors here.  You can either explicitly invoke one of its exisitng constructors or "
                      + "add a constructor with signature: ", 
                    true, superClass);

    if (cd == null) return;
    // if constructor is declared to throw exceptions, add them to thrown list:
    // add BracedBody as the JExpression corresponding to the error
    String[] thrown = cd.getThrown();
    for (int i = 0; i<thrown.length; i++) {
      _thrown.addLast(new Pair<SymbolData, JExpression>(getSymbolData(thrown[i], _getData(), that), that));
    }
    return;
  }
    
  /** Void return statements are allowed in constructor bodies, since according to java, constructors are
    * void return methods.  However, we treat them as if they returned an instance of the class they
    * are a constructor for, so we will return that symbol data here.
    */
  public TypeData forVoidReturnStatementOnly(VoidReturnStatement that) {
    // Just return the type the constructor would return.
    return _bodyData.getSymbolData().getInstanceData();
  }

  /** Throw an error and return null, becuase constructors cannot have value return statements in their bodies. */
  public TypeData forValueReturnStatementOnly(ValueReturnStatement that, TypeData valueRes) {
      _addError("You cannot return a value from a class's constructor", that);
      return _bodyData.getSymbolData().getInstanceData();
  }
  
  /** Walk over the statements in the BracedBody, treating the first line specially.  A super constructor
    * invocation can only appear on the first line of a constructor, so see if there is one on the
    * first line, and if so, call the appropriate method.  If there is no super constructor invocation, 
    * then assume there is an implicit one.  Then visit the rest of the statements in the body like normal.
    * Make sure errors are thrown for any uncaught exceptions.
    */
  public TypeData forBracedBody(BracedBody that) {
//    System.err.println("forBracedBody called in " + _data.getSymbolData() + " for " + that);
    int startIndex = 0;
    final TypeData[] items_result = makeArrayOfRetType(that.getStatements().length);
    if (items_result.length > 0) {
      // The first line of a constructor is treated specially:
      if (that.getStatements()[0] instanceof ExpressionStatement) {
        Expression firstExpression = ((ExpressionStatement) that.getStatements()[0]).getExpression();
        if (firstExpression instanceof SimpleThisConstructorInvocation) {
          items_result[0] = simpleThisConstructorInvocationAllowed((SimpleThisConstructorInvocation) firstExpression);
          startIndex ++;
        }
        
        else if (firstExpression instanceof ComplexThisConstructorInvocation) {
          items_result[0] = complexThisConstructorInvocationNotAllowed((ComplexThisConstructorInvocation) firstExpression);
          startIndex++;
        }    
        else if (firstExpression instanceof SimpleSuperConstructorInvocation) {
          items_result[0] = simpleSuperConstructorInvocationAllowed((SimpleSuperConstructorInvocation) firstExpression);
          startIndex++;
        }
        else if (firstExpression instanceof ComplexSuperConstructorInvocation) {
          items_result[0] = complexSuperConstructorInvocationAllowed((ComplexSuperConstructorInvocation) firstExpression);
          startIndex++;
        }      
      }
    }
    if (startIndex == 0) implicitSuperConstructor(that);
    
    int thrownSize = _thrown.size();
//    System.err.println("_thrown.size() in " + _data + " = " + thrownSize);
    for (int j = 0; j < thrownSize; j++) {
      if (isUncaughtCheckedException(_thrown.get(j).getFirst(), that)) {
        handleUncheckedException(_thrown.get(j).getFirst(), _thrown.get(j).getSecond());
      }
    }
    // TODO: ???? Provision for field initialization?
    /** The following is supposed to be equivalent to calling SpecialTypeChecker.forBody(that, items_result) */
    for (int i = startIndex; i < that.getStatements().length; i++) {
        items_result[i] = that.getStatements()[i].visit(this);
      // Walk over what has been thrown and throw an error if it contains an unchecked exception
      for (int j = thrownSize; j < _thrown.size(); j++) {
        if (isUncaughtCheckedException(_thrown.get(j).getFirst(), that)) {
          handleUncheckedException(_thrown.get(j).getFirst(), _thrown.get(j).getSecond());
        }
      }
    }

    return forBracedBodyOnly(that, items_result);
  }
  
  /** Tests the methods in the above class. */
  public static class ConstructorBodyTypeCheckerTest extends TestCase {
    
    private ConstructorBodyTypeChecker _cbtc;
    
    private MethodData _bd1;
//    private MethodData _bd2;
    
    private SymbolData _sd1;
    private SymbolData _sd2;
    private SymbolData _sd3;
    private SymbolData _sd4;
    private SymbolData _sd5;
    private SymbolData _sd6;
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[0]);
    private ModifiersAndVisibility _abstractMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final"});
    
    public ConstructorBodyTypeCheckerTest() { this(""); }
    public ConstructorBodyTypeCheckerTest(String name) { super(name); }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("elephant");
      _sd6 = new SymbolData("cebu");

      VariableData[] vds = new VariableData[] { new VariableData("i", _publicMav, SymbolData.INT_TYPE, true, null), 
                                                new VariableData(SymbolData.BOOLEAN_TYPE) };
      _bd1 = new MethodData("monkey", 
                            _packageMav, 
                            new TypeParameter[0], 
                            _sd1, 
                            vds,
                            new String[0],
                            _sd1,
                            null); // no SourceInfo
      
      _bd1.getParams()[0].setEnclosingData(_bd1);                      
      _bd1.getParams()[1].setEnclosingData(_bd1);                      

      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable.clear();
      LanguageLevelConverter._newSDs.clear();
      _bd1.addEnclosingData(_sd1);
      _bd1.addVars(_bd1.getParams());
      _cbtc = new ConstructorBodyTypeChecker(_bd1, 
                                             new File(""), 
                                             "", 
                                             new LinkedList<String>(), 
                                             new LinkedList<String>(), 
                                             new LinkedList<VariableData>(), 
                                             new LinkedList<Pair<SymbolData, JExpression>>());
      _cbtc._importedPackages.addFirst("java.lang");
    }
    
    
    public void testForVoidReturnStatementOnly() {
      _cbtc._bodyData = _bd1; // this body data returns _sd1 (yeah, it's a constructor)

      //test one that works
      BracedBody bb1 = new BracedBody(SourceInfo.NONE, 
                                      new BodyItemI[] { new VoidReturnStatement(SourceInfo.NONE)});

      TypeData sd = bb1.visit(_cbtc);

      assertEquals("There should be no errors.", 0, errors.size());
      assertEquals("Should return i.like.monkey type.", _sd1.getInstanceData(), sd);

    }
   
    public void testforValueReturnStatementOnly() {
      //value return statement should always throw an error.
      BodyItemI[] bis = 
        new BodyItemI[] { new ValueReturnStatement(SourceInfo.NONE,
                                                   new BooleanLiteral(SourceInfo.NONE, true))};
      BracedBody bb1 = new BracedBody(SourceInfo.NONE, bis);
      TypeData sd = bb1.visit(_cbtc);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("Should return i.like.monkey type", _sd1.getInstanceData(), sd);
      assertEquals("Error message should be correct", "You cannot return a value from a class's constructor", 
                   errors.get(0).getFirst());

    }
    
    public void testCreateANewInstanceOfMe() {
      //make sure that the correct visitor is returned from createANewInstanceOfMe
      BodyTypeChecker btc = 
        _cbtc.createANewInstanceOfMe(_cbtc._bodyData, _cbtc._file, _cbtc._package, _cbtc._importedFiles, 
                                     _cbtc._importedPackages, _cbtc._vars, _cbtc._thrown);
      assertTrue("Should be an instance of ConstructorBodyTypeChecker", btc instanceof ConstructorBodyTypeChecker);
    }
    
    public void testForBracedBody() {
      LanguageLevelVisitor llv = 
        new LanguageLevelVisitor(new File(""), 
                                 "", 
                                 null, // enclosingClassName for top level traversal
                                 new LinkedList<String>(), 
                                 new LinkedList<String>(), 
                                 new HashSet<String>(), 
                                 new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                 new LinkedList<Command>());
      llv.errors = new LinkedList<Pair<String, JExpressionIF>>();
      llv._errorAdded=false;
//      LanguageLevelConverter.symbolTable = llv.symbolTable = new Symboltable();
      llv.continuations = new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>();
      llv.visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
//      llv._hierarchy = new Hashtable<String, TypeDefBase>();
      llv._classesInThisFile = new HashSet<String>();
      
      // TODO: The next line should be unnecessary because the subsequent two lines should force its loading
      SymbolData throwable = llv.getSymbolData("java.lang.Exception", SourceInfo.NONE, true);  
      SymbolData eb = llv.getSymbolData("java.util.prefs.BackingStoreException", SourceInfo.NONE, true);
      SymbolData re = llv.getSymbolData("java.lang.RuntimeException", SourceInfo.NONE, true);
//      LanguageLevelConverter.symbolTable = symbolTable = llv.symbolTable;
      
      assert symbolTable.contains(eb);
      assert symbolTable.contains(re);
      assert symbolTable.containsKey("java.lang.Throwable");
      assert symbolTable.containsKey("java.lang.Exception");

      _sd3.setIsContinuation(false);
      _sd3.setMav(_publicMav);
      _sd1.setSuperClass(_sd3);
      // Make sure that it is not okay to invoke a super constructor that throws an exception if the enclosing method 
      // is not declared to throw it
      _cbtc._bodyData.getMethodData().setThrown(new String[0]);
      _sd3.setMav(_publicMav);
      _sd3.setIsContinuation(false);
      _cbtc.symbolTable.put(_sd3.getName(), _sd3);
      MethodData constructor = new MethodData("zebra", 
                                              _publicMav, 
                                              new TypeParameter[0], 
                                              _sd3, 
                                              new VariableData[0], 
                                              new String[] {"java.util.prefs.BackingStoreException"}, 
                                              _sd3, 
                                              null);
      _sd3.addMethod(constructor);
      
      SimpleSuperConstructorInvocation ssci = 
        new SimpleSuperConstructorInvocation(SourceInfo.NONE, 
                                             new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      BracedBody supConstr = 
        new BracedBody(SourceInfo.NONE, new BodyItemI[]{new ExpressionStatement(SourceInfo.NONE, ssci)});
      _cbtc._thrown = new LinkedList<Pair<SymbolData, JExpression>>();
      supConstr.visit(_cbtc);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "The constructor of this class's super class could throw the exception " +
                   "java.util.prefs.BackingStoreException, so the enclosing constructor needs to be declared to throw it", 
                   errors.getLast().getFirst());

      
      //if enclosing method is delared to throw it, should be okay:
      _cbtc._thrown = new LinkedList<Pair<SymbolData, JExpression>>();
      _cbtc._bodyData.getMethodData().setThrown(new String[] {"java.util.prefs.BackingStoreException"});
      supConstr.visit(_cbtc);
      assertEquals("There should still be one error", 1, errors.size());
      
//      //if implicit reference, still give error.
      _cbtc._thrown = new LinkedList<Pair<SymbolData, JExpression>>();
      _cbtc._bodyData.getMethodData().setThrown(new String[0]);
      BracedBody emptyBody = new BracedBody(SourceInfo.NONE, new BodyItemI[0]);
      emptyBody.visit(_cbtc);
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct", "There is an implicit call to the superclass's constructor here.  That constructor could throw the exception java.util.prefs.BackingStoreException, so the enclosing constructor needs to be declared to throw it", errors.getLast().getFirst());

      //if enclosing method is delared to throw it, should be okay:
      _cbtc._bodyData.getMethodData().setThrown(new String[] {"java.util.prefs.BackingStoreException"});
      emptyBody.visit(_cbtc);
      assertEquals("There should still be two errors", 2, errors.size());
      
      //make sure that it is not okay to invoke a this constructor that throws an exception if the enclosing constructor is not declared to throw it
      _cbtc._thrown = new LinkedList<Pair<SymbolData, JExpression>>();
      BracedBody thisConstr = new BracedBody(SourceInfo.NONE, new BodyItemI[]{new ExpressionStatement(SourceInfo.NONE, new SimpleThisConstructorInvocation(SourceInfo.NONE, new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0])))});

      MethodData thisConstructor = new MethodData("cebu", _publicMav, new TypeParameter[0], _sd6, new VariableData[0], new String[] {"java.util.prefs.BackingStoreException"}, _sd6, null);
      MethodData thisConstructorNoThrown = new MethodData("cebu", _publicMav, new TypeParameter[0], _sd6, new VariableData[0], new String[0], _sd6, null);
      _sd6.addMethod(thisConstructor);
      BodyData oldData = _cbtc._bodyData;
      _cbtc._data = thisConstructorNoThrown;
      _cbtc._bodyData = thisConstructorNoThrown;

      thisConstr.visit(_cbtc);
      assertEquals("There should be 3 errors", 3, errors.size());
      assertEquals("The error message should be correct", "This constructor could throw the exception java.util.prefs.BackingStoreException, so this enclosing constructor needs to be declared to throw it", errors.getLast().getFirst());

      //if enclosing method is delared to throw it, should be okay:
      _cbtc._thrown = new LinkedList<Pair<SymbolData, JExpression>>();
      _cbtc._bodyData.getMethodData().setThrown(new String[] {"java.util.prefs.BackingStoreException"});
      thisConstr.visit(_cbtc);
      assertEquals("There should still be 3 errors", 3, errors.size());
      
      
      //make sure that it is not okay to invoke a complex super constructor that throws an exception if the enlcosing constructor is not declared to throw it.
      _cbtc._thrown = new LinkedList<Pair<SymbolData, JExpression>>();
      
      _sd5.setIsContinuation(false);
      _sd5.addInnerClass(_sd3);
      _sd3.setOuterData(_sd5);
      
      _sd5.setMav(_publicMav);
      _sd5.setIsContinuation(false);
      symbolTable.put("elephant", _sd5);
      
      BracedBody complexSC = new BracedBody(SourceInfo.NONE, new BodyItemI[] {new ExpressionStatement(SourceInfo.NONE, new ComplexSuperConstructorInvocation(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "e")), new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0])))});
      oldData.getMethodData().setThrown(new String[0]);
      _cbtc._vars.add(new VariableData("e", _publicMav, _sd5, true, _sd3));
      _cbtc._data = oldData;
      _cbtc._bodyData = oldData;
      
      complexSC.visit(_cbtc);
      assertEquals("There should be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", "The constructor of this class's super class could throw the exception java.util.prefs.BackingStoreException, so the enclosing constructor needs to be declared to throw it", errors.getLast().getFirst());

      //if enclosing method is delared to throw it, should be okay:
      _cbtc._thrown = new LinkedList<Pair<SymbolData, JExpression>>();

      _cbtc._bodyData.getMethodData().setThrown(new String[] {"java.util.prefs.BackingStoreException"});
      complexSC.visit(_cbtc);
      assertEquals("There should still be 4 errors", 4, errors.size());

    }
    
    public void testSimpleThisConstructorInvocationAllowed() {
      // if there is a constructor of the right form, all variable datas will be given a value
      MethodData constructor = 
        new MethodData("zebra", _publicMav, new TypeParameter[0], _sd3, 
                       new VariableData[] {new VariableData(SymbolData.INT_TYPE)}, new String[0], _sd3, null);
      _sd3.addMethod(constructor);
      _cbtc._bodyData = constructor;
      _cbtc._data = constructor;
      
      VariableData vd1 = new VariableData("i", _finalMav, SymbolData.INT_TYPE, false, _sd3);
      VariableData vd2 = new VariableData("d", _finalMav, SymbolData.DOUBLE_TYPE, false, _sd3);
      VariableData vd3 = new VariableData("notFinal", _publicMav, SymbolData.BOOLEAN_TYPE, false, _sd3);
      _cbtc._vars.add(vd1);
      _cbtc._vars.add(vd2);
      _cbtc._vars.add(vd3);
      _sd3.addVar(vd1);
      _sd3.addVar(vd2);
      _sd3.addVar(vd3);
      
      SimpleThisConstructorInvocation constr = 
        new SimpleThisConstructorInvocation(SourceInfo.NONE, 
                                            new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[] {
        new IntegerLiteral(SourceInfo.NONE, 5)}));
      _cbtc.simpleThisConstructorInvocationAllowed(constr);
      assertEquals("Should be no errors", 0, errors.size());
      assertEquals("vd1 should have value", true, vd1.hasValue());
      assertEquals("vd2 should have value", true, vd2.hasValue());
      assertEquals("vd3 is not final, and thus should not have a value", false, vd3.hasValue());
      assertEquals("thrown should have 0 elements", 0, _cbtc._thrown.size());
      
      //if there is not a constructor of the right form, an error will be given
      vd1.lostValue();
      vd2.lostValue();
      
      SimpleThisConstructorInvocation constr2 = new SimpleThisConstructorInvocation(SourceInfo.NONE, new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      _cbtc.simpleThisConstructorInvocationAllowed(constr2);
      assertEquals("Should be one error", 1, errors.size());
      assertEquals("Error message should be correct", "No constructor found in class zebra with signature: zebra().", errors.getLast().getFirst());
      assertFalse("vd1 should not have value", vd1.hasValue());
      assertFalse("vd2 should not have value", vd2.hasValue());
      assertFalse("vd3 should not have a value", vd3.hasValue());
      assertEquals("thrown should have 0 elements", 0, _cbtc._thrown.size());

      
      //if there is a constructor of the right form, but it throws an exception, the exception will be added to the _thrown list.
      constructor.setThrown(new String[] {"java.util.prefs.BackingStoreException"});
      _cbtc.simpleThisConstructorInvocationAllowed(constr);
      assertEquals("Should still be 1 error", 1, errors.size());
      assertTrue("vd1 should have a value", vd1.hasValue());
      assertTrue("vd2 should have a value", vd2.hasValue());
      assertFalse("vd3 is not final, and thus should not have a value", vd3.hasValue());
      assertEquals("thrown should have 1 element", 1, _cbtc._thrown.size());
      
    }

    
    public void testComplexThisConstructorInvocationNotAllowed() {
      ComplexThisConstructorInvocation constr = new ComplexThisConstructorInvocation(SourceInfo.NONE, new NullLiteral(SourceInfo.NONE), new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      _cbtc.complexThisConstructorInvocationNotAllowed(constr);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "Constructor Invocations of this form are never allowed.  A constructor invocation can appear here, but it must either be a super constructor invocation or have the form this(...)", errors.getLast().getFirst());
    }
    

    public void testSimpleSuperConstructorInvocationAllowed() {
      //if current class does not have a super class, give an error
      SimpleSuperConstructorInvocation constr = new SimpleSuperConstructorInvocation(SourceInfo.NONE, new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[] {new IntegerLiteral(SourceInfo.NONE, 5)}));
      _cbtc.simpleSuperConstructorInvocationAllowed(constr);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "The class i.like.monkey does not have a super class", errors.getLast().getFirst());
      
      //if current class has a super class, but there isn't a constructor of the right form, give an error
      _sd1.setSuperClass(_sd3);
      _cbtc.simpleSuperConstructorInvocationAllowed(constr);
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "No constructor found in class zebra with signature: zebra(int).", errors.getLast().getFirst());
      
      //if everything is right, should work with no errors
      MethodData constructor = new MethodData("zebra", _publicMav, new TypeParameter[0], _sd3, new VariableData[] {new VariableData(SymbolData.INT_TYPE)}, new String[0], _sd3, null);
      _sd3.addMethod(constructor);
      _cbtc.simpleSuperConstructorInvocationAllowed(constr);
      assertEquals("Should still be 2 errors", 2, errors.size());
      
      //if there is a constructor of the right form, but it throws an exception, the exception will be added to the _thrown list.
      constructor.setThrown(new String[] {"java.util.prefs.BackingStoreException"});
      _cbtc.simpleSuperConstructorInvocationAllowed(constr);
      assertEquals("Should still be 2 errors", 2, errors.size());
      assertEquals("thrown should have 1 element", 1, _cbtc._thrown.size());
      
      //if super class is an inner class, it must be static--if not, throw an error
      constructor.setThrown(new String[0]);
      _sd3.setOuterData(_sd5);
      _cbtc.simpleSuperConstructorInvocationAllowed(constr);
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", "zebra is a non-static inner class of elephant.  Its constructor must be invoked from an instance of its outer class", errors.getLast().getFirst());
      

      //if super class is a static inner class, no error
      _sd3.addModifier("static");
      _cbtc.simpleSuperConstructorInvocationAllowed(constr);
      assertEquals("Should still be 3 errors", 3, errors.size());
 
    }
    
    public void testComplexSuperConstructorInvocationAllowed() {

      //if enclosingResult is a PackageData, return null and add an error
      ComplexSuperConstructorInvocation constr1 = new ComplexSuperConstructorInvocation(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "nonExistant")), new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      _cbtc.complexSuperConstructorInvocationAllowed(constr1);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "Could not resolve symbol nonExistant", errors.getLast().getFirst());
      
      //if the superclass is null, add error
      ComplexSuperConstructorInvocation constr2 = new ComplexSuperConstructorInvocation(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "zebra")), new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      symbolTable.put("zebra", _sd3);
      _sd3.setIsContinuation(false);
      _sd3.setMav(_publicMav);
      _cbtc.complexSuperConstructorInvocationAllowed(constr2);
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "A qualified super constructor invocation can only be used to invoke the constructor of your super class from the context of its outer class.  The class i.like.monkey does not have a super class, so you cannot do this here", errors.getLast().getFirst());

      //if the superclass exists, but the outer data is null, throw error
      _sd1.setSuperClass(_sd5);
      _cbtc.complexSuperConstructorInvocationAllowed(constr2);
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", "A qualified super constructor invocation can only be used to invoke the constructor of your super class from the context of its outer class.  The super class elephant does not have an outer class, so you cannot do this here", errors.getLast().getFirst());
      
      //if the outer data of the super class does not match the name specified in the constructor, give error
      _sd5.setOuterData(_sd3);
      ComplexSuperConstructorInvocation constr3 = new ComplexSuperConstructorInvocation(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "u.like.emu")), new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      symbolTable.put("u.like.emu", _sd4);
      _sd4.setPackage("u.like");
      _sd4.setIsContinuation(false);
      _sd4.setMav(_publicMav);
      _cbtc.complexSuperConstructorInvocationAllowed(constr3);
      assertEquals("Should be 4 errors", 4, errors.size());
      assertEquals("Error message should be correct", "A qualified super constructor invocation can only be used to invoke the constructor of your super class from the context of its outer class.  The class or interface u.like.emu is not the outer class of the super class elephant", errors.getLast().getFirst());
      
      //if the enclosing is not an instance type, give an error
      _cbtc.complexSuperConstructorInvocationAllowed(constr2);
      assertEquals("Should be 5 errors", 5, errors.size());
      assertEquals("Error message should be correct", "A qualified super constructor invocation can only be made from the context of an instance of the outer class of the super class.  You have specified a type name", errors.getLast().getFirst());

      //if it is an instance type but can't find constructor, give error
      ComplexSuperConstructorInvocation constr4 = new ComplexSuperConstructorInvocation(SourceInfo.NONE, new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "var")), new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]));
      _cbtc._vars.add(new VariableData("var", _publicMav, _sd3, true, _sd1));
      _cbtc.complexSuperConstructorInvocationAllowed(constr4);
      assertEquals("Should be 6 errors", 6, errors.size());
      assertEquals("Error message should be correct", "No constructor found in class elephant with signature: elephant().", errors.getLast().getFirst());
      
      //if it is an instance type and can match constructor, no problems--should add anything thrown to throws list
      MethodData constructor = new MethodData("elephant", _publicMav, new TypeParameter[0], _sd5, new VariableData[0], new String[] {"java.util.prefs.BackingStoreException"}, _sd5, null);
      _sd5.addMethod(constructor);
      _cbtc.complexSuperConstructorInvocationAllowed(constr4);
      assertEquals("Should still be 6 errors", 6, errors.size());
      assertEquals("_thrown should now have 1 element", 1, _cbtc._thrown.size());
      
      //if it is an instance type, but the super class is static, give an error
      _sd5.addModifier("static");
      _cbtc.complexSuperConstructorInvocationAllowed(constr4);
      assertEquals("Should be 7 errors", 7, errors.size());
      assertEquals("Error message should be correct", "A qualified super constructor invocation can only be used to invoke the constructor of a non-static super class from the context of its outer class.  The super class elephant is a static inner class", errors.getLast().getFirst());
    }
    
    
    public void testImplicitSuperConstructor() {
      BracedBody constr = new BracedBody(SourceInfo.NONE, new BodyItemI[0]);
      //if current class has a super class, but there isn't a constructor of the right form, give an error
      _sd1.setSuperClass(_sd3);
      _cbtc.implicitSuperConstructor(constr);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "You must invoke one of zebra's constructors here.  You can either explicitly invoke one of its exisitng constructors or add a constructor with signature: zebra().", 
                   errors.getLast().getFirst());
      
      //if everything is right, should work with no errors
      MethodData constructor = new MethodData("zebra", _publicMav, new TypeParameter[0], _sd3, new VariableData[0], new String[0], _sd3, null);
      _sd3.addMethod(constructor);
      _cbtc.implicitSuperConstructor(constr);
      assertEquals("Should still be 1 error", 1, errors.size());
      
      //if there is a constructor of the right form, but it throws an exception, the exception will be added to the _thrown list.
      constructor.setThrown(new String[] {"java.util.prefs.BackingStoreException"});
      _cbtc.implicitSuperConstructor(constr);
      assertEquals("Should still be 1 error", 1, errors.size());
      assertEquals("thrown should have 1 element", 1, _cbtc._thrown.size());
      
      //if super class is an inner class, it must be static--if not, throw an error
      constructor.setThrown(new String[0]);
      _sd3.setOuterData(_sd5);
      _cbtc.implicitSuperConstructor(constr);
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "There is an implicit call to the constructor of zebra here, but zebra is a non-static inner class of elephant.  Thus, you must explicitly invoke its constructor from an instance of its outer class", errors.getLast().getFirst());
      

      //if super class is a static inner class, no error
      _sd3.addModifier("static");
      _cbtc.implicitSuperConstructor(constr);
      assertEquals("Should still be 2 errors", 2, errors.size());
    }
  }
}


  


