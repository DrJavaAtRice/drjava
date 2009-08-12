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

//import edu.rice.cs.javaast.SourceInfo;
import junit.framework.TestCase;


/*
 * Language Level Visitor that represents the Elementary Language Level.  Enforces constraints during the
 * first walk of the AST (checking for langauge specific errors and building the symbol table).
 * This class enforces things that are common to all contexts reachable within a class body at 
 * the Elementary Language Level. 
 */
public class ClassBodyElementaryVisitor extends ElementaryVisitor {
  
  /**The SymbolData corresponding to this class.*/
  private SymbolData _symbolData;
  
  
  /*
   * Constructor for ClassBodyElementaryVisitor.
   * @param sd  The SymbolData that encloses the context we are visiting.
   * @param file  The source file this came from.
   * @param packageName  The package the source file is in
   * @importedFiles  A list of classes that were specifically imported
   * @param importedPackages  A list of package names that were specifically imported
   * @param classDefsInThisFile  A list of the classes that are defined in the source file
   * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need to be resolved
   */
  public ClassBodyElementaryVisitor(SymbolData sd, File file, String packageName, LinkedList<String> importedFiles, 
                                  LinkedList<String> importedPackages, LinkedList<String> classDefsInThisFile, Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations) {
    super(file, packageName, importedFiles, importedPackages, classDefsInThisFile, continuations);    
    _symbolData = sd;
  }
  
  /*Give an appropraite error*/
  public Void forStatementDoFirst(Statement that) {
    _addError("Statements cannot appear outside of method bodies.", that);
    return null;
  }
  
  /*Make sure that this concrete method def is not declared to be abstract*/
  public Void forConcreteMethodDefDoFirst(ConcreteMethodDef that) {
    ModifiersAndVisibility mav = that.getMav();
    String[] modifiers = mav.getModifiers();
    // Concrete methods cannot have any modifiers at the Elementary level since only "abstract" is allowed
    // at all, and this is not an abstract method.
    for (int i = 0; i < modifiers.length; i++) {
      if (modifiers[i].equals("abstract")) {
        _addError("Methods that have a braced body cannot be declared \"abstract\".", that);
        break;
      }
    }
    if (that.getThrows().length > 0) { //throws are prohibited at the Elementary level.
      _addAndIgnoreError("Methods cannot throw exceptions at the Elementary level", that);
    }
    return super.forConcreteMethodDefDoFirst(that);
  }
  
  /*Make sure that this abstract method def is declared to be abstract, and that it is not
   * declared to throw any exceptions*/
  public Void forAbstractMethodDefDoFirst(AbstractMethodDef that) {
    if (that.getThrows().length > 0) { //throws are prohibited at the Elementary level.
      _addAndIgnoreError("Methods cannot throw exceptions at the Elementary level", that);
    }
    if (!_symbolData.hasModifier("abstract")) {
      _addError("Abstract methods can only be declared in abstract classes.", that);
      return null;
    }
    else {
      return super.forAbstractMethodDefDoFirst(that);
    }
  }

  /*Throw an appropriate error*/
  public Void forInstanceInitializerDoFirst(InstanceInitializer that) {
    _addError("This open brace must mark the beginning of a method or class body.", that);
    return null;
  }
  
  /**Cannot initialize variables in a class body at Elementary Level  Must be done in the constructor.*/
  public Void forInitializedVariableDeclaratorDoFirst(InitializedVariableDeclarator that) {
    _addError("Cannot initialize a class's fields at the Elementary level.  To set the value of a field, when you instantiate the class, pass the desired value to the class's constructor.", that);
    return forVariableDeclaratorDoFirst(that);
  }
  
  /** Convert the Variable declartaion to variable datas.  Then, make sure that no fields are declared to be abstract.
    * Finally, add the variable datas to the symbol data, and give an error if two fields have the same names
    */
  public Void forVariableDeclarationOnly(VariableDeclaration that) {
    VariableData[] vds = _variableDeclaration2VariableData(that, _symbolData);
    for (int i = 0; i<vds.length; i++) {
      if (vds[i].hasModifier("abstract")) {
        _addAndIgnoreError("Fields cannot be abstract", that);
      }
    }
    if(!_symbolData.addFinalVars(vds)) {  //TODO: no need to addFinalVars--made final in _variableDeclaration2VariableData
      _addAndIgnoreError("You cannot have two fields with the same name.  Either you already have a field by that name in this class, or one of your superclasses has a field by that name", that);
    }
    return null;
  }

  /*Create a method data corresponding to this method declaration, and then visit the
   * concrete method def with a new bodybody visitor, passing it the enclosing method data.
   * Make sure the method name is different from the class name.
   * Methods are automatically public at the Elementary level.
   */
  public Void forConcreteMethodDef(ConcreteMethodDef that) {
    forConcreteMethodDefDoFirst(that);
    if (prune(that)) return null;
    MethodData md = createMethodData(that, _symbolData);
    md.addPublicMav(); // All methods are automatically public at the Elementary level.
    String className = getUnqualifiedClassName(_symbolData.getName());
    if (className.equals(md.getName())) {
      _addAndIgnoreError("Constructors are they only methods that can have the same name as the class they appear in, and they are not allowed at the Elementary level",
                         that);
    }
    else {
      _symbolData.addMethod(md);
    }
    that.getBody().visit(new BodyBodyElementaryVisitor(md, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));
    return forConcreteMethodDefOnly(that);
  }

  
  /** Create a method data corresponding to this method declaration, and then visit the
    * abstract method def with a new bodybody visitor, passing it the enclosing method data.
    * Make sure the method name is different from the class name.
    * Methods are automatically public at the Elementary level.
    */
  public Void forAbstractMethodDef(AbstractMethodDef that) {
    forAbstractMethodDefDoFirst(that);
    if (prune(that)) return null;
    MethodData md = createMethodData(that, _symbolData);
    String className = getUnqualifiedClassName(_symbolData.getName());
    if (className.equals(md.getName())) {
      _addAndIgnoreError("Constructors are they only methods that can have the same name as the class they appear in, and they are not allowed at the Elementary level",
                         that);
    }
    else {
      _symbolData.addMethod(md);
    }
    return forAbstractMethodDefOnly(that);
  }
  
  /** Test the methods declared in the enclosing (above) class. */
  public static class ClassBodyElementaryVisitorTest extends TestCase {
    
    private ClassBodyElementaryVisitor _cbbv;
    
    private SymbolData _sd1;
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[0]);
    private ModifiersAndVisibility _abstractMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"final"});
    private ModifiersAndVisibility _finalPrivateMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[]{"final", "private"});
    
    public ClassBodyElementaryVisitorTest() {
      this("");
    }
    public ClassBodyElementaryVisitorTest(String name) {
      super(name);
    }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");

      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable = symbolTable = new Symboltable();
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      _hierarchy = new Hashtable<String, TypeDefBase>();
      _classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
      _cbbv = new ClassBodyElementaryVisitor(_sd1, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>(), new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
      _cbbv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      _cbbv._resetNonStaticFields();
      _cbbv._importedPackages.addFirst("java.lang");
      _errorAdded = false;
    }
    
    public void testForConcreteMethodDefDoFirst() {
      // Check one that works
      ConcreteMethodDef cmd = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _packageMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cmd.visit(_cbbv);
      assertEquals("There should not be any errors", 0, errors.size());
      
      // Check one that doesn't work
      ConcreteMethodDef cmd2 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _abstractMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0], 
                                                     new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cmd2.visit(_cbbv);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "Methods that have a braced body cannot be declared \"abstract\".", errors.get(0).getFirst());
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
      amd.visit(_cbbv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "Abstract methods can only be declared in abstract classes.", errors.get(0).getFirst());
      
      // Check one that works
      _cbbv._symbolData.setMav(_abstractMav);
      AbstractMethodDef amd2 = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _abstractMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd2.visit(_cbbv);
      assertEquals("There should still be one error", 1, errors.size());
    }
    
    public void testForInstanceInitializerDoFirst() {
      InstanceInitializer ii = new InstanceInitializer(JExprParser.NO_SOURCE_INFO, 
                                                       new Block(JExprParser.NO_SOURCE_INFO, 
                                                                 new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0])));
      ii.visit(_cbbv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "This open brace must mark the beginning of a method or class body.", errors.get(0).getFirst());    
    }
    
    /* These test is shared with BodyBodyElementaryVisitor,
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
      VariableData vd1 = new VariableData("field1", _finalPrivateMav, SymbolData.DOUBLE_TYPE, false, _cbbv._symbolData);
      VariableData vd2 = new VariableData("field2", _finalPrivateMav, SymbolData.BOOLEAN_TYPE, false, _cbbv._symbolData);
      vdecl.visit(_cbbv);
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
      VariableData vd3 = new VariableData("field3", _finalPrivateMav, SymbolData.DOUBLE_TYPE, false, _cbbv._symbolData);
      vdecl2.visit(_cbbv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct", "You cannot have two fields with the same name.  Either you already have a field by that name in this class, or one of your superclasses has a field by that name", errors.get(0).getFirst());
      assertTrue("field3 was added.", _sd1.getVars().contains(vd3));

      //check one that overrides the super class's field
      VariableDeclaration vdecl3 = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,
                                                       _packageMav,
                                                           new VariableDeclarator[] {
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                            new Word (JExprParser.NO_SOURCE_INFO, "field4"))});
      
      
      VariableData vd4 = new VariableData("field4", _finalPrivateMav, SymbolData.DOUBLE_TYPE, false, _cbbv._symbolData);
      SymbolData myData = new SymbolData("myData");
      myData.addVar(vd4);
      _cbbv._symbolData.setSuperClass(myData);
      vdecl3.visit(_cbbv);
      assertEquals("There should be two errors.", 2, errors.size());
      assertEquals("The error message should be correct", "You cannot have two fields with the same name.  Either you already have a field by that name in this class, or one of your superclasses has a field by that name", errors.get(1).getFirst());
      
      
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
      VariableData vd1 = new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, true, _cbbv._symbolData);
      VariableData vd2 = new VariableData("field2", _finalMav, SymbolData.BOOLEAN_TYPE, true, _cbbv._symbolData);
      VariableData[] vds = _cbbv.formalParameters2VariableData(fps, _cbbv._symbolData);
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("vd1 should be the first entry in vds.", vd1, vds[0]);
      assertEquals("vd2 should be the second entry in vds.", vd2, vds[1]);
    }
    
    
    public void testCreateMethodData() {
      // Test one that doesn't work.
      MethodDef mdef = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _publicMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      MethodData mdata = new MethodData("methodName", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                                   new VariableData[0], 
                                   new String[0],
                                   _sd1,
                                   null);
      assertEquals("Should return the correct MethodData", mdata, _cbbv.createMethodData(mdef, _cbbv._symbolData));
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "The keyword \"public\" cannot be used at the Elementary level", errors.get(0).getFirst());
      
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
                                                          MethodData result = _cbbv.createMethodData(mdef, _cbbv._symbolData);
                                                          mdata.getParams()[0].setEnclosingData(result);
                                                          mdata.getParams()[1].setEnclosingData(result);

                                                          // have to add the parameters to the vars manually
                                                          mdata.addVars(new VariableData[] { new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, true, result) });                                                          
                                                          assertEquals("Should return the correct MethodData", mdata, result);
                                                          assertEquals("There should be two errors.", 2, errors.size());
//                                                          assertEquals("The second error message should be correct.", "Methods cannot throw exceptions at the Elementary level", errors.get(1).getFirst());
//                                                          assertEquals("The third error message should be correct.", "The keyword \"void\" cannot be used at the Elementary level", errors.get(2).getFirst());
                                                          assertEquals("The second error message should be correct.", "You cannot have two method parameters with the same name", errors.get(1).getFirst());
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
      mdef.visit(_cbbv);
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
      mdef.visit(_cbbv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Constructors are they only methods that can have the same name as the class they appear in, and they are not allowed at the Elementary level",
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
      _cbbv._symbolData.setMav(_abstractMav);
      mdef.visit(_cbbv);
      assertEquals("There should not be any errors.", 0, errors.size());
      // Test one that doesn't work.
      mdef = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                             _abstractMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                             new Word(JExprParser.NO_SOURCE_INFO, "monkey"),
                                             new FormalParameter[0],
                                             new ReferenceType[0]);
      mdef.visit(_cbbv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Constructors are they only methods that can have the same name as the class they appear in, and they are not allowed at the Elementary level",
                   errors.get(0).getFirst());
    }
    
    public void testForInitializedVariableDeclaratorDoFirst() {
      InitializedVariableDeclarator ivd = new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO,
                                                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"),
                                                                            new Word(JExprParser.NO_SOURCE_INFO, "i"),
                                                                            new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5));
      
      ivd.visit(_cbbv);
      
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct",
                   "Cannot initialize a class's fields at the Elementary level.  To set the value of a field, when you instantiate the class, pass the desired value to the class's constructor.", 
                   errors.get(0).getFirst());
    }
  }
}