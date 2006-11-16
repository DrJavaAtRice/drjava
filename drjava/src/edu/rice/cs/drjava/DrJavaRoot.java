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

package edu.rice.cs.drjava;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import javax.swing.UIManager;
import javax.swing.*;

import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.OutputStreamRedirector;
import edu.rice.cs.util.newjvm.ExecJVM;
import edu.rice.cs.util.classloader.ToolsJarClassLoader;
import edu.rice.cs.util.swing.Utilities;

import edu.rice.cs.drjava.ui.MainFrame;
import edu.rice.cs.drjava.ui.SplashScreen;
import edu.rice.cs.drjava.ui.ClassPathFilter;
import edu.rice.cs.drjava.ui.DrJavaErrorWindow;
import edu.rice.cs.drjava.ui.DrJavaErrorHandler;
import edu.rice.cs.drjava.ui.SimpleInteractionsWindow;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.drjava.config.FileConfiguration;
import edu.rice.cs.drjava.config.*;

import static edu.rice.cs.drjava.config.OptionConstants.*;

/** Main class for DrJava.
 *  @version $Id$
 */
public class DrJavaRoot {
  
  /* Constants for language levels */
  public static final int FULL_JAVA = 0;
  public static final int ELEMENTARY_LEVEL = 1;
  public static final int INTERMEDIATE_LEVEL = 2;
  public static final int ADVANCED_LEVEL = 3;
  public static final String[] LANGUAGE_LEVEL_EXTENSIONS = new String[] {".java", ".dj0", ".dj1", ".dj2"};
  
  /** Class to probe to see if the debugger is available */
  public static final String TEST_DEBUGGER_CLASS = "com.sun.jdi.Bootstrap";

  private static final PrintStream _consoleOut = System.out;
  private static final PrintStream _consoleErr = System.err;
  
//  /** This field is only used in the instance of this class in the Interpreter JVM. */
//  private static PreventExitSecurityManager _manager = null;
  
  private static boolean _attemptingAugmentedClassPath = false;
  private static SimpleInteractionsWindow _debugConsole = null;
  
  /* Config objects can't be public static final, since we have to delay construction until we know the 
   * config file's location.  (Might be specified on command line.) Instead, use accessor methods to 
   * prevent others from assigning new values. */
  
  public static void main(final String[] args) {
//    Utilities.show("DrJavaRoot started with args = " + Arrays.toString(args));
    // let DrJava class handle command line arguments
    if (!DrJava.handleCommandLineArgs(args)) {
      System.exit(0);
    }

    String[] filesToOpen = DrJava.getFilesToOpen();
    final int numFiles = filesToOpen.length;
      
    /* files to open held in filesToOpen[0:numFiles-1] which may be an initial segment of filesToOpen */
    
    /* In some unit test cases, creating a MainFrame in the main thread generated index out of bounds exceptions.  It appear that this
     * creation process generates some swing events that are processed by the event thread.  Hence we need to create the MainFrame in
     * the event thread.
     */
//    Utilities.invokeAndWait(new Runnable() {
//      public void run() {
        try {
          String configLAFName = DrJava.getConfig().getSetting(LOOK_AND_FEEL);
          String currLAFName = UIManager.getLookAndFeel().getClass().getName();
          if (! configLAFName.equals(currLAFName)) UIManager.setLookAndFeel(configLAFName);
          
          // The MainFrame *must* be constructed after the compiler setup process has
          // occurred; otherwise, the list of compilers in the UI will be wrong.
          
//      Utilities.showDebug("Creating MainFrame");
          
          final MainFrame mf = new MainFrame();
          
//      Utilities.showDebug("MainFrame created");
          
          // Make sure all uncaught exceptions are shown in an DrJavaErrorHandler
          DrJavaErrorWindow.setFrame(mf);
          System.setProperty("sun.awt.exception.handler", "edu.rice.cs.drjava.ui.DrJavaErrorHandler");
          
          _openCommandLineFiles(mf, filesToOpen, numFiles);
          
          /* This call on invokeLater only runs in the main thread, so we use SwingUtilities rather than Utilities.
           * We use invokeLater here ensure all files have finished loading and added to the fileview before the MainFrame
           * is set visible.  When this was not done, we occasionally encountered a NullPointerExceptio on startUp when 
           * specifying a file (ex: java -jar drjava.jar somefile.java)
           */
          SwingUtilities.invokeLater(new Runnable(){ public void run(){ mf.setVisible(true); } });
          
          // redirect stdout to DrJava's console
          System.setOut(new PrintStream(new OutputStreamRedirector() {
            public void print(String s) { mf.getModel().systemOutPrint(s); }
          }));
          
          // redirect stderr to DrJava's console
          System.setErr(new PrintStream(new OutputStreamRedirector() {
            public void print(String s) { mf.getModel().systemErrPrint(s); }
          }));
          
//      Utilities.showDebug("showDebugConsole flag = " + DrJava.getShowDebugConsole());
          // Show debug console if enabled
          if (DrJava.getShowDebugConsole()) showDrJavaDebugConsole(mf);
        }
        catch (Throwable t) {
          // Show any errors to the real System.err and in an DrJavaErrorHandler
          _consoleErr.println(t.getClass().getName() + ": " + t.getMessage());
          t.printStackTrace(_consoleErr);
          System.out.println("error thrown");
          new DrJavaErrorHandler().handle(t);
        }
//      }
//    });
  }

  /** Handle the list of files specified on the command line.  Feature request #509701.
   *  If file exists, open it in DrJava.  Otherwise, ignore it.
   *  Is there a better way to handle nonexistent files?  Dialog box, maybe?
   */
  static void openCommandLineFiles(final MainFrame mf, final String[] filesToOpen) { 
    openCommandLineFiles(mf, filesToOpen, filesToOpen.length);
  }
  
  /** Handle the list of files specified on the command line.  Feature request #509701. If the final element in 
    * filesToOpen is a pathSeparator, it opens the debug console. If file exists, open it in DrJava.  Otherwise, ignore
    * it.  Is there a better way to handle nonexistent files?  Dialog box, maybe?
    * Why the wait?
    */
  static void openCommandLineFiles(final MainFrame mf, final String[] filesToOpen, final int len) { 
    Utilities.invokeAndWait(new Runnable() { public void run() { _openCommandLineFiles(mf, filesToOpen, len); }});
  }
      
  private static void _openCommandLineFiles(MainFrame mf, String[] filesToOpen, int len) {
//    Utilities.showDebug("Files to open: " + Arrays.toString(filesToOpen));
    for (int i = 0; i < len; i++) {
      String currFileName = filesToOpen[i];
      boolean isProjectFile = currFileName.endsWith(".pjt");
      final File file = new File(currFileName).getAbsoluteFile();
      FileOpenSelector command = new FileOpenSelector() {
        public File[] getFiles() { return new File[] {file}; }
      };
      try {
        if (isProjectFile) mf.openProject(command);
        else mf.getModel().openFile(command);
      }

      catch (FileNotFoundException ex) {
        // TODO: show a dialog? (file not found)
      }
      catch (SecurityException se) {
        // TODO: show a dialog? (file not found)
      }
      catch (AlreadyOpenException aoe) {
        // This explicitly does nothing to ignore duplicate files.
      }
      catch (FileMovedException aoe) {
        // This explicitly does nothing to ignore duplicate files.
      }
      catch (IOException ex) {
        // TODO: show a dialog? (file not found)
      }
      catch (Exception ex) { throw new UnexpectedException(ex); }  
    }
  }

  /** Shows a separate interactions window with a reference to DrJava's MainFrame defined as "mainFrame".  
   *  Useful for debugging DrJava.
   *  @param mf MainFrame to define in the new window
   */
  public static void showDrJavaDebugConsole(MainFrame mf) {
    if (_debugConsole == null) {
      _debugConsole = new SimpleInteractionsWindow("DrJava Debug Console") {
        protected void close() {
          dispose();
          _debugConsole = null;
        }
      };
      _debugConsole.defineConstant("mainFrame", mf);
      _debugConsole.defineConstant("model", mf.getModel());
      _debugConsole.defineConstant("config", DrJava.getConfig());
      _debugConsole.setInterpreterPrivateAccessible(true);
      _debugConsole.setVisible(true);
    }
    else  _debugConsole.toFront();
  }

  /** Get the actual System.err stream.
   *  @return System.err
   */
  public static PrintStream consoleErr() { return  _consoleErr; }

  /** Get the actual System.out stream.
    * @return System.out
   */
  public static PrintStream consoleOut() { return  _consoleOut; }
  
}

