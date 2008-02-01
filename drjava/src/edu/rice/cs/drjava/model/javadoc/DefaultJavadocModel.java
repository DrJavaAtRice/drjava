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

package edu.rice.cs.drjava.model.javadoc;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Properties;

import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.concurrent.ConcurrentUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.FileMovedException;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.Configuration;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.drjava.model.compiler.CompilerError;

import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.util.DirectorySelector;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.OperationCanceledException;

import static edu.rice.cs.plt.debug.DebugUtil.error;

/** Default implementation of JavadocModel interface; generates Javadoc HTML files for a set of documents.
 *  @version $Id$
 */
public class DefaultJavadocModel implements JavadocModel {

  /** Used by CompilerErrorModel to open documents that have errors. */
  private GlobalModel _model;

  /**Manages listeners to this model. */
  private final JavadocEventNotifier _notifier = new JavadocEventNotifier();

  /** Location of the java command to use (if not the default in {@code java.home}) */
  private final File _javaCommand;

  /** Location of the tools library containing the javadoc code (if not on the javaCommand's boot class path) */
  private final Iterable<File> _toolsPath;
  
  /** The error model containing all current Javadoc errors. */
  private CompilerErrorModel _javadocErrorModel;
  
  /**
   * Main constructor.
   * @param model Source of documents for this JavadocModel
   * @param javaCommand  Location of the java command to use (if not the default in {@code java.home})
   * @param toolsPath  Location of the tools library containing the javadoc code (if not on the 
   *                   javaCommand's boot class path)
   */
  public DefaultJavadocModel(GlobalModel model, File javaCommand, Iterable<File> toolsPath) {
    _model = model;
    _javaCommand = javaCommand;
    _toolsPath = toolsPath;
    _javadocErrorModel = new CompilerErrorModel();
  }
  
  public boolean isAvailable() { return true; }

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
  
  /** Javadocs all open documents, after ensuring that all are saved.  The user provides a destination, and the global 
    * model provides the package info.  Must run in the event-handling thread.
    * @param select a command object for selecting a directory and warning a user about bad input
    * @param saver a command object for saving a document (if it moved/changed)
    * @param classPath a collection of classpath elements to be used by Javadoc
    * @throws IOException if there is a problem manipulating files
    */
  public void javadocAll(DirectorySelector select, final FileSaveSelector saver) throws IOException {
        
    /* Only javadoc if all are saved. Removed because it is already done inside suggestJavadocDestination; fixes bug 
       where pop-up is shown twice) */
    if (_model.hasModifiedDocuments() || _model.hasUntitledDocuments()) { return; }  /* abort if files remain unsaved */
    
    Configuration config = DrJava.getConfig();
    File destDir = config.getSetting(OptionConstants.JAVADOC_DESTINATION);
    
    // Get the destination directory via the DirectorySelector, if appropriate.
    try {
      if (destDir.equals(FileOps.NULL_FILE)) {
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
            ("The directory you chose does not exist:\n'" + destDir + "'\nWould you like to create it?",
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
  
    _notifier.javadocStarted();  // fire first so _javadocAllWorker can fire javadocEnded
    // Start a new thread to do the work.
    final File destDirF = destDir;
    new Thread("DrJava Javadoc Thread") {
      public void run() { _javadocAllWorker(destDirF, saver); }
    }.start();
  }

  /** This method handles most of the logic of performing a Javadoc operation, once we know that it won't be canceled.
    * @param destDirFile the destination directory for the doc files
    * @param saver a command object for saving a document (if it moved/changed)
    * @param classpath an array of classpath elements to be used by Javadoc
    */
  private void _javadocAllWorker(File destDirFile, FileSaveSelector saver) {
    // Note: JAVADOC_FROM_ROOTS is intended to set the -subpackages flag, but I don't think that's something
    // we should support -- in general, we only support performing operations on the files that are open.

    List<String> docFiles = new ArrayList<String>(); // files to send to Javadoc

    for (OpenDefinitionsDocument doc: _model.getOpenDefinitionsDocuments()) {
      try {
        // This will throw an IllegalStateException if no file can be found
        File file = _getFileFromDocument(doc, saver);
        docFiles.add(file.getPath());
      }
      catch (IllegalStateException e) {
        // Something wrong with _getFileFromDocument; ignore
      }
      catch (IOException ioe) {
        // can't access file; ignore
      }
    }

    // Don't attempt to create Javadoc if no files are open, or if open file is unnamed.
    if (docFiles.size() == 0) return;

    // Run the actual Javadoc process
    _runJavadoc(docFiles, destDirFile, IterUtil.<String>empty(), false);
  }



  // -------------------- Javadoc Current Document --------------------

  /** Generates Javadoc for the given document only, after ensuring it is saved. Saves the output in a temp directory
    * which is passed to _javadocDocuemntWorker, which is passed to a subsequent javadocEnded event.
    * @param doc Document to generate Javadoc for
    * @param saver a command object for saving the document (if it moved/changed)
    * @param classPath a collection of classpath elements to be used by Javadoc
    *
    * @throws IOException if there is a problem manipulating files
    */
  public void javadocDocument(final OpenDefinitionsDocument doc, final FileSaveSelector saver) throws IOException {
    // Prompt to save if necessary
    //  (TO DO: should only need to save the current document)
    if (doc.isUntitled() || doc.isModifiedSinceSave()) _notifier.saveBeforeJavadoc();

    // Make sure it is saved
    if (doc.isUntitled() || doc.isModifiedSinceSave()) return;  // The user didn't save, so don't generate Javadoc

    // Try to get the file from the document
    final File file = _getFileFromDocument(doc, saver);

    // Generate to a temporary directory
    final File destDir = IOUtil.createAndMarkTempDirectory("DrJava-javadoc", "");

    _notifier.javadocStarted();  // fire first so _javadocDocumntWorker can fire javadocEnded
    // Start a new thread to do the work.
    new Thread("DrJava Javadoc Thread") {
      public void run() {
        Iterable<String> extraArgs = IterUtil.make("-noindex", "-notree", "-nohelp", "-nonavbar");
        _runJavadoc(IterUtil.make(file.getPath()), destDir, extraArgs, true);
      }
    }.start();
  }

  // -------------------- Helper Methods --------------------

  /** Suggests a default location for generating Javadoc, based on the given document's source root.  (Appends 
    * JavadocModel.SUGGESTED_DIR_NAME to the sourceroot.) Ensures that the document is saved first, or else no 
    * reasonable suggestion will be found.
    * @param doc Document with the source root to use as the default.
    * @return Suggested destination directory, or null if none could be determined.
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
   * Run a new process to generate javdocs, and then tell the listeners when we're done.
   *
   * @param files  List of files to generate
   * @param destDir  Directory where the results are being saved
   * @param extraArgs  List of additional arguments to use with javadoc (besides those gathered from config settings)
   * @param allDocs  Whether this is running on all documents
   */
  private void _runJavadoc(Iterable<String> files, File destDir, Iterable<String> extraArgs, boolean allDocs) {
    Iterable<String> args = IterUtil.empty();
    args = IterUtil.compose(args, IterUtil.make("-d", destDir.getPath()));
    args = IterUtil.compose(args, IterUtil.make("-classpath", IOUtil.pathToString(_model.getClassPath())));
    args = IterUtil.compose(args, _getLinkArgs());
    args = IterUtil.compose(args, "-" + DrJava.getConfig().getSetting(OptionConstants.JAVADOC_ACCESS_LEVEL));
    args = IterUtil.compose(args, extraArgs);
    String custom = DrJava.getConfig().getSetting(OptionConstants.JAVADOC_CUSTOM_PARAMS);
    args = IterUtil.compose(args, ArgumentTokenizer.tokenize(custom));
    args = IterUtil.compose(args, files);
    
    File javaCommand = (_javaCommand == null) ? new File(System.getProperty("java.home", "")) : _javaCommand;
    Iterable<File> jvmClassPath = (_toolsPath == null) ? IterUtil.<File>empty() : _toolsPath;
    
    List<CompilerError> errors = new ArrayList<CompilerError>();
    try {
      Process p = ConcurrentUtil.runJavaProcess(javaCommand, "com.sun.tools.javadoc.Main", args, 
                                                jvmClassPath, new File(System.getProperty("user.dir", "")),
                                                new Properties(), IterUtil.<String>empty());
      Thunk<String> outputString = ConcurrentUtil.processOutAsString(p);
      Thunk<String> errorString = ConcurrentUtil.processErrAsString(p);
      p.waitFor();
      errors.addAll(_extractErrors(outputString.value()));
      errors.addAll(_extractErrors(errorString.value()));
    }
    catch (IOException e) {
      errors.add(new CompilerError("IOException: " + e.getMessage(), false));
    }
    catch (InterruptedException e) {
      errors.add(new CompilerError("InterruptedException: " + e.getMessage(), false));
    }
    
    _javadocErrorModel = new CompilerErrorModel(IterUtil.asArray(errors, CompilerError.class), _model);
    
    // waitFor() exit value is 1 for both errors and warnings, so it's no use
    boolean success = _javadocErrorModel.hasOnlyWarnings();
    if (success && !allDocs) { IOUtil.deleteOnExitRecursively(destDir); }
    _notifier.javadocEnded(success, destDir, allDocs);
  }
  
  private Iterable<String> _getLinkArgs() {
    Configuration config = DrJava.getConfig();
    String linkVersion = config.getSetting(OptionConstants.JAVADOC_LINK_VERSION);
    if (linkVersion.equals(OptionConstants.JAVADOC_1_3_TEXT)) {
      return IterUtil.make("-link", config.getSetting(OptionConstants.JAVADOC_1_3_LINK));
    }
    else if (linkVersion.equals(OptionConstants.JAVADOC_1_4_TEXT)) {
      return IterUtil.make("-link", config.getSetting(OptionConstants.JAVADOC_1_4_LINK));
    }
    else if (linkVersion.equals(OptionConstants.JAVADOC_1_5_TEXT)) {
      return IterUtil.make("-link", config.getSetting(OptionConstants.JAVADOC_1_5_LINK));
    }
    else {
      // should never happen -- use an enum to guarantee
      return IterUtil.empty();
    }
  }

  /**
   * Reads through javadoc output text, looking for Javadoc errors.  This code will detect Exceptions and 
   * Errors thrown during generation of the output, as well as errors and warnings generated by Javadoc.
   * This code works for both JDK 1.3 and 1.4, assuming you pass in data from the correct stream.  (Be safe 
   * and check both.)
   */
  private List<CompilerError> _extractErrors(String text) {
    BufferedReader r = new BufferedReader(new StringReader(text));
    List<CompilerError> result = new ArrayList<CompilerError>();

    String[] errorIndicators = new String[]{ "Error: ", "Exception: ", "invalid flag:" };
    
    try {
      String output = r.readLine();
      while (output != null) {
        if (TextUtil.containsAny(output, errorIndicators)) {
          // If we found one, put the remaining stream contents in one CompilerError.
          result.add(new CompilerError(output + '\n' + IOUtil.toString(r), false));
        }
        else {
          // Otherwise, parser for a normal error message.
          CompilerError error = _parseJavadocErrorLine(output);
          if (error != null) { result.add(error); }
        }
        output = r.readLine();
      }
    }
    catch (IOException e) { error.log(e); /* should not happen, since we're reading from a string */ }

    return result;
  }

  /** Convert a line of Javadoc text to a CompilerError.  If unable to do so, returns {@code null}. */
  private CompilerError _parseJavadocErrorLine(String line) {
    int errStart = line.indexOf(".java:");
    if (errStart == -1) { return null; /* ignore the line if it doesn't have file info */ }
    // filename is everything up to and including the '.java'
    String fileName = line.substring(0, errStart+5);
    
    // line number is all contiguous number characters after the colon
    int lineno = -1;
    final StringBuilder linenoString = new StringBuilder();
    int pos = errStart+6;
    while ((line.charAt(pos) >= '0') && (line.charAt(pos) <= '9')) {
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
    
    if (lineno >= 0) { return new CompilerError(new File(fileName), lineno, 0, errMessage, isWarning); }
    else { return new CompilerError(new File(fileName), errMessage, isWarning); }
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
  private File _getFileFromDocument(OpenDefinitionsDocument doc, FileSaveSelector saver) throws IOException {
    try {
      // This call will abort the iteration if there is no file, unless we can recover (like for a FileMovedException).
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

}
