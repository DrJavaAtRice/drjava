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


/** Top-level Language Level Visitor that processes the Full Java Language Level.  It constructs a symbol table from a
  * Java source file if possible.  It does not attempt to check for errors since Full Java files are compiled by javac.
  */
public class FullJavaVisitor extends LanguageLevelVisitor {
  
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

  /** Main constructor, which is called from sites where the genericTypes table is not empty. */
  public FullJavaVisitor(File file, 
                         String packageName, 
                         String enclosingClassName,
                         LinkedList<String> importedFiles, 
                         LinkedList<String> importedPackages,
                         HashSet<String> classesInThisFile, 
                         Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations,
                         LinkedList<Command> fixUps,
                         HashMap<String, SymbolData> genericTypes) {
    super(file, packageName, enclosingClassName, importedFiles, importedPackages, classesInThisFile, continuations, 
          fixUps, genericTypes);
  }
  
  
  /** This constructor is called from sites where no generic type variables are in scope. */
  public FullJavaVisitor(File file, 
                         String packageName, 
                         String enclosingClassName,
                         LinkedList<String> importedFiles, 
                         LinkedList<String> importedPackages,
                         HashSet<String> classesInThisFile, 
                         Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations,
                         LinkedList<Command> fixUps) {
    this(file, packageName, enclosingClassName, importedFiles, importedPackages, classesInThisFile, continuations, 
         fixUps, new HashMap<String, SymbolData>());
  }
  
  /** This constructor is called only in testing.  It initializes all of the static fields of LanguageLevelVisitor. */
  public FullJavaVisitor(File file) {
    this(file, 
         new LinkedList<Pair<String, JExpressionIF>>(),
         new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
         new LinkedList<Command>(),
         new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>());
  }
  
  /** This constructor is called from LanguageLevelConverter when it is instantiating a new
    * FullJavaVisitor to visit a new file.  Package is set to "" by default.  The generic
    * types table (_genericTypes) is set to an emtpy HashMap by default.
    * @param file  The File corresponding to the source file we are visiting
    * @param errors  The list of errors that have been encountered so far.
    * @param continuations  The table of classes we have encountered but still need to resolve
    * @param visitedFiles  The list of files we have visited
    */
  public FullJavaVisitor(File file, 
                         LinkedList<Pair<String, JExpressionIF>> errors, 
                         Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations,
                         LinkedList<Command> fixUps,
                         LinkedList<Pair<LanguageLevelVisitor, SourceFile>> visitedFiles) {
    this(file, new LinkedList<String>(), errors, continuations, fixUps, visitedFiles);
  }

  /** This constructor is called from LanguageLevelConverter when it is instantiating a new
    * FullJavaVisitor to visit a new file.  Package is set to "" by default.
    * @param file  The File corresponding to the source file we are visiting
    * @param importedPackages The list of strings describing the imported packages for this file
    * @param errors  The list of errors that have been encountered so far.
    * @param continuations  The table of classes we have encountered but still need to resolve
    * @param visitedFiles  The list of files we have visited
    */
  public FullJavaVisitor(File file,
                         LinkedList<String> importedPackages,
                         LinkedList<Pair<String, JExpressionIF>> errors, 
                         Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations,
                         LinkedList<Command> fixUps,
                         LinkedList<Pair<LanguageLevelVisitor, SourceFile>> visitedFiles) {
    this(file, "", null, new LinkedList<String>(), importedPackages, new HashSet<String>(), continuations, fixUps);
    this.errors = errors;
    this.visitedFiles= visitedFiles; //new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>();
//    _hierarchy = new Hashtable<String, TypeDefBase>();//hierarchy;
  }

  /** At the FullJava Level, there is no code augmentation.  If we are working with a class that has no constructor,
    * we must record fact that has an implicit 0-ary default constructor.
    * @param sd  The SymbolData we are checking
   */
  public void createConstructor(SymbolData sd) {
    SymbolData superSd = sd.getSuperClass();
    
    //If sd is a continuation, there was an error somewhere else.  just return.
    if (sd.isContinuation()) return;
    
    String name = getUnqualifiedClassName(sd.getName());
    
    // if sd already has a constructor, just return.
    boolean hasOtherConstructor = sd.hasMethod(name);
    if (hasOtherConstructor) {
          LanguageLevelConverter._newSDs.remove(sd); // this won't do anything if sd is not in _newSDs.
//          System.err.println(sd + " removed from _newSDs.  _newSDs = " + LanguageLevelConverter._newSDs);
          return;
    }
    
    // otherwise, it doesn't have a constructor, so let's add it!
    MethodData md = MethodData.make(name,
                                    new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"}), 
                                    new TypeParameter[0], 
                                    sd, 
                                    new VariableData[0], // No Parameters
                                    new String[0], 
                                    sd,
                                    null);

    addGeneratedMethod(sd, md);
    LanguageLevelConverter._newSDs.remove(sd); // does nothing if sd is not in _newSDs.
  }
  
  /** Process the inner class def and then resolve it and store it in
    * the outer class's list of inner classes.  This method is common to both ClassBodyFullJavaVisitor
    * and InterfaceBodyIntermediateVisitor but needs the correct Data so we pass it in.  This method is tested
    * in those files.  We use the fully qualified name along with the $'s so we don't accidentally conflict with an
    * actual class in the symbol table.
    * @param that  The AST Node for the inner class def
    * @param data  The Data that encloses this inner class.  This is a Data because the inner class can be in a class 
    *              or a method.
    * @param relName The relative name of the inner class including no qualifiers
    * @param name  The qualified name of the inner class including the qualified name of the enclosing data followed by 
    *              a $.  For local classes, we append a number after the $ corresponding to the order that the local 
    *              class appears in the enclosing class and then put the name of the class.  For anonymous classes, we 
    *              only put a number after the $.  For example, if class A has a method B that has a local class C, then
    *              name should be "A$1C".
    */
  protected void handleInnerClassDef(InnerClassDef that, Data data, String relName, String name) {
//    if (_file.getName().endsWith("MultipleNested.dj2")) 
//      System.err.println("********** Processing InnerClassDef for " + name + " defined in " + data.getName());
    
    assert (data instanceof SymbolData) || (data instanceof MethodData);
//    assert (data instanceof SymbolData) ? data.getName().equals(_enclosingClassName) : true;
    
    forInnerClassDefDoFirst(that);
    if (prune(that)) return;

    that.getMav().visit(this);
    that.getName().visit(this);
    
    // Create a new generic types table for visiting this inner class.
    @SuppressWarnings("unchecked")
    HashMap<String, SymbolData> genericTypes = (HashMap<String, SymbolData>) _genericTypes.clone();
    TypeParameter[] tps = that.getTypeParameters();
    for (TypeParameter tp: tps) {
      final String typeName = tp.getVariable().getName();
      final String boundName = tp.getBound().getName();
      SymbolData boundSD = _identifyType(boundName, that.getSourceInfo(), _enclosingClassName);
      if (boundSD == null) { // create a dummy SymbolData 
        boundSD = symbolTable.get("java.lang.Object"); //  TODO: could create a separate SymbolData.NOT_BOUND singleton class?
//        System.err.println("Creating dummy SymbolData for bounding type " + typeName + " in inner class " + name);
      }
      genericTypes.put(typeName, boundSD);
    }
    
    /* The following two lines apparently do nothing.  Note that they are NOT in the scope of the new generic types
     * table.  TODO: confirm that these two lines do nothing and eliminate them. */
    that.getSuperclass().visit(this);
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    
    /* The following line is NOT in the scope of the new generic type table but should be.  Since we are not
     * performing generic type checking, we may get away with the error.  TODO: refactor this code to
     * create a new LLV for processing a generic class. */
    SymbolData sd = defineInnerSymbolData(that, relName, name, data);
    // The preceding only fails if there is an error in the program
    
    if (sd != null) { // We have a symbol data to work with, so visit the body using the new generic types table
      identifyInnerClasses(that);
      ClassBodyFullJavaVisitor cbfjv =
        new ClassBodyFullJavaVisitor(sd, _file, _package, _importedFiles, _importedPackages, _classesInThisFile, 
                                     continuations, fixUps, genericTypes);
      that.getBody().visit(cbfjv);
    }
   
    // Inner classes are not put into _classesInThisFile since they are parsed whenever their outer classes are parsed.

    forInnerClassDefOnly(that);
  }
  
  /** Process the inner interface, and resolve it and store it in the
    * outer class's list of inner classes.  This method is common to both ClassBodyIntermediateVisitor
    * and InterfaceBodyIntermediateVisitor but needs the correct SymbolData so we pass it in.  This method is tested
    * in those files.
    */
  protected void handleInnerInterfaceDef(InnerInterfaceDef that, Data data, String relName, String name) {
//    System.err.println("Processing InnerInterfaceDef for " + name);
    assert (data instanceof SymbolData) || (data instanceof MethodData);
    
    forInnerInterfaceDefDoFirst(that);
    if (prune(that)) return;
    
    that.getMav().visit(this);
    that.getName().visit(this);

    // Create a new generic types table for visiting this inner class.
    // TODO: refactor this code to combine common code patterns in this method and forInnerClassDef
    @SuppressWarnings("unchecked")
    HashMap<String, SymbolData> genericTypes = (HashMap<String, SymbolData>) _genericTypes.clone();
    TypeParameter[] tps = that.getTypeParameters();
    for (TypeParameter tp: tps) {
      final String typeName = tp.getVariable().getName();
      final String boundName = tp.getBound().getName();
      SymbolData boundSD = _identifyType(boundName, that.getSourceInfo(), _enclosingClassName);
      if (boundSD == null) { // create a dummy SymbolData 
        boundSD = symbolTable.get("java.lang.Object"); //  TODO: could create a separate SymbolData.NOT_BOUND singleton class?
//        System.err.println("Creating dummy SymbolData for bounding type " + typeName + " in inner class " + name);
        // TODO!  !!!!! Create an appropriate fixUp mechanism
      }
      genericTypes.put(typeName, boundSD);
    }
    
    /* The following line apparently does nothing.  Note that it is NOT in the scope of the new generic types
     * table.  TODO: confirm that this line does nothing and eliminate it. */
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    
    /* The following line is NOT in the scope of the new generic type table but should be.  Since we are not
     * performing generic type checking, we may get away with the error.  TODO: refactor this code to
     * create a new LLV for processing a generic class. */
    SymbolData sd = defineInnerSymbolData(that, relName, name, data);  // only returns null if error is encountered
    
    if (sd != null) { // We have a symbol data to work with, so visit the body using the new generic types table
      sd.setInterface(true);
      identifyInnerClasses(that);
      InterfaceBodyFullJavaVisitor ibfjv = 
        new InterfaceBodyFullJavaVisitor(sd, _file, _package, _importedFiles, _importedPackages, _classesInThisFile, 
                                         continuations, fixUps, genericTypes);
      that.getBody().visit(ibfjv);
    }

    forInnerInterfaceDefOnly(that);
  }
  
  /** Process ModifiersAndVisibility. */
  public Void forModifiersAndVisibilityDoFirst(ModifiersAndVisibility that) {
    String[] modifiersAndVisibility = that.getModifiers();
    StringBuffer sb = new StringBuffer();
    String temp;
    int count = 0;    
    for(int i = 0; i < modifiersAndVisibility.length; i++) {
      temp = modifiersAndVisibility[i];
      if (!(temp.equals("abstract") || temp.equals("public") || temp.equals("private") || temp.equals("protected") || 
            temp.equals("static") || temp.equals("final"))) {
        sb.append(" \"" + temp + "\"");
        count++;
      }
    }
    // Do NOT check for illegal keywords.  We are only building a symbol table.  The javac compiler checks for errors.
    return super.forModifiersAndVisibilityDoFirst(that);
  }

  /** Process SwitchStatement. */
  public Void forSwitchStatementDoFirst(SwitchStatement that) { return super.forSwitchStatementDoFirst(that); }
  
  /* Ignore instance initializer. */
  public Void forInstanceInitializerDoFirst(InstanceInitializer that) { return null; }
  
  /* Ignore static initializer. */
  public Void forStaticInitializerDoFirst(StaticInitializer that) { return null; }
  
  /* Ignore labeled statement. */
  public Void forLabeledStatementDoFirst(LabeledStatement that) { return null; }

  /* Ignore LabeledBreakStatement. */
  public Void forLabeledBreakStatementDoFirst(LabeledBreakStatement that) { return null; }
 
  /* Ignore LabeledContinue Statement. */
  public Void forLabeledContinueStatementDoFirst(LabeledContinueStatement that) { return null; }

  /* Ignore SynchronizedStatement. */
  public Void forSynchronizedStatementDoFirst(SynchronizedStatement that) { return null; }

  /** Ignore TypeParameter. */
  public Void forTypeParameterDoFirst(TypeParameter that) { return null; }

  /** Ignore ConditionalExpression. */
  public Void forConditionalExpressionDoFirst(ConditionalExpression that) { return null; }

  /** Ignore instanceofExpression. */
  public Void forInstanceofExpressionDoFirst(InstanceofExpression that) { return null; }

  /** Process PrimitiveType. */
  public Void forPrimitiveTypeDoFirst(PrimitiveType that) { return super.forPrimitiveTypeDoFirst(that); }

//  /* Process TryCatchStatement. */
//  public Void forTryCatchStatementDoFirst(TryCatchStatement that) {
//    return super.forTryCatchStatementDoFirst(that);
//  }
  
  /** Check to see if the specified className is a class defined in the current file.
    * If it is, it will be stored in _classesInThisFile.
    */
  private boolean _isClassInCurrentFile(String className) {
    Iterator<String> iter = _classesInThisFile.iterator();
    while (iter.hasNext()) {
      String s = iter.next();
      if (s.equals(className) || s.endsWith('.' + className)) return true;
    }
    return false;   
  }
  
  /** This overriding is a hack.  The parameters to a static method can be referenced within the method itself,
    *  even though they are not declared to be static fields.  Making them static has no effect on any other processing.
    */
  protected String[] getFormalParameterMav(Data d) { 
    return (d.hasModifier("static")) ? new String[] {"static"} : new String[] { };
  }
  
  /** Use the doFirst method to make sure there aren't any errors with the declaration.  Then, use defineSymbolData to 
    * create the appropriate symbol data, and then visit the class body.
    * Once the ClassDef has been handled, remove the class from _classesInThisFile so we know it's been taken care of.
    * @param that  The ClassDef being handled.
    * TODO: refactor with same method in IntermediateVisitor; almost identical.
    */
  public Void forClassDef(ClassDef that) {    
    forClassDefDoFirst(that);
    if (prune(that)) return null;
    
    that.getMav().visit(this);
    that.getName().visit(this);

    String className = getQualifiedClassName(that.getName().getText());

    // Create a new generic types table for visiting this class.
    // TODO: refactor this code to combine common code patterns in this method and forInnerClassDef
    @SuppressWarnings("unchecked")
    HashMap<String, SymbolData> genericTypes = (HashMap<String, SymbolData>) _genericTypes.clone();
    TypeParameter[] tps = that.getTypeParameters();
    for (TypeParameter tp: tps) {
      final String typeName = tp.getVariable().getName();
      final String boundName = tp.getBound().getName();
      SymbolData boundSD = _identifyType(boundName, that.getSourceInfo(), _enclosingClassName);
      if (boundSD == null) { // create a dummy SymbolData 
        boundSD = symbolTable.get("java.lang.Object"); //  TODO: could create a separate SymbolData.NOT_BOUND singleton class?
//        System.err.println("Creating dummy SymbolData for bounding type " + typeName + " in inner class " + name);
      }
      genericTypes.put(typeName, boundSD);
    }
    
    /* The following line is NOT in the scope of the new generic type table but should be.  Since we are not
     * performing generic type checking, we may get away with the error.  TODO: refactor this code to
     * create a new LLV for processing a generic class. */
    SymbolData sd = defineSymbolData(that, className);
    
    /* The following two lines apparently do nothing. They are in the wrong generic type scope. */
    that.getSuperclass().visit(this);
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    
    if (sd != null) {
//      System.err.println("********** Calling identifyInnerClasses for SymbolData " + sd.getName());
      identifyInnerClasses(that);
      ClassBodyFullJavaVisitor cbfjv = 
        new ClassBodyFullJavaVisitor(sd, _file, _package, _importedFiles, _importedPackages, _classesInThisFile, 
                                     continuations, fixUps, genericTypes);
      that.getBody().visit(cbfjv);
    }
    forClassDefOnly(that);
    
    return null;
  }

  /** This is a noop, because we do not do code augmentation at the Advanced Level. */
  protected void createToString(SymbolData sd) { return; }

  /** This is a noop, because we do not do code augmentation at the Advanced Level. */
  protected void createHashCode(SymbolData sd) { return; }
    
  /** This is a noop, because we do not do code augmentation at the Advanced Level. */
  protected void createEquals(SymbolData sd) { return; }    
   
  /** Create a SymbolData corresponding to this interface and add it appropriately.
    * Then visit the body to handle anything defined inside the interface.
    * Once the interface has been resolved, remove it from _classesInThisFile.
    * TODO: refactor with same method in IntermediateVisitor; they appear identical
    */
  public Void forInterfaceDef(InterfaceDef that) {
    forInterfaceDefDoFirst(that);
    if (prune(that)) return null;

    String className = getQualifiedClassName(that.getName().getText());
//    if (className.equals("listFW.IList")) System.err.println("Attempting to define symbol " + className);
    
    SymbolData sd = defineSymbolData(that, className);  
    // TODO: should the preceding statement follow interface processing in 345
        
    // Note: sd can only be null if an error occurs in defineSymbol
    if (sd != null) sd.setInterface(true);
    
    that.getMav().visit(this);
    that.getName().visit(this); 
    
    // Create a new generic types table for visiting this interface.
    // TODO: refactor this code to combine common code patterns in this method and forInnerClassDef
    @SuppressWarnings("unchecked")
    HashMap<String, SymbolData> genericTypes = (HashMap<String, SymbolData>) _genericTypes.clone();
    TypeParameter[] tps = that.getTypeParameters();
    for (TypeParameter tp: tps) {
      final String typeName = tp.getVariable().getName();
      final String boundName = tp.getBound().getName();
      SymbolData boundSD = _identifyType(boundName, that.getSourceInfo(), _enclosingClassName);
      if (boundSD == null) { // create a dummy SymbolData 
        boundSD = symbolTable.get("java.lang.Object"); //  TODO: could create a separate SymbolData.NOT_BOUND singleton class?
//        System.err.println("Creating dummy SymbolData for bounding type " + typeName + " in inner class " + name);
      }
      genericTypes.put(typeName, boundSD);
    }
    
    /* The following line apparently does nothing.  Note that it is NOT in the scope of the new generic types
     * table.  TODO: confirm that this line does nothing and eliminate it. */
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    
    if (sd != null) {
      sd.setInterface(true);
      identifyInnerClasses(that);
      InterfaceBodyFullJavaVisitor ibfjv =
        new InterfaceBodyFullJavaVisitor(sd, _file, _package, _importedFiles, _importedPackages, _classesInThisFile, 
                                         continuations, fixUps, genericTypes);
      that.getBody().visit(ibfjv);
    }
    
    forInterfaceDefOnly(that);
    _classesInThisFile.remove(className);
    return null;
  }
  
  /** Look up the super type of this class instantiation and add it to the symbol table.  Visit the body of the class
    * instantiation.  All handling of this as an anonymous inner class (i.e. adding it to the enclosing SD's list of
    * inner classes, creating a symbol data for the anonyomous inner class, etc) is handled in the TypeChecker pass.  
    * This is because no one will depend on that symbolData until we create it.
    * @param that       The SimpleAnonymousClassInstantiation being processed.
    * @param enclosing  The SymbolData of the enclosing class.
    */
  public void simpleAnonymousClassInstantiationHelper(SimpleAnonymousClassInstantiation that, SymbolData enclosing) {
    /* Nested inner computation (e.g., new E().new W()) breaks conformity checking.  So we suppress it. */
  }
  
  /** Do not resolve the super class type of this instantiation, because it will have already been resolved (it
    * is an inner class of the enclosing of this instantiation.  When the enclosing was resolved, all of its inner 
    * classes should have also been resolved).
    * Visit the body of the class instantiation.  
    * @param that  The ComplexAnonymousClassInstantiation being processed.
    * @param enclosing  The SymbolData of the enclosing class.
    */
  public void complexAnonymousClassInstantiationHelper(ComplexAnonymousClassInstantiation that, SymbolData enclosing) {
    /* Nested inner computation (e.g., new E().new W()) breaks conformity checking.  So we suppress it. */
  }
  
  /* Resolve the ArrayType by looking it up. */  
  public Void forArrayType(ArrayType that) {
    forArrayTypeDoFirst(that);
    if (prune(that)) return null;
//    System.err.println("###### getSymbolData on " + that.getName() + " SHOULD BE ARRAY NAME");
    getSymbolData(that.getName(), that.getSourceInfo());
    return null;
  }
  
  /** Test the methods defined in the enclosing class.  There is a test method corresponding to almost every method in 
    * the above class.
    * TODO:  WHICH IS WRONG!  Many of the methods in FullJavaVisitor are invoked at the level of ClassBodyFullJavaVisitor
    * and BodyBodyFullJavaVisitor.  Moreover, they rely on overrides that are present only at that subclass level.  Move
    * the tests that relate to subclasses to the appropriate subclasses!
    */
  public static class FullJavaVisitorTest extends TestCase {
    
    /*Some initial initializations:*/
    private FullJavaVisitor _fv;
    
    private SymbolData _sd1;
    private SymbolData _sd2;
    private SymbolData _sd3;
    private SymbolData _sd4;
    private SymbolData _sd5;
    private SymbolData _sd6;
    private SymbolData _objectSD;
    
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[0]);
    private ModifiersAndVisibility _abstractMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract"});
    private ModifiersAndVisibility _staticMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"static"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final"});
    private ModifiersAndVisibility _volatileMav = 
      new ModifiersAndVisibility(SourceInfo.NONE, new String[]{"volatile"});
    
    public FullJavaVisitorTest() { this(""); }
    public FullJavaVisitorTest(String name) { super(name); }
    
    public void setUp() {
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      LanguageLevelConverter.symbolTable.clear();
      LanguageLevelConverter._newSDs.clear();
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, IterUtil.make(new File("lib/buildlib/junit.jar")));
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
//      _hierarchy = new Hashtable<String, TypeDefBase>();
      _fv = new FullJavaVisitor(new File(""), 
                                errors,
                                continuations,
                                new LinkedList<Command>(),
                                new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>());
      _fv._classesInThisFile = new HashSet<String>();
      _fv.continuations = new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>();
      _fv._importedPackages.addFirst("java.lang");
      _errorAdded = false;
      
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");
      
      _sd1.setIsContinuation(false);
      _sd1.setInterface(false);
      _sd1.setPackage("i.like");
      _sd1.setTypeParameters(new TypeParameter[0]);
      _sd1.setInterfaces(new ArrayList<SymbolData>()); 
      
      _objectSD = LanguageLevelConverter.symbolTable.get("java.lang.Object");
      _sd1.setSuperClass(_objectSD);
      
      _fv._enclosingClassName = "i.like.monkey";
      _fv.symbolTable.put("i.like.monkey", _sd1);

      _sd1.setSuperClass(_objectSD);
      _errorAdded = false;  // static field of this.  TODO: fix this!
    }
    
    public void initTopLevel() {
      _fv._enclosingClassName = null;
    }
    
    public void testForModifiersAndVisibilityDoFirst() {
      
      //Check that the proper modifiers are allowed:
      _fv.forModifiersAndVisibilityDoFirst(_abstractMav);
      _fv.forModifiersAndVisibilityDoFirst(_publicMav);
      _fv.forModifiersAndVisibilityDoFirst(_privateMav);
      _fv.forModifiersAndVisibilityDoFirst(_protectedMav);
      _fv.forModifiersAndVisibilityDoFirst(_staticMav);
      _fv.forModifiersAndVisibilityDoFirst(_finalMav);
      
      ModifiersAndVisibility mavs = new ModifiersAndVisibility(SourceInfo.NONE, 
                                                               new String[] {"private", "static"});
       _fv.forModifiersAndVisibilityDoFirst(mavs);
      assertEquals("there should still be 0 errors", 0, errors.size());

      //check errors:
      
      _fv.forModifiersAndVisibilityDoFirst(_volatileMav);
      assertEquals("there should now be no errors", 0, errors.size());
//      assertEquals("The error message should be correct for private modifier:", 
//                   "The keyword \"volatile\" cannot be used at the Advanced level", 
//                   errors.get(0).getFirst());

      ModifiersAndVisibility mavs2 = new ModifiersAndVisibility(SourceInfo.NONE, 
                                                                new String[] {"final", "volatile"});
     
      _fv.forModifiersAndVisibilityDoFirst(mavs2);
      assertEquals("There should now be 1 error", 1, errors.size());
      assertEquals("The error message should be correct for 1 bad, 1 good modifier:", 
                   "Illegal combination of modifiers. Can't use final and volatile together.", 
                   errors.get(0).getFirst());

      ModifiersAndVisibility mavs3 = new ModifiersAndVisibility(SourceInfo.NONE, 
                                                                new String[] {"synchronized", "native"});
     
      _fv.forModifiersAndVisibilityDoFirst(mavs3);
      assertEquals("There should now be 1 errors", 1, errors.size());
//      assertEquals("The error message should be correct for 2 bad modifiers:", 
//                   "The keywords \"synchronized\" \"native\" cannot be used at the Advanced level", 
//                   errors.get(1).getFirst());
    }
    
    public void testForClassDefDoFirst() {
//      System.err.println("*** Starting testForClassDefDoFirst");
      // check an example that works
      ClassDef cd0 = new ClassDef(SourceInfo.NONE, _publicMav,
                                  new Word(SourceInfo.NONE, "Lisa"),
                                  new TypeParameter[0], 
                                  new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                                  new ReferenceType[0], 
                                  new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      _fv.forClassDefDoFirst(cd0);
      assertEquals("should be no errors", 0, errors.size());
      
      // check that an error is not thrown if a class doesn't explicitely extend another class
      ClassDef cd1 = new ClassDef(SourceInfo.NONE, _publicMav, 
                                  new Word(SourceInfo.NONE, "Test"), new TypeParameter[0], JExprParser.NO_TYPE,
                                  new ReferenceType[0], new BracedBody(SourceInfo.NONE, new BodyItemI[0]));

      _fv.forClassDefDoFirst(cd1);
      assertEquals("there should still be 0 errors", 0, errors.size());
       
      //check that an error is not thrown if a class implements any interfaces.
      ReferenceType rt2 = new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]);
      ClassDef cd2 = new ClassDef(SourceInfo.NONE, _publicMav, 
                                   new Word(SourceInfo.NONE, "Test"), new TypeParameter[0], 
                                   new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]),
                                   new ReferenceType[] { rt2 }, 
                                   new BracedBody(SourceInfo.NONE, new BodyItemI[0]));

                                 
      _fv.forClassDefDoFirst(cd2);
      assertEquals("there should still be 0 errors", 0, errors.size());
//      System.err.println("Ending testForClassDefDoFirst");
    }
    
    public void testForFormalParameterDoFirst() {
      PrimitiveType pt = new PrimitiveType(SourceInfo.NONE, "int");
      Word w = new Word(SourceInfo.NONE, "param");
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(SourceInfo.NONE, pt, w);
      
      // check an example that works
      FormalParameter fp = new FormalParameter(SourceInfo.NONE, uvd, false);
      _fv.forFormalParameterDoFirst(fp);
      assertEquals("should be no errors", 0, errors.size());
      
      // check that no errors are thrown if the FormalParameter is final
      FormalParameter fp2 = new FormalParameter(SourceInfo.NONE, uvd, true);  
      _fv.forFormalParameterDoFirst(fp2);
      assertEquals("should still be no errors", 0, errors.size());
    }
    
    public void test_NotAllowed() {
      SourceInfo noInfo = SourceInfo.NONE;
      Word w = new Word(SourceInfo.NONE, "word");
      TypeParameter[] tps = new TypeParameter[0];
      ReferenceType[] rts = new ReferenceType[0];
      BracedBody emptyBody = new BracedBody(SourceInfo.NONE, new BodyItemI[0]);
      UnbracedBody emptyUnbracedBody = new UnbracedBody(SourceInfo.NONE, new BodyItemI[0]);
      ClassOrInterfaceType superClass = new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]);
      FormalParameter[] fps = new FormalParameter[0];
      CompoundWord cw = new CompoundWord(noInfo, new Word[0]);
      Statement stmt = new EmptyStatement(noInfo);
      Expression e = new EmptyExpression(noInfo);
      Block b = new Block(noInfo, emptyBody);
      //      ClassModifier cm = ClassModifier.NONE;
      TypeVariable tv = new TypeVariable(noInfo, "name");
      
      InnerInterfaceDef ii = new InnerInterfaceDef(noInfo, _publicMav, w, tps, rts, emptyBody);
      InnerClassDef ic = new InnerClassDef(noInfo, _publicMav, w, tps, superClass, rts, emptyBody);
      
      StaticInitializer si = new StaticInitializer(noInfo, b);
      LabeledStatement ls = new LabeledStatement(noInfo, new Word(noInfo, "label"), stmt);

      LabeledBreakStatement bs = new LabeledBreakStatement(noInfo, new Word(noInfo, "myLabel"));
      LabeledContinueStatement cs = new LabeledContinueStatement(noInfo, new Word(noInfo, "yourLabel"));
      SimpleNameReference snr = new SimpleNameReference(noInfo, w);
      SynchronizedStatement syncs = new SynchronizedStatement(noInfo, snr, b);
      TypeParameter tp = new TypeParameter(noInfo, tv, superClass);
      ConditionalExpression ce = new ConditionalExpression(noInfo, snr, snr, snr);
      
      TryCatchStatement tcs = new NormalTryCatchStatement(noInfo, b, new CatchBlock[0]);
      SwitchCase defaultSc = new DefaultCase(SourceInfo.NONE, emptyUnbracedBody);
      SwitchStatement ssBadDefault = 
        new SwitchStatement(noInfo, new IntegerLiteral(SourceInfo.NONE, 5), new SwitchCase[]{defaultSc, defaultSc});
     
     si.visit(_fv);
     assertEquals("There should be 0 errors", 0, errors.size());

     ls.visit(_fv);
     assertEquals("There should be 0 errors", 0, errors.size());

     bs.visit(_fv);
     assertEquals("There should be 0 errors", 0, errors.size());

     cs.visit(_fv);
     assertEquals("There should be 0 errors", 0, errors.size());
     
     syncs.visit(_fv);
     assertEquals("There should be 0 errors", 0, errors.size());
    
     tp.visit(_fv);
     assertEquals("There should be 0 errors", 0, errors.size());

     ce.visit(_fv);
     assertEquals("There should be 0 errors", 0, errors.size());
     
     tcs.visit(_fv);
     assertEquals("There should be 0 errors", 0, errors.size());
     
     ssBadDefault.visit(_fv);
     assertEquals("There should be 0 errors", 0, errors.size());
     }
    
    public void testForPrimitiveTypeDoFirst() {
      
      SourceInfo noInfo = SourceInfo.NONE;
      
      //only primative types boolean, char, int, and double are allowed at Intermediate level. 
      PrimitiveType i = new PrimitiveType(noInfo, "int");
      PrimitiveType c = new PrimitiveType(noInfo, "char");
      PrimitiveType d = new PrimitiveType(noInfo, "double");
      PrimitiveType b = new PrimitiveType(noInfo, "boolean");
      
      i.visit(_fv);
      assertEquals("After visiting int, errors should still be 0", 0, errors.size());
      
      c.visit(_fv);
      assertEquals("After visiting char, errors should still be 0", 0, errors.size());
      
      d.visit(_fv);
      assertEquals("After visiting double, errors should still be 0", 0, errors.size());
      
      b.visit(_fv);
      assertEquals("After visiting boolean, errors should still be 0", 0, errors.size());
      
      // now the types that formerly threw errors:
      
      PrimitiveType byt = new PrimitiveType(noInfo, "byte");
      PrimitiveType s = new PrimitiveType(noInfo, "short");
      PrimitiveType l = new PrimitiveType(noInfo, "long");
      PrimitiveType f = new PrimitiveType(noInfo, "float");
      
      byt.visit(_fv);
      assertEquals("After visiting byte, errors should be 0", 0, errors.size());
      
      s.visit(_fv);
      assertEquals("After visiting short, errors should be 0", 0, errors.size());
      
      l.visit(_fv);
      assertEquals("After visiting long, errors should be 0", 0, errors.size());
      
      f.visit(_fv);
      assertEquals("After visiting float, errors should be 0", 0, errors.size());
    }
    
    public void testForArrayType() {
//      System.err.println("**** Starting testForArrayType");
      SymbolData sd = new SymbolData("Name");
      sd.setIsContinuation(false);
      sd.setInterface(false);
      sd.setPackage("");
      sd.setTypeParameters(new TypeParameter[0]);
      sd.setInterfaces(new ArrayList<SymbolData>()); 
      sd.setSuperClass(LanguageLevelConverter.symbolTable.get("java.lang.Object"));
      LanguageLevelConverter.symbolTable.put("Name", sd);
                       
      ArrayInitializer ai = new ArrayInitializer(SourceInfo.NONE, new VariableInitializerI[0]);
      TypeVariable tv = new TypeVariable(SourceInfo.NONE, "Name");
      ArrayType at = new ArrayType(SourceInfo.NONE, "Name[]", tv);
      
      at.visit(_fv);
      assertEquals("There should be no errors", 0, errors.size());
      SymbolData asd = LanguageLevelConverter.symbolTable.get("Name[]");
      assertNotNull("asd should not be null", asd);
      ArrayData ad = (ArrayData) asd;
      assertEquals("ad should have an elt sd of name 'Name'", "Name", ad.getElementType().getName());
      
      //Test a multi-dimensional array
      tv = new TypeVariable(SourceInfo.NONE, "java.lang.Object");
      at = new ArrayType(SourceInfo.NONE, "Object[]", tv);
      ArrayType at2 = new ArrayType(SourceInfo.NONE, "Object[][]", at);

      at2.visit(_fv);
      assertEquals("There should be no errors", 0, errors.size());
      assertNotNull("Object should be in the symbolTable", LanguageLevelConverter.symbolTable.get("java.lang.Object"));
      assertNotNull("Object[] should be in the symbolTable", 
                    LanguageLevelConverter.symbolTable.get("java.lang.Object[]"));
      assertNotNull("Object[][] should be in the symbolTable", 
                    LanguageLevelConverter.symbolTable.get("java.lang.Object[][]"));
    }
    
    // TODO: resurrect a test of this method
//    public void testCreateConstructor() {
//      SymbolData sd = 
//        new SymbolData("ClassName", _publicMav, new TypeParameter[0], null, new LinkedList<SymbolData>(), null);
//      VariableData v1 = new VariableData("i", _publicMav, SymbolData.INT_TYPE, false, sd);
//      VariableData v2 = new VariableData("j", _publicMav, SymbolData.CHAR_TYPE, false, sd);
//      VariableData v3 = new VariableData("var", _publicMav, SymbolData.DOUBLE_TYPE, false, sd);
//      sd.addVar(v1);
//      sd.addVar(v2);
//      sd.setSuperClass(_sd1);
//      
//      MethodData md = new MethodData("ClassName", _publicMav, new TypeParameter[0], sd, 
//                                   new VariableData[0], 
//                                   new String[0], 
//                                   sd,
//                                   null);
//      md.addVars(md.getParams());
//      _fv.createConstructor(sd);
//      
//      assertEquals("sd should have 1 method: its own constructor", md, sd.getMethods().getFirst());
//      
//      // since this is the only constructor in the symbol data, all the fields should be assigned to have a value after 
//      // visiting sd.
//      v1 = new VariableData("i", _publicMav, SymbolData.INT_TYPE, true, sd);
//      v2 = new VariableData("j", _publicMav, SymbolData.CHAR_TYPE, true, sd);
//
//      
//      //now test a subclass of sd:
//      SymbolData subSd = 
//        new SymbolData("Subclass",_publicMav, new TypeParameter[0], null, new ArrayList<SymbolData>(), null);
//      subSd.addVar(v3);
//      subSd.setSuperClass(sd);
//
//      
//      VariableData v1Param = new VariableData("super_i", _packageMav, SymbolData.INT_TYPE, true, null);
//      VariableData v2Param = new VariableData("super_j", _packageMav, SymbolData.CHAR_TYPE, true, null);
//      VariableData[] vars = {v1Param, v2Param, v3};
//      MethodData md2 = new MethodData("Subclass", _publicMav, new TypeParameter[0], subSd,
//                                      new VariableData[0], new String[0], subSd, null);
//      md2.addVars(md2.getParams());
//                
//      _fv.createConstructor(subSd);
//      v1Param.setEnclosingData(subSd.getMethods().getFirst());
//      v2Param.setEnclosingData(subSd.getMethods().getFirst());
//      assertEquals("subSd should have 1 method: its own constructor.", md2, subSd.getMethods().getFirst());
//    }
//    
//    public void xtest_getFieldAccessorName() {
//      // This may change in the future if we change getFieldAccessorName
//      assertEquals("Should correctly convert from lower case to upper case", "name", _fv.getFieldAccessorName("name"));
//    }
    
    //ToString, HashCode, and Equals should be no-ops.
    public void testCreateToString() {
      SymbolData sd = 
        new SymbolData("ClassName", _publicMav, new TypeParameter[0], null, new ArrayList<SymbolData>(), null);
      _fv.createToString(sd);
      //should have been no-op
      assertEquals("sd should have no methods", 0, sd.getMethods().size());
    }
    
    public void testCreateHashCode() {
      SymbolData sd = 
        new SymbolData("ClassName", _publicMav, new TypeParameter[0], null, new ArrayList<SymbolData>(), null);      
      _fv.createHashCode(sd);
      //should have been no-op
      assertEquals("sd should have 0 methods", 0, sd.getMethods().size());
    }
    
    public void testCreateEquals() {
      SymbolData sd = 
        new SymbolData("ClassName", _publicMav, new TypeParameter[0], null, new ArrayList<SymbolData>(), null);
      _fv.createEquals(sd);
      //should have been no-op
      assertEquals("sd should have 0 methods", 0, sd.getMethods().size());
    }
    
    public void testForClassDef() {
//      System.err.println("**** Starting testForClassDef");
      //check an example that's not abstract
      
      // Top-level init
      initTopLevel();
      
      ClassDef cd0 = 
        new ClassDef(SourceInfo.NONE, _packageMav, 
                     new Word(SourceInfo.NONE, "Lisa"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(SourceInfo.NONE, new BodyItemI[0])); 
      
      cd0.visit(_fv);
      assertEquals("There should be no errors", 0, errors.size());
      assertTrue("Should have resolved java.lang.Object", 
                 LanguageLevelConverter.symbolTable.containsKey("java.lang.Object"));
      assertFalse("Should not be a continuation", 
                  LanguageLevelConverter.symbolTable.get("java.lang.Object").isContinuation());
      SymbolData sd = LanguageLevelConverter.symbolTable.get("Lisa");
      assertTrue("Lisa should be in _newSDs", LanguageLevelConverter._newSDs.containsKey(sd));
      assertEquals("sd should have no methods", 0, sd.getMethods().size());
      assertEquals("sd's package should be correct", "", sd.getPackage());
      
      // check an example that's abstract
      assert _fv._package == "";
      ClassDef cd1 = 
        new ClassDef(SourceInfo.NONE, _abstractMav, new Word(SourceInfo.NONE, "Bart"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(SourceInfo.NONE, "System", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      cd1.visit(_fv);
      assertEquals("There should be no errors", 0, errors.size());
      assertTrue("Should have resolved java.lang.System", 
                 LanguageLevelConverter.symbolTable.containsKey("java.lang.System"));
      assertFalse("Should not be a continuation", 
                  LanguageLevelConverter.symbolTable.get("java.lang.System").isContinuation());
      sd = LanguageLevelConverter.symbolTable.get("Bart");
      
      assertEquals("There should be 0 methods", 0, sd.getMethods().size()); //(no code augmentation is done)
      
      //Check an example where the class extends TestCase, and has a test method that returns void.
      ConcreteMethodDef cmd = new ConcreteMethodDef(SourceInfo.NONE, 
                                                    _packageMav, 
                                                    new TypeParameter[0], 
                                                    new VoidReturn(SourceInfo.NONE, "void"), 
                                                    new Word(SourceInfo.NONE, "testMethodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(SourceInfo.NONE, new BodyItemI[0]));

      ClassDef cd3 = 
        new ClassDef(SourceInfo.NONE, 
                     _abstractMav, 
                     new Word(SourceInfo.NONE, "TestSuper2"),
                     new TypeParameter[0], new ClassOrInterfaceType(SourceInfo.NONE, "TestCase", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(SourceInfo.NONE, new BodyItemI[] {cmd}));


      _fv._file = new File("TestSuper2.dj2");
      _fv._importedFiles.addLast("junit.framework.TestCase");
      LanguageLevelConverter.symbolTable.put("junit.framework.TestCase", new SymbolData("junit.framework.TestCase"));
      cd3.visit(_fv);
      assertEquals("There should still just be no errors", 0, errors.size());
      assertNotNull("Should have looked up TestSuper2", 
                    LanguageLevelConverter.symbolTable.get("TestSuper2"));
      
      //Check a method with void return, but name not starting with test, so it's not okay.
      //This is now checked in the type checker!
      ConcreteMethodDef cmd2 = new ConcreteMethodDef(SourceInfo.NONE, 
                                                    _packageMav, 
                                                    new TypeParameter[0], 
                                                    new VoidReturn(SourceInfo.NONE, "void"), 
                                                    new Word(SourceInfo.NONE, "uhOh"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(SourceInfo.NONE, new BodyItemI[0]));

      ClassDef cd4 = 
        new ClassDef(SourceInfo.NONE, 
                     _abstractMav, 
                     new Word(SourceInfo.NONE, "TestVoidNoTestMethod"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(SourceInfo.NONE,"junit.framework.TestCase", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(SourceInfo.NONE, new BodyItemI[] { cmd2 }));

      _fv._file = new File("TestVoidNoTestMethod.dj2");
      cd4.visit(_fv);

      assertEquals("There should still be 0 errors", 0, errors.size());
      _fv._importedFiles.remove("junit.framework.TestCase"); 
//      System.err.println("**** Ending testForClassDef");
    }
    
    public void testForInterfaceDef() {
      AbstractMethodDef amd = 
        new AbstractMethodDef(SourceInfo.NONE, 
                              _publicMav, 
                              new TypeParameter[0], 
                              new PrimitiveType(SourceInfo.NONE, "int"),
                              new Word(SourceInfo.NONE, "myMethod"), 
                              new FormalParameter[0], 
                              new ReferenceType[0]);
      AbstractMethodDef amd2 = 
        new AbstractMethodDef(SourceInfo.NONE, 
                              _publicMav, 
                              new TypeParameter[0], 
                              new PrimitiveType(SourceInfo.NONE, "int"),
                              new Word(SourceInfo.NONE, "myMethod"), 
                              new FormalParameter[0], 
                              new ReferenceType[0]);
      InterfaceDef id = 
        new InterfaceDef(SourceInfo.NONE, 
                         _publicMav, 
                         new Word(SourceInfo.NONE, "id"), 
                         new TypeParameter[0], 
                         new ReferenceType[0], 
                         new BracedBody(SourceInfo.NONE, new BodyItemI[] { amd }));
      InterfaceDef id2 = 
        new InterfaceDef(SourceInfo.NONE, 
                         _publicMav, 
                         new Word(SourceInfo.NONE, "id2"), 
                         new TypeParameter[0], 
                         new ReferenceType[] { new ClassOrInterfaceType(SourceInfo.NONE, "id", new Type[0]) }, 
                         new BracedBody(SourceInfo.NONE, new BodyItemI[] { amd2 }));
      SymbolData sd = new SymbolData("i.like.monkey.id", _publicMav, new TypeParameter[0], new ArrayList<SymbolData>(), null);
      sd.setIsContinuation(true);
      MethodData md = 
        new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[0], 
                       new String[0], sd, amd);

      ArrayList<SymbolData> interfaces = new ArrayList<SymbolData>();
      interfaces.add(sd);
      SymbolData sd2 = new SymbolData("i.like.monkey.id2", _publicMav, new TypeParameter[0], interfaces, null);
      sd2.setIsContinuation(true);
      MethodData md2 = 
        new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[0], 
                       new String[0], sd2, amd2);
      
      LanguageLevelConverter.symbolTable.put("i.like.monkey.id", sd);
      LanguageLevelConverter.symbolTable.put("i.like.monkey.id2", sd2);

      id.visit(_fv);
      id2.visit(_fv);
      assertEquals("Should be no errors", 0, errors.size());
      assertEquals("Should return the same symbol datas: id", sd, 
                   LanguageLevelConverter.symbolTable.get("i.like.monkey.id"));
      assertEquals("Should return the same symbol datas:id2 ", sd2, 
                   LanguageLevelConverter.symbolTable.get("i.like.monkey.id2"));
    }
    
    public void testHandleInnerClassDef() { 
//      System.err.println("**** Starting testHandleInnerClassDef");
      // setUp has created a FullJavaVisitor which initializes the symbolTable
      
//      LanguageLevelConverter.symbolTable.put("java.lang.Object", _objectSD);
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
                          new BracedBody(SourceInfo.NONE, new BodyItemI[] { cd1 }));

//      SymbolData outerData = new SymbolData("i.eat.potato");
//      outerData.setIsContinuation(false);
//      outerData.setSuperClass(_objectSD);
                                         
//      SymbolData sd0 = 
//        new SymbolData(outerData.getName() + "$Lisa", _packageMav, new TypeParameter[0], _objectSD, 
//                       new ArrayList<SymbolData>(), null); 
//      SymbolData sd1 = 
//        new SymbolData(outerData.getName() + "$Lisa$Bart", _packageMav, new TypeParameter[0], _objectSD, 
//                       new ArrayList<SymbolData>(), null); 
//      
//      outerData.addInnerClass(sd0);
//      sd0.setOuterData(outerData);
//
//      sd0.addInnerClass(sd1);
//      sd1.setOuterData(sd0);

//      sd0.setIsContinuation(true);
//      sd1.setIsContinuation(true);      
            
//      LanguageLevelConverter.symbolTable.put(outerData.getName() + "$Lisa", sd0);

      _fv.handleInnerClassDef(cd0, _sd1, "Lisa", _sd1.getName() + ".Lisa");                        
                              
      SymbolData sd0 = _sd1.getInnerClassOrInterface("Lisa");
      assertEquals("There should be no errors", 0, errors.size());
      assertNotNull("Lisa is inner class of i.like.monkey", sd0);
      assertEquals("sd0 should have the correct outer data", _sd1, sd0.getOuterData());
      assertEquals("Lisa should have 0 methods", 0, sd0.getMethods().size());
      assertNotNull("symbolTable contains fully qualified name for Lisa", 
                    _fv.getQualifiedSymbolData("i.like.monkey.Lisa"));
      
      SymbolData sd1 = sd0.getInnerClassOrInterface("Bart");
      assertEquals("There should be no errors", 0, errors.size());
      assertNotNull("Bart is inner class of Lisa", sd1);
      assertEquals("sd1 should have the correct outer data", sd0, sd1.getOuterData());
      assertEquals("Bart should have 0 methods", 0, sd1.getMethods().size());
      assertNotNull("symbolTable contains fully qualified name for Bart", 
                    _fv.getQualifiedSymbolData("i.like.monkey.Lisa.Bart"));
//      System.err.println("**** Ending testHandleInnerClassDef");
    }
    
    public void xtestHandleInnerInterfaceDef() {
      InnerInterfaceDef cd1 = 
        new InnerInterfaceDef(SourceInfo.NONE, _packageMav, new Word(SourceInfo.NONE, "Bart"),
                              new TypeParameter[0], new ReferenceType[0], 
                              new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      InnerInterfaceDef cd0 = 
        new InnerInterfaceDef(SourceInfo.NONE, _packageMav, new Word(SourceInfo.NONE, "Lisa"),
                              new TypeParameter[0], new ReferenceType[0], 
                              new BracedBody(SourceInfo.NONE, new BodyItemI[] { cd1 }));

      SymbolData outerData = new SymbolData("i.drink.vanilla.coke");

      
      SymbolData sd0 = 
        new SymbolData(outerData.getName() + "$Lisa", _packageMav, new TypeParameter[0], new ArrayList<SymbolData>(), 
                       null); 
      SymbolData sd1 = 
        new SymbolData(outerData.getName() + "$Lisa$Bart", _packageMav, new TypeParameter[0], 
                       new ArrayList<SymbolData>(), null);
      sd0.addInnerInterface(sd1);
      sd0.setIsContinuation(true);
      sd1.setIsContinuation(true);

      outerData.addInnerInterface(sd0);
      sd0.setOuterData(outerData);

      sd0.addInnerInterface(sd1);
      sd1.setOuterData(sd0);

      sd0.setIsContinuation(true);
      sd1.setIsContinuation(true);

      
      _fv.handleInnerInterfaceDef(cd0, outerData, "Lisa", outerData.getName() + "$Lisa");

      SymbolData sd = outerData.getInnerClassOrInterface("Lisa");
      
      assertEquals("There should be no errors", 0, errors.size());
      assertEquals("This symbolData should now have sd0 as an inner interface", sd0, sd);
      assertEquals("sd0 should have the correct outer data", outerData, sd0.getOuterData());
      assertEquals("sd1 should have the correct outer data", sd0, sd1.getOuterData());
      assertEquals("Sd should now have sd1 as an inner interface", sd1, sd.getInnerClassOrInterface("Bart"));
      assertTrue("Lisa should be an interface", sd0.isInterface());
      assertTrue("Bart should be an interface", sd1.isInterface());
    }
    
    
    public void testCreateMethodData() {
      // Test one that doesn't work at the old advanced level
      MethodDef mdef = new ConcreteMethodDef(SourceInfo.NONE, 
                                                    _volatileMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(SourceInfo.NONE, "int"), 
                                                    new Word(SourceInfo.NONE, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      MethodData mdata = new MethodData("methodName", _volatileMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                                   new VariableData[0], 
                                   new String[0],
                                   _sd1,
                                   null);
      assertEquals("Should return the correct MethodData", mdata, _fv.createMethodData(mdef, _sd1));
      assertEquals("There should be no errors", 0, errors.size());

      mdef = new ConcreteMethodDef(SourceInfo.NONE, 
                                                    _finalMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(SourceInfo.NONE, "int"), 
                                                    new Word(SourceInfo.NONE, "methodName2"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      mdata = new MethodData("methodName2", _finalMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                                   new VariableData[0], 
                                   new String[0],
                                   _sd1,
                                   null);
      assertEquals("Should return the correct MethodData", mdata, _fv.createMethodData(mdef, _sd1));
      assertEquals("There should still be no errors", 0, errors.size());

      
      // Test one that does work.
      FormalParameter fp1 =
        new FormalParameter(SourceInfo.NONE, 
                            new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                                                new PrimitiveType(SourceInfo.NONE, "double"), 
                                                                new Word (SourceInfo.NONE, "field1")),
                            false);
      FormalParameter fp2 =
        new FormalParameter(SourceInfo.NONE, 
                            new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                                                new PrimitiveType(SourceInfo.NONE, "int"), 
                                                                new Word (SourceInfo.NONE, "field1")),
                            false);
      mdef =
        new AbstractMethodDef(SourceInfo.NONE, 
                              _abstractMav, 
                              new TypeParameter[] { new TypeParameter(SourceInfo.NONE,
                                                                      new TypeVariable(SourceInfo.NONE, "T"),
                                                                      new TypeVariable(SourceInfo.NONE, "U"))},
                              new VoidReturn(SourceInfo.NONE, "void"), 
                              new Word(SourceInfo.NONE, "methodName"),
                              new FormalParameter[] {fp1, fp2},
                              new ReferenceType[] { new TypeVariable(SourceInfo.NONE, "X") });
      VariableData[] vardatas =
        new VariableData[] { new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, true, null),
                             new VariableData("field1", _finalMav, SymbolData.INT_TYPE, true, null) };
      mdata = new MethodData("methodName", 
                             _abstractMav, 
                             new TypeParameter[] { new TypeParameter(SourceInfo.NONE,
                                                                     new TypeVariable(SourceInfo.NONE, "T"),
                                                                     new TypeVariable(SourceInfo.NONE, "U"))}, 
                             SymbolData.VOID_TYPE, 
                             vardatas, 
                             new String[] { "X" },
                             _sd1,
                             null);
      
      mdata.getParams()[0].setEnclosingData(mdata);
      mdata.getParams()[1].setEnclosingData(mdata);
      
      MethodData expectedMethod = _fv.createMethodData(mdef, _sd1);
      
      // have to add the parameters to the vars manually
      mdata.getParams()[0].setEnclosingData(expectedMethod);
      mdata.getParams()[1].setEnclosingData(expectedMethod);
      VariableData[] vd = new VariableData[] { 
        new VariableData("f,ield1", _finalMav, SymbolData.DOUBLE_TYPE, true, expectedMethod)
      };
      mdata.addVars(vd);  
//      assertEquals("Should return the correct MethodData", mdata, expectedMethod);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The first error message should be correct.", 
                   "You cannot have two method parameters with the same name", errors.get(0).getFirst());
    }
    
    /* NOTE: This is test excluded because the tested method is now a no-op. */
    public void xtestSimpleAnonymousClassInstantiationHelper() {
//      System.err.println("**** Starting testSimpleAnonymousClassInstantiationHelper");
      SimpleAnonymousClassInstantiation basic = 
        new SimpleAnonymousClassInstantiation(SourceInfo.NONE, 
                                              new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                                              new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]),
                                              new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      

     _fv._package = "i.like";
     _fv.simpleAnonymousClassInstantiationHelper(basic, _sd1);
     assertEquals("There should be no errors", 0, errors.size());
     assertNotNull("Object should be in the symbol table", _objectSD);
     assertEquals("_sd1 should have one inner class", 1, _sd1.getInnerClasses().size());
     SymbolData inner = _sd1.getInnerClasses().get(0);
     assertEquals("The inner class should have the proper name", "i.like.monkey$1", inner.getName());
     assertEquals("The inner class should have proper outer data", _sd1, inner.getOuterData());
     assertEquals("The inner class should have proper super class", _objectSD, inner.getSuperClass());
     assertEquals("The inner class should have the right package", "i.like", inner.getPackage());
     assertEquals("The inner class should have 0 methods", 0, inner.getMethods().size());
     
//     System.err.println("**** Ending testSimpleAnonymousClassInstantiationHelper");
    }

    /* NOTE: This is test excluded because the tested method is now a no-op. */
    public void xtestComplexAnonymousClassInstantiationHelper() {
      ComplexAnonymousClassInstantiation basic = 
        new ComplexAnonymousClassInstantiation(SourceInfo.NONE, 
                                               new SimpleNameReference(SourceInfo.NONE,
                                                                       new Word(SourceInfo.NONE, "java.lang.Object")),
                                               new ClassOrInterfaceType(SourceInfo.NONE, "Inner", new Type[0]), 
                                               new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]),
                                               new BracedBody(SourceInfo.NONE, new BodyItemI[0]));

     _fv._package = "i.like";
     _fv._enclosingClassName = "i.like.monkey";
     _fv.complexAnonymousClassInstantiationHelper(basic, _sd1);  // TODO: the wrong enclosing context?
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
     assertEquals("The inner class should have 0 methods", 0, inner.getMethods().size());
    }

    /* NOTE: the following test is excluded because the anonymous class helper functions are now no-ops. */
    public void xtestForVariableDeclaration() {
      // Make sure that if forVariableDeclaration is called with a AnonymousClassInstantiation, the symbolData is only 
      // added once.  This is to make sure an old bug stays fixed.
      SimpleAnonymousClassInstantiation basic = 
        new SimpleAnonymousClassInstantiation(SourceInfo.NONE, 
                                              new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                                              new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]),
                                              new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      VariableDeclarator[] d1 = {
        new InitializedVariableDeclarator(SourceInfo.NONE, 
                                          new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                                          new Word(SourceInfo.NONE, "b"), basic)
      };
      VariableDeclaration vd1 = new VariableDeclaration(SourceInfo.NONE,_publicMav, d1); 
      
      ClassBodyFullJavaVisitor cbav = 
        new ClassBodyFullJavaVisitor(_sd1, _sd1.getName(), _fv._file, _fv._package, _fv._importedFiles, _fv._importedPackages, 
                                     _fv._classesInThisFile, _fv.continuations, _fv.fixUps);
      vd1.visit(cbav);
      assertEquals("Should be 1 inner class of _sd1", 1, _sd1.getInnerClasses().size());
//      System.err.println("**** Completed testForVariableDeclaration");
    }
    
    public void testDummy() { }
  }
}