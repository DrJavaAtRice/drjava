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
import edu.rice.cs.javalanglevels.parser.*;
import edu.rice.cs.javalanglevels.util.*;

import java.util.*;
import java.io.*;

import junit.framework.TestCase;

/*
 * Language Level Visitor that represents the Intermediate Language Level.  Enforces constraints during the
 * first walk of the AST (checking for langauge specific errors and building the symbol table).
 * This class enforces things that are common to all contexts reachable within a class body at 
 * the Intermediate Language Level. 
 */
public class ClassBodyIntermediateVisitor extends IntermediateVisitor {
  
  /** The SymbolData corresponding to this class. */
  private SymbolData _enclosing;
  
  /** Constructor for ClassBodyAdvancedVisitor.
    * @param sd  The SymbolData that encloses the context we are visiting.
    * @param file  The source file this came from.
    * @param packageName  The package the source file is in
    * @importedFiles  A list of classes that were specifically imported
    * @param importedPackages  A list of package names that were specifically imported
    * @param classesInThisFile  A list of the classes that are yet to be defined in this source file
    * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need to 
    *                       be resolved
    * @param fixUps  A list of commands to be performed after this pass to fixup the symbolTable
    */
  public ClassBodyIntermediateVisitor(SymbolData sd,
                                      String className,
                                      File file, 
                                      String packageName, 
                                      LinkedList<String> importedFiles, 
                                      LinkedList<String> importedPackages, 
                                      HashSet<String> classesInThisFile, 
                                      Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations,
                                      LinkedList<Command> fixUps) {
    super(file,  packageName, className, importedFiles, importedPackages, classesInThisFile, continuations, fixUps);    
    _enclosing = sd;
  }
  
  /*Add an appropriate error*/
  public Void forStatementDoFirst(Statement that) {
    _addError("Statements cannot appear outside of method bodies", that);
    return null;
  }
  
  /** Ignore AbstractMake sure that this abstract method def is declared to be abstract. */
  public Void forAbstractMethodDefDoFirst(AbstractMethodDef that) {

//    ModifiersAndVisibility mav = that.getMav();
//    String[] modifiers = mav.getModifiers();
    if (! _enclosing.isInterface() && ! _enclosing.hasModifier("abstract")) { // interfaces not yet marked abstract
      _addError("Abstract methods can only be declared in abstract classes", that);
    }
    return super.forAbstractMethodDefDoFirst(that);
  }

  /**Add an appropriate error*/
  public Void forInstanceInitializerDoFirst(InstanceInitializer that) {
    _addError("This open brace must mark the beginning of a method or class body", that);
    return null;
  }
    
  /** Processes a field declaration. Converts VariableDeclaration to VariableData[].  Ensures that no fields are 
    * declared to be abstract. Finally, adds the variable datas to the symbol data, and emits an 
    * error if two fields have the same names. */
  public Void forVariableDeclarationOnly(VariableDeclaration that) {
//    System.err.println("Calling _variableDeclaration2VariableData on " + that);
    VariableData[] vds = _variableDeclaration2VariableData(that, _enclosing);
//    System.err.println("Constructed vds array = " + Arrays.toString(vds));
    // make sure that every non-static field is private and no static field are uninitialized:
//    LinkedList<VariableData> vdsList = new LinkedList<VariableData>();
    for (int i = 0; i < vds.length; i++) {
      if (! vds[i].isStatic()) vds[i].setPrivate();
      else if (that.getDeclarators()[i] instanceof UninitializedVariableDeclarator) {  
        _addAndIgnoreError("All static fields must be initialized", that);
      }
// TODO: where is abstract check?     
    }
//    System.err.println("Processed vds array = " + Arrays.toString(vds));
    if (! _enclosing.addFinalVars(vds /* vdsList.toArray(new VariableData[vdsList.size()]) */)) {
//      System.err.println("Duplicate variable declaration found");
      _addAndIgnoreError("You cannot have two fields with the same name.  Either you already have a field by that "
                           + "name in this class, or one of your superclasses or interfaces has a field by that name", 
                         that);
    }
    return null;
  }

  // TODO: lift the following two methods to LLV since they are identical in ClassBodyFullJavaVisitor. 
  
  /** Call the method in FullJavaVisitor since it's common to this and FullJavaBodyFullJavaVisitor. */
  public Void forInnerInterfaceDef(InnerInterfaceDef that) {
    String relName = that.getName().getText();
    String innerClassName = getQualifiedClassName(_enclosing.getName()) + '.' + relName; 
    handleInnerInterfaceDef(that, _enclosing, relName, innerClassName);
    return null;
  }
  /** Process a local inner class definition */
  public Void forInnerClassDef(InnerClassDef that) {
//    System.err.println("CBIV.forInnerClassDef called on " + that.getName());
    String relName = that.getName().getText();
    String innerClassName = getQualifiedClassName(_enclosing.getName()) + '.' + relName;
    handleInnerClassDef(that, _enclosing, relName, innerClassName);
    return null;
  }
  
  /** Creates a method data corresponding to this method declaration, and then visit the concrete method def with a new 
    * bodybody visitor, passing it the enclosing method data. Methods are still public by default, but this can be 
    * overridden by the user. Make sure the method name is different from the class name.
    */
  public Void forConcreteMethodDef(ConcreteMethodDef that) {
    forConcreteMethodDefDoFirst(that);
    if (prune(that)) return null;
    MethodData md = createMethodData(that, _enclosing);
    
    // At Intermediate Level, methods are still public by default.
    if (! md.hasModifier("public") && ! md.hasModifier("private") && ! md.hasModifier("protected")) {
      md.addModifier("public");
    }

    String className = getUnqualifiedClassName(_enclosing.getName());
    if (className.equals(md.getName())) {
      _addAndIgnoreError("Only constructors can have the same name as the class they appear in, and constructors do "
                           + "not have an explicit return type",
                         that);
    }
    else _enclosing.addMethod(md);

    that.getBody().visit(new BodyBodyIntermediateVisitor(md, _file, _package, _enclosingClassName, _importedFiles, 
                                                         _importedPackages, _classesInThisFile, continuations, 
                                                         fixUps, new HashSet<String>()));
    return forConcreteMethodDefOnly(that);
  }
  
  /** Create a method data corresponding to this method declaration.
    * Make sure the method name is different from the class name.
    * At the Intermediate Level, methods are public by default, but this can be overridden by the user.
    */
  public Void forAbstractMethodDef(AbstractMethodDef that) {
    forAbstractMethodDefDoFirst(that);
    if (prune(that)) return null;
    MethodData md = createMethodData(that, _enclosing);
    
    // At Intermediate Level, methods are still public by default.
    if (! md.hasModifier("public") && ! md.hasModifier("private") && ! md.hasModifier("protected")) {
      md.addModifier("public");
    }

    String className = getUnqualifiedClassName(_enclosing.getName());
    if (className.equals(md.getName())) {
      _addAndIgnoreError("Only constructors can have the same name as the class they appear in, and constructors do "
                           + "not have an explicit return type",
                         that);
    }
    else _enclosing.addMethod(md);

    return forAbstractMethodDefOnly(that);
  }
  
  /** Create a constructor corresponding to the specifications in the ConstructorDef.  */
  public Void forConstructorDef(ConstructorDef that) {
    forConstructorDefDoFirst(that);
    if (prune(that)) return null;

    that.getMav().visit(this);
    String name = getUnqualifiedClassName(that.getName().getText());
    if (! name.equals(getUnqualifiedClassName(_enclosing.getName()))) {
      _addAndIgnoreError("The constructor return type and class name must match", that);
    }

    // Turn the thrown exceptions from a ReferenceType[] to a String[]
    String[] throwStrings = referenceType2String(that.getThrows());
    
    SymbolData returnType = _enclosing;
    MethodData md = new MethodData(name, that.getMav(), new TypeParameter[0], returnType, 
                                   new VariableData[0], throwStrings, _enclosing, that);  // VariableData is dummy
    
    // At Intermediate Level, constructors are still public by default.
    if (! md.hasModifier("public") && ! md.hasModifier("private") && ! md.hasModifier("protected")) {
      md.addModifier("public");
    }
    
    _checkError(); // reset check flag
    
    // Turn the parameters from a FormalParameterList to a VariableData[]
    VariableData[] vds = formalParameters2VariableData(that.getParameters(), _enclosing);
    if (! _checkError()) {  // if there was an error converting the formalParameters, don't use them.
      md.setParams(vds);
      if (! md.addFinalVars(vds)) {
        _addAndIgnoreError("You cannot have two method parameters with the same name", that);
      }
    }
    
    _enclosing.addMethod(md);
    that.getStatements().visit(new BodyBodyIntermediateVisitor(md, _file, _package, _enclosingClassName, _importedFiles,
                                                               _importedPackages, _classesInThisFile, continuations, 
                                                               fixUps, new HashSet<String>()));
    //note that we have seen a constructor.
    _enclosing.incrementConstructorCount();
    return forConstructorDefOnly(that);
  }
  
  /** Delegate to method in LLV. */
  public Void forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    complexAnonymousClassInstantiationHelper(that, _enclosing);   // TODO: the wrong enclosing context?
    return null;
  }

  /** Delegate to method in LLV. */
  public Void forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    simpleAnonymousClassInstantiationHelper(that, _enclosing);
    return null;
  }
    
//  /** Check for problems with modifiers that are specific to method definitions. */
//  public Void forModifiersAndVisibilityDoFirst(ModifiersAndVisibility that) {
//    String[] modifiers = that.getModifiers();
//    if (Utilities.isAbstract(modifiers) && Utilities.isStatic(modifiers)) _badModifiers("static", "abstract", that);
//    return super.forModifiersAndVisibilityDoFirst(that);
//  }
  
  /** Test the methods in the above (enclosing) class. */
  public static class ClassBodyIntermediateVisitorTest extends TestCase {
    
    private ClassBodyIntermediateVisitor _cbiv;
    
    private SymbolData _sd1;
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
    private ModifiersAndVisibility _publicFinalMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public", "final"});
    private ModifiersAndVisibility _protectedMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[0]);
    private ModifiersAndVisibility _abstractMav =
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final"});
    private ModifiersAndVisibility _staticMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"static"});
    private ModifiersAndVisibility _abstractStaticMav =
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract", "static"});
    private ModifiersAndVisibility _privateAbstractMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"private", "abstract"});
    private ModifiersAndVisibility _staticFinalMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"static", "final"});
     private ModifiersAndVisibility _finalStaticMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final", "static"});
    private ModifiersAndVisibility _finalPrivateMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final", "private"});
    private ModifiersAndVisibility _privateFinalMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"private", "final"});
    
    
    public ClassBodyIntermediateVisitorTest() { this(""); }
    public ClassBodyIntermediateVisitorTest(String name) { super(name); }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");

      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable.clear();
      LanguageLevelConverter._newSDs.clear();
      LanguageLevelConverter.symbolTable.put("i.like.monkey", _sd1);
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
//      _hierarchy = new Hashtable<String, TypeDefBase>();
      _cbiv = new ClassBodyIntermediateVisitor(_sd1,
                                               _sd1.getName(),
                                               new File(""), 
                                               "", 
                                               new LinkedList<String>(), 
                                               new LinkedList<String>(), 
                                               new HashSet<String>(), 
                                               new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                               new LinkedList<Command>());
      _cbiv.continuations = new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(); // no _sd1
      _cbiv._classesInThisFile = new HashSet<String>();
//      _cbiv._resetNonStaticFields();
      _cbiv._importedPackages.addFirst("java.lang");
      _errorAdded = false;
    }
    
    public void testForConcreteMethodDefDoFirst() {
      // Check one that works
      ConcreteMethodDef cmd = new ConcreteMethodDef(SourceInfo.NONE, 
                                                    _privateMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(SourceInfo.NONE, "int"), 
                                                    new Word(SourceInfo.NONE, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      cmd.visit(_cbiv);
      assertEquals("There should not be any errors", 0, errors.size());
      
      
      
      // Check one that doesn't work because it is declared abstract but is actually a concrete method
      ConcreteMethodDef cmd2 = new ConcreteMethodDef(SourceInfo.NONE, 
                                                     _abstractMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NONE, "double"), 
                                                     new Word(SourceInfo.NONE, "methodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0], 
                                                     new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      cmd2.visit(_cbiv);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "Methods that have a braced body cannot be declared \"abstract\"", 
                   errors.get(0).getFirst());
      
//      // Check one that doesn't work because it is static
//      ConcreteMethodDef cmd3 = new ConcreteMethodDef(SourceInfo.NONE, 
//                                                     _staticMav, 
//                                                     new TypeParameter[0], 
//                                                     new PrimitiveType(SourceInfo.NONE, "double"), 
//                                                     new Word(SourceInfo.NONE, "methodName"),
//                                                     new FormalParameter[0],
//                                                     new ReferenceType[0], 
//                                                     new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
//      cmd3.visit(_cbiv);
//      assertEquals("There should be two errors", 2, errors.size());
//      assertEquals("The error message should be correct", 
//                   "Static methods cannot be used at the Intermediate level", 
//                   errors.get(1).getFirst());
//      
      
    }
    
    public void testForAbstractMethodDefDoFirst() {
      // Check one that doesn't work
      AbstractMethodDef amd = new AbstractMethodDef(SourceInfo.NONE, 
                                                    _abstractMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(SourceInfo.NONE, "int"), 
                                                    new Word(SourceInfo.NONE, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0]);
      amd.visit(_cbiv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "Abstract methods can only be declared in abstract classes", 
                   errors.get(0).getFirst());
      
      // Check one that works
      _cbiv._enclosing.setMav(_abstractMav);
      AbstractMethodDef amd2 = new AbstractMethodDef(SourceInfo.NONE, 
                                                     _abstractMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NONE, "double"), 
                                                     new Word(SourceInfo.NONE, "methodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd2.visit(_cbiv);
      assertEquals("There should still be one error", 1, errors.size());
      
//      // Check one that doesn't work because it is static
//      AbstractMethodDef amd3 = new AbstractMethodDef(SourceInfo.NONE, 
//                                                     _abstractStaticMav, 
//                                                     new TypeParameter[0], 
//                                                     new PrimitiveType(SourceInfo.NONE, "double"), 
//                                                     new Word(SourceInfo.NONE, "methodName"),
//                                                     new FormalParameter[0],
//                                                     new ReferenceType[0]);
//      amd3.visit(_cbiv);
//      assertEquals("There should be two errors", 2, errors.size());
//      assertEquals("The error message should be correct", "Static methods cannot be used at the Intermediate level", 
//                   errors.get(1).getFirst());
    }

    public void testForInstanceInitializerDoFirst() {
      InstanceInitializer ii = new InstanceInitializer(SourceInfo.NONE, 
                                                       new Block(SourceInfo.NONE, 
                                                                 new BracedBody(SourceInfo.NONE, new BodyItemI[0])));
      ii.visit(_cbiv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "This open brace must mark the beginning of a method or class body", 
                   errors.get(0).getFirst());
    }
    
    /* These test is shared with BodyIntermediateVisitor,
     * perhaps we could factor it out. */
    
    public void testForVariableDeclarationOnly() {
      // Check one that works
      VariableDeclaration vdecl = new VariableDeclaration(SourceInfo.NONE,
                                                          _packageMav,
                                                          new VariableDeclarator[] {
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new PrimitiveType(SourceInfo.NONE, "double"), 
                                            new Word (SourceInfo.NONE, "field1")),
          new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                              new PrimitiveType(SourceInfo.NONE, "boolean"), 
                                              new Word (SourceInfo.NONE, "field2"))});
      VariableData vd1 = 
        new VariableData("field1", _privateFinalMav, SymbolData.DOUBLE_TYPE, false, _cbiv._enclosing);
      VariableData vd2 = 
        new VariableData("field2", _privateFinalMav, SymbolData.BOOLEAN_TYPE, false, _cbiv._enclosing);
      vdecl.visit(_cbiv);
      
//      VariableData vd0 = _sd1.getVars().get(0);
//      System.err.println("Errors were: " + errors);
      assertEquals("There should not be any errors.", 0, errors.size());
//      System.err.println("_sd1.getVars() = " + _sd1.getVars());
//      
//      System.err.println("vd1 = " + vd1);
//      System.err.println("vd1.getMav() = " + vd1.getMav());
//      System.err.println("vd1.getType() = " + vd1.getType());
//      System.err.println("vd1.getMav().getModifiers() = " + vd1.getMav().getModifiers());
//      assertEquals("enclosingData are equal", vd1.getEnclosingData(), vd0.getEnclosingData());
//      assertEquals("mavs are equal", vd1.getMav(), vd0.getMav());
//      assertEquals("vd1.equals(vd0)", vd1, vd0);

//      System.err.println("_sd1 vars =  " + _sd1.getVars());
//      System.err.println("vd1 = " + vd1 + "; vd2 = " + vd2);
      assertTrue("field1 was added.", _sd1.getVars().contains(vd1));
      assertTrue("field2 was added.", _sd1.getVars().contains(vd2));
      
      //TODO: Test that static fields are made public
      
      // Check one that doesn't work
      VariableDeclaration vdecl2 = new VariableDeclaration(SourceInfo.NONE,
                                                        _packageMav,
                                                        new VariableDeclarator[] {
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new PrimitiveType(SourceInfo.NONE, "double"), 
                                            new Word (SourceInfo.NONE, "field3")),
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new PrimitiveType(SourceInfo.NONE, "int"), 
                                            new Word (SourceInfo.NONE, "field3"))});
      VariableData vd3 = 
        new VariableData("field3", _privateFinalMav, SymbolData.DOUBLE_TYPE, false, _cbiv._enclosing);
      vdecl2.visit(_cbiv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "You cannot have two fields with the same name.  Either you already have a field by that name in "
                     + "this class, or one of your superclasses or interfaces has a field by that name", 
                   errors.get(0).getFirst());
//      System.err.println("_sd1 vars =  " + _sd1.getVars());
//      System.err.println("vd3 = " + vd3);
      assertTrue("field3 was added.", _sd1.getVars().contains(vd3));
      
      //Check a static field that has not been assigned is an error
      VariableDeclaration vdecl3 = new VariableDeclaration(SourceInfo.NONE,
                                                           _staticMav,
                                                           new VariableDeclarator[] {
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new PrimitiveType(SourceInfo.NONE, "double"), 
                                            new Word (SourceInfo.NONE, "field4"))});
      VariableData vd4 = 
        new VariableData("field4", _staticFinalMav, SymbolData.DOUBLE_TYPE, false, _cbiv._enclosing);  
      
      vdecl3.visit(_cbiv);
//      System.err.println("vd4 = " + vd4);;
//      assertEquals("There should still be one error", 1, errors.size());
      assertEquals("The error message should be correct", "All static fields must be initialized", 
                   errors.get(1).getFirst());
//      System.err.println("_sd1 vars =  " + _sd1.getVars());
      assertTrue("field4 was added.", _sd1.getVars().contains(vd4));   
      
      // Check a non-static field that has been assigned.
      VariableDeclaration vdecl5 = new VariableDeclaration(SourceInfo.NONE,
                                                        _packageMav,
                                                        new VariableDeclarator[] {
        new InitializedVariableDeclarator(SourceInfo.NONE, 
                                            new PrimitiveType(SourceInfo.NONE, "double"), 
                                            new Word (SourceInfo.NONE, "field5"), 
                                          new DoubleLiteral(SourceInfo.NONE, 2.4))});
      vdecl5.visit(_cbiv);
      VariableData vd5 = 
        new VariableData("field5", _privateFinalMav, SymbolData.DOUBLE_TYPE, true, _cbiv._enclosing);
      vd5.setHasInitializer(true);
//      VariableData vd0 = _sd1.getVars().get(4);
//      System.err.println("vd5 = " + vd5);
//      System.err.println("vd0 = " + vd0);
//      System.err.println("vd5.getMav() = " + vd5.getMav());
//      System.err.println("vd5.getType() = " + vd5.getType());
//      System.err.println("vd0.getMav() = " + vd0.getMav());
//      System.err.println("vd0.getType() = " + vd0.getType());
//      assertEquals("mavs are equal", vd5.getMav(), vd0.getMav());
//      assertEquals("enclosingData are equal", vd5.getEnclosingData(), vd0.getEnclosingData());
//      assertEquals("vd5.equals(vd0)", vd5, vd0);
      assertTrue("Field 5 was added.", _sd1.getVars().contains(vd5));
      
      // Check one that overrides the super class's field
      VariableDeclaration vdecl6 = new VariableDeclaration(SourceInfo.NONE,
                                                       _packageMav,
                                                           new VariableDeclarator[] {
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new PrimitiveType(SourceInfo.NONE, "double"), 
                                            new Word (SourceInfo.NONE, "field6"))});
      
      
      VariableData vd6 = 
        new VariableData("field6", _finalPrivateMav, SymbolData.DOUBLE_TYPE, false, _cbiv._enclosing);
      SymbolData myData = new SymbolData("myData");
      myData.addVar(vd6);
      _cbiv._enclosing.setSuperClass(myData);
      vdecl6.visit(_cbiv);
      assertEquals("There should be three errors.", 3, errors.size());
      assertEquals("The error message should be correct", "You cannot have two fields with the same name.  Either you" +
                   " already have a field by that name in this class, or one of your superclasses or interfaces has a" +
                   " field by that name", 
                   errors.getLast().getFirst());

    }
    
    public void testFormalParameters2VariableData() {
      FormalParameter[] fps = new FormalParameter[] {
        new FormalParameter(SourceInfo.NONE, 
                            new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                                              new PrimitiveType(SourceInfo.NONE, "double"), 
                                                              new Word (SourceInfo.NONE, "field1")),
                            false),
        new FormalParameter(SourceInfo.NONE, 
                            new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                                              new PrimitiveType(SourceInfo.NONE, "boolean"), 
                                                              new Word (SourceInfo.NONE, "field2")),
                            false)};

      MethodData md = new MethodData("methodName", 
                                     _packageMav, 
                                     new TypeParameter[0], 
                                     SymbolData.INT_TYPE, 
                                     new VariableData[0],  // a dummy value
                                     new String[0],
                                     _sd1,  // enclosing class
                                     null); // no SourceInfo
      VariableData vd1 = new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, true, _sd1);
      VariableData vd2 = new VariableData("field2", _finalMav, SymbolData.BOOLEAN_TYPE, true, _sd1);
//      System.err.println("vd1 = " + vd1);
//      System.err.println("vd2 = " + vd2);
      VariableData[] vds = _cbiv.formalParameters2VariableData(fps, _sd1);
      assertEquals("There should not be any errors.", 0, errors.size());
//      System.err.println("vds[0] = " + vds[0]);
//      System.err.println("vds[1] = " + vds[1]);
      assertEquals("vd1 should be the first entry in vds.", vd1, vds[0]);
      assertEquals("vd2 should be the second entry in vds.", vd2, vds[1]);
    }
    

    

    
    public void testForConcreteMethodDef() {
      // Test one that works.
      MethodDef mdef = new ConcreteMethodDef(SourceInfo.NONE, 
                                             _privateMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(SourceInfo.NONE, "int"), 
                                             new Word(SourceInfo.NONE, "methodName"),
                                             new FormalParameter[0],
                                             new ReferenceType[0], 
                                             new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      mdef.visit(_cbiv);
      assertEquals("There should not be any errors.", 0, errors.size());

      
      //Check one that works but needs to be augmented with public
      ConcreteMethodDef cmd1 = new ConcreteMethodDef(SourceInfo.NONE,
                                                    _packageMav,
                                                    new TypeParameter[0],
                                                    new PrimitiveType(SourceInfo.NONE, "int"),
                                                    new Word(SourceInfo.NONE, "noMavMethod"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0],
                                                    new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      cmd1.visit(_cbiv);
      assertEquals("There should not be any errors", 0, errors.size());
      assertEquals("_sd1 should contain 2 methods", 2, _sd1.getMethods().size());
      assertTrue("The second method should be default public", _sd1.getMethods().get(1).hasModifier("public"));

      
      
      // Test one that doesn't work.
      mdef = new ConcreteMethodDef(SourceInfo.NONE, 
                                             _packageMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(SourceInfo.NONE, "int"), 
                                             new Word(SourceInfo.NONE, "monkey"),
                                             new FormalParameter[0],
                                             new ReferenceType[0], 
                                             new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      mdef.visit(_cbiv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Only constructors can have the same name as the class they appear in, and constructors do not "
                     + "have an explicit return type",
                   errors.get(0).getFirst());
    }
    
    public void testForAbstractMethodDef() {
      // Test one that works but needs to be augmented with public.
      MethodDef mdef = new AbstractMethodDef(SourceInfo.NONE, 
                                             _abstractMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(SourceInfo.NONE, "int"), 
                                             new Word(SourceInfo.NONE, "methodName"),
                                             new FormalParameter[0],
                                             new ReferenceType[0]);
      _cbiv._enclosing.setMav(_abstractMav);

      mdef.visit(_cbiv);
      assertEquals("There should not be any errors", 0, errors.size());
      assertEquals("_sd1 should contain 1 methods", 1, _sd1.getMethods().size());
      assertTrue("The method should be default public", _sd1.getMethods().getFirst().hasModifier("public"));

      
      
      
      // Test one that doesn't work.
      mdef = new AbstractMethodDef(SourceInfo.NONE, 
                                             _abstractMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(SourceInfo.NONE, "int"), 
                                             new Word(SourceInfo.NONE, "monkey"),
                                             new FormalParameter[0],
                                             new ReferenceType[0]);
      mdef.visit(_cbiv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Only constructors can have the same name as the class they appear in, " +
                   "and constructors do not have an explicit return type",
                   errors.get(0).getFirst());
    }
    
    /* These test is shared with BodyIntermediateVisitor,
     * perhaps we could factor it out. */
    
//    public void testForOtherExpressionOnly() {
//      // Test that if the OtherExpression contains a Word, that the Word is resolved.
//      assertFalse("java.lang.System should not be in the symbolTable.", symbolTable.containsKey("java.lang.System"));
//      Expression ex = new Expression( SourceInfo.NONE,
//                                     new ExpressionPiece[] { new OtherExpression(SourceInfo.NONE, 
//                                                                                 new Word(SourceInfo.NONE,
//                                                                                          "System"))});
//      ex.visit(_cbiv);
//      assertEquals("There should not be any errors.", 0, errors.size());
//      assertTrue("java.lang.System should be in the symbolTable.", symbolTable.containsKey("java.lang.System"));
//    }
    
    public void testForInitializedVariableDeclaratorDoFirst() {
      InitializedVariableDeclarator ivd = 
        new InitializedVariableDeclarator(SourceInfo.NONE,
                                          new PrimitiveType(SourceInfo.NONE, "int"),
                                          new Word(SourceInfo.NONE, "i"),
                                          new IntegerLiteral(SourceInfo.NONE, 5));
      
      ivd.visit(_cbiv);
      
      assertEquals("There should be no errors now", 0, errors.size());
//      assertEquals("Error message should be correct",
//                   "Cannot initialize a class's fields at the Intermediate level.  To set the value of a field, when" +
//                   "you instantiate the class, assign the desired value using the class's constructor",
//                   errors.get(0).getFirst());
    }


    public void testForInnerInterfaceDef() {
      InnerInterfaceDef cd1 = new InnerInterfaceDef(SourceInfo.NONE, _packageMav, 
                                                    new Word(SourceInfo.NONE, "Bart"),
                                       new TypeParameter[0], new ReferenceType[0], 
                                       new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      InnerInterfaceDef cd0 = new InnerInterfaceDef(SourceInfo.NONE, _packageMav, 
                                                    new Word(SourceInfo.NONE, "Lisa"),
                                       new TypeParameter[0], new ReferenceType[0], 
                                            new BracedBody(SourceInfo.NONE, new BodyItemI[] {cd1}));
      
      SymbolData sd0 = new SymbolData(_cbiv._enclosing.getName() + "$Lisa", _packageMav, new TypeParameter[0], 
                                      new ArrayList<SymbolData>(), null); 
      SymbolData sd1 = new SymbolData(_cbiv._enclosing.getName() + "$Lisa$Bart", _packageMav, new TypeParameter[0], 
                                      new ArrayList<SymbolData>(), null);
      sd0.addInnerInterface(sd1);
      sd0.setIsContinuation(true);
      sd1.setIsContinuation(true);
      
      LanguageLevelConverter.symbolTable.put(_cbiv._enclosing.getName() + "$Lisa", sd0);
      LanguageLevelConverter.symbolTable.put(_cbiv._enclosing.getName() + "$Lisa$Bart", sd1);

      cd0.visit(_cbiv);

      SymbolData sd = _cbiv._enclosing.getInnerClassOrInterface("Lisa");

      // NOTE: No longer allowing inner interfaces at the intermediate level
      assertEquals("There should be no errors", 0, errors.size());
      // Nested interfaces now work
    }
    
    public void testForConstructorDef() {
      //this is a ConstructorDef with no formal parameters and no throws
      ConstructorDef cd = new ConstructorDef(SourceInfo.NONE, 
                                             new Word(SourceInfo.NONE, "MyClass"), _publicMav, 
                                             new FormalParameter[0], new ReferenceType[0], 
                                             new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      //What if constructor name and SymbolData name don't match?  Should throw an error.
      _cbiv._enclosing = new SymbolData("NotRightName");
      cd.visit(_cbiv);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "The constructor return type and class name must match", errors.getLast().getFirst());
      
      //If they are the same, it should work just fine.
      _cbiv._enclosing = new SymbolData("MyClass");
      
      MethodData constructor = new MethodData("MyClass", _publicMav, new TypeParameter[0], _cbiv._enclosing, 
                                              new VariableData[0], 
                                              new String[0], 
                                              _cbiv._enclosing,
                                              null);
      
      
      cd.visit(_cbiv);
      
      
      assertEquals("Should still be 1 error", 1, errors.size());
      assertEquals("SymbolData should have 1 method", 1, _cbiv._enclosing.getMethods().size());
      assertTrue("SymbolData's constructor should be correct", _cbiv._enclosing.getMethods().contains(constructor));
      
      //With a ConstructorDef with more throws and variables, should work okay.
      FormalParameter fp = 
        new FormalParameter(SourceInfo.NONE, 
                            new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                                                new PrimitiveType(SourceInfo.NONE, "int"), 
                                                                new Word(SourceInfo.NONE, "i")), false);
      ReferenceType rt = new TypeVariable(SourceInfo.NONE, "MyMadeUpException");
      ConstructorDef cd2 = new ConstructorDef(SourceInfo.NONE, 
                                              new Word(SourceInfo.NONE, "MyClass"), 
                                              _publicMav, 
                                              new FormalParameter[] {fp}, 
                                              new ReferenceType[] {rt}, 
                                              new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      VariableData vd = new VariableData("i", _finalMav, SymbolData.INT_TYPE, true, _cbiv._enclosing);
      MethodData constructor2 = new MethodData("MyClass", 
                                               _publicMav, 
                                               new TypeParameter[0], 
                                               _cbiv._enclosing, 
                                               new VariableData[] {vd}, 
                                               new String[] {"MyMadeUpException"}, 
                                               _cbiv._enclosing,
                                               null);

                                           
      constructor2.addVar(vd);
      cd2.visit(_cbiv);
//      vd.setEnclosingData(_cbiv._enclosing.getMethods().getLast());
      assertEquals("Should still be 1 error", 1, errors.size());
      assertEquals("SymbolData should have 2 methods", 2, _cbiv._enclosing.getMethods().size());
      
      assertTrue("SymbolData should have new constructor", _cbiv._enclosing.getMethods().contains(constructor2));
      
                                              
      //If two variable names are duplicated, should throw an error.
      FormalParameter fp2 = 
        new FormalParameter(SourceInfo.NONE, 
                            new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                                                new PrimitiveType(SourceInfo.NONE, "double"), 
                                                                new Word(SourceInfo.NONE, "i")), false);
      
      ConstructorDef cd3 = new ConstructorDef(SourceInfo.NONE, 
                                              new Word(SourceInfo.NONE, "MyClass"), _publicMav, 
                                              new FormalParameter[] {fp, fp2}, new ReferenceType[] {rt}, 
                                             new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      cd3.visit(_cbiv);
      
      assertEquals("Should now be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "You cannot have two method parameters with the same name", 
                   errors.getLast().getFirst());
    }    
  }
}