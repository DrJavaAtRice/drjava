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
 * This class enforces things that are common to all contexts reachable within a method body or other body 
 * (not class or interface body)the Advanced Language Level). 
 */
public class BodyBodyAdvancedVisitor extends AdvancedVisitor {

  /**The MethodData of this method.*/
  private BodyData _bodyData;
  
  /*
   * Constructor for BodyBodyAdvancedVisitor.
   * @param bodyData  The BodyData that encloses the context we are visiting.
   * @param file  The source file this came from.
   * @param packageName  The package the source file is in
   * @importedFiles  A list of classes that were specifically imported
   * @param importedPackages  A list of package names that were specifically imported
   * @param classDefsInThisFile  A list of the classes that are defined in the source file
   * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need to be resolved
   */
  public BodyBodyAdvancedVisitor(BodyData bodyData, File file, String packageName, LinkedList<String> importedFiles, 
                             LinkedList<String> importedPackages, LinkedList<String> classDefsInThisFile, Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations) {
    super(file, packageName, importedFiles, importedPackages, classDefsInThisFile, continuations);
    _bodyData = bodyData;
  }
  
  /*Give an appropriate error*/
  public void forMethodDefDoFirst(MethodDef that) {
    _addError("Methods definitions cannot appear within the body of another method or block.", that);
  }
  
  /* There is currently no way to differentiate between a block statement and
   * an instance initializer in a braced body given the general nature of a 
   * braced body.  Whenever an instance initialization is visited in a method
   * body, we must assume that it is a block statement.
   */
  public void forInstanceInitializer(InstanceInitializer that) {
    forBlock(that.getCode());
  }

  /*
   * Visit this BlockData with a new BodyBodyAdvanced visitor after making sure no errors need to be thrown.
   */
  public void forBlock(Block that) {
    forBlockDoFirst(that);
    if (prune(that)) { return; }
    BlockData bd = new BlockData(_bodyData);
    _bodyData.addBlock(bd);
    that.getStatements().visit(new BodyBodyAdvancedVisitor(bd, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));
    forBlockOnly(that);
  }
  
  /** 
   * Visit the block as in forBlock(), but first add the exception parameter as a variable in 
   * that block.
   */
  public void forCatchBlock(CatchBlock that) {
    forCatchBlockDoFirst(that);
    if (prune(that)) { return; }
    
    Block b = that.getBlock();
    forBlockDoFirst(b);
    if (prune(b)) { return; }
    BlockData bd = new BlockData(_bodyData);
    _bodyData.addBlock(bd);
    
    VariableData exceptionVar = formalParameters2VariableData(new FormalParameter[]{ that.getException() }, bd)[0];
    if (prune(that.getException())) { return; }
    bd.addVar(exceptionVar);
    
    b.getStatements().visit(new BodyBodyIntermediateVisitor(bd, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));
    forBlockOnly(b);
    forCatchBlockOnly(that);
  }
  
  /*Add the variables that were declared to the body data and make sure that no two
   * variables have the same name.*/
  public void forVariableDeclarationOnly(VariableDeclaration that) {
    if (!_bodyData.addVars(_variableDeclaration2VariableData(that, _bodyData))) {
      _addAndIgnoreError("You cannot have two variables with the same name.", that);
    }
  }
  
  /**
   * Call the super method to convert these to a VariableData array.
   * Do some special handling of the modifier "final" in the error message, since final can be used to declare a local variable.
   * @param enclosingData  The Data which contains the variables
   */
  protected VariableData[] _variableDeclaration2VariableData(VariableDeclaration vd, Data enclosingData) {
    VariableData[] vds = super._variableDeclaration2VariableData(vd, enclosingData);
    for (int i = 0; i < vds.length; i++) {
      if ((vds[i].hasModifier("final") && vds[i].getMav().getModifiers().length > 1) || (!vds[i].hasModifier("final") && vds[i].getMav().getModifiers().length > 0)) {
        StringBuffer sbuff = new StringBuffer("the keyword(s) ");
        String[] modifiers = vds[i].getMav().getModifiers();
        for (int j = 0; j<modifiers.length; j++) {
          if (!modifiers[j].equals("final")) {sbuff.append("\"" + modifiers[j] + "\" ");}
        }
        _addAndIgnoreError("You cannot use " + sbuff.toString() + "to declare a local variable", vd);
      }
    }
    return vds;
  }
  
  
  
  /**Override method in AdvancedVisitor that throws an error here.*/
  public void forTryCatchStatementDoFirst(TryCatchStatement that) {
    //do nothing!  No errors to throw here.
  }

  /*
   * Make sure that no modifiers appear before the InnerClassDef, and then delegate.
   */
  public void forInnerClassDef(InnerClassDef that) {
    if (that.getMav().getModifiers().length > 0) {_addAndIgnoreError("No modifiers may appear before a class declaration here", that.getMav());}
    handleInnerClassDef(that, _bodyData, getQualifiedClassName(_bodyData.getSymbolData().getName()) + "$" + _bodyData.getSymbolData().preincrementLocalClassNum() + that.getName().getText());
  }
  
  /**
   * Give an error, since InnerInterfaces cannot appear here.
   */
  public void forInnerInterfaceDef(InnerInterfaceDef that) {
    _addError("Inner interface declarations cannot appear here", that);
  }
  
  /**
   * Delegate to method in LLV
   */
  public void forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    complexAnonymousClassInstantiationHelper(that, _bodyData);
  }

  /**
   * Delegate to method in LLV
   */
  public void forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    simpleAnonymousClassInstantiationHelper(that, _bodyData);
  }

    
   /**
    * Test most of the methods declared above right here:
    */
  public static class BodyBodyAdvancedVisitorTest extends TestCase {
    
    private BodyBodyAdvancedVisitor _bav;
    
    private SymbolData _sd1;
    private MethodData _md1;
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[0]);
    private ModifiersAndVisibility _abstractMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"final"});
    private ModifiersAndVisibility _staticMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"static"});
    
    
    public BodyBodyAdvancedVisitorTest() {
      this("");
    }
    
    public BodyBodyAdvancedVisitorTest(String name) {
      super(name);
    }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");
      _md1 = new MethodData("methodName", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                                   new VariableData[0], 
                                   new String[0],
                                   _sd1,
                                   null);

      errors = new LinkedList<Pair<String, JExpressionIF>>();
      symbolTable = new Symboltable();
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      _hierarchy = new Hashtable<String, TypeDefBase>();
      _classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
      _bav = new BodyBodyAdvancedVisitor(_md1, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>(), new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
      
      _bav.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      _bav._resetNonStaticFields();
      _bav._importedPackages.addFirst("java.lang");
      _bav._newSDs = new Hashtable<SymbolData, LanguageLevelVisitor>();
      _errorAdded = false;
    }
    
    public void testForMethodDefDoFirst() {
      ConcreteMethodDef cmd = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _packageMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cmd.visit(_bav);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Methods definitions cannot appear within the body of another method or block.",
                   errors.get(0).getFirst());
    }
    
    /* These last two tests are shared with ClassBodyIntermediateVisitor,
     * perhaps we could factor them out. */
    
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
      VariableData vd1 = new VariableData("field1", _packageMav, SymbolData.DOUBLE_TYPE, false, _bav._bodyData);
      VariableData vd2 = new VariableData("field2", _packageMav, SymbolData.BOOLEAN_TYPE, false, _bav._bodyData);
      vdecl.visit(_bav);
      assertEquals("There should not be any errors.", 0, errors.size());
      LinkedList<VariableData> vds = _md1.getVars();
//      for (int i = 0; i<vds.size(); i++) {
//        System.out.println(vds.get(i).getName() + " " + vds.get(i).getMav() + " " + vds.get(i).getType().getName() + " " +
//                           vds.get(i).hasValue() + " " + vds.get(i).getEnclosingData().getName());
//      }
      assertTrue("field1 was added.", _md1.getVars().contains(vd1));
      assertTrue("field2 was added.", _md1.getVars().contains(vd2));
      
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
      VariableData vd3 = new VariableData("field3", _packageMav, SymbolData.DOUBLE_TYPE, false, _bav._bodyData);
      vdecl2.visit(_bav);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct", "You cannot have two variables with the same name.", errors.get(0).getFirst());
      assertTrue("field3 was added.", _md1.getVars().contains(vd3));
    }
    
    public void testForTryCatchStatement() {
      //Make sure that no error is thrown
      BracedBody emptyBody = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]);
      Block b = new Block(JExprParser.NO_SOURCE_INFO, emptyBody);

      NormalTryCatchStatement ntcs = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[0]);
      TryCatchFinallyStatement tcfs = new TryCatchFinallyStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[0], b);
      ntcs.visit(_bav);
      tcfs.visit(_bav);
      assertEquals("After visiting both NormalTryCatchStatement and TryCatchFinallyStatement, there should be no errors", 0, errors.size());
      
      //make sure that if there is an error in one of the bodies, it is caught:   (this is an arbitrary error).
      BracedBody errorBody = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {
        new ExpressionStatement(JExprParser.NO_SOURCE_INFO, 
                                new BitwiseOrExpression(JExprParser.NO_SOURCE_INFO, new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 1), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 2)))});
      Block errorBlock = new Block(JExprParser.NO_SOURCE_INFO, errorBody);
      
      ntcs = new NormalTryCatchStatement(JExprParser.NO_SOURCE_INFO, errorBlock, new CatchBlock[0]);
      ntcs.visit(_bav);
      assertEquals("Should be one error", 1, errors.size());
      assertEquals("Error message should be correct", "Bitwise or expressions cannot be used at any language level.  Perhaps you meant to compare two values using regular or (||)", errors.get(0).getFirst());
      
      //make sure that if there is an error in one of the catch statements, it is caught: (this is an arbitrary error).
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), new Word(JExprParser.NO_SOURCE_INFO, "i"));
      FormalParameter fp = new FormalParameter(JExprParser.NO_SOURCE_INFO, uvd, false);

      tcfs = new TryCatchFinallyStatement(JExprParser.NO_SOURCE_INFO, b, new CatchBlock[] {
        new CatchBlock(JExprParser.NO_SOURCE_INFO, fp, errorBlock)}, b);
        
     tcfs.visit(_bav);
     assertEquals("Should be two errors", 2, errors.size());
     assertEquals("Error message should be correct", "Bitwise or expressions cannot be used at any language level.  Perhaps you meant to compare two values using regular or (||)", errors.get(1).getFirst());
    }
    
    public void testForInnerClassDef() {
     
      //test one that works
      SymbolData obj = new SymbolData("java.lang.Object");
      symbolTable.put("java.lang.Object", obj);
      InnerClassDef cd0 = new InnerClassDef(JExprParser.NO_SOURCE_INFO, _packageMav, new Word(JExprParser.NO_SOURCE_INFO, "Rod"),
                                       new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), new ReferenceType[0], 
                                       new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

      cd0.visit(_bav);
      assertEquals("There should be no errors", 0, errors.size());
      SymbolData innerClass = _bav._bodyData.getInnerClassOrInterface("Rod");
      assertNotNull("Should have a inner class named Rod", innerClass);
      
      //test one with explicit modifiers
      InnerClassDef cd1 = new InnerClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, new Word(JExprParser.NO_SOURCE_INFO, "Todd"),
                                       new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), new ReferenceType[0], 
                                            new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cd1.visit(_bav);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "No modifiers may appear before a class declaration here", errors.getLast().getFirst());

      

    }
    
    public void testForInnerInterfaceDef() {
      //There should always be an error
      InnerInterfaceDef iid = new InnerInterfaceDef(JExprParser.NO_SOURCE_INFO, _packageMav, new Word(JExprParser.NO_SOURCE_INFO, "Broken"),
                                                    new TypeParameter[0], new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      iid.visit(_bav);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "Inner interface declarations cannot appear here", errors.getLast().getFirst());
      
    }
    
  }
}