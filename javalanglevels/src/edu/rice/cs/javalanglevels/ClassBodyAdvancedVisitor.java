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

import junit.framework.TestCase;



/*
 * Language Level Visitor that represents the Advanced Language Level.  Enforces constraints during the
 * first walk of the AST (checking for langauge specific errors and building the symbol table).
 * This class enforces things that are common to all contexts reachable within a class body at 
 * the Advanced Language Level. 
 */
public class ClassBodyAdvancedVisitor extends AdvancedVisitor {
  
  /**The SymbolData corresponding to this class.*/
  private SymbolData _symbolData;
  
  /*
   * Constructor for ClassBodyAdvancedVisitor.
   * @param sd  The SymbolData that encloses the context we are visiting.
   * @param file  The source file this came from.
   * @param packageName  The package the source file is in
   * @importedFiles  A list of classes that were specifically imported
   * @param importedPackages  A list of package names that were specifically imported
   * @param classDefsInThisFile  A list of the classes that are defined in the source file
   * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need to be resolved
   */
  public ClassBodyAdvancedVisitor(SymbolData sd, File file, String packageName, LinkedList<String> importedFiles, 
                                  LinkedList<String> importedPackages, LinkedList<String> classDefsInThisFile, Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations) {
    super(file, packageName, importedFiles, importedPackages, classDefsInThisFile, continuations);    
    _symbolData = sd;
  }

  /*Give an appropraite error*/
  public void forStatementDoFirst(Statement that) {
    _addError("Statements cannot appear outside of method bodies", that);
  }
  
  /*Make sure that this concrete method def is not declared to be abstract*/
  public void forConcreteMethodDefDoFirst(ConcreteMethodDef that) {
    ModifiersAndVisibility mav = that.getMav();
    String[] modifiers = mav.getModifiers();
    for (int i = 0; i < modifiers.length; i++) {
      if (modifiers[i].equals("abstract")) {
        _addError("Methods that have a braced body cannot be declared \"abstract\"", that);
        break;
      }
    }
  }
  
  /*Make sure that this abstract method def is declared to be abstract*/
  public void forAbstractMethodDefDoFirst(AbstractMethodDef that) {
    if (!_symbolData.hasModifier("abstract")) {
      _addError("Abstract methods can only be declared in abstract classes", that);
    }
    ModifiersAndVisibility mav = that.getMav();
    String[] modifiers = mav.getModifiers();
    super.forAbstractMethodDefDoFirst(that);
  }

  /*Add an appropriate error*/
  public void forInstanceInitializerDoFirst(InstanceInitializer that) {
    _addError("This open brace must mark the beginning of a method or class body", that);
  }
   
  
  /* 
   * Convert the Variable declartaion to variable datas.  Then, make sure that all
   * static fields are initialized and that no fields are declared to be abstract.
   * Finally, add the variable datas to the symbol data, and give an error if
   * two fields have the same names
   */
  public void forVariableDeclarationOnly(VariableDeclaration that) {
    VariableData[] vds = _variableDeclaration2VariableData(that, _symbolData);

    //make sure that none of the static fields are uninitialized:
    LinkedList<VariableData> vdsList = new LinkedList<VariableData>();
    for (int i = 0; i<vds.length; i++) {
      if (vds[i].hasModifier("static") && (that.getDeclarators()[i] instanceof UninitializedVariableDeclarator)) {
        _addAndIgnoreError("All static fields must be initialized", that);
      }
      if (vds[i].hasModifier("abstract")) {
        _addAndIgnoreError("Fields cannot be abstract", that);
      }
      else {
        vdsList.addLast(vds[i]);
      }
    }
    if(!_symbolData.addVars(vdsList.toArray(new VariableData[vdsList.size()]))) {
      _addAndIgnoreError("You cannot have two fields with the same name.  Either you already have a field by that name in this class, or one of your superclasses or interfaces has a field by that name", that);
    }
  }
  
  
  /*Create a method data corresponding to this method declaration, and then visit the
   * concrete method def with a new bodybody visitor, passing it the enclosing method data.
   * Make sure the method name is different from the class name.
   */
  public void forConcreteMethodDef(ConcreteMethodDef that) {
    forConcreteMethodDefDoFirst(that);
    if (prune(that)) { return; }
    
    MethodData md = createMethodData(that, _symbolData);
    String className = getUnqualifiedClassName(_symbolData.getName());
    
    if (className.equals(md.getName())) {
      _addAndIgnoreError("Only constructors can have the same name as the class they appear in, and constructors do not have an explicit return type",
                         that);
    }
    else {
      _symbolData.addMethod(md);
    }
    that.getBody().visit(new BodyBodyAdvancedVisitor(md, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));
  }

  /*Create a method data corresponding to this method declaration, and then visit the
   * abstract method def with a new bodybody visitor, passing it the enclosing method data.
   * Make sure the method name is different from the class name.
   */
  public void forAbstractMethodDef(AbstractMethodDef that) {
    forAbstractMethodDefDoFirst(that);
    if (prune(that)) { return; }

    MethodData md = createMethodData(that, _symbolData);
    String className = getUnqualifiedClassName(_symbolData.getName());
    if (className.equals(md.getName())) {
      _addAndIgnoreError("Only constructors can have the same name as the class they appear in, and constructors do not have an explicit return type",
                         that);
    }
    else {
      _symbolData.addMethod(md);
    }
  }
  
 
  
  
  /**Call the method in AdvancedVisitor since it's common to this and AdvancedBodyAdvancedVisitor. */
  public void forInnerInterfaceDef(InnerInterfaceDef that) {
    handleInnerInterfaceDef(that, _symbolData, getQualifiedClassName(_symbolData.getName()) + "$" + that.getName().getText());
  }
  
  /**Call the method in AdvancedVisitor since it's common to this and AdvancedBodyAdvancedVisitor. */
  public void forInnerClassDef(InnerClassDef that) {
    handleInnerClassDef(that, _symbolData, getQualifiedClassName(_symbolData.getName()) + "$" + that.getName().getText());
  }

  /**
   * Create a constructor corresponding to the specifications in the ConstructorDef, and then
   * visit the constructor body, passing the constructor as the enclosing data.
   */
  public void forConstructorDef(ConstructorDef that) {
    forConstructorDefDoFirst(that);
    if (prune(that)) { return; }
    
    that.getMav().visit(this);
    String name = getUnqualifiedClassName(that.getName().getText());
    if ((that.getName().getText().indexOf(".") != -1 && !that.getName().getText().equals(_symbolData.getName())) || !name.equals(getUnqualifiedClassName(_symbolData.getName()))) {
      _addAndIgnoreError("The constructor return type and class name must match", that);
    }

    // Turn the thrown exceptions from a ReferenceType[] to a String[]
    String[] throwStrings = referenceType2String(that.getThrows());
    
    SymbolData returnType = _symbolData;
    MethodData md = new MethodData(name, that.getMav(), new TypeParameter[0], returnType, 
                                   new VariableData[0], throwStrings, _symbolData, that);

    _checkError(); // reset check flag
    // Turn the parameters from a FormalParameterList to a VariableData[]
    VariableData[] vds = formalParameters2VariableData(that.getParameters(), md);
    if (! _checkError()) {  //if there was an error converting the formalParameters, don't use them.
      md.setParams(vds);
      if (!md.addFinalVars(vds)) {
        _addAndIgnoreError("You cannot have two method parameters with the same name", that);
      }
    }
    
    _symbolData.addMethod(md);
    that.getStatements().visit(new BodyBodyAdvancedVisitor(md, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));

    //note that we have seen a constructor.
    _symbolData.incrementConstructorCount();
  }
  
  /** Delegate to method in LanguageLevelVisitor */
  public void forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    complexAnonymousClassInstantiationHelper(that, _symbolData);
  }

  /**Delegate to method in LanguageLevelVisitor */
  public void forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    simpleAnonymousClassInstantiationHelper(that, _symbolData);
  }

  
  
   /**
    * Test the methods that are declared above.
   */
  public static class ClassBodyAdvancedVisitorTest extends TestCase {
    
    private ClassBodyAdvancedVisitor _cbav;
    
    private SymbolData _sd1;
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[0]);
    private ModifiersAndVisibility _abstractMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"final"});
    private ModifiersAndVisibility _staticMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"static"});
    private ModifiersAndVisibility _abstractStaticMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"abstract", "static"});
    private ModifiersAndVisibility _finalStaticMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"static", "final"});
    
    
    public ClassBodyAdvancedVisitorTest() {
      this("");
    }
    public ClassBodyAdvancedVisitorTest(String name) {
      super(name);
    }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");

      errors = new LinkedList<Pair<String, JExpressionIF>>();
      symbolTable = new Symboltable();
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      _hierarchy = new Hashtable<String, TypeDefBase>();
      _classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
      _cbav = new ClassBodyAdvancedVisitor(_sd1, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>(), new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
      _cbav.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      _cbav._resetNonStaticFields();
      _cbav._importedPackages.addFirst("java.lang");
      _cbav._newSDs = new Hashtable<SymbolData, LanguageLevelVisitor>();
      _errorAdded = false;
    }
    
    public void testForConcreteMethodDefDoFirst() {
      // Check one that works
      ConcreteMethodDef cmd = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _publicMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cmd.visit(_cbav);
      assertEquals("There should not be any errors", 0, errors.size());
      
      // Check one that doesn't work because it is declared abstract but is actually a concrete method
      ConcreteMethodDef cmd2 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _abstractMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0], 
                                                     new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cmd2.visit(_cbav);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "Methods that have a braced body cannot be declared \"abstract\"", errors.get(0).getFirst());
      
      //Check that a static method does not result in an error.
      ConcreteMethodDef cmd3 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _staticMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "methodName2"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0], 
                                                     new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cmd3.visit(_cbav);
      assertEquals("There should still be one error", 1, errors.size());
      
      
    }
    
    public void testForAbstractMethodDefDoFirst() {
      // Check one that doesn't work
      AbstractMethodDef amd = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _abstractMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0]);
      amd.visit(_cbav);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "Abstract methods can only be declared in abstract classes", errors.get(0).getFirst());
      
      // Check one that works
      _cbav._symbolData.setMav(_abstractMav);
      AbstractMethodDef amd2 = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _abstractMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd2.visit(_cbav);
      assertEquals("There should still be one error", 1, errors.size());
      
      // Check that static methods are now allowed at the Advanced level.
      AbstractMethodDef amd3 = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _abstractStaticMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "methodName2"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd3.visit(_cbav);
      assertEquals("There should be two errors", 2, errors.size());
      assertEquals("The error message should be correct.", "Illegal combination of modifiers. Can't use static and abstract together.", errors.get(1).getFirst());
    }

    public void testForInstanceInitializerDoFirst() {
      InstanceInitializer ii = new InstanceInitializer(JExprParser.NO_SOURCE_INFO, 
                                                       new Block(JExprParser.NO_SOURCE_INFO, 
                                                                 new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0])));
      ii.visit(_cbav);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "This open brace must mark the beginning of a method or class body", errors.get(0).getFirst());
    }
    
    /* These test is shared with BodyIntermediateVisitor,
     * perhaps we could factor it out. */
    
    public void testForVariableDeclarationOnly() {
      // Check one that works
      VariableDeclaration vdecl = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,
                                                       _packageMav,
                                                       new VariableDeclarator[] {
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                               new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                               new Word (JExprParser.NO_SOURCE_INFO, "field1")),
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                               new PrimitiveType(JExprParser.NO_SOURCE_INFO, "boolean"), 
                               new Word (JExprParser.NO_SOURCE_INFO, "field2"))});
      VariableData vd1 = new VariableData("field1", _packageMav, SymbolData.DOUBLE_TYPE, true, _cbav._symbolData);
      VariableData vd2 = new VariableData("field2", _packageMav, SymbolData.BOOLEAN_TYPE, true, _cbav._symbolData);
      vdecl.visit(_cbav);
      assertEquals("There should not be any errors.", 0, errors.size());
      assertTrue("field1 was added.", _sd1.getVars().contains(vd1));
      assertTrue("field2 was added.", _sd1.getVars().contains(vd2));
      
      // Check one that doesn't work
      VariableDeclaration vdecl2 = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,
                                                        _packageMav,
                                                        new VariableDeclarator[] {
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                            new Word (JExprParser.NO_SOURCE_INFO, "field3")),
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                            new Word (JExprParser.NO_SOURCE_INFO, "field3"))});
      VariableData vd3 = new VariableData("field3", _packageMav, SymbolData.DOUBLE_TYPE, true, _cbav._symbolData);
      vdecl2.visit(_cbav);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct", "You cannot have two fields with the same name.  Either you already have a field by that name in this class, or one of your superclasses or interfaces has a field by that name", errors.get(0).getFirst());
      assertTrue("field3 was added.", _sd1.getVars().contains(vd3));
      
      //Check a static field that has not been assigned (won't work)
      VariableDeclaration vdecl3 = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,
                                                        _staticMav,
                                                        new VariableDeclarator[] {
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                            new Word (JExprParser.NO_SOURCE_INFO, "field4"))});
      VariableData vd4 = new VariableData("field4", _finalStaticMav, SymbolData.DOUBLE_TYPE, false, _cbav._symbolData);
          vdecl3.visit(_cbav);
        assertEquals("There should be two errors", 2, errors.size());
        assertEquals("The error message should be correct", "All static fields must be initialized", errors.get(1).getFirst());
        assertFalse("field4 was not added.", _sd1.getVars().contains(vd4));
        
      
      //Check a non-static field that has been assigned.  (will work);
      VariableDeclaration vdecl5 = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,
                                                        _publicMav,
                                                        new VariableDeclarator[] {
        new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                            new Word (JExprParser.NO_SOURCE_INFO, "field5"), new DoubleLiteral(JExprParser.NO_SOURCE_INFO, 2.4))});
      vdecl5.visit(_cbav);
      VariableData vd5 = new VariableData("field5", _publicMav, SymbolData.DOUBLE_TYPE, true, _cbav._symbolData);
      vd5.setHasInitializer(true);
      assertEquals("There should still be two errors", 2, errors.size());
      assertTrue("Field 5 was added.", _sd1.getVars().contains(vd5));
      
      //check one that overrides the super class's field
      VariableDeclaration vdecl6 = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,
                                                       _packageMav,
                                                           new VariableDeclarator[] {
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                            new Word (JExprParser.NO_SOURCE_INFO, "field6"))});
      
      
      VariableData vd6 = new VariableData("field6", _packageMav, SymbolData.DOUBLE_TYPE, true, _cbav._symbolData);
      SymbolData myData = new SymbolData("myData");
      myData.addVar(vd6);
      _cbav._symbolData.setSuperClass(myData);
      vdecl6.visit(_cbav);
      assertEquals("There should be three errors.", 3, errors.size());
      assertEquals("The error message should be correct", "You cannot have two fields with the same name.  Either you already have a field by that name in this class, or one of your superclasses or interfaces has a field by that name", errors.get(2).getFirst());

    }
    
    public void testFormalParameters2VariableData() {
      FormalParameter[] fps = new FormalParameter[] {
        new FormalParameter(JExprParser.NO_SOURCE_INFO, 
                            new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                              new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                              new Word (JExprParser.NO_SOURCE_INFO, "field1")),
                            false),
        new FormalParameter(JExprParser.NO_SOURCE_INFO, 
                            new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                              new PrimitiveType(JExprParser.NO_SOURCE_INFO, "boolean"), 
                                                              new Word (JExprParser.NO_SOURCE_INFO, "field2")),
                            false)};
      VariableData vd1 = new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, true, _cbav._symbolData);
      VariableData vd2 = new VariableData("field2", _finalMav, SymbolData.BOOLEAN_TYPE, true, _cbav._symbolData);
      VariableData[] vds = _cbav.formalParameters2VariableData(fps, _cbav._symbolData);
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("vd1 should be the first entry in vds.", vd1, vds[0]);
      assertEquals("vd2 should be the second entry in vds.", vd2, vds[1]);
    }
    

    

    
    public void testForConcreteMethodDef() {
      // Test one that works.
      MethodDef mdef = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                             _packageMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                             new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                             new FormalParameter[0],
                                             new ReferenceType[0], 
                                             new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      mdef.visit(_cbav);
      assertEquals("There should not be any errors.", 0, errors.size());
      // Test one that doesn't work.
      mdef = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                             _packageMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                             new Word(JExprParser.NO_SOURCE_INFO, "monkey"),
                                             new FormalParameter[0],
                                             new ReferenceType[0], 
                                             new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      mdef.visit(_cbav);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Only constructors can have the same name as the class they appear in, and constructors do not have an explicit return type",
                   errors.get(0).getFirst());
    }
    
    public void testForAbstractMethodDef() {
      // Test one that works.
      MethodDef mdef = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                             _abstractMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                             new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                             new FormalParameter[0],
                                             new ReferenceType[0]);
      _cbav._symbolData.setMav(_abstractMav);
      mdef.visit(_cbav);
      assertEquals("There should not be any errors.", 0, errors.size());
      // Test one that doesn't work.
      mdef = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                             _abstractMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                             new Word(JExprParser.NO_SOURCE_INFO, "monkey"),
                                             new FormalParameter[0],
                                             new ReferenceType[0]);
      mdef.visit(_cbav);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Only constructors can have the same name as the class they appear in, and constructors do not have an explicit return type",
                   errors.get(0).getFirst());
    }
    
   
    public void testForInitializedVariableDeclaratorDoFirst() {
      InitializedVariableDeclarator ivd = new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO,
                                                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"),
                                                                            new Word(JExprParser.NO_SOURCE_INFO, "i"),
                                                                            new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 2));
      
      ivd.visit(_cbav);
      
      assertEquals("There should be no errors now", 0, errors.size());
    }

    public void testForInnerClassDef() {
      SymbolData obj = new SymbolData("java.lang.Object");
      symbolTable.put("java.lang.Object", obj);
      InnerClassDef cd1 = new InnerClassDef(JExprParser.NO_SOURCE_INFO, _packageMav, new Word(JExprParser.NO_SOURCE_INFO, "Bart"),
                                       new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), new ReferenceType[0], 
                                       new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      InnerClassDef cd0 = new InnerClassDef(JExprParser.NO_SOURCE_INFO, _packageMav, new Word(JExprParser.NO_SOURCE_INFO, "Lisa"),
                                       new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), new ReferenceType[0], 
                                            new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {cd1}));

      
      SymbolData sd0 = new SymbolData(_cbav._symbolData.getName() + "$Lisa", _packageMav, new TypeParameter[0], obj, new LinkedList<SymbolData>(), null); 
      _cbav._symbolData.addInnerClass(sd0);
      sd0.setOuterData(_cbav._symbolData);
      SymbolData sd1 = new SymbolData(_cbav._symbolData.getName() + "$Lisa$Bart", _packageMav, new TypeParameter[0], obj, new LinkedList<SymbolData>(), null); 
      sd0.addInnerClass(sd1);
      sd1.setOuterData(sd0);

      sd0.setIsContinuation(true);
      sd1.setIsContinuation(true);

      
            
      symbolTable.put(_cbav._symbolData.getName() + "$Lisa", sd0);
//      symbolTable.put(_cbav._symbolData.getName() + "$Lisa$Bart", sd1);

      cd0.visit(_cbav);

//      sd0.setName("Lisa");
//      sd1.setName("Bart");
      
      SymbolData sd = _cbav._symbolData.getInnerClassOrInterface("Lisa");
      assertEquals("There should be no errors", 0, errors.size());
      assertEquals("This symbolData should now have sd0 as an inner class", sd0, sd);
      assertEquals("sd0 should have the correct outer data", _cbav._symbolData, sd0.getOuterData());
      assertEquals("sd1 should have the correct outer data", sd0, sd1.getOuterData());
      assertEquals("Sd should now have sd1 as an inner class", sd1, sd.getInnerClassOrInterface("Bart"));
      
      
      assertEquals("Lisa should have 0 methods", 0, sd0.getMethods().size());
           
    }
    
    public void testForInnerInterfaceDef() {
      SymbolData obj = new SymbolData("java.lang.Object");
      symbolTable.put("java.lang.Object", obj);
      InnerInterfaceDef cd1 = new InnerInterfaceDef(JExprParser.NO_SOURCE_INFO, _packageMav, new Word(JExprParser.NO_SOURCE_INFO, "Bart"),
                                       new TypeParameter[0], new ReferenceType[0], 
                                       new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      InnerInterfaceDef cd0 = new InnerInterfaceDef(JExprParser.NO_SOURCE_INFO, _packageMav, new Word(JExprParser.NO_SOURCE_INFO, "Lisa"),
                                       new TypeParameter[0], new ReferenceType[0], 
                                            new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {cd1}));
      
      SymbolData sd0 = new SymbolData(_cbav._symbolData.getName() + "$Lisa", _packageMav, new TypeParameter[0], new LinkedList<SymbolData>(), null); 
      SymbolData sd1 = new SymbolData(_cbav._symbolData.getName() + "$Lisa$Bart", _packageMav, new TypeParameter[0], new LinkedList<SymbolData>(), null);
      sd0.addInnerInterface(sd1);
      sd0.setIsContinuation(true);
      sd1.setIsContinuation(true);
      
      _cbav._symbolData.addInnerInterface(sd0);
      sd0.setOuterData(_cbav._symbolData);

      sd0.addInnerInterface(sd1);
      sd1.setOuterData(sd0);

//    
//      symbolTable.put(_cbav._symbolData.getName() + "$Lisa", sd0);
//      symbolTable.put(_cbav._symbolData.getName() + "$Lisa$Bart", sd1);

      cd0.visit(_cbav);

      SymbolData sd = _cbav._symbolData.getInnerClassOrInterface("Lisa");

      assertEquals("There should be no errors", 0, errors.size());
      assertEquals("This symbolData should now have sd0 as an inner interface", sd0, sd);
      assertEquals("sd0 should have the correct outer data", _cbav._symbolData, sd0.getOuterData());
      assertEquals("sd1 should have the correct outer data", sd0, sd1.getOuterData());
      assertEquals("Sd should now have sd1 as an inner interface", sd1, sd.getInnerClassOrInterface("Bart"));
      assertTrue("Lisa should be an interface", sd0.isInterface());
      assertTrue("Bart should be an interface", sd1.isInterface());

    
    }
    
    public void testForConstructorDef() {
      //this is a ConstructorDef with no formal parameters and no throws
      ConstructorDef cd = new ConstructorDef(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "MyClass"), _publicMav, new FormalParameter[0], new ReferenceType[0], 
                                             new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      //What if constructor name and SymbolData name don't match?  Should throw an error.
      _cbav._symbolData = new SymbolData("NotRightName");
      cd.visit(_cbav);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "The constructor return type and class name must match", errors.getLast().getFirst());
      
      //If they are the same, it should work just fine.
      _cbav._symbolData = new SymbolData("MyClass");
      
      MethodData constructor = new MethodData("MyClass", _publicMav, new TypeParameter[0], _cbav._symbolData, 
                                              new VariableData[0], 
                                              new String[0], 
                                              _cbav._symbolData,
                                              null);
      
      
      cd.visit(_cbav);
      
      
      assertEquals("Should still be 1 error", 1, errors.size());
      assertEquals("SymbolData should have 1 method", 1, _cbav._symbolData.getMethods().size());
      assertTrue("SymbolData's constructor should be correct", _cbav._symbolData.getMethods().contains(constructor));
      
      //With a ConstructorDef with more throws and variables, should work okay.
      FormalParameter fp = new FormalParameter(JExprParser.NO_SOURCE_INFO, new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), new Word(JExprParser.NO_SOURCE_INFO, "i")), false);
      ReferenceType rt = new TypeVariable(JExprParser.NO_SOURCE_INFO, "MyMadeUpException");
      ConstructorDef cd2 = new ConstructorDef(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "MyClass"), _publicMav, new FormalParameter[] {fp}, new ReferenceType[] {rt}, 
                                             new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      VariableData vd = new VariableData("i", _finalMav, SymbolData.INT_TYPE, true, null);
      MethodData constructor2 = new MethodData("MyClass", _publicMav, new TypeParameter[0], _cbav._symbolData, 
                                               new VariableData[] {vd}, 
                                               new String[] {"MyMadeUpException"}, 
                                              _cbav._symbolData,
                                              null);
                                              

                                              
      constructor2.addVar(vd);
      cd2.visit(_cbav);
      vd.setEnclosingData(_cbav._symbolData.getMethods().getLast());                                        
      assertEquals("Should still be 1 error", 1, errors.size());
      assertEquals("SymbolData should have 2 methods", 2, _cbav._symbolData.getMethods().size());
      
      assertTrue("SymbolData should have new constructor", _cbav._symbolData.getMethods().contains(constructor2));
      
                                              
      //If two variable names are duplicated, should throw an error.
      FormalParameter fp2 = new FormalParameter(JExprParser.NO_SOURCE_INFO, new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), new Word(JExprParser.NO_SOURCE_INFO, "i")), false);
      
      ConstructorDef cd3 = new ConstructorDef(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "MyClass"), _publicMav, new FormalParameter[] {fp, fp2}, new ReferenceType[] {rt}, 
                                             new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cd3.visit(_cbav);
      
      assertEquals("Should now be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct","You cannot have two method parameters with the same name" , errors.getLast().getFirst());
      
      //Test that an error is thrown if the class name and constructor name are packaged differently
      _cbav._symbolData.setName("package.MyClass2");
      ConstructorDef cd4 = new ConstructorDef(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "different.MyClass2"), _publicMav, new FormalParameter[0], new ReferenceType[0], 
                                             new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      cd4.visit(_cbav);

      assertEquals("There should now be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", "The constructor return type and class name must match", errors.getLast().getFirst());

      
    }    
  }
}