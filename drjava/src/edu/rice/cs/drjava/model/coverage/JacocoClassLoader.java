/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the names of its contributors may be used 
 *      to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO: PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software. Open Source Initative Approved is a trademark
 * of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/ or 
 * http://github.com/DrJavaAtRice/drjava.
 * 
 * This file is derived from code created for SETTE: Symbolic Execution based Test Tool Evaluator by Budapest University
 * of Technology and Economics (BME).  The following comments document the licensing terms for this code base.
 * 
 * SETTE is a tool to help the evaluation and comparison of symbolic execution based test input  
 * generator tools. 
 * 
 * Budapest University of Technology and Economics (BME)
 * 
 * Authors: Lajos Cseppentő <lajos.cseppento@inf.mit.bme.hu>, Zoltán Micskei <micskeiz@mit.bme.hu> 
 * 
 * Copyright 2014-2015 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with 
 * the License. You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the  License is distributed on 
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and  limitations under the License. 
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.coverage;


import java.io.File;
import java.io.IOException;

import java.lang.ClassLoader;

import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.IterableOps;
import edu.rice.cs.util.UnexpectedException;

import org.jacoco.core.instr.Instrumenter;

/** A class loader that instruments classes for code coverage.
  * @version $Id$
  */
public class JacocoClassLoader extends ClassLoader {
  
  private static final Log _log = new Log("JUnitTestManager.txt", false);
  
  private final File[] _binaryDirectories; 
  private final Instrumenter _instrumenter; 
  
  public JacocoClassLoader(Iterable<File> binaryDirectories, Instrumenter instrumenter, ClassLoader parent) { 
    super(parent); 
    
    _binaryDirectories = IterableOps.toArray(binaryDirectories, File.class); 
    _instrumenter = instrumenter; 
    
    _log.log("JaCoCoClassLoader has been created"); 
  } 
  
  @Override 
  protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException { 
    
    Class<?> javaClass = findLoadedClass(className); 
    
    if (javaClass != null) { 
      // class was already loaded (and instrumented if bytecode was found) 
      _log.log("The class " + className + " was already loaded"); 
      return javaClass; 
    } 
    
    try { 
      // first try to load from one of the binary directories and instrument the class
      File classFile = findBinaryFile(className);
      if (classFile != null) {
        _log.log("Found the class file " + classFile + " for the class " + className);
        _log.log("Instrumenting and defining class: " + className); 
        final byte[] instrumentedBytes = _instrumenter.instrument(IOUtil.toByteArray(classFile), className); 
        final Class<?> definedClass = defineClass(className, instrumentedBytes, 0, instrumentedBytes.length);
        _log.log("Returning instrumented class " + className);
        return definedClass;
      }  
      else { 
        // was not found, try to load with the parent, but it will not be instrumented 
        _log.log("Calling super.loadClass() for class " + className + " (corresponding file was not found by jacoco)"); 
        return super.loadClass(className, resolve); 
      } 
    } catch (IOException ex) { 
      _log.log("In loading " + className + ", an IOException was thrown by jacoco", ex); 
      throw new UnexpectedException(ex); 
    } 
  } 
  
  public Class<?> tryLoadClass(String name) { 
    try { return loadClass(name); } 
    catch (ClassNotFoundException ex) { return null; } 
  } 
  
  /** Finds the corresponding binary file for the specified class. 
    * @param className the name of the class 
    * @return the binary file or null if it was not found 
    */ 
  public File findBinaryFile(String className) { 
    
    // iterate binary directories in order 
    for (File dir : _binaryDirectories) { 
      File file = new File(dir, FileOps.classNameToClassFilename(className)); 
      if (file.exists()) return file; 
    } 
    
    // not found 
    return null; 
  } 
}
