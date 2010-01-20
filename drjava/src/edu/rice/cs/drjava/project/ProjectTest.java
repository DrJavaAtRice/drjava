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

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.util.AbsRelFile;

import static edu.rice.cs.util.StringOps.convertToLiteral;

import edu.rice.cs.util.sexp.SEList;
import edu.rice.cs.util.sexp.SExpParseException;
import edu.rice.cs.util.sexp.SExpParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/** Test class for project files */
public class ProjectTest extends DrJavaTestCase {
  
  File base;
  File parent;
  File buildDir;
  File srcDir;

  private String absp; // absolute path
  public void setUp() throws Exception {
    super.setUp();
    try { 
      base = new File(System.getProperty("java.io.tmpdir")).getCanonicalFile();
      parent = IOUtil.createAndMarkTempDirectory("proj", "", base);
      buildDir = new File(parent, "built");
      buildDir.mkdir();  // create the specified directory
      srcDir = new File(parent, "src");
      srcDir.mkdir(); // create the specified directory
      absp = parent.getCanonicalPath() + File.separator; 
    }
    catch(IOException e) { fail("could not initialize temp path string"); }
  }

  public void tearDown() throws Exception {
    IOUtil.deleteOnExitRecursively(parent);
    super.tearDown();
  }
  
  /** Test to make sure all elements of the project are read correctly into the IR */
  public void testLegacyParseProject() throws IOException, MalformedProjectFileException, java.text.ParseException {
    String proj1 =
      ";; DrJava project file.  Written with build: 20040623-1933\n" +
      "(source ;; comment\n" +
      "   (file (name \"src/sexp/Atom.java\")(select 32 32)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"src/sexp/BoolAtom.java\")(select 0 0)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"src/sexp/Cons.java\")(select 0 0)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"src/sexp/Empty.java\")(select 24 28)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"src/sexp/Lexer.java\")(select 0 0)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"src/sexp/NumberAtom.java\")(select 12 12)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"src/sexp/SEList.java\")(select 0 0)))\n" + // doesn't have mod date
      "(auxiliary ;; absolute file names\n" +
      "   (file (name " + convertToLiteral(new File(parent, "junk/sexp/Tokens.java").getCanonicalPath()) + 
         ")(select 32 32)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name " + convertToLiteral(new File(parent, "jdk1.5.0/JScrollPane.java").getCanonicalPath()) + 
         ")(select 9086 8516)(mod-date \"16-Jul-2004 03:45:23\")))\n" +
      "(collapsed ;; relative paths\n" +
      "   (path \"./[ Source Files ]/sexp/\")\n" +
      "   (path \"./[ External ]/\"))\n" +
      "(build-dir ;; absolute path\n" +
      "   (file (name " +  convertToLiteral(new File(parent,"built").getCanonicalPath()) + ")))\n" +
      "(work-dir ;; absolute path\n" +
      "   (file (name " +  convertToLiteral(new File(parent,"src").getCanonicalPath()) + ")))\n" +
      "(proj-root ;; absolute path\n" +
      "   (file (name " +  convertToLiteral(new File(parent,"src").getCanonicalPath()) + ")))\n" +
      "(classpaths\n" +
      "   (file (name " +  convertToLiteral(new File(parent,"src/edu/rice/cs/lib").getCanonicalPath()) + ")))\n" +
      "(main-class\n" +
      "   some.package.ClassName)";
    
    File f = new File(parent, "test1.pjt");

    IOUtil.writeStringToFile(f, proj1);
//    System.err.println("Project directory is " + parent);
//    System.err.println("Project file is " + f);
//    System.err.println("projFile exists? " + f.exists());
    ProjectFileIR pfir = ProjectFileParserFacade.ONLY.parse(f);
//    System.err.println("buildDir = " + pfir.getBuildDirectory().getCanonicalPath());
    assertEquals("number of source files", 7, pfir.getSourceFiles().length);
    assertEquals("number of aux files", 2, pfir.getAuxiliaryFiles().length);
    assertEquals("number of collapsed", 2, pfir.getCollapsedPaths().length);
    assertEquals("number of classpaths", 1, IterUtil.sizeOf(pfir.getClassPaths()));
    File base = f.getParentFile();
    assertEquals("first source filename", new File(base,"src/sexp/Atom.java").getPath(), pfir.getSourceFiles()[0].getPath());
    assertEquals("mod-date value", 
                 ProjectProfile.MOD_DATE_FORMAT.parse("16-Jul-2004 03:45:23").getTime(),
                 pfir.getSourceFiles()[0].getSavedModDate());
    assertEquals("last source filename", new File(base,"src/sexp/SEList.java").getPath(), 
                 pfir.getSourceFiles()[6].getPath());
    assertEquals("first aux filename", new File(base,"junk/sexp/Tokens.java").getPath(), 
                 pfir.getAuxiliaryFiles()[0].getCanonicalPath());
    assertEquals("last collapsed path", "./[ External ]/", pfir.getCollapsedPaths()[1]);
    assertEquals("build-dir name", new File(base, "built").getCanonicalPath(), 
                 pfir.getBuildDirectory().getCanonicalPath());
    assertEquals("work-dir name", new File(base, "src").getCanonicalPath(), 
                 pfir.getWorkingDirectory().getCanonicalPath());
    assertEquals("classpath name", new File(base, "src/edu/rice/cs/lib").getCanonicalPath(), 
                 IterUtil.first(pfir.getClassPaths()).getCanonicalPath());
    assertEquals("main-class name", "some.package.ClassName", 
                 pfir.getMainClass());
  }

   /** Test to make sure all elements of the project are read correctly into the IR */
  public void testParseProject() throws IOException, MalformedProjectFileException, java.text.ParseException {
    String proj2 =
      ";; DrJava project file.  Written with build: 2006??\n" +
      "(proj-root-and-base (file (name \"src\")))\n" +
      "(source-files ;; comment\n" +
      "   (file (name \"sexp/Atom.java\")(select 32 32)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"sexp/BoolAtom.java\")(select 0 0)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"sexp/Cons.java\")(select 0 0)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"sexp/Empty.java\")(select 24 28)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"sexp/Lexer.java\")(select 0 0)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"sexp/NumberAtom.java\")(select 12 12)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"sexp/SEList.java\")(select 0 0)))\n" + // doesn't have mod date
      "(auxiliary ;; absolute file names\n" +
      "   (file (name " + convertToLiteral(new File(parent,"junk/sexp/Tokens.java").getCanonicalPath()) +
          ")(select 32 32)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name " + convertToLiteral(new File(parent,"jdk1.5.0/JScrollPane.java").getCanonicalPath()) +
          ")(select 9086 8516)(mod-date \"16-Jul-2004 03:45:23\")))\n" +
      "(collapsed ;; relative paths\n" +
      "   (path \"./[ Source Files ]/sexp/\")\n" +
      "   (path \"./[ External ]/\"))\n" +
      "(build-dir ;; absolute path\n" +
      "   (file (name " +  convertToLiteral(new File(parent, "built").getCanonicalPath()) + ")))\n" +
      "(work-dir (file (name \"src\")))\n" +
      "(classpaths\n" +
      "   (file (name " +  convertToLiteral(new File(parent, "src/edu/rice/cs/lib").getCanonicalPath()) + ")))\n" +
      "(main-class\n" +
      "   some.ClassName)";
    
    File f = new File(parent, "test2.pjt");

    IOUtil.writeStringToFile(f, proj2);
//    System.err.println("Project directory is " + parent);
//    System.err.println("Project file is " + f);
//    System.err.println("projFile exists? " + f.exists());
    ProjectFileIR pfir = ProjectFileParserFacade.ONLY.parse(f);
//    System.err.println("buildDir = " + pfir.getBuildDirectory().getCanonicalPath());
    assertEquals("number of source files", 7, pfir.getSourceFiles().length);
    assertEquals("number of aux files", 2, pfir.getAuxiliaryFiles().length);
    assertEquals("number of collapsed", 2, pfir.getCollapsedPaths().length);
    assertEquals("number of classpaths", 1, IterUtil.sizeOf(pfir.getClassPaths()));
    File base = f.getParentFile();
    File root = new File(base, "src");
    assertEquals("proj-root-and-base", root.getPath(), pfir.getProjectRoot().getPath());
    assertEquals("first source filename", new File(base,"src/sexp/Atom.java").getPath(), pfir.getSourceFiles()[0].getPath());
    assertEquals("mod-date value", 
                 ProjectProfile.MOD_DATE_FORMAT.parse("16-Jul-2004 03:45:23").getTime(),
                 pfir.getSourceFiles()[0].getSavedModDate());
    assertEquals("last source filename", new File(root, "sexp/SEList.java").getPath(), 
                 pfir.getSourceFiles()[6].getPath());
    assertEquals("first aux filename", new File(base,"junk/sexp/Tokens.java").getPath(), 
                 pfir.getAuxiliaryFiles()[0].getCanonicalPath());
    assertEquals("last collapsed path", "./[ External ]/", pfir.getCollapsedPaths()[1]);
    assertEquals("build-dir name", new File(base, "built").getCanonicalPath(), 
                 pfir.getBuildDirectory().getCanonicalPath());
    assertEquals("work-dir name", new File(base, "src").getCanonicalPath(), 
                 pfir.getWorkingDirectory().getCanonicalPath());
    assertEquals("classpath name", new File(base, "src/edu/rice/cs/lib").getCanonicalPath(), 
                 IterUtil.first(pfir.getClassPaths()).getCanonicalPath());
    assertEquals("main-class name", "some.ClassName", 
                 pfir.getMainClass());
  }
  
  public void testParseFile() throws SExpParseException {
    SEList c = SExpParser.parse("(file (name \"file-name\") (select 1 2))").get(0);
    DocFile df = ProjectFileParser.ONLY.parseFile(c,null);
    Pair<Integer,Integer> p = df.getSelection();
    assertEquals("First int should be a 1", 1, (int)p.first()); //need cast to prevent ambiguity
    assertEquals("Second int should be a 2", 2, (int)p.second());//need cast to prevent ambiguity
    assertEquals("Name should have been file-name", "file-name", df.getPath());
  }

  public void testWriteFile() throws IOException, MalformedProjectFileException {
    File pf = new File(parent, "test3.pjt");
    IOUtil.writeStringToFile(pf, "");
    ProjectProfile fb = new ProjectProfile(pf);

    fb.addSourceFile(makeGetter(0, 0, 0, 0,  "dir1/testfile1.java", "dir1", false, false, pf));
    fb.addSourceFile(makeGetter(1, 1, 0, 0,  "dir1/testfile2.java", "dir1", false, false, pf));
    fb.addSourceFile(makeGetter(20, 22, 0, 0, "dir2/testfile3.java", "dir2", false, false, pf));
    fb.addSourceFile(makeGetter(1, 1, 0, 0,  "dir2/testfile4.java", "dir2", true, false, pf));
    fb.addSourceFile(makeGetter(0, 0, 0, 0,  "dir3/testfile5.java", "", false, false, pf));
    fb.addAuxiliaryFile(makeGetter(1, 1, 0, 0, absp + "test/testfile6.java", "/home/javaplt", false, false, null));
    fb.addAuxiliaryFile(makeGetter(1, 1, 0, 0, absp + "test/testfile7.java", "/home/javaplt", false, false, null));
    fb.addCollapsedPath("./[ Source Files ]/dir1/");
    fb.addClassPathFile(new AbsRelFile(parent, "lib"));
    fb.setBuildDirectory(new File(parent, "built"));
    fb.setWorkingDirectory(new File(parent, "src"));
    fb.setMainClass("some.main.ClassName");
    fb.write();

    StringBuilder received = new StringBuilder();
    FileReader fr = new FileReader(pf);
    int c = fr.read();
    while (c >= 0) {
      received.append((char)c);
      c = fr.read();
    }
    fr.close();
//    assertEquals("Make relative", "dir1/test.java",
//                 fb.makeRelative(new File(pf.getParentFile(),"dir1/test.java")));
//    assertEquals("The file written by the builder", expected, received);

    // parse in the file that was just written.
    ProjectFileIR pfir = null;
    try { pfir = ProjectFileParserFacade.ONLY.parse(pf); }
    catch(MalformedProjectFileException e) {
      throw new MalformedProjectFileException(e.getMessage() + ", file: " + pf);
    }
    assertEquals("number of source files", 5, pfir.getSourceFiles().length);
    assertEquals("number of aux files", 2, pfir.getAuxiliaryFiles().length);
    assertEquals("number of collapsed", 1, pfir.getCollapsedPaths().length);
    assertEquals("number of classpaths", 1, IterUtil.sizeOf(pfir.getClassPaths()));

//    assertEquals("first source filename", new File(parent,"/dir1/testfile1.java").getPath(), 
//                 pfir.getSourceFiles()[0].getPath());
//    assertEquals("last source filename", new File(parent,"/dir3/testfile5.java").getPath(), 
//                 pfir.getSourceFiles()[4].getPath());
    assertEquals("first aux filename", new File(parent,"test/testfile6.java").getCanonicalPath(), 
                 pfir.getAuxiliaryFiles()[0].getCanonicalPath());
    assertEquals("last collapsed path", "./[ Source Files ]/dir1/", pfir.getCollapsedPaths()[0]);
    assertEquals("build-dir name", buildDir.getCanonicalPath(), pfir.getBuildDirectory().getCanonicalPath());
    assertEquals("work-dir name", srcDir.getCanonicalPath(), pfir.getWorkingDirectory().getCanonicalPath());
    assertEquals("classpath name", new File(parent,"lib").getCanonicalPath(),
                 IterUtil.first(pfir.getClassPaths()).getCanonicalPath());
    assertEquals("main-class name", "some.main.ClassName",
                 pfir.getMainClass());
    pf.delete();
  }
  
  // ----- ProjectFileParser -----
  
  /** Test to make sure all elements of the project are read correctly into the IR */
  public void testLegacyParseProjectPJT() throws IOException, MalformedProjectFileException, java.text.ParseException {
    String proj1 =
      ";; DrJava project file.  Written with build: 20040623-1933\n" +
      "(source ;; comment\n" +
      "   (file (name \"src/sexp/Atom.java\")(select 32 32)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"src/sexp/BoolAtom.java\")(select 0 0)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"src/sexp/Cons.java\")(select 0 0)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"src/sexp/Empty.java\")(select 24 28)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"src/sexp/Lexer.java\")(select 0 0)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"src/sexp/NumberAtom.java\")(select 12 12)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"src/sexp/SEList.java\")(select 0 0)))\n" + // doesn't have mod date
      "(auxiliary ;; absolute file names\n" +
      "   (file (name " + convertToLiteral(new File(parent, "junk/sexp/Tokens.java").getCanonicalPath()) + 
         ")(select 32 32)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name " + convertToLiteral(new File(parent, "jdk1.5.0/JScrollPane.java").getCanonicalPath()) + 
         ")(select 9086 8516)(mod-date \"16-Jul-2004 03:45:23\")))\n" +
      "(collapsed ;; relative paths\n" +
      "   (path \"./[ Source Files ]/sexp/\")\n" +
      "   (path \"./[ External ]/\"))\n" +
      "(build-dir ;; absolute path\n" +
      "   (file (name " +  convertToLiteral(new File(parent,"built").getCanonicalPath()) + ")))\n" +
      "(work-dir ;; absolute path\n" +
      "   (file (name " +  convertToLiteral(new File(parent,"src").getCanonicalPath()) + ")))\n" +
      "(proj-root ;; absolute path\n" +
      "   (file (name " +  convertToLiteral(new File(parent,"src").getCanonicalPath()) + ")))\n" +
      "(classpaths\n" +
      "   (file (name " +  convertToLiteral(new File(parent,"src/edu/rice/cs/lib").getCanonicalPath()) + ")))\n" +
      "(main-class\n" +
      "   some.thing.ClassName)";
    
    File f = new File(parent, "test1.pjt");

    IOUtil.writeStringToFile(f, proj1);
//    System.err.println("Project directory is " + parent);
//    System.err.println("Project file is " + f);
//    System.err.println("projFile exists? " + f.exists());
    ProjectFileIR pfir = ProjectFileParser.ONLY.parse(f);
//    System.err.println("buildDir = " + pfir.getBuildDirectory().getCanonicalPath());
    assertEquals("number of source files", 7, pfir.getSourceFiles().length);
    assertEquals("number of aux files", 2, pfir.getAuxiliaryFiles().length);
    assertEquals("number of collapsed", 2, pfir.getCollapsedPaths().length);
    assertEquals("number of classpaths", 1, IterUtil.sizeOf(pfir.getClassPaths()));
    File base = f.getParentFile();
    assertEquals("first source filename", new File(base,"src/sexp/Atom.java").getPath(), pfir.getSourceFiles()[0].getPath());
    assertEquals("mod-date value", 
                 ProjectProfile.MOD_DATE_FORMAT.parse("16-Jul-2004 03:45:23").getTime(),
                 pfir.getSourceFiles()[0].getSavedModDate());
    assertEquals("last source filename", new File(base,"src/sexp/SEList.java").getPath(), 
                 pfir.getSourceFiles()[6].getPath());
    assertEquals("first aux filename", new File(base,"junk/sexp/Tokens.java").getPath(), 
                 pfir.getAuxiliaryFiles()[0].getCanonicalPath());
    assertEquals("last collapsed path", "./[ External ]/", pfir.getCollapsedPaths()[1]);
    assertEquals("build-dir name", new File(base, "built").getCanonicalPath(), 
                 pfir.getBuildDirectory().getCanonicalPath());
    assertEquals("work-dir name", new File(base, "src").getCanonicalPath(), 
                 pfir.getWorkingDirectory().getCanonicalPath());
    assertEquals("classpath name", new File(base, "src/edu/rice/cs/lib").getCanonicalPath(), 
                 IterUtil.first(pfir.getClassPaths()).getCanonicalPath());
    assertEquals("main-class name", "some.thing.ClassName", 
                 pfir.getMainClass());
  }

   /** Test to make sure all elements of the project are read correctly into the IR */
  public void testParseProjectPJT() throws IOException, MalformedProjectFileException, java.text.ParseException {
    String proj2 =
      ";; DrJava project file.  Written with build: 2006??\n" +
      "(proj-root-and-base (file (name \"src\")))\n" +
      "(source-files ;; comment\n" +
      "   (file (name \"sexp/Atom.java\")(select 32 32)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"sexp/BoolAtom.java\")(select 0 0)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"sexp/Cons.java\")(select 0 0)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"sexp/Empty.java\")(select 24 28)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"sexp/Lexer.java\")(select 0 0)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"sexp/NumberAtom.java\")(select 12 12)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name \"sexp/SEList.java\")(select 0 0)))\n" + // doesn't have mod date
      "(auxiliary ;; absolute file names\n" +
      "   (file (name " + convertToLiteral(new File(parent,"junk/sexp/Tokens.java").getCanonicalPath()) +
          ")(select 32 32)(mod-date \"16-Jul-2004 03:45:23\"))\n" +
      "   (file (name " + convertToLiteral(new File(parent,"jdk1.5.0/JScrollPane.java").getCanonicalPath()) +
          ")(select 9086 8516)(mod-date \"16-Jul-2004 03:45:23\")))\n" +
      "(collapsed ;; relative paths\n" +
      "   (path \"./[ Source Files ]/sexp/\")\n" +
      "   (path \"./[ External ]/\"))\n" +
      "(build-dir ;; absolute path\n" +
      "   (file (name " +  convertToLiteral(new File(parent, "built").getCanonicalPath()) + ")))\n" +
      "(work-dir (file (name \"src\")))\n" +
      "(classpaths\n" +
      "   (file (name " +  convertToLiteral(new File(parent, "src/edu/rice/cs/lib").getCanonicalPath()) + ")))\n" +
      "(main-class\n" +
      "   some.thing.Name)";
    
    File f = new File(parent, "test2.pjt");

    IOUtil.writeStringToFile(f, proj2);
//    System.err.println("Project directory is " + parent);
//    System.err.println("Project file is " + f);
//    System.err.println("projFile exists? " + f.exists());
    ProjectFileIR pfir = ProjectFileParser.ONLY.parse(f);
//    System.err.println("buildDir = " + pfir.getBuildDirectory().getCanonicalPath());
    assertEquals("number of source files", 7, pfir.getSourceFiles().length);
    assertEquals("number of aux files", 2, pfir.getAuxiliaryFiles().length);
    assertEquals("number of collapsed", 2, pfir.getCollapsedPaths().length);
    assertEquals("number of classpaths", 1, IterUtil.sizeOf(pfir.getClassPaths()));
    File base = f.getParentFile();
    File root = new File(base, "src");
    assertEquals("proj-root-and-base", root.getPath(), pfir.getProjectRoot().getPath());
    assertEquals("first source filename", new File(base,"src/sexp/Atom.java").getPath(), pfir.getSourceFiles()[0].getPath());
    assertEquals("mod-date value", 
                 ProjectProfile.MOD_DATE_FORMAT.parse("16-Jul-2004 03:45:23").getTime(),
                 pfir.getSourceFiles()[0].getSavedModDate());
    assertEquals("last source filename", new File(root, "sexp/SEList.java").getPath(), 
                 pfir.getSourceFiles()[6].getPath());
    assertEquals("first aux filename", new File(base,"junk/sexp/Tokens.java").getPath(), 
                 pfir.getAuxiliaryFiles()[0].getCanonicalPath());
    assertEquals("last collapsed path", "./[ External ]/", pfir.getCollapsedPaths()[1]);
    assertEquals("build-dir name", new File(base, "built").getCanonicalPath(), 
                 pfir.getBuildDirectory().getCanonicalPath());
    assertEquals("work-dir name", new File(base, "src").getCanonicalPath(), 
                 pfir.getWorkingDirectory().getCanonicalPath());
    assertEquals("classpath name", new File(base, "src/edu/rice/cs/lib").getCanonicalPath(), 
                 IterUtil.first(pfir.getClassPaths()).getCanonicalPath());
    assertEquals("main-class name", "some.thing.Name", 
                 pfir.getMainClass());
  }

  public void testWriteFilePJT() throws IOException, MalformedProjectFileException {
    File pf = new File(parent, "test3.pjt");
    IOUtil.writeStringToFile(pf, "");
    ProjectProfile fb = new ProjectProfile(pf);

    fb.addSourceFile(makeGetter(0, 0, 0, 0,  "dir1/testfile1.java", "dir1", false, false, pf));
    fb.addSourceFile(makeGetter(1, 1, 0, 0,  "dir1/testfile2.java", "dir1", false, false, pf));
    fb.addSourceFile(makeGetter(20, 22, 0, 0, "dir2/testfile3.java", "dir2", false, false, pf));
    fb.addSourceFile(makeGetter(1, 1, 0, 0,  "dir2/testfile4.java", "dir2", true, false, pf));
    fb.addSourceFile(makeGetter(0, 0, 0, 0,  "dir3/testfile5.java", "", false, false, pf));
    fb.addAuxiliaryFile(makeGetter(1, 1, 0, 0, absp + "test/testfile6.java", "/home/javaplt", false, false, null));
    fb.addAuxiliaryFile(makeGetter(1, 1, 0, 0, absp + "test/testfile7.java", "/home/javaplt", false, false, null));
    fb.addCollapsedPath("./[ Source Files ]/dir1/");
    fb.addClassPathFile(new AbsRelFile(parent, "lib"));
    fb.setBuildDirectory(new File(parent, "built"));
    fb.setWorkingDirectory(new File(parent, "src"));
    fb.setMainClass("some.main.ClassName");
    fb.writeOld();

    StringBuilder received = new StringBuilder();
    FileReader fr = new FileReader(pf);
    int c = fr.read();
    while (c >= 0) {
      received.append((char)c);
      c = fr.read();
    }
    fr.close();
//    assertEquals("Make relative", "dir1/test.java",
//                 fb.makeRelative(new File(pf.getParentFile(),"dir1/test.java")));
//    assertEquals("The file written by the builder", expected, received);

    // parse in the file that was just written.
    ProjectFileIR pfir = null;
    try { pfir = ProjectFileParser.ONLY.parse(pf); }
    catch(MalformedProjectFileException e) {
      throw new MalformedProjectFileException(e.getMessage() + ", file: " + pf);
    }
    assertEquals("number of source files", 5, pfir.getSourceFiles().length);
    assertEquals("number of aux files", 2, pfir.getAuxiliaryFiles().length);
    assertEquals("number of collapsed", 1, pfir.getCollapsedPaths().length);
    assertEquals("number of classpaths", 1, IterUtil.sizeOf(pfir.getClassPaths()));

//    assertEquals("first source filename", new File(parent,"/dir1/testfile1.java").getPath(), 
//                 pfir.getSourceFiles()[0].getPath());
//    assertEquals("last source filename", new File(parent,"/dir3/testfile5.java").getPath(), 
//                 pfir.getSourceFiles()[4].getPath());
    assertEquals("first aux filename", new File(parent,"test/testfile6.java").getPath(), 
                 pfir.getAuxiliaryFiles()[0].getPath());
    assertEquals("last collapsed path", "./[ Source Files ]/dir1/", pfir.getCollapsedPaths()[0]);
    assertEquals("build-dir name", buildDir, pfir.getBuildDirectory());
    assertEquals("work-dir name", srcDir, pfir.getWorkingDirectory());
    assertEquals("classpath name", new File(parent,"lib"), IterUtil.first(pfir.getClassPaths()));
    assertEquals("main-class name", "some.main.ClassName", pfir.getMainClass());
    pf.delete();
  }

  // ----- XMLProjectFileParser -----
  
  public void testWriteFileXML() throws IOException, MalformedProjectFileException {
    File pf = new File(parent, "test3.xml");
    IOUtil.writeStringToFile(pf, "");
    ProjectProfile fb = new ProjectProfile(pf);

    fb.addSourceFile(makeGetter(0, 0, 0, 0,  "dir1/testfile1.java", "dir1", false, false, pf));
    fb.addSourceFile(makeGetter(1, 1, 0, 0,  "dir1/testfile2.java", "dir1", false, false, pf));
    fb.addSourceFile(makeGetter(20, 22, 0, 0, "dir2/testfile3.java", "dir2", false, false, pf));
    fb.addSourceFile(makeGetter(1, 1, 0, 0,  "dir2/testfile4.java", "dir2", true, false, pf));
    fb.addSourceFile(makeGetter(0, 0, 0, 0,  "dir3/testfile5.java", "", false, false, pf));
    fb.addAuxiliaryFile(makeGetter(1, 1, 0, 0, absp + "test/testfile6.java", "/home/javaplt", false, false, null));
    fb.addAuxiliaryFile(makeGetter(1, 1, 0, 0, absp + "test/testfile7.java", "/home/javaplt", false, false, null));
    fb.addCollapsedPath("./[ Source Files ]/dir1/");
    fb.addClassPathFile(new AbsRelFile(parent, "lib"));
    fb.setBuildDirectory(new File(parent, "built"));
    fb.setWorkingDirectory(new File(parent, "src"));
    fb.setMainClass("some.main.ClassName");
    fb.write();

    StringBuilder received = new StringBuilder();
    FileReader fr = new FileReader(pf);
    int c = fr.read();
    while (c >= 0) {
      received.append((char)c);
      c = fr.read();
    }
    fr.close();
//    assertEquals("Make relative", "dir1/test.java",
//                 fb.makeRelative(new File(pf.getParentFile(),"dir1/test.java")));
//    assertEquals("The file written by the builder", expected, received);

    // parse in the file that was just written.
    ProjectFileIR pfir = null;
    try { pfir = XMLProjectFileParser.ONLY.parse(pf); }
    catch(MalformedProjectFileException e) {
      throw new MalformedProjectFileException(e.getMessage() + ", file: " + pf);
    }
    assertEquals("number of source files", 5, pfir.getSourceFiles().length);
    assertEquals("number of aux files", 2, pfir.getAuxiliaryFiles().length);
    assertEquals("number of collapsed", 1, pfir.getCollapsedPaths().length);
    assertEquals("number of classpaths", 1, IterUtil.sizeOf(pfir.getClassPaths()));

//    assertEquals("first source filename", new File(parent,"/dir1/testfile1.java").getPath(), 
//                 pfir.getSourceFiles()[0].getPath());
//    assertEquals("last source filename", new File(parent,"/dir3/testfile5.java").getPath(), 
//                 pfir.getSourceFiles()[4].getPath());
    assertEquals("first aux filename", new File(parent,"test/testfile6.java").getCanonicalPath(), 
                 pfir.getAuxiliaryFiles()[0].getCanonicalPath());
    assertEquals("last collapsed path", "./[ Source Files ]/dir1/", pfir.getCollapsedPaths()[0]);
    assertEquals("build-dir name", buildDir.getCanonicalPath(), pfir.getBuildDirectory().getCanonicalPath());
    assertEquals("work-dir name", srcDir.getCanonicalPath(), pfir.getWorkingDirectory().getCanonicalPath());
    assertEquals("classpath name", new File(parent,"lib").getCanonicalPath(),
                 IterUtil.first(pfir.getClassPaths()).getCanonicalPath());
    assertEquals("main-class name", "some.main.ClassName",
                 pfir.getMainClass());
    pf.delete();
  }
  
  private DocumentInfoGetter makeGetter(final int sel1, final int sel2, final int scrollv,
                                        final int scrollh, final String fname, final String pack,
                                        final boolean active, final boolean isUntitled, final File pf) {
    return new DocumentInfoGetter() {
      public Pair<Integer,Integer> getSelection() { 
        return new Pair<Integer,Integer>(Integer.valueOf(sel1),Integer.valueOf(sel2)); 
      }
      public Pair<Integer,Integer> getScroll() { 
        return new Pair<Integer,Integer>(Integer.valueOf(scrollv),Integer.valueOf(scrollh)); 
      }
      public File getFile() {
        if (pf == null) return new File(fname);
        else return new File(pf.getParentFile(),fname);
      }
      public String getPackage() { return pack; }
      public boolean isActive() { return active; }
      public boolean isUntitled() { return isUntitled; }
    };

  }
}
