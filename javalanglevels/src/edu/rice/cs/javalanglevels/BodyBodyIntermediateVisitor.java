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

/** Language Level Visitor for the Intermediate Language Level.  Enforces constraints during the
  * first walk of the AST (checking for language specific errors and building the symbol table).
  * This class enforces things that are common to all contexts reachable within a method body or other body 
  * (not class or interface body) at the Intermediate Language Level). 
  */
public class BodyBodyIntermediateVisitor extends IntermediateVisitor {

  /**The MethodData of this method.*/
  private BodyData _bodyData;
  
  /** Constructor for BodyBodyElementaryVisitor.
    * @param bodyData  The BodyData that encloses the context we are visiting.
    * @param file  The source file this came from.
    * @param packageName  The package the source file is in
    * @importedFiles  A list of classes that were specifically imported
    * @param importedPackages  A list of package names that were specifically imported
    * @param classDefsInThisFile  A list of the classes that are defined in the source file
    * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need 
    *                       to be resolved
    */
  public BodyBodyIntermediateVisitor(BodyData bodyData,
                                     File file, 
                                     String packageName, 
                                     LinkedList<String> importedFiles, 
                                     LinkedList<String> importedPackages,
                                     LinkedList<String> classDefsInThisFile, 
                                     Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations,
                                     LinkedList<String> innerClassesToBeParsed) {
    super(file, 
          packageName, 
          importedFiles, 
          importedPackages, 
          classDefsInThisFile, 
          continuations);
    _bodyData = bodyData;
    _innerClassesToBeParsed = innerClassesToBeParsed;
  }
  
  /*Give an appropriate error*/
  public Void forMethodDefDoFirst(MethodDef that) {
    _addError("Methods definitions cannot appear within the body of another method or block.", that);
    return null;
  }
  
  /* There is currently no way to differentiate between a block statement and
   * an instance initializer in a braced body given the general nature of a 
   * braced body.  Whenever an instance initialization is visited in a method
   * body, we must assume that it is a block statement.
   */
  public Void forInstanceInitializer(InstanceInitializer that) {
    return forBlock(that.getCode());
  }

 /* Visit this BlockData with a new BodyBodyIntermediate visitor after making sure no errors need to be thrown.*/
  public Void forBlock(Block that) {
    forBlockDoFirst(that);
    if (prune(that)) return null;
    BlockData bd = new BlockData(_bodyData);
    _bodyData.addBlock(bd);
    that.getStatements().visit(new BodyBodyIntermediateVisitor(bd, _file, _package, _importedFiles, _importedPackages,
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
    
    b.getStatements().visit(new BodyBodyIntermediateVisitor(bd, _file, _package, _importedFiles, _importedPackages, 
                                                            _classNamesInThisFile, continuations, 
                                                            _innerClassesToBeParsed));
    forBlockOnly(b);
    return forCatchBlockOnly(that);
  }
  
  /** Adds the variables that were declared to the body data and make sure that no two variables have the same name.*/
  public Void forVariableDeclarationOnly(VariableDeclaration that) {
    if (! _bodyData.addFinalVars(_variableDeclaration2VariableData(that, _bodyData))) {
//      System.err.println("Generating duplicate variable error");
      _addAndIgnoreError("You cannot have two variables with the same name.", that);
    }
    return null;
  }
  
  /** Override method in IntermediateVisitor that throws an error here.*/
  public Void forTryCatchStatementDoFirst(TryCatchStatement that) { return null; /*  No errors to throw here. */ }
  
  /** Process a local inner class definition */
  public Void forInnerClassDef(InnerClassDef that) {
//    System.err.println("BBIV.forInnerClassDef called on " + that.getName());
    handleInnerClassDef(that, _bodyData, getQualifiedClassName(_bodyData.getSymbolData().getName()) + "." + 
                        _bodyData.getSymbolData().preincrementLocalClassNum() + that.getName().getText());
    return null;
  }
  
   /** Process a local inner interface definition */
  public Void forInnerInterfaceDef(InnerInterfaceDef that) {
    handleInnerInterfaceDef(that, _bodyData, getQualifiedClassName(_bodyData.getSymbolData().getName()) + "." + 
                        _bodyData.getSymbolData().preincrementLocalClassNum() + that.getName().getText());
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
  
  /** If this is the body of a constructor, referencing 'this' is illegal. So, check to see if this is a constructor,
    * and if so, throw an error. This should catch both the ComplexThisReference and the SimpleThisReference case.
    */
  //TODO: it might be nice to create a ConstructorBodyIntermediateVisitor, so this check is not necessary here.
  public Void forThisReferenceDoFirst(ThisReference that) {
    if (isConstructor(_bodyData)) {
      _addAndIgnoreError("You cannot reference the field 'this' inside a constructor at the Intermediate Level", that);
    }
    return null;
  }

  /** Call the super method to convert these to a VariableData array, then make sure that
    * each VariableData is set to be final, as required at the Intermediate level.
    * @param enclosingData  The Data which contains the variables
    */
  protected VariableData[] _variableDeclaration2VariableData(VariableDeclaration vd, Data enclosingData) {
    VariableData[] vds = llVariableDeclaration2VariableData(vd, enclosingData);
    for (int i = 0; i < vds.length; i++) {
      if (vds[i].getMav().getModifiers().length > 0) {
        StringBuilder s = new StringBuilder("the keyword(s) ");
        String[] modifiers = vds[i].getMav().getModifiers();
        for (String m: modifiers) { if (! m.equals("final")) s.append("\"" + m + "\" "); }
        _addAndIgnoreError("You cannot use " + s + "to declare a local variable", vd);
      }
      vds[i].setFinal();
      vds[i].setIsLocalVariable(true);
    }
//    System.err.println("Return VariableDatas " + vds);
    return vds;
  }
  
//  /** Check for problems with modifiers that are specific to method definitions. */
//  public Void forModifiersAndVisibilityDoFirst(ModifiersAndVisibility that) {
//    String[] modifiers = that.getModifiers();
//    if (Utilities.isAbstract(modifiers) && Utilities.isStatic(modifiers))  _badModifiers("static", "abstract", that);
//    return super.forModifiersAndVisibilityDoFirst(that);
//  }
  
  /** Test most of the methods declared above right here: */
  public static class BodyBodyIntermediateVisitorTest extends TestCase {
    
    private BodyBodyIntermediateVisitor _bbv;
    
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
    
    
    public BodyBodyIntermediateVisitorTest() { this(""); }
    
    public BodyBodyIntermediateVisitorTest(String name) { super(name); }
    
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
      _bbv = 
        new BodyBodyIntermediateVisitor(_md1, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), 
                                        new LinkedList<String>(), 
                                        new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>(),
                                        new LinkedList<String>());
      _bbv._classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
      _bbv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      _bbv._resetNonStaticFields();
      _bbv._importedPackages.addFirst("java.lang");
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
      cmd.visit(_bbv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Methods definitions cannot appear within the body of another method or block.",
                   errors.get(0).getFirst());
    }
    
    /* These last two tests are shared with ClassBodyIntermediateVisitor,
     * perhaps we could factor them out. */
    
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
      VariableData vd1 = new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, false, _bbv._bodyData);
      VariableData vd2 = new VariableData("field2", _finalMav, SymbolData.BOOLEAN_TYPE, false, _bbv._bodyData);
      vdecl.visit(_bbv);
      assertEquals("There should not be any errors.", 0, errors.size());
      assertTrue("field1 was added.", _md1.getVars().contains(vd1));
      assertTrue("field2 was added.", _md1.getVars().contains(vd2));
      
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
      VariableData vd3 = new VariableData("field3", _finalMav, SymbolData.DOUBLE_TYPE, false, _bbv._bodyData);
      vdecl2.visit(_bbv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct", "You cannot have two variables with the same name.", 
                   errors.get(0).getFirst());
      assertTrue("field3 was added.", _md1.getVars().contains(vd3));
    }
    
//    public void testForOtherExpressionOnly() {
//      // Test that if the OtherExpressino contains a Word, that the Word is resolved.
//      assertFalse("java.lang.System should not be in the symbolTable.", 
//                  LanguageLevelConverter.symbolTable.containsKey("java.lang.System"));
//      Expression ex = 
//        new Expression( SourceInfo.NO_INFO,
//                       new ExpressionPiece[] { new OtherExpression(SourceInfo.NO_INFO, 
//                                                                   new Word(SourceInfo.NO_INFO, "System"))});
//      ex.visit(_bbv);
//////      System.out.println(errors.get(0).getFirst());
////      for (int i = 0; i < errors.size(); i++)
////        System.out.println(errors.get(i).getFirst());
//      assertEquals("There should not be any errors.", 0, errors.size());
//      assertTrue("java.lang.System should be in the symbolTable.", 
//                 LanguageLevelConverter.symbolTable.containsKey("java.lang.System"));
//    }
    
    public void testForTryCatchStatement() {
      //Make sure that no error is thrown
      BracedBody emptyBody = new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]);
      Block b = new Block(SourceInfo.NO_INFO, emptyBody);

      NormalTryCatchStatement ntcs = new NormalTryCatchStatement(SourceInfo.NO_INFO, b, new CatchBlock[0]);
      TryCatchFinallyStatement tcfs = new TryCatchFinallyStatement(SourceInfo.NO_INFO, b, new CatchBlock[0], b);
      ntcs.visit(_bbv);
      tcfs.visit(_bbv);
      assertEquals("After visiting both NormalTryCatchStatement and TryCatchFinallyStatement, there should be no " 
                     + "errors", 0, errors.size());
      
      //make sure that if there is an error in one of the bodies, it is caught:
      BracedBody errorBody = new BracedBody(SourceInfo.NO_INFO, new BodyItemI[] {
        new ExpressionStatement(SourceInfo.NO_INFO, 
                                new BitwiseOrExpression(SourceInfo.NO_INFO, 
                                                        new SimpleNameReference(SourceInfo.NO_INFO, 
                                                                                new Word(SourceInfo.NO_INFO, "i")), 
                                                        new IntegerLiteral(SourceInfo.NO_INFO, 10)))});
      Block errorBlock = new Block(SourceInfo.NO_INFO, errorBody);
      
      ntcs = new NormalTryCatchStatement(SourceInfo.NO_INFO, errorBlock, new CatchBlock[0]);
      ntcs.visit(_bbv);
      assertEquals("Should be one error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "Bitwise or expressions cannot be used at any language level.  " 
                     + "Perhaps you meant to compare two values using regular or (||)", 
                   errors.getLast().getFirst());
      
      //make sure that if there is an error in one of the catch statements, it is caught:
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(SourceInfo.NO_INFO, new PrimitiveType(SourceInfo.NO_INFO, "int"), new Word(SourceInfo.NO_INFO, "i"));
      FormalParameter fp = new FormalParameter(SourceInfo.NO_INFO, uvd, false);

      tcfs = new TryCatchFinallyStatement(SourceInfo.NO_INFO, b, new CatchBlock[] {
        new CatchBlock(SourceInfo.NO_INFO, fp, errorBlock)}, b);
        
     tcfs.visit(_bbv);
     assertEquals("Should be two errors", 2, errors.size());
     assertEquals("Error message should be correct", "Bitwise or expressions cannot be used at any language level.  Perhaps you meant to compare two values using regular or (||)", errors.getLast().getFirst());
    }
    
    public void testForThisReferenceDoFirst() {
      SimpleThisReference str = new SimpleThisReference(SourceInfo.NO_INFO);
      ComplexThisReference ctr = new ComplexThisReference(SourceInfo.NO_INFO, new SimpleNameReference(SourceInfo.NO_INFO, new Word(SourceInfo.NO_INFO, "field")));

      //if a this reference occurs outside of a constructor, no error
      _bbv._bodyData = _md1;
      str.visit(_bbv);
      ctr.visit(_bbv);
      assertEquals("Should be no errors", 0, errors.size());
           
      
      //if a this reference occurs in a constructor, give an error
      MethodData constr = new MethodData("monkey", _publicMav, new TypeParameter[0], _sd1, 
                                   new VariableData[0], 
                                   new String[0],
                                   _sd1,
                                   null);
      _bbv._bodyData = constr;
      str.visit(_bbv);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "You cannot reference the field 'this' inside a constructor at the Intermediate Level", errors.getLast().getFirst());
      
      ctr.visit(_bbv);
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "You cannot reference the field 'this' inside a constructor at the Intermediate Level", errors.getLast().getFirst());
      
    }
    
  }
}