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

import org.objectweb.asm.*;
import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.javalanglevels.tree.Type; // resove ambiguity
import edu.rice.cs.javalanglevels.parser.JExprParser;
import edu.rice.cs.javalanglevels.parser.ParseException;
//import edu.rice.cs.javalanglevels.util.Log;
import java.util.*;
import java.io.*;
import java.lang.reflect.Modifier;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.reflect.PathClassLoader;
import edu.rice.cs.plt.reflect.EmptyClassLoader;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.io.IOUtil;

import junit.framework.TestCase;

/**
 * Top-level Language Level Visitor that represents what is common between all Language Levels.  
 * Enforces constraints during the first walk of the AST (checking for general errors and building 
 * the symbol table).
 * This class enforces things that are common to all contexts reachable at any Language Level, as well as
 * top level constraints.
 */
public class LanguageLevelVisitor extends JExpressionIFPrunableDepthFirstVisitor_void {
  
  /**Errors we have encountered during this pass: string is the text of the error, JExpressionIF is the part of
    * the AST where the error occurs*/
  protected static LinkedList<Pair<String, JExpressionIF>> errors;
  
  /**Stores the classes we have referenced, and all their information, once they are resolved.*/
  static Symboltable symbolTable;
  
  /*Stores the information on the refernce as well as the visitor we were using when we encountered the
   * reference, for a type that has not been resolved yet*/
  static Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations;
  
  /**Stores all the SymbolDatas we have created as well as the visitor they were created from.*/
  static Hashtable<SymbolData, LanguageLevelVisitor> _newSDs;
  
  /*
   * A list of other files that are visited.  If the SourceFile is not null, then the source file was
   * visited as opposed to the class file.  This info is used by LanguageLevelConverter in DrJava.
   * We keep the LLV rather than the file, because the LLV has a file, and we need some other information
   * stored in the LLV to properly look up the file.
   */
  static LinkedList<Pair<LanguageLevelVisitor, SourceFile>> visitedFiles;
  
  /**True once we have encountered an error we cannot recover from.*/
  static boolean _errorAdded;
  
  /**The source file that is being compiled */
  File _file;
  
  /** The package of the current file */
  String _package;
  
  /** A list of file names (classes) imported by the current file. */
  LinkedList<String> _importedFiles;
  
  /** A list of package names imported by the current file. */
  LinkedList<String> _importedPackages;
  
  /** A list of ClassDefs and InterfaceDefs in the current file. */
  LinkedList<String> _classNamesInThisFile;
  
  /**
   * A list of qualified class names of the subclasses of this class that have been traversed and 
   * continuations pending the resolution of their superclasses.
   */
  static Hashtable<String, TypeDefBase> _hierarchy;
  
  /**
   * A Hashtable containing a list of qualified class names of classes waiting to be parsed 
   * within SourceFiles we're in the process of compiling mapped to a pair containing their
   * ClassDefs and LanguageLevelVisitors.
   */
  static Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>> _classesToBeParsed;
//  protected static final Log _log = new Log("/Users/cork/drjava/javalanglevels/LLVisitor.txt", true);
  
  /** This constructor is called from the subclasses of LanguageLevelVisitor.
    * @param file  The File corresponding to the source file we are visiting
    * @param packageName  The name of the package corresponding to the file
    * @param importedFiles  The list of files (classes) imported by this source file
    * @param importedPackages  The list of packages imported by this source file
    * @param classNamesInThisFile  The list of names of classes defined in this file
    * @param continuations  The table of classes we have encountered but still need to resolve
    */
  public LanguageLevelVisitor(File file, String packageName, LinkedList<String> importedFiles, 
                              LinkedList<String> importedPackages, LinkedList<String> classNamesInThisFile, 
                              Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations) {
    _file = file;
    _package = packageName;
    _importedFiles = importedFiles;
    _importedPackages = importedPackages;
    _classNamesInThisFile = classNamesInThisFile;
    this.continuations = continuations;
  }
  
  
  /**Create a new LanguageLevelVisitor corresponding to the new file*/
  public LanguageLevelVisitor createANewInstanceOfMe(File file) {
    return new LanguageLevelVisitor(file, "", new LinkedList<String>(), new LinkedList<String>(), 
                                    new LinkedList<String>(), continuations);
  }
  
  /**
   * This is a special constructor called from the TypeChecker that sets classesToBeParsed.
   * Normally, classesToBeParsed is set by the concrete instantiation of the LangaugeLevelVisitor,
   * but in the case of the TypeChecker, we just create a LangaugeLevelVisitor so we can use its
   * getSymbolData code.  Thus, we must give it a classesToBeParsed to avoid a null pointer exception.
   */
  public LanguageLevelVisitor(File file, String packageName, LinkedList<String> importedFiles, 
                              LinkedList<String> importedPackages, LinkedList<String> classNamesInThisFile, 
                              Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>> classesToBeParsed,
                              Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations) {
    this(file, packageName, importedFiles, importedPackages, classNamesInThisFile, continuations);
    _classesToBeParsed = classesToBeParsed;
  }
  
  
  /*Reset the nonStatic fields of this visitor.  Used during testing. */
  protected void _resetNonStaticFields() {
    _file = new File("");
    _package = "";
    _importedFiles = new LinkedList<String>();
    _importedPackages = new LinkedList<String>();
    _classNamesInThisFile = new LinkedList<String>();
  }
  
  /** 
   * Originally wanted to take parameter name and return getName, but this faces
   * problems if there are two fields with the same name but one is uppercase and
   * one is lower or if one starts with an underscore and the other doesn't.
   * So we just return name for now.
   */
  public static String getFieldAccessorName(String name) {
    return name;
  }
  
  
  /**@return the source file*/
  public File getFile() {
    return _file;
  }
  
  /**
   * Return true if this data is a constructor.
   * It is considered to be a constructor if it is a method data,
   * its name and return type are the same, and its return type matches
   * its enclosing sd.
   */
  protected boolean isConstructor(Data d) {
    if (!(d instanceof MethodData)) return false;
    MethodData md = (MethodData) d;
    
    return (md.getReturnType() != null) && (md.getSymbolData() != null) &&
      (md.getReturnType().getName().indexOf(md.getName()) != -1) && 
      (md.getReturnType() == md.getSymbolData());
  }
  
  /**
   * Takes a classname and returns only the final segment of it.  This removes all the 
   * dots and dollar signs.
   */
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
  
  /**
   * Convert the ReferenceType[] to a String[] with the names of the ReferenceTypes.
   */
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
        Iterable<File> searchPath = IterUtil.compose(LanguageLevelConverter.OPT.bootClassPath(),
                                                     LanguageLevelConverter.OPT.classPath());
        _cachedResult = new PathClassLoader(EmptyClassLoader.INSTANCE, searchPath);
      }
      return _cachedResult;
    }
  };
  
  /**
   * Use the ASM class reader to read the class file corresponding to the class in
   * the specified directory, and use the information from ASM to build a SymbolData corresponding
   * to the class.
   * @param qualifiedClassName  The fully qualified class name of the class we are looking up
   * @param directoryName  The directory where the class is located.
   */
  private SymbolData _classFile2SymbolData(String qualifiedClassName, String directoryName) {
    ClassReader reader = null;
    try {
      String fileName = qualifiedClassName.replace('.', '/') + ".class";
      InputStream stream = RESOURCES.value().getResourceAsStream(fileName);
      if (stream == null && directoryName != null) {
        stream = PathClassLoader.getResourceInPathAsStream(fileName, new File(directoryName));
      }
      if (stream == null) { return null; }
      // Let IOUtil handle the stream here, because it closes it when it's done, unlike ASM.
      reader = new ClassReader(IOUtil.toByteArray(stream));
    }
    catch (IOException e) { return null; }
    
    // This is done so that the SymbolData in the Symboltable is updated and returned.
    final SymbolData sd;
    SymbolData sdLookup = symbolTable.get(qualifiedClassName); 
    if (sdLookup == null) {
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
        String typeString = org.objectweb.asm.Type.getType(desc).getClassName();
        SymbolData type = getSymbolDataForClassFile(typeString, lookupInfo);
        if (type != null) { sd.addVar(new VariableData(name, _createMav(access), type, true, sd)); }
        return null;
      }
      
      public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exceptions) {
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
        for (int i = 0; i < exceptions.length; i++) {
          exceptions[i] = exceptions[i].replace('/', '.');
        }
        
        if (valid) {
          MethodData m = new MethodData(methodName, _createMav(access), new TypeParameter[0], returnType,
                                        args, exceptions, sd, null);
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
  
  /**
   * Build a SourceInfo corresponding to the specified class name, with -1 as the
   * value for row and column of the start and finish.
   */
  protected SourceInfo _makeSourceInfo(String qualifiedClassName) {
    return new SourceInfo(new File(qualifiedClassName), -1, -1, -1, -1);
  }
  
  /**
   * Looks up the SymbolData for a qualified name that is in the list of classes to be parsed.
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
      continuations.put(qualifiedClassName, new Pair<SourceInfo, LanguageLevelVisitor>(si, this));
      SymbolData sd = new SymbolData(qualifiedClassName);
      symbolTable.put(qualifiedClassName, sd);
      return sd;
    }
  }
  
  /**
   * Check to see if the specified classname is the name of a fully qualified java library class.
   */
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
  
  /**
   * Return true if the specified VariableData overwrites one of the members of the list of VariableDatas
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
  
  /**
   * Set resolve to true since we're in a superclass and should be
   * able to find the class.  If the result is null, give an error.
   * Remove the symbol data from the continuations list, and return it.
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
  
  /**
   * Checks to see if the provided class name is the name of a primative type, and if so,
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
  
  /**
   * Assume the class name is qualified already, and try to look it up.
   * If it's a continuation and resolve is true, this resolves the class 
   * by reading its class file if it's a java library file or looking it
   * up in the file system otherwise.
   * If it's not qualified with either the package name or java.lang, throw an
   * error somehow.
   * @param className  The name to look up.  Presumed to be qualified.
   * @param si  The source info of where this was called from.
   * @param resolve  true if we want to resolve the SymbolData.
   * @param addError  Whether we want to throw an error or not if we can't find the class 
   */ 
  private SymbolData _getSymbolData_IsQualified(String className, SourceInfo si, boolean resolve, boolean fromClassFile, 
                                                boolean addError) {
    // We assume a period in a class name means it is qualified, make sure it's either in
    // this package, is imported specifically, is in an imported package, or is in java.lang.
    // We can't directly check it, because parsing class files adds every class that is recursively
    // referenced by that class file, and we shouldn't be allowed to see some of them.
    SymbolData sd = symbolTable.get(className);
    /* If sd is not null then if it's not a continuation, we're done, return it.
     * If we're from a class file, then a continuation is ok because we assume
     * that we'll find it later.  If you don't return here, you can get into
     * an infinite loop if there's a self-referencing class.
     */
    if (sd != null && (!sd.isContinuation() || fromClassFile)) { return sd; }
    
    // Look it up in the symbol table, see if it's a Java library class, look it up from the filesystem.
    if (isJavaLibraryClass(className)) {
      return _classFile2SymbolData(className, null);
    }
    else {
      sd = _getSymbolData_FromFileSystem(className, si, resolve, addError);
      if (sd != SymbolData.KEEP_GOING) {
        return sd;
      }
      if (addError) {
        _addAndIgnoreError("The class " + className + " was not found.", new NullLiteral(si));
      }
      return null;
    }
  }
  
  /**
   * Get the inner SymbolData for this array type. If it's not null, then try to lookup the ArrayData
   * corresponding to that type plus '[]'.  If it's already there, return it.  If not, create a new ArrayData
   * and put it in the Symbol Table.
   * @param className  The String name of the array type we're trying to resolve.
   * @param si  The SourceInfo corresponding to the class.  Used if an error is encountered.
   * @param resolve  true if this SymbolData needs to be completely resolved.
   * @param fromClassFile  true if this type is from a class file.
   */
  private SymbolData _getSymbolData_ArrayType(String className, SourceInfo si, boolean resolve, boolean fromClassFile, 
                                              boolean addError, boolean checkImportedPackages) {
    // shouldn't be resolving an array type since you can't extend one, so resolve should be false
    SymbolData innerSd = getSymbolDataHelper(className.substring(0, className.length() - 2), si, resolve, fromClassFile, 
                                             addError, checkImportedPackages);
    if (innerSd != null) {
      SymbolData sd = symbolTable.get(innerSd.getName() + "[]");
      if (sd != null) { return sd; }
      
      else {
        sd = new ArrayData(innerSd, this, si);
        symbolTable.put(sd.getName(), sd);
        return sd;
      }
    }
    else { return null; }
  }
  
  /**
   * The SymbolData we want is defined later in the current file.  If it needs to be looked up or
   * resolved, do so.
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
    else {
      return sd;
    }
  }
  
  /** Check the file system for the class name, returning the corresponding SymbolData if there is a match
    * @param qualifiedClassName  The name of the class we're looking up.
    * @param si  Information about where the class was called from.
    * @param resolve  true if we want to fully resolve the SymbolData.
    * @param addError  true if we want to throw errors.
    */
  private SymbolData _getSymbolData_FromFileSystem(final String qualifiedClassName, SourceInfo si, boolean resolve,
                                                   boolean addError) {
    // Check is the qualified class name is in _classesToBeParsed to save the time it would take to parse
    // all the java files in this package.
    Pair<TypeDefBase, LanguageLevelVisitor> pair = _classesToBeParsed.get(qualifiedClassName);
    
    if (pair != null) {
      return _lookupFromClassesToBeParsed(qualifiedClassName, si, resolve);
    }
    
    // If it's not in the symbol table or list of classes to be parsed, 
    // check if the class is defined in one of the files in this package.
    // We have to begin looking for files at the source root of the current _file.
    File parent = _file.getParentFile();
    String directoryName = "";
    if (parent != null) {
      directoryName = parent.getAbsolutePath();
    }
    
    final String path;
    String qualifiedClassNameWithSlashes = qualifiedClassName.replace('.', System.getProperty("file.separator").charAt(0));
    if (directoryName != null) {      
      String newPackage = _package.replace('.', System.getProperty("file.separator").charAt(0));
      int indexOfPackage = directoryName.length();
      if (newPackage.length() > 0) {
        // Removes the slash after the package
        indexOfPackage = directoryName.indexOf(newPackage) - 1;
      }
      
      //this should only be necessary when testing. 
      if (indexOfPackage < 0) {
        indexOfPackage = directoryName.length();
      }
      
      directoryName = directoryName.substring(0, indexOfPackage);
      
      path = directoryName + System.getProperty("file.separator") + qualifiedClassNameWithSlashes;
    }
    else {
      path = qualifiedClassName;
    }
    
    
    /* newPath is the directory of the file we're trying to resolve */
    String newPath;
    /* newPackage is the package of the file we're trying to resolve */
    String newPackage = "";
    int lastSlash = qualifiedClassNameWithSlashes.lastIndexOf(System.getProperty("file.separator"));
    if (lastSlash != -1) {
      newPackage = qualifiedClassNameWithSlashes.substring(0, lastSlash);
      newPath = directoryName + System.getProperty("file.separator") + newPackage;
      newPackage = newPackage.replace(System.getProperty("file.separator").charAt(0), '.');
    }
    else {
      int lastSlashInPath = path.lastIndexOf(System.getProperty("file.separator"));
      if (lastSlashInPath != -1) {
        newPath = path.substring(0, lastSlashInPath);
      }
      else {
        newPath = "";
      }
    }
    
    
    
    // First look for the .class file
    File classFile = new File(path + ".class");
    // Then look for the most recently modified .java, .djx file.    
    File[] sourceFiles = new File(newPath).listFiles(new FileFilter() {
      public boolean accept(File f) {
        try {
          f = f.getCanonicalFile();
          return new File(path + ".dj0").getCanonicalFile().equals(f) ||
            new File(path + ".dj1").getCanonicalFile().equals(f) ||
            new File(path + ".dj2").getCanonicalFile().equals(f);
        }
        catch (IOException e) { return false; }
        // TODO: Do something with Java files.
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
    
    // Check if sourceFile is the current file.  If so, there's an error because if the class to look for
    // is in the current file, it would've been in _classNamesInThisFile.
    //TODO: it is possible this should be an error.  But I am no longer positive.
//    if (sourceFile.equals(_file)) {
//      if (addError) {
//        _addAndIgnoreError("The class " + qualifiedClassName + " was not found in the file " + sourceFile, 
//                           new NullLiteral(si));
//      }
//      System.out.println("Just added an error. returning null.  class should have been in current file.");
//      return null;
//    }
//    if (qualifiedClassName.contains("List")) {System.out.println("Line 777: There are " + continuations.size() + 
//      " continuations " + continuations);}
    
    // Then check the corresponding class file to see if it's up to date.
    SymbolData sd = symbolTable.get(qualifiedClassName);
    if (sd != null && ! sd.isContinuation()) { return sd; }
    if (sourceFile != null) {
      // First see if we even need to resolve this class. If not, create a continuation and return it.
      if (!resolve) {
        if (sd != null) { return sd; }
        else {
          sd = new SymbolData(qualifiedClassName);
          continuations.put(qualifiedClassName, 
                            new Pair<SourceInfo, LanguageLevelVisitor>(si, createANewInstanceOfMe(sourceFile)));//this));
          
          symbolTable.put(qualifiedClassName, sd);
          return sd;
        }
      }
      
      // Get the last modified times of the java file and class file.
      // if the classfile doesn't exist, classFileLastModified will get 0, and we will parse it.
      if (sourceFile.lastModified() > classFile.lastModified()) {
        // the class file is out of date, parse the java file
        //First, make sure the java file is readable:
        if (!sourceFile.canRead()) {
          if (addError) {
            _addAndIgnoreError("The file " + sourceFile.getAbsolutePath() + 
                               " is present, but does not have proper read permissions", new NullLiteral(si));
          }
          return null;
        }
        
        //Also make sure the directory where the new class file will eventually be written is writable.
        if (!new File(newPath).canWrite()) {
          if (addError) {
            _addAndIgnoreError("The file " + sourceFile.getAbsolutePath() + 
                               " needs to be recompiled, but its directory does not have proper write permissions", 
                               new NullLiteral(si));
          }
          return null;
        }
        
        /** Insure that this file hasn't already been processed */
        try { 
          File canonicalSource = sourceFile.getCanonicalFile();
          boolean alreadyVisited = false;
          try { alreadyVisited |= _file.getCanonicalFile().equals(canonicalSource); }
          catch (IOException e) { /* ignore */ }
          if (!alreadyVisited) {
            for (Pair<LanguageLevelVisitor, SourceFile> p : visitedFiles) {
              try { 
                alreadyVisited |= p.getFirst()._file.getCanonicalFile().equals(canonicalSource);
                if (alreadyVisited) { break; }
              }
              catch (IOException e) { /* ignore */ }
            }
          }
          
          if (alreadyVisited) { return SymbolData.KEEP_GOING; }
        }
        catch (IOException e) {
          if (addError) {
            _addAndIgnoreError("The file " + sourceFile.getAbsolutePath() + 
                               " is present, but its full path cannot be resolved " + 
                               "(symbolic links may not have proper permissions)", 
                               new NullLiteral(si));
          }
          return null;
        }
        
        LanguageLevelVisitor lv;
        
        if (LanguageLevelConverter.isElementaryFile(sourceFile)) {
          lv = new ElementaryVisitor(sourceFile, errors, symbolTable, continuations, visitedFiles, _newSDs);
        }
        else if (LanguageLevelConverter.isIntermediateFile(sourceFile)) {
          lv = new IntermediateVisitor(sourceFile, errors, symbolTable, continuations, visitedFiles, _newSDs);
        }
        else if (LanguageLevelConverter.isAdvancedFile(sourceFile)) {
          lv = new AdvancedVisitor(sourceFile, errors, symbolTable, continuations, visitedFiles, _newSDs);
        }
        else {
          throw new RuntimeException("Internal Program Error: Invalid file format not caught initially" + 
                                     sourceFile.getName() + ". Please report this bug");
        }
        
        SourceFile sf;
        try {
          JExprParser jep = new JExprParser(sourceFile);
          sf = jep.SourceFile();
        }
        catch(ParseException pe) {
          if (addError) {
            // The NullLiteral is a hack to get a JExpression with the correct SourceInfo inside.
            _addAndIgnoreError(pe.getMessage(), 
                               new NullLiteral(new SourceInfo(sourceFile,
                                                              pe.currentToken.beginLine,
                                                              pe.currentToken.beginColumn,
                                                              pe.currentToken.endLine,
                                                              pe.currentToken.endColumn)));
          }
          return null;
        }
        catch (FileNotFoundException fnfe) {
          // This should never happen.
          if (addError) {
            _addAndIgnoreError("File " + sourceFile + " could not be found.", new NullLiteral(si));
          }
          return null;
        }
        // See if there were any errors caused by the first pass on the java file.
        int numErrors = errors.size();
        sf.visit(lv);
        if (numErrors != errors.size()) {
          return null;
        }
        else {
          //Resolve any continuations.
          while (!lv.continuations.isEmpty()) {
            Enumeration<String> en = lv.continuations.keys();
            
            while (en.hasMoreElements()) {
              String className = en.nextElement();
              Pair<SourceInfo, LanguageLevelVisitor> p = lv.continuations.remove(className);
              SymbolData returnedSd = p.getSecond().getSymbolData(className, p.getFirst(), true);
              //if (returnedSd == null) {
              //  errors.add(new Pair<String, JExpressionIF>("Could not resolve " + className, 
              //                                             new NullLiteral(p.getFirst())));
              //}
            }
          }
          
          // Create any constructors.
          Hashtable<SymbolData, LanguageLevelVisitor> newSDs = lv._newSDs;
          
          Enumeration<SymbolData> keys = newSDs.keys();
          while (keys.hasMoreElements()) {
            SymbolData key = keys.nextElement();
            LanguageLevelVisitor sdlv = newSDs.get(key);    // Can return null because of silly side effects!
            if (sdlv != null) sdlv.createConstructor(key);  // Null test is a kludge! Deletes (key,sdlv) from _newSDs!
          }
          assert LanguageLevelVisitor._newSDs.isEmpty();
          
          sf.visit(new TypeChecker(sourceFile, newPackage, errors, symbolTable, lv._importedFiles, lv._importedPackages));
        }
        
        // will this put entries into the symbol table that this class shouldn't be able to see?
        
        // The symbol table should now contain the SymbolData of the class we're looking for.
        sd = symbolTable.get(qualifiedClassName);
        if (sd == null || sd.isContinuation()) {
          if (addError) {
            _addAndIgnoreError("File " + sourceFile + " does not contain class " + qualifiedClassName, sf);
          }
          return null;
        }
        else {
          visitedFiles.add(new Pair<LanguageLevelVisitor, SourceFile>(lv, sf));
          return sd;
        }
      }
    }
    if (classFile.exists()) {
      // read this classfile, create the SymbolData and return it
      sd = _classFile2SymbolData(qualifiedClassName, directoryName);
      if (sd == null) {
        if (addError) {
          _addAndIgnoreError("File " + classFile + " is not a valid class file.", null);
        }
        return null;
      }
      else {
        if (sourceFile != null) {
          // Visit the sourceFile anyway even though the classFile is up to date so we
          // can pass the SourceFile to the LanguageLevelConverter.
          SourceFile sf;
          try {
            sf = new JExprParser(sourceFile).SourceFile();
          }
          catch(ParseException pe) {
            if (addError) {
              // The NullLiteral is a hack to get a JExpression with the correct SourceInfo inside.
              _addAndIgnoreError(pe.getMessage(), 
                                 new NullLiteral(new SourceInfo(sourceFile,
                                                                pe.currentToken.beginLine,
                                                                pe.currentToken.beginColumn,
                                                                pe.currentToken.endLine,
                                                                pe.currentToken.endColumn)));
            }
            return null;
          }
          catch (FileNotFoundException fnfe) {
            // This should never happen.
            if (addError) {
              _addAndIgnoreError("File " + sourceFile + " could not be found.", new NullLiteral(si));
            }
            return null;
          }
          
          LanguageLevelVisitor lv = createANewInstanceOfMe(sourceFile);
          lv._package = newPackage;
          visitedFiles.add(new Pair<LanguageLevelVisitor, SourceFile>(lv, null));//lv, sf));
          
          
        }
        
        return sd;
      }
    }
    return SymbolData.KEEP_GOING;
  }
  
  /**
   * Call getSymbolData with default values
   * By default, resolve will be false.  It's only true when looking up a superclass.
   * By default, fromClassFile will be false, since this is only true when we are trying to resolve types from the 
   * context of a class file.
   * By default addError will be true, since we want to display errors.
   * By default checkImportedStuff will be true, since we want to consider imported packages and classes initially.
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
    if (resolve && sd != null) {
      if (sd.isContinuation()) { throw new RuntimeException("Internal Program Error: " + sd.getName() + 
                                                            " should not be a continuation.  Please report this bug.");}
      continuations.remove(sd.getName());
    }
    return sd;
  }
  
  /**
   * Call getSymbolData with some default values
   * By default addError will be true, since we want to display errors.
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
  
  /** This wrapper method processes qualified class names by looking up each piece sequentially.  Once 
    * a SymbolData is found corresponding to the class name processed thus far, we look up each piece of the 
    * rest of the class name as inner classes.  This method calls getSymbolDataHelper to look up classes.
    * @param className the name of the class to lookup.
    * @param si  The SourceInfo of the reference to className used in case of an error.
    * @param resolve  Whether to return a continuation or fully parse the class.
    * @param fromClassFile  Whether this was called from the class file reader.
    * @param addError  Whether to add errors or not
    * @param checkImportedStuff  Whether to try prepending the imported package names
    */
  protected SymbolData getSymbolData(String className, SourceInfo si, boolean resolve, boolean fromClassFile, 
                                     boolean addError, boolean checkImportedStuff) {
    
    //_log.log("getSymbolData(" + className + ", " + si + ", " + resolve + ", " + fromClassFile + ", " + addError +
    //         ", " + checkImportedStuff);
    
    int indexOfNextDot = className.indexOf(".");
    /* we don't think this is necessay, but as a safety percausion, check the $ that denotes anonymous inner classes 
     * and inner classes. */
    int indexOfNextDollar = className.indexOf("$");  
    if (indexOfNextDot == -1 && indexOfNextDollar == -1) {
      return getSymbolDataHelper(className, si, resolve, fromClassFile, addError, checkImportedStuff);
    }
    else { indexOfNextDot = 0; }
    SymbolData whatever;
    int length = className.length();
    while (indexOfNextDot != length) {
      indexOfNextDot = className.indexOf(".", indexOfNextDot + 1);
      if (indexOfNextDot == -1) { indexOfNextDot = length; }
      String s = className.substring(0, indexOfNextDot);
      /* We want to resolve after every piece until the last one because we need to know
       * when we actually have a class so that we can tell that the rest of the pieces
       * are inner classes.  We use the resolve parameter's value for the last piece
       * since that means there are no inner classes
       */
      boolean newResolve = resolve || (indexOfNextDot != length);
      whatever = getSymbolDataHelper(s, si, newResolve, fromClassFile, false, checkImportedStuff);
      if (whatever != null) {
        String innerClassName = "";
        SymbolData outerClass = whatever;
        if (whatever != null && indexOfNextDot != length) {
          outerClass = whatever;
          innerClassName = className.substring(indexOfNextDot + 1);
          whatever = outerClass.getInnerClassOrInterface(innerClassName);
        }
        if (whatever == SymbolData.AMBIGUOUS_REFERENCE) {
          _addAndIgnoreError("Ambiguous reference to class or interface " + className, new NullLiteral(si));
          return null;
        }
        if (whatever != null) { return whatever;}
        else { 
          //perhaps this was an array type--try to resolve it without the [], and then put it in the symbol table
          if (className.endsWith("[]")) { 
            SymbolData sd = 
              getSymbolData(className.substring(0, className.length() - 2), si, resolve, fromClassFile, addError, 
                            checkImportedStuff);
            if (sd != null) {
              ArrayData ad = new ArrayData(sd, this, si);
              symbolTable.put(ad.getName(), ad);
              return ad;
            }
            return sd;
          }
          
          if (addError) {
            _addAndIgnoreError("Class " + innerClassName + " is not an inner class of the class " + 
                               outerClass.getName(), 
                               new NullLiteral(si));
          }
          return null;
        }
      }
    }
    
    if (! fromClassFile && addError) {
      // _log.log("Returning an Invalid class name for " + className);
      String newName = className;
      int lastDollar = newName.lastIndexOf("$");
      newName = newName.substring(lastDollar + 1, newName.length());
      _addAndIgnoreError("Invalid class name " + newName, new NullLiteral(si));
    }
    return null;
  }
  
  
  
  /**
   * Try to look up name from the context of the lhs.
   * @param lhs  The TypeData correpsonding to the enclosing of this name reference
   * @param name  The name piece to look up from the context of lhs
   * @param si  The SourceInfo corresponding to this reference
   * @param addError  true if an error should be added
   * @return  The SymbolData corresponding to this lookup, or KEEP_GOING or null if it could not be found
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
  
  
  
  /**
   * This method takes in a class name (it may or may not be qualified) and tries to get it from the symbol table
   * as is.  If this fails, it will see if the class name corresponds to one of the imported files.
   * Then it will prepend the current package and try again.  It will then try to prepend the imported
   * packages to the class name.  Finally, it will try prepending java.lang and see if it is in the symbol table.
   * After each check, this method also checks if such a class file (.class or .java) exists and if so, will
   * build up a SymbolData for the class by compiling the class if the class is out of date or does not exist,
   * and then reading the class file for the needed information.  The newly created SymbolData will be put 
   * into the symbol table.  Returns null if the className is not found.
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
      return _getSymbolData_ArrayType(className, si, resolve, fromClassFile, addError, checkImportedStuff);
    }
    
    // Check for qualified types.
    if (className.indexOf(".") != -1) {
      return _getSymbolData_IsQualified(className, si, resolve, fromClassFile, addError);
    }
    
    String name = null; // name of the SymbolData to be returned
    String qualifiedClassName = getQualifiedClassName(className);
    // Check if className is defined in this file.
    if (_classNamesInThisFile.contains(qualifiedClassName)) {
      return _getSymbolData_FromCurrFile(qualifiedClassName, si, resolve);
    }
    
    // Check if className was specifically imported. --Not done at elementary level.
    // At this point, we know that class name is not qualified.
    //We will not check that the package is correct here, because it is caught in the type checker.
    Iterator<String> iter = _importedFiles.iterator();
    if (checkImportedStuff) {
      while (iter.hasNext()) {
        String s = iter.next();
        if (s.endsWith(className)) {
          // All imported files should be in the symbol table.
          SymbolData tempSd = symbolTable.get(s);
          // Only need to fully resolve if resolve is on and the imported file is a continuation.
          if (resolve && tempSd.isContinuation()) {
            return getSymbolData(s, si, resolve, fromClassFile, addError, false); // addError??
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
      if (sd == null || (sd.isContinuation() && resolve)) {
        sd = _getSymbolData_FromFileSystem(qualifiedClassName, si, resolve, addError);
        if (sd != SymbolData.KEEP_GOING) {
          return sd;
        }
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
      iter = _importedPackages.iterator();
      while (iter.hasNext()) {
        String s = iter.next() + "." + className;
        SymbolData tempSd;
        if (s.indexOf("$") != -1) {
          tempSd = getSymbolData(s, si, resolve, fromClassFile, false, false);
        }
        else {
          tempSd = getSymbolDataHelper(s, si, resolve, fromClassFile, false, false);
        }
        if (tempSd != null) {
          if (resultSd == null) { resultSd = tempSd; }
          else {
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
  
  /**
   * Creates a ModifiersAndVisibility from the provided modifier flags.
   */
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
    return new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, strings.toArray(new String[strings.size()]));
  }
  
  
  /**
   * The Qualified Class Name is the package, followed by a dot, followed by the rest of the class name.
   * If the provided className is already qualified, just return it.  If the package is not empty,
   * and the className does not start with the package, append the package name onto the className, and return it.
   * @param className  The className to qualify.
   */
  protected String getQualifiedClassName(String className) {
    if (!_package.equals("") && !className.startsWith(_package)) {return _package + "." + className;}
    else { return className;}
  }
  
  
  
  /**
   * Do what is necessary to process this TypeDefBase from the context of enclosing.
   * This method is very similar to addSymbolData, except that it uses an enclosing data for reference.
   */
  protected SymbolData addInnerSymbolData(TypeDefBase typeDefBase, String qualifiedClassName, String partialName, 
                                          Data enclosing, boolean isClass) {
    //try to look up in symbol table, in case it has already been defined
    SymbolData sd = symbolTable.get(qualifiedClassName);
    
    //try to look up in enclosing's list of inner classes
    if (sd == null) {sd = enclosing.getInnerClassOrInterface(partialName);}
    
    if (sd != null && !sd.isContinuation()) {
      _addAndIgnoreError("This class has already been defined.", typeDefBase);
      return null;
    }
    
    if (sd != null) {
      //make sure it is a direct inner class or interface of this data.
      if (sd.getOuterData() != enclosing) {sd = null;}
    }
    
    
    //create a new symbolData for it--this is the first time we've seen it
    if (sd == null) { 
      sd = new SymbolData(qualifiedClassName);
      sd.setOuterData(enclosing);
      if (isClass) {enclosing.getSymbolData().addInnerClass(sd);}
      else {(enclosing.getSymbolData()).addInnerInterface(sd);}
    }
    
    // create the LinkedList for the SymbolDatas of the interfaces
    LinkedList<SymbolData> interfaces = new LinkedList<SymbolData>();
    SymbolData tempSd;
    ReferenceType[] rts = typeDefBase.getInterfaces();
    for (int i = 0; i < rts.length; i++) {
      tempSd = getSymbolData(rts[i].getName(), rts[i].getSourceInfo(), false, false, false);
      
      if (tempSd != null) {
        interfaces.addLast(tempSd);  
      }
      
      else if (enclosing instanceof SymbolData) {
        //check to see if this is an inner class referencing an inner interface
        tempSd = enclosing.getInnerClassOrInterface(rts[i].getName());
        if (tempSd == null) {
          String qualifyingPart = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf("$"));
          tempSd = new SymbolData(qualifyingPart + "$" + rts[i].getName());
          tempSd.setInterface(true);
          enclosing.getSymbolData().addInnerInterface(tempSd); //interfaces can only be defined in symbol datas
          tempSd.setOuterData(enclosing);
          continuations.put(rts[i].getName(), new Pair<SourceInfo, LanguageLevelVisitor>(rts[i].getSourceInfo(), this));          
        }
        interfaces.addLast(tempSd);
      }
      
      else {
        _addAndIgnoreError("Cannot resolve interface " + rts[i].getName(), rts[i]);
        return null;
      }
    }
    
    //Set the package to be the current package
    sd.setPackage(_package);
    
    SymbolData superClass = null;
    
    if (typeDefBase instanceof InterfaceDef) {
      //add Object as the super class of this, so that it will know it implements Object's methods.
      superClass = getSymbolData("Object", typeDefBase.getSourceInfo(), false);
      sd.setInterface(true);
    }
    
    else if (typeDefBase instanceof ClassDef) {
      ClassDef cd = (ClassDef) typeDefBase;
      ReferenceType rt = cd.getSuperclass();
      String superClassName = rt.getName();
      superClass = getSymbolData(superClassName, rt.getSourceInfo(), false, false, false);
      
      if (superClass == null) {
        superClass = enclosing.getInnerClassOrInterface(superClassName);
        if (superClass == null) {
          String qualifyingPart = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf("$"));
          superClass = new SymbolData(qualifyingPart + "$" + superClassName);
          enclosing.addInnerClass(superClass);
          superClass.setOuterData(enclosing);
          continuations.put(superClassName, new Pair<SourceInfo, LanguageLevelVisitor>(rt.getSourceInfo(), this)); 
        }
      }
      sd.setInterface(false);
    }
    
    else {throw new RuntimeException("Internal Program Error: typeDefBase was not a ClassDef or InterfaceDef." + 
                                     "  Please report this bug.");}
    
    // get the SymbolData of the superclass which must be in the symbol table
    // since we visited the type in forClassDef() although it may be a continuation. 
    
    
    // there is a continuation in the symbol table, update the fields
    sd.setMav(typeDefBase.getMav());
    sd.setTypeParameters(typeDefBase.getTypeParameters());
    sd.setSuperClass(superClass);
    sd.setInterfaces(interfaces);
    sd.setIsContinuation(false);
    continuations.remove(sd.getName());
    if (sd != null && !sd.isInterface()) {_newSDs.put(sd, this); }
    return sd;
  }
  
  
  /**
   * This method takes in a TypeDefBase (which is either a ClassDef or an InterfaceDef), generates a SymbolData, and 
   * adds the name and SymbolData pair to the symbol table.  It checks that this class is not already in the symbol 
   * table.
   * @param typeDefBase  The AST node for the class def, interface def, inner class def, or inner interface def.
   * @param qualifiedClassName  The name for the class.
   */
  protected SymbolData addSymbolData(TypeDefBase typeDefBase, String qualifiedClassName) {
    String name = qualifiedClassName;
    SymbolData sd = symbolTable.get(name);
    if (sd != null && !sd.isContinuation()) {
      _addAndIgnoreError("This class has already been defined.", typeDefBase);
      return null;
    }
    
    // create the LinkedList for the SymbolDatas of the interfaces
    LinkedList<SymbolData> interfaces = new LinkedList<SymbolData>();
    SymbolData tempSd;
    ReferenceType[] rts = typeDefBase.getInterfaces();
    for (int i = 0; i < rts.length; i++) {
      tempSd = getSymbolData(rts[i].getName(), rts[i].getSourceInfo(), false, false, false);
      
      if (tempSd != null) {
        interfaces.addLast(tempSd);  
      }
      
      else if (qualifiedClassName.indexOf("$") != -1) {
        //check to see if this is an inner class referencing an inner interface
        String qualifyingPart = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf("$"));
        tempSd = getSymbolData(qualifyingPart + "$" + rts[i].getName(), rts[i].getSourceInfo(), false, false, false);
        if (tempSd == null) {
          tempSd = new SymbolData(qualifyingPart + "$" + rts[i].getName());
          tempSd.setInterface(true);
          continuations.put(rts[i].getName(), new Pair<SourceInfo, LanguageLevelVisitor>(rts[i].getSourceInfo(), this));          
        }
        
        interfaces.addLast(tempSd);
      }
      
      else if (tempSd == null) {
        _addAndIgnoreError("Could not resolve " + rts[i].getName(), rts[i]);
        // Couldn't resolve the interface.
        return null;
      }
      
    }
    
    if (sd == null) { // create a new SymbolData.
      sd = new SymbolData(name);
      symbolTable.put(name, sd);
    }
    
    
    //Set the package to be the current package
    sd.setPackage(_package);
    
    
    SymbolData superClass = null;
    
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
      superClass = getSymbolData(superClassName, rt.getSourceInfo(), false); //TODO: change this back to true?
      
      if (superClass == null) {
        // Couldn't resolve the super class: make it Object by default
        superClass = getSymbolData("java.lang.Object", typeDefBase.getSourceInfo(), false);
      }
      sd.setInterface(false);
    }
    
    
    
    else {throw new RuntimeException("Internal Program Error: typeDefBase was not a ClassDef or InterfaceDef." + 
                                     "  Please report this bug.");}
    
    // get the SymbolData of the superclass which must be in the symbol table
    // since we visited the type in forClassDef() although it may be a continuation. 
    
    
    // there is a continuation in the symbol table, update the fields
    sd.setMav(typeDefBase.getMav());
    sd.setTypeParameters(typeDefBase.getTypeParameters());
    sd.setSuperClass(superClass);
    sd.setInterfaces(interfaces);
    sd.setIsContinuation(false);
    continuations.remove(sd.getName());
    
    if (!sd.isInterface()) {_newSDs.put(sd, this); }
    return sd;
  }
  
  /**
   * Convert the specified array of FormalParameters into an array of VariableDatas which is then returned.
   * All formal parameters are automatically made final.
   * TODO: At the advanced level, this may need to be overwritten?
   */
  protected VariableData[] formalParameters2VariableData(FormalParameter[] fps, Data enclosing) {
    VariableData[] varData = new VariableData[fps.length];
    VariableDeclarator vd;
    String[] mav;
    for (int i = 0; i < varData.length; i++) {
      vd = fps[i].getDeclarator();
      mav = new String[] {"final"};
      String name = vd.getName().getText();
      SymbolData type = getSymbolData(vd.getType().getName(), vd.getType().getSourceInfo());
      if (type != null) {
        varData[i] = 
          new VariableData(name, new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, mav), type, true, enclosing);
        varData[i].gotValue();
      }
      else { 
        /* if type is null, then there was an error, trying to resolve it. 
         * Just put 'null' in the array, because we will never try to access it. */
        _addError("Class or Interface " + vd.getType().getName() + " not found", vd);
        varData[i]=null;
      }
    }
    return varData;
  }
  
  
  
  /* Create a MethodData corresponding to the MethodDef within the context of the SymbolData sd. */
  protected MethodData createMethodData(MethodDef that, SymbolData sd) {
    that.getMav().visit(this);
    that.getName().visit(this);
    
    // Turn the thrown exceptions from a ReferenceType[] to a String[]
    String[] throwStrings = referenceType2String(that.getThrows());
    
    // Turn the ReturnTypeI into a SymbolData    
    String rtString = that.getResult().getName();
    SymbolData returnType;
    //TODO: Overwrite this at the Advanced level (or maybe not)
    if (rtString.equals("void")) {// && level.equals("Elementary")) {  
      returnType = SymbolData.VOID_TYPE;
    }
    else {
      returnType = getSymbolData(rtString, that.getResult().getSourceInfo());
    }
    MethodData md = new MethodData(that.getName().getText(), that.getMav(), that.getTypeParams(), returnType, 
                                   new VariableData[0], throwStrings, sd, that);
    // Turn the parameters from a FormalParameterList to a VariableData[]
    VariableData[] vds = formalParameters2VariableData(that.getParams(), md);
    
    if (_checkError()) {  //if there was an error converting the formalParameters, don't use them.
      return md;
    }
    
    md.setParams(vds);
    
    // Adds the formal parameters to the list of vars defined in this method.
    if (!md.addVars(vds)) { //TODO: should this not have been changed from addFinalVars?
      _addAndIgnoreError("You cannot have two method parameters with the same name", that);      
    }
    return md;
  }
  
  
  
  /** This method assumes that the modifiers for this particular VariableDeclaration
    * have already been checked.  It does no semantics checking.  It simiply converts
    * the declarators to variable datas, by trying to resolve the types of each declarator.
    */
  protected VariableData[] _variableDeclaration2VariableData(VariableDeclaration vd, Data enclosing) {
    LinkedList<VariableData> vds = new LinkedList<VariableData>();
    ModifiersAndVisibility mav = vd.getMav();
    VariableDeclarator[] declarators = vd.getDeclarators();
    Type type;
    String name;
    for (int i = 0; i < declarators.length; i++) {
      declarators[i].visit(this);
      type = declarators[i].getType();
      name = declarators[i].getName().getText();
      SymbolData sd = getSymbolData(type.getName(), type.getSourceInfo());
      
      if (sd == null) {
        //see if this is a partially qualified field reference
        sd = enclosing.getInnerClassOrInterface(type.getName());
      }
      
      if (sd == null) {
        //if we still couldn't resolve sd, create a continuation for it.
        sd = new SymbolData(enclosing.getSymbolData().getName() + "$" + type.getName());
        enclosing.getSymbolData().addInnerClass(sd);
        sd.setOuterData(enclosing.getSymbolData());
        continuations.put(sd.getName(), new Pair<SourceInfo, LanguageLevelVisitor>(type.getSourceInfo(), this));
      }
      
      if (sd != null) {
        boolean initialized = declarators[i] instanceof InitializedVariableDeclarator;
        // want hasBeenAssigned to be true if this variable declaration is initialized, and false otherwise.
        VariableData vdata = new VariableData(name, mav, sd, initialized, enclosing); 
        vdata.setHasInitializer(initialized);
        vds.addLast(vdata); 
      }
      
      else _addAndIgnoreError("Class or Interface " + type.getName() + " not found", declarators[i].getType());
    }
    return vds.toArray(new VariableData[vds.size()]);
  }
  
  /**
   * This method is called when an error should be added to the static LinkedList of errors.
   * This version is called from the DoFirst methods in the LanguageLevelVisitors to halt
   * parsing of the construct.
   */
  protected static void _addError(String message, JExpressionIF that) {
    _errorAdded = true;
    errors.addLast(new Pair<String, JExpressionIF>(message, that));
  }
  
  /**
   * This method is called when an error should be added, but tree-walking should continue
   * on this construct.  Generally, if the error is not added in the DoFirst, the _errorAdded
   * flag is not checked anyway, so this version should be called.
   */
  protected static void _addAndIgnoreError(String message, JExpressionIF that) {
    if (_errorAdded) {
      throw new RuntimeException("Internal Program Error: _addAndIgnoreError called while _errorAdded was true." + 
                                 "  Please report this bug.");
    }
    _errorAdded = false;
    errors.addLast(new Pair<String, JExpressionIF>(message, that));
  }
  
  protected boolean prune(JExpressionIF node) {
    return _checkError();
  }
  
  /**If _errorAdded is true, set it back to false and return true.
    * This will cause the current construct to be skipped, but will allow this first pass
    * to otherwise continue unimpeeded.
    * Otherwise, return false, which will allow this first pass to continue normally.
    */
  protected static boolean _checkError() {
    if (_errorAdded) {
      _errorAdded = false;
      return true;
    }
    else {
      return false;
    }
  }
  
  /**Add an error explaining the modifiers' conflict.*/
  private void _badModifiers(String first, String second, ModifiersAndVisibility that) {
    _addError("Illegal combination of modifiers. Can't use " + first + " and " + second + " together.", that);
  }
  
  /**
   * Check for problems with modifiers that are common to all language levels:
   * duplicate modifiers and illegal combinations of modifiers.
   */
  public void forModifiersAndVisibilityDoFirst(ModifiersAndVisibility that) {
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
          if (!visibility.equals("package")) {
            _badModifiers(visibility, s, that);
          }
          else if (s.equals("private") && isAbstract) {
            _badModifiers("private", "abstract", that);
          }
          else {
            visibility = s;
          }
        }
        else if (s.equals("abstract")) {
          isAbstract = true;
        }
        else if (s.equals("final")) { 
          isFinal = true;
          if (isAbstract) {
            _badModifiers("final", "abstract", that);
          }
        }
        else if (s.equals("static")) { 
          isStatic = true;
          if (isAbstract) {
            _badModifiers("static", "abstract", that);
          }
        }
        else if (s.equals("native")) { 
          isNative = true;
          if (isAbstract) {
            _badModifiers("native", "abstract", that);
          }
        }
        else if (s.equals("synchronized")) { 
          isSynchronized = true;
          if (isAbstract) {
            _badModifiers("synchronized", "abstract", that);
          }
        }
        else if (s.equals("volatile")) { 
          isVolatile = true;
          if (isFinal) {
            _badModifiers("final", "volatile", that);
          }
        }
      }
      forJExpressionDoFirst(that);
    }
  }
  
  /**
   * Check for problems with ClassDefs that are common to all Language Levels:
   * Make sure that the top level class is not private, and that the class name has
   * not already been imported.s
   */
  public void forClassDefDoFirst(ClassDef that) {
    String name = that.getName().getText();
    Iterator<String> iter = _importedFiles.iterator();
    while (iter.hasNext()) {
      String s = iter.next();
      if (s.endsWith(name) && !s.equals(getQualifiedClassName(name))) {
        _addAndIgnoreError("The class " + name + " was already imported.", that);
      }
    }
    
    //top level classes cannot be private.
    String[] mavStrings = that.getMav().getModifiers();
    if (!(that instanceof InnerClassDef)) {
      for (int i = 0; i < mavStrings.length; i++) {
        if (mavStrings[i].equals("private")) {
          _addAndIgnoreError("Top level classes cannot be private", that);
        }
      }
    }
    
    forTypeDefBaseDoFirst(that);
  }
  
  /**
   * Check for problems with InterfaceDefs that are common to all language levels:
   * specifically, top level interfaces cannot be private or final.
   */
  public void forInterfaceDefDoFirst(InterfaceDef that) {
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
    
    forTypeDefBaseDoFirst(that);
  }
  
  /**
   * Check for problems with InnerInterfaceDefs that are common to all language levels:
   * specifically, they cannot be final.
   */
  public void forInnerInterfaceDefDoFirst(InnerInterfaceDef that) {
    String[] mavStrings = that.getMav().getModifiers();
    for (int i = 0; i < mavStrings.length; i++) {
      if (mavStrings[i].equals("final")) {
        _addAndIgnoreError("Interfaces cannot be final", that);
      }
    }
    forTypeDefBaseDoFirst(that);  
  }
  
  /**
   * This sets the package name field in order to find other classes
   * in the same package.
   */
  public void forPackageStatementOnly(PackageStatement that) {
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
    forJExpressionOnly(that);
  }
  
  
  /**Make sure the class being imported has not already been imported and that it doesn
    * not duplicate the packagie--i.e. import something that is already in the package.
    * If there are no errors, add it to the list of imported files, and create a continuation for it.
    * The class will be resolved later.
    */
  public void forClassImportStatementOnly(ClassImportStatement that) {
    CompoundWord cWord = that.getCWord();
    Word[] words = cWord.getWords();
    
    //Make sure that this specific imported class has not already been specifically imported
    for (int i = 0; i<_importedFiles.size(); i++) {
      String name = _importedFiles.get(i);
      int indexOfLastDot = name.lastIndexOf(".");
      if (indexOfLastDot != -1 && 
          (words[words.length-1].getText()).equals(name.substring(indexOfLastDot + 1, name.length()))) {
        _addAndIgnoreError("The class " + words[words.length-1].getText() + " has already been imported.", that);
        return;
      }
    }
    
    StringBuffer tempBuff = new StringBuffer(words[0].getText());
    for (int i = 1; i < words.length; i++) {tempBuff.append("." + words[i].getText());}
    
    String temp = tempBuff.toString();
    
    //Make sure that this imported class does not dupliate the package.
    //Although this is allowed in full java, we decided to not allow it at any LanguageLevel.
    int indexOfLastDot = temp.lastIndexOf(".");
    if (indexOfLastDot != -1) {
      if (_package.equals(temp.substring(0, indexOfLastDot))) {
        _addAndIgnoreError("You do not need to import " + temp + ".  It is in your package so it is already visible", that);
        return;
      }
    }
    
    //Now add the class to the list of imported files
    _importedFiles.addLast(temp);  
    
    SymbolData sd = symbolTable.get(temp);
    if (sd == null) {
      // Create a continuation for the imported class and put it into the symbol table so
      // that on lookup, we can check imported classes before classes in the same package.
      sd = new SymbolData(temp);
      continuations.put(temp, new Pair<SourceInfo, LanguageLevelVisitor>(that.getSourceInfo(), this));
      symbolTable.put(temp, sd);
    }
    forImportStatementOnly(that);
  }
  
  /**Check to make sure that this package import statement is not trying to import the current pacakge. */
  public void forPackageImportStatementOnly(PackageImportStatement that) { 
    CompoundWord cWord = that.getCWord();
    Word[] words = cWord.getWords();
    StringBuffer tempBuff = new StringBuffer(words[0].getText());
    for (int i = 1; i < words.length; i++) {tempBuff.append("." + words[i].getText());}
    String temp = tempBuff.toString();
    
    
    //make sure this imported package does not match the current package
    if (_package.equals(temp)) {
      _addAndIgnoreError("You do not need to import package " + temp + 
                         ". It is your package so all public classes in it are already visible.", that);
      return;
    }
    
    _importedPackages.addLast(temp);
    
    forImportStatementOnly(that);
  }
  
  /**Bitwise operators are not allowed at any language level...*/
  public void forShiftAssignmentExpressionDoFirst(ShiftAssignmentExpression that) {
    _addAndIgnoreError("Shift assignment operators cannot be used at any language level", that);
  }
  public void forBitwiseAssignmentExpressionDoFirst(BitwiseAssignmentExpression that) {
    _addAndIgnoreError("Bitwise operators cannot be used at any language level", that);
  }
  public void forBitwiseBinaryExpressionDoFirst(BitwiseBinaryExpression that) {
    _addAndIgnoreError("Bitwise binary expressions cannot be used at any language level", that);
  }
  public void forBitwiseOrExpressionDoFirst(BitwiseOrExpression that) {
    _addAndIgnoreError("Bitwise or expressions cannot be used at any language level." + 
                       "  Perhaps you meant to compare two values using regular or (||)", that);
  }
  public void forBitwiseXorExpressionDoFirst(BitwiseXorExpression that) {
    _addAndIgnoreError("Bitwise xor expressions cannot be used at any language level", that);
  }
  public void forBitwiseAndExpressionDoFirst(BitwiseAndExpression that) {
    _addAndIgnoreError("Bitwise and expressions cannot be used at any language level." + 
                       "  Perhaps you meant to compare two values using regular and (&&)", that);
  }
  public void forBitwiseNotExpressionDoFirst(BitwiseNotExpression that) {
    _addAndIgnoreError("Bitwise not expressions cannot be used at any language level." + 
                       "  Perhaps you meant to negate this value using regular not (!)", that);
  }
  public void forShiftBinaryExpressionDoFirst(ShiftBinaryExpression that) {
    _addAndIgnoreError("Bit shifting operators cannot be used at any language level", that);
  }
  public void forBitwiseNotExpressionDoFirst(ShiftBinaryExpression that) {
    _addAndIgnoreError("Bitwise operators cannot be used at any language level", that);
  }
  
  
  /** The EmptyExpression is a sign of an error. It means that we were missing something
    * we needed when the parser built the AST*/
  public void forEmptyExpressionDoFirst(EmptyExpression that) {
    _addAndIgnoreError("You appear to be missing an expression here", that);
  }
  
  /** The NoOp expression signifies a missing binary operator that was encountered when the
    * parser built the AST. */
  public void forNoOpExpressionDoFirst(NoOpExpression that) {
    _addAndIgnoreError("You are missing a binary operator here", that);
  }
  
  /**
   * If one of the ClassDefs defined in this source file is a 
   * TestCase class, make sure it is the only thing in the file.
   */
  public void forSourceFileDoFirst(SourceFile that) {
    
    for (int i = 0; i< that.getTypes().length; i++) {
      if (that.getTypes()[i] instanceof ClassDef) {
        ClassDef c = (ClassDef) that.getTypes()[i];
        String superName = c.getSuperclass().getName();
        if (superName.equals("TestCase") || superName.equals("junit.framework.TestCase")) {
          if (that.getTypes().length > 1) {
            _addAndIgnoreError("TestCases must appear in files by themselves at all language levels", c);
          }
        }
      }
    }
    
  }
  
  /**
   * Check to make sure there aren't any immediate errors in this SourceFile by calling the
   * doFirst method.  Then, check to make sure that java.lang is imported, and if it is not, add
   * it to the list of importedpackages, since it is imported by default.  Make a list of all classes
   * defined in this file.
   * Then, visit them one by one.
   */
  public void forSourceFile(SourceFile that) {
    forSourceFileDoFirst(that);
    if (prune(that)) { return; }
    
    // The parser enforces that there is either zero or one PackageStatement.
    for (int i = 0; i < that.getPackageStatements().length; i++) that.getPackageStatements()[i].visit(this);
    for (int i = 0; i < that.getImportStatements().length; i++) that.getImportStatements()[i].visit(this);
    if (! _importedPackages.contains("java.lang"))
      _importedPackages.addFirst("java.lang");
    
    TypeDefBase[] types = that.getTypes();
    // store the qualified names of all classes defined in this file
    _classNamesInThisFile = new LinkedList<String>();
    for (int i = 0; i < types.length; i++) {
      // TODO: Add static inner classes in the file to classes (for advanced level)
      
      String qualifiedClassName = getQualifiedClassName(types[i].getName().getText());
      _classNamesInThisFile.addFirst(qualifiedClassName);
      _classesToBeParsed.put(qualifiedClassName, 
                             new Pair<TypeDefBase, LanguageLevelVisitor>(types[i], this));
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
    
    forSourceFileOnly(that);
  }
  
  /** Call the ResolveNameVisitor to see if this is a reference to a Type name. */
  public void forSimpleNameReference(SimpleNameReference that) {
    that.visit(new ResolveNameVisitor());
  }
  
  /** Call the ResolveNameVisitor to see if this is a reference to a Type name. */
  public void forComplexNameReference(ComplexNameReference that) {
    that.visit(new ResolveNameVisitor());
  }
  
  /**Do nothing.  This is handled in the forVariableDeclarationOnly case.*/
  public void forVariableDeclaration(VariableDeclaration that) {
    forVariableDeclarationDoFirst(that);
    if (prune(that)) { return; }
    that.getMav().visit(this);
    forVariableDeclarationOnly(that);
  }
  
  /**
   * If the method being generated already exists in the SymbolData,
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
  
  /**
   * Creates the automatically generated constructor for this class.  It needs
   * to take in the same arguments as its super class' constructor as well as
   * its fields.  If there are multiple constructors in the super class, pick the
   * one with the least number of parameters.
   * No constructor is created if this is an advanced level file (overridden at advanced level), because
   * no code augmentation is done.
   */
  public void createConstructor(SymbolData sd) {
    if (LanguageLevelConverter.isAdvancedFile(_file)) {return;}
    
    SymbolData superSd = sd.getSuperClass();
    
    //there was an error somewhere else.  just return.
    if (sd.isContinuation()) {return;}
    
    LinkedList<MethodData> superMethods = superSd.getMethods();
    String superUnqualifiedName = getUnqualifiedClassName(superSd.getName());
    
    LanguageLevelVisitor sslv = _newSDs.remove(superSd);
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
                                   new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"}), 
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
          new VariableData(paramName, new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[0]), 
                           superParam.getType().getSymbolData(), true, md);
        newParam.setGenerated(true);
        params.add(newParam);
        // Next line done on each iteration so that createUniqueName handles nameless super parameters (in class files)
        md.addVar(newParam); 
      }
    }
    
    //only add in those fields that do not have a value and are not static.
    boolean hasOtherConstructor = sd.hasMethod(name);
    
    for (VariableData field : sd.getVars()) {
      
      if (!field.hasInitializer() && !field.hasModifier("static")) {
        if (!hasOtherConstructor) { field.gotValue(); } // Set hasValue if no other constructors need to be visited
        // Rather than creating a new parameter, we use the field, since all the important data is the same in both of them.
        params.add(field);
      }
    }
    md.setParams(params.toArray(new VariableData[params.size()]));
    md.setVars(params);
    
    addGeneratedMethod(sd, md);
    _newSDs.remove(sd); //this won't do anything if sd is not in _newSDs.
  }
  
  /**
   * Overridden at the Advanced Level, because no code augmentation is done there.
   * Create a method that is an accessor for each field in the class.
   * File file is passed in so this can remain a static method
   */
  protected static void createAccessors(SymbolData sd, File file) {
    if (LanguageLevelConverter.isAdvancedFile(file)) {return;}
    LinkedList<VariableData> fields = sd.getVars();
    Iterator<VariableData> iter = fields.iterator();
    while (iter.hasNext()) {
      VariableData vd = iter.next();      
      if (!vd.hasModifier("static")) { 
        String name = getFieldAccessorName(vd.getName());
        String[] mavStrings;
        mavStrings = new String[] {"public"};
        MethodData md = new MethodData(name,
                                       new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, mavStrings), 
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
  
  /**
   * Create a method called toString that returns type String.
   * Overridden at the Advanced Level files, because no code augmentation is done for
   * them so you don't want to create this method.
   */ 
  protected void createToString(SymbolData sd) {
    String name = "toString";
    String[] mavStrings;
    mavStrings = new String[] {"public"};
    //    }
    MethodData md = new MethodData(name,
                                   new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, mavStrings), 
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
                                   new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, mavStrings), 
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
                                   new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, mavStrings), 
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
  public void forMemberType(MemberType that) {
    forMemberTypeDoFirst(that);
    if (prune(that)) { return; }
    forMemberTypeOnly(that);
  }
  
  /**Return the SymbolData for java.lang.String by default*/
  public void forStringLiteralOnly(StringLiteral that) {
    getSymbolData("String", that.getSourceInfo(), true);
  }
  
  /** Try to resolve the type of the instantiation, and make sure there are no errors*/
  public void forSimpleNamedClassInstantiation(SimpleNamedClassInstantiation that) {
    forSimpleNamedClassInstantiationDoFirst(that);
    if (prune(that)) { return; }
    that.getType().visit(this);
    that.getArguments().visit(this);
    
    // Put the allocated type into the symbol table
    /* TODO!: Shouldn't this happen for all Instantiations?
     * Even for all Types, regardless of where they show up?
     */
    getSymbolData(that.getType().getName(), that.getSourceInfo());
    
    forSimpleNamedClassInstantiationOnly(that);
  }
  
  
  /**
   * Checks that the 2 provided arrays are equal.  If they are both null,
   * they are equal.  If one is null and one is not, they are not equal.  If
   * they do not have the same length, they are not equal.  Otherwise, iterate
   * through the arrays comparing corresponding elements.  The two arrays are
   * equal if all of their corresponding elements are equal.
   * @param array1  The first array to check.
   * @param array2  The array to compare array1 to.
   * @return  true if the 2 arrays are equal.
   */ 
  public static boolean arrayEquals(Object[] array1, Object[] array2) {
    if (array1 == null && array2 == null) { return true; }
    if (array1 == null || array2 == null) { return false; }
    if (array1.length != array2.length) { return false; }
    for (int i = 0; i < array1.length; i++) {
      if (!array1[i].equals(array2[i])) { return false;}
    }
    return true;
  }
  
  /**
   * Use this to see if a name references a type that needs to be added to the symbolTable.
   */
  private class ResolveNameVisitor extends JExpressionIFAbstractVisitor<TypeData> {
    
    public ResolveNameVisitor() {
    }
    
    /**
     * Most expressions are not relevant for this check--visit them with outer visitor.
     */
    public TypeData defaultCase(JExpressionIF that) {
      that.visit(LanguageLevelVisitor.this);
      return null;
    }
    
    /**
     * Try to look up this simple name reference and match it to a symbol data.
     * If it could not be matched, return a package data.
     * @param that  The thing we're trying to match to a type
     */
    public TypeData forSimpleNameReference(SimpleNameReference that) {
      SymbolData result = getSymbolData(that.getName().getText(), that.getSourceInfo());
      //it could not be resolved: return a Package Data
      if (result==SymbolData.KEEP_GOING) {
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
      
      if (result == SymbolData.KEEP_GOING) { 
        if (lhs instanceof PackageData) {
          return new PackageData((PackageData) lhs, that.getName().getText());
        }
        return null;
      }
      
      return result;
    }
  }
  
  
  
  /**
   * Test the methods defined in the above class.
   */
  public static class LanguageLevelVisitorTest extends TestCase {
    
    private LanguageLevelVisitor _llv;
    
    private SymbolData _sd1;
    private SymbolData _sd2;
    private SymbolData _sd3;
    private SymbolData _sd4;
    private SymbolData _sd5;
    private SymbolData _sd6;
    private ModifiersAndVisibility _publicMav = 
      new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public"});
    private ModifiersAndVisibility _protectedMav = 
      new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"protected"});
    private ModifiersAndVisibility _privateMav = 
      new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"private"});
    private ModifiersAndVisibility _packageMav = 
      new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[0]);
    private ModifiersAndVisibility _finalMav = 
      new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[]{"final"});
    
    public LanguageLevelVisitorTest() { this(""); }
    public LanguageLevelVisitorTest(String name) { 
      super(name);
      _llv = new LanguageLevelVisitor(new File(""), "", 
                                      new LinkedList<String>(), 
                                      new LinkedList<String>(), 
                                      new LinkedList<String>(), 
                                      new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
    }
    
    public void setUp() {
      _llv = new LanguageLevelVisitor(new File(""), "", 
                                      new LinkedList<String>(), 
                                      new LinkedList<String>(), 
                                      new LinkedList<String>(), 
                                      new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>());
      
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      _errorAdded=false;
      symbolTable = new Symboltable();
      _llv.continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>();      
      _hierarchy = new Hashtable<String, TypeDefBase>();
      _classesToBeParsed = new Hashtable<String, Pair<TypeDefBase, LanguageLevelVisitor>>();
      _llv._resetNonStaticFields();
      _llv._importedPackages.add("java.lang");
      _llv._newSDs = new Hashtable<SymbolData, LanguageLevelVisitor>();
      _sd1 = new SymbolData("i.like.monkey");
      _sd2 = new SymbolData("i.like.giraffe");
      _sd3 = new SymbolData("zebra");
      _sd4 = new SymbolData("u.like.emu");
      _sd5 = new SymbolData("");
      _sd6 = new SymbolData("cebu");
    }
    
    /**
     * Tests the getUnqualifiedClassName method.
     */
    public void testGetUnqualifiedClassName() {
      assertEquals("getUnqualifiedClassName with a qualified name with an inner class", "innermonkey", 
                   _llv.getUnqualifiedClassName("i.like.monkey$innermonkey"));
      assertEquals("getUnqualifiedClassName with a qualified name", "monkey", 
                   _llv.getUnqualifiedClassName("i.like.monkey"));
      assertEquals("getUnqualifiedClassName with an unqualified name", "monkey", _llv.getUnqualifiedClassName("monkey"));
      assertEquals("getUnqualifiedClassName with an empty string", "", _llv.getUnqualifiedClassName(""));
    }
    
    public void testClassFile2SymbolData() {
      
      //test a java.lang symbol data
      SymbolData objectSD = _llv._classFile2SymbolData("java.lang.Object", "");
      SymbolData stringSD = _llv._classFile2SymbolData("java.lang.String", "");
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
                   _llv.getSymbolData("java.lang.String", JExprParser.NO_SOURCE_INFO).getPackage());
      
      //now, test that a second call to the same method won't replace the symbol data that is already there.
      SymbolData newStringSD = _llv._classFile2SymbolData("java.lang.String", "");
      assertTrue("Second call to classFileToSymbolData should not change sd in hash table.", 
                 stringSD == _llv.symbolTable.get("java.lang.String"));
      assertTrue("Second call to classFileToSymbolData should return same SD.", 
                 newStringSD == _llv.symbolTable.get("java.lang.String"));      
      //now, test one of our own small class files.
      
      SymbolData bartSD = _llv._classFile2SymbolData("Bart", "testFiles");
      assertFalse("bartSD should not be null", bartSD == null);
      assertFalse("bartSD should not be a continuation", bartSD.isContinuation());
      MethodData md1 = 
        new MethodData("myMethod", _privateMav, 
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
        new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                     new Word(JExprParser.NO_SOURCE_INFO, "Lisa"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Object", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      // Use a ElementaryVisitor so lookupFromClassesToBeParsed will actually visit the ClassDef.
      ElementaryVisitor bv = 
        new ElementaryVisitor(new File(""), errors, symbolTable, continuations, 
                              new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(), 
                              new Hashtable<SymbolData, LanguageLevelVisitor>());
      
      // Test that passing resolve equals false returns a continuation.
      assertTrue("Should return a continuation", 
                 _llv._lookupFromClassesToBeParsed("Lisa", JExprParser.NO_SOURCE_INFO, false).isContinuation());
      // Put Lisa in the hierarchy and test that there is one error and that the message
      // says that there is cyclic inheritance.
//      _hierarchy.put("Lisa", cd);
//      _classesToBeParsed.put("Lisa", new Pair<TypeDefBase, LanguageLevelVisitor>(cd, bv));
//      assertEquals("Should return null because Lisa is in the hierarchy", 
//                   null,
//                   _llv._lookupFromClassesToBeParsed("Lisa", JExprParser.NO_SOURCE_INFO, true));
//      assertEquals("Should be one error", 1, errors.size());
//      assertEquals("Error message should be correct", "Cyclic inheritance involving Lisa", errors.get(0).getFirst());
//      _hierarchy.remove("Lisa");
      //Re-add Lisa because the first call with resolve set to true removed it and
      // test that Lisa is actually visited and added to the symbolTable.
      _classesToBeParsed.put("Lisa", new Pair<TypeDefBase, LanguageLevelVisitor>(cd, bv));
      assertFalse("Should return a non-continuation", 
                  _llv._lookupFromClassesToBeParsed("Lisa", 
                                                    JExprParser.NO_SOURCE_INFO,
                                                    true).isContinuation());
    }
    
    public void testGetSymbolDataForClassFile() {
      // Test that passing a legal class return a non-continuation.
      assertFalse("Should return a non-continuation", 
                  _llv.getSymbolDataForClassFile("java.lang.String", JExprParser.NO_SOURCE_INFO).isContinuation());
      
      // Test that passing a userclass that can't be found returns null and adds an error.
      assertEquals("Should return null with a user class that can't be found",
                   null,
                   _llv.getSymbolDataForClassFile("Marge", JExprParser.NO_SOURCE_INFO));
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "Class Marge not found.", 
                   errors.get(0).getFirst());
    }
    
    
    public void testGetSymbolData_Primitive() {
      assertEquals("should be boolean type", SymbolData.BOOLEAN_TYPE, _llv._getSymbolData_Primitive("boolean"));
      assertEquals("should be char type", SymbolData.CHAR_TYPE, _llv._getSymbolData_Primitive("char"));
      assertEquals("should be byte type", SymbolData.BYTE_TYPE, _llv._getSymbolData_Primitive("byte"));
      assertEquals("should be short type", SymbolData.SHORT_TYPE, _llv._getSymbolData_Primitive("short"));
      assertEquals("should be int type", SymbolData.INT_TYPE, _llv._getSymbolData_Primitive("int"));
      assertEquals("should be long type", SymbolData.LONG_TYPE, _llv._getSymbolData_Primitive("long"));
      assertEquals("should be float type", SymbolData.FLOAT_TYPE, _llv._getSymbolData_Primitive("float"));
      assertEquals("should be double type", SymbolData.DOUBLE_TYPE, _llv._getSymbolData_Primitive("double"));
      assertEquals("should be void type", SymbolData.VOID_TYPE, _llv._getSymbolData_Primitive("void"));
      assertEquals("should be null type", SymbolData.NULL_TYPE, _llv._getSymbolData_Primitive("null"));
      assertEquals("should return null--not a primitive", null, _llv._getSymbolData_Primitive("java.lang.String"));
    }
    
    public void testGetSymbolData_IsQualified() {
      _llv._file = new File("testFiles/Fake.dj0");
      SymbolData sd = new SymbolData("testPackage.File");
      _llv._package = "testPackage";
      symbolTable.put("testPackage.File", sd);
      
      SymbolData sd1 = new SymbolData("java.lang.String");
      symbolTable.put("java.lang.String", sd1);
      
      //Test that classes not in the symbol table are handled correctly.
      assertEquals("should return null--does not exist", null, 
                   _llv._getSymbolData_IsQualified("testPackage.File", JExprParser.NO_SOURCE_INFO, true, false, true));
      assertEquals("should be one error so far.", 1, errors.size());
      
      
      SymbolData sd2 = _llv._getSymbolData_IsQualified("java.lang.Integer", JExprParser.NO_SOURCE_INFO, true, true, true);
      assertEquals("should return non-continuation java.lang.Integer", "java.lang.Integer", sd2.getName());
      assertFalse("should not be a continuation.", sd2.isContinuation());
      
      SymbolData sd3 = _llv._getSymbolData_IsQualified("Wow", JExprParser.NO_SOURCE_INFO, true, true, true);
      assertEquals("should return Wow", "Wow", sd3.getName());
      assertFalse("Should not be a continuation.", sd3.isContinuation());
      
      
      //Test that classes in the symbol table are handled correctly
      assertEquals("should return null sd--does not exist", null, 
                   _llv._getSymbolData_IsQualified("testPackage.File", JExprParser.NO_SOURCE_INFO, false, false, true));
      assertEquals("Should be 2 errors", 2, errors.size());
      
      sd.setIsContinuation(false);
      assertEquals("should return non-continuation sd", sd, 
                   _llv._getSymbolData_IsQualified("testPackage.File", JExprParser.NO_SOURCE_INFO, true, false,  true));
      
      
      assertEquals("Should return sd1.", sd1, 
                   _llv._getSymbolData_IsQualified("java.lang.String", JExprParser.NO_SOURCE_INFO, true, false, true));
      assertFalse("sd1 should no longer be a continuation.", sd1.isContinuation());
      
      
      
      //check that stuff not in symbol table and packaged incorrectly is handled right.
      assertEquals("should return null-because it's not a valid class", null, 
                   _llv._getSymbolData_IsQualified("testPackage.not.in.symboltable", 
                                                   JExprParser.NO_SOURCE_INFO, true, false, true));
      
      assertEquals("should be three errors so far.", 3, errors.size());
      assertNull("should return null", 
                 _llv._getSymbolData_IsQualified("testPackage.not.in.symboltable", 
                                                 JExprParser.NO_SOURCE_INFO, false, false, false));
      
      assertNull("should return null.", 
                 _llv._getSymbolData_IsQualified("notRightPackage", JExprParser.NO_SOURCE_INFO, false, false, false));
      assertEquals("should still be three errors.", 3, errors.size());
    }
    
    public void testGetSymbolData_ArrayType() {
      //Initially, force the inner sd of this array type to be null, to test that.
      assertEquals("Should return null, because inner sd is null.", null, 
                   _llv._getSymbolData_ArrayType("TestFile[]", JExprParser.NO_SOURCE_INFO, false, false, false, false));
      
      /**Now, put a real SymbolData base in the table.*/
      SymbolData sd = new SymbolData("Iexist");
      symbolTable.put("Iexist", sd);
      _llv._getSymbolData_ArrayType("Iexist[]", JExprParser.NO_SOURCE_INFO, false, false, false, false).getName();
      assertTrue("Should have created an array data and add it to symbol table.", symbolTable.containsKey("Iexist[]"));
      SymbolData ad = symbolTable.get("Iexist[]");
      
      //make sure that ad has the appropriate fields and super classes and interfaces and methods
      assertEquals("Should only have field 'length'", 1, ad.getVars().size());
      assertNotNull("Should contain field 'length'", ad.getVar("length"));
      
      assertEquals("Should only have one method-clone", 1, ad.getMethods().size());
      assertTrue("Should contain method clone", ad.hasMethod("clone"));
      
      assertEquals("Should have Object as super class", symbolTable.get("java.lang.Object"), ad.getSuperClass());
      assertEquals("Should have 2 interfaces", 2, ad.getInterfaces().size());
      assertEquals("Interface 1 should be java.lang.Cloneable", "java.lang.Cloneable", 
                   ad.getInterfaces().get(0).getName());
      assertEquals("Interface 2 should be java.io.Serializable", "java.io.Serializable", 
                   ad.getInterfaces().get(1).getName());
      
      
      /**Now try it with the full thing already in the symbol table.*/
      assertEquals("Since it's already in symbol table now, should just return it.", ad, 
                   _llv._getSymbolData_ArrayType("Iexist[]", JExprParser.NO_SOURCE_INFO, false, false, false, false));
      
      /**Now, try it with a multiple dimension array.*/
      _llv._getSymbolData_ArrayType("Iexist[][]", JExprParser.NO_SOURCE_INFO, false, false, false, false);
      assertTrue("Should have added a multidimensional array to the table.", symbolTable.containsKey("Iexist[][]"));
      
      SymbolData sd2 = new SymbolData("String");
      symbolTable.put("String", sd2);
      _llv._getSymbolData_ArrayType("String[][]", JExprParser.NO_SOURCE_INFO, false, false, false, false);
      assertTrue("Should have added String[] to table", symbolTable.containsKey("String[]"));
      assertTrue("Should have added String[][] to table", symbolTable.containsKey("String[][]"));
    }
    
    public void testGetSymbolData_FromCurrFile() {
      _sd4.setIsContinuation(false);
      _sd6.setIsContinuation(true);
      symbolTable.put("u.like.emu", _sd4);
      symbolTable.put("cebu", _sd6);
      
      // Test if it's already in the symbol table and doesn't need to be resolved
      // not stopping when it should.  get error b/c not in classes to be parsed 
      // assertEquals("symbol data is not a continuation, so should just be returned.", _sd6, 
      //   _llv._getSymbolData_FromCurrFile("cebu", JExprParser.NO_SOURCE_INFO, true));
      assertEquals("symbol data is a continuation, but resolve is false, so should just be returned.", _sd4, 
                   _llv._getSymbolData_FromCurrFile("u.like.emu", JExprParser.NO_SOURCE_INFO, false));
      
      //test if it needs to be resolved:
      ClassDef cd = 
        new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                     new Word(JExprParser.NO_SOURCE_INFO, "Lisa"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Object", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      // Use a ElementaryVisitor so lookupFromClassesToBeParsed will actually visit the ClassDef.
      ElementaryVisitor bv = new ElementaryVisitor(new File(""));
      
      _classesToBeParsed.put("Lisa", new Pair<TypeDefBase, LanguageLevelVisitor>(cd, bv));
      assertFalse("Should return a non-continuation", 
                  _llv._getSymbolData_FromCurrFile("Lisa", JExprParser.NO_SOURCE_INFO, true).isContinuation());
    }
    
    public void testGetSymbolData_FromFileSystem() {
      //what if it is in classes to be parsed?
      
      //and what if the class we're looking up is in the same package as the current file?
      //qualified
      _llv._package="fully.qualified";
      _llv._file = new File("testFiles/fully/qualified/Fake.dj0");
      SymbolData sd2 = new SymbolData("fully.qualified.Woah");
      _llv.symbolTable.put("fully.qualified.Woah", sd2);
      
      SymbolData result = 
        _llv._getSymbolData_FromFileSystem("fully.qualified.Woah", JExprParser.NO_SOURCE_INFO, false, true);
      
      assertEquals("Should return sd2, unresolved.", sd2, result);
      assertTrue("sd2 should still be unresolved", sd2.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
      result = _llv._getSymbolData_FromFileSystem("fully.qualified.Woah", JExprParser.NO_SOURCE_INFO, false, true);
      assertEquals("Should return sd2, now unresolved.", sd2, result);
      assertTrue("sd2 should not be resolved", sd2.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
      result = _llv._getSymbolData_FromFileSystem("fully.qualified.Woah", JExprParser.NO_SOURCE_INFO, true, true);
      assertEquals("Should return sd2, now resolved.", sd2, result);
      assertFalse("sd2 should now be resolved", sd2.isContinuation());   
      assertEquals("Should be no errors", 0, errors.size());
      
      
      //what if the files are in different packages
      _llv.symbolTable.remove("fully.qualified.Woah");
      _llv.visitedFiles.clear();
      _llv._package="another.package";
      _llv._file = new File("testFiles/another/package/Wowsers.dj0");
      sd2 = new SymbolData("fully.qualified.Woah");
      _llv.symbolTable.put("fully.qualified.Woah", sd2);
      
      result = _llv._getSymbolData_FromFileSystem("fully.qualified.Woah", JExprParser.NO_SOURCE_INFO, false, true);
      
      assertEquals("Should return sd2, unresolved.", sd2, result);
      assertTrue("sd2 should still be unresolved", sd2.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
      result = _llv._getSymbolData_FromFileSystem("fully.qualified.Woah", JExprParser.NO_SOURCE_INFO, true, true);
      assertEquals("Should return sd2, now resolved.", sd2, result);
      
      assertFalse("sd2 should be resolved", sd2.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
      //what if there is no package
      //Now, check cases when desired SymbolData is in the symbol table.
      _llv._package = "";
      _llv._file = new File ("testFiles/Cool.dj0");
      
      //unqualified
      SymbolData sd1 = new SymbolData("Wow");
      SymbolData obj = _llv._getSymbolData_FromFileSystem("java.lang.Object", JExprParser.NO_SOURCE_INFO, true, true);
      sd1.setSuperClass(obj);
      _llv.symbolTable.put("Wow", sd1);
      
      result = _llv._getSymbolData_FromFileSystem("Wow", JExprParser.NO_SOURCE_INFO, false, true);
      assertEquals("Should return sd1, unresolved.", sd1, result);
      assertTrue("sd1 should still be unresolved.", sd1.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
      result = _llv._getSymbolData_FromFileSystem("Wow", JExprParser.NO_SOURCE_INFO, true, true);
      assertEquals("Should return sd1, resolved.", sd1, result);
      assertFalse("sd1 should be resolved.", sd1.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
      result = _llv._getSymbolData_FromFileSystem("Wow", JExprParser.NO_SOURCE_INFO, true, true);
      assertEquals("Should return sd1.", sd1, result);
      assertFalse("sd1 should still be resolved.", sd1.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
      
      //finding the most recent file
      result = _llv._getSymbolData_FromFileSystem("James", JExprParser.NO_SOURCE_INFO, true, true);
      assertEquals("Result should be a symbol data corresponding to James", "James", result.getName());
      assertEquals("Result should have 3 variables.", 3, result.getVars().size());
      assertEquals("Should be no errors", 0, errors.size());
      
      //returning KEEP_GOING when it doesn't exist.
      _llv._package = "myPackage";
      assertEquals("Should return KEEP_GOING-does not exist.", SymbolData.KEEP_GOING, 
                   _llv._getSymbolData_FromFileSystem("WrongPackage.className", JExprParser.NO_SOURCE_INFO, true, false));
      assertEquals("Should be no errors", 0, errors.size());
      
      //Now, test case where class file still exists, but java file is gone.
      _llv._package = "";
      _llv._file = new File("testFiles/Fake.dj0");
      LinkedList<VariableData> vds = new LinkedList<VariableData>();
      result = _llv._getSymbolData_FromFileSystem("Doh", JExprParser.NO_SOURCE_INFO, true, true);
      vds.addLast(new VariableData("i", _packageMav, SymbolData.INT_TYPE, true, result));
      vds.addLast(new VariableData("o", _packageMav, obj, true, result));
      
      assertEquals("should have correct variable datas", vds, result.getVars());
      assertFalse("should not be a continuation", result.isContinuation());
      
      //Now test case where java file has been updated more recently than class file.
      //TODO: How can we test this since repository is checked out (i.e. the files all have the same timestamp)?
    }
    
    public void testGetSymbolData() {
      _llv._package="";
      _llv._file = new File("testFiles/akdjskj");
      
      // No dot case
      SymbolData sd1 = new SymbolData("Wow");
      _llv.symbolTable.put("Wow", sd1);
      assertEquals("Should return an equal SymbolData", 
                   sd1, _llv.getSymbolData("Wow", JExprParser.NO_SOURCE_INFO, true, false));
      assertFalse("Should not be a continuation", sd1.isContinuation());
      
      // Invalid case
      SymbolData result = _llv.getSymbolData("ima.invalid", JExprParser.NO_SOURCE_INFO, true, false);
      assertEquals("Should return null-invalid class name", null, result);
      assertEquals("There should be one error", 1, _llv.errors.size());
      assertEquals("The error message should be correct", "Invalid class name ima.invalid", errors.get(0).getFirst());
      
      // Fully qualified class name
      _llv._package="fully.qualified";
      _llv._file = new File("testFiles/fully/qualified/Fake.dj0");
      SymbolData sd2 = new SymbolData("fully.qualified.Woah");
      _llv.symbolTable.put("fully.qualified.Woah", sd2);
      
      result = _llv.getSymbolData("fully.qualified.Woah", JExprParser.NO_SOURCE_INFO, true, false);
      
      assertEquals("Should return sd2, resolved.", sd2, result);
      assertFalse("sd2 should be resolved", sd2.isContinuation());
      
      // Inner class
      sd1.setName("fully.qualified.Woah.Wow");
      sd2.addInnerClass(sd1);
      sd1.setOuterData(sd2);
      result = _llv.getSymbolData("fully.qualified.Woah.Wow", JExprParser.NO_SOURCE_INFO, true, false);
      assertEquals("Should return sd1 (the inner class!)", sd1, result);
      
      // Inner inner class
      SymbolData sd3 = new SymbolData("fully.qualified.Woah.Wow.James");
      sd1.addInnerClass(sd3);
      sd3.setOuterData(sd1);
      result = _llv.getSymbolData("fully.qualified.Woah.Wow.James", JExprParser.NO_SOURCE_INFO, true, false);
      assertEquals("Should return sd3", sd3, result);
    }
    
    
    public void testGetSymbolDataHelper() {
      // Primitive types
      assertEquals("should return the int SymbolData", SymbolData.INT_TYPE, 
                   _llv.getSymbolDataHelper("int", JExprParser.NO_SOURCE_INFO, true, true, true, true));
      assertEquals("should return the byte SymbolData", SymbolData.BYTE_TYPE, 
                   _llv.getSymbolDataHelper("byte", JExprParser.NO_SOURCE_INFO, false, false, false, true));
      
      // Array types
      ArrayData ad = new ArrayData(SymbolData.INT_TYPE, _llv, JExprParser.NO_SOURCE_INFO);
      SymbolData result = _llv.getSymbolDataHelper("int[]", JExprParser.NO_SOURCE_INFO, true, true, true, true);
      ad.getVars().get(0).setEnclosingData(result);  //.equals(...) on VariableData compares enclosing datas with ==.
      ad.getMethods().get(0).setEnclosingData(result.getMethods().get(0).getEnclosingData()); //similar hack
      assertEquals("should return the array type", ad, result);
      
      // Qualified types
      SymbolData sd = new SymbolData("java.lang.System");
      symbolTable.put("java.lang.System", sd);
      assertEquals("should return the same sd", sd, 
                   _llv.getSymbolDataHelper("java.lang.System", JExprParser.NO_SOURCE_INFO, false, true, true, true));
      assertTrue("should be a continuation", sd.isContinuation());
      assertEquals("should return the now resolved sd", sd, 
                   _llv.getSymbolDataHelper("java.lang.System", JExprParser.NO_SOURCE_INFO, true, false, true, true));
      assertFalse("should not be a continuation", sd.isContinuation());
      
      // In this file
      sd = new SymbolData("fully.qualified.Qwerty");
      symbolTable.put("fully.qualified.Qwerty", sd);
      _llv._classNamesInThisFile.addLast("fully.qualified.Qwerty");
      // Use a ElementaryVisitor so lookupFromClassesToBeParsed will actually visit the ClassDef.
      ElementaryVisitor bv = new ElementaryVisitor(new File(""), errors, symbolTable, continuations, 
                                                   new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>(), _newSDs);
      bv._package = "fully.qualified";
      bv._file = new File("testFiles/fully/qualified/Fake.dj0");
      ClassDef cd = new ClassDef(JExprParser.NO_SOURCE_INFO, 
                                 _packageMav, 
                                 new Word(JExprParser.NO_SOURCE_INFO, "Qwerty"),
                                 new TypeParameter[0],
                                 new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Object", new Type[0]),
                                 new ReferenceType[0], 
                                 new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      _llv._classesToBeParsed.put("fully.qualified.Qwerty", new Pair<TypeDefBase, LanguageLevelVisitor>(cd, bv));
      assertEquals("should return sd the continuation", sd, 
                   bv.getSymbolDataHelper("Qwerty", JExprParser.NO_SOURCE_INFO, false, true, true, true));
      assertTrue("should be a continuation", sd.isContinuation());
      assertEquals("should return sd, now resolved", sd, 
                   bv.getSymbolDataHelper("Qwerty", JExprParser.NO_SOURCE_INFO, true, true, true, true));
      assertFalse("should not be a continuation", sd.isContinuation());
      
      // Imported files
      _llv._importedFiles.addLast("a.b.c");
      sd = new SymbolData("a.b.c");
      symbolTable.put("a.b.c.", sd);
      assertEquals("should find the continuation in the symbol table", sd, 
                   _llv.getSymbolDataHelper("c", JExprParser.NO_SOURCE_INFO, false, true, true, true));
      assertTrue("should be a continuation", sd.isContinuation());
      
      _llv._package="fully.qualified";
      _llv._file = new File("testFiles/fully/qualified/Fake.dj0");
      _llv._importedFiles.addLast("fully.qualified.Woah");
      SymbolData sd2 = new SymbolData("fully.qualified.Woah");
      symbolTable.put("fully.qualified.Woah", sd2);
      assertEquals("should find the resolved symbol data in the symbol table", sd2, 
                   _llv.getSymbolDataHelper("Woah", JExprParser.NO_SOURCE_INFO, true, false, true, true));
      assertFalse("should not be a continuation", sd2.isContinuation());
      
      // File System
      _llv._importedFiles.clear();
      _llv.visitedFiles.clear();
      symbolTable.remove("fully.qualified.Woah");
      sd2 = new SymbolData("fully.qualified.Woah");
      _llv.symbolTable.put("fully.qualified.Woah", sd2);
      
      result = _llv.getSymbolDataHelper("Woah", JExprParser.NO_SOURCE_INFO, false, true, true, true);
      
      assertEquals("Should return sd2, unresolved.", sd2, result);
      assertTrue("sd2 should still be unresolved", sd2.isContinuation());
      
      result = _llv.getSymbolDataHelper("Woah", JExprParser.NO_SOURCE_INFO, false, true, true, true);
      assertEquals("Should return sd2, now unresolved.", sd2, result);
      assertTrue("sd2 should not be resolved", sd2.isContinuation());
      
      result = _llv.getSymbolDataHelper("Woah", JExprParser.NO_SOURCE_INFO, true, true, true, true);
      assertEquals("Should return sd2, now resolved.", sd2, result);
      assertFalse("sd2 should now be resolved", sd2.isContinuation());
      
      // Imported Packages
      symbolTable.remove("fully.qualified.Woah");
      _llv.visitedFiles.clear();
      _llv._file = new File("testFiles/Fake.dj0");
      _llv._package = "";
      _llv._importedPackages.addLast("fully.qualified");
      sd2 = new SymbolData("fully.qualified.Woah");
      symbolTable.put("fully.qualified.Woah", sd2);
      assertEquals("should find the unresolved symbol data in the symbol table", sd2, 
                   _llv.getSymbolDataHelper("Woah", JExprParser.NO_SOURCE_INFO, false, false, true, true));
      assertTrue("should not be a continuation", sd2.isContinuation());
      result = _llv.getSymbolDataHelper("Woah", JExprParser.NO_SOURCE_INFO, true, false, true, true);
      assertEquals("should find the resolved symbol data in the symbol table", sd2, result);
      assertFalse("should not be a continuation", sd2.isContinuation());
      
      //test java.lang classes that need to be looked up
      //want to resolve
      SymbolData stringSD = new SymbolData("java.lang.String");
      SymbolData newsd1 = _llv.getSymbolDataHelper("String", JExprParser.NO_SOURCE_INFO, true, true, true, true);
      assertEquals("should have correct name.", stringSD.getName(), newsd1.getName());
      assertFalse("should not be a continuation", newsd1.isContinuation());
      
      // Test ambiguous class name (i.e. it is unqualified, and matches unqualified names in 2 or more packages.
      symbolTable.put("random.package.String", new SymbolData("random.package.String"));
      symbolTable.put("java.lang.Object", new SymbolData("java.lang.Object"));
      _llv._importedPackages.addLast("random.package");
      result = _llv.getSymbolDataHelper("String", JExprParser.NO_SOURCE_INFO, true, true, true, true);
      assertEquals("Result should be null", null, result);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "The class name String is ambiguous." + 
                   "  It could be java.lang.String or random.package.String", 
                   errors.get(0).getFirst());
      
      symbolTable.remove("random.package.String");
      
    }
    
    public void test_forModifiersAndVisibility() {
      // Test access specifiers.
      _llv.forModifiersAndVisibility(_publicMav);
      _llv.forModifiersAndVisibility(_protectedMav);
      _llv.forModifiersAndVisibility(_privateMav);
      _llv.forModifiersAndVisibility(_packageMav);
      
      
      assertEquals("There should be no errors.", 0, errors.size());
      
      // Test "public", "private"
      ModifiersAndVisibility testMav = 
        new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public", "private"});
      _llv.forModifiersAndVisibility(testMav);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "Illegal combination of modifiers." + 
                   " Can't use private and public together.", errors.get(0).getFirst());
      
      // Test "public", "abstract"
      testMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"public", "abstract"});
      _llv.forModifiersAndVisibility(testMav);
      assertEquals("Still only one error.", 1, errors.size());
      
      // Test "abstract", "final"
      testMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"abstract", "final"});
      _llv.forModifiersAndVisibility(testMav);
      assertEquals("There should be two errors.", 2, errors.size());
      assertEquals("The error message should be correct.", "Illegal combination of modifiers." + 
                   " Can't use final and abstract together.", errors.get(1).getFirst());
      
      // Test "final", "abstract"
      testMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"final", "abstract"});
      _llv.forModifiersAndVisibility(testMav);
      assertEquals("There should be two errors.", 3, errors.size());
      assertEquals("The error message should be correct.", "Illegal combination of modifiers." + 
                   " Can't use final and abstract together.", errors.get(2).getFirst());
      
      // Test "volatile", "final"
      testMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"volatile", "final"});
      _llv.forModifiersAndVisibility(testMav);
      assertEquals("There should be two errors.", 4, errors.size());
      assertEquals("The error message should be correct.", "Illegal combination of modifiers." + 
                   " Can't use final and volatile together.", errors.get(3).getFirst());
      
      // Test "static", "final", "static"
      testMav = new ModifiersAndVisibility(JExprParser.NO_SOURCE_INFO, new String[] {"static", "final", "static"});
      _llv.forModifiersAndVisibility(testMav);
      assertEquals("There should be two errors.", 5, errors.size());
      assertEquals("The error message should be correct.", "Duplicate modifier: static", errors.get(4).getFirst());
    }
    
    public void testGetQualifiedClassName() {
      //first test when the package is empty:
      _llv._package="";
      assertEquals("Should not change qualified name.", "simpson.Bart", _llv.getQualifiedClassName("simpson.Bart"));
      assertEquals("Should not change unqualified name.", "Lisa", _llv.getQualifiedClassName("Lisa"));
      
      //now test when package is not empty.
      _llv._package="myPackage";
      assertEquals("Should not change properly packaged qualified name.", "myPackage.Snowball", 
                   _llv.getQualifiedClassName("myPackage.Snowball"));
      assertEquals("Should append package to front of not fully packaged name", "myPackage.simpson.Snowball", 
                   _llv.getQualifiedClassName("simpson.Snowball"));
      assertEquals("Should append package to front of unqualified class name.", "myPackage.Grandpa", 
                   _llv.getQualifiedClassName("Grandpa"));
    }
    
    public void testAddSymbolData() {
      /**Put super class in symbol table.*/
      SymbolData obj = new SymbolData("java.lang.Object");
      obj.setIsContinuation(false);
      symbolTable.put("java.lang.Object", obj);
      
      ClassDef cd = 
        new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, new Word(JExprParser.NO_SOURCE_INFO, "Awesome"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      SymbolData sd = new SymbolData("Awesome"); /**Create a continuation and store it in table.*/
      sd.setSuperClass(symbolTable.get("java.lang.Object"));
      symbolTable.put("Awesome", sd);
      SymbolData result = _llv.addSymbolData(cd, "Awesome");
      assertFalse("result should not be a continuation.", result.isContinuation());
      assertFalse("sd should also no longer be a continuation.", sd.isContinuation());
      assertEquals("result and sd should be equal.", sd, result);
      
      /**Hierarchy should be empty at the end.*/
      assertEquals("hierarchy should be empty", 0, _hierarchy.size());
      
      /**Check that if the class is already defined, an appropriate error is thrown.*/
      assertEquals("Should return null, because it is already in the SymbolTable.", null, _llv.addSymbolData(cd, "Awesome"));
      assertEquals("Length of errors should now be 1.", 1, errors.size());
      assertEquals("Error message should be correct.", "This class has already been defined.", errors.get(0).getFirst());
      assertEquals("hierarchy should be empty.", 0, _hierarchy.size());
      
    }
    
    public void test_variableDeclaration2VariableData() {
      VariableDeclarator[] d1 = {
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                            new Word(JExprParser.NO_SOURCE_INFO, "i")) };
      VariableDeclaration vd1 = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,_publicMav, d1); 
      VariableData[] vdata1 = { new VariableData("i", _publicMav, SymbolData.INT_TYPE, false, _sd1) };
      
      assertTrue("Should properly recognize a basic VariableDeclaration", 
                 arrayEquals(vdata1, _llv._variableDeclaration2VariableData(vd1, _sd1)));
      
      VariableDeclarator[] d2 = {
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                            new PrimitiveType(JExprParser.NO_SOURCE_INFO, "int"), 
                                            new Word(JExprParser.NO_SOURCE_INFO, "i")), 
        new InitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                          new PrimitiveType(JExprParser.NO_SOURCE_INFO, "boolean"), 
                                          new Word(JExprParser.NO_SOURCE_INFO, "b"), 
                                          new BooleanLiteral(JExprParser.NO_SOURCE_INFO, true)) };
      VariableDeclaration vd2 = new VariableDeclaration(JExprParser.NO_SOURCE_INFO,_privateMav, d2); 
      VariableData bData = new VariableData("b", _privateMav, SymbolData.BOOLEAN_TYPE, true, _sd1);
      bData.setHasInitializer(true);
      VariableData[] vdata2 = {new VariableData("i", _privateMav, SymbolData.INT_TYPE, false, _sd1),
        bData};
      
      assertTrue("Should properly recognize a more complicated VariableDeclaration", 
                 arrayEquals(vdata2, _llv._variableDeclaration2VariableData(vd2, _sd1)));
      
      //check that if the type cannot be found, no error is thrown.
      VariableDeclarator[] d3 = { 
        new UninitializedVariableDeclarator(JExprParser.NO_SOURCE_INFO, 
                                            new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "LinkedList", new Type[0]), 
                                            new Word(JExprParser.NO_SOURCE_INFO, "myList"))};
      VariableDeclaration vd3 = new VariableDeclaration(JExprParser.NO_SOURCE_INFO, _privateMav, d3);
      _llv._variableDeclaration2VariableData(vd3, _sd1);
      assertEquals("There should now be no errors", 0, errors.size());
//     assertEquals("The error message should be correct", "Class or Interface LinkedList not found", 
//                  errors.get(0).getFirst());
      
    }
    
    public void test_addError() {
      LinkedList<Pair<String, JExpressionIF>> e = new LinkedList<Pair<String, JExpressionIF>>();
      
      NullLiteral nl = new NullLiteral(JExprParser.NO_SOURCE_INFO);
      NullLiteral nl2 = new NullLiteral(JExprParser.NO_SOURCE_INFO);
      
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
      
      NullLiteral nl = new NullLiteral(JExprParser.NO_SOURCE_INFO);
      NullLiteral nl2 = new NullLiteral(JExprParser.NO_SOURCE_INFO);
      
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
        new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                     new Word(JExprParser.NO_SOURCE_INFO, "Awesome"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      _llv.forClassDefDoFirst(cd);
      assertEquals("There should be no errors.", 0, errors.size());
      _llv._importedFiles.addLast(new File("Awesome").getAbsolutePath());
      _llv.forClassDefDoFirst(cd);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "The class Awesome was already imported.", 
                   errors.get(0).getFirst());
      
      ClassDef cd2 = new ClassDef(JExprParser.NO_SOURCE_INFO, _privateMav, 
                                  new Word(JExprParser.NO_SOURCE_INFO, "privateClass"),
                                  new TypeParameter[0], 
                                  new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), 
                                  new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      _llv.forClassDefDoFirst(cd2);
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("The 2nd error message should be correct", "Top level classes cannot be private", 
                   errors.get(1).getFirst());
      
    }
    
    public void testForInterfaceDefDoFirst() {
      InterfaceDef id = new InterfaceDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                                         new Word(JExprParser.NO_SOURCE_INFO, "Awesome"),
                                         new TypeParameter[0], new ReferenceType[0], 
                                         new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      _llv.forInterfaceDefDoFirst(id);
      assertEquals("There should be no errors.", 0, errors.size());
      
      InterfaceDef id2 = new InterfaceDef(JExprParser.NO_SOURCE_INFO, _privateMav, 
                                          new Word(JExprParser.NO_SOURCE_INFO, "privateinterface"),
                                          new TypeParameter[0], new ReferenceType[0], 
                                          new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      _llv.forInterfaceDefDoFirst(id2);
      assertEquals("There should be 1 errors", 1, errors.size());
      assertEquals("The error message should be correct", "Top level interfaces cannot be private", 
                   errors.get(0).getFirst());
      
      InterfaceDef id3 = new InterfaceDef(JExprParser.NO_SOURCE_INFO, _finalMav, 
                                          new Word(JExprParser.NO_SOURCE_INFO, "finalinterface"),
                                          new TypeParameter[0], new ReferenceType[0], 
                                          new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      _llv.forInterfaceDefDoFirst(id3);
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct", "Interfaces cannot be final", errors.get(1).getFirst());      
    }
    
    public void testForInnerInterfaceDefDoFirst() {
      InterfaceDef id = new InterfaceDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                                         new Word(JExprParser.NO_SOURCE_INFO, "Awesome"),
                                         new TypeParameter[0], new ReferenceType[0], 
                                         new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      id.visit(_llv);
      assertEquals("There should be no errors.", 0, errors.size());
      
      InnerInterfaceDef id2 = 
        new InnerInterfaceDef(JExprParser.NO_SOURCE_INFO, _finalMav, new Word(JExprParser.NO_SOURCE_INFO, "finalinterface"),
                              new TypeParameter[0], new ReferenceType[0], 
                              new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      id2.visit(_llv);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "Interfaces cannot be final", errors.get(0).getFirst());   
    }
    
    public void testForPackageStatementOnly() {
      Word[] words = new Word[] {new Word(JExprParser.NO_SOURCE_INFO, "alpha"),
        new Word(JExprParser.NO_SOURCE_INFO, "beta")};
      CompoundWord cw = new CompoundWord(JExprParser.NO_SOURCE_INFO, words);
      PackageStatement ps = new PackageStatement(JExprParser.NO_SOURCE_INFO, cw);
      _llv._file = new File("alpha/beta/delta");
      _llv.forPackageStatementOnly(ps);
      assertEquals("_package should be set correctly.", "alpha.beta", _llv._package);
      assertEquals("There should be no errors.", 0, errors.size());
      _llv._file = new File("alpha/beta/beta/delta");
      _llv.forPackageStatementOnly(ps);
      assertEquals("_package should be set correctly.", "alpha.beta", _llv._package);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "The package name must mirror your file's directory.", 
                   errors.get(0).getFirst());
    }
    
    public void testForClassImportStatementOnly() {
      
      //Test one that works
      Word[] words = new Word[] {new Word(JExprParser.NO_SOURCE_INFO, "alpha"),
        new Word(JExprParser.NO_SOURCE_INFO, "beta")};
      CompoundWord cw = new CompoundWord(JExprParser.NO_SOURCE_INFO, words);
      ClassImportStatement cis = new ClassImportStatement(JExprParser.NO_SOURCE_INFO, cw);
      SymbolData sd = new SymbolData("alpha.beta");
      _llv.forClassImportStatementOnly(cis);
      assertTrue("imported files should contain alpha.beta", _llv._importedFiles.contains("alpha.beta"));
      assertEquals("There should be a continuation.", sd, symbolTable.get("alpha.beta"));
      assertTrue("It should be in continuations.", _llv.continuations.containsKey("alpha.beta"));
      
      // Test one that should throw an error: Class has already been imported. alpha.beta should now be in the 
      // symbolTable, and alpha should be in the list of packages, so this will throw an error
      Word[] words2 = new Word[] {new Word(JExprParser.NO_SOURCE_INFO, "gamma"),
        new Word(JExprParser.NO_SOURCE_INFO, "beta")};
      CompoundWord cw2 = new CompoundWord(JExprParser.NO_SOURCE_INFO, words2);
      ClassImportStatement cis2 = new ClassImportStatement(JExprParser.NO_SOURCE_INFO, cw2);
      cis2.visit(_llv);
      
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", "The class beta has already been imported.", 
                   errors.get(0).getFirst());
      
      //Test one that should throw an error: Importing a class from the current package
      _llv._package = "myPackage";
      Word[] words3 = 
        new Word[] { new Word(JExprParser.NO_SOURCE_INFO, "myPackage"), new Word(JExprParser.NO_SOURCE_INFO, "cookie")};
      CompoundWord cw3 = new CompoundWord(JExprParser.NO_SOURCE_INFO, words3);
      ClassImportStatement cis3 = new ClassImportStatement(JExprParser.NO_SOURCE_INFO, cw3);
      cis3.visit(_llv);
      
      assertEquals("There should now be 2 errors", 2, errors.size());
      assertEquals("The second error message should be correct", "You do not need to import myPackage.cookie." + 
                   "  It is in your package so it is already visible", errors.get(1).getFirst());
      
      
    }
    
    public void testForPackageImportStatementOnly() {
      //Test one that works
      Word[] words = new Word[] {new Word(JExprParser.NO_SOURCE_INFO, "alpha"),
        new Word(JExprParser.NO_SOURCE_INFO, "beta")};
      CompoundWord cw = new CompoundWord(JExprParser.NO_SOURCE_INFO, words);
      PackageImportStatement cis = new PackageImportStatement(JExprParser.NO_SOURCE_INFO, cw);
      SymbolData sd = new SymbolData("alpha.beta");
      _llv.forPackageImportStatementOnly(cis);
      assertEquals("There should be no errorrs", 0, errors.size());
      assertTrue("Imported Packages should now contain alpha.beta", _llv._importedPackages.contains("alpha.beta"));
      
      //Test one that should not throw an error: Importing a subpackage of the current package
      _llv._package = "myPackage";
      Word[] words3 = new Word[] {new Word(JExprParser.NO_SOURCE_INFO, "myPackage"), new Word(JExprParser.NO_SOURCE_INFO, 
                                                                                              "cookie")};
      CompoundWord cw3 = new CompoundWord(JExprParser.NO_SOURCE_INFO, words3);
      PackageImportStatement pis = new PackageImportStatement(JExprParser.NO_SOURCE_INFO, cw3);
      pis.visit(_llv);
      
      assertEquals("There should be no errors", 0, errors.size());
      assertTrue("Imported Packages should now contain myPackage.cookie", 
                 _llv._importedPackages.contains("myPackage.cookie"));
      
      
      
      //Test one that should throw an error: Importing the current package
      Word[] words2 = new Word[] {new Word(JExprParser.NO_SOURCE_INFO, "myPackage")};
      CompoundWord cw2 = new CompoundWord(JExprParser.NO_SOURCE_INFO, words2);
      PackageImportStatement pis2 = new PackageImportStatement(JExprParser.NO_SOURCE_INFO, cw2);
      pis2.visit(_llv);
      
      assertEquals("There should now be 1 errors", 1, errors.size());
      assertEquals("The error message should be correct", "You do not need to import package myPackage." + 
                   " It is your package so all public classes in it are already visible.", errors.get(0).getFirst());
      
    }
    
    public void testForSourceFile() {
      ClassDef cd = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, new Word(JExprParser.NO_SOURCE_INFO, "Awesome"),
                                 new TypeParameter[0], 
                                 new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "java.lang.Object", new Type[0]), 
                                 new ReferenceType[0], 
                                 new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      ClassDef cd2 = new ClassDef(JExprParser.NO_SOURCE_INFO, _publicMav, new Word(JExprParser.NO_SOURCE_INFO, "Gnarly"),
                                  new TypeParameter[0], 
                                  new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, "Awesome", new Type[0]), 
                                  new ReferenceType[0], 
                                  new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      InterfaceDef id = new InterfaceDef(JExprParser.NO_SOURCE_INFO, _publicMav, 
                                         new Word(JExprParser.NO_SOURCE_INFO, "NiftyWords"),
                                         new TypeParameter[0], 
                                         new ReferenceType[0], 
                                         new BracedBody(JExprParser.NO_SOURCE_INFO, new BodyItemI[0]));
      
      SourceFile sf = new SourceFile(JExprParser.NO_SOURCE_INFO,
                                     new PackageStatement[0],
                                     new ImportStatement[0],
                                     new TypeDefBase[] {cd, cd2, id});
      _llv.forSourceFile(sf);
      
      assertTrue("_classNamesInThisFile should contain the two ClassDefs.", 
                 _llv._classNamesInThisFile.contains("Awesome"));
      assertTrue("_classNamesInThisFile should contain the two ClassDefs.", _llv._classNamesInThisFile.contains("Gnarly"));
      
      assertTrue("_classNamesInThisFile should contain the InterfaceDef", _llv._classNamesInThisFile.contains("NiftyWords"));
      assertTrue("_classesToBeParsed should contain the two ClassDefs.", _llv._classesToBeParsed.containsKey("Awesome"));
      assertTrue("_classesToBeParsed should contain the two ClassDefs.", _llv._classesToBeParsed.containsKey("Gnarly"));
      assertTrue("_classesToBeParsed should contain the InterfaceDef", _llv._classesToBeParsed.containsKey("NiftyWords"));
      
    }
    
    public void testReferenceType2String() {
      // Try a TypeVariable
      TypeVariable tv = new TypeVariable(JExprParser.NO_SOURCE_INFO, "T");
      String[] result = _llv.referenceType2String(new ReferenceType[] { tv });
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("Results should have one String.", 1, result.length);
      assertEquals("The String should be \"T\".", "T", result[0]);
      
      // Try a ClassOrInterfaceType
      ClassOrInterfaceType coit = new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, 
                                                           "MyClass", 
                                                           new Type[] { new TypeVariable(JExprParser.NO_SOURCE_INFO, "T"),
        new TypeVariable(JExprParser.NO_SOURCE_INFO, "U")}
      );
      result = _llv.referenceType2String(new ReferenceType[] { tv, coit });
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("Results should have two Strings.", 2, result.length);
      assertEquals("The first String should be \"T\".", "T", result[0]);
      assertEquals("The second String should be \"MyClass\".", "MyClass", result[1]);
      
      // Try a MemberType
      MemberType mt = new MemberType(JExprParser.NO_SOURCE_INFO,
                                     "MyClass.MyClass2",
                                     coit,
                                     new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, 
                                                              "MyClass2", 
                                                              new Type[0]));
      result = _llv.referenceType2String(new ReferenceType[] { mt });
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("Results should have one String.", 1, result.length);
      assertEquals("The first String should be \"MyClass.MyClass2\".", "MyClass.MyClass2", result[0]);
    }
    
    
    public void testExceptionsInSymbolTable() {
      //make sure that exceptions are being added to symbol table 
      BracedBody bb = 
        new BracedBody(JExprParser.NO_SOURCE_INFO, 
                       new BodyItemI[] { 
        new ThrowStatement(JExprParser.NO_SOURCE_INFO, 
                           new SimpleNamedClassInstantiation(JExprParser.NO_SOURCE_INFO, 
                                                             new ClassOrInterfaceType(JExprParser.NO_SOURCE_INFO, 
                                                                                      "java.util.prefs.BackingStoreException", 
                                                                                      new Type[0]),
                                                             new ParenthesizedExpressionList(JExprParser.NO_SOURCE_INFO, 
                                                                                             new Expression[0])))});
      
      bb.visit(_llv);
      assertFalse("The SymbolTable should have java.util.prefs.BackingStoreException", 
                  symbolTable.get("java.util.prefs.BackingStoreException")==null);
      
    }
    
    public void testShouldBreak() {
      //shift assignment expressions:
      LeftShiftAssignmentExpression shift1 = 
        new LeftShiftAssignmentExpression(JExprParser.NO_SOURCE_INFO, new NullLiteral(JExprParser.NO_SOURCE_INFO), 
                                          new NullLiteral(JExprParser.NO_SOURCE_INFO));
      RightUnsignedShiftAssignmentExpression shift2 = 
        new RightUnsignedShiftAssignmentExpression(JExprParser.NO_SOURCE_INFO, 
                                                   new NullLiteral(JExprParser.NO_SOURCE_INFO), 
                                                   new NullLiteral(JExprParser.NO_SOURCE_INFO));
      RightSignedShiftAssignmentExpression shift3 = 
        new RightSignedShiftAssignmentExpression(JExprParser.NO_SOURCE_INFO, 
                                                 new NullLiteral(JExprParser.NO_SOURCE_INFO), 
                                                 new NullLiteral(JExprParser.NO_SOURCE_INFO));
      
      
      shift1.visit(_llv);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("error message should be correct", "Shift assignment operators cannot be used at any language level",
                   errors.getLast().getFirst());
      
      shift2.visit(_llv);
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("error message should be correct", "Shift assignment operators cannot be used at any language level",
                   errors.getLast().getFirst());
      
      shift3.visit(_llv);
      assertEquals("Should be 3 errors", 3, errors.size());
      assertEquals("error message should be correct", "Shift assignment operators cannot be used at any language level",
                   errors.getLast().getFirst());
      
      //BitwiseAssignmentExpressions
      BitwiseAndAssignmentExpression bit1 = 
        new BitwiseAndAssignmentExpression(JExprParser.NO_SOURCE_INFO, new NullLiteral(JExprParser.NO_SOURCE_INFO), 
                                           new NullLiteral(JExprParser.NO_SOURCE_INFO));
      BitwiseOrAssignmentExpression bit2 = 
        new BitwiseOrAssignmentExpression(JExprParser.NO_SOURCE_INFO, new NullLiteral(JExprParser.NO_SOURCE_INFO), 
                                          new NullLiteral(JExprParser.NO_SOURCE_INFO));
      BitwiseXorAssignmentExpression bit3 = 
        new BitwiseXorAssignmentExpression(JExprParser.NO_SOURCE_INFO, new NullLiteral(JExprParser.NO_SOURCE_INFO), 
                                           new NullLiteral(JExprParser.NO_SOURCE_INFO));
      
      bit1.visit(_llv);
      assertEquals("Should be 4 errors", 4, errors.size());
      assertEquals("error message should be correct", "Bitwise operators cannot be used at any language level", 
                   errors.getLast().getFirst());
      
      bit2.visit(_llv);
      assertEquals("Should be 5 errors", 5, errors.size());
      assertEquals("error message should be correct", "Bitwise operators cannot be used at any language level", 
                   errors.getLast().getFirst());
      
      bit3.visit(_llv);
      assertEquals("Should be 6 errors", 6, errors.size());
      assertEquals("error message should be correct", "Bitwise operators cannot be used at any language level", 
                   errors.getLast().getFirst());
      
      //BitwiseExpressions
      BitwiseAndExpression bit4 = 
        new BitwiseAndExpression(JExprParser.NO_SOURCE_INFO, new NullLiteral(JExprParser.NO_SOURCE_INFO), 
                                 new NullLiteral(JExprParser.NO_SOURCE_INFO));
      BitwiseOrExpression bit5 = 
        new BitwiseOrExpression(JExprParser.NO_SOURCE_INFO, new NullLiteral(JExprParser.NO_SOURCE_INFO), 
                                new NullLiteral(JExprParser.NO_SOURCE_INFO));
      BitwiseXorExpression bit6 = 
        new BitwiseXorExpression(JExprParser.NO_SOURCE_INFO, new NullLiteral(JExprParser.NO_SOURCE_INFO), 
                                 new NullLiteral(JExprParser.NO_SOURCE_INFO));
      BitwiseNotExpression bit7 = 
        new BitwiseNotExpression(JExprParser.NO_SOURCE_INFO, new NullLiteral(JExprParser.NO_SOURCE_INFO));
      
      
      bit4.visit(_llv);
      assertEquals("Should be 7 errors", 7, errors.size());
      assertEquals("error message should be correct", "Bitwise and expressions cannot be used at any language level." + 
                   "  Perhaps you meant to compare two values using regular and (&&)", errors.getLast().getFirst());
      
      bit5.visit(_llv);
      assertEquals("Should be 8 errors", 8, errors.size());
      assertEquals("error message should be correct", "Bitwise or expressions cannot be used at any language level." + 
                   "  Perhaps you meant to compare two values using regular or (||)", errors.getLast().getFirst());
      
      bit6.visit(_llv);
      assertEquals("Should be 9 errors", 9, errors.size());
      assertEquals("error message should be correct", "Bitwise xor expressions cannot be used at any language level", 
                   errors.getLast().getFirst());
      
      bit7.visit(_llv);
      assertEquals("Should be 10 errors", 10, errors.size());
      assertEquals("error message should be correct", "Bitwise not expressions cannot be used at any language level." +
                   "  Perhaps you meant to negate this value using regular not (!)", errors.getLast().getFirst());
      
      //shift binary expressions
      LeftShiftExpression shift4 = 
        new LeftShiftExpression(JExprParser.NO_SOURCE_INFO, new NullLiteral(JExprParser.NO_SOURCE_INFO), 
                                new NullLiteral(JExprParser.NO_SOURCE_INFO));
      RightUnsignedShiftExpression shift5 = 
        new RightUnsignedShiftExpression(JExprParser.NO_SOURCE_INFO, new NullLiteral(JExprParser.NO_SOURCE_INFO), 
                                         new NullLiteral(JExprParser.NO_SOURCE_INFO));
      RightSignedShiftExpression shift6 = 
        new RightSignedShiftExpression(JExprParser.NO_SOURCE_INFO, new NullLiteral(JExprParser.NO_SOURCE_INFO), 
                                       new NullLiteral(JExprParser.NO_SOURCE_INFO));
      
      
      shift4.visit(_llv);
      assertEquals("Should be 11 error", 11, errors.size());
      assertEquals("error message should be correct", "Bit shifting operators cannot be used at any language level", 
                   errors.getLast().getFirst());
      
      shift5.visit(_llv);
      assertEquals("Should be 12 errors", 12, errors.size());
      assertEquals("error message should be correct", "Bit shifting operators cannot be used at any language level", 
                   errors.getLast().getFirst());
      
      shift6.visit(_llv);
      assertEquals("Should be 13 errors", 13, errors.size());
      assertEquals("error message should be correct", "Bit shifting operators cannot be used at any language level", 
                   errors.getLast().getFirst());
      
      //empty expression
      EmptyExpression e = new EmptyExpression(JExprParser.NO_SOURCE_INFO);
      e.visit(_llv);
      assertEquals("Should be 14 errors", 14, errors.size());
      assertEquals("Error message should be correct", "You appear to be missing an expression here", 
                   errors.getLast().getFirst());
      
      //noop expression
      NoOpExpression noop = 
        new NoOpExpression(JExprParser.NO_SOURCE_INFO, new NullLiteral(JExprParser.NO_SOURCE_INFO), 
                           new NullLiteral(JExprParser.NO_SOURCE_INFO));
      noop.visit(_llv);
      assertEquals("Should be 15 errors", 15, errors.size());
      assertEquals("Error message should be correct", "You are missing a binary operator here", 
                   errors.getLast().getFirst());
    }
    
    public void testIsConstructor() {
      MethodData constr = 
        new MethodData("monkey", _publicMav, new TypeParameter[0], _sd1, new VariableData[0], new String[0], _sd1, 
                       new NullLiteral(JExprParser.NO_SOURCE_INFO));
      MethodData notRightOuter = 
        new MethodData("monkey", _publicMav, new TypeParameter[0], _sd1, new VariableData[0], new String[0], _sd2, 
                       new NullLiteral(JExprParser.NO_SOURCE_INFO));
      _sd2.setOuterData(_sd1);
      _sd1.addInnerClass(_sd2);
      MethodData notRightName = 
        new MethodData("chimp", _publicMav, new TypeParameter[0], _sd1, new VariableData[0], new String[0], _sd1, 
                       new NullLiteral(JExprParser.NO_SOURCE_INFO));
      MethodData notRightReturnType = 
        new MethodData("monkey", _publicMav, new TypeParameter[0], _sd2, new VariableData[0], new String[0], _sd1, 
                       new NullLiteral(JExprParser.NO_SOURCE_INFO));
      
      //try one that works
      assertTrue(_llv.isConstructor(constr));
      
      //wrong outer
      assertFalse(_llv.isConstructor(notRightOuter));
      
      //wrong name
      assertFalse(_llv.isConstructor(notRightName));
      
      //wrong return type
      assertFalse(_llv.isConstructor(notRightReturnType));
      
      //not a method data
      assertFalse(_llv.isConstructor(_sd1));
    } 
  }
}
