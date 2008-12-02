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

package edu.rice.cs.drjava;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.dnd.*;
import java.awt.event.WindowEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.UIManager;
import javax.swing.*;

import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.OutputStreamRedirector;
import edu.rice.cs.util.swing.Utilities;

import edu.rice.cs.drjava.ui.MainFrame;
import edu.rice.cs.drjava.ui.DrJavaErrorWindow;
import edu.rice.cs.drjava.ui.DrJavaErrorHandler;
import edu.rice.cs.drjava.ui.SimpleInteractionsWindow;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.config.*;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticTheme;

import static edu.rice.cs.drjava.config.OptionConstants.*;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Main class for DrJava.
  * @version $Id$
  */
public class DrJavaRoot {
  
  /* Constants for language levels */
  public static final int FULL_JAVA = 0;
  public static final int ELEMENTARY_LEVEL = 1;
  public static final int INTERMEDIATE_LEVEL = 2;
  public static final int ADVANCED_LEVEL = 3;
  public static final String[] LANGUAGE_LEVEL_EXTENSIONS = new String[] {"java", "dj0", "dj1", "dj2"};
  
  /** Class to probe to see if the debugger is available */
  public static final String TEST_DEBUGGER_CLASS = "com.sun.jdi.Bootstrap";
  
  public static final String PLASTIC_THEMES_PACKAGE = "com.jgoodies.looks.plastic.theme";
  
  private static final PrintStream _consoleOut = System.out;
  private static final PrintStream _consoleErr = System.err;
  
//  /** This field is only used in the instance of this class in the Interpreter JVM. */
  
  private static SimpleInteractionsWindow _debugConsole = null;
  
  private static boolean anyLineNumbersSpecified = false;
  
  /** Main frame of this DrJava instance. */
  private static MainFrame _mainFrame = null;
  
  /* Config objects can't be public static final, since we have to delay construction until we know the 
   * config file's location.  (Might be specified on command line.) Instead, use accessor methods to 
   * prevent others from assigning new values. */
  
  public static void main(final String[] args) {
    debug.log("Starting up");
    // Platform-specific UI setup.
    PlatformFactory.ONLY.beforeUISetup();
    
//    Utilities.show("DrJavaRoot started with args = " + Arrays.toString(args));
    // let DrJava class handle command line arguments
    if (!DrJava.handleCommandLineArgs(args)) {
      System.exit(0);
    }
    
    final String[] filesToOpen = DrJava.getFilesToOpen();
    final int numFiles = filesToOpen.length;
    
    /* files to open held in filesToOpen[0:numFiles-1] which may be an initial segment of filesToOpen */
    
    /* In some unit test cases, creating a MainFrame in the main thread generated index out of bounds exceptions.  It appear that this
     * creation process generates some swing events that are processed by the event thread.  Hence we need to create the MainFrame in
     * the event thread.
     */
    
    /* Set the LookAndFeel for this session. If using a Plastic LAF, the theme must be set before setting the LAF. */
    try {
      String configLAFName = DrJava.getConfig().getSetting(LOOK_AND_FEEL);
      String currLAFName = UIManager.getLookAndFeel().getClass().getName();
      String failureMessage =
        "DrJava could not load the configured theme for the Plastic Look and Feel.\n" +
        "If you've manually edited your configuration file, try \n" +
        "removing the key \"plastic.theme\" and restarting DrJava.\n" +
        "In the meantime, the system default Look and Feel will be used.\n";
      String failureTitle = "Theme not found";
      if(Utilities.isPlasticLaf(configLAFName)) {
        String themeName = PLASTIC_THEMES_PACKAGE + "." + DrJava.getConfig().getSetting(PLASTIC_THEMES);
        try {
          PlasticTheme theme = (PlasticTheme) Class.forName(themeName).getConstructor(new Class<?>[]{}).newInstance();
          PlasticLookAndFeel.setPlasticTheme(theme);
          PlasticLookAndFeel.setTabStyle(PlasticLookAndFeel.TAB_STYLE_METAL_VALUE);
          com.jgoodies.looks.Options.setPopupDropShadowEnabled(true);
          if(! configLAFName.equals(currLAFName)) UIManager.setLookAndFeel(configLAFName);
        } catch(NoSuchMethodException nsmex) {
          JOptionPane.showMessageDialog(null, failureMessage, failureTitle, JOptionPane.ERROR_MESSAGE);
        } catch(SecurityException sex) {
          JOptionPane.showMessageDialog(null, failureMessage, failureTitle, JOptionPane.ERROR_MESSAGE);
        } catch(InstantiationException iex) {
          JOptionPane.showMessageDialog(null, failureMessage, failureTitle, JOptionPane.ERROR_MESSAGE);
        } catch(IllegalAccessException iaex) {
          JOptionPane.showMessageDialog(null, failureMessage, failureTitle, JOptionPane.ERROR_MESSAGE);
        } catch(IllegalArgumentException iaex) {
          JOptionPane.showMessageDialog(null, failureMessage, failureTitle, JOptionPane.ERROR_MESSAGE);
        } catch(InvocationTargetException itex) {
          JOptionPane.showMessageDialog(null, failureMessage, failureTitle, JOptionPane.ERROR_MESSAGE);
        }
      } else if (! configLAFName.equals(currLAFName)) {
        UIManager.setLookAndFeel(configLAFName);
      }
      
      // The MainFrame *must* be constructed after the compiler setup process has
      // occurred; otherwise, the list of compilers in the UI will be wrong.
      
//      Utilities.showDebug("Creating MainFrame");
      
      try { _mainFrame = new MainFrame(); }
      catch(RuntimeException e) {
        Throwable c = e.getCause();
        if ((c instanceof java.rmi.server.ExportException) && (c.getMessage().equals("Listen failed on port: 0"))) {
          JOptionPane.showMessageDialog(null,
                                        "DrJava could not communicate with its Interactions Pane.\n"+
                                        "This can happen if a firewall does not allow DrJava to access\n"+
                                        "the network. If you have a firewall, please make sure that\n"+
                                        "DrJava has network access.\n"+
                                        "DrJava does not access the internet or other computers, except\n"+
                                        "for \"Open Java API Javadoc\", but it needs network access to\n"+
                                        "communicate with the Interactions Pane.",
                                        "Network Access Failed",
                                        JOptionPane.ERROR_MESSAGE);
          System.exit(1);
        }
        else throw e;
      }    
//      Utilities.showDebug("MainFrame created");
      
      // Make sure all uncaught exceptions are shown in an DrJavaErrorHandler
      DrJavaErrorWindow.setFrame(_mainFrame);
      System.setProperty("sun.awt.exception.handler", "edu.rice.cs.drjava.ui.DrJavaErrorHandler");
      
      // false means "do not jump to the line number that may be specified, just open the file"
      _openCommandLineFiles(_mainFrame, filesToOpen, numFiles, false);
      
      /* We use EventQueue.invokeLater rather than Utilities.invokeLater to ensure all files have been loaded and
       * added to the fileview before the MainFrame is set visible.  When this was not done, we occasionally encountered
       * a NullPointerException on start up when specifying a file (ex: java -jar drjava.jar somefile.java)
       */
      EventQueue.invokeLater(new Runnable(){ 
        public void run(){ 
          _mainFrame.start();
          if (anyLineNumbersSpecified) { // this time, we do want to jump to the line number
            _openCommandLineFiles(_mainFrame, filesToOpen, numFiles, true);
          }
        } 
      });
      
      // redirect stdout to DrJava's console
      System.setOut(new PrintStream(new OutputStreamRedirector() {
        public void print(String s) { _mainFrame.getModel().systemOutPrint(s); }
      }));
      
      // redirect stderr to DrJava's console
      System.setErr(new PrintStream(new OutputStreamRedirector() {
        public void print(String s) { _mainFrame.getModel().systemErrPrint(s); }
      }));
      
//      Utilities.showDebug("showDebugConsole flag = " + DrJava.getShowDebugConsole());
      // Show debug console if enabled
      if (DrJava.getShowDebugConsole()) showDrJavaDebugConsole(_mainFrame);
    }
    catch(Throwable t) {
      // Show any errors to the real System.err and in an DrJavaErrorHandler
      _consoleErr.println(t.getClass().getName() + ": " + t.getMessage());
      t.printStackTrace(_consoleErr);
      System.out.println("error thrown");
      new DrJavaErrorHandler().handle(t);
    }
  }
  
  /** Handle the list of files specified on the command line.  Feature request #509701.
    * If file exists, open it in DrJava.  Otherwise, ignore it.
    * Is there a better way to handle nonexistent files?  Dialog box, maybe?
    */
  static void openCommandLineFiles(final MainFrame mf, final String[] filesToOpen, boolean jump) { 
    openCommandLineFiles(mf, filesToOpen, filesToOpen.length, jump);
  }
  
  /** Handle the list of files specified on the command line.  Feature request #509701. If the final element in 
    * filesToOpen is a pathSeparator, it opens the debug console. If file exists, open it in DrJava.  Otherwise, ignore
    * it.  Is there a better way to handle nonexistent files?  Dialog box, maybe?
    * Why the wait?
    */
  static void openCommandLineFiles(final MainFrame mf, final String[] filesToOpen, final int len, final boolean jump) { 
    Utilities.invokeAndWait(new Runnable() { public void run() { _openCommandLineFiles(mf, filesToOpen, len, jump); }});
  }
  
  private static void _openCommandLineFiles(final MainFrame mf, String[] filesToOpen, int len, boolean jump) {
    assert EventQueue.isDispatchThread();
//    Utilities.showDebug("Files to open: " + Arrays.toString(filesToOpen));
    anyLineNumbersSpecified = false;
    for (int i = 0; i < len; i++) {
      String currFileName = filesToOpen[i];
      
      // check if the file contains a line number
      // the line number can be specified at the end of the file name,
      // separated by File.pathSeparator
      int lineNo = -1;
      int pathSepIndex = currFileName.indexOf(File.pathSeparatorChar);
      if (pathSepIndex>=0) {
        try {
          lineNo = Integer.valueOf(currFileName.substring(pathSepIndex+1));
          anyLineNumbersSpecified = true;
        }
        catch(NumberFormatException nfe) { lineNo = -1; }
        currFileName = currFileName.substring(0,pathSepIndex);
      }
      
      boolean isProjectFile = currFileName.endsWith(OptionConstants.PROJECT_FILE_EXTENSION) ||
        currFileName.endsWith(OptionConstants.OLD_PROJECT_FILE_EXTENSION);
      final File file = new File(currFileName).getAbsoluteFile();
      FileOpenSelector command = new FileOpenSelector() {
        public File[] getFiles() { return new File[] {file}; }
      };
      try {
        if (isProjectFile) mf.openProject(command);
        else if (currFileName.endsWith(OptionConstants.EXTPROCESS_FILE_EXTENSION)) MainFrame.openExtProcessFile(file);
        else {
          if (jump && (lineNo >= 0)) {
            /* if a line number has been specified, open the file using MainFrame.open and jump to lineNo using 
             * MainFrame._jumpToLine.  Note: this can only be done after MainFrame.start() has been called. */
            mf.open(command);
            mf._jumpToLine(lineNo); 
          }
          else {
            // without line number, use the model's openFile.
            mf.getModel().openFile(command);
          }
        }
      }
      
      catch (FileNotFoundException ex) {
        // TODO: show a dialog? (file not found) (mgricken)
      }
      catch (SecurityException se) {
        // TODO: show a dialog? (file not found) (mgricken)
      }
      catch (AlreadyOpenException aoe) {
        // This explicitly does nothing to ignore duplicate files.
      }
      catch (FileMovedException aoe) {
        // This explicitly does nothing to ignore duplicate files.
      }
      catch (IOException ex) {
        // TODO: show a dialog? (file not found) (mgricken)
      }
      catch (Exception ex) { throw new UnexpectedException(ex); }
    }
  }
  
  /** Shows a separate interactions window with a reference to DrJava's MainFrame defined as "mainFrame".  
    * Useful for debugging DrJava.
    * @param mf MainFrame to define in the new window
    */
  public static void showDrJavaDebugConsole(MainFrame mf) {
    if (_debugConsole == null) {
      _debugConsole = new SimpleInteractionsWindow("DrJava Debug Console") {
        protected void close() {
          dispose();
          _debugConsole = null;
        }
      };
      // TODO: define appropriate context
//      _debugConsole.defineConstant("mainFrame", mf);
//      _debugConsole.defineConstant("model", mf.getModel());
//      _debugConsole.defineConstant("config", DrJava.getConfig());
      _debugConsole.setInterpreterPrivateAccessible(true);
      _debugConsole.setVisible(true);
    }
    else  _debugConsole.toFront();
  }
  
  /** Get the actual System.err stream.
    * @return System.err
    */
  public static PrintStream consoleErr() { return  _consoleErr; }
  
  /** Get the actual System.out stream.
    * @return System.out
    */
  public static PrintStream consoleOut() { return  _consoleOut; }
  
  /** User dragged something into the component. */
  public static void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
    _mainFrame.dragEnter(dropTargetDragEvent);
  }
  
  /** User dropped something on the component. */
  public static void drop(DropTargetDropEvent dropTargetDropEvent) {
    _mainFrame.drop(dropTargetDropEvent);
  }

  /** Installs the modal window adapter if available, otherwise installs a non-modal dummy listener.
    * @param w window trying to get the modal window listener
    * @param toFrontAction action to be performed after the window has been moved to the front again
    * @param closeAction action to be performed when the window is closing
    */
  public static void installModalWindowAdapter(final Window w,
                                               final Runnable1<? super WindowEvent> toFrontAction,
                                               final Runnable1<? super WindowEvent> closeAction) {
    _mainFrame.installModalWindowAdapter(w, toFrontAction, closeAction);
  }
  
  /** Removes the modal window adapter.
    * @param w window releasing the modal window adapter */
  public static void removeModalWindowAdapter(Window w) {
    _mainFrame.removeModalWindowAdapter(w);
  }
}

