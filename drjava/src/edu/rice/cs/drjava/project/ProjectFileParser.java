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

public class ProjectFileParser {
  /* singleton instance of ProjectFileParser */
  public static ProjectFileParser ONLY = new ProjectFileParser();
  
  private ProjectFileParser(){}
  
  public ProjectFileIR parse(File projFile) throws IOException, FileNotFoundException {
    BufferedReader r = new BufferedReader(new FileReader(projFile));
    /* we actually want the path to the project file, not the file itself */
    if( projFile.isFile() ) {
      projFile = projFile.getParentFile();
    }
    SourceTag st = TagFactory.makeSourceTag(projFile, r);
    ResourceTag rt = TagFactory.makeResourceTag(projFile, r);
    MiscTag mt = TagFactory.makeMiscTag(projFile, r);
    ClasspathTag ct = TagFactory.makeClasspathTag(projFile, r);
    JarTag jt = TagFactory.makeJarTag(projFile, r);
    
    return new ProjectFileIRImpl(st, rt, mt, ct, jt);
  }
  
  /**
   * concrete implementation of the ProjectFileIR which is the intreface through which DrJava
   * access info stored in a project file
   */
  class ProjectFileIRImpl implements ProjectFileIR {
    private SourceTag    _source = null;
    private ResourceTag  _resource = null;
    private MiscTag      _misc = null;
    private ClasspathTag _classpath = null;
    private JarTag       _jar = null;
    
    public ProjectFileIRImpl(SourceTag st, ResourceTag rt, MiscTag mt, ClasspathTag ct, JarTag jt) {
      _source = st;
      _resource = rt;
      _misc = mt;
      _classpath = ct;
      _jar = jt;
    }
    
    /**
     * @return an array full of all the source files in this project file
     */
    public File[] getSourceFiles() {
      return _source.entries();
    }
    
    /**
     * @return an array full of all the resource files in this project file
     */
    public File[] getResourceFiles() {
      return _resource.entries();
    }
    
    /**
     * @return an array full of all the miscellaneous files in this project file
     */
    public File[] getMiscFiles() {
      return _misc.entries();
    }
    
    /**
     * @return an array full of all the classpath path elements in the classpath for this project file
     */
    public File[] getClasspath() {
      return _classpath.entries();
    }
    
    /**
     * @return an the name of the Jar main class associated with this project
     */
    public String getJarMainClass() {
      return _jar.entries()[0].getName();
    } 
  }
}
