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
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.FileOption;

/**
 * A compiler interface to find Javac (1.5.0) from the location
 * specified in Configuration.
 *
 * @version $Id$
 */
public class Javac150FromSetLocation extends CompilerProxy implements OptionConstants {
  // To implement #523222, we had to make this not a singleton,
  // to allow it to re-determine the location of the compiler multiple times.
//  public static final CompilerInterface ONLY = new JavacFromSetLocation();
 
  /** Private constructor due to singleton. */
  public Javac150FromSetLocation() {
    super("edu.rice.cs.drjava.model.compiler.Javac150Compiler", _getClassLoader());
  }

  private static ClassLoader _getClassLoader() {
    File loc = DrJava.getConfig().getSetting(JAVAC_LOCATION);
    if (loc == FileOption.NULL_FILE) throw new RuntimeException("javac location not set");

    try {
      //URL url = new File(loc).toURL();
      URL url = loc.toURL();
      return new URLClassLoader(new URL[] { url });
    }
    catch (MalformedURLException e) { throw new RuntimeException("malformed url exception"); }
  }

  public boolean isAvailable() { return VERSION.equals("1.5") && super.isAvailable(); }
  public String getName() { return "javac 1.5.0"; }
}