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
import java.util.*;
import java.io.*;

import junit.framework.TestCase;



/** Language Level Visitor that represents the FullJava Language Level. Only builds the symbol table).  No syntax
  * checking is performed.  All .java files will be compiled by "javac", which will check for syntax errors.
  */
public class BodyBodyFullJavaVisitor extends FullJavaVisitor {

  /** The MethodData of this method.*/
  private BodyData _bodyData;
  
  /** Constructor for BodyBodyFullJavaVisitor.
    * @param bodyData  The BodyData that encloses the context we are visiting.
    * @param file  The source file this came from.
    * @param packageName  The package the source file is in
    * @importedFiles  A list of classes that were specifically imported
    * @param importedPackages  A list of package names that were specifically imported
    * @param classDefsInThisFile  A list of the classes that are defined in the source file
    * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need to 
    *                       be resolved
    */
  public BodyBodyFullJavaVisitor(BodyData bodyData,
                                 File file, 
                                 String packageName,
                                 LinkedList<String> importedFiles, 
                                 LinkedList<String> importedPackages, 
                                 LinkedList<String> classDefsInThisFile, 
                                 Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations,
                                 LinkedList<String> innerClassesToBeParsed) {
    super(file, packageName, importedFiles, importedPackages, classDefsInThisFile, continuations);
    _bodyData = bodyData;
    _innerClassesToBeParsed = innerClassesToBeParsed;
  }
  
  /** Ignore MethodDef. */
  public Void forMethodDefDoFirst(MethodDef that) { return null; }
  
  /* There is currently no way to differentiate between a block statement and
   * an instance initializer in a braced body given the general nature of a 
   * braced body.  Whenever an instance initialization is visited in a method
   * body, we must assume that it is a block statement.
   */
  public Void forInstanceInitializer(InstanceInitializer that) { return forBlock(that.getCode());  }

  /* Visit this BlockData with a new BodyBodyFullJava visitor. */
  public Void forBlock(Block that) {
    forBlockDoFirst(that);
    if (prune(that)) return null;
    BlockData bd = new BlockData(_bodyData);
    _bodyData.addBlock(bd);
    that.getStatements().visit(new BodyBodyFullJavaVisitor(bd, _file, _package, _importedFiles, _importedPackages, 
                                                           _classNamesInThisFile, continuations, 
                                                           _innerClassesToBeParsed));
    return forBlockOnly(that);
  }
  
  /** Visit the block as in forBlock(), but first add the exception parameter as a variable in that block. */
  public Void forCatchBlock(CatchBlock that) {
    forCatchBlockDoFirst(that);
    if (prune(that)) return null;
    
    Block b = that.getBlock();
    forBlockDoFirst(b);
    if (prune(b)) return null;
    BlockData bd = new BlockData(_bodyData);
    _bodyData.addBlock(bd);
    
    VariableData exceptionVar = formalParameters2VariableData(new FormalParameter[]{ that.getException() }, bd)[0];
    if (prune(that.getException())) return null;
    bd.addVar(exceptionVar);
    
    b.getStatements().visit(new BodyBodyFullJavaVisitor(bd, _file, _package, _importedFiles, _importedPackages, 
                                                        _classNamesInThisFile, continuations, 
                                                        _innerClassesToBeParsed));
    forBlockOnly(b);
    return forCatchBlockOnly(that);
  }
  
  /** Adds the variables that were declared to the body data and make sure that no two variables have the same name.*/
  public Void forVariableDeclarationOnly(VariableDeclaration that) {
    if (! _bodyData.addVars(_variableDeclaration2VariableData(that, _bodyData))) {
/* The following commenting out of code is kludge to get around the fact that LL processing does not allow a for
 * index variable to repeated in successive for loops. */
//      _addAndIgnoreError("You cannot have two variables with the same name.", that);
    }
    return null;
  }
  
  /** Ignore TryCatchStatement.*/
  public Void forTryCatchStatementDoFirst(TryCatchStatement that) { return null; }

  /** Process a local inner class definition */
  public Void forInnerClassDef(InnerClassDef that) {
    // TODO: is this necessarily local?
    handleInnerClassDef(that, _bodyData, getQualifiedClassName(_bodyData.getSymbolData().getName()) + "."
                          + _bodyData.getSymbolData().preincrementLocalClassNum() + that.getName().getText());
    return null;
  }
  
  /** Process a local inner interface definition */
  public Void forInnerInterfaceDef(InnerInterfaceDef that) {
    // TODO: is this necessarily local?
    handleInnerInterfaceDef(that, _bodyData, getQualifiedClassName(_bodyData.getSymbolData().getName()) + "."
                              + _bodyData.getSymbolData().preincrementLocalClassNum() + that.getName().getText());
    return null;
  }
  
  /** Delegate to method in LLV. */
  public Void forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    complexAnonymousClassInstantiationHelper(that, _bodyData);
    return null;
  }

  /** Delegate to method in LLV. */
  public Void forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    simpleAnonymousClassInstantiationHelper(that, _bodyData);
    return null;
  }

  /** Test most of the methods declared above right here. */
  public static class BodyBodyFullJavaVisitorTest extends TestCase {
    
    private BodyBodyFullJavaVisitor _bfv;
    
    private SymbolData _sd1;
    private MethodData _md1;
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
    
    
    public BodyBodyFullJavaVisitorTest() { this(""); }
    
    public BodyBodyFullJavaVisitorTest(String name) { super(name); }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");
      _md1 = new MethodData("methodName", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                                   new VariableData[0], 
                                   new String[0],
                                   _sd1,
                                   null);

      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable.clear();
      LanguageLevelConverter._newSDs.clear();
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      _hierarchy = new Hashtable<String, TypeDefBase>();
      _bfv = new BodyBodyFullJavaVisitor(_md1, 
                                         new File(""), 
                                         "", 
                                         new LinkedList<String>(), 
                                         new LinkedList<String>(), 
                                         new LinkedList<String>(), 
                                         new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>(),
                                         new LinkedList<String>());
      _bfv._classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
      _bfv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      _bfv._resetNonStaticFields();
      _bfv._importedPackages.addFirst("java.lang");
      _errorAdded = false;
    }
    
    public void testForMethodDefDoFirst() {
      ConcreteMethodDef cmd = new ConcreteMethodDef(SourceInfo.NO_INFO, 
                                                    _packageMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(SourceInfo.NO_INFO, "int"), 
                                                    new Word(SourceInfo.NO_INFO, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      cmd.visit(_bfv);
      assertEquals("There should be no errors", 0, errors.size());  // This can happen in a local inner class
//      assertEquals("The error message should be correct.", 
//                   "Methods definitions cannot appear within the body of another method or block.",
//                   errors.get(0).getFirst());
    }
    
    /* These last two tests are shared with ClassBodyIntermediateVisitor,
     * perhaps we could factor them out. */
    
    public void testForVariableDeclarationOnly() {
      // Check one that works
      VariableDeclarator[] vdecs = new VariableDeclarator[] {
        new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                            new PrimitiveType(SourceInfo.NO_INFO, "double"), 
                                            new Word (SourceInfo.NO_INFO, "field1")),
          new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                              new PrimitiveType(SourceInfo.NO_INFO, "boolean"), 
                                              new Word (SourceInfo.NO_INFO, "field2"))};
      VariableDeclaration vdecl = new VariableDeclaration(SourceInfo.NO_INFO, _packageMav, vdecs);
      
      VariableData vd1 = new VariableData("field1", _packageMav, SymbolData.DOUBLE_TYPE, false, _bfv._bodyData);
      VariableData vd2 = new VariableData("field2", _packageMav, SymbolData.BOOLEAN_TYPE, false, _bfv._bodyData);
      vdecl.visit(_bfv);
      assertEquals("There should not be any errors.", 0, errors.size());
      LinkedList<VariableData> vars = _md1.getVars();
//      for (int i = 0; i < vars.size(); i++) {
//        System.err.println(vars.get(i).getName() + " " + vars.get(i).getMav() + " " + vars.get(i).getType().getName() 
//                            + " " + vars.get(i).hasValue() + " " + vars.get(i).getEnclosingData().getName());
//      }
//      System.err.println("vars[0] = " + vars.get(0));
//      System.err.println("vd1 = " + vd1);
      assertTrue("field1 was added.", vars.contains(vd1));
      assertTrue("field2 was added.", vars.contains(vd2));
      
      // Check one that doesn't work
      VariableDeclaration vdecl2 = new VariableDeclaration(SourceInfo.NO_INFO,
                                                        _packageMav,
                                                        new VariableDeclarator[] {
        new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                            new PrimitiveType(SourceInfo.NO_INFO, "double"), 
                                            new Word (SourceInfo.NO_INFO, "field3")),
        new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                            new PrimitiveType(SourceInfo.NO_INFO, "int"), 
                                            new Word (SourceInfo.NO_INFO, "field3"))});
      VariableData vd3 = new VariableData("field3", _packageMav, SymbolData.DOUBLE_TYPE, false, _bfv._bodyData);
      vdecl2.visit(_bfv);
      assertEquals("There should still be no errors.", 0, errors.size());
      
/* The following test was commented out because of the kludge introduced in forVariableDeclarationOnly above */      
//      assertEquals("There should be one error.", 1, errors.size());
//      assertEquals("The error message should be correct", 
//                   "You cannot have two variables with the same name.", errors.get(0).getFirst());
      assertTrue("field3 was added.", _md1.getVars().contains(vd3));
    }
    
    public void testForTryCatchStatement() {
      //Make sure that no error is thrown
      BracedBody emptyBody = new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]);
      Block b = new Block(SourceInfo.NO_INFO, emptyBody);

      NormalTryCatchStatement ntcs = new NormalTryCatchStatement(SourceInfo.NO_INFO, b, new CatchBlock[0]);
      TryCatchFinallyStatement tcfs = new TryCatchFinallyStatement(SourceInfo.NO_INFO, b, new CatchBlock[0], b);
      ntcs.visit(_bfv);
      tcfs.visit(_bfv);
      assertEquals("After visiting NormalTryCatchStatement and TryCatchFinallyStatement, there should be no errors", 
                   0, errors.size());
      
      //make sure that if there is an error in one of the bodies, it is caught:   (this is an arbitrary error).
      BracedBody errorBody = new BracedBody(SourceInfo.NO_INFO, new BodyItemI[] {
        new ExpressionStatement(SourceInfo.NO_INFO, 
                                new BitwiseOrExpression(SourceInfo.NO_INFO, 
                                                        new IntegerLiteral(SourceInfo.NO_INFO, 1), 
                                                        new IntegerLiteral(SourceInfo.NO_INFO, 2)))});
      Block errorBlock = new Block(SourceInfo.NO_INFO, errorBody);
      
      ntcs = new NormalTryCatchStatement(SourceInfo.NO_INFO, errorBlock, new CatchBlock[0]);
      ntcs.visit(_bfv);
//      if (errors.size() > 0) System.err.println("Error was:" + errors.get(0).getFirst());
      assertEquals("Should be no errors", 0, errors.size());  // bitwise operations are allowed
      
      // make sure that if there is an error in one of the catch statements, it is caught: (this is an arbitrary error).
      UninitializedVariableDeclarator uvd = 
        new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                            new PrimitiveType(SourceInfo.NO_INFO, "int"), 
                                            new Word(SourceInfo.NO_INFO, "i"));
      FormalParameter fp = new FormalParameter(SourceInfo.NO_INFO, uvd, false);

      tcfs = new TryCatchFinallyStatement(SourceInfo.NO_INFO, b, new CatchBlock[] {
        new CatchBlock(SourceInfo.NO_INFO, fp, errorBlock)}, b);
        
     tcfs.visit(_bfv);
//     if (errors.size() > 0) System.err.println("Error was:" + errors.get(0).getFirst());
     assertEquals("Should be no errors", 0, errors.size());  // bitwise operations are allowed
    }
    
     public void testForInnerClassDef() {
     
      // Test a local inner class definition and reference
      SymbolData obj = new SymbolData("java.lang.Object");
      LanguageLevelConverter.symbolTable.put("java.lang.Object", obj);
      InnerClassDef cd0 = 
        new InnerClassDef(SourceInfo.NO_INFO, 
                          _packageMav, 
                          new Word(SourceInfo.NO_INFO, "Rod"),
                          new TypeParameter[0], 
                          new ClassOrInterfaceType(SourceInfo.NO_INFO, "java.lang.Object", new Type[0]), 
                          new ReferenceType[0], 
                          new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      cd0.visit(_bfv);
      assertEquals("There should be no errors", 0, errors.size());
      SymbolData innerClass = _bfv._bodyData.getInnerClassOrInterface("Rod");
      assertNotNull("Should have a inner class named Rod", innerClass);
           
      // Test one with explicit modifiers
      InnerClassDef cd1 = 
        new InnerClassDef(SourceInfo.NO_INFO, 
                          _publicMav, 
                          new Word(SourceInfo.NO_INFO, "Todd"),
                          new TypeParameter[0], 
                          new ClassOrInterfaceType(SourceInfo.NO_INFO, "java.lang.Object", new Type[0]), 
                          new ReferenceType[0], 
                          new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      cd1.visit(_bfv);
      assertEquals("There should be no errors", 0, errors.size());  // class modifiers are allowed
    }
    
     public void testForInnerInterfaceDef() {       
       //Test a trivial inner interface definition
       InnerInterfaceDef iid = 
         new InnerInterfaceDef(SourceInfo.NO_INFO, 
                               _packageMav, 
                               new Word(SourceInfo.NO_INFO, "Broken"),
                               new TypeParameter[0], 
                               new ReferenceType[0], 
                               new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
       iid.visit(_bfv);
       assertEquals("There should be no errors", 0, errors.size());
       
       // Test a inner interface definition and reference
       SymbolData obj = new SymbolData("java.lang.Object");
       LanguageLevelConverter.symbolTable.put("java.lang.Object", obj);
       InnerInterfaceDef id0 = 
         new InnerInterfaceDef(SourceInfo.NO_INFO, 
                               _packageMav, 
                               new Word(SourceInfo.NO_INFO, "RodInterface"),
                               new TypeParameter[0], 
                               new ReferenceType[0], 
                               new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
       id0.visit(_bfv);
       assertEquals("There should be no errors", 0, errors.size());
       SymbolData innerInterface = _bfv._bodyData.getInnerClassOrInterface("RodInterface");
       assertNotNull("Should have a inner interface named RodInterface", innerInterface);
       
       // Test one with explicit modifiers
      InnerInterfaceDef id1 = 
        new InnerInterfaceDef(SourceInfo.NO_INFO, 
                          _publicMav, 
                          new Word(SourceInfo.NO_INFO, "Todd"),
                          new TypeParameter[0], 
                          new ReferenceType[0], 
                          new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      id1.visit(_bfv);
      assertEquals("There should be no errors", 0, errors.size());  // class modifiers are allowed
     }
     public void testDummy() { }
  }
}