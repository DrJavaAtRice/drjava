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

/* Language Level Visitor that represents the Intermediate Language Level.  Enforces constraints during the first walk of 
 * the AST (checking for langauge specific errors and building the symbol table). This class enforces things that are 
 * common to all contexts reachable within an interface body at the Intermediate Language Level. 
 */
public class InterfaceBodyIntermediateVisitor extends IntermediateVisitor {
  
  /**The SymbolData corresponding to this interface.*/
  private SymbolData _symbolData;
  
  /*
   * Constructor for InterfaceBodyIntermediateVisitor.
   * @param sd  The SymbolData that encloses the context we are visiting.
   * @param file  The source file this came from.
   * @param packageName  The package the source file is in
   * @importedFiles  A list of classes that were specifically imported
   * @param importedPackages  A list of package names that were specifically imported
   * @param classDefsInThisFile  A list of the classes that are defined in the source file
   * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need to be resolved
   */
  public InterfaceBodyIntermediateVisitor(SymbolData sd, File file, String packageName, LinkedList<String> importedFiles, 
                                  LinkedList<String> importedPackages, LinkedList<String> classDefsInThisFile,Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations) {
    super(file, packageName, importedFiles, importedPackages, classDefsInThisFile, continuations);
    _symbolData = sd;
  }
  
  /*Throw an appropriate error*/
  public void forStatementDoFirst(Statement that) {
    _addError("Statements cannot appear outside of method bodies", that);
  }
  
  /*Throw an appropriate error*/
  public void forConcreteMethodDefDoFirst(ConcreteMethodDef that) {
    _addError("You cannot have concrete methods definitions in interfaces", that);
  }

  /**Throw an appropriate error*/
  public void forInstanceInitializerDoFirst(InstanceInitializer that) {
    _addError("This open brace must mark the beginning of an interface body", that);
  }
  
  /**No fields in interfaces at Intermediate Level.  Give an appropriate error.*/
  public void forVariableDeclarationDoFirst(VariableDeclaration that) {
    _addError("You cannot have fields in interfaces at the Intermediate level", that);
  }
  
  /** No super references for interfaces--give an appropriate error.*/
  public void forSuperReferenceDoFirst(SuperReference that) {
    _addAndIgnoreError("The field 'super' does not exist in interfaces.  Only classes have a 'super' field", that);
  }
  
  /**No This literal in interfaces--give an appropriate error*/
  public void forThisReferenceDoFirst(ThisReference that) {
    _addAndIgnoreError("The field 'this' does not exist in interfaces.  Only classes have a 'this' field.", that);
  }

  /* Make sure that the method is not declared to be private or protected.  Make it public and abstract
   * if it is not already declared to be so (since this is the default in the absence of modifiers). 
   * Make sure the method name is not the same as the interface name.
   */
  public void forAbstractMethodDef(AbstractMethodDef that) {
    forAbstractMethodDefDoFirst(that);
    if (_checkError()) return;
    
    MethodData md = createMethodData(that, _symbolData);
    
    //All interface methods are considered public by default: enforce this.
    if (md.hasModifier("private")) {
      _addAndIgnoreError("Interface methods cannot be made private.  They must be public.", that.getMav());
    }
    if (md.hasModifier("protected")) {
      _addAndIgnoreError("Interface methods cannot be made protected.  They must be public.", that.getMav());
    }
    
 // All interface methods are considered public by default.
    md.addModifier("public");
    md.addModifier("abstract"); //and all interface methods are abstract. 
    String className = getUnqualifiedClassName(_symbolData.getName());
    if (className.equals(md.getName())) {
      _addAndIgnoreError("Only constructors can have the same name as the class they appear in, " + 
                         "and constructors cannot appear in interfaces.", that);
    }
    else _symbolData.addMethod(md);
  }
  
  /** Throw an error: Interfaces cannot have constructors */
  public void forConstructorDefDoFirst(ConstructorDef that) {
      _addAndIgnoreError("Constructor definitions cannot appear in interfaces", that);
  }
  
  /** Call the super method to convert these to a VariableData array, then make sure that
    * each VariableData is final, as required at the Intermediate level.
    * @param enclosingData  The Data immediately enclosing the variables
    */
  protected VariableData[] _variableDeclaration2VariableData(VariableDeclaration vd, Data enclosingData) {
    VariableData[] vds = super._variableDeclaration2VariableData(vd, enclosingData);
    for (int i = 0; i < vds.length; i++) {
      vds[i].setFinalAndStatic();
    }
    return vds;
  }
  
  /** Delegate to method in LLV */
  public void forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    complexAnonymousClassInstantiationHelper(that, _symbolData);
  }

  /** Delegate to method in LLV */
  public void forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    simpleAnonymousClassInstantiationHelper(that, _symbolData);
  }
  
   /**
    * Test the methods declared in the above class.
    */
  public static class InterfaceBodyIntermediateVisitorTest extends TestCase {
    
    private InterfaceBodyIntermediateVisitor _ibiv;
    
    private SymbolData _sd1;
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[0]);
    private ModifiersAndVisibility _abstractMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"final"});
    
    
    public InterfaceBodyIntermediateVisitorTest() {
      this("");
    }
    public InterfaceBodyIntermediateVisitorTest(String name) {
      super(name);
    }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");

      errors = new LinkedList<Pair<String, JExpressionIF>>();
      symbolTable = new Symboltable();
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      _hierarchy = new Hashtable<String, TypeDefBase>();
      _classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
      _ibiv = new InterfaceBodyIntermediateVisitor(_sd1, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>(), new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
      _ibiv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      _ibiv._resetNonStaticFields();
      _ibiv._importedPackages.addFirst("java.lang");
      _errorAdded = false;
    }
    
    public void testForConcreteMethodDefDoFirst() {
      // Check that an error is thrown
      ConcreteMethodDef cmd = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _publicMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cmd.visit(_ibiv);
      assertEquals("There should not be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "You cannot have concrete methods definitions in interfaces", errors.getLast().getFirst());
      
    }
    
    public void testForAbstractMethodDefDoFirst() {
      // Check one that works
      _ibiv._symbolData.setMav(_abstractMav);
      AbstractMethodDef amd2 = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _abstractMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd2.visit(_ibiv);
      assertEquals("There should be no errors", 0, errors.size());
      assertTrue("The method def should be public", _ibiv._symbolData.getMethods().get(0).hasModifier("public"));

    }

    public void testForInstanceInitializerDoFirst() {
      InstanceInitializer ii = new InstanceInitializer(JExprParser.NO_SOURCE_INFO, 
                                                       new Block(JExprParser.NO_SOURCE_INFO, 
                                                                 new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0])));
      ii.visit(_ibiv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "This open brace must mark the beginning of an interface body", errors.get(0).getFirst());    
    }

    public void testForSimpleThisReferenceDoFirst() {
     SimpleThisReference tl = new SimpleThisReference(JExprParser.NO_SOURCE_INFO);
     tl.visit(_ibiv);
     assertEquals("There should be one error", 1, errors.size());
     assertEquals("The error message should be correct", "The field 'this' does not exist in interfaces.  Only classes have a 'this' field.", errors.get(0).getFirst());
    }
    
    
    public void testForComplexThisReferenceDoFirst() {
     ComplexThisReference tl = new ComplexThisReference(JExprParser.NO_SOURCE_INFO, new NullLiteral(JExprParser.NO_SOURCE_INFO));
     tl.visit(_ibiv);
     assertEquals("There should be one error", 1, errors.size());
     assertEquals("The error message should be correct", "The field 'this' does not exist in interfaces.  Only classes have a 'this' field.", errors.get(0).getFirst());

    }
    
    public void testForSimpleSuperReferenceDoFirst() {
      SimpleSuperReference sr = new SimpleSuperReference(JExprParser.NO_SOURCE_INFO);
      sr.visit(_ibiv);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "The field 'super' does not exist in interfaces.  Only classes have a 'super' field", errors.get(0).getFirst());
    }

    public void testForComplexSuperReferenceDoFirst() {
      ComplexSuperReference cr = new ComplexSuperReference(JExprParser.NO_SOURCE_INFO, new NullLiteral(JExprParser.NO_SOURCE_INFO));
      cr.visit(_ibiv);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "The field 'super' does not exist in interfaces.  Only classes have a 'super' field", errors.get(0).getFirst());
    }

    
    public void testForVariableDeclarationDoFirst() {
      // Check that an error is thrown
      VariableDeclaration vdecl = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,
                                                       _packageMav,
                                                       new VariableDeclarator[] {
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                               new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                               new Word (JExprParser.NO_SOURCE_INFO, "field1")),
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                               new PrimitiveType(JExprParser.NO_SOURCE_INFO, "boolean"), 
                               new Word (JExprParser.NO_SOURCE_INFO, "field2"))});
      vdecl.visit(_ibiv);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "You cannot have fields in interfaces at the Intermediate level", errors.getLast().getFirst());
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
      _ibiv._symbolData.setMav(_abstractMav);
      mdef.visit(_ibiv);
      assertEquals("There should not be any errors.", 0, errors.size());
      
      // Test one that doesn't work.
      mdef = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                             _abstractMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                             new Word(JExprParser.NO_SOURCE_INFO, "monkey"),
                                             new FormalParameter[0],
                                             new ReferenceType[0]);
      mdef.visit(_ibiv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Only constructors can have the same name as the class they appear in, and constructors cannot appear in interfaces.",
                   errors.get(0).getFirst());
      
      
      //It's okay for the method to be public
      AbstractMethodDef amd3 = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _publicMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "methodName2"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd3.visit(_ibiv);
      assertEquals("There should still be one error", 1, errors.size());
      assertTrue("The method def should be public", _ibiv._symbolData.getMethods().get(1).hasModifier("public"));

      //What if the method is called private? Should throw error
      AbstractMethodDef amd4 = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _privateMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "methodName3"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd4.visit(_ibiv);
      assertEquals("There should be two errors", 2, errors.size());
      assertEquals("The error message should be correct","Interface methods cannot be made private.  They must be public." , errors.get(1).getFirst());
    
      //What if the method is protected: Should throw error
      AbstractMethodDef amd5 = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _protectedMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "methodName4"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd5.visit(_ibiv);
      assertEquals("There should be three errors", 3, errors.size());
      assertEquals("The error message should be correct","Interface methods cannot be made protected.  They must be public." , errors.get(2).getFirst());
    }
    
    
    public void testForConstructorDef() {
     ///this is a ConstructorDef with no formal paramaters and no throws
      ConstructorDef cd = new ConstructorDef(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "MyClass"), _publicMav, new FormalParameter[0], new ReferenceType[0], 
                                             new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      //Check that the appropriate error is thrown.
      cd.visit(_ibiv);
      assertEquals("There should now be one error", 1, errors.size());
      assertEquals("The error message should be correct", "Constructor definitions cannot appear in interfaces", errors.get(0).getFirst());
      
    }
    
    
  }
}
