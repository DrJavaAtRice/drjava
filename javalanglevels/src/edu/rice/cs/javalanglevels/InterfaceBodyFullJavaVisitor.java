/*BEGIN_COPYRIGHT_BLOCK
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu). All rights reserved. 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the names of its contributors may be used 
 *      to endorse or promote products derived from this software without specific prior written permission.
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
import edu.rice.cs.javalanglevels.util.Utilities;

import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.IterUtil;
import java.util.*;
import java.io.*;

import junit.framework.TestCase;

/** Language Level Visitor class that implements the FullJava Language Level.  Enforces constraints during the first 
  * walk of the AST (checking for langauge specific errors and building the symbol table).  This class enforces some
  * constraints imposed on an interface body at the FullJava Language Level. 
  */
public class InterfaceBodyFullJavaVisitor extends FullJavaVisitor {
  
  /**The SymbolData corresponding to this interface, which encloses the body of the interface. */
  private SymbolData _enclosing;
  
  /** Old constructor for InterfaceBodyFullJavaVisitor.
    * @param sd             The SymbolData that encloses the context we are visiting.
    * @param file           The source file this came from.
    * @param packageName    The package the source file is in
    * @importedFiles        A list of classes that were specifically imported
    * @param importedPackages   A list of package names that were specifically imported
    * @param classesInThisFile  A list of the classes that are yet to be defined in this source file
    * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need to 
    *                       be resolved
    */
  public InterfaceBodyFullJavaVisitor(SymbolData sd, 
                                      File file, 
                                      String packageName,
                                      LinkedList<String> importedFiles, 
                                      LinkedList<String> importedPackages, 
                                      HashSet<String> classesInThisFile,
                                      Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations,
                                      LinkedList<Command> fixUps) {
    super(file, packageName, sd.getName(), importedFiles, importedPackages, classesInThisFile, continuations, fixUps);
    _enclosing = sd;
//    if (_enclosingClassName != sd.getName()) Utilities.show("enclosingClassName does not equal sd.getName(). Trace follows.\n" +
//                                                            Utilities.getStackTrace());
  }
  
  /** Preferred constructor for InterfaceBodyFullJavaVisitor.
    * @param sd             The SymbolData that encloses the context we are visiting.
    * @param file           The source file this came from.
    * @param packageName    The package the source file is in
    * @importedFiles        A list of classes that were specifically imported
    * @param importedPackages   A list of package names that were specifically imported
    * @param classesInThisFile  A list of the classes that are yet to be defined in this source file
    * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need to 
    *                       be resolved
    * @param genericTypes   A HashMap mapping in scope generic type names to their bounds (SymbolDatas).
    */
  public InterfaceBodyFullJavaVisitor(SymbolData sd, 
                                      File file, 
                                      String packageName,
                                      LinkedList<String> importedFiles, 
                                      LinkedList<String> importedPackages, 
                                      HashSet<String> classesInThisFile,
                                      Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations,
                                      LinkedList<Command> fixUps,
                                      HashMap<String, SymbolData> genericTypes) {
    super(file, packageName, sd.getName(), importedFiles, importedPackages, classesInThisFile, continuations, fixUps,
          genericTypes);
    _enclosing = sd;
//    if (_enclosingClassName != sd.getName()) Utilities.show("enclosingClassName does not equal sd.getName(). Trace follows.\n" +
//                                                            Utilities.getStackTrace());
  }
  
  /** Ignore Statement. */
  public Void forStatementDoFirst(Statement that) { return null; }
  
  /*Throw an appropriate error*/
  public Void forConcreteMethodDefDoFirst(ConcreteMethodDef that) {
    _addError("You cannot have concrete methods definitions in interfaces", that);
    return null;
  }

  
  /**Throw an appropriate error*/
  public Void forInstanceInitializerDoFirst(InstanceInitializer that) {
    _addError("This open brace must mark the beginning of an interface body", that);
    return null;
  }
  
  /** Ignore VariableDeclaration. */
  public Void forVariableDeclarationOnly(VariableDeclaration that) { return null; }

  /**No This literal in interfaces--give an appropriate error*/
  public Void forThisReferenceDoFirst(ThisReference that) {
    _addAndIgnoreError("The field 'this' does not exist in interfaces.  Only classes have a 'this' field.", that);
    return null;
  }

  /** No super references for interfaces--give an appropriate error.*/
  public Void forSuperReferenceDoFirst(SuperReference that) {
    _addAndIgnoreError("The field 'super' does not exist in interfaces.  Only classes have a 'super' field", that);
    return null;
  }
  
  public Void forAbstractMethodDef(AbstractMethodDef that) {
    forAbstractMethodDefDoFirst(that);
    if (_checkError()) return null;
    
    assert _enclosing != null;
    
      // Process type parameters
    TypeParameter[] tps = that.getTypeParams();

    // TODO !!! pass genericTypes as a parameter to createMethod and critical submethods.  The scope of the
    // new generic types table includes all of createMethodData processing, notably processing the return type.
    // Passing an extended generic types table to BodyBodyFullJavaVisitor is insufficient
      
    // Save a snapshot of _genericTypes
    HashMap<String, SymbolData> oldGenericTypes = _genericTypes;
    
    // Rebind _genericTypes to a shallow copy of itself for use in processing this method. The temporary variable copy
    // gets around a bug in javac in processing the SuppressWarnings statement.
    @SuppressWarnings("unchecked")
    HashMap<String, SymbolData> copy = (HashMap<String, SymbolData>)_genericTypes.clone();
    _genericTypes = copy;
    
    if (tps != null) {  // extend genericTypes by new type variable bindings
      for (TypeParameter tp: tps) {
        final String typeName = tp.getVariable().getName();
        final String boundName = tp.getBound().getName();
        SymbolData boundSD = _identifyType(boundName, that.getSourceInfo(), _enclosingClassName);
        if (boundSD == null) { // create a dummy SymbolData 
          boundSD = symbolTable.get("java.lang.Object"); //  TODO: could create a separate unbound type variable singleton class?
//        System.err.println("Creating dummy SymbolData for bounding type " + typeName + " in inner class " + name);
          // TODO!  !!!!! Create an appropriate fixUp mechanism
        }
//        System.err.println("In method " + _enclosing + "." + that.getName().getText() + ", type " + typeName 
//                             + " is bound to " + boundSD);
        _genericTypes.put(typeName, boundSD);
      }
    }
    
    MethodData md = createMethodData(that, _enclosing);
    
    //All interface methods are considered public by default: enforce this.
    if (md.hasModifier("private")) {
      _addAndIgnoreError("Interface methods cannot be private.  They must be public.", that.getMav());
    }
    if (md.hasModifier("protected")) {
      _addAndIgnoreError("Interface methods cannot be protected.  They must be public.", that.getMav());
    }
    
    // All interface methods are considered public by default.
    md.addModifier("public");
    md.addModifier("abstract"); // and all interface methods are abstract. 
    String className = getUnqualifiedClassName(_enclosing.getName());
    if (className.equals(md.getName())) {
      _addAndIgnoreError("Only constructors can have the same name as the class they appear in, " + 
                         "and constructors cannot appear in interfaces.", that);
    }
    else _enclosing.addMethod(md);

    _genericTypes = oldGenericTypes;
    return null;
  }
  
  /** Call the method in FullJavaVisitor since it's common to this ClassBodyFullJavaVisitor. */
  public Void forInnerInterfaceDef(InnerInterfaceDef that) {
    String relName = that.getName().getText();
    handleInnerInterfaceDef(that, _enclosing, relName, getQualifiedClassName(_enclosing.getName()) + '.' + relName);
    return null;
  }
  
  /**Call the method in FullJavaVisitor; it's common to this ClassBodyAdvancedVisitor.*/
  public Void forInnerClassDef(InnerClassDef that) {
    String relName = that.getName().getText();
    handleInnerClassDef(that, _enclosing, relName, getQualifiedClassName(_enclosing.getName()) + '.' + relName);
    return null;
  }
  
  /** Throw an error: Interfaces cannot have constructors */
  public Void forConstructorDefDoFirst(ConstructorDef that) {
    _addAndIgnoreError("Constructor definitions cannot appear in interfaces", that);
    return null;
  }
  
  /** Processes a static field declaration within an interface. Calls the super method to convert the VariableDeclaration
    * to a VariableData array, then makes sure that each VariableData is final and static, as required in an
    * interface.
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
  public Void forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    complexAnonymousClassInstantiationHelper(that, _enclosing);  // TODO: the wrong enclosing context?
    return null;
  }

  /** Delegate to method in LLV*/
  public Void forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
//    System.err.println("Calling simpleAnonymousClassInstantiation Helper from InterfaceBody " + that.getSourceInfo());
    simpleAnonymousClassInstantiationHelper(that, _enclosing);
    return null;
  }

  /** Test the methods in the above class. */
  public static class InterfaceBodyFullJavaVisitorTest extends TestCase {
    
    private InterfaceBodyFullJavaVisitor _ibfv;
    
    private SymbolData _sd1;
    private SymbolData _objectSD;
     
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[0]);
    private ModifiersAndVisibility _abstractMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final"});
    
    public InterfaceBodyFullJavaVisitorTest() { this(""); }
    public InterfaceBodyFullJavaVisitorTest(String name) { super(name);  }
    
    public void setUp() {
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable.clear();
      LanguageLevelConverter._newSDs.clear();
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, IterUtil.make(new File("lib/buildlib/junit.jar")));
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();   
      
            
      _sd1 = new SymbolData("MyInterface");
      _sd1.setIsContinuation(false);
      _sd1.setInterface(true);
      _sd1.setPackage("");
      _sd1.setTypeParameters(new TypeParameter[0]);
      _sd1.setInterfaces(new ArrayList<SymbolData>());
      
      // NOTE: the following ensures that the symbolTable includes essential types
      _ibfv = new InterfaceBodyFullJavaVisitor(_sd1, 
                                               new File(""), 
                                               "", 
                                               new LinkedList<String>(), 
                                               new LinkedList<String>(), 
                                               new HashSet<String>(), 
                                               new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                               new LinkedList<Command>());
      _ibfv._classesInThisFile = new HashSet<String>();
      _ibfv.continuations = new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>();
      _ibfv._importedPackages.addFirst("java.lang");
      _ibfv._enclosingClassName = "MyInterface";

      _objectSD = LanguageLevelConverter.symbolTable.get("java.lang.Object");
      _sd1.setSuperClass(_objectSD);
      
      LanguageLevelConverter.symbolTable.put("MyInterface", _sd1);

      _errorAdded = false;
    }
    
    public void testForConcreteMethodDefDoFirst() {
      // Check that an error is thrown
      ConcreteMethodDef cmd = new ConcreteMethodDef(SourceInfo.NONE, 
                                                    _publicMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(SourceInfo.NONE, "int"), 
                                                    new Word(SourceInfo.NONE, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      cmd.visit(_ibfv);
      assertEquals("There should not be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "You cannot have concrete methods definitions in interfaces", 
                   errors.getLast().getFirst());
      
    }
    
    public void testForAbstractMethodDefDoFirst() {
      // Check one that works
      _ibfv._enclosing.setMav(_abstractMav);
      AbstractMethodDef amd2 = new AbstractMethodDef(SourceInfo.NONE, 
                                                     _abstractMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NONE, "double"), 
                                                     new Word(SourceInfo.NONE, "methodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd2.visit(_ibfv);
      assertEquals("There should be no errors", 0, errors.size());
      assertTrue("The method def should be public", 
                 _ibfv._enclosing.getMethods().get(0).hasModifier("public"));

    }

    public void testForInstanceInitializerDoFirst() {
      InstanceInitializer ii = 
        new InstanceInitializer(SourceInfo.NONE, 
                                new Block(SourceInfo.NONE, 
                                          new BracedBody(SourceInfo.NONE, new BodyItemI[0])));
      ii.visit(_ibfv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "This open brace must mark the beginning of an interface body", 
                   errors.get(0).getFirst());    
    }

    public void testForSimpleThisReferenceDoFirst() {
     SimpleThisReference tl = new SimpleThisReference(SourceInfo.NONE);
     tl.visit(_ibfv);
     assertEquals("There should be one error", 1, errors.size());
     assertEquals("The error message should be correct", 
                  "The field 'this' does not exist in interfaces.  Only classes have a 'this' field.", 
                  errors.get(0).getFirst());
    }
    
    public void testForComplexThisReferenceDoFirst() {
     ComplexThisReference tl = new ComplexThisReference(SourceInfo.NONE, 
                                                        new NullLiteral(SourceInfo.NONE));
     tl.visit(_ibfv);
     assertEquals("There should be one error", 1, errors.size());
     assertEquals("The error message should be correct", 
                  "The field 'this' does not exist in interfaces.  Only classes have a 'this' field.", 
                  errors.get(0).getFirst());

    }
    
    public void testForSimpleSuperReferenceDoFirst() {
     SimpleSuperReference sr = new SimpleSuperReference(SourceInfo.NONE);
     sr.visit(_ibfv);
     assertEquals("There should be one error", 1, errors.size());
     assertEquals("The error message should be correct", 
                  "The field 'super' does not exist in interfaces.  Only classes have a 'super' field", 
                  errors.get(0).getFirst());
    }
    
    public void testForComplexSuperReferenceDoFirst() {
      ComplexSuperReference cr = new ComplexSuperReference(SourceInfo.NONE, 
                                                           new NullLiteral(SourceInfo.NONE));
      cr.visit(_ibfv);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "The field 'super' does not exist in interfaces.  Only classes have a 'super' field", 
                   errors.get(0).getFirst());
    }
    
    public void xtestForVariableDeclarationDoFirst() {
      //Check that if a field is initialized, no error is thrown
      VariableDeclaration vdecl0 = new VariableDeclaration(SourceInfo.NONE,
                                                       _packageMav,
                                                       new VariableDeclarator[] {
        new InitializedVariableDeclarator(SourceInfo.NONE, 
                                          new PrimitiveType(SourceInfo.NONE, "double"), 
                                          new Word (SourceInfo.NONE, "field0"), 
                                          new DoubleLiteral(SourceInfo.NONE, 2.345))});

      vdecl0.visit(_ibfv);
      assertEquals("There should be no errors", 0, errors.size());
                                                           
      
      // Check that an error is thrown if the fields are not initialized
      VariableDeclaration vdecl = new VariableDeclaration(SourceInfo.NONE,
                                                       _packageMav,
                                                       new VariableDeclarator[] {
        new InitializedVariableDeclarator(SourceInfo.NONE, 
                               new PrimitiveType(SourceInfo.NONE, "double"), 
                               new Word (SourceInfo.NONE, "field1"),
                               new DoubleLiteral(SourceInfo.NONE, 2.45)),
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                               new PrimitiveType(SourceInfo.NONE, "boolean"), 
                               new Word (SourceInfo.NONE, "field2"))});
      vdecl.visit(_ibfv);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "All fields in interfaces must be assigned a value when they are declared", 
                   errors.getLast().getFirst());
    }
    
    public void testForAbstractMethodDef() {
      // Test one that works.
      MethodDef mdef = new AbstractMethodDef(SourceInfo.NONE, 
                                             _abstractMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(SourceInfo.NONE, "int"), 
                                             new Word(SourceInfo.NONE, "methodName"),
                                             new FormalParameter[0],
                                             new ReferenceType[0]);
      _ibfv._enclosing.setMav(_abstractMav);
      mdef.visit(_ibfv);
      assertEquals("There should not be any errors.", 0, errors.size());
      
      // Test one that doesn't work.
      mdef = new AbstractMethodDef(SourceInfo.NONE, 
                                             _abstractMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(SourceInfo.NONE, "int"), 
                                             new Word(SourceInfo.NONE, "MyInterface"),
                                             new FormalParameter[0],
                                             new ReferenceType[0]);
      mdef.visit(_ibfv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Only constructors can have the same name as the class they appear in, and constructors cannot " 
                     + "appear in interfaces.",
                   errors.get(0).getFirst());
      
      //It's okay for the method to be public
      AbstractMethodDef amd3 = new AbstractMethodDef(SourceInfo.NONE, 
                                                     _publicMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NONE, "double"), 
                                                     new Word(SourceInfo.NONE, "methodName2"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd3.visit(_ibfv);
      assertEquals("There should still be one error", 1, errors.size());
      assertTrue("The method def should be public", _ibfv._enclosing.getMethods().get(1).hasModifier("public"));

      //What if the method is called private? Should throw error
      AbstractMethodDef amd4 = new AbstractMethodDef(SourceInfo.NONE, 
                                                     _privateMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NONE, "double"), 
                                                     new Word(SourceInfo.NONE, "methodName3"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd4.visit(_ibfv);
      assertEquals("There should be two errors", 2, errors.size());
      assertEquals("The error message should be correct", 
                   "Interface methods cannot be private.  They must be public." , 
                   errors.get(1).getFirst());
    
      //What if the method is protected: Should throw error
      AbstractMethodDef amd5 = new AbstractMethodDef(SourceInfo.NONE, 
                                                     _protectedMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NONE, "double"), 
                                                     new Word(SourceInfo.NONE, "methodName4"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd5.visit(_ibfv);
      assertEquals("There should be three errors", 3, errors.size());
      assertEquals("The error message should be correct",
                   "Interface methods cannot be protected.  They must be public." , 
                   errors.get(2).getFirst());
    }
    

    public void testForInnerClassDef() {
      _ibfv._enclosing = new SymbolData("MyInterface");
      _ibfv._enclosing.setInterface(true);
      
//      SymbolData obj = new SymbolData("java.lang.Object");
      LanguageLevelConverter._newSDs.clear();
//      LanguageLevelConverter.symbolTable.put("java.lang.Object", obj);
      
      InnerClassDef cd1 = 
        new InnerClassDef(SourceInfo.NONE, 
                          _packageMav, 
                          new Word(SourceInfo.NONE, "Bart"),
                          new TypeParameter[0], 
                          new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                          new ReferenceType[0], 
                          new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      InnerClassDef cd0 = 
        new InnerClassDef(SourceInfo.NONE, 
                          _packageMav, 
                          new Word(SourceInfo.NONE, "Lisa"),
                          new TypeParameter[0], 
                          new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                          new ReferenceType[0], 
                          new BracedBody(SourceInfo.NONE, new BodyItemI[] {cd1}));
     
//      SymbolData sd0 = new SymbolData(_ibfv._enclosing.getName() + "$Lisa", _packageMav, new TypeParameter[0], obj, 
//                                      new ArrayList<SymbolData>(), null); 
//      SymbolData sd1 = 
//        new SymbolData(_ibfv._enclosing.getName() + "$Lisa$Bart", _packageMav, new TypeParameter[0], obj, 
//                       new ArrayList<SymbolData>(), null); 
      
//      _ibfv._enclosing.addInnerClass(sd0);
//      sd0.setOuterData(_ibfv._enclosing);

//      sd0.addInnerClass(sd1);
//      sd1.setOuterData(sd0);
//
//      sd0.setIsContinuation(true);
//      sd1.setIsContinuation(true);
//      LanguageLevelConverter._newSDs.clear();
//      LanguageLevelConverter.symbolTable.put(_ibfv._enclosing.getName() + "$Lisa", sd0);

      cd0.visit(_ibfv);

      SymbolData sd0 = _ibfv._enclosing.getInnerClassOrInterface("Lisa");
      assertNotNull("Inner class Lisa exists", sd0);
      
      assertEquals("Lisa should have the correct outer data", _ibfv._enclosing, sd0.getOuterData());
      assertTrue("_sd1 should be an interface", _sd1.isInterface());
      assertFalse("sd0 should be a class", sd0.isInterface());
      assertEquals("Lisa should have 0 methods", 0, sd0.getMethods().size());  
      
      SymbolData sd1 = sd0.getInnerClassOrInterface("Bart");
      assertNotNull("Inner class Bart exists", sd1);
      
      assertEquals("Bart should have the correct outer data", sd0, sd1.getOuterData());
      assertFalse("sd1 should be a class", sd1.isInterface());
      assertEquals("Bart should have 0 methods", 0, sd1.getMethods().size());
    }
    
    public void testForInnerInterfaceDef() {

      InnerInterfaceDef cd1 = 
        new InnerInterfaceDef(SourceInfo.NONE, _packageMav, new Word(SourceInfo.NONE, "Bart"),
                              new TypeParameter[0], new ReferenceType[0], 
                              new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      InnerInterfaceDef cd0 = 
        new InnerInterfaceDef(SourceInfo.NONE, _packageMav, new Word(SourceInfo.NONE, "Lisa"),
                              new TypeParameter[0], new ReferenceType[0], 
                              new BracedBody(SourceInfo.NONE, new BodyItemI[] {cd1}));
      
//      SymbolData sd0 = new SymbolData(_ibfv._enclosing.getName() + "$Lisa", _packageMav, new TypeParameter[0], 
//                                      new ArrayList<SymbolData>(), null); 
//      SymbolData sd1 = new SymbolData(_ibfv._enclosing.getName() + "$Lisa$Bart", _packageMav, new TypeParameter[0], 
//                                      new ArrayList<SymbolData>(), null);
//      sd0.addInnerInterface(sd1);
//
//      
//      _ibfv._enclosing.addInnerInterface(sd0);
//      sd0.setOuterData(_ibfv._enclosing);
//
//      sd0.addInnerInterface(sd1);
//      sd1.setOuterData(sd0);
//
//      
//      sd0.setIsContinuation(true);
//      sd1.setIsContinuation(true);
//      LanguageLevelConverter._newSDs.clear();
//      LanguageLevelConverter.symbolTable.put(_ibfv._enclosing.getName() + "$Lisa", sd0);
//      LanguageLevelConverter._newSDs.clear();
//      LanguageLevelConverter.symbolTable.put(_ibfv._enclosing.getName() + "$Lisa$Bart", sd1);

      cd0.visit(_ibfv);

      SymbolData sd0 = _ibfv._enclosing.getInnerClassOrInterface("Lisa");
      assertNotNull("Inner interfacae Lisa exists", sd0);
      
      assertEquals("Lisa should have the correct outer data", _ibfv._enclosing, sd0.getOuterData());
      assertTrue("_sd1 should be an interface", _sd1.isInterface());
      assertTrue("sd0 should be a interface", sd0.isInterface());
      assertEquals("Lisa should have 0 methods", 0, sd0.getMethods().size());  
      
      SymbolData sd1 = sd0.getInnerClassOrInterface("Bart");
      assertNotNull("Inner interface Bart exists", sd1);
      
      assertEquals("Bart should have the correct outer data", sd0, sd1.getOuterData());
      assertTrue("sd1 should be an interface", sd1.isInterface());
      assertEquals("Bart should have 0 methods", 0, sd1.getMethods().size());
    }

    public void testForConstructorDef() {
     ///this is a ConstructorDef with no formal paramaters and no throws
      ConstructorDef cd =
        new ConstructorDef(SourceInfo.NONE, new Word(SourceInfo.NONE, "MyClass"),
                           _publicMav, new FormalParameter[0], new ReferenceType[0], 
                            new BracedBody(SourceInfo.NONE, new BodyItemI[0]));

      // Check that the appropriate error is thrown.
      cd.visit(_ibfv);
      assertEquals("There should now be one error", 1, errors.size());
      assertEquals("The error message should be correct", "Constructor definitions cannot appear in interfaces", 
                   errors.get(0).getFirst());
      
    }
    public void testDummy() { }
  }
}