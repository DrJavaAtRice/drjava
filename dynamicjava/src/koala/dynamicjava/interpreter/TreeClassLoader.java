/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.interpreter;

import java.io.*;
import java.net.*;
import java.util.*;

import koala.dynamicjava.parser.wrapper.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.util.*;

import java.security.CodeSource;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * This class is responsible for loading bytecode classes
 *
 *
 * <p>All classes <em>created</em> by <code>TreeClassLoader</code>s
 * have an identical
 * {@link java.security CodeSource}. This code source has no certificates
 * but may have a location. The latter is set to the value of the system
 * property with the value of {@link #CODE_SOURCE_URL_PROPERTY} as key, if
 * it is a valid URL. If the property is defined but its value is not a valid
 * URL, the location of the code source is set to <code>null</code>. If the
 * property is not defined, the value of {@link #DEFAULT_CODE_SOURCE_URL} is
 * used as location.</p>
 * @author  Stephane Hillion
 * @author <a href="mailto:hkrug@rationalizer.com">Holger Krug</a>
 * @version 1.1 - 1999/05/18
 */

public class TreeClassLoader extends SecureClassLoader
  implements ClassLoaderContainer {

  /**
   * The default value for the {@link java.security.CodeSource} URL.
   * May be overriden by setting the system property with name given by
   * the value of {@link #CODE_SOURCE_URL_PROPERTY}.
   */
  public static String DEFAULT_CODE_SOURCE_URL =
    "http://koala.ilog.fr/djava/javadoc/koala/dynamicjava/interpreter/TreeClassLoader.html";

  /**
   * Name of the system property to define the value for the URL
   * of the {@link java.security.CodeSource} assigned to classes
   * created by this classloader. The default value is the value of
   * {@link #DEFAULT_CODE_SOURCE_URL}.
   *
   * <p>If the property value is not a wellformed URL, the URL is set
   * to <code>null</code>.
   */
  public static String CODE_SOURCE_URL_PROPERTY =
    "koala.dynamicjava.interpreter.TreeClassLoader.codesource.url";

  /**
   * The code source for classes defined by instances of
   * <code>TreeClassLoader</code>. Initializes when <code>TreeClassLoader</code>
   * is loaded the first time.
   *
   * @see #DEFAULT_CODE_SOURCE_URL
   * @see #CODE_SOURCE_URL_PROPERTY
   */
  protected static CodeSource codeSource;

  /**
   * Initializes the code source.
   */
  static {
    try {
      String url = System.getProperty(CODE_SOURCE_URL_PROPERTY);
      // if ( url != null ) codeSource = new CodeSource(new URL(url), null);
      if ( url != null ) codeSource = new CodeSource(new URL(url), (Certificate[]) null);
    } catch (java.net.MalformedURLException mfue) {
      // property value malformed, return null
      // [XXX]: print error message
      codeSource = new CodeSource(null, (Certificate[]) null);
    }
    try {
      codeSource = new CodeSource(new URL(DEFAULT_CODE_SOURCE_URL), (Certificate[]) null);
    } catch (java.net.MalformedURLException mfue) {
      // should never appear
      throw new RuntimeException(mfue.getMessage());
    }
  }

  /**
   * The place where the interpreted classes are stored
   */
  protected Map<String,Class> classes = new HashMap<String,Class>(11);

  /**
   * The syntax trees
   */
  protected Map<String,Node> trees = new HashMap<String,Node>(11);

  /**
   * The interpreter
   */
  protected Interpreter interpreter;

  /**
   * The auxiliary class loader
   */
  protected ClassLoader classLoader;

  /**
   * Creates a new class loader
   * @param i the object used to interpret the classes
   */
  public TreeClassLoader(Interpreter i) {
    this(i, null);
  }

  /**
   * Creates a new class loader
   * @param i the object used to interpret the classes
   * @param cl the auxiliary class loader used to load external classes
   */
  public TreeClassLoader(Interpreter i, ClassLoader cl) {
    super(i.getClass().getClassLoader());
    interpreter   = i;
    classLoader   = cl;
  }

  /**
   * Converts an array of bytes into an instance of class Class and
   * links this class.
   *
   * @exception ClassFormatError if the class could not be defined
   */
  public Class defineClass(String name, byte[] code)  {
    Class c = defineClass(name, code, 0, code.length, codeSource);
    classes.put(name, c);
    trees.remove(name);
    return c;
  }

  /**
   * Returns the additional class loader that is used for loading
   * classes from the net.
   * @return null if there is no additional class loader
   */
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  /**
   * Whether a class was defined by this class loader
   */
  public boolean hasDefined(String name) {
    return classes.containsKey(name);
  }

  /**
   * Returns the names of the defined classes in a set
   */
  public Set getClassNames() {
    return classes.keySet();
  }

  /**
   * Adds a class syntax tree to the list of the loaded trees
   * @param name the name of the type
   * @param node the tree
   */
  public void addTree(String name, TypeDeclaration node) {
    trees.put(name, node);
  }

  /**
   * Gets a tree
   */
  public TypeDeclaration getTree(String name) {
    return (TypeDeclaration)trees.get(name);
  }

  /**
   * Adds an URL in the class path
   */
  public void addURL(URL url) {
    //if (classLoader == null) {
    //  classLoader = new URLClassLoader(new URL[] { url });
    //} else {
      classLoader = new URLClassLoader(new URL[] { url }, classLoader);
    //}
  }

  /**
   * Finds the specified class.
   *
   * @param  name the name of the class
   * @return the resulting <code>Class</code> object
   * @exception ClassNotFoundException if the class could not be find
   */
  protected Class findClass(String name) throws ClassNotFoundException {
    if (classes.containsKey(name)) {
      return classes.get(name);
    }

    try {
      if (classLoader != null) {
        return Class.forName(name, true, classLoader);
      }
    } catch (ClassNotFoundException e) {
    }

    return interpreter.loadClass(name);
  }
}
