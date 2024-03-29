/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2019, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the names of its contributors may be used 
 *      to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software. Open Source Initative Approved is a trademark
 * of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/ or 
 * http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/
package edu.rice.cs.drjava.platform;

import javax.swing.Action;
import java.net.URL;

/** The central interface for all platform-specific code in DrJava.  A default platform-neutral implementation is
  * provided in DefaultPlatform.
  * @version $Id$
  * @see edu.rice.cs.drjava.platform.DefaultPlatform
  */
public interface PlatformSupport {
  
  /** @return whether this is a Mac OS X platform. */
  public boolean isMacPlatform();
  
  /** @return whether this is a Windows platform. */
  public boolean isWindowsPlatform();
  
  /** @return the current Java specification version. */
  public String getJavaSpecVersion();
  
  /** Utility method to determine if the current Swing look and feel is the
   *  platform-specific look and feel for the client platform.
   *  @return true if current Swing look and feel is the system look and feel
   */
  public boolean isUsingSystemLAF();
  
  /** Hook for performing general UI setup.  Called before all other UI setup is done. */
  public void beforeUISetup();
  
  /** Hook for performing general UI setup.  Called after all other UI setup is done.
   *  @param about the Action associated with openning the About dialog
   *  @param prefs the Action associated with openning the Preferences dialog
   *  @param quit the Action associated with quitting the DrJava application
   */
  public void afterUISetup(Action about, Action prefs, Action quit);
  
  /** Utility method for opening a URL in a browser in a platform-specific way.
   *  The default implementation uses Runtime.exec to execute a command specified
   *  in Preferences.  Platform implementations should attempt the default method
   *  first, then try to use a "default browser", if such a thing exists on the
   *  specific platform.
   *  @param address the URL to open
   *  @return true if the URL was successfully handled, false otherwise
   */
  public boolean openURL(URL address);
  
  /** Set the keyboard mnemonic for the component in a way that is consistent with
    * the current platform.
    * @param obj the component whose mnemonic should be set
    * @param mnemonic the key code which represents the mnemonic
    * @see javax.swing.AbstractButton#setMnemonic(int)
    * @see java.awt.event.KeyEvent */
  public void setMnemonic(javax.swing.AbstractButton obj, int mnemonic);

  /** Set the keyboard mnemonic for the component in a way that is consistent with
    * the current platform.
    * @param obj the component whose mnemonic should be set
    * @param mnemonic a char specifying the mnemonic value
    * @see javax.swing.AbstractButton#setMnemonic(char) */
  public void setMnemonic(javax.swing.AbstractButton obj, char mnemonic);

  /** Set the keyboard mnemonic for the component in a way that is consistent with
    * the current platform.
    * @param obj the component whose mnemonic should be set
    * @param mnemonic the key code which represents the mnemonic
    * @see javax.swing.ButtonModel#setMnemonic(int)
    * @see java.awt.event.KeyEvent */
  public void setMnemonic(javax.swing.ButtonModel obj, int mnemonic);

  /** Set the keyboard mnemonic for the component in a way that is consistent with
    * the current platform.
    * @param obj the component whose mnemonic should be set
    * @param tabIndex the index of the tab that the mnemonic refers to
    * @param mnemonic the key code which represents the mnemonic
    * @see javax.swing.JTabbedPane#setMnemonicAt(int,int)
    * @see java.awt.event.KeyEvent */
  public void setMnemonicAt(javax.swing.JTabbedPane obj, int tabIndex, int mnemonic);
  
  /** @return true if file extensions can be registered and unregistered. */
  public boolean canRegisterFileExtensions();
  
  /** Register .drjava file extensions.
    * @return true if registering succeeded */
  public boolean registerDrJavaFileExtensions();

  /** Unregister .drjava file extensions.
    * @return true if unregistering succeeded */
  public boolean unregisterDrJavaFileExtensions();
  
  /** @return true if .drjava file extensions are registered. */
  public boolean areDrJavaFileExtensionsRegistered();
  
  /** Register .java file extension.
    * @return true if registering succeeded */
  public boolean registerJavaFileExtension();
  
  /** Unregister .java file extension.
    * @return true if unregistering succeeded */
  public boolean unregisterJavaFileExtension();
  
  /** @return true if .java file extension is registered. */
  public boolean isJavaFileExtensionRegistered();
}
