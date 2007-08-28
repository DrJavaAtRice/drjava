/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.drjava.model;

import java.lang.ClassLoader;
import java.net.URL;

import edu.rice.cs.drjava.model.repl.WrapperClassLoader;


public class BrainClassLoader extends ClassLoader {
  
  ClassLoader projectCL;
  ClassLoader buildCL;
  ClassLoader projectFilesCL;
  ClassLoader externalFilesCL;
  ClassLoader extraCL;
  ClassLoader systemCL;
  
  public BrainClassLoader(ClassLoader p, ClassLoader b, ClassLoader pf, ClassLoader ef, ClassLoader e) {
    projectCL = p;
    buildCL = b;
    projectFilesCL = pf;
    externalFilesCL = ef;
    extraCL = e;
    systemCL = new WrapperClassLoader(this.getClass().getClassLoader().getSystemClassLoader());
  }
  
  /** Handles getting the resource for loading a class. */
  public URL getResource(String name) {
    URL resource = projectCL.getResource(name);
    if (resource != null) return resource;
    
    resource = buildCL.getResource(name);
    if (resource != null) return resource;
    
    resource = projectFilesCL.getResource(name);
    if (resource != null) return resource;
    
    resource = externalFilesCL.getResource(name);
    if (resource != null) return resource;
    
    resource = extraCL.getResource(name);
    if (resource != null) return resource;

    resource = systemCL.getResource(name);
    if (resource != null) return resource;

    return resource;
  }
}




