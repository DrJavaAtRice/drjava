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

package edu.rice.cs.drjava.project;

import java.util.ArrayList;
import java.util.List;
//import java.util.Vector;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.*;

import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.drjava.Version;
import edu.rice.cs.util.Pair;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.swing.Utilities;

import static edu.rice.cs.util.StringOps.*;
import static edu.rice.cs.util.FileOps.*;

/** The internal representation of a project; it is the internal analog of a project file. Includes support for 
 *  writing corresponding project file. 
 */
public class ProjectProfile implements ProjectFileIR {
  
  /* Private fields */
  
  private List<DocFile> _sourceFiles = new ArrayList<DocFile>();
  private List<DocFile> _auxFiles = new ArrayList<DocFile>();
  private List<String> _collapsedPaths = new ArrayList<String>();
  
  private File _buildDir = null;
  private File _workDir = null;
  
  private List<File> _classPathFiles = new ArrayList<File>();
  
  private File _mainClass = null;
  
  /** root of project source tree */
  private File _projectRoot;
  
  private File _projectFile;  /* Invariant: _projectFile.getParentFile() != null */
  
  private File _createJarFile = null;
  
  private int _createJarFlags = 0;
  
  /* Constructors create new ProjectProfiles with specifed project file name and project root that is parent folder of
   * the project file.  The project file presumably may not exist yet.  */
  
  public ProjectProfile(String fileName) { _projectFile = new File(fileName); }
  public ProjectProfile(File f) { 
    _projectFile = f; 
    _projectRoot = _projectFile.getParentFile();
//    Utilities.show("Initial Project Root is " + _projRoot);
    assert _projectRoot != null;
  }
  
  /* Public getters */
  
  /** @return an array of the source files in this project. */
  public DocFile[] getSourceFiles() { return _sourceFiles.toArray(new DocFile[_sourceFiles.size()]); }
    
  /** @return an array full of all the aux files (project outside source tree) in this project. */
  public DocFile[] getAuxiliaryFiles() { return _auxFiles.toArray(new DocFile[_auxFiles.size()]); }
  
  /** @return project file. */
  public File getProjectFile() { return _projectFile; }
    
  /** @return the build directory stored in this project file */
  public File getBuildDirectory() { return _buildDir; }
  
   /** @return the working directory stored in this project profile */
  public File getWorkingDirectory() { return _workDir; }
  
  /** @return an array of path strings correspond to which folders in the tree should not be shown.  Any paths not in 
   *  this list will be expanded when the project is opened.
   */
  public String[] getCollapsedPaths() { return _collapsedPaths.toArray(new String[_collapsedPaths.size()]); }
    
  /** @return an array full of all the classpath path elements in the classpath for this project file */
  public File[] getClassPaths() { return _classPathFiles.toArray(new File[_classPathFiles.size()]); }
  
  /** @return the name of the file that holds the Jar main class associated with this project */
  public File getMainClass() { return _mainClass; }
  
  /** @return the directory that is the root of the project source tree. If project root is not set, returns the parent
   *  of the project file.  Never returns null. */
  public File getProjectRoot() { 
    if ((_projectRoot == null) || _projectRoot.equals(FileOption.NULL_FILE))
      _projectRoot = _projectFile.getParentFile();
    return _projectRoot;
  }
  
  /** @return the output file used in the "Create Jar" dialog. */
  public File getCreateJarFile() { return _createJarFile; }
  
  /** @return the output file used in the "Create Jar" dialog. */
  public int getCreateJarFlags() { return _createJarFlags; }
  
  /** Public setters, modifiers */
  
  public void addSourceFile(DocFile df) { _sourceFiles.add(df); }
  
  public void addSourceFile(DocumentInfoGetter getter) {
    if (!getter.isUntitled()) {
      try { addSourceFile(docFileFromGetter(getter)); }
      catch(IOException e) { throw new UnexpectedException(e); }
    }
  }
  
  public void addAuxiliaryFile(DocFile df) { _auxFiles.add(df); }
    
  public void addAuxiliaryFile(DocumentInfoGetter getter) {
    if (! getter.isUntitled()) {
      try { addAuxiliaryFile(docFileFromGetter(getter)); }
      catch(IOException e) { throw new UnexpectedException(e); }
    }
  }
  
  public void addClassPathFile(File cp) { if (cp != null) _classPathFiles.add(cp); }
  public void addCollapsedPath(String cp) { if (cp != null) _collapsedPaths.add(cp); }
  public void setBuildDirectory(File dir) { 
//    System.err.println("setBuildDirectory(" + dir + ") called");
    _buildDir = FileOps.validate(dir); 
//    System.err.println("Vaidated form is: " + _buildDir);
  }
  public void setWorkingDirectory(File dir) { _workDir = FileOps.validate(dir); }
  public void setMainClass(File main) { _mainClass = main;  }
  public void setSourceFiles(List<DocFile> sf) { _sourceFiles = sf; }
  public void setClassPaths(List<File> cpf) { _classPathFiles = cpf; }
  public void setCollapsedPaths(List<String> cp) { _collapsedPaths = cp; }
  public void setAuxiliaryFiles(List<DocFile> af) { _auxFiles = af; }

  /** Assumes that root.getParentFile != null */
  public void setProjectRoot(File root) { 
    _projectRoot = root; 
    assert root.getParentFile() != null;
  }
  
  public void setCreateJarFile(File createJarFile) { _createJarFile = createJarFile; }
  public void setCreateJarFlags(int createJarFlags) { _createJarFlags = createJarFlags; }
  
  /** This method writes what information has been passed to this builder so far to disk in s-expression format. */
  public void write() throws IOException {
    FileWriter fw = new FileWriter(_projectFile);
    
    // write opening comment line
    fw.write(";; DrJava project file, written by build " + Version.getBuildTimeString());
    fw.write(";; relative files are made relative to: " + _projectFile.getCanonicalPath());
    
    // write the project root
    if (_projectRoot != null) {
      fw.write("\n(proj-root");
//      Utilities.show("Writing project root = " + _projRoot);
      fw.write("\n" + encodeFile(_projectRoot, "  ", true));
      fw.write(")");
    }
    else fw.write("\n;; no project root; should never happen");
        
    // write source files
    if (!_sourceFiles.isEmpty()) {
      fw.write("\n(source");
      for(DocFile df: _sourceFiles) { fw.write("\n" + encodeDocFile(df, "  ")); }
      fw.write(")"); // close the source expression
    }
    else fw.write("\n;; no source files");
    
    // write aux files
    if (!_auxFiles.isEmpty()) {
      fw.write("\n(auxiliary");
      for(DocFile df: _auxFiles) { fw.write("\n" + encodeDocFile(df, "  ", false)); }
      fw.write(")"); // close the auxiliary expression
    }
    else fw.write("\n;; no aux files");
    
    // write collapsed paths
    if (!_collapsedPaths.isEmpty()) {
      fw.write("\n(collapsed");
      for(String s: _collapsedPaths) {
        fw.write("\n  (path " + convertToLiteral(s) + ")");
      }
      fw.write(")"); // close the collapsed expression
    }
    else fw.write("\n;; no collapsed branches");
    
    // write classpaths
    if (!_classPathFiles.isEmpty()) {
      fw.write("\n(classpaths");
      for(File f: _classPathFiles) {
        fw.write("\n" + encodeFile(f, "  ", false));
      }
      fw.write(")"); // close the classpaths expression
    }
    else fw.write("\n;; no classpaths files");
    
    // write the build directory
    if (_buildDir != null && _buildDir.getPath() != "") {
      fw.write("\n(build-dir");
      fw.write("\n" + encodeFile(_buildDir, "  ", true));
      fw.write(")");
    }
    else fw.write("\n;; no build directory");
    
     // write the working directory
    if (_workDir != null && _workDir.getPath() != "") {
      fw.write("\n(work-dir");
      fw.write("\n" + encodeFile(_workDir, "  ", true));
      fw.write(")");
    }
    else fw.write("\n;; no working directory");
    
    // write the main class
    if (_mainClass != null) {
      fw.write("\n(main-class");
      fw.write("\n" + encodeFile(_mainClass, "  ", true));
      fw.write(")");
    }
    else fw.write("\n;; no main class");
    
    // write the create jar file
    if (_createJarFile != null) {
      fw.write("\n(create-jar-file");
      fw.write("\n" + encodeFile(_createJarFile, "  ", true));
      fw.write(")");
    }
    else fw.write("\n;; no create jar file");
    
    // write the create jar flags
    if (_createJarFlags != 0) {
      fw.write("\n(create-jar-flags " + _createJarFlags + ")");
    }
    else fw.write("\n;; no create jar flags");
    
    fw.close();
  }
  

  /* Private Methods */
  
  /** @param getter The getter that can get all the info needed to make the document file
   *  @return the document that contains the information retrieved from the getter
   */
  private DocFile docFileFromGetter(DocumentInfoGetter g) throws IOException {    
      return new DocFile(g.getFile().getCanonicalPath(), g.getSelection(), g.getScroll(), g.isActive(), g.getPackage());
  }
  
  /** This encodes a normal file.  None of the special tags are added.
   *  @param f the file to encode
   *  @param prefix the indent level to place the s-expression at
   *  @param relative whether this file should be made relative to the project path
   *  @return the s-expression syntax to describe the given file.
   */
  private String encodeFile(File f, String prefix, boolean relative) throws IOException {
    String path;
    if (relative) path = makeRelative(f);
    else path = f.getCanonicalPath();

    path = replace(path,File.separator, "/");
    return prefix + "(file (name " + convertToLiteral(path) + "))";
  }
  
  /** This encodes a docfile, adding all the special tags that store document-specific information.
   *  @param df the doc file to encode
   *  @param prefix the indent level to place the s-expression at
   *  @param relative whether this file should be made relative to the project path
   *  @param hasDate whether to include the modification date
   *  @return the s-expression syntax to describe the given docfile.
   */
  private String encodeDocFile(DocFile df, String prefix, boolean relative, boolean hasDate) throws IOException {
    String ret = "";
    String path;
    if (relative) path = makeRelative(df);
    else path = df.getCanonicalPath();

    path = replace(path,File.separator,"/");
    ret += prefix + "(file (name " + convertToLiteral(path) + ")";
    
    Pair<Integer,Integer> p1 = df.getSelection();
    Pair<Integer,Integer> p2 = df.getScroll();
    boolean active = df.isActive();
    long modDate = df.lastModified();
    // Add prefix to the next line if any tags exist
    if (p1 != null || p2 != null || active)  ret += "\n" + prefix + "      ";

    // The next three tags go on the same line (if they exist)
    if (p1 != null) ret += "(select " + p1.getFirst() + " " + p1.getSecond() + ")";

    if (p2 != null) ret += "(scroll " + p2.getFirst() + " " + p2.getSecond() + ")";

    if (hasDate && modDate > 0) {
      String s = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date(modDate));
      ret += "(mod-date " + convertToLiteral(s) + ")";
    }
    
    if (active) ret += "(active)";
    
    // the next tag goes on the next line if at all
    String pack = df.getPackage();
    if (pack != null) {
      ret += "\n" + prefix + "      "; // add prefix
      ret += "(package " + convertToLiteral(pack) + ")";
    }
    
    ret += ")"; // close the file expression
    
    return ret;
  }
  /** Encodes a doc file.  The path defaults to relative.
   *  @param df the DocFile to encode
   *  @param prefix the indent level
   */
  private String encodeDocFile(DocFile df, String prefix) throws IOException {
    return encodeDocFile(df, prefix, true, true);
  }
  private String encodeDocFile(DocFile df, String prefix, boolean relative) throws IOException {
    return encodeDocFile(df, prefix, relative, true);
  }
  
  /** @param f the file whose path to make relative to the project path
   *  @return the string name of the file's path relative to the project path
   */
  private String makeRelative(File f) throws IOException { return makeRelativeTo(f, _projectFile).getPath(); }
}