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

import javax.swing.Action;
import java.net.URL;

/**
 * Central interface for all platform-specific code in DrJava.
 * A default platform-neutral implementation is provided in DefaultPlatform.
 * @version $Id$
 * @see edu.rice.cs.drjava.platform.DefaultPlatform
 */
public interface PlatformSupport {
  
  /**
   * Returns whether this is a Mac platform (any JDK version).
   */
  public boolean isMacPlatform();
  
  /**
   * Returns whether this is a Mac platform with JDK 1.3.1.
   */
  public boolean isMac13Platform();
  
  /**
   * Returns whether this is a Mac platform with JDK 1.4.1.
   */
  public boolean isMac14Platform();
  
  /**
   * Returns whether this is a Windows platform.
   */
  public boolean isWindowsPlatform();
  
  /**
   * Returns the current Java specification version.
   */
  public String getJavaSpecVersion();
  
  /**
   * Returns true if the classpath's tools.jar is from version 1.3.
   */
  public boolean has13ToolsJar();
  
  /**
   * Returns true if the classpath's tools.jar is from version 1.4.
   */
  public boolean has14ToolsJar();
  
  /**
   * Utility method to determine if the current Swing look and feel is the
   * platform-specific look and feel for the client platform.
   * @return true if current Swing look and feel is the system look and feel
   */
  public boolean isUsingSystemLAF();
  
  /**
   * Hook for performing general UI setup.  Called before all other UI setup is done.
   */
  public void beforeUISetup();
  
  /**
   * Hook for performing general UI setup.  Called after all other UI setup is done.
   * @param about the Action associated with openning the About dialog
   * @param prefs the Action associated with openning the Preferences dialog
   * @param quit the Action associated with quitting the DrJava application
   */
  public void afterUISetup(Action about, Action prefs, Action quit);
  
  /**
   * Utility method for opening a URL in a browser in a platform-specific way.
   * The default implementation uses Runtime.exec to execute a command specified
   * in Preferences.  Platform implementations should attempt the default method
   * first, then try to use a "default browser", if such a thing exists on the
   * specific platform.
   * @param address the URL to open
   * @return true if the URL was successfully handled, false otherwise
   */
  public boolean openURL(URL address);
}
