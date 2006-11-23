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

package edu.rice.cs.drjava.model.compiler;

import  java.io.File;
import java.util.List;

/** The minimum interface that a compiler must meet to be used by DrJava.
 *  @version $Id$
 */
public interface CompilerInterface {
  
  /** Compile the given files.
   *  @param files  Source files to compile.
   *  @param classPath  Support jars or directories that should be on the classpath.  If @code{null}, the default is used.
   *  @param sourcePath  Location of additional sources to be compiled on-demand.  If @code{null}, the default is used.
   *  @param destination  Location (directory) for compiled classes.  If @code{null}, the default in-place location is used.
   *  @param bootClassPath  The bootclasspath (contains Java API jars or directories); should be consistent with @code{sourceVersion} 
   *                        If @code{null}, the default is used.
   *  @param sourceVersion  The language version of the sources.  Should be consistent with @code{bootClassPath}.  If @code{null},
   *                        the default is used.
   *  @param showWarnings  Whether compiler warnings should be shown or ignored.
   *  @return Errors that occurred. If no errors, should be zero length (not null).
   */
  List<? extends CompilerError> compile(List<? extends File> files, List<? extends File> classPath, 
                                        List<? extends File> sourcePath, File destination, 
                                        List<? extends File> bootClassPath, String sourceVersion, boolean showWarnings);
  
  /** Indicates whether this compiler is actually available. As in: Is it installed and located? This method 
   *  should load the compiler class, which should hopefully prove whether the class can load.  If this 
   *  method returns true, the {@link #compile} method should not fail due to class not being found.
   */
  boolean isAvailable();

  /** Returns the name of this compiler, appropriate to show to the user. */
  String getName();

  /** Should return info about compiler, at least including name. */
  String toString();
  
}
