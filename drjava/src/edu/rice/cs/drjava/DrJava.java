/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2009, JavaPLT group at Rice University (drjava@rice.edu)
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

import static edu.rice.cs.drjava.config.OptionConstants.*;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import edu.rice.cs.drjava.config.FileConfiguration;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.ui.DrJavaErrorHandler;
import edu.rice.cs.plt.concurrent.DelayedInterrupter;
import edu.rice.cs.plt.concurrent.JVMBuilder;
import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.FileOps;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Startup class for DrJava consisting entirely of static members.  The main method reads the .drjava file (creating 
  * one if none exists) to get the critical information required to start the main JVM for DrJava: 
  * (i) the location of tools.jar in the Java JDK installed on this machine (so DrJava can invoke the javac compiler
  *     stored in tools.jar)
  * (ii) the argument string for invoking the main JVM (notably -X options used to determine maximum heap size, etc.)
  * @version $Id$
  */
public class DrJava {
  public static volatile Log _log = new Log("DrJava.txt", false);
  
  private static final String DEFAULT_MAX_HEAP_SIZE_ARG = "-Xmx128M";
  
  private static final ArrayList<String> _filesToOpen = new ArrayList<String>();
  private static final ArrayList<String> _jvmArgs = new ArrayList<String>();
  
  static volatile boolean _showDebugConsole = false;
  
  /** true if a new instance of DrJava should be started instead of
    * connecting to an already running instance. */
  static volatile boolean _forceNewInstance = false;
  
  /** Time in millisecond before restarting DrJava to change the heap size, etc. is deemed a success. */
  private static final int WAIT_BEFORE_DECLARING_SUCCESS = 5000;

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
  
  /** Add a file to the list of files to open. */
  public static void addFileToOpen(String s) {
    _filesToOpen.add(s);
    boolean isProjectFile =
      s.endsWith(OptionConstants.PROJECT_FILE_EXTENSION) ||
      s.endsWith(OptionConstants.PROJECT_FILE_EXTENSION2) ||
      s.endsWith(OptionConstants.OLD_PROJECT_FILE_EXTENSION);
    _forceNewInstance |= isProjectFile;
  }
  
  /** @return true if the debug console should be enabled */
  public static boolean getShowDebugConsole() { return _showDebugConsole; }
  
  /** Starts running DrJava.
    * @param args Command line argument array
    */
  public static void main(final String[] args) {    
    // handleCommandLineArgs will return true if DrJava should be loaded
    if (handleCommandLineArgs(args)) {
      // Platform-specific UI setup.
      PlatformFactory.ONLY.beforeUISetup();
      
//    Utilities.showDebug("Calling configureAndLoadDrJavaRoot with args = " + args);
      configureAndLoadDrJavaRoot(args); 
    }
  }
  
  public static void configureAndLoadDrJavaRoot(String[] args) {
    try {
      // if there were files passed on the command line,
      // try to open them in an existing instance
      if (!_forceNewInstance &&
          DrJava.getConfig().getSetting(edu.rice.cs.drjava.config.OptionConstants.REMOTE_CONTROL_ENABLED) &&
          (_filesToOpen.size() > 0)) {
        try {
          RemoteControlClient.openFile(null);
          if (RemoteControlClient.isServerRunning()) {
            // existing instance is running and responding
            for (int i = 0; i < _filesToOpen.size(); ++i) {
              RemoteControlClient.openFile(new File(_filesToOpen.get(i)));
            }
            // files opened in existing instance, quit
            System.exit(0);
          }
        }
        catch(IOException ioe) {
          ioe.printStackTrace();
        }      
      }
      
      // The code below is in a loop so that DrJava can retry launching itself
      // if it fails the first time after resetting the configuration file.
      // This helps for example when the main JVM heap size is too large, and
      // the JVM cannot be created.
      int failCount = 0;
      while(failCount < 2) {
        // Restart if there are custom JVM args
        String masterMemory = getConfig().getSetting(MASTER_JVM_XMX).trim();
        boolean restart = (getConfig().getSetting(MASTER_JVM_ARGS).length() > 0)
          || (!"".equals(masterMemory) && !OptionConstants.heapSizeChoices.get(0).equals(masterMemory));
        _log.log("restart: "+restart);
        
        LinkedList<String> classArgs = new LinkedList<String>();
        classArgs.addAll(_filesToOpen);
        
        // Add the parameters "-debugConsole" to classArgsList if _showDebugConsole is true
        if (_showDebugConsole) { classArgs.addFirst("-debugConsole"); }
        
        if (! _propertiesFile.equals(DEFAULT_PROPERTIES_FILE)) {
          // Placed in reversed order to get "-config filename"
          classArgs.addFirst(_propertiesFile.getAbsolutePath());
          classArgs.addFirst("-config");
        }
        
        if (restart) {
          
          // Run a new copy of DrJava and exit
          try {
            boolean failed = false;
            JVMBuilder jvmb = JVMBuilder.DEFAULT.jvmArguments(_jvmArgs);
            
            // extend classpath if JUnit/ConcJUnit location specified
            _log.log("JVMBuilder: classPath = "+jvmb.classPath());
            ArrayList<File> extendedClassPath = new ArrayList<File>();
            for(File f: jvmb.classPath()) { extendedClassPath.add(f); }
            _log.log("JVMBuilder: extendedClassPath = "+extendedClassPath);
            jvmb = jvmb.classPath(edu.rice.cs.plt.iter.IterUtil.asSizedIterable(extendedClassPath));
            _log.log("JVMBuilder: jvmArguments = "+jvmb.jvmArguments());
            _log.log("JVMBuilder: classPath = "+jvmb.classPath());
            
            // start new DrJava
            Process p = jvmb.start(DrJavaRoot.class.getName(), classArgs);
            DelayedInterrupter timeout = new DelayedInterrupter(WAIT_BEFORE_DECLARING_SUCCESS);
            try {
              int exitValue = p.waitFor();
              timeout.abort();
              failed = (exitValue != 0);
            }
            catch(InterruptedException e) { /* timeout was reached */ }
            if (failed) {
              if (failCount > 0) {
                // 2nd time that spawning has failed, give up
                JOptionPane.showMessageDialog(null,
                                              "DrJava was unable to start, and resetting your configuration\n" + 
                                              "did not help. Please file a support request at\n" + 
                                              "https://sourceforge.net/projects/drjava/",
                                              "Could Not Start DrJava",
                                              JOptionPane.ERROR_MESSAGE);
                System.exit(0);
              }
              else {
                // 1st time that spawning has failed, offer to reset configuration
                int result = JOptionPane.showConfirmDialog(null,
                                                           "DrJava was unable to start. Your configuration file (.drjava)\n" + 
                                                           "might be corrupt. Do you want to reset your configuration?",
                                                           "Could Not Start DrJava",
                                                           JOptionPane.YES_NO_OPTION);
                if (result != JOptionPane.YES_OPTION) { System.exit(0); }
                // reset configuration, save, and reload it
                getConfig().resetToDefaults();
                getConfig().saveConfiguration();
                if (!handleCommandLineArgs(args)) { System.exit(0); }
                ++failCount;
                continue;
              }
            }
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
          DrJavaRoot.main(classArgs.toArray(new String[0]));
        }
        break;
      }
    }
    catch(Throwable t) {
      // Show any errors to the System.err and in an DrJavaErrorHandler
      System.out.println(t.getClass().getName() + ": " + t.getMessage());
      t.printStackTrace(System.err);System.out.println("error thrown");
      DrJavaErrorHandler.record(t);
    }
  }
  
  /** Handles any command line arguments that have been specified.
    * @return true if DrJava should load, false if not
    */
  static boolean handleCommandLineArgs(String[] args) {
    boolean heapSizeGiven = false;  // indicates whether args includes an argument of the form -Xmx<number>
    
    // Loop through arguments looking for known options
    int argIndex = 0;
    int len = args.length;
    _log.log("handleCommandLineArgs. _filesToOpen: " + _filesToOpen);
    _log.log("\t_filesToOpen cleared");
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
      
      else if (arg.equals("-new")) _forceNewInstance = true;
      
      else if (arg.equals("-delete-after-restart")) {
        File deleteAfterRestart = new File(args[argIndex++]);
        deleteAfterRestart.delete();
      }
      
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
    
    if ((!("".equals(getConfig().getSetting(MASTER_JVM_XMX)))) &&
        (!(edu.rice.cs.drjava.config.OptionConstants.heapSizeChoices.get(0).equals(getConfig().getSetting(MASTER_JVM_XMX))))) { 
      _jvmArgs.add("-Xmx" + getConfig().getSetting(MASTER_JVM_XMX).trim() + "M");
      heapSizeGiven = true;
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
    
    for (int i = argIndex; i < len; i++) { addFileToOpen(args[i]); }
    _log.log("\t _filesToOpen now contains: " + _filesToOpen);

    return true;
  }
  
  /** Displays a usage message about the available options. */
  static void displayUsage() {
    System.out.println("Usage: java -jar drjava.jar [OPTIONS] [FILES]\n");
    System.out.println("where options include:");
    System.out.println("  -config [FILE]        use a custom config file");
    System.out.println("  -new                  force the creation of a new DrJava instance;");
    System.out.println("                        do not connect to existing instance");
    System.out.println("  -help | -?            print this help message");
    System.out.println("  -X<jvmOption>         specify a JVM configuration option for the master DrJava JVM");      
    System.out.println("  -D<name>[=<value>]    set a Java property for the master DrJava JVM");
  }
  
  /** Switches the config object to use a custom config file. Ensures that Java source files aren't 
    * accidentally used.
    */
  static void setPropertiesFile(String fileName) {
    if (! fileName.endsWith(".java"))  _propertiesFile = new File(fileName);
  }
  
  /** Initializes the configuration object with the current notion of the properties file.
    * @throws IllegalStateException if config has already been assigned
    */
  static FileConfiguration _initConfig() throws IllegalStateException {
//    // Make sure someone doesn't try to change the config object.
//    if (_config != null) throw new IllegalStateException("Can only call initConfig once!");
    
    FileConfiguration config;
    
    final File propFile = _propertiesFile;    // a static variable shared across configurations in tests
    
    try { propFile.createNewFile(); }         // be nice and ensure a config file if there isn't one
    catch (IOException e) { /* IOException occurred, continue without a real file */ }
    
    config = new FileConfiguration(propFile);
    try { config.loadConfiguration(); }
    catch (Exception e) {
      // Problem parsing the config file.  Use defaults and remember what happened (for the UI).
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
      // log this error
      DrJavaErrorHandler.record(new UnexpectedException(e, "Could not save the location of tools.jar in \n" +
                                                        "the '.drjava' file in your home directory. \n" +
                                                        "Another process may be using the file.\n\n"));
    }
  }
  
//  /** Displays a prompt to the user indicating that tools.jar could not be found in the specified location, and asks
//    * if he would like to specify a new location.
//    */
//  private static boolean _userWantsToPickAgain() {
//    File selectedFile = getConfig().getSetting(JAVAC_LOCATION);
//    String selectedVersion = _getToolsJarVersion(selectedFile);
//    
//    final String[] text;
//    if (selectedVersion==null) {
//      text = new String[] {
//        "The file you chose did not appear to be the correct 'tools.jar'",
//        "that is compatible with the version of Java that is used to",
//        "run DrJava (Java version " + System.getProperty("java.version") + ").",
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
//        "run DrJava (Java version " + System.getProperty("java.version") + ").",
//        "The file you have selected appears to be for",
//        "Java version " + selectedVersion + ".",
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
  
  /* Erase all non-final bindings created in this class.  Only used in testing. */
  public static void cleanUp() {
    _log.log("cleanUp. _filesToOpen: " + _filesToOpen);
    _filesToOpen.clear();
    _log.log("\t_filesToOpen cleared");
    _jvmArgs.clear();
    // Do not set _config or _propertiesFile to null because THEY ARE static
  }

  /** Warn if this system is Linux with Compiz. */
  public static boolean warnIfLinuxWithCompiz() {
    try {
      if (!System.getProperty("os.name").equals("Linux")) return false; // not Linux
      if (!DrJava.getConfig().getSetting(edu.rice.cs.drjava.config.OptionConstants.WARN_IF_COMPIZ)) return false; // set to ignore
      
      // get /bin/ps
      File ps = new File("/bin/ps");
      
      // execute ps
      ProcessBuilder pb = new ProcessBuilder(ps.getAbsolutePath(), "-A");
      Process psProc = pb.start();
      psProc.waitFor();
      
      // read the output of ps
      BufferedReader br = new BufferedReader(new InputStreamReader(psProc.getInputStream()));
      boolean compiz = false;
      String line = null;
      while((line=br.readLine()) != null) {
        // find the PID of JUnitTestRunner, i.e. the PID of the current process
        if ((line.endsWith("compiz")) ||
            (line.endsWith("compiz.real"))) {
          compiz = true;
          break;
        }
      }
      if (!compiz) return false; // no Compiz
      
      String[] options = new String[] { "Yes", "Yes, and ignore from now on", "No" };
      int res = javax.swing.JOptionPane.showOptionDialog(null,
                                                         "<html>DrJava has detected that you are using Compiz.<br>" + 
                                                         "<br>" + 
                                                         "Compiz and Java Swing are currently incompatible and can cause<br>" + 
                                                         "DrJava or your computer to crash.<br>" + 
                                                         "<br>" + 
                                                         "We recommend that you <b>disable Compiz</b>. On Ubuntu, go to<br>" + 
                                                         "System->Preferences->Appearence, display the Visual Effects tab,<br>" + 
                                                         "and select 'None'.<br>" + 
                                                         "<br>" + 
                                                         "For more information, please go to http://drjava.org/compiz<br>" + 
                                                         "<br>" + 
                                                         "Do you want to start DrJava anyway?</html>",
                                                         "Compiz detected",
                                                         JOptionPane.DEFAULT_OPTION,
                                                         javax.swing.JOptionPane.WARNING_MESSAGE,
                                                         null,
                                                         options,
                                                         options[0]);
      switch(res) {
        case 1:
          // set "ignore" option
          DrJava.getConfig().setSetting(edu.rice.cs.drjava.config.OptionConstants.WARN_IF_COMPIZ, false);
          break;
        case 2:
          System.exit(0);
          break;
      }
      return compiz;
    }
    catch(IOException ioe) {
      return false; // do not warn
    }
    catch(InterruptedException ie) {
      return false; // do not warn
    }
  }
}
