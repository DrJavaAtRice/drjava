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

/** Class body walking LanguageLevelVisitor for the FullJava Language Level. Builds the symbol table for a .java file
  * without performing any syntax checking.  This file will also be compiled by javac, which will catch the syntax
  * errors.
  */
public class ClassBodyFullJavaVisitor extends FullJavaVisitor {
  
  /**The SymbolData corresponding to this class.*/
  private SymbolData _enclosingData;
  
  /** Constructor for ClassBodyFullJavaVisitor.
    * @param sd  The SymbolData that encloses the context we are visiting.
    * @param className The name of the enclosing class
    * @param file  The source file this came from.
    * @param packageName  The package the source file is in
    * @importedFiles  A list of classes that were specifically imported
    * @param importedPackages  A list of package names that were specifically imported
    * @param classDefsInThisFile  A list of the classes that are defined in the source file
    * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need to be resolved
    */
  public ClassBodyFullJavaVisitor(SymbolData sd, 
                                  String className,
                                  File file, 
                                  String packageName,
                                  LinkedList<String> importedFiles, 
                                  LinkedList<String> importedPackages,
                                  LinkedList<String> classDefsInThisFile, 
                                  Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations) {
    super(file, packageName, importedFiles, importedPackages, classDefsInThisFile, continuations);
    _enclosingClassName = className;
    _enclosingData = sd;
  }

  /* Ignore ForStatement. */
  public Void forStatementDoFirst(Statement that) { return null; }
  
  /** Ignore ConcreteMethodDef. */
  
  /** Ignore AbstractMake sure that this abstract method def is declared to be abstract. */
  public Void forAbstractMethodDefDoFirst(AbstractMethodDef that) {

//    ModifiersAndVisibility mav = that.getMav();
//    String[] modifiers = mav.getModifiers();
    if (! _enclosingData.isInterface() && ! _enclosingData.hasModifier("abstract")) { // interfaces not yet marked abstract
      _addError("Abstract methods can only be declared in abstract classes", that);
    }
    return super.forAbstractMethodDefDoFirst(that);
  }

  /*Add an appropriate error*/
  public Void forInstanceInitializerDoFirst(InstanceInitializer that) {
    _addError("This open brace must mark the beginning of a method or class body", that);
    return null;
  }
  
  /** Convert the Variable declartaion to variable datas.  Add the variable datas to the symbol data, and give an error
    * if two fields have the same names
    */
  public Void forVariableDeclarationOnly(VariableDeclaration that) {
    VariableData[] vds = _variableDeclaration2VariableData(that, _enclosingData);

    // Add the variable datas to the symbol data
//    LinkedList<VariableData> vdsList = new LinkedList<VariableData>();
//    for (VariableData vd: vds) { vdsList.addLast(vd); }

    if (! _enclosingData.addVars(vds)) {
      _addAndIgnoreError("You cannot have two fields with the same name.  Either you already have a field by that " 
                           + "name in this class, or one of your superclasses or interfaces has a field by that name", 
                         that);
    }
    return null;
  }
  
  /** Create a method data corresponding to this method declaration, and then visit the
    * concrete method def with a new bodybody visitor, passing it the enclosing method data.
    * Make sure the method name is different from the class name.
    */
  public Void forConcreteMethodDef(ConcreteMethodDef that) {
    forConcreteMethodDefDoFirst(that);
    if (prune(that)) return null;
    
    MethodData md = createMethodData(that, _enclosingData);
    String className = getUnqualifiedClassName(_enclosingData.getName());
    
    if (className.equals(md.getName())) {
      _addAndIgnoreError("Only constructors can have the same name as the class they appear in, and constructors do not have an explicit return type",
                         that);
    }
    else {
      _enclosingData.addMethod(md);
    }
    that.getBody().visit(new BodyBodyFullJavaVisitor(md, _file, _package, _importedFiles, _importedPackages, 
                                                     _classNamesInThisFile, continuations, _innerClassesToBeParsed));
    return null;
  }

  /*Create a method data corresponding to this method declaration, and then visit the
   * abstract method def with a new bodybody visitor, passing it the enclosing method data.
   * Make sure the method name is different from the class name.
   */
  public Void forAbstractMethodDef(AbstractMethodDef that) {
    forAbstractMethodDefDoFirst(that);
    if (prune(that)) return null;

    MethodData md = createMethodData(that, _enclosingData);
    String className = getUnqualifiedClassName(_enclosingData.getName());
    if (className.equals(md.getName())) {
      _addAndIgnoreError("Only constructors can have the same name as the class they appear in, and constructors do "
                           + "not have an explicit return type",
                         that);
    }
    else _enclosingData.addMethod(md);
    return null;
  }
  
  /**Call the method in FullJavaVisitor since it's common to this and FullJavaBodyFullJavaVisitor. */
  public Void forInnerInterfaceDef(InnerInterfaceDef that) {
    handleInnerInterfaceDef(that, _enclosingData, 
                            getQualifiedClassName(_enclosingData.getName()) + "." + that.getName().getText());
    return null;
  }
  
  /**Call the method in FullJavaVisitor since it's common to this and FullJavaBodyFullJavaVisitor. */
  public Void forInnerClassDef(InnerClassDef that) {
    handleInnerClassDef(that, _enclosingData, getQualifiedClassName(_enclosingData.getName()) + "." + that.getName().getText());
    return null;
  }

  /**
   * Create a constructor corresponding to the specifications in the ConstructorDef, and then
   * visit the constructor body, passing the constructor as the enclosing data.
   */
  public Void forConstructorDef(ConstructorDef that) {
    forConstructorDefDoFirst(that);
    if (prune(that)) return null;
    
    that.getMav().visit(this);
    String name = getUnqualifiedClassName(that.getName().getText());
    if ((that.getName().getText().indexOf(".") != -1 && !that.getName().getText().equals(_enclosingData.getName())) || !name.equals(getUnqualifiedClassName(_enclosingData.getName()))) {
      _addAndIgnoreError("The constructor return type and class name must match", that);
    }

    // Turn the thrown exceptions from a ReferenceType[] to a String[]
    String[] throwStrings = referenceType2String(that.getThrows());
    
    SymbolData returnType = _enclosingData;
    MethodData md = MethodData.make(name, that.getMav(), new TypeParameter[0], returnType, 
                                   new VariableData[0], throwStrings, _enclosingData, that);

    _checkError(); // reset check flag
    // Turn the parameters from a FormalParameterList to a VariableData[]
    VariableData[] vds = formalParameters2VariableData(that.getParameters(), md);
    if (! _checkError()) {  //if there was an error converting the formalParameters, don't use them.
      md.setParams(vds);
      if (!md.addVars(vds)) {
        _addAndIgnoreError("You cannot have two method parameters with the same name", that);
      }
    }
    
    _enclosingData.addMethod(md);
    that.getStatements().visit(new BodyBodyFullJavaVisitor(md, _file, _package, _importedFiles, _importedPackages,
                                                           _classNamesInThisFile, continuations, 
                                                           _innerClassesToBeParsed));

    //note that we have seen a constructor.
    _enclosingData.incrementConstructorCount();
    return null;
  }
  
  /** Delegate to method in LanguageLevelVisitor */
  public Void forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    complexAnonymousClassInstantiationHelper(that, _enclosingData);
    return null;
  }

  /**Delegate to method in LanguageLevelVisitor */
  public Void forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    simpleAnonymousClassInstantiationHelper(that, _enclosingData);
    return null;
  }
   
  /** Check for problems with modifiers that are specific to method definitions. */
  public Void forModifiersAndVisibilityDoFirst(ModifiersAndVisibility that) {
    String[] modifiers = that.getModifiers();
//    System.err.println("***Checking for bad modifers in " + Arrays.toString(modifiers) + " isAbstact = " + 
//                       Utilities.isAbstract(modifiers) + " isStatic = " + Utilities.isStatic(modifiers));
    if (Utilities.isAbstract(modifiers) && Utilities.isStatic(modifiers))  _badModifiers("static", "abstract", that);
    return super.forModifiersAndVisibilityDoFirst(that);
  }
  
  /** Test the methods that are declared above. */
  public static class ClassBodyFullJavaVisitorTest extends TestCase {
    
    private ClassBodyFullJavaVisitor _cbfjv;
    
    private SymbolData _sd1;
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = 
      new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = 
      new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[0]);
    private ModifiersAndVisibility _abstractMav = 
      new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"final"});
    private ModifiersAndVisibility _staticMav = new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"static"});
    private ModifiersAndVisibility _abstractStaticMav = 
      new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"abstract", "static"});
    private ModifiersAndVisibility _finalStaticMav = 
      new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"static", "final"});
    
    
    public ClassBodyFullJavaVisitorTest() { this(""); }
    public ClassBodyFullJavaVisitorTest(String name) { super(name); }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");

      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable.clear();
      LanguageLevelConverter._newSDs.clear();
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      _hierarchy = new Hashtable<String, TypeDefBase>();
      _cbfjv = new ClassBodyFullJavaVisitor(_sd1, 
                                           "", 
                                           new File(""), 
                                           "", 
                                           new LinkedList<String>(), 
                                           new LinkedList<String>(), 
                                           new LinkedList<String>(), 
                                           new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
      _cbfjv._classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
      _cbfjv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      _cbfjv._resetNonStaticFields();
      _cbfjv._importedPackages.addFirst("java.lang");

      _errorAdded = false;
    }
    
    public void testForConcreteMethodDefDoFirst() {
      // Check one that works
      ConcreteMethodDef cmd = new ConcreteMethodDef(SourceInfo.NO_INFO, 
                                                    _publicMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(SourceInfo.NO_INFO, "int"), 
                                                    new Word(SourceInfo.NO_INFO, "methodName1"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      cmd.visit(_cbfjv);
      assertEquals("There should not be any errors", 0, errors.size());
      
      // Check one that doesn't work because it is declared abstract but is actually a concrete method
      ConcreteMethodDef cmd2 = new ConcreteMethodDef(SourceInfo.NO_INFO, 
                                                     _abstractMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NO_INFO, "double"), 
                                                     new Word(SourceInfo.NO_INFO, "methodName2"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0], 
                                                     new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      cmd2.visit(_cbfjv);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "Methods that have a braced body cannot be declared \"abstract\"", 
                   errors.get(0).getFirst());
      
      //Check that a static method does not result in an error.
      ConcreteMethodDef cmd3 = new ConcreteMethodDef(SourceInfo.NO_INFO, 
                                                     _staticMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NO_INFO, "double"), 
                                                     new Word(SourceInfo.NO_INFO, "methodName2"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0], 
                                                     new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      cmd3.visit(_cbfjv);
      assertEquals("There should still be one error", 1, errors.size());
      
      
    }
    
    public void testForAbstractMethodDefDoFirst() {
      // Check one that doesn't work
      AbstractMethodDef amd = new AbstractMethodDef(SourceInfo.NO_INFO, 
                                                    _abstractMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(SourceInfo.NO_INFO, "int"), 
                                                    new Word(SourceInfo.NO_INFO, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0]);
      amd.visit(_cbfjv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "Abstract methods can only be declared in abstract classes", 
                   errors.get(0).getFirst());
      
      // Check one that works
      _cbfjv._enclosingData.setMav(_abstractMav);
      AbstractMethodDef amd2 = new AbstractMethodDef(SourceInfo.NO_INFO, 
                                                     _abstractMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NO_INFO, "double"), 
                                                     new Word(SourceInfo.NO_INFO, "methodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd2.visit(_cbfjv);
      assertEquals("There should still be one error", 1, errors.size());
      
      // Check that static methods are now allowed at the FullJava level.
      AbstractMethodDef amd3 = new AbstractMethodDef(SourceInfo.NO_INFO, 
                                                     _abstractStaticMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NO_INFO, "double"), 
                                                     new Word(SourceInfo.NO_INFO, "methodName2"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd3.visit(_cbfjv);
      assertEquals("There should be two errors", 2, errors.size());
      assertEquals("The error message should be correct.", 
                   "Illegal combination of modifiers. Can't use static and abstract together.", 
                   errors.get(1).getFirst());
    }

    public void testForInstanceInitializerDoFirst() {
      InstanceInitializer ii = new InstanceInitializer(SourceInfo.NO_INFO, 
                                                       new Block(SourceInfo.NO_INFO, 
                                                                 new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0])));
      ii.visit(_cbfjv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "This open brace must mark the beginning of a method or class body", errors.get(0).getFirst());
    }
    
    /* These test is shared with BodyIntermediateVisitor, perhaps we could factor it out. */
    
    public void testForVariableDeclarationOnly() {
      // Check one that works
      VariableDeclaration vdecl = new VariableDeclaration(SourceInfo.NO_INFO,
                                                       _packageMav,
                                                       new VariableDeclarator[] {
        new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                               new PrimitiveType(SourceInfo.NO_INFO, "double"), 
                               new Word (SourceInfo.NO_INFO, "field1")),
        new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                               new PrimitiveType(SourceInfo.NO_INFO, "boolean"), 
                               new Word (SourceInfo.NO_INFO, "field2"))});
      VariableData vd1 = new VariableData("field1", _packageMav, SymbolData.DOUBLE_TYPE, true, _cbfjv._enclosingData);
      VariableData vd2 = new VariableData("field2", _packageMav, SymbolData.BOOLEAN_TYPE, true, _cbfjv._enclosingData);
      vdecl.visit(_cbfjv);
      assertEquals("There should not be any errors.", 0, errors.size());
      assertTrue("field1 was added.", _sd1.getVars().contains(vd1));
      assertTrue("field2 was added.", _sd1.getVars().contains(vd2));
      
      // Check one that doesn't work
      VariableDeclaration vdecl2 = 
        new VariableDeclaration(SourceInfo.NO_INFO,
                                _packageMav,
                                new VariableDeclarator[] {
        new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                            new PrimitiveType(SourceInfo.NO_INFO, "double"), 
                                            new Word(SourceInfo.NO_INFO, "field3")),
          new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                              new PrimitiveType(SourceInfo.NO_INFO, "int"), 
                                              new Word(SourceInfo.NO_INFO, "field3"))});
      VariableData vd3 = new VariableData("field3", _packageMav, SymbolData.DOUBLE_TYPE, true, _cbfjv._enclosingData);
      vdecl2.visit(_cbfjv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "You cannot have two fields with the same name.  Either you already have a field by that name in " 
                     + "this class, or one of your superclasses or interfaces has a field by that name", 
                   errors.get(0).getFirst());
      
//      System.err.println("_sd1 vars =  " + _sd1.getVars());
//      System.err.println("vd3 = " + vd3);
//      System.err.println("vd3.getMav() = " + vd3.getMav());
//      System.err.println("vd3.getType() = " + vd3.getType());
      assertTrue("field3 was added.", _sd1.getVars().contains(vd3));
      
      //Check a static field that has not been assigned (won't work)
      VariableDeclaration vdecl3 = new VariableDeclaration(SourceInfo.NO_INFO,
                                                           _staticMav,
                                                           new VariableDeclarator[] {
        new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                            new PrimitiveType(SourceInfo.NO_INFO, "double"), 
                                            new Word (SourceInfo.NO_INFO, "field4"))});
      // true value for hasAssigned in next line based on default initialization provided by Java.  GOOD IDEA?
      VariableData vd4 = new VariableData("field4", _staticMav, SymbolData.DOUBLE_TYPE, true, _cbfjv._enclosingData);
      vdecl3.visit(_cbfjv);
//      System.err.println("vd4 = " + vd4);
      assertEquals("There should still be one error", 1, errors.size());
//      assertEquals("The error message should be correct", "All static fields must be initialized", 
//                   errors.get(1).getFirst());
//      System.err.println("_sd1 vars =  " + _sd1.getVars());
      assertTrue("field4 was added.", _sd1.getVars().contains(vd4));
           
      //Check a non-static field that has been assigned.  (will work);
      VariableDeclaration vdecl5 = new VariableDeclaration(SourceInfo.NO_INFO,
                                                           _publicMav,
                                                           new VariableDeclarator[] {
        new InitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                          new PrimitiveType(SourceInfo.NO_INFO, "double"), 
                                          new Word(SourceInfo.NO_INFO, "field5"), 
                                          new DoubleLiteral(SourceInfo.NO_INFO, 2.4))});
      vdecl5.visit(_cbfjv);
      VariableData vd5 = new VariableData("field5", _publicMav, SymbolData.DOUBLE_TYPE, true, _cbfjv._enclosingData);
      vd5.setHasInitializer(true);
      assertEquals("There should still be one error", 1, errors.size());
      assertTrue("Field 5 was added.", _sd1.getVars().contains(vd5));
      
//      //check one that overrides the super class's field
//      VariableDeclaration vdecl6 = new VariableDeclaration(SourceInfo.NO_INFO,
//                                                           _packageMav,
//                                                           new VariableDeclarator[] {
//        new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
//                                            new PrimitiveType(SourceInfo.NO_INFO, "double"), 
//                                            new Word(SourceInfo.NO_INFO, "field6"))});
//      
//      
//      VariableData vd6 = new VariableData("field6", _packageMav, SymbolData.DOUBLE_TYPE, true, _cbfjv._enclosingData);
//      SymbolData myData = new SymbolData("myData");
//      myData.addVar(vd6);
//      _cbfjv._enclosingData.setSuperClass(myData);
//      vdecl6.visit(_cbfjv);
//      assertEquals("There should be two errors.", 2, errors.size());
//      assertEquals("The error message should be correct", 
//                   "You cannot have two fields with the same name.  Either you already have a field by that name in "
//                     + "this class, or one of your superclasses or interfaces has a field by that name", 
//                   errors.get(1).getFirst());
      
    }
    
    public void xtestFormalParameters2VariableData() {
      FormalParameter[] fps = new FormalParameter[] {
        new FormalParameter(SourceInfo.NO_INFO, 
                            new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                                              new PrimitiveType(SourceInfo.NO_INFO, "double"), 
                                                              new Word (SourceInfo.NO_INFO, "field1")),
                            false),
        new FormalParameter(SourceInfo.NO_INFO, 
                            new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                                              new PrimitiveType(SourceInfo.NO_INFO, "boolean"), 
                                                              new Word (SourceInfo.NO_INFO, "field2")),
                            false)};
      VariableData vd1 = new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, true, _cbfjv._enclosingData);
      VariableData vd2 = new VariableData("field2", _finalMav, SymbolData.BOOLEAN_TYPE, true, _cbfjv._enclosingData);
      VariableData[] vds = _cbfjv.formalParameters2VariableData(fps, _cbfjv._enclosingData);
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("vd1 should be the first entry in vds.", vd1, vds[0]);
      assertEquals("vd2 should be the second entry in vds.", vd2, vds[1]);
    }
    

    

    
    public void xtestForConcreteMethodDef() {
      // Test one that works.
      MethodDef mdef = new ConcreteMethodDef(SourceInfo.NO_INFO, 
                                             _packageMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(SourceInfo.NO_INFO, "int"), 
                                             new Word(SourceInfo.NO_INFO, "methodName"),
                                             new FormalParameter[0],
                                             new ReferenceType[0], 
                                             new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      mdef.visit(_cbfjv);
      assertEquals("There should not be any errors.", 0, errors.size());
      // Test one that doesn't work.
      mdef = new ConcreteMethodDef(SourceInfo.NO_INFO, 
                                             _packageMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(SourceInfo.NO_INFO, "int"), 
                                             new Word(SourceInfo.NO_INFO, "monkey"),
                                             new FormalParameter[0],
                                             new ReferenceType[0], 
                                             new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      mdef.visit(_cbfjv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Only constructors can have the same name as the class they appear in, and constructors do not have an explicit return type",
                   errors.get(0).getFirst());
    }
    
    public void xtestForAbstractMethodDef() {
      // Test one that works.
      MethodDef mdef = new AbstractMethodDef(SourceInfo.NO_INFO, 
                                             _abstractMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(SourceInfo.NO_INFO, "int"), 
                                             new Word(SourceInfo.NO_INFO, "methodName"),
                                             new FormalParameter[0],
                                             new ReferenceType[0]);
      _cbfjv._enclosingData.setMav(_abstractMav);
      mdef.visit(_cbfjv);
      assertEquals("There should not be any errors.", 0, errors.size());
      // Test one that doesn't work.
      mdef = new AbstractMethodDef(SourceInfo.NO_INFO, 
                                             _abstractMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(SourceInfo.NO_INFO, "int"), 
                                             new Word(SourceInfo.NO_INFO, "monkey"),
                                             new FormalParameter[0],
                                             new ReferenceType[0]);
      mdef.visit(_cbfjv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Only constructors can have the same name as the class they appear in, and constructors do not have an explicit return type",
                   errors.get(0).getFirst());
    }
    
   
    public void xtestForInitializedVariableDeclaratorDoFirst() {
      InitializedVariableDeclarator ivd = new InitializedVariableDeclarator(SourceInfo.NO_INFO,
                                                                            new PrimitiveType(SourceInfo.NO_INFO, "int"),
                                                                            new Word(SourceInfo.NO_INFO, "i"),
                                                                            new IntegerLiteral(SourceInfo.NO_INFO, 2));
      
      ivd.visit(_cbfjv);
      
      assertEquals("There should be no errors now", 0, errors.size());
    }

    public void xtestForInnerClassDef() {
      SymbolData obj = new SymbolData("java.lang.Object");
      LanguageLevelConverter.symbolTable.put("java.lang.Object", obj);
      InnerClassDef cd1 = new InnerClassDef(SourceInfo.NO_INFO, _packageMav, new Word(SourceInfo.NO_INFO, "Bart"),
                                       new TypeParameter[0], new ClassOrInterfaceType(SourceInfo.NO_INFO, "java.lang.Object", new Type[0]), new ReferenceType[0], 
                                       new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      InnerClassDef cd0 = new InnerClassDef(SourceInfo.NO_INFO, _packageMav, new Word(SourceInfo.NO_INFO, "Lisa"),
                                       new TypeParameter[0], new ClassOrInterfaceType(SourceInfo.NO_INFO, "java.lang.Object", new Type[0]), new ReferenceType[0], 
                                            new BracedBody(SourceInfo.NO_INFO, new BodyItemI[] {cd1}));

      
      SymbolData sd0 = new SymbolData(_cbfjv._enclosingData.getName() + "$Lisa", _packageMav, new TypeParameter[0], obj, new LinkedList<SymbolData>(), null); 
      _cbfjv._enclosingData.addInnerClass(sd0);
      sd0.setOuterData(_cbfjv._enclosingData);
      SymbolData sd1 = new SymbolData(_cbfjv._enclosingData.getName() + "$Lisa$Bart", _packageMav, new TypeParameter[0], obj, new LinkedList<SymbolData>(), null); 
      sd0.addInnerClass(sd1);
      sd1.setOuterData(sd0);

      sd0.setIsContinuation(true);
      sd1.setIsContinuation(true);

      
            
      LanguageLevelConverter.symbolTable.put(_cbfjv._enclosingData.getName() + "$Lisa", sd0);
//      LanguageLevelConverter.symbolTable.put(_cbfjv._enclosingData.getName() + "$Lisa$Bart", sd1);

      cd0.visit(_cbfjv);

//      sd0.setName("Lisa");
//      sd1.setName("Bart");
      
      SymbolData sd = _cbfjv._enclosingData.getInnerClassOrInterface("Lisa");
      assertEquals("There should be no errors", 0, errors.size());
      assertEquals("This symbolData should now have sd0 as an inner class", sd0, sd);
      assertEquals("sd0 should have the correct outer data", _cbfjv._enclosingData, sd0.getOuterData());
      assertEquals("sd1 should have the correct outer data", sd0, sd1.getOuterData());
      assertEquals("Sd should now have sd1 as an inner class", sd1, sd.getInnerClassOrInterface("Bart"));
      
      
      assertEquals("Lisa should have 0 methods", 0, sd0.getMethods().size());
           
    }
    
    public void xtestForInnerInterfaceDef() {
      SymbolData obj = new SymbolData("java.lang.Object");
      LanguageLevelConverter.symbolTable.put("java.lang.Object", obj);
      InnerInterfaceDef cd1 = new InnerInterfaceDef(SourceInfo.NO_INFO, _packageMav, new Word(SourceInfo.NO_INFO, "Bart"),
                                       new TypeParameter[0], new ReferenceType[0], 
                                       new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      
      InnerInterfaceDef cd0 = new InnerInterfaceDef(SourceInfo.NO_INFO, _packageMav, new Word(SourceInfo.NO_INFO, "Lisa"),
                                       new TypeParameter[0], new ReferenceType[0], 
                                            new BracedBody(SourceInfo.NO_INFO, new BodyItemI[] {cd1}));
      
      SymbolData sd0 = new SymbolData(_cbfjv._enclosingData.getName() + "$Lisa", _packageMav, new TypeParameter[0], new LinkedList<SymbolData>(), null); 
      SymbolData sd1 = new SymbolData(_cbfjv._enclosingData.getName() + "$Lisa$Bart", _packageMav, new TypeParameter[0], new LinkedList<SymbolData>(), null);
      sd0.addInnerInterface(sd1);
      sd0.setIsContinuation(true);
      sd1.setIsContinuation(true);
      
      _cbfjv._enclosingData.addInnerInterface(sd0);
      sd0.setOuterData(_cbfjv._enclosingData);

      sd0.addInnerInterface(sd1);
      sd1.setOuterData(sd0);

//    
//      LanguageLevelConverter.symbolTable.put(_cbfjv._enclosingData.getName() + "$Lisa", sd0);
//      LanguageLevelConverter.symbolTable.put(_cbfjv._enclosingData.getName() + "$Lisa$Bart", sd1);

      cd0.visit(_cbfjv);

      SymbolData sd = _cbfjv._enclosingData.getInnerClassOrInterface("Lisa");

      assertEquals("There should be no errors", 0, errors.size());
      assertEquals("This symbolData should now have sd0 as an inner interface", sd0, sd);
      assertEquals("sd0 should have the correct outer data", _cbfjv._enclosingData, sd0.getOuterData());
      assertEquals("sd1 should have the correct outer data", sd0, sd1.getOuterData());
      assertEquals("Sd should now have sd1 as an inner interface", sd1, sd.getInnerClassOrInterface("Bart"));
      assertTrue("Lisa should be an interface", sd0.isInterface());
      assertTrue("Bart should be an interface", sd1.isInterface());

    
    }
    
    public void xtestForConstructorDef() {
      //this is a ConstructorDef with no formal parameters and no throws
      ConstructorDef cd = new ConstructorDef(SourceInfo.NO_INFO, new Word(SourceInfo.NO_INFO, "MyClass"), _publicMav, new FormalParameter[0], new ReferenceType[0], 
                                             new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      
      //What if constructor name and SymbolData name don't match?  Should throw an error.
      _cbfjv._enclosingData = new SymbolData("NotRightName");
      cd.visit(_cbfjv);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "The constructor return type and class name must match", errors.getLast().getFirst());
      
      //If they are the same, it should work just fine.
      _cbfjv._enclosingData = new SymbolData("MyClass");
      
      MethodData constructor = new MethodData("MyClass", _publicMav, new TypeParameter[0], _cbfjv._enclosingData, 
                                              new VariableData[0], 
                                              new String[0], 
                                              _cbfjv._enclosingData,
                                              null);
      
      
      cd.visit(_cbfjv);
      
      
      assertEquals("Should still be 1 error", 1, errors.size());
      assertEquals("SymbolData should have 1 method", 1, _cbfjv._enclosingData.getMethods().size());
      assertTrue("SymbolData's constructor should be correct", _cbfjv._enclosingData.getMethods().contains(constructor));
      
      //With a ConstructorDef with more throws and variables, should work okay.
      FormalParameter fp = new FormalParameter(SourceInfo.NO_INFO, new UninitializedVariableDeclarator(SourceInfo.NO_INFO, new PrimitiveType(SourceInfo.NO_INFO, "int"), new Word(SourceInfo.NO_INFO, "i")), false);
      ReferenceType rt = new TypeVariable(SourceInfo.NO_INFO, "MyMadeUpException");
      ConstructorDef cd2 = new ConstructorDef(SourceInfo.NO_INFO, new Word(SourceInfo.NO_INFO, "MyClass"), _publicMav, new FormalParameter[] {fp}, new ReferenceType[] {rt}, 
                                             new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      
      VariableData vd = new VariableData("i", _finalMav, SymbolData.INT_TYPE, true, null);
      MethodData constructor2 = new MethodData("MyClass", _publicMav, new TypeParameter[0], _cbfjv._enclosingData, 
                                               new VariableData[] {vd}, 
                                               new String[] {"MyMadeUpException"}, 
                                              _cbfjv._enclosingData,
                                              null);
                                              

                                              
      constructor2.addVar(vd);
      cd2.visit(_cbfjv);
      vd.setEnclosingData(_cbfjv._enclosingData.getMethods().getLast());                                        
      assertEquals("Should still be 1 error", 1, errors.size());
      assertEquals("SymbolData should have 2 methods", 2, _cbfjv._enclosingData.getMethods().size());
      
      assertTrue("SymbolData should have new constructor", _cbfjv._enclosingData.getMethods().contains(constructor2));
      
                                              
      //If two variable names are duplicated, should throw an error.
      FormalParameter fp2 = new FormalParameter(SourceInfo.NO_INFO, new UninitializedVariableDeclarator(SourceInfo.NO_INFO, new PrimitiveType(SourceInfo.NO_INFO, "double"), new Word(SourceInfo.NO_INFO, "i")), false);
      
      ConstructorDef cd3 = new ConstructorDef(SourceInfo.NO_INFO, new Word(SourceInfo.NO_INFO, "MyClass"), _publicMav, new FormalParameter[] {fp, fp2}, new ReferenceType[] {rt}, 
                                             new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      cd3.visit(_cbfjv);
      
      assertEquals("Should now be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct","You cannot have two method parameters with the same name" , errors.getLast().getFirst());
      
      //Test that an error is thrown if the class name and constructor name are packaged differently
      _cbfjv._enclosingData.setName("package.MyClass2");
      ConstructorDef cd4 = new ConstructorDef(SourceInfo.NO_INFO, new Word(SourceInfo.NO_INFO, "different.MyClass2"), _publicMav, new FormalParameter[0], new ReferenceType[0], 
                                             new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      
      cd4.visit(_cbfjv);

      assertEquals("There should now be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", "The constructor return type and class name must match", errors.getLast().getFirst());
    }
    public void testDummy() { }
  }
}