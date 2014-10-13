/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.util.List;
import java.util.Set;
import javax.swing.filechooser.FileFilter;
import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.plt.reflect.JavaVersion;

/** The minimum interface that a compiler must meet to be used by DrJava.
  * @version $Id: CompilerInterface.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public interface CompilerInterface {
  
  /** Indicates whether this compiler is actually available. As in: Is it installed and located? This method 
    * should load the compiler class, which should hopefully prove whether the class can load.  If this 
    * method returns true, the {@link #compile} method should not fail due to class not being found.
    */
  boolean isAvailable();
  
  /** Compile the given files.
    * @param files  Source files to compile.
    * @param classPath  Support jars or directories that should be on the classpath.  If <code>null</code>, the default is used.
    * @param sourcePath  Location of additional sources to be compiled on-demand.  If <code>null</code>, the default is used.
    * @param destination  Location (directory) for compiled classes.  If <code>null</code>, the default in-place location is used.
    * @param bootClassPath  The bootclasspath (contains Java API jars or directories); should be consistent with
    * <code>sourceVersion</code> 
    *                       If <code>null</code>, the default is used.
    * @param sourceVersion  The language version of the sources.  Should be consistent with <code>bootClassPath</code>.  
    * If <code>null</code>, the default is used.
    * @param showWarnings  Whether compiler warnings should be shown or ignored.
    * @return Errors that occurred. If no errors, should be zero length (not null).
    */
  List<? extends DJError> compile(List<? extends File> files, List<? extends File> classPath, 
                                        List<? extends File> sourcePath, File destination, 
                                        List<? extends File> bootClassPath, String sourceVersion, boolean showWarnings);
  
  /** The latest version of Java supported by the compiler */
  JavaVersion version();
  
  /** Returns the name of this compiler, appropriate to show to the user. */
  String getName();
  
  /** Returns a one-line description of the compiler (such as the name and file location) */
  String getDescription();
  
  /** String to display in a combo box (generally {@code getName()}) */
  String toString();
  
  /** A compiler can instruct DrJava to include additional elements for the boot
    * class path of the Interactions JVM. */
  List<File> additionalBootClassPathForInteractions();
  
  /** Transform the command line to be interpreted into something the Interactions JVM can use.
    * This replaces "java MyClass a b c" with Java code to call MyClass.main(new String[]{"a","b","c"}).
    * "import MyClass" is not handled here.
    * transformCommands should support at least "run", "java" and "applet".
    * @param interactionsString unprocessed command line
    * @return command line with commands transformed */
  String transformCommands(String interactionsString);
  
  /** Return true if the specified file is a source file for this compiler. 
    * @param f file to check if it is a source file
    * @return true if the specified file is a source file for this compiler. */
  boolean isSourceFileForThisCompiler(File f);
  
  /** Return the set of source file extensions that this compiler supports.
    * @return the set of source file extensions that this compiler supports. */
  Set<String> getSourceFileExtensions();

  /** Return the suggested file extension that will be appended to a file without extension.
    * @return the suggested file extension */
  public String getSuggestedFileExtension();
  
  /** Return a file filter that can be used to open files this compiler supports.
    * @return file filter for appropriate source files for this compiler. */
  FileFilter getFileFilter();
  
  /** Return the extension of the files that should be opened with the "Open Folder..." command.
    * @return file extension for the "Open Folder..." command for this compiler. */
  String getOpenAllFilesInFolderExtension();
  
  /** Return the set of keywords that should be highlighted in the specified file.
    * @param f file for which to return the keywords
    * @return the set of keywords that should be highlighted in the specified file. */
  Set<String> getKeywordsForFile(File f);
  
  /** Return true if this compiler can be used in conjunction with the language level facility.
    * @return true if language levels can be used. */
  boolean supportsLanguageLevels();
}
