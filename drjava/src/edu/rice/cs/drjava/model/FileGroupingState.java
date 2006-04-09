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
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.util.List;
import java.io.File;
import java.io.IOException;
import edu.rice.cs.drjava.model.FileMovedException;
import edu.rice.cs.util.ClassPathVector;

/** This state pattern is used by the global model to store any information pertaining to the currently open project.  
 *  The state pattern is used because most project information is not needed in list view. (Elspeth Rocks)
 * 
 *  Hint: if you're looking for the instances of this interface, look in DefaultGlobalModel where they are defined 
 *  as anonymous inner classes.
 */
public interface FileGroupingState {

  /** @return true if the model is in project mode, false otherwi */
  public boolean isProjectActive();
  
  /** @return true if the document is part of the active project (in the project path), or false
   *  if it is not (or the model is not in project mode)
   */
  public boolean isInProjectPath(OpenDefinitionsDocument doc);
  
  /** @return true if the file is part of the active project (in the project path), or false
   *  if it is not (or the model is not in project mode)
   */
  public boolean isInProjectPath(File f);
  
//  /** junits all files that the state considers "all" (ie, all files in project directory in project mode). */
//  public void junitAll();
  
  /** Returns the current project file
   *  @return null if not currently in a project
   */
  public File getProjectFile();
  
  /** Returns the project source root 
   *  @return null if no build directory is specified
   */
  public File getProjectRoot();
  
  /** Sets project file to specifed value; used in "Save Project As ..." command in MainFrame. */
  public void setProjectFile(File f);
  
  /** Sets the current project root. */
  public void setProjectRoot(File f);
  
  /** Returns the directory in which to put the class files after compilation
   *  @return null if no build directory is specified
   */
  public File getBuildDirectory();
  
  /** Sets the current build directory. */
  public void setBuildDirectory(File f);
  
  /** Returns the working directory for the slave (interactions pane) JVM. */
  public File getWorkingDirectory();
  
   /** Sets the current working directory for the interactions pane. */
  public void setWorkingDirectory(File f);
  
  /** Returns the source file that has the main method of the project
   *  @return null if no build directory is specified
   */
  public File getMainClass();
  
  /** Sets the file that has the main method of the project
   *  (Note: should point to the sourcefile of the document, not the class file)
   */
  public void setMainClass(File f);
  
  /** Sets the create jar file of the project. */
  public void setCreateJarFile(File f);
  
  /** Return the create jar file for the project. If not in project mode, returns 0. */
  public File getCreateJarFile();
  
  /** Sets the create jar flags of the project. */
  public void setCreateJarFlags(int f);
  
  /** Return the create jar flags for the project. If not in project mode, returns null. */
  public int getCreateJarFlags();
  
  /** Return all files saved as source files in the project file. If not in project mode, returns null. */
  public File[] getProjectFiles();
  
  /** Returns true the given file is in the current project file. */
  public boolean inProject(File f);
  
  /** @return true if the file is a project auxiliary file */
  public boolean isAuxiliaryFile(File f);
  
  /**
   * Returns true if in project mode and the current project file has changed
   */
  public boolean isProjectChanged();
  
  /**
   * Sets that the project state is no longer a snapshot of the open project.
   */
  public void setProjectChanged(boolean changed); 

  /**
   * cleans the build directory
   */
  public void cleanBuildDirectory() throws FileMovedException, IOException;
  
  /** @return a list of class files. */
  public List<File> getClassFiles();
  
  /**
   * Jars all the open documents or the current project
   */
  public void jarAll();
  
  /**
   * Returns a collection of classpath entries specific to the current project.
   * @return the project's extra classpath
   */
  public ClassPathVector getExtraClassPath();
  
  /**
   * Sets the list of project-specific classpath entries.
   */
  public void setExtraClassPath(ClassPathVector cp);
  
}
