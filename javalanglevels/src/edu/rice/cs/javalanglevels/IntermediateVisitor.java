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
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.IterUtil;

import junit.framework.TestCase;


/**
 * Top-level Language Level Visitor that represents the Intermediate Language Level.  Enforces constraints during the
 * first walk of the AST (checking for langauge specific errors and building the symbol table).
 * This class enforces things that are common to all contexts reachable at the Intermediate Language Level 
 * (i.e., inside class bodies, method bodies, interface bodies, etc), but also enforces specific top level 
 * constraints (i.e. you cannot have try catch statements at the top level, etc.)
 */
public class IntermediateVisitor extends LanguageLevelVisitor {
  
  /**
   * This constructor is called when creating a new instance of IntermediateVisitor.  The default 
   * value for className is the empty string.
   */
  public IntermediateVisitor(File file, String packageName, LinkedList<String> importedFiles, 
                         LinkedList<String> importedPackages,LinkedList<String> classNamesInThisFile, Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations) {
    super(file, packageName, importedFiles, importedPackages, classNamesInThisFile, continuations);
  }
  
  /**
   * This constructor is called when testing.  It initializes all of the static fields
   * of LanguageLevelVisitor.
   */
  public IntermediateVisitor(File file) {
    this(file, new LinkedList<Pair<String, JExpressionIF>>(), new Symboltable(), new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>(), new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>(), new Hashtable<SymbolData, LanguageLevelVisitor>());
  }
    
   /**
   * This constructor is called from LanguageLevelVisitor and LanguageLevelConverter when they are instantiating a new
   * IntermediateVisitor to visit a new file with.  Package is set to "" by default.
   * @param file  The File corresponding to the source file we are visiting
   * @param errors  The list of errors that have been encountered so far.
   * @param symbolTable  The table of classes (types) that we have encountered
   * @param continuations  The table of classes we have encountered but still need to resolve
   * @param visitedFiles  The list of files we have visited
   * @param newSDs  The new symbol datas we have created (that will need to have constructors created for them after this pass is finished)
   */
  public IntermediateVisitor(File file, LinkedList<Pair<String, JExpressionIF>> errors, Symboltable symbolTable,
                             Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations,
                             LinkedList<Pair<LanguageLevelVisitor, SourceFile>> visitedFiles,
                             Hashtable<SymbolData, LanguageLevelVisitor> newSDs) {
    super(file, "", new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>(), continuations);
    this.errors = errors;
    this.symbolTable = symbolTable;
    this.visitedFiles= visitedFiles;
    this._newSDs = newSDs;
    _hierarchy = new Hashtable<String, TypeDefBase>();//hierarchy;
    _classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
  }
  
  
  /**Only abstract, public, private, protected, and static are allowed at this level.*/
  public void forModifiersAndVisibilityDoFirst(ModifiersAndVisibility that) {
    String[] modifiersAndVisibility = that.getModifiers();
    StringBuffer sb = new StringBuffer();
    String temp;
    int count = 0;    
    for(int i = 0; i < modifiersAndVisibility.length; i++) {
      temp = modifiersAndVisibility[i];
      if (!(temp.equals("abstract") || temp.equals("public") || temp.equals("private") || temp.equals("protected") || temp.equals("static"))) {
        sb.append(" \"" + temp + "\"");
        count++;
      }
    }
    // check if any illegal keywords were found
    temp = "The keyword";
    if (sb.length() > 0) {
      if (count > 1) {
        temp = temp + "s";
      }
      _addAndIgnoreError(temp + sb.toString() + " cannot be used at the Intermediate level", that);
      return;
    }
    super.forModifiersAndVisibilityDoFirst(that);
  }

  
  /** Do not allow explicit Package Statements at the Intermediate Level. */
  public void forPackageStatementDoFirst(PackageStatement that) {
    _addError("Package statements cannot be used at the Intermediate level.  All Intermediate level classes and interfaces are assumed to be in the default package", that);
  }
  
  /**Do not allow inner classes at the Intermediate Level.*/
  public void forInnerClassDefDoFirst(InnerClassDef that) {
    _addError("Inner classes cannot be used at the Intermediate level", that);
  }

  /**Do not allow inner interfaces at the Intermediate Level.*/
    public void forInnerInterfaceDefDoFirst(InnerInterfaceDef that) {
    _addError("Nested interfaces cannot be used at the Intermediate level", that);
  }

  /**Do not allow static intiializers at the Intermediate Level.*/
  public void forStaticInitializerDoFirst(StaticInitializer that) {
    _addError("Static initializers cannot be used at the Intermediate level", that);
  }

  /**Do not allow labeled statements at the Intermediate Level.*/
  public void forLabeledStatementDoFirst(LabeledStatement that) {
    _addError("Labeled statements cannot be used at the Intermediate level", that);
  }

  /**Do not allow switch statements at the Intermediate Level.*/
  public void forSwitchStatementDoFirst(SwitchStatement that) {
    _addError("Switch statements cannot be used at the Intermediate level", that);
  }

  /**Do not allow while statements at the Intermediate Level.*/
  public void forWhileStatementDoFirst(WhileStatement that) {
    _addError("While statements cannot be used at the Intermediate level", that);
  }

  /**Do not allow do statements at the Intermediate Level.*/
  public void forDoStatementDoFirst(DoStatement that) {
    _addError("Do statements cannot be used at the Intermediate level", that);
  }

  /**Do not allow for statements at the Intermediate Level.*/
  public void forForStatementDoFirst(ForStatement that) {
    _addError("For statements cannot be used at the Intermediate level", that);
  }

  /**Do not allow break statements at the Intermediate Level.*/
  public void forBreakStatementDoFirst(BreakStatement that) {
    _addError("Break statements cannot be used at the Intermediate level", that);
  }

  /**Do not allow continue statements at the Intermediate Level.*/
  public void forContinueStatementDoFirst(ContinueStatement that) {
    _addError("Continue statements cannot be used at the Intermediate level", that);
  }

  /**Do not allow synchronized statements at the Intermediate Level.*/
  public void forSynchronizedStatementDoFirst(SynchronizedStatement that) {
    _addError("Synchronized statements cannot be used at the Intermediate level", that);
  }

  /**Do not allow try-catch statements at the Intermediate Level.*/
  public void forTryCatchStatementDoFirst(TryCatchStatement that) {
    _addAndIgnoreError("A try-catch statement cannot appear here", that);
  }

  /**Make sure that the formal parameter is not final*/
  public void forFormalParameterDoFirst(FormalParameter that) {
    if (that.isIsFinal()) {
      _addError("The keyword \"final\" cannot be used at the Intermediate level", that);
    }
    else {
      forJExpressionDoFirst(that);
    }
  }

  /**Do not allow type parameters (generics) at the Intermediate level*/
  public void forTypeParameterDoFirst(TypeParameter that) {
    _addError("Type Parameters cannot be used at the Intermediate level", that);
  }

  /**Only allow the 4 basic primitives: int, double, boolean, and char*/
  public void forPrimitiveTypeDoFirst(PrimitiveType that) {
    String name = that.getName();
    if (!(name.equals("int") || name.equals("double") || name.equals("boolean") || name.equals("char"))) {
      _addError("Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Intermediate level", that);
    }
    else {
      forTypeDoFirst(that);
    }
  }

  /**Do not allow arrays at the Intermediate Level*/
  public void forArrayTypeDoFirst(ArrayType that) {
    _addError("Arrays cannot be used at the Intermediate level", that);
  }

  /**Do not allow conditional expressions at the Intermediate Level*/
  public void forConditionalExpressionDoFirst(ConditionalExpression that) {
    _addError("Conditional expressions cannot be used at the Intermediate level", that);
  }

  /**Do not allow instanceof expressions at the Intermediate Level*/
  public void forInstanceofExpressionDoFirst(InstanceofExpression that) {
    _addError("Instanceof expressions cannot be used at the Intermediate level", that);
  }
  
  
  /*Check to see if className is one of the classes declared in the current source file,
   * by looking through classNamesInThisFile.*/
  private boolean _isClassInCurrentFile(String className) {
    Iterator<String> iter = _classNamesInThisFile.iterator();
    while (iter.hasNext()) {
      String s = iter.next();
      if (s.equals(className) || s.endsWith("." + className)) {
        return true;
      }
    }
    return false;   
  }  
  
  /*
   * Use the doFirst method to make sure there aren't any errors with the declaration.  Then, 
   * use addSymbolData to create the appropriate symbol data, and then visit the class body.  
   * Finally, autogenerate the toString, equals, hashCode, and accessor
   * methods.  The constructor will be autogenerated right before the TypeChecking pass starts.
   * Once the class def has been handled, remove it from classesToBeParsed.
   */
  public void forClassDef(ClassDef that) {    
    forClassDefDoFirst(that);
    if (prune(that)) { return; }

    String className = getQualifiedClassName(that.getName().getText());
    SymbolData sd = addSymbolData(that, className);
   
    that.getMav().visit(this);
    that.getName().visit(this);
    for (int i = 0; i < that.getTypeParameters().length; i++) that.getTypeParameters()[i].visit(this);
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    
    if (sd != null) {
      that.getBody().visit(new ClassBodyIntermediateVisitor(sd, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));
      createAccessors(sd, _file);
      createToString(sd);
      createHashCode(sd);
      createEquals(sd);
    }
    forClassDefOnly(that);

    _classesToBeParsed.remove(className);
  }

   /**
    * Use the doFirst method to make sure that there aren't any errors with the declaration.
    * Create a SymbolData corresponding to this interface and add it appropriately.
    * Then visit the body to handle anything defined inside the interface.
    * Once the interface has been resolved, remove it from _classesToBeParsed.
    */
  public void forInterfaceDef(InterfaceDef that) {
    forInterfaceDefDoFirst(that);
    if (prune(that)) { return; }
    String className = that.getName().getText();
    that.getMav().visit(this);
    that.getName().visit(this);
    for (int i = 0; i < that.getTypeParameters().length; i++) that.getTypeParameters()[i].visit(this);
    SymbolData sd = addSymbolData(that, getQualifiedClassName(className));
    if (sd != null) {
      sd.setInterface(true);
      that.getBody().visit(new InterfaceBodyIntermediateVisitor(sd, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));
    }
      
    forInterfaceDefOnly(that);
    _classesToBeParsed.remove(getQualifiedClassName(className));
  }  
  
  
    /**
   * Call the super method to convert these to a VariableData array, then make sure that
   * each VariableData is final, as required at the Intermediate level.
   * @param enclosingData  The Data which contains the variables
   */
  protected VariableData[] _variableDeclaration2VariableData(VariableDeclaration vd, Data enclosingData) {
    VariableData[] vds = super._variableDeclaration2VariableData(vd, enclosingData);
    for (int i = 0; i < vds.length; i++) {
      if ((vds[i].hasModifier("static") && vds[i].getMav().getModifiers().length > 1) || (!vds[i].hasModifier("static") && vds[i].getMav().getModifiers().length > 0)) {
        StringBuffer s = new StringBuffer("the keyword(s) ");
        String[] modifiers = vds[i].getMav().getModifiers();
        for (int j = 0; j<modifiers.length; j++) {
          if (!modifiers[j].equals("static")) {s.append("\"" + modifiers[j] + "\" ");}
        }
        _addAndIgnoreError("You cannot use " + s.toString() + "to declare a field at the Intermediate level.  Only the keyword 'static' is allowed", vd);
      }
      if (vds[i].hasModifier("static")) {vds[i].addModifier("public");}
      else {vds[i].addModifier("private");}
      vds[i].setFinal();
    }
    return vds;
  }

  /**
   * Pass this call directly onto the language level visitor.  This is a hack to 
   * bypass the privateAndFinal setting when we are dealing with local variables.
   */
  protected VariableData[] llVariableDeclaration2VariableData(VariableDeclaration vd, Data enclosingData) {
    return super._variableDeclaration2VariableData(vd, enclosingData);
  }

 
  /**
   * Do the work that is shared between SimpleAnonymousClassInstantiations and ComplexAnonymousClassInstantiations.
   * Do not generate automatic accessors for the anonymous class--this will be done in type checker pass.
   * @param that  The AnonymousClassInstantiation being visited
   * @param enclosing  The enclosing Data
   * @param superC  The super class being instantiated--i.e. new A() { ...}, would have a super class of A.
   */
  public void anonymousClassInstantiationHelper(AnonymousClassInstantiation that, Data enclosing, SymbolData superC) {
    that.getArguments().visit(this); 
    
    //Get the SymbolData that will correspond to this anonymous class
    SymbolData sd = new SymbolData(getQualifiedClassName(enclosing.getSymbolData().getName()) + "$" + enclosing.getSymbolData().preincrementAnonymousInnerClassNum());
    enclosing.addInnerClass(sd);
    sd.setOuterData(enclosing);
    sd.setSuperClass(superC); //the super class is what was passed in
    sd.setPackage(_package);
    
    createToString(sd);
    createHashCode(sd);
    createEquals(sd);
    //accessors will be filled in in typeChecker pass
    
    //visit the body to get it all nice and resolved.
    that.getBody().visit(new ClassBodyIntermediateVisitor(sd, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));

  }
  
  /**
   * Look up the super type of this class instantiation and add it to the symbol table.
   * Visit the body of the class instantiation.  All handling of this as an anonymous inner class (i.e. adding it to
   * the enclosing SD's list of inner classes, creating a symbol data for the anonyomous inner class, etc) will be handled
   * in the TypeChecker pass.  This is because no one will depend on that symbolData until we create it.
   * @param that  The SimpleAnonymousClassInstantiation being processed.
   */
  public void simpleAnonymousClassInstantiationHelper(SimpleAnonymousClassInstantiation that, Data data) {
    forSimpleAnonymousClassInstantiationDoFirst(that);
    if (prune(that)) { return; }

    //resolve the super class and make sure it will be in the SymbolTable.
    SymbolData superC = getSymbolData(that.getType().getName(), that.getSourceInfo());
    
    anonymousClassInstantiationHelper(that, data, superC);

    forSimpleAnonymousClassInstantiationOnly(that);
  }
  
  /**
   * Do not resolve the super class type of this instantiation, becuase it will have already been resolved (it
   * is an inner class of the enclosing of this instantiation.  When the enclosing was resolved, all of its inner classes should have
   * also been resolved).
   * Visit the body of the class instantiation.  
   * @param that  The ComplexAnonymousClassInstantiation being processed.
   * @param data  The enclosing data where this was sreferenced from.
   */
  public void complexAnonymousClassInstantiationHelper(ComplexAnonymousClassInstantiation that, Data data) {
    forComplexAnonymousClassInstantiationDoFirst(that);
    if (prune(that)) {return;}
    
    //visit the enclosing 
    that.getEnclosing().visit(this);
    
    //no need to resolve the super class of the type being instantiated, because it is a complex type, so its enclosing data should
    //get added to the symbolTable along with it wherever we resolved its enclosing data.
    
    //originally, make super class null.  This will be updated in the TypeChecker pass.
    anonymousClassInstantiationHelper(that, data, null);

    
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
    private ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[0]);
    private ModifiersAndVisibility _abstractMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"abstract"});
    private ModifiersAndVisibility _staticMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"static"});
    private ModifiersAndVisibility _finalMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"final"});
    
    
    public IntermediateVisitorTest() {
      this("");
    }
    public IntermediateVisitorTest(String name) {
      super(name);
//      _file = File.createTempFile("DrJava-test", ".java");
//      _iv = new ElementaryVisitor(new File(""), "", new LinkedList<String>(), new LinkedList<String>(), 
//                                 new LinkedList<String>());
          }
    
    public void setUp() {

      errors = new LinkedList<Pair<String, JExpressionIF>>();
      symbolTable = new Symboltable();
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      _hierarchy = new Hashtable<String, TypeDefBase>();
      _classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
      _iv = new IntermediateVisitor(new File(""), errors, symbolTable, continuations, new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>(), new Hashtable<SymbolData, LanguageLevelVisitor>());
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, IterUtil.make(new File("lib/buildlib/junit.jar")));
      _iv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      _iv._resetNonStaticFields();
      _iv._importedPackages.addFirst("java.lang");
      _errorAdded = false;
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");
    }
    
    public void testForModifiersAndVisibilityDoFirst() {
      
      //Check that the proper modifiers are allowed:
      _iv.forModifiersAndVisibilityDoFirst(_abstractMav);
      _iv.forModifiersAndVisibilityDoFirst(_publicMav);
      _iv.forModifiersAndVisibilityDoFirst(_privateMav);
      _iv.forModifiersAndVisibilityDoFirst(_protectedMav);
      _iv.forModifiersAndVisibilityDoFirst(_staticMav);
      
      ModifiersAndVisibility mavs = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, 
                                                                new String[] {"private", "static"});
       _iv.forModifiersAndVisibilityDoFirst(mavs);
      assertEquals("there should still be 0 errors", 0, errors.size());

      //check errors:
      
      _iv.forModifiersAndVisibilityDoFirst(_finalMav);
      assertEquals("there should now be 1 errors", 1, errors.size());
      assertEquals("The error message should be correct for private modifier:", "The keyword \"final\" cannot be used at the Intermediate level", errors.get(0).getFirst());

      ModifiersAndVisibility mavs2 = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, 
                                                                new String[] {"private", "final"});
     
      _iv.forModifiersAndVisibilityDoFirst(mavs2);
      assertEquals("There should now be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct for 1 bad, 1 good modifier:", "The keyword \"final\" cannot be used at the Intermediate level", errors.get(1).getFirst());

      ModifiersAndVisibility mavs3 = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, 
                                                                new String[] {"final", "native"});
     
      _iv.forModifiersAndVisibilityDoFirst(mavs3);
      assertEquals("There should now be 3 errors", 3, errors.size());
      assertEquals("The error message should be correct for 2 bad modifiers:", "The keywords \"final\" \"native\" cannot be used at the Intermediate level", errors.get(2).getFirst());

      
    }
    
    public void testForClassDefDoFirst() {
      //check an example that works
      ClassDef cd0 = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, new Word(JExprParser.NO_SOURCE_INFO, "Lisa"),
                                  new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      _iv.forClassDefDoFirst(cd0);
      assertEquals("should be no errors", 0, errors.size());
      
      //check that an error is not thrown if a class doesn't explicitely extend another class
      ClassDef cd1 = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                                  new Word(JExprParser.NO_SOURCE_INFO, "Test"), new TypeParameter[0], JExprParser.NO_TYPE,
                                  new ReferenceType[0], new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

      _iv.forClassDefDoFirst(cd1);
      assertEquals("there should still be 0 errors", 0, errors.size());
       
       //check that an error is not thrown if a class implements any interfaces.
       ClassDef cd2 = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                                   new Word(JExprParser.NO_SOURCE_INFO, "Test"), new TypeParameter[0],
                                   new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), 
                                   new ReferenceType[] {new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0])}, 
                                   new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

                                 
      _iv.forClassDefDoFirst(cd2);
      assertEquals("there should still be 0 errors", 0, errors.size());
    }
    
    public void testForFormalParameterDoFirst() {
      PrimitiveType pt = new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int");
      Word w = new Word(JExprParser.NO_SOURCE_INFO, "param");
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, pt, w);
      
      // check an example that works
      FormalParameter fp = new FormalParameter(JExprParser.NO_SOURCE_INFO, uvd, false);
      _iv.forFormalParameterDoFirst(fp);
      assertEquals("should be no errors", 0, errors.size());
      
      // check that an error is thrown if the FormalParameter is final
      FormalParameter fp2 = new FormalParameter(JExprParser.NO_SOURCE_INFO, uvd, true);  
      _iv.forFormalParameterDoFirst(fp2);
      assertEquals("should be one error", 1, errors.size());
      assertEquals("The error message should be correct for a final parameter.", 
                   "The keyword \"final\" cannot be used at the Intermediate level", 
                   errors.getLast().getFirst());
    }
    
    public void test_NotAllowed() {
      SourceInfo noInfo = JExprParser.NO_SOURCE_INFO;
      Word w = new Word(JExprParser.NO_SOURCE_INFO, "word");
      TypeParameter[] tps = new TypeParameter[0];
      ReferenceType[] rts = new ReferenceType[0];
      BracedBody emptyBody = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]);
      ClassOrInterfaceType superClass = new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]);
      FormalParameter[] fps = new FormalParameter[0];
      CompoundWord cw = new CompoundWord(noInfo, new Word[0]);
      Statement stmt = new EmptyStatement(noInfo);
      Expression e = new NullLiteral(noInfo);
      Block b = new Block(noInfo, emptyBody);
      //      ClassModifier cm = ClassModifier.NONE;
      TypeVariable tv = new TypeVariable(noInfo, "name");
      
      InnerInterfaceDef ii = new InnerInterfaceDef(noInfo, _publicMav, w, tps, rts, emptyBody);
      InnerClassDef ic = new InnerClassDef(noInfo, _publicMav, w, tps, superClass, rts, emptyBody);
      
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
      
      BracedBody hasBitOperator = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new ExpressionStatement(JExprParser.NO_SOURCE_INFO, 
                                                                                                                      new BitwiseOrAssignmentExpression(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "i")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5)))});

      TryCatchStatement tcs = new NormalTryCatchStatement(noInfo, b, new CatchBlock[0]);

     
     si.visit(_iv);
     assertEquals("StaticInitializer is not allowed", "Static initializers cannot be used at the Intermediate level", errors.getLast().getFirst());

     ii.visit(_iv);
     assertEquals("InnerInterfaceDef is not allowed", "Nested interfaces cannot be used at the Intermediate level", errors.getLast().getFirst());
     
     ic.visit(_iv);
     assertEquals("InnerClassDef is not allowed", "Inner classes cannot be used at the Intermediate level", errors.getLast().getFirst());
     
     ls.visit(_iv);
     assertEquals("Labeled Statement is not allowed", "Labeled statements cannot be used at the Intermediate level", errors.getLast().getFirst());

     ss.visit(_iv);
     assertEquals("SwitchStatement is not allowed", "Switch statements cannot be used at the Intermediate level", errors.getLast().getFirst());

     ws.visit(_iv);
     assertEquals("WhileStatement is not allowed", "While statements cannot be used at the Intermediate level", errors.getLast().getFirst());

     ds.visit(_iv);
     assertEquals("DoStatement is not allowed", "Do statements cannot be used at the Intermediate level", errors.getLast().getFirst());

     fs.visit(_iv);
     assertEquals("ForStatement is not allowed", "For statements cannot be used at the Intermediate level", errors.getLast().getFirst());
     
     bs.visit(_iv);
     assertEquals("BreakStatement is not allowed", "Break statements cannot be used at the Intermediate level", errors.getLast().getFirst());

     cs.visit(_iv);
     assertEquals("ContinueStatement is not allowed", "Continue statements cannot be used at the Intermediate level", errors.getLast().getFirst());
   
     syncs.visit(_iv);
     assertEquals("SynchronizedStatement is not allowed", "Synchronized statements cannot be used at the Intermediate level", errors.getLast().getFirst());
    
     tp.visit(_iv);
     assertEquals("TypeParameters is not allowed", "Type Parameters cannot be used at the Intermediate level", errors.getLast().getFirst());

     at.visit(_iv);
     assertEquals("ArrayTypes is not allowed", "Arrays cannot be used at the Intermediate level", errors.getLast().getFirst());

     ce.visit(_iv);
     assertEquals("ConditionalExpression is not allowed", "Conditional expressions cannot be used at the Intermediate level", errors.getLast().getFirst());
     
     hasBitOperator.visit(_iv);
      assertEquals("Bitwise operators are not allowed", "Bitwise operators cannot be used at any language level", errors.getLast().getFirst());
      
     tcs.visit(_iv);
     assertEquals("try-catch statements are not allowed", "A try-catch statement cannot appear here", errors.getLast().getFirst());
    }
    
    public void testForPrimitiveTypeDoFirst() {
      
      SourceInfo noInfo = JExprParser.NO_SOURCE_INFO;
      
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
      
      //now the types that should throw an error:
      
      PrimitiveType byt = new PrimitiveType(noInfo, "byte");
      PrimitiveType s = new PrimitiveType(noInfo, "short");
      PrimitiveType l = new PrimitiveType(noInfo, "long");
      PrimitiveType f = new PrimitiveType(noInfo, "float");
      
      byt.visit(_iv);
      assertEquals("After visiting byte, errors should be 1", 1, errors.size());
      assertEquals("After byte, error message is correct", "Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Intermediate level", errors.getLast().getFirst());

      s.visit(_iv);
      assertEquals("After visiting short, errors should be 2", 2, errors.size());
      assertEquals("After short, error message is correct", "Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Intermediate level", errors.getLast().getFirst());

      l.visit(_iv);
      assertEquals("After visiting long, errors should be 3", 3, errors.size());
      assertEquals("After long, error message is correct", "Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Intermediate level", errors.getLast().getFirst());

      f.visit(_iv);
      assertEquals("After visiting float, errors should be 4", 4, errors.size());
      assertEquals("After float, error message is correct", "Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Intermediate level", errors.getLast().getFirst());
    }
    
    public void test_isClassInCurrentFile() {
     assertFalse("class not in file should return false", _iv._isClassInCurrentFile("NotInFile"));
     _iv._classNamesInThisFile.addLast("package.MyClass");
     assertTrue("full class name in file should return true", _iv._isClassInCurrentFile("package.MyClass"));
     assertTrue("unqualified class name in file should return true", _iv._isClassInCurrentFile("MyClass"));
     
     assertFalse("partial name in file, not same class, should return false", _iv._isClassInCurrentFile("Class"));
                
    }
    
    public void testCreateConstructor() {
      SymbolData sd = new SymbolData("ClassName", _publicMav, new TypeParameter[0], null, new LinkedList<SymbolData>(), null);
      VariableData v1 = new VariableData("i", _publicMav, SymbolData.INT_TYPE, false, sd);
      VariableData v2 = new VariableData("j", _publicMav, SymbolData.CHAR_TYPE, false, sd);
      VariableData v3 = new VariableData("var", _publicMav, SymbolData.DOUBLE_TYPE, false, sd);
      sd.addVar(v1);
      sd.addVar(v2);
      sd.setSuperClass(_sd1);
      
      MethodData md = new MethodData("ClassName", _publicMav, new TypeParameter[0], sd, 
                                   sd.getVars().toArray(new VariableData[0]), 
                                   new String[0], 
                                   sd,
                                   null);
      md.addVars(md.getParams());
      _iv.createConstructor(sd);
      
      assertEquals("sd should have 1 method: its own constructor", md, sd.getMethods().getFirst());
      
      //since this is the only constructor in the symbol data, all the fields should be assigned to have a value after visiting sd.
      v1 = new VariableData("i", _publicMav, SymbolData.INT_TYPE, true, sd);
      v2 = new VariableData("j", _publicMav, SymbolData.CHAR_TYPE, true, sd);

      // We no longer do this in createConstructor
//      assertTrue("v1 should be correct--set to true", sd.getVars().contains(v1));
//      assertTrue("v2 should be correct--set to true", sd.getVars().contains(v2));
                 

      
      //now test a subclass of sd:
      SymbolData subSd = new SymbolData("Subclass",_publicMav, new TypeParameter[0], null, new LinkedList<SymbolData>(), null);
      subSd.addVar(v3);
      subSd.setSuperClass(sd);

      VariableData v1Param = new VariableData("super_i", _packageMav, SymbolData.INT_TYPE, true, null);
      VariableData v2Param = new VariableData("super_j", _packageMav, SymbolData.CHAR_TYPE, true, null);
      VariableData[] vars = {v1Param, v2Param, v3};
      MethodData md2 = new MethodData("Subclass", _publicMav, new TypeParameter[0], subSd,
                                      vars, new String[0], subSd, null);
      md2.addVars(md2.getParams());
                
      _iv.createConstructor(subSd);
      v1Param.setEnclosingData(subSd.getMethods().getFirst());
      v2Param.setEnclosingData(subSd.getMethods().getFirst());
      assertEquals("subSd should have 1 method: its own constructor.", md2, subSd.getMethods().getFirst());
    }
    
    public void test_getFieldAccessorName() {
      // This may change in the future if we change getFieldAccessorName
      assertEquals("Should correctly convert from lower case to upper case", "name", _iv.getFieldAccessorName("name"));
    }
    
    public void testCreateToString() {
      SymbolData sd = new SymbolData("ClassName", _publicMav, new TypeParameter[0], null, new LinkedList<SymbolData>(), null);
      
      MethodData md = new MethodData("toString", _publicMav,
                                     new TypeParameter[0],
                                     _iv.getSymbolData("String", _iv._makeSourceInfo("java.lang.String")), 
                                     new VariableData[0],
                                     new String[0], 
                                     sd,
                                     null); // no SourceInfo
      
      _iv.createToString(sd);
      assertEquals("sd should have 1 method: toString", md, sd.getMethods().getFirst());
    }
    
    public void testCreateHashCode() {
      SymbolData sd = new SymbolData("ClassName", _publicMav, new TypeParameter[0], null, new LinkedList<SymbolData>(), null);      
      MethodData md = new MethodData("hashCode",
                                   _publicMav, 
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
      SymbolData sd = new SymbolData("ClassName", _publicMav, new TypeParameter[0], null, new LinkedList<SymbolData>(), null);
      MethodData md = new MethodData("equals",
                                     _publicMav, 
                                     new TypeParameter[0], 
                                     SymbolData.BOOLEAN_TYPE, 
                                     new VariableData[] { new VariableData(_iv.getSymbolData("Object", _iv._makeSourceInfo("java.lang.Object"))) },
                                     new String[0], 
                                     sd,
                                     null);
                                     

      _iv.createEquals(sd);
      md.getParams()[0].setEnclosingData(sd.getMethods().getFirst());                               
      assertEquals("sd should have 1 method: equals()", md, sd.getMethods().getFirst());
    }
    
    public void testForClassDef() {
      //check an example that's not abstract
      _iv._package = "myPackage";
      ClassDef cd0 = new ClassDef(JExprParser.NO_SOURCE_INFO, _packageMav, new Word(JExprParser.NO_SOURCE_INFO, "Lisa"),
                                 new TypeParameter[0], 
                                  new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Object", new Type[0]), new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0])); 
      
      
      cd0.visit(_iv);
      assertEquals("There should be no errors", 0, errors.size());
      assertTrue("Should have resolved java.lang.Object", symbolTable.containsKey("java.lang.Object"));
      assertFalse("Should not be a continuation", symbolTable.get("java.lang.Object").isContinuation());
      SymbolData sd = symbolTable.get("myPackage.Lisa");
      assertTrue("Lisa should be in _newSDs", _newSDs.containsKey(sd));
      MethodData md2 = new MethodData("equals",
                                   _publicMav, 
                                   new TypeParameter[0], 
                                   SymbolData.BOOLEAN_TYPE, 
                                     new VariableData[] { new VariableData(_iv.getSymbolData("Object", _iv._makeSourceInfo("java.lang.Object"))) },
                                   new String[0], 
                                   sd,
                                   null);
                                   
      md2.getParams()[0].setEnclosingData(sd.getMethods().getLast());  
                             
      assertEquals("sd's last method should be equals()", md2, sd.getMethods().getLast());
      assertEquals("sd's package should be correct", "myPackage", sd.getPackage());
      
      //check an example that's abstract
      _iv._package = "";
      ClassDef cd1 = new ClassDef(JExprParser.NO_SOURCE_INFO, _abstractMav, new Word(JExprParser.NO_SOURCE_INFO, "Bart"),
                                  new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "System", new Type[0]), new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cd1.visit(_iv);
      assertEquals("There should be no errors", 0, errors.size());
      assertTrue("Should have resolved java.lang.System", symbolTable.containsKey("java.lang.System"));
      assertFalse("Should not be a continuation", symbolTable.get("java.lang.System").isContinuation());
      sd = symbolTable.get("Bart");
      
      assertEquals("There should be 3 methods", 3, sd.getMethods().size());
      
      
      //Check an example where the class extends TestCase, and has a test method that returns void.
      ConcreteMethodDef cmd = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _packageMav, 
                                                    new TypeParameter[0], 
                                                    new VoidReturn(JExprParser.NO_SOURCE_INFO, "void"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "testMethodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

      ClassDef cd3 = new ClassDef(JExprParser.NO_SOURCE_INFO, _abstractMav, new Word(JExprParser.NO_SOURCE_INFO, "TestSuper2"),
                                  new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "TestCase", new Type[0]), new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {cmd}));


      _iv._file=new File("TestSuper2.dj0");
      _iv._importedFiles.addLast("junit.framework.TestCase");
      symbolTable.put("junit.framework.TestCase", new SymbolData("junit.framework.TestCase"));
      cd3.visit(_iv);
      assertEquals("There should still just be no errors", 0, errors.size());
      assertNotNull("Should have looked up TestSuper2", symbolTable.get("TestSuper2"));
      
      //Check a method with void return, but name not starting with test, so it's not okay.
      //This is now checked in the type checker!
      ConcreteMethodDef cmd2 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _packageMav, 
                                                    new TypeParameter[0], 
                                                    new VoidReturn(JExprParser.NO_SOURCE_INFO, "void"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "uhOh"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

      ClassDef cd4 = new ClassDef(JExprParser.NO_SOURCE_INFO, _abstractMav, new Word(JExprParser.NO_SOURCE_INFO, "TestVoidNoTestMethod"),
                                  new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "junit.framework.TestCase", new Type[0]), new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {cmd2}));



      _iv._file=new File("TestVoidNoTestMethod.dj0");
      cd4.visit(_iv);

      assertEquals("There should still be 0 errors", 0, errors.size());
      _iv._importedFiles.remove("junit.framework.TestCase");
      
    }
    
    
    public void testForInterfaceDef() {
      AbstractMethodDef amd = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"),
                                                                                               new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[0], new ReferenceType[0]);
      AbstractMethodDef amd2 = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, _publicMav, new TypeParameter[0], new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"),
                                                                                               new Word(JExprParser.NO_SOURCE_INFO, "myMethod"), new FormalParameter[0], new ReferenceType[0]);
      InterfaceDef id = new InterfaceDef(JExprParser.NO_SOURCE_INFO, _publicMav, new Word(JExprParser.NO_SOURCE_INFO, "id"), new TypeParameter[0], new ReferenceType[0], 
                                         new BracedBody(JExprParser.NO_SOURCE_INFO, 
                                                        new BodyItemI[] {amd}));
      InterfaceDef id2 = new InterfaceDef(JExprParser.NO_SOURCE_INFO, _publicMav, new Word(JExprParser.NO_SOURCE_INFO, "id2"), new TypeParameter[0], new ReferenceType[] {new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "id", new Type[0])}, 
                                         new BracedBody(JExprParser.NO_SOURCE_INFO, 
                                                        new BodyItemI[] {amd2}));
      SymbolData sd = new SymbolData("id", _publicMav, new TypeParameter[0], new LinkedList<SymbolData>(), null);
      sd.setIsContinuation(true);
      MethodData md = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[0], new String[0], sd, amd);

      LinkedList<SymbolData> interfaces = new LinkedList<SymbolData>();
      interfaces.addLast(sd);
      SymbolData sd2 = new SymbolData("id2", _publicMav, new TypeParameter[0], interfaces, null);
      sd2.setIsContinuation(true);
      MethodData md2 = new MethodData("myMethod", _publicMav, new TypeParameter[0], SymbolData.INT_TYPE, new VariableData[0], new String[0], sd2, amd2);
      
      _iv.symbolTable.put("id", sd);
      _iv.symbolTable.put("id2", sd2);

      id.visit(_iv);
      id2.visit(_iv);

      assertEquals("Should be no errors", 0, errors.size());
      assertEquals("Should return the same symbol datas: id", sd, symbolTable.get("id"));
      assertEquals("Should return the same symbol datas:id2 ", sd2, symbolTable.get("id2"));
    }
   

    
    public void testCreateMethodData() {
      // Test one that doesn't work.
      MethodDef mdef = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _finalMav, 
                                                    new TypeParameter[0], 
                                                    new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      MethodData mdata = new MethodData("methodName", _finalMav, new TypeParameter[0], SymbolData.INT_TYPE, 
                                   new VariableData[0], 
                                   new String[0],
                                   _sd1,
                                   null);
      assertEquals("Should return the correct MethodData", mdata, _iv.createMethodData(mdef, _sd1));
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "The keyword \"final\" cannot be used at the Intermediate level", errors.get(0).getFirst());
      
      // Test one that does work.
      mdef = new AbstractMethodDef(JExprParser.NO_SOURCE_INFO, 
                                   _abstractMav, 
                                   new TypeParameter[] { new TypeParameter(JExprParser.NO_SOURCE_INFO,
                                                                           new TypeVariable(JExprParser.NO_SOURCE_INFO, "T"),
                                                                           new TypeVariable(JExprParser.NO_SOURCE_INFO, "U"))},
                                   new VoidReturn(JExprParser.NO_SOURCE_INFO, "void"), 
                                   new Word(JExprParser.NO_SOURCE_INFO, "methodName"),
                                   new FormalParameter[] {
                                     new FormalParameter(JExprParser.NO_SOURCE_INFO, 
                                                         new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                                                             new PrimitiveType(JExprParser.NO_SOURCE_INFO, "double"), 
                                                                                             new Word (JExprParser.NO_SOURCE_INFO, "field1")),
                                                         false
                                                           ),
                                     new FormalParameter(JExprParser.NO_SOURCE_INFO, 
                                                         new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                                                                             new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                                                                             new Word (JExprParser.NO_SOURCE_INFO, "field1")),
                                                         false
                                                           )},
                                   new ReferenceType[] { new TypeVariable(JExprParser.NO_SOURCE_INFO, "X") }
                                   );
       mdata = new MethodData("methodName", 
                              _abstractMav, 
                              new TypeParameter[] { new TypeParameter(JExprParser.NO_SOURCE_INFO,
                                                                      new TypeVariable(JExprParser.NO_SOURCE_INFO, "T"),
                                                                      new TypeVariable(JExprParser.NO_SOURCE_INFO, "U"))}, 
                              SymbolData.VOID_TYPE, 
                              new VariableData[] { new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, true, null),
                                new VariableData("field1", _finalMav, SymbolData.INT_TYPE, true, null) }, 
                              new String[] { "X" },
                              _sd1,
                              null);
       
                              
       MethodData result = _iv.createMethodData(mdef, _sd1);
       mdata.getParams()[0].setEnclosingData(result);
       mdata.getParams()[1].setEnclosingData(result);
       // have to add the parameters to the vars manually
       mdata.addVars(new VariableData[] { new VariableData("field1", _finalMav, SymbolData.DOUBLE_TYPE, true, result) });                                                          
       assertEquals("Should return the correct MethodData", mdata, result);
       assertEquals("There should be 2 errors.", 2, errors.size());
       //This is now caught in the type checker.
       //assertEquals("The second error message should be correct.", "The keyword \"void\" cannot be used at the Intermediate level", errors.get(1).getFirst());
       assertEquals("The second error message should be correct.", "You cannot have two method parameters with the same name", errors.get(1).getFirst());
    }
    
    public void testSimpleAnonymousClassInstantiationHelper() {
     SimpleAnonymousClassInstantiation basic = new SimpleAnonymousClassInstantiation(JExprParser.NO_SOURCE_INFO, new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Object", new Type[0]), 
                                                                        new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0]),
                                                                        new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
    

     _iv._package = "i.like";
     _iv.simpleAnonymousClassInstantiationHelper(basic, _sd1);
     assertEquals("There should be no errors", 0, errors.size());
     SymbolData obj = symbolTable.get("java.lang.Object");
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
     ComplexAnonymousClassInstantiation basic = new ComplexAnonymousClassInstantiation(JExprParser.NO_SOURCE_INFO, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "java.lang.Object")),
                                                                                new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Inner", new Type[0]), 
                                                                                new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0]),
                                                                                new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

     _iv._package = "i.like";
     _iv.complexAnonymousClassInstantiationHelper(basic, _sd1);
     assertEquals("There should be no errors", 0, errors.size());
     SymbolData obj = symbolTable.get("java.lang.Object");
     assertNotNull("Object should be in the symbol table", obj);
     SymbolData objInner = symbolTable.get("java.lang.Object.Inner");
     assertEquals("sd1 should have one inner class", 1, _sd1.getInnerClasses().size());
     SymbolData inner = _sd1.getInnerClasses().get(0);
     assertEquals("The inner class should have the proper name", "i.like.monkey$1", inner.getName());
     assertEquals("The inner class should have proper outer data", _sd1, inner.getOuterData());
     assertEquals("The inner class should have null as its super class", null, inner.getSuperClass());
     assertEquals("The inner class should have the right package", "i.like", inner.getPackage());
     assertEquals("The inner class should have 3 methods", 3, inner.getMethods().size());
    }
    
    public void testForVariableDeclaration() {
      //make sure that if forVariableDeclaration is called with a AnonymousClassInstantiation, the symboldata is only added once.
      //this is to make sure an old bug stays fixed.
     SimpleAnonymousClassInstantiation basic = new SimpleAnonymousClassInstantiation(JExprParser.NO_SOURCE_INFO, new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Object", new Type[0]), 
                                                                        new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0]),
                                                                        new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
    

     
     VariableDeclarator[] d1 = {new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), new Word(JExprParser.NO_SOURCE_INFO, "b"), basic)};
     VariableDeclaration vd1 = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,_publicMav, d1); 
     
     ClassBodyIntermediateVisitor cbiv = new ClassBodyIntermediateVisitor(_sd1, _iv._file, _iv._package, _iv._importedFiles, _iv._importedPackages, _iv._classNamesInThisFile, _iv.continuations);
     
     vd1.visit(cbiv);
     assertEquals("Should be 1 inner class of _sd1", 1, _sd1.getInnerClasses().size());
     
    }
    
    public void testForPackageStatementDoFirst() {
      PackageStatement ps = new PackageStatement(JExprParser.NO_SOURCE_INFO, new CompoundWord(JExprParser.NO_SOURCE_INFO, new Word[0]));
      ps.visit(_iv);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "Package statements cannot be used at the Intermediate level.  All Intermediate level classes and interfaces are assumed to be in the default package", errors.getLast().getFirst());
  }
    
  }
}