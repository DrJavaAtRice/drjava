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

import junit.framework.TestCase;
import java.io.*;

import edu.rice.cs.util.sexp.*;
import edu.rice.cs.util.*;

/**
 * A JUnit test case class.
 * Every method starting with the word "test" will be called when running
 * the test with JUnit.
 */
public class ProjectTest extends TestCase {
  
  private String absp = ""; // absolute path name
  
  public void setUp() {
    if (File.separatorChar == '/') { 
      absp = "/home/javaplt/";
    }
    else {
      absp = "c:/tmp/";
    }
  }
  /**
   * Creates a temporary file and writes the given string to that file
   * @param fname the name of the file to create
   * @param text the text to write to the file
   * @return the File that was created
   */
  private File _fillTempFile(String fname, String text) {
    File f = null;
    try {
      f = File.createTempFile(fname, null);
      FileWriter fw = new FileWriter(f);
      fw.write(text, 0, text.length());
      fw.close();
    }
    catch (IOException e) {
      throw new RuntimeException("IOException thrown while writing to temp file");
    }
    return f;
  }
  
  /**
   * Test to make sure all elements of the project are read correctly into the IR
   */
  public void testParseProject() throws IOException, MalformedProjectFileException {
    String proj1 = 
      ";; DrJava project file.  Written with build: 20040623-1933\n" +
      "(source ;; comment\n" +
      "   (file (name \"sexp/Atom.java\")(select 32 32))\n" +
      "   (file (name \"sexp/BoolAtom.java\")(select 0 0))\n" +
      "   (file (name \"sexp/Cons.java\")(select 0 0))\n" +
      "   (file (name \"sexp/Empty.java\")(select 24 28)(active))\n" +
      "   (file (name \"sexp/Lexer.java\")(select 0 0))\n" +
      "   (file (name \"sexp/NumberAtom.java\")(select 12 12))\n" +
      "   (file (name \"sexp/SEList.java\")(select 0 0)))\n" +
      "(auxiliary ;; absolute file names\n" +
      "   (file (name \""+absp+"junk/sexp/Tokens.java\")(select 32 32))\n" +
      "   (file (name \""+absp+"java/Linux-i686/jdk1.5.0/src/javax/swing/JScrollPane.java\")(select 9086 8516)))\n" +
      "(collapsed ;; relative paths\n" +
      "   (file (name \"sexp\"))\n" +
      "   (file (name \"[External]\")))\n" +
      "(build-dir ;; absolute path\n" +
      "   (file (name \""+absp+"drjava/built\")))\n" +
      "(classpaths\n" + 
      "   (file (name \""+absp+"drjava/src/edu/rice/cs/lib\")))\n" + 
      "(main-class\n" +
      "   (file (name \"sexp/SEList.java\")))";
    
    File f = _fillTempFile("test1.pjt", proj1);
    ProjectFileIR pfir = ProjectFileParser.ONLY.parse(f);
    assertEquals("number of source files", 7, pfir.getSourceFiles().length);
    assertEquals("number of aux files", 2, pfir.getAuxiliaryFiles().length);
    assertEquals("number of collapsed", 2, pfir.getCollapsedPaths().length);
    assertEquals("number of classpaths", 1, pfir.getClasspaths().length);
    
    assertEquals("first source filename", "sexp/Atom.java", pfir.getSourceFiles()[0].getPath());
    assertEquals("last source filename", "sexp/SEList.java", pfir.getSourceFiles()[6].getPath());
    assertEquals("first aux filename", ""+absp+"junk/sexp/Tokens.java", pfir.getAuxiliaryFiles()[0].getPath());
    assertEquals("last collapsed path", "[External]", pfir.getCollapsedPaths()[1].getPath());
    assertEquals("build-dir name", ""+absp+"drjava/built", pfir.getBuildDirectory().getPath());
    assertEquals("classpath name", ""+absp+"drjava/src/edu/rice/cs/lib", pfir.getClasspaths()[0].getPath());
    assertEquals("main-class name", "sexp/SEList.java", pfir.getMainClass().getPath());
    
  }
  
  public void testParseFile() throws SExpParseException {
    SEList c = SExpParser.parse("(file (name \"file-name\") (select 1 2))").get(0);
    DocFile df = ProjectFileParser.ONLY.parseFile(c);
    Pair<Integer,Integer> p = df.getSelection();
    assertEquals("First int should be a 1", 1, p.getFirst());
    assertEquals("Second int should be a 2", 2, p.getSecond());
    assertEquals("Name should have been file-name", "file-name", df.getPath());
  }
  
  public void testWriteFile() throws IOException, MalformedProjectFileException {
    File pf = _fillTempFile("test2.pjt","");
    ProjectFileBuilder fb = new ProjectFileBuilder(pf);
    String sr =pf.getCanonicalFile().getParent();
    
    fb.addSourceFile(makeGetter(0,0,0,0,  "dir1/testfile1.java","dir1",false,false,pf));
    fb.addSourceFile(makeGetter(1,1,0,0,  "dir1/testfile2.java","dir1",false,false,pf));
    fb.addSourceFile(makeGetter(20,22,0,0,"dir2/testfile3.java","dir2",false,false,pf));
    fb.addSourceFile(makeGetter(1,1,0,0,  "dir2/testfile4.java","dir2",true, false,pf));
    fb.addSourceFile(makeGetter(0,0,0,0,  "dir3/testfile5.java","dir3",false,false,pf));
    fb.addAuxiliaryFile(makeGetter(1,1,0,0,  ""+absp+"test/testfile6.java","/home/javaplt",false, false,null));
    fb.addAuxiliaryFile(makeGetter(1,1,0,0,  ""+absp+"test/testfile7.java","/home/javaplt",false, false,null));
    fb.addCollapsedPath(new File(pf.getParentFile(), "dir1"));
    fb.addClasspathFile(new File(""+absp+"drjava/lib"));
    fb.setBuildDirectory(new File(""+absp+"drjava/built"));
    fb.setMainClass(new File(pf.getParentFile(), "dir1/testfile1.java"));
    
    String expected = "";
    String received = "";
    fb.write();
    
    FileReader fr = new FileReader(pf);
    int c = fr.read();
    while (c >= 0) {
      received += (char)c;
      c = fr.read();
    }
//    assertEquals("Make relative", "dir1/test.java", 
//                 fb.makeRelative(new File(pf.getParentFile(),"dir1/test.java")));
//    assertEquals("The file written by the builder", expected, received);
    
    // parse in the file that was just written.
    ProjectFileIR pfir = null;
    try {
       pfir = ProjectFileParser.ONLY.parse(pf);
    }
    catch(MalformedProjectFileException e) {
      throw new MalformedProjectFileException(e.getMessage() + ", file: " + pf);
    }
    assertEquals("number of source files", 5, pfir.getSourceFiles().length);
    assertEquals("number of aux files", 2, pfir.getAuxiliaryFiles().length);
    assertEquals("number of collapsed", 1, pfir.getCollapsedPaths().length);
    assertEquals("number of classpaths", 1, pfir.getClasspaths().length);
    
    assertEquals("first source filename", "dir1/testfile1.java", pfir.getSourceFiles()[0].getPath());
    assertEquals("last source filename", "dir3/testfile5.java", pfir.getSourceFiles()[4].getPath());
    assertEquals("first aux filename", ""+absp+"test/testfile6.java", pfir.getAuxiliaryFiles()[0].getPath());
    assertEquals("last collapsed path", "dir1", pfir.getCollapsedPaths()[0].getPath());
    assertEquals("build-dir name", ""+absp+"drjava/built", pfir.getBuildDirectory().getPath());
    assertEquals("classpath name", ""+absp+"drjava/lib", pfir.getClasspaths()[0].getPath());
    assertEquals("main-class name", "dir1/testfile1.java", pfir.getMainClass().getPath());
    pf.delete();
  }
  
  private DocumentInfoGetter makeGetter(final int sel1, final int sel2, final int scrollv, 
                                        final int scrollh, final String fname, final String pack, 
                                        final boolean active, final boolean untitled, final File pf) {
    return new DocumentInfoGetter() {
      public Pair<Integer,Integer> getSelection() { return new Pair<Integer,Integer>(sel1,sel2); }
      public Pair<Integer,Integer> getScroll() { return new Pair<Integer,Integer>(scrollv,scrollh); }
      public File getFile(){ 
        if (pf == null)
          return new File(fname);
        else
          return new File(pf.getParentFile(),fname); 
      }
      public String getPackage(){ return pack; }
      public boolean isActive() { return active; }
      public boolean isUntitled() { return untitled; }
    };
    
  }
}
