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

package edu.rice.cs.drjava.project;

import java.io.*;
import java.util.List;

import edu.rice.cs.drjava.model.FileRegion;
import edu.rice.cs.drjava.model.debug.DebugBreakpointData;
import edu.rice.cs.drjava.model.debug.DebugWatchData;
import edu.rice.cs.util.AbsRelFile;

public interface ProjectFileIR {
  /** @return an array full of all the source files in this project file. */
  public DocFile[] getSourceFiles();
    
  /** These files are the files outside of the project tree that are saved in the project file so that they are opened
   *  as well as the project files.
   *  @return an array full of all the aux files in this project file
   */
  public DocFile[] getAuxiliaryFiles();
  
  /** These files are in the project source tree, but have been explicitly excluded from the project. 
    * @return an array full of all the excluded files in this project file.  
    */
  public DocFile[] getExcludedFiles();
    
  /** @return the build directory stored in this project file */
  public File getBuildDirectory();
  
   /** @return the working directory stored in this project file */
  public File getWorkingDirectory();
  
  /** @return an array of path strings correspond to which folders in the tree should not be shown.  Any paths not in 
   *  this list will be expanded when the project is opened.
   */
  public String[] getCollapsedPaths();
    
  /** @return an array full of all the classpath path elements in the classpath for this project file */
  public Iterable<AbsRelFile> getClassPaths();
  
  /** @return the fully qualified name of the class that holds the Jar main class associated with this project */
  public String getMainClass();
  
  /** @return the File that contains the class specified by getMainClass() */
  public File getMainClassContainingFile();
  
  /** @return the project file for this project. */
  public File getProjectFile();
  
  /** @return the directory that is the root of the project source tree. */
  public File getProjectRoot();
  
  /** @return the output file used in the "Create Jar" dialog. */
  public File getCreateJarFile();
  
  /** @return the flags used in the "Create Jar" dialog. */
  public int getCreateJarFlags();
  
  /** @return the array of bookmarks. */
  public FileRegion[] getBookmarks();
  
  /** @return the array of breakpoints. */
  public DebugBreakpointData[] getBreakpoints();
  
  /** @return the array of watches. */
  public DebugWatchData[] getWatches();
  
  public boolean getAutoRefreshStatus();
  
  public void setSourceFiles(List<DocFile> sf);
  public void setAuxiliaryFiles(List<DocFile> aux);
  public void setExcludedFiles(List<DocFile> ef);
  public void setCollapsedPaths(List<String> paths);
  public void setClassPaths(Iterable<? extends AbsRelFile> cp);
  public void setBuildDirectory(File dir);
  public void setWorkingDirectory(File dir);
  public void setMainClass(String main);
  public void setProjectRoot(File root);
  public void setCreateJarFile(File createJarFile);
  public void setCreateJarFlags(int createJarFlags);
  public void setBookmarks(List<? extends FileRegion> bms);
  public void setBreakpoints(List<? extends DebugBreakpointData> bps);
  public void setWatches(List<? extends DebugWatchData> ws);
  public void setAutoRefreshStatus(boolean b);
  
  /**
   * The version of dr java that created this project (as determined from its serialization as a .pjt or .drjava or .xml file)
   * 
   * @return The version string, if known, or "unknown" otherwise.
   */
  public String getDrJavaVersion();
  
  /**
   * Sets the version of DrJava that built this project.
   * 
   * @param version - the version string, should be called with "unknown" if the version could not be determined.
   */
  public void setDrJavaVersion(String version);
  
  /**
   * Accessor for custom manifest in project.
   * Note that the existance of such a manifest does not mean
   * that the custom manifest is in USE.
   * That depends on other JAR creation settings.
   * 
   * @see #getCreateJarFlags()
   */
  public String getCustomManifest();
  
  /**
   * Mutator for custom manifest.
   */
  public void setCustomManifest(String manifest);
}
