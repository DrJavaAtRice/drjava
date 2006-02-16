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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import javax.swing.UIManager;
import javax.swing.*;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.OutputStreamRedirector;
import edu.rice.cs.util.newjvm.ExecJVM;
import edu.rice.cs.util.classloader.ToolsJarClassLoader;
import edu.rice.cs.util.swing.Utilities;

import edu.rice.cs.drjava.ui.MainFrame;
import edu.rice.cs.drjava.ui.SplashScreen;
import edu.rice.cs.drjava.ui.ClassPathFilter;
import edu.rice.cs.drjava.ui.AWTExceptionHandler;
import edu.rice.cs.drjava.ui.SimpleInteractionsWindow;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.drjava.config.FileConfiguration;
import edu.rice.cs.drjava.config.*;

import static edu.rice.cs.drjava.config.OptionConstants.*;

/** Startup class for DrJava.  The main method reads the .drjava file (creating one if none exists) to get the
 *  critical information required to start the main JVM for DrJava: 
 *  (i) the location of tools.jar in the Java JDK installed on this machine (so DrJava can invoke the javac compiler
 *      stored in tools.jar)
 *  (ii) the argument string for invoking the main JVM (notably -X options used to determine maximum heap size, etc.)
 *  This version of DrJava no longer supports the transitional JSR-14 compilers or the GJ compiler.
 *  @version $Id$
 */
public class DrJava {
  
  
  /** Class to probe to see if the debugger is available */
  public static final String TEST_DEBUGGER_CLASS = "com.sun.jdi.Bootstrap";
  /** Class to probe to see if the compiler is available */
  public static final String TEST_COMPILER_CLASS = "com.sun.tools.javac.main.JavaCompiler";
  
  /** Pause time for displaying DrJava banner on startup (in milliseconds) */
  private static final int PAUSE_TIME = 2000;
  
//  /** This field is only used in the instance of this class in the Interpreter JVM. */
//  private static PreventExitSecurityManager _manager = null;
  
  private static ArrayList<String> _filesToOpen = new ArrayList<String>();
  private static ArrayList<String> _jmvArgs = new ArrayList<String>();

  private static boolean _showDebugConsole = false;
  
  /* Config objects can't be public static final, since we have to delay construction until we know the 
   * config file's location.  (Might be specified on command line.) Instead, use accessor methods to 
   * prevent others from assigning new values. */

  /** Properties file used by the configuration object. Defaults to ".drjava" in the user's home directory. */
  private static File _propertiesFile = new File(System.getProperty("user.home"), ".drjava");
  
  /** Configuration object with all customized and default values.  Initialized from _propertiesFile.  */
  private static FileConfiguration _config = _initConfig();
  
  private static ToolsJarClassLoader _toolsLoader = new ToolsJarClassLoader(getConfig().getSetting(JAVAC_LOCATION));
  private static ClassLoader _thisLoader = DrJava.class.getClassLoader();

  /** Returns the properties file used by the configuration object. */
  public static File getPropertiesFile() { return _propertiesFile; }

  /** Returns the configuration object with all customized and default values. */
  public static FileConfiguration getConfig() { return _config; }

  /** Starts running DrJava.
   *  @param args Command line argument array
   */
  public static void main(final String[] args) {
    
    final SplashScreen splash = new SplashScreen();
    splash.setVisible(true);
    splash.repaint();
//    Utilities.showDebug("Calling configureAndLoadDrJavaRoot with args = " + args);
    configureAndLoadDrJavaRoot(args); 
    
    SwingUtilities.invokeLater(new Runnable() { 
      public void run() { 
        try { Thread.currentThread().sleep(PAUSE_TIME); }
        catch(InterruptedException e) { }
        splash.dispose(); 
      }});
  }
  
  public static void configureAndLoadDrJavaRoot(String[] args) {
    try {
      // handleCommandLineArgs will return true if the DrJava should be loaded
      if (handleCommandLineArgs(args)) {
        
        // Check that compiler and debugger are available on classpath (including tools.jar location)
        checkForCompilersAndDebugger(args);
        
        // Start the DrJava master JVM
        String pathSep = System.getProperty("path.separator");
        String classPath = edu.rice.cs.util.FileOps.convertToAbsolutePathEntries(System.getProperty("java.class.path"));
        
/* The following has been subsumed by toolsFromConfig argument passed to getToolsJarClassPath */        
//        // Add tools.jar from preferences if specified
//        classPath += pathSep;
//        File toolsFromConfig = getConfig().getSetting(JAVAC_LOCATION);
//        if (toolsFromConfig != FileOption.NULL_FILE) {
//          classPath += toolsFromConfig.getAbsolutePath() + pathSep;
//        }
        
        // Fall back on guesses from ToolsJarClassLoader
        File toolsFromConfig = getConfig().getSetting(JAVAC_LOCATION);
        classPath += pathSep + ToolsJarClassLoader.getToolsJarClassPath(toolsFromConfig);
        
        File workDir = getConfig().getSetting(WORKING_DIRECTORY);
        if (workDir == null) workDir = FileOption.NULL_FILE;
        
        // Add the string pathSep to _filesToOpen if _showDebugConsole is true
        if (_showDebugConsole) _filesToOpen.add(pathSep);  // THIS IS A KLUDGE TO PASS THIS BOOLEAN FLAG TO DrJava
        
        String[] jvmArgs = _jmvArgs.toArray(new String[0]);
        String[] classArgs = _filesToOpen.toArray(new String[0]);
        
        // Run a new copy of DrJava and exit
        try {
//          Utilities.showDebug("Starting DrJavaRoot with classArgs = " + Arrays.toString(classArgs) + "; classPath = " + classPath + 
//                             "; jvmArgs = " + Arrays.toString(jvmArgs) + "; workDir = " + workDir);
          ExecJVM.runJVM("edu.rice.cs.drjava.DrJavaRoot", classArgs, classPath, jvmArgs, workDir);
        }
        catch (IOException ioe) {
          // Display error
          final String[] text = {
            "DrJava was unable to load its compiler and debugger.  Would you ",
            "like to start DrJava without a compiler and debugger?", "\nReason: " + ioe.toString()
          };
          int result = JOptionPane.showConfirmDialog(null, text, "Could Not Load Compiler and Debugger",
                                                     JOptionPane.YES_NO_OPTION);
          if (result != JOptionPane.YES_OPTION) { System.exit(0); }
        }
      }
    }
    catch (Throwable t) {
      // Show any errors to the System.err and in an AWTExceptionHandler
      System.err.println(t.getClass().getName() + ": " + t.getMessage());
      t.printStackTrace(System.err);System.out.println("error thrown");
      new AWTExceptionHandler().handle(t);
    }
  }
  
  /** Handles any command line arguments that have been specified.
   *  @return true if DrJava should load, false if not
   */
  static boolean handleCommandLineArgs(String[] args) {
    
    // Loop through arguments looking for known options
    int firstFile = 0;
    int len = args.length;
    _filesToOpen = new ArrayList<String>();
    
    for (int i = 0; i < len; i++) {
      String arg = args[i];
      
      if (arg.equals("-config")) {
        if (len == i + 1) { 
          // config option is missing file name; should we generate an error?
          return true;
        }
        
        // arg.length > i+1 implying args list incudes config file name and perhaps files to open
        setPropertiesFile(args[i + 1]);
        firstFile = i + 2;
        _config = _initConfig();  // read specified .djrava file into _config
      }
      else if ((arg.length() > 1) && (arg.substring(0,2).equals("-X"))) _jmvArgs.add(arg); 
      
      else if (arg.equals("-debugConsole")) _showDebugConsole = true;
      
      else if (arg.equals("-help") || arg.equals("-?")) {
        displayUsage();
        return false;
      }
      else {
        firstFile = i;
        break;
      }
    }

    // Open the remaining args as filenames

    for (int i = firstFile; i < len; i++) _filesToOpen.add(args[i]);
    return true;
  }

  /** Displays a usage message about the available options. */
  static void displayUsage() {
    StringBuffer buf = new StringBuffer();
    buf.append("Usage: java -jar drjava.jar [OPTIONS] [FILES]\n\n");
    buf.append("where options include:\n");
    buf.append("  -config [FILE]        use a custom config file\n");
    buf.append("  -help | -?            print this help message\n");
    buf.append("  -X<jvmOption>         specify a JVM configuration option for the master DrJava JVM\n");      
    System.out.print(buf.toString());
  }
  
   /** Check to see if a compiler and the debugger are available in a tools.jar file.  If it can't find them, it prompts
    *  the user to optionally specify the location of a propert tools.jar file.
    *  @param args Command line argument array, in case we need to restart
   */
  static void checkForCompilersAndDebugger(String[] args) {
    
    boolean needCompiler = ! hasAvailableCompiler();
    boolean needDebugger = ! hasAvailableDebugger();

    // Try to make sure both compiler and debugger are available
    if (needCompiler || needDebugger) promptForToolsJar(needCompiler, needDebugger);
  }

  /** Returns whether the debugger will be able to load successfully.  Checks for the ability to load the 
   *  com.sun.jdi.Bootstrap class.
   */
  public static boolean hasAvailableDebugger() {
    return canLoad(_thisLoader, TEST_DEBUGGER_CLASS) || canLoad(_toolsLoader, TEST_DEBUGGER_CLASS);
  }
  
  public static boolean hasAvailableCompiler() {
   return canLoad(_thisLoader, TEST_COMPILER_CLASS) || canLoad(_toolsLoader, TEST_COMPILER_CLASS);
  }
  
  /* Tests whether the specified class loader can load the specifed class */
   public static boolean canLoad(ClassLoader cl, String className) {
    try {
      cl.loadClass(className);
      return true;
    }
    catch(ClassNotFoundException e) { return false; }
    catch(RuntimeException e) { return false; }
  }
  
  /** Prompts the user that the location of tools.jar needs to be specified to be able to use the compiler and/or the
   *  debugger.  
   *  @param needCompiler whether DrJava needs tools.jar for a compiler
   *  @param needDebugger whether DrJava needs tools.jar for the debugger
   *  @return whether we will need to restart to put tools.jar on classpath
   */
  public static void promptForToolsJar(boolean needCompiler, boolean needDebugger) {
    final String[] text  = new String[] {
      "DrJava cannot find a 'tools.jar' file for the version of Java ",
        "that is being used to run DrJava.  Would you like to specify the ",
        "location of the requisite 'tools.jar' file?   If you say 'No', ",
        "DrJava might be unable to compile or debug Java programs.)"
    };
    
    int result = JOptionPane.showConfirmDialog(null, text, "Locate 'tools.jar'?", JOptionPane.YES_NO_OPTION);

    if (result == JOptionPane.YES_OPTION) {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileFilter(new ClassPathFilter() {
        public boolean accept(File f) {
          if (f.isDirectory()) return true;
          String ext = getExtension(f);
          return ext != null && ext.equals("jar");
        }
        public String getDescription() { return "Jar Files"; }
      });

      // Loop until we find a good tools.jar or the user gives up
      do {
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          File jar = chooser.getSelectedFile();

          if (jar != null) {
            // set the tools.jar property
            getConfig().setSetting(JAVAC_LOCATION, jar);

            // Adjust if we needed a compiler
            if (needCompiler && classLoadersCanFind(TEST_COMPILER_CLASS)) needCompiler = false;

            // Adjust if we need a debugger
            if (needDebugger && classLoadersCanFind(TEST_DEBUGGER_CLASS)) needDebugger = false;
          }
        }
//        Utilities.showDebug("need Compiler = " + needCompiler + "; needDebugger = " + needDebugger);
      }
      while ((needCompiler || needDebugger) && _userWantsToPickAgain());
      
      // Save config with good tools.jar if available
      if ((! needCompiler) && (! needDebugger)) _saveConfig();
    }
  }
  
 
  /** Returns whether the debugger will be able to load successfully when we start the DrJava master JVM with
   *  tools.jar on the classpath. Uses ToolsJarClassLoader to try to load com.sun.jdi.Bootstrap.
   */
  public static boolean classLoadersCanFind(String className) {
    // First check the specified location
    File jar = getConfig().getSetting(JAVAC_LOCATION);
    if (jar != FileOption.NULL_FILE) {
      try {
        URL[] urls = new URL[] { jar.toURL() };
        URLClassLoader loader = new URLClassLoader(urls);
        if (canLoad(loader, className)) return true;
      }
      catch(MalformedURLException e) { /* fall through */ }
    }
    return canLoad(_toolsLoader, className);
  }
    
  /** Switches the config object to use a custom config file. Ensures that Java source files aren't 
   *  accidentally used.
   */
  static void setPropertiesFile(String filename) {
    if (!filename.endsWith(".java"))  _propertiesFile = new File(filename);
  }
  
  /** Initializes the configuration object with the current notion of the properties file.
   *  @throws IllegalStateException if config has already been assigned
   */
  static FileConfiguration _initConfig() throws IllegalStateException {
//    // Make sure someone doesn't try to change the config object.
//    if (_config != null) throw new IllegalStateException("Can only call initConfig once!");
    
    FileConfiguration config;

    try {
      _propertiesFile.createNewFile();
      // be nice and ensure a config file if there isn't one
    }
    catch (IOException e) {
      // IOException occurred, continue without a real file
    }
    config = new FileConfiguration(_propertiesFile);
    try { config.loadConfiguration(); }
    catch (Exception e) {
      // problem parsing the config file.
      // Use defaults and remember what happened (for the UI)
      config.resetToDefaults();
//      Utilities.showDebug("Config Exception is: " + e.toString());
      config.storeStartupException(e);
    }
    _config = config; // required to support calls on DrJava._initConfig() in unit tests
    return config;
  }

  /** Saves the contents of the config file. TO DO: log any IOExceptions that occur. */
  protected static void _saveConfig() {
    try { getConfig().saveConfiguration(); }
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

  /** Displays a prompt to the user indicating that tools.jar could not be found in the specified location, and asks
   *  if he would like to specify a new location.
   */
  private static boolean _userWantsToPickAgain() {
    final String[] text = new String[] {
        "The file you chose did not appear to be the correct 'tools.jar'. ",
        "(Your choice might be an incompatible version of the file.) ",
        "Would you like to pick again?  The 'tools.jar' file is ",
        "generally located in the 'lib' subdirectory under your ",
        "JDK installation directory.",
        "(If you say 'No', DrJava might be unable to compile or ",
        "debug programs.)"
      };

    int result = JOptionPane.showConfirmDialog(null, text, "Locate 'tools.jar'?", JOptionPane.YES_NO_OPTION);
    return result == JOptionPane.YES_OPTION;
  }
}

