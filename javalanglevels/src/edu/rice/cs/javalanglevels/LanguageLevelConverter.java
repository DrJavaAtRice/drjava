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

import java.util.*;
import edu.rice.cs.javalanglevels.*;
import edu.rice.cs.javalanglevels.parser.*;
import edu.rice.cs.javalanglevels.tree.*;
import java.io.*;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * This class represents the mechanism by which we convert a language level file to a .java file of the same name by
 * first visiting the file to error-check it, and then augment the file.  This class is tested at the top level in the AdvancedLevelTest,
 * ElementaryLevelTest, and IntermediateLevelTest.
 */
public class LanguageLevelConverter {

  public static Options OPT = Options.DEFAULT;
    
  /* For Corky's version: set this to false */
  private static final boolean SAFE_SUPPORT_CODE = false;
  
  /**Holds any parse exceptions that are encountered*/
  private LinkedList<JExprParseException> _parseExceptions = new LinkedList<JExprParseException>();
  
  /**Holds any visitor exceptions that are encountered*/
  private LinkedList<Pair<String, JExpressionIF>> _visitorErrors = new LinkedList<Pair<String, JExpressionIF>>();
  
  /***Add the parse exception to the list of parse exceptions*/
  private void _addParseException(ParseException pe) {
    JExprParseException jpe;
    if (pe instanceof JExprParseException) { jpe = (JExprParseException) pe; }
    else { jpe = new JExprParseException(pe); }
    _parseExceptions.addLast(jpe);
  }
  
  /**Add the visitor error to the list of errors*/
  private void _addVisitorError(Pair<String, JExpressionIF> ve) {
    _visitorErrors.addLast(ve);
  }
  
  /**Parse, Visit, Type Check, and Convert any language level files in the array of files*/
  public Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>>
  convert(File[] files, Options options) {
    Map<File,Set<String>> sourceToTopLevelClassMap = new Hashtable<File,Set<String>>();
    return convert(files, options, sourceToTopLevelClassMap);
  }
  
  /**Parse, Visit, Type Check, and Convert any language level files in the array of files
    * @param sourceToTopLevelClassMap a map from source file to names of top-level classes created from that source file;
    *        an empty map should be passed in; it will be filled out by this method */
  public Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>>
  convert(File[] files, Options options, Map<File,Set<String>> sourceToTopLevelClassMap) {
    OPT = options;
    LanguageLevelVisitor._newSDs = new Hashtable<SymbolData, LanguageLevelVisitor>(); /**initialize so we don't get null pointer exception*/
    // We need a LinkedList for errors to be shared by the visitors to each file.
    LinkedList<Pair<String, JExpressionIF>> languageLevelVisitorErrors = new LinkedList<Pair<String, JExpressionIF>>();
    
    //keep track of the continuations to resolve
    Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations = new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>();
    
    //and the newSDs we've created
    Hashtable<SymbolData, LanguageLevelVisitor> languageLevelNewSDs = new Hashtable<SymbolData, LanguageLevelVisitor>();
    
    // Similarly, we need a Hashtable for a shared symbolTable.
    Symboltable languageLevelVisitorSymbolTable = new Symboltable();

    //And a linked list thing to share visited files.
    LinkedList<Pair<LanguageLevelVisitor, SourceFile>> languageLevelVisitedFiles = new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>();
    
    // We are doing two passes on the files, and the second pass needs the first's corresponding
    // SourceFile and ElementaryVisitor so we'll keep them around in a Hashtable.
    Hashtable<Integer, Pair<SourceFile, LanguageLevelVisitor>> mediator = new Hashtable<Integer, Pair<SourceFile, LanguageLevelVisitor>>();
    
    // The visitedFiles returned by any pass may include another file which is already scheduled for compilation.
    // In this case, we don't want to reparse, or perform either the first pass or the type check since it has
    // already been done.  (An error is thrown if we do since it thinks the class has already been defined).
    LinkedList<File> filesNotToCheck = new LinkedList<File>();
    
    // The number of files to compile may change if one file references another one.
    // We don't want to visit these newly referenced files because they've already
    // been visited.
    int originalNumOfFiles = files.length;
    
    // Find the ones that are LL files.
    // Do the passes first for ALL files before proceeding to code augmentation.
    // Otherwise if one class' superclass get augmented first, then it sees a lot
    // of illegal constructs (e.g. public and constructors).
    
    //will maintain the files we visit along with their visitors (for type checking step).
    LinkedList<Pair<LanguageLevelVisitor, SourceFile>> visited = new LinkedList<Pair<LanguageLevelVisitor, SourceFile>>();

    
    for (int ind = 0; ind < originalNumOfFiles; ind++) {
      
      try {
        BufferedReader tempBr = new BufferedReader(new FileReader(files[ind]));
        String firstLine = tempBr.readLine();
        if (firstLine == null) {
          continue;
        }
        tempBr.close();
        // If the file has the correct suffix, then parse it.
        // Also ignore files which are in filesNotToCheck.
        //for some reason, doing .contains on filesNotToCheck was not returning what we wanted.
        //So do this manually, trying to match the Absolute path.
        boolean foundFile = false;
        for (int i = 0; i<filesNotToCheck.size(); i++) {
          if (filesNotToCheck.get(i).getAbsolutePath().equals(files[ind].getAbsolutePath())) {
            foundFile = true;
            break;
          }
        }

        if (_isLanguageLevelFile(files[ind]) && !foundFile) {
          System.out.flush();
          SourceFile sf;
          File f = files[ind];
          JExprParser jep = new JExprParser(f);
          try { 
            sf = jep.SourceFile();
            final Set<String> topLevelClasses = new HashSet<String>();
            for (TypeDefBase t: sf.getTypes()) {
              t.visit(new JExpressionIFAbstractVisitor_void() {
                public void forClassDef(ClassDef that) {
                  topLevelClasses.add(that.getName().getText());
                }
              });
            }
            sourceToTopLevelClassMap.put(f,topLevelClasses);
          }
          catch (ParseException pe) {
            // If there is a ParseException, go to next file.
            _addParseException(pe);
            continue;
          }
          
          //Now create a LanguageLevelVisitor to do the first pass over the file.
          LanguageLevelVisitor llv = null;
          if (isElementaryFile(f)) {
            llv = new ElementaryVisitor(f, new LinkedList<Pair<String, JExpressionIF>>(), languageLevelVisitorSymbolTable, new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>(), languageLevelVisitedFiles, languageLevelNewSDs);
          }
          else if (isIntermediateFile(f)) {
            llv = new IntermediateVisitor(f, new LinkedList<Pair<String, JExpressionIF>>(), languageLevelVisitorSymbolTable, new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>(), languageLevelVisitedFiles, languageLevelNewSDs);
          }
          else if (isAdvancedFile(f)) {
            llv = new AdvancedVisitor(f, new LinkedList<Pair<String, JExpressionIF>>(), languageLevelVisitorSymbolTable, new Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>>(), languageLevelVisitedFiles, languageLevelNewSDs);
          }
          else {
            throw new RuntimeException("Internal Bug: Invalid file format not caught initially.  Please report this bug.");
          }

          
          // First pass
          sf.visit(llv);
          visited.add(new Pair<LanguageLevelVisitor, SourceFile>(llv, sf));
          //add the continuations to the hash table.
          continuations.putAll(llv.continuations);
          languageLevelVisitorErrors.addAll(llv.errors);
        }
      }
      catch (IOException ioe) {
        // The NullLiteral is a hack to get a JExpression with the correct SourceInfo inside.
        _addVisitorError(new Pair<String, JExpressionIF>(ioe.getMessage(), new NullLiteral(JExprParser.NO_SOURCE_INFO)));
      }

    }
    
    
    //Resolve continuations and create constructors.  Also accumulate errors.
    LanguageLevelVisitor.errors = new LinkedList<Pair<String, JExpressionIF>>(); //clear out error list
    
    
        //Resolve continuations
    //Hashtable<String, Pair<SourceInfo, LanguageLevelVisitor>> continuations = llv.continuations;
    
    while (!continuations.isEmpty()) {
      Enumeration<String> en = continuations.keys();
      
      while (en.hasMoreElements()) {
        String className = en.nextElement();
        Pair<SourceInfo, LanguageLevelVisitor> pair = continuations.remove(className);
        SymbolData returnedSd = pair.getSecond().getSymbolData(className, pair.getFirst(), true);
        if (returnedSd == null) {
          LanguageLevelVisitor.errors.add(new Pair<String, JExpressionIF>("Could not resolve " + className, new NullLiteral(pair.getFirst())));
        }
      }
    }
    
    // Create any constructors.
    Hashtable<SymbolData, LanguageLevelVisitor> newSDs = LanguageLevelVisitor._newSDs;
    Enumeration<SymbolData> keys = newSDs.keys();
    while (keys.hasMoreElements()) {
      SymbolData key = keys.nextElement();
      LanguageLevelVisitor sdlv = newSDs.get(key);    // Can return null because of silly side effects!
      if (sdlv != null) sdlv.createConstructor(key);  // Bug fix is a kludge! Deletes (key,sdlv) from _newSDs!
    }
    assert LanguageLevelVisitor._newSDs.isEmpty();
    
    languageLevelVisitorErrors.addAll(LanguageLevelVisitor.errors); //add any errors that accumulated during the continuation resolving/constructor generation
    
    // At this point, there should be no continuations and visitedFiles should be completely populated.
    // Check for errors; don't type-check if there are errors.
    if (languageLevelVisitorErrors.size() > 0) {
      _visitorErrors.addAll(languageLevelVisitorErrors);
    }
    
    
    //Let's TYPE CHECK!!!
          
          else {
            
            for (int ind = 0; ind<visited.size(); ind++) {
  
              LanguageLevelVisitor llv = visited.get(ind).getFirst();
              SourceFile sf = visited.get(ind).getSecond();
              
              //TODO: This is a terrible hack to get around the following problem.  Basically, when we autobox in isAssignableTo in SymbolData, 
              //we look through the object hierarchy for the primitive.  If its corresponding boxed object isn't in the symboltable, we're in trouble.
              //So, for those special cases, go ahead and make sure the object types are in the symbol table before we start type checking.
              //I'd like to make this better, but at least it works for now.
              
              
              //Before you type check, make sure that all boxed types of primitives are in the symbol table
              if (llv.symbolTable.get("java.lang.Integer") == null) {llv.getSymbolData("java.lang.Integer", JExprParser.NO_SOURCE_INFO);}
              if (llv.symbolTable.get("java.lang.Double")==null) {llv.getSymbolData("java.lang.Double", JExprParser.NO_SOURCE_INFO);}
              if (llv.symbolTable.get("java.lang.Character")==null) {llv.getSymbolData("java.lang.Character", JExprParser.NO_SOURCE_INFO);}
              if (llv.symbolTable.get("java.lang.Boolean")==null) {llv.getSymbolData("java.lang.Boolean", JExprParser.NO_SOURCE_INFO);}
              if (llv.symbolTable.get("java.lang.Long")==null) {llv.getSymbolData("java.lang.Long", JExprParser.NO_SOURCE_INFO);}
              if (llv.symbolTable.get("java.lang.Byte")==null) {llv.getSymbolData("java.lang.Byte", JExprParser.NO_SOURCE_INFO);}
              if (llv.symbolTable.get("java.lang.Short")==null) {llv.getSymbolData("java.lang.Short", JExprParser.NO_SOURCE_INFO);}
              if (llv.symbolTable.get("java.lang.Float")==null) {llv.getSymbolData("java.lang.Float", JExprParser.NO_SOURCE_INFO);}
              
              
              // Type check.
              TypeChecker btc = new TypeChecker(llv._file, llv._package, llv.errors, llv.symbolTable, llv._importedFiles, llv._importedPackages);
              sf.visit(btc);
              if (btc.errors.size() > 0) {
                _visitorErrors.addAll(btc.errors);
              }
              
              // Add those files to be compiled to the array of files.
              if (llv.visitedFiles.size() > 0) {            
                LinkedList<File> newFiles = new LinkedList<File>();
                for (int i = 0; i < files.length; i++) {
                  newFiles.addLast(files[i]);
                }
                Iterator<Pair<LanguageLevelVisitor, SourceFile>> iter = llv.visitedFiles.iterator();
                while (iter.hasNext()) {
                  Pair<LanguageLevelVisitor,SourceFile> currPair = iter.next();
                  File fileToAdd = currPair.getFirst()._file;
                  // if currSf is not null, then this visitedFile came from visiting the source file, not the class file. 
                  // We want to compile this source file; add it to the list of files after creating the mediator entry
                  // if necessary.
                  SourceFile currSf = currPair.getSecond();
                  if (currSf != null) {
                    if (newFiles.contains(fileToAdd)) {
                      // Messy, but we must make a mediator entry so that code augmentation has all the data it needs
                      // since we will skip visiting this file here.  
                      // Can just pass llv since it will contain the correct symbolTable.
                      mediator.put(new Integer(newFiles.indexOf(fileToAdd)), new Pair<SourceFile, LanguageLevelVisitor>(currSf, currPair.getFirst())); 
                    }
                    // Now, if we aren't already compiling this file, and we visited the source, add it to the list of
                    // files to be compiled.
                    if (!newFiles.contains(fileToAdd) && currSf != null) {
                      mediator.put(new Integer(newFiles.size()), new Pair<SourceFile, LanguageLevelVisitor>(currSf, currPair.getFirst()));
                      newFiles.addLast(fileToAdd);
                    }
                    
                  }
                  // Also make sure not to re-check these files whether we visited source or class file. 
                  // We only want to perform code augmentation since these files have already been visited.
                  if (!filesNotToCheck.contains(fileToAdd)) {
                    filesNotToCheck.addLast(fileToAdd);
                  }
                }
                files = newFiles.toArray(new File[newFiles.size()]);
              }
              
              mediator.put(new Integer(ind), new Pair<SourceFile, LanguageLevelVisitor>(sf, llv));
            }
          }
    
    
    //If there were any errors in the llv pass or the type checking pass, just return them.
    if (_parseExceptions.size() > 0 || _visitorErrors.size() > 0) {
      return new Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>>(_parseExceptions, _visitorErrors);
    }
    
    // Now do code augmentation.
    for (int ind = 0; ind < files.length; ind++) {
      try {
        BufferedReader tempBr = new BufferedReader(new FileReader(files[ind]));
        String firstLine = tempBr.readLine();
        tempBr.close(); // Important to close the reader, otherwise Windows will not allow the renameTo call later.
        if (firstLine == null) {
          continue;
        }

        // If the file has the correct beginner tag at the beginning, then parse it.
        // It should not end in .beginner since that's just a temporary copy which gets
        // deleted after the compile.
        if (_isLanguageLevelFile(files[ind])) {
          Pair<SourceFile, LanguageLevelVisitor> pair = mediator.get(new Integer(ind));
          if (pair != null) { //if pair is null, we do not actually need to augment this file--it wasn't visited.  Maybe we used the class file?
            SourceFile sf = pair.getFirst();
            LanguageLevelVisitor llv = pair.getSecond();
            File f = files[ind];
            // Do code augmentation.  This will involve a line-by-line copy, editing
            // lines as appropriate.
            String augmentedFilePath = f.getAbsolutePath();
            augmentedFilePath = augmentedFilePath.substring(0, augmentedFilePath.length() - 4); //remove the .dj# extension
            File augmentedFile = new File(augmentedFilePath + ".java"); //replace it with .java
            BufferedReader br = new BufferedReader(new FileReader(f));
            BufferedWriter bw = new BufferedWriter(new FileWriter(augmentedFile));
            
            Augmentor a = new Augmentor(SAFE_SUPPORT_CODE, br, bw, llv);
            sf.visit(a);
            
            br.close();
            bw.close();
          }
        }
      }
      catch (Augmentor.Exception ae) {
        // The NullLiteral is a hack to get a JExpression with the correct SourceInfo inside.
        _addVisitorError(new Pair<String, JExpressionIF>(ae.getMessage(), new NullLiteral(JExprParser.NO_SOURCE_INFO)));
      }
      catch (IOException ioe) {
        // The NullLiteral is a hack to get a JExpression with the correct SourceInfo inside.
        _addVisitorError(new Pair<String, JExpressionIF>(ioe.getMessage(), new NullLiteral(JExprParser.NO_SOURCE_INFO)));
      }
    }
    return new Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>>(_parseExceptions,
                                                                                       _visitorErrors);
  }
    
  /**If a file name ends with .dj0, it is an Elementary File*/
  public static boolean isElementaryFile(File f) {
    return f.getPath().endsWith(".dj0");
  }
  
  /**If a file name ends with .dj1, it is an Intermediate File*/
  public static boolean isIntermediateFile(File f) {
    return f.getPath().endsWith(".dj1");
  }
  
  /**If a file name ends with .dj2, it is an Advanced File*/
  public static boolean isAdvancedFile(File f) {
    return f.getPath().endsWith(".dj2");
  }
  
  /**If a file is an Elementary, Intermediate, or Advanced file, then it is a language level file*/
  public static boolean _isLanguageLevelFile(File f) {
    return isElementaryFile(f) || isIntermediateFile(f) || isAdvancedFile(f);
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
  public static boolean versionIs15(JavaVersion version) {
    return version.supports(JavaVersion.JAVA_5);
  }
  
  /**Do a conversion from the command line, to allow quick testing*/
  public static void main(String[] args) {
    LanguageLevelConverter llc = new LanguageLevelConverter();
    File[] files = new File[args.length];
    for (int i = 0; i < args.length; i++) {
      files[i] = new File(args[i]);
    }
    
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> result = 
        llc.convert(files, new Options(JavaVersion.JAVA_5, IterUtil.<File>empty()));
    System.out.println(result.getFirst().size() + result.getSecond().size() + " errors.");
    for(JExprParseException p : result.getFirst()) {
      System.out.println(p);
    }
    for(Pair<String, JExpressionIF> p : result.getSecond()) {
      System.out.println(p.getFirst() + " " + p.getSecond().getSourceInfo());
    }
  }
  
}
