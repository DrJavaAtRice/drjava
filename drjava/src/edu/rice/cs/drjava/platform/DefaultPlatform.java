/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.platform;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.Configuration;
import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.util.StringOps;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

/**
 * Default platform-neutral implementation of PlatformSupport.  Most implementations
 * will extend this class to inherit default behaviors.
 *
 * @version $Id$
 */
class DefaultPlatform implements PlatformSupport {
  /** Singleton instance. */
  public static DefaultPlatform ONLY = new DefaultPlatform();

  /** Private constructor for singleton pattern. */
  protected DefaultPlatform() { }

  /** Utility method to determine if the current Swing look and feel is the
   *  platform-specific look and feel for the client platform.
   *
   *  @return true if current Swing look and feel is the system look and feel
   */
  public boolean isUsingSystemLAF() {
    String sysLAF = UIManager.getSystemLookAndFeelClassName();
    String curLAF = UIManager.getLookAndFeel().getClass().getName();
    return (sysLAF.equals(curLAF));
  }

  /**
   * Hook for performing general UI setup.  Called before all other UI setup is done.
   * The default implementation does nothing.
   */
  public void beforeUISetup() { }

  /**
   * Hook for performing general UI setup.  Called after all other UI setup is done.
   * The default implementation does nothing.
   *
   * @param about the Action associated with openning the About dialog
   * @param prefs the Action associated with openning the Preferences dialog
   * @param quit  the Action associated with quitting the DrJava application
   */
  public void afterUISetup(Action about, Action prefs, Action quit) { }

  /** Returns whether this is a Mac OS X platform. */
  public boolean isMacPlatform() { return false; }

  /** Returns whether this is a Windows platform. */
  public boolean isWindowsPlatform() { return false; }

  /** Returns the current Java specification version. */
  public String getJavaSpecVersion() {
    return System.getProperty("java.specification.version");
  }

  /** Returns true if the classpath's tools.jar is from version 1.3. */
  public boolean has13ToolsJar() {
    // Javadoc's Main class should not have an execute(String[]) method.
    try {
      Class<?> main = Class.forName("com.sun.tools.javadoc.Main");
      return !_javadocMainHasExecuteMethod(main);
    }
    catch (Throwable t) { return false; }
  }

  /** Returns true if the classpath's tools.jar is from version 1.4. */
  public boolean has14ToolsJar() {
    // Javadoc's Main class should have an execute(String[]) method.
    try {
      Class<?> main = Class.forName("com.sun.tools.javadoc.Main");
      return _javadocMainHasExecuteMethod(main);
    }
    catch (Throwable t) { return false; }
  }

  /** Returns true if the given class object for com.sun.tools.javadoc.Main
   *  has an execute(String[]) method.  If so, that means we have a 1.4
   *  version of tools.jar.
   *
   * @param main Class object for com.sun.tools.javadoc.Main
   */
  private boolean _javadocMainHasExecuteMethod(Class main) {
    try {
      @SuppressWarnings("unchecked") Class<String[]>[] arr = new Class[]{String[].class};
      main.getMethod("execute", arr);
      return true;
    }
    catch (Throwable t) { return false; }
  }

  /** Utility method for opening a URL in a browser in a platform-specific way.
   *  The default implementation uses Runtime.exec to execute a command specified
   *  in Preferences.  Platform implementations should attempt the default method
   *  first, then try to use a "default browser", if such a thing exists on the
   *  specific platform.
   *
   *  @param address the URL to open
   *  @return true if the URL was successfully handled, false otherwise
   */
  public boolean openURL(URL address) {
    // Get the two config options.
    Configuration config = DrJava.getConfig();
    File exe = config.getSetting(OptionConstants.BROWSER_FILE);
    String command = config.getSetting(OptionConstants.BROWSER_STRING);

    // Check for empty settings.
    if ((exe == FileOption.NULL_FILE) && (command.equals(""))) {
      // If the user hasn't specified anything, don't try to run it.
      return false;
    }
    else {
      String addr = address.toString();
      if (command.equals("")) {
        // If there is no command, simply use the URL.
        command = addr;
      }
      else {
        // Otherwise, replace any <URL> tags in the command with the address.
        String tag = "<URL>";
        if (command.indexOf(tag) != -1) {
          command = StringOps.replace(command, tag, addr);
        }
        else {
          // No <URL> specified, so tack it onto the end.
          command = command + " " + addr;
        }
      }

      // Build a string array of command and arguments.
      List<String> args = ArgumentTokenizer.tokenize(command);

      // Prepend the file only if it exists.
      if (exe != FileOption.NULL_FILE) {
        args.add(0, exe.getAbsolutePath());
      }

      // Call the command.
      try {
        // Process proc =
        Runtime.getRuntime().exec(args.toArray(new String[args.size()]));

        // TODO: This may cause a memory leak on Windows, if we don't check the exit code.
      }
      catch (Throwable t) {
        // If there was any kind of problem, ignore it and report failure.
        return false;
      }
    }

    // Otherwise, trust that it worked.
    return true;
  }
}
