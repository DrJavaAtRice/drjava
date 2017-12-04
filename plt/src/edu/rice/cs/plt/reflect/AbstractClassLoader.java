/*BEGIN_COPYRIGHT_BLOCK*
 * 
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

import java.io.InputStream;
import java.io.IOException;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.io.IOUtil;

/**
 * <p>A class loader that provides a helper method definePackageForClass.</p>  
 */
public abstract class AbstractClassLoader extends ClassLoader {
  /**
   * Creates a new class loader using the <tt>ClassLoader</tt> returned by
   * the method {@link #getSystemClassLoader()
   * <tt>getSystemClassLoader()</tt>} as the parent class loader.
   *
   * <p> If there is a security manager, its {@link
   * SecurityManager#checkCreateClassLoader()
   * <tt>checkCreateClassLoader</tt>} method is invoked.  This may result in
   * a security exception.  </p>
   *
   * @throws  SecurityException
   *          If a security manager exists and its
   *          <tt>checkCreateClassLoader</tt> method doesn't allow creation
   *          of a new class loader.
   */  
  protected AbstractClassLoader() { super(); }
  
  /**
   * Creates a new class loader using the specified parent class loader for
   * delegation.
   *
   * <p> If there is a security manager, its {@link
   * SecurityManager#checkCreateClassLoader()
   * <tt>checkCreateClassLoader</tt>} method is invoked.  This may result in
   * a security exception.  </p>
   *
   * @param  parent
   *         The parent class loader
   *
   * @throws  SecurityException
   *          If a security manager exists and its
   *          <tt>checkCreateClassLoader</tt> method doesn't allow creation
   *          of a new class loader.
   *
   * @since  1.2
   */
  protected AbstractClassLoader(ClassLoader parent) { super(parent); }
  
  /** Defines a package for a class, unless it has been defined already. This must be done before
    * the class is defined.
    * @param className the name of the class that is being loaded
    */
  protected Package definePackageForClass(String className) {
    int lastDotPos = className.lastIndexOf('.');
    if (lastDotPos<0) return null; // no package in name, default package == null
    
    String packageName = className.substring(0,lastDotPos);
    Package pack = getPackage(packageName);
    if (pack==null) {
      // not yet defined
      pack = definePackage(packageName, null, null, null, null, null, null, null);
    }
    return pack;
  }
}
