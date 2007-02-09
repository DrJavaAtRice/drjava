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

import static edu.rice.cs.drjava.config.OptionConstants.*;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import edu.rice.cs.drjava.config.FileConfiguration;
import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.ui.DrJavaErrorHandler;
import edu.rice.cs.drjava.ui.ClassPathFilter;
import edu.rice.cs.drjava.ui.SplashScreen;
import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.classloader.ToolsJarClassLoader;
import edu.rice.cs.util.newjvm.ExecJVM;
import edu.rice.cs.plt.debug.DebugUtil;

/** Startup class for DrJava consisting entirely of static members.  The main method reads the .drjava file (creating 
 *  one if none exists) to get the critical information required to start the main JVM for DrJava: 
 *  (i) the location of tools.jar in the Java JDK installed on this machine (so DrJava can invoke the javac compiler
 *      stored in tools.jar)
 *  (ii) the argument string for invoking the main JVM (notably -X options used to determine maximum heap size, etc.)
 *  @version $Id$
 */
public class DrJava {
  
  private static Log _log = new Log("DrJava.txt", false);
  
  private static final String DEFAULT_MAX_HEAP_SIZE_ARG = "-Xmx128M";
  
  private static final ArrayList<String> _filesToOpen = new ArrayList<String>();
  private static final ArrayList<String> _jvmArgs = new ArrayList<String>();

  static volatile boolean _showDebugConsole = false;
  
  /* Config objects can't be public static final, since we have to delay construction until we know the 
   * config file's location.  (Might be specified on command line.) Instead, use accessor methods to 
   * prevent others from assigning new values. */

  /** Default properties file used by the configuration object, i.e. ".drjava" in the user's home directory. */
  public static final File DEFAULT_PROPERTIES_FILE = new File(System.getProperty("user.home"), ".drjava");
  
  /** Properties file used by the configuration object. Defaults to DEFAULT_PROPERTIES_FILE. */
  private static volatile File _propertiesFile = DEFAULT_PROPERTIES_FILE;
  
  /** Configuration object with all customized and default values.  Initialized from _propertiesFile.  */
  private static volatile FileConfiguration _config = _initConfig();
  
  /** Returns the properties file used by the configuration object. */
  public static File getPropertiesFile() { return _propertiesFile; }

  /** Returns the configuration object with all customized and default values. */
  public static FileConfiguration getConfig() { return _config; }

  /** @return an array of the files that were passed on the command line. */
  public static String[] getFilesToOpen() { return _filesToOpen.toArray(new String[0]); }
  
  /** @return true if the debug console should be enabled */
  public static boolean getShowDebugConsole() { return _showDebugConsole; }
  
  /** Starts running DrJava.
   *  @param args Command line argument array
   */
  public static void main(final String[] args) {
    // Platform-specific UI setup.
    PlatformFactory.ONLY.beforeUISetup();
    
    DebugUtil.error.log("This is the error log");
    DebugUtil.debug.log("This is the debug log");

    new SplashScreen().flash();
//    Utilities.showDebug("Calling configureAndLoadDrJavaRoot with args = " + args);
    configureAndLoadDrJavaRoot(args); 
  }
  
  public static void configureAndLoadDrJavaRoot(String[] args) {
    try {
      // handleCommandLineArgs will return true if DrJava should be loaded
      if (handleCommandLineArgs(args)) {
        
        // Restart if there are custom JVM args
        boolean restart = getConfig().getSetting(MASTER_JVM_ARGS).length() > 0;
        
        LinkedList<String> classArgsList = new LinkedList<String>();
        classArgsList.addAll(_filesToOpen);

        // Add the parameters "-debugConsole" to classArgsList if _showDebugConsole is true
        if (_showDebugConsole) { classArgsList.addFirst("-debugConsole"); }
        
        if (!_propertiesFile.equals(DEFAULT_PROPERTIES_FILE)) {
          // Placed in reversed order to get "-config filename"
          classArgsList.addFirst(_propertiesFile.getAbsolutePath());
          classArgsList.addFirst("-config");
        }

        String[] classArgs = classArgsList.toArray(new String[0]);
        
        if (restart) {
          String classPath = System.getProperty("java.class.path");
          
          // Run a new copy of DrJava and exit
          try {
//          Utilities.showDebug("Starting DrJavaRoot with classArgs = " + Arrays.toString(classArgs) + "; classPath = " + classPath + 
//                             "; jvmArgs = " + _jvmArgs + "; workDir = " + workDir);
            ExecJVM.runJVM("edu.rice.cs.drjava.DrJavaRoot", classArgs, classPath, _jvmArgs.toArray(new String[0]), null);
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
        
        else {
          // No restart -- just invoke DrJavaRoot.main.
          DrJavaRoot.main(classArgs);
        }
      }
    }
    catch(Throwable t) {
      // Show any errors to the System.err and in an DrJavaErrorHandler
      System.out.println(t.getClass().getName() + ": " + t.getMessage());
      t.printStackTrace(System.err);System.out.println("error thrown");
      new DrJavaErrorHandler().handle(t);
    }
  }
  
  /** Handles any command line arguments that have been specified.
   *  @return true if DrJava should load, false if not
   */
  static boolean handleCommandLineArgs(String[] args) {
    boolean heapSizeGiven = false;  // indicates whether args includes an argument of the form -Xmx<number>
    
    // Loop through arguments looking for known options
    int argIndex = 0;
    int len = args.length;
    _filesToOpen.clear();
    
    while(argIndex < len) {
      String arg = args[argIndex++];
      
      if (arg.equals("-config")) {
        if (len == argIndex) { 
          // config option is missing file name; should we generate an error?
          return true;
        }
        // arg.length > i+1 implying args list incudes config file name and perhaps files to open
        setPropertiesFile(args[argIndex++]);
        _config = _initConfig();  // read specified .djrava file into _config
      }
      
      else if (arg.startsWith("-X") || arg.startsWith("-D")) {
        if (arg.startsWith("-Xmx")) { heapSizeGiven = true; }
        _jvmArgs.add(arg); 
      }
      
      else if (arg.equals("-debugConsole")) _showDebugConsole = true;
      
      else if (arg.equals("-help") || arg.equals("-?")) {
        displayUsage();
        return false;
      }
      else {
        // this is the first file to open, do not consume
        --argIndex;
        break;
      }
    }
    
    List<String> configArgs = ArgumentTokenizer.tokenize(getConfig().getSetting(MASTER_JVM_ARGS));
    for (String arg : configArgs) {
      if (arg.startsWith("-Xmx")) { heapSizeGiven = true; }
      _jvmArgs.add(arg);
    }

    if (PlatformFactory.ONLY.isMacPlatform()) {
      String iconLoc = System.getProperty("edu.rice.cs.drjava.icon");
      if (iconLoc != null) { // we are running inside the Mac app wrapper
        _jvmArgs.add("-Xdock:name=DrJava");
        _jvmArgs.add("-Xdock:icon=" + iconLoc);
      }
    }
    
    if (!heapSizeGiven) { _jvmArgs.add(DEFAULT_MAX_HEAP_SIZE_ARG); }
        
    _log.log("_jvmArgs = " + _jvmArgs);

    // Open the remaining args as filenames

    for (int i = argIndex; i < len; i++) { _filesToOpen.add(args[i]); }
    return true;
  }

  /** Displays a usage message about the available options. */
  static void displayUsage() {
    final StringBuilder buf = new StringBuilder();
    buf.append("Usage: java -jar drjava.jar [OPTIONS] [FILES]\n\n");
    buf.append("where options include:\n");
    buf.append("  -config [FILE]        use a custom config file\n");
    buf.append("  -help | -?            print this help message\n");
    buf.append("  -X<jvmOption>         specify a JVM configuration option for the master DrJava JVM\n");      
    buf.append("  -D<name>[=<value>]    set a Java property for the master DrJava JVM\n");      
    System.out.print(buf.toString());
  }
  
//  /** Prompts the user that the location of tools.jar needs to be specified to be able to use the compiler and/or the
//   *  debugger.  
//   *  @param needCompiler whether DrJava needs tools.jar for a compiler
//   *  @param needDebugger whether DrJava needs tools.jar for the debugger
//   */
//  public static void promptForToolsJar(boolean needCompiler, boolean needDebugger) {
//    File selectedFile = getConfig().getSetting(JAVAC_LOCATION);
//    String selectedVersion = _getToolsJarVersion(selectedFile);
//    
//    final String[] text;
//    if (selectedVersion==null) {
//      text = new String[] {
//        "DrJava cannot find a 'tools.jar' file for the version of Java ",
//        "that is being used to run DrJava (Java version "+System.getProperty("java.version")+").",
//        "Would you like to specify the location of the requisite 'tools.jar' file?",
//        "If you say 'No', DrJava might be unable to compile or debug Java programs."
//      };
//    }
//    else {
//      text = new String[] {
//        "DrJava cannot find a 'tools.jar' file for the version of Java ",
//        "that is being used to run DrJava (Java version "+System.getProperty("java.version")+").",
//        "The file you have selected appears to be for version "+selectedVersion+".",
//        "Would you like to specify the location of the requisite 'tools.jar' file?",
//        "If you say 'No', DrJava might be unable to compile or debug Java programs.)"
//      };
//    }
//    
//    int result = JOptionPane.showConfirmDialog(null, text, "Locate 'tools.jar'?", JOptionPane.YES_NO_OPTION);
//
//    if (result == JOptionPane.YES_OPTION) {
//      JFileChooser chooser = new JFileChooser();
//      chooser.setFileFilter(new ClassPathFilter() {
//        public boolean accept(File f) {
//          if (f.isDirectory()) return true;
//          String ext = getExtension(f);
//          return ext != null && ext.equals("jar");
//        }
//        public String getDescription() { return "Jar Files"; }
//      });
//
//      // Loop until we find a good tools.jar or the user gives up
//      do {
//        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//          File jar = chooser.getSelectedFile();
//
//          if (jar != null) {
//            // set the tools.jar property
//            getConfig().setSetting(JAVAC_LOCATION, jar);
//
//            // Adjust if we needed a compiler
//            if (needCompiler && classLoadersCanFind(TEST_COMPILER_CLASS)) needCompiler = false;
//
//            // Adjust if we need a debugger
//            if (needDebugger && classLoadersCanFind(TEST_DEBUGGER_CLASS)) needDebugger = false;
//          }
//        }
////        Utilities.showDebug("need Compiler = " + needCompiler + "; needDebugger = " + needDebugger);
//      }
//      while ((needCompiler || needDebugger) && _userWantsToPickAgain());
//      
//      // Save config with good tools.jar if available
//      if ((! needCompiler) && (! needDebugger)) _saveConfig();
//    }
//  }
  
 
  /** Switches the config object to use a custom config file. Ensures that Java source files aren't 
   *  accidentally used.
   */
  static void setPropertiesFile(String fileName) {
    if (!fileName.endsWith(".java"))  _propertiesFile = new File(fileName);
  }
  
  /** Initializes the configuration object with the current notion of the properties file.
   *  @throws IllegalStateException if config has already been assigned
   */
  static FileConfiguration _initConfig() throws IllegalStateException {
//    // Make sure someone doesn't try to change the config object.
//    if (_config != null) throw new IllegalStateException("Can only call initConfig once!");
    
    FileConfiguration config;

    try { _propertiesFile.createNewFile(); }               // be nice and ensure a config file if there isn't one
    catch (IOException e) { /* IOException occurred, continue without a real file */ }
    
    config = new FileConfiguration(_propertiesFile);
    try { config.loadConfiguration(); }
    catch (Exception e) {
      // Problem parsing the config file.  Use defaults and remember what happened (for the UI).
      config.resetToDefaults();
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

//  /** Displays a prompt to the user indicating that tools.jar could not be found in the specified location, and asks
//   *  if he would like to specify a new location.
//   */
//  private static boolean _userWantsToPickAgain() {
//    File selectedFile = getConfig().getSetting(JAVAC_LOCATION);
//    String selectedVersion = _getToolsJarVersion(selectedFile);
//    
//    final String[] text;
//    if (selectedVersion==null) {
//      text = new String[] {
//        "The file you chose did not appear to be the correct 'tools.jar'",
//        "that is compatible with the version of Java that is used to",
//        "run DrJava (Java version "+System.getProperty("java.version")+").",
//        "Your choice might be an incompatible version of the file.",
//        "Would you like to pick again?  The 'tools.jar' file is ",
//        "generally located in the 'lib' subdirectory under your ",
//        "JDK installation directory.",
//        "(If you say 'No', DrJava might be unable to compile or ",
//        "debug programs.)"
//      };
//    }
//    else {
//      text = new String[] {
//        "The file you chose did not appear to be the correct 'tools.jar'",
//        "that is compatible with the version of Java that is used to",
//        "run DrJava (Java version "+System.getProperty("java.version")+").",
//        "The file you have selected appears to be for",
//        "Java version "+selectedVersion+".",
//        "Your choice might be an incompatible version of the file.",
//        "Would you like to pick again?  The 'tools.jar' file is ",
//        "generally located in the 'lib' subdirectory under your ",
//        "JDK installation directory.",
//        "If you say 'No', DrJava might be unable to compile or ",
//        "debug programs."
//      };
//    }
//
//    int result = JOptionPane.showConfirmDialog(null, text, "Locate 'tools.jar'?", JOptionPane.YES_NO_OPTION);
//    return result == JOptionPane.YES_OPTION;
//  }
  
}
