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

package edu.rice.cs.drjava.model;

import java.io.*;
import java.util.*;

/**
 * A temporary class to store config data.
 *
 * This wil change in a future release!
 *
 * @version $Id$
 */
public final class Configuration {
  public static final String EXTRA_CLASSPATH_KEY = "extra.classpath";
  public static final String JAVAC_LOCATION_KEY = "javac.location";
  public static final String JSR14_LOCATION_KEY = "jsr14.location";
  public static final String JSR14_COLLECTIONS_KEY = "jsr14.collectionspath";
  public static final File PROPERTIES_FILE
    = new File(System.getProperty("user.home"), ".drjava");

  public static final String PATH_SEPARATOR = System.getProperty("path.separator");

  public static final Configuration ONLY = new Configuration();

  private final Properties _properties;

  private Configuration() {
    _properties = new Properties();

    try {
      _properties.load(new FileInputStream(PROPERTIES_FILE));
    }
    catch (IOException ioe) {
      // oh well, didn't find any properties.
    }
  }

  /**
   * Returns the setting for the javac classpath, or null if none was
   * specified.
   */
  public String getJavacLocation() {
    return (String) _properties.get(JAVAC_LOCATION_KEY);
  }

  /**
   * Returns the setting for the jsr14 classpath, or null if none was
   * specified.
   */
  public String getJSR14Location() {
    return (String) _properties.get(JSR14_LOCATION_KEY);
  }

  /**
   * Returns the setting for the jsr14 collections classses classpath,
   * or null if none was specified.
   */
  public String getJSR14CollectionsPath() {
    return (String) _properties.get(JSR14_COLLECTIONS_KEY);
  }

  /**
   * Gets additional items to add to the classpath for both
   * compilation and interpretation.
   *
   * The classpath property must use the platform's path separator.
   *
   * @return An array of items to add to the classpaths.
   */
  public String[] getExtraClasspath() {
    String path = (String) _properties.get(EXTRA_CLASSPATH_KEY);
    if (path == null) {
      return new String[0];
    }
    else {
      StringTokenizer tokenizer = new StringTokenizer(path, PATH_SEPARATOR);
      String[] ret = new String[tokenizer.countTokens()];
      for (int i = 0; i < ret.length; i++) {
        ret[i] = tokenizer.nextToken();
      }

      return ret;
    }
  }
}
