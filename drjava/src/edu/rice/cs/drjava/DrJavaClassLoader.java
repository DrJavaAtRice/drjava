/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
 END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava;

import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
/**
 * Custom classloader, that loads from files or URLs
 * modeled after the NextGen classloader (edu.rice.cs.nextgen.classloader.NextGenLoader)
 * $Id$
 */
public class DrJavaClassLoader extends ClassLoader { //URLClassLoader 
//  implements Serializable {

  public DrJavaClassLoader() {
    super(); //new URL[0]);
  }
  
  public DrJavaClassLoader(URL[] urls) {
    super(); //urls);
  }
  
  public DrJavaClassLoader(URL[] urls, ClassLoader parent) { 
    // super(urls,parent);
    super(parent);
  }
  
  private static final int BUFFER_SIZE = 0x2800; // 10K
  private final byte[] readBuffer = new byte[BUFFER_SIZE];
  
  public static String dotToSlash(String s) {
    return s.replace('.','/');
    // System.getProperty("file.separator").charAt(0));
  }
  
  /** 
   * Replace all instances of find with repl in orig, and return the new
   * String.
   */
  public static String replaceSubstring(String orig, String find, String repl) {
    StringBuffer buf = new StringBuffer();
    int pos = 0;
    while (pos < orig.length()) {
      int foundPos = orig.indexOf(find, pos);
      if (foundPos == -1) {
        break;
      } else {
        buf.append(orig.substring(pos, foundPos));
        buf.append(repl);
        pos = foundPos + find.length();
      }
    }
    
    // Now add on everything after the last match
    buf.append(orig.substring(pos));
    return buf.toString();
  }
  
  /** Gets byte[] for class file, or throws IOException. */
  private synchronized byte[] readClassFile(String className) throws IOException {
    // getResourceAsStream finds a file that's in the classpath. It's generally
    // used to load resources (like images) from the same location as
    // class files. However for our purposes of loading the bytes of a class
    // file, this works perfectly. It will find the class in any place in
    // the classpath, and it doesn't force us to search the classpath ourselves.
    String fileName = dotToSlash(className) + ".class";
    InputStream stream = getResourceAsStream(fileName);
    
    if (stream == null) {
      throw new IOException("Resource not found: " + fileName);
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE);
    for(int c = stream.read(readBuffer);
        c != -1; c = stream.read(readBuffer)) {
      baos.write(readBuffer,0,c);
    }
    stream.close();
    baos.close();
    //byte[] data = new byte[stream.available()];
    //stream.read(data);    
    //return data;
    return baos.toByteArray();
  }
  
  protected Class loadClass(String name, boolean resolve)
    throws ClassNotFoundException {
    Class clazz;
    
    // We want to actually load the class ourselves, if security allows us to.
    // This is so that the classloader associated with the class is ours, not
    // the system loader. But some classes (java.*, etc) must be loaded with
    // the system loader.
    if (mustUseSystemLoader(name)) {
      clazz = findSystemClass(name);
    } else {
      // We can load it ourselves. Let's get the bytes.
      try {
        byte[] classData = readClassFile(name);
        // delegates to superclass to define the class
        clazz = defineClass(name, classData, 0, classData.length);
      } catch (IOException ioe) {
        //System.err.println("Got IO Exception reading class file");
        //ioe.printStackTrace();
        throw new ClassNotFoundException("IO Exception in reading class file: " + ioe);
      }
    }
    
    if (resolve) {
      resolveClass(clazz);
    }
    
    return clazz;
  }
    
  /**
   * Map of package name (string) to whether must use system loader (boolean).
   */
  private final HashMap _checkedPackages = new HashMap();
  
  public boolean mustUseSystemLoader(String name) {
    // If name begins with java., must use System loader. This
    // is regardless of the security manager.
    // javax. too, though this is not documented
    if (name.startsWith("java.") || name.startsWith("javax.")) {
      return true;
    }
    
    SecurityManager _security = System.getSecurityManager();
    
    // No security manager? We can do whatever we want!
    if (_security == null) {
      return false;
    }
    
    int lastDot = name.lastIndexOf('.');
    String packageName;
    if (lastDot == -1) {
      packageName = "";
    }
    else {
      packageName = name.substring(0, lastDot);
    }
    
    // Check the cache first
    Object cacheCheck = _checkedPackages.get(packageName);
    if (cacheCheck != null) {
      return ((Boolean) cacheCheck).booleanValue();
    }
    
    // Now try to get the package info. If it fails, it's a system class.
    try {
      _security.checkPackageDefinition(packageName);
      // Succeeded, so does not require system loader.
      _checkedPackages.put(packageName, Boolean.FALSE);
      return false;
    }
    catch (SecurityException se) {
      // Failed, so does require system loader.
      _checkedPackages.put(packageName, Boolean.TRUE);
      return true;
    }
  }
}

