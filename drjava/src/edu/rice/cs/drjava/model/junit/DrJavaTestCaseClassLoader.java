/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.junit;

import junit.runner.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.zip.*;


/**
 * A custom classloader for use in running test cases.
 * this will load all classes for the test case. 
 * 
 * this loader also provides an excludes list. any class that matches
 * an entry in the list will be loaded by the system class loader
 * instead.
 * 
 * This class extends junit.runner.TestCaseClassLoader. however,
 * since the junit version kept all non public code as private, we
 * had to duplicate the class to add the features we need.
 * 
 * getResource and getResourceAsStream will not defer to the system
 * class loader, as the junit version does, but instead will use
 * the custom internal classpath to get resources.
 * 
 * This allows this class to be used an a remote classloader to test
 * cases that spawn multiple jvms. 
 * 
 * @see edu.rice.cs.util.newjvm.CustomSystemClassLoader CustomSystemClassLoader for details on
 * using remote class loaders
 */


public final class DrJavaTestCaseClassLoader extends TestCaseClassLoader {
  /** the class loader that will load from the custom inner classpath */
  ClassLoader _loader;
  
  /** scanned class path */
  private Vector<String> fPathItems;
  
  /** default excluded paths */
  private String[] defaultExclusions= {"junit.framework.", "junit.extensions.", "junit.runner.", "java."};
  
  /** Name of excluded properties file */
  static final String EXCLUDED_FILE= "excluded.properties";
  
  /** Excluded paths */
  private Vector<String> fExcluded;
  
  /** Constructs a TestCaseLoader. It scans the class path and the excluded package paths. */
  public DrJavaTestCaseClassLoader() { this(System.getProperty("java.class.path")); }
  
  /** Constructs a TestCaseLoader. It scans the class path and the excluded package paths. */
  public DrJavaTestCaseClassLoader(String classPath) {
    _loader = getClass().getClassLoader();
    scanPath(classPath);
    readExcludedPackages();
  }
  
  /** Scans the classpath, and creates an internal IR for the classpath and initializes the 
   *  internal loader
   */
  private void scanPath(String classPath) {
    String separator= System.getProperty("path.separator");
    fPathItems= new Vector<String>(10);
    StringTokenizer st= new StringTokenizer(classPath, separator);
    String item;
    while (st.hasMoreTokens()) {
      item = st.nextToken();
      fPathItems.addElement(item);
      try{ _loader = new DrJavaURLClassLoader(new URL[]{new File(item).toURL()}, _loader); }
      catch(MalformedURLException e) { 
        /* do nothing */
      }
    }
  }
  
  /** Gets a resource from the custom classpath. */
  public URL getResource(String name) { return _loader.getResource(name); }
  
  /** Gets a resource stream from the custom classpath. */
  public InputStream getResourceAsStream(String name) {
    return _loader.getResourceAsStream(name);
  } 
  
  /** Teturns true if the classname should be excluded
   *  (loaded from system), false otherwise
   */
  public boolean isExcluded(String name) {
    for (int i= 0; i < fExcluded.size(); i++) {
      if (name.startsWith(fExcluded.elementAt(i))) return true;
    }
    return false; 
  }
  
  /**
   * loads the class
   * 1st. checks if the class is already loaded
   * 2nd. checks if it should be loaded by system
   * 3rd. try to load the class myself
   */
  public synchronized Class<?> loadClass(String name, boolean resolve)
    throws ClassNotFoundException {
    Class<?> c= findLoadedClass(name);
    if (c != null)
      return c;
    //
    // Delegate the loading of excluded classes to the
    // standard class loader.
    //
    if (isExcluded(name)) {
      try {
        c= findSystemClass(name);
        return c;
      } catch (ClassNotFoundException e) {
        // keep searching
      }
    }
    
    try{
      if (c == null) {
        byte[] data= lookupClassData(name);
        if (data == null) {
          throw new ClassNotFoundException();
        }
        c= defineClass(name, data, 0, data.length);
      }
      if (resolve) 
        resolveClass(c);
    }catch(ClassNotFoundException e) {
      // we couldn't load it, try the system
      return findSystemClass(name);
    }
    return c;
  }
  
  /** Reads in and returns data from class file for the given classname. */
  private byte[] lookupClassData(String className) throws ClassNotFoundException {
    byte[] data= null;
    for (int i= 0; i < fPathItems.size(); i++) {
      String path= fPathItems.elementAt(i);
      String fileName= className.replace('.', '/')+".class";
      if (isJar(path)) {
        data= loadJarData(path, fileName);
      } else {
        data= loadFileData(path, fileName);
      }
      if (data != null) {
        return data;
      }
    }
    throw new ClassNotFoundException(className);
  }
  
  /**
   * returns true if the pathEntry points to a jar file
   */
  private boolean isJar(String pathEntry) {
    return pathEntry.endsWith(".jar") || pathEntry.endsWith(".zip");
  }
  
  /**
   * reads in data from a class file and returns it
   */
  private byte[] loadFileData(String path, String fileName) {
    File file= new File(path, fileName);
    if (file.exists()) { 
      return getClassData(file);
    }
    return null;
  }
  
  /**
   * returns the contents of the file as an array of bytes
   */
  private byte[] getClassData(File f) {
    try {
      FileInputStream stream= new FileInputStream(f);
      ByteArrayOutputStream out= new ByteArrayOutputStream(1000);
      byte[] b= new byte[1000];
      int n;
      while ((n= stream.read(b)) != -1) 
        out.write(b, 0, n);
      stream.close();
      out.close();
      return out.toByteArray();
      
    } catch (IOException e) {
    }
    return null;
  }
  
  /**
   * searches the contents of the jar for the specified fileName
   * and returns an array of bytes
   */
  private byte[] loadJarData(String path, String fileName) {
    ZipFile zipFile= null;
    InputStream stream= null;
    File archive= new File(path);
    if (!archive.exists())
      return null;
    try {
      zipFile= new ZipFile(archive);
    } catch(IOException io) {
      return null;
    }
    ZipEntry entry= zipFile.getEntry(fileName);
    if (entry == null)
      return null;
    int size= (int) entry.getSize();
    try {
      stream= zipFile.getInputStream(entry);
      byte[] data= new byte[size];
      int pos= 0;
      while (pos < size) {
        int n= stream.read(data, pos, data.length - pos);
        pos += n;
      }
      zipFile.close();
      return data;
    } catch (IOException e) {
    } finally {
      try {
        if (stream != null)
          stream.close();
      } catch (IOException e) {
      }
    }
    return null;
  }
  
  /**
   * reads in a list of excluded packages from a config file
   */
  private void readExcludedPackages() {  
    fExcluded = new Vector<String>(10);
    for (String de: defaultExclusions) fExcluded.addElement(de);
    
    InputStream is = getClass().getResourceAsStream(EXCLUDED_FILE);
    if (is == null) return;
    Properties p = new Properties();
    try { p.load(is); }
    catch (IOException e) { return; } 
    finally {
      try { is.close(); } 
      catch (IOException e) {
        /* do nothing */
      }
    }
    
    // Raw type because Properties is not generified.
    Enumeration pnames = p.propertyNames();
    
    while (pnames.hasMoreElements()) {
      String key = (String) pnames.nextElement();
      if (key.startsWith("excluded.")) {
        String path = p.getProperty(key);
        path = path.trim();
        if (path.endsWith("*")) path= path.substring(0, path.length()-1);
        if (path.length() > 0) fExcluded.addElement(path);    
      }
    }
  }
  
  /** Allows more control over the URLClassLoader. specifically,
   *  allows us to view what's loaded when
   */
  private static class DrJavaURLClassLoader extends URLClassLoader{
    
    public DrJavaURLClassLoader(URL[] urls, ClassLoader c) { super(urls, c); }
    
    public URL getResource(String name) {
      URL ret = getParent().getResource(name);
      if (ret == null) ret = super.getResource(name);
      return ret;
    }
  }
}