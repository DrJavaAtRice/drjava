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

package edu.rice.cs.drjava.platform;

/**
 * Factory class for accessing the appropriate platform-specific implementation
 * of the PlatformSupport interface.
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