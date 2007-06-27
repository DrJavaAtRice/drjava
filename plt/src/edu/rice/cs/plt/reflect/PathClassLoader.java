/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007 JavaPLT group at Rice University
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
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.io.File;
import java.util.LinkedList;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.debug.DebugUtil;

/**
 * A class loader that mimics the standard application system loader by loading classes from
 * a file path of directories and jar files.
 */
public class PathClassLoader extends URLClassLoader {
  
  /**
   * Create a path class loader with the default parent ({@link ClassLoader#getSystemClassLoader})
   * and the specified path.
   */
  public PathClassLoader(File... path) { this(IterUtil.asIterable(path)); }
  
  /**
   * Create a path class loader with the default parent ({@link ClassLoader#getSystemClassLoader})
   * and the specified path.
   */
  public PathClassLoader(Iterable<? extends File> path) { super(makeURLs(path)); }
  
  /** Create a path class loader with the given parent and path */
  public PathClassLoader(ClassLoader parent, File... path) { this(parent, IterUtil.asIterable(path)); }
  
  /** Create a path class loader with the given parent and path */
  public PathClassLoader(ClassLoader parent, Iterable<? extends File> path) {
    super(makeURLs(path), parent);
  }
  
  private static URL[] makeURLs(Iterable<? extends File> path) {
    LinkedList<URL> result = new LinkedList<URL>();
    for (File f : path) {
      try { result.add(f.toURI().toURL()); }
      catch (IllegalArgumentException e) { DebugUtil.error.log(e); }
      catch (MalformedURLException e) { DebugUtil.error.log(e); }
      // just skip the path element if there's an error
    }
    return result.toArray(new URL[result.size()]);
  }
  
}
