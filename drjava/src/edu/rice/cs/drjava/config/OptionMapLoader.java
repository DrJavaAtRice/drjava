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

package edu.rice.cs.drjava.config;
import java.util.Properties; // don't import all of java.util, or gj.util to prevent name collisions
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
        Option option = null;
        try {
          Object o = field.get(null); // we should be able to pass in null as the 'receiver', since it's static.
          //System.out.println("field name: "+field.getName()+"  o: "+o);
          if (o == null) continue; // Development options can be null in the stable version of the code
          if(!( o instanceof Option)) continue;
          
          option = (Option) o;
        } catch(IllegalAccessException e) {
          // this cannot happen, since we don't get in here unless the field is public.
          throw new RuntimeException("IllegalAccessException happened on a public field.");
        }
        
        String sval = option.getDefaultString();
        DEFAULT_STRINGS.setProperty(option.name,sval);
        DEFAULTS.setString(option,sval);
      }
    }
  }
  
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
  
   /**
   * creates an OptionMap from an InputStream.
   * @param is the inputstream to read from to load these options.
   */
  public void loadInto(OptionMap map) {
    Iterator<OptionParser> options = DEFAULTS.keys();
    while(options.hasNext()) {
      OptionParser option = options.next();
      String val = prop.getProperty(option.name);
      map.setString(option,val);
    }
  }
}
