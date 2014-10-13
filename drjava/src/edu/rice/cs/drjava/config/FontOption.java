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

import java.awt.Font;

/**
 * Class defining all configuration entries of type Font
 * @version $Id: FontOption.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class FontOption extends Option<Font> {

  public FontOption(String key, Font def) { super(key,def); }

  /* Changed on 5/19/2004 to reflect a change in the API specifications of decode in the most recent release of
   * Java 1.5.0 beta. Decode no longer likes "PLAIN", assuming it to be default and returning the wrong font (dialog)
   * if the word is present  This may be fixed in future versions of 1.5.0, but the use of the word PLAIN appears to
   * have been deprecated since 1.3 */
  public Font parse(String s) {
    String newS = s;// s.replaceAll("PLAIN-","")
    int idx = newS.indexOf("PLAIN-");
    while (idx != -1) {
      newS = newS.substring(0, idx) + newS.substring(idx + 6);
      idx = newS.indexOf("PLAIN-");
    }
    return Font.decode(newS);  //Font.decode(s);
  }

  /** Create a String representation of the Font object, in the format: fontname-fontstyle-fontsize. */
  public String format(Font f) {
    final StringBuilder str = new StringBuilder(f.getName());
    str.append("-");
    if (f.isBold()) {
      str.append("BOLD");
    }
    if (f.isItalic()) {
      str.append("ITALIC");
    }
//    if (f.isPlain()) {
//      str.append("PLAIN");
//    }
    if (! f.isPlain()) {
      str.append("-");
    }
    str.append(f.getSize());

    return str.toString();
  }
}