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

import edu.rice.cs.javalanglevels.parser.*;
import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.javalanglevels.util.Log;
import edu.rice.cs.javalanglevels.util.Utilities;
import java.io.*;
import java.util.*;
import junit.framework.TestCase;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.*;

import static edu.rice.cs.javalanglevels.SourceInfo.NONE;

public class Augmentor extends JExpressionIFDepthFirstVisitor<Void> {
//  public static final Log _log = new Log("Augmentor.txt", true);
  
  private static final String newLine = System.getProperty("line.separator");
  private static final int indentWidth = 2; // TODO: get this from DrJava?

  
  /** IDIOCY Pointer:  Why are all these private fields static?  They are set in the constructor.  Was this design 
    * choice made to ensure only one Augmentor exists at a time by clobbering fields.  Stupid, stupid, stupid. */
  
  /** The original source file to be augmented. */
  static private BufferedReader _fileIn;
  
  /** The current line number in _fileIn. */
  static private int _fileInLine;
  
  /** The current column number in _fileIn.  This is the 1 greater than the last column read. */
  static private int _fileInColumn;
  
  /** The destination file. */
  static private BufferedWriter _fileOut;
  
  /** The current line number in _fileOut. */
  static private int _fileOutLine;
  
  /** The dj* line number to which the current line number in _fileOut corresponds. */
  static private int _fileOutCorrespondingLine;

  /** A map from original dj* line number to generated java line number. */
  static private TreeMap<Integer,Integer> _lineNumberMap;
  
  /** The symbol information from this source tree. */
  static private LanguageLevelVisitor _llv;
  
  /** If true, generated toString, hashCode, & equals methods should correctly handle arrays & infinitely recursive
    * structures */
  static private boolean _safeSupportCode;
  
  /** A String of variable definitions to be written at the end of the top-level class definitions */
  static private List<String> _endOfClassVarDefs;
  
  /** The SymbolData enclosing whatever we are currently augmenting.*/
  private SymbolData _enclosingData;
  
  /** Main constructor for Augmentor: Used by the LanguageLevelConverter when converting language level files.
    * @param safeSupportCode  true if the user wants safe support code to be generated (this comes with a high overhead)
    * @param fileIn  A BufferedReader corresponding to the LanguageLevel file we should read from
    * @param fileOut  A BufferedWriter corresponding to the .java file we should write to.
    * @param llv  The LanguageLevelVisitor that was used to traverse the language level file.
    */
  public Augmentor(boolean safeSupportCode, BufferedReader fileIn, BufferedWriter fileOut, LanguageLevelVisitor llv) {
    _fileIn = fileIn;
    _fileInLine = 1;
    _fileInColumn = 1;
    _fileOut = fileOut;
    _fileOutLine = 1;
    _fileOutCorrespondingLine = 1;
    _lineNumberMap = new TreeMap<Integer,Integer>();
    _llv = llv;
    _safeSupportCode = safeSupportCode;
    _endOfClassVarDefs = new LinkedList<String>();
    
    _enclosingData = null;
  }
  
  /** Create another Augmentor sharing the same static fields as the current Augmentor, but with a new _enclosingData d.
    * This constructor should only be called from within another Augmentor.
    * @param d  The EnclosingData from which this Augmentor works.
    */
  protected Augmentor(SymbolData d) { _enclosingData = d; }
  
  /** This method is called by default from cases that do not override forCASEOnly. */
  protected Void defaultCase(JExpressionIF that) { return null; } 
  
  /** Return a Void array of the specified size. */
  protected Void[] makeArrayOfRetType(int len) { return new Void[len]; }
  
  /** Writes out implicit variableDeclarationModfiers that must be added to augmented file.  If no visibility modifier
    * is present, this method makes static fields "public final" and instance fields "private final".  If a visibility
    * modifier is present, it overrides this default.  But all fields are forced to be "final".
    * @param that is the field declaration being augmented
    */
  protected void augmentVariableDeclarationModifiers(VariableDeclaration that) {
    
    // make static fields public final, make instance fields private final
    StringBuilder modifierString = new StringBuilder();
    String[] modifiers = that.getMav().getModifiers();
    if (! Utilities.hasVisibilityModifier(modifiers)) {
      if (Utilities.isStatic(modifiers)) modifierString.append("public ");
      else modifierString.append("private ");
    }                                
    if (! Utilities.isFinal(modifiers)) modifierString.append("final "); 
    _writeToFileOut(modifierString.toString());
  }
  
  /** Do the augmenting appropriate for a Variable Declaration: all Variable Declarations should
    * be augmented with "final" if such a modifier is not already present.
    * @param that  The VariableDeclaration we are augmenting.
    */
  public Void forVariableDeclaration(VariableDeclaration that) {
    _readAndWriteThroughIndex(that.getSourceInfo().getStartLine(), that.getSourceInfo().getStartColumn() - 1);
    augmentVariableDeclarationModifiers(that);
    super.forVariableDeclaration(that);
    return null;
  }
  
  /** All formal parameters (parameters to a method or in a catch clause) are augmented to be "final".
    * Always read up to the start of the FormalParameter before beginning augmentation.
    * @param that  The FormalParameter we are augmenting.
    */
  public Void forFormalParameter(FormalParameter that) {
    _readAndWriteThroughIndex(that.getSourceInfo().getStartLine(), that.getSourceInfo().getStartColumn() - 1);
    if (! that.isIsFinal()) _writeToFileOut("final ");
      
    // We don't bother to visit the declarator, since it does not need to be augmented.
    return null;
  }
  
  /** Do the augmentation necessary for a ConstructorDef.  If no visibility modifier is present, augment with "public"
    * by default.  The Formal Parameters to the MethodDef need to be visited so that they can be augmented with "final".   
    * Always read up to the start of the ConstructorDef before beginning augmentation.
    * @param that  The ConstructorDef we are augmenting.
    */
  public Void forConstructorDef(ConstructorDef that) {
    _readAndWriteThroughIndex(that.getSourceInfo().getStartLine(), that.getSourceInfo().getStartColumn() - 1);
    
    // Check and see if the constructor has modifiers.  If not, make it public by default
    String[] modifiers = that.getMav().getModifiers();
    if (! Utilities.hasVisibilityModifier(modifiers)) _writeToFileOut("public ");

    for (FormalParameter fp : that.getParameters()) { fp.visit(this); }
    // We don't bother visiting the rest of the method declaration
    return null;
  }
  
  /** Do the augmentation necessary for a MethodDef.  If the user did not specify a visibility level,
    * the method is automatically augmented to be "public".  Otherwise, the user's modifier is left unchanged.
    * At all levels, the Formal Parameters to the MethodDef need to be visited so that
    * they can be augmented with "final" if "final" is not already present. 
    * Always read up to the start of the MethodDef before beginning augmentation.
    * @param that  The MethodDef being visited.
    */
  public Void forMethodDef(MethodDef that) {
    SourceInfo mdSourceInfo = that.getSourceInfo();
    _readAndWriteThroughIndex(mdSourceInfo.getStartLine(), mdSourceInfo.getStartColumn() - 1);
      
    if (! Utilities.hasVisibilityModifier(that.getMav().getModifiers())) _writeToFileOut("public ");
    
    for (FormalParameter fp : that.getParams()) { fp.visit(this); }
    // We don't bother visiting the rest of the method declaration
    return null;
  }
  
  
  /** Delegate the augmentation of this AbstractMethodDef to forMethodDef.
    * @param that  The AbstractMethodDef being augmented.
    */
  public Void forAbstractMethodDef(AbstractMethodDef md) { forMethodDef(md); return null; }
  
  /** Delegate the augmentation of this method def's declaration to forMethodDef.  Then, visit the body with a 
    * MethodBodyAugmentor so that each piece of the body can be correctly augmented.
    * @param that  The ConcreteMethodDef being augmented.
    */
  public Void forConcreteMethodDef(ConcreteMethodDef that) {
    assert _enclosingData != null;
    
    forMethodDef(that);
    SymbolData enclosing = _enclosingData.getSymbolData();
    MethodData md = 
      enclosing.getMethod(that.getName().getText(), 
                          formalParameters2TypeDatas(that.getParams(), enclosing));
//    _log.log("Augmenting ConcreteMethodDef " + that + " with MethodData " + md);
    if (md == null) { 
      throw new RuntimeException("Internal Program Error: Can't find method data for " + that.getName() + 
                                 " Please report this bug."); 
    }
    that.getBody().visit(new MethodBodyAugmentor(enclosing));
    return null;
  }
  
  /** Class Defs can only appear at the top level of a source file.  If the class type is public but "public" does
    * not appear as a visibility modifier, add it.  Visit the body of the class definition with a new Augmentor.  
    * Then, (so that this appears after the rest of the class body) add any necessary augmented methods.
    * @param cd  The ClassDef we're augmenting.
    */
  public Void forClassDef(ClassDef cd) {
    String className = cd.getName().getText();
    SymbolData sd = _llv.symbolTable.get(_llv.getQualifiedClassName(className));
    if (sd == null) { throw new RuntimeException("Internal Program Error: Can't find SymbolData for " + 
                                                 cd.getName().getText() + " Please report this bug."); }
    
    ModifiersAndVisibility m = cd.getMav();
    String[] modifiers = m.getModifiers();
    
    /* Support legacy Elementary level (dj0) files by generating a Junit import statement if this import has
     * been auto-generated. */

    if (sd.hasAutoGeneratedJunitImport()) {
      // import statement should be OK here because language level test files can only contain one class. */
      _writeToFileOut("import junit.framework.TestCase;" + newLine);  
    }
    
    /* Support legacy Elementary level (dj0) files by add "public " prefix to classes that must be public (TestCase 
     * files). */
    if (sd.hasModifier("public") && (! Utilities.isPublic(m.getModifiers()))) {
      assert ! Utilities.hasVisibilityModifier(modifiers);
      _readAndWriteThroughIndex(m.getSourceInfo().getStartLine(), m.getSourceInfo().getStartColumn() - 1);
      _writeToFileOut("public ");
    }
    
    BracedBody bb = cd.getBody();
    sd.setAnonymousInnerClassNum(0);
    bb.visit(new Augmentor(sd));
    
    int baseIndent = cd.getSourceInfo().getStartColumn() - 1;
    className = LanguageLevelVisitor.getUnqualifiedClassName(sd.getName());
    _readAndWriteThroughIndex(cd.getSourceInfo().getEndLine(), cd.getSourceInfo().getEndColumn() - 1);
    
    // Augment this class declaration
    writeConstructor(className, sd, baseIndent);
    writeAccessors(sd, baseIndent);
    String valueToStringName = writeValueToString(sd, baseIndent);
    String valueEqualsName = writeValueEquals(sd, baseIndent);
    String valueHashCodeName = writeValueHashCode(sd, baseIndent, valueEqualsName);
    writeToString(sd, baseIndent, valueToStringName);
    writeEquals(className, sd, baseIndent, valueEqualsName);
    writeHashCode(className, sd, baseIndent, false, valueHashCodeName);
    for (String s : _endOfClassVarDefs) {
      _writeToFileOut(newLine + indentString(baseIndent, 1) + s);
    }
    if (_endOfClassVarDefs.size() > 0) {
      _writeToFileOut(newLine);
      _endOfClassVarDefs.clear();
    }
    _writeToFileOut(indentString(baseIndent, 0));
    
    // We don't bother visiting any of the signature nodes -- parameters, type, name, etc.
    return null;
  }

  /** Look up this inner class in the enclosing data, and then visits its body. InnerClassDefs can appear inside method
    * or class or interface bodies.  No augmentation is done, because an InnerClass can only appear at the 
    * AdvancedLevel,
    * and we do not do augmentation at the Advanced Level.  If this were to change, we would need to
    * add the Augmentation back in.
    * @param cd  The InnerClassDef we are augmenting.
    */
  public Void forInnerClassDef(InnerClassDef cd) {
    String className = cd.getName().getText();
    if (_enclosingData == null) {
      throw new RuntimeException("Internal Program Error: Enclosing Data is null.  Please report this bug.");
    }
    SymbolData sd = _enclosingData.getInnerClassOrInterface(className);
    if (sd == null) {
      throw new RuntimeException("Internal Program Error: Can't find SymbolData for " + cd.getName().getText() + 
                                 ". Please report this bug.");
    }
    
    /** WARNING: the code suffix copied from ClassDef; it it works it should be refactored. */
    BracedBody bb = cd.getBody();
    sd.setAnonymousInnerClassNum(0);
    bb.visit(new Augmentor(sd));
    
    int baseIndent = cd.getSourceInfo().getStartColumn() - 1;
    className = LanguageLevelVisitor.getUnqualifiedClassName(sd.getName());
    _readAndWriteThroughIndex(cd.getSourceInfo().getEndLine(), cd.getSourceInfo().getEndColumn() - 1);
    
    // Augment this class declaration
    writeConstructor(className, sd, baseIndent);
    writeAccessors(sd, baseIndent);
    String valueToStringName = writeValueToString(sd, baseIndent);
    String valueEqualsName = writeValueEquals(sd, baseIndent);
    String valueHashCodeName = writeValueHashCode(sd, baseIndent, valueEqualsName);
    writeToString(sd, baseIndent, valueToStringName);
    writeEquals(className, sd, baseIndent, valueEqualsName);
    writeHashCode(className, sd, baseIndent, false, valueHashCodeName);
    for (String s : _endOfClassVarDefs) {
      _writeToFileOut(newLine + indentString(baseIndent, 1) + s);
    }
    if (_endOfClassVarDefs.size() > 0) {
      _writeToFileOut(newLine);
      _endOfClassVarDefs.clear();
    }
    _writeToFileOut(indentString(baseIndent, 0));
    
    
// We don't bother visiting any of the signature nodes -- parameters, type, name, etc.
    return null;
  }
  
  /* Old suffix */
  
//    BracedBody bb = cd.getBody();
//    sd.setAnonymousInnerClassNum(0);
//    bb.visit(new Augmentor(sd));
//
//    _readAndWriteThroughIndex(cd.getSourceInfo().getEndLine(), cd.getSourceInfo().getEndColumn() - 1);;
//
//    // We don't bother visiting any of the signature nodes -- parameters, type, name, etc.
//    return null;
//  }
  
  /** Look up this top level interface in the symbolTable, and then visit its body.  Write any necessary extra variable
    * definitions at the end of the file.  InterfaceDefs can only appear at the top level of a source file.
    * @param cd  The InterfaceDef being augmented.
    */
  public Void forInterfaceDef(InterfaceDef cd) {
    String interfaceName = cd.getName().getText();
    SymbolData sd = _llv.symbolTable.get(_llv.getQualifiedClassName(interfaceName));
    if (sd == null) { throw new RuntimeException("Internal Program Error: Can't find SymbolData for " +
                                                 cd.getName().getText() + ".  Please report this bug."); }
    ModifiersAndVisibility m = cd.getMav();
    
    /* Make interfaces public by default? */
//    if (! Utilities.hasVisibilityModifier(m.getModifiers()) { 
//      _readAndWriteThroughIndex(m.getSourceInfo().getStartLine(), m.getSourceInfo().getStartColumn() - 1);
//      _writeToFileOut("public ");
//    }

    BracedBody bb = cd.getBody();
    sd.setAnonymousInnerClassNum(0);
    bb.visit(new Augmentor(sd));
    
    int baseIndent = cd.getSourceInfo().getStartColumn() - 1;
    _readAndWriteThroughIndex(cd.getSourceInfo().getEndLine(), cd.getSourceInfo().getEndColumn() - 1);
    for (String s : _endOfClassVarDefs) {
      _writeToFileOut(newLine + indentString(baseIndent, 1) + s);
    }
    if (_endOfClassVarDefs.size() > 0) {
      _writeToFileOut(newLine);
      _endOfClassVarDefs.clear();
    }
    _writeToFileOut(indentString(baseIndent, 0));
    // We don't bother visiting any of the signature nodes -- parameters, type, name, etc.
    return null;
  }

  /** Look up this inner interface in the enclosing data, and then visit its body with a new Augmentor.
    * No code augmentation is done since InnerInterfaces can only appear at the Advanced Level, and
    * we are not doing code augmentation at the Advanced Level.  If we were to start doing code augmentation 
    * at the Advanced level, this might need to change.
    * InnerInterfaceDefs can appear inside method or class or interface bodies.
    */
  public Void forInnerInterfaceDef(InnerInterfaceDef cd) {
    String interfaceName = cd.getName().getText();
    if (_enclosingData == null) {
      throw new RuntimeException("Internal Program Error: Enclosing Data is null.  Please report this bug.");
    }
    SymbolData sd = _enclosingData.getInnerClassOrInterface(interfaceName);
    if (sd == null) { 
      throw new RuntimeException("Internal Program Error: Can't find SymbolData for " + cd.getName().getText() + 
                                 ". Please report this bug."); 
    }

    BracedBody bb = cd.getBody();
    sd.setAnonymousInnerClassNum(0);
    bb.visit(new Augmentor(sd));
    
    // We don't bother visiting any of the signature nodes -- parameters, type, name, etc.
    return null;
  }
  
  
  /* Look up this Anonymous Inner Class inside its enclosing data, and then visit its body.  Then, if it
   * is from an Elementary or Intermediate Level file, augment with the necessary automatically generated methods.
   * @param e  The AnonymousClassInstantiation we are augmenting.
   */
  public Void forAnonymousClassInstantiation(AnonymousClassInstantiation e) {
    SymbolData sd = _enclosingData.getNextAnonymousInnerClass();
//    _log.log("Augmenting anonymous class " + e + " with SymbolData " + sd);
    if (sd == null) {
      throw new RuntimeException("Internal Program Error: Couldn't find the SymbolData for the anonymous inner class." +
                                 "  Please report this bug.");
    }
    BracedBody bb = e.getBody();
    sd.setAnonymousInnerClassNum(0);
    bb.visit(new Augmentor(sd));
    
    int baseIndent = e.getSourceInfo().getStartColumn() - 1;
    _readAndWriteThroughIndex(e.getSourceInfo().getEndLine(), e.getSourceInfo().getEndColumn() - 1);
//    if (_isElementaryFile() || _isIntermediateFile()) {
    String className = Data.dollarSignsToDots(e.getType().getName());
    writeAccessors(sd, baseIndent);
    String valueToStringName = writeValueToString(sd, baseIndent);
    String valueEqualsName = writeValueEquals(sd, baseIndent);
    String valueHashCodeName = writeValueHashCode(sd, baseIndent, valueEqualsName);
    writeToString(sd, baseIndent, valueToStringName);
    if (!_safeSupportCode) { writeAnonEquals(baseIndent);}
    else { writeEquals(className, sd, baseIndent, valueEqualsName); }
    writeHashCode(className, sd, baseIndent, true, valueHashCodeName);
    _writeToFileOut(indentString(baseIndent, 0));
    
//    }
    return null;
  }
  
  /**Delegate to for AnonymousClassInstantiation(e).*/
  public Void forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation e) {
    forAnonymousClassInstantiation(e);
    return null;
  }
  
  /** Visit the encosing part of this ComplexAnonymousClass name, and then delegate to 
    * forAnonymousClassInstantiation(e). */
  public Void forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation e) {
    e.getEnclosing().visit(this);
    forAnonymousClassInstantiation(e);
    return null;
  }

  /** Sort the Class and Interface defs based on the order they appear in the file.  Then visit each in turn.
    * Finally, write whatever remains in the file.  (If this is an ElementaryLevel file that needs to import
    * junit.framework.TestCase, make this the very first line of the augmented file.  This is okay, because at the
    * ElementaryLevel, there are no package statements we might get in trouble with.
    */
  public Void forSourceFile(SourceFile sf) {
    TypeDefBase[] cds = sf.getTypes();
    
    // We intentionally neglect to visit the package and import statements

    //Visit each class and interface def in turn.
    for (TypeDefBase cd : cds) { cd.visit(this); }
 
    //Write out whatever is left in the file.
//    _log.log("Processed source file through line " + _fileInLine + " and column " + _fileInColumn);
//    _log.log("Now dumping remainder of file through line " + sf.getSourceInfo().getEndLine());
    //_readAndWriteThroughIndex(sf.getSourceInfo().getEndLine(), sf.getSourceInfo().getEndColumn());
    String remainder = _readThroughIndex(sf.getSourceInfo().getEndLine(), sf.getSourceInfo().getEndColumn());
    if (!remainder.endsWith(newLine)) remainder = remainder + newLine; // make sure file ends in a newLine
    _writeToFileOut(remainder, true);
    return null;
  }
  
  /** Convert the provided FormalParameter array into an array of TypeData corresponding
    * to the types of the FormalParameters.
    */
  protected static TypeData[] formalParameters2TypeDatas(FormalParameter[] fps, SymbolData enclosing) { 
    TypeData[] tds = new TypeData[fps.length];
    int j = 0;
    for (FormalParameter fp: fps) {
      SymbolData type = _llv.getSymbolData(fp.getDeclarator().getType().getName(), fp.getSourceInfo());
      
      if (type == null) {
        //see if this is a partially qualified field reference
        type = enclosing.getInnerClassOrInterface(fp.getDeclarator().getType().getName());
      }
      
      tds[j]= type; j++;  // store type in next empty slot of tds
    }
    assert j == fps.length;
    
    return tds;
  }
  
  
  /**
   * Write a constructor for a previously generated (determined by MethodData.isGenerated())
   * constructor, if one exists.
   * 
   * @param sd  The method's enclosing class.
   * @param baseIndent  The base indent level (number of spaces).
   */
  protected static void writeConstructor(String className, SymbolData sd, int baseIndent) {
    // Find the constructor.  There should be at most one that we generated, so select that one.
    MethodData constructor = null;
    for (MethodData currMd : sd.getMethods()) {
      if (currMd.getName().equals(LanguageLevelVisitor.getUnqualifiedClassName(sd.getName())) && currMd.isGenerated()) {
        constructor = currMd;
        break;
      }
    }
    if (constructor == null) return;
    
    
    // Write the method signature.
    VariableData[] constructorParams = constructor.getParams();
    
    _writeToFileOut(newLine + indentString(baseIndent, 1) + 
                    "/** This method is automatically generated by the Language Level Converter. */" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "public " + className + "(");
    for (int q = 0; q < constructorParams.length; q++) {
      if (q>0) {
        _writeToFileOut(", ");
      }
      VariableData vd = constructorParams[q];
      _writeToFileOut(Data.dollarSignsToDots(vd.getType().getName()) + " " + vd.getName());
    }
    
    _writeToFileOut(") {" + newLine);
    

    // Generate lists of super params & local params
    LinkedList<VariableData> superParams = new LinkedList<VariableData>();
    LinkedList<VariableData> localParams = new LinkedList<VariableData>();
    LinkedList<VariableData> localFields = sd.getVars();
    
    for (VariableData param : constructorParams) {
      boolean hasLocalField = false;
      for (VariableData field : localFields) {
        hasLocalField = hasLocalField || field.getName().equals(param.getName());
        if (hasLocalField) { break; }
      }
      
      if (hasLocalField) { localParams.add(param); }
      else if (param.getName().startsWith("super_")) { superParams.add(param); }
      else { 
        throw new RuntimeException("Internal Program Error: Unexpected parameter name in generated constructor: " + 
                                   param.getName() +".  Please report this bug"); 
      }
    }
    
    // Add the call to the super constructor here.
    _writeToFileOut(indentString(baseIndent, 2) + "super(");
    
    for (int z = 0; z < superParams.size(); z++) {
      if (z > 0) {
        _writeToFileOut(", ");
      }
      _writeToFileOut(superParams.get(z).getName());
    }
    _writeToFileOut(");" + newLine);
    
    // Instantiate local fields.
    for (int i = 0; i < localParams.size(); i++) {
      String varName = localParams.get(i).getName();
      _writeToFileOut(indentString(baseIndent, 2) + "this." + varName + " = " + varName + ";" + newLine);
    }
    _writeToFileOut(indentString(baseIndent, 1) + "}" + newLine);  
  }
  
  /**
   * Write an accessor method for each previously generated (determined by MethodData.isGenerated())
   * accessor.
   * 
   * @param sd  The method's enclosing class.
   * @param baseIndent  The base indent level (number of spaces).
   */
  protected static void writeAccessors(SymbolData sd, int baseIndent) {
    // Find the accessor methods and generate them if we had to create them ourselves.
    LinkedList<MethodData> methods = sd.getMethods();
    MethodData accessor = null;
    Iterator<MethodData> iter = methods.iterator();
    VariableData[] vars = sd.getVars().toArray(new VariableData[sd.getVars().size()]);
    for (int i = 0; i < vars.length; i++) {
      accessor = null;
      iter = methods.iterator();
      while (iter.hasNext()) {
        MethodData currMd = iter.next();
        if (currMd.getName().equals(vars[i].getName()) && currMd.isGenerated()) {
          accessor = currMd;
          break;
        }
      }
      if (accessor != null) {
        _writeToFileOut(newLine + indentString(baseIndent, 1) + 
                        "/** This method is automatically generated by the Language Level Converter. */" + newLine);
        _writeToFileOut(indentString(baseIndent, 1) + "public " + 
                        Data.dollarSignsToDots(vars[i].getType().getName()) + " " + 
                        LanguageLevelVisitor.getFieldAccessorName(vars[i].getName()) + "() {" + newLine);
        _writeToFileOut(indentString(baseIndent, 2) + "return " + vars[i].getName() + ";" + newLine + 
                        indentString(baseIndent, 1) + "}" + newLine);
      }
    }
  }
  
  /**
   * Write a toString method that prints out each field with a visible accessor.
   * 
   * @param sd  The method's enclosing class.
   * @param baseIndent  The base indent level (number of spaces).
   * @param valueToStringName  The name of the generated valueToString method
   */
  protected static void writeToString(SymbolData sd, int baseIndent, String valueToStringName) {
    LinkedList<MethodData> methods = sd.getMethods();
    MethodData toString = null;
    Iterator<MethodData> iter = methods.iterator();
    // Find the toString method and generate it if we had to create it ourselves.
    while (iter.hasNext()) {
      MethodData currMd = iter.next();
      if (currMd.getName().equals("toString") && currMd.isGenerated()) {
        toString = currMd;
        break;
      }
    }
    if (toString == null) return;
    // Builds a list of MethodData accessors for VariableDatas of this class.
    LinkedList<MethodData> allMds = _getVariableAccessorListHelper(sd);
    MethodData[] mds = allMds.toArray(new MethodData[allMds.size()]);

    if (_safeSupportCode) { writeSafeToString(sd, baseIndent, valueToStringName, mds); }
    else { writeSimpleToString(sd, baseIndent, valueToStringName, mds); }
  }
    
  /** Helper to writeToString; writes a toString that handles infinitely-recursive data structures. */
  protected static void writeSafeToString(SymbolData sd, int baseIndent, String valueToStringName, 
                                          MethodData[] accessors) {
    
    String flagName = sd.createUniqueName("__toStringFlag");
    VariableData toStringFlag = new VariableData(flagName, 
                                                 new ModifiersAndVisibility(NONE, 
                                                                            new String[]{ "private", "static" }),
                                 _llv.getQualifiedSymbolData("java.util.LinkedList", SourceInfo.NONE, false),
                                 true, sd);
    toStringFlag.setGenerated(true);
    sd.addVar(toStringFlag);
    
    _writeToFileOut(newLine + indentString(baseIndent, 1) + 
                    "/** This field is automatically generated by the Language Level Converter. */" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "private boolean " + flagName + " = false;" + newLine);
    _writeToFileOut(newLine + indentString(baseIndent, 1) +
                    "/** This method is automatically generated by the Language Level Converter. */" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "public java.lang.String toString() {" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "if (" + flagName + ") {" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "return getClass().getName() + \"(...)\";" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "else {" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + flagName + " = true;" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "String result;" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "try {" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "result = getClass().getName() + \"(\" + " + newLine);
    for (int i = 0; i < accessors.length; i++) {
      if (LanguageLevelConverter.OPT.javaVersion().supports(JavaVersion.JAVA_5) ||
            ! accessors[i].getReturnType().getSymbolData().isPrimitiveType()) {
        
        _writeToFileOut(indentString(baseIndent, 6) + valueToStringName + "(" + accessors[i].getName() + "()) + ");
      }
      else {
        _writeToFileOut(indentString(baseIndent, 6) + accessors[i].getName() + "() + ");
      }

      if (i < accessors.length - 1) {
        _writeToFileOut("\", \" + ");
      }
      _writeToFileOut(newLine);
    }
    _writeToFileOut(indentString(baseIndent, 6) + "\")\";" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "catch (RuntimeException e) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + flagName + " = false;" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "throw e;" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + flagName + " = false;" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "return result;" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "}" + newLine);
  }
  
  /** Helper to writeToString; writes a short toString that does not handle infinitely-recursive data structures. */
  protected static void writeSimpleToString(SymbolData sd, int baseIndent, String valueToStringName,
                                            MethodData[] accessors) {
    
    _writeToFileOut(newLine + indentString(baseIndent, 1) + 
                    "/** This method is automatically generated by the Language Level Converter. */" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "public java.lang.String toString() {" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "return getClass().getName() + \"(\" + " + newLine);
    for (int i = 0; i < accessors.length; i++) {
        _writeToFileOut(indentString(baseIndent, 4) + accessors[i].getName() + "() + ");

      if (i < accessors.length - 1) {
        _writeToFileOut("\", \" + ");
      }
      _writeToFileOut(newLine);
    }
    _writeToFileOut(indentString(baseIndent, 4) + "\")\";" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "}" + newLine);
  }
  
  /**
   * Write an equals method that requires the Object parameter to be an instanceof this type
   * and have matching fields for each of the fields with visible accessors.
   * 
   * @param className  The unqualified name of this method's class or, in the case of anonymous inner classes, the
   *                   name of its superclass (or implemented interface).
   * @param sd  The method's enclosing class.
   * @param baseIndent  The base indent level (number of spaces).
   * @param valueEqualsName  The name of the generated valueEquals method
   */
  protected static void writeEquals(String className, SymbolData sd, int baseIndent, String valueEqualsName) {
    LinkedList<MethodData> methods = sd.getMethods();
    MethodData equals = null;
    Iterator<MethodData> iter = methods.iterator();

    // Find the equals method and generate it if we had to create it ourselves.
    while (iter.hasNext()) {
      MethodData currMd = iter.next();
      if (currMd.getName().equals("equals") && currMd.isGenerated()) {
        equals = currMd;
        break;
      }
    }
    if (equals == null) return;

    LinkedList<MethodData> allMds = _getVariableAccessorListHelper(sd);
    MethodData[] mds = allMds.toArray(new MethodData[allMds.size()]);

    if (_safeSupportCode) { writeSafeEquals(className, sd, baseIndent, valueEqualsName, mds); }
    else { writeSimpleEquals(className, sd, baseIndent, valueEqualsName, mds); }
  }
    
  /** Helper to writeEquals; writes an equals that handles infinitely-recursive data structures. */
  protected static void writeSafeEquals(String className, SymbolData sd, int baseIndent, String valueEqualsName, 
                                        MethodData[] accessors) {
    
    String listName = sd.createUniqueName("__equalsList");
    
    VariableData equalsList = 
      new VariableData(listName, new ModifiersAndVisibility(SourceInfo.NONE, new String[]{ "private", "static" }),
                       _llv.getQualifiedSymbolData("java.util.LinkedList", SourceInfo.NONE, false),
                       true, sd);
    equalsList.setGenerated(true);
    sd.addVar(equalsList);
    
    _writeToFileOut(newLine + indentString(baseIndent, 1) + 
                    "/** This field is automatically generated by the Language Level Converter. */");
    if (LanguageLevelConverter.OPT.javaVersion().supports(JavaVersion.JAVA_5))
      _writeToFileOut(newLine + indentString(baseIndent, 1) + "private java.util.LinkedList<" + className + "> " +
                      listName + " = new java.util.LinkedList<" + className + ">();" + newLine);
    else
      _writeToFileOut(newLine + indentString(baseIndent, 1) + "private java.util.LinkedList " + listName + 
                      " = new java.util.LinkedList();" + newLine + newLine);

    _writeToFileOut(newLine + indentString(baseIndent, 1) + 
                    "/** This method is automatically generated by the Language Level Converter. */" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "public boolean equals(java.lang.Object o) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "if (this == o) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "return true;" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "else if ((o == null) || (! o.getClass().equals(getClass()))) {" +
                    newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "return false;" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "else {" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "boolean alreadyTested = false;" + newLine);
    if (LanguageLevelConverter.OPT.javaVersion().supports(JavaVersion.JAVA_5)) {
      _writeToFileOut(indentString(baseIndent, 3) + "for (" + className + " element : " + listName + ")" + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "alreadyTested = alreadyTested || (o == element);" + newLine + 
                      newLine);
    }
    else {
      if (LanguageLevelConverter.OPT.javaVersion().supports(JavaVersion.JAVA_5)) {
        _writeToFileOut(indentString(baseIndent, 3) + "java.util.Iterator<" + className + "> i = " + listName + 
                        ".iterator();" + newLine);
      }
      else {
        _writeToFileOut(indentString(baseIndent, 3) + "java.util.Iterator i = " + listName + ".iterator();" + newLine);
      }
      _writeToFileOut(indentString(baseIndent, 3) + "while (!alreadyTested && i.hasNext())" + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "alreadyTested = alreadyTested || (o == i.next());" + newLine +
                      newLine);
    }
    _writeToFileOut(indentString(baseIndent, 3) + "if (alreadyTested) { " + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "return true;" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "else {" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + className + " cast = ((" + className + ") o);" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + listName + ".addLast(cast);" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "boolean result;" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "try {" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "result = ");
    int variablesCompared = 0;
    for (int i = 0; i < accessors.length; i++) {
      if (variablesCompared > 0) {
        _writeToFileOut(" && " + newLine + indentString(baseIndent, 7));
      }
      variablesCompared++;
      
      String varName = accessors[i].getName() + "()";
      
      if (LanguageLevelConverter.OPT.javaVersion().supports(JavaVersion.JAVA_5) ||
            ! accessors[i].getReturnType().getSymbolData().isPrimitiveType()) {
        
        _writeToFileOut(valueEqualsName + "(" + varName + ", cast." + varName + ")");
      }
      else {
        _writeToFileOut("(" + varName + " == cast." + varName + ")");
      }
    }
  
    if (variablesCompared == 0)
      _writeToFileOut("true");
    
    _writeToFileOut(";" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "catch (RuntimeException e) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + listName + ".removeLast();" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "throw e;" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + listName + ".removeLast();" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "return result;" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "}" + newLine);
  }
  
  /** Helper to writeEquals; writes a simple equals that does not handle infinitely-recursive data structures. */
  protected static void writeSimpleEquals(String className, SymbolData sd, int baseIndent, String valueEqualsName, 
                                          MethodData[] accessors) {
    
    _writeToFileOut(newLine + indentString(baseIndent, 1) + 
                    "/** This method is automatically generated by the Language Level Converter. */" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "public boolean equals(java.lang.Object o) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "if (this == o) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "return true;" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "else if ((o == null) || (! o.getClass().equals(getClass()))) {" +
                    newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "return false;" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "else {" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + className + " cast = ((" + className + ") o);" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "return ");
    int variablesCompared = 0;
    for (int i = 0; i < accessors.length; i++) {
      if (variablesCompared > 0) {
        _writeToFileOut(" && " + newLine + indentString(baseIndent, 5));
      }
      variablesCompared++;
      
      String varName = accessors[i].getName() + "()";
      
      if (! accessors[i].getReturnType().getSymbolData().isPrimitiveType()) {
        
        _writeToFileOut("(" + varName + " != null && " + varName + ".equals(cast." + varName + "))");
      }
      else {
        _writeToFileOut("(" + varName + " == cast." + varName + ")");
      }
    }
  
    if (variablesCompared == 0)
      _writeToFileOut("true");
    
    _writeToFileOut(";" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "}" + newLine);
  }
  

  /** 
   * AnonymousClasses are only equal if they are identical. 
   */
  protected static void writeAnonEquals(int baseIndent) {
    
    _writeToFileOut(newLine + indentString(baseIndent, 1) + 
                    "/** This method is automatically generated by the Language Level Converter. */" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "public boolean equals(java.lang.Object o) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "return (this == o);" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "}" + newLine);
  }
  
  
  /**
   * Write a hashCode method that is consistent with the generated equals(Object) method.
   * 
   * @param className  The unqualified name of this method's class or, in the case of anonymous inner classes, the
   *                   name of its superclass (or implemented interface).
   * @param sd  The method's enclosing class.
   * @param baseIndent  The base indent level (number of spaces).
   * @param waitForVarDef  True iff static variables cannot be defined in the current context and should be deferred
   *                       by adding them to _endOfClassVarDefs.
   * @param valueHashCodeName  The name of the generated valueHashCode method
   */
  protected static void writeHashCode(String className, SymbolData sd, int baseIndent, boolean waitForVarDef, 
                                      String valueHashCodeName) {
    LinkedList<MethodData> methods = sd.getMethods();
    MethodData hashCode = null;
    Iterator<MethodData> iter = methods.iterator();

    // Find the hashCode method and generate it if we had to create it ourselves.
    while (iter.hasNext()) {
      MethodData currMd = iter.next();
      if (currMd.getName().equals("hashCode") && currMd.isGenerated()) {
        hashCode = currMd;
        break;
      }
    }
    if (hashCode == null) return;

    LinkedList<MethodData> allMds = _getVariableAccessorListHelper(sd);
    MethodData[] mds = allMds.toArray(new MethodData[allMds.size()]);
    
    if (_safeSupportCode) { writeSafeHashCode(className, sd, baseIndent, waitForVarDef, valueHashCodeName, mds); }
    else { writeSimpleHashCode(className, sd, baseIndent, waitForVarDef, valueHashCodeName, mds); }
  }
    
  /** Helper to writeHashCode; writes a hashCode that handles infinitely-recursive data structures.   
   * @param className  The unqualified name of this method's class or, in the case of anonymous inner classes, the
   *                   name of its superclass (or implemented interface).
   * @param sd  The method's enclosing class.
   * @param baseIndent  The base indent level (number of spaces).
   * @param waitForVarDef  True iff static variables cannot be defined in the current context and should be deferred
   *                       by adding them to _endOfClassVarDefs.
   * @param valueHashCodeName  The name of the generated valueHashCode method
   * @param accessors  An Array of the MethodDatas corresponding to the accessors for this class.
   */
  protected static void writeSafeHashCode(String className, SymbolData sd, int baseIndent, boolean waitForVarDef, 
                                          String valueHashCodeName, MethodData[] accessors) {
    
    String listName = "__hashCodeList";
    listName = sd.createUniqueName(listName);
    VariableData hashCodeList =
      new VariableData(listName, new ModifiersAndVisibility(SourceInfo.NONE, new String[]{ "private", "static" }),
                       _llv.getQualifiedSymbolData("java.util.LinkedList", SourceInfo.NONE, false),
                       true, sd);
    hashCodeList.setGenerated(true);
    
    if (waitForVarDef) {
      SymbolData outermostData = sd;
      while (outermostData.getOuterData() != null) {
        outermostData = outermostData.getOuterData().getSymbolData();
      }
      outermostData.addVar(hashCodeList);
      _endOfClassVarDefs.add("/** This field is automatically generated by the Language Level Converter. */");
      if (LanguageLevelConverter.OPT.javaVersion().supports(JavaVersion.JAVA_5)) {
        _endOfClassVarDefs.add("private static java.util.LinkedList<Object> " + listName + 
                               " = new java.util.LinkedList<Object>();");
      }
      else {
        _endOfClassVarDefs.add("private static java.util.LinkedList " + listName + " = new java.util.LinkedList();");
      }
      _endOfClassVarDefs.add("");
    }
    else {
      sd.addVar(hashCodeList);
      _writeToFileOut(newLine + indentString(baseIndent, 1) + 
                      "/** This field is automatically generated by the Language Level Converter. */");
      if (LanguageLevelConverter.OPT.javaVersion().supports(JavaVersion.JAVA_5)) {
        _writeToFileOut(newLine + indentString(baseIndent, 1) +
                        "private static java.util.LinkedList<" + className + "> " + listName + 
                        " = new java.util.LinkedList<" + className + ">();" + newLine);
      }
      else {
        _writeToFileOut(newLine + indentString(baseIndent, 1) + 
                        "private static java.util.LinkedList " + listName + " = new java.util.LinkedList();" + newLine);
      }
    }
    
    _writeToFileOut(newLine + indentString(baseIndent, 1) + 
                    "/** This method is automatically generated by the Language Level Converter. */");
    _writeToFileOut(newLine + indentString(baseIndent, 1) + "public int hashCode() {" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "if (" + listName + ".contains(this)) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "return -1;" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "else {" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + listName + ".addLast(this);" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "int result;" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "try {" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "result = getClass().hashCode()");
    for (int i = 0; i < accessors.length; i++) {
      _writeToFileOut(" ^ " + newLine + indentString(baseIndent, 6));
      SymbolData type = accessors[i].getReturnType().getSymbolData();
      
      if (LanguageLevelConverter.OPT.javaVersion().supports(JavaVersion.JAVA_5) ||
          ! type.isPrimitiveType()) {
        
        _writeToFileOut(valueHashCodeName + "(" + accessors[i].getName() + "())");
      }
      else if (type == SymbolData.BOOLEAN_TYPE) {
        _writeToFileOut("(" + accessors[i].getName() + "() ? 1 : 0)");
      }
      else if (type.isAssignableTo(SymbolData.INT_TYPE, LanguageLevelConverter.OPT.javaVersion())) {
        _writeToFileOut(accessors[i].getName() + "()");
      }
      else {
        _writeToFileOut("(int) " + accessors[i].getName() + "()");           
      }
    }
    
    _writeToFileOut(";" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "catch (RuntimeException e) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + listName + ".removeLast();" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "throw e;" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + listName + ".removeLast();" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "return result;" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "}" + newLine);
  }
  
  /** Helper to writeHashCode; writes a simple hashCode that does not handle infinitely-recursive data structures. 
   * @param className  The unqualified name of this method's class or, in the case of anonymous inner classes, the
   *                   name of its superclass (or implemented interface).
   * @param sd  The method's enclosing class.
   * @param baseIndent  The base indent level (number of spaces).
   * @param waitForVarDef  True iff static variables cannot be defined in the current context and should be deferred
   *                       by adding them to _endOfClassVarDefs.
   * @param valueHashCodeName  The name of the generated valueHashCode method
   * @param accessors  An Array of the MethodDatas corresponding to the accessors for this class.
   */
  protected static void writeSimpleHashCode(String className, SymbolData sd, int baseIndent, boolean waitForVarDef, 
                                            String valueHashCodeName, MethodData[] accessors) {
    
    _writeToFileOut(newLine + indentString(baseIndent, 1) + 
                    "/** This method is automatically generated by the Language Level Converter. */");
    _writeToFileOut(newLine + indentString(baseIndent, 1) + "public int hashCode() {" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "return getClass().hashCode()");
    for (int i = 0; i < accessors.length; i++) {
      _writeToFileOut(" ^ " + newLine + indentString(baseIndent, 4));
      SymbolData type = accessors[i].getReturnType().getSymbolData();
      
      if (! type.isPrimitiveType()) {
        _writeToFileOut("(" + accessors[i].getName() + "() == null ? 0 : " + accessors[i].getName() + "().hashCode())");
      }
      else if (type == SymbolData.BOOLEAN_TYPE) {
        _writeToFileOut("(" + accessors[i].getName() + "() ? 1 : 0)");
      }
      else if (type.isAssignableTo(SymbolData.INT_TYPE, LanguageLevelConverter.OPT.javaVersion())) {
        _writeToFileOut(accessors[i].getName() + "()");
      }
      else {
        _writeToFileOut("(int) " + accessors[i].getName() + "()");           
      }
    }
    
    _writeToFileOut(";" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "}" + newLine);
  }
  
  /**
   * Write a method to generate a String for any Object, including arrays, nulls, and other reference types.
   * 
   * @param sd  The method's enclosing class.
   * @param baseIndent  The base indent level (number of spaces).
   * 
   * @return  The name of the generated valueToString method (__valueToString by default).
   */
  private static String writeValueToString(SymbolData sd, int baseIndent) {
    String methodName = sd.createUniqueMethodName("__valueToString");
    if (_safeSupportCode) { writeSafeValueToString(sd, baseIndent, methodName); }
    return methodName;
  }
  
  /** Helper to writeValueToString; writes a valueToString that correctly handles arbitrary arrays. 
   * @param sd  The method's enclosing class.
   * @param baseIndent  The base indent level (number of spaces to put at the beginning of every line).
   * @param methodName  The name of the generated valueToString method (__valueToString by default).
   */

  private static void writeSafeValueToString(SymbolData sd, int baseIndent, String methodName) {
    String[] primitiveTypes = new String[]{"byte[]", "short[]", "char[]", "int[]", "long[]", "float[]", "double[]",
      "boolean[]"};
    boolean useGenerics = LanguageLevelConverter.OPT.javaVersion().supports(JavaVersion.JAVA_5);
    
    _writeToFileOut(newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "/**" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + 
                    " * This method is automatically generated by the LanguageLevelConverter." + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + 
                    " * As a helper to toString(), it recursively generates a string for any object," + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + " * including nulls, arrays, and standard reference types." + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + " */" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "private java.lang.String " + methodName + "(java.lang.Object o) {" +
                    newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "class ArrayToString {" + newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "public String valueFor(java.lang.Object o) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "if (o instanceof java.lang.Object[]) {" + newLine);

    if (useGenerics) {
      _writeToFileOut(indentString(baseIndent, 5) + 
                      "return arrayToString((java.lang.Object[]) o, new java.util.HashSet<java.lang.Object[]>());" + 
                      newLine);
    }
    else {
      _writeToFileOut(indentString(baseIndent, 5) + 
                      "return arrayToString((java.lang.Object[]) o, new java.util.HashSet());" + newLine);
    }
    _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
    
    for (String type : primitiveTypes) {
      _writeToFileOut(indentString(baseIndent, 4) + "else if (o instanceof " + type + ") {" + newLine);
      _writeToFileOut(indentString(baseIndent, 5) + "return arrayToString((" + type + ") o);" + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
    }
    
    _writeToFileOut(indentString(baseIndent, 4) + "else {" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "// o should be an array, but if not, toString() is called" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "return o.toString();" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
    
    _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine + newLine);
    
    for (String type : primitiveTypes) {
      _writeToFileOut(indentString(baseIndent, 3) + "public java.lang.String arrayToString(" + type + " array) {" +
                      newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "java.lang.StringBuffer result = new java.lang.StringBuffer();" +
                      newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "result.append(\"[\");" + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "if (array.length > 0) { result.append(array[0]); }" + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "for (int i = 1; i < array.length; i++) {" + newLine);
      _writeToFileOut(indentString(baseIndent, 5) + "result.append(\", \");" + newLine);
      _writeToFileOut(indentString(baseIndent, 5) + "result.append(array[i]);" + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "result.append(\"]\");" + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "return result.toString();" + newLine);
      _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine + newLine);
    }
    
    if (useGenerics) {
      _writeToFileOut(indentString(baseIndent, 3) + 
                      "public java.lang.String arrayToString(java.lang.Object[] array, " + 
                      "java.util.HashSet<java.lang.Object[]> alreadyPrinted) {" + newLine);
    }
    else {
      _writeToFileOut(indentString(baseIndent, 3) + 
                      "public java.lang.String arrayToString(java.lang.Object[] array, " + 
                      "java.util.HashSet alreadyPrinted) {" + newLine);
    }
    _writeToFileOut(indentString(baseIndent, 4) + 
                    "if (alreadyPrinted.contains(array)) { return (\"[...]\"); }" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "else { alreadyPrinted.add(array); }" + newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + 
                    "java.lang.StringBuffer result = new java.lang.StringBuffer();" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "result.append(\"[\");" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "boolean nonEmpty = false;" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "for (int i = 0; i < array.length; i++) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "if (nonEmpty) { result.append(\", \"); }" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "nonEmpty = true;" + newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "if (array[i] instanceof java.lang.Object[]) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 6) + 
                    "result.append(arrayToString((java.lang.Object[]) array[i], alreadyPrinted));" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "else {" + newLine);
    _writeToFileOut(indentString(baseIndent, 6) + "result.append(" + methodName + "(array[i]));" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "result.append(\"]\");" + newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "alreadyPrinted.remove(array);" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "return result.toString();" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine + newLine);
    
    _writeToFileOut(indentString(baseIndent, 2) + "}" + newLine + newLine); // end of inner class
    _writeToFileOut(indentString(baseIndent, 2) + "if (o == null) { return \"\" + null; }" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + 
                    "else if (o.getClass().isArray()) { return new ArrayToString().valueFor(o); }" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "else { return o.toString(); }" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "}" + newLine);
  }
  
  /** Helper to writeValueToString; writes a simple valueToString that does not handle arrays. 
   *  NOTE: This is currently unused.  For the simple case, no valueToString method is generated.
   * @param sd  The method's enclosing class.
   * @param baseIndent  The base indent level (number of spaces).
   */
  private static void writeSimpleValueToString(SymbolData sd, int baseIndent, String methodName) {
    _writeToFileOut(newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "/**" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + 
                    " * This method is automatically generated by the LanguageLevelConverter." + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + 
                    " * As a helper to toString(), it generates a string for any object," + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + " * including nulls and standard reference types." + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + " */" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "private java.lang.String " + methodName + "(java.lang.Object o) {" + 
                    newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "if (o == null) { return \"\" + null; }" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "else { return o.toString(); }" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "}" + newLine);
  }
  
  /**
   * Write a method to compare any two objects, including arrays, nulls, and other reference types.
   * 
   * @param sd  The method's enclosing class.
   * @param baseIndent  The base indent level (number of spaces).
   * 
   * @return  The name of the generated valueEquals method (__valueEquals by default).
   */
  private static String writeValueEquals(SymbolData sd, int baseIndent) {
    String methodName = sd.createUniqueMethodName("__valueEquals");
    if (_safeSupportCode) { writeSafeValueEquals(sd, baseIndent, methodName); }
//    else { writeSimpleValueEquals(sd, baseIndent, methodName); }
    return methodName;
  }
  
  /** Helper to writeValueEquals; writes a valueEquals that correctly handles arbitrary arrays. */
  private static void writeSafeValueEquals(SymbolData sd, int baseIndent, String methodName) {
    String[] primitiveTypes = new String[]{"byte[]", "short[]", "char[]", "int[]", "long[]", "float[]", "double[]", 
      "boolean[]"};
    boolean useGenerics = LanguageLevelConverter.OPT.javaVersion().supports(JavaVersion.JAVA_5);
    
    _writeToFileOut(newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "/**" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + 
                    " * This method is automatically generated by the LanguageLevelConverter." + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + 
                    " * As a helper to equals(Object), it recursively compares any two objects," + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + 
                    " * including nulls, arrays, and standard reference types." + newLine);
     _writeToFileOut(indentString(baseIndent, 1) + " */" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "private boolean " + methodName + 
                    "(java.lang.Object o1, java.lang.Object o2) {" + newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "class ArrayEquals {" + newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + 
                    "public boolean valueFor(java.lang.Object o1, java.lang.Object o2) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + 
                    "if (o1 instanceof java.lang.Object[] && o2 instanceof java.lang.Object[]) {" + newLine);
    if (useGenerics) {
      _writeToFileOut(indentString(baseIndent, 5) + "return arrayEquals((java.lang.Object[]) o1, " + 
                      "(java.lang.Object[]) o2, new java.util.HashSet<java.lang.Object>());" + newLine);
    }
    else {
      _writeToFileOut(indentString(baseIndent, 5) + "return arrayEquals((java.lang.Object[]) o1, " + 
                      "(java.lang.Object[]) o2, new java.util.HashSet());" + newLine);
    }
    _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
    
    for (String type : primitiveTypes) {
      _writeToFileOut(indentString(baseIndent, 4) + "else if (o1 instanceof " + type + " && o2 instanceof " + 
                      type + ") {" + newLine);
      _writeToFileOut(indentString(baseIndent, 5) + "return arrayEquals((" + type + ") o1, (" + type + ") o2);" + 
                      newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
    }
    
    _writeToFileOut(indentString(baseIndent, 4) + "else {" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "// o1 and o2 should be arrays, but if not, " + 
                    "or if they have different types, equals(Object) is called" + newLine); 
    _writeToFileOut(indentString(baseIndent, 5) + "return o1.equals(o2);" + newLine); 
    _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
    
    _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine + newLine);
    
    for (String type : primitiveTypes) {
      _writeToFileOut(indentString(baseIndent, 3) + "public boolean arrayEquals(" + type + " array1, " + type + 
                      " array2) {" + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "if (array1.length != array2.length) { return false; }" +
                      newLine + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "else {" + newLine);
      _writeToFileOut(indentString(baseIndent, 5) + "for (int i = 0; i < array1.length; i++) {" + newLine);
      _writeToFileOut(indentString(baseIndent, 6) + "if (array1[i] != array2[i]) { return false; }" + newLine);
      _writeToFileOut(indentString(baseIndent, 5) + "}" + newLine);
      _writeToFileOut(indentString(baseIndent, 5) + "return true;" + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
      _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine + newLine);
    }
    
    if (useGenerics) {
      _writeToFileOut(indentString(baseIndent, 3) + "public boolean arrayEquals(final java.lang.Object[] array1, " + 
                      "final java.lang.Object[] array2, java.util.HashSet<java.lang.Object> alreadyCompared) {" + 
                      newLine + newLine);
    }
    else {
      _writeToFileOut(indentString(baseIndent, 3) + "public boolean arrayEquals(final java.lang.Object[] array1," +
                      " final java.lang.Object[] array2, java.util.HashSet alreadyCompared) {" + newLine + newLine);
    }
    _writeToFileOut(indentString(baseIndent, 4) + "class ArrayPair {" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "public java.lang.Object[] array1() { return array1; }" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "public java.lang.Object[] array2() { return array2; }" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "public boolean equals(java.lang.Object o) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 6) + "if ((o == null) || ! (o instanceof ArrayPair)) { return false; }" + 
                    newLine);
    _writeToFileOut(indentString(baseIndent, 6) + 
                    "else { return (array1.equals(((ArrayPair) o).array1())) && " + 
                    "(array2.equals(((ArrayPair) o).array2())); }" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + 
                    "public int hashCode() { return array1.hashCode() ^ (array2.hashCode() << 1); }" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + 
                    "if (array1.length != array2.length) { return false; }" + newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "else {" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "ArrayPair currentPair = new ArrayPair();" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "if (alreadyCompared.contains(currentPair)) { return true; }" + 
                    newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "alreadyCompared.add(currentPair);" + newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "boolean result = true;" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "for (int i = 0; i < array1.length; i++) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 6) + 
                    "if (array1[i] instanceof java.lang.Object[] && array2[i] instanceof java.lang.Object[]) {" + 
                    newLine);
    _writeToFileOut(indentString(baseIndent, 7) + 
                    "result = arrayEquals((java.lang.Object[]) array1[i], " + 
                    "(java.lang.Object[]) array2[i], alreadyCompared);" + 
                    newLine);
    _writeToFileOut(indentString(baseIndent, 6) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 6) + "else {" + newLine);
    _writeToFileOut(indentString(baseIndent, 7) + "result = " + methodName + "(array1[i], array2[i]);"+ newLine);
    _writeToFileOut(indentString(baseIndent, 6) + "}" + newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 6) + "if (!result) { break; }" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "}" + newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "alreadyCompared.remove(currentPair);" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "return result;" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine + newLine);

    _writeToFileOut(indentString(baseIndent, 2) + "}" + newLine + newLine); // end of inner class
    _writeToFileOut(indentString(baseIndent, 2) + "if (o1 == null) { return o2 == null; }" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "else if (o2 == null) { return false; }" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + 
                    "else if (o1.getClass().isArray() && o2.getClass().isArray()) " +
                    "{ return new ArrayEquals().valueFor(o1, o2); }" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "else { return o1.equals(o2); }" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "}" + newLine);
  }
  
  /** Helper to writeValueEquals; writes a simple valueEquals that does not handle arrays. 
   *  NOTE: This is currently unused.  For the simple case, no valueEquals method is generated.
   */
  private static void writeSimpleValueEquals(SymbolData sd, int baseIndent, String methodName) {
    _writeToFileOut(newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "/**" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + 
                    " * This method is automatically generated by the LanguageLevelConverter." + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + 
                    " * As a helper to equals(Object), it compares any two objects," + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + " * including nulls and standard reference types." + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + " */" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "private boolean " +
                    methodName + "(java.lang.Object o1, java.lang.Object o2) {" + newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "if (o1 == null) { return o2 == null; }" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "else if (o2 == null) { return false; }" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "else { return o1.equals(o2); }" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "}" + newLine);
  }
  
  /** Write a method to generate a hash code for any Object, including arrays, nulls, and other reference types. 
    * @param sd  The method's enclosing class.
    * @param baseIndent  The base indent level (number of spaces).
    * @param valueEqualsName  The name of the method generated by writeValueEquals()
    * 
    * @return  The name of the generated valueHashCode method (__valueHashCode by default).
    */
  private static String writeValueHashCode(SymbolData sd, int baseIndent, String valueEqualsName) {
    String methodName = sd.createUniqueMethodName("__valueHashCode");
    if (_safeSupportCode) { writeSafeValueHashCode(sd, baseIndent, valueEqualsName, methodName); }
//    else { writeSimpleValueHashCode(sd, baseIndent, valueEqualsName, methodName); }
    return methodName;
  }
  
  /** Helper to writeValueHashCode; writes a valueHashCode that correctly handles arbitrary arrays. */
  private static void writeSafeValueHashCode(SymbolData sd, int baseIndent, String valueEqualsName, String methodName) {
    String[] primitiveTypes =
      new String[]{"byte[]", "short[]", "char[]", "int[]", "long[]", "float[]", "double[]", "boolean[]"};
    boolean useGenerics = LanguageLevelConverter.OPT.javaVersion().supports(JavaVersion.JAVA_5);
    
    _writeToFileOut(newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "/**" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + 
                    " * This method is automatically generated by the LanguageLevelConverter." + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + 
                    " * As a helper to hashCode(), it recursively generates a hash code for any object," + newLine);
    _writeToFileOut(indentString(baseIndent, 1) +
                    " * including nulls, arrays, and standard reference types." + newLine);
     _writeToFileOut(indentString(baseIndent, 1) + " */" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "private int " + methodName + "(java.lang.Object o) {" + 
                    newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "class ArrayHashCode {" + newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "public int valueFor(java.lang.Object o) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "if (o instanceof java.lang.Object[]) {" + newLine);
    if (useGenerics) {
      _writeToFileOut(indentString(baseIndent, 5) + 
                      "return arrayHashCode((java.lang.Object[]) o, new java.util.LinkedList<java.lang.Object>());" + 
                      newLine);
    }
    else {
      _writeToFileOut(indentString(baseIndent, 5) + 
                      "return arrayHashCode((java.lang.Object[]) o, new java.util.LinkedList());" + newLine);
    }
    _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
    
    for (String type : primitiveTypes) {
      _writeToFileOut(indentString(baseIndent, 4) + "else if (o instanceof " + type + ") {" + newLine);
      _writeToFileOut(indentString(baseIndent, 5) + "return arrayHashCode((" + type + ") o);" + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
    }
    
    _writeToFileOut(indentString(baseIndent, 4) + "else {" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "// o should be an array, but if not, hashCode() is called" + 
                    newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "return o.hashCode();" + newLine); 
    _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine + newLine);
    
    for (String type : primitiveTypes) {
      _writeToFileOut(indentString(baseIndent, 3) + "public int arrayHashCode(" + type + " array) {" + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "int result = 0;" + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "for (int i = 0; i < array.length; i++) {" + newLine);
      if (type.equals("boolean[]")) {
        _writeToFileOut(indentString(baseIndent, 5) + "result = (result << 1) ^ (array[i] ? 1 : 0);" + newLine);
      }
      else {
        _writeToFileOut(indentString(baseIndent, 5) + "result = (result << 1) ^ (int) array[i];" + newLine);
      }
      _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine);
      _writeToFileOut(indentString(baseIndent, 4) + "return result;" + newLine);
      _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine + newLine);
    }
    
    if (useGenerics) {
      _writeToFileOut(indentString(baseIndent, 3) + "public int arrayHashCode(final java.lang.Object[] array, " + 
                      "final java.util.LinkedList<java.lang.Object> alreadyGenerated) {" + newLine + newLine);
    }
    else {
      _writeToFileOut(indentString(baseIndent, 3) + "public int arrayHashCode(final java.lang.Object[] array, " + 
                      "final java.util.LinkedList alreadyGenerated) {" + newLine + newLine);
    }
    _writeToFileOut(indentString(baseIndent, 4) + "class ArrayWrapper {" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "public java.lang.Object[] array() { return array; }" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "public boolean equals(java.lang.Object o) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 6) + "return (o != null) && (o instanceof ArrayWrapper)  && " + 
                    valueEqualsName + "(array, ((ArrayWrapper) o).array());" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + 
                    "public int hashCode() { return 0; } // This method should never be used -- " + 
                    "only here for consistency." + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine + newLine);

    _writeToFileOut(indentString(baseIndent, 4) + "ArrayWrapper currentWrapper = new ArrayWrapper();" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "if (alreadyGenerated.contains(currentWrapper)) { return -1; }" + 
                    newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "alreadyGenerated.addLast(currentWrapper);" + newLine + newLine);
    
    _writeToFileOut(indentString(baseIndent, 4) + "int result = 0;" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "for (int i = 0; i < array.length; i++) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "if (array[i] instanceof java.lang.Object[]) {" + newLine);
    _writeToFileOut(indentString(baseIndent, 6) +
                    "result = (result << 1) ^ (arrayHashCode((java.lang.Object[]) array[i], alreadyGenerated) >> 1);" +
                    newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "else {" + newLine);
    _writeToFileOut(indentString(baseIndent, 6) + "result = (result << 1) ^ " + methodName + "(array[i]);" + newLine);
    _writeToFileOut(indentString(baseIndent, 5) + "}" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "}" + newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "alreadyGenerated.removeLast();" + newLine);
    _writeToFileOut(indentString(baseIndent, 4) + "return result;" + newLine);
    _writeToFileOut(indentString(baseIndent, 3) + "}" + newLine + newLine);
    
    _writeToFileOut(indentString(baseIndent, 2) + "}" + newLine + newLine); // end of inner class
    _writeToFileOut(indentString(baseIndent, 2) + "if (o == null) { return 0; }" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + 
                    "else if (o.getClass().isArray()) { return new ArrayHashCode().valueFor(o); }" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "else { return o.hashCode(); }" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "}" + newLine);
  }
  
  /** Helper to writeValueHashCode; writes a valueHashCode that does not handle arrays. 
   *  NOTE: This is currently unused.  For the simple case, no valueHashCode method is generated.
   */
  private static void writeSimpleValueHashCode(SymbolData sd, int baseIndent, String valueEqualsName, 
                                               String methodName) {
    _writeToFileOut(newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "/**" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + 
                    " * This method is automatically generated by the LanguageLevelConverter." + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + 
                    " * As a helper to hashCode(), it generates a hash code for any object," + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + " * including nulls and standard reference types." + newLine);
     _writeToFileOut(indentString(baseIndent, 1) + " */" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "private int " + methodName + "(java.lang.Object o) {" + 
                    newLine + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "if (o == null) { return 0; }" + newLine);
    _writeToFileOut(indentString(baseIndent, 2) + "else { return o.hashCode(); }" + newLine);
    _writeToFileOut(indentString(baseIndent, 1) + "}" + newLine);
  }
  
  private static String indentString(int baseIndent, int indentCount) {
    int length = indentCount * indentWidth + baseIndent;
    StringBuffer result = new StringBuffer(length);
    for (int i = 0; i < length; i++) {
      result.append(' ');
    }
    return result.toString();
  }
  
//  private static boolean _isElementaryFile()   { return LanguageLevelConverter.isElementaryFile(_llv._file); }
//  private static boolean _isIntermediateFile() { return LanguageLevelConverter.isIntermediateFile(_llv._file); }
//  private static boolean _isAdvancedFile()     { return LanguageLevelConverter.isAdvancedFile(_llv._file); }
  
  private static LinkedList<MethodData> _getVariableAccessorListHelper(SymbolData currClass) {
    List<Pair<VariableData, MethodData>> accessorMappings = new Vector<Pair<VariableData, MethodData>>();
    LinkedList<SymbolData> classes = new LinkedList<SymbolData>();
    classes.add(currClass);
    
    // Gather all accessor methods that have a matching signature with their variables
    while (classes.size() > 0) {
      SymbolData tempSd = classes.removeFirst();
      if (LanguageLevelVisitor.isJavaLibraryClass(tempSd.getName())) { break; }
      
      for (int i = 0; i<tempSd.getVars().size(); i++) {
        VariableData tempVd = tempSd.getVars().get(i);
        MethodData md = tempSd.getMethod(tempVd.getName(), new TypeData[0]);
        if (md != null)
          accessorMappings.add(new Pair<VariableData, MethodData>(tempVd, md));
      }
      // Note that we don't need to check interface fields, because they are always static.
      
      // Insure that we traverse the superclass hierarchy before we traverse the outer class hierarchy    
      SymbolData superClass = tempSd.getSuperClass();
      if (superClass != null) classes.addFirst(superClass); 
      Data outerData = tempSd.getOuterData();
      if (outerData != null) { classes.addLast(outerData.getSymbolData()); }
    }
  
    // Eliminate those accessors that are inaccessible, that throw exceptions, that are static, that are shadowed, 
    // or that have a different return type
    LinkedList<MethodData> allMethods = new LinkedList<MethodData>();
    for (int i = accessorMappings.size() - 1; i >= 0; i--) {
      VariableData vd = accessorMappings.get(i).getFirst();
      MethodData md = accessorMappings.get(i).getSecond();
      boolean canSeeMethod =
        TypeChecker.checkAccess(new NullLiteral(SourceInfo.NONE), md.getMav(), md.getName(), 
                                       md.getSymbolData(), currClass, "method", false);
      //TODO: it is okay to throw Runtime exceptions or Errors) {
      if (canSeeMethod && (! md.hasModifier("static")) && (md.getThrown().length == 0) &&
          vd.getType().getSymbolData().isAssignableTo(md.getReturnType(), LanguageLevelConverter.OPT.javaVersion())) {
        boolean isShadowed = false;
        for (int j = i - 1; j >= 0; j--) {
          if (accessorMappings.get(j).getSecond().getName().equals(md.getName())) { isShadowed = true; break; }
        }
        if (!isShadowed) { allMethods.addFirst(md); }
      }
    }
    return allMethods;
  }

  /** Reads _fileIn through the given (line, column) returning this text.  On completion, the current cursor 
    * (_fileInLine, fileInColumn) is one character after (line, column).
    * @param line The line number to read through.
    * @param column The column to read to (or 0 to read to through the end of the previous line).
    */
  private static String _readThroughIndex(int line, int column) {
    if (_fileInLine > line || (_fileInLine == line && _fileInColumn - 1 > column)) {
      throw new RuntimeException("Internal Program Error: Attempt to read in " + _llv._file.getName() + 
                                 " at a point that is already past: line " + line + ", column " + column + 
                                 "; (currently at " + _fileInLine + ", " + _fileInColumn + ").  Please report this bug.");
    }
    
    try {
      StringBuffer result = new StringBuffer();
      while (_fileInLine < line) {
        String l = _fileIn.readLine();
        if (l == null) {
          _fileOut.flush();
          throw new RuntimeException("Internal Program Error: Attempt to read in " + _llv._file.getName() + 
                                     " past the end of file: line " + line + ", column " + column + "; (currently at " +
                                     _fileInLine + ", " + _fileInColumn + ").  Please report this bug.");
        }
        
        result.append(l).append(newLine);
        
        _fileInLine++;
        _fileInColumn = 1;
      }
      
      int lastLineLength = column - _fileInColumn + 1;
      char[] chars = new char[lastLineLength];
      int charsRead = _fileIn.read(chars, 0, lastLineLength);
      if (charsRead != lastLineLength) {
        _fileOut.flush();
        throw new RuntimeException("Internal Program Error: Attempt to read in " + _llv._file.getName() + 
                                   " past the end of file: line " + line + ", column " + column + "; (currently at " +
                                   _fileInLine + ", " + _fileInColumn + ").  Please report this bug.");
      }
      result.append(chars);
      _fileInLine = line;
      _fileInColumn = column + 1;
      return result.toString();
    }
    catch (IOException ioe) { throw new Augmentor.Exception(ioe); }
  }
    
  /** Reads _fileIn through the given line & column and write to output.  On completion, the current cursor is one 
    * character after (line, column).
    * @param line The line number to read through.
    * @param column The column to read to (or 0 to read to through the end of the previous line).
    */
  private static void _readAndWriteThroughIndex(int line, int column) {
    String text = _readThroughIndex(line, column);
    _writeToFileOut(text, true); // yes, writing straight from input
  }
  
  private static void _writeToFileOut(String s) { _writeToFileOut(s, false); }
  
  /** Write the string to _fileOut. If fromInput is true, the string is coming straight from the input file,
    * which means the corresponding line number should be incremented as well.
    * @param s The string to write.
    * @param fromInput true if the corresponding line number should be incremented as well */
  private static void _writeToFileOut(String s, boolean fromInput) {
    try {
      String[] lines = s.split(newLine, -1);
      for(int i=0; i<lines.length-1; ++i) {
        _fileOut.write(lines[i]);
        // add line number to map if it doesn't exist yet
        if (_lineNumberMap.get(_fileOutCorrespondingLine)==null) 
          _lineNumberMap.put(_fileOutCorrespondingLine, _fileOutLine);
        // end-of-line line number mapping; disabled since we output the entire map at the beginning of the file
        // _fileOut.write("//["+_fileOutCorrespondingLine+"]");
        _fileOut.write(newLine);
        ++_fileOutLine;
        if (fromInput) ++_fileOutCorrespondingLine; // true if we are copying straight from input
      }
      _fileOut.write(lines[lines.length-1]);
    }
    catch (IOException ioe) { throw new Augmentor.Exception(ioe); }
  }
  
  /** Reads _fileIn through the specified (line, column) but leaves the file cursor unchanged.
    * @param line The line number to read through.
    * @param column The column to read to (or 0 to read to through the end of the previous line).
    */
  private static String _peek(int line, int column) {
    
    try {
      _fileIn.mark(LanguageLevelConverter.INPUT_BUFFER_SIZE);
      // Save the cursor
      int fileInLine = _fileInLine;
      int fileInColumn = _fileInColumn;
      String text = _readThroughIndex(line, column);
      _fileIn.reset();
      // Reset the cursor
      _fileInLine = fileInLine;
      _fileInColumn = fileInColumn;
      return text;
      }
      catch (IOException ioe) { throw new Augmentor.Exception(ioe); }
  }
  
  /** Returns a copy of the line number map that maps original dj* line numbers
    * to generated java line numbers.
    * @return copy of line number map */
  public static SortedMap<Integer,Integer> getLineNumberMap() {
    return new TreeMap<Integer,Integer>(_lineNumberMap);
  }
  
  public static class MethodBodyAugmentor extends Augmentor {
    
    /** Mandatory forwarding constructor. */
    protected MethodBodyAugmentor(SymbolData enclosing) { super(enclosing); }
    
    /** Writes out implicit variableDeclarationModfiers that must be added to augmented file. */
    protected void augmentVariableDeclarationModifiers(VariableDeclaration that) { _writeToFileOut("final "); }
  }
  
  public static class Exception extends RuntimeException {
    public Exception(java.lang.Exception nested) { super(nested); }
  }
  
  /** Test class for the Augmentor class. */
  public static class AugmentorTest extends TestCase {

    public AugmentorTest() {
      this("");
    }
    public AugmentorTest(String name) {
      super(name);
    }
    
    private Augmentor _a;
    private Symboltable _s = LanguageLevelConverter.symbolTable; // Define a short synonym
    private File _f = new File("");
    
    public void setUp() {
      LanguageLevelVisitor llv =
        new IntermediateVisitor(_f, 
                                new LinkedList<Pair<String, JExpressionIF>>(), 
                                new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                new LinkedList<Command>(),
                                new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>());
      _a = new Augmentor(true, null, null, llv);
      LanguageLevelConverter.symbolTable.clear();
      Symboltable _s = LanguageLevelConverter.symbolTable;   
      LanguageLevelConverter.OPT = new Options(JavaVersion.JAVA_5, EmptyIterable.<File>make());
    }

    public void testFormalParameters2TypeDatas() {
      FormalParameter[] fp = new FormalParameter[0];
      TypeData[] result = formalParameters2TypeDatas(fp, _a._enclosingData);
      assertEquals("The result is empty", 0, result.length);
      
      PrimitiveType intt = new PrimitiveType(SourceInfo.NONE, "int");
      FormalParameter param = 
        new FormalParameter(SourceInfo.NONE,
                            new UninitializedVariableDeclarator(SourceInfo.NONE, intt, 
                                                                new Word(SourceInfo.NONE, "j")), false);
      SymbolData intData = SymbolData.INT_TYPE;
      _s.put("int", intData);
      
      ClassOrInterfaceType stringt = new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.String", new Type[0]);
      FormalParameter param2 =
        new FormalParameter(SourceInfo.NONE, 
                            new UninitializedVariableDeclarator(SourceInfo.NONE, stringt, 
                                                                new Word(SourceInfo.NONE, "j")), false);
      SymbolData stringData = new SymbolData("java.lang.String");
      _s.put("java.lang.String", stringData);

      fp = new FormalParameter[]{ param, param2 };
      result = formalParameters2TypeDatas(fp, _a._enclosingData);
      assertTrue("Arrays should be equal", 
                 LanguageLevelVisitor.arrayEquals(result, new TypeData[]{ intData, stringData }));
      
      UninitializedVariableDeclarator vd =
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new ClassOrInterfaceType(SourceInfo.NONE, "Inner", new Type[0]), 
                                            new Word(SourceInfo.NONE, "t"));
      //test an inner class
      FormalParameter param3 = 
        new FormalParameter(SourceInfo.NONE, vd, false);
      fp = new FormalParameter[] {param3};
      SymbolData inner = new SymbolData("Inner");
      inner.setIsContinuation(false);
      _a._enclosingData = new SymbolData("me");
      _a._enclosingData.addInnerClass(inner);
      result = formalParameters2TypeDatas(fp, _a._enclosingData);
      assertTrue("Arrays should be equal", LanguageLevelVisitor.arrayEquals(result, new TypeData[] {inner}));
    }


    public void testIndentString() {
      assertEquals("Should return a string of 0 tabs", "", indentString(0, 0));
      assertEquals("Should return a string of 6 tabs", "            ", indentString(2, 5));
    }

//    public void testIsElementaryFile() {
//      _llv = new ElementaryVisitor(new File("elementary.dj0"), new LinkedList<Pair<String, JExpressionIF>>(), _s, 
//                                   new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(), 
//                                   new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(), 
//                                   new Hashtable<SymbolData, LanguageLevelVisitor>());
//      assertTrue("This is an elementary file", _isElementaryFile());
//      _llv = new IntermediateVisitor(new File("intermediate.dj1"), 
//                                     new LinkedList<Pair<String, JExpressionIF>>(), _s, 
//                                     new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
//                                     new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(),
//                                     new Hashtable<SymbolData, LanguageLevelVisitor>());
//      assertFalse("This is an intermediate file", _isElementaryFile());
//      _llv = new AdvancedVisitor(new File("advanced.dj2"),
//                                 new LinkedList<Pair<String, JExpressionIF>>(), _s, 
//                                 new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
//                                 new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(),
//                                 new Hashtable<SymbolData, LanguageLevelVisitor>());
//      assertFalse("This is an advanced file", _isElementaryFile());
//      _llv = new ElementaryVisitor(new File("full.java"), 
//                                   new LinkedList<Pair<String, JExpressionIF>>(), _s, 
//                                   new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(), 
//                                   new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(), 
//                                   new Hashtable<SymbolData, LanguageLevelVisitor>());
//      assertFalse("This is a full file", _isElementaryFile());
//    }

//    public void testIsIntermediateFile() {
//      _llv = new ElementaryVisitor(new File("elementary.dj0"), 
//                                   new LinkedList<Pair<String, JExpressionIF>>(), _s,
//                                   new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
//                                   new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(),
//                                   new Hashtable<SymbolData, LanguageLevelVisitor>());
//      assertFalse("This is an elementary file", _isIntermediateFile());
//      _llv = new IntermediateVisitor(new File("intermediate.dj1"),
//                                     new LinkedList<Pair<String, JExpressionIF>>(), _s,
//                                     new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
//                                     new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(),
//                                     new Hashtable<SymbolData, LanguageLevelVisitor>());
//      assertTrue("This is an intermediate file", _isIntermediateFile());
//      _llv = new AdvancedVisitor(new File("advanced.dj2"),
//                                 new LinkedList<Pair<String, JExpressionIF>>(), _s, 
//                                 new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
//                                 new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(),
//                                 new Hashtable<SymbolData, LanguageLevelVisitor>());
//      assertFalse("This is an advanced file", _isIntermediateFile());
//      _llv = new ElementaryVisitor(new File("full.java"),
//                                   new LinkedList<Pair<String, JExpressionIF>>(), _s,
//                                   new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
//                                   new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(),
//                                   new Hashtable<SymbolData, LanguageLevelVisitor>());
//      assertFalse("This is a full file", _isIntermediateFile());
//    }
//
//    public void testIsAdvancedFile() {
//      _llv = new ElementaryVisitor(new File("elementary.dj0"),
//                                   new LinkedList<Pair<String, JExpressionIF>>(), _s, 
//                                   new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
//                                   new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(),
//                                   new Hashtable<SymbolData, LanguageLevelVisitor>());
//      assertFalse("This is an elementary file", _isAdvancedFile());
//      _llv = new IntermediateVisitor(new File("intermediate.dj1"), 
//                                     new LinkedList<Pair<String, JExpressionIF>>(), _s, 
//                                     new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(), 
//                                     new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(), 
//                                     new Hashtable<SymbolData, LanguageLevelVisitor>());
//      assertFalse("This is an intermediate file", _isAdvancedFile());
//      _llv = new AdvancedVisitor(new File("advanced.dj2"), 
//                                 new LinkedList<Pair<String, JExpressionIF>>(), _s, 
//                                 new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
//                                 new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(),
//                                 new Hashtable<SymbolData, LanguageLevelVisitor>());
//      assertTrue("This is an advanced file", _isAdvancedFile());
//      _llv = new ElementaryVisitor(new File("full.java"), 
//                                   new LinkedList<Pair<String, JExpressionIF>>(), _s, 
//                                   new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(), 
//                                   new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(), 
//                                   new Hashtable<SymbolData, LanguageLevelVisitor>());
//      assertFalse("This is a full file", _isAdvancedFile());
//    }
//    
    public void testGetVariableAccessorListHelper() {
      ModifiersAndVisibility _publicMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
      ModifiersAndVisibility _privateMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"private"});

      SymbolData houston = new SymbolData("Houston");
      SymbolData texas = new SymbolData("Texas");
      houston.setSuperClass(texas);
      
      //first, add a field to texas that has a public gettor.  This one should be returned.
      texas.addVar(new VariableData("lone_star", _publicMav, SymbolData.INT_TYPE, true, texas));
      MethodData lone_star = new MethodData("lone_star", _publicMav, 
                                            new TypeParameter[0], SymbolData.INT_TYPE, 
                                            new VariableData[0], 
                                            new String[0], texas, 
                                            new NullLiteral(SourceInfo.NONE));
      texas.addMethod(lone_star); 

      //no gettor for cool, therefore, it should not be returned.
      //NOTE: gettor might be best spelled "getter", but whoever is typing believes it should be spelled this way.
      texas.addVar(new VariableData("cool", _publicMav, SymbolData.DOUBLE_TYPE, true, texas));

      //now, add a private gettor for armidillo to texas.  This should not be returned, because it is private.
      texas.addVar(new VariableData("armadillo", _publicMav, SymbolData.BOOLEAN_TYPE, true, texas));
      MethodData armadillo = new MethodData("armadillo", _privateMav, 
                                            new TypeParameter[0], 
                                            SymbolData.BOOLEAN_TYPE,
                                            new VariableData[0], 
                                            new String[0], texas, 
                                            new NullLiteral(SourceInfo.NONE));
      texas.addMethod(armadillo);
      
      // Now add a field badRoad to Houston.  Its gettor returns a supertype of its type, so it is okay to call.  
      // Will be returned.
      houston.addVar(new VariableData("badRoad", _publicMav, SymbolData.CHAR_TYPE, true, houston));
      MethodData badRoad = new MethodData("badRoad", _publicMav,
                                          new TypeParameter[0],
                                          SymbolData.INT_TYPE,
                                          new VariableData[0], 
                                          new String[0], houston, 
                                          new NullLiteral(SourceInfo.NONE));
      houston.addMethod(badRoad);
      
      LinkedList<MethodData> expected = new LinkedList<MethodData>();
      expected.add(badRoad);
      expected.add(lone_star);
      LinkedList<MethodData> actual = _getVariableAccessorListHelper(houston);
//      for(MethodData m : actual)
//        System.out.println(m.getName());
      assertEquals("Should return the right list of gettors", expected, actual);
      
      
      //if there aren't any fields or methods, that's still okay!
      SymbolData soLonely = new SymbolData("I have no fields!");
      assertEquals("Should return an empty list", 0, _getVariableAccessorListHelper(soLonely).size());
    }
    
  }
}