/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import edu.rice.cs.util.*;
import edu.rice.cs.drjava.config.OptionConstants;

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
  public boolean canRegisterFileExtensions() {
    // only works in .exe version for now
    try {
      return getDrJavaFile().getName().endsWith(".exe");
    }
    catch(IOException ioe) { return false; }
  }
  
  private static final String DRJAVA_PROJECT_PROGID = "DrJava.Project";
  private static final String DRJAVA_EXTPROCESS_PROGID = "DrJava.ExtProcess";
  private static final String DRJAVA_JAVA_PROGID = "DrJava.Java";
  
  /** Register .drjava and .djapp file extensions.
    * @return true if registering succeeded */
  public boolean registerDrJavaFileExtensions() {
    boolean retval = registerFileExtension(OptionConstants.PROJECT_FILE_EXTENSION, DRJAVA_PROJECT_PROGID, "DrJava project file", "text", "text/plain");
    retval &= registerFileExtension(OptionConstants.EXTPROCESS_FILE_EXTENSION, DRJAVA_EXTPROCESS_PROGID, "DrJava addon file", "program", "multipart/mixed");
    return retval;
  }

  /** Unregister .drjava and .djapp file extensions.
    * @return true if unregistering succeeded */
  public boolean unregisterDrJavaFileExtensions() {
    boolean retval = unregisterFileExtension(OptionConstants.PROJECT_FILE_EXTENSION, DRJAVA_PROJECT_PROGID);
    retval &= unregisterFileExtension(OptionConstants.EXTPROCESS_FILE_EXTENSION, DRJAVA_EXTPROCESS_PROGID);
    return retval;
  }
  
  /** @return true if .drjava and .djapp file extensions are registered. */
  public boolean areDrJavaFileExtensionsRegistered() {
    return
      isFileExtensionRegistered(OptionConstants.PROJECT_FILE_EXTENSION, DRJAVA_PROJECT_PROGID) && 
      isFileExtensionRegistered(OptionConstants.EXTPROCESS_FILE_EXTENSION, DRJAVA_EXTPROCESS_PROGID);
  }
  
  /** Register .java file extension.
    * @return true if registering succeeded */
  public boolean registerJavaFileExtension() {
    return registerFileExtension(".java", DRJAVA_JAVA_PROGID, "Java source file", "text", "text/plain");
  }
  
  /** Unregister .java file extension.
    * @return true if unregistering succeeded */
  public boolean unregisterJavaFileExtension() {
    return unregisterFileExtension(".java", DRJAVA_JAVA_PROGID);
  }
  
  /** @return true if .java file extension is registered. */
  public boolean isJavaFileExtensionRegistered() {
    return isFileExtensionRegistered(".java", DRJAVA_JAVA_PROGID);
  }
     
  /** Return true if a file extension is registered.
    * @param extension extension, like ".drjava"
    * @param progid program ID, like "DrJava.Project"
    * @return true if a file extension is registered */
  private boolean isFileExtensionRegistered(String extension, String progid) {
    try {
      // check the file extension
      String oldDefault = WindowsRegistry.getKey(WindowsRegistry.HKEY_CLASSES_ROOT, extension, "");
      if ((oldDefault==null) || (!progid.equals(oldDefault))) return false; // not set
      
      // now check the command
      String cmdLine = getCommandLine()+" \"%1\" %*";
      String oldCmdLine = WindowsRegistry.getKey(WindowsRegistry.HKEY_CLASSES_ROOT, progid+"\\shell\\open\\command", "");
      return ((oldCmdLine!=null) && (cmdLine.equals(oldCmdLine)));
    }
    catch(WindowsRegistry.RegistryException re) {
      return false;
    }
    catch(IOException ioe) {
      return false;
    }
  }
  
  //public static final edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("platform.txt",true);
  
  /** Register a file extension.
    * @param extension extension, like ".drjava"
    * @param progid program ID, like "DrJava.Project"
    * @param fileDesc file description, like "DrJava project file"
    * @return true if registering succeeded */
  private boolean registerFileExtension(String extension, String progid, String fileDesc, String perceived, String mime) {
    try {
      String cmdLine = getCommandLine();
      //      The general form of a file extension key is:
      //
      //    * HKEY_CLASSES_ROOT
      //          o .ext
      //
      //            (Default) = ProgID.ext.1 (REG_SZ)
      //            PerceivedType = PerceivedType (REG_SZ)
      //            Content Type = mime content type (REG_SZ)
      //                + OpenWithProgids
      //                      # ProgID2.ext.1
      //                      # ProgID3.ext.1
      //                + OpenWithList
      //                      # AlternateProgram1.exe
      //                      # AlternateProgram2.exe
      //                + ProgID.ext.1
      //                      # shellnew      
      try {
        String oldDefault = WindowsRegistry.getKey(WindowsRegistry.HKEY_CLASSES_ROOT, extension, "");
        if ((oldDefault!=null) && (!progid.equals(oldDefault))) {
          // add the old default to the OpenWithProgids and OpenWithList
          WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, extension+"\\OpenWithProgids", oldDefault, "");
          WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, extension+"\\OpenWithList", oldDefault, "");
        }
      }
      catch(WindowsRegistry.RegistryException re) { /* if the lookup fails, then there was nothing to do anyway */ }
      
      WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, extension, "", progid);
      WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, extension, "PerceivedType", perceived);
      WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, extension, "Content Type", mime);
      
      WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, extension+"\\OpenWithProgids", progid, "");
      WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, extension+"\\OpenWithList", progid, "");
      
      WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, progid, "", fileDesc);
      
      WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, progid+"\\shell\\open", "FriendlyAppName", "DrJava");
      WindowsRegistry.setKey(WindowsRegistry.HKEY_CLASSES_ROOT, progid+"\\shell\\open\\command", "",
                             cmdLine+" \"%1\" %*");
      return true;
    }
    catch(WindowsRegistry.RegistryException re) {
      return false;
    }
    catch(IOException ioe) {
      return false;
    }
  }

  /** Unregister a file extension.
    * @param extension extension, like ".drjava"
    * @param progid program ID, like "DrJava.Project"
    * @return true if unregistering succeeded */
  public boolean unregisterFileExtension(String extension, String progid) {
    boolean otherProgidsLeft = false; // true if other programs are still registered and the file type shouldn't be deleted
    try {
      int handle;
      WindowsRegistry.QueryInfoResult qir;
      try {
        handle = WindowsRegistry.openKey(WindowsRegistry.HKEY_CLASSES_ROOT, extension+"\\OpenWithProgids",
                                         WindowsRegistry.KEY_ALL_ACCESS);
        try {
          WindowsRegistry.deleteValue(handle, progid);
        }
        catch(WindowsRegistry.RegistryException re) { /* if it couldn't be deleted, there was nothing to do anyway */ }
        qir = WindowsRegistry.queryInfoKey(handle);
        otherProgidsLeft |= (qir.valueCount>0);
        otherProgidsLeft |= (qir.subkeyCount>0);
        WindowsRegistry.flushKey(handle);
        WindowsRegistry.closeKey(handle);
        
        handle = WindowsRegistry.openKey(WindowsRegistry.HKEY_CLASSES_ROOT, extension+"\\OpenWithList",
                                         WindowsRegistry.KEY_ALL_ACCESS);
        try {
          WindowsRegistry.deleteValue(handle, progid);
        }
        catch(WindowsRegistry.RegistryException re) { /* if it couldn't be deleted, there was nothing to do anyway */ }
        qir = WindowsRegistry.queryInfoKey(handle);
        otherProgidsLeft |= (qir.valueCount>0);
        otherProgidsLeft |= (qir.subkeyCount>0);
        WindowsRegistry.flushKey(handle);
        WindowsRegistry.closeKey(handle);
        
        if (!otherProgidsLeft) {
          WindowsRegistry.delKey(WindowsRegistry.HKEY_CLASSES_ROOT, extension+"\\OpenWithProgids");
          WindowsRegistry.delKey(WindowsRegistry.HKEY_CLASSES_ROOT, extension);
        }
        WindowsRegistry.delKey(WindowsRegistry.HKEY_CLASSES_ROOT, progid+"\\shell\\open");
        WindowsRegistry.delKey(WindowsRegistry.HKEY_CLASSES_ROOT, progid);
      }
      catch(WindowsRegistry.RegistryException re) {
        /* if it couldn't be deleted, there was nothing to do anyway */
      }
      
      // also need to delete from
      // HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.ext\Application
      File drjavaFile = null;
      String ourCmdLine = null;
      try {
        drjavaFile = getDrJavaFile();
        ourCmdLine = getCommandLine()+" \"%1\" %*";
      }
      catch(IOException ioe) { return false; }

      try {
        // LOG.log("[1]");
        handle = WindowsRegistry.openKey(WindowsRegistry.HKEY_CURRENT_USER,
                                         "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\"+extension,
                                         WindowsRegistry.KEY_ALL_ACCESS);
        // LOG.log("[2]");
        try {
          String s = WindowsRegistry.queryValue(handle, "Application");
          // LOG.log("Application = "+s);
          if ((s!=null) && (s.equals(drjavaFile.getName()))) {
            // LOG.log("[3]");
            WindowsRegistry.deleteValue(handle, progid);
            // LOG.log("[4]");
          }
        }
        catch(WindowsRegistry.RegistryException re) {
          // LOG.log("[A] "+re.toString());
          /* if it couldn't be deleted, there was nothing to do anyway */
        }
        
        // LOG.log("[5]");
        WindowsRegistry.flushKey(handle);
        // LOG.log("[6]");
        WindowsRegistry.closeKey(handle);
        // LOG.log("[7]");
      }
      catch(WindowsRegistry.RegistryException re) {
        // LOG.log("[B] "+re.toString());
        /* if it couldn't be deleted, there was nothing to do anyway */
      }

      // HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.ext\OpenWithProgids\progid
      otherProgidsLeft = false;
      try {
        // LOG.log("[8]");
        handle = WindowsRegistry.openKey(WindowsRegistry.HKEY_CURRENT_USER,
                                         "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\"+extension+"\\OpenWithProgids",
                                         WindowsRegistry.KEY_ALL_ACCESS);
        // LOG.log("[9]");
        try {
          // LOG.log("[10] delete progid: "+progid);
          WindowsRegistry.deleteValue(handle, progid);
          // LOG.log("[11]");
        }
        catch(WindowsRegistry.RegistryException re) {
          // LOG.log("[B] "+re.toString());
          /* if it couldn't be deleted, there was nothing to do anyway */
        }
        // LOG.log("[12]");
        qir = WindowsRegistry.queryInfoKey(handle);
        // LOG.log("[13]");
        otherProgidsLeft |= (qir.valueCount>0);
        otherProgidsLeft |= (qir.subkeyCount>0);
        // LOG.log("[15]");
        WindowsRegistry.flushKey(handle);
        // LOG.log("[16]");
        WindowsRegistry.closeKey(handle);
        // LOG.log("[17], left="+otherProgidsLeft);

        if (!otherProgidsLeft) {
          // LOG.log("[18]");
          WindowsRegistry.delKey(WindowsRegistry.HKEY_CURRENT_USER,
                                 "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\"+extension+"\\OpenWithProgids");
          // LOG.log("[19]");
        }
      }
      catch(WindowsRegistry.RegistryException re) {
        // LOG.log("[C] "+re.toString());
        /* if it couldn't be deleted, there was nothing to do anyway */
      }

      String mruList = "";
      // HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.ext\OpenWithList\MRUList
      // HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.ext\OpenWithList\a      
      try {
        // LOG.log("[20]");
        handle = WindowsRegistry.openKey(WindowsRegistry.HKEY_CURRENT_USER,
                                         "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\"+extension+"\\OpenWithList",
                                         WindowsRegistry.KEY_ALL_ACCESS);
        // LOG.log("[21]");
        try {
          String s = WindowsRegistry.queryValue(handle, "MRUList");
          // LOG.log("[22] s="+s);
          if (s!=null) mruList = s;
        }
        catch(WindowsRegistry.RegistryException re) {
          // LOG.log("[D] "+re.toString());
          /* if it couldn't be read, there was nothing to do anyway */
        }
        // LOG.log("[23] MRUlist="+mruList);
        String newMRUList = "";
        for(int i=0; i<mruList.length(); ++i) {
          String letter = mruList.substring(i,i+1); 
          // LOG.log("[24] i="+i+" letter="+letter);
          boolean keep = true;
          try {
            // LOG.log("[24]");
            String value = WindowsRegistry.queryValue(handle, letter);
            // LOG.log("[25] value="+value);
            if (value!=null) {
              // value is something like "drjava.exe"
              // check if this is our command line
              // HKEY_LOCAL_MACHINE\SOFTWARE\Classes\Applications\<value>\shell\open\command
              // LOG.log("[26]");
              try {
                String cmdLine =
                  WindowsRegistry.getKey(WindowsRegistry.HKEY_LOCAL_MACHINE,
                                         "SOFTWARE\\Classes\\Applications\\"+value+"\\shell\\open\\command","");
                // LOG.log("[27] cmdLine="+cmdLine);
                // LOG.log("ourCmdLine="+ourCmdLine);
                if ((cmdLine!=null) && (cmdLine.equals(ourCmdLine))) {
                  // LOG.log("[28]");
                  // this is ours, delete it
                  keep = false;
                  WindowsRegistry.delKey(WindowsRegistry.HKEY_LOCAL_MACHINE,
                                         "SOFTWARE\\Classes\\Applications\\"+value+"\\shell\\open\\command");
                  // LOG.log("[29]");
                  WindowsRegistry.delKey(WindowsRegistry.HKEY_LOCAL_MACHINE,
                                         "SOFTWARE\\Classes\\Applications\\"+value+"\\shell\\open");
                  // LOG.log("[30]");
                  WindowsRegistry.delKey(WindowsRegistry.HKEY_LOCAL_MACHINE,
                                         "SOFTWARE\\Classes\\Applications\\"+value+"\\shell");
                  // LOG.log("[31]");
                  WindowsRegistry.delKey(WindowsRegistry.HKEY_LOCAL_MACHINE,
                                         "SOFTWARE\\Classes\\Applications\\"+value);
                  // LOG.log("[32]");
                }
              }
              catch(WindowsRegistry.RegistryException re) {
                // LOG.log("[E] "+re.toString());
                /* if it couldn't be read, there was nothing to do anyway */
              }
              
              // HKEY_CURRENT_USER\Software\Classes\Applications\<value>\shell\open\command
              // LOG.log("[33]");
              try {
                String cmdLine =
                  WindowsRegistry.getKey(WindowsRegistry.HKEY_CURRENT_USER,
                                         "Software\\Classes\\Applications\\"+value+"\\shell\\open\\command","");
                // LOG.log("[34] cmdLine"+cmdLine);
                // LOG.log("ourCmdLine="+ourCmdLine);
                if ((cmdLine!=null) && (cmdLine.equals(ourCmdLine))) {
                  // LOG.log("[35]");
                  // this is ours, delete it
                  keep = false;
                  WindowsRegistry.delKey(WindowsRegistry.HKEY_CURRENT_USER,
                                         "Software\\Classes\\Applications\\"+value+"\\shell\\open\\command");
                  // LOG.log("[36]");
                  WindowsRegistry.delKey(WindowsRegistry.HKEY_CURRENT_USER,
                                         "Software\\Classes\\Applications\\"+value+"\\shell\\open");
                  // LOG.log("[37]");
                  WindowsRegistry.delKey(WindowsRegistry.HKEY_CURRENT_USER,
                                         "Software\\Classes\\Applications\\"+value+"\\shell");
                  // LOG.log("[38]");
                  WindowsRegistry.delKey(WindowsRegistry.HKEY_CURRENT_USER,
                                         "Software\\Classes\\Applications\\"+value);
                  // LOG.log("[39]");
                }
              }
              catch(WindowsRegistry.RegistryException re) {
                // LOG.log("[F] "+re.toString());
                /* if it couldn't be read, there was nothing to do anyway */
              }
              // LOG.log("[40] keep="+keep);
              if (!keep) {
                try {
                  // LOG.log("[41]");
                  WindowsRegistry.deleteValue(handle, letter);
                  // LOG.log("[42]");
                }
                catch(WindowsRegistry.RegistryException re) {
                  // LOG.log("[G] "+re.toString());
                  /* if it couldn't be read, ignore it, we can still fix it with MRUList */
                }
              }
            }
          }
          catch(WindowsRegistry.RegistryException re) {
            // LOG.log("[H] "+re.toString());
            /* if it couldn't be read, there was nothing to do anyway */
          }
          // LOG.log("[43]");
          if (keep) newMRUList = newMRUList + letter;
          // LOG.log("[44] newMRUList="+newMRUList);
        }
        // LOG.log("[45] final newMRUList="+newMRUList);
        // LOG.log("mruList="+mruList);
        if (!mruList.equals(newMRUList)) {
          // LOG.log("[46]");
          // update MRUList
          WindowsRegistry.setValue(handle, "MRUList", newMRUList);
          // LOG.log("[47]");
        }
        // LOG.log("[48]");
        WindowsRegistry.flushKey(handle);
        // LOG.log("[49]");
        WindowsRegistry.closeKey(handle);
        // LOG.log("[50]");
      }
      catch(WindowsRegistry.RegistryException re) {
        // LOG.log("[I] "+re.toString());
        /* if it couldn't be read, there was nothing to do anyway */
      }      
      return true;
    }
    catch(WindowsRegistry.RegistryException re) {
      // LOG.log("[Z] "+re);
      return false;
    }
  }

  private File getDrJavaFile() throws IOException {
    // detect current DrJava file (.jar or .exe)
    String[] cps = System.getProperty("java.class.path").split("\\;", -1);
    File found = null;
    for(String cp: cps) {
      try {
        File f = new File(cp);
        if (!f.exists()) { continue; }
        if (f.isDirectory()) {
          // this is a directory, maybe DrJava is contained here as individual files
          File cf = new File(f, edu.rice.cs.drjava.DrJava.class.getName().replace('.', File.separatorChar)+".class");
          if (cf.exists() && cf.isFile()) {
            found = f;
            break;
          }
        }
        else if (f.isFile()) {
          // this is a file, it should be a jar file
          java.util.jar.JarFile jf = new java.util.jar.JarFile(f);
          // if it's not a jar file, an exception will already have been thrown
          // so we know it is a jar file
          // now let's check if it contains DrJava
          if (jf.getJarEntry(edu.rice.cs.drjava.DrJava.class.getName().replace('.', '/')+".class")!=null) {
            found = f;
            break;
          }
        }
      }
      catch(IOException e) { /* ignore, we'll continue with the next classpath item */ }
    }
    if (found==null) throw new IOException("DrJava file not found");
    return found;
  }
  
  private String getCommandLine() throws WindowsRegistry.RegistryException, IOException {
    final File drjavaFile = getDrJavaFile();
    
    String cmdLine = drjavaFile.getAbsolutePath();
    if (!drjavaFile.getAbsolutePath().endsWith(".exe")) {
      // jar file, we need to start it with Java
      // look up .jar
      String jarProgid = WindowsRegistry.getKey(WindowsRegistry.HKEY_CLASSES_ROOT, ".jar", "");
      String jarLine = WindowsRegistry.getKey(WindowsRegistry.HKEY_CLASSES_ROOT, jarProgid+"\\shell\\open\\command", "");
      BalancingStreamTokenizer tok = new BalancingStreamTokenizer(new StringReader(jarLine));
      tok.wordRange(0,255);
      tok.whitespaceRange(' ',' ');
      tok.addQuotes("\"","\"");
      String jarCommand = tok.getNextToken();
      cmdLine = jarCommand + " -jar "+drjavaFile.getAbsolutePath();
    }
    return cmdLine;
  }
}
