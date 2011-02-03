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
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.*;
import edu.rice.cs.plt.io.IOUtil;

import junit.framework.TestCase;

/** Top-level Language Level Visitor that implements the constraint checking and symbol table building that is common to
  * first pass processing for the Functional and FullJava levels.  There are two major complications in performing this
  * pass.  First, references to symbols appear in the signatures of type (class/interface) definitions that have not yet
  * been defined.  In the symbol table, the binding of these references must be deferred until a fixup list is executed
  * after the first pass visit has finished.  This visitor and its descendants maintain a FixUp list for this purpose.
  * Second, the loading of signature information into the symbol table (called "resolving" in this documentation) is
  * deferred for some symbols.  A dummy entry called a "continuation" is created in the symbol table for each such 
  * symbol.  
  */
public class LanguageLevelVisitor extends JExpressionIFPrunableDepthFirstVisitor {
  
  public static final ModifiersAndVisibility PUBLIC_MAV = 
    new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public"});
  public static final ModifiersAndVisibility PROTECTED_MAV = 
    new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"protected"});
  public static final ModifiersAndVisibility PRIVATE_MAV = 
    new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"private"});
  public static final ModifiersAndVisibility PACKAGE_MAV = 
    new ModifiersAndVisibility(SourceInfo.NONE, new String[0]);
  public static final ModifiersAndVisibility FINAL_MAV = 
    new ModifiersAndVisibility(SourceInfo.NONE, new String[]{"final"});
    
  /** Errors we have encountered during this pass: string is the text of the error, JExpressionIF is the part of
    * the AST where the error occurs. */
  protected static LinkedList<Pair<String, JExpressionIF>> errors;
  
  /** Stores the classes we have referenced, and all their information, once they are resolved.  Bound to static field
    * LanguageLevelConverter.symboltable. */
  public final Symboltable symbolTable;
  
  /** A table of the names of symbols for which dummy symbol entries (continuations) have been created and resolution 
    * has been deferred.  In some cases (symbols subsequently defined in a file being converted), resolution occurs 
    * during execution.  TODO: make this field dynamic. */
  static Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations;
  
  /** A table of the commands to be executed after this visitation is complete; these commands fill in missing objects
    * in the symbolTable (which were not available at the time the containing object was constructed.
    * TODO: make this field dynamic. */
  static LinkedList<Command> fixUps;

  // TODO: !!! This field appears vestigal; it does not appear to affect execution.  Eliminate it
  /* A list of other files that are being visited.  If the SourceFile is not null, then the source file was
   * visited as opposed to the class file.  This info is used by LanguageLevelConverter in DrJava.
   * We keep the LLV rather than the file, because the LLV has a file, and we need some other information
   * stored in the LLV to properly look up the file.
   */
  static LinkedList<Pair<LanguageLevelVisitor, SourceFile>> visitedFiles;
  
  /**True once we have encountered an error we cannot recover from.  TODO: ??? recover in what sense. */
  static boolean _errorAdded;
  
  /** The source file that is being compiled */
  File _file;
  
  /** The package of the current file */
  String _package;
  
  /** The name of the current enclosing class.  This is null for a top-level visitor but is bound to an object in
    * IntermediateVisitor/FullJavaVisitor.  The package name prefix is included.  Inner class names include class 
    * qualifiers. */
  String _enclosingClassName;
  
  /** A list of file names (classes) imported by the current file. TODO: change the name to _importedClasses or
    * _importedTypes. */
  LinkedList<String> _importedFiles;
  
  /** A list of package names imported by the current file. */
  LinkedList<String> _importedPackages;
  
  // A mapping from in scope generic type parameters to their bounds.  
  public HashMap<String, SymbolData> _genericTypes;
  
  /** The fully qualified class names for top level ClassDefs and InterfaceDefs in the current file that have not
    * yet been defined.  This filed is used to optimize symbol table lookups obviating the need for some fixups. */
  HashSet<String> _classesInThisFile;
  
//  /** The inner classes in this class body; null if this is not within a class body. */
//  HashSet<String> _innerClassesInThisBody;
  
  protected static final Log _log = new Log("LLConverter.txt", false);
  
  /** This constructor is called from the subclasses of LanguageLevelVisitor.
    * @param file  The File corresponding to the source file we are visiting
    * @param packageName  The name of the package corresponding to the file
    * @param importedFiles  The list of files (classes) imported by this source file
    * @param importedPackages  The list of packages imported by this source file
    * @param classesInThisFile  The list of names of classes defined in this file
    * @param continuations  The table of classes we have encountered but still need to resolve
    */
  public LanguageLevelVisitor(File file, 
                              String packageName,
                              String enclosingClassName,
                              LinkedList<String> importedFiles, 
                              LinkedList<String> importedPackages, 
                              HashSet<String> classesInThisFile, 
                              Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations,
                              LinkedList<Command> fixUps,
                              HashMap<String, SymbolData> genericTypes) {
    _file = file;
    _package = packageName;
    _enclosingClassName = enclosingClassName;
    if (_enclosingClassName != null && _enclosingClassName.startsWith("null")) assert false;
    _importedFiles = importedFiles;
    _importedPackages = importedPackages;
    _classesInThisFile = classesInThisFile;
//    _innerClassesInThisBody = new HashSet<String>();
    this.continuations = continuations;
    this.fixUps = fixUps;
    _genericTypes = genericTypes;
    
    symbolTable = LanguageLevelConverter.symbolTable;
    
    assert fixUps != null;
    assert _genericTypes != null;
    
    // Ensure that the imported packages include "java.lang" 
    if (! _importedPackages.contains("java.lang")) _importedPackages.addFirst("java.lang");
    // Ensure that the symbol table contains the essential types;  TODO: this is kludge; fix it !!!
    LanguageLevelConverter.loadSymbolTable();
  }
  
  /** This constructor is used only in testing.
    * @param file  The File corresponding to the source file we are visiting
    * @param packageName  The name of the package corresponding to the file
    * @param importedFiles  The list of files (classes) imported by this source file
    * @param importedPackages  The list of packages imported by this source file
    * @param classesInThisFile  The list of names of classes defined in this file
    * @param continuations  The table of classes we have encountered but still need to resolve
    */
  public LanguageLevelVisitor(File file, 
                              String packageName,
                              String enclosingClassName,
                              LinkedList<String> importedFiles, 
                              LinkedList<String> importedPackages, 
                              HashSet<String> classesInThisFile, 
                              Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations,
                              LinkedList<Command> fixUps) {
    this(file, packageName, enclosingClassName, importedFiles, importedPackages, classesInThisFile, continuations, fixUps, 
         new HashMap<String, SymbolData>());
  }
  
  /* Reset the nonStatic fields of this visitor.  Used during testing. */
  protected void _resetNonStaticFields() {
    _file = new File("");
    _enclosingClassName = null;
    _package = "";
    _importedFiles = new LinkedList<String>();
    _importedPackages = new LinkedList<String>();
  }
  
  /** @return the accessor name corresponding to given field name. */
  public static String getFieldAccessorName(String name) { return name; }
  
  /**@return the source file*/
  public File getFile() { return _file; }
  
  /** @return true if this data is a constructor, i.e., it is a method data, its name and return type are the same, and
    * its return type matches its enclosing sd.
    */
  protected boolean isConstructor(Data d) {
    if ( !(d instanceof MethodData) ) return false;
    MethodData md = (MethodData) d;
    SymbolData rt = md.getReturnType();
    SymbolData sd = md.getSymbolData();  // if this is a constructor, sd must be a defined symbol
    
    return (rt != null && sd != null && rt.getName().indexOf(md.getName()) != -1 && rt == sd);
  }
  
  /** Factory method that constructs an appropriate class body visitor for this visitor class (either 
    * ClassBodyIntermediateVisitor or ClassBodyFullJavaVisitor). This class and method should be abstract, but the LLV 
    * class is used concretely in testing and  elswhere.  The default choice is ClassBodyFullJavaVisitor. */
  public LanguageLevelVisitor newClassBodyVisitor(SymbolData anonSD, String anonName) {
    return new ClassBodyFullJavaVisitor(anonSD, anonName, _file, _package, _importedFiles, _importedPackages, 
                                        _classesInThisFile, continuations, fixUps);
  }
    
  /** Takes a classname and returns only the final segment of it.  This removes all the dots.  Returns "" for anonymous
    * class names.   TODO: Do we need to fix this? */
  public static String getUnqualifiedClassName(String className) { 
    int lastIndexOfDot = className.lastIndexOf('.');
    if (lastIndexOfDot != -1) {
      className = className.substring(lastIndexOfDot + 1);
    }
    int lastIndexOfDollar = className.lastIndexOf('$');
    if (lastIndexOfDollar != -1) {
      className = className.substring(lastIndexOfDollar + 1);
    }
    // Remove any leading numbers  TODO: why do this?  If we encounter a 
    while (className.length() > 0 && Character.isDigit(className.charAt(0))) { 
      className = className.substring(1, className.length());
    }
    return className;
  }
  
  /** Convert the ReferenceType[] to a String[] with the names of the ReferenceTypes. */
  protected static String[] referenceType2String(ReferenceType[] rts) {
    String[] throwStrings = new String[rts.length];
    for (int i = 0; i < throwStrings.length; i++) {
      throwStrings[i] = rts[i].getName();
    }
    return throwStrings;
  }
  

//  /** Build a SourceInfo corresponding to the specified class name, with -1 as the
//    * value for row and column of the start and finish.
//    */
//  protected static SourceInfo _makeSourceInfo(String qualifiedClassName) {
//    return new SourceInfo(new File(qualifiedClassName), -1, -1, -1, -1);
//  }
  
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
  
  /* Creates a new ArrayData of the element type specified by eltSd  with llv and si as the corresponding 
   * LanguageLevelVisitor and SourceInfo and enters it in the SymbolTable. NOTE: this code erroneously
   * marks an array symbol as a non-continuations when the element symbol is still a continuation!
   */
  public ArrayData defineArraySymbolData(SymbolData eltSd, LanguageLevelVisitor llv, SourceInfo si) {
    ArrayData arraySd = new ArrayData(eltSd, llv, si);  // sets _ isContinuation to false! 
//    System.err.println("##### Defining the array symbol " + arraySd.getName());
    symbolTable.put(arraySd.getName(), arraySd);
    return arraySd;
  }
  
  /* Convenience method used in testing. */ 
  private SymbolData getArraySymbolData(String eltClassName, SourceInfo si, boolean addError, boolean checkImports) {
    return _getArraySymbolData(eltClassName, si, addError, checkImports /*, _classesInThisFile*/);
  }
                                           
  /** Gets the SymbolData for an array type given className, the class name for its element type. First, it looks up the 
    * the SymbolData for className.  If it's not null, then it tries to lookup the SymbolData for className + "[]"
    * If it's found, return it.  If not, create a new ArrayData and put it in the symbolTable.  This method is a leaf 
    * in the 'get...Symbol' hierarchy.
    * @param className  The String name of the array type we're trying to resolve.
    * @param si  The SourceInfo corresponding to the class.  Used if an error is encountered..
    */
  private SymbolData _getArraySymbolData(String eltClassName, SourceInfo si, boolean addError, boolean checkImports/*, 
                                         HashSet<String> classesInThisFile*/) {
    // resolve should only be true when post-visitation resolution is performed
//    if (eltClassName.equals("String[]")) System.err.println("String[] passed to getArraySymbolData");
//    if (eltClassName.equals("String")) System.err.println("String passed to getArraySymbolData");
    SymbolData eltSD =  getSymbolData(eltClassName, si, addError, checkImports/*, classesInThisFile*/);
    if (eltSD != null) {
//      if (eltSD.getName().equals("java.lang.String")) System.err.println("java.lang.String FOUND");
      SymbolData sd = symbolTable.get(eltSD.getName() + "[]");  // Look up fully qualified name
      if (sd != null) return sd;
      else return defineArraySymbolData(eltSD, this, si /*, classesInThisFile*/);
    }
    else return null;
  }
  
  /** Get the SymbolData for the array type given the fully qualified className for the element type.  Lookup the 
    * element type; if it's not null, then try to lookup the symbol for the qualifiedClassName + "[]".  If it's already
    * there, return it.  If not, create a new ArrayData and put it in the Symbol Table.  This method is a leaf in the 
    * 'get...SymbolDataHelper' hierarchy.
    * @param className  The String name of the array type we're trying to resolve.
    * @param si  The SourceInfo corresponding to the class.  Used if an error is encountered.
    * @param resolve  true if this SymbolData needs to be completely resolved.
    * @param fromClassFile  true if this type is from a class file.
    */
  private SymbolData _getQualifiedArraySymbolData(String eltClassName, SourceInfo si, boolean resolve, 
                                               boolean fromClassFile /*, HashSet<String> classesInThisFile*/) {
    // resolve should only be true when post-visitation resolution is performed
    SymbolData eltSD = getQualifiedSymbolData(eltClassName, si, resolve, fromClassFile, true /*, classesInThisFile*/);
    if (eltSD != null) {
      SymbolData sd = symbolTable.get(eltSD.getName() + "[]");  // Look up fully qualified name
      if (sd != null) return sd;
      else return defineArraySymbolData(eltSD, this, si /*, classesInThisFile*/);
    }
    else return null;
  }
  
  /** Checks the file system for the class name, returning the corresponding SymbolData if there is an up-to-date match.
    * If resolve is false but a corresponding source file is matched, a continuation is returned.  If no source file is
    * found but a class file is found, the symbol is resolved against the class file even if resolve is false.
    * If resolve is true and a corresponding source file is found, the symbol is resolved against the corresponding 
    * class file provided it is up-to-date.  If it is not up-to-date, the method throws an error message.  If no source 
    * file or class file corresponding to the class is found, SymbolData.NOT_FOUND is returned.
    * @param qualifiedClassName  The name of the class we're looking up.
    * @param si                  Information about where the class was called from.
    * @param resolve             true if we want to fully resolve the SymbolData.
    * @param addError            true if we want to throw errors.
`    */
  protected SymbolData _getSymbolDataFromFileSystem(final String qualifiedClassName, SourceInfo si, boolean resolve,
                                                    boolean addError) {
    // If qualifiedClassName is already defined (and not a continuation to resolve), return
    SymbolData sd = symbolTable.get(qualifiedClassName);
    if (sd != null && (! sd.isContinuation() || ! resolve)) return sd;  
    // Note: sd != null && ! sd.isContinuation => sd has already been resolved
    
//    // Is qualifiedClassName in _classesInThisFile, look it up directly in the parsed ASTs
//    boolean present = _classesInThisFile.contains(qualifiedClassName);
//    if (present) return _identifyTypeFromClassNamesInThisfile(qualifiedClassName, si, resolve);
    
    /* If qualifiedClassName is not in the symbol table or list of classes to be parsed, check if the class is defined
     * in this package tree.
     */
    String qualifiedClassNameWithSlashes = 
      qualifiedClassName.replace('.', System.getProperty("file.separator").charAt(0));
    File _fileParent = _file.getParentFile();
    // Create var that is eventually set to root of package (for _file)
    String programRoot = (_fileParent == null) ? "" : _fileParent.getAbsolutePath();  
    assert (programRoot != null); // parent != null => parent exists.
    
    final String path;  // The expected path name of the class file (less .class) for qualifiedClassName

    if (programRoot.length() > 0) {      
      String packageWithSlashes = _package.replace('.', System.getProperty("file.separator").charAt(0));
      // Get index of slash preceding first char of package name
      int indexOfPackage = programRoot.lastIndexOf(packageWithSlashes); 
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
          return new File(path + ".dj").getCanonicalFile().equals(f) ||
            new File(path + ".dj0").getCanonicalFile().equals(f) ||
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

//    // Claim: sourceFile is not the current file or sd is an inner class.  Otherwise, className would have been in 
//    // _classesInThisFile.
    
    // If sourceFile exists, we have identified the class corresponding to qualifiedClassName.  If resolve is false, 
    // simply create and return the appropriate continuation, deferring the loading of class information until reolution
    // time.  If there is no corresponding class file or the class file is not 
    // up-to-date, signal an error.  Otherwise load the symbol table information from the class file

    if (sourceFile != null) {
      // First see if we even need to resolve this class. If not, create a continuation and return it.
      if (! resolve) { 
        assert sd == null;
        sd = makeContinuation(si, qualifiedClassName); // create a continuation for qualifiedClassName; defer resolution
        return sd;
//        else {
//          sd = new SymbolData(qualifiedClassName);
//          continuations.put(qualifiedClassName, 
//                            new Triple<SourceInfo, LanguageLevelVisitor, SymbolData>(si, this, sd));
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
    
    // if source file exists, confirm that the corresponding class file is up to date
    // if source file does not exist, confirm that a class file does exist
    if (classFile.exists()) {
      // read this classfile, create the SymbolData and return it
     _log.log("Reading classFile " + qualifiedClassName);
      sd = LanguageLevelConverter._classFile2SymbolData(qualifiedClassName, programRoot);
      if (sd == null) {
        if (addError) {
          _addAndIgnoreError("File " + classFile + " is not a valid class file.",  new NullLiteral(si));
        }
        return null;
      }
      _log.log("Returning symbol constructed by loading class file");
      return sd;
    }
    return SymbolData.NOT_FOUND;
  }
 
  /** Resolves the continuation cont. */
  public SymbolData resolveSymbol(SourceInfo si, SymbolData cont) { 
//    System.err.println("***ALARM*** resolveSymbol called for '" + cont + "'");
    return getQualifiedSymbolData(cont.getName(), si, true); 
  }
  
  /** Call getSymbolData with some default values. By default addError is true, since we want to 
    * display errors. By default checkImports is true, since we want to consider imported packages and classes.
    * @param className  The referenced name of the class to resolve.
    * @param si         The SourceInfo corresponding to the reference to the type
    */  
  public SymbolData getSymbolData(String className, SourceInfo si) {
    return getSymbolData(className, si, true, true);
  }
  
  /** Call getSymbolData with some default values.  By default checkImports will be true, since we want to
    * consider imported packages and classes initially.
    * @param className      The referenced name of the class to identify
    * @param si             The SourceInfo corresponding to the reference to the type
    * @param addError       true if we want to give an error if this class cannot be resolved.
    */  
  protected SymbolData getSymbolData(String className, SourceInfo si, boolean addError) {
    return this.getSymbolData(className, si, addError, true);
  }
  
  /** Simple signature for getSymbol that uses the current context to fill in context information, i.e., it passes _file
    * for file, _package for pkg, _importedFiles for importedFiles, _importedPackages for importedPackages, and 
    * _enclosingClass for enclosingClassName.  This version should be used in all contexts EXCEPT fixups which are 
    * executed outside of any context and must provide saved context information.
    */
  protected SymbolData getSymbolData(String className, SourceInfo si, boolean addError, boolean checkImports) {
    return getSymbolData(_file, _package, _importedFiles, _importedPackages, _enclosingClassName, 
                         className, si, addError, checkImports);
  }

  /** This method processes classNames which may or may not include qualifying prefixes.  Array types are recognized and
    * treated recursively. The raw className is initially compared with:
    *   * top-level classes defined in the this file;
    *   * fully qualified classes in the file system;
    *   * inner classes defined in the enclosing class;
    *   * classes in the same package defined in other files;
    *   * imported classes; and
    *   * classes in imported packages.
    * Then className is decomposed in a prefix and an extension where prefixes are matched against symbols as described
    * above.  If a matching prefix is found, the remainder is matched against inner classes of the matched symbol.
    * The protocol does not exactly match the one in the JLS. 
    * This results of method are relative to _file, _package, _importedFiles, _importedPackages, and _enclosingClassName.
    * The external variables _classesInThisFile and _innerClassesInThisBody are used to reduce the number of fixups but 
    * should not affect the ultimate results (after fixups) of any searches.
    * This method calls getQualifiedSymbolData to look up fully qualified class names in the symbol table; this process
    * does not depend on anything but the contents of the symbol table.
    * @param file                   The file containing the className reference.
    * @param package                The package corresponding to file.
    * @param importedFiles          The imported files for this file
    * @param importedPackages       The imported Packages for this file
    * @param enclosingClassName     The enclosing className
    * @param className              The referenced name of the class to lookup.
    * @param si                     The SourceInfo of the reference to className used in case of an error.
    * @param addError               Whether to add errors or not
    * @param checkImports           Whether to try prepending the imported package names
    */
  protected SymbolData getSymbolData(File file,
                                     String pkg,
                                     LinkedList<String> importedFiles,
                                     LinkedList<String> importedPackages,
                                     String enclosingClassName,
                                     String className, 
                                     SourceInfo si, 
                                     boolean addError, 
                                     boolean checkImports) {
    
    if (className == null) {
//      System.err.println("***ERROR*** getSymbolData called with null className");
      assert false;
    }
    
    /** Check to see if type with className (as is) can be found (including a check against generic type variables). */
    SymbolData existingSD = getQualifiedSymbolData(className, si, false, false, addError);
    if (existingSD != null) return existingSD;
        
    if (className.endsWith("[]")) { // className refers to an array type
      String eltClassName = className.substring(0, className.length() - 2);  // may not be fully qualified
//      if (eltClassName.equals("String")) System.err.println("getSymbolData called for String[]");
      return getArraySymbolData(/* file, pkg, importedFiles, importedPackages, enclosingClassName, */
                                eltClassName, si, addError, checkImports);
    }
    
    // Try matching the className against current package
    String qualClassName = getQualifiedClassName(pkg, className);  // TODO: make this work for an inner class
    existingSD = getQualifiedSymbolData(qualClassName, si);
    if (existingSD != null) return existingSD; 
    
    // Check for relative inner class reference
    if (enclosingClassName != null) {
      // Assume that className is an inner class relative to _enclosingClassName (which always holds for local 
      // classes and often holds for immediate inner class references.  Fortunately, local class references cannot be
      // forward references.
      SymbolData enclosingSD = getQualifiedSymbolData(enclosingClassName, si);
      if (enclosingSD != null) {
        SymbolData sd = enclosingSD.getInnerClassOrInterface(className);
        if (sd != null) return sd;
//        // NOTE: the following should be unnecessary since the forward referenced inner symbol should be sd above 
//        // Check for forward reference to an inner class of the enclosing class
//        String qualifiedName = enclosingSD + '.' + className;
//        if (_innerClassesInThisBody.contains(qualifiedName))  // forward reference to inner class/interface
//          return getQualifiedSymbolData(qualifiedName, si);   // return continuation
      }
    }
    
    // TODO: imported inner class can have qualification so the following logic is broken.  The following logic
    // ignores the possibility of importing an inner class.  Fix this !!!
    if (className.indexOf('.') == -1) { // className has no qualification; may be imported
    
      // Check if the className's package was imported.
      if (checkImports) {
//        if (className.equals("Object")) System.err.println("***SHOUT*** checking imports for 'Object'");
        
        // Check if className was specifically imported.
        // We will not check that the package is correct here, because it is caught in the type checker.
        Iterator<String> iter = importedFiles.iterator();
        while (iter.hasNext()) {
          String s = iter.next();
          if (s.endsWith(className)) {
            SymbolData importSD = symbolTable.get(s); // All imported files should be in the symbol table.
//            if (importSD == null) System.err.println("***ALARM*** Imported symbol lookup failed for " + s);
            // if importSD is a continuation it will be subsequently be resolved
            return importSD;
          }
        }
      }
     
      // Look for a match against imported packages
      // TODO:  Within a relative class name the separators must be converted from '.' to '$'
      SymbolData resultSD = null;
      assert importedPackages.contains("java.lang");
//      assert symbolTable.containsKey("java.lang.Object");
      for (String prefix: importedPackages) {
        String s = prefix + '.' + className;
//        if (className.equals("java.lang.Object")) System.err.println("***ALARM*** Looking up: " + s);
        SymbolData sD = getQualifiedSymbolData(s, si, false, false, false);
//        if (qualClassName.equals("java.lang.Object")) 
//          System.err.println("matching sd is: " + sD + "\nsymbolTable.get(\"" + s + "\") = "+ symbolTable.get(s));
        if (sD != null) {
          if (resultSD == null || resultSD.equals(sD)) resultSD = sD;
          else {  // sD is NOT the first match; flag an error
            if (addError) {  // TODO: why do we suppress this error in some cases?
              _addAndIgnoreError("The class name " + qualClassName + " is ambiguous.  It could be " + resultSD.getName()
                                   + " or " + sD.getName(), new NullLiteral(si));
              return null;
            }
          }
        }
      }
      if (resultSD != null) return resultSD;
      else return null;  // subsequent searching assumes that className is qualified.
    }
    
    // Decompose class name as fully qualified name followed by an inner class reference
    // TODO: the separator within inner class names is '$'
    int indexOfNextDot = 0;
//    int indexOfNextDollar = className.indexOf("$");   // '$' only appears as separator for inner class names  
    SymbolData sd;
    int length = className.length();
    while (indexOfNextDot != length) {
      indexOfNextDot = className.indexOf('.', indexOfNextDot + 1);
      if (indexOfNextDot == -1) { indexOfNextDot = length; }
      String prefix = className.substring(0, indexOfNextDot);
      
      /* We want to try finding each prefix in the symbol table; the decomposition is putative. */
      sd = getQualifiedSymbolData(prefix, si, false, false, false);
      if (sd != null && sd != SymbolData.AMBIGUOUS_REFERENCE) { // prefix matches an existing symbol
        String outerClassName = prefix;
        String innerClassName = "";
        if (indexOfNextDot != length) {
          SymbolData outerClassSD = sd;
          innerClassName = className.substring(indexOfNextDot + 1);  // putative relative name of inner class
//          System.err.println("Outer class prefix found: " + prefix + " inner class extension: " + innerClassName);
          // NOTE: should be able to search symbolTable using getSymbolData
          sd = outerClassSD.getInnerClassOrInterface(innerClassName);
          if (sd != null) return sd;
//          System.err.println("Corresponding symbol = " + sd);
//          if (sd == null) { // create continuation for inner class; we are forbidding some ambiguities Java may permit
//            sd = addInnerSymbolData(si, outerClassName + '.' + innerClassName, outerClassSD);
//          }
          /* otherwise try another decomposition. */
        }
      }
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
    boolean checkImports = false;
    
    if (lhs == null) return null;
    
    else if (lhs instanceof PackageData) {
      String qualClassName = lhs.getName() + '.' + name;
      return getQualifiedSymbolData(qualClassName, si, false, false, addError);
    }
    
    else { // if (lhs instanceof SymbolData) {
      SymbolData result = lhs.getInnerClassOrInterface(name);
      if (result == SymbolData.AMBIGUOUS_REFERENCE) {
        if (addError) { _addAndIgnoreError("Ambiguous reference to class or interface " + name, new NullLiteral(si)); }
        return null;
      }
      return result;
    }
  }
  
  /** Tries to find (or in some cases creates) the SymbolData for the fiven fully qualified class name.  It
    * searches imported files, primitive types, as well as types in the symbol table. */
  protected SymbolData getQualifiedSymbolData(String qualClassName) {
    return getQualifiedSymbolData(qualClassName, SourceInfo.NONE);
  }
  
  /** Tries to find (or in some cases creates) the SymbolData for the fiven fully qualified class name.  It
    * searches imported files, primitive types, as well as types in the symbol table. */
  protected SymbolData getQualifiedSymbolData(String qualClassName, SourceInfo si) {
    return getQualifiedSymbolData(qualClassName, si, false, false, true);
  }
  
  /** This method tries to find (or in some cases creates) the SymbolData for the fiven fully qualified class name.  It
    * searches imported files, primitive types, as well as types in the symbol table. If resolve is true, it loads the
    * symbolTable with all of the requisite information about qualClassName. */
  protected SymbolData getQualifiedSymbolData(String qualClassName, SourceInfo si, boolean resolve) {
    return getQualifiedSymbolData(qualClassName, si, resolve, false, true);
  }
  
  
  /** This method tries to find (or in some cases creates) the SymbolData for the fiven fully qualified class name or
    * class name.  It uses _classesInThisFile to avoid returning null in some cases (eliminating the need for some
    * fixups).  Except for the _classesInThisFile optimization (which works uniformly if it is set to null during
    * continuation resolution and fixups), this lookup only depends on the contents of the symbol table and the file
    * system.
    * @param qualClassName  The fully qualified name of the class to lookup.
    * @param si             The SourceInfo of the reference to qualClassName used in case of an error.
    * @param resolve        Whether to return a continuation or fully parse the class.
    * @param fromClassFile  Whether this was called from the class file reader.
    * @param addError       Whether to add errors.  We don't add errors when iterating through a qualified class name's
    *                       package.  (??)
    */
  protected SymbolData getQualifiedSymbolData(String qualClassName, SourceInfo si, boolean resolve, boolean fromClassFile, 
                                           boolean addError) {
    assert qualClassName != null;
//    if (qualClassName.startsWith("RefInnerClassCrazy")) 
//      System.err.println("ALARM: getQualifiedSymbolData called for '" + qualClassName + "'");
//    
    if (qualClassName.equals("java.lang.Throwable")) {
//      System.err.println("***ALARM: getQualifiedSymbolData called for '" + qualClassName + "'");
//      if (symbolTable.get(qualClassName) != null) System.err.println("***ALARM: java.lang.Throwable already exists");
    }

    assert (qualClassName != null && ! qualClassName.equals(""));
    
    // Check for primitive types.
//    System.err.println("***** Checking for primitive symbol " + qualClassName);
    SymbolData sd = LanguageLevelConverter._getPrimitiveSymbolData(qualClassName);
    if (sd != null) { 
//      System.err.println("***** Matched for primitive symbol " + sd);
      return sd; 
    }
    
    // Check for references to generic types  (only happens in FullJava code)
    // TODO !!! Does not handle forward references
    String name = getUnqualifiedClassName(qualClassName);
    if (_genericTypes.containsKey(name)) {
//      Utilities.show("Return type " + name + " is generic and value is " + _genericTypes.get(name));
      return _genericTypes.get(name);
    }
    
    // Check for already defined types
    SymbolData existingSD = symbolTable.get(qualClassName);
    if (existingSD != null && (! resolve || ! existingSD.isContinuation())) return existingSD;
    
    // Check for array types.
    if (qualClassName.endsWith("[]"))
      return _getQualifiedArraySymbolData(qualClassName.substring(0, qualClassName.length() - 2), si, resolve, 
                                       fromClassFile);
    // Check for generic type variables
    SymbolData genericBinding = _genericTypes.get(qualClassName);
    if (genericBinding != null) return genericBinding;  // may return SymbolData.NOT_FOUND because bound is undefined
    
    // If qualClassName is a library file, resolve it immediately by reading its class file.
    if (isJavaLibraryClass(qualClassName)) {
      _log.log("Calling  _classFile2SymbolData");
      SymbolData cfSD = LanguageLevelConverter._classFile2SymbolData(qualClassName, null);
      if (! qualClassName.startsWith("java.") && ! qualClassName.startsWith("sun."))
//        System.err.println("Defining class file symbol " + qualClassName);
      assert cfSD == null || symbolTable.contains(cfSD);
      return cfSD;
    }
    
    if (_classesInThisFile.contains(qualClassName))  // Make continuation for top level class not yet parsed in this file
      return makeContinuation(si, qualClassName);
    
    // If performing post-visit resolution, read the signature info for this symbol from a class file
    if (resolve) {  // Look for up-to-date class file
      SymbolData newSd = _getSymbolDataFromFileSystem(qualClassName, si, true, true);  // resolve, addError = true
      if (newSd != null && newSd != SymbolData.NOT_FOUND) {
        _log.log("Returning " + sd + " from file system");
        return newSd;
      }
      else {
//        System.err.println("********* ALARM: The class " + qualClassName + " was not found.  Symbol = " + newSd);
        _addAndIgnoreError("The class " + qualClassName + " was not found.", new NullLiteral(si));
        assert false;
      }
    }
    return null;    // qualClassName not found
  }
  
 
  /** The Qualified Class Name is the package, followed by a dot, followed by the rest of the class name.
    * If the provided className is already qualified, just return it.  If the package is not empty,
    * and the className does not start with the package, append the package name onto the className, and return it.
    * @param className  The className to qualify.
    */
  protected String getQualifiedClassName(String className) { return getQualifiedClassName(_package, className); }
  
  /** If the specified package pkg is empty or pkg is a prefix of className, return className.  Otherwise return
    * className qualified with the pkg prefix.
    * @param pkg        The package name to use as a prefix.
    * @param className  The className to qualify.
    */
  public static String getQualifiedClassName(String pkg, String className) {
//    if (className.equals("java")) throw new RuntimeException("BOGUS getQualifiedClassName call on 'java'");
    if (! pkg.equals("") && ! className.startsWith(pkg)) return pkg + '.' + className;
    else return className;
  }
  
  // Creates a continuation for an inner class/interface; qualifiedTypeName is known to exist
  protected SymbolData addInnerSymbolData(SourceInfo si, String qualifiedTypeName, Data enclosing) {
    SymbolData sd = makeContinuation(si, qualifiedTypeName); // create continuation
    SymbolData enclosingSD = enclosing.getSymbolData();  // must exist in symbol table
    // if qualifiedTypeName refers to an external inner class, the following will likely fail.  TODO: eliminate this
    enclosing.getSymbolData().addInnerClass(sd);
    sd.setOuterData(enclosingSD);
    return sd;
  }
  
  /** This method creates the specified continuation in the symbol table.  Assumes qualClassName is fully qualified.
    * @param si  The SourceInfo corresponding to this occurrence of the class symbol
    * @param referencedClassName  The referenced name for the class.  In some cases, it is fully qualified.
    */
  protected SymbolData makeContinuation(SourceInfo si, String qualClassName) {
//    System.err.println("***** makeContinuation called for " + qualClassName);
    if (qualClassName.equals("D.E")) assert false;
    SymbolData sd = new SymbolData(qualClassName);  // create a continuation
    symbolTable.put(qualClassName, sd);
    continuations.put(qualClassName, new Triple<SourceInfo, LanguageLevelVisitor, SymbolData>(si, this, sd));
//    System.err.println("Created continuation for " + qualClassName + " at LLV:1124");
    return sd;
  }
  
  /** Looks up the type with name rt (which is arbitrary source text for a type) from within the class name 
    * qualifiedClassName. At top level, qualifiedClassName == null. The parameter qualifiedClassName is required 
    * becaue this method may be called in a fixup.  TODO: consolidate with _identifyType. */
  protected SymbolData _lookupTypeFromWithinClass(ReferenceType rt, String qualifiedClassName) {
    // Perform a raw lookup assuming name of rt is fully qualified
    String rtName = rt.getName();
    SourceInfo si = rt.getSourceInfo();
    // Perform a lookup at top level.
    assert _importedPackages.contains("java.lang");
    SymbolData sD = getSymbolData(rtName, si, false);
//    if (rtName.equals("Object") && sD == null)
//      System.err.println("Looking up 'Object' in '" + qualifiedClassName + "' yields null");
    if (sD == null && qualifiedClassName != null) { // check if rt refers to an inner type of qualifiedClassName
      SymbolData sd = getQualifiedSymbolData(qualifiedClassName, SourceInfo.NONE);
      sD = sd.getInnerClassOrInterface(rtName);
      assert sD == getQualifiedSymbolData(qualifiedClassName + '.' + rtName, SourceInfo.NONE);
    }  // The following case should be unncessary because getInnerClassOrInterface should look back through enclosing classes
    else if (qualifiedClassName != null) {
      int prefixLen = qualifiedClassName.lastIndexOf('.');
      if (sD == null && prefixLen >= 0) { // check if rt refers to an inner class of the class enclosing sd 
        // Check to see if this is an inner class referencing an inner interface
        String qualifyingBase = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
        SymbolData outerSD = getQualifiedSymbolData(qualifyingBase, SourceInfo.NONE);
        if (outerSD != null) {
          sD = outerSD.getInnerClassOrInterface(rtName);
          assert sD == getQualifiedSymbolData(qualifyingBase + '.' + rtName, si);
          // TODO: expand this search to include interfaces defined in any of the enclosing classes.  
        }
      }
    }
    return sD;
  }
  
  /** Overloaded signature for defineSymbolData.  Passes _enclosingClassName for enclosingClassName and
    * _classesInThisFile for classesInThisFile */
   protected SymbolData defineSymbolData(TypeDefBase typeDefBase, final String qualifiedTypeName) {
     return defineSymbolData(typeDefBase, qualifiedTypeName, _enclosingClassName /*, _classesInThisFile*/ );
   }
   
  /** Given a TypeDefBase (which is either a ClassDef or an InterfaceDef) and the corresponding qualifiedTypeName, this
    * method generates a SymbolData, and adds the name and SymbolData pair to the symbol table.  It checks that this 
    * class is not already in the symbol table. This error should never happen for an inner class or interface.
    * Assumes that the defined class is top level.  If used for inner class definition, the caller must be perform
    * any special inner class initialization.
    * @param typeDefBase  The AST node for the class def, interface def, inner class def, or inner interface def.
    * @param qualifiedTypeName  The fully qualified name of the class or interface
    * @return the defined SymbolData or null if the type has already been defined
    */
  protected SymbolData defineSymbolData(final TypeDefBase typeDefBase, final String qualifiedTypeName,
                                        final String enclosingClassName /*, final HashSet<String> classesInThisFile*/) {
    assert (typeDefBase instanceof InterfaceDef) || (typeDefBase instanceof ClassDef);
    assert ! qualifiedTypeName.startsWith("null.");
    String name = qualifiedTypeName;  // may be an interface
    SymbolData contSd = symbolTable.get(qualifiedTypeName);
//    System.err.println("In defineSymbolData call for " + qualifiedTypeName + ", contSd = " + contSd);
    if (contSd != null && ! contSd.isContinuation()) {
      _addAndIgnoreError("The class or interface " + name + " has already been defined.", typeDefBase);
      return null;
    }
    // If no continuation exists, create a SymbolData for this definition
    final SymbolData sd = (contSd == null) ? new SymbolData(qualifiedTypeName) : contSd;
    symbolTable.put(qualifiedTypeName, sd);
    
//    // Save _enclosingClassName in a final var; may be null if called at the top level, e.g. defining a
//    // top level class or interface.
//    final String enclosingClassName = _enclosingClassName; 
    
    // Make this SymbolData as a non-continuation
    sd.setIsContinuation(false);
    //Set the package to be the current package
    sd.setPackage(_package);
    // Set the MAV and type parameters (the latter are not used currently)
    sd.setMav(typeDefBase.getMav());
    sd.setTypeParameters(typeDefBase.getTypeParameters());
    
    // Create the LinkedList for the SymbolDatas of the interfaces
    final ArrayList<SymbolData> interfaces = new ArrayList<SymbolData>();
    
    // Get or create SymbolDatas for the interfaces
    ReferenceType[] rts = typeDefBase.getInterfaces();
    for (int i = 0; i < rts.length; i++) {
      final ReferenceType rt = rts[i];
      final String rtName = rt.getName();
      boolean forwardRef = false;
      SymbolData iD = _lookupTypeFromWithinClass(rt, enclosingClassName);
      if (iD != null && ! iD.isContinuation() && ! iD.isInterface()) {
        _addAndIgnoreError("The symbol " + rtName + " is not an interface", typeDefBase);
      }
      if (iD == null || iD.isContinuation())  { // create a dummy symbol pending fixUp TODO: is this necessary?
        iD = new SymbolData(rtName);
        forwardRef = true;
      }
      
      interfaces.add(iD);                     
      if (forwardRef) { 
        // create a fixup for this interface slot
        final int j = i;
        Command fixUp = new Command() {
          public void execute() {
            SymbolData newID = _lookupTypeFromWithinClass(rt, enclosingClassName);
            if (newID == null) _addAndIgnoreError("The symbol " + rtName + " is not defined", typeDefBase);
            else if (! newID.isInterface()) 
              _addAndIgnoreError("The symbol " + rtName + " is not an interface", typeDefBase);
            interfaces.set(j, newID);
            sd.addEnclosingData(newID);
          }
        };
        fixUps.add(fixUp);
      }
    }
      
    // Set the inferfaces; fixups will be done on the elements of the interface ArrayList, but this does not
    // add the found interface to the enclosing data of sd.
    sd.setInterfaces(interfaces);
     
    // Create SymbolData variable for superclass
    SymbolData superSD = null;
    
    // Get or create the SymbolData for the superclass/interface; setInterface and setSuperClass
    
    if (typeDefBase instanceof InterfaceDef) {
      // set Object as the super class of this, so that it will know it implements Object's methods.
      SymbolData objectSD = getSymbolData("java.lang.Object", typeDefBase.getSourceInfo(), false);
      sd.setInterface(true);
      sd.setSuperClass(objectSD);
    }
    
    else if (typeDefBase instanceof ClassDef) {
      sd.setInterface(false);
      ClassDef cd = (ClassDef) typeDefBase;
      final ReferenceType rt = cd.getSuperclass();
      superSD = _lookupTypeFromWithinClass(rt, enclosingClassName);    
      
      if (superSD != null) sd.setSuperClass(superSD);
      else {
        Command fixUp = new Command() {
          public void execute() { 
            SymbolData newSuperSD = _lookupTypeFromWithinClass(rt, enclosingClassName);
            if (newSuperSD == null)
              _addAndIgnoreError("The class " + sd + " has an undefined superclass " + rt, typeDefBase);
            else  // TODO: Does not check that newSuperSD is not an interace  
              sd.setSuperClass(newSuperSD); 
          }
        };
        fixUps.add(fixUp);
      }
    }


    // Remove symbol name from continuation table.
    _log.log("REMOVING continuation " + qualifiedTypeName);
    continuations.remove(qualifiedTypeName);
    
    // Add sd to the list of classes defined in program text; used to generate constructors. TODO: What about Full Java?
    if (! sd.isInterface()) { LanguageLevelConverter._newSDs.put(sd, this); }
    
    _classesInThisFile.remove(qualifiedTypeName);  // a no-op if qualifiedClassName is an inner class
    return sd;
  }
  
  /** Takes in a TypeDefBase (which is either an InnerClassDef or an InnerInterfaceDef) and creates a SymbolData for it,
    * either by converting a continuation to it or by creating a new symbol (if no continuationis present).
    * @param typeDefBase  The AST node for the class def, interface def, inner class def, or inner interface def.
    * @param relName      The relative (unqualified) name of the symbol
    * @param qualifiedTypeName  The fully qualified name for the class; null if this definition is a duplicate
    * @param enclosing    The enclosing SymbolData or MethodData (for a local class defined within a method).
    */
  protected SymbolData defineInnerSymbolData(TypeDefBase typeDefBase, String relName, String qualifiedTypeName, 
                                             Data enclosing) {
    assert (enclosing instanceof SymbolData) || (enclosing instanceof MethodData);
    /* IMPORTANT: this is defineSymbolData for inner classes! */

    SymbolData sd = defineSymbolData(typeDefBase, qualifiedTypeName /*, _classesInThisFile*/); 
//    if (sd == null) System.err.println("defineSymbolData failed for " + qualifiedTypeName);
    assert sd != null;
    // Set fields of sd that are required for innerSymbols

    sd.setOuterData(enclosing);
    
    if (sd.isInterface()) {
//      assert enclosing instanceof SymbolData;
//      assert enclosing.getName().equals(_enclosingClassName):
      ((SymbolData) enclosing).addInnerInterface(sd); 
    }
    else if (! enclosing.getName().equals(_enclosingClassName)) {  
      // sd is a local class embedded in a method.  We need to add sd to the innerclasses of _enclosingClassName
//      if (! (enclosing instanceof MethodData))
//        System.err.println("***** In defineInnerSymbolData, enclosing = " + enclosing 
//                             + " but _enclosingClassName = " + _enclosingClassName);
      assert enclosing instanceof MethodData;
      SymbolData enclosingClassSD = getQualifiedSymbolData(_enclosingClassName);
      assert enclosingClassSD != null;
      enclosingClassSD.addInnerClass(sd);
      enclosing.addInnerClass(sd);  // adds innerClass to list for the enclosing MethodData 
    }
    else {
      // sd is a non-local inner class
      assert enclosing.getName().equals(_enclosingClassName);
      enclosing.addInnerClass(sd);
//    _innerClassesInThisBody.remove(sd);  // a no-op if _innerClassesInThisBody is empty
    }
    return sd;
  }
  
  /** This method takes in an AnonymousClassInstantion, generates a SymbolData for it, and 
    * adds the name and SymbolData pair to the symbol table.
    * @param AnonymousClassInstantiation  The AST node for the anonymous class instantiation.
    * @param qualifiedTypeName  The fully qualified name of the class
    */
  protected SymbolData defineAnonymousSymbolData(final AnonymousClassInstantiation anonInst, 
                                                 final String qualifiedAnonName,
                                                 final String superName) {
    // Generated name cannot be in symbolTable
//    System.err.println("defineAnonymousSymbolData called for " + qualifiedAnonName + " extending " + superName);

    final SourceInfo si = anonInst.getSourceInfo();
    // Create a SymbolData for this definition
    final SymbolData sd = new SymbolData(qualifiedAnonName);
    symbolTable.put(qualifiedAnonName, sd);
    
    // Make this SymbolData as a non-continuation
    sd.setIsContinuation(false);
    //Set the package to be the current package
    sd.setPackage(_package);
    
//    sd.setMav(anonInst.getMav());  // What is Mav for anonymous class?
//    sd.setTypeParameters(anonInst.getTypeParameters()); 
    
    if (_enclosingClassName != null) {
      SymbolData enclosingSD = getQualifiedSymbolData(_enclosingClassName, SourceInfo.NONE);
      assert (enclosingSD != null); 
      enclosingSD.addInnerClass(sd);
      
//      if (enclosingSD.getName().equals("HasAnonymousInnerClass")) 
//        System.err.println("****** The SymbolData for " + sd + " added to the inner classes of " + enclosingSD);
      
      // Set fields of sd that are required for innerSymbols
      sd.setOuterData(enclosingSD);

    }
        
    SymbolData superSD = getSymbolData(superName, anonInst.getSourceInfo());
        
    if (superSD != null) {
      if (superSD.isInterface()) {
        sd.setSuperClass(getQualifiedSymbolData("java.lang.Object", si));
        sd.setInterfaces(new ArrayList<SymbolData>(Arrays.asList(new SymbolData[] { superSD })));
      }
      else sd.setSuperClass(superSD);  //  By default sd.getInterfaces() == new ArrayList<SymbolData>()
    }
    else {
      // Create a fixup
      Command fixUp = new Command() {
        public void execute() { 
          SymbolData superSD = getSymbolData(superName, si);
          if (superSD == null) 
            _addAndIgnoreError("The class/interface " + superName + " was not found.",  anonInst);
          else if (superSD.isInterface()) {
            sd.setSuperClass(getQualifiedSymbolData("java.lang.Object", si));
            sd.setInterfaces(new ArrayList<SymbolData>(Arrays.asList(new SymbolData[] { superSD })));
          }                        
          else sd.setSuperClass(superSD);  //  By default sd.getInterfaces() == new ArrayList<SymbolData>()
        }
      };
      fixUps.add(fixUp);
    }
    return sd;
  }
  
  /** This method is factored out of formalParameters2VariableData so it can be overridden in FullJavaVisitor.
    * @return the formal parameter mav appropriate for the language level; "Functional level" is default. */
  protected String[] getFormalParameterMav(Data d) { return new String[] {"final"}; }
  
  /** Convert the specified array of FormalParameters into an array of VariableDatas which is then returned.
    * All formal parameters are automatically made final.
    * @param fps        The AST node for the parameter list
    * @param enclosing  The SymbolData for the enclosing class (not method!)
    * NOTE: enclosing refers to the enclosing class rather than enclosing method because any new types
    * defined in the method are not visible in the parameter list.
    * TODO: At the advanced level, this may need to be overwritten?
    */
  protected VariableData[] formalParameters2VariableData(FormalParameter[] fps, SymbolData enclosing) {
    assert enclosing != null /* && (enclosing instanceof SymbolData || enclosing instanceof BlockData)*/; 
    // BodyData ::= MethodData | BlockData
    
//    Utilities.show("formalParameters2VariableData called on " + fps);
    // Should consolidate with same method in FullJavadVisitor; almost identical
    final VariableData[] varData = new VariableData[fps.length];
    final String enclosingClassName = enclosing.getName();
    
    String[] mav = getFormalParameterMav(enclosing);
        
    for (int i = 0; i < fps.length; i++) {
      VariableDeclarator vd = fps[i].getDeclarator();
      String name = vd.getName().getText();  // getName returns a Word
     
      Type type = vd.getType();
      final String typeName = type.getName();
//      if (name.equals("myArray")) 
//        System.err.println("*** 2Var called for var " + name + " type = " + typeName);
      final SourceInfo si = type.getSourceInfo();
      // Note: typeName CANNOT be a local type; no such type is in scope
      SymbolData sd = _identifyType(typeName, si, enclosingClassName);

      varData[i] = 
        new VariableData(name, new ModifiersAndVisibility(SourceInfo.NONE, mav), sd, true, enclosing);
      
      assert ! varData[i].isPrivate();
      
      if (sd == null || sd == SymbolData.NOT_FOUND) { // a forward Type reference
        // To establish a reference to a not-yet-defined type, create a fixup
        final int j = i;
        /* The following is a kludge to make method signature collision detection work (unless different
         * names are used for the same type). */
        varData[j].setType(new SymbolData(typeName));
        Command fixUp = new Command() {
          public void execute() { 
            SymbolData newSd = _identifyType(typeName, si, enclosingClassName);
            if (newSd == null || newSd == SymbolData.NOT_FOUND) 
              System.err.println("****** In fixUp, the type " + typeName + " at " + si + " was NOT found.");
//            assert newSd != null && newSd != SymbolData.NOT_FOUND;  // TODO !!!: Expand to error message?
            if (newSd != null) varData[j].setType(newSd);
          }
        };
        fixUps.add(fixUp);
      }
    
//        System.err.println("For inner class/interface " + typeName + " found type " + type);
      varData[i].gotValue();
      varData[i].setIsLocalVariable(true);
    }
    return varData;
  }
   
//  /** Identifies the SymbolData in symbolTable matching typeName.  Returns null if no match is
//    * found.  Searches for typeName as a fully qualified Name and as a relative name within
//    * the current class.  TODO: match inner classes of classes enclosing the current class. */
//  private SymbolData _identifyVarType(String typeName, SourceInfo si) {
//    SymbolData sd = getSymbolData(typeName, si);
//    if (sd != null) return sd;
//    SymbolData enclosingSD = getQualifiedSymbolData(_enclosingClassName, SourceInfo.NONE);
//    assert enclosingSD != null;
//    return _enclosingClass.getInnerClassOrInterface(typeName);
//    // TODO: fails for nested inner classes
//  }
  
  /** Looks up the return type of a method. */
  private SymbolData _lookupReturnString(String rtString, SourceInfo si) {
    return  rtString.equals("void") ? SymbolData.VOID_TYPE : getSymbolData(rtString, si);
  }
      
  /** Creates a MethodData corresponding to the MethodDef within the context of the SymbolData sd. */
  protected MethodData createMethodData(final MethodDef that, final SymbolData sd) {
    
    assert _enclosingClassName != null && getQualifiedSymbolData(_enclosingClassName).equals(sd);
    
//    _log.log("createMethodData(" + that + ", " + sd + ") called.");
//    System.err.println("createMethodData(" + that.getName().getText() + ", " + sd + ") called.");
//    System.err.println("_enclosingClassName = " + _enclosingClassName);
    that.getMav().visit(this);
    that.getName().visit(this);
    
    // Turn the thrown exceptions from a ReferenceType[] to a String[]
    String[] throwStrings = referenceType2String(that.getThrows());
       
    final String rtString = that.getResult().getName();

    // Identify the return type
    final SourceInfo si = that.getResult().getSourceInfo();
//    if (! sd.equals(getQualifiedSymbolData(_enclosingClassName, SourceInfo.NONE))) {
//      System.err.println("sd = " + sd);
//      System.err.println("other = " + getQualifiedSymbolData(_enclosingClassName, SourceInfo.NONE));
//      assert false;
//    }
    // Note: rtString cannot be a local type; no such type is in scope.  BUT it can be a type variable or
    // a forward reference.
    SymbolData returnType = _identifyType(rtString, si, _enclosingClassName);
    
    final String name = that.getName().getText();
//    System.err.println("Creating MethodData for " + name + " in type " + sd);
    final MethodData md = 
      MethodData.make(name, that.getMav(), that.getTypeParams(), returnType, null, throwStrings, sd, that);
    VariableData[] vds = formalParameters2VariableData(that.getParams(), sd);
        
    if (returnType == null) {
//      System.err.println("Creating return type fixup for " + rtString + " in method " + name + " in class " + sd);
      final String enclosingClassName = _enclosingClassName;
      Command fixUp = new Command() {
        public void execute() {
          SymbolData newReturnType = _identifyType(rtString, si, enclosingClassName);
          if (newReturnType == null && (! (LanguageLevelVisitor.this instanceof FullJavaVisitor))) {
            _addAndIgnoreError("The return type " + rtString + " for method " + name + " in type " + sd + " is undefined.", 
                               that);
//            System.err.println("The return type " + rtString + " for method " + name + " in type " + sd + " is undefined.");
//            assert false;
          }
          else {
//            System.err.println("FixUp set the returnType of " + name + "in type " + sd + " to " + newReturnType);
            md.setReturnType(newReturnType);
          }
        }
      };
      fixUps.add(fixUp);
    }
    
//    System.err.println("Called createMethodData(" + name + ", " + sd.getName() + ")");
//    _log.log("createMethodData called.  Created MethodData " + md + '\n' + "with modifiers:" + md.getMav());
    // Turn the parameters from a FormalParameterList to a VariableData[]

    
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
  
  /** Generates a brief print string for a VariableDeclarator. */
  private static String declaratorsToString(VariableDeclarator[] vds) {
    StringBuilder printString = new StringBuilder("{ ");
    for (VariableDeclarator vd: vds) {
      printString.append(vd.getName().getText()).append(": ").append(vd.getType().getName()).append("; ");
    }
    return printString.append('}').toString();
  }
  
  /** This method assumes that the modifiers for this particular VariableDeclaration have already been checked.  It 
    * does no semantic checking.  It simply converts the declarators to variable datas, by trying to resolve the types
    * of each declarator.  The VariableDeclaration may be a field declaration!  The Data enclosing may be a MethodData!
    */
  protected VariableData[] _variableDeclaration2VariableData(VariableDeclaration vd, final Data enclosing) {
    assert enclosing != null;
//    System.err.println("*** 2Var called for \n" + declaratorsToString(vd.getDeclarators()) + "\nin " + enclosing);
    LinkedList<VariableData> vds = new LinkedList<VariableData>();
    ModifiersAndVisibility mav = vd.getMav();
    VariableDeclarator[] declarators = vd.getDeclarators();
    for (final VariableDeclarator declarator: declarators) {
      declarator.visit(this); // Does NOTHING!
      final Type type = declarator.getType();
      final String name = declarator.getName().getText();
      final String typeName = type.getName();
//      assert enclosing == getQualifiedSymbolData(_enclosingClassName, SourceInfo.NONE);
      // TODO: if enclosing is a MethodData, we should first look for a local class!!!  This search will always
      // succeed if a matching local class exists because no forward reference is possible. (Confirm this!) !!!
      /* TODO: do we need to worry about case when enclosing is a MethodData?  Yes. defineInnerSymbolData 
       * already treats local classes specially, but it doesn't help.  References to local types use relative
       * class names. */
      SymbolData sd = _identifyType(typeName, declarator.getSourceInfo(), _enclosingClassName);  // may be null
      boolean initialized = declarator instanceof InitializedVariableDeclarator;
      // want hasBeenAssigned to be true if this variable declaration is initialized, and false otherwise.
//      System.err.println("Creating new VariableData " + name + " : " + typeName + " within " + _enclosingClassName);
      final VariableData vdata = new VariableData(name, mav, sd, initialized, enclosing); 
      vdata.setHasInitializer(initialized);
//        vdata.setIsLocalVariable(true);
      vds.addLast(vdata); 
//        System.err.println("identifyReturnType(" + type + ", " + name + ", " + enclosing + ") returned null");
      if (sd == null) { // TODO !!! Can this really happen?
        // Create fixup
        final String enclosingName = _enclosingClassName;  // Grab the current enclosing class name
//        System.err.println("**** Creating fixup for preceding VariableData");
        Command fixup = new Command() {
          public void execute() {
//            System.err.println("**** Executing fixup for " + typeName + " within " + enclosingName);
            SymbolData newSd = _identifyType(typeName, declarator.getSourceInfo(), enclosingName);
            if (newSd != null) vdata.setType(newSd);
            else if (! (LanguageLevelVisitor.this instanceof FullJavaVisitor) ) // TODO: fix this kludge!!!
              _addAndIgnoreError("Class or Interface " + typeName + " not found", type);
          }
        };
        fixUps.add(fixup);       
      }
    }
//    System.err.println("Returning VariableDatas " + vds);
    return vds.toArray(new VariableData[vds.size()]);
  }                       
                                  
  /** Identifies the SymbolData matching name in symbolTable.  Returns null if no match is found.  Searches for typeName
    * as a fully qualified Name and as a relative name within the enclosing class.  enclosingClassName is null if type 
    * is part of the header for a class or interface.  Methods can introduce local types.  Make sure that we can match
    * inner classes of the chain of enclosing datas.  We need to use the relative inner class name to do this. */
  protected SymbolData _identifyType(String name, SourceInfo si, String enclosingClassName) {
//    System.err.println("***** Calling _identifyType(" + name  + ") within " + enclosingClassName);
    
    // If name is a type variable, return the binding
    if (_genericTypes.containsKey(name)) return _genericTypes.get(name);
    
    SymbolData sd = getSymbolData(name, si);  // TODO: uses wrong enclosingClassName!!!
    if (sd != null) return sd;
    
//    if (name.equals("ResType") && sd == null) 
//      Utilities.show("_genericTypes = " + _genericTypes + "Trace follows.\n" + Utilities.getStackTrace());
        
//    if (enclosingClassName == null) Utilities.show("_identifyType called with null enclosingClassName. Trace follows.\n" +
//                                                   Utilities.getStackTrace());
   
//      System.err.println("***ERROR*** in _identifyType " + enclosingClassName + " NOT FOUND");
    if (enclosingClassName == null) return null;  // happens for binding occurrences of type variables?
    
    SymbolData enclosingSD = getQualifiedSymbolData(enclosingClassName, SourceInfo.NONE);   
    if (enclosingSD == null) return null;
    
    sd = enclosingSD.getInnerClassOrInterface(name);
     
//    // Create continuation for new type
//    String qualifiedTypeName = enclosingClassName + '.' + name;
//    if (_innerClassesInThisBody.contains(qualifiedTypeName)) {  
//      // reference to an inner class that will subsequently be defined
//      sd = addInnerSymbolData(si, qualifiedTypeName, enclosingSD);
//    }
   
//    System.err.println("***** _identifyType(" + name  + ") within " + enclosingClassName + " RETURNED " + sd);
    return sd;  // Note: sd is null if name is not identified.
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
    Pair<String, JExpressionIF> newMsg = new Pair<String, JExpressionIF>(message, that);
    if (! errors.contains(newMsg)) errors.addLast(newMsg);
//    else System.err.println("Suppressing error as duplicate: " + newMsg);
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
  
  /** Add an error explaining the modifiers' conflict. */
  public void _badModifiers(String first, String second, JExpressionIF that) {
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
      if (s.endsWith(name) && ! s.equals(getQualifiedClassName(name))) {  // TODO: this test is too coarse!
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
       getQualifiedSymbolData("java.lang." + that.getName().getText(), that.getSourceInfo(), false, false, false);
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
  
  /** Do the common work for SimpleAnonymousClassInstantiations and ComplexAnonymousClassInstantiations
    * and in FullJava and Functional Java.
    * @param that       The AnonymousClassInstantiation being visited.
    * @param enclosing  The SymbolData of the enclosing class.
    * @param superC  The super class being instantiated--i.e. new A() { ...}, would have a super class of A.
    */
  public void anonymousClassInstantiationHelper(AnonymousClassInstantiation that, SymbolData enclosing, String superName) {
    that.getArguments().visit(this);
    SymbolData enclosingSD = enclosing.getSymbolData();
    String enclosingSDName = enclosingSD.getName();
    assert enclosingSDName.equals(_enclosingClassName);
    String anonName = getQualifiedClassName(enclosingSDName) + "$" +  enclosingSD.preincrementAnonymousInnerClassNum();
    
//    System.err.println("****** In anonymousCIH the anonName = " + anonName + " superName = " + superName 
//                         + " enclosing = " + enclosing);
    
    // Define the SymbolData that will correspond to this anonymous class
    SymbolData anonSD = defineAnonymousSymbolData(that, anonName, superName);
    
//    if (this instanceof IntermediateVisitor) {
    // These methods are no-ops in FullJavaVisitor
    createToString(anonSD);
    createHashCode(anonSD);
    createEquals(anonSD);
    // Accessors will be filled in in typeChecker pass
//    }
    
    // Visit the body (with the appropritate class body visitor to get it all nice and resolved.
//    System.err.println("Calling appropriate class body visitor for " + anonName);
    that.getBody().visit(newClassBodyVisitor(anonSD, anonName));
  }
  
  /** Processes the class body that. */
  protected void identifyInnerClasses(TypeDefBase that) {
    String prefix = _enclosingClassName == null ? "" : _enclosingClassName + '.';
    String enclosingType = getQualifiedClassName(prefix + that.getName().getText());
//    System.err.println("***** identifyInnerClasses called for " + enclosingType + " in file " + _file);
    assert enclosingType != null;
    // Process the members of this class
//    System.err.println("Finding inner classes in " + enclosingType);
    SymbolData sd = getSymbolData(enclosingType, SourceInfo.NONE);
//    System.err.println("SymbolData for " + enclosingType + " is " + sd);
    enclosingType = sd.getName(); // that may be a local class, which has a more elaborate name
    BracedBody body = that.getBody();
    for (BodyItemI bi: body.getStatements()) {
      if (bi instanceof TypeDefBase) {
        TypeDefBase type = (TypeDefBase) bi;
        String rawClassName = type.getName().getText();
//        System.err.println("Adding " + rawClassName + " to inner classes of " + enclosingType + "\n");
        String fullClassName = enclosingType + '.' + rawClassName;
//        System.err.println("Adding " + rawClassName + " to _innerClassesInThisBody inside " + that + "\n");
        
//          _innerClassesInThisBody.add(fullClassName);
//        System.err.println("***** Making continuation for " + fullClassName + " in file " + _file);
        SymbolData innerSD = makeContinuation(bi.getSourceInfo(), fullClassName);
//        System.err.println("***** Continuation " + innerSD + " returned");
//        Utilities.show("***** Continuation " + innerSD + " returned");
        sd.addInnerClass(innerSD);
      }
    }
//    System.err.println("_innerClassesInThisBody = " + _innerClassesInThisBody);
  }
    
  /** Process the members/statements of the class body, method body, or ordinary body (e.g. a try body, catch clause 
    * body, or compound statement body. This is VERY BAD data design.  These various bodies have significantly 
    * different meanings. */
//  public Void forBracedBodyDoFirst(BracedBody that) {
//    if (! (this instanceof BodyBodyFullJavaVisitor)) {  // full Java method bodies are excluded by this test
//      // Process the members of this class
//      SymbolData sd = getSymbolData(_enclosingClassName, SourceInfo.NONE);
//      for (BodyItemI bi: that.getStatements()) {
//        if (bi instanceof TypeDefBase) {
//          TypeDefBase type = (TypeDefBase) bi;
//          String rawClassName = type.getName().getText();
//          _log.log("Adding " + rawClassName + " to _innerClassesInThisBody inside " + that + "\n");
//          String fullClassName = _enclosingClassName + '.' + rawClassName;
////        System.err.println("Adding " + rawClassName + " to _innerClassesInThisBody inside " + that + "\n");
//          
//          _innerClassesInThisBody.add(fullClassName);
//          SymbolData innerSD = makeContinuation(bi.getSourceInfo(), fullClassName);
//          sd.addInnerClass(innerSD);
//        }
//      }
//    System.err.println("_innerClassesInThisBody = " + _innerClassesInThisBody);
//    }
//    
//    return super.forBracedBodyDoFirst(that);
//  }
  
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
        _package = _package + '.' + temp;
      }    
      String directory = _file.getParent();
      if (directory == null || !directory.endsWith(newPackage)) {
        _addAndIgnoreError("The package name must mirror your file's directory.", that);
      }
    }
    // Call getSymbolData to see if this is actually a class as well as a Package Name.  If it is, an error will be 
    // given in the TypeChecking step.
    // If file is a .java file and not compiled, won't find it.  This is not consistent with the JLS.
    // if file is a ll file and not compiled, will find it, though this is not consistent with the JLS.
//    getSymbolData(_package, that.getSourceInfo(), false);
    return forJExpressionOnly(that);
  }
  
  
  /** Make sure the class being imported has not already been imported.
    * If there are no errors, add it to the list of imported files, and create a continuation for it.
    * The class will be resolved later.
    */
  public Void forClassImportStatementOnly(ClassImportStatement that) {
    CompoundWord cWord = that.getCWord();
    Word[] words = cWord.getWords();
    
    // Make sure that this specific imported class has not already been specifically imported
    for (int i = 0; i < _importedFiles.size(); i++) {
      String name = _importedFiles.get(i);
      int indexOfLastDot = name.lastIndexOf('.');
      if (indexOfLastDot != -1 && 
          (words[words.length-1].getText()).equals(name.substring(indexOfLastDot + 1, name.length()))) {
        _addAndIgnoreError("The class " + words[words.length-1].getText() + " has already been imported.", that);
        return null;
      }
    }
    
    StringBuilder nameBuff = new StringBuilder(words[0].getText());
    for (int i = 1; i < words.length; i++) {nameBuff.append('.' + words[i].getText());}
    
    String qualifiedTypeName = nameBuff.toString();
    
//    // Make sure that this imported class does not duplicate the package.  WHY? FIX THIS.
//    // Although this is allowed in full java, we decided to not allow it at any LanguageLevel.
//    int indexOfLastDot = qualifiedTypeName.lastIndexOf('.');
//    if (indexOfLastDot != -1) {
//      if (_package.equals(qualifiedTypeName.substring(0, indexOfLastDot))) {
//        _addAndIgnoreError("You do not need to import " + qualifiedTypeName 
//                             + ".  It is in your package so it is already visible", 
//                           that);
//        return null;
//      }
//    }
    
    //Now add the class to the list of imported files
    _importedFiles.addLast(qualifiedTypeName);  
    
    // Create a continuation for imported class if one does not already exist
    createImportedSymbolContinuation(qualifiedTypeName, that.getSourceInfo());
    return forImportStatementOnly(that);
  }
  
  /** Create a continuation for imported class specified by qualifiedName if one does not already exist. */
  protected SymbolData createImportedSymbolContinuation(String qualifiedTypeName, SourceInfo si) {

    SymbolData sd = symbolTable.get(qualifiedTypeName);
    if (sd == null) {
      // Create a continuation for the imported class and put it into the symbol table so
      // that on lookup, we can check imported classes before classes in the same package.
//      System.err.println("Creating continuation for imported class " + temp);
      sd = makeContinuation(si, qualifiedTypeName);
    }
    return sd;
  }
  
  /**Check to make sure that this package import statement is not trying to import the current pacakge. */
  public Void forPackageImportStatementOnly(PackageImportStatement that) { 
    CompoundWord cWord = that.getCWord();
    Word[] words = cWord.getWords();
    StringBuilder tempBuff = new StringBuilder(words[0].getText());
    for (int i = 1; i < words.length; i++) { tempBuff.append('.' + words[i].getText()); }
    String temp = tempBuff.toString();
    
    
    //make sure this imported package does not match the current package
    if (_package.equals(temp)) {
      _addAndIgnoreError("You do not need to import package " + temp + 
                         ". It is your package so all public classes in it are already visible.", that);
      return null;
    }

    if (! _importedPackages.contains(temp)) _importedPackages.addLast(temp);
    
    return forImportStatementOnly(that);
  }
  
  /** Makes sure that this concrete method def is not declared to be abstract. */
  public Void forConcreteMethodDefDoFirst(ConcreteMethodDef that) {
    ModifiersAndVisibility mav = that.getMav();
    String[] modifiers = mav.getModifiers();
    // Concrete methods can be public, private, protected, or static at the Intermediate (Functional) level.
    for (int i = 0; i < modifiers.length; i++) {
      if (modifiers[i].equals("abstract")) {
        _addError("Methods that have a braced body cannot be declared \"abstract\"", that);
        break;
      }
    }
    return super.forConcreteMethodDefDoFirst(that);
  }
  
  /** Makes sure that this abstract method def is not declared to be static. */
  public Void forAbstractMethodDefDoFirst(AbstractMethodDef that) {
    ModifiersAndVisibility mav = that.getMav();
    String[] modifiers = mav.getModifiers();
    // Concrete methods can now be public, private, protected at the Intermediate level.  They still cannot be static.
    if (Utilities.isStatic(modifiers)) _badModifiers("static", "abstract", that);
    return super.forAbstractMethodDefDoFirst(that);
  }
  
  /** Bitwise operators are allowed in Full Java */
  public Void forShiftAssignmentExpressionDoFirst(ShiftAssignmentExpression that) { return null; }
  public Void forBitwiseAssignmentExpressionDoFirst(BitwiseAssignmentExpression that) { return null; }
  public Void forBitwiseBinaryExpressionDoFirst(BitwiseBinaryExpression that) { return null; }
  public Void forBitwiseOrExpressionDoFirst(BitwiseOrExpression that) { return null; }
  public Void forBitwiseXorExpressionDoFirst(BitwiseXorExpression that) { return null; }
  public Void forBitwiseAndExpressionDoFirst(BitwiseAndExpression that) { return null; }
  public Void forBitwiseNotExpressionDoFirst(BitwiseNotExpression that) { return null; }
  public Void forShiftBinaryExpressionDoFirst(ShiftBinaryExpression that) { return null; }
  public Void forBitwiseNotExpressionDoFirst(ShiftBinaryExpression that) { return null; }
  
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
    _classesInThisFile = new HashSet<String>();
    for (int i = 0; i < types.length; i++) {
      // TODO: Add inner classes to this list?
      
      String qualifiedClassName = getQualifiedClassName(types[i].getName().getText());
      _classesInThisFile.add(qualifiedClassName);
//      System.err.println("Adding " + qualifiedClassName + " to _classesInThisFile");
      _log.log("Adding " + qualifiedClassName + " to _classesInThisFile");
    }
    
    for (int i = 0; i < types.length; i++) {
      // Remove the class that is about to be visited from the list of ClassDefs in this file.
      String qualifiedClassName = getQualifiedClassName(types[i].getName().getText());
      // Only visit a class if _classesInThisFile contains it.  Otherwise, this class has 
      // already been processed since it was a superclass of a previous class.
      if (_classesInThisFile.contains(qualifiedClassName)) {
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
//    System.err.println("forVariableDeclaration in LLV called for " + that);
    forVariableDeclarationDoFirst(that);
    
    if (prune(that)) return null;
//    System.err.println("forVariableDeclarationDoFirst(...) completed with no errors");
    that.getMav().visit(this);
//    System.err.println("Mav visit completed in forVariableDeclaration; getClass() = "  + getClass());
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
 
//    System.err.println("**** createConstructor called for " + sd);
        
//    if (sd == null) {
//      System.err.println("**** Error **** After fixups, SymbolData " + sd + " has null for a super class");
//      assert false;
//    }
//    
    if (sd.isContinuation()) {
      _addAndIgnoreError("Could not generate constructor for class " + sd + " because it has no definition", 
                new NullLiteral(SourceInfo.NONE));
      return;
    }
    
    SymbolData superSd = sd.getSuperClass();
    if (superSd == null) {
      _addAndIgnoreError("Could not generate constructor for class " + sd + " because it has no superclass", 
                new NullLiteral(SourceInfo.NONE));
      return;
    }
    
    else {
      LinkedList<MethodData> superMethods = superSd.getMethods();
      String superUnqualifiedName = getUnqualifiedClassName(superSd.getName());
      
      LanguageLevelVisitor sslv = LanguageLevelConverter._newSDs.remove(superSd);
      
      // if sslv == null, the superclass constructor has already been generated or we are caught in a cyclic
      // inheritance hierarchy
      if (sslv != null) {
        sslv.createConstructor(superSd);
//        System.err.println("Creating constructor for superclass " + superSd);
      }
      
      // Find the super's smallest constructor.
      MethodData superConstructor = null;
      for (MethodData superMd: superMethods) {
//      Iterator<MethodData> iter = superMethods.iterator();
//      while (iter.hasNext()) {
//        MethodData superMd = iter.next();
        if (superMd.getName().equals(superUnqualifiedName)) {
          if (superConstructor == null || superMd.getParams().length < superConstructor.getParams().length) {
            superConstructor = superMd;
          }
        }
      }
      if (superConstructor == null) {
        _addAndIgnoreError("Could not generate constructor for class " + sd + " superclass has no constructor, perhaps"
                           + " because the class hierarchy is cyclic.",
                           new NullLiteral(SourceInfo.NONE));
        return;
      }
//      if (superConstructor == null) {
//        System.err.println("**** Error **** The superclass " + superSd + " has no constructors ");
//      }
      String name = getUnqualifiedClassName(sd.getName());
      MethodData md = new MethodData(name,
                                     PUBLIC_MAV, 
                                     new TypeParameter[0], 
                                     sd, 
                                     new VariableData[0], // Parameters to be filled in later. 
                                     new String[0], 
                                     sd,
                                     null);
      
      LinkedList<VariableData> params = new LinkedList<VariableData>();
//      if (superConstructor != null) {
      for (VariableData superParam : superConstructor.getParams()) {
        String paramName = md.createUniqueName("super_" + superParam.getName());
        SymbolData superParamSD = superParam.getType();
        assert superParamSD != null;
        VariableData newParam = new VariableData(paramName, PACKAGE_MAV, superParamSD, true, sd);  // Note: sd was md
        newParam.setGenerated(true);
        params.add(newParam);
        // Next line done on each iteration so that createUniqueName handles nameless super parameters (in class files)
        md.addVar(newParam);  // Fixups have already been executed
      }
//      }
    
      // only add in those fields that do not have a value and are not static.
      boolean hasOtherConstructor = sd.hasMethod(name);
      
      for (VariableData field : sd.getVars()) {
        
        if (! field.hasInitializer() && ! field.hasModifier("static")) {
          if (! hasOtherConstructor) { field.gotValue(); } // Set hasValue if no other constructors need to be visited
          // Rather than creating a new parameter, we use the field, since all the important data is the same in both of
          // them.
          VariableData param = field.copyWithoutVisibility();
          params.add(param);
        }
      }
      // Some fields may be declared private, but parameters cannot be; unprivatize the 
      
      md.setParams(params.toArray(new VariableData[params.size()]));
      md.setVars(params);
      
//      System.err.println("**** Adding constructor " + md + " **** to symbol " + sd);
//      if (md.getName().equals("ClassName"))
//        System.err.println("****** constructor visibility = " + md.getMav());
      addGeneratedMethod(sd, md);
    }
    LanguageLevelConverter._newSDs.remove(sd); // this won't do anything if sd is not in _newSDs.
  }
  
  /** Create a method that is an accessor for each field in the class.
    * File file is passed in so this can remain a static method
    * TODO: should this be called AFTER all fixups have been performed?  No method needs to be in 
    *       symbol table.
    */
  protected static void createAccessors(SymbolData sd, File file) {
    if (LanguageLevelConverter.isAdvancedFile(file)) return;
    LinkedList<VariableData> fields = sd.getVars();
    for (final VariableData vd: fields) {     
      if (! vd.hasModifier("static")) { 
        String name = getFieldAccessorName(vd.getName());
        SymbolData returnTypeSD = vd.getType();
        final MethodData md = new MethodData(name,
                                             PUBLIC_MAV, 
                                             new TypeParameter[0], 
                                             returnTypeSD, 
                                             new VariableData[0],
                                             new String[0], 
                                             sd,
                                             null); // no SourceInfo
        addGeneratedMethod(sd, md);
        if (returnTypeSD == null) { // create a fixup to patch the return type of md; vd may have pending return type
          Command fixUp = new Command() {
            public void execute() { md.setReturnType(vd.getType()); }
          };
          fixUps.add(fixUp);
        } 
      }
    }
  } 
  
  /** Create a method called toString that returns type String. Overridden at the Advanced Level files, because n code
    * augmentation is done for them so you don't want to create this method.
    */ 
  protected void createToString(SymbolData sd) {
    String name = "toString";
    MethodData md = new MethodData(name,
                                   PUBLIC_MAV, 
                                   new TypeParameter[0], 
                                   getSymbolData("java.lang.String", SourceInfo.make("java.lang.String")), 
                                   new VariableData[0],
                                   new String[0], 
                                   sd,
                                   null); // no SourceInfo
    addGeneratedMethod(sd, md);    
  }
  
  /** Creates a method called hashCode that returns an int. Overriden for FullJava files, because no code augmentation 
    * is done for them, so we don't want to create this method.
    */ 
  protected void createHashCode(SymbolData sd) {    
    String name = "hashCode";
    MethodData md = new MethodData(name,
                                   PUBLIC_MAV, 
                                   new TypeParameter[0], 
                                   SymbolData.INT_TYPE, 
                                   new VariableData[0],
                                   new String[0], 
                                   sd,
                                   null); // no SourceInfo
    addGeneratedMethod(sd, md);
  }
  
  /** Creates a method called equals() that takes in an Object argument and returns a boolean.  Overriden for FullJava
    * files, because no code augmentation is done for them, so we don't want to create this method.
    */ 
  protected void createEquals(SymbolData sd) {    
    String name = "equals";
    SymbolData type = getSymbolData("java.lang.Object", SourceInfo.make("java.lang.Object"));
    VariableData param = new VariableData(type);
    MethodData md = new MethodData(name,
                                   PUBLIC_MAV,
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
    getQualifiedSymbolData("java.lang.String", that.getSourceInfo(), true);
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
    
    // TODO? create a fixup?
    
    return forSimpleNamedClassInstantiationOnly(that);
  }
  
  
  /** Determines whether array1 equals array2 using the equals method on Object[] arrays in java.util.Arrays.
    * @return true if the two array argument (which may be null) are equal.
    */ 
  public static boolean arrayEquals(Object[] array1, Object[] array2) {
//    return Arrays.equals(array1, array2); 
    int n = array1.length;
    if (n != array2.length) return false;
    for (int i = 0; i < n; i++) {
      Object o1 = array1[i];
      Object o2 = array2[i];
      if (o1 == null && o2 != null) return false;
      if (! o1.equals(o2)) return false;
    };
    return true;
  }
  
  /** Use this to see if a name references a type that needs to be added to the symbolTable. */
  private class ResolveNameVisitor extends JExpressionIFAbstractVisitor<TypeData> {
    
    public ResolveNameVisitor() { }
    
    /** Most expressions are not relevant for this check--visit them with outer visitor. */
    public TypeData defaultCase(JExpressionIF that) {
      that.visit(LanguageLevelVisitor.this);
      return null;
    }
    
    /** Try to look up this simple name reference and match it to a symbol data. If it could not be matched, return a 
      * package data.
      * @param that  The thing we're trying to match to a type
      */
    public TypeData forSimpleNameReference(SimpleNameReference that) {
      SymbolData result = getSymbolData(that.getName().getText(), that.getSourceInfo());
      // it could not be resolved: return a Package Data
      // TODO: create a fixup !!!
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
      
      // TODO: create a fixup?
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

    public LanguageLevelVisitorTest() { this(""); }
    public LanguageLevelVisitorTest(String name) { super(name); }
    
    public void setUp() {
      Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations =
        new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>();
      LinkedList<Command> fixUps = new LinkedList<Command>();
      // The following ensures that essential symbols have been loaded into the symbolTable, a static field of 
      // LanguageLevelConverter.
      testLLVisitor = new LanguageLevelVisitor(new File(""), 
                                               "",
                                               "i.like.monkey",
                                               new LinkedList<String>(), 
                                               new LinkedList<String>(), 
                                               new HashSet<String>(), 
                                               continuations,
                                               fixUps);
      errors = new LinkedList<Pair<String, JExpressionIF>>();
      _errorAdded = false;
      
      visitedFiles = new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>();      
//      _hierarchy = new Hashtable<String, TypeDefBase>();
      testLLVisitor._classesInThisFile = new HashSet<String>();
      if (! testLLVisitor._importedPackages.contains("java.lang")) testLLVisitor._importedPackages.add("java.lang");

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
      SymbolData objectSD = LanguageLevelConverter._classFile2SymbolData("java.lang.Object", "");
      SymbolData stringSD = LanguageLevelConverter._classFile2SymbolData("java.lang.String", "");
      MethodData md = new MethodData("substring", PUBLIC_MAV, new TypeParameter[0], stringSD, 
                                     new VariableData[] {new VariableData(SymbolData.INT_TYPE)},
                                     new String[0], stringSD, null);
      assertTrue("java.lang.String should have been converted successfully", 
                 stringSD.getName().equals("java.lang.String"));
      assertEquals("java.lang.String's superSD should should be java.lang.Object", 
                   objectSD,
                   stringSD.getSuperClass());
      
      LinkedList<MethodData> methods = stringSD.getMethods();
      Iterator<MethodData> iter = methods.iterator();
      boolean found = false;
      
      while (iter.hasNext()) {
        MethodData currMd = iter.next();
        if (currMd.getName().equals("substring") && currMd.getParams().length == 1 && 
            currMd.getParams()[0].getInstanceData() == SymbolData.INT_TYPE.getInstanceData()) {
          // TODO: comparing getInstanceData() on preceding line is goofy; should check getType() instead
          found = true;
          md.getParams()[0].setEnclosingData(currMd);
          break;
        }
      }
      
      assertTrue("Should have found method substring(int) in java.lang.String", found);
      
      assertEquals("java.lang.String should be packaged correctly", "java.lang", 
                   testLLVisitor.getSymbolData("java.lang.String", SourceInfo.NONE).getPackage());
      
      //now, test that a second call to the same method won't replace the symbol data that is already there.
      SymbolData newStringSD = LanguageLevelConverter._classFile2SymbolData("java.lang.String", "");
      assertTrue("Second call to classFileToSymbolData should not change sd in hash table.", 
                 stringSD == LanguageLevelConverter.symbolTable.get("java.lang.String"));
      assertTrue("Second call to classFileToSymbolData should return same SD.", 
                 newStringSD == LanguageLevelConverter.symbolTable.get("java.lang.String"));      
      //now, test one of our own small class files.
      
      SymbolData bartSD = LanguageLevelConverter._classFile2SymbolData("Bart", "testFiles");
      assertFalse("bartSD should not be null", bartSD == null);
      assertFalse("bartSD should not be a continuation", bartSD.isContinuation());
      MethodData md1 = 
        new MethodData("myMethod", PROTECTED_MAV, 
                       new TypeParameter[0], SymbolData.BOOLEAN_TYPE, 
                       new VariableData[] { new VariableData(SymbolData.INT_TYPE) }, 
                       new String[] {"java.lang.Exception"}, bartSD, null);
      
      md1.getParams()[0].setEnclosingData(bartSD.getMethods().getLast());
      MethodData md2 = new MethodData("Bart", PUBLIC_MAV, new TypeParameter[0], bartSD,
                                      new VariableData[0], new String[0], bartSD, null);
      
      VariableData vd1 = new VariableData("i", PUBLIC_MAV, SymbolData.INT_TYPE, true, bartSD);
      
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
    
//    public void testLookupFromClassesToBeParsed() {
//      // Create a ClassDef.  Recreate the ClassOrInterfaceType for Object instead of using 
//      // JExprParser.NO_TYPE since otherwise the ElementaryVisitor will complain that the
//      // user must explicitly extend Object.
//      ClassDef cd = 
//        new ClassDef(SourceInfo.NONE, PUBLIC_MAV, 
//                     new Word(SourceInfo.NONE, "Lisa"),
//                     new TypeParameter[0], 
//                     new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
//                     new ReferenceType[0], 
//                     new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
//      
//      // Use a ElementaryVisitor so lookupFromClassesToBeParsed will actually visit the ClassDef.
//      IntermediateVisitor bv = new IntermediateVisitor(new File(""), 
//                                                       errors, 
//                                                       continuations, 
//                                                       new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>());
//      
//      // Test that passing resolve equals false returns a continuation.
////      assertTrue("Should return a continuation", 
////                 testLLVisitor._identifyTypeFromClassesToBeParsed("Lisa", SourceInfo.NONE, false).isContinuation());
//      // Put Lisa in the hierarchy and test that there is one error and that the message
//      // says that there is cyclic inheritance.
////      _hierarchy.put("Lisa", cd);
////      _classesInThisFile.put("Lisa", new Pair<TypeDefBase, LanguageLevelVisitor>(cd, bv));
////      assertEquals("Should return null because Lisa is in the hierarchy", 
////                   null,
////                   _llv._identifyTypeFromClassesToBeParsed("Lisa", SourceInfo.NONE, true));
////      assertEquals("Should be one error", 1, errors.size());
////      assertEquals("Error message should be correct", "Cyclic inheritance involving Lisa", errors.get(0).getFirst());
////      _hierarchy.remove("Lisa");
//      //Re-add Lisa because the first call with resolve set to true removed it and
//      // test that Lisa is actually visited and added to the symbolTable.
//      testLLVisitor._classesInThisFile.put("Lisa", new Pair<TypeDefBase, LanguageLevelVisitor>(cd, bv));
//      assertFalse("Should return a non-continuation", 
//                  testLLVisitor._identifyTypeFromClassesToBeParsed("Lisa", 
//                                                    SourceInfo.NONE,
//                                                    true).isContinuation());
//    }
    
    public void testGetSymbolDataForClassFile() {
      // Test that passing a legal class return a non-continuation.
      assertFalse("Should return a non-continuation", 
                  LanguageLevelConverter.getSymbolDataForClassFile("java.lang.String", null).isContinuation());
      
      // Test that passing a userclass that can't be found returns null and adds an error.
      assertNull("Should return null with a user class that can't be found",
                 LanguageLevelConverter.getSymbolDataForClassFile("Marge", null));
//      assertEquals("There should be one error", 1, errors.size());
//      assertEquals("The error message should be correct", "Class Marge not found.", errors.get(0).getFirst());
    }
    
    
    public void testGetSymbolData_Primitive() {
      assertEquals("should be boolean type", SymbolData.BOOLEAN_TYPE, 
                   LanguageLevelConverter._getPrimitiveSymbolData("boolean"));
      assertEquals("should be char type", SymbolData.CHAR_TYPE, LanguageLevelConverter._getPrimitiveSymbolData("char"));
      assertEquals("should be byte type", SymbolData.BYTE_TYPE, LanguageLevelConverter._getPrimitiveSymbolData("byte"));
      assertEquals("should be short type", SymbolData.SHORT_TYPE, LanguageLevelConverter._getPrimitiveSymbolData("short"));
      assertEquals("should be int type", SymbolData.INT_TYPE, LanguageLevelConverter._getPrimitiveSymbolData("int"));
      assertEquals("should be long type", SymbolData.LONG_TYPE, LanguageLevelConverter._getPrimitiveSymbolData("long"));
      assertEquals("should be float type", SymbolData.FLOAT_TYPE, LanguageLevelConverter._getPrimitiveSymbolData("float"));
      assertEquals("should be double type", SymbolData.DOUBLE_TYPE, LanguageLevelConverter._getPrimitiveSymbolData("double"));
      assertEquals("should be void type", SymbolData.VOID_TYPE, LanguageLevelConverter._getPrimitiveSymbolData("void"));
      assertEquals("should be null type", SymbolData.NULL_TYPE, LanguageLevelConverter._getPrimitiveSymbolData("null"));
      assertEquals("should return null--not a primitive", null, 
                   LanguageLevelConverter._getPrimitiveSymbolData("java.lang.String"));
    }
    
//    public void testGetQualifiedSymbolData() {
//      testLLVisitor._file = new File("testFiles/Fake.dj0");
//      SymbolData sd = new SymbolData("testPackage.File");
//      testLLVisitor._package = "testPackage";
//      LanguageLevelConverter.symbolTable.put("testPackage.File", sd);
//      
//      SymbolData sd1 = new SymbolData("java.lang.String");
//      LanguageLevelConverter.symbolTable.put("java.lang.String", sd1);
//      
//      //Test that classes not in the symbol table are handled correctly.
//      assertEquals("should the continuation symbol", sd, 
//                   testLLVisitor._getQualifiedSymbolData("testPackage.File", SourceInfo.NONE, true, false, true));
////      assertEquals("should be one error so far.", 1, errors.size());
//      
//      
//      SymbolData sd2 = testLLVisitor._getQualifiedSymbolData("java.lang.Integer", SourceInfo.NONE, true, true, true);
//      assertEquals("should return non-continuation java.lang.Integer", "java.lang.Integer", sd2.getName());
//      assertFalse("should not be a continuation.", sd2.isContinuation());
//      
//      SymbolData sd3 = testLLVisitor._getQualifiedSymbolData("Wow", SourceInfo.NONE, true, true, true);
//      assertEquals("search should fail", null, sd3);
////      assertEquals("should return Wow", "Wow", sd3.getName());
////      assertFalse("Should not be a continuation.", sd3.isContinuation());
//      
//      // "testPackage.File" has been entered as a continuation in symbolTable.  Why should the following lookup fail?
////      //Test that classes in the symbol table are handled correctly
////      assertEquals("should return null sd--does not exist", null, 
////                   _llv._getQualifiedSymbolData("testPackage.File", SourceInfo.NONE, false, false, true));
////      assertEquals("Should be 1 error", 1, errors.size());
//      
//      sd.setIsContinuation(false);
//      assertEquals("should return non-continuation sd", sd, 
//                   testLLVisitor._getQualifiedSymbolData("testPackage.File", SourceInfo.NONE, true, false,  true));
//      
//      
//      assertEquals("Should return sd1.", sd1, 
//                   testLLVisitor._getQualifiedSymbolData("java.lang.String", SourceInfo.NONE, true, false, true));
//      assertFalse("sd1 should no longer be a continuation.", sd1.isContinuation());
//      
//      
//      
//      //check that stuff not in symbol table and packaged incorrectly is handled right.
//      assertEquals("should return null-because it's not a valid class", null, 
//                   testLLVisitor._getQualifiedSymbolData("testPackage.not.in.symboltable", 
//                                                   SourceInfo.NONE, true, false, true));
//      
//      assertEquals("should be two errors so far.", 2, errors.size());
//      assertNull("should return null", 
//                 testLLVisitor._getQualifiedSymbolData("testPackage.not.in.symboltable", 
//                                                 SourceInfo.NONE, false, false, false));
//      
//      assertNull("should return null.", 
//                 testLLVisitor._getQualifiedSymbolData("notRightPackage", SourceInfo.NONE, false, false, false));
//      assertEquals("should still be two errors.", 2, errors.size());
//    }
    
    public void testGetArraySymbolData() {
      //Initially, force the inner sd of this array type to be null, to test that.
      assertEquals("Should return null, because inner sd is null.", null, 
                   testLLVisitor.getArraySymbolData("TestFile", SourceInfo.NONE, false, false));
      
      /**Now, put a real SymbolData base in the table.*/
      SymbolData sd = new SymbolData("Iexist");
      LanguageLevelConverter.symbolTable.put("Iexist", sd);
      testLLVisitor.getArraySymbolData("Iexist", SourceInfo.NONE, false, false).getName();
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
                   testLLVisitor.getArraySymbolData("Iexist", SourceInfo.NONE, false, false));
      
      /**Now, try it with a multiple dimension array.*/
      testLLVisitor.getArraySymbolData("Iexist[]", SourceInfo.NONE, false, false);
      assertTrue("Should have added a multidimensional array to the table.", 
                 LanguageLevelConverter.symbolTable.containsKey("Iexist[][]"));
      
      SymbolData sd2 = new SymbolData("java.lang.String");
      LanguageLevelConverter.symbolTable.put("java.lang.String", sd2);
      testLLVisitor.getArraySymbolData("String[]", SourceInfo.NONE, false, true);
      assertTrue("Should have added java.lang.String[] to table", 
                 LanguageLevelConverter.symbolTable.containsKey("java.lang.String[]"));
      assertTrue("Should have added java.lang.String[][] to table", 
                 LanguageLevelConverter.symbolTable.containsKey("java.lang.String[][]"));
    }
    /** Tests _getSymbolDataFromFileSystem and one case of getQualifiedSymbolData. */
    public void testGetSymbolDataFromFileSystem() {
      _sd4.setIsContinuation(false);
      _sd6.setIsContinuation(true);
      LanguageLevelConverter.symbolTable.put("u.like.emu", _sd4);
      LanguageLevelConverter.symbolTable.put("cebu", _sd6);
      
      // Test if it's already in the symbol table and doesn't need to be resolved not stopping when it should.  
      // get error b/c not in classes to be parsed 
      assertEquals("symbol data is a not a continuation, but resolve is false so should just be returned", _sd6, 
         testLLVisitor._getSymbolDataFromFileSystem("cebu", SourceInfo.NONE, false, true));
      assertEquals("symbol data is a continuation, but resolve is false, so should just be returned.", _sd4, 
                   testLLVisitor._getSymbolDataFromFileSystem("u.like.emu", SourceInfo.NONE, false, true));
      
      // Lookup a name not in the file system with resolve equal to false, to confirm that null is returned.
      
      assertEquals("Should return SymbolData.NOT_FOUND", SymbolData.NOT_FOUND, 
                   testLLVisitor._getSymbolDataFromFileSystem("Corky", SourceInfo.NONE, false, true));
        // TODO: fix this test             
//      SymbolData matchCorky = testLLVisitor._getSymbolDataFromFileSystem("Corky", SourceInfo.NONE, true);
//      assertFalse("Should return a non-continuation", matchCorky.isContinuation());
      
      // Test if it needs to be resolved:
      ClassDef cd = 
        new ClassDef(SourceInfo.NONE, PUBLIC_MAV, 
                     new Word(SourceInfo.NONE, "Lisa"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      // Use a ElementaryVisitor so lookupFromClassesToBeParsed will actually visit the ClassDef.
      IntermediateVisitor bv = new IntermediateVisitor(new File(""));
      
      testLLVisitor._classesInThisFile.add("Lisa" /*, new Pair<TypeDefBase, LanguageLevelVisitor>(cd, bv)*/);
      assert testLLVisitor._classesInThisFile.contains("Lisa");
      SymbolData matchLisa = 
        testLLVisitor.getQualifiedSymbolData("Lisa", SourceInfo.NONE, true);
      assertTrue("Should return a continuation", matchLisa.isContinuation());
    }
    
    public void testGetSymbolDataFromFileSystem2() {
      //what if it is in classes to be parsed?
      //and what if the class we're looking up is in the same package as the current file?
      //qualified
      testLLVisitor._package="fully.qualified";
      testLLVisitor._file = new File("testFiles/fully/qualified/Fake.dj0");
      SymbolData sd2 = new SymbolData("fully.qualified.Woah");  // continuation
      testLLVisitor.symbolTable.put("fully.qualified.Woah", sd2);
      
      SymbolData result = 
        testLLVisitor._getSymbolDataFromFileSystem("fully.qualified.Woah", SourceInfo.NONE, false, true);
      
      assertEquals("Should return sd2, unresolved.", sd2, result);
      assertTrue("sd2 should still be unresolved", sd2.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
//      result = _llv._getSymbolDataFromFileSystem("fully.qualified.Woah", SourceInfo.NONE, false, true);
//      assertEquals("Should return sd2, now unresolved.", sd2, result);
//      assertTrue("sd2 should not be resolved", sd2.isContinuation());
//      assertEquals("Should be no errors", 0, errors.size());
      
//      result = _llv._getSymbolDataFromFileSystem("fully.qualified.Woah", SourceInfo.NONE, true, true);
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
      
      result = testLLVisitor._getSymbolDataFromFileSystem("fully.qualified.Woah", SourceInfo.NONE, false, true);
      
      assertEquals("Should return sd2, unresolved.", sd2, result);
      assertTrue("sd2 should still be unresolved", sd2.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
//      result = _llv._getSymbolDataFromFileSystem("fully.qualified.Woah", SourceInfo.NONE, true, true);
//      assertEquals("Should return sd2, now resolved.", sd2, result);
      
//      assertFalse("sd2 should be resolved", sd2.isContinuation());
//      assertEquals("Should be no errors", 0, errors.size());
      
      //what if there is no package
      //Now, check cases when desired SymbolData is in the symbol table.
      testLLVisitor._package = "";
      testLLVisitor._file = new File ("testFiles/Cool.dj0");  // non-existent file
      
      //unqualified
      SymbolData sd1 = new SymbolData("Wow");
      SymbolData obj = testLLVisitor._getSymbolDataFromFileSystem("java.lang.Object", SourceInfo.NONE, true, true);
      sd1.setSuperClass(obj);
      testLLVisitor.symbolTable.put("Wow", sd1);
      
      result = testLLVisitor._getSymbolDataFromFileSystem("Wow", SourceInfo.NONE, false, true);
      assertEquals("Should return sd1, unresolved.", sd1, result);
      assertTrue("sd1 should still be unresolved.", sd1.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
      result = testLLVisitor._getSymbolDataFromFileSystem("Wow", SourceInfo.NONE, true, true);
      assertEquals("Should return sd1, resolved.", sd1, result);
      assertFalse("sd1 should be resolved.", sd1.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
      result = testLLVisitor._getSymbolDataFromFileSystem("Wow", SourceInfo.NONE, true, true);
      assertEquals("Should return sd1.", sd1, result);
      assertFalse("sd1 should still be resolved.", sd1.isContinuation());
      assertEquals("Should be no errors", 0, errors.size());
      
      
      //finding the most recent file
      result = testLLVisitor._getSymbolDataFromFileSystem("James", SourceInfo.NONE, true, true);
      assertEquals("Search for James should fail", null, result);
//      assertEquals("Result should have 3 variables.", 3, result.getVars().size());
//      assertEquals("Should be no errors", 0, errors.size());
      
      //returning NOT_FOUND when it doesn't exist.
      testLLVisitor._package = "myPackage";
      assertEquals("Should return NOT_FOUND-does not exist.", 
                   SymbolData.NOT_FOUND, 
                   testLLVisitor._getSymbolDataFromFileSystem("WrongPackage.className", 
                                                               SourceInfo.NONE, true, false));
      assertEquals("Should be no errors", 0, errors.size());
      
      // Now, test case where class file still exists, but java file is gone.
      testLLVisitor._package = "";
      testLLVisitor._file = new File("testFiles/Fake.dj0");
      LinkedList<VariableData> vds = new LinkedList<VariableData>();
      result = testLLVisitor._getSymbolDataFromFileSystem("Doh", SourceInfo.NONE, true, true);
      vds.addLast(new VariableData("i", PACKAGE_MAV, SymbolData.INT_TYPE, true, result));
      vds.addLast(new VariableData("o", PACKAGE_MAV, obj, true, result));
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
                   sd1, testLLVisitor.getQualifiedSymbolData("Wow", SourceInfo.NONE, true));
      assertFalse("Should not be a continuation", sd1.isContinuation());  // There is a pre-existing class file Wow!
      
      // Invalid case
      SymbolData result = testLLVisitor.getSymbolData("ima.invalid", SourceInfo.NONE, true, false);
      assertEquals("Should return null-invalid class name", null, result);
      assertEquals("There should not be any errors", 0, testLLVisitor.errors.size());
      
      // Fully qualified class name
      testLLVisitor._package="fully.qualified";
      testLLVisitor._file = new File("testFiles/fully/qualified/Fake.dj0");
      SymbolData sd2 = new SymbolData("fully.qualified.Symbol");
      testLLVisitor.symbolTable.put("fully.qualified.Symbol", sd2);
      
      result = testLLVisitor.getSymbolData("fully.qualified.Symbol", SourceInfo.NONE, true, false);
      
      assertEquals("Should return sd2, resolved.", sd2, result);
      assertTrue("sd2 should be resolved", sd2.isContinuation());
      
      // Inner class
      sd1.setName("fully.qualified.Woah.Wow");
      sd2.addInnerClass(sd1);
      sd1.setOuterData(sd2);
      testLLVisitor.symbolTable.put("fully.qualified.Woah.Wow", sd1);
      testLLVisitor.symbolTable.remove("Wow");
      sd1.setIsContinuation(false);
      result = testLLVisitor.getSymbolData("fully.qualified.Woah.Wow", SourceInfo.NONE, true, false);
      assertEquals("Should return sd1 (the inner class!)", sd1, result);
      
      // Inner inner class
      SymbolData sd3 = new SymbolData("fully.qualified.Woah.Wow.James");
      sd1.addInnerClass(sd3);
//      System.err.println("SYMBOL TABLE ENTRY FOR \"fully.qualified.Woah.Wow.James\" = " + 
//                         _llv.symbolTable.get("fully.qualified.Woah.Wow.James"));
      sd3.setOuterData(sd1);
//      System.err.println("INNER CLASS LOOKUP YIELDS: " + sd1.getInnerClassOrInterface("James"));
      result = testLLVisitor.getSymbolData("fully.qualified.Woah.Wow.James", SourceInfo.NONE, true, false);
      assertEquals("Should return sd3", sd3, result);
    }
    
    public void testGetQualifiedSymbolData() {
      // Primitive types
      assertEquals("should return the int SymbolData", SymbolData.INT_TYPE, 
                   testLLVisitor.getQualifiedSymbolData("int", SourceInfo.NONE, true, true, true));
      assertEquals("should return the byte SymbolData", SymbolData.BYTE_TYPE, 
                   testLLVisitor.getQualifiedSymbolData("byte", SourceInfo.NONE, false, false, false));
      
      // Array types
      ArrayData ad = new ArrayData(SymbolData.INT_TYPE, testLLVisitor, SourceInfo.NONE);
      SymbolData result = testLLVisitor.getQualifiedSymbolData("int[]", SourceInfo.NONE, true, true, true);
      ad.getVars().get(0).setEnclosingData(result);  //.equals(...) on VariableData compares enclosing datas with ==.
      ad.getMethods().get(0).setEnclosingData(result.getMethods().get(0).getEnclosingData()); //similar hack
      assertEquals("should return the array type", ad, result);
      
      // Qualified types
      SymbolData sd = new SymbolData("java.lang.System");
      LanguageLevelConverter.symbolTable.put("java.lang.System", sd);
      assertEquals("should return the same sd", sd, 
                   testLLVisitor.getQualifiedSymbolData("java.lang.System", SourceInfo.NONE, false, true, true));
      assertTrue("should be a continuation", sd.isContinuation());
      assertEquals("should return the now resolved sd", sd, 
                   testLLVisitor.getQualifiedSymbolData("java.lang.System", SourceInfo.NONE, true, false, true));
      assertFalse("should not be a continuation", sd.isContinuation());
      
      // In this file
      sd = new SymbolData("fully.qualified.Qwerty");
      LanguageLevelConverter.symbolTable.put("fully.qualified.Qwerty", sd);
      testLLVisitor._classesInThisFile.add("fully.qualified.Qwerty");
      // Use a ElementaryVisitor so lookupFromClassesToBeParsed will actually visit the ClassDef.
      IntermediateVisitor bv = new IntermediateVisitor(new File(""), 
                                                       errors, 
                                                       continuations,
                                                       fixUps,
                                                       new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>());
      bv._package = "fully.qualified";
      bv._file = new File("testFiles/fully/qualified/Fake.dj0");
      ClassDef cd = new ClassDef(SourceInfo.NONE, 
                                 PACKAGE_MAV, 
                                 new Word(SourceInfo.NONE, "Qwerty"),
                                 new TypeParameter[0],
                                 new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]),
                                 new ReferenceType[0], 
                                 new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      bv._classesInThisFile.add("fully.qualified.Qwerty" /*, new Pair<TypeDefBase, LanguageLevelVisitor>(cd, bv)*/);
      assertEquals("should return sd the continuation", sd, 
                   bv.getSymbolData("Qwerty", SourceInfo.NONE, true, true));
      assertTrue("should be a continuation", sd.isContinuation());
      assertEquals("should also return a continuation", sd, 
                   bv.getQualifiedSymbolData("fully.qualified.Qwerty", SourceInfo.NONE, false, false, true));
      assertTrue("should be a continuation", sd.isContinuation());
      
      // Imported files
      testLLVisitor._importedFiles.addLast("a.b.c");
      sd = new SymbolData("a.b.c");
//      System.err.println("SymbolData for 'a.b.c' is " + sd);
      LanguageLevelConverter.symbolTable.put("a.b.c", sd);
//      System.err.println("SymbolTable entry for 'a.b.c' is " + LanguageLevelConverter.symbolTable.get("a.b.c"));
//      LanguageLevelConverter.symbolTable.put("foobar", new SymbolData("This is strange"));
//      System.err.println("SymbolTable entry for 'foobar' is " + LanguageLevelConverter.symbolTable.get("foobar"));
      assertEquals("should find the continuation in the symbol table", sd, 
                   testLLVisitor.getQualifiedSymbolData("a.b.c", SourceInfo.NONE, false, true, true));
      // TODO: create an import table to look at when no match is found in symbolTable.
      assertTrue("should be a continuation", sd.isContinuation());
      
      testLLVisitor._package="fully.qualified";
      testLLVisitor._file = new File("testFiles/fully/qualified/Fake.dj0");
      testLLVisitor._importedFiles.addLast("fully.qualified.Woah");
      SymbolData sd2 = new SymbolData("fully.qualified.Woah");
      sd2.setIsContinuation(false);
      LanguageLevelConverter.symbolTable.put("fully.qualified.Woah", sd2);
      result = testLLVisitor.getQualifiedSymbolData("fully.qualified.Woah", SourceInfo.NONE, true, false, true);
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
//                         _llv.getQualifiedSymbolData("fully.qualified.Woah", 
//      SourceInfo.NONE, true, true, true));
     
      result = testLLVisitor.getQualifiedSymbolData("fully.qualified.Woah", SourceInfo.NONE, false, false, true);
      
      assertEquals("Should return sd2, unresolved.", sd2, result);
      assertTrue("sd2 should still be unresolved", sd2.isContinuation());
      
      result = testLLVisitor.getQualifiedSymbolData("fully.qualified.Woah", SourceInfo.NONE, false, false, true);
      assertEquals("Should return sd2, now unresolved.", sd2, result);
      assertTrue("sd2 should not be resolved", sd2.isContinuation());
      
      // The following "test" forces the definition of "Woah" to be retrieved from the file system but THERE IS NO CLASS
      // FILE so the file system search returns null!
      result = testLLVisitor.getQualifiedSymbolData("fully.qualified.Woah", SourceInfo.NONE, true, false, true);
      assertEquals("Should return sd2, now resolved.", sd2, result);
      assertFalse("sd2 should now be resolved", sd2.isContinuation());
      
      // Imported Packages
      LanguageLevelConverter.symbolTable.remove("fully.qualified.Woah");
      testLLVisitor.visitedFiles.clear();
      testLLVisitor._file = new File("testFiles/Fake.dj0");
      testLLVisitor._package = "";
      testLLVisitor._importedPackages.addLast("fully.qualified");
      sd2 = new SymbolData("fully.qualified.Woah");
      LanguageLevelConverter.symbolTable.put("fully.qualified.Woah", sd2);
      assertEquals("should find the unresolved symbol data in the symbol table", sd2, 
                   testLLVisitor.getQualifiedSymbolData("fully.qualified.Woah", SourceInfo.NONE, false, false, true));
      assertTrue("should not be a continuation", sd2.isContinuation());
      
      sd2.setIsContinuation(false);
      result = testLLVisitor.getQualifiedSymbolData("fully.qualified.Woah", SourceInfo.NONE, true, false, true);
      assertEquals("should find the resolved symbol data in the symbol table", sd2, result);
      assertFalse("should not be a continuation", sd2.isContinuation());
      
      //test java.lang classes that need to be looked up
      //want to resolve
      SymbolData stringSD = new SymbolData("java.lang.String");
      SymbolData newsd1 = testLLVisitor.getQualifiedSymbolData("java.lang.String", SourceInfo.NONE, true, true, true);
      assertEquals("should have correct name.", stringSD.getName(), newsd1.getName());
      assertFalse("should not be a continuation", newsd1.isContinuation());
      
      // Test ambiguous class name (i.e. it is unqualified, and matches unqualified names in 2 or more packages.
      LanguageLevelConverter.symbolTable.put("random.package.String", new SymbolData("random.package.String"));
//      LanguageLevelConverter.symbolTable.put("java.lang.Object", new SymbolData("java.lang.Object"));
      testLLVisitor._importedPackages.addLast("random.package");
      result = testLLVisitor.getSymbolData("String", SourceInfo.NONE);
      assertEquals("Result should be null", null, result);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "The class name String is ambiguous." + 
                   "  It could be java.lang.String or random.package.String", 
                   errors.get(0).getFirst());
      
      LanguageLevelConverter.symbolTable.remove("random.package.String");
      
    }
    
    public void test_forModifiersAndVisibility() {
      // Test access specifiers.
      testLLVisitor.forModifiersAndVisibility(PUBLIC_MAV);
      testLLVisitor.forModifiersAndVisibility(PROTECTED_MAV);
      testLLVisitor.forModifiersAndVisibility(PRIVATE_MAV);
      testLLVisitor.forModifiersAndVisibility(PACKAGE_MAV);
      
      
      assertEquals("There should be no errors.", 0, errors.size());
      
      // Test "public", "private"
      ModifiersAndVisibility testMav = 
        new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public", "private"});
      testLLVisitor.forModifiersAndVisibility(testMav);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "Illegal combination of modifiers." + 
                   " Can't use private and public together.", errors.get(0).getFirst());
      
      // Test "public", "abstract"
      testMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"public", "abstract"});
      testLLVisitor.forModifiersAndVisibility(testMav);
      assertEquals("Still only one error.", 1, errors.size());
      
      // Test "abstract", "final"
      testMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"abstract", "final"});
      testLLVisitor.forModifiersAndVisibility(testMav);
      assertEquals("There should be two errors.", 2, errors.size());
      assertEquals("The error message should be correct.", "Illegal combination of modifiers." + 
                   " Can't use final and abstract together.", errors.get(1).getFirst());
      
      // Test "final", "abstract"
      testMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"final", "abstract"});
      testLLVisitor.forModifiersAndVisibility(testMav);
      assertEquals("There should still be two errors.", 2, errors.size());  // Generated error is duplicate
      assertEquals("The error message should be correct.", "Illegal combination of modifiers." + 
                   " Can't use final and abstract together.", errors.get(1).getFirst());
      
      // Test "volatile", "final"
      testMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"volatile", "final"});
      testLLVisitor.forModifiersAndVisibility(testMav);
      assertEquals("There should be three errors.", 3, errors.size());  // Generated one new error
      assertEquals("The error message should be correct.", "Illegal combination of modifiers." + 
                   " Can't use final and volatile together.", errors.get(2).getFirst());
      
      // Test "static", "final", "static"
      testMav = new ModifiersAndVisibility(SourceInfo.NONE, new String[] {"static", "final", "static"});
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
      SymbolData obj = LanguageLevelConverter.symbolTable.get("java.lang.Object");
//      obj.setIsContinuation(false);
//      LanguageLevelConverter.symbolTable.put("java.lang.Object", obj);
      
      ClassDef cd = 
        new ClassDef(SourceInfo.NONE, PUBLIC_MAV, new Word(SourceInfo.NONE, "Awesome"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      SymbolData sd = new SymbolData("Awesome"); /**Create a continuation and store it in table.*/
      sd.setSuperClass(LanguageLevelConverter.symbolTable.get("java.lang.Object"));
      LanguageLevelConverter.symbolTable.put("Awesome", sd);
      SymbolData result = testLLVisitor.defineSymbolData(cd, "Awesome");
      assertFalse("result should not be a continuation.", result.isContinuation());
      assertFalse("sd should also no longer be a continuation.", sd.isContinuation());
      assertEquals("result and sd should be equal.", sd, result);
      
//      /**Hierarchy should be empty at the end.*/
//      assertEquals("hierarchy should be empty", 0, _hierarchy.size());
      
      /**Check that if the class is already defined, an appropriate error is thrown.*/
      assertEquals("Should return null, because it is already in the SymbolTable.", null, 
                   testLLVisitor.defineSymbolData(cd, "Awesome"));
      assertEquals("Length of errors should now be 1.", 1, errors.size());
      assertEquals("Error message should be correct.", "The class or interface Awesome has already been defined.", 
                   errors.get(0).getFirst());
//      assertEquals("hierarchy should be empty.", 0, _hierarchy.size());
      
    }
    
    public void test_variableDeclaration2VariableData() {
      VariableDeclarator[] d1 = {
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new PrimitiveType(SourceInfo.NONE, "int"), 
                                            new Word(SourceInfo.NONE, "i")) };
      VariableDeclaration vd1 = new VariableDeclaration(SourceInfo.NONE,PUBLIC_MAV, d1); 
      VariableData[] vdata1 = { new VariableData("i", PUBLIC_MAV, SymbolData.INT_TYPE, false, _sd1) };
      
      assertTrue("Should properly recognize a basic VariableDeclaration", 
                 arrayEquals(vdata1, testLLVisitor._variableDeclaration2VariableData(vd1, _sd1)));
      
      VariableDeclarator[] d2 = {
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new PrimitiveType(SourceInfo.NONE, "int"), 
                                            new Word(SourceInfo.NONE, "i")), 
        new InitializedVariableDeclarator(SourceInfo.NONE, 
                                          new PrimitiveType(SourceInfo.NONE, "boolean"), 
                                          new Word(SourceInfo.NONE, "b"), 
                                          new BooleanLiteral(SourceInfo.NONE, true)) };
      VariableDeclaration vd2 = new VariableDeclaration(SourceInfo.NONE,PRIVATE_MAV, d2); 
      VariableData bData = new VariableData("b", PRIVATE_MAV, SymbolData.BOOLEAN_TYPE, true, _sd1);
      bData.setHasInitializer(true);
      VariableData[] vdata2 = {new VariableData("i", PRIVATE_MAV, SymbolData.INT_TYPE, false, _sd1),
        bData};
      
      assertTrue("Should properly recognize a more complicated VariableDeclaration", 
                 arrayEquals(vdata2, testLLVisitor._variableDeclaration2VariableData(vd2, _sd1)));
      
      //check that if the type cannot be found, no error is thrown.
      VariableDeclarator[] d3 = { 
        new UninitializedVariableDeclarator(SourceInfo.NONE, 
                                            new ClassOrInterfaceType(SourceInfo.NONE, "LinkedList", new Type[0]), 
                                            new Word(SourceInfo.NONE, "myList"))};
      VariableDeclaration vd3 = new VariableDeclaration(SourceInfo.NONE, PRIVATE_MAV, d3);
      testLLVisitor._variableDeclaration2VariableData(vd3, _sd1);
      assertEquals("There should now be no errors", 0, errors.size());
//     assertEquals("The error message should be correct", "Class or Interface LinkedList not found", 
//                  errors.get(0).getFirst());
      
    }
    
    public void test_addError() {
      LinkedList<Pair<String, JExpressionIF>> e = new LinkedList<Pair<String, JExpressionIF>>();
      
      NullLiteral nl = new NullLiteral(SourceInfo.NONE);
      NullLiteral nl2 = new NullLiteral(SourceInfo.NONE);
      
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
      
      NullLiteral nl = new NullLiteral(SourceInfo.NONE);
      NullLiteral nl2 = new NullLiteral(SourceInfo.NONE);
      
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
        new ClassDef(SourceInfo.NONE, PUBLIC_MAV, 
                     new Word(SourceInfo.NONE, "Awesome"),
                     new TypeParameter[0], 
                     new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                     new ReferenceType[0], 
                     new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      testLLVisitor.forClassDefDoFirst(cd);
      assertEquals("There should be no errors.", 0, errors.size());
      testLLVisitor._importedFiles.addLast(new File("Awesome").getAbsolutePath());
      testLLVisitor.forClassDefDoFirst(cd);
      assertEquals("There should be one error.", 1, errors.size());
      assertEquals("The error message should be correct.", "The class Awesome was already imported.", 
                   errors.get(0).getFirst());
      
      ClassDef cd2 = new ClassDef(SourceInfo.NONE, PRIVATE_MAV, 
                                  new Word(SourceInfo.NONE, "privateClass"),
                                  new TypeParameter[0], 
                                  new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                                  new ReferenceType[0], 
                                  new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      testLLVisitor.forClassDefDoFirst(cd2);
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("The 2nd error message should be correct", "Top level classes cannot be private", 
                   errors.get(1).getFirst());
      
    }
    
    public void testForInterfaceDefDoFirst() {
      InterfaceDef id = new InterfaceDef(SourceInfo.NONE, PUBLIC_MAV, 
                                         new Word(SourceInfo.NONE, "Awesome"),
                                         new TypeParameter[0], new ReferenceType[0], 
                                         new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      testLLVisitor.forInterfaceDefDoFirst(id);
      assertEquals("There should be no errors.", 0, errors.size());
      
      InterfaceDef id2 = new InterfaceDef(SourceInfo.NONE, PRIVATE_MAV, 
                                          new Word(SourceInfo.NONE, "privateinterface"),
                                          new TypeParameter[0], new ReferenceType[0], 
                                          new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      testLLVisitor.forInterfaceDefDoFirst(id2);
      assertEquals("There should be 1 errors", 1, errors.size());
      assertEquals("The error message should be correct", "Top level interfaces cannot be private", 
                   errors.get(0).getFirst());
      
      InterfaceDef id3 = new InterfaceDef(SourceInfo.NONE, FINAL_MAV, 
                                          new Word(SourceInfo.NONE, "finalinterface"),
                                          new TypeParameter[0], new ReferenceType[0], 
                                          new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      testLLVisitor.forInterfaceDefDoFirst(id3);
      assertEquals("There should be 2 errors", 2, errors.size());
      assertEquals("The error message should be correct", "Interfaces cannot be final", errors.get(1).getFirst());      
    }
    
    public void testForInnerInterfaceDefDoFirst() {
      InterfaceDef id = new InterfaceDef(SourceInfo.NONE, PUBLIC_MAV, 
                                         new Word(SourceInfo.NONE, "Awesome"),
                                         new TypeParameter[0], new ReferenceType[0], 
                                         new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      id.visit(testLLVisitor);
      assertEquals("There should be no errors.", 0, errors.size());
      
      InnerInterfaceDef id2 = 
        new InnerInterfaceDef(SourceInfo.NONE, FINAL_MAV, new Word(SourceInfo.NONE, "finalinterface"),
                              new TypeParameter[0], new ReferenceType[0], 
                              new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      id2.visit(testLLVisitor);
      assertEquals("There should be 1 error", 1, errors.size());
      assertEquals("The error message should be correct", "Interfaces cannot be final", errors.get(0).getFirst());   
    }
    
    public void testForPackageStatementOnly() {
      Word[] words = new Word[] {new Word(SourceInfo.NONE, "alpha"),
        new Word(SourceInfo.NONE, "beta")};
      CompoundWord cw = new CompoundWord(SourceInfo.NONE, words);
      PackageStatement ps = new PackageStatement(SourceInfo.NONE, cw);
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
      Word[] words = new Word[] { new Word(SourceInfo.NONE, "alpha"), new Word(SourceInfo.NONE, "beta")};
      CompoundWord cw = new CompoundWord(SourceInfo.NONE, words);
      ClassImportStatement cis = new ClassImportStatement(SourceInfo.NONE, cw);
      SymbolData sd = new SymbolData("alpha.beta");
      testLLVisitor.forClassImportStatementOnly(cis);
      assertTrue("imported files should contain alpha.beta", testLLVisitor._importedFiles.contains("alpha.beta"));
      // continuations should not appear in symbolTable
//      assertEquals("There should be a continuation.", sd, LanguageLevelConverter.symbolTable.get("alpha.beta"));
      assertTrue("It should be in continuations.", testLLVisitor.continuations.containsKey("alpha.beta"));
      
      // Test one that should throw an error: Class has already been imported. alpha.beta should now be in the 
      // symbolTable, and alpha should be in the list of packages, so this will throw an error
      Word[] words2 = new Word[] { new Word(SourceInfo.NONE, "gamma"), new Word(SourceInfo.NONE, "beta")};
      CompoundWord cw2 = new CompoundWord(SourceInfo.NONE, words2);
      ClassImportStatement cis2 = new ClassImportStatement(SourceInfo.NONE, cw2);
      cis2.visit(testLLVisitor);
      
      assertEquals("There should be one error", 1, errors.size());
      assertEquals("The error message should be correct", 
                   "The class beta has already been imported.", 
                   errors.get(0).getFirst());
      
    // Consistent with javac,  the following test no longer throws an error.
//      //Test one that should throw an error: Importing a class from the current package
//      testLLVisitor._package = "myPackage";
//      Word[] words3 =  new Word[] { new Word(SourceInfo.NONE, "myPackage"), new Word(SourceInfo.NONE, "cookie")};
//      CompoundWord cw3 = new CompoundWord(SourceInfo.NONE, words3);
//      ClassImportStatement cis3 = new ClassImportStatement(SourceInfo.NONE, cw3);
//      cis3.visit(testLLVisitor);
//      
//      assertEquals("There should now be 2 errors", 2, errors.size());
//      assertEquals("The second error message should be correct", 
//                   "You do not need to import myPackage.cookie.  It is in your package so it is already visible", 
//                   errors.get(1).getFirst());
    }
    
    public void testForPackageImportStatementOnly() {
      //Test one that works
      Word[] words = new Word[] {new Word(SourceInfo.NONE, "alpha"),
        new Word(SourceInfo.NONE, "beta")};
      CompoundWord cw = new CompoundWord(SourceInfo.NONE, words);
      PackageImportStatement cis = new PackageImportStatement(SourceInfo.NONE, cw);
      SymbolData sd = new SymbolData("alpha.beta");
      testLLVisitor.forPackageImportStatementOnly(cis);
      assertEquals("There should be no errorrs", 0, errors.size());
      assertTrue("Imported Packages should now contain alpha.beta", 
                 testLLVisitor._importedPackages.contains("alpha.beta"));
      
      //Test one that should not throw an error: Importing a subpackage of the current package
      testLLVisitor._package = "myPackage";
      Word[] words3 = new Word[] {new Word(SourceInfo.NONE, "myPackage"), new Word(SourceInfo.NONE, 
                                                                                              "cookie")};
      CompoundWord cw3 = new CompoundWord(SourceInfo.NONE, words3);
      PackageImportStatement pis = new PackageImportStatement(SourceInfo.NONE, cw3);
      pis.visit(testLLVisitor);
      
      assertEquals("There should be no errors", 0, errors.size());
      assertTrue("Imported Packages should now contain myPackage.cookie", 
                 testLLVisitor._importedPackages.contains("myPackage.cookie"));
      
      
      
      //Test one that should throw an error: Importing the current package
      Word[] words2 = new Word[] {new Word(SourceInfo.NONE, "myPackage")};
      CompoundWord cw2 = new CompoundWord(SourceInfo.NONE, words2);
      PackageImportStatement pis2 = new PackageImportStatement(SourceInfo.NONE, cw2);
      pis2.visit(testLLVisitor);
      
      assertEquals("There should now be 1 errors", 1, errors.size());
      assertEquals("The error message should be correct", "You do not need to import package myPackage." + 
                   " It is your package so all public classes in it are already visible.", errors.get(0).getFirst());
      
    }
    
    public void testForSourceFile() {
      ClassDef cd = new ClassDef(SourceInfo.NONE, PUBLIC_MAV, new Word(SourceInfo.NONE, "Awesome"),
                                 new TypeParameter[0], 
                                 new ClassOrInterfaceType(SourceInfo.NONE, "java.lang.Object", new Type[0]), 
                                 new ReferenceType[0], 
                                 new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      ClassDef cd2 = new ClassDef(SourceInfo.NONE, PUBLIC_MAV, new Word(SourceInfo.NONE, "Gnarly"),
                                  new TypeParameter[0], 
                                  new ClassOrInterfaceType(SourceInfo.NONE, "Awesome", new Type[0]), 
                                  new ReferenceType[0], 
                                  new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      InterfaceDef id = new InterfaceDef(SourceInfo.NONE, PUBLIC_MAV, 
                                         new Word(SourceInfo.NONE, "NiftyWords"),
                                         new TypeParameter[0], 
                                         new ReferenceType[0], 
                                         new BracedBody(SourceInfo.NONE, new BodyItemI[0]));
      
      SourceFile sf = new SourceFile(SourceInfo.NONE,
                                     new PackageStatement[0],
                                     new ImportStatement[0],
                                     new TypeDefBase[] {cd, cd2, id});
      testLLVisitor.forSourceFile(sf);
      
      assertTrue("_classesInThisFile should contain the two ClassDefs.", 
                 testLLVisitor._classesInThisFile.contains("Awesome"));
      assertTrue("_classesInThisFile should contain the two ClassDefs.", 
                 testLLVisitor._classesInThisFile.contains("Gnarly"));
      
      assertTrue("_classesInThisFile should contain the InterfaceDef", 
                 testLLVisitor._classesInThisFile.contains("NiftyWords"));
      assertTrue("_classesInThisFile should contain the two ClassDefs.", 
                 testLLVisitor._classesInThisFile.contains("Awesome"));
      assertTrue("_classesInThisFile should contain the two ClassDefs.", 
                 testLLVisitor._classesInThisFile.contains("Gnarly"));
      assertTrue("_classesInThisFile should contain the InterfaceDef", 
                 testLLVisitor._classesInThisFile.contains("NiftyWords"));
      
    }
    
    public void testReferenceType2String() {
      // Try a TypeVariable
      TypeVariable tv = new TypeVariable(SourceInfo.NONE, "T");
      String[] result = testLLVisitor.referenceType2String(new ReferenceType[] { tv });
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("Results should have one String.", 1, result.length);
      assertEquals("The String should be \"T\".", "T", result[0]);
      
      // Try a ClassOrInterfaceType
      ClassOrInterfaceType coit = new ClassOrInterfaceType(SourceInfo.NONE, 
                                                           "MyClass", 
                                                           new Type[] { new TypeVariable(SourceInfo.NONE, "T"),
        new TypeVariable(SourceInfo.NONE, "U")}
      );
      result = testLLVisitor.referenceType2String(new ReferenceType[] { tv, coit });
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("Results should have two Strings.", 2, result.length);
      assertEquals("The first String should be \"T\".", "T", result[0]);
      assertEquals("The second String should be \"MyClass\".", "MyClass", result[1]);
      
      // Try a MemberType
      MemberType mt = new MemberType(SourceInfo.NONE,
                                     "MyClass.MyClass2",
                                     coit,
                                     new ClassOrInterfaceType(SourceInfo.NONE, 
                                                              "MyClass2", 
                                                              new Type[0]));
      result = testLLVisitor.referenceType2String(new ReferenceType[] { mt });
      assertEquals("There should not be any errors.", 0, errors.size());
      assertEquals("Results should have one String.", 1, result.length);
      assertEquals("The first String should be \"MyClass.MyClass2\".", "MyClass.MyClass2", result[0]);
    }
    
    
    public void testExceptionsInSymbolTable() {
            
      // Make sure that exceptions are being added to symbol table 
      ClassOrInterfaceType exceptionType = 
        new ClassOrInterfaceType(SourceInfo.NONE, "java.util.prefs.BackingStoreException", new Type[0]);
      ParenthesizedExpressionList expList = new ParenthesizedExpressionList(SourceInfo.NONE, new Expression[0]);
      
      BracedBody bb = 
        new BracedBody(SourceInfo.NONE, 
                       new BodyItemI[] { new ThrowStatement(SourceInfo.NONE, 
                                                            new SimpleNamedClassInstantiation(SourceInfo.NONE, 
                                                                                              exceptionType, 
                                                                                              expList))});
      bb.visit(testLLVisitor);
      assertNotNull("The SymbolTable should have java.util.prefs.BackingStoreException", 
                  LanguageLevelConverter.symbolTable.get("java.util.prefs.BackingStoreException"));
      
    }
    
    public void testShouldBreak() {
      //shift assignment expressions:
      LeftShiftAssignmentExpression shift1 = 
        new LeftShiftAssignmentExpression(SourceInfo.NONE, new NullLiteral(SourceInfo.NONE), 
                                          new NullLiteral(SourceInfo.NONE));
      RightUnsignedShiftAssignmentExpression shift2 = 
        new RightUnsignedShiftAssignmentExpression(SourceInfo.NONE, 
                                                   new NullLiteral(SourceInfo.NONE), 
                                                   new NullLiteral(SourceInfo.NONE));
      RightSignedShiftAssignmentExpression shift3 = 
        new RightSignedShiftAssignmentExpression(SourceInfo.NONE, 
                                                 new NullLiteral(SourceInfo.NONE), 
                                                 new NullLiteral(SourceInfo.NONE));
      
      
      shift1.visit(testLLVisitor);
      assertEquals("Should be no errors", 0, errors.size());
//      assertEquals("error message should be correct", 
//                   "Shift assignment operators cannot be used at any language level",
//                   errors.getLast().getFirst());
      
      shift2.visit(testLLVisitor);
      assertEquals("Should be no errors", 0, errors.size());
//      assertEquals("error message should be correct", 
//                   "Shift assignment operators cannot be used at any language level",
//                   errors.getLast().getFirst());
      
      shift3.visit(testLLVisitor);
      assertEquals("Should be no errors", 0, errors.size());
//      assertEquals("error message should be correct", 
//                   "Shift assignment operators cannot be used at any language level",
//                   errors.getLast().getFirst());
    
      //BitwiseAssignmentExpressions
      BitwiseAndAssignmentExpression bit1 = 
        new BitwiseAndAssignmentExpression(SourceInfo.NONE, new NullLiteral(SourceInfo.NONE), 
                                           new NullLiteral(SourceInfo.NONE));
      BitwiseOrAssignmentExpression bit2 = 
        new BitwiseOrAssignmentExpression(SourceInfo.NONE, new NullLiteral(SourceInfo.NONE), 
                                          new NullLiteral(SourceInfo.NONE));
      BitwiseXorAssignmentExpression bit3 = 
        new BitwiseXorAssignmentExpression(SourceInfo.NONE, new NullLiteral(SourceInfo.NONE), 
                                           new NullLiteral(SourceInfo.NONE));
      
      bit1.visit(testLLVisitor);
      assertEquals("Should be no errors", 0, errors.size());
//      assertEquals("error message should be correct", "Bitwise operators cannot be used at any language level", 
//                   errors.getLast().getFirst());
      
      bit2.visit(testLLVisitor);
      assertEquals("Should be no errors", 0, errors.size());
//      assertEquals("error message should be correct", "Bitwise operators cannot be used at any language level", 
//                   errors.getLast().getFirst());
      
      bit3.visit(testLLVisitor);
      assertEquals("Should be no errors", 0, errors.size());
//      assertEquals("error message should be correct", "Bitwise operators cannot be used at any language level", 
//                   errors.getLast().getFirst());
      
      //BitwiseExpressions
      BitwiseAndExpression bit4 = 
        new BitwiseAndExpression(SourceInfo.NONE, new NullLiteral(SourceInfo.NONE), 
                                 new NullLiteral(SourceInfo.NONE));
      BitwiseOrExpression bit5 = 
        new BitwiseOrExpression(SourceInfo.NONE, new NullLiteral(SourceInfo.NONE), 
                                new NullLiteral(SourceInfo.NONE));
      BitwiseXorExpression bit6 = 
        new BitwiseXorExpression(SourceInfo.NONE, new NullLiteral(SourceInfo.NONE), 
                                 new NullLiteral(SourceInfo.NONE));
      BitwiseNotExpression bit7 = 
        new BitwiseNotExpression(SourceInfo.NONE, new NullLiteral(SourceInfo.NONE));
      
      
      bit4.visit(testLLVisitor);
      assertEquals("Should be no errors", 0, errors.size());
//      assertEquals("error message should be correct", 
//                   "Bitwise and expressions cannot be used at any language level." + 
//                   "  Perhaps you meant to compare two values using regular and (&&)", 
//                   errors.getLast().getFirst());
      
      bit5.visit(testLLVisitor);
      assertEquals("Should be no errors", 0, errors.size());
//      assertEquals("error message should be correct", 
//                   "Bitwise or expressions cannot be used in the functional level." +
//                   "  Perhaps you meant to compare two values using regular or (||)", 
//                   errors.getLast().getFirst());
      
      bit6.visit(testLLVisitor);
      assertEquals("Should be no errors", 0, errors.size());
//      assertEquals("error message should be correct", "Bitwise xor expressions cannot be used at any language level", 
//                   errors.getLast().getFirst());
      
      bit7.visit(testLLVisitor);
      assertEquals("Should be no errors", 0, errors.size());
//      assertEquals("error message should be correct", 
//                   "Bitwise not expressions cannot be used at any language level." +
//                   "  Perhaps you meant to negate this value using regular not (!)", 
//                   errors.getLast().getFirst());
      
      //shift binary expressions
      LeftShiftExpression shift4 = 
        new LeftShiftExpression(SourceInfo.NONE, new NullLiteral(SourceInfo.NONE), 
                                new NullLiteral(SourceInfo.NONE));
      RightUnsignedShiftExpression shift5 = 
        new RightUnsignedShiftExpression(SourceInfo.NONE, new NullLiteral(SourceInfo.NONE), 
                                         new NullLiteral(SourceInfo.NONE));
      RightSignedShiftExpression shift6 = 
        new RightSignedShiftExpression(SourceInfo.NONE, new NullLiteral(SourceInfo.NONE), 
                                       new NullLiteral(SourceInfo.NONE));
      
      shift4.visit(testLLVisitor);
      assertEquals("Should be no errors", 0, errors.size());
//      assertEquals("error message should be correct", "Bit shifting operators cannot be used at any language level", 
//                   errors.getLast().getFirst());
      
      shift5.visit(testLLVisitor);
      assertEquals("Should be no errors", 0, errors.size());
//      assertEquals("error message should be correct", "Bit shifting operators cannot be used at any language level", 
//                   errors.getLast().getFirst());
      
      shift6.visit(testLLVisitor);
      assertEquals("Should be no errors", 0, errors.size());
//      assertEquals("error message should be correct", "Bit shifting operators cannot be used at any language level", 
//                   errors.getLast().getFirst());
      
      //empty expression
      EmptyExpression e = new EmptyExpression(SourceInfo.NONE);
      e.visit(testLLVisitor);
      assertEquals("Should be 1 error", 1, errors.size());
      assertEquals("Error message should be correct", "You appear to be missing an expression here", 
                   errors.getLast().getFirst());
      
      //noop expression
      NoOpExpression noop = 
        new NoOpExpression(SourceInfo.NONE, new NullLiteral(SourceInfo.NONE), 
                           new NullLiteral(SourceInfo.NONE));
      noop.visit(testLLVisitor);
      assertEquals("Should be 2 errors", 2, errors.size());
      assertEquals("Error message should be correct", "You are missing a binary operator here", 
                   errors.getLast().getFirst());
    }
    
    public void testIsConstructor() {
      MethodData constr = 
        new MethodData("monkey", PUBLIC_MAV, new TypeParameter[0], _sd1, new VariableData[0], new String[0], _sd1, 
                       new NullLiteral(SourceInfo.NONE));
      MethodData notRightOuter = 
        new MethodData("monkey", PUBLIC_MAV, new TypeParameter[0], _sd1, new VariableData[0], new String[0], _sd2, 
                       new NullLiteral(SourceInfo.NONE));
      _sd2.setOuterData(_sd1);
      _sd1.addInnerClass(_sd2);
      MethodData notRightName = 
        new MethodData("chimp", PUBLIC_MAV, new TypeParameter[0], _sd1, new VariableData[0], new String[0], _sd1, 
                       new NullLiteral(SourceInfo.NONE));
      MethodData notRightReturnType = 
        new MethodData("monkey", PUBLIC_MAV, new TypeParameter[0], _sd2, new VariableData[0], new String[0], _sd1, 
                       new NullLiteral(SourceInfo.NONE));
      
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
