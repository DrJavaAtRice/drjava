/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.FileOutputStream;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.plt.reflect.JavaVersion;

/** An abstract parent for all javac-based compiler interfaces that may need to filter .exe files from the
  * classpath, i.e. javac from JDKs 1.6.0_04 or newer.
  * @version $Id$
  */
public abstract class Javac160FilteringCompiler extends JavacCompiler {
  protected final boolean _filterExe;
  protected final File _tempJUnit;
  protected static final String PREFIX = "drscala-junit";
  protected static final String SUFFIX = ".jar";  
  
  protected Javac160FilteringCompiler(JavaVersion.FullVersion version,
                                      String location,
                                      List<? extends File> defaultBootClassPath) {
    super(version, location, defaultBootClassPath);

    _filterExe = version.compareTo(JavaVersion.parseFullVersion("1.6.0_04")) >= 0;
    File tempJUnit = null;
    if (_filterExe) {
      // if we need to filter out exe files from the classpath, we also need to
      // extract junit.jar and create a temporary file
      try {
        // edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("jdk160.txt",true);
        // LOG.log("Filtering exe files from classpath.");
        InputStream is = Javac160FilteringCompiler.class.getResourceAsStream("/junit.jar");
        if (is!=null) {
          // LOG.log("\tjunit.jar found");
          tempJUnit = edu.rice.cs.plt.io.IOUtil.createAndMarkTempFile(PREFIX,SUFFIX);
          FileOutputStream fos = new FileOutputStream(tempJUnit);
          int size = edu.rice.cs.plt.io.IOUtil.copyInputStream(is,fos);
          // LOG.log("\t"+size+" bytes written to "+tempJUnit.getAbsolutePath());
        }
        else {
          // LOG.log("\tjunit.jar not found");
          if (tempJUnit!=null) {
            tempJUnit.delete();
            tempJUnit = null;
          }
        }
      }
      catch(IOException ioe) {
        if (tempJUnit!=null) {
          tempJUnit.delete();
          tempJUnit = null;
        }
      }
      // sometimes this file may be left behind, so create a shutdown hook
      // that deletes temporary files matching our pattern
      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        public void run() {
          try {
            File temp = File.createTempFile(PREFIX, SUFFIX);
            IOUtil.attemptDelete(temp);
            File[] toDelete = temp.getParentFile().listFiles(new FilenameFilter() {
              public boolean accept(File dir, String name) {
                if ((!name.startsWith(PREFIX)) || (!name.endsWith(SUFFIX))) return false;
                String rest = name.substring(PREFIX.length(), name.length()-SUFFIX.length());
                try {
                  Integer i = new Integer(rest);
                  // we could create an integer from the rest, this is one of our temporary files
                  return true;
                }
                catch(NumberFormatException e) { /* couldn't convert, ignore this file */ }
                return false;
              }
            });
            for(File f: toDelete) {
              f.delete();
            }
          }
          catch(IOException ioe) { /* could not delete temporary files, ignore */ }
        }
      })); 
    }
    _tempJUnit = tempJUnit;
  }
  
  protected java.util.List<File> getFilteredClassPath(java.util.List<? extends File> classPath) {
    java.util.List<File> filteredClassPath = null;
    if (classPath!=null) {
      filteredClassPath = new LinkedList<File>(classPath);
      
      if (_filterExe) {
        FileFilter filter = IOUtil.extensionFilePredicate("exe");
        Iterator<? extends File> i = filteredClassPath.iterator();
        while (i.hasNext()) {
          if (filter.accept(i.next())) { i.remove(); }
        }
        if (_tempJUnit!=null) { filteredClassPath.add(_tempJUnit); }
      }
    }
    return filteredClassPath;
  }
}
