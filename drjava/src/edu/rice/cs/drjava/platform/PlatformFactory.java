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

package edu.rice.cs.drjava.platform;

/**
 * Factory class for accessing the appropriate platform-specific implementation
 * of the PlatformSupport interface.
 * @version $Id$
 * @see PlatformSupport
 */
public class PlatformFactory {
  
  /**
   * A platform-appropriate implementation of the PlatformSupport interface
   * Singleton field populated by the static factory method.
   */
  public static final PlatformSupport ONLY = getPlatformSupport();
  
  /**
   * Static factory method.
   * @return a platform-appropriate implementation of PlatformSupport
   */
  private static PlatformSupport getPlatformSupport() {
    // Check for Mac OS X.
    String mrjVer = System.getProperty("mrj.version");
    
    // Check for other OS types.
    String os = System.getProperty("os.name");
    
    if (mrjVer != null) {
      // This must be a Mac, but what JDK version?
      String jdkVer = System.getProperty("java.specification.version");
      
      if (jdkVer.equals("1.4")) {
        // This is a 1.4 compliant JDK.
        // System.out.println("Mac14Platform");
        return Mac14Platform.ONLY;
      }
      else if (jdkVer.equals("1.3")) {
        // This is a 1.3 compliant JDK.
        // System.out.println("Mac13Platform");
        return Mac13Platform.ONLY;
      }
      else {
        // We don't know what version of the JDK this is, so use a default for OS X.
        // System.out.println("MacPlatform");
        return MacPlatform.ONLY;
      }
    }
    // Check for Windows platform.
    else if ((os != null) && (os.toLowerCase().indexOf("windows") == 0)) {
      // This must be a Windows OS.
      // System.out.println("WindowsPlatform");
      return WindowsPlatform.ONLY;
    }
    else {
      // This isn't one of our specifically-supported platforms, so use the default.
      // System.out.println("DefaultPlatform");
      return DefaultPlatform.ONLY;
    }
  }
}