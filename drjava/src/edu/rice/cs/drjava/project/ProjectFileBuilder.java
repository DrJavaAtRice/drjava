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

import java.util.List;
import java.util.Vector;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.*;

import edu.rice.cs.drjava.Version;
import edu.rice.cs.util.Pair;
import edu.rice.cs.util.UnexpectedException;

/**
 * The project file builder is responsible for
 * encoding the project file based on the information
 * given to it through the public interface offered.
 */
public class ProjectFileBuilder {
  
  /////////////// Private fields //////////////
  
  private List<DocFile> _sourceFiles = new Vector<DocFile>();
  
  private List<DocFile> _auxFiles = new Vector<DocFile>();
  
  private List<String> _collapsedPaths = new Vector<String>();
  
  private File _buildDir = null;
  
  private List<File> _classpathFiles = new Vector<File>();
  
  private File _mainClass = null;
 
  private File _projectFile;
  
  ////////////// Constructors /////////////////
  
  public ProjectFileBuilder(String filename) {
    _projectFile = new File(filename);
  }
  public ProjectFileBuilder(File f) {
    _projectFile = f;
  }
  
  ////////////// Public methods ///////////////
  
  public void addSourceFile(DocumentInfoGetter getter) {
    if (!getter.isUntitled()) {
      try {
        _sourceFiles.add(docFileFromGetter(getter));
      }
      catch(IOException e) {
        throw new UnexpectedException(e);
      }
    }
  }
  public void addAuxiliaryFile(DocumentInfoGetter getter) {
    if (!getter.isUntitled()) {
      try {
        _auxFiles.add(docFileFromGetter(getter));
      }
      catch(IOException e) {
        throw new UnexpectedException(e);
      }
    }
  }
  public void addClasspathFile(File cp) {
    if (cp != null) {
      _classpathFiles.add(cp);
    }
  }
  public void addCollapsedPath(String cp) {
    if (cp != null) {
      _collapsedPaths.add(cp);
    }
  }
  public void setBuildDirectory(File dir) {
    _buildDir = dir;
  }
  public void setMainClass(File main) {
    _mainClass = main;
  }
  
  /**
   * This method writes what information has been passed
   * to this builder so far to disk in s-expression format
   */
  public void write() throws IOException {
    FileWriter fw = new FileWriter(_projectFile);
    
    // write opening comment line
    fw.write(";; DrJava project file, written by build " + Version.getBuildTimeString());
    
    // write source files
    if (!_sourceFiles.isEmpty()) {
      fw.write("\n(source");
      for(DocFile df: _sourceFiles) {
        fw.write("\n" + encodeDocFile(df, "  "));
      }
      fw.write(")"); // close the source expression
    }
    else {
      fw.write("\n;; no source files");
    }
    
    // write aux files
    if (!_auxFiles.isEmpty()) {
      fw.write("\n(auxiliary");
      for(DocFile df: _auxFiles) {
        fw.write("\n" + encodeDocFile(df, "  ", false));
      }
      fw.write(")"); // close the auxiliary expression
    }
    else {
      fw.write("\n;; no aux files");
    }
    
    // write collapsed paths
    if (!_collapsedPaths.isEmpty()){
      fw.write("\n(collapsed");
      for(String s: _collapsedPaths) {
        fw.write("\n  (path " + convertToLiteral(s) + ")");
      }
      fw.write(")"); // close the collapsed expression
    }
    else {
      fw.write("\n;; no collapsed branches");
    }
    
    // write classpaths
    if (!_classpathFiles.isEmpty()) {
      fw.write("\n(classpaths");
      for(File f: _classpathFiles) {
        fw.write("\n" + encodeFile(f, "  "));
      }
      fw.write(")"); // close the classpaths expression
    }
    else {
      fw.write("\n;; no classpaths files");
    }
    
    // write the build directory
    if (_buildDir != null) {
      fw.write("\n(build-dir");
      fw.write("\n" + encodeFile(_buildDir, "  "));
      fw.write(")");
    }
    else {
      fw.write("\n;; no build directory");
    }
    
    // write the main class
    if (_mainClass != null) {
      fw.write("\n(main-class");
      fw.write("\n" + encodeFile(_mainClass, "  "));
      fw.write(")");
    }
    else {
      fw.write("\n;; no main class");
    }
    
    fw.close();
  }
  
  ////////////// Private Methods //////////////
  
  /**
   * @param getter The getter that can get all the info needed to
   * make the document file
   * @return the document that contains the information retrieved
   * from the getter
   */
  private DocFile docFileFromGetter(DocumentInfoGetter getter) throws IOException {    
      return new DocFile(getter.getFile().getCanonicalPath(),
                         getter.getSelection(),
                         getter.getScroll(),
                         getter.isActive(),
                         getter.getPackage());
  }
  
  /**
   * This encodes a normal file.  none of the special tags are added
   * @param f the file to encode
   * @param prefix the indent level to place the s-expression at
   * @param relative whether this file should be made relative to the project path
   * @return the s-expression syntax to describe the given file.
   */
  private String encodeFile(File f, String prefix, boolean relative) throws IOException {
    String path;
    if (relative) {
      path = makeRelative(f);
    }
    else {
      path = f.getCanonicalPath();
    }
    return prefix + "(file (name " + convertToLiteral(path) + "))";
  }
  /**
   * encodes a file.  The path defaults to relative.
   * @param f the file to encode
   * @param prefix the indent level
   */
  private String encodeFile(File f, String prefix) throws IOException {
    return encodeFile(f, prefix, true);
  }
  
  /**
   * This encodes a docfile, adding all the special tags that store
   * document-specific information.
   * @param df the doc file to encode
   * @param prefix the indent level to place the s-expression at
   * @param relative whether this file should be made relative to the project path
   * @param hasDate whether to include the modification date
   * @return the s-expression syntax to describe the given docfile.
   */
  private String encodeDocFile(DocFile df, String prefix, boolean relative, boolean hasDate) throws IOException {
    String ret = "";
    String path;
    if (relative) {
      path = makeRelative(df);
    }
    else {
      path = df.getCanonicalPath();
    }
    ret += prefix + "(file (name " + convertToLiteral(path) + ")";
    
    Pair<Integer,Integer> p1 = df.getSelection();
    Pair<Integer,Integer> p2 = df.getScroll();
    boolean active = df.isActive();
    long modDate = df.lastModified();
    // Add prefix to the next line if any tags exist
    if (p1 != null || p2 != null || active) {
      ret += "\n" + prefix + "      ";
    }
    // The next three tags go on the same line (if they exist)
    if (p1 != null) {
      ret += "(select " + p1.getFirst() + " " + p1.getSecond() + ")";
    }
    if (p2 != null) {
      ret += "(scroll " + p2.getFirst() + " " + p2.getSecond() + ")";
    }
    if (hasDate && modDate > 0) {
      String s = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date(modDate));
      ret += "(mod-date " + convertToLiteral(s) + ")";
    }
    if (active) {
      ret += "(active)";
    }
    // the next tag goes on the next line if at all
    String pack = df.getPackage();
    if (pack != null) {
      ret += "\n" + prefix + "      "; // add prefix
      ret += "(package " + convertToLiteral(pack) + ")";
    }
    
    ret += ")"; // close the file expression
    
    return ret;
  }
  /**
   * encodes a doc file.  The path defaults to relative.
   * @param df the DocFile to encode
   * @param prefix the indent level
   */
  private String encodeDocFile(DocFile df, String prefix) throws IOException {
    return encodeDocFile(df, prefix, true,true);
  }
  private String encodeDocFile(DocFile df, String prefix, boolean relative) throws IOException {
    return encodeDocFile(df,prefix,relative,true);
  }
  
  /**
   * @param the file whose path to make relative to the project path
   * @return the string name of the file's path relative to the project path
   */
  private String makeRelative(File f) throws IOException {
    String proj = _projectFile.getParentFile().getCanonicalPath() + File.separator;
    String path = f.getCanonicalPath();
    if (path.startsWith(proj)) {
      return path.substring(proj.length());
    }
    return path;
  }
  
  /**
   * Converts the given string to a valid Java string literal.
   * All back slashes, quotes, new-lines, and tabs are converted
   * to their escap character form, and the sourounding quotes 
   * are added.
   * @param s the normal string to turn into a string literal
   * @return the valid Java string literal
   */
  public static String convertToLiteral(String s) {
    String output = s;
    output = replaceAll(output, "\\", "\\\\"); // convert \ to \\
    output = replaceAll(output, "\"", "\\\""); // convert " to \"
    output = replaceAll(output, "\t", "\\t");  // convert [tab] to \t
    output = replaceAll(output, "\n", "\\n");  // convert [newline] to \n
    return "\"" + output + "\"";
  }
  
  /**
   * replaces all occurrences of the given a string with a new string.
   * This method was reproduced here to remove any dependencies on the
   * java v1.4 api.
   * @param str the string in which the replacements should occur
   * @param toReplace the substring to replace
   * @param replacement the substring to put in its place
   * @return the new changed string
   */
  private static String replaceAll(String str, String toReplace, String replacement) {
    String result = str;
    int i = result.indexOf(toReplace); 
    while (i >= 0) {
      result = result.substring(0,i) + replacement + result.substring(i+1);
      i = result.indexOf(toReplace, i + replacement.length());
    }
    return result;
  }
}