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

package edu.rice.cs.drjava.ui;

import java.io.File;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.DrJavaFileUtils;

/** A file filter for all source files. If a ".dj?" file exists, the corresponding ".java" file is not
  * shown. 
 *  @version $Id$
 */
public class SmartSourceFilter extends JavaSourceFilter {
  
  /** Returns true if the file's extension matches ".java" or ".dj?". */
  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }
    
    String name = f.getName();
    if (DrJavaFileUtils.isLLFile(name)) { return true; }
    if (!name.endsWith(OptionConstants.JAVA_FILE_EXTENSION)) { return false; }

    // this is a ".java" file
    File parent = f.getParentFile();
    if (parent==null) {
      // can't do the smart thing; but since this is a ".java" file, accept it
      return true;
    }
    
    if (new File(parent,DrJavaFileUtils.getDJForJavaFile(name)).exists()) return false;
    if (new File(parent,DrJavaFileUtils.getDJ0ForJavaFile(name)).exists()) return false;
    if (new File(parent,DrJavaFileUtils.getDJ1ForJavaFile(name)).exists()) return false;
    if (new File(parent,DrJavaFileUtils.getDJ2ForJavaFile(name)).exists()) return false;

    return true; // ".java" and no matching ".dj?"
  }

  /** @return A description of this filter to display. */
  public String getDescription() {
    return "DrJava source files (*"+OptionConstants.JAVA_FILE_EXTENSION+", *"+
      OptionConstants.DJ_FILE_EXTENSION+", *"+OptionConstants.OLD_DJ0_FILE_EXTENSION+", *"+
      OptionConstants.OLD_DJ1_FILE_EXTENSION+", *"+OptionConstants.OLD_DJ2_FILE_EXTENSION+")";
  }
}
