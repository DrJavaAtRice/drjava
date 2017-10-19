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

/**
 * <p>A class loader defining the search for classes and resources to first check
 * a parent loader, and then delegate to a child loader.  This allows two distinct
 * class loading hierarchies to be merged.</p>
 * 
 * <p>One application is to reverse the usual search order: if I'm given loader {@code c} and 
 * would like to define an instance of {@code MyLoader} that delegates to {@code c} before checking 
 * its own resources, I can invoke {@code new MyLoader(c)}; on the other hand, if I want to check 
 * the {@code MyLoader's} resources <em>first</em>, I might do so with a {@code ComposedClassLoader}:
 * {@code new ComposedClassLoader(new MyLoader(null), c)}.</p>
 */
public class ComposedClassLoader extends ClassLoader {
  
  private final ClassLoader _child;
  
  public ComposedClassLoader(ClassLoader parent, ClassLoader child) {
    super(parent);
    _child = child;
  }
  
  @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
    return _child.loadClass(name);
  }
  
  @Override protected URL findResource(String name) {
    return _child.getResource(name);
  }
  
}
