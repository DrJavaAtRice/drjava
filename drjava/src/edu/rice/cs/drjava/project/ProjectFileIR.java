/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.project;

import java.io.*;
import java.util.List;

import edu.rice.cs.drjava.model.debug.DebugBreakpointData;
import edu.rice.cs.drjava.model.debug.DebugWatchData;

public interface ProjectFileIR {
  /** @return an array full of all the source files in this project file. */
  public DocFile[] getSourceFiles();
    
  /** These files are the files outside of the project tree that are saved in the project file so that they are opened
   *  as well as the project files.
   *  @return an array full of all the aux files in this project file
   */
  public DocFile[] getAuxiliaryFiles();
    
  /** @return the build directory stored in this project file */
  public File getBuildDirectory();
  
   /** @return the working directory stored in this project file */
  public File getWorkingDirectory();
  
  /** @return an array of path strings correspond to which folders in the tree should not be shown.  Any paths not in 
   *  this list will be expanded when the project is opened.
   */
  public String[] getCollapsedPaths();
    
  /** @return an array full of all the classpath path elements in the classpath for this project file */
  public File[] getClassPaths();
  
  /** @return the name of the file that holds the Jar main class associated with this project */
  public File getMainClass();
  
  /** @return the project file for this project. */
  public File getProjectFile();
  
  /** @return the directory that is the root of the project source tree. */
  public File getProjectRoot();
  
  /** @return the output file used in the "Create Jar" dialog. */
  public File getCreateJarFile();
  
  /** @return the flags used in the "Create Jar" dialog. */
  public int getCreateJarFlags();
  
  /** @return the array of breakpoints. */
  public DebugBreakpointData[] getBreakpoints();
  
  /** @return the array of watches. */
  public DebugWatchData[] getWatches();
  
  public void setSourceFiles(List<DocFile> sf);
  public void setAuxiliaryFiles(List<DocFile> aux);
  public void setCollapsedPaths(List<String> paths);
  public void setClassPaths(List<? extends File> cp);
  public void setBuildDirectory(File dir);
  public void setWorkingDirectory(File dir);
  public void setMainClass(File main);
  public void setProjectRoot(File root);
  public void setCreateJarFile(File createJarFile);
  public void setCreateJarFlags(int createJarFlags);
  public void setBreakpoints(List<DebugBreakpointData> bps);
  public void setWatches(List<DebugWatchData> ws);
}
