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

import java.awt.Font;

/**
 * Class defining all configuration entries of type Font
 * @version $Id$
 */
public class FontOption extends Option<Font> {

  public FontOption(String key, Font def) { super(key,def); }

  //Changed on 5/19/2004 to reflect a change in the API specifications of decode in the most recent release of 
  //java 1.5.0 beta. Decode no longer likes "PLAIN", assuming it to be default and returning the wrong font (dialog)
  //if the word is present /**/ This may be fixed in future versions of 1.5.0, but the use of the word PLAIN appears to
  //have been deprecated since 1.3
  public Font parse(String s) {
    return Font.decode(s.replaceAll("PLAIN-",""));  //Font.decode(s);
  }

  /**
   * Create a String representation of the Font object, in the format:
   *   fontname-fontstyle-fontsize
   */
  public String format(Font f) {
    StringBuffer str = new StringBuffer(f.getName());
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