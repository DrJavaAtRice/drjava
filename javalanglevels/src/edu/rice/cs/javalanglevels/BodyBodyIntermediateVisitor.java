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
    * @param classesInThisFile  A list of the classes that are yet to be defined in this source file
    * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need 
    *                       to be resolved
    * @param fixUps  A list of commands to be performed after this pass to fixup the symbolTable
    */
  public BodyBodyIntermediateVisitor(BodyData bodyData,
                                     File file, 
                                     String packageName, 
                                     String enclosingClassName,
                                     LinkedList<String> importedFiles, 
                                     LinkedList<String> importedPackages,
                                     HashSet<String> classesInThisFile, 
                                     Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations,
                                     LinkedList<Command> fixUps,
                                     HashSet<String> innerClassesInThisBody) {
    super(file, 
          packageName,
          enclosingClassName,
          importedFiles, 
          importedPackages, 
          classesInThisFile, 
          continuations,
          fixUps);
    _bodyData = bodyData;
    assert _enclosingClassName != null;
//    _innerClassesInThisBody = innerClassesInThisBody;
  }
  
  /** Give an appropriate error */
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

 /* Visit this Block within the same BlockData with a new BodyBodyIntermediate visitor (to deal with new local scope?)
  * after making sure no errors need to be thrown.*/
  public Void forBlock(Block that) {
    forBlockDoFirst(that);
    if (prune(that)) return null;
    // The following code is incomprehensible.  Why mutate _bodyData?  Why create bd?
    BlockData bd = new BlockData(_bodyData);
    _bodyData.addBlock(bd);
    that.getStatements().visit(new BodyBodyIntermediateVisitor(bd, _file, _package, _enclosingClassName, _importedFiles,
                                                               _importedPackages, _classesInThisFile, continuations, 
                                                               fixUps, new HashSet<String>()));
    return forBlockOnly(that);
  }
  
  /** Visit the block as in forBlock(), but first add the exception parameter as a variable in that 
    * lock. 
    * TODO: move this method into LanguageLevelVisitor. */
  public Void forCatchBlock(CatchBlock that) {
//    System.err.println("***ALARM*** BodyBodyIntermediateVisitor is visiting catch block");
    forCatchBlockDoFirst(that);
    if (prune(that)) return null;
    
    Block b = that.getBlock();
    forBlockDoFirst(b);
    if (prune(b)) return null;
    BlockData bd = new BlockData(_bodyData);
    _bodyData.addBlock(bd);
    
    SymbolData enclosing = getQualifiedSymbolData(_enclosingClassName);
   
    VariableData exceptionVar = 
      formalParameters2VariableData(new FormalParameter[]{ that.getException() }, enclosing)[0];  // !!!!!! Wny not bd?
    if (prune(that.getException())) return null;
    bd.addVar(exceptionVar);
    
//    System.err.println("Visiting augmented catch block with new visitor!");
    BodyBodyIntermediateVisitor bbijv = 
      new BodyBodyIntermediateVisitor(bd, _file, _package, _enclosingClassName, _importedFiles,
                                      _importedPackages, _classesInThisFile, continuations, fixUps,
                                      new HashSet<String>());
    b.getStatements().visit(bbijv);
    forBlockOnly(b);
    return forCatchBlockOnly(that);
  }
  
  /** Adds the variables that were declared to the body data and make sure that no two variables have the same name.*/
  public Void forVariableDeclarationOnly(VariableDeclaration that) {
    if (! _bodyData.addFinalVars(_variableDeclaration2VariableData(that, _bodyData))) {
      /* Does not allow repeated use of a for loop index variable.  TODO: fix this. */
//      System.err.println("Generating duplicate variable error");
      _addAndIgnoreError("You cannot have two variables with the same name.", that);
    }
    return null;
  }
  
//  /** Override method in IntermediateVisitor that throws an error here.*/
//  public Void forTryCatchStatementDoFirst(TryCatchStatement that) { return null; /*  No errors to throw here. */ }
  
  /** Process a local inner class definition */
  public Void forInnerClassDef(InnerClassDef that) {
    // TODO: is this necessarily local?
    SymbolData enclosingClass = _bodyData.getSymbolData();
    assert _enclosingClassName != null;
    assert enclosingClass != null;
    assert _enclosingClassName.equals(getQualifiedClassName(enclosingClass.getName()));
    
    String relName = that.getName().getText();
    String fullName = _enclosingClassName + '$' + enclosingClass.preincrementLocalClassNum() + relName;
//    System.err.println("***ALARM*** Processing local class '" + relName + "' in class " + _enclosingClassName
//                         + " with flattened class name " + fullName);
    handleInnerClassDef(that, _bodyData, relName, fullName);
    // How do we know that generated number is correct?
    return null;
  }
  
  /** Process a local inner interface definition */
  public Void forInnerInterfaceDef(InnerInterfaceDef that) {
//    System.err.println("***Signalling local interface error");
    _addAndIgnoreError("Local interfaces are illegal in Java.", that);
//    handleInnerInterfaceDef(that, _bodyData, getQualifiedClassName(_bodyData.getSymbolData().getName()) + '.' + 
//                        _bodyData.getSymbolData().preincrementLocalClassNum() + that.getName().getText());
//    // How do we know that generated number is correct?
    return null;
  }

  /** Delegate to helper method. */
  public Void forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    SymbolData enclosing = getQualifiedSymbolData(_enclosingClassName);
    assert enclosing != null;
    simpleAnonymousClassInstantiationHelper(that, enclosing);
    return null;
  }
  
  /** Delegate to helper method. */
  public Void forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    SymbolData enclosing = getQualifiedSymbolData(_enclosingClassName);
    assert enclosing != null;
    complexAnonymousClassInstantiationHelper(that, enclosing);  // TODO: the wrong enclosing context?
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

  /** Process a local variable declaration within a method.  Calls the super method to convert these to a VariableData
    * array, then makes sure that each VariableData is set to be final, as required at the Intermediate level.
    * @param enclosingData  The Data which contains the variables
    */
  protected VariableData[] _variableDeclaration2VariableData(VariableDeclaration vd, Data enclosingData) {
    VariableData[] vds = super._variableDeclaration2VariableData(vd, enclosingData);
    for (int i = 0; i < vds.length; i++) {
      if (vds[i].getMav().getModifiers().length > 0) {
        StringBuilder s = new StringBuilder("the keyword(s) ");
        String[] modifiers = vds[i].getMav().getModifiers();
        for (String m: modifiers) { if (! m.equals("final")) s.append("\"" + m + "\" "); }
        _addAndIgnoreError("You cannot use " + s + "to declare a local variable", vd);
      }
      if (! vds[i].isFinal()) vds[i].setFinal();
      vds[i].setIsLocalVariable(true);  // Was commented out.  Why ????
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
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav =
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[0]);
    private ModifiersAndVisibility _abstractMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final"});
    
    
    public BodyBodyIntermediateVisitorTest() { this(""); }
    
    public BodyBodyIntermediateVisitorTest(String name) { super(name); }
    
    public void setUp() {
      _sd1 = new SymbolData("ILikeMonkey");
      _sd1.setIsContinuation(false);
      
      _md1 = new MethodData("methodName", 
                            _publicMav, 
                            new TypeParameter[0], 
                            SymbolData.INT_TYPE, 
                            new VariableData[0], 
                            new String[0],
                            _sd1,
                            null);

      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable.clear();
      LanguageLevelConverter._newSDs.clear();
      LanguageLevelConverter.symbolTable.put("ILikeMonkey", _sd1);
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
//      _hierarchy = new Hashtable<String, TypeDefBase>();
      
      _bbv = 
        new BodyBodyIntermediateVisitor(_md1, 
                                        new File(""), 
                                        "", 
                                        "ILikeMonkey", 
                                        new LinkedList<String>(), 
                                        new LinkedList<String>(), 
                                        new HashSet<String>(), 
                                        new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                        new LinkedList<Command>(),
                                        new HashSet<String>());
      
      _bbv._classesInThisFile = new HashSet<String>();
      _bbv.continuations = new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>();
//      _bbv._resetNonStaticFields();
      _bbv._importedPackages.addFirst("java.lang");
      _sd1.setSuperClass(_bbv.getQualifiedSymbolData("java.lang.Object"));

      _errorAdded = false;
    }
    
    public void testForMethodDefDoFirst() {
      ConcreteMethodDef cmd = new ConcreteMethodDef(SourceInfo.NONE, 
                                                    _packageMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(SourceInfo.NONE, "int"), 
                                                    new Word(SourceInfo.NONE, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      cmd.visit(_bbv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Methods definitions cannot appear within the body of another method or block.",
                   errors.get(0).getFirst());
    }
    
    /* These last two tests are shared with ClassBodyIntermediateVisitor, perhaps we could factor them out. */
    
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
      VariableData vd1 = new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, false, _bbv._bodyData);
      VariableData vd2 = new VariableData("field2", _finalMav, SymbolData.BOOLEAN_TYPE, false, _bbv._bodyData);
      vdecl.visit(_bbv);
//      if (errors.size() > 0) System.err.println("Error was:" + errors.get(0).getFirst());
      assertEquals("There should not be any errors.", 0, errors.size());
      assertTrue("field1 was added.", _md1.getVars().contains(vd1));
      assertTrue("field2 was added.", _md1.getVars().contains(vd2));
      
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
//        new Expression( SourceInfo.NONE,
//                       new ExpressionPiece[] { new OtherExpression(SourceInfo.NONE, 
//                                                                   new Word(SourceInfo.NONE, "System"))});
//      ex.visit(_bbv);
//////      System.out.println(errors.get(0).getFirst());
////      for (int i = 0; i < errors.size(); i++)
////        System.out.println(errors.get(i).getFirst());
//      assertEquals("There should not be any errors.", 0, errors.size());
//      assertTrue("java.lang.System should be in the symbolTable.", 
//                 LanguageLevelConverter.symbolTable.containsKey("java.lang.System"));
//    }
    
    /** Generate a block containing a bitwise or operation and the specified SourceInfo si.   Note: we need
      * to generate error blocks with distinct literal data to avoid generating duplicate error messages
      * which are suppressed in the cumulative errors table. */
    private Block _generateErrorBlock(int litValue) {
      final SourceInfo si = SourceInfo.NONE;
      BracedBody errorBody = new BracedBody(si, new BodyItemI[] {
        new ExpressionStatement(si, 
                                new BitwiseOrExpression(si, 
                                                        new SimpleNameReference(si, 
                                                                                new Word(si, "i")), 
                                                        new IntegerLiteral(si, litValue)))});
      return new Block(si, errorBody);
    }
      
      
    public void testForTryCatchStatement() {
      //Make sure that no error is thrown
      BracedBody emptyBody = new BracedBody(SourceInfo.NONE, new BodyItemI[0]);
      Block b = new Block(SourceInfo.NONE, emptyBody);

      NormalTryCatchStatement ntcs = new NormalTryCatchStatement(SourceInfo.NONE, b, new CatchBlock[0]);
      TryCatchFinallyStatement tcfs = new TryCatchFinallyStatement(SourceInfo.NONE, b, new CatchBlock[0], b);
      ntcs.visit(_bbv);
      tcfs.visit(_bbv);
      assertEquals("After visiting both NormalTryCatchStatement and TryCatchFinallyStatement, there should be no " 
                     + "errors", 0, errors.size());
      
//      //make sure that if there is an error in one of the bodies, it is caught:
//      BracedBody errorBody = new BracedBody(SourceInfo.NONE, new BodyItemI[] {
//        new ExpressionStatement(SourceInfo.NONE, 
//                                new BitwiseOrExpression(SourceInfo.NONE, 
//                                                        new SimpleNameReference(SourceInfo.NONE, 
//                                                                                new Word(SourceInfo.NONE, "i")), 
//                                                        new IntegerLiteral(SourceInfo.NONE, 10)))});
//      Block errorBlock = new Block(SourceInfo.NONE, errorBody);
      
//      assert ! SourceInfo.TEST_0.equals(SourceInfo.TEST_1);
      ntcs = new NormalTryCatchStatement(SourceInfo.TEST_0, _generateErrorBlock(0), new CatchBlock[0]);
      ntcs.visit(_bbv);
      assertEquals("Should be one error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "Bitwise or expressions cannot be used in the functional language level.  " 
                     + "Perhaps you meant to compare two values using regular or (||)", 
                   errors.getLast().getFirst());
      
      //make sure that if there is an error in one of the catch statements, it is caught:
      UninitializedVariableDeclarator uvd = 
        new UninitializedVariableDeclarator(SourceInfo.TEST_1, 
                                            new PrimitiveType(SourceInfo.TEST_1, "int"), 
                                            new Word(SourceInfo.TEST_1, "i"));
      FormalParameter fp = new FormalParameter(SourceInfo.TEST_1, uvd, false);

      tcfs = new TryCatchFinallyStatement(SourceInfo.TEST_1, b, new CatchBlock[] {
        new CatchBlock(SourceInfo.TEST_1, fp, _generateErrorBlock(1))
      }, b);
      
     assertEquals("Should be one error", 1, errors.size());
     tcfs.visit(_bbv);
     assertEquals("Should be two errors", 2, errors.size());
     assertEquals("Error message should be correct", 
                  "Bitwise or expressions cannot be used in the functional language level."
                  + "  Perhaps you meant to compare two values using regular or (||)", 
                  errors.getLast().getFirst());
    }
    
    public void testForThisReferenceDoFirst() {
      SimpleThisReference str = new SimpleThisReference(SourceInfo.NONE);
      ComplexThisReference ctr = 
        new ComplexThisReference(SourceInfo.NONE, 
                                 new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "field")));

      //if a this reference occurs outside of a constructor, no error
      _bbv._bodyData = _md1;
      str.visit(_bbv);
      ctr.visit(_bbv);
      assertEquals("Should be no errors", 0, errors.size());
           
      
      //if a this reference occurs in a constructor, give an error
      MethodData constr = new MethodData("ILikeMonkey", _publicMav, new TypeParameter[0], _sd1, 
                                   new VariableData[0], 
                                   new String[0],
                                   _sd1,
                                   null);
      _bbv._bodyData = constr;
      str.visit(_bbv);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot reference the field 'this' inside a constructor at the Intermediate Level", 
                   errors.getLast().getFirst());
      
      ctr.visit(_bbv);
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", 
                   "You cannot reference the field 'this' inside a constructor at the Intermediate Level", 
                   errors.getLast().getFirst());
      
      
    }
    
     public void testForInnerClassDef() {
     
      // Test a local inner class definition and reference
      SymbolData obj = new SymbolData("ILikeMonkey");
      LanguageLevelConverter.symbolTable.put("ILikeMonkey", obj);
      InnerClassDef cd0 = 
        new InnerClassDef(SourceInfo.NONE, 
                          _packageMav, 
                          new Word(SourceInfo.NONE, "Rod"),
                          new TypeParameter[0], 
                          new ClassOrInterfaceType(SourceInfo.NONE, "ILikeMonkey", new Type[0]), 
                          new ReferenceType[0], 
                          new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      cd0.visit(_bbv);
      assertEquals("There should be no errors", 0, errors.size());
      SymbolData innerClass1 = _bbv._bodyData.getInnerClassOrInterface("Rod");
      assertNotNull("Should have a inner class named Rod", innerClass1);
      
      // Test one with explicit modifiers
      InnerClassDef cd1 = 
        new InnerClassDef(SourceInfo.NONE, 
                          _publicMav, 
                          new Word(SourceInfo.NONE, "Todd"),
                          new TypeParameter[0], 
                          new ClassOrInterfaceType(SourceInfo.NONE, "ILikeMonkey", new Type[0]), 
                          new ReferenceType[0], 
                          new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      cd1.visit(_bbv);
      assertEquals("There should be no errors", 0, errors.size());  // modifiers are allowed
      SymbolData innerClass2 = _bbv._bodyData.getInnerClassOrInterface("Todd");
      assertNotNull("Should have a inner class named Todd", innerClass2);
     }
     
     public void testForInnerInterfaceDef() {       
       //Test a trivial inner interface definition
       InnerInterfaceDef iid = 
         new InnerInterfaceDef(SourceInfo.NONE, 
                               _packageMav, 
                               new Word(SourceInfo.NONE, "Broken"),
                               new TypeParameter[0], 
                               new ReferenceType[0], 
                               new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
       iid.visit(_bbv);
       assertEquals("There should be one error", 1, errors.size());
       assertEquals("The error message should be correct", 
                    "Local interfaces are illegal in Java.", errors.get(0).getFirst());
       SymbolData innerInterface = _bbv._bodyData.getInnerClassOrInterface("Broken");
       assertNull("Should NOT have a inner interface named Broken", innerInterface);
       
       // Test a inner interface definition and reference
       InnerInterfaceDef id0 = 
         new InnerInterfaceDef(SourceInfo.NONE, 
                               _packageMav, 
                               new Word(SourceInfo.NONE, "RodInterface"),
                               new TypeParameter[0], 
                               new ReferenceType[0], 
                               new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
       id0.visit(_bbv);
       assertEquals("There should be 2 errors", 2, errors.size());
       assertEquals("The error message should be correct", 
                    "Local interfaces are illegal in Java.", errors.get(1).getFirst());
       innerInterface = _bbv._bodyData.getInnerClassOrInterface("RodInterface");
       assertNull("Should NOT have a inner interface named RodInterface", innerInterface);
       
       // Test one with explicit modifiers
       InnerInterfaceDef id1 = 
         new InnerInterfaceDef(SourceInfo.NONE, 
                               _publicMav, 
                               new Word(SourceInfo.NONE, "Todd"),
                               new TypeParameter[0], 
                               new ReferenceType[0], 
                               new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
       id1.visit(_bbv);
       assertEquals("There should be three errors", 3, errors.size());  // class modifiers are allowed
       assertEquals("The error message should be correct", 
                    "Local interfaces are illegal in Java.", errors.get(2).getFirst());
       innerInterface = _bbv._bodyData.getInnerClassOrInterface("Todd");
       assertNull("Should NOT have a inner interface named Todd", innerInterface);
     }
     
     public void testDummy() { }
  }
}