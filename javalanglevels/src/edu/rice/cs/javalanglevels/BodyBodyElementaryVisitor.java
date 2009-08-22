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
 * Language Level Visitor that represents the Elementary Language Level.  Enforces constraints during the
 * first walk of the AST (checking for langauge specific errors and building the symbol table).
 * This class enforces things that are common to all contexts reachable within a method body or other body 
 * (not class or interface body) at the Elementary Language Level). 
 */
public class BodyBodyElementaryVisitor extends ElementaryVisitor {
  
  /**The MethodData of this method. */
  private BodyData _bodyData;
  
  /*
   * Constructor for BodyBodyElementaryVisitor.
   * @param bodyData  The BodyData that encloses the context we are visiting.
   * @param file  The source file this came from.
   * @param packageName  The package the source file is in
   * @importedFiles  A list of classes that were specifically imported
   * @param importedPackages  A list of package names that were specifically imported
   * @param classDefsInThisFile  A list of the classes that are defined in the source file
   * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need to be
   * resolved
   */
  public BodyBodyElementaryVisitor(BodyData bodyData, 
                                   File file, String packageName, 
                                   LinkedList<String> importedFiles, 
                                   LinkedList<String> importedPackages, 
                                   LinkedList<String> classDefsInThisFile, 
                                   Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations,
                                 LinkedList<String> innerClassesToBeParsed) {
    super(file, packageName, importedFiles, importedPackages, classDefsInThisFile, continuations);
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
  
  /* Visit this BlockData with a new BodyBodyElementary visitor after making sure no errors need to be thrown.*/
  public Void forBlock(Block that) {
    forBlockDoFirst(that);
    if (_checkError()) return null;
    
    BlockData bd = new BlockData(_bodyData);
    _bodyData.addBlock(bd);
    that.getStatements().visit(new BodyBodyElementaryVisitor(bd, _file, _package, _importedFiles, _importedPackages, 
                                                             _classNamesInThisFile, continuations, 
                                                             _innerClassesToBeParsed));
    return null;
  }
  
  /*Add the variables that were declared to the body data and make sure that no two
   * variables have the same name.*/
  public Void forVariableDeclarationOnly(VariableDeclaration that) {
    if (!_bodyData.addFinalVars(_variableDeclaration2VariableData(that, _bodyData))) {
      _addAndIgnoreError("You cannot have two variables with the same name.", that);
    }
    return null;
  }
  
  /**
   * Call the super method to convert these to a VariableData array, then make sure that
   * each VariableData gets set to be final, as required at the Elementary level.
   * @param enclosingData  The Data which contains the variables
   */
  protected VariableData[] _variableDeclaration2VariableData(VariableDeclaration vd, Data enclosingData) {
    VariableData[] vds = llVariableDeclaration2VariableData(vd, enclosingData);
    for (int i = 0; i < vds.length; i++) {
      if (vds[i].getMav().getModifiers().length > 0) {
        StringBuffer s = new StringBuffer("the keyword(s) ");
        String[] modifiers = vds[i].getMav().getModifiers();
        for (int j = 0; j<modifiers.length; j++) { s.append("\"" + modifiers[j] + "\" "); }
        _addAndIgnoreError("You cannot use " + s.toString() + "to declare a local variable at the Elementary level", 
                           vd);
      }
      vds[i].setFinal();
      
    }
    return vds;
  }
  
  /** Test most of the methods declared above right here: */
  public static class BodyBodyElementaryVisitorTest extends TestCase {
    
    private BodyBodyElementaryVisitor _bbv;
    
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
    private ModifiersAndVisibility _finalPrivateMav = 
      new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[]{"final", "private"});
    
    
    public BodyBodyElementaryVisitorTest() { this(""); }
    
    public BodyBodyElementaryVisitorTest(String name) { super(name); }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");
      _md1 = new MethodData("methodName", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                            new VariableData[0], 
                            new String[0],
                            _sd1,
                            null);
      
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable = symbolTable = new Symboltable();
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      _hierarchy = new Hashtable<String, TypeDefBase>();
      _bbv = new BodyBodyElementaryVisitor(_md1, new File(""), "", new LinkedList<String>(), new LinkedList<String>(), 
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
    
    /* These last two tests are shared with ClassBodyElementaryVisitor,
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
  }
}