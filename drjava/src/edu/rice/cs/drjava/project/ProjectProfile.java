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

package edu.rice.cs.drjava.project;

import java.util.ArrayList;
import java.util.List;
//import java.util.Vector;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.io.*;

import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.drjava.Version;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.drjava.model.DocumentRegion;
import edu.rice.cs.drjava.model.debug.DebugBreakpointData;
import edu.rice.cs.drjava.model.debug.DebugWatchData;
import edu.rice.cs.drjava.model.debug.DebugException;

import static edu.rice.cs.util.StringOps.*;

/** The internal representation of a project; it is the internal analog of a project file. Includes support for 
 *  writing corresponding project file. 
 */
public class ProjectProfile implements ProjectFileIR {
  static final String MOD_DATE_FORMAT_STRING = "dd-MMM-yyyy HH:mm:ss";
  static final DateFormat MOD_DATE_FORMAT =
    new SimpleDateFormat(MOD_DATE_FORMAT_STRING, Locale.US);
  
  /* Private fields */
  
  private List<DocFile> _sourceFiles = new ArrayList<DocFile>();
  private List<DocFile> _auxFiles = new ArrayList<DocFile>();
  private List<String> _collapsedPaths = new ArrayList<String>();
  
  private File _buildDir = FileOps.NULL_FILE;
  private File _workDir = FileOps.NULL_FILE;
  
  private List<File> _classPathFiles = new ArrayList<File>();
  
  private File _mainClass = null;
  
  /** root of project source tree.  Invariant: _projectRoot.exists() */
  private File _projectRoot; /* Invariant after init: _projectRoot.exists() implying _projectRoot != null. */
  
  private File _projectFile;  /* Invariant after init: _projectFile.getParentFile().exists() implying _projectFile != null */
  
  private File _createJarFile = FileOps.NULL_FILE;
  
  private int _createJarFlags = 0;
  
  private List<DocumentRegion> _bookmarks = new ArrayList<DocumentRegion>();
  private List<DebugBreakpointData> _breakpoints = new ArrayList<DebugBreakpointData>();
  private List<DebugWatchData> _watches = new ArrayList<DebugWatchData>();
  
  /** Constructs a File for fileName and forwards this call to the main constructor. */
  public ProjectProfile(String fileName) throws IOException { this(new File(fileName)); }
  
  /** Creates new ProjectProfiles with specifed project file name and project root that is parent folder of
    * the project file.  The project file presumably may not exist yet, but its parent folder is assumed to exist.
    * Assumes that the File f is not a null reference.
    * @throws IOException parent directory of project file does not exist.
    */
  public ProjectProfile(File f) throws IOException { 
    _projectFile = f; 
    _projectRoot = _projectFile.getParentFile();
    if (! _projectRoot.exists()) throw new IOException("Parent directory of project root " + _projectRoot + 
                                                       " does not exist");
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
  public Iterable<File> getClassPaths() { return _classPathFiles; }
  
  /** @return the name of the file that holds the Jar main class associated with this project */
  public File getMainClass() { return _mainClass; }
  
  /** @return the project root directory which must exist. */
  public File getProjectRoot() { return _projectRoot; }
  
  /** @return the output file used in the "Create Jar" dialog. */
  public File getCreateJarFile() { return _createJarFile; }
  
  /** @return the output file used in the "Create Jar" dialog. */
  public int getCreateJarFlags() { return _createJarFlags; }
  
  /** @return an array of the bookmarks in this project. */
  public DocumentRegion[] getBookmarks() { return _bookmarks.toArray(new DocumentRegion[_bookmarks.size()]); }
  
  /** @return an array of the breakpoints in this project. */
  public DebugBreakpointData[] getBreakpoints() { return _breakpoints.toArray(new DebugBreakpointData[_breakpoints.size()]); }
  
  /** @return an array of the watches in this project. */
  public DebugWatchData[] getWatches() { return _watches.toArray(new DebugWatchData[_watches.size()]); }
  
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
// removed call to validate to allow build directory that doesn't exist:
// it will be created when necessary
    _buildDir = dir; // FileOps.validate(dir); 
//    System.err.println("Vaidated form is: " + _buildDir);
  }
  public void setWorkingDirectory(File dir) { _workDir = FileOps.validate(dir); }
  public void setMainClass(File main) { _mainClass = main;  }
  public void setSourceFiles(List<DocFile> sf) { _sourceFiles = new ArrayList<DocFile>(sf); }
  public void setClassPaths(Iterable<? extends File> cpf) {
    _classPathFiles = new ArrayList<File>();
    for (File f : cpf) { _classPathFiles.add(f); }
  }
  public void setCollapsedPaths(List<String> cp) { _collapsedPaths = new ArrayList<String>(cp); }
  public void setAuxiliaryFiles(List<DocFile> af) { _auxFiles = new ArrayList<DocFile>(af); }

  /** Assumes that root.getParentFile != null */
  public void setProjectRoot(File root) { 
    _projectRoot = root; 
    assert root.getParentFile() != null;
  }
  
  public void setCreateJarFile(File createJarFile) { _createJarFile = createJarFile; }
  public void setCreateJarFlags(int createJarFlags) { _createJarFlags = createJarFlags; }
  
  public void setBookmarks(List<? extends DocumentRegion> bms) { _bookmarks = new ArrayList<DocumentRegion>(bms); }
  public void setBreakpoints(List<? extends DebugBreakpointData> bps) { _breakpoints = new ArrayList<DebugBreakpointData>(bps); }
  public void setWatches(List<? extends DebugWatchData> ws) { _watches = new ArrayList<DebugWatchData>(ws); }
  
  /** This method writes what information has been passed to this builder so far to disk in s-expression format. */
  public void write() throws IOException {
    FileWriter fw = new FileWriter(_projectFile);
    
    // write opening comment line
    fw.write(";; DrJava project file, written by build " + Version.getBuildTimeString());
    fw.write("\n;; files in the source tree are relative to: " + _projectRoot.getCanonicalPath());
    fw.write("\n;; other files with relative paths are rooted at (the parent of) this project file");
    
    // write the project root
    /* In the new project file form, this property has been renamed "proj-root-and-base" (instead of "proj-root") to
     * indicate that the project root now serves as the base for source file path names. */
    assert (_projectRoot != null);

    fw.write("\n(proj-root-and-base");
//      Utilities.show("Writing project root = " + _projRoot);
    fw.write("\n" + encodeFileRelative(_projectRoot, "  ", _projectFile));
    fw.write(")");

    // write source files
    /* This property has been renamed "source-files" (instead of "source") so that old versions of DrJava will not 
     * recognize it.  In the new project file format, source files are relative to the project root, not the parent
     * of the project file. */
    if (!_sourceFiles.isEmpty()) {
      fw.write("\n(source-files");
      DocFile active = null;
      for(DocFile df: _sourceFiles) {
        if(df.isActive()) {
          active = df;
          fw.write("\n" + encodeDocFileRelative(df, "  "));
          break; //Assert that there is only one active document in the project
        }
      }
      for(DocFile df: _sourceFiles) { 
        if(df != active)
          fw.write("\n" + encodeDocFileRelative(df, "  "));
      }
      fw.write(")"); // close the source expression
    }
    else fw.write("\n;; no source files");
    
    // write aux files
    if (!_auxFiles.isEmpty()) {
      fw.write("\n(auxiliary");
      for(DocFile df: _auxFiles) { fw.write("\n" + encodeDocFileAbsolute(df, "  ")); }
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
        fw.write("\n" + encodeFileAbsolute(f, "  "));
      }
      fw.write(")"); // close the classpaths expression
    }
    else fw.write("\n;; no classpaths files");
    
    // write the build directory
    if (_buildDir != null && _buildDir.getPath() != "") {
      fw.write("\n(build-dir");
      fw.write("\n" + encodeFileRelative(_buildDir, "  ", _projectFile));
      fw.write(")");
    }
    else fw.write("\n;; no build directory");
    
     // write the working directory
    if (_workDir.getPath() != "") {
      fw.write("\n(work-dir");
      fw.write("\n" + encodeFileRelative(_workDir, "  ", _projectFile));
      fw.write(")");
    }
    else fw.write("\n;; no working directory");
    
    // write the main class
    if (_mainClass != null) {
      fw.write("\n;; rooted at the (parent of the) project file");
      fw.write("\n(main-class");
      fw.write("\n" + encodeFileRelative(_mainClass, "  ", _projectFile));
      fw.write(")");
    }
    else fw.write("\n;; no main class");
    
//    // write the create jar file
//    if (_createJarFile != null) {
//      fw.write("\n(create-jar-file");
//      fw.write("\n" + encodeFile(_createJarFile, "  ", true));
//      fw.write(")");
//    }
//    else fw.write("\n;; no create jar file");
//    
//    // write the create jar flags
//    if (_createJarFlags != 0) {
//      fw.write("\n(create-jar-flags " + _createJarFlags + ")");
//    }
//    else fw.write("\n;; no create jar flags");

    // write breakpoints
    if (!_breakpoints.isEmpty()) {
      fw.write("\n(breakpoints");
      for(DebugBreakpointData bp: _breakpoints) { fw.write("\n" + encodeBreakpointRelative(bp, "  ")); }
      fw.write(")"); // close the breakpoints expression
    }
    else fw.write("\n;; no breakpoints");

    // write watches
    if (!_watches.isEmpty()) {
      fw.write("\n(watches");
      for(DebugWatchData w: _watches) { fw.write("\n" + encodeWatch(w, "  ")); }
      fw.write(")"); // close the watches expression
    }
    else fw.write("\n;; no watches");

    // write bookmarks
    if (!_bookmarks.isEmpty()) {
      fw.write("\n(bookmarks");
      for(DocumentRegion bm: _bookmarks) { fw.write("\n" + encodeBookmarkRelative(bm, "  ")); }
      fw.write(")"); // close the bookmarks expression
    }
    else fw.write("\n;; no bookmarks");

    fw.close();
  }
  

  /* Private Methods */
  
  /** @param getter The getter that can get all the info needed to make the document file
   *  @return the document that contains the information retrieved from the getter
   */
  private DocFile docFileFromGetter(DocumentInfoGetter g) throws IOException {    
      return new DocFile(g.getFile().getCanonicalPath(), g.getSelection(), g.getScroll(), g.isActive(), g.getPackage());
  }
  
  
  /** This encodes a normal file relative to File base.  None of the special tags are added.
   *  @param f the file to encode
   *  @param prefix the indent level to place the s-expression at
   *  @param relative whether this file should be made relative to the project path
   *  @return the s-expression syntax to describe the given file.
   */
  private String encodeFileRelative(File f, String prefix, File base) throws IOException {
    String path = FileOps.makeRelativeTo(f, base).getPath();
    path = replace(path, File.separator, "/");
    return prefix + "(file (name " + convertToLiteral(path) + "))";
  }

  /** This encodes a normal file relative to _projectRoot.  None of the special tags are added. */
  private String encodeFileRelative(File f, String prefix) throws IOException { 
    return encodeFileRelative(f, prefix, _projectRoot); 
  }
    
  /** This encodes a normal file with its canonical path.  None of the special tags are added.
   *  @param f the file to encode
   *  @param prefix the indent level to place the s-expression at
   *  @return the s-expression syntax to describe the given file.
   */
  private String encodeFileAbsolute(File f, String prefix) throws IOException {
    String path = f.getCanonicalPath();
    path = replace(path,File.separator, "/");
    return prefix + "(file (name " + convertToLiteral(path) + "))";
  }
  
  /** This encodes a docfile, adding all the special tags that store document-specific information.
   *  @param df the doc file to encode
   *  @param prefix the indent level to place the s-expression at
   *  @param relative whether this file should be made relative to _projectRoot
   *  @return the s-expression syntax to describe the given docfile.
   */
  private String encodeDocFile(DocFile df, String prefix, boolean relative) throws IOException {
    String ret = "";
    String path;
    if (relative) path = FileOps.makeRelativeTo(df, _projectRoot).getPath();
    else path = IOUtil.attemptCanonicalFile(df).getPath();

    path = replace(path, File.separator, "/");
    ret += prefix + "(file (name " + convertToLiteral(path) + ")";
    
    Pair<Integer,Integer> p1 = df.getSelection();
    Pair<Integer,Integer> p2 = df.getScroll();
    //boolean active = false; //df.isActive();
    long modDate = df.lastModified();
    // Add prefix to the next line if any tags exist
    if (p1 != null || p2 != null /*|| active */)  ret += "\n" + prefix + "      ";

    // The next three tags go on the same line (if they exist)
    if (p1 != null) ret += "(select " + p1.first() + " " + p1.second() + ")";

    if (p2 != null) ret += "(scroll " + p2.first() + " " + p2.second() + ")";

    if (modDate > 0) {
      String s = MOD_DATE_FORMAT.format(new Date(modDate));
      ret += "(mod-date " + convertToLiteral(s) + ")";
    }
    
    //if (active) ret += "(active)"; //Active document is first on list
    
    // the next tag goes on the next line if at all
    String pack = df.getPackage();
    if (pack != null) {
      ret += "\n" + prefix + "      "; // add prefix
      ret += "(package " + convertToLiteral(pack) + ")";
    }
    
    ret += ")"; // close the file expression
    
    return ret;
  }
  /** Encodes a doc file relative to _projectRoot.
   *  @param df the DocFile to encode
   *  @param prefix the indent level
   */
  private String encodeDocFileRelative(DocFile df, String prefix) throws IOException {
    return encodeDocFile(df, prefix, true);
  }
  private String encodeDocFileAbsolute(DocFile df, String prefix) throws IOException {
    return encodeDocFile(df, prefix, false);
  }
  
  /** This encodes a breakpoint relative to _projectRoot.
   *  @param bp the breakpoint to encode
   *  @param prefix the indent level to place the s-expression at
   *  @return the s-expression syntax to describe the given breakpoint.
   */
  private String encodeBreakpointRelative(DebugBreakpointData bp, String prefix) throws IOException {
    String ret = "";
    String path = FileOps.makeRelativeTo(bp.getFile(), _projectRoot).getPath();
    
    path = replace(path,File.separator,"/");
    ret += prefix + "(breakpoint (name " + convertToLiteral(path) + ")";
    
    int offset = bp.getOffset();
    int lineNumber = bp.getLineNumber();
    ret += "\n" + prefix + "      ";
    ret += "(offset " + offset + ")";
    ret += "(line " + lineNumber + ")";
    if (bp.isEnabled()) ret += "(enabled)";
    ret += ")"; // close the breakpoint expression
    
    return ret;
  }
 
  /** This encodes a watch.
   *  @param w the watch to encode
   *  @param prefix the indent level to place the s-expression at
   *  @return the s-expression syntax to describe the given watch.
   */
  private String encodeWatch(DebugWatchData w, String prefix) throws IOException {
    String ret = "";

    ret += prefix + "(watch " + convertToLiteral(w.getName()) + ")";
    
    return ret;
  }

  /** This encodes a bookmark relative to _projectRoot.
   *  @param bm the bookmark to encode
   *  @param prefix the indent level to place the s-expression at
   *  @return the s-expression syntax to describe the given breakpoint.
   */
  private String encodeBookmarkRelative(DocumentRegion bp, String prefix) throws IOException {
    String ret = "";
    String path = FileOps.makeRelativeTo(bp.getDocument().getFile(), _projectRoot).getPath();
    
    path = replace(path,File.separator,"/");
    ret += prefix + "(bookmark (name " + convertToLiteral(path) + ")";
    
    int startOffset = bp.getStartOffset();
    int endOffset = bp.getEndOffset();
    ret += "\n" + prefix + "      ";
    ret += "(start " + startOffset + ")";
    ret += "(end " + endOffset + ")";
    ret += ")"; // close the bookmarks expression
    
    return ret;
  }
}