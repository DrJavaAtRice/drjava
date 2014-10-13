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

/** Exception indicating that an OptionParser could not parse the specified value for a given configurable option.
  * @version $Id: OptionParseException.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class OptionParseException extends IllegalArgumentException {
  
  public String key;
  public String value;
  public String message;
  public OptionParseException[] causes; 
  
  /** Exception indicating that an OptionParser could not parse the specified value for a given configurable option.
    * @param key The name of the configuration option
    * @param value The invalid value which caused the parse error
    * @param message Some helpful message explaining the parse error
    */
  public OptionParseException(String key, String value, String message) {
    this.key = key;
    this.value = value;
    this.message = message;
    this.causes = null;
  }
  
  public OptionParseException(OptionParseException[] causes) {
    this.key = this.value = this.message = null;
    this.causes = causes;
  }
  
  /** Format a nice message for the user. */
  public String toString() {
    OptionParseException ope = this;
    if (causes != null) {
      if (causes.length!=1) return "Could not parse configuration options.";
      ope = causes[0];
    }
    final StringBuilder sb = new StringBuilder();
    sb.append("Could not parse configuration option.\nOption: ");
    sb.append(ope.key);
    sb.append("\nGiven value: \"");
    sb.append(ope.value);
    sb.append("\"\n");
    sb.append(ope.message);
    return sb.toString();
  }
}