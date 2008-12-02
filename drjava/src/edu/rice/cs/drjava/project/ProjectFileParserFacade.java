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

import java.io.*;
import edu.rice.cs.drjava.project.MalformedProjectFileException;

/** Abstract project file parser. */
public class ProjectFileParserFacade {  
  /** Singleton instance of ProjectFileParserFacade */
  public static final ProjectFileParserFacade ONLY = new ProjectFileParserFacade();
  protected ProjectFileParserFacade() { }

  protected File _projectFile;
  protected boolean _xmlProjectFile;
  
  /** @param projFile the file to parse
   *  @return the project file IR
   */
  public ProjectFileIR parse(File projFile) throws IOException, FileNotFoundException, MalformedProjectFileException {
    FileReader fr = new FileReader(projFile);
    int read = fr.read();
    if (read==-1) {
      // empty project file, throw exception
      throw new MalformedProjectFileException("Empty project file.");
    }
    if (((char)read)!=';') {
      // does not start with a ';', can't be an old S-expression format project file
      // try new XML format parser
      fr.close();
      return XMLProjectFileParser.ONLY.parse(projFile);
    }
    read = fr.read();
    if (read==-1) {
      // project file just contained ";", throw exception
      throw new MalformedProjectFileException("Incomplete project file.");
    }
    if (((char)read)!=';') {
      // does not start with ";;", can't be an old S-expression format project file
      // try new XML format parser
      fr.close();
      return XMLProjectFileParser.ONLY.parse(projFile);
    }
    fr.close();
    // file started with ";;", try old S-expression format parser
    return ProjectFileParser.ONLY.parse(projFile);
  }
}
