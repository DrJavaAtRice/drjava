/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Collection;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.util.newjvm.ExecJVM;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.Configuration;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.platform.PlatformSupport;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.drjava.model.compiler.CompilerError;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;

/** Default implementation of JavadocModel interface; generates Javadoc HTML files for a set of documents.
 *  @version $Id$
 */
public class DefaultJavadocModel implements JavadocModel {

  /** Used by CompilerErrorModel to open documents that have errors. */
  private GlobalModel _model;

  /**Manages listeners to this model. */
  private final JavadocEventNotifier _notifier = new JavadocEventNotifier();

  /** The error model containing all current Javadoc errors. */
  private CompilerErrorModel _javadocErrorModel;

  /** Main constructor.
   *  @param getter Source of documents for this JavadocModel
   */
  public DefaultJavadocModel(GlobalModel model) {
    _model = model;
    _javadocErrorModel = new CompilerErrorModel();
  }

  //-------------------------- Listener Management --------------------------//

  /** Add a JavadocListener to the model.
   *  @param listener a listener that reacts to Javadoc events
   */
  public void addListener(JavadocListener listener) { _notifier.addListener(listener); }

  /** Remove a JavadocListener from the model.  If the listener is not currently
   *  listening to this model, this method has no effect.
   *  @param listener a listener that reacts to Javadoc events
   */
  public void removeListener(JavadocListener listener) { _notifier.removeListener(listener); }

  /** Removes all JavadocListeners from this model. */
  public void removeAllListeners() { _notifier.removeAllListeners(); }

  //----------------------------- Error Results -----------------------------//

  /** Accessor for the Javadoc error model.
   *  @return the CompilerErrorModel managing Javadoc errors.
   */
  public CompilerErrorModel getJavadocErrorModel() { return _javadocErrorModel; }

  /** Clears all current Javadoc errors. */
  public void resetJavadocErrors() {
    _javadocErrorModel = new CompilerErrorModel();
  }

  // -------------------- Javadoc All Documents --------------------
  
  /** Javadocs all open documents, after ensuring that all are saved.
   *  The user provides a destination, and the gm provides the package info.
   *  Must run in the event-handling thread.
   *
   * @param select a command object for selecting a directory and warning a user
   *        about bad input
   * @param saver a command object for saving a document (if it moved/changed)
   * @param classpath a collection of classpath elements to be used by Javadoc
   *
   * @throws IOException if there is a problem manipulating files
   */
  public void javadocAll(DirectorySelector select, final FileSaveSelector saver, final String classPath) 
    throws IOException {
        
    // Only javadoc if all are saved. Removed because it is already done inside suggestJavadocDestination (fixes bug where pop-up is shown twice)
//    _attemptSaveAllDocuments();
    if (_model.hasModifiedDocuments() || _model.hasUntitledDocuments()) { return; }  /* abort if files remain unsaved */
    
    // Make sure that there is at least one saved document.
//    List<OpenDefinitionsDocument> docs = _model.getOpenDefinitionsDocuments();
       
//    for (OpenDefinitionsDocument doc: docs) {
//      if (doc.isUntitled()) return;  // ignore javadoc, since a document is still unsaved
//    }
//    
    Configuration config = DrJava.getConfig();
    File destDir = config.getSetting(OptionConstants.JAVADOC_DESTINATION);
    
    // Get the destination directory via the DirectorySelector, if appropriate.
    try {
      if (destDir.equals(FileOption.NULL_FILE)) {
        /* This is the default, stock behavior of a new install. If no destination is set, don't pass 
         anything to the ui command. Let the command object decide what to do. */
        destDir = select.getDirectory(null);
      }
      else
        // Otherwise, tell the command object to prefer the config's default.
        destDir = select.getDirectory(destDir);
      
      // Make sure the destination is usable.
      while (!destDir.exists() || !destDir.isDirectory() || !destDir.canWrite()) {
        if (!destDir.getPath().equals("") && !destDir.exists()) {
          // If the choice doesn't exist, ask to create it.
          boolean create = select.askUser
            ("The directory you chose does not exist:\\n'" + destDir + "'\nWould you like to create it?",
             "Create Directory?");
          if (create) {
            boolean dirMade = destDir.mkdirs();
            if (! dirMade) throw new IOException("Could not create directory: " + destDir);
          }
          else return;
        }
        else if (!destDir.isDirectory() || destDir.getPath().equals("")) {
          // We can't use it if it isn't a directory
          select.warnUser("The file you chose is not a directory:\n" +
                          "'" + destDir + "'\n" +
                          "Please choose another.",
                          "Not a Directory!");
          destDir = select.getDirectory(null);
        }
        else {
          //If the directory isn't writable, tell the user and ask again.
          select.warnUser("The directory you chose is not writable:\n" +
                          "'" + destDir + "'\n" +
                          "Please choose another directory.",
                          "Cannot Write to Destination!");
          destDir = select.getDirectory(null);
        }
      }
    }
    catch (OperationCanceledException oce) { return; } // If the user cancels anywhere, silently return.
  
    // Start a new thread to do the work.
    final File destDirF = destDir;
    new Thread("DrJava Javadoc Thread") {
      public void run() { _javadocAllWorker(destDirF, saver, classPath); }
    }.start();
  }
    
   

  /**
   * This method handles most of the logic of performing a Javadoc operation,
   * once we know that it won't be canceled.
   *
   * @param destDirFile the destination directory for the doc files
   * @param saver a command object for saving a document (if it moved/changed)
   * @param classpath an array of classpath elements to be used by Javadoc
   */
  private void _javadocAllWorker(File destDirFile, FileSaveSelector saver, String classPath) {
    
    if (!_ensureValidToolsJar()) return;

    String destDir = destDirFile.getAbsolutePath();

    // Accumulate a set of arguments to JavaDoc - package or file names.
    HashSet<String> docUnits      = new HashSet<String>(); // units to send to Javadoc (packages or files)
    HashSet<File>   sourceRootSet = new HashSet<File>();   // set of unique source roots for open files
    HashSet<File>   defaultRoots  = new HashSet<File>();   // source roots for files in default package
    HashSet<String> topLevelPacks = new HashSet<String>(); // top level package names to include

    // This depends on the current value of the "javadoc.all.packages" option.
    boolean docAll = DrJava.getConfig().getSetting(OptionConstants.JAVADOC_FROM_ROOTS).booleanValue();

    // Each document has a package hierarchy to traverse.
    List<OpenDefinitionsDocument> docs = _model.getOpenDefinitionsDocuments();
    for (OpenDefinitionsDocument doc: docs) {
      File file = null;

      try {
        // This will throw an IllegalStateException if no file can be found
        file = _getFileFromDocument(doc, saver);

        // File shouldn't be null here, but just in case...
        if (file == null) throw new IllegalStateException("No file for this document.");

        File sourceRoot = doc.getSourceRoot();
        String pack = doc.getPackageName();

        if (pack.equals("")) {
          // No package name for this file
          if (! defaultRoots.contains(sourceRoot)) {
            // This file uses the default package.
            // Include all the other source files at the source root.
            // But don't do it if we've already done it for this directory.
            defaultRoots.add(sourceRoot);
            File[] javaFiles = sourceRoot.listFiles(FileOps.JAVA_FILE_FILTER);
            for (File f: javaFiles) { docUnits.add(f.getAbsolutePath());}
          }
        }
        else {
          // There is a package name
          String topLevelPack;
          File searchRoot;

          int index = pack.indexOf('.');
          if (docAll && index != -1) {
            // We need to doc all packages from the root level down.

            // TODO: write a unit test for a package name w/ no dot!
            //  (This was broken before, but it works now)
            topLevelPack = pack.substring(0, index);
            searchRoot = new File(sourceRoot, topLevelPack);
          }
          else {
            // Only look in the current package or deeper
            topLevelPack = pack;
            searchRoot = new File(sourceRoot, pack.replace('.', File.separatorChar));
          }

          // But we don't want to traverse the hierarchy more than once.
          if (! topLevelPacks.contains(topLevelPack) || ! sourceRootSet.contains(sourceRoot)) {
            // HashSets don't have duplicates, so it's ok to add both in either case
            topLevelPacks.add(topLevelPack);
            sourceRootSet.add(sourceRoot);
            docUnits.addAll(FileOps.packageExplore(topLevelPack, searchRoot));
          }
        }
      }
      catch (IllegalStateException ise) {
        // No file for this document; skip it
      }
      catch (IOException ioe) {
        // There was a problem getting the file for this document.
        // Kill javadoc and display the exception as an error.
        _notifier.javadocStarted();  // fire first so it can fire javadocEnded
        _showCompilerError(ioe.getMessage(), file);
        return;
      }
      catch (InvalidPackageException ipe) {
        // Bad package - kill the javadoc operation and display the exception
        // as an error.
        _notifier.javadocStarted();  // fire first so it can fire javadocEnded
        _showCompilerError(ipe.getMessage(), file);
         return;
      }
    }

    // Don't attempt to create Javadoc if no files are open, or if open file is unnamed.
    if (docUnits.size() == 0) return;

    // Build the source path.
    StringBuffer sourcePath = new StringBuffer();
    String separator = System.getProperty("path.separator");
    sourceRootSet.addAll(defaultRoots);
    File[] sourceRoots = sourceRootSet.toArray(new File[sourceRootSet.size()]);
    for (int a = 0 ; a  < sourceRoots.length; a++) {
      if (a != 0)  sourcePath.append(separator);
      sourcePath.append(sourceRoots[a].getAbsolutePath());
    }

    // Generate all command line arguments
    ArrayList<String> args = _buildCommandLineArgs(docUnits, destDir,
                                                   sourcePath.toString(),
                                                   classPath);

    // Run the actual Javadoc process
    _runJavadoc(args, classPath, destDirFile, true);
  }



  // -------------------- Javadoc Current Document --------------------

  /**
   * Generates Javadoc for the given document only, after ensuring it is saved.
   * Saves the output to a temporary directory, which is provided in the
   * javadocEnded event.
   *
   * @param doc Document to generate Javadoc for
   * @param saver a command object for saving the document (if it moved/changed)
   * @param classpath a collection of classpath elements to be used by Javadoc
   *
   * @throws IOException if there is a problem manipulating files
   */
  public void javadocDocument(final OpenDefinitionsDocument doc,
                              final FileSaveSelector saver,
                              final String classPath)           throws IOException {
    // Prompt to save if necessary
    //  (TO DO: should only need to save the current document)
    if (doc.isUntitled() || doc.isModifiedSinceSave()) _notifier.saveBeforeJavadoc();

    // Make sure it is saved
    if (doc.isUntitled() || doc.isModifiedSinceSave()) {
      // The user didn't save, so don't generate Javadoc
      return;
    }

    // Try to get the file from the document
    final File file = _getFileFromDocument(doc, saver);

    // Generate to a temporary directory
    final File destDir = FileOps.createTempDirectory("DrJava-javadoc");

    // Start a new thread to do the work.
    new Thread("DrJava Javadoc Thread") {
      public void run() {
//        _javadocDocumentWorker(destDir, file, doc, saver, classpathArray);
        _javadocDocumentWorker(destDir, file, classPath);
      }
    }.start();
  }

  /**
   * Handles most of the logic for generating Javadoc for a single file,
   * once we know that it won't be canceled.
   *
   * @param destDirFile the destination directory for the doc files
   * @param docFile the file of the document
   * @param classpath an array of classpath elements to be used by Javadoc
   */
  private void _javadocDocumentWorker(File destDirFile, File docFile, String classPath) {
    if (!_ensureValidToolsJar()) return;

    // Generate all command line arguments
    String destDir = destDirFile.getAbsolutePath();
    ArrayList<String> args = _buildCommandLineArgs(docFile, destDir, classPath);

    // Run the actual Javadoc process
    _runJavadoc(args, classPath, destDirFile, false);
  }



  // -------------------- Helper Methods --------------------

  /**
   * Suggests a default location for generating Javadoc, based on the given
   * document's source root.  (Appends JavadocModel.SUGGESTED_DIR_NAME to
   * the sourceroot.)
   *
   * Ensures that the document is saved first, or else no reasonable
   * suggestion will be found.
   *
   * @param doc Document with the source root to use as the default.
   * @return Suggested destination directory, or null if none could be
   * determined.
   */
  public File suggestJavadocDestination(OpenDefinitionsDocument doc) {
    _attemptSaveAllDocuments();

    try {
      File sourceRoot = doc.getSourceRoot();
      return new File(sourceRoot, SUGGESTED_DIR_NAME);
    }
    catch (InvalidPackageException ipe) { return null; }
  }

  /**
   * If any documents are modified, this gives the user a chance
   * to save them before proceeding.
   *
   * Callers can check _getter.hasModifiedDocuments() after calling
   * this method to determine if the user cancelled the save process.
   */
  private void _attemptSaveAllDocuments() {
    // Only javadoc if all are saved.
    if (_model.hasModifiedDocuments() || _model.hasUntitledDocuments()) _notifier.saveBeforeJavadoc();
  }

  /**
   * Ensures that a valid version of tools.jar is being used for our classpath.
   * Ends the process with an error and returns false if not.
   *
   * Using JDK 1.4 with a 1.3 tools.jar is invalid, but using JDK 1.3 with
   * a 1.4 tools.jar is ok.
   */
  private boolean _ensureValidToolsJar() {
    PlatformSupport platform = PlatformFactory.ONLY;
    String version = platform.getJavaSpecVersion();
    if (!"1.3".equals(version) && platform.has13ToolsJar()) {
      String msg =
        "There is an incompatible version of tools.jar on your\n" +
        "classpath, so Javadoc cannot run.\n" +
        "(tools.jar is version 1.3, JDK is version " + version + ")";
      _notifier.javadocStarted();  // fire first so it can fire javadocEnded
      _showCompilerError(msg, null);
      return false;
    }
    return true;
  }

  /**
   * Treats the given message as a Javadoc error, firing the
   * end event necessary to show the error.  The javadocStarted() event
   * <i>must</i> have already been fired, and Javadoc generation must
   * halt after calling this method.
   *
   * @param msg Message to display as an error
   * @param f File that caused the error
   */
  private void _showCompilerError(String msg, File f) {
    CompilerError[] errors = new CompilerError[1];
    errors[0] = new CompilerError(f, -1, -1, msg, false);
    _javadocErrorModel = new CompilerErrorModel(errors, _model);
    _notifier.javadocEnded(false, null, false);
  }

  /**
   * Tell the listeners that we're starting to generate Javadoc,
   * start a new process to actually generate it, and then tell
   * the listeners when we're done.
   *
   * @param args Command line arguments to pass to Javadoc
   * @param classpath Classpath to pass to Javadoc
   * @param destDirFile Directory where the results are being saved
   * @param allDocs Whether we are running over all open documents
   */
  private void _runJavadoc(ArrayList<String> args, String classPath,
                           File destDirFile, boolean allDocs) {
    // Start a new process to execute Javadoc and tell listeners it has started
    // And finally, when we're done notify the listeners with a success flag
    boolean result;
    try {
      // Notify all listeners that Javadoc is starting.
      _notifier.javadocStarted();

      result = _javadoc(args.toArray(new String[args.size()]), classPath);

      // If success and we're generating current, make sure the temp
      //  directory gets deleted on exit.
      if (result && !allDocs) FileOps.deleteDirectoryOnExit(destDirFile);

      // Notify all listeners that we're done.
      _notifier.javadocEnded(result, destDirFile, allDocs);
    }
    catch (Throwable e) {
      // This fires javadocEnded, showing the error
      _showCompilerError(e.getMessage(), null);
    }
  }

  /**
   * This function invokes javadoc.  It should work for all versions of Java
   * from 1.3 on, assuming com.sun.tools.javadoc is in the classpath (generally
   * found in the same tools.jar that is needed for using the debugger).
   * [ed. this line is no longer true] - OR javadoc is in the path.
   *
   * TODO: this should be moved to the platform specific area of the code base
   * when we develop the 1.4 javadoc process, which doesn't need to start a new
   * JVM.  (Of course, it can be a fallback for 1.4 also.)
   *
   * @param args the command-line arguments for Javadoc
   * @param classpath an array of classpath elements to use in the Javadoc JVM
   * @return true if Javadoc succeeded in building the HTML, otherwise false
   */
  private boolean _javadoc(String[] args, String classPath) throws IOException {
    final String JAVADOC_CLASS = "com.sun.tools.javadoc.Main";
    Process javadocProcess;
    
    // This was a quick fix in order to get a working jar out.
    
    String jsr14path = DrJava.getConfig().getSetting(OptionConstants.JSR14_LOCATION).toString();
    double version = Double.valueOf(System.getProperty("java.specification.version"));
    String[] jvmArgs = new String[0];
    if (version < 1.5 && jsr14path != null && !jsr14path.equals(""))
      jvmArgs = new String[]{"-Xbootclasspath/p:" + jsr14path};
    
    // We must use this classpath nonsense to make sure our new Javadoc JVM
    // can see everything the interactions pane can see.
    javadocProcess =  ExecJVM.runJVM(JAVADOC_CLASS, args, new String[]{classPath}, jvmArgs, FileOption.NULL_FILE);

    //System.err.println("javadoc started with args:\n" + Arrays.asList(args));

    /* waitFor() call appears to block indefinitely in 1.4.1, because
     * the process will block if output buffers get full.
     * Yes, this is extremely retarded.
     */
//     value = javadocProcess.waitFor();

    // We have to use a busy-wait and vent output buffers.
    LinkedList<String> outLines = new LinkedList<String>();
    LinkedList<String> errLines = new LinkedList<String>();
    boolean done = false;
    while (!done) {
      try {
        Thread.sleep(500);
        javadocProcess.exitValue();
        done = true;
      }
      catch (InterruptedException e) {
        // try again
      }
      catch (IllegalThreadStateException e) {
        ExecJVM.ventBuffers(javadocProcess, outLines, errLines);
      }
    }
    ExecJVM.ventBuffers(javadocProcess, outLines, errLines);
//    System.err.println("Javadoc process completed.");

    // Unfortunately, javadoc returns 1 for normal errors and for exceptions.
    // We cannot tell them apart without parsing.

    ArrayList<CompilerError> errors = _extractErrors(outLines);
    errors.addAll(_extractErrors(errLines));

    _javadocErrorModel = new CompilerErrorModel
      (errors.toArray(new CompilerError[errors.size()]), _model);
//    System.out.println("built Javadoc error model");

    // Returns true if no "real" errors have occurred.
    return _javadocErrorModel.hasOnlyWarnings();
  }

  /**
   * Reads through a LinkedList of text lines, looking for Javadoc errors.
   * This code will detect Exceptions and Errors thrown during generation of
   * the output, as well as errors and warnings generated by Javadoc.
   * This code works for both JDK 1.3 and 1.4, assuming you pass in data
   * from the correct stream.  (Be safe and check both.)
   * @param lines a LinkedList of Strings representing lines of text
   * @return an ArrayList of CompilerErrors corresponding to the text
   */
  private ArrayList<CompilerError> _extractErrors(LinkedList lines) {
    // Javadoc never produces more than 100 errors, so this will never auto-expand.
    ArrayList<CompilerError> errors = new ArrayList<CompilerError>(100);

    final String ERROR_INDICATOR = "Error: ";
    final String EXCEPTION_INDICATOR = "Exception: ";
    final String BAD_FLAG_INDICATOR = "invalid flag:";
    while (lines.size() > 0) {
//         System.out.println("[javadoc raw error] " + output);

      String output = (String) lines.removeFirst();

      // Check for the telltale signs of a thrown exception or error.
      int errStart;
      errStart = output.indexOf(ERROR_INDICATOR);

      // If we haven't found an error, look for an exception.
      if (errStart == -1) {
        errStart = output.indexOf(EXCEPTION_INDICATOR);
      }

      // If we haven't found either, look for a bad flag report.
      if (errStart == -1) {
        errStart = output.indexOf(BAD_FLAG_INDICATOR);
      }

      if (errStart != -1) {
        // If we found one, put the entirety of stderr in one CompilerError.
        StringBuffer buf = new StringBuffer(60 * lines.size());
        buf.append(output);
        while (lines.size() > 0) {
          output = (String) lines.removeFirst();
          buf.append('\n');
          buf.append(output);
        }
        errors.add(new CompilerError(buf.toString(), false));
      }
      else {
        // Otherwise, parser for a normal error message.
        CompilerError error = _parseJavadocErrorLine(output);
        if (error != null) {
          errors.add(error);
//           System.err.println("[javadoc err]" + error);
        }
      }
    }

    return errors;
  }

  /**
   * Parse a line of text written by Javadoc to stderr in order to see if it
   * lists a specific file (and optionally, a line number) and error message.
   * If so, create a corresponding CompilerError.  If an exception stack trace
   * is encountered, all following text is copied into a new CompilerError.
   *
   * @param line the line of JavaDoc error output to parse - possibly null
   * @return if the error output contains the text ".java:", a CompilerError with the file,
   * message, and line number (if present) where the error occurred. Otherwise, returns null.
   */
  private CompilerError _parseJavadocErrorLine(String line) {
    // First things first: check input.
    if (line == null) {
      return null;
    }

    final String JAVA_INDICATOR = ".java:";
    final String GJ_INDICATOR = ".gj:";

    CompilerError error = null;

    // if the line doesn't have a file and a line number, it is context printed out for a previous error.
    // We can ignore it, becuase the user gets this when they click the error message in our GUI.
    int errStart = line.indexOf(JAVA_INDICATOR);

    // Also look for a GJ file extension.
    if (errStart == -1) {
      errStart = line.indexOf(GJ_INDICATOR);
    }

    if (errStart != -1) {
      // filename is everything up to and including the '.java'
      String filename = line.substring(0, errStart+5);

      // line number is all contiguous number characters after the colon
      int lineno = -1;
      StringBuffer linenoString = new StringBuffer();
      int pos = errStart+6;
      while ((line.charAt(pos)>='0') && (line.charAt(pos)<='9')) {
        linenoString.append(line.charAt(pos));
        pos++;
      }
      // Hopefully, there is a colon after the line number but before the error message.
      // If so, record the line number.
      // Otherwise, try to recover by just using everything after ERR_INDICATOR as the error message
      if (line.charAt(pos) == ':') {
        try {
          // Adjust Javadoc's one-based line numbers to our zero-based indeces.
          lineno = Integer.valueOf(linenoString.toString()).intValue() -1;
        } catch (NumberFormatException e) {
        }
      } else {
        pos = errStart;
      }

      // error message is everything after the colon and space that are after the line number
      String errMessage = line.substring(pos+2);

      // check to see if the first word in the error message is "warning"
      boolean isWarning = false;
      if (errMessage.substring(0, 7).equalsIgnoreCase("warning")) {
        isWarning = true;
      }

      if (lineno >= 0) {
        error = new CompilerError(new File(filename), lineno, 0, errMessage, false);
          
      } else {
        error = new CompilerError(new File(filename), errMessage, false);
      }
    }
    return error;
  }



  /**
   * Attempts to get the file from the given document.
   * If the file has moved, we use the given FileSaveSelector to let the user save it
   * in a new location.
   *
   * @param doc OpenDefinitionsDocument from which to get the file
   * @param saver FileSaveSelector to allow the user to save the file if it has moved.
   *
   * @throws IllegalStateException if the doc has no file (hasn't been saved)
   * @throws IOException if the file can't be saved after it was moved
   */
  private File _getFileFromDocument(OpenDefinitionsDocument doc, FileSaveSelector saver)
    throws IOException
  {
    try {
      // This call will abort the iteration if there is no file,
      // unless we can recover (like for a FileMovedException).
      return doc.getFile();
    }
    catch (FileMovedException fme) {
      // The file has moved - prompt the user to recover.
      // XXX: This is probably not thread safe!
      if (saver.shouldSaveAfterFileMoved(doc, fme.getFile())) {
        try {
          doc.saveFileAs(saver);
          return doc.getFile();
        }
        catch (FileMovedException fme2) {
          // If the user is this intent on shooting themselves in the foot,
          // get out of the way.
          fme2.printStackTrace();
          throw new IOException("Could not find file: " + fme2);
        }
      }
      else {
        throw new IllegalStateException("No file exists for this document.");
      }
    }
  }

  /**
   * Builds a list of command line arguments to pass to the new process
   * when generating Javadoc for a collection of files or packages.
   *
   * The list includes arguments to set the sourcepath and to
   * link against online library documentation.
   *
   * @param docUnits All files or packages to include in the Javadoc
   * @param destDir Destination directory to pass
   * @param sourcePath Full sourcepath to pass
   * @param classpath All classpath entries to pass
   */
  protected ArrayList<String> _buildCommandLineArgs(Collection<String> docUnits,
                                                    String destDir,
                                                    String sourcePath,
                                                    String classPath)
  {
    ArrayList<String> args = new ArrayList<String>();
    _addBasicArguments(args, destDir, sourcePath, classPath);
    _addOnlineLinkArguments(args);
    args.addAll(docUnits);
    return args;
  }

  /**
   * Builds a list of command line arguments to pass to the new process
   * when generating Javadoc for a single file.
   *
   * The list does not include arguments for source path or
   * online links to documentation.
   *
   * @param file the file
   * @param destDir Destination directory to pass
   * @param classpath All classpath entries to pass
   */
  protected ArrayList<String> _buildCommandLineArgs(File file, String destDir,
                                                    String classPath)
  {
    ArrayList<String> args = new ArrayList<String>();
    _addBasicArguments(args, destDir, "", classPath);
    _addSingleDocArguments(args);
    args.add(file.getAbsolutePath());
    return args;
  }

  /**
   * Adds all the basic command line arguments to the args list, for
   * generating Javadoc for either a single or collection of files.
   *
   * @param args List of arguments
   * @param destDir Destination directory to pass
   * @param sourcePath Full sourcepath to pass, or the empty string (NOT NULL).
   * @param classpath All classpath entries to pass
   */
  private void _addBasicArguments(ArrayList<String> args,
                                  String destDir,
                                  String sourcePath,
                                  String classPath)
  {
    // Determine the access level
    Configuration config = DrJava.getConfig();
    String accLevel = config.getSetting(OptionConstants.JAVADOC_ACCESS_LEVEL);
    StringBuffer accArg = new StringBuffer(10);
    accArg.append('-');
    accArg.append(accLevel);

    // Add access level, source path, and dest dir
    args.add(accArg.toString());
    if (!sourcePath.equals("")) {
      args.add("-sourcepath");
      args.add(sourcePath);
    }
    args.add("-d");
    args.add(destDir);
    
    // Add classpath
    args.add("-classpath");
    args.add(classPath);

    // Add custom args specified by the user
    String custom = config.getSetting(OptionConstants.JAVADOC_CUSTOM_PARAMS);
    args.addAll(ArgumentTokenizer.tokenize(custom));
  }

  /**
   * Adds command line arguments for links to online library documentation
   * to the given list of command line arguments.
   * @param args List of arguments to modify
   */
  private void _addOnlineLinkArguments(ArrayList<String> args) {
    Configuration config = DrJava.getConfig();
    String linkVersion = config.getSetting(OptionConstants.JAVADOC_LINK_VERSION);
    if (linkVersion.equals(OptionConstants.JAVADOC_1_3_TEXT)) {
      args.add("-link");
      args.add(config.getSetting(OptionConstants.JAVADOC_1_3_LINK));
    }
    else if (linkVersion.equals(OptionConstants.JAVADOC_1_4_TEXT)) {
      args.add("-link");
      args.add(config.getSetting(OptionConstants.JAVADOC_1_4_LINK));
    }
    else if (linkVersion.equals(OptionConstants.JAVADOC_1_5_TEXT)) {
      args.add("-link");
      args.add(config.getSetting(OptionConstants.JAVADOC_1_5_LINK));
    }
  }

  /**
   * Adds command line arguments for generating a single Javadoc file,
   * suppressing most of the navigation on the page.
   * @param args List of arguments to modify.
   */
  private void _addSingleDocArguments(ArrayList<String> args) {
    args.add("-noindex");
    args.add("-notree");
    args.add("-nohelp");
    args.add("-nonavbar");
  }
}
