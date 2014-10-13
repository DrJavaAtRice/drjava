/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;

import edu.rice.cs.drjava.DrJava;

import java.util.Vector;

/** Class representing values from the DrJava configuration file that can be inserted as variables in external processes.
  * @version $Id: ConfigProperty.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class ConfigProperty extends EagerProperty {
  /** True if this is a list of values. This allows the sep="..." attribute. */
  protected boolean _isList = false;
  
  /** Create a configuration property. */
  public ConfigProperty(String name) {
    super(name, "Help not available.");
    resetAttributes();
  }

  /** Update the property so the value is current.
    * @param pm PropertyMaps used for substitution when replacing variables */
  public void update(PropertyMaps pm) {
    OptionMap om = DrJava.getConfig().getOptionMap();
    for (OptionParser<?> op : om.keys()) {
      String key = op.getName();
      String value = om.getString(op);
      if (_name.equals("config." + key)) {
        if (op instanceof VectorOption<?>) {
          @SuppressWarnings("unchecked")
          Vector<?> vec = ((VectorOption)op).parse(value);
          StringBuilder sb = new StringBuilder();
          for(Object o: vec) {
            sb.append(_attributes.get("sep"));
            sb.append(o.toString());
          }
          _value = sb.toString();
          if (_value.startsWith(_attributes.get("sep"))) {
            _value= _value.substring(_attributes.get("sep").length());
          }
        }
        else if (_name.equals("config.debug.step.exclude")) {
          java.util.StringTokenizer tok = new java.util.StringTokenizer(value);
          StringBuilder sb = new StringBuilder();
          while(tok.hasMoreTokens()) {
            sb.append(_attributes.get("sep"));
            sb.append(tok.nextToken());
          }
          _value = sb.toString();
          if (_value.startsWith(_attributes.get("sep"))) {
            _value= _value.substring(_attributes.get("sep").length());
          }
        }
        else {
          _value = value;
        }
        return;
      }
    }
    _value = "--unknown--";
  }

  /** Reset attributes to their defaults. */
  public void resetAttributes() {
    _attributes.clear();
    OptionMap om = DrJava.getConfig().getOptionMap();
    for (OptionParser<?> op : om.keys()) {
      String key = op.getName();
      if (_name.equals("config." + key)) {
        if (op instanceof VectorOption<?>) {
          _isList = true;
          _attributes.put("sep", java.io.File.pathSeparator);
        }
        else if (_name.equals("config.debug.step.exclude")) {
          _isList = true;
          _attributes.put("sep", ",");
        }
        else _isList = false;
        return;
      }
    }
  }

  /** Return the value. */
  public String toString() { return _value; }
} 
