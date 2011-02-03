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
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.*;

import junit.framework.TestCase;

/** Top-level Language Level Visitor that represents the Intermediate Language Level.  Enforces constraints during the
  * first walk of the AST (checking for langauge specific errors and building the symbol table).
  * This class enforces things that are common to all contexts reachable at the Intermediate Language Level 
  * (i.e., inside class bodies, method bodies, interface bodies, etc), but also enforces specific top level 
  * constraints (i.e. you cannot have try catch statements at the top level, etc.)
  */
public class IntermediateVisitor extends LanguageLevelVisitor {
  
  /* Inheriting the following fields:
   * 
   File _file;
   String _package;
   String _enclosingClassName;
   LinkedList<String> _importedFiles;
   LinkedList<String> _importedPackages;
   HashMap<String, SymbolData> _generic types
   *
   */
  
  /** This constructor is called when creating a new instance of IntermediateVisitor.  The default value for className
    * is the empty string.  The default value of _genericTypes (a field of LanguageLevelVisitor) is an empty HashMap.
    */
  public IntermediateVisitor(File file, 
                             String packageName,
                             String enclosingClassName,
                             LinkedList<String> importedFiles, 
                             LinkedList<String> importedPackages,
                             HashSet<String> classesInThisFile, 
                             Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations,
                             LinkedList<Command> fixUps) {
    super(file, packageName, enclosingClassName, importedFiles, importedPackages, classesInThisFile, continuations, fixUps);
  }

  /** This constructor is called when testing.  It initializes all of the static fields of LanguageLevelVisitor. */
  public IntermediateVisitor(File file) {
    this(file, 
         new LinkedList<Pair<String, JExpressionIF>>(),
         new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(), 
         new LinkedList<Command>(),
         new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>());
  }
  
  /** This constructor is called from LanguageLevelVisitor and LanguageLevelConverter when they are instantiating a new
    * IntermediateVisitor to visit a new file with.  _package is set to "" by default. _enclosingClassName is set to null
    * @param file  The File corresponding to the source file we are visiting
    * @param errors  The list of errors that have been encountered so far.
    * @param continuations  The table of classes we have encountered but still need to resolve
    * @param fixUps   This list of symbolTable fixups to perform after visitation
    * @param visitedFiles  The list of files we have visited
    */
  public IntermediateVisitor(File file,
                             LinkedList<Pair<String, JExpressionIF>> errors,
                             Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations,
                             LinkedList<Command> fixUps,
                             LinkedList<Pair<LanguageLevelVisitor, SourceFile>> visitedFiles) {
    this(file, new LinkedList<String>(), errors, continuations, fixUps, visitedFiles);
  }
  
  /** This constructor is called from LanguageLevelVisitor and LanguageLevelConverter when they are instantiating a new
    * IntermediateVisitor to visit a new file with.  _package is set to "" by default. _enclosingClassName is set to null
    * @param file             The File corresponding to the source file we are visiting
    * @param importedPackages The list of strings describing the imported packages for this file
    * @param errors           The list of errors that have been encountered so far.
    * @param continuations    The table of classes we have encountered but still need to resolve
    * @param fixUps           This list of symbolTable fixups to perform after visitation
    * @param visitedFiles     The list of files we have visited
    */
  public IntermediateVisitor(File file,
                             LinkedList<String> importedPackages,
                             LinkedList<Pair<String, JExpressionIF>> errors,
                             Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations,
                             LinkedList<Command> fixUps,
                             LinkedList<Pair<LanguageLevelVisitor, SourceFile>> visitedFiles) {
    super(file, "", null, new LinkedList<String>(), importedPackages, new HashSet<String>(), continuations, fixUps);
    this.errors = errors;
    this.visitedFiles= visitedFiles;
//    _hierarchy = new Hashtable<String, TypeDefBase>(); //hierarchy;
  }
  
  /** Factory method inherited from LLV class. */
  public LanguageLevelVisitor newClassBodyVisitor(SymbolData anonSD, String anonName) {
    return new ClassBodyFullJavaVisitor(anonSD, anonName, _file, _package, _importedFiles, _importedPackages, 
                                        _classesInThisFile, continuations, fixUps);
  }
  
  /** Check to make sure the inner class def is well-formed, resolve it, and store the resulting symbol in the outer 
    * class's list of inner classes.  This method is used in InterfaceBodyIntermediateVisitor but needs the correct 
    * Data so we pass it in.  This method is tested in those files.  We use the fully qualified name so that we 
    * don't accidentally conflict with another class in the symbol table.
    * @param that  AST Node for the inner class def
    * @param enclosing  Data that encloses this inner class; the inner class can be in a class or a method.
    * @param relName The relative name of the inner class including no qualifiers
    * @param name  Fully qualified name of the inner class. For a local class, we construct the same name as the Java 
    *   compiler, which inserts "$<seq#>" as the delimiter preceding the raw class name. For example, if class A
    *   has a method B with a local class C, then qualified name for this class is "A$1C", provided the class is the
    *   first local class (including anonymous classes) in the enclosing class.
    */
  // TODO: add a factory method for constructing the classbody visitor (so it is different for FullJava and Functional
  // and hoist this code into LanguageLevelVisitor
  protected void handleInnerClassDef(InnerClassDef that, Data enclosing, String relName, String name) {
//    System.err.println("Processing InnerClassDef for " + name + " defined in " + enclosing.getName());
    
    assert (enclosing instanceof SymbolData) || (enclosing instanceof MethodData);
    forInnerClassDefDoFirst(that);
    if (prune(that)) return;
    
    that.getMav().visit(this);
    that.getName().visit(this);
    
    // TODO: type parameters are illegal in functional code.  Fix this!!!
//    for (int i = 0; i < that.getTypeParameters().length; i++) that.getTypeParameters()[i].visit(this);
    
    that.getSuperclass().visit(this);  // formerly commented out.  Why?
    
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    
    SymbolData sd = defineInnerSymbolData(that, relName, name, enclosing);
    if (sd != null) { // We have a SymbolData to work with, so visit the body and augment
      that.getBody().visit(new ClassBodyIntermediateVisitor(sd,
                                                            sd.getName(),
                                                            _file, 
                                                            _package,
                                                            _importedFiles, 
                                                            _importedPackages, 
                                                            _classesInThisFile, 
                                                            continuations,
                                                            fixUps));
      
      // The following methods are no-ops in FullJavaVisitor
      createAccessors(sd, _file);
      createToString(sd);
      createHashCode(sd);
      createEquals(sd);
    }
    // IMPORTANT: error message if sd is null?
    // Inner classes are not entered in _classesInThisFile since they are parsed when their outer classes are parsed.
    
    forInnerClassDefOnly(that);
  }
  
  /** Confirms that the inner interface is well-formed, resolves it, and stores it in the outer class's list of inner 
    * classes.  This method is common to both ClassBodyIntermediateVisitor and InterfaceBodyIntermediateVisitor but 
    * needs the correct SymbolData so we pass it in.  This method is tested in those files.
    * @param that  AST Node for the inner class def
    * @param enclosing  Data that encloses this inner class; the inner class can be in a class or a method.
    * @param name  Fully qualified name of the inner class. For a local class, we construct the same name as the Java 
    *   compiler (??), which inserts "$<seq#>" as the delimiter preceding the raw class name. For example, if class A
    *   has a method B with a local class C, then qualified name for this class is "A$1C", provided the class is the
    *   first local class (including anonymous classes) in the enclosing class.
    */
  // TODO: add a factory method for constructing the classbody visitor (so it is different for FullJava and Functional
  // and hoist this code into LanguageLevelVisitor
  protected void handleInnerInterfaceDef(InnerInterfaceDef that, Data enclosing, String relName, String name) {
//    System.err.println("Processing InnerInterfaceDef for " + name);
    assert (enclosing instanceof SymbolData) || (enclosing instanceof MethodData);
    forInnerInterfaceDefDoFirst(that);
    if (prune(that)) return;
    
    that.getMav().visit(this);
    that.getName().visit(this);
        
    // Type parameters are illegal in functional code.  TODO: Fix this!!!!
//    assert that.getTypeParameters().length == 0;

    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    
    SymbolData sd = defineInnerSymbolData(that, relName, name, enclosing);
    if (sd != null) { 
      that.getBody().visit(new InterfaceBodyIntermediateVisitor(sd, 
                                                                _file, 
                                                                _package,
                                                                _importedFiles, 
                                                                _importedPackages,
                                                                _classesInThisFile, 
                                                                continuations,
                                                                fixUps));
    }
    
    forInnerInterfaceDefOnly(that);
  }
  
  /** Only abstract, public, private, protected, static and final are allowed in language levels.*/
  public Void forModifiersAndVisibilityDoFirst(ModifiersAndVisibility that) {
    String[] modifiersAndVisibility = that.getModifiers();
    StringBuffer sb = new StringBuffer();
    String temp;
    int count = 0;    
    for(int i = 0; i < modifiersAndVisibility.length; i++) {
      temp = modifiersAndVisibility[i];
      if (! temp.equals("final") && ! temp.equals("abstract") && ! temp.equals("public") && ! temp.equals("private") && 
          ! temp.equals("protected") && ! temp.equals("static")) {
        sb.append(" \"" + temp + "\"");
        count++;
      }
    }
    // check if any illegal keywords were found
    temp = "The keyword";
    if (sb.length() > 0) {
      if (count > 1)  temp = temp + "s";
      _addAndIgnoreError(temp + sb.toString() + " cannot be used at the Intermediate level", that);
      return null;
    }
    return super.forModifiersAndVisibilityDoFirst(that);
  }
  
//  /** Do not allow explicit Package Statements at the Intermediate Level. */
//  public Void forPackageStatementDoFirst(PackageStatement that) {
////    _addError("Package statements cannot be used at the Intermediate level.  " +
////              "All Intermediate level classes and interfaces are assumed to be in the default package", that);
//    return null;
//  }
//  
  /* The following two overrides appear unnecessary. */
//  /** Allow inner classes at the Intermediate Level.  Override any inherited code. */
//  public Void forInnerClassDefDoFirst(InnerClassDef that) {
////    _addError("Inner classes cannot be used at the Intermediate level", that);
//    return null;
//  }
//  
//  /** Allow inner interfaces at the Intermediate Level. Override any inherited code. */
//    public Void forInnerInterfaceDefDoFirst(InnerInterfaceDef that) {
////    _addError("Nested interfaces cannot be used at the Intermediate level", that);
//    return null;
//  }
  
//  /**Do not allow static intiializers at the Intermediate Level.*/
//  public Void forStaticInitializerDoFirst(StaticInitializer that) {
//    _addError("Static initializers cannot be used at the Intermediate level", that);
//    return null;
//  }
  
//  /**Do not allow labeled statements at the Intermediate Level.*/
//  public Void forLabeledStatementDoFirst(LabeledStatement that) {
//    _addError("Labeled statements cannot be used at the Intermediate level", that);
//    return null;
//  }
  
//  /**Do not allow switch statements at the Intermediate Level.*/
//  public Void forSwitchStatementDoFirst(SwitchStatement that) {
//    _addError("Switch statements cannot be used at the Intermediate level", that);
//    return null;
//  }
  
//  /**Do not allow while statements at the Intermediate Level.*/
//  public Void forWhileStatementDoFirst(WhileStatement that) {
//    _addError("While statements cannot be used at the Intermediate level", that);
//    return null;
//  }
  
//  /**Do not allow do statements at the Intermediate Level.*/
//  public Void forDoStatementDoFirst(DoStatement that) {
//    _addError("Do statements cannot be used at the Intermediate level", that);
//    return null;
//  }
  
//  /**Do not allow for statements at the Intermediate Level.*/
//  public Void forForStatementDoFirst(ForStatement that) {
//    _addError("For statements cannot be used at the Intermediate level", that);
//    return null;
//  }
  
//  /**Do not allow break statements at the Intermediate Level.*/
//  public Void forBreakStatementDoFirst(BreakStatement that) {
//    _addError("Break statements cannot be used at the Intermediate level", that);
//    return null;
//  }
  
//  /**Do not allow continue statements at the Intermediate Level.*/
//  public Void forContinueStatementDoFirst(ContinueStatement that) {
//    _addError("Continue statements cannot be used at the Intermediate level", that);
//    return null;
//  }
  
  /**Do not allow synchronized statements at the Intermediate Level.*/
  public Void forSynchronizedStatementDoFirst(SynchronizedStatement that) {
    _addError("Synchronized statements cannot be used at the Intermediate level", that);
    return null;
  }
  
//  /**Do not allow try-catch statements at the Intermediate Level.*/
//  public Void forTryCatchStatementDoFirst(TryCatchStatement that) {
//    _addAndIgnoreError("A try-catch statement cannot appear here", that);
//    return null;
//  }
  
//  /**Make sure that the formal parameter is not final*/
//  public Void forFormalParameterDoFirst(FormalParameter that) {
//    if (that.isIsFinal()) {
//      _addError("The keyword \"final\" cannot be used at the Intermediate level", that);
//    return null;
//    }
//    else {
//      return forJExpressionDoFirst(that);
//    }
//  }
  
  /**Do not allow type parameters (generics) at the Intermediate level*/
  public Void forTypeParameterDoFirst(TypeParameter that) {
    _addError("Type Parameters cannot be used at the Intermediate level", that);
    return null;
  }
  
//  /**Only allow the 4 basic primitives: int, double, boolean, and char*/
//  public Void forPrimitiveTypeDoFirst(PrimitiveType that) {
//    String name = that.getName();
//    if (!(name.equals("int") || name.equals("double") || name.equals("boolean") || name.equals("char"))) {
//      _addError("Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used " +
//                "at the Intermediate level", that);
//    return null;
//    }
//    else {
//      return forTypeDoFirst(that);
//    }
//  }
  
//  /**Do not allow arrays at the Intermediate Level*/
//  public Void forArrayTypeDoFirst(ArrayType that) {
//    _addError("Arrays cannot be used at the Intermediate level", that);
//    return null;
//  }
  
//  /**Do not allow conditional expressions at the Intermediate Level*/
//  public Void forConditionalExpressionDoFirst(ConditionalExpression that) {
//    _addError("Conditional expressions cannot be used at the Intermediate level", that);
//    return null;
//  }
  
//  /**Do not allow instanceof expressions at the Intermediate Level*/
//  public Void forInstanceofExpressionDoFirst(InstanceofExpression that) {
//    _addError("Instanceof expressions cannot be used at the Intermediate level", that);
//    return null;
//  }
  
  
  /*Check to see if className is one of the classes declared in the current source file,
   * by looking through classesInThisFile.*/
  private boolean _isClassInCurrentFile(String className) {
    Iterator<String> iter = _classesInThisFile.iterator();
    while (iter.hasNext()) {
      String s = iter.next();
      if (s.equals(className) || s.endsWith('.' + className)) {
        return true;
      }
    }
    return false;   
  }  
  
  /** Use the doFirst method to make sure there aren't any errors with the declaration.  Then, use defineSymbolData to 
    * create the appropriate SymbolData, and then visit the class body.  Finally, autogenerate the toString, equals, 
    * hashCode, and accessor methods.  The constructor will be autogenerated right before the TypeChecking pass starts.
    * Once the class def has been handled, remove it from _classesInThisFile.
    */
  public Void forClassDef(ClassDef that) {    
    forClassDefDoFirst(that);
    if (prune(that)) return null;
    
    String className = getQualifiedClassName(that.getName().getText());
//    System.err.println("Processing class " + className);
    
    String superName = that.getSuperclass().getName();
      
    SymbolData sd = defineSymbolData(that, className);
  
    // Note: sd returns null only if there is an error such as redefining a class
//    assert getQualifiedSymbolData(className, that.getSourceInfo()) != null;

    that.getMav().visit(this);
    that.getName().visit(this);
    that.getSuperclass().visit(this);
    
    if (sd != null) identifyInnerClasses(that);
    
    // Type parameters are illegal in functional code.
//    for (int i = 0; i < that.getTypeParameters().length; i++) that.getTypeParameters()[i].visit(this);
    
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    
    if (sd != null) {
      // Perform special processing for classes extending TestCase   
      if (superName.equals("TestCase") || superName.equals("junit.framework.TestCase")) {
        // Test cases are automatically public; isTestCase => sd != null
        sd.addModifier("public");
        // Check to see if we need to autognerate an import of TestCase class
        if (! _importedPackages.contains("junit.framework") && ! _importedFiles.contains("junit.framework.TestCase")) {
          sd.setHasAutoGeneratedJunitImport(true);
          _importedFiles.addLast("junit.framework.TestCase");
          createImportedSymbolContinuation("junit.framework.TestCase", that.getSourceInfo());
//          System.err.println("*********** Imported symbol continuation created for junit.framework.TestCase");
        }
        assert getQualifiedSymbolData("junit.framework.TestCase", that.getSourceInfo()) != null;
      }
      
      that.getBody().visit(new ClassBodyIntermediateVisitor(sd, className, _file, _package, _importedFiles, 
                                                            _importedPackages, _classesInThisFile, continuations,
                                                            fixUps));
      createAccessors(sd, _file);
      createToString(sd);
      createHashCode(sd);
      createEquals(sd);
    }
    
    forClassDefOnly(that);
    _classesInThisFile.remove(className);
    return null;
  }
  
  /** Use the doFirst method to make sure that there aren't any errors with the declaration.  Define a SymbolData 
    * corresponding to this interface definition. Then visit the body to handle anything defined inside the interfac
    * Once the interface has been resolved, remove it from _classesInThisFile.
    */
  public Void forInterfaceDef(InterfaceDef that) {
    forInterfaceDefDoFirst(that);
    if (prune(that)) return null;
    String className = getQualifiedClassName(that.getName().getText());
    
    // Type parameters are illegal in functional code.  Fix this!!!
//    assert that.getTypeParameters().length == 0;
    
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);

    SymbolData sd = defineSymbolData(that, className);
    // Note: sd can only be null if an error occurs in defineSymbol
    
    if (sd != null) {
      sd.setInterface(true);
      identifyInnerClasses(that);  // inner interfaces??
      that.getBody().visit(new InterfaceBodyIntermediateVisitor(sd, _file, _package, _importedFiles, _importedPackages, 
                                                                _classesInThisFile, continuations, fixUps));
    }
    
    that.getMav().visit(this);
    that.getName().visit(this);
    forInterfaceDefOnly(that);
    _classesInThisFile.remove(className);
    return null;
  }
  
  /**Bitwise operators are not allowed at any language level...*/
  public Void forShiftAssignmentExpressionDoFirst(ShiftAssignmentExpression that) {
    _addAndIgnoreError("Shift assignment operators cannot be used at any language level", that);
    return null;
  }
  public Void forBitwiseAssignmentExpressionDoFirst(BitwiseAssignmentExpression that) {
    _addAndIgnoreError("Bitwise operators cannot be used at any language level", that);
    return null;
  }
  public Void forBitwiseBinaryExpressionDoFirst(BitwiseBinaryExpression that) {
    _addAndIgnoreError("Bitwise binary expressions cannot be used at any language level", that);
    return null;
  }
  public Void forBitwiseOrExpressionDoFirst(BitwiseOrExpression that) {
//    System.err.println("Visiting BitwiseOrExpression: " + that);
    _addAndIgnoreError("Bitwise or expressions cannot be used in the functional language level." + 
                       "  Perhaps you meant to compare two values using regular or (||)", that);
    return null;
  }
  public Void forBitwiseXorExpressionDoFirst(BitwiseXorExpression that) {
    _addAndIgnoreError("Bitwise xor expressions cannot be used at any language level", that);
    return null;
  }
  public Void forBitwiseAndExpressionDoFirst(BitwiseAndExpression that) {
    _addAndIgnoreError("Bitwise and expressions cannot be used at any language level." + 
                       "  Perhaps you meant to compare two values using regular and (&&)", that);
    return null;
  }
  public Void forBitwiseNotExpressionDoFirst(BitwiseNotExpression that) {
    _addAndIgnoreError("Bitwise not expressions cannot be used at any language level." + 
                       "  Perhaps you meant to negate this value using regular not (!)", that);
    return null;
  }
  public Void forShiftBinaryExpressionDoFirst(ShiftBinaryExpression that) {
    _addAndIgnoreError("Bit shifting operators cannot be used at any language level", that);
    return null;
  }
  public Void forBitwiseNotExpressionDoFirst(ShiftBinaryExpression that) {
    _addAndIgnoreError("Bitwise operators cannot be used at any language level", that);
    return null;
  }
  
//  /** Call the super method to convert these to a VariableData array, then make sure that
//    * each VariableData is final, as required at the Intermediate level.
//    * @param enclosingData  The Data which contains the variables
//     */
//  protected VariableData[] _variableDeclaration2VariableData(VariableDeclaration vd, Data enclosingData) {
//    VariableData[] vds = super._variableDeclaration2VariableData(vd, enclosingData);
//    for (int i = 0; i < vds.length; i++) {
//      if ((vds[i].hasModifier("static") && vds[i].getMav().getModifiers().length > 1) || 
//          (! vds[i].hasModifier("static") && vds[i].getMav().getModifiers().length > 0)) {
//        StringBuffer s = new StringBuffer("the keyword(s) ");
//        String[] modifiers = vds[i].getMav().getModifiers();
//        for (int j = 0; j<modifiers.length; j++) {
//          if (! modifiers[j].equals("static")) {s.append("\"" + modifiers[j] + "\" ");}
//        }
//        _addAndIgnoreError("You cannot use " + s.toString() + "to declare a field at the Intermediate level.  " + 
//                           "Only the keyword 'static' is allowed", vd);
//      }
//      if (vds[i].hasModifier("static")) vds[i].addModifier("public");
//      else vds[i].addModifier("private"); 
//      vds[i].setFinal();
//    }
//    return vds;
//  }
  
  /** Pass this call directly onto the language level visitor.  This is a hack to bypass the privateAndFinal setting 
    * when we are dealing with local variables.
    */
  protected VariableData[] llVariableDeclaration2VariableData(VariableDeclaration vd, Data enclosingData) {
    return super._variableDeclaration2VariableData(vd, enclosingData);
  }
  
//  /** Do the work that is shared between SimpleAnonymousClassInstantiations and ComplexAnonymousClassInstantiations.  Do
//    * not generate automatic accessors for the anonymous class--this will be done in type checker pass.
//    * @param that  The AnonymousClassInstantiation being visited
//    * @param enclosing  The enclosing Data
//    * @param superC  The super class being instantiated--i.e. new A() { ...}, would have a super class of A.
//    */
//  public void anonymousClassInstantiationHelper(AnonymousClassInstantiation that, SymbolData enclosing, 
//    SymbolData superC) {
//  
//    that.getArguments().visit(this); 
//    
//    // Generate the internal class name for this anonymous inner class
//    String anonName = getQualifiedClassName(enclosing.getSymbolData().getName()) + "$" + 
//      enclosing.getSymbolData().preincrementAnonymousInnerClassNum();
//    
//    // Create the SymbolData that will correspond to this anonymous class
//    SymbolData sd = new SymbolData(anonName);
//    enclosing.addInnerClass(sd);
//    sd.setOuterData(enclosing);
//    
//    if (superC != null && ! superC.isInterface()) {
//      sd.setSuperClass(superC); // the super class is what was passed in
//    }
//    sd.setPackage(_package);
//    
//    createToString(sd);
//    createHashCode(sd);
//    createEquals(sd);
//    //accessors will be filled in in typeChecker pass
//    
//    //visit the body to get it all nice and resolved.  NOTE: what about the fact that all of the methods MUST BE PUBLIC
//    that.getBody().visit(new ClassBodyIntermediateVisitor(sd, anonName, _file, _package, _importedFiles,
//                                                          _importedPackages, _classesInThisFile, continuations,
//                                                          fixUps));
//  }
  
  /** Look up the supertype of this class instantiation and add it to the symbol table. Visit the body of the class 
    * instantiation.  The processing of this anonymous inner class (i.e. adding it to the enclosing SD's list of inner 
    * classes, creating a SymbolData for the anonyomous inner class, etc) will be handled in the TypeChecker pass.
    * This is because no one will depend on that symbolData until we create it.
    * @param that       The SimpleAnonymousClassInstantiation being processed.
    * @param enclosing  The SymbolData of the enclosing class.
    */
  public void simpleAnonymousClassInstantiationHelper(SimpleAnonymousClassInstantiation that, SymbolData enclosing) {
    forSimpleAnonymousClassInstantiationDoFirst(that);
    if (prune(that)) return;
    
    String superName = that.getType().getName();
    anonymousClassInstantiationHelper(that, enclosing, superName);
    
    forSimpleAnonymousClassInstantiationOnly(that);
  }
  
  /** Do not resolve the super class type of this instantiation, because it will have already been resolved (it is an
    * inner class of the enclosing SD.  When the enclosing SD was resolved, all of its inner classes 
    * should have also been resolved).  Visit the body of the class instantiation.  
    * @param that  The ComplexAnonymousClassInstantiation being processed.
    * @param enclosing  The SymbolData of the enclosing class.
    */
  public void complexAnonymousClassInstantiationHelper(ComplexAnonymousClassInstantiation that, SymbolData enclosing) {
    forComplexAnonymousClassInstantiationDoFirst(that);
    if (prune(that)) return;
    
    //visit the enclosing 
    that.getEnclosing().visit(this);
    
    String superName = that.getType().getName();
    anonymousClassInstantiationHelper(that, enclosing, superName);
    
    forComplexAnonymousClassInstantiationOnly(that);
  }
  
  /** Test the methods declared in the above class. */
  public static class IntermediateVisitorTest extends TestCase {
    
    private IntermediateVisitor _iv;
    
    private SymbolData _sd1;
    private SymbolData _sd2;
    private SymbolData _sd3;
    private SymbolData _sd4;
    private SymbolData _sd5;
    private SymbolData _sd6;
    public static final ModifiersAndVisibility PUBLIC_MAV = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] { "public" });
    public static final ModifiersAndVisibility PROTECTED_MAV = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] { "protected" });
    public static final ModifiersAndVisibility PRIVATE_MAV = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] { "private" });
    public static final ModifiersAndVisibility PACKAGE_MAV = new ModifiersAndVisibility(SourceInfo.NONE, new String[0]);
    public static final ModifiersAndVisibility ABSTRACT_MAV = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract"});
    public static final ModifiersAndVisibility PRIVATE_ABSTRACT_MAV = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract", "private"});  // illegal
    public static final ModifiersAndVisibility STATIC_MAV = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"static"});
    public static final ModifiersAndVisibility FINAL_MAV = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final"});
    
    public IntermediateVisitorTest() { this(""); }
    public IntermediateVisitorTest(String name) {
      super(name);
//      _file = File.createTempFile("DrJava-test", ".java");
//      _iv = new ElementaryVisitor(new File(""), "", new LinkedList<String>(), new LinkedList<String>(), 
//                                 new LinkedList<String>());
    }
    
    public void setUp() {
      
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable.clear();
      LanguageLevelConverter._newSDs.clear();
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, IterUtil.make(new File("lib/buildlib/junit.jar")));
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>();      
//      _hierarchy = new Hashtable<String, TypeDefBase>();
      
      _iv = new IntermediateVisitor(new File(""),
                                    errors,
                                    continuations,
                                    new LinkedList<Command>(),
                                    new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>());
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, IterUtil.make(new File("lib/buildlib/junit.jar")));
      _iv._classesInThisFile = new HashSet<String>();
      _iv.continuations = new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>();
//      _iv._resetNonStaticFields();
      _iv._importedPackages.addFirst("java.lang");
      _errorAdded = false;
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");
      
      _sd1.setIsContinuation(false);
      _sd1.setInterface(false);
      _sd1.setPackage("");
      _sd1.setTypeParameters(new TypeParameter[0]);
      _sd1.setInterfaces(new ArrayList<SymbolData>()); 
      
      _iv._enclosingClassName = "i.like.monkey";
      _iv.symbolTable.put("i.like.monkey", _sd1);
      SymbolData objectSD = _iv.getQualifiedSymbolData("java.lang.Object", SourceInfo.NONE);
      _sd1.setSuperClass(objectSD);
      _errorAdded = false;  // static field of this.  TODO: fix this!
    }
    
    public void initTopLevel() {
      _iv._enclosingClassName = null;
    }
    
    public void testForModifiersAndVisibilityDoFirst() {
      
      // Check that the proper modifiers are allowed:
      _iv.forModifiersAndVisibilityDoFirst(ABSTRACT_MAV);
      _iv.forModifiersAndVisibilityDoFirst(PUBLIC_MAV);
      _iv.forModifiersAndVisibilityDoFirst(PRIVATE_MAV);
      _iv.forModifiersAndVisibilityDoFirst(PROTECTED_MAV);
      _iv.forModifiersAndVisibilityDoFirst(STATIC_MAV);
      
      ModifiersAndVisibility mavs = new ModifiersAndVisibility(SourceInfo.NONE, 
                                                               new String[] {"private", "static"});
      _iv.forModifiersAndVisibilityDoFirst(mavs);
      assertEquals("there should still be 0 errors", 0, errors.size());
      
      //check errors:
      
      _iv.forModifiersAndVisibilityDoFirst(FINAL_MAV);
//      assertEquals("there should now be 1 errors", 1, errors.size());
//      assertEquals("The error message should be correct for private modifier:", 
//                   "The keyword \"final\" cannot be used at the Intermediate level", errors.get(0).getFirst());
      
      ModifiersAndVisibility mavs2 = new ModifiersAndVisibility(SourceInfo.NONE, 
                                                                new String[] {"private", "final"});
      
      _iv.forModifiersAndVisibilityDoFirst(mavs2);
      assertEquals("There should still be 0 errors", 0, errors.size());
      
      ModifiersAndVisibility mavs3 = new ModifiersAndVisibility(SourceInfo.NONE, 
                                                                new String[] {"final", "native"});
      
      _iv.forModifiersAndVisibilityDoFirst(mavs3);
      assertEquals("There should now be 1 errors", 1, errors.size());
      assertEquals("The error message should be correct for 1 bad modifier:",
                   "The keyword \"native\" cannot be used at the Intermediate level", 
                   errors.get(0).getFirst());
    }
    
    public void testForClassDefDoFirst() {
      //check an example that works
      ClassDef cd0 = new ClassDef(SourceInfo.NONE, PUBLIC_MAV, new Word(SourceInfo.NONE, "Lisa"),
                                  new TypeParameter[0], 
                                  new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                                  new ReferenceType[0], 
                                  new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      _iv.forClassDefDoFirst(cd0);
      assertEquals("should be no errors", 0, errors.size());
      
      //check that an error is not thrown if a class doesn't explicitely extend another class
      ClassDef cd1 = new ClassDef(SourceInfo.NONE, PUBLIC_MAV, 
                                  new Word(SourceInfo.NONE, "Test"), new TypeParameter[0], JExprParser.NO_TYPE,
                                  new ReferenceType[0], new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      _iv.forClassDefDoFirst(cd1);
      assertEquals("there should still be 0 errors", 0, errors.size());
      
      //check that an error is not thrown if a class implements any interfaces.
      ClassDef cd2 = 
        new ClassDef(SourceInfo.NONE, PUBLIC_MAV, 
                     new Word(SourceInfo.NONE, "Test"),
                     new TypeParameter[0],
                     new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                     new ReferenceType[] {new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0])}, 
                     new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      
      _iv.forClassDefDoFirst(cd2);
      assertEquals("there should still be 0 errors", 0, errors.size());
    }
    
    public void testForFormalParameterDoFirst() {
      PrimitiveType pt = new PrimitiveType(SourceInfo.NONE, "int");
      Word w = new Word(SourceInfo.NONE, "param");
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(SourceInfo.NONE, pt, w);
      
      // check an example that works
      FormalParameter fp = new FormalParameter(SourceInfo.NONE, uvd, false);
      _iv.forFormalParameterDoFirst(fp);
      assertEquals("should be no errors", 0, errors.size());
      
      // check that an error is thrown if the FormalParameter is final
      FormalParameter fp2 = new FormalParameter(SourceInfo.NONE, uvd, true);  
      _iv.forFormalParameterDoFirst(fp2);
      assertEquals("should be no errors", 0, errors.size());
    }
    
    public void test_NotAllowed() {
      SourceInfo noInfo = SourceInfo.NONE;
      Word w = new Word(SourceInfo.NONE, "word");
      TypeParameter[] tps = new TypeParameter[0];
      ReferenceType[] rts = new ReferenceType[0];
      BracedBody emptyBody = new BracedBody(SourceInfo.NONE, new BodyItemI[0]);
      ClassOrInterfaceType superClass = new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]);
      FormalParameter[] fps = new FormalParameter[0];
      CompoundWord cw = new CompoundWord(noInfo, new Word[0]);
      Statement stmt = new EmptyStatement(noInfo);
      Expression e = new NullLiteral(noInfo);
      Block b = new Block(noInfo, emptyBody);
      //      ClassModifier cm = ClassModifier.NONE;
      TypeVariable tv = new TypeVariable(noInfo, "name");
      
      InnerInterfaceDef ii = new InnerInterfaceDef(noInfo, PUBLIC_MAV, w, tps, rts, emptyBody);
      InnerClassDef ic = new InnerClassDef(noInfo, PUBLIC_MAV, w, tps, superClass, rts, emptyBody);
      
      StaticInitializer si = new StaticInitializer(noInfo, b);
      LabeledStatement ls = new LabeledStatement(noInfo, new Word(noInfo, "label"), stmt);
      SwitchStatement ss = new SwitchStatement(noInfo, e, new SwitchCase[0]);
      WhileStatement ws = new WhileStatement(noInfo, e, stmt);
      DoStatement ds = new DoStatement(noInfo, stmt, e);
      ForStatement fs = new ForStatement(noInfo, new UnparenthesizedExpressionList(noInfo, new Expression[] {e}), 
                                         e, new UnparenthesizedExpressionList(noInfo, new Expression[] {e}),
                                         stmt);
      BreakStatement bs = new UnlabeledBreakStatement(noInfo);
      ContinueStatement cs = new UnlabeledContinueStatement(noInfo);
      SynchronizedStatement syncs = new SynchronizedStatement(noInfo, e, b);
      TypeParameter tp = new TypeParameter(noInfo, tv, superClass);
      ArrayInitializer ai = new ArrayInitializer(noInfo, new VariableInitializerI[0]);
      ArrayType at = new ArrayType(noInfo, "myName", tv);
      VoidReturn vr = new VoidReturn(noInfo, "string");
      ConditionalExpression ce = new ConditionalExpression(noInfo, e, e, e);
      
      BracedBody hasBitOperator = 
        new BracedBody(SourceInfo.NONE, new BodyItemI[] { 
        new ExpressionStatement(SourceInfo.NONE, 
                                new BitwiseOrAssignmentExpression(SourceInfo.NONE, 
                                                                  new SimpleNameReference(SourceInfo.NONE, 
                                                                                          new Word(SourceInfo.NONE, 
                                                                                                   "i")), 
                                                                  new IntegerLiteral(SourceInfo.NONE, 5)))});
      
      TryCatchStatement tcs = new NormalTryCatchStatement(noInfo, b, new CatchBlock[0]);
      
      si.visit(_iv);
      assertEquals("After visiting static initializer, errors should still be 0", 0, errors.size());
      
      ii.visit(_iv);
      assertEquals("After visiting inner interface, errors should still be 0", 0, errors.size());
      
      ic.visit(_iv);
      assertEquals("After visiting inner class, errors should still be 0", 0, errors.size());
      
      ls.visit(_iv);
      assertEquals("After visiting labeled statment, errors should still be 0", 0, errors.size());
      
      ss.visit(_iv);
      assertEquals("After visiting switch statment, errors should still be 0", 0, errors.size());
      
      ws.visit(_iv);
      assertEquals("After visiting while statment, errors should still be 0", 0, errors.size());
      
      ds.visit(_iv);
      assertEquals("After visiting do statment, errors should still be 0", 0, errors.size());
      
      fs.visit(_iv);
      assertEquals("After visiting for statment, errors should still be 0", 0, errors.size());
      
      bs.visit(_iv);
      assertEquals("After visiting break statment, errors should still be 0", 0, errors.size());
      
      cs.visit(_iv);
      assertEquals("After visiting continue statment, errors should still be 0", 0, errors.size());
      
      syncs.visit(_iv);
      assertEquals("After visiting synchronized statment, errors should now be 1", 1, errors.size());
      assertEquals("SynchronizedStatement is not allowed", 
                   "Synchronized statements cannot be used at the Intermediate level", 
                   errors.getLast().getFirst());
      
      tp.visit(_iv);
      assertEquals("After visiting type parameter, errors should now be 2", 2, errors.size());
      assertEquals("TypeParameters are not allowed", 
                   "Type Parameters cannot be used at the Intermediate level", 
                   errors.getLast().getFirst());
    }
    
    public void testForPrimitiveTypeDoFirst() {
      
      SourceInfo noInfo = SourceInfo.NONE;
      
      //only primative types boolean, char, int, and double are allowed at Intermediate level. 
      PrimitiveType i = new PrimitiveType(noInfo, "int");
      PrimitiveType c = new PrimitiveType(noInfo, "char");
      PrimitiveType d = new PrimitiveType(noInfo, "double");
      PrimitiveType b = new PrimitiveType(noInfo, "boolean");
      
      i.visit(_iv);
      assertEquals("After visiting int, errors should still be 0", 0, errors.size());
      
      c.visit(_iv);
      assertEquals("After visiting char, errors should still be 0", 0, errors.size());
      
      d.visit(_iv);
      assertEquals("After visiting double, errors should still be 0", 0, errors.size());
      
      b.visit(_iv);
      assertEquals("After visiting boolean, errors should still be 0", 0, errors.size());
      
      // now the types that formerly threw errors:
      
      PrimitiveType byt = new PrimitiveType(noInfo, "byte");
      PrimitiveType s = new PrimitiveType(noInfo, "short");
      PrimitiveType l = new PrimitiveType(noInfo, "long");
      PrimitiveType f = new PrimitiveType(noInfo, "float");
      
      byt.visit(_iv);
      assertEquals("After visiting byte, errors should be 0", 0, errors.size());
      
      s.visit(_iv);
      assertEquals("After visiting short, errors should be 0", 0, errors.size());
      
      l.visit(_iv);
      assertEquals("After visiting long, errors should be 0", 0, errors.size());
      
      f.visit(_iv);
      assertEquals("After visiting float, errors should be 0", 0, errors.size());
    }
    
    public void test_isClassInCurrentFile() {
      assertFalse("class not in file should return false", _iv._isClassInCurrentFile("NotInFile"));
      _iv._classesInThisFile.add("package.MyClass");
      assertTrue("full class name in file should return true", _iv._isClassInCurrentFile("package.MyClass"));
      assertTrue("unqualified class name in file should return true", _iv._isClassInCurrentFile("MyClass"));
      
      assertFalse("partial name in file, not same class, should return false", _iv._isClassInCurrentFile("Class"));
      
    }
  
    /** Tests createConstructor.  Must ensure that no fixups remain pending before invoking this method. */
    public void testCreateConstructor() {
      SymbolData sd =
        new SymbolData("ClassName", PUBLIC_MAV, new TypeParameter[0], null, new ArrayList<SymbolData>(), null);
      VariableData v1 = new VariableData("i", PUBLIC_MAV, SymbolData.INT_TYPE, false, sd);
      VariableData v2 = new VariableData("j", PUBLIC_MAV, SymbolData.CHAR_TYPE, false, sd);
      sd.addVar(v1);
      sd.addVar(v2);
      sd.setSuperClass(_sd1);
      sd.setIsContinuation(false);

//      System.err.println("****** Creating constructor for " + _sd1);
      _iv.createConstructor(_sd1);  // Cannot create constructor for s1 without creating one for its superclass.
      
      /* Construct expected MethodData */
      // Copy vars without visibility
      LinkedList<VariableData> params = new LinkedList<VariableData>();
      for (VariableData vd: sd.getVars()) {
        VariableData newParam = vd.copyWithoutVisibility();
        newParam.setGenerated(true);
        params.add(newParam);
      };
        
      MethodData md = new MethodData("ClassName", 
                                     PUBLIC_MAV, 
                                     new TypeParameter[0], 
                                     sd, 
                                     params.toArray(new VariableData[params.size()]), 
                                     new String[0], 
                                     sd,
                                     null);
      md.addVars(md.getParams());     
      md.setGenerated(true);
      
//      System.err.println("****** Before creating Classname constructor, ClassName methods = " + sd.getMethods());
//      System.err.println("****** Creating constructor for " + sd);
      _iv.createConstructor(sd);
//      System.err.println("****** After creating constructor, ClassName method = " + sd.getMethods());

      MethodData conSD = sd.getMethods().getFirst();
          
//      System.err.println("****** Generated MethodData: " + conSD.toBigString());
//      System.err.println("****** Expectred MethodData: " + md.toBigString());
      assertEquals("sd should have 1 method: its own constructor", md, conSD);
      
//      // Since this is the only constructor in the SymbolData, all the fields should be assigned after visiting sd.
//      v1 = new VariableData("i", PUBLIC_MAV, SymbolData.INT_TYPE, true, sd);
//      v2 = new VariableData("j", PUBLIC_MAV, SymbolData.CHAR_TYPE, true, sd);
      
      // We no longer do this in createConstructor
//      assertTrue("v1 should be correct--set to true", sd.getVars().contains(v1));
//      assertTrue("v2 should be correct--set to true", sd.getVars().contains(v2));

      // Now test a subclass of sd:

      SymbolData subSD = 
        new SymbolData("Subclass", PUBLIC_MAV, new TypeParameter[0], null, new ArrayList<SymbolData>(), null);
      VariableData v3 = new VariableData("var", PUBLIC_MAV, SymbolData.DOUBLE_TYPE, false, subSD);
      subSD.addVar(v3);
      
//      // Revise params rather than recreating them, because they contain hidden fields like enclosingData that
//      // must be right for equality testing to succeeed
//      params.get(0).setName("super_i");
//      params.get(1).setName("super_j");
//      VariableData v3Param = v3.copyWithoutVisibility();
//      v3Param.setGenerated(true);
//      params.add(v3Param);
      
      subSD.setSuperClass(sd);
      
      // Create copies of v1, v2. v3 with Package MAV
      VariableData v1Param = new VariableData("super_i", PACKAGE_MAV, SymbolData.INT_TYPE, true, subSD);
      VariableData v2Param = new VariableData("super_j", PACKAGE_MAV, SymbolData.CHAR_TYPE, true, subSD);
      VariableData v3Param = new VariableData("var", PACKAGE_MAV, SymbolData.DOUBLE_TYPE, true, subSD);
          
      v1Param.setGenerated(true);
      v2Param.setGenerated(true);
      v3Param.setGenerated(true);
      
      VariableData[] newParams = new VariableData[] { v1Param, v2Param, v3Param };
      
//      VariableData[] newParams = params.toArray(new VariableData[params.size()]);
      
      MethodData md2 = 
        new MethodData("Subclass", 
                       PUBLIC_MAV, 
                       new TypeParameter[0], 
                       subSD, 
                       newParams,
                       new String[0], 
                       subSD, 
                       null);
      
      md2.addVars(md2.getParams());
      md2.setGenerated(true);
      
      _iv.createConstructor(subSD);
      
      conSD = subSD.getMethods().getFirst();  // Reusing conSD local variable
//      System.err.println("****** Expected params = " + Arrays.toString(md2.getParams()));
//      System.err.println("****** Results  params = " + Arrays.toString(conSD.getParams()));
      
//      System.err.println("****** Expected vars = " + md2.getVars());
//      System.err.println("****** Results  vars = " + conSD.getVars());
      
//      System.err.println("****** Constructor for Subclass is: " + conSD.toBigString());
//      System.err.println("****** Expected MethodData is: " + md2.toBigString());
      
      assert md2.getName().equals(conSD.getName());
      assert md2.getMav().equals(conSD.getMav());
      assert arrayEquals(md2.getTypeParameters(), conSD.getTypeParameters());
      assert arrayEquals(md2.getThrown(), conSD.getThrown());
      assert md2.getEnclosingData().equals(conSD.getEnclosingData());
      LinkedList<VariableData> mVars = md2.getVars();
      LinkedList<VariableData> cVars = conSD.getVars();
      assert mVars.size() == cVars.size();
      for (int i = 0; i < mVars.size(); i++) {
//        System.err.println("****** EnclosingData of mVars " + i + " = " + mVars.get(i).getEnclosingData());
//        System.err.println("****** EnclosingData of cVars " + i + " = " + cVars.get(i).getEnclosingData());
        assertEquals("Var Test" + i, mVars.get(i), cVars.get(i));
      }
        
      assert md2.getVars().equals(conSD.getVars());
      assert arrayEquals(md2.getParams(), conSD.getParams());

      assertEquals("subSD should have 1 method: its own constructor.", md2, conSD);
    }
    
    public void test_getFieldAccessorName() {
      // This may change in the future if we change getFieldAccessorName
      assertEquals("Should correctly convert from lower case to upper case", "name", _iv.getFieldAccessorName("name"));
    }
    
    public void testCreateToString() {
      SymbolData sd = 
        new SymbolData("ClassName", PUBLIC_MAV, new TypeParameter[0], null, new ArrayList<SymbolData>(), null);
      
      MethodData md = new MethodData("toString", 
                                     PUBLIC_MAV,
                                     new TypeParameter[0],
                                     _iv.getSymbolData("java.lang.String", SourceInfo.make("java.lang.String")), 
                                     new VariableData[0],
                                     new String[0], 
                                     sd,
                                     null); // no SourceInfo
      
      _iv.createToString(sd);
      assertEquals("sd should have 1 method: toString", md, sd.getMethods().getFirst());
    }
    
    public void testCreateHashCode() {
      SymbolData sd = new SymbolData("ClassName", 
                                     PUBLIC_MAV, 
                                     new TypeParameter[0], 
                                     null, 
                                     new ArrayList<SymbolData>(), 
                                     null);      
      MethodData md = new MethodData("hashCode",
                                     PUBLIC_MAV, 
                                     new TypeParameter[0], 
                                     SymbolData.INT_TYPE, 
                                     new VariableData[0],
                                     new String[0], 
                                     sd,
                                     null);
      _iv.createHashCode(sd);
      assertEquals("sd should have 1 method: hashCode()", md, sd.getMethods().getFirst());
    }
    
    public void testCreateEquals() {
      SymbolData sd =
        new SymbolData("ClassName", PUBLIC_MAV, new TypeParameter[0], null, new ArrayList<SymbolData>(), null);
      VariableData[] vds =
        new VariableData[] { new VariableData(_iv.getSymbolData("java.lang.Object", SourceInfo.make("java.lang.Object"))) };
      MethodData md = new MethodData("equals",
                                     PUBLIC_MAV, 
                                     new TypeParameter[0], 
                                     SymbolData.BOOLEAN_TYPE, 
                                     vds,
                                     new String[0], 
                                     sd,
                                     null);
      _iv.createEquals(sd);
      md.getParams()[0].setEnclosingData(sd.getMethods().getFirst());                               
      assertEquals("sd should have 1 method: equals()", md, sd.getMethods().getFirst());
    }
    
    public void testForClassDef() {
      //check an example that's not abstract
      initTopLevel();
      ClassDef cd0 = 
        new ClassDef(SourceInfo.NONE, PACKAGE_MAV, new Word(SourceInfo.NONE, "Lisa"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(SourceInfo.NONE, new BodyItemI[0])); 
      
      cd0.visit(_iv);
      assertEquals("There should be no errors", 0, errors.size());
      assertTrue("Should have resolved java.lang.Object", 
                 LanguageLevelConverter.symbolTable.containsKey("java.lang.Object"));
      assertFalse("Should not be a continuation", 
                  LanguageLevelConverter.symbolTable.get("java.lang.Object").isContinuation());
      SymbolData sd = LanguageLevelConverter.symbolTable.get("Lisa");
      assertTrue("Lisa should be in _newSDs", LanguageLevelConverter._newSDs.containsKey(sd));
      MethodData md2 = 
        new MethodData("equals",
                       PUBLIC_MAV, 
                       new TypeParameter[0], 
                       SymbolData.BOOLEAN_TYPE, 
                       new VariableData[] { new VariableData(_iv.getSymbolData("java.lang.Object", 
                                                                               SourceInfo.make("java.lang.Object"))) },
                       new String[0], 
                       sd,
                       null);
      
      md2.getParams()[0].setEnclosingData(sd.getMethods().getLast());  
      
      assertEquals("sd's last method should be equals()", md2, sd.getMethods().getLast());
      assertEquals("sd's package should be correct", "", sd.getPackage());
      
      //check an example that's abstract
      _iv._package = "";
      ClassDef cd1 = new ClassDef(SourceInfo.NONE, 
                                  ABSTRACT_MAV, 
                                  new Word(SourceInfo.NONE, "Bart"),
                                  new TypeParameter[0],
                                  new ClassOrInterfaceType(SourceInfo.NONE, "System", new Type[0]), 
                                  new ReferenceType[0], 
                                  new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      cd1.visit(_iv);
      assertEquals("There should be no errors", 0, errors.size());
      assertTrue("Should have resolved java.lang.System", 
                 LanguageLevelConverter.symbolTable.containsKey("java.lang.System"));
      assertFalse("Should not be a continuation", 
                  LanguageLevelConverter.symbolTable.get("java.lang.System").isContinuation());
      sd = LanguageLevelConverter.symbolTable.get("Bart");
      
      assertEquals("There should be 3 methods", 3, sd.getMethods().size());
      
      
      //Check an example where the class extends TestCase, and has a test method that returns void.
      ConcreteMethodDef cmd = new ConcreteMethodDef(SourceInfo.NONE, 
                                                    PACKAGE_MAV, 
                                                    new TypeParameter[0], 
                                                    new VoidReturn(SourceInfo.NONE, "void"), 
                                                    new Word(SourceInfo.NONE, "testMethodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      ClassDef cd3 = new ClassDef(SourceInfo.NONE, 
                                  ABSTRACT_MAV, 
                                  new Word(SourceInfo.NONE, "TestSuper2"),
                                  new TypeParameter[0], 
                                  new ClassOrInterfaceType(SourceInfo.NONE, "TestCase", new Type[0]), 
                                  new ReferenceType[0], 
                                  new BracedBody(SourceInfo.NONE, new BodyItemI[] { cmd }));
      
      _iv._file=new File("TestSuper2.dj0");
      _iv._importedFiles.addLast("junit.framework.TestCase");
      LanguageLevelConverter.symbolTable.put("junit.framework.TestCase", new SymbolData("junit.framework.TestCase"));
      cd3.visit(_iv);
      assertEquals("There should still just be no errors", 0, errors.size());
      assertNotNull("Should have looked up TestSuper2", LanguageLevelConverter.symbolTable.get("TestSuper2"));
      
      // Check a method with void return, but name not starting with test, but it's still okay.
      //This is now checked in the type checker!
      ConcreteMethodDef cmd2 = new ConcreteMethodDef(SourceInfo.NONE, 
                                                     PACKAGE_MAV, 
                                                     new TypeParameter[0], 
                                                     new VoidReturn(SourceInfo.NONE, "void"), 
                                                     new Word(SourceInfo.NONE, "uhOh"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0], 
                                                     new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      ClassDef cd4 = new ClassDef(SourceInfo.NONE, 
                                  ABSTRACT_MAV, 
                                  new Word(SourceInfo.NONE, "TestVoidNoTestMethod"),
                                  new TypeParameter[0], 
                                  new ClassOrInterfaceType(SourceInfo.NONE, "junit.framework.TestCase", new Type[0]), 
                                  new ReferenceType[0], 
                                  new BracedBody(SourceInfo.NONE, new BodyItemI[] { cmd2 }));
      
      
      
      _iv._file=new File("TestVoidNoTestMethod.dj0");
      cd4.visit(_iv);
      
      assertEquals("There should still be 0 errors", 0, errors.size());
      _iv._importedFiles.remove("junit.framework.TestCase");
      
    }
    
    public void testForInterfaceDef() {
      initTopLevel();
      AbstractMethodDef amd = new AbstractMethodDef(SourceInfo.NONE, 
                                                    PUBLIC_MAV, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(SourceInfo.NONE, "int"),
                                                    new Word(SourceInfo.NONE, "myMethod"), 
                                                    new FormalParameter[0], 
                                                    new ReferenceType[0]);
      AbstractMethodDef amd2 = new AbstractMethodDef(SourceInfo.NONE, 
                                                     PUBLIC_MAV, 
                                                     new TypeParameter[0], 
                                                     new PrimitiveType(SourceInfo.NONE, "int"),
                                                     new Word(SourceInfo.NONE, "myMethod"), 
                                                     new FormalParameter[0], 
                                                     new ReferenceType[0]);
      InterfaceDef id = new InterfaceDef(SourceInfo.NONE, 
                                         PUBLIC_MAV, 
                                         new Word(SourceInfo.NONE, "id"), 
                                         new TypeParameter[0], 
                                         new ReferenceType[0], 
                                         new BracedBody(SourceInfo.NONE, new BodyItemI[] {amd}));
      InterfaceDef id2 = 
        new InterfaceDef(SourceInfo.NONE, 
                         PUBLIC_MAV, 
                         new Word(SourceInfo.NONE, "id2"), 
                         new TypeParameter[0], 
                         new ReferenceType[] { new ClassOrInterfaceType(SourceInfo.NONE, "id", new Type[0]) }, 
                         new BracedBody(SourceInfo.NONE, new BodyItemI[] { amd2 }));
      SymbolData sd = new SymbolData("id", PUBLIC_MAV, new TypeParameter[0], new ArrayList<SymbolData>(), null);
      sd.setIsContinuation(true);
      MethodData md = 
        new MethodData("myMethod", PUBLIC_MAV, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[0], 
                       new String[0], sd, amd);
      
      ArrayList<SymbolData> interfaces = new ArrayList<SymbolData>();
      interfaces.add(sd);
      SymbolData sd2 = new SymbolData("id2", PUBLIC_MAV, new TypeParameter[0], interfaces, null);
      sd2.setIsContinuation(true);
      MethodData md2 = 
        new MethodData("myMethod", PUBLIC_MAV, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[0], 
                       new String[0], sd2, amd2);
      LanguageLevelConverter.symbolTable.put("id", sd);
      LanguageLevelConverter.symbolTable.put("id2", sd2);
      
      id.visit(_iv);
      id2.visit(_iv);
      
      assertEquals("Should be no errors", 0, errors.size());
      assertEquals("Should return the same symbol datas: id", sd, LanguageLevelConverter.symbolTable.get("id"));
      assertEquals("Should return the same symbol datas:id2 ", sd2, LanguageLevelConverter.symbolTable.get("id2"));
    }
    
    
    
    public void testCreateMethodData() {
      // Test one that doesn't work.
      MethodDef mdef = new AbstractMethodDef(SourceInfo.NONE, 
                                             PRIVATE_ABSTRACT_MAV, 
                                             new TypeParameter[0], 
                                             new PrimitiveType(SourceInfo.NONE, "int"), 
                                             new Word(SourceInfo.NONE, "methodName"),
                                             new FormalParameter[0],
                                             new ReferenceType[0]); 
      
      MethodData mdata = new MethodData("methodName", PRIVATE_ABSTRACT_MAV, new TypeParameter[0], SymbolData.INT_TYPE, 
                                        new VariableData[0], 
                                        new String[0],
                                        _sd1,
                                        null);
      _iv._package = "i.like";
      _iv._enclosingClassName = "i.like.monkey";
      _iv.symbolTable.put("i.like.monkey", _sd1);
//      System.err.println("SymbolData for i.like.monkey = " + _iv.getQualifiedSymbolData("i.like.monkey", SourceInfo.NONE));
      assertEquals("Should return the correct MethodData", mdata, _iv.createMethodData(mdef, _sd1));
      assertEquals("There should be one errors.", 1, errors.size());
//      assertEquals("The error message should be correct.", 
//      "The keyword \"final\" cannot be used at the Intermediate level", 
//        errors.get(0).getFirst());
      
      // Test one that does work.
      UninitializedVariableDeclarator uvd1 =
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new PrimitiveType(SourceInfo.NONE, "double"), 
                                            new Word(SourceInfo.NONE, "field1"));
      UninitializedVariableDeclarator uvd2 =
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new PrimitiveType(SourceInfo.NONE, "int"), 
                                            new Word(SourceInfo.NONE, "field1"));
      mdef = 
        new AbstractMethodDef(SourceInfo.NONE, 
                              ABSTRACT_MAV, 
                              new TypeParameter[] { new TypeParameter(SourceInfo.NONE,
                                                                      new TypeVariable(SourceInfo.NONE, "T"),
                                                                      new TypeVariable(SourceInfo.NONE, "U"))},
                              new VoidReturn(SourceInfo.NONE, "void"), 
                              new Word(SourceInfo.NONE, "methodName"),
                              new FormalParameter[] {
                                new FormalParameter(SourceInfo.NONE, 
                                                    uvd1,
                                                    false
                                                   ),
                                  new FormalParameter(SourceInfo.NONE, 
                                                      uvd2,
                                                      false
                                                     )},
                              new ReferenceType[] { new TypeVariable(SourceInfo.NONE, "X") }
                              );
      mdata = 
        new MethodData("methodName", 
                       ABSTRACT_MAV, 
                       new TypeParameter[] { new TypeParameter(SourceInfo.NONE,
                                                               new TypeVariable(SourceInfo.NONE, "T"),
                                                               new TypeVariable(SourceInfo.NONE, "U"))}, 
                       SymbolData.VOID_TYPE, 
                       new VariableData[] { new VariableData("field1", FINAL_MAV, SymbolData.DOUBLE_TYPE, true, _sd1),
                         new VariableData("field1", FINAL_MAV, SymbolData.INT_TYPE, true, _sd1) }, 
                       new String[] { "X" },
                       _sd1,
                       null);
      
      
      MethodData result = _iv.createMethodData(mdef, _sd1);
      /* Enclosing data for formal parameters is the enclosing class, not the enclosing method. */
//      mdata.getParams()[0].setEnclosingData(result);
//      mdata.getParams()[1].setEnclosingData(result);
      
      // have to add the parameters to the vars manually
      mdata.addVars(new VariableData[] { new VariableData("field1", FINAL_MAV, SymbolData.DOUBLE_TYPE, true, _sd1) });
//      System.err.println("****** mdata  = " + mdata);
//      System.err.println("****** result = " + result);
      assertEquals("Should return the correct MethodData", mdata, result);
      assertEquals("There should be 2 errors.", 2, errors.size());
      // This is now caught in the type checker.
//      assertEquals("The second error message should be correct.", 
//                   "The keyword \"void\" cannot be used at the Intermediate level", 
//                   errors.get(1).getFirst());
      assertEquals("The second error message should be correct.", 
                   "You cannot have two method parameters with the same name", 
                   errors.get(1).getFirst());
    }
    
    public void testSimpleAnonymousClassInstantiationHelper() {
      SimpleAnonymousClassInstantiation basic = 
        new SimpleAnonymousClassInstantiation(SourceInfo.NONE, 
                                              new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                                              new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]),
                                              new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      _iv._package = "i.like";
      _iv._enclosingClassName = "i.like.monkey";
      _iv.symbolTable.put("i.like.monkey", _sd1);
//      System.err.println("SymbolData for i.like.monkey = " + _iv.getQualifiedSymbolData("i.like.monkey", SourceInfo.NONE));
      _iv.simpleAnonymousClassInstantiationHelper(basic, _sd1);
      assertEquals("There should be no errors", 0, errors.size());
      SymbolData obj = LanguageLevelConverter.symbolTable.get("java.lang.Object");
      assertNotNull("Object should be in the symbol table", obj);
      assertEquals("sd1 should have one inner class", 1, _sd1.getInnerClasses().size());
      SymbolData inner = _sd1.getInnerClasses().get(0);
      assertEquals("The inner class should have the proper name", "i.like.monkey$1", inner.getName());
      assertEquals("The inner class should have proper outer data", _sd1, inner.getOuterData());
      assertEquals("The inner class should have proper super class", obj, inner.getSuperClass());
      assertEquals("The inner class should have the right package", "i.like", inner.getPackage());
      assertEquals("The inner class should have 3 methods", 3, inner.getMethods().size());
    }
    
    public void testComplexAnonymousClassInstantiationHelper() {
      SimpleNameReference snr =
        new SimpleNameReference(SourceInfo.NONE, new Word(SourceInfo.NONE, "java.lang.Object"));
      ComplexAnonymousClassInstantiation basic = 
        new ComplexAnonymousClassInstantiation(SourceInfo.NONE, 
                                               snr,
                                               new ClassOrInterfaceType(SourceInfo.NONE, "Inner", new Type[0]), 
                                               new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]),
                                               new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      _iv._package = "i.like";
      _iv._enclosingClassName = "i.like.monkey";
      _iv.complexAnonymousClassInstantiationHelper(basic, _sd1); // TODO: the wrong enclosing context?
      assertEquals("There should be no errors", 0, errors.size());
      SymbolData obj = LanguageLevelConverter.symbolTable.get("java.lang.Object");
      assertNotNull("Object should be in the symbol table", obj);
      SymbolData objInner = LanguageLevelConverter.symbolTable.get("java.lang.Object.Inner");
      assertEquals("sd1 should have one inner class", 1, _sd1.getInnerClasses().size());
      SymbolData inner = _sd1.getInnerClasses().get(0);
      assertEquals("The inner class should have the proper name", "i.like.monkey$1", inner.getName());
      assertEquals("The inner class should have proper outer data", _sd1, inner.getOuterData());
      assertEquals("The inner class should have null as its super class", null, inner.getSuperClass());
      assertEquals("The inner class should have the right package", "i.like", inner.getPackage());
      assertEquals("The inner class should have 3 methods", 3, inner.getMethods().size());
    }
    
    public void testForVariableDeclaration() {
      // Confirm that if forVariableDeclaration is called with a AnonymousClassInstantiation, the symboldata is only 
      // added once. This is to make sure an old bug stays fixed.
      SimpleAnonymousClassInstantiation basic = 
        new SimpleAnonymousClassInstantiation(SourceInfo.NONE, 
                                              new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                                              new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]),
                                              new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      
      
      VariableDeclarator[] d1 = { 
        new InitializedVariableDeclarator(SourceInfo.NONE, 
                                          new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                                          new Word(SourceInfo.NONE, "b"), basic)};
      
      VariableDeclaration vd1 = new VariableDeclaration(SourceInfo.NONE,PUBLIC_MAV, d1); 
      
      ClassBodyIntermediateVisitor cbiv = 
        new ClassBodyIntermediateVisitor(_sd1,
                                         _sd1.getName(),
                                         _iv._file, 
                                         _iv._package,
                                         _iv._importedFiles, 
                                         _iv._importedPackages, 
                                         _iv._classesInThisFile, 
                                         _iv.continuations,
                                         _iv.fixUps);
      
      vd1.visit(cbiv);
      assertEquals("Should be 1 inner class of _sd1", 1, _sd1.getInnerClasses().size());
    }
    
    public void testForPackageStatementDoFirst() {
      PackageStatement ps = new PackageStatement(SourceInfo.NONE, new CompoundWord(SourceInfo.NONE, new Word[0]));
      ps.visit(_iv);
      assertEquals("Should be no errors", 0, errors.size());
//      assertEquals("Error message should be correct", "Package statements cannot be used at the Intermediate level."
//                     + "  All Intermediate level classes and interfaces are assumed to be in the default package", 
//                   errors.getLast().getFirst());
    }
  }
}