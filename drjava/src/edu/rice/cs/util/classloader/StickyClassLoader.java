/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.classloader;

import java.util.Arrays;
import java.net.URL;
import java.io.IOException;

import edu.rice.cs.util.FileOps;

/**
 * A {@link ClassLoader} that works as the union of two classloaders, but
 * always tries to delegate to the first of these.
 *
 * The purpose for this class is to ensure that classes loaded transitively
 * due to some class's loading are also loaded with the right classloader.
 * Here's the problem: Say that class A contains a reference to class B,
 * but the specific B is unknown to clients of class A.
 * Class A is loadable by the standard classloader, but class B needs
 * to be loaded with a (known) custom classloader.
 * <P>
 *
 * If A were loaded using
 * the standard classloader, it would fail because this would cause the
 * transitive loading of B to be done by the system loader as well.
 * If A were loaded with the custom loader, the same thing would happen --
 * the custom loader would delegate to the system loader to load A (since
 * it doesn't load non-custom-loader-requiring classes), but this would
 * associate class A with the system classloader. (Every class is associated
 * with the loader that called {@link ClassLoader#defineClass} to define it
 * in the JVM.) This association would make B be loaded by the standard loader!
 * <P>
 *
 * To get around this problem, we use this class, which acts mostly
 * as a union of two classloaders. The trick, however, is that the
 * StickyClassLoader has itself associated with all classes it loads, even
 * though the actual work is done by the two loaders it delegate to.
 * (It does this by calling {@link ClassLoader#findResource} on the
 * subordinate loaders to get the class data, but then by calling
 * {@link ClassLoader#defineClass} itself to preserve the association.
 *
 * @version $Id$
 */
public class StickyClassLoader extends ClassLoader {
  private final ClassLoader _newLoader;
  private final String[] _classesToLoadWithOld;

  /**
   * Creates a sticky class loader with the given primary and secondary
   * loaders to join together.
   *
   * All classes will be attempted to be loaded with the primary loader,
   * and the secondary will be used as a fallback.
   *
   * @param newLoader Primary loader
   * @param oldLoader Secondary loader
   */
  public StickyClassLoader(final ClassLoader newLoader,
                           final ClassLoader oldLoader)
  {
    this(newLoader, oldLoader, new String[0]);
  }

  /**
   * Creates a sticky class loader with the given primary and secondary
   * loaders to join together.
   *
   * All classes will be attempted to be loaded with the primary loader
   * (except for classes in <code>classesToLoadWithOld</code>),
   * and the secondary will be used as a fallback.
   *
   * @param newLoader Primary loader
   * @param oldLoader Secondary loader
   * @param classesToLoadWithOld All class names in this array will be loaded
   *                             only with the secondary classloader. This is
   *                             vital to ensure that only one copy of some
   *                             classes are loaded, since two differently
   *                             loaded versions of a class act totally
   *                             independantly! (That is, they have different,
   *                             incompatible types.) Often it'll be necessary
   *                             to make key interfaces that are used between
   *                             components get loaded via one standard 
   *                             classloader, to ensure that things can be
   *                             cast to that interface.
   */
  public StickyClassLoader(final ClassLoader newLoader,
                           final ClassLoader oldLoader,
                           final String[] classesToLoadWithOld)
  {
    super(oldLoader);
    _newLoader = newLoader;
    _classesToLoadWithOld = new String[classesToLoadWithOld.length];
    System.arraycopy(classesToLoadWithOld, 0, _classesToLoadWithOld, 0,
                     classesToLoadWithOld.length);
    Arrays.sort(_classesToLoadWithOld);
  }

  /**
   * Gets the requested resource, looking first in the new loader
   * and then in the old loader.
   *
   * @param name Name of resource to find
   * @return URL of the resource if found/accessible, or null if not.
   */
  public URL getResource(String name) {
    URL resource = _newLoader.getResource(name);

    if (resource == null) {
      resource = getParent().getResource(name);
    }

    //System.err.println("resource: " + name + " --> " + resource);

    return resource;
  }

  /**
   * Loads the given class, delegating first to the new class loader and
   * then second to the old class loader. The returned Class object will have
   * its ClassLoader ({@link Class#getClassLoader}) set to be this. This is very
   * important because it causes classes that are loaded due to this class 
   * being loaded (ancestor classes/interfaces, referenced classes, etc) to
   * use the same loader.
   *
   * There are a few exceptions to this explanation:
   * <OL>
   * <LI>If the class is in java.* or javax.*, it will be loaded using
   *     {@link ClassLoader#getSystemClassLoader}. This is because only
   *     the system loader is allowed to load system classes!
   *     Also: sun.*.
   * </LI>
   * <LI>If the class name is in the list of classes to load with the
   *     old class loader (passed to constructor), the new loader is not
   *     considered when trying to load the class.
   *     This is useful to make sure that certain classes (or interfaces)
   *     only have one copy in the system, to ensure that you can cast to that
   *     class/interface regardless of which loader loaded the other class.
   * </LI>
   * </OL>
   */
  protected Class loadClass(String name, boolean resolve) 
    throws ClassNotFoundException
  {
    // check if it's already loaded!
    Class clazz;
    clazz = findLoadedClass(name);
    if (clazz != null) {
      return clazz;
    }
    
    if (name.startsWith("java.") ||
        name.startsWith("javax.") ||
        name.startsWith("sun."))
    {
      try {
        clazz = getSystemClassLoader().loadClass(name);
      }
      catch (ClassNotFoundException e) {
        // It might be a non-system class, like javax.mail.*.
        //  Fall back on the secondary loader.
        clazz = _loadWithSecondary(name);
      }
    }
    else if (Arrays.binarySearch(_classesToLoadWithOld, name) >= 0) {
      // Don't fall back to secondary if this fails...
      clazz = getParent().loadClass(name);
    }
    else {
      // Load with the secondary loader
      clazz = _loadWithSecondary(name);
    }

    if (resolve) {
      resolveClass(clazz);
    }

    /*
    System.out.println("Sticky loaded OK: " + name + " " + clazz + " loader=" +
                       clazz.getClassLoader());
    */
    return clazz;
  }
  
  /**
   * Try to load the class with the given name with the secondary (new)
   * loader.  Uses getResource to find the class.
   * @param name Name of the class to load.
   */
  protected Class _loadWithSecondary(String name) 
    throws ClassNotFoundException
  {
    // we get the data using getResource because if we just delegate
    // the call to loadClass on old or new loader, it will use that
    // loader as the associated class loader for the class. that's bad.
    try {
      String fileName = name.replace('.', '/') + ".class";
      
      URL resource = getResource(fileName);
      if (resource == null) {
        throw new ClassNotFoundException("Resource not found: " + fileName);
      }
      
      byte[] data = FileOps.readStreamAsBytes(resource.openStream());
      try {
        return defineClass(name, data, 0, data.length);
      }
      catch (Error t) {
        /*
         System.err.println("Sticky " + this + " error when loading " + name +
         " with resolve=" + resolve + ":");
         */
        //t.printStackTrace();
        throw t;
      }
    }
    catch (IOException ioe) {
      throw new ClassNotFoundException(ioe.toString());
    } 
  }
}
