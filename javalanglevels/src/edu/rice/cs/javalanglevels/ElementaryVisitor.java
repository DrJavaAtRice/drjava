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


/*
 * Top-level Language Level Visitor that represents the Elementary Language Level.  Enforces constraints during the
 * first walk of the AST (checking for langauge specific errors and building the symbol table).
 * This class enforces things that are common to all contexts reachable at the Elementary Language Level (i.e., inside class bodies,
 * method bodies, etc), but also enforces specific top level constraints (i.e. you cannot have try 
 * catch statements at the top level, etc.)
 */
public class ElementaryVisitor extends LanguageLevelVisitor {
  
  /**
   * This constructor is called when creating a new instance of ElementaryVisitor.  The default 
   * value for className is the empty string.
   */
  public ElementaryVisitor(File file, String packageName, LinkedList<String> importedFiles, 
                         LinkedList<String> importedPackages,LinkedList<String> classDefsInThisFile, Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations) {
    super(file, packageName, importedFiles, importedPackages, classDefsInThisFile, continuations);
  }
  
  /**
   * This constructor is called when testing.  It initializes all of the static fields
   * of LanguageLevelVisitor.
   */
  public ElementaryVisitor(File file) {
    this(file, new LinkedList<Pair<String, JExpressionIF>>(), new Symboltable(), new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>(), new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(), new Hashtable<SymbolData, LanguageLevelVisitor>());
  }
    
 /**
   * This constructor is called from LanguageLevelVisitor and LanguageLevelConverter when they are instantiating a new
   * ElementaryVisitor to visit a new file with.  Package is set to "" by default.
   * @param file  The File corresponding to the source file we are visiting
   * @param errors  The list of errors that have been encountered so far.
   * @param symbolTable  The table of classes (types) that we have encountered
   * @param continuations  The table of classes we have encountered but still need to resolve
   * @param visitedFiles  The list of files we have visited
   * @param newSDs  The new symbol datas we have created (that will need to have constructors created for them after this pass is finished)
   */
  public ElementaryVisitor(File file, LinkedList<Pair<String, JExpressionIF>> errors, Symboltable symbolTable, 
                           Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations, 
                           LinkedList<Pair<LanguageLevelVisitor, SourceFile>> visitedFiles, 
                           Hashtable<SymbolData, LanguageLevelVisitor> newSDs) {
    super(file, "", new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>(), continuations);
    this.errors = errors;
    this.symbolTable = symbolTable;
    this.visitedFiles= visitedFiles;//new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>();
    this._newSDs = newSDs;
    _hierarchy = new Hashtable<String, TypeDefBase>();//hierarchy;
    _classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();    
  }
  
  
  /*Try to look up the className*/
  protected SymbolData getSymbolData (String className, SourceInfo si, boolean resolve, boolean fromClassFile) {
    /** 
     * At the elementary level, class names that contain a dot must begin with java.lang because there are
     * no import statements and no inner classes.
     */
    
    if (className.indexOf(".") != -1 && !fromClassFile) {
      _addAndIgnoreError("Class names should not contain \".\" at the Elementary level", new NullLiteral(si));
      return null;
    }
    else {
      return super.getSymbolDataHelper(className, si, resolve, fromClassFile, true, false);
    }
  }
  
  /**
   * Call the super method to convert these to a VariableData array, then make sure that
   * each VariableData is final, as required at the Elementary level.
   * @param enclosingData  The immediately enclosing data of the variables
   */
  protected VariableData[] _variableDeclaration2VariableData(VariableDeclaration vd, Data enclosingData) {
    VariableData[] vds = super._variableDeclaration2VariableData(vd, enclosingData);
    for (int i = 0; i < vds.length; i++) {
      vds[i].setPrivateAndFinal();
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
   * Check the modifiers and visibility specifiers that the user has given.  Make sure they are appropriate.
   * Only abstract is allowed at this level.
   */
  public void forModifiersAndVisibilityDoFirst(ModifiersAndVisibility that) {
    String[] modifiersAndVisibility = that.getModifiers();
    StringBuffer sb = new StringBuffer();
    String temp;
    int count = 0;    
    for(int i = 0; i < modifiersAndVisibility.length; i++) {
      temp = modifiersAndVisibility[i];
      if (!temp.equals("abstract")) {
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
      _addAndIgnoreError(temp + sb.toString() + " cannot be used at the Elementary level", that);
      return;
    }
    super.forModifiersAndVisibilityDoFirst(that);
  }

  /*Make sure that the student has not used "implements" and is not trying to instantiate a black listed class.*/
  public void forClassDefDoFirst(ClassDef that) {
    if (that.getInterfaces().length > 0) {
      _addAndIgnoreError("The keyword \"implements\" cannot be used at the Elementary level", that);
    }
    
    //See if this is a Blacklisted class.  Blacklisted classes are any classes in java.lang or TestCase.
    SymbolData javaLangClass = getSymbolData("java.lang." + that.getName().getText(), that.getSourceInfo(), true, false, false, false);
    if ((that.getName().getText().equals("TestCase")) || (javaLangClass != null && !(javaLangClass.isContinuation()))) {
      _addError("You cannot define a class with the name " + that.getName().getText() + " at the Elementary Level, because that class name is reserved.  Please choose a different name for this class", that);
    }
    super.forClassDefDoFirst(that);
  }

  /*Give an appropriate error*/
  public void forInnerClassDefDoFirst(InnerClassDef that) {
    _addError("You cannot define classes inside of other classes at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forInterfaceDefDoFirst(InterfaceDef that) {
    _addError("Interfaces cannot be used at the Elementary level", that);
  }

  
  /*Give an appropriate error*/
  public void forConstructorDefDoFirst(ConstructorDef that) {
    _addError("All methods must have a return type and a name at the Elementary level", that);
  }

  /* TODO: forInstanceInitializer is overidden in ClassBodyElementaryVisitor
   * & MethodBodyElementaryVisitor; why the discrepancy?
   */
  public void forStaticInitializerDoFirst(StaticInitializer that) {
    _addError("Static initializers cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forPackageStatementDoFirst(PackageStatement that) {
    _addError("Package statements cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forImportStatementDoFirst(ImportStatement that) {
    _addError("Import statements cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
    public void forLabeledStatementDoFirst(LabeledStatement that) {
    _addError("Labeled statements cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forSwitchStatementDoFirst(SwitchStatement that) {
    _addError("Switch statements cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forWhileStatementDoFirst(WhileStatement that) {
    _addError("While statements cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forDoStatementDoFirst(DoStatement that) {
    _addError("Do statements cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forForStatementDoFirst(ForStatement that) {
    _addError("For statements cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forBreakStatementDoFirst(BreakStatement that) {
    _addError("Break statements cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forContinueStatementDoFirst(ContinueStatement that) {
    _addError("Continue statements cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forThrowStatementDoFirst(ThrowStatement that) {
    _addError("Throw statements cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forSynchronizedStatementDoFirst(SynchronizedStatement that) {
    _addError("Synchronized statements cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forTryCatchStatementDoFirst(TryCatchStatement that) {
    _addError("Try-catch statements cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forFormalParameterDoFirst(FormalParameter that) {
    if (that.isIsFinal()) {
      _addError("The keyword \"final\" cannot be used at the Elementary level", that);
    }
    else {
      super.forFormalParameterDoFirst(that);
    }
  }

  /*Give an appropriate error*/
  public void forTypeParameterDoFirst(TypeParameter that) {
    _addError("Type Parameters cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error, if the student is not using an allowable primitive*/
  public void forPrimitiveTypeDoFirst(PrimitiveType that) {
    String name = that.getName();
    if (!(name.equals("int") || name.equals("double") || name.equals("boolean") || name.equals("char"))) {
      _addError("Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Elementary level", that);
    }
  }

  /*Give an appropriate error*/
  public void forArrayTypeDoFirst(ArrayType that) {
    _addError("Arrays cannot be used at the Elementary level", that);
  }


  /*Give an appropriate error*/
  public void forConditionalExpressionDoFirst(ConditionalExpression that) {
    _addError("Conditional expressions cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forInstanceofExpressionDoFirst(InstanceofExpression that) {
    _addError("Instanceof cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forCastExpressionDoFirst(CastExpression that) {
    _addError("Cast expressions cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forAnonymousClassInstantiationDoFirst(AnonymousClassInstantiation that) {
    _addError("Anonymous inner classes cannot be used at the Elementary level", that);
  }

  /*Give an appropriate error*/
  public void forNullLiteralDoFirst(NullLiteral that) {
    _addError("Null cannot be used at the Elementary level", that);
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
   * check to see if this class extends TestCase.  If so, it needs to be made public and
   * junit.framework needs to be imported.  Use addSymbolData to create the appropriate symbol data,
   * and then visit the class body.  Finally, autogenerate the toString, equals, hashCode, and accessor
   * methods.  The constructor will be autogenerated right before the TypeChecking pass starts.
   * Once the class def has been handled, remove it from classesToBeParsed.
   */
  public void forClassDef(ClassDef that) {
    forClassDefDoFirst(that);
    if (prune(that)) { return; }
    
    boolean isTestCase = false;
    String className = getQualifiedClassName(that.getName().getText());
   
    String superName = that.getSuperclass().getName();
    if (superName.equals("TestCase") || superName.equals("junit.framework.TestCase")) {
      isTestCase=true;
      if (! _importedPackages.contains("junit.framework"))
        _importedPackages.addFirst("junit.framework");
      
      getSymbolData("junit.framework.TestCase", that.getSourceInfo(), true, false, true, false);   //add this to the symbol table.
    }

    SymbolData sd = addSymbolData(that, className);
    if (sd == null) {
      return;
    }
    
    //Test cases are automatically public
    if (isTestCase) {
      sd.addModifier("public");
    }

    that.getMav().visit(this);
    that.getName().visit(this);
    for (int i = 0; i < that.getTypeParameters().length; i++) that.getTypeParameters()[i].visit(this);
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    
    if (sd != null) {
      that.getBody().visit(new ClassBodyElementaryVisitor(sd, _file, _package, _importedFiles, _importedPackages, _classNamesInThisFile, continuations));

      createAccessors(sd, _file);
      createToString(sd);
      createHashCode(sd);
      createEquals(sd);
    }
    forClassDefOnly(that);
    
    _classesToBeParsed.remove(className);
  }
  
   /**
    * Test the methods in the above class.
    */
  public static class ElementaryVisitorTest extends TestCase {
    
    private ElementaryVisitor _bv;
    
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
    
    
    public ElementaryVisitorTest() {
      this("");
    }
    public ElementaryVisitorTest(String name) {
      super(name);
    }
    
    public void setUp() {

      errors = new LinkedList<Pair<String, JExpressionIF>>();
      symbolTable = new Symboltable();
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, edu.rice.cs.javalanglevels.tree.SourceFile>>();      
      _hierarchy = new Hashtable<String, TypeDefBase>();
      _classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
      _bv = new ElementaryVisitor(new File(""), errors, symbolTable, new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>(), new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(), new Hashtable<SymbolData, LanguageLevelVisitor>());
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, IterUtil.make(new File("lib/buildlib/junit.jar")));
      _bv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      _bv._resetNonStaticFields();
      _bv._importedPackages.addFirst("java.lang");
      _errorAdded = false;
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");
    }
    
    public void testForModifiersAndVisibilityDoFirst() {
      
      //only the abstract modifier is allowed
      _bv.forModifiersAndVisibilityDoFirst(_abstractMav);
      assertEquals("there should be no errors", 0, errors.size());
      
      //check some errors
      _bv.forModifiersAndVisibilityDoFirst(_publicMav);
      assertEquals("there should now be 1 error", 1, errors.size());
      assertEquals("The error message should be correct for pub modifier:", "The keyword \"public\" cannot be used at the Elementary level", errors.getLast().getFirst());
      
      _bv.forModifiersAndVisibilityDoFirst(_privateMav);
      assertEquals("there should now be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct for private modifier:", "The keyword \"private\" cannot be used at the Elementary level", errors.getLast().getFirst());

      ModifiersAndVisibility mavs2 = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, 
                                                                new String[] {"private", "static"});
       _bv.forModifiersAndVisibilityDoFirst(mavs2);
      assertEquals("there should now be 3 errors", 3, errors.size());
      assertEquals("The error message should be correct for 2 modifiers:", "The keywords \"private\" \"static\" cannot be used at the Elementary level", errors.getLast().getFirst());
    }
    
    public void testForClassDefDoFirst() {
      //check an example that works--no super class
      ClassDef cd1 = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                                  new Word(JExprParser.NO_SOURCE_INFO, "Test"), new TypeParameter[0],
                                  JExprParser.NO_TYPE, 
                                  new ReferenceType[0], new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      _bv.forClassDefDoFirst(cd1);
      assertEquals("should be no errors", 0, errors.size());
      
      //check that no error is thrown if a class explicitely extends another class
      ClassDef cd0 = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, new Word(JExprParser.NO_SOURCE_INFO, "Lisa"),
                                 new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), new ReferenceType[0], new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

      _bv.forClassDefDoFirst(cd0);
      assertEquals("there should still be no errors", 0, errors.size());
       
       //check that an error is thrown if a class implements any interfaces.
       ClassDef cd2 = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                                   new Word(JExprParser.NO_SOURCE_INFO, "Test"), new TypeParameter[0],
                                   JExprParser.NO_TYPE, 
                                   new ReferenceType[] {new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0])}, 
                                   new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

                                 
      _bv.forClassDefDoFirst(cd2);
      assertEquals("there should now be 1 errors", 1, errors.size());
      assertEquals("The error message should be correct for implements", 
                   "The keyword \"implements\" cannot be used at the Elementary level", 
                   errors.getLast().getFirst());
      
      //make sure that if a java.lang class is shadowed, an error is given
      ClassDef cd3 = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                                  new Word(JExprParser.NO_SOURCE_INFO, "String"), new TypeParameter[0],
                                  JExprParser.NO_TYPE, 
                                  new ReferenceType[0], new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      cd3.visit(_bv);
      assertEquals("There should now be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct", "You cannot define a class with the name String at the Elementary Level, because that class name is reserved.  Please choose a different name for this class", errors.getLast().getFirst());

      ClassDef cd4 = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                                  new Word(JExprParser.NO_SOURCE_INFO, "Integer"), new TypeParameter[0],
                                  JExprParser.NO_TYPE, 
                                  new ReferenceType[0], new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      cd4.visit(_bv);
      assertEquals("There should now be 3 errors", 3, errors.size());
      assertEquals("The error message should be correct", "You cannot define a class with the name Integer at the Elementary Level, because that class name is reserved.  Please choose a different name for this class", errors.getLast().getFirst());

      
      //make sure that if TestCase is shadowed, an error is given
      ClassDef cd5 = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                                  new Word(JExprParser.NO_SOURCE_INFO, "TestCase"), new TypeParameter[0],
                                  JExprParser.NO_TYPE, 
                                  new ReferenceType[0], new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      cd5.visit(_bv);
      assertEquals("There should now be 4 errors", 4, errors.size());
      assertEquals("The error message should be correct", "You cannot define a class with the name TestCase at the Elementary Level, because that class name is reserved.  Please choose a different name for this class", errors.getLast().getFirst());

      
    }
    
    public void testForFormalParameterDoFirst() {
      PrimitiveType pt = new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int");
      Word w = new Word(JExprParser.NO_SOURCE_INFO, "param");
      UninitializedVariableDeclarator uvd = new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, pt, w);
      
      // check an example that works
      FormalParameter fp = new FormalParameter(JExprParser.NO_SOURCE_INFO, uvd, false);
      _bv.forFormalParameterDoFirst(fp);
      assertEquals("should be no errors", 0, errors.size());
      
      // check that an error is thrown if the FormalParameter is final
      FormalParameter fp2 = new FormalParameter(JExprParser.NO_SOURCE_INFO, uvd, true);  
      _bv.forFormalParameterDoFirst(fp2);
      assertEquals("should be one error", 1, errors.size());
      assertEquals("The error message should be correct for a final parameter.", 
                   "The keyword \"final\" cannot be used at the Elementary level", 
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
      Expression e = new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 1);
      Block b = new Block(noInfo, emptyBody);
      TypeVariable tv = new TypeVariable(noInfo, "name");
      
      InnerClassDef icd = new InnerClassDef(noInfo, _publicMav, w, tps, superClass, rts, emptyBody);
      InterfaceDef ifd = new InterfaceDef(noInfo, _publicMav, w, tps, rts, emptyBody);
      ConstructorDef cd = new ConstructorDef(noInfo, new Word(JExprParser.NO_SOURCE_INFO, "constructor"), _publicMav, fps, rts, emptyBody);
      StaticInitializer si = new StaticInitializer(noInfo, b);
      PackageStatement ps = new PackageStatement(noInfo, cw);
      ImportStatement is = new ClassImportStatement(noInfo, cw);
      LabeledStatement ls = new LabeledStatement(noInfo, new Word(noInfo, "label"), stmt);
      SwitchStatement ss = new SwitchStatement(noInfo, e, new SwitchCase[0]);
      WhileStatement ws = new WhileStatement(noInfo, e, stmt);
      DoStatement ds = new DoStatement(noInfo, stmt, e);
      ForStatement fs = new ForStatement(noInfo, new UnparenthesizedExpressionList(noInfo, new Expression[0]), 
                                         new EmptyForCondition(noInfo), new UnparenthesizedExpressionList(noInfo, new Expression[0]), 
                                         stmt);
      BreakStatement bs = new UnlabeledBreakStatement(noInfo);
      ContinueStatement cs = new UnlabeledContinueStatement(noInfo);
      ThrowStatement ts = new ThrowStatement(noInfo, e);
      SynchronizedStatement syncs = new SynchronizedStatement(noInfo, e, b);
      TryCatchStatement tcs = new NormalTryCatchStatement(noInfo, b, new CatchBlock[0]);
      TypeParameter tp = new TypeParameter(noInfo, tv, superClass);
      ArrayInitializer ai = new ArrayInitializer(noInfo, new VariableInitializerI[0]);
      ArrayType at = new ArrayType(noInfo, "myName", tv);
      VoidReturn vr = new VoidReturn(noInfo, "string");
      ConditionalExpression ce = new ConditionalExpression(noInfo, e, e, e);
      InstanceofExpression ioe = new InstanceofExpression(noInfo, e, tv);
      CastExpression caste = new CastExpression(noInfo, superClass, e);
      SimpleAnonymousClassInstantiation aice = new SimpleAnonymousClassInstantiation(noInfo, tv, 
                                                                                     new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0]), 
                                                                                     emptyBody);
     
      ComplexAnonymousClassInstantiation aice2 = new ComplexAnonymousClassInstantiation(noInfo, new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")),
                                                                                        tv, 
                                                                                        new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, new Expression[0]), 
                                                                                        emptyBody);
      NullLiteral nl = new NullLiteral(noInfo);
     
      BracedBody hasBitOperator = new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {new ExpressionStatement(JExprParser.NO_SOURCE_INFO, 
                                                                                                                      new RightSignedShiftExpression(JExprParser.NO_SOURCE_INFO, 
                                                                                                                                                     new SimpleNameReference(JExprParser.NO_SOURCE_INFO, new Word(JExprParser.NO_SOURCE_INFO, "j")), new IntegerLiteral(JExprParser.NO_SOURCE_INFO, 5)))});
      
      
      icd.visit(_bv);
      assertEquals("InnerClassDef is not allowed", "You cannot define classes inside of other classes at the Elementary level", errors.getLast().getFirst());
      
      ifd.visit(_bv);
      assertEquals("InterfaceDef is not allowed", "Interfaces cannot be used at the Elementary level", errors.getLast().getFirst());
      
      cd.visit(_bv);
      assertEquals("ConstructorDef is not allowed", "All methods must have a return type and a name at the Elementary level", errors.getLast().getFirst());
      
      si.visit(_bv);
      assertEquals("StaticInitializer is not allowed", "Static initializers cannot be used at the Elementary level", errors.getLast().getFirst());
      
      ps.visit(_bv);
      assertEquals("PackageStatement is not allowed", "Package statements cannot be used at the Elementary level", errors.getLast().getFirst());
      
      is.visit(_bv);
      assertEquals("ImportStatement is not allowed", "Import statements cannot be used at the Elementary level", errors.getLast().getFirst());
      
      ls.visit(_bv);
      assertEquals("Labeled Statement is not allowed", "Labeled statements cannot be used at the Elementary level", errors.getLast().getFirst());
      
      ss.visit(_bv);
      assertEquals("SwitchStatement is not allowed", "Switch statements cannot be used at the Elementary level", errors.getLast().getFirst());
      
      ws.visit(_bv);
      assertEquals("WhileStatement is not allowed", "While statements cannot be used at the Elementary level", errors.getLast().getFirst());
      
      ds.visit(_bv);
      assertEquals("DoStatement is not allowed", "Do statements cannot be used at the Elementary level", errors.getLast().getFirst());
      
      fs.visit(_bv);
      assertEquals("ForStatement is not allowed", "For statements cannot be used at the Elementary level", errors.getLast().getFirst());
      
      bs.visit(_bv);
      assertEquals("BreakStatement is not allowed", "Break statements cannot be used at the Elementary level", errors.getLast().getFirst());
      
      cs.visit(_bv);
      assertEquals("ContinueStatement is not allowed", "Continue statements cannot be used at the Elementary level", errors.getLast().getFirst());
      
      ts.visit(_bv);
      assertEquals("ThrowStatement is not allowed", "Throw statements cannot be used at the Elementary level", errors.getLast().getFirst());
      
      syncs.visit(_bv);
      assertEquals("SynchronizedStatement is not allowed", "Synchronized statements cannot be used at the Elementary level", errors.getLast().getFirst());
      
      tcs.visit(_bv);
      assertEquals("TryCatchStatement is not allowed", "Try-catch statements cannot be used at the Elementary level", errors.getLast().getFirst());
      
      tp.visit(_bv);
      assertEquals("TypeParameters is not allowed", "Type Parameters cannot be used at the Elementary level", errors.getLast().getFirst());
      
      at.visit(_bv);
      assertEquals("ArrayTypes is not allowed", "Arrays cannot be used at the Elementary level", errors.getLast().getFirst());
      
      ce.visit(_bv);
      assertEquals("ConditionalExpression is not allowed", "Conditional expressions cannot be used at the Elementary level", errors.getLast().getFirst());
      
      ioe.visit(_bv);
      assertEquals("InstanceofExpression is not allowed", "Instanceof cannot be used at the Elementary level", errors.getLast().getFirst());
      
      caste.visit(_bv);
      assertEquals("CastExpression is not allowed", "Cast expressions cannot be used at the Elementary level", errors.getLast().getFirst());
      
      aice.visit(_bv);
      assertEquals("AnonymousInnerClassExpression is not allowed", "Anonymous inner classes cannot be used at the Elementary level", errors.getLast().getFirst());
      
      aice2.visit(_bv);
      assertEquals("AnonymousInnerClassExpression is not allowed", "Anonymous inner classes cannot be used at the Elementary level", errors.getLast().getFirst());
      
      
      nl.visit(_bv);
      assertEquals("NullLiteral is not allowed", "Null cannot be used at the Elementary level", errors.getLast().getFirst());
      
      hasBitOperator.visit(_bv);
      assertEquals("Bit shifting operators are not allowed", "Bit shifting operators cannot be used at any language level", errors.getLast().getFirst());
      
    }
    
    public void testForPrimitiveTypeDoFirst() {
      
      SourceInfo noInfo = JExprParser.NO_SOURCE_INFO;
      
     //only primative types boolean, char, int, and double are allowed at elementary level. 
      PrimitiveType i = new PrimitiveType(noInfo, "int");
      PrimitiveType c = new PrimitiveType(noInfo, "char");
      PrimitiveType d = new PrimitiveType(noInfo, "double");
      PrimitiveType b = new PrimitiveType(noInfo, "boolean");
      
      i.visit(_bv);
      assertEquals("After visiting int, errors should still be 0", 0, errors.size());

      c.visit(_bv);
      assertEquals("After visiting char, errors should still be 0", 0, errors.size());
            
      d.visit(_bv);
      assertEquals("After visiting double, errors should still be 0", 0, errors.size());

      b.visit(_bv);
      assertEquals("After visiting boolean, errors should still be 0", 0, errors.size());
      
      //now the types that should throw an error:
      
      PrimitiveType byt = new PrimitiveType(noInfo, "byte");
      PrimitiveType s = new PrimitiveType(noInfo, "short");
      PrimitiveType l = new PrimitiveType(noInfo, "long");
      PrimitiveType f = new PrimitiveType(noInfo, "float");
      
      byt.visit(_bv);
      assertEquals("After visiting byte, errors should be 1", 1, errors.size());
      assertEquals("After byte, error message is correct", "Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Elementary level", errors.getLast().getFirst());

      s.visit(_bv);
      assertEquals("After visiting short, errors should be 2", 2, errors.size());
      assertEquals("After short, error message is correct", "Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Elementary level", errors.getLast().getFirst());

      l.visit(_bv);
      assertEquals("After visiting long, errors should be 3", 3, errors.size());
      assertEquals("After long, error message is correct", "Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Elementary level", errors.getLast().getFirst());

      f.visit(_bv);
      assertEquals("After visiting float, errors should be 4", 4, errors.size());
      assertEquals("After float, error message is correct", "Only the primitive types \"int\", \"double\", \"boolean\", and \"char\" can be used at the Elementary level", errors.getLast().getFirst());
    }
    
    public void test_isClassInCurrentFile() {
     assertFalse("class not in file should return false", _bv._isClassInCurrentFile("NotInFile"));
     _bv._classNamesInThisFile.addLast("package.MyClass");
     assertTrue("full class name in file should return true", _bv._isClassInCurrentFile("package.MyClass"));
     assertTrue("unqualified class name in file should return true", _bv._isClassInCurrentFile("MyClass"));
     
     assertFalse("partial name in file, not same class, should return false", _bv._isClassInCurrentFile("Class"));
                
    }
    
    
    public void test_getFieldAccessorName() {
      // This may change in the future if we change getFieldAccessorName
      assertEquals("Should correctly convert from lower case to upper case", "name", _bv.getFieldAccessorName("name"));
    }
    
    
    public void testForClassDef() {
      //check an example that's not abstract
      ClassDef cd0 = new ClassDef(JExprParser.NO_SOURCE_INFO, _packageMav, new Word(JExprParser.NO_SOURCE_INFO, "Lisa"),
                                  new TypeParameter[0], JExprParser.NO_TYPE, new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cd0.visit(_bv);
      assertEquals("There should be no errors", 0, errors.size());
      assertTrue("Should have resolved java.lang.Object", symbolTable.containsKey("java.lang.Object"));
      assertFalse("Should not be a continuation", symbolTable.get("java.lang.Object").isContinuation());
      SymbolData sd = symbolTable.get("Lisa");

      MethodData md2 = new MethodData("equals",
                                   _publicMav, 
                                   new TypeParameter[0], 
                                   SymbolData.BOOLEAN_TYPE, 
                                     new VariableData[] { new VariableData(_bv.getSymbolData("Object", _bv._makeSourceInfo("java.lang.Object"), true)) },
                                   new String[0], 
                                   sd,
                                   null);
      md2.getParams()[0].setEnclosingData(sd.getMethods().getLast());
      assertEquals("sd's last method should be equals()", md2, sd.getMethods().getLast());
      assertFalse("sd should not be public", sd.hasModifier("public"));
      
      //check an example that's abstract
      ClassDef cd1 = new ClassDef(JExprParser.NO_SOURCE_INFO, _abstractMav, new Word(JExprParser.NO_SOURCE_INFO, "Bart"),
                                  new TypeParameter[0], JExprParser.NO_TYPE, new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      cd1.visit(_bv);
      assertEquals("There should be no errors", 0, errors.size());
      sd = symbolTable.get("Bart");
      assertTrue("Bart should be in _newSDs", _newSDs.containsKey(sd));
      MethodData md = new MethodData("Bart", _publicMav, new TypeParameter[0], sd, 
                          (VariableData[])sd.getVars().toArray(new VariableData[0]), 
                          new String[0], 
                          sd,
                          null);
      assertFalse("sd should not be public", sd.hasModifier("public"));
      assertEquals("There should be 3 methods", 3, sd.getMethods().size());
                  
      //Check an example that extends TestCase, but where the file is of the wrong name.
      ConcreteMethodDef cmd = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                     _packageMav, 
                                                     new TypeParameter[0], 
                                                     new VoidReturn(JExprParser.NO_SOURCE_INFO, "void"), 
                                                     new Word(JExprParser.NO_SOURCE_INFO, "testMethodName"),
                                                     new FormalParameter[0],
                                                     new ReferenceType[0], 
                                                     new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

      ClassDef cd3 = new ClassDef(JExprParser.NO_SOURCE_INFO, _abstractMav, new Word(JExprParser.NO_SOURCE_INFO, "TestSuper2"),
                                  new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "TestCase", new Type[0]), 
                                  new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {cmd}));
      _bv._file = new File("WrongFile.dj0");
      cd3.visit(_bv);
      
      assertEquals("There should be no errors", 0, errors.size());

      
      //Check an example where the class extends TestCase, and has a test method that returns void.
      ConcreteMethodDef cmd2 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _packageMav, 
                                                    new TypeParameter[0], 
                                                    new VoidReturn(JExprParser.NO_SOURCE_INFO, "void"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "testMethodName"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

      ClassDef cd4 = new ClassDef(JExprParser.NO_SOURCE_INFO, _abstractMav, new Word(JExprParser.NO_SOURCE_INFO, "TestSuper3"),
                                  new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "TestCase", new Type[0]), new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {cmd2}));


      _bv._file=new File("TestSuper3.dj0");
      cd4.visit(_bv);

      assertEquals("There should still be no errors", 0, errors.size());
      
      //Check a method with void return, but name not starting with test, so it's not okay.
      ConcreteMethodDef cmd3 = new ConcreteMethodDef(JExprParser.NO_SOURCE_INFO, 
                                                    _packageMav, 
                                                    new TypeParameter[0], 
                                                    new VoidReturn(JExprParser.NO_SOURCE_INFO, "void"), 
                                                    new Word(JExprParser.NO_SOURCE_INFO, "uhOh"),
                                                    new FormalParameter[0],
                                                    new ReferenceType[0], 
                                                    new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));

      ClassDef cd5 = new ClassDef(JExprParser.NO_SOURCE_INFO, _abstractMav, new Word(JExprParser.NO_SOURCE_INFO, "TestVoidNoTestMethod"),
                                  new TypeParameter[0], new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "TestCase", new Type[0]), new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[] {cmd3}));


      _bv._file=new File("TestVoidNoTestMethod.dj0");
      cd5.visit(_bv);
      
      assertEquals("There should still be one error", 0, errors.size());
      
    }
    
  }
}