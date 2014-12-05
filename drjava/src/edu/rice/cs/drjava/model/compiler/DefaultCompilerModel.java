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

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import java.util.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.Option;
import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.DrJavaFileUtils;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.javalanglevels.*;
import edu.rice.cs.javalanglevels.parser.*;
import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.util.swing.ScrollableListDialog;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Default implementation of the CompilerModel interface. This implementation is used for normal DrJava execution
  * (as opposed to testing DrJava).  TO DO: convert edu.rice.cs.util.Pair to edu.rice.cs.plt.tuple.Pair; requires 
  * making the same conversion in javalanglevels.
  * @version $Id$
  */
public class DefaultCompilerModel implements CompilerModel {
  
  /** for logging debug info */
  private static final Log _log = new edu.rice.cs.util.Log("DefaultCompilerModel.txt", false);
  
  /** The available compilers */
  private final List<CompilerInterface> _compilers;
  
  /** Current compiler -- one of _compilers, or a NoCompilerAvailable */
  private CompilerInterface _active;
  
  /** Manages listeners to this model. */
  private final CompilerEventNotifier _notifier = new CompilerEventNotifier();
  
  /** The global model to which this compiler model belongs. */
  private final GlobalModel _model;
  
  /** The error model containing all current compiler errors. */
  private CompilerErrorModel _compilerErrorModel;
  
  /** The lock providing mutual exclustion between compilation and unit testing */
  private Object _compilerLock = new Object();
  
  /** The LanguageLevelStackTraceMapper that helps translate .java line 
    * numbers to .dj* line numbers when an error is thrown */
  public LanguageLevelStackTraceMapper _LLSTM;
  
  /** Main constructor.  
    * @param m the GlobalModel that is the source of documents for this CompilerModel
    * @param compilers  The compilers to use.  The first will be made active; all are assumed
    *                   to be available.  An empty list is acceptable.
    */
  public DefaultCompilerModel(GlobalModel m, Iterable<? extends CompilerInterface> compilers) {
    
    _compilers = new ArrayList<CompilerInterface>();
    ArrayList<String> compilerNames = new ArrayList<String>();
    
    for (CompilerInterface i : compilers) { _compilers.add(i); compilerNames.add(i.getName());}
    
    OptionConstants.COMPILER_PREFERENCE_CONTROL.setList(compilerNames); // populates the compiler list for preference panel
    
    String dCompName = DrJava.getConfig().getSetting(OptionConstants.DEFAULT_COMPILER_PREFERENCE);
    
    if (_compilers.size() > 0) {
      if (! dCompName.equals(OptionConstants.COMPILER_PREFERENCE_CONTROL.NO_PREFERENCE) &&
           compilerNames.contains(dCompName)) 
        _active = _compilers.get(compilerNames.indexOf(dCompName));
      else 
        _active = _compilers.get(0);
    }
    else
      _active = NoCompilerAvailable.ONLY;
    
    _model = m;
    _compilerErrorModel = new CompilerErrorModel(new DJError[0], _model);
    _LLSTM = new LanguageLevelStackTraceMapper(_model);
  }
  
  
  //--------------------------------- Locking -------------------------------//
  
  /** Returns the lock used to prevent simultaneous compilation and JUnit testing */
  public Object getCompilerLock() { return _compilerLock; }
  
  //-------------------------- Listener Management --------------------------//
  
  /** Adds a CompilerListener to the model.  This operation is synchronized by the readers/writers protocol in 
    * EventNotifier<T>.
    * @param listener  A listener that reacts to compiler events.
    */
  public void addListener(CompilerListener listener) { _notifier.addListener(listener); }
  
  /** Removes a CompilerListener from the model.  If the listener is not installed, this method has no effect.
    * @param listener a listener that reacts to compiler events
    * This operation is synchronized by the readers/writers protocol in EventNotifier<T>.
    */
  public void removeListener(CompilerListener listener) { _notifier.removeListener(listener); }
  
  /** Removes all CompilerListeners from this model. */
  public void removeAllListeners() { _notifier.removeAllListeners(); }
  
  //-------------------------------- Triggers --------------------------------//
  
  
  /** Compile all open documents.
    * <p>Before compiling, all unsaved and untitled documents are saved, and compilation ends if the user cancels this 
    * step.  The compilation classpath and sourcepath includes the build directory (if it exists), the source roots, 
    * the project "extra classpath" (if it exists), the global "extra classpath", and the current JVM's classpath
    * (which includes drjava.jar, containing JUnit classes).</p>
    * This method formerly only compiled documents which were out of sync with their class file, as a performance 
    * optimization.  However, bug #634386 pointed out that unmodified files could depend on modified files, in which 
    * case this command would not recompile a file in some situations when it should.  Since we value correctness over
    * performance, we now always compile all open documents.</p>
    * @throws IOException if a filesystem-related problem prevents compilation
    */
  public void compileAll() throws IOException {
    if (_prepareForCompile()) { _doCompile(_model.getOpenDefinitionsDocuments()); }
    else _notifier.compileAborted(new UnexpectedException("Some modified open files are unsaved"));
  }
  
  /** Compiles all documents in the project source tree.  Assumes DrJava currently contains an active project.
    * <p>Before compiling, all unsaved and untitled documents are saved, and compilation ends if the user cancels this 
    * step.  The compilation classpath and sourcepath includes the build directory (if it exists), the source roots, 
    * the project "extra classpath" (if it exists), the global "extra classpath", and the current JVM's classpath
    * (which includes drjava.jar, containing JUnit classes).</p>
    * This method formerly only compiled documents which were out of sync with their class file, as a performance 
    * optimization.  However, bug #634386 pointed out that unmodified files could depend on modified files, in which 
    * case this command would not recompile a file in some situations when it should.  Since we value correctness over
    * performance, we now always compile all open documents.</p>
    * @throws IOException if a filesystem-related problem prevents compilation
    */
  public void compileProject() throws IOException {
    if (! _model.isProjectActive()) 
      throw new UnexpectedException("compileProject invoked when DrJava is not in project mode");
    
    if (_prepareForCompile()) { _doCompile(_model.getProjectDocuments()); }
    else _notifier.compileAborted(new UnexpectedException("Project contains unsaved modified files"));
  }
  
  /** Compiles all of the given files.
    * <p>Before compiling, all unsaved and untitled documents are saved, and compilation ends if the user cancels this 
    * step.  The compilation classpath and sourcepath includes the build directory (if it exists), the source roots, 
    * the project "extra classpath" (if it exists), the global "extra classpath", and the current JVM's classpath
    * (which includes drjava.jar, containing JUnit classes).</p>
    * This method formerly only compiled documents which were out of sync with their class file, as a performance 
    * optimization.  However, bug #634386 pointed out that unmodified files could depend on modified files, in which 
    * case this command would not recompile a file in some situations when it should.  Since we value correctness over
    * performance, we now always compile all open documents.</p>
    * @throws IOException if a filesystem-related problem prevents compilation
    */
  public void compile(List<OpenDefinitionsDocument> defDocs) throws IOException {
    if (_prepareForCompile()) { _doCompile(defDocs); }
    else _notifier.compileAborted(new UnexpectedException("The files to be compiled include unsaved modified files"));
  }
  
  /** Compiles the given file.
    * <p>Before compiling, all unsaved and untitled documents are saved, and compilation ends if the user cancels this 
    * step.  The compilation classpath and sourcepath includes the build directory (if it exists), the source roots, 
    * the project "extra classpath" (if it exists), the global "extra classpath", and the current JVM's classpath
    * (which includes drjava.jar, containing JUnit classes).</p>
    * This method formerly only compiled documents which were out of sync with their class file, as a performance 
    * optimization.  However, bug #634386 pointed out that unmodified files could depend on modified files, in which 
    * case this command would not recompile a file in some situations when it should.  Since we value correctness over
    * performance, we now always compile all open documents.</p>
    * @throws IOException if a filesystem-related problem prevents compilation
    */
  public void compile(OpenDefinitionsDocument doc) throws IOException {
    if (_prepareForCompile()) { _doCompile(Arrays.asList(doc)); }
    else _notifier.compileAborted(new UnexpectedException(doc + "is modified but unsaved"));
  }
  
  /** Check that there are no unsaved or untitled files currently open.
    * @return  @code{true} iff compilation should continue
    */
  private boolean _prepareForCompile() {
    if (_model.hasModifiedDocuments()) _notifier.saveBeforeCompile();
    // If user cancelled save, abort compilation
    return ! _model.hasModifiedDocuments();
  }
  
  /** Compile the given documents. */
  private void _doCompile(List<OpenDefinitionsDocument> docs) throws IOException {
    _LLSTM.clearCache();
    final ArrayList<File> filesToCompile = new ArrayList<File>();
    final ArrayList<File> excludedFiles = new ArrayList<File>();
    final ArrayList<DJError> packageErrors = new ArrayList<DJError>();
    for (OpenDefinitionsDocument doc : docs) {
      if (doc.isSourceFile()) {
        File f = doc.getFile();
        // Check for null in case the file is untitled (not sure this is the correct check)
        if (f != null && f != FileOps.NULL_FILE) { filesToCompile.add(f); }
        doc.setCachedClassFile(FileOps.NULL_FILE); // clear cached class file
        
        try { doc.getSourceRoot(); }
        catch (InvalidPackageException e) {
          packageErrors.add(new DJError(f, e.getMessage(), false));
        }
      }
      else excludedFiles.add(doc.getFile());
    }
    
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.compileStarted(); } });
    try {
      if (! packageErrors.isEmpty()) { _distributeErrors(packageErrors); }
      else {
        try {
          File buildDir = _model.getBuildDirectory();
          if (buildDir != null && buildDir != FileOps.NULL_FILE && ! buildDir.exists() && ! buildDir.mkdirs()) {
            throw new IOException("Could not create build directory: " + buildDir);
          }
          
//          File workDir = _model.getWorkingDirectory();
//          if (workDir == FileOps.NULL_FILE) workDir = null;
//          if (workDir != null && ! workDir.exists() && ! workDir.mkdirs()) {
//            throw new IOException("Could not create working directory: " + workDir);
//          }
          
          _compileFiles(filesToCompile, buildDir);
        }
        catch (Throwable t) {
          DJError err = new DJError(t.toString(), false);
          _distributeErrors(Arrays.asList(err));
          throw new UnexpectedException(t);
        }
      }
    }
    finally {
      Utilities.invokeLater(new Runnable() {
        public void run() { _notifier.compileEnded(_model.getWorkingDirectory(), excludedFiles); }
      });
    }
  }
  
  
  //-------------------------------- Helpers --------------------------------//
  
  /** Converts JExprParseExceptions thrown by the JExprParser in language levels to CompilerErrors. */
  private LinkedList<DJError> _parseExceptions2CompilerErrors(LinkedList<JExprParseException> pes) {
    final LinkedList<DJError> errors = new LinkedList<DJError>();
    Iterator<JExprParseException> iter = pes.iterator();
    while (iter.hasNext()) {
      JExprParseException pe = iter.next();
      errors.addLast(new DJError(pe.getFile(), pe.currentToken.beginLine-1, pe.currentToken.beginColumn-1, 
                                 pe.getMessage(), false));
    }
    return errors;
  }
  
  /** Converts errors thrown by the language level visitors to CompilerErrors. */
  private LinkedList<DJError> _visitorErrors2CompilerErrors(LinkedList<Pair<String, JExpressionIF>> visitorErrors) {
    final LinkedList<DJError> errors = new LinkedList<DJError>();
    Iterator<Pair<String, JExpressionIF>> iter = visitorErrors.iterator();
    while (iter.hasNext()) {
      Pair<String, JExpressionIF> pair = iter.next();
      String message = pair.getFirst();      
//      System.out.println("Got error message: " + message);
      JExpressionIF jexpr = pair.getSecond();
      
      SourceInfo si;
      if (jexpr == null) si = SourceInfo.NO_INFO;
      else si = pair.getSecond().getSourceInfo();
      
      errors.addLast(new DJError(si.getFile(), si.getStartLine()-1, si.getStartColumn()-1, message, false));
    }
    return errors;
  }
  
  /** Compile the given files and update the model with any errors that result.  Does not notify listeners.  
    * All public compile methods delegate to this one so this method is the only one that uses synchronization to 
    * prevent compiling and unit testing at the same time.
    * @param files The files to be compiled
    * @param buildDir The output directory for all the .class files; @code{null} means output to the same 
    *                 directory as the source file
    */
  private void _compileFiles(List<File> files, File buildDir) throws IOException {
    if (! files.isEmpty()) {
      /* Canonicalize buildDir */
      if (buildDir == FileOps.NULL_FILE) buildDir = null; // compiler interface wants null pointer if no build directory
      if (buildDir != null) buildDir = IOUtil.attemptCanonicalFile(buildDir);
      
      List<File> classPath = CollectUtil.makeList(_model.getClassPath());
      
      // Temporary hack to allow a boot class path to be specified
      List<File> bootClassPath = null;
      String bootProp = System.getProperty("drjava.bootclasspath");
      if (bootProp != null) { bootClassPath = CollectUtil.makeList(IOUtil.parsePath(bootProp)); }
      
      final LinkedList<DJError> errors = new LinkedList<DJError>();
      
      List<? extends File> preprocessedFiles = _compileLanguageLevelsFiles(files, errors, classPath, bootClassPath);
      
      System.err.println("Performed Language Level Translation of " + preprocessedFiles);
      if (errors.isEmpty()) {
        CompilerInterface compiler = getActiveCompiler();
        
        // Mutual exclusion with JUnit code that finds all test classes (in DefaultJUnitModel)
        synchronized(_compilerLock) {
          if (preprocessedFiles == null) {
            errors.addAll(compiler.compile(files, classPath, null, buildDir, bootClassPath, null, true));
          }
          else {
            /** If compiling a language level file, do not show warnings, as these are not caught by the language level 
              * parser */
            errors.addAll(compiler.compile(preprocessedFiles, classPath, null, buildDir, bootClassPath, null, false));
          }
        }
      }
      _distributeErrors(errors);
    }
    else { 
      // TODO: Is this necessary?
      _distributeErrors(Collections.<DJError>emptyList());
    }
  }
  
  /** Reorders files so that all file names containing "Test" are at the end.  */
  private static List<File> _testFileSort(List<File> files) {
    LinkedList<File> testFiles = new LinkedList<File>();
    LinkedList<File> otherFiles = new LinkedList<File>();
    for (File f: files) {
      if (f.getName().contains("Test")) testFiles.add(f);
      else otherFiles.add(f);
    }
    otherFiles.addAll(testFiles);
    return otherFiles;
  }
  
  /** Compiles the language levels files in the list.  Adds any errors to the given error list.
    * @return  An updated list for compilation containing no Language Levels files, or @code{null}
    *          if there were no Language Levels files to process.
    */
  private List<File> _compileLanguageLevelsFiles(List<File> files, List<DJError> errors, Iterable<File> classPath, 
                                                 Iterable<File> bootClassPath) {
    /* Construct the collection of files to be compild by javac, renaming any language levels (.dj*) files to the 
     * corresponding java (.java) files.  By using a HashSet, we avoid creating duplicates in this collection.
     */
    HashSet<File> javaFileSet = new HashSet<File>();
    LinkedList<File> newFiles = new LinkedList<File>();  // Used to record the LL files that must be converted
    final LinkedList<File> filesToBeClosed = new LinkedList<File>();  // Used to record .java files that are open at 
    // the same time as their .dj? files.
    boolean containsLanguageLevels = false;
    for (File f : files) {
      File canonicalFile = IOUtil.attemptCanonicalFile(f);
      String fileName = canonicalFile.getPath();
      if (DrJavaFileUtils.isLLFile(fileName)) {
        containsLanguageLevels = true;
        File javaFile = new File(DrJavaFileUtils.getJavaForLLFile(fileName));
        
        //checks if .dj? file has a matching .java file open in project. Eventually warns user (later on in code)
        if (files.contains(javaFile)) filesToBeClosed.add(javaFile);
          // delete file later so closeFiles doesn't complain about missing files
        else
          // Delete the stale .java file now (if it exists), a file with this name will subsequently be generated
          javaFile.delete();
        
        javaFileSet.add(javaFile);
        newFiles.add(javaFile);
      }   
      else javaFileSet.add(canonicalFile);
    }
    
    for (File f: filesToBeClosed) {
      if (files.contains(DrJavaFileUtils.getDJForJavaFile(f)) ||
          files.contains(DrJavaFileUtils.getDJ0ForJavaFile(f)) ||
          files.contains(DrJavaFileUtils.getDJ1ForJavaFile(f)) ||
          files.contains(DrJavaFileUtils.getDJ2ForJavaFile(f))) {
        files.remove(f);
      }
    }
    
    if (!filesToBeClosed.isEmpty()) {
      final JButton closeButton = new JButton(new AbstractAction("Close Files") {
        public void actionPerformed(ActionEvent e) {
          // no op, i.e. delete everything
        }
      });
      final JButton keepButton = new JButton(new AbstractAction("Keep Open") {
        public void actionPerformed(ActionEvent e) {
          // clear the set, i.e. do not delete anything
          filesToBeClosed.clear();
        }
      });
      ScrollableListDialog<File> dialog = new ScrollableListDialog.Builder<File>()
        .setTitle("Java File" + (filesToBeClosed.size() == 1?"":"s") + " Need to Be Closed")
        .setText("The following .java " + (filesToBeClosed.size() == 1?
                                             "file has a matching .dj? file":
                                             "files have matching .dj? files") + " open.\n" + 
                 (filesToBeClosed.size() == 1?
                    "This .java file needs":
                    "These .java files need") + " to be closed for proper compiling.")
        .setItems(filesToBeClosed)
        .setMessageType(JOptionPane.WARNING_MESSAGE)
        .setFitToScreen(true)
        .clearButtons()
        .addButton(closeButton)
        .addButton(keepButton)
        .build();
      
      dialog.showDialog();
      
      LinkedList<OpenDefinitionsDocument> docsToBeClosed = new LinkedList<OpenDefinitionsDocument>();
      for(File f: filesToBeClosed) {
        try {
          docsToBeClosed.add(_model.getDocumentForFile(f));
        }
        catch(IOException ioe) { /* ignore, just don't close this document */ }
      }
      _model.closeFiles(docsToBeClosed);
      // delete the files now because closeFiles has executed and won't complain about missing files anymore
      for(File f: filesToBeClosed) {        
        // Delete the stale .java file now (if it exists), a file with this name will subsequently be generated
        f.delete();
      }
    }
    
    if (containsLanguageLevels) {
      /* Check if we should delete class files in directories with language level files. */
      final File buildDir = _model.getBuildDirectory();
      final File sourceDir = _model.getProjectRoot();
      if (!DrJava.getConfig().getSetting(OptionConstants.DELETE_LL_CLASS_FILES)
            .equals(OptionConstants.DELETE_LL_CLASS_FILES_CHOICES.get(0))) {
        // not "never"
        final HashSet<File> dirsWithLLFiles = new HashSet<File>();
        for(File f: newFiles) {
          try {
            File dir = f.getParentFile();
            if (buildDir != null && buildDir != FileOps.NULL_FILE &&
                sourceDir != null && sourceDir != FileOps.NULL_FILE) {
              // build directory set
              String rel = edu.rice.cs.util.FileOps.stringMakeRelativeTo(dir,sourceDir);
              dir = new File(buildDir,rel);
            }            
            dirsWithLLFiles.add(dir);
          }
          catch(IOException ioe) { /* just don't add this directory */ }
        }
        
        if (DrJava.getConfig().getSetting(OptionConstants.DELETE_LL_CLASS_FILES)
              .equals(OptionConstants.DELETE_LL_CLASS_FILES_CHOICES.get(1))) {
          // "ask me"
          final JButton deleteButton = new JButton(new AbstractAction("Delete Class Files") {
            public void actionPerformed(ActionEvent e) {
              // no op
            }
          });
          final JButton keepButton = new JButton(new AbstractAction("Keep Class Files") {
            public void actionPerformed(ActionEvent e) {
              // clear the set, i.e. do not delete anything
              dirsWithLLFiles.clear();
            }
          });
          ScrollableListDialog<File> dialog = new ScrollableListDialog.Builder<File>()
            .setTitle("Delete Class Files")
            .setText("We suggest that you delete all class files in the directories with language\n" +
                     "level files. Do you want to delete the class files in the following director" +
                     (dirsWithLLFiles.size() == 1?"y":"ies") + "?")
            .setItems(new ArrayList<File>(dirsWithLLFiles))
            .setMessageType(JOptionPane.QUESTION_MESSAGE)
            .setFitToScreen(true)
            .clearButtons()
            .addButton(deleteButton)
            .addButton(keepButton)
            .build();
          
          dialog.showDialog();
        }
        
        // Delete all class files in the directories listed. If the user was asked and said "keep",
        // then the set will be empty
        for(File f: dirsWithLLFiles) {
          f.listFiles(new java.io.FilenameFilter() {
            public boolean accept(File dir, String name) {
              int endPos = name.lastIndexOf(".class");
              if (endPos < 0) return false; // can't be a class file
              new File(dir, name).delete();
              // don't need to return true, we're deleting the file here already
              return false;
            }
          });
        }
      }
      
      /* Perform language levels conversion, creating corresponding .java files. */
      LanguageLevelConverter llc = new LanguageLevelConverter();
      Options llOpts;  /* Options passed as arguments to LLConverter */
      if (bootClassPath == null) { llOpts = new Options(getActiveCompiler().version(), classPath); }
      else { llOpts = new Options(getActiveCompiler().version(), classPath, bootClassPath); }
      
      // NOTE: the workaround "_testFileSort(files)" instead of simply "files") may no longer be necessary.
      
      /* Perform the LL conversion incorporating the following workaround:  Forward references can generate spurious 
       * conversion errors in some cases.  This problem can be mitigated by compiling JUnit test files (with names
       * containing the substring "Test") last.  
       */
      Map<File,Set<String>> sourceToTopLevelClassMap = new HashMap<File,Set<String>>();
      Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> llErrors = 
        llc.convert(_testFileSort(files).toArray(new File[0]), llOpts, sourceToTopLevelClassMap);
      
      /* Add any errors encountered in conversion to the compilation error log. */
      errors.addAll(_parseExceptions2CompilerErrors(llErrors.getFirst()));
      errors.addAll(_visitorErrors2CompilerErrors(llErrors.getSecond()));
      
      // Since we (optionally) delete all class files in LL directories, we don't need the code
      // to smart-delete class files anymore.
      // smartDeleteClassFiles(sourceToTopLevelClassMap);
    }
    
    if (containsLanguageLevels) { return new LinkedList<File>(javaFileSet); }
    else { return null; }
  }
  
  /** Sorts the given array of CompilerErrors and divides it into groups based on the file, giving each group to the
    * appropriate OpenDefinitionsDocument, opening files if necessary.  Called immediately after compilations finishes.
    */
  private void _distributeErrors(List<? extends DJError> errors) throws IOException {
//    resetCompilerErrors();  // Why is this done?
//    System.err.println("Preparing to construct CompilerErrorModel for errors: " + errors);
    _compilerErrorModel = new CompilerErrorModel(errors.toArray(new DJError[0]), _model);
    _model.setNumCompilerErrors(_compilerErrorModel.getNumCompilerErrors());  // cache number of compiler errors in global model
  }
  
  //----------------------------- Error Results -----------------------------//
  
  /** Gets the CompilerErrorModel representing the last compile. */
  public CompilerErrorModel getCompilerErrorModel() { return _compilerErrorModel; }
  
  /** Gets the total number of errors in this compiler model. */
  public int getNumErrors() { return getCompilerErrorModel().getNumErrors(); }
  
  /** Gets the total number of current compiler errors. */
  public int getNumCompilerErrors() { return getCompilerErrorModel().getNumCompilerErrors(); }
  
  /** Gets the total number of current warnings. */  
  public int getNumWarnings() { return getCompilerErrorModel().getNumWarnings(); }
  
  /** Resets the compiler error state to have no errors. */
  public void resetCompilerErrors() {
    // TODO: see if we can get by without this function
    _compilerErrorModel = new CompilerErrorModel(new DJError[0], _model);
  }
  
  //-------------------------- Compiler Management --------------------------//
  
  /** Returns all registered compilers that are actually available.  If there are none,
    * the result is {@link NoCompilerAvailable#ONLY}.
    */
  public Iterable<CompilerInterface> getAvailableCompilers() {
    if (_compilers.isEmpty()) { return IterUtil.singleton(NoCompilerAvailable.ONLY); }
    else { return IterUtil.snapshot(_compilers); }
  }
  
  /** Gets the compiler that is the "active" compiler.
    *
    * @see #setActiveCompiler
    */
  public CompilerInterface getActiveCompiler() { return _active; }
  
  /** Sets which compiler is the "active" compiler.
    *
    * @param compiler Compiler to set active.
    * @throws IllegalArgumentException  If the compiler is not in the list of available compilers
    *
    * @see #getActiveCompiler
    */
  public void setActiveCompiler(CompilerInterface compiler) {
    if (_compilers.isEmpty() && compiler.equals(NoCompilerAvailable.ONLY)) {
      // _active should be set correctly already
    }
    else if (_compilers.contains(compiler)) {
      _active = compiler;
      _notifier.activeCompilerChanged();
    }
    else {
      throw new IllegalArgumentException("Compiler is not in the list of available compilers: " + compiler);
    }
  }
  
  /* This method is no longer used. */
//  /** Add a compiler to the active list */
//  public void addCompiler(CompilerInterface compiler) {
//    if (_compilers.isEmpty()) {
//      _active = compiler;
//    }
//    _compilers.add(compiler);
//  }
  
//  /** Delete the .class files that match the following pattern:
//    * XXX.dj? --> XXX.class
//    *             XXX$*.class
//    * @param sourceToTopLevelClassMap a map from directories to classes in them
//    */
//  public void smartDeleteClassFiles(Map<File,Set<String>> sourceToTopLevelClassMap) {
//    final File buildDir = _model.getBuildDirectory();
//    final File sourceDir = _model.getProjectRoot();
//    // Accessing the disk is the most costly part; therefore, we want to scan each directory only once.
//    // We create a map from parent directory to class names in that directory.
//    // Then we scan the files in each directory and delete files that match the class names listed for it.
//    // dirToClassNameMap: key=parent directory, value=set of classes in this directory
//    Map<File,Set<String>> dirToClassNameMap = new HashMap<File,Set<String>>();
//    for(Map.Entry<File,Set<String>> e: sourceToTopLevelClassMap.entrySet()) {
//      try {
//        File dir = e.getKey().getParentFile();
//        if (buildDir != null && buildDir != FileOps.NULL_FILE &&
//            sourceDir != null && sourceDir != FileOps.NULL_FILE) {
//          // build directory set
//          String rel = edu.rice.cs.util.FileOps.stringMakeRelativeTo(dir,sourceDir);
//          dir = new File(buildDir,rel);
//        }
//        Set<String> classNames = dirToClassNameMap.get(dir);
//        if (classNames == null) classNames = new HashSet<String>();
//        classNames.addAll(e.getValue());
//        dirToClassNameMap.put(dir,classNames);
////          System.out.println(e.getKey() + " --> " + dir);
////          for(String name: e.getValue()) {
////            System.out.println("\t" + name);
////          }
//      }
//      catch(IOException ioe) { /* we'll fail to delete this, but that's better than deleting something we shouldn't */ }
//    }
//    // Now that we have a map from parent directories to the class names that should be deleted
//    // in them, we scan the files in each directory, then check if the names match the class names.      
//    for(final Map.Entry<File,Set<String>> e: dirToClassNameMap.entrySet()) {
////        System.out.println("Processing dir: " + e.getKey());
////        System.out.println("\t" + java.util.Arrays.toString(e.getValue().toArray(new String[0])));
//      e.getKey().listFiles(new java.io.FilenameFilter() {
//        public boolean accept(File dir, String name) {
////            System.out.println("\t" + name);
//          int endPos = name.lastIndexOf(".class");
//          if (endPos < 0) return false; // can't be a class file
//          int dollarPos = name.indexOf('$');
//          if ((dollarPos >= 0) && (dollarPos < endPos)) endPos = dollarPos;
//          // class name goes to the .class or the first $, whichever comes first
//          Set<String> classNames = e.getValue();
//          if (classNames.contains(name.substring(0,endPos))) { 
//            // this is a class file that is generated from a .dj? file
//            new File(dir, name).delete();
//            // don't need to return true, we're deleting the file here already
////              System.out.println("\t\tDeleted");
//          }
//          return false;
//        }
//      });
//    }
//  }
  
  /** returns the LanguageLevelStackTraceMapper
    * @return the LanguageLevelStackTraceMapper
    * */
  public LanguageLevelStackTraceMapper getLLSTM() { return _LLSTM; } 
}
