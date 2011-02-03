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
  private SymbolData _enclosing;
  
  /** Deprecated onstructor for ClassBodyFullJavaVisitor.
    * @param sd  The SymbolData that encloses the context we are visiting.  Must be non-null.
    * @param className The name of the enclosing class.  Must be non-null and non-empty.
    * @param file  The source file this came from.
    * @param packageName  The package the source file is in
    * @importedFiles  A list of classes that were specifically imported
    * @param importedPackages  A list of package names that were specifically imported
    * @param classesInThisFile  A list of the classes that are yet to be defined in this source file
    * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need to 
    *                       be resolved
    * TODO: coalesce className and enclosingClassName
    */
  public ClassBodyFullJavaVisitor(SymbolData sd, 
                                  String className,
                                  File file, 
                                  String packageName,
                                  LinkedList<String> importedFiles, 
                                  LinkedList<String> importedPackages,
                                  HashSet<String> classesInThisFile, 
                                  Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations,
                                  LinkedList<Command> fixUps) {
    super(file, packageName, className, importedFiles, importedPackages, classesInThisFile, continuations, fixUps);
    _enclosing = sd;
    assert sd != null && className != null &&  ! className.equals("");
  }
  
  /** Preferred constructor for ClassBodyFullJavaVisitor.
    * @param sd  The SymbolData that encloses the context we are visiting.  Must be non-null.ty.
    * @param file  The source file this came from.
    * @param packageName  The package the source file is in
    * @importedFiles  A list of classes that were specifically imported
    * @param importedPackages  A list of package names that were specifically imported
    * @param classesInThisFile  A list of the classes that are yet to be defined in this source file
    * @param continuations  A hashtable corresponding to the continuations (unresolved Symbol Datas) that will need to 
    *                       be resolved
    * TODO: coalesce className and enclosingClassName
    */
  public ClassBodyFullJavaVisitor(SymbolData sd, 
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
    assert sd != null;
  }
  
  /* Ignore ForStatement. */
  public Void forStatementDoFirst(Statement that) { return null; }
  
  /** Ignore ConcreteMethodDef. */
  
  /** Ignore AbstractMake sure that this abstract method def is declared to be abstract. */
  public Void forAbstractMethodDefDoFirst(AbstractMethodDef that) {
    
//    ModifiersAndVisibility mav = that.getMav();
//    String[] modifiers = mav.getModifiers();
    if (! _enclosing.isInterface() && ! _enclosing.hasModifier("abstract")) { // interfaces not yet marked abstract
      _addError("Abstract methods can only be declared in abstract classes", that);
    }
    return super.forAbstractMethodDefDoFirst(that);
  }
  
  /*Add an appropriate error*/
  public Void forInstanceInitializerDoFirst(InstanceInitializer that) {
    _addError("This open brace must mark the beginning of a method or class body", that);
    return null;
  }
  
  /** Processes a field declaration.  Converts the VariableDeclaration to VariableData[].  Adds the variable datas to 
    * the symbol data, and gives an error if two fields have the same names
    */
  public Void forVariableDeclarationOnly(VariableDeclaration that) {
//    System.err.println("forVariableDeclarationOnly called in ClassBodyFullJavaVisitor for " + that);
    VariableData[] vds = _variableDeclaration2VariableData(that, _enclosing);
//    System.err.println(" ## generated vds = " + Arrays.toString(vds));
    
    // Add the variable datas to the symbol data    
    if (! _enclosing.addVars(vds)) {
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
    assert _enclosing != null;
    
    TypeParameter[] tps = that.getTypeParams();

    // TODO !!! pass genericTypes as a parameter to createMethod and critical submethods.  The scope of the
    // new generic types table includes all of createMethodData processing, notably processing the return type.
    // Passing an extended generic types table to BodyBodyFullJavaVisitor is insufficient
      
    // Save a snapshot of _genericTypes
    HashMap<String, SymbolData> oldGenericTypes = _genericTypes;
    
    // Rebind _genericTypes to a shallow copy of itself for use in processing this method. The temporary variable copy
    // gets around a bug in javac in processing the SuppressWarnings statement.
    @SuppressWarnings("unchecked")
    HashMap<String, SymbolData> copy = (HashMap<String, SymbolData>) _genericTypes.clone();
    _genericTypes = copy;
    
    if (tps != null && tps.length > 0) {  // extend genericTypes by new polymorphic method type variable bindings
//      Utilities.show("forConcreteMethodDef encountered a non-empty type parameters list:\n" + Arrays.toString(tps));
//      System.err.println("forConcreteMethodDef encountered a non-empty type parameters list:\n" + Arrays.toString(tps));
      for (TypeParameter tp: tps) {
        final String typeName = tp.getVariable().getName();
        final String boundName = tp.getBound().getName();
//        System.err.println("***** Type variable " + typeName + " is bound to " + boundName);
        SymbolData boundSD = _identifyType(boundName, that.getSourceInfo(), _enclosingClassName);
//        System.err.println("***** Corresponding SymbolData is " + boundSD);
//        Utilities.show("Type variable " + typeName + " is bound to " + boundName + " Corresponding SD is " + boundSD);
        //  TODO: could create a separate unbound type variable singleton class?
        if (boundSD == null) { // create a dummy SymbolData 
          boundSD = symbolTable.get("java.lang.Object"); 
//        System.err.println("Creating dummy SymbolData for bounding type " + typeName + " in inner class " + name);
          // TODO!  !!!!! Create an appropriate fixUp mechanism
        }
//        System.err.println("In method " + _enclosing + "." + that.getName().getText() + ", type " + typeName 
//                             + " is bound to " + boundSD);
        _genericTypes.put(typeName, boundSD);
      }
    }
    
    MethodData md = createMethodData(that, _enclosing);
    String className = getUnqualifiedClassName(_enclosing.getName());
    
    if (className.equals(md.getName())) {
      _addAndIgnoreError("Only constructors can have the same name as the class they appear in, and constructors "
                           + "do not have an explicit return type",
                         that);
    }
    else _enclosing.addMethod(md);
    that.getBody().visit(new BodyBodyFullJavaVisitor(md, _file, _package, _enclosingClassName, _importedFiles, 
                                                     _importedPackages, 
                                                     _classesInThisFile, continuations, fixUps, 
                                                     new HashSet<String>(), _genericTypes));
    _genericTypes = oldGenericTypes;
    return null;
  }
  
  /** Creates a MethodData corresponding to this method declaration. Ensures that the method name is different from the 
    * class name.
    */
  public Void forAbstractMethodDef(AbstractMethodDef that) {
    forAbstractMethodDefDoFirst(that);
    if (prune(that)) return null;
    
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
    String className = getUnqualifiedClassName(_enclosing.getName());
    if (className.equals(md.getName())) {
      _addAndIgnoreError("Only constructors can have the same name as the class they appear in, and constructors do "
                           + "not have an explicit return type",
                         that);
    }
    else _enclosing.addMethod(md);
    _genericTypes = oldGenericTypes;
    return null;
  }
  
  /**Call the method in FullJavaVisitor since it's common to this and FullJavaBodyFullJavaVisitor. */
  public Void forInnerInterfaceDef(InnerInterfaceDef that) {
    String relName = that.getName().getText();
    handleInnerInterfaceDef(that, _enclosing, relName, getQualifiedClassName(_enclosing.getName()) + '.' + relName);
    return null;
  }
  
  /**Call the method in FullJavaVisitor since it's common to this and FullJavaBodyFullJavaVisitor. */
  public Void forInnerClassDef(InnerClassDef that) {
    String relName = that.getName().getText();
    handleInnerClassDef(that, _enclosing, relName, getQualifiedClassName(_enclosing.getName()) + '.' + relName);
    return null;
  }
  
  /** Create a constructor corresponding to the specifications in the ConstructorDef, and then
    * visit the constructor body, passing the constructor as the enclosing data.
    */
  public Void forConstructorDef(ConstructorDef that) {
    forConstructorDefDoFirst(that);
    if (prune(that)) return null;
    
    that.getMav().visit(this);
    String name = getUnqualifiedClassName(that.getName().getText());
    if ((that.getName().getText().indexOf('.') != -1 && ! that.getName().getText().equals(_enclosing.getName()))
          || ! name.equals(getUnqualifiedClassName(_enclosing.getName()))) {
      _addAndIgnoreError("The constructor return type and class name must match", that);
    }
    
    // Turn the thrown exceptions from a ReferenceType[] to a String[]
    String[] throwStrings = referenceType2String(that.getThrows());
    
    SymbolData returnType = _enclosing;
    MethodData md = MethodData.make(name, that.getMav(), new TypeParameter[0], returnType, 
                                    new VariableData[0], throwStrings, _enclosing, that);  // VariableData is dummy
    
    _checkError(); // reset check flag
    // Turn the parameters from a FormalParameterList to a VariableData[]
    VariableData[] vds = formalParameters2VariableData(that.getParameters(), _enclosing);
    if (! _checkError()) {  //if there was an error converting the formalParameters, don't use them.
      md.setParams(vds);
      if (!md.addVars(vds)) {
        _addAndIgnoreError("You cannot have two method parameters with the same name", that);
      }
    }
    
    _enclosing.addMethod(md);
    that.getStatements().visit(new BodyBodyFullJavaVisitor(md, _file, _package, _enclosingClassName, _importedFiles, 
                                                           _importedPackages, _classesInThisFile, continuations, fixUps,
                                                           new HashSet<String>()));
    
    //note that we have seen a constructor.
    _enclosing.incrementConstructorCount();
    return null;
  }
  
  /** Delegate to method in LanguageLevelVisitor */
  public Void forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    complexAnonymousClassInstantiationHelper(that, _enclosing);  // TODO: the wrong enclosing context?
    return null;
  }
  
  /**Delegate to method in LanguageLevelVisitor */
  public Void forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    simpleAnonymousClassInstantiationHelper(that, _enclosing);
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
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[0]);
    private ModifiersAndVisibility _abstractMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final"});
    private ModifiersAndVisibility _staticMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"static"});
    private ModifiersAndVisibility _abstractStaticMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract", "static"});
    private ModifiersAndVisibility _finalStaticMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"static", "final"});
    
    
    public ClassBodyFullJavaVisitorTest() { this(""); }
    public ClassBodyFullJavaVisitorTest(String name) { super(name); }
    
    public void setUp() {
      _sd1 = new SymbolData("i.like.monkey");  // creates a continuation
      
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable.clear();
      LanguageLevelConverter.symbolTable.put("i.like.monkey", _sd1);
      LanguageLevelConverter._newSDs.clear();
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
//      _hierarchy = new Hashtable<String, TypeDefBase>();
      _cbfjv = new ClassBodyFullJavaVisitor(_sd1, 
                                            "i.like.monkey", 
                                            new File(""), 
                                            "", 
                                            new LinkedList<String>(), 
                                            new LinkedList<String>(), 
                                            new HashSet<String>(), 
                                            new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                            new LinkedList<Command>());
      _cbfjv._classesInThisFile = new HashSet<String>();
      _cbfjv.continuations = new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(); // no _sd1
//      _cbfjv._resetNonStaticFields();
      _cbfjv._importedPackages.addFirst("java.lang");
      
      _errorAdded = false;
    }
    
    public void testForConcreteMethodDefDoFirst() {
      // Check one that works
      ConcreteMethodDef cmd = new ConcreteMethodDef(SourceInfo.NONE, 
                                                    _publicMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(SourceInfo.NONE, "int"), 
                                                    new Word(SourceInfo.NONE, "methodName1"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      cmd.visit(_cbfjv);
      assertEquals("There should not be any errors", 0, errors.size());
      
      // Check one that doesn't work because it is declared abstract but is actually a concrete method
      ConcreteMethodDef cmd2 = new ConcreteMethodDef(SourceInfo.NONE, 
                                                     _abstractMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NONE, "double"), 
                                                     new Word(SourceInfo.NONE, "methodName2"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0], 
                                                     new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      cmd2.visit(_cbfjv);
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "Methods that have a braced body cannot be declared \"abstract\"", 
                   errors.get(0).getFirst());
      
      //Check that a static method does not result in an error.
      ConcreteMethodDef cmd3 = new ConcreteMethodDef(SourceInfo.NONE, 
                                                     _staticMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NONE, "double"), 
                                                     new Word(SourceInfo.NONE, "methodName2"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0], 
                                                     new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      cmd3.visit(_cbfjv);
      assertEquals("There should still be one error", 1, errors.size());
      
      
    }
    
    public void testForAbstractMethodDefDoFirst() {
      // Check one that doesn't work
      AbstractMethodDef amd = new AbstractMethodDef(SourceInfo.NONE, 
                                                    _abstractMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(SourceInfo.NONE, "int"), 
                                                    new Word(SourceInfo.NONE, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0]);
      amd.visit(_cbfjv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "Abstract methods can only be declared in abstract classes", 
                   errors.get(0).getFirst());
      
      // Check one that works
      _cbfjv._enclosing.setMav(_abstractMav);
      AbstractMethodDef amd2 = new AbstractMethodDef(SourceInfo.NONE, 
                                                     _abstractMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NONE, "double"), 
                                                     new Word(SourceInfo.NONE, "methodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd2.visit(_cbfjv);
      assertEquals("There should still be one error", 1, errors.size());
      
      // Check that static methods are now allowed at the FullJava level.
      AbstractMethodDef amd3 = new AbstractMethodDef(SourceInfo.NONE, 
                                                     _abstractStaticMav, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NONE, "double"), 
                                                     new Word(SourceInfo.NONE, "methodName2"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0]);
      amd3.visit(_cbfjv);
      assertEquals("There should be two errors", 2, errors.size());
      assertEquals("The error message should be correct.", 
                   "Illegal combination of modifiers. Can't use static and abstract together.", 
                   errors.get(1).getFirst());
    }
    
    public void testForInstanceInitializerDoFirst() {
      InstanceInitializer ii = new InstanceInitializer(SourceInfo.NONE, 
                                                       new Block(SourceInfo.NONE, 
                                                                 new BracedBody(SourceInfo.NONE, new BodyItemI[0])));
      ii.visit(_cbfjv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "This open brace must mark the beginning of a method or class body", errors.get(0).getFirst());
    }
    
    public void testForVariableDeclaration() {
      
      ArrayInitializer ai = new ArrayInitializer(SourceInfo.NONE, new VariableInitializerI[0]);
      TypeVariable tv = new TypeVariable(SourceInfo.NONE, "String");
      SymbolData _string = LanguageLevelConverter.symbolTable.get("java.lang.String");
      assertNotNull("java.lang.String already in table", _string);
      ArrayType at = new ArrayType(SourceInfo.NONE, "String[]", tv);
      
      VariableDeclarator vd = 
        new UninitializedVariableDeclarator(SourceInfo.NONE, at, new Word(SourceInfo.NONE, "myArray"));
      
      VariableDeclaration vdecl = 
        new VariableDeclaration(SourceInfo.NONE, _publicMav, new VariableDeclarator[] { vd });
      assertEquals("There should be no errors", 0, errors.size());
      
//      System.err.println("*** Beginning traversal of VariableDeclaration with String[]");
      vdecl.visit(_cbfjv);
//      System.err.println("Traversal of VariableDeclaration above is complete");
      assertEquals("There should be no errors", 0, errors.size());
//      System.err.println("That error is: " + errors.getLast().getFirst());
      
      SymbolData bob = LanguageLevelConverter.symbolTable.get("java.lang.String[]");
//      System.err.println("Getting READY to fail");
//      try { Thread.sleep(1000); } catch (Exception e) { };
      assertNotNull("bob should not be null", bob);
    }
    
    /* These test is shared with BodyIntermediateVisitor, perhaps we could factor it out. */
    
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
      VariableData vd1 = new VariableData("field1", _packageMav, SymbolData.DOUBLE_TYPE, false, _cbfjv._enclosing);
      VariableData vd2 = new VariableData("field2", _packageMav, SymbolData.BOOLEAN_TYPE, false, _cbfjv._enclosing);
      vdecl.visit(_cbfjv);
      assertEquals("There should not be any errors.", 0, errors.size());
      
//      System.err.println("_sd1 vars =  " + _sd1.getVars());
      assertTrue("field1 was added.", _sd1.getVars().contains(vd1));
      assertTrue("field2 was added.", _sd1.getVars().contains(vd2));
      
      // Check one that doesn't work
      VariableDeclaration vdecl2 = 
        new VariableDeclaration(SourceInfo.NONE,
                                _packageMav,
                                new VariableDeclarator[] {
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new PrimitiveType(SourceInfo.NONE, "double"), 
                                            new Word(SourceInfo.NONE, "field3")),
          new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                              new PrimitiveType(SourceInfo.NONE, "int"), 
                                              new Word(SourceInfo.NONE, "field3"))});
      VariableData vd3 = new VariableData("field3", _packageMav, SymbolData.DOUBLE_TYPE, false, _cbfjv._enclosing);
      vdecl2.visit(_cbfjv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "You cannot have two fields with the same name.  Either you already have a field by that name in " 
                     + "this class, or one of your superclasses or interfaces has a field by that name", 
                   errors.get(0).getFirst());
//      System.err.println("_sd1 vars =  " + _sd1.getVars());
      assertTrue("field3 was added.", _sd1.getVars().contains(vd3));
      
      //Check a static field that has not been assigned (won't work)
      VariableDeclaration vdecl3 = new VariableDeclaration(SourceInfo.NONE,
                                                           _staticMav,
                                                           new VariableDeclarator[] {
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new PrimitiveType(SourceInfo.NONE, "double"), 
                                            new Word (SourceInfo.NONE, "field4"))});
      // true value for hasAssigned in next line based on default initialization provided by Java.  GOOD IDEA?
      VariableData vd4 = new VariableData("field4", _staticMav, SymbolData.DOUBLE_TYPE, false, _cbfjv._enclosing);
      vdecl3.visit(_cbfjv);
//      System.err.println("vd4 = " + vd4);
      assertEquals("There should still be one error", 1, errors.size());
//      assertEquals("The error message should be correct", "All static fields must be initialized", 
//                   errors.get(1).getFirst());
//      System.err.println("_sd1 vars =  " + _sd1.getVars());
      assertTrue("field4 was added.", _sd1.getVars().contains(vd4));
      
      //Check a non-static field that has been assigned.  (will work);
      VariableDeclaration vdecl5 = new VariableDeclaration(SourceInfo.NONE,
                                                           _publicMav,
                                                           new VariableDeclarator[] {
        new InitializedVariableDeclarator(SourceInfo.NONE, 
                                          new PrimitiveType(SourceInfo.NONE, "double"), 
                                          new Word(SourceInfo.NONE, "field5"), 
                                          new DoubleLiteral(SourceInfo.NONE, 2.4))});
      vdecl5.visit(_cbfjv);
      VariableData vd5 = new VariableData("field5", _publicMav, SymbolData.DOUBLE_TYPE, true, _cbfjv._enclosing);
      vd5.setHasInitializer(true);
      assertEquals("There should still be one error", 1, errors.size());
      assertTrue("Field 5 was added.", _sd1.getVars().contains(vd5));
      
//      //check one that overrides the super class's field
//      VariableDeclaration vdecl6 = new VariableDeclaration(SourceInfo.NONE,
//                                                           _packageMav,
//                                                           new VariableDeclarator[] {
//        new UninitializedVariableDeclarator(SourceInfo.NONE, 
//                                            new PrimitiveType(SourceInfo.NONE, "double"), 
//                                            new Word(SourceInfo.NONE, "field6"))});
//      
//      
//      VariableData vd6 = new VariableData("field6", _packageMav, SymbolData.DOUBLE_TYPE, true, _cbfjv._enclosing);
//      SymbolData myData = new SymbolData("myData");
//      myData.addVar(vd6);
//      _cbfjv._enclosing.setSuperClass(myData);
//      vdecl6.visit(_cbfjv);
//      assertEquals("There should be two errors.", 2, errors.size());
//      assertEquals("The error message should be correct", 
//                   "You cannot have two fields with the same name.  Either you already have a field by that name in "
//                     + "this class, or one of your superclasses or interfaces has a field by that name", 
//                   errors.get(1).getFirst());
      
    }
    
    public void testFormalParameters2VariableData() {
      FormalParameter[] fps = new FormalParameter[] {
        new FormalParameter(SourceInfo.NONE, 
                            new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                                                new PrimitiveType(SourceInfo.NONE, "double"), 
                                                                new Word (SourceInfo.NONE, "field1")),
                            false),
          new FormalParameter(SourceInfo.NONE, 
                              new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                                                  new PrimitiveType(SourceInfo.NONE, "boolean"), 
                                                                  new Word (SourceInfo.NONE, "field2")),
                              false)};
      
      
      VariableData vd1 = new VariableData("field1", _packageMav, SymbolData.DOUBLE_TYPE, true, _cbfjv._enclosing);
      VariableData vd2 = new VariableData("field2", _packageMav, SymbolData.BOOLEAN_TYPE, true, _cbfjv._enclosing);
      
      VariableData[] mVds = new VariableData[] { vd1, vd2 };
      
      MethodData aMethod = new MethodData("method", 
                                          _publicMav, 
                                          new TypeParameter[0], 
                                          _cbfjv._enclosing, 
                                          mVds, 
                                          new String[0], 
                                          _cbfjv._enclosing,
                                          null);
      
      VariableData[] vds = _cbfjv.formalParameters2VariableData(fps, _cbfjv._enclosing);
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("vd1 should be the first entry in vds.", vd1, vds[0]);
      assertEquals("vd2 should be the second entry in vds.", vd2, vds[1]);
    }
    
    
    
    
    
    public void xtestForConcreteMethodDef() {
      // Test one that works.
      MethodDef mdef = new ConcreteMethodDef(SourceInfo.NONE, 
                                             _packageMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(SourceInfo.NONE, "int"), 
                                             new Word(SourceInfo.NONE, "methodName"),
                                             new FormalParameter[0],
                                             new ReferenceType[0], 
                                             new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      mdef.visit(_cbfjv);
      assertEquals("There should not be any errors.", 0, errors.size());
      // Test one that doesn't work.
      mdef = new ConcreteMethodDef(SourceInfo.NONE, 
                                   _packageMav, 
                                   new TypeParameter[0], 
                                   new PrimitiveType(SourceInfo.NONE, "int"), 
                                   new Word(SourceInfo.NONE, "monkey"),
                                   new FormalParameter[0],
                                   new ReferenceType[0], 
                                   new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      mdef.visit(_cbfjv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Only constructors can have the same name as the class they appear in, and constructors do not have an explicit return type",
                   errors.get(0).getFirst());
    }
    
    public void xtestForAbstractMethodDef() {
      // Test one that works.
      MethodDef mdef = new AbstractMethodDef(SourceInfo.NONE, 
                                             _abstractMav, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(SourceInfo.NONE, "int"), 
                                             new Word(SourceInfo.NONE, "methodName"),
                                             new FormalParameter[0],
                                             new ReferenceType[0]);
      _cbfjv._enclosing.setMav(_abstractMav);
      mdef.visit(_cbfjv);
      assertEquals("There should not be any errors.", 0, errors.size());
      // Test one that doesn't work.
      mdef = new AbstractMethodDef(SourceInfo.NONE, 
                                   _abstractMav, 
                                   new TypeParameter[0], 
                                   new PrimitiveType(SourceInfo.NONE, "int"), 
                                   new Word(SourceInfo.NONE, "monkey"),
                                   new FormalParameter[0],
                                   new ReferenceType[0]);
      mdef.visit(_cbfjv);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", 
                   "Only constructors can have the same name as the class they appear in, and constructors do not have an explicit return type",
                   errors.get(0).getFirst());
    }
    
    
    public void xtestForInitializedVariableDeclaratorDoFirst() {
      InitializedVariableDeclarator ivd = new InitializedVariableDeclarator(SourceInfo.NONE,
                                                                            new PrimitiveType(SourceInfo.NONE, "int"),
                                                                            new Word(SourceInfo.NONE, "i"),
                                                                            new IntegerLiteral(SourceInfo.NONE, 2));
      
      ivd.visit(_cbfjv);
      
      assertEquals("There should be no errors now", 0, errors.size());
    }
    
    public void xtestForInnerClassDef() {
      SymbolData obj = new SymbolData("java.lang.Object");
      LanguageLevelConverter.symbolTable.put("java.lang.Object", obj);
      InnerClassDef cd1 = new InnerClassDef(SourceInfo.NONE, _packageMav, new Word(SourceInfo.NONE, "Bart"),
                                            new TypeParameter[0], new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), new ReferenceType[0], 
                                            new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      InnerClassDef cd0 = new InnerClassDef(SourceInfo.NONE, _packageMav, new Word(SourceInfo.NONE, "Lisa"),
                                            new TypeParameter[0], new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), new ReferenceType[0], 
                                            new BracedBody(SourceInfo.NONE, new BodyItemI[] {cd1}));
      
      
      SymbolData sd0 = new SymbolData(_cbfjv._enclosing.getName() + "$Lisa", _packageMav, new TypeParameter[0], obj, 
                                      new ArrayList<SymbolData>(), null); 
      _cbfjv._enclosing.addInnerClass(sd0);
      sd0.setOuterData(_cbfjv._enclosing);
      SymbolData sd1 = new SymbolData(_cbfjv._enclosing.getName() + "$Lisa$Bart", _packageMav, new TypeParameter[0], 
                                      obj, new ArrayList<SymbolData>(), null); 
      sd0.addInnerClass(sd1);
      sd1.setOuterData(sd0);
      
      sd0.setIsContinuation(true);
      sd1.setIsContinuation(true);
      
      
      
      LanguageLevelConverter.symbolTable.put(_cbfjv._enclosing.getName() + "$Lisa", sd0);
//      LanguageLevelConverter.symbolTable.put(_cbfjv._enclosing.getName() + "$Lisa$Bart", sd1);
      
      cd0.visit(_cbfjv);
      
//      sd0.setName("Lisa");
//      sd1.setName("Bart");
      
      SymbolData sd = _cbfjv._enclosing.getInnerClassOrInterface("Lisa");
      assertEquals("There should be no errors", 0, errors.size());
      assertEquals("This symbolData should now have sd0 as an inner class", sd0, sd);
      assertEquals("sd0 should have the correct outer data", _cbfjv._enclosing, sd0.getOuterData());
      assertEquals("sd1 should have the correct outer data", sd0, sd1.getOuterData());
      assertEquals("Sd should now have sd1 as an inner class", sd1, sd.getInnerClassOrInterface("Bart"));
      
      
      assertEquals("Lisa should have 0 methods", 0, sd0.getMethods().size());
      
    }
    
    public void xtestForInnerInterfaceDef() {
//      SymbolData obj = new SymbolData("java.lang.Object");
//      LanguageLevelConverter.symbolTable.put("java.lang.Object", obj);
      InnerInterfaceDef cd1 = new InnerInterfaceDef(SourceInfo.NONE, _packageMav, new Word(SourceInfo.NONE, "Bart"),
                                                    new TypeParameter[0], new ReferenceType[0], 
                                                    new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      InnerInterfaceDef cd0 = new InnerInterfaceDef(SourceInfo.NONE, _packageMav, new Word(SourceInfo.NONE, "Lisa"),
                                                    new TypeParameter[0], new ReferenceType[0], 
                                                    new BracedBody(SourceInfo.NONE, new BodyItemI[] {cd1}));
      
      SymbolData sd0 = new SymbolData(_cbfjv._enclosing.getName() + "$Lisa", _packageMav, new TypeParameter[0], 
                                      new ArrayList<SymbolData>(), null); 
      SymbolData sd1 = new SymbolData(_cbfjv._enclosing.getName() + "$Lisa$Bart", _packageMav, new TypeParameter[0], 
                                      new ArrayList<SymbolData>(), null);
      sd0.addInnerInterface(sd1);
      sd0.setIsContinuation(true);
      sd1.setIsContinuation(true);
      
      _cbfjv._enclosing.addInnerInterface(sd0);
      sd0.setOuterData(_cbfjv._enclosing);
      
      sd0.addInnerInterface(sd1);
      sd1.setOuterData(sd0);
      
//    
//      LanguageLevelConverter.symbolTable.put(_cbfjv._enclosing.getName() + "$Lisa", sd0);
//      LanguageLevelConverter.symbolTable.put(_cbfjv._enclosing.getName() + "$Lisa$Bart", sd1);
      
      cd0.visit(_cbfjv);
      
      SymbolData sd = _cbfjv._enclosing.getInnerClassOrInterface("Lisa");
      
      assertEquals("There should be no errors", 0, errors.size());
      assertEquals("This symbolData should now have sd0 as an inner interface", sd0, sd);
      assertEquals("sd0 should have the correct outer data", _cbfjv._enclosing, sd0.getOuterData());
      assertEquals("sd1 should have the correct outer data", sd0, sd1.getOuterData());
      assertEquals("Sd should now have sd1 as an inner interface", sd1, sd.getInnerClassOrInterface("Bart"));
      assertTrue("Lisa should be an interface", sd0.isInterface());
      assertTrue("Bart should be an interface", sd1.isInterface());
      
      
    }
    
    public void xtestForConstructorDef() {
      //this is a ConstructorDef with no formal parameters and no throws
      ConstructorDef cd = new ConstructorDef(SourceInfo.NONE, new Word(SourceInfo.NONE, "MyClass"), _publicMav, new FormalParameter[0], new ReferenceType[0], 
                                             new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      //What if constructor name and SymbolData name don't match?  Should throw an error.
      _cbfjv._enclosing = new SymbolData("NotRightName");
      cd.visit(_cbfjv);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "The constructor return type and class name must match", errors.getLast().getFirst());
      
      //If they are the same, it should work just fine.
      _cbfjv._enclosing = new SymbolData("MyClass");
      
      MethodData constructor = new MethodData("MyClass", 
                                              _publicMav, 
                                              new TypeParameter[0], 
                                              _cbfjv._enclosing, 
                                              new VariableData[0], 
                                              new String[0], 
                                              _cbfjv._enclosing,
                                              null);
      
      
      cd.visit(_cbfjv);
      
      
      assertEquals("Should still be 1 error", 1, errors.size());
      assertEquals("SymbolData should have 1 method", 1, _cbfjv._enclosing.getMethods().size());
      assertTrue("SymbolData's constructor should be correct", _cbfjv._enclosing.getMethods().contains(constructor));
      
      //With a ConstructorDef with more throws and variables, should work okay.
      FormalParameter fp = 
        new FormalParameter(SourceInfo.NONE, 
                            new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                                                new PrimitiveType(SourceInfo.NONE, "int"), 
                                                                new Word(SourceInfo.NONE, "i")), 
                            false);
      ReferenceType rt = new TypeVariable(SourceInfo.NONE, "MyMadeUpException");
      ConstructorDef cd2 = 
        new ConstructorDef(SourceInfo.NONE, 
                           new Word(SourceInfo.NONE, "MyClass"), 
                           _publicMav, 
                           new FormalParameter[] {fp}, 
                           new ReferenceType[] {rt}, 
                           new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      VariableData vd = new VariableData("i", _finalMav, SymbolData.INT_TYPE, true, null);
      MethodData constructor2 = new MethodData("MyClass", _publicMav, new TypeParameter[0], _cbfjv._enclosing, 
                                               new VariableData[] {vd}, 
                                               new String[] {"MyMadeUpException"}, 
                                               _cbfjv._enclosing,
                                               null);
      
      
      
      constructor2.addVar(vd);
      cd2.visit(_cbfjv);
      vd.setEnclosingData(_cbfjv._enclosing.getMethods().getLast());                                        
      assertEquals("Should still be 1 error", 1, errors.size());
      assertEquals("SymbolData should have 2 methods", 2, _cbfjv._enclosing.getMethods().size());
      
      assertTrue("SymbolData should have new constructor", _cbfjv._enclosing.getMethods().contains(constructor2));
      
      
      //If two variable names are duplicated, should throw an error.
      FormalParameter fp2 = 
        new FormalParameter(SourceInfo.NONE, 
                            new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                                                new PrimitiveType(SourceInfo.NONE, "double"), 
                                                                new Word(SourceInfo.NONE, "i")), 
                            false);
      
      ConstructorDef cd3 = 
        new ConstructorDef(SourceInfo.NONE, 
                           new Word(SourceInfo.NONE, "MyClass"), 
                           _publicMav,
                           new FormalParameter[] {fp, fp2}, 
                           new ReferenceType[] {rt}, 
                           new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      cd3.visit(_cbfjv);
      
      assertEquals("Should now be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct","You cannot have two method parameters with the same name" , 
                   errors.getLast().getFirst());
      
      //Test that an error is thrown if the class name and constructor name are packaged differently
      _cbfjv._enclosing.setName("package.MyClass2");
      ConstructorDef cd4 = 
        new ConstructorDef(SourceInfo.NONE, 
                           new Word(SourceInfo.NONE, "different.MyClass2"), 
                           _publicMav, 
                           new FormalParameter[0], 
                           new ReferenceType[0], 
                           new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      cd4.visit(_cbfjv);
      
      assertEquals("There should now be 3 errors", 3, errors.size());
      assertEquals("Error message should be correct", "The constructor return type and class name must match", errors.getLast().getFirst());
    }
    public void testDummy() { }
  }
}