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

package koala;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * This interface hold the information about this build of DrJava.
 * This file is copied to Version.java by the build process, which also
 * fills in the right values of the date and time.
 *
 * This javadoc corresponds to build drjava-20040601-1531;
 *
 * @version $Id$
 */
public abstract class Version {
  /**
   * This string will be automatically expanded upon "ant commit".
   * Do not edit it by hand!
   */
  private static final String BUILD_TIME_STRING = "20040601-1531";

  /** A {@link Date} version of the build time. */
  private static final Date BUILD_TIME = _getBuildDate();

  public static String getBuildTimeString() {
    return BUILD_TIME_STRING;
  }

  public static Date getBuildTime() {
    return BUILD_TIME;
  }

  private static Date _getBuildDate() {
    try {
      return new SimpleDateFormat("yyyyMMdd-HHmm z").parse(BUILD_TIME_STRING + " GMT");
    }
    catch (Exception e) { // parse format or whatever problem
      return null;
    }
  }

  public static void main(String[] args) {
    System.out.println("Version for edu.rice.cs.drjava: " + BUILD_TIME_STRING);
  }
} 
