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
import java.io.*;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.IterUtil;

import junit.framework.TestCase;


/*
 * Top-level Language Level Visitor that represents the Advanced Language Level.  Enforces constraints during the
 * first walk of the AST (checking for langauge specific errors and building the symbol table).
 * This class enforces things that are common to all contexts reachable at the Advanced Language Level (i.e., inside class bodies,
 * method bodies, interface bodies, top level, inner inner classes, etc), but also enforces specific top level constraints (i.e. you cannot have try 
 * catch statements at the top level, etc.)
 */

public class AdvancedVisitor extends LanguageLevelVisitor {
  
  /**
   * This constructor is called from the subclasses of Advanced Visitor when creating a new instance of AdvancedVisitor.  
   * The default value for className is the empty string.
   */
  public AdvancedVisitor(File file, String packageName, LinkedList<String> importedFiles, 
                         LinkedList<String> importedPackages,LinkedList<String> classNamesInThisFile, Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations) {
    super(file, packageName, importedFiles, importedPackages, classNamesInThisFile, continuations);
  }
  

  /**
   * This constructor is called from LanguageLevelVisitor and LanguageLevelConverter when they are instantiating a new
   * AdvancedVisitor to visit a new file with.  Package is set to "" by default.
   * @param file  The File corresponding to the source file we are visiting
   * @param errors  The list of errors that have been encountered so far.
   * @param symbolTable  The table of classes (types) that we have encountered
   * @param continuations  The table of classes we have encountered but still need to resolve
   * @param visitedFiles  The list of files we have visited
   * @param newSDs  The new symbol datas we have created (that will need to have constructors created for them after this pass is finished)
   */
  public AdvancedVisitor(File file, LinkedList<Pair<String, JExpressionIF>> errors, Symboltable symbolTable,
                         Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations,
                         LinkedList<Pair<LanguageLevelVisitor, SourceFile>> visitedFiles,
                         Hashtable<SymbolData, LanguageLevelVisitor> newSDs) {
    super(file, "", new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>(), continuations);
    this.errors = errors;
    this.symbolTable = symbolTable;
    this.visitedFiles= visitedFiles;//new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>();
    this._newSDs = newSDs;
    _hierarchy = new Hashtable<String, TypeDefBase>();//hierarchy;
    _classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
  }

    /**
     * At the Advanced Level, no code augmentation is done.
     * However, if we are working with a class that has no constructor,
     * we need to pretend that it has a 0-ary default constructor.
     * This doesn't actually need to be written in the file, it
     * just needs to be in our internal representation of the class.
     * @param sd  The SymbolData we are checking
   */
  public void createConstructor(SymbolData sd) {
    SymbolData superSd = sd.getSuperClass();
    
    //If sd is a continuation, there was an error somewhere else.  just return.
    if (sd.isContinuation()) return;
    
    String name = getUnqualifiedClassName(sd.getName());
    
    //if sd already has a constructor, just return.
    boolean hasOtherConstructor = sd.hasMethod(name);
    if (hasOtherConstructor) {
          _newSDs.remove(sd); //this won't do anything if sd is not in _newSDs.
          return;
    }
    
    //otherwise, it doesn't have a constructor, so let's add it!
    MethodData md = MethodData.make(name,
                                   new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"}), 
                                   new TypeParameter[0], 
                                   sd, 
                                   new VariableData[0], // No Parameters
                                   new String[0], 
                                   sd,
                                   null);

    addGeneratedMethod(sd, md);
    _newSDs.remove(sd); //this won't do anything if sd is not in _newSDs.
  }

  
  /** Check the modifiers and visibility specifiers that the user has given.  Make sure they are appropriate.
    * Only abstract, public, private, protected, and static and final are allowed at this level.
    */
  public void forModifiersAndVisibilityDoFirst(ModifiersAndVisibility that) {
    String[] modifiersAndVisibility = that.getModifiers();
    StringBuffer sb = new StringBuffer();
    String temp;
    int count = 0;    
    for(int i = 0; i < modifiersAndVisibility.length; i++) {
      temp = modifiersAndVisibility[i];
      if (!(temp.equals("abstract") || temp.equals("public") || temp.equals("private") || temp.equals("protected") || 
            temp.equals("static") || temp.equals("final"))) {
        sb.append(" \"" + temp + "\"");
        count++;
      }
    }
    // check if any illegal keywords were found.  If so, give an error.
    temp = "The keyword";
    if (sb.length() > 0) {
      if (count > 1) {
        temp = temp + "s";
      }
      _addAndIgnoreError(temp + sb.toString() + " cannot be used at the Advanced level", that);
      return;
    }
    super.forModifiersAndVisibilityDoFirst(that);
  }

  /**
   * The default case in a switch statement at the Advanced Level can only appear as the last case.
   */
  public void forSwitchStatementDoFirst(SwitchStatement that) {
    for (int i = 0; i<that.getCases().length-1; i++) {
      SwitchCase sc = that.getCases()[i];
      if (sc instanceof DefaultCase) {
        _addAndIgnoreError("Default case must be the last case of a switch statement at the Advanced level", sc);
      }
    }
    super.forSwitchStatementDoFirst(that);
  }
  
  
  /**
   * Here are several constructs that cannot be used at the Advanced Level
   */
  /*Give error because InstanceInitializers cannot be used at the Advanced Level.*/
  public void forInstanceInitializerDoFirst(InstanceInitializer that) {
    _addError("Instance initializers cannot be used at the Advanced level", that);
  }
  
  /*Give error because StaticInitializers cannot be used at the Advanced Level.*/
  public void forStaticInitializerDoFirst(StaticInitializer that) {
    _addError("Static initializers cannot be used at the Advanced level", that);
  }
  
  /*Give error because LabeledStatements cannot be used at the Advanced Level.*/
  public void forLabeledStatementDoFirst(LabeledStatement that) {
    _addError("Labeled statements cannot be used at the Advanced level", that);
  }

  /*Give error because LabeledBreakStatements cannot be used at the Advanced Level.*/
  public void forLabeledBreakStatementDoFirst(LabeledBreakStatement that) {
    _addError("Labeled statements cannot be used at the Advanced level, so you cannot break to a label", that);
  }
 
  /*Give error because LabeledContinueStatements cannot be used at the Advanced Level.*/
  public void forLabeledContinueStatementDoFirst(LabeledContinueStatement that) {
    _addError("Labeled statements cannot be used at the Advanced level, so you cannot use a labeled continue statement", that);

  }

  /*Give error because SynchronizedStatements cannot be used at the Advanced Level.*/
  public void forSynchronizedStatementDoFirst(SynchronizedStatement that) {
    _addError("Synchronized statements cannot be used at the Advanced level", that);
  }

  /*Give error because TypeParameters cannot be used at the Advanced Level.*/
  public void forTypeParameterDoFirst(TypeParameter that) {
    _addError("Type Parameters cannot be used at the Advanced level", that);
  }

  /*Give error because ConditionalExpressions cannot be used at the Advanced Level.*/
  public void forConditionalExpressionDoFirst(ConditionalExpression that) {
    _addError("Conditional expressions cannot be used at the Advanced level", that);
  }

    /* For now, Give error because Instanceof Expressions cannot be used at the Advanced Level.
     * TODO: Perhaps we should allow this at the advanced level--if we can find a good example of where students would need it.
     */
  public void forInstanceofExpressionDoFirst(InstanceofExpression that) {
    _addError("Instanceof expressions cannot be used at the Advanced level", that);
  }


  /**Only int, double, boolean, and char can be used at any language level.*/
  public void forPrimitiveTypeDoFirst(PrimitiveType that) {
    String name = that.getName();
    if (!(name.equals("int") || name.equals("double") || name.equals("boolean") || name.equals("char"))) {
      _addError("Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Advanced level", that);
    }
    else {
      super.forPrimitiveTypeDoFirst(that);
    }
  }

  /* TryCatchStatements cannot appear at the top level of a file, so give an error
   * if one appears here.
   */
  public void forTryCatchStatementDoFirst(TryCatchStatement that) {
    _addAndIgnoreError("A try-catch statement cannot appear here", that);
    super.forTryCatchStatementDoFirst(that);
  }
  
  /**
   * Check to see if the specified className is a class defined in the current file.
   * If it is, it will be stored in _classNamesInThisFile.
   */
  private boolean _isClassInCurrentFile(String className) {
    Iterator<String> iter = _classNamesInThisFile.iterator();
    while (iter.hasNext()) {
      String s = iter.next();
      if (s.equals(className) || s.endsWith("." + className)) {
        return true;
      }
    }
    return false;   
  }
  
  
  /**
   * Convert the specified array of FormalParameters into an array of VariableDatas which is then returned.
   * For each parameter, try to resolve its type.  A type can be
   * a regular top level type, an inner class type, or something we haven't seen yet (i.e.,
   * a continuation).
   * All formal parameters are automatically made final.
   * All static method parameters are automatically made static-final (see note below).
   * @param fps  The formal parameters we are trying to resolve.
   * @param d  The data these formal parameters belong to.
   * @return  An array of VariableData corresponding to the formal parameters.
   */
  protected VariableData[] formalParameters2VariableData(FormalParameter[] fps, Data d) {
    VariableData[] varData = new VariableData[fps.length];
    VariableDeclarator vd;
    String[] mav;
    
    //This is something of a hack.  The parameters to a static method can be referened within the method itself,
    //even though they are not declared to be static fields.  Since making them static has no effect on any other processing, go
    //ahead and make them static so that the type checking stage will be easier.
    if (d instanceof MethodData && d.hasModifier("static")) {
      mav = new String[] {"final", "static"};
    }
    else {
      mav = new String[] {"final"};
    }
    
    for (int i = 0; i < varData.length; i++) {
      vd = fps[i].getDeclarator();
      String name = vd.getName().getText();
      SymbolData type = getSymbolData(vd.getType().getName(), vd.getType().getSourceInfo());
      
      if (type == null) {
        //see if this is a partially qualified field reference
        type = d.getInnerClassOrInterface(vd.getType().getName());
      }
      
      if (type == null) {
        //if we still couldn't resolve sd, create a continuation for it.
        type = new SymbolData(d.getSymbolData().getName() + "$" + vd.getType().getName());
        d.getSymbolData().addInnerClass(type);
        type.setOuterData(d.getSymbolData());
        continuations.put(type.getName(), new Pair<SourceInfo, LanguageLevelVisitor>(vd.getType().getSourceInfo(), this));
      }
      
      varData[i] = new VariableData(name, 
                                    new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, mav), 
                                    type, true, d);
      varData[i].gotValue();

    }
    return varData;
  }
  
  
  /**
   * Handle a ClassDef at the Advanced Level.  To do this, get its name and call addSymbolData to do the appropriate lookups.
   * Then visit everything in the class def.
   * Once the ClassDef has been handled, remove the class from _classesToBeParsed so we know it's been taken care of.
   * @param that  The ClassDef being handled.
   */
  public void forClassDef(ClassDef that) {    
    forClassDefDoFirst(that);
    if (prune(that)) return;

    String className = getQualifiedClassName(that.getName().getText());
    SymbolData sd = addSymbolData(that, className); //does the class initalization
   
    that.getMav().visit(this);
    
    that.getName().visit(this);
    for (int i = 0; i < that.getTypeParameters().length; i++) that.getTypeParameters()[i].visit(this);
//    that.getSuperclass().visit(this);
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    
    if (sd != null) { //we were able to resolve the type, so visit the body.
      that.getBody().visit(new ClassBodyAdvancedVisitor(sd, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));

      /**Currently, at the Advanced Level, these are no-ops, but leave the calls here for now,
       * in case we decide to add augmentation back in.
       * createConstructor is called elsewhere.*/
      createAccessors(sd, _file);
      createToString(sd);
      createHashCode(sd);
      createEquals(sd);

    }
    forClassDefOnly(that);

    _classesToBeParsed.remove(className);
  }
  
  
  
  /***
   * This is a noop, because we do not do code augmentation at the Advanced Level.
   */
  protected void createToString(SymbolData sd) {
    return; //noop
  }

  /***
   * This is a noop, because we do not do code augmentation at the Advanced Level.
   */
    protected void createHashCode(SymbolData sd) {    
      return; //noop
    }
    
  /***
   * This is a noop, because we do not do code augmentation at the Advanced Level.
   */
    protected void createEquals(SymbolData sd) {    
      return; //noop
    }    

    
   
  /**
   * Create a SymbolData corresponding to this interface and add it appropriately.
   * Then visit the body to handle anything defined inside the interface.
   * Once the interface has been resolved, remove it from _classesToBeParsed.
   */
  public void forInterfaceDef(InterfaceDef that) {
    forInterfaceDefDoFirst(that);
    if (prune(that)) return;

    String className = that.getName().getText();
    that.getMav().visit(this);
    that.getName().visit(this);
    for (int i = 0; i < that.getTypeParameters().length; i++) that.getTypeParameters()[i].visit(this);
    SymbolData sd = addSymbolData(that, getQualifiedClassName(className)); //does the initialization
    if (sd != null) { //we have a symbol data to work with--visit the body.
      sd.setInterface(true); //set the (cough) interface flag
      that.getBody().visit(new InterfaceBodyAdvancedVisitor(sd, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));
    }
      
    forInterfaceDefOnly(that);
    _classesToBeParsed.remove(getQualifiedClassName(className));
  }
  
  
  /**
   * Check to make sure the inner class def is okay, and then resolve it and store it in
   * the outer class's list of inner classes.  This method is common to both ClassBodyAdvancedVisitor
   * and InterfaceBodyIntermediateVisitor but needs the correct Data so we pass it in.  This method is tested
   * in those files.  We use the fully qualified name along with the $'s so we don't accidentally conflict with an
   * actual class in the symbol table.
   * @param that  The AST Node for the inner class def
   * @param data  The Data that encloses this inner class.  This is a Data because the inner class can be in a class or a method.
   * @param name  The name of the inner class.  This should be the qualified name of the enclosing data followed by a $.  
   *              For local classes, we append a number after the $ corresponding to the order that the local class
   *              appears in the method and then put the name of the class.  For anonymous classes, we only put a number 
   *              after the $.  For example, if class A has a method B that has a local class C, then name should be "A$1C".
   */
  protected void handleInnerClassDef(InnerClassDef that, Data data, String name) {
    forInnerClassDefDoFirst(that);
    if (prune(that)) return;

    that.getMav().visit(this);
    that.getName().visit(this);
    for (int i = 0; i < that.getTypeParameters().length; i++) that.getTypeParameters()[i].visit(this);
    that.getSuperclass().visit(this);
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    
    SymbolData sd = addInnerSymbolData(that, name, that.getName().getText(), data, true);

    
    if (sd != null) { //we have a symbol data to work with, so visit the body and augment
      that.getBody().visit(new ClassBodyAdvancedVisitor(sd, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));
      
      /**At the Advanced Level, these are currently no-ops, but leave the call in, in case we decide to do some form of
       * augmentation later.  createConstructor is called elsewhere.*/
      createAccessors(sd, _file);
      createToString(sd);
      createHashCode(sd);
      createEquals(sd);

    }
   
    // Inner classes should never be put into _classesToBeParsed since they are parsed whenever their outer classes are parsed.

    forInnerClassDefOnly(that);
  }
  
  /**
   * Check to make sure that the inner interface is okay, and resolve it and store it in the
   * outer class's list of inner classes.  This method is common to both ClassBodyIntermediateVisitor
   * and InterfaceBodyIntermediateVisitor but needs the correct SymbolData so we pass it in.  This method is tested
   * in those files.
   */
  protected void handleInnerInterfaceDef(InnerInterfaceDef that, SymbolData symbolData, String name) {
    forInnerInterfaceDefDoFirst(that);
    if (prune(that)) return;

    that.getMav().visit(this);
    that.getName().visit(this);
    for (int i = 0; i < that.getTypeParameters().length; i++) that.getTypeParameters()[i].visit(this);
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);

    SymbolData sd = addInnerSymbolData(that, name, that.getName().getText(), symbolData, true);
    if (sd != null) {

      that.getBody().visit(new InterfaceBodyAdvancedVisitor(sd, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));
    }

    forInnerInterfaceDefOnly(that);
  }

  /** Do the work that is shared between SimpleAnonymousClassInstantiations and ComplexAnonymousClassInstantiations.
    * Do not generate automatic accessors for the anonymous class--this will be done in type checker pass.
    * @param that  The AnonymousClassInstantiation being visited
    * @param enclosing  The enclosing Data
    * @param superC  The super class being instantiated--i.e. new A() { ...}, would have a super class of A.
    */
  public void anonymousClassInstantiationHelper(AnonymousClassInstantiation that, Data enclosing, SymbolData superC) {
    that.getArguments().visit(this); 
    
    //Get the SymbolData that will correspond to this anonymous class
    SymbolData sd = new SymbolData(getQualifiedClassName(enclosing.getSymbolData().getName()) + "$" + enclosing.getSymbolData().preincrementAnonymousInnerClassNum());
    enclosing.addInnerClass(sd);
    sd.setOuterData(enclosing);
    
    if (superC != null && ! superC.isInterface()) {
      sd.setSuperClass(superC); //the super class is what was passed in
    }
    sd.setPackage(_package);
    
    createToString(sd);
    createHashCode(sd);
    createEquals(sd);
    //accessors will be filled in in typeChecker pass
    
    //visit the body to get it all nice and resolved.
    that.getBody().visit(new ClassBodyAdvancedVisitor(sd, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));

  }
  
  /** Look up the super type of this class instantiation and add it to the symbol table.  Visit the body of the class
    * instantiation.  All handling of this as an anonymous inner class (i.e. adding it to the enclosing SD's list of
    * inner classes, creating a symbol data for the anonyomous inner class, etc) is handled in the TypeChecker pass.  
    * This is because no one will depend on that symbolData until we create it.
    * @param that  The SimpleAnonymousClassInstantiation being processed.
    */
  public void simpleAnonymousClassInstantiationHelper(SimpleAnonymousClassInstantiation that, Data data) {
    forSimpleAnonymousClassInstantiationDoFirst(that);
    if (prune(that)) return;

    //resolve the super class and make sure it will be in the SymbolTable.
    SymbolData superC = getSymbolData(that.getType().getName(), that.getSourceInfo());
    
    anonymousClassInstantiationHelper(that, data, superC);

    forSimpleAnonymousClassInstantiationOnly(that);
  }
  
  /**
   * Do not resolve the super class type of this instantiation, becuase it will have already been resolved (it
   * is an inner class of the enclosing of this instantiation.  When the enclosing was resolved, all of its inner classes should have
   * also been resolved).
   * Visit the body of the class instantiation.  
   * @param that  The ComplexAnonymousClassInstantiation being processed.
   * @param data  The enclosing data where this was sreferenced from.
   */
  public void complexAnonymousClassInstantiationHelper(ComplexAnonymousClassInstantiation that, Data data) {
    forComplexAnonymousClassInstantiationDoFirst(that);
    if (prune(that)) return;
    
    //visit the enclosing 
    that.getEnclosing().visit(this);
    
    //no need to resolve the super class of the type being instantiated, because it is a complex type, so its enclosing data should
    //get added to the symbolTable along with it wherever we resolved its enclosing data.
    
    //originally, make super class null.  This will be updated in the TypeChecker pass.
    anonymousClassInstantiationHelper(that, data, null);

    
    forComplexAnonymousClassInstantiationOnly(that);
  }
  
  
  /*Resolve the ArrayType by looking it up.*/  
  public void forArrayType(ArrayType that) {
    forArrayTypeDoFirst(that);
    if (prune(that)) return;
    getSymbolData(that.getName(), that.getSourceInfo());
  }

  
  /**
   * Test the methods defined in the enclosing class.
   * There is a test method corresponding to almost every method in the above class.
   */
  public static class AdvancedVisitorTest extends TestCase {
    
    /*Some initial initializations:*/
    private AdvancedVisitor _av;
    
    private SymbolData _sd1;
    private SymbolData _sd2;
    private SymbolData _sd3;
    private SymbolData _sd4;
    private SymbolData _sd5;
    private SymbolData _sd6;
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[0]);
    private ModifiersAndVisibility _abstractMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"abstract"});
    private ModifiersAndVisibility _staticMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"static"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"final"});
    private ModifiersAndVisibility _volatileMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[]{"volatile"});
    
    
    public AdvancedVisitorTest() {
      this("");
    }
    public AdvancedVisitorTest(String name) {
      super(name);
    }
    
    public void setUp() {

      errors = new LinkedList<Pair<String, JExpressionIF>>();
      symbolTable = new Symboltable();
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      _hierarchy = new Hashtable<String, TypeDefBase>();
      _classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
      _av = new AdvancedVisitor(new File(""), errors, symbolTable, continuations, new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>(), new Hashtable<SymbolData, LanguageLevelVisitor>());
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, IterUtil.make(new File("lib/buildlib/junit.jar")));
      _av.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      _av._resetNonStaticFields();
      _av._importedPackages.addFirst("java.lang");
      _errorAdded = false;
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");
    }
    
    public void testForModifiersAndVisibilityDoFirst() {
      
      //Check that the proper modifiers are allowed:
      _av.forModifiersAndVisibilityDoFirst(_abstractMav);
      _av.forModifiersAndVisibilityDoFirst(_publicMav);
      _av.forModifiersAndVisibilityDoFirst(_privateMav);
      _av.forModifiersAndVisibilityDoFirst(_protectedMav);
      _av.forModifiersAndVisibilityDoFirst(_staticMav);
      _av.forModifiersAndVisibilityDoFirst(_finalMav);
      
      ModifiersAndVisibility mavs = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, 
                                                                new String[] {"private", "static"});
       _av.forModifiersAndVisibilityDoFirst(mavs);
      assertEquals("there should still be 0 errors", 0, errors.size());

      //check errors:
      
      _av.forModifiersAndVisibilityDoFirst(_volatileMav);
      assertEquals("there should now be 1 errors", 1, errors.size());
      assertEquals("The error message should be correct for private modifier:", "The keyword \"volatile\" cannot be used at the Advanced level", errors.get(0).getFirst());

      ModifiersAndVisibility mavs2 = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, 
                                                                new String[] {"final", "volatile"});
     
      _av.forModifiersAndVisibilityDoFirst(mavs2);
      assertEquals("There should now be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct for 1 bad, 1 good modifier:", "The keyword \"volatile\" cannot be used at the Advanced level", errors.get(1).getFirst());

      ModifiersAndVisibility mavs3 = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, 
                                                                new String[] {"synchronized", "native"});
     
      _av.forModifiersAndVisibilityDoFirst(mavs3);
      assertEquals("There should now be 3 errors", 3, errors.size());
      assertEquals("The error message should be correct for 2 bad modifiers:", "The keywords \"synchronized\" \"native\" cannot be used at the Advanced level", errors.get(2).getFirst());

      
    }
    
    public void testForClassDefDoFirst() {
      //check an example that works
      ClassDef cd0 = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, new Word(JExprParser.NO_SOURCE_INFO, "Lisa"),
                                  new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      _av.forClassDefDoFirst(cd0);
      assertEquals("should be no errors", 0, errors.size());
      
      //check that an error is not thrown if a class doesn't explicitely extend another class
      ClassDef cd1 = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                                  new Word(JExprParser.NO_SOURCE_INFO, "Test"), new TypeParameter[0], JExprParser.NO_TYPE,
                                  new ReferenceType[0], new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

      _av.forClassDefDoFirst(cd1);
      assertEquals("there should still be 0 errors", 0, errors.size());
       
       //check that an error is not thrown if a class implements any interfaces.
       ClassDef cd2 = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                                   new Word(JExprParser.NO_SOURCE_INFO, "Test"), new TypeParameter[0], 
                                   new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]),
                                   new ReferenceType[] {new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0])}, 
                                   new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

                                 
      _av.forClassDefDoFirst(cd2);
      assertEquals("there should still be 0 errors", 0, errors.size());
    }
    
    public void testForFormalParameterDoFirst() {
      PrimitiveType pt = new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int");
      Word w = new Word(JExprParser.NO_SOURCE_INFO, "param");
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, pt, w);
      
      // check an example that works
      FormalParameter fp = new FormalParameter(JExprParser.NO_SOURCE_INFO, uvd, false);
      _av.forFormalParameterDoFirst(fp);
      assertEquals("should be no errors", 0, errors.size());
      
      // check that no errors are thrown if the FormalParameter is final
      FormalParameter fp2 = new FormalParameter(JExprParser.NO_SOURCE_INFO, uvd, true);  
      _av.forFormalParameterDoFirst(fp2);
      assertEquals("should still be no errors", 0, errors.size());
    }
    
    public void test_NotAllowed() {
      SourceInfo noInfo = JExprParser.NO_SOURCE_INFO;
      Word w = new Word(JExprParser.NO_SOURCE_INFO, "word");
      TypeParameter[] tps = new TypeParameter[0];
      ReferenceType[] rts = new ReferenceType[0];
      BracedBody emptyBody = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]);
      UnbracedBody emptyUnbracedBody = new UnbracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]);
      ClassOrInterfaceType superClass = new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]);
      FormalParameter[] fps = new FormalParameter[0];
      CompoundWord cw = new CompoundWord(noInfo, new Word[0]);
      Statement stmt = new EmptyStatement(noInfo);
      Expression e = new EmptyExpression(noInfo);
      Block b = new Block(noInfo, emptyBody);
      //      ClassModifier cm = ClassModifier.NONE;
      TypeVariable tv = new TypeVariable(noInfo, "name");
      
      InnerInterfaceDef ii = new InnerInterfaceDef(noInfo, _publicMav, w, tps, rts, emptyBody);
      InnerClassDef ic = new InnerClassDef(noInfo, _publicMav, w, tps, superClass, rts, emptyBody);
      
      StaticInitializer si = new StaticInitializer(noInfo, b);
      LabeledStatement ls = new LabeledStatement(noInfo, new Word(noInfo, "label"), stmt);

      LabeledBreakStatement bs = new LabeledBreakStatement(noInfo, new Word(noInfo, "myLabel"));
      LabeledContinueStatement cs = new LabeledContinueStatement(noInfo, new Word(noInfo, "yourLabel"));
      SynchronizedStatement syncs = new SynchronizedStatement(noInfo, e, b);
      TypeParameter tp = new TypeParameter(noInfo, tv, superClass);
      ConditionalExpression ce = new ConditionalExpression(noInfo, e, e, e);
      
      TryCatchStatement tcs = new NormalTryCatchStatement(noInfo, b, new CatchBlock[0]);
      SwitchCase defaultSc = new DefaultCase(JExprParser.NO_SOURCE_INFO, emptyUnbracedBody);
      SwitchStatement ssBadDefault = new SwitchStatement(noInfo, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5), new SwitchCase[]{defaultSc, defaultSc});
      

     
     si.visit(_av);
     assertEquals("There should be 1 error", 1, errors.size());
     assertEquals("StaticInitializer is not allowed", "Static initializers cannot be used at the Advanced level", errors.getLast().getFirst());

     ls.visit(_av);
     assertEquals("There should be 2 errors", 2, errors.size());
     assertEquals("Labeled Statement is not allowed", "Labeled statements cannot be used at the Advanced level", errors.getLast().getFirst());

    
     bs.visit(_av);
     assertEquals("There should be 3 errors", 3, errors.size());
     assertEquals("LabeledBreakStatement is not allowed", "Labeled statements cannot be used at the Advanced level, so you cannot break to a label", errors.getLast().getFirst());

     cs.visit(_av);
     assertEquals("There should be 4 error", 4, errors.size());
     assertEquals("ContinueStatement is not allowed", "Labeled statements cannot be used at the Advanced level, so you cannot use a labeled continue statement", errors.getLast().getFirst());
     
     syncs.visit(_av);
     assertEquals("There should be 5 error", 5, errors.size());
     assertEquals("SynchronizedStatement is not allowed", "Synchronized statements cannot be used at the Advanced level", errors.getLast().getFirst());
    
     tp.visit(_av);
     assertEquals("There should be 6 errors", 6, errors.size());
     assertEquals("TypeParameters is not allowed", "Type Parameters cannot be used at the Advanced level", errors.getLast().getFirst());

     ce.visit(_av);
     assertEquals("There should be 7 errors", 7, errors.size());
     assertEquals("ConditionalExpression is not allowed", "Conditional expressions cannot be used at the Advanced level", errors.getLast().getFirst());
     
     tcs.visit(_av);
     assertEquals("There should be 8 errors", 8, errors.size());
     assertEquals("try-catch statements are not allowed", "A try-catch statement cannot appear here", errors.getLast().getFirst());
     
     ssBadDefault.visit(_av);
     assertEquals("There should be 9 errors", 9, errors.size());
     assertEquals("Default case has to be last case", "Default case must be the last case of a switch statement at the Advanced level", errors.getLast().getFirst());
    }
    
    public void testForArrayType() {
      symbolTable.put("name", new SymbolData("name"));
      ArrayInitializer ai = new ArrayInitializer(JExprParser.NO_SOURCE_INFO, new VariableInitializerI[0]);
      TypeVariable tv = new TypeVariable(JExprParser.NO_SOURCE_INFO, "name");
      ArrayType at = new ArrayType(JExprParser.NO_SOURCE_INFO, "name[]", tv);
      
      
      at.visit(_av);
      assertEquals("There should be no errors", 0, errors.size());
      SymbolData sd = _av.symbolTable.get("name[]");
      assertNotNull("sd should not be null", sd);
      ArrayData ad = (ArrayData) sd;
      assertEquals("ad should have an inner sd of name name:", "name", ad.getElementType().getName());
      
      ai = new ArrayInitializer(JExprParser.NO_SOURCE_INFO, new VariableInitializerI[0]);
      tv = new TypeVariable(JExprParser.NO_SOURCE_INFO, "String");
      at = new ArrayType(JExprParser.NO_SOURCE_INFO, "String[]", tv);
      
      VariableDeclarator vd = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, at, new Word(JExprParser.NO_SOURCE_INFO, "myArray"));
      VariableDeclaration vdecl = new VariableDeclaration(JExprParser.NO_SOURCE_INFO, _publicMav, new VariableDeclarator[] {vd});
      vdecl.visit(_av);
      SymbolData bob = _av.symbolTable.get("java.lang.String[]");
      assertNotNull("bob should not be null", bob);
      
      //Test a multi-dimensional array
      tv = new TypeVariable(JExprParser.NO_SOURCE_INFO, "Object");
      at = new ArrayType(JExprParser.NO_SOURCE_INFO, "Object[]", tv);
      ArrayType at2 = new ArrayType(JExprParser.NO_SOURCE_INFO, "Object[][]", at);

      at2.visit(_av);
      assertEquals("There should be no errors", 0, errors.size());
      assertNotNull("Object should be in the symbolTable", _av.symbolTable.get("java.lang.Object"));
      assertNotNull("Object[] should be in the symbolTable", _av.symbolTable.get("java.lang.Object[]"));
      assertNotNull("Object[][] should be in the symbolTable", _av.symbolTable.get("java.lang.Object[][]"));


    }
    
    public void testForPrimitiveTypeDoFirst() {
      
      SourceInfo noInfo = JExprParser.NO_SOURCE_INFO;
      
     //only primative types boolean, char, int, and double are allowed at Intermediate level. 
      PrimitiveType i = new PrimitiveType(noInfo, "int");
      PrimitiveType c = new PrimitiveType(noInfo, "char");
      PrimitiveType d = new PrimitiveType(noInfo, "double");
      PrimitiveType b = new PrimitiveType(noInfo, "boolean");
      
      i.visit(_av);
      assertEquals("After visiting int, errors should still be 0", 0, errors.size());

      c.visit(_av);
      assertEquals("After visiting char, errors should still be 0", 0, errors.size());
            
      d.visit(_av);
      assertEquals("After visiting double, errors should still be 0", 0, errors.size());

      b.visit(_av);
      assertEquals("After visiting boolean, errors should still be 0", 0, errors.size());
      
      //now the types that should throw an error:
      
      PrimitiveType byt = new PrimitiveType(noInfo, "byte");
      PrimitiveType s = new PrimitiveType(noInfo, "short");
      PrimitiveType l = new PrimitiveType(noInfo, "long");
      PrimitiveType f = new PrimitiveType(noInfo, "float");
      
      byt.visit(_av);
      assertEquals("After visiting byte, errors should be 1", 1, errors.size());
      assertEquals("After byte, error message is correct", "Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Advanced level", errors.getLast().getFirst());

      s.visit(_av);
      assertEquals("After visiting short, errors should be 2", 2, errors.size());
      assertEquals("After short, error message is correct", "Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Advanced level", errors.getLast().getFirst());

      l.visit(_av);
      assertEquals("After visiting long, errors should be 3", 3, errors.size());
      assertEquals("After long, error message is correct", "Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Advanced level", errors.getLast().getFirst());

      f.visit(_av);
      assertEquals("After visiting float, errors should be 4", 4, errors.size());
      assertEquals("After float, error message is correct", "Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Advanced level", errors.getLast().getFirst());
    }
    
    public void test_isClassInCurrentFile() {
     assertFalse("class not in file should return false", _av._isClassInCurrentFile("NotInFile"));
     _av._classNamesInThisFile.addLast("package.MyClass");
     assertTrue("full class name in file should return true", _av._isClassInCurrentFile("package.MyClass"));
     assertTrue("unqualified class name in file should return true", _av._isClassInCurrentFile("MyClass"));
     
     assertFalse("partial name in file, not same class, should return false", _av._isClassInCurrentFile("Class"));
                
    }
    
    public void testCreateConstructor() {
      SymbolData sd = new SymbolData("ClassName", _publicMav, new TypeParameter[0], null, new LinkedList<SymbolData>(), null);
      VariableData v1 = new VariableData("i", _publicMav, SymbolData.INT_TYPE, false, sd);
      VariableData v2 = new VariableData("j", _publicMav, SymbolData.CHAR_TYPE, false, sd);
      VariableData v3 = new VariableData("var", _publicMav, SymbolData.DOUBLE_TYPE, false, sd);
      sd.addVar(v1);
      sd.addVar(v2);
      sd.setSuperClass(_sd1);
      
      MethodData md = new MethodData("ClassName", _publicMav, new TypeParameter[0], sd, 
                                   new VariableData[0], 
                                   new String[0], 
                                   sd,
                                   null);
      md.addVars(md.getParams());
      _av.createConstructor(sd);
      
      assertEquals("sd should have 1 method: its own constructor", md, sd.getMethods().getFirst());
      
      //since this is the only constructor in the symbol data, all the fields should be assigned to have a value after visiting sd.
      v1 = new VariableData("i", _publicMav, SymbolData.INT_TYPE, true, sd);
      v2 = new VariableData("j", _publicMav, SymbolData.CHAR_TYPE, true, sd);

      
      //now test a subclass of sd:
      SymbolData subSd = new SymbolData("Subclass",_publicMav, new TypeParameter[0], null, new LinkedList<SymbolData>(), null);
      subSd.addVar(v3);
      subSd.setSuperClass(sd);

      
      VariableData v1Param = new VariableData("super_i", _packageMav, SymbolData.INT_TYPE, true, null);
      VariableData v2Param = new VariableData("super_j", _packageMav, SymbolData.CHAR_TYPE, true, null);
      VariableData[] vars = {v1Param, v2Param, v3};
      MethodData md2 = new MethodData("Subclass", _publicMav, new TypeParameter[0], subSd,
                                      new VariableData[0], new String[0], subSd, null);
      md2.addVars(md2.getParams());
                
      _av.createConstructor(subSd);
      v1Param.setEnclosingData(subSd.getMethods().getFirst());
      v2Param.setEnclosingData(subSd.getMethods().getFirst());
      assertEquals("subSd should have 1 method: its own constructor.", md2, subSd.getMethods().getFirst());
    }
    
    public void test_getFieldAccessorName() {
      // This may change in the future if we change getFieldAccessorName
      assertEquals("Should correctly convert from lower case to upper case", "name", _av.getFieldAccessorName("name"));
    }
    
    //ToString, HashCode, and Equals should be no-ops.
    public void testCreateToString() {
      SymbolData sd = new SymbolData("ClassName", _publicMav, new TypeParameter[0], null, new LinkedList<SymbolData>(), null);
      _av.createToString(sd);
      //should have been no-op
      assertEquals("sd should have no methods", 0, sd.getMethods().size());
    }
    
    public void testCreateHashCode() {
      SymbolData sd = new SymbolData("ClassName", _publicMav, new TypeParameter[0], null, new LinkedList<SymbolData>(), null);      
      _av.createHashCode(sd);
      //should have been no-op
      assertEquals("sd should have 0 methods", 0, sd.getMethods().size());
    }
    
    public void testCreateEquals() {
      SymbolData sd = new SymbolData("ClassName", _publicMav, new TypeParameter[0], null, new LinkedList<SymbolData>(), null);
      _av.createEquals(sd);
      //should have been no-op
      assertEquals("sd should have 0 methods", 0, sd.getMethods().size());
    }
    
    public void testForClassDef() {
      //check an example that's not abstract
      _av._package = "myPackage";
      ClassDef cd0 = new ClassDef(JExprParser.NO_SOURCE_INFO, _packageMav, new Word(JExprParser.NO_SOURCE_INFO, "Lisa"),
                                 new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Object", new Type[0]), new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0])); 
      
      
      cd0.visit(_av);
      assertEquals("There should be no errors", 0, errors.size());
      assertTrue("Should have resolved java.lang.Object", symbolTable.containsKey("java.lang.Object"));
      assertFalse("Should not be a continuation", symbolTable.get("java.lang.Object").isContinuation());
      SymbolData sd = symbolTable.get("myPackage.Lisa");
      assertTrue("Lisa should be in _newSDs", _newSDs.containsKey(sd));
      assertEquals("sd should have no methods", 0, sd.getMethods().size());
      assertEquals("sd's package should be correct", "myPackage", sd.getPackage());
      
      //check an example that's abstract
      _av._package = "";
      ClassDef cd1 = new ClassDef(JExprParser.NO_SOURCE_INFO, _abstractMav, new Word(JExprParser.NO_SOURCE_INFO, "Bart"),
                                  new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "System", new Type[0]), new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cd1.visit(_av);
      assertEquals("There should be no errors", 0, errors.size());
      assertTrue("Should have resolved java.lang.System", symbolTable.containsKey("java.lang.System"));
      assertFalse("Should not be a continuation", symbolTable.get("java.lang.System").isContinuation());
      sd = symbolTable.get("Bart");
      
      assertEquals("There should be 0 methods", 0, sd.getMethods().size()); //(no code augmentation is done)
      
      //Check an example where the class extends TestCase, and has a test method that returns void.
      ConcreteMethodDef cmd = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _packageMav, 
                                                    new TypeParameter[0], 
                                                    new VoidReturn(JExprParser.NO_SOURCE_INFO, "void"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "testMethodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

      ClassDef cd3 = new ClassDef(JExprParser.NO_SOURCE_INFO, _abstractMav, new Word(JExprParser.NO_SOURCE_INFO, "TestSuper2"),
                                  new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "TestCase", new Type[0]), new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {cmd}));


      _av._file=new File("TestSuper2.dj2");
      _av._importedFiles.addLast("junit.framework.TestCase");
      symbolTable.put("junit.framework.TestCase", new SymbolData("junit.framework.TestCase"));
      cd3.visit(_av);
      assertEquals("There should still just be no errors", 0, errors.size());
      assertNotNull("Should have looked up TestSuper2", symbolTable.get("TestSuper2"));
      
      //Check a method with void return, but name not starting with test, so it's not okay.
      //This is now checked in the type checker!
      ConcreteMethodDef cmd2 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _packageMav, 
                                                    new TypeParameter[0], 
                                                    new VoidReturn(JExprParser.NO_SOURCE_INFO, "void"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "uhOh"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

      ClassDef cd4 = new ClassDef(JExprParser.NO_SOURCE_INFO, _abstractMav, new Word(JExprParser.NO_SOURCE_INFO, "TestVoidNoTestMethod"),
                                  new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "junit.framework.TestCase", new Type[0]), new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {cmd2}));



      _av._file=new File("TestVoidNoTestMethod.dj2");
      cd4.visit(_av);

      assertEquals("There should still be 0 errors", 0, errors.size());
      _av._importedFiles.remove("junit.framework.TestCase");
      
    }
    
    
    public void testForInterfaceDef() {
      AbstractMethodDef amd = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"),
                                                                                               new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[0], new ReferenceType[0]);
      AbstractMethodDef amd2 = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"),
                                                                                               new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[0], new ReferenceType[0]);
      InterfaceDef id = new InterfaceDef(JExprParser.NO_SOURCE_INFO, _publicMav, new Word(JExprParser.NO_SOURCE_INFO, "id"), new TypeParameter[0], new ReferenceType[0], 
                                         new BracedBody(JExprParser.NO_SOURCE_INFO, 
                                                        new BodyItemI[] {amd}));
      InterfaceDef id2 = new InterfaceDef(JExprParser.NO_SOURCE_INFO, _publicMav, new Word(JExprParser.NO_SOURCE_INFO, "id2"), new TypeParameter[0], new ReferenceType[] {new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "id", new Type[0])}, 
                                         new BracedBody(JExprParser.NO_SOURCE_INFO, 
                                                        new BodyItemI[] {amd2}));
      SymbolData sd = new SymbolData("id", _publicMav, new TypeParameter[0], new LinkedList<SymbolData>(), null);
      sd.setIsContinuation(true);
      MethodData md = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[0], new String[0], sd, amd);

      LinkedList<SymbolData> interfaces = new LinkedList<SymbolData>();
      interfaces.addLast(sd);
      SymbolData sd2 = new SymbolData("id2", _publicMav, new TypeParameter[0], interfaces, null);
      sd2.setIsContinuation(true);
      MethodData md2 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[0], new String[0], sd2, amd2);
      
      _av.symbolTable.put("id", sd);
      _av.symbolTable.put("id2", sd2);

      id.visit(_av);
      id2.visit(_av);
      assertEquals("Should be no errors", 0, errors.size());
      assertEquals("Should return the same symbol datas: id", sd, symbolTable.get("id"));
      assertEquals("Should return the same symbol datas:id2 ", sd2, symbolTable.get("id2"));
    }
    
    public void testHandleInnerClassDef() {      
      SymbolData obj = new SymbolData("java.lang.Object");
      symbolTable.put("java.lang.Object", obj);
      InnerClassDef cd1 = new InnerClassDef(JExprParser.NO_SOURCE_INFO, _packageMav, new Word(JExprParser.NO_SOURCE_INFO, "Bart"),
                                       new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), new ReferenceType[0], 
                                       new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      InnerClassDef cd0 = new InnerClassDef(JExprParser.NO_SOURCE_INFO, _packageMav, new Word(JExprParser.NO_SOURCE_INFO, "Lisa"),
                                       new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), new ReferenceType[0], 
                                            new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {cd1}));

      SymbolData outerData = new SymbolData("i.eat.potato");
      SymbolData sd0 = new SymbolData(outerData.getName() + "$Lisa", _packageMav, new TypeParameter[0], obj, new LinkedList<SymbolData>(), null); 
      SymbolData sd1 = new SymbolData(outerData.getName() + "$Lisa$Bart", _packageMav, new TypeParameter[0], obj, new LinkedList<SymbolData>(), null); 
      
      outerData.addInnerClass(sd0);
      sd0.setOuterData(outerData);

      sd0.addInnerClass(sd1);
      sd1.setOuterData(sd0);

      sd0.setIsContinuation(true);
      sd1.setIsContinuation(true);
      
            
      symbolTable.put(outerData.getName() + "$Lisa", sd0);

      _av.handleInnerClassDef(cd0, outerData, outerData.getName() + "$Lisa");

      SymbolData sd = outerData.getInnerClassOrInterface("Lisa");
      assertEquals("There should be no errors", 0, errors.size());
      assertEquals("This symbolData should now have sd0 as an inner class", sd0, sd);
      assertEquals("sd0 should have the correct outer data", outerData, sd0.getOuterData());
      assertEquals("sd1 should have the correct outer data", sd0, sd1.getOuterData());
      assertEquals("Sd should now have sd1 as an inner class", sd1, sd.getInnerClassOrInterface("Bart"));
      
      
      assertEquals("Lisa should have 0 methods", 0, sd0.getMethods().size());
    }
    
    public void testHandleInnerInterfaceDef() {
      SymbolData obj = new SymbolData("java.lang.Object");
      symbolTable.put("java.lang.Object", obj);
      InnerInterfaceDef cd1 = new InnerInterfaceDef(JExprParser.NO_SOURCE_INFO, _packageMav, new Word(JExprParser.NO_SOURCE_INFO, "Bart"),
                                       new TypeParameter[0], new ReferenceType[0], 
                                       new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      InnerInterfaceDef cd0 = new InnerInterfaceDef(JExprParser.NO_SOURCE_INFO, _packageMav, new Word(JExprParser.NO_SOURCE_INFO, "Lisa"),
                                       new TypeParameter[0], new ReferenceType[0], 
                                            new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {cd1}));

      SymbolData outerData = new SymbolData("i.drink.vanilla.coke");

      
      SymbolData sd0 = new SymbolData(outerData.getName() + "$Lisa", _packageMav, new TypeParameter[0], new LinkedList<SymbolData>(), null); 
      SymbolData sd1 = new SymbolData(outerData.getName() + "$Lisa$Bart", _packageMav, new TypeParameter[0], new LinkedList<SymbolData>(), null);
      sd0.addInnerInterface(sd1);
      sd0.setIsContinuation(true);
      sd1.setIsContinuation(true);

      outerData.addInnerInterface(sd0);
      sd0.setOuterData(outerData);

      sd0.addInnerInterface(sd1);
      sd1.setOuterData(sd0);

      sd0.setIsContinuation(true);
      sd1.setIsContinuation(true);

      
      _av.handleInnerInterfaceDef(cd0, outerData, outerData.getName() + "$Lisa");

      SymbolData sd = outerData.getInnerClassOrInterface("Lisa");
      
      assertEquals("There should be no errors", 0, errors.size());
      assertEquals("This symbolData should now have sd0 as an inner interface", sd0, sd);
      assertEquals("sd0 should have the correct outer data", outerData, sd0.getOuterData());
      assertEquals("sd1 should have the correct outer data", sd0, sd1.getOuterData());
      assertEquals("Sd should now have sd1 as an inner interface", sd1, sd.getInnerClassOrInterface("Bart"));
      assertTrue("Lisa should be an interface", sd0.isInterface());
      assertTrue("Bart should be an interface", sd1.isInterface());
    }
    
    
    public void testCreateMethodData() {
      // Test one that doesn't work.
      MethodDef mdef = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _volatileMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      MethodData mdata = new MethodData("methodName", _volatileMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                                   new VariableData[0], 
                                   new String[0],
                                   _sd1,
                                   null);
      assertEquals("Should return the correct MethodData", mdata, _av.createMethodData(mdef, _sd1));
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "The keyword \"volatile\" cannot be used at the Advanced level", errors.get(0).getFirst());

      
      mdef = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _finalMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "methodName2"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      mdata = new MethodData("methodName2", _finalMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                                   new VariableData[0], 
                                   new String[0],
                                   _sd1,
                                   null);
      assertEquals("Should return the correct MethodData", mdata, _av.createMethodData(mdef, _sd1));
      assertEquals("There should still just be 1 error", 1, errors.size());

      
      // Test one that does work.
      mdef = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                   _abstractMav, 
                                   new TypeParameter[] { new TypeParameter(JExprParser.NO_SOURCE_INFO,
                                                                           new TypeVariable(JExprParser.NO_SOURCE_INFO, "T"),
                                                                           new TypeVariable(JExprParser.NO_SOURCE_INFO, "U"))},
                                   new VoidReturn(JExprParser.NO_SOURCE_INFO, "void"), 
                                   new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                   new FormalParameter[] {
                                     new FormalParameter(JExprParser.NO_SOURCE_INFO, 
                                                         new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                                                             new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                                                             new Word (JExprParser.NO_SOURCE_INFO, "field1")),
                                                         false
                                                           ),
                                     new FormalParameter(JExprParser.NO_SOURCE_INFO, 
                                                         new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                                                             new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                                                             new Word (JExprParser.NO_SOURCE_INFO, "field1")),
                                                         false
                                                           )},
                                   new ReferenceType[] { new TypeVariable(JExprParser.NO_SOURCE_INFO, "X") }
                                   );
                                   mdata = new MethodData("methodName", 
                                                          _abstractMav, 
                                                          new TypeParameter[] { new TypeParameter(JExprParser.NO_SOURCE_INFO,
                                                                                                  new TypeVariable(JExprParser.NO_SOURCE_INFO, "T"),
                                                                                                  new TypeVariable(JExprParser.NO_SOURCE_INFO, "U"))}, 
                                                          SymbolData.VOID_TYPE, 
                                                          new VariableData[] { new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, true, null),
                                                            new VariableData("field1", _finalMav, SymbolData.INT_TYPE, true, null) }, 
                                                          new String[] { "X" },
                                                          _sd1,
                                                          null);
                                                          
                                                          mdata.getParams()[0].setEnclosingData(mdata);
                                                          mdata.getParams()[1].setEnclosingData(mdata);


           MethodData expectedMethod = _av.createMethodData(mdef, _sd1);
           
           // have to add the parameters to the vars manually
           mdata.getParams()[0].setEnclosingData(expectedMethod);
           mdata.getParams()[1].setEnclosingData(expectedMethod);
           mdata.addVars(new VariableData[] { new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, true, expectedMethod) });  
           assertEquals("Should return the correct MethodData", mdata, expectedMethod);
           assertEquals("There should be 2 errors.", 2, errors.size());
           assertEquals("The second error message should be correct.", "You cannot have two method parameters with the same name", errors.get(1).getFirst());
    }
    
    public void testSimpleAnonymousClassInstantiationHelper() {
     SimpleAnonymousClassInstantiation basic = new SimpleAnonymousClassInstantiation(JExprParser.NO_SOURCE_INFO, new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Object", new Type[0]), 
                                                                        new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0]),
                                                                        new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
    

     _av._package = "i.like";
     _av.simpleAnonymousClassInstantiationHelper(basic, _sd1);
     assertEquals("There should be no errors", 0, errors.size());
     SymbolData obj = symbolTable.get("java.lang.Object");
     assertNotNull("Object should be in the symbol table", obj);
     assertEquals("sd1 should have one inner class", 1, _sd1.getInnerClasses().size());
     SymbolData inner = _sd1.getInnerClasses().get(0);
     assertEquals("The inner class should have the proper name", "i.like.monkey$1", inner.getName());
     assertEquals("The inner class should have proper outer data", _sd1, inner.getOuterData());
     assertEquals("The inner class should have proper super class", obj, inner.getSuperClass());
     assertEquals("The inner class should have the right package", "i.like", inner.getPackage());
     assertEquals("The inner class should have 0 methods", 0, inner.getMethods().size());
    }

    
    public void testComplexAnonymousClassInstantiationHelper() {
     ComplexAnonymousClassInstantiation basic = new ComplexAnonymousClassInstantiation(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "java.lang.Object")),
                                                                                new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Inner", new Type[0]), 
                                                                                new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0]),
                                                                                new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

     _av._package = "i.like";
     _av.complexAnonymousClassInstantiationHelper(basic, _sd1);
     assertEquals("There should be no errors", 0, errors.size());
     SymbolData obj = symbolTable.get("java.lang.Object");
     assertNotNull("Object should be in the symbol table", obj);
     SymbolData objInner = symbolTable.get("java.lang.Object.Inner");
     assertEquals("sd1 should have one inner class", 1, _sd1.getInnerClasses().size());
     SymbolData inner = _sd1.getInnerClasses().get(0);
     assertEquals("The inner class should have the proper name", "i.like.monkey$1", inner.getName());
     assertEquals("The inner class should have proper outer data", _sd1, inner.getOuterData());
     assertEquals("The inner class should have null as its super class", null, inner.getSuperClass());
     assertEquals("The inner class should have the right package", "i.like", inner.getPackage());
     assertEquals("The inner class should have 0 methods", 0, inner.getMethods().size());
    }

    public void testForVariableDeclaration() {
      //make sure that if forVariableDeclaration is called with a AnonymousClassInstantiation, the symboldata is only added once.
      //this is to make sure an old bug stays fixed.
      SimpleAnonymousClassInstantiation basic = new SimpleAnonymousClassInstantiation(JExprParser.NO_SOURCE_INFO, new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Object", new Type[0]), 
                                                                        new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0]),
                                                                        new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
    

     
     VariableDeclarator[] d1 = {new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), new Word(JExprParser.NO_SOURCE_INFO, "b"), basic)};
     VariableDeclaration vd1 = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,_publicMav, d1); 
     
     ClassBodyAdvancedVisitor cbav = new ClassBodyAdvancedVisitor(_sd1, _av._file, _av._package, _av._importedFiles, _av._importedPackages, _av._classNamesInThisFile, _av.continuations);
     
     vd1.visit(cbav);
     assertEquals("Should be 1 inner class of _sd1", 1, _sd1.getInnerClasses().size());
     
     
      
      
    }

   
  }
  
}