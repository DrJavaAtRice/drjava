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

package edu.rice.cs.drjava;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.jar.JarFile;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.*;

import edu.rice.cs.drjava.ui.MainFrame;
import edu.rice.cs.drjava.ui.SplashScreen;
import edu.rice.cs.drjava.ui.ClasspathFilter;
import edu.rice.cs.drjava.ui.AWTExceptionHandler;
import edu.rice.cs.drjava.ui.SimpleInteractionsWindow;
import edu.rice.cs.util.PreventExitSecurityManager;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.OutputStreamRedirector;
import edu.rice.cs.util.newjvm.ExecJVM;
import edu.rice.cs.util.classloader.ToolsJarClassLoader;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.drjava.config.*;

/** 
 * Main class for DrJava. 
 * @version $Id$
 */
public class DrJava implements OptionConstants {
  /** Class to probe to see if the debugger is available */
  public static final String TEST_DEBUGGER_CLASS = "com.sun.jdi.Bootstrap";
  
  private static final PrintStream _consoleOut = System.out;
  private static final PrintStream _consoleErr = System.err;
  private static PreventExitSecurityManager _manager;
  private static String[] _filesToOpen = new String[0];
  private static boolean _attemptingAugmentedClasspath = false;
  private static boolean _showDrJavaDebugConsole = false;
  private static boolean _usingJSR14v20 = false;
  private static SimpleInteractionsWindow _debugConsole = null;

  /*
   * Config objects can't be public static final, since we have to delay 
   * construction until we know the config file's location.  (Might be 
   * specified on command line.)
   * Instead, use accessor methods to prevent others from assigning new values.
   */
  
  /**
   * Properties file used by the configuration object.
   * Defaults to ".drjava" in the user's home directory.
   */
  private static File _propertiesFile = 
    new File(System.getProperty("user.home"), ".drjava");
  
  /**
   * Configuration object with all customized and default values.
   */
  private static FileConfiguration _config = null;

  /**
   * Returns the properties file used by the configuration object.
   */
  public static File getPropertiesFile() {
    return _propertiesFile;
  }
  
  /**
   * Returns the configuration object with all customized and default values.
   */
  public static synchronized FileConfiguration getConfig() {
    // Ensure config has been created (eg. in a test)
    if (_config == null) {
      initConfig();
    }
    
    return _config;
  }
  
  
  /**
   * Starts running DrJava.  Not done in the actual main method so a
   * custom class loader can be used.
   * @param args Command line argument array
   */
  //public static void beginProgram(final String[] args) {
  public static void main(final String[] args) {
    try {
      // handleCommandLineArgs will return true if the program should load
      if (handleCommandLineArgs(args)) {
        
        try {
          initConfig();
        }
        catch (IllegalStateException ise) {
          // Shouldn't happen: _config shouldn't be assigned yet
          throw new UnexpectedException(ise);
        }

        String configLAFName = _config.getSetting(LOOK_AND_FEEL);
        String currLAFName = UIManager.getLookAndFeel().getClass().getName();
        if (!configLAFName.equals(currLAFName)) {
          UIManager.setLookAndFeel(configLAFName);
        }

        _usingJSR14v20 = checkForJSR14v20();

        checkForCompilersAndDebugger(args);

        // Show splash screen
        SplashScreen splash = new SplashScreen();
        splash.show();
      
        // The MainFrame *must* be constructed after the compiler setup process has
        // occurred; otherwise, the list of compilers in the UI will be wrong.
        // At some point this should be fixed, which would involve making the
        // CompilerRegistry notify listeners when there is a change in the list of
        // available compilers.
        final MainFrame mf = new MainFrame();
        
        // Make sure all uncaught exceptions are shown in an AWTExceptionHandler
        AWTExceptionHandler.setFrame(mf);
        System.setProperty("sun.awt.exception.handler", 
                           "edu.rice.cs.drjava.ui.AWTExceptionHandler");
        
        // This enabling of the security manager must happen *after* the mainframe
        // is constructed. See bug #518509.
        enableSecurityManager();

        openCommandLineFiles(mf, _filesToOpen);
        splash.dispose();
        mf.show();


        // redirect stdout to DrJava's console
        System.setOut(new PrintStream(new OutputStreamRedirector() {
          public void print(String s) {
            mf.getModel().systemOutPrint(s);
          }
        }));
        
        // redirect stderr to DrJava's console
        System.setErr(new PrintStream(new OutputStreamRedirector() {
          public void print(String s) {
            mf.getModel().systemErrPrint(s);
          }
        }));
        
        // Show debug console if enabled
        if (_showDrJavaDebugConsole) {
          showDrJavaDebugConsole(mf);
        }
      }
        
    } 
    catch (Throwable t) {
      // Show any errors to the real System.err and in an AWTExceptionHandler
      _consoleErr.println(t.getClass().getName() + ": " + t.getMessage());
      t.printStackTrace(_consoleErr);
      new AWTExceptionHandler().handle(t);
    }
  }

  /**
   * Handles any command line arguments that have been specified.
   * @return true if DrJava should load, false if not
   */
  static boolean handleCommandLineArgs(String[] args) {
    int firstFile = 0;
    
    // Loop through arguments looking for known options
    for (int i=0; i < args.length; i++) {
      if (args[i].equals("-config")) {
        if (args.length > i+1) {
          setPropertiesFile(args[i+1]);
          firstFile = i+2;
        }
        else {
          firstFile = i+1;
        }
      }
      
      else if (args[i].equals("-attemptingAugmentedClasspath")) {
        _attemptingAugmentedClasspath = true;
      }
      
      else if (args[i].equals("-debugConsole")) {
        _showDrJavaDebugConsole = true;
      }
      
      else if (args[i].equals("-help") || args[i].equals("-?")) {
        displayUsage();
        return false;
      }
    }
    
    // Open the rest as filenames
    int numFiles = args.length - firstFile;
    _filesToOpen = new String[numFiles];
    System.arraycopy(args, firstFile, _filesToOpen, 0, numFiles);
    
    return true;
  }
  
  /**
   * Displays a usage message about the available options.
   */
  static void displayUsage() {
    StringBuffer buf = new StringBuffer();
    buf.append("Usage: java -jar drjava.jar [OPTIONS] [FILES]\n\n");
    buf.append("where options include:\n");
    buf.append("  -config [FILE]        to use a custom config file\n");
    buf.append("  -help | -?            print this help message\n");
    _consoleOut.print(buf.toString());
  }
  
  /**
   * Switches the config object to use a custom config file.
   * Ensures that Java source files aren't accidentally used.
   */
  static void setPropertiesFile(String filename) {
    if (!filename.endsWith(".java")) {
      _propertiesFile = new File(filename);
    }
  }
  
  
  /**
   * Initializes the configuration object with the current
   * notion of the properties file.
   * @throws IllegalStateException if config has already been assigned
   */
  static synchronized void initConfig() throws IllegalStateException {
    // Make sure someone doesn't try to change the config object.
    if (_config != null) {
      throw new IllegalStateException("Can only call initConfig once!");
    }
    
    try {
      _propertiesFile.createNewFile();
      // be nice and ensure a config file if there isn't one
    }
    catch (IOException e) {
      // IOException occurred, continue without a real file
    }
    _config = new FileConfiguration(_propertiesFile);
    try {
      _config.loadConfiguration();
    }
    catch (Exception e) {
      // problem parsing the config file.
      // Use defaults and remember what happened (for the UI)
      _config.resetToDefaults();
      _config.storeStartupException(e);
    }
  }
  
  /**
   * Saves the contents of the config file.
   * TO DO: log any IOExceptions that occur
   */
  protected static void _saveConfig() {
    try {
      getConfig().saveConfiguration();
    }
    catch(IOException e) {
      JOptionPane.showMessageDialog(null,
                                    "Could not save the location of tools.jar in \n" +
                                    "the '.drjava' file in your home directory. \n" +
                                    "Another process may be using the file.\n\n" + e,
                                    "Could Not Save Changes",
                                    JOptionPane.ERROR_MESSAGE);  
      // TODO: log this error
    }
  }
  
  
  /**
   * Handle the list of files specified on the command line.  Feature request #509701.
   * If file exists, open it in DrJava.  Otherwise, ignore it.
   * Is there a better way to handle nonexistent files?  Dialog box, maybe?
   */
  static void openCommandLineFiles(MainFrame mf, String[] filesToOpen) {
    for(int i = 0; i < filesToOpen.length; i++) {
      final File file = new File(filesToOpen[i]);
      FileOpenSelector command = new FileOpenSelector() {
        public File getFile() {
          return file;
        }
        public File[] getFiles() {
          return new File[] {file};
        }
      };
      try {
        //OpenDefinitionsDocument doc = 
        mf.getModel().openFile(command);
      }
      catch (FileNotFoundException ex) {
        // TODO: show a dialog? (file not found)
      }
      catch (AlreadyOpenException aoe) {
        // This explicitly does nothing to ignore duplicate files.
      }
      catch (Exception ex) {
        throw new UnexpectedException(ex);
      }
    }
  }
  
  /**
   * Helper method called by checkForJSR14v2 and checkForJSR14v24.
   */
  private static boolean checkJSR14JarForClass(String checkClass, String msg, String title) {
    File jsr14 = _config.getSetting(JSR14_LOCATION);
    
    if (jsr14 != FileOption.NULL_FILE) {
      try {
        JarFile jsr14jar = new JarFile(jsr14);
        if (jsr14jar.getJarEntry(checkClass) != null) {
          // If we're using Java 1.3, don't allow JSR14v2.x
          if (System.getProperty("java.specification.version").equals("1.3")) {
            // Show a warning message, but only if we haven't restarted
            if (!_attemptingAugmentedClasspath) {
              JOptionPane.showMessageDialog(null, msg, title,
                                            JOptionPane.WARNING_MESSAGE);
            }
            return false;
          }
          
          return true;
        }
      }
      catch (IOException ex) {
        // if there was an error loading the jar file, just return false
      }

    }
    // either caught an exception thrown by the classloader or had no jsr14 specified.
    // Therefore, not using jsr14 v2.x.
    return false;
  }
  
  /**
   * Try to determine if the preferences specify jsr14 v2.4, so that we can
   * restart with the proper boot classpath.
   */
  public static boolean checkForJSR14v24() {
    String fs = "/"; // In jar files, the file separator is always '/'
    String checkClass = "com" + fs + "sun" + fs + "javadoc" + fs + "ParameterizedType.class";
    String msg = "The JSR-14 v2.4 compiler is only compatible with JDK 1.4.2.\n" +
      "It will not be available in your list of compilers.";
    String title = "Cannot Load JSR-14 v2.4";
    return checkJSR14JarForClass(checkClass, msg, title);
  }
  

  /**
   * Try to determine if the preferences specify jsr14 v2.0, so that we can
   * restart with the proper boot classpath.
   */
  public static boolean checkForJSR14v20() {
    String fs = "/"; // In jar files, the file separator is always '/'
    String checkClass = "com" + fs + "sun" + fs + "tools" + fs + "javac" + fs + "comp" + fs + "Check.class";
    String msg = "The JSR-14 v2.x compiler is not compatible with JDK 1.3.\n" +
      "It will not be available in your list of compilers.";
    String title = "Cannot Load JSR-14 v2.x";
    return checkJSR14JarForClass(checkClass, msg, title);
  }

  /**
   * Check to see if a compiler and the debugger are available.  If necessary,
   * starts DrJava in a new JVM with an augmented classpath to make these 
   * available.  If it can't find them at all, it prompts the user to 
   * optionally specify tools.jar
   * @param args Command line argument array, in case we need to restart
   */
  static void checkForCompilersAndDebugger(String[] args) {
    if (_attemptingAugmentedClasspath) {
      // We're on our second attempt-- just load DrJava
      return;
    }
    
    boolean restartForToolsJar = false;

    // Try to make sure both compiler and debugger are available
    if (hasAvailableCompiler()) {
      
      if (hasAvailableDebugger()) {
        // Everything is already on the classpath; start normally
        restartForToolsJar = false;
      }
      else if (classLoadersCanFindDebugger()) {
        // We know where tools.jar is, so restart with it on the classpath
        restartForToolsJar = true;
      }
      else {
        // Have a compiler (probably JSR14) but can't find JDI classes...
        // Prompt user for debugger (in tools.jar)
        restartForToolsJar = promptForToolsJar(false, true);
      }
    }
    else {
      
      if (hasAvailableDebugger()) {
        // Debugger but no compiler => probably jpda on classpath.
        // Prompt user for compiler (in tools.jar)
        promptForToolsJar(true, false);
        // don't need to restart for tools.jar
      }
      else if (classLoadersCanFindDebugger()) {
        // Debugger if we restart, but no compiler => jpda in prefs?
        // Prompt use for compiler (in tools.jar)
        promptForToolsJar(true, false);
        restartForToolsJar = true;
      }
      else {
        // No debugger or compiler
        // Prompt user for tools.jar
        restartForToolsJar = promptForToolsJar(true, true);
      }
    }
    
    // Originally this also took in a flag if it was necessary to
    // restart to be able to use JSR-14 on OS X.  That is no longer
    // necessary, but I'll leave the contract like this for the time
    // being (in case another condition comes up).
    restartIfNecessary(restartForToolsJar, args);
  }
  
  /**
   * Returns whether the CompilerRegistry has been able to load a compiler.
   */
  public static boolean hasAvailableCompiler() {
    return !CompilerRegistry.ONLY.isNoCompilerAvailable();
  }
  
  /**
   * Returns whether the debugger will be able to load successfully.
   * Checks for the ability to load the com.sun.jdi.Bootstrap class.
   */
  public static boolean hasAvailableDebugger() {
    try {
      Class.forName(TEST_DEBUGGER_CLASS);
      return true;
    }
    catch (ClassNotFoundException cnfe) {
      return false;
    }
    catch (UnsupportedClassVersionError ucve) {
      return false;
    }
  }
  
  /**
   * Returns whether the debugger will be able to load successfully
   * if we restart with our notion of tools.jar on the classpath.
   * Uses ToolsJarClassLoader to try to load com.sun.jdi.Bootstrap.
   */
  public static boolean classLoadersCanFindDebugger() {
    // First check the specified location
    File jar = getConfig().getSetting(JAVAC_LOCATION);
    if (jar != FileOption.NULL_FILE) {
      try {
        URL[] urls = new URL[] { jar.toURL() };
        URLClassLoader loader = new URLClassLoader(urls);
        loader.loadClass(TEST_DEBUGGER_CLASS);
        return true;
      }
      catch (ClassNotFoundException e) {
        // no debugger in this jar file; try ToolsJarClasLoader
      }
      catch (UnsupportedClassVersionError ucve) {
        return false;
      }
      catch (MalformedURLException e) {
        // specified jar invalid; try ToolsJarClassLoader
      }
    }

    // If not, try to guess tools.jar location
    ToolsJarClassLoader loader = new ToolsJarClassLoader();
    try {
      loader.loadClass("com.sun.jdi.Bootstrap");
      return true;
    }
    catch (ClassNotFoundException cnfe) {
      return false;
    }
    catch (UnsupportedClassVersionError ucve) {
      return false;
    }
  }
  
  public static boolean bootClasspathHasJSR14v24() {
    try {
      Class.forName("com.sun.javadoc.ParameterizedType");
      return true;
    }
    catch (Throwable t) {
      return false;
    }
  }

  /**
   * helper method to determine if jsr14 v2.0 jar is on the boot classpath.
   * @return true iff java.lang.Enum can be loaded successfully.
   */
  public static boolean bootClasspathHasJSR14v20() {
    try {
      Class.forName("java.lang.Enum");
      return true;
    }
    catch (Throwable t) {
      // failed to load java.lang.Enum, so jsr14 v2.0 is not on the boot claspath.
      // This logic avoids a restart if _usingJSR14v20 with the correct boot classpath.
      //t.printStackTrace();
      return false;
    }
  }
  
  /**
   * Tries to run a new DrJava process with our notion of tools.jar
   * appended to the end of the classpath.  This should allow us to
   * always make the debugger available.
   *
   * Note: this used to take in a flag for JSR-14 in addition to tools.jar,
   * but that isn't needed anymore.  I'll leave the contract like this in 
   * case another condition becomes necessary.  For now, it just returns
   * if you pass in false for the first argument.
   * Also, this function now restarts when using jsr14 v2.0, since it has to be
   * on the JVM's boot classpath.
   *
   * @param forToolsJar Whether to restart DrJava to find tools.jar
   * @param args Array of command line arguments to pass
   */
  public static void restartIfNecessary(boolean forToolsJar, String[] args) {
    //JOptionPane.showMessageDialog(null, "forToolsJar = " + forToolsJar);
    if (!forToolsJar) {
      if (!_usingJSR14v20 || bootClasspathHasJSR14v20()) {
        return;
      }
    }

    //System.out.println("restarting with debugger...");

    String pathSep = System.getProperty("path.separator");
    String classpath = System.getProperty("java.class.path");
    
    // Class arguments
    String[] classArgs = new String[args.length + 1];
    System.arraycopy(args, 0, classArgs, 0, args.length);
    classArgs[args.length] = "-attemptingAugmentedClasspath";
    
    // JVM arguments
    String[] jvmArgs;
    
    if (_usingJSR14v20) {
      //System.out.println("Using JSR14v20, appending bootclasspath");
      String jsr14 = _config.getSetting(JSR14_LOCATION).getAbsolutePath();
      jvmArgs = new String[] { "-Xbootclasspath/p:" + jsr14 };
    }
    else {
      jvmArgs = new String[0];
    }
    
    if (forToolsJar) {
      // Try to restart with tools.jar on the classpath
      classpath += pathSep;
    
      // Add tools.jar from preferences if specified
      File toolsFromConfig = getConfig().getSetting(JAVAC_LOCATION);
      if (toolsFromConfig != FileOption.NULL_FILE) {
        classpath += toolsFromConfig.getAbsolutePath() + pathSep;
      }
      
      // Fall back on guesses from ToolsJarClassLoader
      classpath += ToolsJarClassLoader.getToolsJarClasspath();
    }
    
    // Run a new copy of DrJava and exit
    try {
      ExecJVM.runJVM("edu.rice.cs.drjava.DrJava", classArgs, 
                     classpath, jvmArgs);
      System.exit(0);
    }
    catch (IOException ioe) {
      // Display error
      final String[] text = {
        "DrJava was unable to load its debugger.  Would you ",
        "like to start DrJava without a debugger?",
        "\nReason: " + ioe.toString()
      };
      int result = JOptionPane.showConfirmDialog(null, text,
                                                 "Could Not Load Debugger",
                                                 JOptionPane.YES_NO_OPTION);
      if (result != JOptionPane.YES_OPTION) {
        System.exit(0);
      }
    }
  }
  
  public static boolean usingJSR14v20() {
    return _usingJSR14v20;
  }

  /**
   * Prompts the user that the location of tools.jar needs to be
   * specified to be able to use the compiler and/or the debugger.
   * Returns whether it will be necessary to restart after the
   * user's response in order to make the debugger available on
   * the classpath.
   * @param needCompiler whether DrJava needs tools.jar for a compiler
   * @param needDebugger whether DrJava needs tools.jar for the debugger
   * @return whether we will need to restart to put tools.jar on classpath
   */
  public static boolean promptForToolsJar(boolean needCompiler,
                                          boolean needDebugger) {
    boolean restartRequired = false;
    final String[] text = {
      "DrJava cannot find the Java SDK's 'tools.jar' file. ",
      "This file is necessary to compile files and use the ",
      "debugger.  It is generally located in the 'lib' ",
      "subdirectory of your Java installation directory. ",
      "Would you like to specify its location? ",
      "(If you say 'No', DrJava might be unable to compile ",
      "or debug programs.)"
    };
    
    int result = JOptionPane.showConfirmDialog(null, text, "Locate 'tools.jar'?",
                                               JOptionPane.YES_NO_OPTION);
    
    if (result == JOptionPane.YES_OPTION) {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileFilter(new ClasspathFilter() {
        public boolean accept(File f) {
          if (f.isDirectory()) {
            return true;
          }
          String ext = getExtension(f);
          return ext != null && ext.equals("jar");
        }
        public String getDescription() {
          return "Jar Files";
        }
      });
      
      // Loop until we find a good tools.jar or the user gives up
      do {
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          File jar = chooser.getSelectedFile();
          
          if (jar != null) {
            // set the tools.jar property
            getConfig().setSetting(JAVAC_LOCATION, jar);
            
            // Adjust if we needed a compiler
            if (needCompiler) {
              // need to re-call getAvailable for it to re-check availability
              CompilerInterface[] compilers
                = CompilerRegistry.ONLY.getAvailableCompilers();
              
              if (compilers[0] != NoCompilerAvailable.ONLY) {
                needCompiler = false;
                CompilerRegistry.ONLY.setActiveCompiler(compilers[0]);
                _saveConfig();
              }
            }
            
            // Adjust if we need a debugger
            if (needDebugger) {
              if (classLoadersCanFindDebugger()) {
                needDebugger = false;
                restartRequired = true;
                _saveConfig();
              }
            }
          }
        }
      }
      while ((needCompiler || needDebugger) && _userWantsToPickAgain());
    }
    
    return restartRequired;
  }
  
  /**
   * Displays a prompt to the user indicating that tools.jar could not be
   * found in the specified location, and asks if he would like to specify
   * a new location.
   */
  private static boolean _userWantsToPickAgain() {
    final String[] text = {
      "The file you chose did not appear to be a valid 'tools.jar'. ",
      "(Your choice might be an incompatible version of the file.) ",
      "Would you like to pick again?  The 'tools.jar' file is ",
      "generally located in the 'lib' subdirectory under your ",
      "JDK installation directory.",
      "(If you say 'No', DrJava might be unable to compile or ",
      "debug programs.)"
    };
    
    int result = JOptionPane.showConfirmDialog(null,
                                               text,
                                               "Locate 'tools.jar'?",
                                               JOptionPane.YES_NO_OPTION);
    
    return result == JOptionPane.YES_OPTION;
  }
  
  /**
   * Shows a separate interactions window with a reference to DrJava's
   * MainFrame defined as "mainFrame".  Useful for debugging DrJava.
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
      _debugConsole.defineConstant("mainFrame", mf);
      _debugConsole.defineConstant("model", mf.getModel());
      _debugConsole.defineConstant("config", _config);
      _debugConsole.setInterpreterPrivateAccessible(true);
      _debugConsole.show();
    }
    else {
      _debugConsole.toFront();
    }
  }

  public static PreventExitSecurityManager getSecurityManager() {
    return _manager;
  }
  
  public static void enableSecurityManager() {
    if (_manager == null) {
      _manager = PreventExitSecurityManager.activate();
    }
    
    if (System.getSecurityManager() != _manager) {
      System.setSecurityManager(_manager);
    }
  }
  
  public static void disableSecurityManager() {
    _manager.deactivate();
  }
  
  /**
   * Get the actual System.err stream.
   * @return System.err
   */
  public static PrintStream consoleErr() {
    return  _consoleErr;
  }
  
  /**
   * Get the actual System.out stream.
   * @return System.out
   */
  public static PrintStream consoleOut() {
    return  _consoleOut;
  }
}

