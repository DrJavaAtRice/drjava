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
import  edu.rice.cs.util.ClasspathVector;

/** The minimum interface that a compiler must meet to be used by DrJava.
 *  @version $Id$
 */
public interface CompilerInterface {
  
  /** Compile the given files.
   *  @param files Source files to compile.
   *  @param sourceRoot Source root directory, the base of the package structure.
   *  @return Array of errors that occurred. If no errors, should be zero length array (not null).
   */
  
  CompilerError[] compile(File sourceRoot, File[] files);
  
  /** Compile the given files.
   *  @param files Source files to compile.
   *  @param sourceRoots Array of source root directories, the base of the package structure for all files to compile.
   *  @return Array of errors that occurred. If no errors, should be zero length array (not null).
   */
  CompilerError[] compile(File[] sourceRoots, File[] files);

  /** Indicates whether this compiler is actually available. As in: Is it installed and located? This method 
   *  should load the compiler class, which should hopefully prove whether the class can load.  If this 
   *  method returns true, the {@link #compile} method should not fail due to class not being found.
   */
  boolean isAvailable();

  /** Returns the name of this compiler, appropriate to show to the user. */
  String getName();

  /** Should return info about compiler, at least including name. */
  String toString();
  
  /** Allows us to set the extra classpath for the compilers without referencing the config object in a loaded class 
   *  file.
   */ 
  void setExtraClassPath(String extraClassPath);
  
  /** Sets the extra classpath in the form of a ClasspathVector. This should include any classpath entries from 
   *  the project's classpath, if any, and the entries from EXTRA_CLASSPATH.
   * @param extraClassPath the classpath to use as the compiler's extra classpath
   */
  void setExtraClassPath(ClasspathVector extraClassPath);
  
  /** Sets whether to allow assertions in Java 1.4.  (Allows us not to reference the config object in a 
   *  loaded class file.)
   */
  void setAllowAssertions(boolean allow);
  
  /** Sets whether or not warnings are allowed. */
  void setWarningsEnabled(boolean warningsEnabled);
  
  /** This method allows us to set the JSR14 collections path across a class loader.
   *  (cannot cast a loaded class to a subclass, so all compiler interfaces must have this method)
   */ 
  void addToBootClassPath(File s);
  
  /** Sets the output directory, or null for default */
  void setBuildDirectory(File dir);
  
}



