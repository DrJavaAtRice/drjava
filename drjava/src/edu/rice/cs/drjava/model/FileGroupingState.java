/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model;

import java.io.File;
import java.util.List;
import java.util.Map;
import edu.rice.cs.util.AbsRelFile;
import edu.rice.cs.drjava.config.OptionParser;

/** This state pattern is used by the global model to store any information pertaining to the currently open project.  
  * The state pattern is used because most project information is not needed in list view. (Elspeth Rocks)
  * 
  * Hint: if you're looking for the instances of this interface, look in AbstractGlobalModel where they are defined 
  * as anonymous inner classes.
  */
public interface FileGroupingState {
  
  /** @return true if the model is in project mode, false otherwi */
  public boolean isProjectActive();
  
  /** @param doc the document to check
    * @return true if the document is part of the active project (in the project path), or false
    * if it is not (or the model is not in project mode)
    */
  public boolean inProjectPath(OpenDefinitionsDocument doc);
  
  /** @param f the file to check
    * @return true if the file is part of the active project (in the project path), or false
    * if it is not (or the model is not in project mode)
    */
  public boolean inProjectPath(File f);
  
//  /** junits all files that the state considers "all" (ie, all files in project directory in project mode). */
//  public void junitAll();
  
  /** Returns the current project file
    * @return null if not currently in a project
    */
  public File getProjectFile();
  
  /** Returns the project source root 
    * @return null if no build directory is specified
    */
  public File getProjectRoot();
  
  /** Sets project file to specifed value; used in "Save Project As ..." command in MainFrame. 
    * @param f the file to be set
    */
  public void setProjectFile(File f);
  
  /** Sets the current project root. 
    * @param f the file to be set as the root
    */
  public void setProjectRoot(File f);
  
  /** Adds file to list of auxiliary files in project 
    * @param f the file to be added
    */
  public void addAuxFile(File f);
  
  /** Removes file to list of auxiliary files in project.  
    * @param f the file to be removed
    */
  public void remAuxFile(File f);
  
  /** Returns the directory in which to put the class files after compilation
    * @return null if no build directory is specified
    */
  public File getBuildDirectory();
  
  /** Sets the current build directory. 
    * @param f the new build directory to be set
    */
  public void setBuildDirectory(File f);
  
  /** @return the working directory for the slave (interactions pane) JVM. */
  public File getWorkingDirectory();
  
  /** Sets the current working directory for the interactions pane. 
    * @param f the new working directory to be set
    */
  public void setWorkingDirectory(File f);
  
  /** Returns the name of the class that has the main method of the project
    * @return null if no build directory is specified
    */
  public String getMainClass();
  
  /** Sets the name of the class that has the main method of the project
    * (Note: should point to the sourcefile of the document, not the class file)
    * @param f the file containing the new main class to be set
    */
  public void setMainClass(String f);
  
  /** Sets the create jar file of the project. 
    * @param f the new create jar file to be set 
    */
  public void setCreateJarFile(File f);
  
  /** @return the create jar file for the project. If not in project mode, returns 0. */
  public File getCreateJarFile();
  
  /** Sets the create jar flags of the project. 
    * @param f the flags to be set
    */
  public void setCreateJarFlags(int f);
  
  /** @return the create jar flags for the project. If not in project mode, returns null. */
  public int getCreateJarFlags();
  
  /** @return all files saved as source files in the project file. If not in project mode, returns null. */
  public File[] getProjectFiles();
  
  /** @param f the file to be searched for within the current project
    * @return true the given file is in the current project file. 
    */
  public boolean inProject(File f);
  
  /** @param f the file to be checked
    * @return true if the file is a project auxiliary file 
    */
  public boolean isAuxiliaryFile(File f);
  
  /** @return true if in project mode and the current project file has changed. */
  public boolean isProjectChanged();
  
  /** Sets that the project state is no longer a snapshot of the open project. 
    * @param changed true if the project has changed; false otherwise
    */
  public void setProjectChanged(boolean changed); 
  
  /** Cleans the build directory. */
  public void cleanBuildDirectory();
  
  /** @return a list of class files. */
  public List<File> getClassFiles();
  
  /** Returns a collection of classpath entries specific to the current project.
    * @return the project's extra classpath
    */
  public Iterable<AbsRelFile> getExtraClassPath();
  
  /** Sets the list of project-specific classpath entries. 
    * @param cp the class path to be set
    */
  public void setExtraClassPath(Iterable<AbsRelFile> cp);
  
  /** Excludes file from the project. 
    * @param f the file to be excluded
    */
  public void addExcludedFile(File f);
  
  /** @param f the file to be checked
    * @return true if the file is excluded from the current project 
    */
  public boolean isExcludedFile(File f);
  
  /** @return an array of the files excluded from the current project */
  public File[] getExclFiles();
  
  /** Remove the specified file from the files excluded from the current project 
    * @param f the file to be removed from the list of exclusions
    */
  public void removeExcludedFile(File f);
  
  /** Sets the array of files excluded from the current project
    * @param fs the list of excluded files to be set
    */
  public void setExcludedFiles(File[] fs);
  
  public boolean getAutoRefreshStatus();
  
  public void setAutoRefreshStatus(boolean b);
  
  /** @return the stored preferences. */
  public Map<OptionParser<?>,String> getPreferencesStoredInProject();
  
  public void setPreferencesStoredInProject(Map<OptionParser<?>,String> sp);
  
  /** Sets the custom manifest on the project 
    * @param manifest the manifest to be set
    */
  public void setCustomManifest(String manifest);
  
  /** @return the custom manifest on the project */
  public String getCustomManifest();
}
