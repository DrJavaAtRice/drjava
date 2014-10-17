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

package edu.rice.cs.drjava.platform;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.Configuration;
import edu.rice.cs.drjava.config.OptionConstants;

import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.StringOps;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.List;

/** Default platform-neutral implementation of PlatformSupport.  Most implementations
  * will extend this class to inherit default behaviors.
  * @version $Id: DefaultPlatform.java 5711 2012-09-11 19:42:33Z rcartwright $
  */
class DefaultPlatform implements PlatformSupport {
  /** Singleton instance. */
  public static DefaultPlatform ONLY = new DefaultPlatform();

  /** Private constructor for singleton pattern. */
  protected DefaultPlatform() { }

  /** Utility method to determine if the current Swing look and feel is the platform-specific look and feel for the
    * client platform.
    * @return true if current Swing look and feel is the system look and feel
    */
  public boolean isUsingSystemLAF() {
    String sysLAF = UIManager.getSystemLookAndFeelClassName();
    String curLAF = UIManager.getLookAndFeel().getClass().getName();
    return (sysLAF.equals(curLAF));
  }
  
  /** Hook for performing general UI setup.  Called before all other UI setup is done. The default implementation 
    * does nothing.
    */
  public void beforeUISetup() { }

  /** Hook for performing general UI setup.  Called after all other UI setup is done. The default implementation 
    * does nothing.
    *
    * @param about the Action associated with openning the About dialog
    * @param prefs the Action associated with openning the Preferences dialog
    * @param quit  the Action associated with quitting the DrScala application
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

  /** Returns true if the given class object for com.sun.tools.javadoc.Main
   *  has an execute(String[]) method.  If so, that means we have a 1.4
   *  version of tools.jar.
   *
   * @param main Class object for com.sun.tools.javadoc.Main
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private boolean _javadocMainHasExecuteMethod(Class<?> main) {
    try {
      Class<String[]>[] arr = new Class[]{String[].class};
      main.getMethod("execute", arr);
      return true;
    }
    catch (Throwable t) { return false; }
  }

  /** Utility method for opening a URL in a browser in a platform-specific way.
    * The default implementation uses Runtime.exec to execute a command specified
    * in Preferences.  Platform implementations should attempt the default method
    * first, then try to use a "default browser", if such a thing exists on the
    * specific platform.
    *
    * @param address the URL to open
    * @return true if the URL was successfully handled, false otherwise
    */
  public boolean openURL(URL address) {
    // Get the two config options.
    Configuration config = DrJava.getConfig();
    File exe = config.getSetting(OptionConstants.BROWSER_FILE);
    String command = config.getSetting(OptionConstants.BROWSER_STRING);

    // Check for empty settings.
    if ((exe == FileOps.NULL_FILE) && (command.equals(""))) {
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
      if (exe != FileOps.NULL_FILE) args.add(0, exe.getAbsolutePath());

      // Call the command.
      try {
        // Process proc =
        Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
      }
      catch (Throwable t) {
        // If there was any kind of problem, ignore it and report failure.
        return false;
      }
    }

    // Otherwise, trust that it worked.
    return true;
  }
  
  /** Set the keyboard mnemonic for the component in a way that is consistent with
    * the current platform.
    * @param obj the component whose mnemonic should be set
    * @param mnemonic the key code which represents the mnemonic
    * @see javax.swing.AbstractButton#setMnemonic(int)
    * @see java.awt.event.KeyEvent */
  public void setMnemonic(javax.swing.AbstractButton obj, int mnemonic) {
    // by default just set the object's mnemonic
    obj.setMnemonic(mnemonic);
  }

  /** Set the keyboard mnemonic for the component in a way that is consistent with
    * the current platform.
    * @param obj the component whose mnemonic should be set
    * @param mnemonic a char specifying the mnemonic value
    * @see javax.swing.AbstractButton#setMnemonic(char) */
  public void setMnemonic(javax.swing.AbstractButton obj, char mnemonic) {
    // by default just set the object's mnemonic
    obj.setMnemonic(mnemonic);
  }
  
  /** Set the keyboard mnemonic for the component in a way that is consistent with
    * the current platform.
    * @param obj the component whose mnemonic should be set
    * @param mnemonic the key code which represents the mnemonic
    * @see javax.swing.ButtonModel#setMnemonic(int)
    * @see java.awt.event.KeyEvent */
  public void setMnemonic(javax.swing.ButtonModel obj, int mnemonic) {
    // by default just set the object's mnemonic
    obj.setMnemonic(mnemonic);
  }
  
  /** Set the keyboard mnemonic for the component in a way that is consistent with
    * the current platform.
    * @param obj the component whose mnemonic should be set
    * @param tabIndex the index of the tab that the mnemonic refers to
    * @param mnemonic the key code which represents the mnemonic
    * @see javax.swing.JTabbedPane#setMnemonicAt(int,int)
    * @see java.awt.event.KeyEvent */
  public void setMnemonicAt(javax.swing.JTabbedPane obj, int tabIndex, int mnemonic) {
    // by default just set the object's mnemonic
    obj.setMnemonicAt(tabIndex, mnemonic);
  }
  
  /** @return true if file extensions can be registered and unregistered. */
  public boolean canRegisterFileExtensions() { return false; }
  
  /** Register .drscala and .dsapp file extensions.
    * @return true if registering succeeded */
  public boolean registerDrJavaFileExtensions() { return false; }

  /** Unregister .drscala and .dsapp file extensions.
    * @return true if unregistering succeeded */
  public boolean unregisterDrJavaFileExtensions() { return false; }
  
  /** @return true if .drscala and .dsapp file extensions are registered. */
  public boolean areDrJavaFileExtensionsRegistered() { return false; }
  
  /** Register .java file extension.
    * @return true if registering succeeded */
  public boolean registerJavaFileExtension() { return false; }
  
  /** Unregister .java file extension.
    * @return true if unregistering succeeded */
  public boolean unregisterJavaFileExtension() { return false; }
  
  /** @return true if .java file extension is registered. */
  public boolean isJavaFileExtensionRegistered() { return false; }
}
