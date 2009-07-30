/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.platform;
import java.net.URL;

/**
 * Platform-specific code unique to the Windows platform.
 */
class WindowsPlatform extends DefaultPlatform {
  /**
   * Singleton instance.
   */
  public static WindowsPlatform ONLY = new WindowsPlatform();
  
  /**
   * Private constructor for singleton pattern.
   */
  protected WindowsPlatform() {};
  
  /**
   * Returns whether this is a Windows platform.
   */
  public boolean isWindowsPlatform() {
    return true;
  }
 
  public boolean openURL(URL address) {
    // First, try to delegate up.
    if (super.openURL(address)) {
      return true;
    }
    else {
      try {
        // Windows doesn't like how Java formats file URLs:
        //  "file:/C:/dir/file.html" isn't legal.
        // Instead, we need to put another slash in the protocol:
        //  "file://C:/dir/file.html"
        String addressString = address.toString();
        if (addressString.startsWith("file:/")) {
          String suffix = addressString.substring("file:/".length(), addressString.length());
          addressString = "file://" + suffix;
        }
        
        // If there is no command specified, or it won't work, try using "rundll32".
        //Process proc = 
        Runtime.getRuntime().exec(new String[] {
          "rundll32", "url.dll,FileProtocolHandler", addressString });
        
        // TODO: This may cause a memory leak on Windows, if we don't check the exit code.scp
      }
      catch (Throwable t) {
        // If there was any kind of problem, ignore it and report failure.
        return false;
      }
    }
    
    // Otherwise, trust that it worked.
    return true;
  }
  
  /** @return true if file extensions can be registered and unregistered. */
  public boolean canRegisterFileExtensions() { return true; }

  /** Register .drjava file extension.
    * @return true if registering succeeded */
  public boolean registerProjectFileExtension() {
    // TODO
//    try {
//      WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, ".drjava", "", "DrJavaProject");
//      WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, "DrJavaProject", "", "DrJava project file");
//      WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, "DrJavaProject\\shell\\open\\command", "",
//                             "drjava.exe \"%1\" %*");
//      return true;
//    }
//    catch(WindowsRegistry.RegistryException re) {
      return false;
//    }
  }

  /** Unregister .drjava file extension.
    * @return true if unregistering succeeded */
  public boolean unregisterProjectFileExtension() {
    // TODO
    return false;
  }
  
  /** @return true if .drjava file extension is registered. */
  public boolean isProjectFileExtensionRegistered() {
    // TODO
    return false;
  }
  
  /** Register .java file extension.
    * @return true if registering succeeded */
  public boolean registerJavaFileExtension() {
    // TODO
//    try {
//      WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, ".java", "", "DrJavaSource");
//      WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, "DrJavaSource", "", "Java source file");
//      WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, "DrJavaSource\\shell\\open\\command", "",
//                             "drjava.exe \"%1\" %*");
//      return true;
//    }
//    catch(WindowsRegistry.RegistryException re) {
      return false;
//    }
  }
  
  /** Unregister .java file extension.
    * @return true if unregistering succeeded */
  public boolean unregisterJavaFileExtension() {
    // TODO
    return false;
  }
  
  /** @return true if .java file extension is registered. */
  public boolean isJavaFileExtensionRegistered() {
    // TODO
    return false;
  }
}
