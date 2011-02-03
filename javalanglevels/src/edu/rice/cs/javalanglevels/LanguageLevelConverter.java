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

import java.lang.reflect.Modifier;
import java.io.*;
import java.util.*;

import org.objectweb.asm.*;

import edu.rice.cs.javalanglevels.*;
import edu.rice.cs.javalanglevels.parser.*;
import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.javalanglevels.util.Log;
import edu.rice.cs.javalanglevels.util.Utilities;

import edu.rice.cs.plt.iter.*;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.reflect.EmptyClassLoader;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.reflect.PathClassLoader;

/** An instance of this class converts a language level file to a .java file of the same name by first visiting the 
  * file to error-check it, and then by augmenting the file.  This class is tested at the top level in the
  * AdvancedLevelTest, ElementaryLevelTest, and IntermediateLevelTest.
  */
public class LanguageLevelConverter {
  
  public static final Log _log = new Log("LLConverter.txt", false);
  
  /** Hashtable for a shared symbolTable.  Since this field is static, only one instance of
    * LanguageLevelConverter should exist at a time.  If we create a
    * LanguageLevelConverter instance for each translation, we must drop the static attribute. */
  public static final Symboltable symbolTable = new Symboltable();
  
  public static Options OPT = Options.DEFAULT;
  
  /* For Corky's version: set this to false */
  private static final boolean SAFE_SUPPORT_CODE = false;
  public static final int INPUT_BUFFER_SIZE = 8192;  // This reportedly is the current default in the JDK.
  
  /**Number of line number mappings (from dj* to java) per line. */
  public static final int LINE_NUM_MAPPINGS_PER_LINE = 8;
  
  /** Stores all the SymbolDatas (and corresponding visitors) created as in course of conversion.  If we create a
    * LanguageLevelConverter instance for each translation, we must drop the static attribute. */
  public static final Hashtable<SymbolData, LanguageLevelVisitor> _newSDs = 
    new Hashtable<SymbolData, LanguageLevelVisitor>();
  
  /**Holds any parse exceptions that are encountered*/
  private LinkedList<JExprParseException> _parseExceptions = new LinkedList<JExprParseException>();
  
  /**Holds any visitor exceptions that are encountered*/
  private LinkedList<Pair<String, JExpressionIF>> _visitorErrors = new LinkedList<Pair<String, JExpressionIF>>();
  
  /* Relying on default constructor. */
  
  /** Ensures that the symbol table contains essential symbols.  Should be done in the symbol table class. */
  public static void loadSymbolTable() {

    if (symbolTable.get("java.lang.Object") == null)    _classFile2SymbolData("java.lang.Object");
    if (symbolTable.get("java.lang.Integer") == null)   _classFile2SymbolData("java.lang.Integer");
    if (symbolTable.get("java.lang.Double") == null)    _classFile2SymbolData("java.lang.Double");
    if (symbolTable.get("java.lang.Boolean") == null)   _classFile2SymbolData("java.lang.Boolean");
    if (symbolTable.get("java.lang.Long") == null)      _classFile2SymbolData("java.lang.Long");
    if (symbolTable.get("java.lang.Byte") == null)      _classFile2SymbolData("java.lang.Byte");
    if (symbolTable.get("java.lang.Short") == null)     _classFile2SymbolData("java.lang.Short");
    if (symbolTable.get("java.lang.Float") == null)     _classFile2SymbolData("java.lang.Float");
    if (symbolTable.get("java.lang.Character") == null) _classFile2SymbolData("java.lang.Character");
    
    if (symbolTable.get("java.lang.String") == null)    _classFile2SymbolData("java.lang.String");

    SymbolData objectSD = symbolTable.get("java.lang.Object");   
    SymbolData integerSD = symbolTable.get("java.lang.Integer");
    assert objectSD != null && integerSD != null;
    assert integerSD.isAssignableTo(objectSD, JavaVersion.JAVA_5);
    assert SymbolData.INT_TYPE.isAssignableTo(objectSD, JavaVersion.JAVA_5);
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
  
  /** Creates a ModifiersAndVisibility from the provided modifier flags. */
  private static ModifiersAndVisibility _createMav(int flags) {
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
    return new ModifiersAndVisibility(SourceInfo.NONE, strings.toArray(new String[strings.size()]));
  }
  
  /** Defines library classes assuming they are available to the PathClassLoader. */
  public static SymbolData _classFile2SymbolData(String qualifiedClassName) { 
    return _classFile2SymbolData(qualifiedClassName, null);
  }
  
  /** Uses the ASM class reader to read the class file corresponding to the class in the specified directory, and uses
    * the information from ASM to build a SymbolData corresponding to the class.  Ensures that the returned SymbolData
    * (if any) is inserted in the symbolTable.
    * @param qualifiedClassName  The fully qualified class name of the class we are looking up
    * @param programRoot  The directory where the class is located.
    * @return The SymbolData for the class file if the class file was found; null otherwise.
    */
  public static SymbolData _classFile2SymbolData(final String qualifiedClassName, final String programRoot) {
 
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
    
    // Class file found; create the symbol table entry
    final SymbolData sd;
//    if (qualifiedClassName.equals("java.lang.Object")) 
//      System.err.println("***SHOUT*** java.lang.Object is being added to symbolTable from class file");
    SymbolData sdLookup = LanguageLevelConverter.symbolTable.get(qualifiedClassName); 
    if (sdLookup == null)  { // create a continuation for sd
      sd = new SymbolData(qualifiedClassName); 
      LanguageLevelConverter.symbolTable.put(qualifiedClassName, sd);
    }
    else { sd = sdLookup; }
    
    assert LanguageLevelConverter.symbolTable.contains(sd);
    
//    if (! sd.isContinuation()) { System.err.println("***NOTE*** Non-continuation " + sd + " resolved from class file"); }
    // make it be a non-continuation, since we are filling it in
    sd.setIsContinuation(false);
    
    final SourceInfo lookupInfo = SourceInfo.make(qualifiedClassName);
    final String unqualifiedClassName = LanguageLevelVisitor.getUnqualifiedClassName(qualifiedClassName);
    
    // TODO !!! Use classFile2SymbolData directly.  Should class files have their supertypes defined anywhere but in 
    // class files?
    ClassVisitor extractData = new ClassVisitor() {
      
      public void visit(int version, int access, String name, String sig, String sup, String[] interfaces) {
        sd.setMav(_createMav(access));
        sd.setInterface(Modifier.isInterface(access));
        
        int slash = name.lastIndexOf('/');
        if (slash == -1) { sd.setPackage(""); }
        else { sd.setPackage(name.substring(0, slash).replace('/', '.')); }
        
        if (sup == null) { sd.clearSuperClass(); }
        else { 
          String superClassName = sup.replace('/', '.');
//          if (name.equals("Integer")) System.err.println("The superclass of Integer is " + superClassName);
          SymbolData superSD = LanguageLevelConverter.symbolTable.get(superClassName);
          if (superSD == null || superSD.isContinuation()) {
            superSD = getSymbolDataForClassFile(superClassName, programRoot);
            if (superSD != null) LanguageLevelConverter.symbolTable.put(superClassName, superSD);
          }
          sd.setSuperClass(superSD); 
        }
        
        if (interfaces != null) {
//          if (qualifiedClassName.equals("java.lang.RuntimeException"))
//            System.err.println("interfaces for java.lang.RuntimeException: " + Arrays.toString(interfaces));
          for (String iName : interfaces) {
            String interfaceName = iName.replace('/', '.');
            SymbolData superInterface = LanguageLevelConverter.symbolTable.get(interfaceName);
            if (superInterface == null || superInterface.isContinuation()) {
              superInterface = getSymbolDataForClassFile(interfaceName, programRoot);
              if (superInterface != null) LanguageLevelConverter.symbolTable.put(interfaceName, superInterface);
            }
            if (superInterface != null) sd.addInterface(superInterface);
          }
//          if (qualifiedClassName.equals("java.lang.RuntimeException"))
//            System.err.println("Recorded interfaces = " + sd.getInterfaces());
        }
      }
      
      public FieldVisitor visitField(int access, String name, String desc, String sig, Object value) {
        /* Private fields cannot be ignored because they are used in code augmentation for generating constructors,
         * equals, and hashCode. */
        String typeString = org.objectweb.asm.Type.getType(desc).getClassName();
        SymbolData type = getSymbolDataForClassFile(typeString, programRoot);
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
          returnType = getSymbolDataForClassFile(returnString, programRoot);
          valid = valid && (returnType != null);
        }
        org.objectweb.asm.Type[] argTypes = org.objectweb.asm.Type.getArgumentTypes(desc);
        VariableData[] args = new VariableData[argTypes.length]; 
        for (int i = 0; i < argTypes.length; i++) {
          SymbolData argType = getSymbolDataForClassFile(argTypes[i].getClassName(), programRoot);
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
//    System.err.println("####### Loading file system class " + qualifiedClassName + " and all of its unloaded supertypes");
    reader.accept(extractData, ClassReader.SKIP_CODE);
//    System.err.println("####### Finished loading " + qualifiedClassName);
    
    // Remove the class from the list of continuations to resolve.
    Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations = 
      LanguageLevelVisitor.continuations;
    if (continuations != null) continuations.remove(qualifiedClassName);  // UGH!  Why is this necessary?
//    if (qualifiedClassName.equals("java.lang.Throwable")) 
//      System.err.println("***Package name for constructed symbol 'java.lang.Thowable' is: " + sd.getPackage());
    return sd;
  }
  
  /** Resolves a reference to a class embedded in a class file.  Assumes the class name is fully qualified.  
    * If the program is well-formed, the referenced class will either be:
    * (i)   primitive;
    * (ii)  already present in the symbol table; or
    * (iii) a reference to another class defined by a class file.
    * If the result is null, gives an error.  Removes the symbol data from the continuations list, and return it.
    * @return the result of trying to resolve className.
    */
  public static SymbolData getSymbolDataForClassFile(String className, String programRoot) {
 
    // Check for primitive types. 
    SymbolData sd = _getPrimitiveSymbolData(className);
    if (sd != null) { return sd; }
    
    // Check for already defined types
    SymbolData existingSD = LanguageLevelConverter.symbolTable.get(className);
    if (existingSD != null && ! existingSD.isContinuation()) return existingSD;
     
    return _classFile2SymbolData(className, programRoot);  // resolve it by reading a class file
  }
  
  /** Checks to see if the provided class name is the name of a primative type, and if so,
    * returns the corresponding SymbolData.  Otherwise, returns null.
    */
  public static SymbolData _getPrimitiveSymbolData(String className) {
    if (className.equals("boolean"))     return SymbolData.BOOLEAN_TYPE;
    else if (className.equals("char"))   return SymbolData.CHAR_TYPE;
    else if (className.equals("byte"))   return SymbolData.BYTE_TYPE;
    else if (className.equals("short"))  return SymbolData.SHORT_TYPE;
    else if (className.equals("int"))    return SymbolData.INT_TYPE;
    else if (className.equals("long"))   return SymbolData.LONG_TYPE;
    else if (className.equals("float"))  return SymbolData.FLOAT_TYPE;
    else if (className.equals("double")) return SymbolData.DOUBLE_TYPE;
    else if (className.equals("void"))   return SymbolData.VOID_TYPE;
    else if (className.equals("null"))   return SymbolData.NULL_TYPE;
//    System.err.println("***** className " + className + " did not match primitive names");
    return null;
  }
  
  /***Add the parse exception to the list of parse exceptions*/
  private void _addParseException(ParseException pe) {
    JExprParseException jpe;
    if (pe instanceof JExprParseException) { jpe = (JExprParseException) pe; }
    else { jpe = new JExprParseException(pe); }
    _parseExceptions.addLast(jpe);
  }
  
  /** Add the visitor error to the list of errors, */
  private void _addVisitorError(Pair<String, JExpressionIF> ve) { _visitorErrors.addLast(ve); }
  
  
  /** Parse, Visit, Type Check, and Convert any language level files in the array of files. */
  public Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>>
    convert(File[] files, Options options) {
    Map<File,Set<String>> sourceToTopLevelClassMap = new Hashtable<File,Set<String>>();
    return convert(files, options, sourceToTopLevelClassMap);
  }
  
  /** Parse, visit, type check, and convert any language level files (and unconverted LL files they reference) in files/
    * @param files  The array of files to process.
    * @param sourceToTopLevelClassMap  A map from source files to names of top-level classes created from that source file;
    *        it is initially empty and subsequently filled out by this method */
  // "Visit" is an extremely vague notion; I presume it means construct a symbol table for the file.
  public Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>>
    convert(File[] files, Options options, Map<File,Set<String>> sourceToTopLevelClassMap) {
    
//    System.err.println("LanguageLevelConverter.convert called on files:  " + Arrays.toString(files));
    _log.log("LanguageLevelConverter.convert called on files:  " + Arrays.toString(files));
    OPT = options;
//    System.err.println("Options = " + options);
    assert symbolTable != null;
    symbolTable.clear();
    _newSDs.clear();
    
    /**initialize so we don't get null pointer exception*/
    // We need a LinkedList for errors to be shared by the visitors to each file.
    LinkedList<Pair<String, JExpressionIF>> languageLevelVisitorErrors = new LinkedList<Pair<String, JExpressionIF>>();
    
    // Keep track of the continuations to resolve.  A continuation is a fully qualified type name with no attributes.
    Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>> continuations = 
      new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>();
    
    // Keep track of the fixups to peform.  A fixup is a command that must be executed (after all continuations are
    // resolved to fully update the symbolTable.
    LinkedList<Command> fixUps = new LinkedList<Command>();
    
    // And a linked list to share visited files.
    LinkedList<Pair<LanguageLevelVisitor, SourceFile>> languageLevelVisitedFiles =  
      new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>();
    
    /* We are doing two passes on the files, and the second pass needs the first's corresponding
     SourceFile and LanguageLevelVisitor so we'll keep them around in a Hashtable. */
    Hashtable<Integer, Pair<SourceFile, LanguageLevelVisitor>> mediator = 
      new Hashtable<Integer, Pair<SourceFile, LanguageLevelVisitor>>();
    
//    /* The visitedFiles returned by any pass may include another file which is already scheduled for compilation.
//     In this case, we don't want to reparse, or perform either the first pass or the type check since it has
//     already been done.  (An error is thrown if we do since it thinks the class has already been defined). */
//    // WHAT visitedFiles are RETURNED?  HOW?  The return type is a Pair containing NO files!  I SMELL GLOBAL VARIABLES!
//    LinkedList<File> filesNotToCheck = new LinkedList<File>();
    
//    /* The number of files to compile may change if one file references another one.
//     We don't want to visit these newly referenced files because they've already
//     been visited. */
//    // WHAT DOES VISIT MEAN?
//    int originalNumOfFiles = files.length;
    
    /* Find the files in the File[] array files that are LL, advanced or Full Java files. Do the parsing and conformance 
     * checking passes first for ALL files before proceeding to type-checking (for LL files) and code augmentation.  
     * Type-checking and class augmentation require a complete symbol table. */
    
    /* Maintains the files we visit along with their visitors (for type checking and augmentation steps). */
    LinkedList<Triple<LanguageLevelVisitor, SourceFile, File>> visited = 
      new LinkedList<Triple<LanguageLevelVisitor, SourceFile, File>>();
    
    /* Maintains the files to be augmented along with their visitors (for augmentation step). */
    LinkedList<Triple<LanguageLevelVisitor, SourceFile, File>> toAugment = 
      new LinkedList<Triple<LanguageLevelVisitor, SourceFile, File>>();
    
    /* Maintains the list of advanced files, which are converted to .java files and treated like .java files. */
    LinkedList<File> advanced = new LinkedList<File>();
    
    /* Maintains the list of Full Java files, which are parsed for symbols and checked for gross errors. */
    LinkedList<File> javaFiles = new LinkedList<File>();
    
    /** First pass: classfication and conformance checking */
    for (File f : files) {    
      try {
//        if (filesNotToCheck.contains(f)) continue;  // Detects equal File objects
        
        // Check for a null file
        BufferedReader tempBr = new BufferedReader(new FileReader(f));
        String firstLine = tempBr.readLine();
        tempBr.close();
        if (firstLine == null) continue;
        
        if (isAdvancedFile(f))  advanced.addLast(f);
        else if (isFullJavaFile(f)) javaFiles.addLast(f);
        
        if (isJavaFile(f)) {  /* a .dj0, .dj1, .dj2,, .dj, or .java file */
          System.out.flush();
          SourceFile sf;
          JExprParser jep = new JExprParser(f);
          try { 
//            System.err.println("Parsing " + f);
            _log.log("Parsing " + f);
            sf = jep.SourceFile();
//            System.err.println("Completed parsing " + f);
            final Set<String> topLevelClasses = new HashSet<String>();
            for (TypeDefBase t: sf.getTypes()) {
              t.visit(new JExpressionIFAbstractVisitor<Void>() {
                public Void forClassDef(ClassDef that) { topLevelClasses.add(that.getName().getText()); return null; }
                public Void defaultCase(JExpressionIF that) { return null; }
              });
            }
            sourceToTopLevelClassMap.put(f, topLevelClasses);
          } 
          catch (ParseException pe) {
            // If there is a ParseException, go to next file.
            _addParseException(pe);
            _log.log("GENERATED ParseException for file " + f);
            continue;
          }
          
          LinkedList<String> importedPackageBase = new LinkedList<String>();
          importedPackageBase.add("java.lang");
            
          // Now create a LanguageLevelVisitor to do the first pass over the file.
          LanguageLevelVisitor llv;
          if (isLanguageLevelFile(f)) {
            llv = new IntermediateVisitor(f,
                                          importedPackageBase,
                                          new LinkedList<Pair<String, JExpressionIF>>(),
                                          new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(),
                                          new LinkedList<Command>(),
                                          languageLevelVisitedFiles);
          }
          else {
            assert isAdvancedFile(f) || isFullJavaFile(f);
            llv = new FullJavaVisitor(f,
                                      importedPackageBase,
                                      new LinkedList<Pair<String, JExpressionIF>>(),
                                      new Hashtable<String, Triple<SourceInfo, LanguageLevelVisitor, SymbolData>>(), 
                                      new LinkedList<Command>(),
                                      languageLevelVisitedFiles);
          }
          
          // Conformance checking pass
          sf.visit(llv);
          _log.log("\nDUMPING SYMBOLTABLE AFTER PHASE 1 PROCESSING OF " + f + "\n\n" + symbolTable + "\n");
          visited.add(new Triple<LanguageLevelVisitor, SourceFile, File>(llv, sf, f));
          _log.log("\nCONTINUATIONS AFTER PHASE 1 PROCESSING OF " + f + "\n\n" + llv.continuations + "\n");
          _log.log("\nERRORS AFTER PHASE 1 PROCESSING OF " + f + "\n\n" + llv.errors + "\n");
//          if (! llv.errors.isEmpty()) Utilities.show("errors after " + f + "\n" + llv.errors);
          //add the continuations to the hash table.
          continuations.putAll(llv.continuations);
          fixUps.addAll(llv.fixUps);
          languageLevelVisitorErrors.addAll(llv.errors);
        }
      }
      catch (IOException ioe) {
        // The NullLiteral is a hack to get a JExpression with the correct SourceInfo inside.
        _addVisitorError(new Pair<String, JExpressionIF>(ioe.getMessage(), new NullLiteral(SourceInfo.NONE)));
      }
    }

    /* Resolve continuations created in conformance pass and log any generated errors.  TODO: refactor use of 
     * getSymbolData with a flag; create a new method for resolving continuations.  Use a common helper.  */
    LanguageLevelVisitor.errors = new LinkedList<Pair<String, JExpressionIF>>(); //clear out error list
    
    _log.log("\nDUMPING SYMBOLTABLE BEFORE CONTINUATION RESOLUTION\n\n" + symbolTable + "\n");
//    System.err.println("Resolving continuations " + continuations);
    _log.log("Resolving continuations: " + continuations + "\n");
    while (! continuations.isEmpty()) {
      Enumeration<String> en = continuations.keys();
      
      while (en.hasMoreElements()) {
        String name = en.nextElement();   // name referenced in continuation
        Triple<SourceInfo, LanguageLevelVisitor, SymbolData> triple = continuations.remove(name);
        /* Resolving this continuation updates all references to symbol of this continuation. */
        SourceInfo si = triple.getFirst();   // SourceInfo in continuation
        LanguageLevelVisitor llv = triple.getSecond();
        SymbolData sD = triple.getThird();  // SymbolData in continuation
//        System.err.println("Attempting to resolve " + name + " with source info " + si + " and SymbolData " + sD);
        SymbolData newSD = sD.resolve(si, llv);
        _log.log("Attempting to resolve " + sD + " with source info " + si + "\n  Result = " + newSD);
//        System.err.println("Result = " + newSD);
        if (newSD == null) {
//          if (name.equals("listFW.IList")) {
//            System.err.println("Cannot resolve listFW.List\nsymbolTable is:\n" + symbolTable);
//          }
          LanguageLevelVisitor.errors.add(new Pair<String, JExpressionIF>("Converter could not resolve " + name,                                                                    new NullLiteral(triple.getFirst())));
        }
      }
    }
    
    _log.log("\nDUMPING SYMBOLTABLE AFTER PASS 1\n\n" + symbolTable + "\n");
    
        
    // Execute fixups; must be done before creating constructors because some symbols required for constructor
    // definition may be missing
    for (Command c: fixUps) { c.execute(); }
    
    /* Create any constructors identified in the conformance pass and log any errors encountered. */
    Enumeration<SymbolData> keys = _newSDs.keys();
    while (keys.hasMoreElements()) {
      SymbolData key = keys.nextElement();
      LanguageLevelVisitor sdlv = _newSDs.get(key);   // Can return null because of silly side effects!
      if (sdlv != null) {
//        System.err.println("*** Creating constructor for " + key);
        sdlv.createConstructor(key);  // Bug fix is a kludge! Deletes (key,sdlv) from _newSDs!
      }
    } 
    
//    assert _newSDs.isEmpty();
    /* Add any errors that accumulated during the continuation resolving/constructor generation. */
    
    languageLevelVisitorErrors.addAll(LanguageLevelVisitor.errors); 
    
    // At this point, there should be no continuations and visitedFiles should be completely populated.
    
    /* If there are no errors, perform type-checking on LL files. */
    if (languageLevelVisitorErrors.size() > 0)  _visitorErrors.addAll(languageLevelVisitorErrors);
    else  {
      /* Perform type-checking on visited LL files and build list of files toAugment. */
      for (Triple<LanguageLevelVisitor, SourceFile, File> triple: visited) {
        
        LanguageLevelVisitor llv = triple.getFirst();
        SourceFile sf = triple.getSecond();
        File f = triple.getThird();
        
        if (isAdvancedFile(f)) { toAugment.addLast(triple); }
        else if (isLanguageLevelFile(f)) {
          // This is a hack to get around the following problem.  Basically, when we autobox in isAssignableTo in 
          // SymbolData, we look through the object hierarchy for the primitive.  If its corresponding
          // boxed object isn't in the symboltable, we're in trouble.  So, for those special cases, go ahead and make 
          // sure the object types are in the symbol table before we start type checking
          
          //Before you type check, make sure that all boxed types of primitives are in the symbol table
          
          if (symbolTable.get("java.lang.Integer") == null) llv.getSymbolData("java.lang.Integer", SourceInfo.NONE);
          if (symbolTable.get("java.lang.Double") == null)  llv.getSymbolData("java.lang.Double", SourceInfo.NONE);
          if (symbolTable.get("java.lang.Boolean") == null) llv.getSymbolData("java.lang.Boolean", SourceInfo.NONE);
          if (symbolTable.get("java.lang.Long") == null)    llv.getSymbolData("java.lang.Long", SourceInfo.NONE);
          if (symbolTable.get("java.lang.Byte") == null)    llv.getSymbolData("java.lang.Byte", SourceInfo.NONE);
          if (symbolTable.get("java.lang.Short") == null)   llv.getSymbolData("java.lang.Short", SourceInfo.NONE);
          if (symbolTable.get("java.lang.Float") == null)   llv.getSymbolData("java.lang.Float", SourceInfo.NONE);
          if (symbolTable.get("java.lang.Character") == null) 
            llv.getSymbolData("java.lang.Character", SourceInfo.NONE);
          
//          System.err.println("**** Type checking " + f);
          // Type check.
          TypeChecker btc = 
            new TypeChecker(llv._file, llv._package, llv.errors, symbolTable, llv._importedFiles, llv._importedPackages);
//        System.err.println("Visiting source file " + sf.getSourceInfo ());
          sf.visit(btc);
          toAugment.addLast(triple);
          if (btc.errors.size() > 0) _visitorErrors.addAll(btc.errors);
        }
      }
      
//      /* Set up code augmentation.  We will NOT try to augment files unless they appear in the visited List and are not
//       * already Full Java (.java) files.  Unlisted LL files cannot be found reliably during type checking because there
//       * is no naming convention that tells the type checker what files to look for. */
      
//      Iterator<Pair<LanguageLevelVisitor, SourceFile>> iter = visited.iterator();
//      LinkedList<File> filesToAugment = new LinkedList<File>();
//      for (int ind = 0; iter.hasNext(); ind++) { // Note unusual loop termination condition; iter, ind in lock step
//        Pair<LanguageLevelVisitor, SourceFile> currPair = iter.next();
//        File fileToAdd = currPair.getFirst()._file;
//        
//        if (isLanguageLevelFile(fileToAdd)) { // fileToAdd is a visited LL file
////          Utilities.show(fileToAdd + " is a LL file for augmentation");
//          filesToAugment.addLast(fileToAdd);
//          mediator.put(ind, new Pair<SourceFile, LanguageLevelVisitor>(currPair.getSecond(), currPair.getFirst())); 
//        }
//        
//        // Also make sure not to re-check these files whether we visited source or class file. 
//        // We only want to perform code augmentation since these files have already been visited.
////        if (! filesNotToCheck.contains(fileToAdd)) filesNotToCheck.addLast(fileToAdd);
//      }
//      
//      filesToAugment.addAll(advanced);  // include advanced files in files to augment
//      files = filesToAugment.toArray(new File[filesToAugment.size()]); 
////      Utilities.show("Created files array: " + Arrays.toString(files));
    }
    
//        mediator.put(new Integer(ind), new Pair<SourceFile, LanguageLevelVisitor>(sf, llv));
//      }
//  }
    
    // If there were any errors in the llv pass or the type checking pass, just return them.
    if (_parseExceptions.size() > 0 || _visitorErrors.size() > 0) {
      return new Pair<LinkedList<JExprParseException>, 
        LinkedList<Pair<String, JExpressionIF>>>(_parseExceptions, _visitorErrors);
    }
//    Utilities.show("Processed LL files: " + Arrays.toString(files));
//    Utilities.show("mediator is: " + mediator);
    
    /* Perform code augmentation. */   
    for (Triple<LanguageLevelVisitor, SourceFile, File> triple: toAugment)  {
      try {
        LanguageLevelVisitor llv = triple.getFirst();
        SourceFile sf = triple.getSecond();
        File f = triple.getThird();
        
        File augmentedFile = getJavaForLLFile(f);   // create  empty .java file for .dj? file
        
        if (isAdvancedFile(f)) { Utilities.copyFile(f, augmentedFile); }
        else {
          BufferedReader tempBr = new BufferedReader(new FileReader(f));
          String firstLine = tempBr.readLine();
          tempBr.close(); // Important to close the reader, otherwise Windows will not allow the renameTo call later.
          if (firstLine == null) continue;
          
          // If the file has an appropriate LL extension, then parse it.
          if (isLanguageLevelFile(f)) {
            if (triple != null) {  // if triple is null, we do not actually need to augment this file--it wasn't visited.
              
              // Do code augmentation
              BufferedReader br = new BufferedReader(new FileReader(f), INPUT_BUFFER_SIZE);
              StringWriter sw = new StringWriter();
              BufferedWriter bw = new BufferedWriter(sw);
              
              // _log.log("Augmenting the source file " + sf);
//            Utilities.show("Augmenting the source file " + sf.getSourceInfo().getFile());
              Augmentor a = new Augmentor(SAFE_SUPPORT_CODE, br, bw, llv);
              sf.visit(a);
              
              br.close();
              bw.close();
              
              // write out the line number map and the augmented java file
              PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(augmentedFile)));
              SortedMap<Integer,Integer> lineNumberMap = a.getLineNumberMap();
              pw.println("// Language Level Converter line number map: dj*->java. Entries: "+lineNumberMap.size());
              // We print out LINE_NUM_MAPPINGS_PER_LINE mappings per line, so we need numLines
              // at the top of the file, and one more for a descriptive comment.
              // That means we need to increase the line numbers in the generated java file by numLines+1
              int numLines = (int)Math.ceil(((double)lineNumberMap.size())/LINE_NUM_MAPPINGS_PER_LINE);
              int mapCount = 0;
              for(Map.Entry<Integer,Integer> e: lineNumberMap.entrySet()) {
                // e.getKey(): dj* line number; e.getValue(): java line number (must be increased by numLines)
                if (mapCount%LINE_NUM_MAPPINGS_PER_LINE==0) pw.print("//");
                pw.printf(" %5d->%-5d", e.getKey(), (e.getValue()+numLines+1));
                if (mapCount%LINE_NUM_MAPPINGS_PER_LINE==LINE_NUM_MAPPINGS_PER_LINE-1) pw.println();
                ++mapCount;
              }
              if (mapCount%LINE_NUM_MAPPINGS_PER_LINE!=0) pw.println(); // print a newline unless we just printed one
              
              String augmented = sw.toString();
              pw.write(augmented, 0, augmented.length());
              pw.close();
            }
          }
        }
      }
      catch (Augmentor.Exception ae) {
        // The NullLiteral is a hack to get a JExpression with the correct SourceInfo inside.
        _addVisitorError(new Pair<String, JExpressionIF>(ae.getMessage(), new NullLiteral(SourceInfo.NONE)));
      }
      catch (IOException ioe) {
        // The NullLiteral is a hack to get a JExpression with the correct SourceInfo inside.
        _addVisitorError(new Pair<String, JExpressionIF>(ioe.getMessage(), new NullLiteral(SourceInfo.NONE)));
      }
    }
    return new Pair<LinkedList<JExprParseException>, 
      LinkedList<Pair<String, JExpressionIF>>>(_parseExceptions, _visitorErrors);
  }
  
  /** If a file name ends with .dj0, it is an Elementary File*/
  public static boolean isElementaryFile(File f) { return f.getPath().endsWith(".dj0"); } 
  /** If a file name ends with .dj1, it is an Intermediate File*/
  public static boolean isIntermediateFile(File f) { return f.getPath().endsWith(".dj1"); }
  /** If a file name ends with .dj2, it is an Advanced File, which is a legacy notion. */
  public static boolean isAdvancedFile(File f) { return f.getPath().endsWith(".dj2"); }
  /** If a file name ends with .dj, it is a Functional Java File */
  public static boolean isFunctionalJavaFile(File f) { return f.getPath().endsWith(".dj"); }
  /** If a file name ends with .dj, it is a Functional Java File */
  public static boolean isFullJavaFile(File f) { return f.getPath().endsWith(".java"); }
  
  /**If a file is an Elementary orIntermediate file, then it is a language level file. */
  private static boolean isLanguageLevelFile(File f) {
    return isElementaryFile(f) || isIntermediateFile(f) || isFunctionalJavaFile(f);
  }
  /**If a file is an Elementary orIntermediate file, then it is a language level file. */
  private static boolean isJavaFile(File f) {
    return isLanguageLevelFile(f) || isAdvancedFile(f) || isFullJavaFile(f);
  }
  
  private static File getJavaForLLFile(File f) {
    String augmentedFilePath = f.getAbsolutePath();
    int dotPos = augmentedFilePath.lastIndexOf('.');
    augmentedFilePath = augmentedFilePath.substring(0, dotPos); //remove the extension
    return new File(augmentedFilePath + ".java"); //replace it with .java
  }
  
  /**Only certain versions of the java compiler support autoboxing*/
  public static boolean versionSupportsAutoboxing(JavaVersion version) {
    return version.supports(JavaVersion.JAVA_5);
    // If we care to support it, also allows JSR-14
  }
  
  /**Only certain versions of the java compiler support generics*/
  public static boolean versionSupportsGenerics(JavaVersion version) {
    return version.supports(JavaVersion.JAVA_5);
    // If we care to support it, also allows JSR-14
  }
  
  /**Only 1.5 supports for each*/
  public static boolean versionSupportsForEach(JavaVersion version) {
    return version.supports(JavaVersion.JAVA_5);
  }
  
  /* @return true if the compiler version is 1.5 */
  public static boolean versionIs15(JavaVersion version) { return version.supports(JavaVersion.JAVA_5); }
  
  /**Do a conversion from the command line, to allow quick testing*/
  public static void main(String[] args) {
    LanguageLevelConverter llc = new LanguageLevelConverter();
    
    if (args.length == 0) {
      System.out.println("Java Language Level Converter");
      System.out.println("Please pass file names (*.dj, *.dj0, *.dj1, *.dj2) as arguments.");
      System.out.println("Note: The converter will use Java's classpath to resolve classes.");
      System.out.println("      If classes are not found, use java -cp <classpath> to set the classpath.");
      return;
    }
    
    File[] files = new File[args.length];
    for (int i = 0; i < args.length; i++) {
      files[i] = new File(args[i]);
    }
    
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result = 
      llc.convert(files, new Options(JavaVersion.JAVA_5,
                                     IOUtil.parsePath(System.getProperty("java.class.path", ""))));
    System.out.println(result.getFirst().size() + result.getSecond().size() + " errors.");
    for(JExprParseException p : result.getFirst()) {
      System.out.println(p);
    }
    for(Pair<String, JExpressionIF> p : result.getSecond()) {
      System.out.println(p.getFirst() + " " + p.getSecond().getSourceInfo());
    }
  }
}
