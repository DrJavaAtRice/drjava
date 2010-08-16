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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.drjava.model.DrJavaFileUtils;
import edu.rice.cs.drjava.project.MalformedProjectFileException;
import edu.rice.cs.drjava.model.debug.DebugException;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.StringOps;

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;

import static edu.rice.cs.plt.object.ObjectUtil.hash;

/** Utilities for DrJava's main window. */
public class MainFrameStatics {
  
  /** Propose to the user to change the extension of the file.
    * @param parent parent GUI component
    * @param input input file 
    * @param title dialog title
    * @param message dialog message
    * @param changeButton text for the "yes, change it!" button
    * @param keepButton text for the "no, leave it!" button
    * @param newExt new extension if changed
    */
  public static File proposeToChangeExtension(Component parent, File input,
                                              String title,
                                              String message,
                                              String changeButton,
                                              String keepButton,
                                              String newExt) {
    Object[] options = {changeButton, keepButton};  
    int rc = 1;
    if (!Utilities.TEST_MODE) {
      rc = JOptionPane.showOptionDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    }
    if (rc == 0) {
      try {
        String fileName = DrJavaFileUtils.removeExtension(input.getCanonicalPath()) + newExt;
        File file = new File(fileName);
        return file;
      }
      catch(IOException ioe) { showIOError(parent, ioe); }
    }
    return input;
  }

  /** Confirms with the user that the file should be overwritten.
    * @param f file to overwrite
    * @return <code>true</code> iff the user accepts overwriting.
    */
  public static boolean verifyOverwrite(Component parent, File f) {
    Object[] options = {"Yes","No"};
    int n = JOptionPane.showOptionDialog(parent,
                                         "<html>This file already exists.  Do you wish to overwrite the file?<br>"+
                                         f.getPath()+"<html>",
                                         "Confirm Overwrite",
                                         JOptionPane.YES_NO_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null,
                                         options,
                                         options[1]);
    return (n == JOptionPane.YES_OPTION);
  }
  
  public static void showProjectFileParseError(Component parent, MalformedProjectFileException mpfe) {
    showError(parent, mpfe, "Invalid Project File", "DrJava could not read the given project file.");
  }
  
  public static void showFileNotFoundError(Component parent, FileNotFoundException fnf) {
    showError(parent, fnf, "File Not Found", "The specified file was not found on disk.");
  }
  
  public static void showIOError(Component parent, IOException ioe) {
    showError(parent, ioe, "Input/output error", "An I/O exception occurred during the last operation.");
  }
  
  public static void showClassNotFoundError(Component parent, ClassNotFoundException cnfe) {
    showError(parent, cnfe, "Class Not Found",
              "A ClassNotFound exception occurred during the last operation.\n" +
              "Please check that your classpath includes all relevant directories.\n\n");
  }
  
  public static void showNoClassDefError(Component parent, NoClassDefFoundError ncde) {
    showError(parent, ncde, "No Class Def",
              "A NoClassDefFoundError occurred during the last operation.\n" +
              "Please check that your classpath includes all relevant paths.\n\n");
  }
  
  public static void showDebugError(Component parent, DebugException de) {
    showError(parent, de, "Debug Error", "A Debugger error occurred in the last operation.\n\n");
  }
  
  public static void showJUnitInterrupted(Component parent, UnexpectedException e) {
    showWarning(parent, e.getCause(), "JUnit Testing Interrupted", 
                "The slave JVM has thrown a RemoteException probably indicating that it has been reset.\n\n");
  }
  
  public static void showJUnitInterrupted(Component parent, String message) {
    JOptionPane.showMessageDialog(parent, message, "JUnit Testing Interrupted", JOptionPane.WARNING_MESSAGE);
  }
  
  public static void showError(Component parent, Throwable e, String title, String message) {    
    JOptionPane.showMessageDialog(parent, message + "\n" + e + "\n"+ StringOps.getStackTrace(e),
                                  title, JOptionPane.ERROR_MESSAGE);
  }
  
  public static void showWarning(Component parent, Throwable e, String title, String message) {
    JOptionPane.showMessageDialog(parent, message + "\n" + e, title, JOptionPane.WARNING_MESSAGE);
  }
  
  public static abstract class AutoCompletePopupEntry implements Comparable<AutoCompletePopupEntry> {
    /** Return the simple class name, e.g. "Integer". */
    public abstract String getClassName();
    /** Return the full package including the last period, e.g. "java.lang.". */
    public abstract String getFullPackage();
    /** Return the OpenDefinitionsDocument associated with this entry, or null if none. */
    public abstract OpenDefinitionsDocument getOpenDefinitionsDocument();
    
    public int compareTo(AutoCompletePopupEntry other) {
      int res = getClassName().toLowerCase().compareTo(other.getClassName().toLowerCase());
      if (res != 0) { return res; }
      return getFullPackage().toLowerCase().compareTo(other.getFullPackage().toLowerCase());
    }
    // WARNING: this relation is finer grained that the equivalance relation induced by compareTo above
    public boolean equals(Object other) {
      if (other == null || ! (other instanceof AutoCompletePopupEntry)) return false;  // multiple subclasses defined
      AutoCompletePopupEntry o = (AutoCompletePopupEntry) other;
      return (getClassName().equals(o.getClassName()) && getFullPackage().equals(o.getFullPackage()));
    }
    public int hashCode() { return hash(getClassName(), getFullPackage()); }
  }
  
  /** Wrapper class for the "Go to File" dialog list entries.
    * Provides the ability to have the same OpenDefinitionsDocument in there multiple
    * times with different toString() results.
    */
  public static class GoToFileListEntry extends AutoCompletePopupEntry {
    private final OpenDefinitionsDocument doc;
    private String fullPackage = null;
    private final String str;
    public GoToFileListEntry(OpenDefinitionsDocument d, String s) {
      doc = d;
      str = s;
    }
    public String getFullPackage() {
      if (fullPackage != null) { return fullPackage; }
      fullPackage = "";
      if (doc != null) {
        try {
          fullPackage = doc.getPackageName();
          if (fullPackage.length() > 0) { fullPackage += '.'; }
        }
        catch(Exception e) { fullPackage = ""; }
      }
      return fullPackage;
    }
    public String getClassName() { return str; }
    public String toString() { return str; }
    public OpenDefinitionsDocument getOpenDefinitionsDocument() { return doc; }
  }
  
  /** Wrapper class for the "Open Javadoc" and "Auto Import" dialog list entries.
    * Provides the ability to have the same class name in there multiple times in different packages.
    */
  public static class JavaAPIListEntry extends AutoCompletePopupEntry {
    private final String str, fullStr;
    private final URL url;
    public JavaAPIListEntry(String s, String full, URL u) {
      str = s;
      fullStr = full;
      url = u;
    }
    public String toString() { return str; }
    public String getFullString() { return fullStr; }
    public URL getURL() { return url; }
    public String getClassName() { return str; }
    public String getFullPackage() {
      int pos = fullStr.lastIndexOf('.');
      if (pos>=0) { return fullStr.substring(0,pos+1); }
      return "";
    }
    public OpenDefinitionsDocument getOpenDefinitionsDocument() { return null; }
  }
  
  /** Returns a JRadioButtonMenuItem that looks like a JCheckBoxMenuItem. This is a workaround for a known 
    * bug on OS X's version of Java. (See http://developer.apple.com/qa/qa2001/qa1154.html)
    * @param action Action for the menu item
    * @return JRadioButtonMenuItem with a checkbox icon
    */
  public static JMenuItem newCheckBoxMenuItem(Action action) {
    String RADIO_ICON_KEY = "RadioButtonMenuItem.checkIcon";
    String CHECK_ICON_KEY = "CheckBoxMenuItem.checkIcon";
    
    // Store the default radio button icon to put back later
    Object radioIcon = UIManager.get(RADIO_ICON_KEY);
    
    // Replace radio button's checkIcon with that of JCheckBoxMenuItem
    // so that our menu item looks like a checkbox
    UIManager.put(RADIO_ICON_KEY, UIManager.get(CHECK_ICON_KEY));
    JRadioButtonMenuItem pseudoCheckBox = new JRadioButtonMenuItem(action);
    
    // Put original radio button checkIcon back.
    UIManager.put(RADIO_ICON_KEY, radioIcon);
    
    return pseudoCheckBox;
  }
}
