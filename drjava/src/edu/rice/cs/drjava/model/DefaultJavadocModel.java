/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 *
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.io.*;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.util.newjvm.ExecJVM;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.Configuration;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.drjava.model.compiler.CompilerError;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;

public class DefaultJavadocModel implements JavadocModel {
  
  /**
   * Used by CompilerErrorModel to open documents that have errors.
   */
  private IGetDocuments getter;
  
  /**
   * The error model containing all current Javadoc errors.
   */
  private CompilerErrorModel _javadocErrorModel;
  
  /**
   * Constructor.
   * @param getter used to build the CompilerErrorModel
   */
  public DefaultJavadocModel(IGetDocuments getter) {
    this.getter = getter;
    this._javadocErrorModel = new CompilerErrorModel<CompilerError>(new CompilerError[0], getter);
  }
  
  /**
   * Accessor for the Javadoc error model.
   * @return the CompilerErrorModel managing Javadoc errors.
   */
  public CompilerErrorModel getJavadocErrorModel() {
    return _javadocErrorModel;
  }
  
  /**
   * Clears all current Javadoc errors.
   */
  public void resetJavadocErrors() {
    _javadocErrorModel = new CompilerErrorModel<CompilerError>(new CompilerError[0], getter);
  }
  
  /**
   * Javadocs all open documents, after ensuring that all are saved.
   * The user provides a destination, and the gm provides the package info.
   * @param select a command object for selecting a directory and warning a user
   *        about bad input
   * @param saver a command object for saving a document (if it moved/changed)
   * @param classpath a collection of classpath elements to be used by Javadoc
   * @param listener an object to be notified of start and end events, etc.
   * @throws IOException if there is a problem manipulating files
   * @throws InvalidPackageException if a document has a bad package statement
   */
  public void javadocAll(DirectorySelector select, final FileSaveSelector saver,
                         final List<String> classpath,
                         final JavadocListener listener)
    throws IOException, InvalidPackageException {
    // Only javadoc if all are saved.
    if (getter.hasModifiedDocuments()) {
      listener.saveBeforeJavadoc();
    }
    
    if (getter.hasModifiedDocuments()) {
      // if any files haven't been saved after we told our
      // listeners to do so, don't proceed with the rest
      // of the operation.
      return;
    }
    
    // Make sure that there is at least one saved document.
    List<OpenDefinitionsDocument> docs = getter.getDefinitionsDocuments();
    
    boolean noneYet = true;
    int numDocs = docs.size();
    for (int i = 0; (noneYet && (i < numDocs)); i++) {
      OpenDefinitionsDocument doc = docs.get(i);
      noneYet = doc.isUntitled();
    }
    
    // If there are no saved files, ignore the javadoc command.
    if (noneYet) {
      return;
    }
    
    Configuration config = DrJava.getConfig();
    File destDir = config.getSetting(OptionConstants.JAVADOC_DESTINATION);
    boolean ask = config.getSetting(OptionConstants.JAVADOC_PROMPT_FOR_DESTINATION).booleanValue();
    
    // Get the destination directory via the DirectorySelector, if appropriate.
    try {
      // If we no destination is set, or the user has asked for prompts,
      // ask the user for a destination directory.
      if (destDir.equals(FileOption.NULL_FILE) || ask) {
        if (!destDir.equals(FileOption.NULL_FILE)) {
          destDir = select.getDirectory(destDir);
        }
        else {
          destDir = select.getDirectory(null);
        }
      }
        
      // Make sure the destination is writable.
      while (!destDir.exists() || !destDir.canWrite()) {
        // If the choice was rejected, tell the user and ask again.
        select.warnUser("The destination directory you have chosen\n"
                          + "does not exist or is not readable. Please\n"
                          + "choose another directory.",
                        "Bad Destination");
        destDir = select.getDirectory(null);
      }
    }
    catch (OperationCanceledException oce) {
      // If the user cancels the dialog, silently return.
      return;
    }
    
    // Start a new thread to do the work.
    final File destDirF = destDir;
    new Thread() {
      public void run() {
        _javadocWorker(destDirF, saver, classpath, listener);
      }
    }.start();
  }
    
  /**
   * This method handles most of the logic of performing a Javadoc operation,
   * once we know that it won't be canceled.
   * @param destDir the destination directory for the doc files
   * @param saver a command object for saving a document (if it moved/changed)
   * @param classpath a collection of classpath elements to be used by Javadoc
   * @param listener an object to be notified of start and end events, etc.
   */
  private void _javadocWorker(File destDirFile, FileSaveSelector saver,
                              List<String> classpaths,
                              JavadocListener listener) {
    String destDir = destDirFile.getAbsolutePath();
    
    // Accumulate a set of arguments to JavaDoc - package or file names.
    HashSet<String> docUnits = new HashSet<String>();  // units to send to Javadoc (packages or files)
    HashSet<File> sourceRootSet = new HashSet<File>();  // set of unique source roots for open files
    HashSet<File> defaultRoots = new HashSet<File>();  // source roots for files in default package
    HashSet<String> topLevelPacks = new HashSet<String>();  // top level package names to include

    // This depends on the current value of the "javadoc.all.packages" option.
    boolean docAll = DrJava.getConfig().getSetting(OptionConstants.JAVADOC_FROM_ROOTS).booleanValue();

    // Each document has a package hierarchy to traverse.
    List<OpenDefinitionsDocument> docs = getter.getDefinitionsDocuments();
    for (int i = 0; i < docs.size(); i++) {
      OpenDefinitionsDocument doc = docs.get(i);
      File file = null;
      
      try {
        try {
          // This call will abort the iteration if there is no file,
          // unless we can recover (like for a FileMovedException).
          file = doc.getFile();
        }
        catch (FileMovedException fme) {
          // The file has moved - prompt the user to recover.
          // XXX: This is probably not thread safe!
          if (saver.shouldSaveAfterFileMoved(doc, fme.getFile())) {
            try {
              doc.saveFileAs(saver);
              file = doc.getFile();
            }
            catch (FileMovedException fme2) {
              // If the user is this intent on shooting themselves in the foot,
              // get out of the way.
              throw new UnexpectedException(fme2);
            }
            catch (IOException ioe) {
              throw new UnexpectedException(ioe);
            }
          }
          else {
            continue;
          }
        }
        // After all this garbage, file should be properly initialized.
        File sourceRoot = doc.getSourceRoot();
        String pack = doc.getPackageName();
        
        if (pack.equals("")) {
          if (!defaultRoots.contains(sourceRoot)) {
            // This file uses the default package.
            // Include all the other source files at the source root.
            // But don't do it if we've already done it for this directory.
            defaultRoots.add(sourceRoot);
            File[] javaFiles = sourceRoot.listFiles(FileOps.JAVA_FILE_FILTER);
            
            for (int j = 0; j < javaFiles.length; j++) {
              docUnits.add(javaFiles[j].getAbsolutePath());
            }
          }
        }
        else {
          String topLevelPack;
          File searchRoot;

          int index = pack.indexOf('.');
          if (docAll && index != -1) {
            // We need to doc all packages from the root level down.
            
            // TODO: write a unit test for a package name w/ no dot!
            topLevelPack = pack.substring(0, index);
            searchRoot = new File(sourceRoot, topLevelPack);
          }
          else {
            // Only look in the current package or deeper
            topLevelPack = pack;
            searchRoot = new File(sourceRoot,
                                  pack.replace('.', File.separatorChar));
          }

          // But we don't want to traverse the hierarchy more than once.
          if (!topLevelPacks.contains(topLevelPack)
                || !sourceRootSet.contains(sourceRoot)) {
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
      catch (InvalidPackageException ipe) {
        // Bad package - kill the javadoc operation and display the exception
        // as an error.
        _javadocErrorModel = new CompilerErrorModel
          (new CompilerError[] { new CompilerError(file, -1, -1,
                                                   ipe.getMessage(),
                                                   false) },
           getter);
         listener.javadocStarted();
         listener.javadocEnded(false, null);
         return;
      }
    }
    
    // Don't attempt to create Javadoc if no files are open, or if open file is unnamed.
    if (docUnits.size() == 0) {
      return;
    }
    
    // Build the source path.
    StringBuffer sourcePath = new StringBuffer();
    String separator = System.getProperty("path.separator");
    sourceRootSet.addAll(defaultRoots);
    File[] sourceRoots = (File[]) sourceRootSet.toArray(new File[0]);
    for(int a = 0 ; a  < sourceRoots.length; a++){
      if (a != 0){
        sourcePath.append(separator);
      }
      sourcePath.append(sourceRoots[a].getAbsolutePath());
    }
    
    // Build the "command-line" arguments.
    Configuration config = DrJava.getConfig();
    String accLevel = config.getSetting(OptionConstants.JAVADOC_ACCESS_LEVEL);
    StringBuffer accArg = new StringBuffer(10);
    accArg.append('-');
    accArg.append(accLevel);
    
    ArrayList<String> args = new ArrayList<String>();
    args.add(accArg.toString());
    args.add("-sourcepath");
    args.add(sourcePath.toString());
    args.add("-d");
    args.add(destDir);
    
    // Add classpath
    args.add("-classpath");
    String[] classpath = classpaths.toArray(new String[0]);
    StringBuffer cp = new StringBuffer();
    String sep = System.getProperty("path.separator");
    for (int i=0; i < classpath.length; i++) {
      cp.append(classpath[i]);
      cp.append(sep);
    }
    args.add(cp.toString());
    
    String linkVersion = config.getSetting(OptionConstants.JAVADOC_LINK_VERSION);
    if (linkVersion.equals(OptionConstants.JAVADOC_1_3_TEXT)) {
      args.add("-link");
      args.add(config.getSetting(OptionConstants.JAVADOC_1_3_LINK));
    }
    else if (linkVersion.equals(OptionConstants.JAVADOC_1_4_TEXT)) {
      args.add("-link");
      args.add(config.getSetting(OptionConstants.JAVADOC_1_4_LINK));
    }
    
    String custom = config.getSetting(OptionConstants.JAVADOC_CUSTOM_PARAMS);
    args.addAll(ArgumentTokenizer.tokenize(custom));
/*    StreamTokenizer st = new StreamTokenizer(new StringReader(custom));
    st.ordinaryChars('\u0021','\u00ff');
    st.wordChars('\u0021','\u00ff');
    
    try {
      while (st.nextToken() != StreamTokenizer.TT_EOF) {
        if ((st.ttype == StreamTokenizer.TT_WORD)
              || (st.ttype == '"'))
        {
          args.add(st.sval);
        }
        else {
          throw new IllegalArgumentException("Unknown token type: " + st.ttype);
        }
      }
    }
    catch (IOException ioe) {
      // Can't happen with a StringReader.
      throw new UnexpectedException(ioe);
    }
    */
    args.addAll(docUnits);
    

    // Start a new Thread to execute Javadoc and tell listeners it has started
    // And finally, when we're done notify the listeners with a success flag
    boolean result = false;
    try {
      // Notify all listeners that Javadoc is starting.
      listener.javadocStarted();
      
      result = _javadoc_1_3((String[]) args.toArray(new String[0]), classpath);
    }
    catch (Throwable e) {
      e.printStackTrace();
      throw new UnexpectedException(e);
    }
    finally {
      // Notify all listeners that Javadoc is done.
      listener.javadocEnded(result, destDirFile);
    }
  }

  /**
   * This function invokes javadoc.  It should work for all versions of Java
   * from 1.3 on, assuming com.sun.tools.javadoc is in the classpath (generally
   * found in the same tools.jar that is needed for using the debugger).
   * [ed. this line is no longer true] - OR javadoc is in the path.
   * TODO: this should be moved to the platform specific area of the code base
   * when we develop the 1.4 javadoc process, which doesn't need to start a new
   * JVM.  (Of course, it can be a fallback for 1.4 also.)
   * @param args the command-line arguments for Javadoc
   * @param classpath an array of classpath elements to use in the Javadoc JVM
   * @return true if Javadoc succeeded in building the HTML, otherwise false
   */
  private boolean _javadoc_1_3(String[] args, String[] classpath)
    throws IOException, ClassNotFoundException, InterruptedException {
    final String JAVADOC_CLASS = "com.sun.tools.javadoc.Main";
    Process javadocProcess;
    
    // We must use this classpath nonsense to make sure our new Javadoc JVM
    // can see everything the interactions pane can see.
    javadocProcess =  ExecJVM.runJVM(JAVADOC_CLASS, args, classpath, new String[0]);
    
//    System.err.println("javadoc started with args:\n" + Arrays.asList(args));
    
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

    ArrayList<CompilerError> errors = extractErrors(outLines);
    errors.addAll(extractErrors(errLines));
  
    _javadocErrorModel = new CompilerErrorModel
      ((CompilerError[])(errors.toArray(new CompilerError[errors.size()])), getter);
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
  private ArrayList<CompilerError> extractErrors(LinkedList lines) {
    // Javadoc never produces more than 100 errors, so this will never auto-expand.
    ArrayList<CompilerError> errors = new ArrayList<CompilerError>(100);
    
    final String ERROR_INDICATOR = "Error: ";
    final String EXCEPTION_INDICATOR = "Exception: ";
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
        CompilerError error = parseJavadocErrorLine(output);
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
  private CompilerError parseJavadocErrorLine(String line) {
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
        error = new CompilerError(new File(filename), lineno, 0, errMessage, isWarning);
      } else {
        error = new CompilerError(new File(filename), errMessage, isWarning);
      }
    }
    return error;
  }
}
