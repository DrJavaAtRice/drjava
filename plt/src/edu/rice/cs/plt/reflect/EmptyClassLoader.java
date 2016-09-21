/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.reflect;

import java.net.URL;
import java.io.InputStream;

/**
 * A class loader that will load no classes.  Useful as the root of class loader trees
 * in which the system class loader should not be the root.
 */
public class EmptyClassLoader extends ClassLoader {
  
  public static final EmptyClassLoader INSTANCE = new EmptyClassLoader();
  
  private EmptyClassLoader() { super(null); }
  
  @Override public Class<?> loadClass(String name) throws ClassNotFoundException {
    throw new ClassNotFoundException();
  }
  
  @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    throw new ClassNotFoundException();
  }
  
  @Override public URL getResource(String name) { return null; }
  
// This is final in Java 1.4.  To support backwards-compatibility, we cannot override it.
// (Otherwise, in 1.4 we get a VerifyError.)
//  @Override public Enumeration<URL> getResources(String name) {
//    return IterUtil.asEnumeration(EmptyIterator.<URL>make());
//  }
  
  @Override public InputStream getResourceAsStream(String name) { return null; }
  
  @Override protected String findLibrary(String libName) { return null; }
  
}
