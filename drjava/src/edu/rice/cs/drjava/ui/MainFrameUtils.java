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

import javax.swing.*;
import java.awt.*;
import java.io.*;

/** Utilities for DrJava's main window. */
public class MainFrameUtils {
  
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
        if (! file.exists() || verifyOverwrite(parent, file)) { 
          return file;
        }
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
}
