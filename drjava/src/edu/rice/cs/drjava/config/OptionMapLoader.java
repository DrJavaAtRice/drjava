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

package edu.rice.cs.drjava.config;
import edu.rice.cs.util.UnexpectedException;

import java.util.Properties;
import java.util.Iterator;
import java.io.*;
import java.lang.reflect.*;
public class OptionMapLoader implements OptionConstants {

  /** bag of default options (programmatically defined, instead of in an options file) */
  private static DefaultOptionMap DEFAULTS = new DefaultOptionMap();
  private static Properties DEFAULT_STRINGS = new Properties();

  static {
    // initialize DEFAULTS objects, based on OptionConstants using reflection.
    Field[] fields = OptionConstants.class.getDeclaredFields();
    for(int i = 0; i < fields.length; i++) {
      Field field = fields[i];
      int mods = field.getModifiers();
      if(Modifier.isStatic(mods) && Modifier.isPublic(mods) && Modifier.isFinal(mods)) {
        // field is public static and final.
        Option option;
        try {
          Object o = field.get(null); // we should be able to pass in null as the 'receiver', since it's static.
          //System.out.println("field name: "+field.getName()+"  o: "+o);
          if (o == null || !(o instanceof Option)) {
            continue; // Development options can be null in the stable version of the code
          }

          option = (Option) o;
        }
        catch(IllegalAccessException e) {
          // this cannot happen, since we don't get in here unless the field is public.
          throw new UnexpectedException(e);
        }

        String sval = option.getDefaultString();
        DEFAULT_STRINGS.setProperty(option.name,sval);
        DEFAULTS.setString(option,sval);
      }
    }
  }

  /** Default OptionMapLoader. */
  public static final OptionMapLoader DEFAULT = new OptionMapLoader(DEFAULT_STRINGS);

  /**
   * creates an OptionMapLoader from a given input stream.
   * does not maintain a reference to this input stream after
   * Constructor creates
   * @param is the input stream to read.
   */
  public OptionMapLoader(InputStream is) throws IOException {
    this(new Properties(DEFAULT_STRINGS));
    try {
      prop.load(is);
    }
    finally {
      is.close();
    }
  }

  private final Properties prop;

  private OptionMapLoader(Properties prop) {
    this.prop = prop;
  }

  public void loadInto(OptionMap map) {
    Iterator<OptionParser> options = DEFAULTS.keys();
    while(options.hasNext()) {
      OptionParser option = options.next();
      String val = prop.getProperty(option.name);
      map.setString(option,val);
    }
  }
}
