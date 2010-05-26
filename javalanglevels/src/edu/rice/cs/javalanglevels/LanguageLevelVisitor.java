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

import org.objectweb.asm.*;
import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.javalanglevels.tree.Type; // resolve ambiguity
import edu.rice.cs.javalanglevels.parser.JExprParser;
import edu.rice.cs.javalanglevels.parser.ParseException;
import edu.rice.cs.javalanglevels.util.Log;
import edu.rice.cs.javalanglevels.util.Utilities;
import java.util.*;
import java.io.*;
import java.lang.reflect.Modifier;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.reflect.PathClassLoader;
import edu.rice.cs.plt.reflect.EmptyClassLoader;
import edu.rice.cs.plt.iter.*;
import edu.rice.cs.plt.io.IOUtil;

import junit.framework.TestCase;

/** Top-level Language Level Visitor that represents what is common between all Language Levels.  Enforces constraints
  * during the first walk of the AST (checking for general errors and building the symbol table).  This class enforces
  * things that are common to all contexts reachable at any Language Level, as well as top level constraints.
  */
public class LanguageLevelVisitor extends JExpressionIFPrunableDepthFirstVisitor {
  
  /** Errors we have encountered during this pass: string is the text of the error, JExpressionIF is the part of
    * the AST where the error occurs. */
  protected static LinkedList<Pair<String, JExpressionIF>> errors;
  
  /** Stores the classes we have referenced, and all their information, once they are resolved.  Bound to static field
    * LanguageLevelConverter.symboltable. */
  public final Symboltable symbolTable;
  
  /*Stores the information on the refernce as well as the visitor we were using when we encountered the
   * reference, for a type that has not been resolved yet*/
  static Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations;

  
  /* A list of other files that are visited.  If the SourceFile is not null, then the source file was
   * visited as opposed to the class file.  This info is used by LanguageLevelConverter in DrJava.
   * We keep the LLV rather than the file, because the LLV has a file, and we need some other information
   * stored in the LLV to properly look up the file.
   */
  static LinkedList<Pair<LanguageLevelVisitor, SourceFile>> visitedFiles;
  
  /**True once we have encountered an error we cannot recover from.*/
  static boolean _errorAdded;
  
  /** A list of qualified class names of the subclasses of this class that have been traversed and 
    * continuations pending the resolution of their superclasses.
    */
  static Hashtable<String, TypeDefBase> _hierarchy;
  
  /**The source file that is being compiled */
  File _file;
  
  /** The package of the current file */
  String _package;
  
  /** The name of the class currently being parsed.  This is null for a top-level visitor but is set in ...ClassBodyVisitor. 
    * The package name prefix is excluded.  */
  String _enclosingClassName;
  
  /** A list of file names (classes) imported by the current file. */
  LinkedList<String> _importedFiles;
  
  /** A list of package names imported by the current file. */
  LinkedList<String> _importedPackages;
  
  /** A list of ClassDefs and InterfaceDefs in the current file. */
  LinkedList<String> _classNamesInThisFile;
  
  /** A Hashtable containing a list of qualified class names of classes waiting to be parsed 
    * within SourceFiles we're in the process of compiling mapped to a pair containing their
    * ClassDefs and LanguageLevelVisitors.
    */
  Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>> _classesToBeParsed;
  LinkedList<String> _innerClassesToBeParsed;
  
  protected static final Log _log = new Log("LLConverter.txt", false);
  
  /** This constructor is called from the subclasses of LanguageLevelVisitor.
    * @param file  The File corresponding to the source file we are visiting
    * @param packageName  The name of the package corresponding to the file
    * @param importedFiles  The list of files (classes) imported by this source file
    * @param importedPackages  The list of packages imported by this source file
    * @param classNamesInThisFile  The list of names of classes defined in this file
    * @param continuations  The table of classes we have encountered but still need to resolve
    */
  public LanguageLevelVisitor(File file, 
                              String packageName, 
                              LinkedList<String> importedFiles, 
                              LinkedList<String> importedPackages, 
                              LinkedList<String> classNamesInThisFile, 
                              Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations) {
    _file = file;
    _enclosingClassName = null;
    _package = packageName;
    _importedFiles = importedFiles;
    _importedPackages = importedPackages;
    _classNamesInThisFile = classNamesInThisFile;
    _classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
    _innerClassesToBeParsed = new LinkedList<String>();
    this.continuations = continuations;
    symbolTable = LanguageLevelConverter.symbolTable;
  }
  
  /** This is a special constructor called from the TypeChecker that sets classesToBeParsed.
    * Normally, classesToBeParsed is set by the concrete instantiation of the LanguageLevelVisitor,
    * but in the case of the TypeChecker, we just create a LangaugeLevelVisitor so we can use its
    * getSymbolData code.  Thus, we must give it a classesToBeParsed to avoid a null pointer exception.
    */
  public LanguageLevelVisitor(File file, 
                              String packageName, 
                              LinkedList<String> importedFiles, 
                              LinkedList<String> importedPackages, 
                              LinkedList<String> classNamesInThisFile, 
                              Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>> classesToBeParsed,
                              Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations) {
    this(file, packageName, importedFiles, importedPackages, classNamesInThisFile, continuations);
    _classesToBeParsed = classesToBeParsed;
  }
  
  /* Reset the nonStatic fields of this visitor.  Used during testing. */
  protected void _resetNonStaticFields() {
    _file = new File("");
    _enclosingClassName = null;
    _package = "";
    _importedFiles = new LinkedList<String>();
    _importedPackages = new LinkedList<String>();
    _classNamesInThisFile = new LinkedList<String>();
  }
  
  /** @return the accessor name corresponding to given field name. */
  public static String getFieldAccessorName(String name) { return name; }
  
  /**@return the source file*/
  public File getFile() { return _file; }
  
  /** @return true if this data is a constructor, i.e., it is a method data,
    * its name and return type are the same, and its return type matches its enclosing sd.
    */
  protected boolean isConstructor(Data d) {
    if (!(d instanceof MethodData)) return false;
    MethodData md = (MethodData) d;
    
    return (md.getReturnType() != null) && (md.getSymbolData() != null) &&
      (md.getReturnType().getName().indexOf(md.getName()) != -1) && 
      (md.getReturnType() == md.getSymbolData());
  }
  
  /** Takes a classname and returns only the final segment of it.  This removes all the dots and dollar signs. */
  public static String getUnqualifiedClassName(String className) { 
    int lastIndexOfDot = className.lastIndexOf(".");
    if (lastIndexOfDot != -1) {
      className = className.substring(lastIndexOfDot + 1);
    }
    int lastIndexOfDollar = className.lastIndexOf("$");
    if (lastIndexOfDollar != -1) {
      className = className.substring(lastIndexOfDollar + 1);
    }
    //Remove any leading numbers
    while (className.length() > 0 && Character.isDigit(className.charAt(0))) { 
      className = className.substring(1, className.length());
    }
    return className;
  }
  
  /** Convert the ReferenceType[] to a String[] with the names of the ReferenceTypes. */
  protected String[] referenceType2String(ReferenceType[] rts) {
    String[] throwStrings = new String[rts.length];
    for (int i = 0; i < throwStrings.length; i++) {
      throwStrings[i] = rts[i].getName();
    }
    return throwStrings;
  }
  
  /** We'll use this class loader to look up resources (*not* to load classes) */
  private static final Thunk<ClassLoader> RESOURCES = new Thunk<ClassLoader>() {
    private Options _cachedOptions = null;
    private ClassLoader _cachedResult = null;
    public ClassLoader value() {
      if (LanguageLevelConverter.OPT != _cachedOptions) {
        _cachedOptions = LanguageLevelConverter.OPT;
        Iterable<File> searchPath = IterUtil.<File>compose(LanguageLevelConverter.OPT.bootClassPath(),
                                                     LanguageLevelConverter.OPT.classPath());
        _cachedResult = new PathClassLoader(EmptyClassLoader.INSTANCE, searchPath);
      }
      return _cachedResult;
    }
  };
  
  /** Uses the ASM class reader to read the class file corresponding to the class in the specified directory, and uses
    * the information from ASM to build a SymbolData corresponding to the class.
    * @param qualifiedClassName  The fully qualified class name of the class we are looking up
    * @param directoryName  The directory where the class is located.
    */
  private SymbolData _classFile2SymbolData(String qualifiedClassName, String programRoot) {
    ClassReader reader = null;
    try {
      String fileName = qualifiedClassName.replace('.', '/') + ".class";
      InputStream stream = RESOURCES.value().getResourceAsStream(fileName);
      if (stream == null && programRoot != null) {
        stream = PathClassLoader.getResourceInPathAsStream(fileName, new File(programRoot));
      }
      if (stream == null) { return null; }
      // Let IOUtil handle the stream here, because it closes it when it's done, unlike ASM.
      reader = new ClassReader(IOUtil.toByteArray(stream));
    }
    catch (IOException e) { return null; }
    
    // This is done so that the SymbolData in the Symboltable is updated and returned.
    final SymbolData sd;
    SymbolData sdLookup = symbolTable.get(qualifiedClassName); 
    if (sdLookup == null)  { // create dummy continuation for sd
      sd = new SymbolData(qualifiedClassName);
      symbolTable.put(qualifiedClassName, sd);
    }
    else { sd = sdLookup; }
    
    // make it be a non-continuation, since we are filling it in
    sd.setIsContinuation(false);
    
    final SourceInfo lookupInfo = _makeSourceInfo(qualifiedClassName);
    final String unqualifiedClassName = getUnqualifiedClassName(qualifiedClassName);
    
    ClassVisitor extractData = new ClassVisitor() {
      
      public void visit(int version, int access, String name, String sig, String sup, String[] interfaces) {
        sd.setMav(_createMav(access));
        sd.setInterface(Modifier.isInterface(access));
        
        int slash = name.lastIndexOf('/');
        if (slash == -1) { sd.setPackage(""); }
        else { sd.setPackage(name.substring(0, slash).replace('/', '.')); }
        
        if (sup == null) { sd.setSuperClass(null); }
        else { sd.setSuperClass(getSymbolDataForClassFile(sup.replace('/', '.'), lookupInfo)); }
        
        if (interfaces != null) {
          for (String iName : interfaces) {
            SymbolData superInterface = getSymbolDataForClassFile(iName.replace('/', '.'), lookupInfo);
            if (superInterface != null) { sd.addInterface(superInterface); }
          }
        }
      }
      
      public FieldVisitor visitField(int access, String name, String desc, String sig, Object value) {
        /* Private fields cannot be ignored because they are used in code augmentation for generating constructors,
         * equals, and hashCode. */
        String typeString = org.objectweb.asm.Type.getType(desc).getClassName();
        SymbolData type = getSymbolDataForClassFile(typeString, lookupInfo);
        if (type != null) { sd.addVar(new VariableData(name, _createMav(access), type, true, sd)); }
        return null;
      }
      
      public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exceptions) {
        if (Modifier.isPrivate(access)) return null; // ignore private methods in class files; they are invisible
        boolean valid = true;
        String methodName;
        SymbolData returnType;
        if (name.equals("<init>")) {
          methodName = unqualifiedClassName;
          returnType = sd;
        }
        else {
          methodName = name;
          String returnString = org.objectweb.asm.Type.getReturnType(desc).getClassName();
          returnType = getSymbolDataForClassFile(returnString, lookupInfo);
          valid = valid && (returnType != null);
        }
        org.objectweb.asm.Type[] argTypes = org.objectweb.asm.Type.getArgumentTypes(desc);
        VariableData[] args = new VariableData[argTypes.length]; 
        for (int i = 0; i < argTypes.length; i++) {
          SymbolData argType = getSymbolDataForClassFile(argTypes[i].getClassName(), lookupInfo);
          if (argType == null) { valid = false; }
          else { args[i] = new VariableData(argType); }
        }
        if (exceptions == null) { exceptions = new String[0]; }
        for (int i = 0; i < exceptions.length; i++) { exceptions[i] = exceptions[i].replace('/', '.'); }
        
        if (valid) {
          MethodData m = 
            MethodData.make(methodName, _createMav(access), new TypeParameter[0], returnType, args, exceptions, sd, null);
          for (VariableData arg : args) { arg.setEnclosingData(m); }
          sd.addMethod(m, false, true);
        }
        return null;
      }
      
      public void visitSource(String source, String debug) {}
      public void visitOuterClass(String owner, String name, String desc) {}
      public AnnotationVisitor visitAnnotation(String desc, boolean visible) { return null; }
      public void visitAttribute(Attribute attr) {}
      public void visitInnerClass(String name, String outerName, String innerName, int access) {}
      public void visitEnd() {}
      
    };
    reader.accept(extractData, ClassReader.SKIP_CODE);
    
    
    //Remove the class from the list of continuations to resolve.
    continuations.remove(qualifiedClassName);
    
    return sd;
  }
  
  /** Build a SourceInfo corresponding to the specified class name, with -1 as the
    * value for row and column of the start and finish.
    */
  protected SourceInfo _makeSourceInfo(String qualifiedClassName) {
    return new SourceInfo(new File(qualifiedClassName), -1, -1, -1, -1);
  }
  
  /** Looks up the SymbolData for a qualified name that is in the list of classes to be parsed.
    * This method assumes that qualifiedClassName is in _classesToBeParsed.
    */
  private SymbolData _lookupFromClassesToBeParsed(String qualifiedClassName, SourceInfo si, boolean resolve) {
    if (resolve) {
      Pair<TypeDefBase, LanguageLevelVisitor> p = _classesToBeParsed.get(qualifiedClassName);
      if (p == null) {
        // This occurs when a class depends upon another class in the same file that has a bogus super class.
        // Perhaps occurs elsewhere...?
        return null;
      }
      // Check for cyclic inheritance.
      TypeDefBase cd = p.getFirst();
      LanguageLevelVisitor llv = p.getSecond();
      cd.visit(llv);
      return symbolTable.get(qualifiedClassName);
    }
    else {
      // Return a continuation, since it shouldn't be in the symbolTable yet based on where we call this method from.
      // The visitor we pair here doesn't matter because it should always get removed from the continuations list before
      // it is visited.
//      System.err.println("Creating continuation for class to be parsed: " + qualifiedClassName);
      SymbolData sd = addSymbolData(si, qualifiedClassName);
      return sd;
    }
  }
  
  /** Check to see if the specified classname is the name of a fully qualified java library class. */
  public static boolean isJavaLibraryClass(String className) {
    return className.startsWith("java.") ||
      className.startsWith("javax.") ||
      className.startsWith("org.ietf.") ||
      className.startsWith("org.omg.") ||
      className.startsWith("org.w3c.") ||
      className.startsWith("org.xml.") ||
      className.startsWith("sun.") ||
      className.startsWith("junit.framework."); //TODO: help!
  }
  
  /** @return true if the specified VariableData overwrites one of the members of the list of VariableDatas
    * and false otherwise.  A VariableData is overwritten if its name is shadowed.
    */
  public static boolean isDuplicateVariableData(LinkedList<VariableData> vds, VariableData toInsert) {
    for (int i = 0; i<vds.size(); i++) {
      VariableData temp = vds.get(i);
      if (temp.getName().equals(toInsert.getName())) {
        return true;
      }
    }
    return false;
  }
  
  /** Resolves references to class files embedded in a class file.  Sets resolve to true since we are loading the symbol
    * table from compiled code.  The class name is guaranteed to be fully qualified.  If the result is null, gives an 
    * error.  Removes the symbol data from the continuations list, and return it.
    * @return the result of trying to resolve className.
    */
  protected SymbolData getSymbolDataForClassFile(String className, SourceInfo si) {
    SymbolData sd = getSymbolDataHelper(className, si, true, true, true, false);
    
    if (sd == null) {
      // This is an error in the user's class file so throw an error.
      // The NullLiteral is a hack to get a JExpression with the correct SourceInfo inside.
      _addAndIgnoreError("Class " + className + " not found.", new NullLiteral(si));
      return null;
    }
    sd.setIsContinuation(false);
    
    continuations.remove(sd.getName());
    return sd;
  }
  
  /** Checks to see if the provided class name is the name of a primative type, and if so,
    * returns the corresponding SymbolData.  Otherwise, returns null.
    */
  private SymbolData _getSymbolData_Primitive(String className) {
    if (className.equals("boolean")) {
      return SymbolData.BOOLEAN_TYPE;
    }
    else if (className.equals("char")) {
      return SymbolData.CHAR_TYPE;
    }
    else if (className.equals("byte")) {
      return SymbolData.BYTE_TYPE;
    }
    else if (className.equals("short")) {
      return SymbolData.SHORT_TYPE;
    }
    else if (className.equals("int")) {
      return SymbolData.INT_TYPE;
    }
    else if (className.equals("long")) {
      return SymbolData.LONG_TYPE;
    }
    else if (className.equals("float")) {
      return SymbolData.FLOAT_TYPE;
    }
    else if (className.equals("double")) {
      return SymbolData.DOUBLE_TYPE;
    }
    else if (className.equals("void")) {
      return SymbolData.VOID_TYPE;
    }
    else if (className.equals("null")) {
      return SymbolData.NULL_TYPE;
    }
    return null;
  }
  
  /** Assume the class name is qualified, and try to look it up.
    * If it's a continuation and resolve is true, resolve the class 
    * by reading its class file.
    * @param className  The name to look up.  Presumed to be qualified.
    * @param si  The source info of where this was called from.
    * @param resolve  true if we want to resolve the SymbolData.
    * @param addError  Whether we want to throw an error or not if we can't find the class 
    */ 
  private SymbolData _getQualifiedSymbolData(String className, SourceInfo si, boolean resolve, boolean fromClassFile, 
                                             boolean addError) {
    _log.log("_getQualifiedSymbolData called on '" + className + "'");
   
    // We assume a period in a class name means it is qualified, make sure it's either in
    // this package, is imported specifically, is in an imported package, or is in java.lang.
    // We can't directly check it, because parsing class files adds every class that is recursively
    // referenced by that class file, and we shouldn't be allowed to see some of them.
    SymbolData sd = symbolTable.get(className);
//    if (className.equals("fully.qualified.Woah")) System.err.println("_getQualifiedSymbolData(" + className + ", ...) called" +
//                                                                    "\nsd = " + sd);
    /* If sd is not null then return it unless it is a continuation that we are resolving.
     * If we're from a class file, then a continuation is ok because we assume
     * that we'll find it later. (?)  If you don't return here, you can get into
     * an infinite loop if there's a self-referencing class.
     */
    _log.log("Corresponding symbolTable entry = " + sd);
    if (sd != null && (! resolve || ! sd.isContinuation() || fromClassFile)) { 
      _log.log("Returning " + sd);
      return sd; 
    }
    
    // Look it up in the symbol table, see if it's a Java library class, look it up from the filesystem.
    if (isJavaLibraryClass(className)) {
      _log.log("Calling  _classFile2SymbolData");
      return _classFile2SymbolData(className, null);
    }
    else if (resolve) {  // Look for class file if resolving a continuation
      SymbolData newSd = _getSymbolData_FromFileSystem(className, si, resolve, addError);
      if (newSd != SymbolData.NOT_FOUND) {
        _log.log("Returning " + sd + " from file system");
        return newSd;
      }
      else if (sd != null && sd.isContinuation()) return sd;
      if (addError) {
        _addAndIgnoreError("The class " + className + " was not found.", new NullLiteral(si));
      }
    }
    _log.log("Returning null");
    return null;
  }
  
  /* Creates a new ArrayData of the element type specified by eltSd  with llv and si as the corresponding 
   * LanguageLevelVisitor and SourceInfo and enters it in the SymbolTable. */
  public ArrayData defineArraySymbolData(SymbolData eltSd, LanguageLevelVisitor llv, SourceInfo si) {
    ArrayData arraySd = new ArrayData(eltSd, llv, si);
    symbolTable.put(arraySd.getName(), arraySd);
    return arraySd;
  }
        
  /** Gets the inner SymbolData for this array type. If it's not null, then try to lookup the ArrayData
    * corresponding to that type plus '[]'.  If it's already there, return it.  If not, create a new ArrayData
    * and put it in the Symbol Table.  This method is a leaf in the 'get...Symbol' hierarchy.
    * @param className  The String name of the array type we're trying to resolve.
    * @param si  The SourceInfo corresponding to the class.  Used if an error is encountered.
    * @param resolve  true if this SymbolData needs to be completely resolved.
    * @param fromClassFile  true if this type is from a class file.
    */
  private SymbolData _getArraySymbolData(String className, SourceInfo si, boolean resolve, boolean fromClassFile, 
                                         boolean addError, boolean checkImportedPackages) {
    // shouldn't be resolving an array type since you can't extend one, so resolve should be false
    SymbolData innerSd = getSymbolDataHelper(className.substring(0, className.length() - 2), si, resolve, fromClassFile, 
                                             addError, checkImportedPackages);
    if (innerSd != null) {
      SymbolData sd = symbolTable.get(innerSd.getName() + "[]");
      if (sd != null) { return sd; }
      else { return defineArraySymbolData(innerSd, this, si); }
    }
    else { return null; }
  }
  
  /** The SymbolData we want is defined later in the current file.  If it needs to be looked up or resolved, do so.
    * @param qualifiedClassName  The name of the class we're looking up.
    * @param si  Information about where the class was called from.
    * @param resolve  true if we want to fully resolve the SymbolData.
    */
  private SymbolData _getSymbolData_FromCurrFile(String qualifiedClassName, SourceInfo si, boolean resolve) {
    SymbolData sd = symbolTable.get(qualifiedClassName);
    if (sd == null || (sd.isContinuation() && resolve)) {
      // The class is below the one we're currently parsing or there was an error in parsing one of the classes.
      return _lookupFromClassesToBeParsed(qualifiedClassName, si, resolve);
    }
    else return sd;
  }
  
    /** Check the file system for the class name, returning the corresponding SymbolData if there is a match.  If no match
    * is found an resolve is false, a continuation for the symbol is created and entered in the symbol table and continuatoin
    * table.  This method is a leaf in the 'getSymbol' calling hierarchy.
    * @param qualifiedClassName  The name of the class we're looking up.
    * @param si  Information about where the class was called from.
    * @param resolve  true if we want to fully resolve the SymbolData.
    * @param addError  true if we want to throw errors.
    */
  private SymbolData _getSymbolData_FromFileSystem(final String qualifiedClassName, SourceInfo si, boolean resolve,
                                                   boolean addError) {
    // If qualifiedClassName is already defined (and not a continuation to resolve), return
    SymbolData sd = symbolTable.get(qualifiedClassName);
    if (sd != null && (! resolve || ! sd.isContinuation())) { return sd; }
    
    // Is qualifiedClassName in _classesToBeParsed, look it up directly in the parsed ASTs
    Pair<TypeDefBase, LanguageLevelVisitor> pair = _classesToBeParsed.get(qualifiedClassName);
    if (pair != null) return _lookupFromClassesToBeParsed(qualifiedClassName, si, resolve);
    
    /* If qualifiedClassName is not in the symbol table or list of classes to be parsed, check if the class is defined
     * in this package tree.
     */
    String qualifiedClassNameWithSlashes = 
      qualifiedClassName.replace('.', System.getProperty("file.separator").charAt(0));
    File _fileParent = _file.getParentFile();
    String programRoot = (_fileParent == null) ? "" : _fileParent.getAbsolutePath();  // Eventually set to root of package (for _file)
    assert (programRoot != null); // parent != null => parent exists.
    
    final String path;  // The expected path name of the class file (less .class) for qualifiedClassName

    if (programRoot.length() > 0) {      
      String packageWithSlashes = _package.replace('.', System.getProperty("file.separator").charAt(0));
      int indexOfPackage = programRoot.lastIndexOf(packageWithSlashes); // index of slash preceding first char of package name
      if (indexOfPackage < 0) path = qualifiedClassName;
      else {
        programRoot = programRoot.substring(0, indexOfPackage);
        path = programRoot + System.getProperty("file.separator") + qualifiedClassNameWithSlashes;
      }
    }
    else {
      path = qualifiedClassNameWithSlashes;  // Using file system root for programRoot
    }
    
    String dirPath; /* the expected directory of the file we're trying to resolve */
    String newPackage = ""; /* the package of the file we're trying to resolve */
    int lastSlashIndex = qualifiedClassNameWithSlashes.lastIndexOf(System.getProperty("file.separator"));
    if (lastSlashIndex != -1) {
      String newPackageWithSlashes = qualifiedClassNameWithSlashes.substring(0, lastSlashIndex);
      dirPath = programRoot + System.getProperty("file.separator") + newPackageWithSlashes;
      newPackage = newPackageWithSlashes.replace(System.getProperty("file.separator").charAt(0), '.');
    }
    else {
      int lastPathSlashIndex = path.lastIndexOf(System.getProperty("file.separator"));
      if (lastPathSlashIndex != -1) dirPath = path.substring(0, lastPathSlashIndex);
      else dirPath = "";
    }
    
    // Find class file and matching source file for qualifiedClassName -- if they exist
    
    File classFile = new File(path + ".class");  // create File object for class file
    
    // Then look for the most recently modified matching source (.djx or .java) file.    
    File[] sourceFiles = new File(dirPath).listFiles(new FileFilter() {
      public boolean accept(File f) {
        try {
          f = f.getCanonicalFile();
          return new File(path + ".dj0").getCanonicalFile().equals(f) ||
            new File(path + ".dj1").getCanonicalFile().equals(f) ||
            new File(path + ".dj2").getCanonicalFile().equals(f) ||
            new File(path + ".java").getCanonicalFile().equals(f);
        }
        catch (IOException e) { return false; }
      }});
    
    File sourceFile = null; // sourceFile is either null or an existing file
    if (sourceFiles != null) {
      long mostRecentTime = 0;
      for (File f : sourceFiles) {
        long currentLastModified = f.lastModified();
        if (f.exists() && mostRecentTime < currentLastModified) {
          mostRecentTime = currentLastModified;
          sourceFile = f;
        }
      }
    }

    // Claim: sourceFile is not the current file.  Otherwise, className would have been in _classNamesInThisFile.
    
    // If sourceFile exists, we have identified the class corresponding to qualifiedClassName.  If resolve is false, 
    // simply create and return the appropriate continuation, deferring the loading of class information until reolution
    // time.  If there is no corresponding class file or the class file is not 
    // up-to-date, signal an error.  Otherwise load the symbol table information from the class file

    if (sourceFile != null) {
      // First see if we even need to resolve this class. If not, create a continuation and return it.
      if (! resolve) { 
        assert sd == null;
        sd = addSymbolData(si, qualifiedClassName); // defer loading class file information
        return sd;
//        else {
//          sd = new SymbolData(qualifiedClassName);
//          continuations.put(qualifiedClassName, new Pair<SourceInfo, LanguageLevelVisitor>(si, this));
//          symbolTable.put(qualifiedClassName, sd);
//          return sd;
        }
      // Get last modified time of corresponding class file
      long classModTime = classFile.lastModified();
      if (classModTime == 0L) return null;  // if classFile does not exist, return
      
      if (sourceFile.lastModified() > classModTime) { // the class file is out of date
        if (addError) {
          _addAndIgnoreError("The file " + sourceFile.getAbsolutePath() + 
                             " needs to be recompiled; it's class files either do not exist or are out of date.",
                             new NullLiteral(si));
        }
        return null;
      }
    }
    
    // if source file exists, the corresponding class file is up to date
    if (classFile.exists()) {
      // read this classfile, create the SymbolData and return it
     _log.log("Reading classFile " + qualifiedClassName);
      sd = _classFile2SymbolData(qualifiedClassName, programRoot);
      if (sd == null) {
        if (addError) {
          _addAndIgnoreError("File " + classFile + " is not a valid class file.", null);
        }
        return null;
      }
      _log.log("Returning symbol constructed by loading class file");
      return sd;
    }
    return SymbolData.NOT_FOUND;
  }
 
  /** Calls getSymbolData with default values By default, resolve is false.  By default, fromClassFile is false, since 
    * this is only true when we are trying to resolve types from the context of a class file. By default addError is
    * true, since we want to display errors.  By default checkImportedStuff is true, since we want to consider 
    * imported packages and classes initially.
    * @param className  The String name of the class to resolve
    * @param si  The SourceInfo corresponding to the reference to the type
    */
  protected SymbolData getSymbolData(String className, SourceInfo si) {
    return getSymbolData(className, si, false, false, true, true);
  }
  
  /** Call getSymbolData with some default values.  Once it has returned, check to see if you got back the resolved 
    * class if resolve is true. By default, fromClassFile is false, since this is only true when we are trying to 
    * resolve types from the context of a class file.  By default addError is true, since we want to display errors.
    * By default checkImportedStuff will be true, since we want to consider imported packages and classes initially.
    * @param className  The String name of the class to resolve.
    * @param si  The SourceInfo corresponding to the reference to the type
    * @param resolve  true if we want to resolve the symbol data corresponding to this class, 
    *                 false if we want to leave it as a continuation
    */  
  public SymbolData getSymbolData(String className, SourceInfo si, boolean resolve) {
    SymbolData sd = getSymbolData(className, si, resolve, false, true, true);
    return sd;
  }
  
  /** Calls getSymbolData with some default values.  By default addError will be true, since we want to display errors.
    * By default checkImportedStuff will be true, since we want to consider imported packages and classes initially.
    * @param className  The String name of the class to resolve
    * @param si  The SourceInfo corresponding to the reference to the type
    * @param resolve  true if we want to resolve the symbol data corresponding to this class, 
    *                 false if we want to leave it as a continuation
    * @param fromClassFile  true only when we are trying to resolve types from the context of a class file.
    */  
  protected SymbolData getSymbolData(String className, SourceInfo si, boolean resolve, boolean fromClassFile) {
    return getSymbolData(className, si, resolve, fromClassFile, true, true);
  }
  
  /** Call getSymbolData with some default values.  By default checkImportedStuff will be true, since we want to
    * consider imported packages and classes initially.
    * @param className  The String name of the class to resolve
    * @param si  The SourceInfo corresponding to the reference to the type
    * @param resolve  true if we want to resolve the symbol data corresponding to this class, false if we want to 
    *                 leave it as a continuation
    * @param fromClassFile  true only when we are trying to resolve types from the context of a class file.
    * @param addError  true if we want to give an error if this class cannot be resolved.
    */  
  protected SymbolData getSymbolData(String className, SourceInfo si, boolean resolve, boolean fromClassFile, 
                                     boolean addError) {
    return this.getSymbolData(className, si, resolve, fromClassFile, addError, true);
  }
  
  /** This method processes qualified class names by looking up each piece sequentially.  Once a SymbolData is found
    * matching to the class name processed thus far, we process rest of the class name as an inner class reference.  
    * This method calls getSymbolDataHelper to look up classes.
    * @param className           The name of the class to lookup.
    * @param si                  The SourceInfo of the reference to className used in case of an error.
    * @param resolve             Whether to return a continuation or fully parse the class.
    * @param fromClassFile       Whether this was called from the class file reader.
    * @param addError            Whether to add errors or not
    * @param checkImportedStuff  Whether to try prepending the imported package names
    */
  protected SymbolData getSymbolData(String className, SourceInfo si, boolean resolve, boolean fromClassFile, 
                                     boolean addError, boolean checkImportedStuff) {
 
    if (className.endsWith("[]")) { // className refers to an array type
      String rawClassName = className.substring(0, className.length() - 2);
      SymbolData sd = getSymbolData(rawClassName, si, resolve, fromClassFile, addError, checkImportedStuff);
      if (sd == null) return null;   // Should only happen in tests.
      ArrayData ad = new ArrayData(sd, this, si);
      symbolTable.put(ad.getName(), ad);
      return ad;
    }
    
    // First, handle classNames that clearly do NOT refer to inner classes
    int indexOfNextDot = className.indexOf(".");
    int indexOfNextDollar = className.indexOf("$");   // '$' is assumed not to appear in source program type names
    if (indexOfNextDot == -1 && indexOfNextDollar == -1)
      return getSymbolDataHelper(className, si, resolve, fromClassFile, addError, checkImportedStuff);
    
    // Try to decompose className into an inner class reference, but name may simply be fully qualified
    indexOfNextDot = 0;   
    SymbolData sd;
    int length = className.length();
    while (indexOfNextDot != length) {
      indexOfNextDot = className.indexOf(".", indexOfNextDot + 1);
      if (indexOfNextDot == -1) { indexOfNextDot = length; }
      String prefix = className.substring(0, indexOfNextDot);
      /* We want to resolve after every piece until the last one because we need to know when we actually have a class
       * so that we can tell that the rest of the pieces are inner classes.  We use the resolve parameter's value for 
       * the last piece since that means there are no inner classes
       */
//      boolean newResolve = resolve || (indexOfNextDot != length);
      sd = getSymbolDataHelper(prefix, si, resolve, fromClassFile, false, checkImportedStuff);
//      if (prefix.equals("fully.qualified.Woah")) throw new RuntimeException(prefix + " passed to helper and newResolve = " + resolve );
      if (sd != null) { // prefix matches an extant symbol
        String outerClassName = prefix;
        String innerClassName = "";
        if (indexOfNextDot != length) {
          SymbolData outerClassSD = sd;
          innerClassName = className.substring(indexOfNextDot + 1);
//          System.err.println("Outer class prefix found: " + prefix + " inner class extension: " + innerClassName);
          sd = outerClassSD.getInnerClassOrInterface(innerClassName);
//          System.err.println("Corresponding symbol = " + sd);
          if (sd == null) { // create continuation for inner class; we are forbidding some ambiguities Java may permit
            sd = addInnerSymbolData(si, outerClassName + "." + innerClassName, outerClassSD);
          }
          return sd;
        }
        else if (sd == SymbolData.AMBIGUOUS_REFERENCE) {
          _addAndIgnoreError("Ambiguous reference to class or interface " + className, new NullLiteral(si));
          return null;
        }
        else if (sd != null && sd != SymbolData.NOT_FOUND) { return sd; }
      }
      // sd may be null or an error element of SymbolData
    }
    
    // No match was found
    if (! fromClassFile && addError) {
      // _log.log("Returning an Invalid class name for " + className);
      String newName = className;
      int lastDollar = newName.lastIndexOf("$");
      newName = newName.substring(lastDollar + 1, newName.length());
//      Utilities.show("Invalid class name " + newName + " and " + className);
      _addAndIgnoreError("Invalid class name " + newName, new NullLiteral(si));
//      throw new RuntimeException("Invalid class name " + newName + " encountered in file " + _file);
    }
    return null;
  }
  
  /** Try to look up name from the context of the lhs.
    * @param lhs  The TypeData corresponding to the enclosing of this name reference
    * @param name  The name piece to look up from the context of lhs
    * @param si  The SourceInfo corresponding to this reference
    * @param addError  true if an error should be added
    * @return  The SymbolData corresponding to this lookup, or NOT_FOUND or null if it could not be found
    */
  protected SymbolData getSymbolData(TypeData lhs, String name, SourceInfo si, boolean addError) {
    //arguments we do not need to pass in
    boolean resolve = false;
    boolean fromClassFile = false;
    boolean checkImportedStuff = false;
    
    if (lhs == null) {return null;}
    
    else if (lhs instanceof PackageData) {
      String className = lhs.getName() + "." + name;
      return getSymbolDataHelper(className, si, resolve, fromClassFile, addError, checkImportedStuff);
    }
    
    else { //if (lhs instanceof SymbolData) {
      SymbolData result = lhs.getInnerClassOrInterface(name);
      if (result == SymbolData.AMBIGUOUS_REFERENCE) {
        if (addError) { _addAndIgnoreError("Ambiguous reference to class or interface " + name, new NullLiteral(si)); }
        return null;
      }
      return result;
    }
  }
  
  /** This method takes in a class name (it may or may not be qualified) and tries to find it in the symbol 
    * table.  If the class name is not qualified and this search fails, it tries to match the class name with classes
    * defined in the current file.  Then it tries to match it with one of the imported files.  If this fails
    * it tries to prepend the imported packages to the class name.   If this fails, either report an error (if resolve
    * is true) or return null.
    * @param className  The name of the class to lookup.
    * @param si  The SourceInfo of the reference to className used in case of an error.
    * @param resolve  Whether to return a continuation or fully parse the class.
    * @param fromClassFile  Whether this was called from the class file reader.
    * @param addError  Whether to add errors.  We don't add errors when iterating through a qualified class name's
    * package.
    */
  protected SymbolData getSymbolDataHelper(String className, SourceInfo si, boolean resolve, boolean fromClassFile, 
                                           boolean addError, boolean checkImportedStuff) {
    // Check for primitive types.    
    SymbolData sd = _getSymbolData_Primitive(className);
    if (sd != null) { return sd; }
    
    // Check for array types.
    if (className.endsWith("[]")) {
      return _getArraySymbolData(className, si, resolve, fromClassFile, addError, checkImportedStuff);
    }
    
    // Check for qualified types. (FAILS for inner classes).  This invocation may be the result of a recursive call.
    if (className.indexOf(".") != -1) return _getQualifiedSymbolData(className, si, resolve, fromClassFile, addError);
    
    String name = null; // name of the SymbolData to be returned
    String qualifiedClassName = getQualifiedClassName(className);
//    if (className.equals("MyInnerClass")) 
//      System.err.println("QualifiedClassName for MyInnerClass = " + qualifiedClassName);
//    System.err.println("qualifiedClassName for " + className + " is " + qualifiedClassName);
    // Check if className is defined in this file.
    if (_classNamesInThisFile.contains(qualifiedClassName)) {
      return _getSymbolData_FromCurrFile(qualifiedClassName, si, resolve);
    }
   
    // Check if className was specifically imported -- Not done at elementary level.
    // At this point, we know that class name is not qualified.
    // We will not check that the package is correct here, because it is caught in the type checker.
    Iterator<String> iter = _importedFiles.iterator();
    if (checkImportedStuff) {
      while (iter.hasNext()) {
        String s = iter.next();
        if (s.endsWith(className)) {
          // All imported files should be in the symbol table.
          SymbolData tempSd = symbolTable.get(s);
          // Only need to fully resolve if resolve is on and the imported file is a continuation.
//          if (tempSd == null) System.err.println("Symbol lookup failed for " + s);
          if (resolve && tempSd != null && tempSd.isContinuation()) {
//            if (className.equals("Woah")) System.err.println("Calling getSymbolData for Woah");
            return getSymbolData(s, si, resolve, fromClassFile, addError, false);  // POTENTIAL INFINITE RECURSION!
          }
          else return tempSd;
        }
      }
    }
    
    // Check if the qualified class name is already in the symbol table at this package level.
    // Skip checking if this class is in the package if it's qualified and not qualified with this
    // package.
    if (className.indexOf(".") == -1 || (!_package.equals("") && className.startsWith(_package))) {
      sd = symbolTable.get(qualifiedClassName);
//      if (className.equals("Woah")) 
//        System.err.println("Potentially calling getSymbolData_FromFileSystem for Woah; sd = " + sd);
      if (sd == null || (sd.isContinuation() && resolve)) {
        sd = _getSymbolData_FromFileSystem(qualifiedClassName, si, resolve, addError);
//        if (className.equals("Woah")) 
//          System.err.println("getSymbolData_FromFileSystem for Woah returned = " + sd);
        if (sd != null && sd != SymbolData.NOT_FOUND) return sd;
      }      
      else {
        // Either we're in the default package and we found the unqualified name or we found a continuation and don't
        // need to resolve it.
        return sd;
      }
    }

    SymbolData resultSd = null;
    // Check if the className's package was imported.
    if (checkImportedStuff) {
//      if (className.equals("Object")) System.err.println("Checking import packages for Object");
      iter = _importedPackages.iterator();
      while (iter.hasNext()) {
        String s = iter.next() + "." + className;
//        if (className.equals("Object")) System.err.println("Looking up: " + s);
        SymbolData tempSd;
        tempSd = getSymbolDataHelper(s, si, resolve, fromClassFile, false, false);
//        if (className.equals("Object")) 
//          System.err.println("matching sd is: " + tempSd + "\nsymbolTable.get(\"" + s + "\") = "+ symbolTable.get(s));
        if (tempSd != null) {
          if (resultSd == null) resultSd = tempSd;
          else {  // tempSd is NOT the first match; flag an error
            if (addError) {
              _addAndIgnoreError("The class name " + className + " is ambiguous.  It could be " + resultSd.getName() + 
                                 " or " + tempSd.getName(), new NullLiteral(si));
              return null;
            }
          }
        }
      }
    }
    return resultSd;
  }
  
  /** Creates a ModifiersAndVisibility from the provided modifier flags. */
  private ModifiersAndVisibility _createMav(int flags) {
    LinkedList<String> strings = new LinkedList<String>();
    if (Modifier.isAbstract(flags)) { strings.addLast("abstract"); }
    if (Modifier.isFinal(flags)) { strings.addLast("final"); }
    if (Modifier.isNative(flags)) { strings.addLast("native"); }
    if (Modifier.isPrivate(flags)) { strings.addLast("private"); }
    if (Modifier.isProtected(flags)) { strings.addLast("protected"); }
    if (Modifier.isPublic(flags)) { strings.addLast("public"); }
    if (Modifier.isStatic(flags)) { strings.addLast("static"); }
    if (Modifier.isStrict(flags)) { strings.addLast("strictfp"); }
    if (Modifier.isSynchronized(flags)) { strings.addLast("synchronized"); }
    if (Modifier.isTransient(flags)) { strings.addLast("transient"); }
    if (Modifier.isVolatile(flags)) { strings.addLast("volatile"); }
    return new ModifiersAndVisibility(SourceInfo.NO_INFO, strings.toArray(new String[strings.size()]));
  }
  
  /** The Qualified Class Name is the package, followed by a dot, followed by the rest of the class name.
    * If the provided className is already qualified, just return it.  If the package is not empty,
    * and the className does not start with the package, append the package name onto the className, and return it.
    * @param className  The className to qualify.
    */
  protected String getQualifiedClassName(String className) {
//    if (className.equals("java")) throw new RuntimeException("BOGUS getQualifiedClassName call on 'java'");
    if (!_package.equals("") && ! className.startsWith(_package)) return _package + "." + className;
    else return className;
  }
  
  // Creates a continuation for an inner class or interface
  protected SymbolData addInnerSymbolData(SourceInfo si, String qualifiedTypeName, Data enclosing) {
    SymbolData sd = new SymbolData(qualifiedTypeName); // create continuation
    SymbolData enclosingSD = enclosing.getSymbolData();
    // if qualifiedTypeName refers to an external inner class, the following will likely fail
    symbolTable.put(qualifiedTypeName, sd);  
    enclosing.getSymbolData().addInnerClass(sd);
    sd.setOuterData(enclosingSD);
    continuations.put(qualifiedTypeName, new Pair<SourceInfo, LanguageLevelVisitor>(si, this));
//    System.err.println("Creating continuation for inner type " + qualifiedTypeName);
    return sd;
  }
  
//  /** Does what is necessary to process this TypeDefBase from the context of the enclosing class.
//    * This method is very similar to addSymbolData, except that it uses an enclosing data for reference.
//    */
//  protected SymbolData addInnerSymbolData(TypeDefBase typeDefBase, 
//                                          String qualifiedTypeName, 
//                                          String partialName, 
//                                          Data enclosing, 
//                                          boolean isClass) {
//    // Try to look up in symbol table, in case it has already been defined
//    SymbolData sd = symbolTable.get(qualifiedTypeName);
//    
//    // Try to look up in enclosing's list of inner classes
//    if (sd == null) { sd = enclosing.getInnerClassOrInterface(partialName); }
//    
//    if (sd != null && ! sd.isContinuation()) {
////      Utilities.show("This class has already been defined sd = " + sd);
//      _addAndIgnoreError("This class has already been defined.", typeDefBase);
////      throw new RuntimeException("ALREADY DEFINED CLASS ERROR; THROWING EXCEPTION");
//      return null;
//    }
//    
//    if (sd != null) {
//      // make sure it is a direct inner class or interface of this data.
//      if (sd.getOuterData() != enclosing) { sd = null; }
//    }
//    
//    /* IMPORTANT: this is defineSymbol for inner classes! Why is all this here for forward references? */
//    // create a new CONTINUATION symbolData for it if this is the first time we've seen it
//    if (sd == null) { 
//      sd = new SymbolData(qualifiedTypeName);
//      sd.setOuterData(enclosing);
//      if (isClass) { enclosing.getSymbolData().addInnerClass(sd); }
//      else { enclosing.getSymbolData().addInnerInterface(sd); }
//    }
//    
////    // create the LinkedList for the SymbolDatas of the interfaces
////    LinkedList<SymbolData> interfaces = new LinkedList<SymbolData>();
////    SymbolData tempSd;
////    ReferenceType[] rts = typeDefBase.getInterfaces();
////    for (int i = 0; i < rts.length; i++) {
////      SourceInfo si = rts[i].getSourceInfo();
////      String tempName = rts[i].getName();
////      tempSd = getSymbolData(tempName, si, false, false, false);
////      
////      if (tempSd != null) { interfaces.addLast(tempSd); }
////      
////      else if (enclosing instanceof SymbolData) {
////        // Check to see if this is an inner class referencing an inner interface
////        tempSd = enclosing.getInnerClassOrInterface(tempName);
////        if (tempSd == null) {
////          String qualifyingPart = qualifiedTypeName.substring(0, qualifiedTypeName.lastIndexOf("."));
////          String qualifiedTempName = qualifyingPart + "." + tempName;
////          // Should we introduce addInnerInterfaceSymbol ?
////          tempSd = new SymbolData(qualifiedTempName);
////          tempSd.setInterface(true);
////          enclosing.getSymbolData().addInnerInterface(tempSd); //interfaces can only be defined in symbol datas
////          tempSd.setOuterData(enclosing);
//////          System.err.println("Creating inner interface continuation for " + qualifiedTempName + " at LLV: 1042");
////          continuations.put(qualifiedTempName, new Pair<SourceInfo, LanguageLevelVisitor>(si, this));          
////        }
////        interfaces.addLast(tempSd);
////      }
////      
////      else {
////        _addAndIgnoreError("Cannot resolve interface " + rts[i].getName(), rts[i]);
////        return null;
////      }
//    
//    }
//    
//    //Set the package to be the current package
//    sd.setPackage(_package);
//    
//    SymbolData superClass = null;
//    
//    if (typeDefBase instanceof InterfaceDef) {
//      // Add Object as the super class of this, so that it will know it implements Object's methods.
//      superClass = getSymbolData("Object", typeDefBase.getSourceInfo(), false);
//      sd.setInterface(true);
//    }
//    
//    else if (typeDefBase instanceof ClassDef) {
//      ClassDef cd = (ClassDef) typeDefBase;
//      ReferenceType rt = cd.getSuperclass();
//      String superClassName = rt.getName();
//      superClass = getSymbolData(superClassName, rt.getSourceInfo(), false, false, false);
//      
//      if (superClass == null) {
//        superClass = enclosing.getInnerClassOrInterface(superClassName);
//        if (superClass == null) {
//          String qualifyingPart = qualifiedTypeName.substring(0, qualifiedTypeName.lastIndexOf("."));
//          superClass = new SymbolData(qualifyingPart + "." + superClassName);
//          enclosing.addInnerClass(superClass);
//          superClass.setOuterData(enclosing);
////          System.err.println("Creating continuation for " + superClassName + " at LLV:1079");
//          continuations.put(superClassName, new Pair<SourceInfo, LanguageLevelVisitor>(rt.getSourceInfo(), this)); 
//        }
//      }
//      sd.setInterface(false);
//    }
//    
//    else {throw new RuntimeException("Internal Program Error: typeDefBase was not a ClassDef or InterfaceDef." + 
//                                     "  Please report this bug.");}
//    
//    // get the SymbolData of the superclass which must be in the symbol table
//    // since we visited the type in forClassDef() although it may be a continuation. 
//    
//    // there is a continuation in the symbol table, update the fields
//    sd.setMav(typeDefBase.getMav());
//    sd.setTypeParameters(typeDefBase.getTypeParameters());
//    sd.setSuperClass(superClass);
//    sd.setInterfaces(interfaces);
//    sd.setIsContinuation(false);
//    continuations.remove(sd.getName());
//    if (sd != null && !sd.isInterface()) { LanguageLevelConverter._newSDs.put(sd, this); }
//    return sd;
//  }
  
  /** This method creates the specified continuation in the symbol table.  Should never happen for an inner class.
    * @param si  The SourceInfo corresponding to this occurrence of the class symbol
    * @param qualifiedClassName  The name for the class.
    */
  protected SymbolData addSymbolData(SourceInfo si, String qualifiedClassName) {
    SymbolData sd = new SymbolData(qualifiedClassName);  // create a continuation
    continuations.put(qualifiedClassName, new Pair<SourceInfo, LanguageLevelVisitor>(si, this));
    symbolTable.put(qualifiedClassName, sd);
//    System.err.println("Creating continuation for " + qualifiedClassName + " at LLV:1124");
    return sd;
  }
  
  /** This method takes in a TypeDefBase (which is either a ClassDef or an InterfaceDef), generates a SymbolData, and 
    * adds the name and SymbolData pair to the symbol table.  It checks that this class is not already in the symbol 
    * table. Should never happen for an inner class or interface.
    * @param typeDefBase  The AST node for the class def, interface def, inner class def, or inner interface def.
    * @param qualifiedClassName  The name for the class; null if this definition is a duplicate
    */
  protected SymbolData defineSymbolData(TypeDefBase typeDefBase, String qualifiedClassName) {
    String name = qualifiedClassName;  // may be an interface
    SymbolData sd = symbolTable.get(name);
    if (sd != null && ! sd.isContinuation()) {
      _addAndIgnoreError("The class or interface " + name + " has already been defined.", typeDefBase);
      return null;
    }
    
    // create the LinkedList for the SymbolDatas of the interfaces
    LinkedList<SymbolData> interfaces = new LinkedList<SymbolData>();
    SymbolData tempSd;
    
    // Create SymbolDatas (continuations) for the interfaces if they do not already exist
    ReferenceType[] rts = typeDefBase.getInterfaces();
    for (ReferenceType rt: rts) {
      tempSd = getSymbolData(rt.getName(), rt.getSourceInfo(), false, false, false);
      
      if (tempSd != null) interfaces.addLast(tempSd);  
      else if (qualifiedClassName.indexOf(".") != -1) { // class is inner
        // Check to see if this is an inner class referencing an inner interface
        String qualifyingPart = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf("."));
        tempSd = getSymbolData(qualifyingPart + "." + rt.getName(), rt.getSourceInfo(), false, false, false);
        if (tempSd == null) {
          String tempName = qualifyingPart + "." + rt.getName();
          tempSd = new SymbolData(tempName);
          tempSd.setInterface(true);
//          System.err.println("Creating continuation for " + tempName  + " at LLV: 1144");
          continuations.put(tempName, new Pair<SourceInfo, LanguageLevelVisitor>(rt.getSourceInfo(), this));          
        }
        interfaces.addLast(tempSd);
      }
      else if (tempSd == null) {  // class is not inner; ith superinterface not yet defined
        String tempName = rt.getName();
        _log.log("CREATING continuation " + tempName + " with SourceInfo " + rt.getSourceInfo());
//        System.err.println("CREATING continuation for " + tempName + " at LLV: 1154");
        tempSd = new SymbolData(tempName);
        tempSd.setInterface(true);
        continuations.put(tempName, new Pair<SourceInfo, LanguageLevelVisitor>(rt.getSourceInfo(), this));    
      }
    }
    
    if (sd == null) { // create a new SymbolData.
      sd = new SymbolData(name);
      symbolTable.put(name, sd);
    }
    
    //Set the package to be the current package
    sd.setPackage(_package);
     
    SymbolData superClass = null;
    
    // Create a SymbolData for the superclass of typeDefBase
    
    if (typeDefBase instanceof InterfaceDef) {
      //add Object as the super class of this, so that it will know it implements Object's methods.
      superClass = getSymbolData("Object", typeDefBase.getSourceInfo(), false);
      sd.setInterface(true);
    }
    
    else if (typeDefBase instanceof ClassDef) {
      ClassDef cd = (ClassDef) typeDefBase;
      ReferenceType rt = cd.getSuperclass();
      // rt cannot be null because every user-defined class extends something.  We must resolve
      // the superclass before proceeding in order to properly identify words as
      // fields or static references to classes.
      String superClassName = rt.getName();
//      if (superClassName.equals("TestCase") || superClassName.equals("junit.framework.TestCase")) {
//            System.out.println("WARNING! " + superClassName + " encountered as superclass");
//            assert false;
//      }
      SourceInfo si = rt.getSourceInfo();
      // The following line generates an infinite recursion in some cases if resolve (the 3rd parm) is true.  Yet
      // when superclass is TestCase and TestCase is not imported
      superClass = getSymbolData(superClassName, si, false); //TODO: if true can generate infinite loop in helper
      
      if (superClass == null) {
        // Couldn't resolve the super class: make it Object by default
        superClass = addSymbolData(si, superClassName);
      }
      sd.setInterface(false);
    }
    
    else { throw new RuntimeException("Internal Program Error: typeDefBase was not a ClassDef or InterfaceDef." + 
                                      "  Please report this bug."); }
    
    // get the SymbolData of the superclass which must be in the symbol table
    // since we visited the type in forClassDef() although it may be a continuation. 
    
    // there is a continuation in the symbol table, update the fields
    sd.setMav(typeDefBase.getMav());
    sd.setTypeParameters(typeDefBase.getTypeParameters());
    sd.setSuperClass(superClass);
    sd.setInterfaces(interfaces);
    sd.setIsContinuation(false);
    _log.log("REMOVING continuation " + sd.getName());
    continuations.remove(sd.getName());
    
    if (! sd.isInterface()) { LanguageLevelConverter._newSDs.put(sd, this); }
    return sd;
  }
  
  /** This method takes in a TypeDefBase (which is either an InnerClassDef or an InnerInterfaceDef), generates a 
    * SymbolData, and adds the name and SymbolData pair to the symbol table.  It checks that this class is not already 
    * in the symbol table, except as a continuation. Should never happen for an inner class or interface.
    * @param typeDefBase  The AST node for the class def, interface def, inner class def, or inner interface def.
    * @param qualifiedClassName  The name for the class; null if this definition is a duplicate
    */
  protected SymbolData defineInnerSymbolData(TypeDefBase typeDefBase, String qualifiedTypeName, Data enclosing) { 
        /* IMPORTANT: this is defineSymbol for inner classes! */
    String name = qualifiedTypeName;  // may be an interface
    SymbolData sd = symbolTable.get(name);
    if (sd != null && ! sd.isContinuation()) {
      _addAndIgnoreError("The class or interface " + name + " has already been defined.", typeDefBase);
      return null;
    }
    
    // create the LinkedList for the SymbolDatas of the interfaces
    LinkedList<SymbolData> interfaces = new LinkedList<SymbolData>();
    SymbolData tempSd;
    ReferenceType[] rts = typeDefBase.getInterfaces();
    for (int i = 0; i < rts.length; i++) {
      SourceInfo si = rts[i].getSourceInfo();
      String tempName = rts[i].getName();
      tempSd = getSymbolData(tempName, si, false, false, false);
      
      if (tempSd != null) { interfaces.addLast(tempSd); }
      
      else if (enclosing instanceof SymbolData) {
        // Check to see if this is an inner class referencing an inner interface
        tempSd = enclosing.getInnerClassOrInterface(tempName);
        if (tempSd == null) {
          String qualifyingPart = qualifiedTypeName.substring(0, qualifiedTypeName.lastIndexOf("."));
          String qualifiedTempName = qualifyingPart + "." + tempName;
          // Should we introduce addInnerInterfaceSymbol ?
          tempSd = new SymbolData(qualifiedTempName);
          tempSd.setInterface(true);
          enclosing.getSymbolData().addInnerInterface(tempSd); //interfaces can only be defined in symbol datas
          tempSd.setOuterData(enclosing);
//          System.err.println("Creating inner interface continuation for " + qualifiedTempName + " at LLV: 1042");
          continuations.put(qualifiedTempName, new Pair<SourceInfo, LanguageLevelVisitor>(si, this));          
        }
        interfaces.addLast(tempSd);
      }
      
      else {
        _addAndIgnoreError("Cannot resolve interface " + rts[i].getName(), rts[i]);
        return null;
      }
    }
        
    // create a new symbolData for this inner class or interface if not seen before
    if (sd == null) { 
      sd = new SymbolData(qualifiedTypeName);
      sd.setOuterData(enclosing);
      if (typeDefBase instanceof ClassDef) { enclosing.getSymbolData().addInnerClass(sd); }
      else { 
        enclosing.getSymbolData().addInnerInterface(sd); 
      }
    }
    //Set the package to be the current package
    sd.setPackage(_package);
    
    SymbolData superClass = null;
    
    if (typeDefBase instanceof InterfaceDef) {
      // Add Object as the super class of this, so that it will know it implements Object's methods.
      superClass = getSymbolData("Object", typeDefBase.getSourceInfo(), false);
      sd.setInterface(true);
    }
    else if (typeDefBase instanceof ClassDef) {
      ClassDef cd = (ClassDef) typeDefBase;
      ReferenceType rt = cd.getSuperclass();
      String superClassName = rt.getName();
      superClass = getSymbolData(superClassName, rt.getSourceInfo(), false, false, false);
      
      if (superClass == null) {  // Why is this necessary?  Forward reference to another inner class?
        superClass = enclosing.getInnerClassOrInterface(superClassName);
        if (superClass == null) {
          String qualifyingPart = qualifiedTypeName.substring(0, qualifiedTypeName.lastIndexOf("."));
          superClass = new SymbolData(qualifyingPart + "." + superClassName);
          enclosing.addInnerClass(superClass);
          superClass.setOuterData(enclosing);
//          System.err.println("Creating continuation for " + superClassName + " at LLV:1079");
          continuations.put(superClassName, new Pair<SourceInfo, LanguageLevelVisitor>(rt.getSourceInfo(), this)); 
        }
      }
      sd.setInterface(false);
    }
    
    else throw new RuntimeException("Internal Program Error: typeDefBase was not a ClassDef or InterfaceDef." + 
                                     "  Please report this bug.");
    
    // get the SymbolData of the superclass which must be in the symbol table
    // since we visited the type in forClassDef() although it may be a continuation. 
    
    // there is a continuation in the symbol table, update the fields
    sd.setMav(typeDefBase.getMav());
    sd.setTypeParameters(typeDefBase.getTypeParameters());
    sd.setSuperClass(superClass);
    sd.setInterfaces(interfaces);
    sd.setIsContinuation(false);
    _log.log("REMOVING continuation " + sd.getName());
    continuations.remove(sd.getName());
    if (! sd.isInterface()) { LanguageLevelConverter._newSDs.put(sd, this); }
    return sd;
  }
    
  /** Convert the specified array of FormalParameters into an array of VariableDatas which is then returned.
    * All formal parameters are automatically made final.
    * TODO: At the advanced level, this may need to be overwritten?
    */
  protected VariableData[] formalParameters2VariableData(FormalParameter[] fps, Data enclosing) {
//    Utilities.show("formalParameters2VariableData called on " + fps);
    //Should conssolidate with same method in AdvancedVisitor; almost identical
    VariableData[] varData = new VariableData[fps.length];
    VariableDeclarator vd;
    String[] mav = new String[] {"final"};
        
    for (int i = 0; i < varData.length; i++) {
      vd = fps[i].getDeclarator();
      String name = vd.getName().getText();  // getName returns a Word
      String typeName = vd.getType().getName();
      SourceInfo si = vd.getType().getSourceInfo();
      SymbolData type = getSymbolData(typeName, si);
      
      if (type == null) {
        // See if this is a partially qualified field reference in the symbol table
        type = enclosing.getInnerClassOrInterface(typeName);
//        System.err.println("For inner class/interface " + typeName + " found type " + type);
      }
      
      if (type == null) { // create a continuation for it
        String qualifiedTypeName = enclosing.getSymbolData().getName() + "." + typeName;
        if (_innerClassesToBeParsed.contains(qualifiedTypeName)) {  // reference to an inner class.
          type = addInnerSymbolData(si, qualifiedTypeName, enclosing);
        }
        else { // reference to a top level class or an external class
          type = addSymbolData(si, typeName);
        }
      }
      
      varData[i] = new VariableData(name, new ModifiersAndVisibility(SourceInfo.NO_INFO, mav), type, true, enclosing);
      varData[i].gotValue();
      varData[i].setIsLocalVariable(true);
    }
   
    return varData;
  }
  
  /** Create a MethodData corresponding to the MethodDef within the context of the SymbolData sd. */
  protected MethodData createMethodData(MethodDef that, SymbolData sd) {
//    Utilities.show("createMethodData called on " + that);
//    _log.log("createMethodData(" + that + ", " + sd + ") called.");
    that.getMav().visit(this);
    that.getName().visit(this);
    
    // Turn the thrown exceptions from a ReferenceType[] to a String[]
    String[] throwStrings = referenceType2String(that.getThrows());
    
    // Turn the ReturnTypeI into a SymbolData    
    String rtString = that.getResult().getName();
    SymbolData returnType;
    //TODO: Overwrite this at the Advanced level (or maybe not)
    if (rtString.equals("void"))  returnType = SymbolData.VOID_TYPE;
    else returnType = getSymbolData(rtString, that.getResult().getSourceInfo());
    
    MethodData md = MethodData.make(that.getName().getText(), that.getMav(), that.getTypeParams(), returnType, 
                                    new VariableData[0], throwStrings, sd, that);
//    _log.log("createMethodData called.  Created MethodData " + md + '\n' + "with modifiers:" + md.getMav());
    // Turn the parameters from a FormalParameterList to a VariableData[]
    VariableData[] vds = formalParameters2VariableData(that.getParams(), md);
    
    if (_checkError()) {  //if there was an error converting the formalParameters, don't use them.
      return md;
    }
    
    md.setParams(vds);
    
    // Adds the formal parameters to the list of vars defined in this method.
    if (! md.addVars(vds)) { //TODO: should this not have been changed from addFinalVars?
      _addAndIgnoreError("You cannot have two method parameters with the same name", that);      
    }
    return md;
  }
  
  /** This method assumes that the modifiers for this particular VariableDeclaration
    * have already been checked.  It does no semantics checking.  It simiply converts
    * the declarators to variable datas, by trying to resolve the types of each declarator.  The VariableDeclaration
    * may be a field declaration!
    */
  protected VariableData[] _variableDeclaration2VariableData(VariableDeclaration vd, Data enclosing) {
    LinkedList<VariableData> vds = new LinkedList<VariableData>();
    ModifiersAndVisibility mav = vd.getMav();
    VariableDeclarator[] declarators = vd.getDeclarators();
    for (VariableDeclarator declarator: declarators) {
      declarator.visit(this); // Does NOTHING!
      Type type = declarator.getType();
      String name = declarator.getName().getText();
      SymbolData sd = handleDeclarator(type, name, enclosing);
   
      if (sd != null) {
        boolean initialized = declarator instanceof InitializedVariableDeclarator;
        // want hasBeenAssigned to be true if this variable declaration is initialized, and false otherwise.
//        System.err.println("creating new VariableData for " + name);
        VariableData vdata = new VariableData(name, mav, sd, initialized, enclosing); 
        vdata.setHasInitializer(initialized);
//        vdata.setIsLocalVariable(true);
        vds.addLast(vdata); 
      }
      
      else _addAndIgnoreError("Class or Interface " + name + " not found", type);
    }
//    System.err.println("Returning VariableDatas " + vds);
    return vds.toArray(new VariableData[vds.size()]);
  }
  
  // What happens with array types?
  SymbolData handleDeclarator(Type type, String name, Data enclosing) {

    String typeName = type.getName();
    SourceInfo si = type.getSourceInfo();
    SymbolData sd = getSymbolData(typeName, si);
    
    if (sd == null) {
      // See if this is a partially qualified field reference
      sd = enclosing.getInnerClassOrInterface(typeName);
    }
    
    if (sd == null) { // create a continuation for it
      String qualifiedTypeName = enclosing.getSymbolData().getName() + "." + typeName;
      if (_innerClassesToBeParsed.contains(qualifiedTypeName)) {  
        // reference to an inner class. Exclude .dj1 and .dj0 files?
        sd = addInnerSymbolData(si, qualifiedTypeName, enclosing);
      }
      else { // reference to a top level class or an external class
        sd = addSymbolData(si, typeName);
      }
    }
    return sd;
  }
                               
  /** This method is called when an error should be added to the static LinkedList of errors.
    * This version is called from the DoFirst methods in the LanguageLevelVisitors to halt
    * parsing of the construct.
    */
  protected static void _addError(String message, JExpressionIF that) {
//    Utilities.show("_addError(" + message + ", " + that + ") called");
    _errorAdded = true;
    Pair<String, JExpressionIF> p = new Pair<String, JExpressionIF>(message, that);
    if (! errors.contains(p)) errors.addLast(p);
  }
  
  /** This method is called when an error should be added, but tree-walking should continue
    * on this construct.  Generally, if the error is not added in the DoFirst, the _errorAdded
    * flag is not checked anyway, so this version should be called.
    */
  protected static void _addAndIgnoreError(String message, JExpressionIF that) {
//    Utilities.show("_addAndIgnoreError(" + message + ", " + that + ") called");
    if (_errorAdded) {
      throw new RuntimeException("Internal Program Error: _addAndIgnoreError called while _errorAdded was true." + 
                                 "  Please report this bug.");
    }
    _errorAdded = false;
    errors.addLast(new Pair<String, JExpressionIF>(message, that));
  }
  
  protected boolean prune(JExpressionIF node) { return _checkError(); }
  
  /** If _errorAdded is true, set it back to false and return true.
    * This will cause the current construct to be skipped, but will allow this first pass
    * to otherwise continue unimpeded.
    * Otherwise, return false, which will allow this first pass to continue normally.
    */
  protected static boolean _checkError() {
    if (_errorAdded) {
      _errorAdded = false;
      return true;
    }
    else return false;
  }
  
  /**Add an error explaining the modifiers' conflict.*/
  private void _badModifiers(String first, String second, ModifiersAndVisibility that) {
    _addError("Illegal combination of modifiers. Can't use " + first + " and " + second + " together.", that);
  }
  
  /** Check for problems with modifiers that are common to all language levels: duplicate modifiers and illegal
    * combinations of modifiers.
    */
  public Void forModifiersAndVisibilityDoFirst(ModifiersAndVisibility that) {
    String[] modifiersAndVisibility = that.getModifiers();
    Arrays.sort(modifiersAndVisibility);
    if (modifiersAndVisibility.length > 0) {
      String s = modifiersAndVisibility[0];
      // check for duplicate modifiers
      for (int i = 1; i < modifiersAndVisibility.length; i++) {
        if (s.equals(modifiersAndVisibility[i])) {
          _addError("Duplicate modifier: " + s, that);
        }
        s = modifiersAndVisibility[i];
      }
      
      // check for illegal combination of modifiers
      String visibility = "package";
      boolean isAbstract = false;
      boolean isStatic = false;
      boolean isFinal = false;
      boolean isSynchronized = false;
      boolean isStrictfp = false;
      boolean isTransient = false;
      boolean isVolatile = false;
      boolean isNative = false;
      for (int i = 0; i < modifiersAndVisibility.length; i++) {
        s = modifiersAndVisibility[i];
        if (s.equals("public") || s.equals("protected") || s.equals("private")) {
          if (! visibility.equals("package")) _badModifiers(visibility, s, that);
          else if (s.equals("private") && isAbstract) _badModifiers("private", "abstract", that);
          else visibility = s;
        }
        else if (s.equals("abstract")) isAbstract = true;
        else if (s.equals("final")) { 
          isFinal = true;
          if (isAbstract) _badModifiers("final", "abstract", that);
        }
        else if (s.equals("static")) { 
          isStatic = true;
          if (isAbstract)  _badModifiers("static", "abstract", that);
        }
        else if (s.equals("native")) { 
          isNative = true;
          if (isAbstract) _badModifiers("native", "abstract", that);
        }
        else if (s.equals("synchronized")) { 
          isSynchronized = true;
          if (isAbstract) _badModifiers("synchronized", "abstract", that);
        }
        else if (s.equals("volatile")) { 
          isVolatile = true;
          if (isFinal) _badModifiers("final", "volatile", that);
        }
      }
      return forJExpressionDoFirst(that);  // Does nothing!
    }
    return null;
  }
  
  /** Check for problems in ClassDefs.  Make sure that the top level class is
    * not private, and that the class name has not already been imported.
    */
  public Void forClassDefDoFirst(ClassDef that) {
    String name = that.getName().getText();  // name of defined class
    Iterator<String> iter = _importedFiles.iterator();
    while (iter.hasNext()) {
      String s = iter.next();
      if (s.endsWith(name) && ! s.equals(getQualifiedClassName(name))) {
        _addAndIgnoreError("The class " + name + " was already imported.", that);
      }
    }
    
    // top level classes cannot be private.
    String[] mavStrings = that.getMav().getModifiers();
    if (! (that instanceof InnerClassDef)) {
      for (int i = 0; i < mavStrings.length; i++) {
        if (mavStrings[i].equals("private")) {
          _addAndIgnoreError("Top level classes cannot be private", that);
        }
      }
    }
    
    // See if this is a Blacklisted class.  Blacklisted classes are any classes in java.lang or TestCase.
    SymbolData javaLangClass = 
      getSymbolData("java.lang." + that.getName().getText(), that.getSourceInfo(), true, false, false, false);
    if (that.getName().getText().equals("TestCase") || (javaLangClass != null && ! javaLangClass.isContinuation())) {
      _addError("You cannot define a class with the name " + that.getName().getText() + 
                " because that class name is reserved." +
                "  Please choose a different name for this class", that);
    }
    return forTypeDefBaseDoFirst(that);
  }
  
  /** Check for problems with InterfaceDefs: specifically, top level interfaces cannot be private or final. */
  public Void forInterfaceDefDoFirst(InterfaceDef that) {
    //top level interfaces cannot be private or final.
    String[] mavStrings = that.getMav().getModifiers();
    for (int i = 0; i < mavStrings.length; i++) {
      if (mavStrings[i].equals("private")) {
        _addAndIgnoreError("Top level interfaces cannot be private", that);
      }
      if (mavStrings[i].equals("final")) {
        _addAndIgnoreError("Interfaces cannot be final", that);
      }
    }
    return forTypeDefBaseDoFirst(that);
  }
  
  /** Check for problems with InnerInterfaceDefs that are common to all language levels: specifically, they cannot be 
    * final.
    */
  public Void forInnerInterfaceDefDoFirst(InnerInterfaceDef that) {
    String[] mavStrings = that.getMav().getModifiers();
    for (int i = 0; i < mavStrings.length; i++) {
      if (mavStrings[i].equals("final")) {
        _addAndIgnoreError("Interfaces cannot be final", that);
      }
    }
    return forTypeDefBaseDoFirst(that);  
  }
  
  /** This sets the package name field in order to find other classes in the same package. */
  public Void forPackageStatementOnly(PackageStatement that) {
    CompoundWord cWord = that.getCWord();
    Word[] words = cWord.getWords();
    String newPackage;
    String separator = System.getProperty("file.separator");
    if (words.length > 0) {
      _package = words[0].getText();
      newPackage = _package;
      for (int i = 1; i < words.length; i++) {
        String temp = words[i].getText();
        newPackage = newPackage + separator + temp;
        _package = _package + "." + temp;
      }    
      String directory = _file.getParent();
      if (directory == null || !directory.endsWith(newPackage)) {
        _addAndIgnoreError("The package name must mirror your file's directory.", that);
      }
    }
    //call getSymbolData to see if this is actually a class as well as a Package Name.  If it is, an error
    //will be given in the TypeChecking step
    //If file is a .java file and not compiled, won't find it.  This is not consistent with the JLS.
    //if file is a ll file and not compiled, will find it, though this is not consistent with the JLS.
    getSymbolData(_package, that.getSourceInfo(), false, false, false);
    return forJExpressionOnly(that);
  }
  
  
  /** Make sure the class being imported has not already been imported and that it doesn
    * not duplicate the package--i.e. import something that is already in the package.
    * If there are no errors, add it to the list of imported files, and create a continuation for it.
    * The class will be resolved later.
    */
  public Void forClassImportStatementOnly(ClassImportStatement that) {
    CompoundWord cWord = that.getCWord();
    Word[] words = cWord.getWords();
    
    // Make sure that this specific imported class has not already been specifically imported
    for (int i = 0; i<_importedFiles.size(); i++) {
      String name = _importedFiles.get(i);
      int indexOfLastDot = name.lastIndexOf(".");
      if (indexOfLastDot != -1 && 
          (words[words.length-1].getText()).equals(name.substring(indexOfLastDot + 1, name.length()))) {
        _addAndIgnoreError("The class " + words[words.length-1].getText() + " has already been imported.", that);
        return null;
      }
    }
    
    StringBuilder nameBuff = new StringBuilder(words[0].getText());
    for (int i = 1; i < words.length; i++) {nameBuff.append("." + words[i].getText());}
    
    String qualifiedTypeName = nameBuff.toString();
    
    // Make sure that this imported class does not duplicate the package.  WHY? FIX THIS.
    // Although this is allowed in full java, we decided to not allow it at any LanguageLevel.
    int indexOfLastDot = qualifiedTypeName.lastIndexOf(".");
    if (indexOfLastDot != -1) {
      if (_package.equals(qualifiedTypeName.substring(0, indexOfLastDot))) {
        _addAndIgnoreError("You do not need to import " + qualifiedTypeName + ".  It is in your package so it is already visible", that);
        return null;
      }
    }
    
    //Now add the class to the list of imported files
    _importedFiles.addLast(qualifiedTypeName);  
    
    // Create a continuation for imported class (named temp) if one does not already exist
    SymbolData sd = symbolTable.get(qualifiedTypeName);
    if (sd == null) {
      // Create a continuation for the imported class and put it into the symbol table so
      // that on lookup, we can check imported classes before classes in the same package.
//      System.err.println("Creating continuation for imported class " + temp);
      sd = addSymbolData(that.getSourceInfo(), qualifiedTypeName);
    }
    return forImportStatementOnly(that);
  }
  
  /**Check to make sure that this package import statement is not trying to import the current pacakge. */
  public Void forPackageImportStatementOnly(PackageImportStatement that) { 
    CompoundWord cWord = that.getCWord();
    Word[] words = cWord.getWords();
    StringBuilder tempBuff = new StringBuilder(words[0].getText());
    for (int i = 1; i < words.length; i++) { tempBuff.append("." + words[i].getText()); }
    String temp = tempBuff.toString();
    
    
    //make sure this imported package does not match the current package
    if (_package.equals(temp)) {
      _addAndIgnoreError("You do not need to import package " + temp + 
                         ". It is your package so all public classes in it are already visible.", that);
      return null;
    }

    _importedPackages.addLast(temp);
    
    return forImportStatementOnly(that);
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
    _addAndIgnoreError("Bitwise or expressions cannot be used at any language level." + 
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
  
  
  /** The EmptyExpression is a sign of an error. It means that we were missing something
    * we needed when the parser built the AST*/
  public Void forEmptyExpressionDoFirst(EmptyExpression that) {
    _addAndIgnoreError("You appear to be missing an expression here", that);
    return null;
  }
  
  /** The NoOp expression signifies a missing binary operator that was encountered when the
    * parser built the AST. */
  public Void forNoOpExpressionDoFirst(NoOpExpression that) {
    _addAndIgnoreError("You are missing a binary operator here", that);
    return null;
  }
  
  /** If a ClassDef defined in this source file is a TestCase class, make sure it is the only thing in the file. */
  public Void forSourceFileDoFirst(SourceFile that) {
    
    for (int i = 0; i < that.getTypes().length; i++) {
      if (that.getTypes()[i] instanceof ClassDef) {
        ClassDef c = (ClassDef) that.getTypes()[i];
        String superName = c.getSuperclass().getName();
        if (superName.equals("TestCase") || superName.equals("junit.framework.TestCase")) {
          // TODO; add code to exclude the following test for FullJava files
          if (that.getTypes().length > 1) {
            _addAndIgnoreError("TestCases must appear in files by themselves in functional code", c);
          }
        }
      }
    }
    return null; 
  }
  
  /** Check to make sure there aren't any immediate errors in this SourceFile by calling the
    * doFirst method.  Then, check to make sure that java.lang is imported, and if it is not, add
    * it to the list of importedpackages, since it is imported by default.  Make a list of all classes
    * defined in this file.
    * Then, visit them one by one.
    */
  public Void forSourceFile(SourceFile that) {
//    System.err.println("Processing source file " + that.getSourceInfo().getFile());
    forSourceFileDoFirst(that);  // Confirms that TestCase classes appear alone in files
    if (prune(that)) return null;
    
    // The parser enforces that there is either zero or one PackageStatement.
    for (int i = 0; i < that.getPackageStatements().length; i++) that.getPackageStatements()[i].visit(this);
    for (int i = 0; i < that.getImportStatements().length; i++) that.getImportStatements()[i].visit(this);
    if (! _importedPackages.contains("java.lang")) _importedPackages.addFirst("java.lang");
    
    TypeDefBase[] types = that.getTypes();
    // store the qualified names of all classes defined in this file in:
    _classNamesInThisFile = new LinkedList<String>();
    for (int i = 0; i < types.length; i++) {
      // TODO: Add static inner classes to this list?
      
      String qualifiedClassName = getQualifiedClassName(types[i].getName().getText());
      _classNamesInThisFile.addFirst(qualifiedClassName);
//      System.err.println("Adding " + qualifiedClassName + " to _classesToBeParsed");
      _log.log("Adding " + qualifiedClassName + " to _classesToBeParsed");
      _classesToBeParsed.put(qualifiedClassName, new Pair<TypeDefBase, LanguageLevelVisitor>(types[i], this));
    }
    
    for (int i = 0; i < types.length; i++) {
      // Remove the class that is about to be visited from the list of ClassDefs in this file.
      String qualifiedClassName = getQualifiedClassName(types[i].getName().getText());
      // Only visit a class if _classesToBeParsed contains it.  Otherwise, this class has 
      // already been resolved since it was a superclass of a previous class.
      if (_classesToBeParsed.containsKey(qualifiedClassName)) {
        types[i].visit(this);
      }
    }
    
    return forSourceFileOnly(that);
  }
  
  /** Call the ResolveNameVisitor to see if this is a reference to a Type name. */
  public Void forSimpleNameReference(SimpleNameReference that) {
    that.visit(new ResolveNameVisitor());
    return null;
  }
  
  /** Call the ResolveNameVisitor to see if this is a reference to a Type name. */
  public Void forComplexNameReference(ComplexNameReference that) {
    that.visit(new ResolveNameVisitor());
    return null;
  }
  
  /** Do nothing.  This is handled in the forVariableDeclarationOnly case.*/
  public Void forVariableDeclaration(VariableDeclaration that) {
    forVariableDeclarationDoFirst(that);
    if (prune(that)) return null;
    that.getMav().visit(this);
    return forVariableDeclarationOnly(that);
  }
  
  /** If the method being generated already exists in the SymbolData,
    * throw an error, because generated methods cannot be overwritten.
    */
  protected static void addGeneratedMethod(SymbolData sd, MethodData md) {
    MethodData rmd = SymbolData.repeatedSignature(sd.getMethods(), md);
    if (rmd == null) {
      sd.addMethod(md, true);
      md.setGenerated(true);
    }
    
    else if (!(getUnqualifiedClassName(sd.getName()).equals(md.getName()))) {
      //if it is not a constructor, it cannot be overridden--give an error
      _addAndIgnoreError("The method " + md.getName() + " is automatically generated, and thus you cannot override it", 
                         rmd.getJExpression());
    }
  }
  
  /** Creates the automatically generated constructor for this class.  It needs to take in the same arguments as its 
    * super class' constructor as well as its fields.  If there are multiple constructors in the super class, pick the
    * one with the least number of parameters. No constructor is created if this is an advanced level file (overridden 
    * at advanced level), because no code augmentation is done.
    */
  public void createConstructor(SymbolData sd) {
    if (LanguageLevelConverter.isAdvancedFile(_file)) return;
    
    SymbolData superSd = sd.getSuperClass();
    
    //there was an error somewhere else.  just return.
    if (sd.isContinuation()) return;
    
    LinkedList<MethodData> superMethods = superSd.getMethods();
    String superUnqualifiedName = getUnqualifiedClassName(superSd.getName());
    
    LanguageLevelVisitor sslv = LanguageLevelConverter._newSDs.remove(superSd);
    if (sslv != null) {sslv.createConstructor(superSd);}
    
    // Find the super's smallest constructor.
    MethodData superConstructor = null;
    Iterator<MethodData> iter = superMethods.iterator();
    while (iter.hasNext()) {
      MethodData superMd = iter.next();
      if (superMd.getName().equals(superUnqualifiedName)) {
        if (superConstructor == null || superMd.getParams().length < superConstructor.getParams().length) {
          superConstructor = superMd;
        }
      }
    }
    
    String name = getUnqualifiedClassName(sd.getName());
    MethodData md = new MethodData(name,
                                   new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"public"}), 
                                   new TypeParameter[0], 
                                   sd, 
                                   new VariableData[0], // Parameters to be filled in later. 
                                   new String[0], 
                                   sd,
                                   null);
    
    LinkedList<VariableData> params = new LinkedList<VariableData>();
    if (superConstructor != null) {
      for (VariableData superParam : superConstructor.getParams()) {
        String paramName = md.createUniqueName("super_" + superParam.getName());
        VariableData newParam = 
          new VariableData(paramName, new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[0]), 
                           superParam.getType().getSymbolData(), true, md);
        newParam.setGenerated(true);
        params.add(newParam);
        // Next line done on each iteration so that createUniqueName handles nameless super parameters (in class files)
        md.addVar(newParam); 
      }
    }
    
    // only add in those fields that do not have a value and are not static.
    boolean hasOtherConstructor = sd.hasMethod(name);
    
    for (VariableData field : sd.getVars()) {
      
      if (! field.hasInitializer() && ! field.hasModifier("static")) {
        if (! hasOtherConstructor) { field.gotValue(); } // Set hasValue if no other constructors need to be visited
        // Rather than creating a new parameter, we use the field, since all the important data is the same in both of
        // them.
        params.add(field);
      }
    }
    md.setParams(params.toArray(new VariableData[params.size()]));
    md.setVars(params);
    
    addGeneratedMethod(sd, md);
    LanguageLevelConverter._newSDs.remove(sd); // this won't do anything if sd is not in _newSDs.
  }
  
  /** Create a method that is an accessor for each field in the class.
    * File file is passed in so this can remain a static method
    */
  protected static void createAccessors(SymbolData sd, File file) {
    if (LanguageLevelConverter.isAdvancedFile(file)) return;
    LinkedList<VariableData> fields = sd.getVars();
    Iterator<VariableData> iter = fields.iterator();
    while (iter.hasNext()) {
      VariableData vd = iter.next();      
      if (!vd.hasModifier("static")) { 
        String name = getFieldAccessorName(vd.getName());
        String[] mavStrings;
        mavStrings = new String[] {"public"};
        MethodData md = new MethodData(name,
                                       new ModifiersAndVisibility(SourceInfo.NO_INFO, mavStrings), 
                                       new TypeParameter[0], 
                                       vd.getType().getSymbolData(), 
                                       new VariableData[0],
                                       new String[0], 
                                       sd,
                                       null); // no SourceInfo
        addGeneratedMethod(sd, md);
      }
    }
  }
  
  /** Create a method called toString that returns type String. Overridden at the Advanced Level files, because n code
    * augmentation is done for them so you don't want to create this method.
    */ 
  protected void createToString(SymbolData sd) {
    String name = "toString";
    String[] mavStrings;
    mavStrings = new String[] {"public"};
    //    }
    MethodData md = new MethodData(name,
                                   new ModifiersAndVisibility(SourceInfo.NO_INFO, mavStrings), 
                                   new TypeParameter[0], 
                                   getSymbolData("String", _makeSourceInfo("java.lang.String")), 
                                   new VariableData[0],
                                   new String[0], 
                                   sd,
                                   null); // no SourceInfo
    addGeneratedMethod(sd, md);    
  }
  
  /**
   * Create a method called hashCode that returns an int.
   * Overriden for Advanced Level files, because no code augmentation is done for
   * them, so we don't want to create this method.
   */ 
  protected void createHashCode(SymbolData sd) {    
    String name = "hashCode";
    String[] mavStrings;
    mavStrings = new String[] {"public"};
    MethodData md = new MethodData(name,
                                   new ModifiersAndVisibility(SourceInfo.NO_INFO, mavStrings), 
                                   new TypeParameter[0], 
                                   SymbolData.INT_TYPE, 
                                   new VariableData[0],
                                   new String[0], 
                                   sd,
                                   null); // no SourceInfo
    addGeneratedMethod(sd, md);
  }
  
  /**
   * Create a method called equals() that takes in an Object argument and returns a boolean.
   * Overriden for Advanced Level files, because no code augmentation is done for
   * them, so we don't want to create this method.
   */ 
  protected void createEquals(SymbolData sd) {    
    String name = "equals";
    String[] mavStrings;
    mavStrings = new String[] {"public"};
    SymbolData type = getSymbolData("java.lang.Object", _makeSourceInfo("java.lang.Object"));
    VariableData param = new VariableData(type);
    MethodData md = new MethodData(name,
                                   new ModifiersAndVisibility(SourceInfo.NO_INFO, mavStrings), 
                                   new TypeParameter[0], 
                                   SymbolData.BOOLEAN_TYPE, 
                                   new VariableData[] {param},
                                   new String[0], 
                                   sd,
                                   null); // no SourceInfo
    param.setEnclosingData(md);
    addGeneratedMethod(sd, md);
  }
  
  /**
   * This is overwritten because we don't want to visit each half of
   * MemberType recursively.  Just take the whole thing and look for it in
   * forMemberTypeOnly (calls forTypeOnly eventually to get looked up).
   */
  public Void forMemberType(MemberType that) {
    forMemberTypeDoFirst(that);
    if (prune(that)) return null;
    return forMemberTypeOnly(that);
  }
  
  /**Return the SymbolData for java.lang.String by default*/
  public Void forStringLiteralOnly(StringLiteral that) {
    getSymbolData("String", that.getSourceInfo(), true);
    return null;
  }
  
  /** Try to resolve the type of the instantiation, and make sure there are no errors*/
  public Void forSimpleNamedClassInstantiation(SimpleNamedClassInstantiation that) {
    forSimpleNamedClassInstantiationDoFirst(that);
    if (prune(that)) return null;
    that.getType().visit(this);
    that.getArguments().visit(this);
    
    // Put the allocated type into the symbol table
    /* TODO!: Shouldn't this happen for all Instantiations?
     * Even for all Types, regardless of where they show up?
     */
    getSymbolData(that.getType().getName(), that.getSourceInfo());
    
    return forSimpleNamedClassInstantiationOnly(that);
  }
  
  
  /** Determines whether array1 equals array2 using the equals method on Object[] arrays in java.util.Arrays.
    * @return true if the two array argument (which may be null) are equal.
    */ 
  public static boolean arrayEquals(Object[] array1, Object[] array2) { return Arrays.equals(array1, array2); }
  
  /** Use this to see if a name references a type that needs to be added to the symbolTable. */
  private class ResolveNameVisitor extends JExpressionIFAbstractVisitor<TypeData> {
    
    public ResolveNameVisitor() { }
    
    /** Most expressions are not relevant for this check--visit them with outer visitor. */
    public TypeData defaultCase(JExpressionIF that) {
      that.visit(LanguageLevelVisitor.this);
      return null;
    }
    
    /** Try to look up this simple name reference and match it to a symbol data.
      * If it could not be matched, return a package data.
      * @param that  The thing we're trying to match to a type
      */
    public TypeData forSimpleNameReference(SimpleNameReference that) {
      SymbolData result = getSymbolData(that.getName().getText(), that.getSourceInfo());
      // it could not be resolved: return a Package Data
      if (result == SymbolData.NOT_FOUND) {
        return new PackageData(that.getName().getText());
      }
      return result;
    }
    
    /** Try to look up the enclosing of this complex name reference and then try to match the name on the right
      * within that context to a type.  If it could not be matched, return a package data.
      * @param that  The thing we're trying to match to a type
      */
    public TypeData forComplexNameReference(ComplexNameReference that) {
      TypeData lhs = that.getEnclosing().visit(this);
      SymbolData result = getSymbolData(lhs, that.getName().getText(), that.getSourceInfo(), true);
      
      if (result == SymbolData.NOT_FOUND) { 
        if (lhs instanceof PackageData) {
          return new PackageData((PackageData) lhs, that.getName().getText());
        }
        return null;
      }
      
      return result;
    }
  }
  
  /** Test the methods defined in the above class.*/
  public static class LanguageLevelVisitorTest extends TestCase {
    
    private LanguageLevelVisitor testLLVisitor;
    private Hashtable<SymbolData, LanguageLevelVisitor> testNewSDs;
    
    private SymbolData _sd1;
    private SymbolData _sd2;
    private SymbolData _sd3;
    private SymbolData _sd4;
    private SymbolData _sd5;
    private SymbolData _sd6;
    private ModifiersAndVisibility _publicMav = 
      new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = 
      new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = 
      new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = 
      new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[0]);
    private ModifiersAndVisibility _finalMav = 
      new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[]{"final"});
    
    public LanguageLevelVisitorTest() { this(""); }
    public LanguageLevelVisitorTest(String name) { super(name); }
    
    public void setUp() {
      testLLVisitor = new LanguageLevelVisitor(new File(""), 
                                               "", 
                                               new LinkedList<String>(), 
                                               new LinkedList<String>(), 
                                               new LinkedList<String>(), 
                                               new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());

      errors = new LinkedList<Pair<String, JExpressionIF>>();
      _errorAdded=false;
      LanguageLevelConverter.symbolTable.clear();
      
      testLLVisitor.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>();      
      _hierarchy = new Hashtable<String, TypeDefBase>();
      testLLVisitor._classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
      testLLVisitor._resetNonStaticFields();
      testLLVisitor._importedPackages.add("java.lang");

      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");
    }
    
    /** Tests the getUnqualifiedClassName method. */
    public void testGetUnqualifiedClassName() {
      assertEquals("getUnqualifiedClassName with a qualified name with an inner class", "innermonkey", 
                   testLLVisitor.getUnqualifiedClassName("i.like.monkey$innermonkey"));
      assertEquals("getUnqualifiedClassName with a qualified name", "monkey", 
                   testLLVisitor.getUnqualifiedClassName("i.like.monkey"));
      assertEquals("getUnqualifiedClassName with an unqualified name", "monkey", 
                   testLLVisitor.getUnqualifiedClassName("monkey"));
      assertEquals("getUnqualifiedClassName with an empty string", "", testLLVisitor.getUnqualifiedClassName(""));
    }
    
    public void testClassFile2SymbolData() {
      
      //test a java.lang symbol data
      SymbolData objectSD = testLLVisitor._classFile2SymbolData("java.lang.Object", "");
      SymbolData stringSD = testLLVisitor._classFile2SymbolData("java.lang.String", "");
      MethodData md = new MethodData("substring", _publicMav, new TypeParameter[0], stringSD, 
                                     new VariableData[] {new VariableData(SymbolData.INT_TYPE)},
                                     new String[0], stringSD, null);
      assertTrue("java.lang.String should have been converted successfully", 
                 stringSD.getName().equals("java.lang.String"));
      assertEquals("java.lang.String's superClass should should be java.lang.Object", 
                   objectSD,
                   stringSD.getSuperClass());
      
      LinkedList<MethodData> methods = stringSD.getMethods();
      Iterator<MethodData> iter = methods.iterator();
      boolean found = false;
      
      while (iter.hasNext()) {
        MethodData currMd = iter.next();
        if (currMd.getName().equals("substring") && currMd.getParams().length == 1 && 
            currMd.getParams()[0].getType() == SymbolData.INT_TYPE.getInstanceData()) {
          found = true;
          md.getParams()[0].setEnclosingData(currMd);
          break;
        }
      }
      
      assertTrue("Should have found method substring(int) in java.lang.String", found);
      
      assertEquals("java.lang.String should be packaged correctly", "java.lang", 
                   testLLVisitor.getSymbolData("java.lang.String", SourceInfo.NO_INFO).getPackage());
      
      //now, test that a second call to the same method won't replace the symbol data that is already there.
      SymbolData newStringSD = testLLVisitor._classFile2SymbolData("java.lang.String", "");
      assertTrue("Second call to classFileToSymbolData should not change sd in hash table.", 
                 stringSD == testLLVisitor.symbolTable.get("java.lang.String"));
      assertTrue("Second call to classFileToSymbolData should return same SD.", 
                 newStringSD == testLLVisitor.symbolTable.get("java.lang.String"));      
      //now, test one of our own small class files.
      
      SymbolData bartSD = testLLVisitor._classFile2SymbolData("Bart", "testFiles");
      assertFalse("bartSD should not be null", bartSD == null);
      assertFalse("bartSD should not be a continuation", bartSD.isContinuation());
      MethodData md1 = 
        new MethodData("myMethod", _protectedMav, 
                       new TypeParameter[0], SymbolData.BOOLEAN_TYPE, 
                       new VariableData[] { new VariableData(SymbolData.INT_TYPE) }, 
                       new String[] {"java.lang.Exception"}, bartSD, null);
      
      md1.getParams()[0].setEnclosingData(bartSD.getMethods().getLast());
      MethodData md2 = new MethodData("Bart", _publicMav, new TypeParameter[0], bartSD,
                                      new VariableData[0], new String[0], bartSD, null);
      
      VariableData vd1 = new VariableData("i", _publicMav, SymbolData.INT_TYPE, true, bartSD);
      
      LinkedList<MethodData> bartsMD = new LinkedList<MethodData>();
      bartsMD.addFirst(md1);
      bartsMD.addFirst(md2);
      
      LinkedList<VariableData> bartsVD = new LinkedList<VariableData>();
      bartsVD.addLast(vd1);
      
      assertEquals("Bart's super class should be java.lang.Object: errors = " + errors, objectSD, 
                   bartSD.getSuperClass());
      assertEquals("Bart's Variable Data should be a linked list containing only vd1", bartsVD, bartSD.getVars());
      assertEquals("The first method data of bart's should be correct", md2, bartSD.getMethods().getFirst());
      
      assertEquals("The second method data of bart's should be correct", md1, bartSD.getMethods().getLast());
      assertEquals("Bart's Method Data should be a linked list containing only md1", bartsMD, bartSD.getMethods());
    }
    
    public void testLookupFromClassesToBeParsed() {
      // Create a ClassDef.  Recreate the ClassOrInterfaceType for Object instead of using 
      // JExprParser.NO_TYPE since otherwise the ElementaryVisitor will complain that the
      // user must explicitly extend Object.
      ClassDef cd = 
        new ClassDef(SourceInfo.NO_INFO, _publicMav, 
                     new Word(SourceInfo.NO_INFO, "Lisa"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(SourceInfo.NO_INFO, "Object", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      
      // Use a ElementaryVisitor so lookupFromClassesToBeParsed will actually visit the ClassDef.
      IntermediateVisitor bv = new IntermediateVisitor(new File(""), 
                                                       errors, 
                                                       continuations, 
                                                       new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>());
      
      // Test that passing resolve equals false returns a continuation.
      assertTrue("Should return a continuation", 
                 testLLVisitor._lookupFromClassesToBeParsed("Lisa", SourceInfo.NO_INFO, false).isContinuation());
      // Put Lisa in the hierarchy and test that there is one error and that the message
      // says that there is cyclic inheritance.
//      _hierarchy.put("Lisa", cd);
//      _classesToBeParsed.put("Lisa", new Pair<TypeDefBase, LanguageLevelVisitor>(cd, bv));
//      assertEquals("Should return null because Lisa is in the hierarchy", 
//                   null,
//                   _llv._lookupFromClassesToBeParsed("Lisa", SourceInfo.NO_INFO, true));
//      assertEquals("Should be one error", 1, errors.size());
//      assertEquals("Error message should be correct", "Cyclic inheritance involving Lisa", errors.get(0).getFirst());
//      _hierarchy.remove("Lisa");
      //Re-add Lisa because the first call with resolve set to true removed it and
      // test that Lisa is actually visited and added to the symbolTable.
      testLLVisitor._classesToBeParsed.put("Lisa", new Pair<TypeDefBase, LanguageLevelVisitor>(cd, bv));
      assertFalse("Should return a non-continuation", 
                  testLLVisitor._lookupFromClassesToBeParsed("Lisa", 
                                                    SourceInfo.NO_INFO,
                                                    true).isContinuation());
    }
    
    public void testGetSymbolDataForClassFile() {
      // Test that passing a legal class return a non-continuation.
      assertFalse("Should return a non-continuation", 
                  testLLVisitor.getSymbolDataForClassFile("java.lang.String", SourceInfo.NO_INFO).isContinuation());
      
      // Test that passing a userclass that can't be found returns null and adds an error.
      assertEquals("Should return null with a user class that can't be found",
                   null,
                   testLLVisitor.getSymbolDataForClassFile("Marge", SourceInfo.NO_INFO));
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "Class Marge not found.", 
                   errors.get(0).getFirst());
    }
    
    
    public void testGetSymbolData_Primitive() {
      assertEquals("should be boolean type", SymbolData.BOOLEAN_TYPE, 
                   testLLVisitor._getSymbolData_Primitive("boolean"));
      assertEquals("should be char type", SymbolData.CHAR_TYPE, testLLVisitor._getSymbolData_Primitive("char"));
      assertEquals("should be byte type", SymbolData.BYTE_TYPE, testLLVisitor._getSymbolData_Primitive("byte"));
      assertEquals("should be short type", SymbolData.SHORT_TYPE, testLLVisitor._getSymbolData_Primitive("short"));
      assertEquals("should be int type", SymbolData.INT_TYPE, testLLVisitor._getSymbolData_Primitive("int"));
      assertEquals("should be long type", SymbolData.LONG_TYPE, testLLVisitor._getSymbolData_Primitive("long"));
      assertEquals("should be float type", SymbolData.FLOAT_TYPE, testLLVisitor._getSymbolData_Primitive("float"));
      assertEquals("should be double type", SymbolData.DOUBLE_TYPE, testLLVisitor._getSymbolData_Primitive("double"));
      assertEquals("should be void type", SymbolData.VOID_TYPE, testLLVisitor._getSymbolData_Primitive("void"));
      assertEquals("should be null type", SymbolData.NULL_TYPE, testLLVisitor._getSymbolData_Primitive("null"));
      assertEquals("should return null--not a primitive", null, 
                   testLLVisitor._getSymbolData_Primitive("java.lang.String"));
    }
    
    public void testGetQualifiedSymbolData() {
      testLLVisitor._file = new File("testFiles/Fake.dj0");
      SymbolData sd = new SymbolData("testPackage.File");
      testLLVisitor._package = "testPackage";
      LanguageLevelConverter.symbolTable.put("testPackage.File", sd);
      
      SymbolData sd1 = new SymbolData("java.lang.String");
      LanguageLevelConverter.symbolTable.put("java.lang.String", sd1);
      
      //Test that classes not in the symbol table are handled correctly.
      assertEquals("should the continuation symbol", sd, 
                   testLLVisitor._getQualifiedSymbolData("testPackage.File", SourceInfo.NO_INFO, true, false, true));
//      assertEquals("should be one error so far.", 1, errors.size());
      
      
      SymbolData sd2 = testLLVisitor._getQualifiedSymbolData("java.lang.Integer", SourceInfo.NO_INFO, true, true, true);
      assertEquals("should return non-continuation java.lang.Integer", "java.lang.Integer", sd2.getName());
      assertFalse("should not be a continuation.", sd2.isContinuation());
      
      SymbolData sd3 = testLLVisitor._getQualifiedSymbolData("Wow", SourceInfo.NO_INFO, true, true, true);
      assertEquals("search should fail", null, sd3);
//      assertEquals("should return Wow", "Wow", sd3.getName());
//      assertFalse("Should not be a continuation.", sd3.isContinuation());
      
      // "testPackage.File" has been entered as a continuation in symbolTable.  Why should the following lookup fail?
//      //Test that classes in the symbol table are handled correctly
//      assertEquals("should return null sd--does not exist", null, 
//                   _llv._getQualifiedSymbolData("testPackage.File", SourceInfo.NO_INFO, false, false, true));
//      assertEquals("Should be 1 error", 1, errors.size());
      
      sd.setIsContinuation(false);
      assertEquals("should return non-continuation sd", sd, 
                   testLLVisitor._getQualifiedSymbolData("testPackage.File", SourceInfo.NO_INFO, true, false,  true));
      
      
      assertEquals("Should return sd1.", sd1, 
                   testLLVisitor._getQualifiedSymbolData("java.lang.String", SourceInfo.NO_INFO, true, false, true));
      assertFalse("sd1 should no longer be a continuation.", sd1.isContinuation());
      
      
      
      //check that stuff not in symbol table and packaged incorrectly is handled right.
      assertEquals("should return null-because it's not a valid class", null, 
                   testLLVisitor._getQualifiedSymbolData("testPackage.not.in.symboltable", 
                                                   SourceInfo.NO_INFO, true, false, true));
      
      assertEquals("should be two errors so far.", 2, errors.size());
      assertNull("should return null", 
                 testLLVisitor._getQualifiedSymbolData("testPackage.not.in.symboltable", 
                                                 SourceInfo.NO_INFO, false, false, false));
      
      assertNull("should return null.", 
                 testLLVisitor._getQualifiedSymbolData("notRightPackage", SourceInfo.NO_INFO, false, false, false));
      assertEquals("should still be two errors.", 2, errors.size());
    }
    
    public void testGetArraySymbolData() {
      //Initially, force the inner sd of this array type to be null, to test that.
      assertEquals("Should return null, because inner sd is null.", null, 
                   testLLVisitor._getArraySymbolData("TestFile[]", SourceInfo.NO_INFO, false, false, false, false));
      
      /**Now, put a real SymbolData base in the table.*/
      SymbolData sd = new SymbolData("Iexist");
      LanguageLevelConverter.symbolTable.put("Iexist", sd);
      testLLVisitor._getArraySymbolData("Iexist[]", SourceInfo.NO_INFO, false, false, false, false).getName();
      assertTrue("Should have created an array data and add it to symbol table.", 
                 LanguageLevelConverter.symbolTable.containsKey("Iexist[]"));
      SymbolData ad = LanguageLevelConverter.symbolTable.get("Iexist[]");
      
      //make sure that ad has the appropriate fields and super classes and interfaces and methods
      assertEquals("Should only have field 'length'", 1, ad.getVars().size());
      assertNotNull("Should contain field 'length'", ad.getVar("length"));
      
      assertEquals("Should only have one method-clone", 1, ad.getMethods().size());
      assertTrue("Should contain method clone", ad.hasMethod("clone"));
      
      assertEquals("Should have Object as super class", 
                   LanguageLevelConverter.symbolTable.get("java.lang.Object"), 
                   ad.getSuperClass());
      assertEquals("Should have 2 interfaces", 2, ad.getInterfaces().size());
      assertEquals("Interface 1 should be java.lang.Cloneable", "java.lang.Cloneable", 
                   ad.getInterfaces().get(0).getName());
      assertEquals("Interface 2 should be java.io.Serializable", "java.io.Serializable", 
                   ad.getInterfaces().get(1).getName());
      
      
      /**Now try it with the full thing already in the symbol table.*/
      assertEquals("Since it's already in symbol table now, should just return it.", ad, 
                   testLLVisitor._getArraySymbolData("Iexist[]", SourceInfo.NO_INFO, false, false, false, false));
      
      /**Now, try it with a multiple dimension array.*/
      testLLVisitor._getArraySymbolData("Iexist[][]", SourceInfo.NO_INFO, false, false, false, false);
      assertTrue("Should have added a multidimensional array to the table.", 
                 LanguageLevelConverter.symbolTable.containsKey("Iexist[][]"));
      
      SymbolData sd2 = new SymbolData("String");
      LanguageLevelConverter.symbolTable.put("String", sd2);
      testLLVisitor._getArraySymbolData("String[][]", SourceInfo.NO_INFO, false, false, false, false);
      assertTrue("Should have added String[] to table", LanguageLevelConverter.symbolTable.containsKey("String[]"));
      assertTrue("Should have added String[][] to table", LanguageLevelConverter.symbolTable.containsKey("String[][]"));
    }
    
    public void testGetSymbolData_FromCurrFile() {
      _sd4.setIsContinuation(false);
      _sd6.setIsContinuation(true);
      LanguageLevelConverter.symbolTable.put("u.like.emu", _sd4);
      LanguageLevelConverter.symbolTable.put("cebu", _sd6);
      
      // Test if it's already in the symbol table and doesn't need to be resolved
      // not stopping when it should.  get error b/c not in classes to be parsed 
      // assertEquals("symbol data is not a continuation, so should just be returned.", _sd6, 
      //   _llv._getSymbolData_FromCurrFile("cebu", SourceInfo.NO_INFO, true));
      assertEquals("symbol data is a continuation, but resolve is false, so should just be returned.", _sd4, 
                   testLLVisitor._getSymbolData_FromCurrFile("u.like.emu", SourceInfo.NO_INFO, false));
      
      //test if it needs to be resolved:
      ClassDef cd = 
        new ClassDef(SourceInfo.NO_INFO, _publicMav, 
                     new Word(SourceInfo.NO_INFO, "Lisa"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(SourceInfo.NO_INFO, "Object", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      
      // Use a ElementaryVisitor so lookupFromClassesToBeParsed will actually visit the ClassDef.
      IntermediateVisitor bv = new IntermediateVisitor(new File(""));
      
      testLLVisitor. _classesToBeParsed.put("Lisa", new Pair<TypeDefBase, LanguageLevelVisitor>(cd, bv));
      assertFalse("Should return a non-continuation", 
                  testLLVisitor._getSymbolData_FromCurrFile("Lisa", SourceInfo.NO_INFO, true).isContinuation());
    }
    
    public void testGetSymbolData_FromFileSystem() {
      //what if it is in classes to be parsed?
      //and what if the class we're looking up is in the same package as the current file?
      //qualified
      testLLVisitor._package="fully.qualified";
      testLLVisitor._file = new File("testFiles/fully/qualified/Fake.dj0");
      SymbolData sd2 = new SymbolData("fully.qualified.Woah");  // continuation
      testLLVisitor.symbolTable.put("fully.qualified.Woah", sd2);
      
      SymbolData result = 
        testLLVisitor._getSymbolData_FromFileSystem("fully.qualified.Woah", SourceInfo.NO_INFO, false, true);
      
      assertEquals("Should return sd2, unresolved.", sd2, result);
      assertTrue("sd2 should still be unresolved", sd2.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
//      result = _llv._getSymbolData_FromFileSystem("fully.qualified.Woah", SourceInfo.NO_INFO, false, true);
//      assertEquals("Should return sd2, now unresolved.", sd2, result);
//      assertTrue("sd2 should not be resolved", sd2.isContinuation());
//      assertEquals("Should be no errors", 0, errors.size());
      
//      result = _llv._getSymbolData_FromFileSystem("fully.qualified.Woah", SourceInfo.NO_INFO, true, true);
//      assertEquals("Should return sd2, now resolved.", sd2, result);
//      assertFalse("sd2 should now be resolved", sd2.isContinuation());   
//      assertEquals("Should be no errors", 0, errors.size());
      
      
      //what if the files are in different packages
      testLLVisitor.symbolTable.remove("fully.qualified.Woah");
      testLLVisitor.visitedFiles.clear();
      testLLVisitor._package="another.package";
      testLLVisitor._file = new File("testFiles/another/package/Wowsers.dj0");
      sd2 = new SymbolData("fully.qualified.Woah");
      testLLVisitor.symbolTable.put("fully.qualified.Woah", sd2);
      
      result = testLLVisitor._getSymbolData_FromFileSystem("fully.qualified.Woah", SourceInfo.NO_INFO, false, true);
      
      assertEquals("Should return sd2, unresolved.", sd2, result);
      assertTrue("sd2 should still be unresolved", sd2.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
//      result = _llv._getSymbolData_FromFileSystem("fully.qualified.Woah", SourceInfo.NO_INFO, true, true);
//      assertEquals("Should return sd2, now resolved.", sd2, result);
      
//      assertFalse("sd2 should be resolved", sd2.isContinuation());
//      assertEquals("Should be no errors", 0, errors.size());
      
      //what if there is no package
      //Now, check cases when desired SymbolData is in the symbol table.
      testLLVisitor._package = "";
      testLLVisitor._file = new File ("testFiles/Cool.dj0");  // non-existent file
      
      //unqualified
      SymbolData sd1 = new SymbolData("Wow");
      SymbolData obj = testLLVisitor._getSymbolData_FromFileSystem("java.lang.Object", SourceInfo.NO_INFO, true, true);
      sd1.setSuperClass(obj);
      testLLVisitor.symbolTable.put("Wow", sd1);
      
      result = testLLVisitor._getSymbolData_FromFileSystem("Wow", SourceInfo.NO_INFO, false, true);
      assertEquals("Should return sd1, unresolved.", sd1, result);
      assertTrue("sd1 should still be unresolved.", sd1.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
      result = testLLVisitor._getSymbolData_FromFileSystem("Wow", SourceInfo.NO_INFO, true, true);
      assertEquals("Should return sd1, resolved.", sd1, result);
      assertFalse("sd1 should be resolved.", sd1.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
      result = testLLVisitor._getSymbolData_FromFileSystem("Wow", SourceInfo.NO_INFO, true, true);
      assertEquals("Should return sd1.", sd1, result);
      assertFalse("sd1 should still be resolved.", sd1.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
      
      //finding the most recent file
      result = testLLVisitor._getSymbolData_FromFileSystem("James", SourceInfo.NO_INFO, true, true);
      assertEquals("Search for James should fail", null, result);
//      assertEquals("Result should have 3 variables.", 3, result.getVars().size());
//      assertEquals("Should be no errors", 0, errors.size());
      
      //returning NOT_FOUND when it doesn't exist.
      testLLVisitor._package = "myPackage";
      assertEquals("Should return NOT_FOUND-does not exist.", 
                   SymbolData.NOT_FOUND, 
                   testLLVisitor._getSymbolData_FromFileSystem("WrongPackage.className", 
                                                               SourceInfo.NO_INFO, true, false));
      assertEquals("Should be no errors", 0, errors.size());
      
      // Now, test case where class file still exists, but java file is gone.
      testLLVisitor._package = "";
      testLLVisitor._file = new File("testFiles/Fake.dj0");
      LinkedList<VariableData> vds = new LinkedList<VariableData>();
      result = testLLVisitor._getSymbolData_FromFileSystem("Doh", SourceInfo.NO_INFO, true, true);
      vds.addLast(new VariableData("i", _packageMav, SymbolData.INT_TYPE, true, result));
      vds.addLast(new VariableData("o", _packageMav, obj, true, result));
// Since some list elements are arrays, comparison test is suspect
//      assertEquals("should have correct variable datas", vds, result.getVars());
//      assertFalse("should not be a continuation", result.isContinuation());
      
      //Now test case where java file has been updated more recently than class file.
      //TODO: How can we test this since repository is checked out (i.e. the files all have the same timestamp)?
    }
    
    public void testGetSymbolData() {
      testLLVisitor._package="";
      testLLVisitor._file = new File("testFiles/akdjskj");
      
      // No dot case
      SymbolData sd1 = new SymbolData("Wow");
      testLLVisitor.symbolTable.put("Wow", sd1);
      assertEquals("Should return an equal SymbolData", 
                   sd1, testLLVisitor.getSymbolData("Wow", SourceInfo.NO_INFO, true, false));
      assertFalse("Should not be a continuation", sd1.isContinuation());  // There is a pre-existing class file Wow!
      
      // Invalid case
      SymbolData result = testLLVisitor.getSymbolData("ima.invalid", SourceInfo.NO_INFO, true, false);
      assertEquals("Should return null-invalid class name", null, result);
      assertEquals("There should be one error", 1, testLLVisitor.errors.size());
      assertEquals("The error message should be correct", "Invalid class name ima.invalid", errors.get(0).getFirst());
      
      // Fully qualified class name
      testLLVisitor._package="fully.qualified";
      testLLVisitor._file = new File("testFiles/fully/qualified/Fake.dj0");
      SymbolData sd2 = new SymbolData("fully.qualified.Symbol");
      testLLVisitor.symbolTable.put("fully.qualified.Symbolh", sd2);
      
      result = testLLVisitor.getSymbolData("fully.qualified.Symbol", SourceInfo.NO_INFO, true, false);
      
      assertEquals("Should return sd2, resolved.", sd2, result);
      assertTrue("sd2 should be resolved", sd2.isContinuation());
      
      // Inner class
      sd1.setName("fully.qualified.Woah.Wow");
      sd2.addInnerClass(sd1);
      sd1.setOuterData(sd2);
      testLLVisitor.symbolTable.put("fully.qualified.Woah.Wow", sd1);
      testLLVisitor.symbolTable.remove("Wow");
      sd1.setIsContinuation(false);
      result = testLLVisitor.getSymbolData("fully.qualified.Woah.Wow", SourceInfo.NO_INFO, true, false);
      assertEquals("Should return sd1 (the inner class!)", sd1, result);
      
      // Inner inner class
      SymbolData sd3 = new SymbolData("fully.qualified.Woah.Wow.James");
      sd1.addInnerClass(sd3);
//      System.err.println("SYMBOL TABLE ENTRY FOR \"fully.qualified.Woah.Wow.James\" = " + 
//                         _llv.symbolTable.get("fully.qualified.Woah.Wow.James"));
      sd3.setOuterData(sd1);
//      System.err.println("INNER CLASS LOOKUP YIELDS: " + sd1.getInnerClassOrInterface("James"));
      result = testLLVisitor.getSymbolData("fully.qualified.Woah.Wow.James", SourceInfo.NO_INFO, true, false);
      assertEquals("Should return sd3", sd3, result);
    }
    
    
    public void testGetSymbolDataHelper() {
      // Primitive types
      assertEquals("should return the int SymbolData", SymbolData.INT_TYPE, 
                   testLLVisitor.getSymbolDataHelper("int", SourceInfo.NO_INFO, true, true, true, true));
      assertEquals("should return the byte SymbolData", SymbolData.BYTE_TYPE, 
                   testLLVisitor.getSymbolDataHelper("byte", SourceInfo.NO_INFO, false, false, false, true));
      
      // Array types
      ArrayData ad = new ArrayData(SymbolData.INT_TYPE, testLLVisitor, SourceInfo.NO_INFO);
      SymbolData result = testLLVisitor.getSymbolDataHelper("int[]", SourceInfo.NO_INFO, true, true, true, true);
      ad.getVars().get(0).setEnclosingData(result);  //.equals(...) on VariableData compares enclosing datas with ==.
      ad.getMethods().get(0).setEnclosingData(result.getMethods().get(0).getEnclosingData()); //similar hack
      assertEquals("should return the array type", ad, result);
      
      // Qualified types
      SymbolData sd = new SymbolData("java.lang.System");
      LanguageLevelConverter.symbolTable.put("java.lang.System", sd);
      assertEquals("should return the same sd", sd, 
                   testLLVisitor.getSymbolDataHelper("java.lang.System", SourceInfo.NO_INFO, false, true, true, true));
      assertTrue("should be a continuation", sd.isContinuation());
      assertEquals("should return the now resolved sd", sd, 
                   testLLVisitor.getSymbolDataHelper("java.lang.System", SourceInfo.NO_INFO, true, false, true, true));
      assertFalse("should not be a continuation", sd.isContinuation());
      
      // In this file
      sd = new SymbolData("fully.qualified.Qwerty");
      LanguageLevelConverter.symbolTable.put("fully.qualified.Qwerty", sd);
      testLLVisitor._classNamesInThisFile.addLast("fully.qualified.Qwerty");
      // Use a ElementaryVisitor so lookupFromClassesToBeParsed will actually visit the ClassDef.
      IntermediateVisitor bv = new IntermediateVisitor(new File(""), 
                                                       errors, 
                                                       continuations, 
                                                       new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>());
      bv._package = "fully.qualified";
      bv._file = new File("testFiles/fully/qualified/Fake.dj0");
      ClassDef cd = new ClassDef(SourceInfo.NO_INFO, 
                                 _packageMav, 
                                 new Word(SourceInfo.NO_INFO, "Qwerty"),
                                 new TypeParameter[0],
                                 new ClassOrInterfaceType(SourceInfo.NO_INFO, "Object", new Type[0]),
                                 new ReferenceType[0], 
                                 new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      bv._classesToBeParsed.put("fully.qualified.Qwerty", new Pair<TypeDefBase, LanguageLevelVisitor>(cd, bv));
      assertEquals("should return sd the continuation", sd, 
                   bv.getSymbolDataHelper("Qwerty", SourceInfo.NO_INFO, false, true, true, true));
      assertTrue("should be a continuation", sd.isContinuation());
      assertEquals("should return sd, now resolved", sd, 
                   bv.getSymbolDataHelper("Qwerty", SourceInfo.NO_INFO, true, true, true, true));
      assertFalse("should not be a continuation", sd.isContinuation());
      
      // Imported files
      testLLVisitor._importedFiles.addLast("a.b.c");
      sd = new SymbolData("a.b.c");
      LanguageLevelConverter.symbolTable.put("a.b.c.", sd);
      assertEquals("should find the continuation in the symbol table", sd, 
                   testLLVisitor.getSymbolDataHelper("c", SourceInfo.NO_INFO, false, true, true, true));
      assertTrue("should be a continuation", sd.isContinuation());
      
      testLLVisitor._package="fully.qualified";
      testLLVisitor._file = new File("testFiles/fully/qualified/Fake.dj0");
      testLLVisitor._importedFiles.addLast("fully.qualified.Woah");
      SymbolData sd2 = new SymbolData("fully.qualified.Woah");
      sd2.setIsContinuation(false);
      LanguageLevelConverter.symbolTable.put("fully.qualified.Woah", sd2);
      result = testLLVisitor.getSymbolDataHelper("Woah", SourceInfo.NO_INFO, true, false, true, true);
//      System.err.println("result for 'fully.qualifed.Woah' is " + result);
      assertEquals("should find the resolved symbol data in the symbol table", sd2, result);
      assertFalse("should not be a continuation", sd2.isContinuation());
      
      // File System
      testLLVisitor._importedFiles.clear();
      testLLVisitor.visitedFiles.clear();
      LanguageLevelConverter.symbolTable.remove("fully.qualified.Woah");
      sd2 = new SymbolData("fully.qualified.Woah");
      LanguageLevelConverter.symbolTable.put("fully.qualified.Woah", sd2);
      
//      System.err.println("_llv.getSymbolData for fully.qualified.Woah = " +
//                         _llv.getSymbolDataHelper("fully.qualified.Woah", 
//      SourceInfo.NO_INFO, true, true, true, true));
     
      result = testLLVisitor.getSymbolDataHelper("Woah", SourceInfo.NO_INFO, false, false, true, true);
      
      assertEquals("Should return sd2, unresolved.", sd2, result);
      assertTrue("sd2 should still be unresolved", sd2.isContinuation());
      
      result = testLLVisitor.getSymbolDataHelper("Woah", SourceInfo.NO_INFO, false, false, true, true);
      assertEquals("Should return sd2, now unresolved.", sd2, result);
      assertTrue("sd2 should not be resolved", sd2.isContinuation());
      
      // The following "test" forces the definition of "Woah" to be retrieved from the file system but THERE IS NO CLASS
      // FILE so the file system search returns null!
//      result = _llv.getSymbolDataHelper("Woah", SourceInfo.NO_INFO, true, false, true, true);
//      assertEquals("Should return sd2, now resolved.", sd2, result);
//      assertFalse("sd2 should now be resolved", sd2.isContinuation());
      
      // Imported Packages
      LanguageLevelConverter.symbolTable.remove("fully.qualified.Woah");
      testLLVisitor.visitedFiles.clear();
      testLLVisitor._file = new File("testFiles/Fake.dj0");
      testLLVisitor._package = "";
      testLLVisitor._importedPackages.addLast("fully.qualified");
      sd2 = new SymbolData("fully.qualified.Woah");
      LanguageLevelConverter.symbolTable.put("fully.qualified.Woah", sd2);
      assertEquals("should find the unresolved symbol data in the symbol table", sd2, 
                   testLLVisitor.getSymbolDataHelper("Woah", SourceInfo.NO_INFO, false, false, true, true));
      assertTrue("should not be a continuation", sd2.isContinuation());
      
      sd2.setIsContinuation(false);
      result = testLLVisitor.getSymbolDataHelper("Woah", SourceInfo.NO_INFO, true, false, true, true);
      assertEquals("should find the resolved symbol data in the symbol table", sd2, result);
      assertFalse("should not be a continuation", sd2.isContinuation());
      
      //test java.lang classes that need to be looked up
      //want to resolve
      SymbolData stringSD = new SymbolData("java.lang.String");
      SymbolData newsd1 = testLLVisitor.getSymbolDataHelper("String", SourceInfo.NO_INFO, true, true, true, true);
      assertEquals("should have correct name.", stringSD.getName(), newsd1.getName());
      assertFalse("should not be a continuation", newsd1.isContinuation());
      
      // Test ambiguous class name (i.e. it is unqualified, and matches unqualified names in 2 or more packages.
      LanguageLevelConverter.symbolTable.put("random.package.String", new SymbolData("random.package.String"));
      LanguageLevelConverter.symbolTable.put("java.lang.Object", new SymbolData("java.lang.Object"));
      testLLVisitor._importedPackages.addLast("random.package");
      result = testLLVisitor.getSymbolDataHelper("String", SourceInfo.NO_INFO, true, true, true, true);
      assertEquals("Result should be null", null, result);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "The class name String is ambiguous." + 
                   "  It could be java.lang.String or random.package.String", 
                   errors.get(0).getFirst());
      
      LanguageLevelConverter.symbolTable.remove("random.package.String");
      
    }
    
    public void test_forModifiersAndVisibility() {
      // Test access specifiers.
      testLLVisitor.forModifiersAndVisibility(_publicMav);
      testLLVisitor.forModifiersAndVisibility(_protectedMav);
      testLLVisitor.forModifiersAndVisibility(_privateMav);
      testLLVisitor.forModifiersAndVisibility(_packageMav);
      
      
      assertEquals("There should be no errors.", 0, errors.size());
      
      // Test "public", "private"
      ModifiersAndVisibility testMav = 
        new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"public", "private"});
      testLLVisitor.forModifiersAndVisibility(testMav);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "Illegal combination of modifiers." + 
                   " Can't use private and public together.", errors.get(0).getFirst());
      
      // Test "public", "abstract"
      testMav = new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"public", "abstract"});
      testLLVisitor.forModifiersAndVisibility(testMav);
      assertEquals("Still only one error.", 1, errors.size());
      
      // Test "abstract", "final"
      testMav = new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"abstract", "final"});
      testLLVisitor.forModifiersAndVisibility(testMav);
      assertEquals("There should be two errors.", 2, errors.size());
      assertEquals("The error message should be correct.", "Illegal combination of modifiers." + 
                   " Can't use final and abstract together.", errors.get(1).getFirst());
      
      // Test "final", "abstract"
      testMav = new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"final", "abstract"});
      testLLVisitor.forModifiersAndVisibility(testMav);
      assertEquals("There should still be two errors.", 2, errors.size());  // Generated error is duplicate
      assertEquals("The error message should be correct.", "Illegal combination of modifiers." + 
                   " Can't use final and abstract together.", errors.get(1).getFirst());
      
      // Test "volatile", "final"
      testMav = new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"volatile", "final"});
      testLLVisitor.forModifiersAndVisibility(testMav);
      assertEquals("There should be three errors.", 3, errors.size());  // Generated one new error
      assertEquals("The error message should be correct.", "Illegal combination of modifiers." + 
                   " Can't use final and volatile together.", errors.get(2).getFirst());
      
      // Test "static", "final", "static"
      testMav = new ModifiersAndVisibility(SourceInfo.NO_INFO, new String[] {"static", "final", "static"});
      testLLVisitor.forModifiersAndVisibility(testMav);
      assertEquals("There should be four errors.", 4, errors.size());  // Generated one new error
      assertEquals("The error message should be correct.", "Duplicate modifier: static", errors.get(3).getFirst());
    }
    
    public void testGetQualifiedClassName() {
      //first test when the package is empty:
      testLLVisitor._package="";
      assertEquals("Should not change qualified name.", "simpson.Bart", 
                   testLLVisitor.getQualifiedClassName("simpson.Bart"));
      assertEquals("Should not change unqualified name.", "Lisa", testLLVisitor.getQualifiedClassName("Lisa"));
      
      //now test when package is not empty.
      testLLVisitor._package="myPackage";
      assertEquals("Should not change properly packaged qualified name.", "myPackage.Snowball", 
                   testLLVisitor.getQualifiedClassName("myPackage.Snowball"));
      assertEquals("Should append package to front of not fully packaged name", "myPackage.simpson.Snowball", 
                   testLLVisitor.getQualifiedClassName("simpson.Snowball"));
      assertEquals("Should append package to front of unqualified class name.", "myPackage.Grandpa", 
                   testLLVisitor.getQualifiedClassName("Grandpa"));
    }
    
    public void testAddSymbolData() {
      /**Put super class in symbol table.*/
      SymbolData obj = new SymbolData("java.lang.Object");
      obj.setIsContinuation(false);
      LanguageLevelConverter.symbolTable.put("java.lang.Object", obj);
      
      ClassDef cd = 
        new ClassDef(SourceInfo.NO_INFO, _publicMav, new Word(SourceInfo.NO_INFO, "Awesome"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(SourceInfo.NO_INFO, "java.lang.Object", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      
      SymbolData sd = new SymbolData("Awesome"); /**Create a continuation and store it in table.*/
      sd.setSuperClass(LanguageLevelConverter.symbolTable.get("java.lang.Object"));
      LanguageLevelConverter.symbolTable.put("Awesome", sd);
      SymbolData result = testLLVisitor.defineSymbolData(cd, "Awesome");
      assertFalse("result should not be a continuation.", result.isContinuation());
      assertFalse("sd should also no longer be a continuation.", sd.isContinuation());
      assertEquals("result and sd should be equal.", sd, result);
      
      /**Hierarchy should be empty at the end.*/
      assertEquals("hierarchy should be empty", 0, _hierarchy.size());
      
      /**Check that if the class is already defined, an appropriate error is thrown.*/
      assertEquals("Should return null, because it is already in the SymbolTable.", null, 
                   testLLVisitor.defineSymbolData(cd, "Awesome"));
      assertEquals("Length of errors should now be 1.", 1, errors.size());
      assertEquals("Error message should be correct.", "The class or interface Awesome has already been defined.", 
                   errors.get(0).getFirst());
      assertEquals("hierarchy should be empty.", 0, _hierarchy.size());
      
    }
    
    public void test_variableDeclaration2VariableData() {
      VariableDeclarator[] d1 = {
        new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                            new PrimitiveType(SourceInfo.NO_INFO, "int"), 
                                            new Word(SourceInfo.NO_INFO, "i")) };
      VariableDeclaration vd1 = new VariableDeclaration(SourceInfo.NO_INFO,_publicMav, d1); 
      VariableData[] vdata1 = { new VariableData("i", _publicMav, SymbolData.INT_TYPE, false, _sd1) };
      
      assertTrue("Should properly recognize a basic VariableDeclaration", 
                 arrayEquals(vdata1, testLLVisitor._variableDeclaration2VariableData(vd1, _sd1)));
      
      VariableDeclarator[] d2 = {
        new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                            new PrimitiveType(SourceInfo.NO_INFO, "int"), 
                                            new Word(SourceInfo.NO_INFO, "i")), 
        new InitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                          new PrimitiveType(SourceInfo.NO_INFO, "boolean"), 
                                          new Word(SourceInfo.NO_INFO, "b"), 
                                          new BooleanLiteral(SourceInfo.NO_INFO, true)) };
      VariableDeclaration vd2 = new VariableDeclaration(SourceInfo.NO_INFO,_privateMav, d2); 
      VariableData bData = new VariableData("b", _privateMav, SymbolData.BOOLEAN_TYPE, true, _sd1);
      bData.setHasInitializer(true);
      VariableData[] vdata2 = {new VariableData("i", _privateMav, SymbolData.INT_TYPE, false, _sd1),
        bData};
      
      assertTrue("Should properly recognize a more complicated VariableDeclaration", 
                 arrayEquals(vdata2, testLLVisitor._variableDeclaration2VariableData(vd2, _sd1)));
      
      //check that if the type cannot be found, no error is thrown.
      VariableDeclarator[] d3 = { 
        new UninitializedVariableDeclarator(SourceInfo.NO_INFO, 
                                            new ClassOrInterfaceType(SourceInfo.NO_INFO, "LinkedList", new Type[0]), 
                                            new Word(SourceInfo.NO_INFO, "myList"))};
      VariableDeclaration vd3 = new VariableDeclaration(SourceInfo.NO_INFO, _privateMav, d3);
      testLLVisitor._variableDeclaration2VariableData(vd3, _sd1);
      assertEquals("There should now be no errors", 0, errors.size());
//     assertEquals("The error message should be correct", "Class or Interface LinkedList not found", 
//                  errors.get(0).getFirst());
      
    }
    
    public void test_addError() {
      LinkedList<Pair<String, JExpressionIF>> e = new LinkedList<Pair<String, JExpressionIF>>();
      
      NullLiteral nl = new NullLiteral(SourceInfo.NO_INFO);
      NullLiteral nl2 = new NullLiteral(SourceInfo.NO_INFO);
      
      e.addLast(new Pair<String,JExpressionIF>("Boy, is this an error!", nl));
      _addError("Boy, is this an error!", nl);
      
      assertTrue("An error should have been added.", _errorAdded);
      assertEquals("The errors list should be correct.", e, errors);
      
      e.addLast(new Pair<String,JExpressionIF>("Error again!", nl2));
      _addError("Error again!", nl2);
      
      assertTrue("Another error should have been aded.", _errorAdded);
      assertEquals("The new errors list should be correct.", e, errors);
    }
    
    public void test_addAndIgnoreError() {
      LinkedList<Pair<String, JExpressionIF>> e = new LinkedList<Pair<String, JExpressionIF>>();
      
      NullLiteral nl = new NullLiteral(SourceInfo.NO_INFO);
      NullLiteral nl2 = new NullLiteral(SourceInfo.NO_INFO);
      
      _errorAdded = false;
      
      e.addLast(new Pair<String,JExpressionIF>("Nobody pays attention to me!", nl));
      _addAndIgnoreError("Nobody pays attention to me!", nl);
      
      assertFalse("_errorAdded should be false.", _errorAdded);
      assertEquals("The errors list should be correct.", e, errors);
      
      e.addLast(new Pair<String,JExpressionIF>("Cellophane, I'm Mr. Cellophane", nl2));
      _addAndIgnoreError("Cellophane, I'm Mr. Cellophane", nl2);
      
      assertFalse("errorAdded should still be false.", _errorAdded);
      assertEquals("The new errors list should be correct.", e, errors);
      
      _errorAdded = true;
      try {
        _addAndIgnoreError("This should throw an exception, because _errorAdded is true.", nl);
        assertTrue("An error should have been thrown!", false);
      }
      catch (RuntimeException exc) {
        assertEquals("Make sure runtime exception message is correct.", 
                     "Internal Program Error: _addAndIgnoreError called while _errorAdded was true." + 
                     "  Please report this bug.",
                     exc.getMessage());
      }
      _errorAdded = false;
    }
    
    public void test_checkError() {
      _errorAdded = false;
      assertFalse("_checkError should return false", _checkError());
      
      _errorAdded = true;
      assertTrue("_checkError should return true", _checkError());
      assertFalse("_checkError should have set _errorAdded to false.", _errorAdded);
    }    
    
    public void testForClassDefDoFirst() {      
      ClassDef cd = 
        new ClassDef(SourceInfo.NO_INFO, _publicMav, 
                     new Word(SourceInfo.NO_INFO, "Awesome"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(SourceInfo.NO_INFO, "java.lang.Object", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      testLLVisitor.forClassDefDoFirst(cd);
      assertEquals("There should be no errors.", 0, errors.size());
      testLLVisitor._importedFiles.addLast(new File("Awesome").getAbsolutePath());
      testLLVisitor.forClassDefDoFirst(cd);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "The class Awesome was already imported.", 
                   errors.get(0).getFirst());
      
      ClassDef cd2 = new ClassDef(SourceInfo.NO_INFO, _privateMav, 
                                  new Word(SourceInfo.NO_INFO, "privateClass"),
                                  new TypeParameter[0], 
                                  new ClassOrInterfaceType(SourceInfo.NO_INFO, "java.lang.Object", new Type[0]), 
                                  new ReferenceType[0], 
                                  new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      testLLVisitor.forClassDefDoFirst(cd2);
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("The 2nd error message should be correct", "Top level classes cannot be private", 
                   errors.get(1).getFirst());
      
    }
    
    public void testForInterfaceDefDoFirst() {
      InterfaceDef id = new InterfaceDef(SourceInfo.NO_INFO, _publicMav, 
                                         new Word(SourceInfo.NO_INFO, "Awesome"),
                                         new TypeParameter[0], new ReferenceType[0], 
                                         new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      testLLVisitor.forInterfaceDefDoFirst(id);
      assertEquals("There should be no errors.", 0, errors.size());
      
      InterfaceDef id2 = new InterfaceDef(SourceInfo.NO_INFO, _privateMav, 
                                          new Word(SourceInfo.NO_INFO, "privateinterface"),
                                          new TypeParameter[0], new ReferenceType[0], 
                                          new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      testLLVisitor.forInterfaceDefDoFirst(id2);
      assertEquals("There should be 1 errors", 1, errors.size());
      assertEquals("The error message should be correct", "Top level interfaces cannot be private", 
                   errors.get(0).getFirst());
      
      InterfaceDef id3 = new InterfaceDef(SourceInfo.NO_INFO, _finalMav, 
                                          new Word(SourceInfo.NO_INFO, "finalinterface"),
                                          new TypeParameter[0], new ReferenceType[0], 
                                          new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      testLLVisitor.forInterfaceDefDoFirst(id3);
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct", "Interfaces cannot be final", errors.get(1).getFirst());      
    }
    
    public void testForInnerInterfaceDefDoFirst() {
      InterfaceDef id = new InterfaceDef(SourceInfo.NO_INFO, _publicMav, 
                                         new Word(SourceInfo.NO_INFO, "Awesome"),
                                         new TypeParameter[0], new ReferenceType[0], 
                                         new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      id.visit(testLLVisitor);
      assertEquals("There should be no errors.", 0, errors.size());
      
      InnerInterfaceDef id2 = 
        new InnerInterfaceDef(SourceInfo.NO_INFO, _finalMav, new Word(SourceInfo.NO_INFO, "finalinterface"),
                              new TypeParameter[0], new ReferenceType[0], 
                              new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      id2.visit(testLLVisitor);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "Interfaces cannot be final", errors.get(0).getFirst());   
    }
    
    public void testForPackageStatementOnly() {
      Word[] words = new Word[] {new Word(SourceInfo.NO_INFO, "alpha"),
        new Word(SourceInfo.NO_INFO, "beta")};
      CompoundWord cw = new CompoundWord(SourceInfo.NO_INFO, words);
      PackageStatement ps = new PackageStatement(SourceInfo.NO_INFO, cw);
      testLLVisitor._file = new File("alpha/beta/delta");
      testLLVisitor.forPackageStatementOnly(ps);
      assertEquals("_package should be set correctly.", "alpha.beta", testLLVisitor._package);
      assertEquals("There should be no errors.", 0, errors.size());
      testLLVisitor._file = new File("alpha/beta/beta/delta");
      testLLVisitor.forPackageStatementOnly(ps);
      assertEquals("_package should be set correctly.", "alpha.beta", testLLVisitor._package);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "The package name must mirror your file's directory.", 
                   errors.get(0).getFirst());
    }
    
    public void testForClassImportStatementOnly() {
      
      //Test one that works
      Word[] words = new Word[] {new Word(SourceInfo.NO_INFO, "alpha"),
        new Word(SourceInfo.NO_INFO, "beta")};
      CompoundWord cw = new CompoundWord(SourceInfo.NO_INFO, words);
      ClassImportStatement cis = new ClassImportStatement(SourceInfo.NO_INFO, cw);
      SymbolData sd = new SymbolData("alpha.beta");
      testLLVisitor.forClassImportStatementOnly(cis);
      assertTrue("imported files should contain alpha.beta", testLLVisitor._importedFiles.contains("alpha.beta"));
      assertEquals("There should be a continuation.", sd, LanguageLevelConverter.symbolTable.get("alpha.beta"));
      assertTrue("It should be in continuations.", testLLVisitor.continuations.containsKey("alpha.beta"));
      
      // Test one that should throw an error: Class has already been imported. alpha.beta should now be in the 
      // symbolTable, and alpha should be in the list of packages, so this will throw an error
      Word[] words2 = new Word[] {new Word(SourceInfo.NO_INFO, "gamma"),
        new Word(SourceInfo.NO_INFO, "beta")};
      CompoundWord cw2 = new CompoundWord(SourceInfo.NO_INFO, words2);
      ClassImportStatement cis2 = new ClassImportStatement(SourceInfo.NO_INFO, cw2);
      cis2.visit(testLLVisitor);
      
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "The class beta has already been imported.", 
                   errors.get(0).getFirst());
      
      //Test one that should throw an error: Importing a class from the current package
      testLLVisitor._package = "myPackage";
      Word[] words3 = 
        new Word[] { new Word(SourceInfo.NO_INFO, "myPackage"), new Word(SourceInfo.NO_INFO, "cookie")};
      CompoundWord cw3 = new CompoundWord(SourceInfo.NO_INFO, words3);
      ClassImportStatement cis3 = new ClassImportStatement(SourceInfo.NO_INFO, cw3);
      cis3.visit(testLLVisitor);
      
      assertEquals("There should now be 2 errors", 2, errors.size());
      assertEquals("The second error message should be correct", "You do not need to import myPackage.cookie." + 
                   "  It is in your package so it is already visible", errors.get(1).getFirst());
      
      
    }
    
    public void testForPackageImportStatementOnly() {
      //Test one that works
      Word[] words = new Word[] {new Word(SourceInfo.NO_INFO, "alpha"),
        new Word(SourceInfo.NO_INFO, "beta")};
      CompoundWord cw = new CompoundWord(SourceInfo.NO_INFO, words);
      PackageImportStatement cis = new PackageImportStatement(SourceInfo.NO_INFO, cw);
      SymbolData sd = new SymbolData("alpha.beta");
      testLLVisitor.forPackageImportStatementOnly(cis);
      assertEquals("There should be no errorrs", 0, errors.size());
      assertTrue("Imported Packages should now contain alpha.beta", testLLVisitor._importedPackages.contains("alpha.beta"));
      
      //Test one that should not throw an error: Importing a subpackage of the current package
      testLLVisitor._package = "myPackage";
      Word[] words3 = new Word[] {new Word(SourceInfo.NO_INFO, "myPackage"), new Word(SourceInfo.NO_INFO, 
                                                                                              "cookie")};
      CompoundWord cw3 = new CompoundWord(SourceInfo.NO_INFO, words3);
      PackageImportStatement pis = new PackageImportStatement(SourceInfo.NO_INFO, cw3);
      pis.visit(testLLVisitor);
      
      assertEquals("There should be no errors", 0, errors.size());
      assertTrue("Imported Packages should now contain myPackage.cookie", 
                 testLLVisitor._importedPackages.contains("myPackage.cookie"));
      
      
      
      //Test one that should throw an error: Importing the current package
      Word[] words2 = new Word[] {new Word(SourceInfo.NO_INFO, "myPackage")};
      CompoundWord cw2 = new CompoundWord(SourceInfo.NO_INFO, words2);
      PackageImportStatement pis2 = new PackageImportStatement(SourceInfo.NO_INFO, cw2);
      pis2.visit(testLLVisitor);
      
      assertEquals("There should now be 1 errors", 1, errors.size());
      assertEquals("The error message should be correct", "You do not need to import package myPackage." + 
                   " It is your package so all public classes in it are already visible.", errors.get(0).getFirst());
      
    }
    
    public void testForSourceFile() {
      ClassDef cd = new ClassDef(SourceInfo.NO_INFO, _publicMav, new Word(SourceInfo.NO_INFO, "Awesome"),
                                 new TypeParameter[0], 
                                 new ClassOrInterfaceType(SourceInfo.NO_INFO, "java.lang.Object", new Type[0]), 
                                 new ReferenceType[0], 
                                 new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      ClassDef cd2 = new ClassDef(SourceInfo.NO_INFO, _publicMav, new Word(SourceInfo.NO_INFO, "Gnarly"),
                                  new TypeParameter[0], 
                                  new ClassOrInterfaceType(SourceInfo.NO_INFO, "Awesome", new Type[0]), 
                                  new ReferenceType[0], 
                                  new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      InterfaceDef id = new InterfaceDef(SourceInfo.NO_INFO, _publicMav, 
                                         new Word(SourceInfo.NO_INFO, "NiftyWords"),
                                         new TypeParameter[0], 
                                         new ReferenceType[0], 
                                         new BracedBody(SourceInfo.NO_INFO, new BodyItemI[0]));
      
      SourceFile sf = new SourceFile(SourceInfo.NO_INFO,
                                     new PackageStatement[0],
                                     new ImportStatement[0],
                                     new TypeDefBase[] {cd, cd2, id});
      testLLVisitor.forSourceFile(sf);
      
      assertTrue("_classNamesInThisFile should contain the two ClassDefs.", 
                 testLLVisitor._classNamesInThisFile.contains("Awesome"));
      assertTrue("_classNamesInThisFile should contain the two ClassDefs.", testLLVisitor._classNamesInThisFile.contains("Gnarly"));
      
      assertTrue("_classNamesInThisFile should contain the InterfaceDef", testLLVisitor._classNamesInThisFile.contains("NiftyWords"));
      assertTrue("_classesToBeParsed should contain the two ClassDefs.", testLLVisitor._classesToBeParsed.containsKey("Awesome"));
      assertTrue("_classesToBeParsed should contain the two ClassDefs.", testLLVisitor._classesToBeParsed.containsKey("Gnarly"));
      assertTrue("_classesToBeParsed should contain the InterfaceDef", testLLVisitor._classesToBeParsed.containsKey("NiftyWords"));
      
    }
    
    public void testReferenceType2String() {
      // Try a TypeVariable
      TypeVariable tv = new TypeVariable(SourceInfo.NO_INFO, "T");
      String[] result = testLLVisitor.referenceType2String(new ReferenceType[] { tv });
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("Results should have one String.", 1, result.length);
      assertEquals("The String should be \"T\".", "T", result[0]);
      
      // Try a ClassOrInterfaceType
      ClassOrInterfaceType coit = new ClassOrInterfaceType(SourceInfo.NO_INFO, 
                                                           "MyClass", 
                                                           new Type[] { new TypeVariable(SourceInfo.NO_INFO, "T"),
        new TypeVariable(SourceInfo.NO_INFO, "U")}
      );
      result = testLLVisitor.referenceType2String(new ReferenceType[] { tv, coit });
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("Results should have two Strings.", 2, result.length);
      assertEquals("The first String should be \"T\".", "T", result[0]);
      assertEquals("The second String should be \"MyClass\".", "MyClass", result[1]);
      
      // Try a MemberType
      MemberType mt = new MemberType(SourceInfo.NO_INFO,
                                     "MyClass.MyClass2",
                                     coit,
                                     new ClassOrInterfaceType(SourceInfo.NO_INFO, 
                                                              "MyClass2", 
                                                              new Type[0]));
      result = testLLVisitor.referenceType2String(new ReferenceType[] { mt });
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("Results should have one String.", 1, result.length);
      assertEquals("The first String should be \"MyClass.MyClass2\".", "MyClass.MyClass2", result[0]);
    }
    
    
    public void testExceptionsInSymbolTable() {
            
      // Make sure that exceptions are being added to symbol table 
      ClassOrInterfaceType exceptionType = new ClassOrInterfaceType(SourceInfo.NO_INFO, 
                                                                    "java.util.prefs.BackingStoreException", 
                                                                    new Type[0]);
      ParenthesizedExpressionList expList = new ParenthesizedExpressionList(SourceInfo.NO_INFO, new Expression[0]);
      
      BracedBody bb = 
        new BracedBody(SourceInfo.NO_INFO, 
                       new BodyItemI[] { new ThrowStatement(SourceInfo.NO_INFO, 
                                                            new SimpleNamedClassInstantiation(SourceInfo.NO_INFO, 
                                                                                              exceptionType, expList))});
      bb.visit(testLLVisitor);
      assertFalse("The SymbolTable should have java.util.prefs.BackingStoreException", 
                  LanguageLevelConverter.symbolTable.get("java.util.prefs.BackingStoreException") == null);
      
    }
    
    public void testShouldBreak() {
      //shift assignment expressions:
      LeftShiftAssignmentExpression shift1 = 
        new LeftShiftAssignmentExpression(SourceInfo.NO_INFO, new NullLiteral(SourceInfo.NO_INFO), 
                                          new NullLiteral(SourceInfo.NO_INFO));
      RightUnsignedShiftAssignmentExpression shift2 = 
        new RightUnsignedShiftAssignmentExpression(SourceInfo.NO_INFO, 
                                                   new NullLiteral(SourceInfo.NO_INFO), 
                                                   new NullLiteral(SourceInfo.NO_INFO));
      RightSignedShiftAssignmentExpression shift3 = 
        new RightSignedShiftAssignmentExpression(SourceInfo.NO_INFO, 
                                                 new NullLiteral(SourceInfo.NO_INFO), 
                                                 new NullLiteral(SourceInfo.NO_INFO));
      
      
      shift1.visit(testLLVisitor);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("error message should be correct", "Shift assignment operators cannot be used at any language level",
                   errors.getLast().getFirst());
      
      shift2.visit(testLLVisitor);
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("error message should be correct", "Shift assignment operators cannot be used at any language level",
                   errors.getLast().getFirst());
      
      shift3.visit(testLLVisitor);
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("error message should be correct", "Shift assignment operators cannot be used at any language level",
                   errors.getLast().getFirst());
      
      //BitwiseAssignmentExpressions
      BitwiseAndAssignmentExpression bit1 = 
        new BitwiseAndAssignmentExpression(SourceInfo.NO_INFO, new NullLiteral(SourceInfo.NO_INFO), 
                                           new NullLiteral(SourceInfo.NO_INFO));
      BitwiseOrAssignmentExpression bit2 = 
        new BitwiseOrAssignmentExpression(SourceInfo.NO_INFO, new NullLiteral(SourceInfo.NO_INFO), 
                                          new NullLiteral(SourceInfo.NO_INFO));
      BitwiseXorAssignmentExpression bit3 = 
        new BitwiseXorAssignmentExpression(SourceInfo.NO_INFO, new NullLiteral(SourceInfo.NO_INFO), 
                                           new NullLiteral(SourceInfo.NO_INFO));
      
      bit1.visit(testLLVisitor);
      assertEquals("Should be 4 errors", 4, errors.size());
      assertEquals("error message should be correct", "Bitwise operators cannot be used at any language level", 
                   errors.getLast().getFirst());
      
      bit2.visit(testLLVisitor);
      assertEquals("Should be 5 errors", 5, errors.size());
      assertEquals("error message should be correct", "Bitwise operators cannot be used at any language level", 
                   errors.getLast().getFirst());
      
      bit3.visit(testLLVisitor);
      assertEquals("Should be 6 errors", 6, errors.size());
      assertEquals("error message should be correct", "Bitwise operators cannot be used at any language level", 
                   errors.getLast().getFirst());
      
      //BitwiseExpressions
      BitwiseAndExpression bit4 = 
        new BitwiseAndExpression(SourceInfo.NO_INFO, new NullLiteral(SourceInfo.NO_INFO), 
                                 new NullLiteral(SourceInfo.NO_INFO));
      BitwiseOrExpression bit5 = 
        new BitwiseOrExpression(SourceInfo.NO_INFO, new NullLiteral(SourceInfo.NO_INFO), 
                                new NullLiteral(SourceInfo.NO_INFO));
      BitwiseXorExpression bit6 = 
        new BitwiseXorExpression(SourceInfo.NO_INFO, new NullLiteral(SourceInfo.NO_INFO), 
                                 new NullLiteral(SourceInfo.NO_INFO));
      BitwiseNotExpression bit7 = 
        new BitwiseNotExpression(SourceInfo.NO_INFO, new NullLiteral(SourceInfo.NO_INFO));
      
      
      bit4.visit(testLLVisitor);
      assertEquals("Should be 7 errors", 7, errors.size());
      assertEquals("error message should be correct", "Bitwise and expressions cannot be used at any language level." + 
                   "  Perhaps you meant to compare two values using regular and (&&)", errors.getLast().getFirst());
      
      bit5.visit(testLLVisitor);
      assertEquals("Should be 8 errors", 8, errors.size());
      assertEquals("error message should be correct", "Bitwise or expressions cannot be used at any language level." + 
                   "  Perhaps you meant to compare two values using regular or (||)", errors.getLast().getFirst());
      
      bit6.visit(testLLVisitor);
      assertEquals("Should be 9 errors", 9, errors.size());
      assertEquals("error message should be correct", "Bitwise xor expressions cannot be used at any language level", 
                   errors.getLast().getFirst());
      
      bit7.visit(testLLVisitor);
      assertEquals("Should be 10 errors", 10, errors.size());
      assertEquals("error message should be correct", "Bitwise not expressions cannot be used at any language level." +
                   "  Perhaps you meant to negate this value using regular not (!)", errors.getLast().getFirst());
      
      //shift binary expressions
      LeftShiftExpression shift4 = 
        new LeftShiftExpression(SourceInfo.NO_INFO, new NullLiteral(SourceInfo.NO_INFO), 
                                new NullLiteral(SourceInfo.NO_INFO));
      RightUnsignedShiftExpression shift5 = 
        new RightUnsignedShiftExpression(SourceInfo.NO_INFO, new NullLiteral(SourceInfo.NO_INFO), 
                                         new NullLiteral(SourceInfo.NO_INFO));
      RightSignedShiftExpression shift6 = 
        new RightSignedShiftExpression(SourceInfo.NO_INFO, new NullLiteral(SourceInfo.NO_INFO), 
                                       new NullLiteral(SourceInfo.NO_INFO));
      
      
      shift4.visit(testLLVisitor);
      assertEquals("Should be 11 error", 11, errors.size());
      assertEquals("error message should be correct", "Bit shifting operators cannot be used at any language level", 
                   errors.getLast().getFirst());
      
      shift5.visit(testLLVisitor);
      assertEquals("Should be 12 errors", 12, errors.size());
      assertEquals("error message should be correct", "Bit shifting operators cannot be used at any language level", 
                   errors.getLast().getFirst());
      
      shift6.visit(testLLVisitor);
      assertEquals("Should be 13 errors", 13, errors.size());
      assertEquals("error message should be correct", "Bit shifting operators cannot be used at any language level", 
                   errors.getLast().getFirst());
      
      //empty expression
      EmptyExpression e = new EmptyExpression(SourceInfo.NO_INFO);
      e.visit(testLLVisitor);
      assertEquals("Should be 14 errors", 14, errors.size());
      assertEquals("Error message should be correct", "You appear to be missing an expression here", 
                   errors.getLast().getFirst());
      
      //noop expression
      NoOpExpression noop = 
        new NoOpExpression(SourceInfo.NO_INFO, new NullLiteral(SourceInfo.NO_INFO), 
                           new NullLiteral(SourceInfo.NO_INFO));
      noop.visit(testLLVisitor);
      assertEquals("Should be 15 errors", 15, errors.size());
      assertEquals("Error message should be correct", "You are missing a binary operator here", 
                   errors.getLast().getFirst());
    }
    
    public void testIsConstructor() {
      MethodData constr = 
        new MethodData("monkey", _publicMav, new TypeParameter[0], _sd1, new VariableData[0], new String[0], _sd1, 
                       new NullLiteral(SourceInfo.NO_INFO));
      MethodData notRightOuter = 
        new MethodData("monkey", _publicMav, new TypeParameter[0], _sd1, new VariableData[0], new String[0], _sd2, 
                       new NullLiteral(SourceInfo.NO_INFO));
      _sd2.setOuterData(_sd1);
      _sd1.addInnerClass(_sd2);
      MethodData notRightName = 
        new MethodData("chimp", _publicMav, new TypeParameter[0], _sd1, new VariableData[0], new String[0], _sd1, 
                       new NullLiteral(SourceInfo.NO_INFO));
      MethodData notRightReturnType = 
        new MethodData("monkey", _publicMav, new TypeParameter[0], _sd2, new VariableData[0], new String[0], _sd1, 
                       new NullLiteral(SourceInfo.NO_INFO));
      
      //try one that works
      assertTrue(testLLVisitor.isConstructor(constr));
      
      //wrong outer
      assertFalse(testLLVisitor.isConstructor(notRightOuter));
      
      //wrong name
      assertFalse(testLLVisitor.isConstructor(notRightName));
      
      //wrong return type
      assertFalse(testLLVisitor.isConstructor(notRightReturnType));
      
      //not a method data
      assertFalse(testLLVisitor.isConstructor(_sd1));
    } 
  }
}
