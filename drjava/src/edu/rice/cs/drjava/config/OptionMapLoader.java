/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;
import edu.rice.cs.util.UnexpectedException;

import java.util.Properties;
import java.io.*;
import java.lang.reflect.*;
public class OptionMapLoader implements OptionConstants {
  
  /** bag of default options (programmatically defined, instead of in an options file) */
  private static final DefaultOptionMap DEFAULTS = new DefaultOptionMap();
  private static final Properties DEFAULT_STRINGS = new Properties();
  private static volatile Field[] fields = OptionConstants.class.getDeclaredFields();
  
  static {
    // initialize DEFAULTS objects, based on OptionConstants using reflection.
    for (int i = 0; i < fields.length; i++) {
      Field field = fields[i];
      int mods = field.getModifiers();
      if (Modifier.isStatic(mods) && Modifier.isPublic(mods) && Modifier.isFinal(mods)) {
        // field is public static and final.
        Option<?> option;
        try {
          Object o = field.get(null); // we should be able to pass in null as the 'receiver', since it's static.
          //System.out.println("field name: " + field.getName() + "  o: " + o);
          if (o == null || !(o instanceof Option<?>)) {
            continue; // Development options can be null in the stable version of the code
          }
          
          option = (Option<?>) o;
        }
        catch(IllegalAccessException e) {
          // this cannot happen, since we don't get in here unless the field is public.
          throw new UnexpectedException(e);
        }
        
        String sval = option.getDefaultString();
        DEFAULT_STRINGS.setProperty(option.name, sval);
        DEFAULTS.setString(option, sval);
      }
    }
  }
  
  /** Default OptionMapLoader. */
  public static final OptionMapLoader DEFAULT = new OptionMapLoader(DEFAULT_STRINGS);
  
  /** Creates an OptionMapLoader from a given input stream.  Does not maintain a reference to this input stream.
    * @param is the input stream to read.
    */
  public OptionMapLoader(InputStream is) throws IOException {
    this(new Properties(DEFAULT_STRINGS));
    try { prop.load(is); }
    finally { is.close(); }
  }
  
  private final Properties prop;
  
  private OptionMapLoader(Properties prop) {
    this.prop = prop;
  }
  
  public void loadInto(OptionMap map) {
    java.util.ArrayList<OptionParseException> es = new java.util.ArrayList<OptionParseException>();
    for (OptionParser<?> option : DEFAULTS.keys()) {
      try {
        String val = prop.getProperty(option.name);
        map.setString(option, val);
      }
      catch(OptionParseException ope) {
        es.add(ope);
        map.setString(option, DEFAULT.prop.getProperty(option.name));
      }
    }
    if (es.size() > 0) throw new OptionParseException(es.toArray(new OptionParseException[0]));
  }
}
