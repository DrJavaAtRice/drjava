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

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.util.Pair;
import edu.rice.cs.util.sexp.*;

/** This parser uses the s-expression parser defined in the util pacakge.  The SExp tree given by the parser is 
 *  interpreted into a ProjectFileIR that is given to the user.  This class must also deal with different
 *  versions of the project file.
 * 
 *  <p> If at some point new information is to be stored in the project file, the following places in the code that need to
 *  changed: <menu> <li> If the new information pertains to a document, the DocFile class should be augmented to
 *  store the new info.  <li> The interface for the DocumentInfoGetter should be expanded to allow for the new
 *  data to be retrieved.  <li> Add a new clause to the else-if ladder in the FilePropertyVisitor.  <li> 
 *  Add the new information to the DocFile form the DocumentInfoGetter in the ProjectFileBuilder's 
 *  addSourceDocument method.</p>
 * 
 *  <p> If the change is at the top level, you must modify the evaluateExpression method in this parser and add the 
 *  corresponding methods to the ProjectFileIR, ProjectFileIRImpl, and ProjectFileBuilder</p>
 */
public class ProjectFileParser {
  /** Xingleton instance of ProjectFileParser */
  public static final ProjectFileParser ONLY = new ProjectFileParser();
  
  private ProjectFileParser() { }
  
  /* methods */
  
  /** @param projFile the file to parse
   *  @return the project file IR
   */
  public ProjectFileIR parse(File projFile) 
    throws IOException, FileNotFoundException, MalformedProjectFileException{
    List<SEList> forest = null;
    try { forest = SExpParser.parse(projFile); }
    catch(SExpParseException e) {
      throw new MalformedProjectFileException("Parse Error: " + e.getMessage());
    }
    
    ProjectFileIRImpl pfir = new ProjectFileIRImpl();
    
    try {
      for (SEList exp : forest) { evaluateExpression(exp, pfir, new FileListVisitor(projFile.getParent())); }
    }
    catch(PrivateProjectException e) { throw new MalformedProjectFileException("Parse Error: " + e.getMessage()); }
    
    return pfir;
  }
  
  /** Given a top-level s-expression, this method checks the name of the node and configures the given pfir 
   *  appropriately.  If the expression is empty, it is ignored.
   *  @param e the top-level s-expression to check
   *  @param pfir the ProjectFileIR to update
   */
  private void evaluateExpression(SEList e, ProjectFileIRImpl pfir, FileListVisitor flv) {
    if (e == Empty.ONLY) return;
    Cons exp = (Cons)e; // If it's not empty, it's a cons
      
    String name = exp.accept(NameVisitor.ONLY);
    if (name.compareToIgnoreCase("source") == 0) {
      List<DocFile> fList = exp.getRest().accept(flv);
      pfir.setSourceFiles(fList);
    }
    else if (name.compareToIgnoreCase("auxiliary") == 0) {
      List<DocFile> fList = exp.getRest().accept(flv);
      pfir.setAuxiliaryFiles(fList);
    }
    else if (name.compareToIgnoreCase("collapsed") == 0) {
      List<String> sList = exp.getRest().accept(PathListVisitor.ONLY);
      pfir.setCollapsedPaths(sList);
    }
    else if (name.compareToIgnoreCase("build-dir") == 0) {
      List<DocFile> fList = exp.getRest().accept(flv);
      if (fList.size() > 1) throw new PrivateProjectException("Cannot have multiple build directories");
      else if (fList.size() == 0) pfir.setBuildDirectory(null);
      else pfir.setBuildDirectory(fList.get(0));
    }
    else if (name.compareToIgnoreCase("work-dir") == 0) {
      List<DocFile> fList = exp.getRest().accept(flv);
      if (fList.size() > 1) throw new PrivateProjectException("Cannot have multiple working directories");
      else if (fList.size() == 0) pfir.setWorkingDirectory(null);
      else pfir.setWorkingDirectory(fList.get(0));
    }
    else if (name.compareToIgnoreCase("classpaths") == 0) {
      List<DocFile> fList = exp.getRest().accept(flv);
      pfir.setClassPaths(fList);
    }
    else if (name.compareToIgnoreCase("main-class") == 0) {
      List<DocFile> fList = exp.getRest().accept(flv);
      if (fList.size() > 1) {
        throw new PrivateProjectException("Cannot have multiple main classes");
      }
      else if (fList.size() == 0) {
        pfir.setMainClass(null);
      }
      else {
        pfir.setMainClass(fList.get(0));
      }
    }
    else if (name.compareToIgnoreCase("proj-root") == 0) {
      List<DocFile> fList = exp.getRest().accept(flv);
      if (fList.size() > 1) {
        throw new PrivateProjectException("Cannot have multiple project roots");
      }
      else if (fList.size() == 0) {
        pfir.setProjectRoot(null);
      }
      else {
        pfir.setProjectRoot(fList.get(0));
      }
    }
  } 
  
  /** Parses out the labeled node (a non-empty list) into a DocFile. The node must have the "file" label on it.
   *  @param s the non-empty list expression
   *  @return the DocFile described by this s-expression
   */
  DocFile parseFile(SExp s, String parentDir) {
    String name = s.accept(NameVisitor.ONLY);
    if (name.compareToIgnoreCase("file") != 0)
      throw new PrivateProjectException("Expected a file tag, found: " + name);
    if (! (s instanceof Cons))
      throw new PrivateProjectException("Expected a labeled node, found a label: " + name);
    SEList c = ((Cons)s).getRest(); // get parameter list
    
    FilePropertyVisitor v = new FilePropertyVisitor(parentDir);
    return c.accept(v);
  }
  
  private String parseFileName(SExp s) {
    if (s instanceof Cons) {
      SEList l = ((Cons)s).getRest();
      if (l == Empty.ONLY)
        throw new PrivateProjectException("expected filename, but nothing found");
      else {
        String name = l.accept(NameVisitor.ONLY);
        name = edu.rice.cs.util.StringOps.replace(name,"\\","/");
        return name;
      }
    }
    else throw new PrivateProjectException("expected name tag, found string");
  }
  
  private Pair<Integer,Integer> parseIntPair(SExp s) {
    int row;
    int col;
    
    /* we're getting in a "(select # #)" */
    if (!(s instanceof Cons)) {
      throw new PrivateProjectException("expected name tag, found string");
    }
    
    // get rid of "select"
    final List<Integer> intList = new ArrayList<Integer>();
    SEList l = ((Cons)s).getRest();
    List<Integer> li = l.accept(new SExpVisitor<List<Integer>>() {
      public List<Integer> forEmpty(Empty e) { return intList; }
  
      public List<Integer> forCons(Cons c) {
        c.getFirst().accept(this);
        return c.getRest().accept(this);
      }
  
      public List<Integer> forBoolAtom(BoolAtom b) {
        throw new PrivateProjectException("unexpected boolean found, int expected");
      }
      
      public List<Integer> forNumberAtom(NumberAtom n) {
        intList.add(new Integer(n.intValue()));
        return intList;
      }
      
      public List<Integer> forTextAtom(TextAtom t) {
        throw new PrivateProjectException("unexpected string found where number expected: " + t.getText());
      }
      
    });
    
    if (li.size() == 2) return new Pair<Integer,Integer>(li.get(0), li.get(1));
    else throw new PrivateProjectException("expected a list of 2 ints for select, found list of size " + li.size());
  }

    /** Takes input of form "(str str)" and returns the second string. */
    private String parseStringNode(SExp n) {
      if (n instanceof Cons) {
        return ((Cons)n).getRest().accept(NameVisitor.ONLY);
      }
      else throw new PrivateProjectException("List expected, but found text instead");  
    }
  
  /* nested/inner classes */
  
  
  /** Parses out a list of file nodes. */
  private static class FileListVisitor implements SEListVisitor<List<DocFile>> {
    String _parentDir;
    public FileListVisitor(String parent) { _parentDir = parent; }
    public List<DocFile> forEmpty(Empty e) { return new ArrayList<DocFile>(); }
    public List<DocFile> forCons(Cons c) {
      List<DocFile> list = c.getRest().accept(this);
      DocFile tmp = ProjectFileParser.ONLY.parseFile(c.getFirst(), _parentDir);
      list.add(0,tmp); // add to the end
      return list;
    }
  };
  
  /** Traverses the list of expressions found after "file" tag and returns the DocFile described by those properties. */
  private static class FilePropertyVisitor implements SEListVisitor<DocFile> {
    private String fname = "";
    private Pair<Integer,Integer> select = new Pair<Integer,Integer>(new Integer(0),new Integer(0));
    private Pair<Integer,Integer> scroll = new Pair<Integer,Integer>(new Integer(0),new Integer(0));
    private boolean active = false;
    private String pack = "";
    private Date modDate = null;
    
    private String _parentDir;
    public FilePropertyVisitor(String parentDir) { _parentDir = parentDir; }
    
    public DocFile forCons(Cons c) {
      String name = c.getFirst().accept(NameVisitor.ONLY); 
      if (name.compareToIgnoreCase("name") == 0) { fname = ProjectFileParser.ONLY.parseFileName(c.getFirst()); }
      else if (name.compareToIgnoreCase("select") == 0) { select = ProjectFileParser.ONLY.parseIntPair(c.getFirst()); }
      else if (name.compareToIgnoreCase("scroll") == 0) { scroll = ProjectFileParser.ONLY.parseIntPair(c.getFirst()); }
      else if (name.compareToIgnoreCase("active") == 0) { active = true; }
      else if (name.compareToIgnoreCase("package") == 0) { 
        pack = ProjectFileParser.ONLY.parseStringNode(c.getFirst()); 
      }
      else if (name.compareToIgnoreCase("mod-date") == 0) {
        String tmp = ProjectFileParser.ONLY.parseStringNode(c.getFirst());
        try { modDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(tmp); }
        catch (java.text.ParseException e) { throw new PrivateProjectException("Bad mod-date: " + e.getMessage()); }
      }
        
      return c.getRest().accept(this);
    }
    
    public DocFile forEmpty(Empty c) {
      if (_parentDir == null || new File(fname).isAbsolute()) {
        return new DocFile(fname, select, scroll, active, pack);
      }
      else {
        DocFile f = new DocFile(_parentDir, fname, select, scroll, active, pack);
        if (modDate != null) f.setSavedModDate(modDate.getTime());
        return f;
      }
    }
  }
  
  /** Parses out a list of path nodes into a list of Strings. */
  private static class PathListVisitor implements SEListVisitor<List<String>> {
    public static final PathListVisitor ONLY = new PathListVisitor();
    private PathListVisitor() { }
    
    public List<String> forEmpty(Empty e) { return new ArrayList<String>(); }
    public List<String> forCons(Cons c) {
      List<String> list = c.getRest().accept(this);
      SExp first = c.getFirst();
      String name = first.accept(NameVisitor.ONLY); 
      if (name.compareToIgnoreCase("path") == 0) {
        String tmp = ProjectFileParser.ONLY.parseStringNode(c.getFirst());
        list.add(0,tmp); // add to the end
      }
      return list;
    }
  };
  
  /** Retrieves the name of a node.  The node should either be a list with its first element being a text atom, 
   *  or a text atom itself.
   */
  private static class NameVisitor implements SExpVisitor<String> {
    public static final NameVisitor ONLY = new NameVisitor();
    private NameVisitor() { }
    
    public String forEmpty(Empty e) {
      throw new PrivateProjectException("Found an empty node, expected a labeled node");
    }
    public String forCons(Cons c) {
      return c.getFirst().accept(this);
    }
    public String forBoolAtom(BoolAtom b) {
      throw new PrivateProjectException("Found a boolean, expected a label");
    }
    public String forNumberAtom(NumberAtom n) {
      throw new PrivateProjectException("Found a number, expected a label");
    }
    public String forTextAtom(TextAtom t) {
      return t.getText();
    }
  };
  
  
  /** Concrete implementation of the ProjectFileIR which is the interface through which DrJava
   *  access info stored in a project file
   */
  public static class ProjectFileIRImpl implements ProjectFileIR {
    List<DocFile> _src;
    List<DocFile> _aux;
    List<String> _collapsed;
    File _buildDir;
    File _workDir;
    List<? extends File> _classPaths;
    File _mainClass;
    File _projRoot;
    
    /** Starts the project file IR off with all its default values. */
    public ProjectFileIRImpl() {
      _src = new ArrayList<DocFile>();
      _aux = new ArrayList<DocFile>();
      _collapsed = new ArrayList<String>();
      _classPaths = new ArrayList<DocFile>();
      _buildDir = null;
      _workDir = null;
      _mainClass = null;
      _projRoot = null;
    }
    
    /** @return an array full of all the source files in this project file. */
    public DocFile[] getSourceFiles() { return _src.toArray(new DocFile[0]); }
    
    /** @return an array full of all the resource files in this project file. */
    public DocFile[] getAuxiliaryFiles() { return _aux.toArray(new DocFile[0]); }
    
    /** @return an array full of all the resource files in this project file. */
    public String[] getCollapsedPaths() { return _collapsed.toArray(new String[0]); }
    
    /** @return an array full of all the classpath path elements in the classpath for this project file. */
    public File[] getClassPaths() { return _classPaths.toArray(new File[0]); }

    /** @return the build directory stored in this project file. */
    public File getBuildDirectory() { return _buildDir; }
    
    /** @return the working directory stored in this project file . */
    public File getWorkingDirectory() { return _workDir; }
    
    /** @return the file of the class whose main method should be run when running the project in DrJava. */
    public File getMainClass() { return _mainClass; }
    
    /** @return the file pointing to the directory that all source files are relative to. */
    public File getProjectRoot() { return _projRoot; }
    
    /* Package Protected Setter Methods */
    
    void setSourceFiles(List<DocFile> src) { _src = src; }
    void setAuxiliaryFiles(List<DocFile> aux) { _aux = aux; }
    void setCollapsedPaths(List<String> path) { _collapsed = path; }
    void setClassPaths(List<DocFile> cp) { _classPaths = cp; }
    void setBuildDirectory(File dir) { _buildDir = dir; }
    void setWorkingDirectory(File dir) { _workDir = dir; }
    void setMainClass(File main) { _mainClass = main; }
    void setProjectRoot(File root) { _projRoot = root; }
  } // end ProjectFileIRImpl class

  
  private static class PrivateProjectException extends RuntimeException{
    public PrivateProjectException(String message) {
      super(message);
    }
  }
}
