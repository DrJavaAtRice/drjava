/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava;

import static edu.rice.cs.drjava.config.OptionConstants.*;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import edu.rice.cs.drjava.config.ResourceBundleConfiguration;
import edu.rice.cs.drjava.config.FileConfiguration;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.ui.DrJavaErrorHandler;
import edu.rice.cs.plt.concurrent.DelayedInterrupter;
import edu.rice.cs.plt.concurrent.JVMBuilder;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.drjava.model.DrJavaFileUtils;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Startup class for DrJava consisting entirely of static members.  The main method reads the .drjava file (creating 
  * one if none exists) to get the critical information required to start the main JVM for DrJava: 
  * (i) the location of tools.jar in the Java JDK installed on this machine (so DrJava can invoke the javac compiler
  *     stored in tools.jar)
  * (ii) the argument string for invoking the main JVM (notably -X options used to determine maximum heap size, etc.)
  * 
  * Here is a summary of the launch mechanism of DrJava:
  * 
  * 1. DrJava.main will be started.
  *
  * 2. DrJava.handleCommandLineArgs scans the command line arguments.
  * 2.1. This involves determining if the -new parameter forces a new instance.
  * 
  * 3. DrJava.configureAndLoadDrJavaRoot determines if remote control should be used:
  * 3.1. If -new doesn't force a new instance
  * 3.2. and REMOTE_CONTROL_ENABLED
  * 3.3. and files have been specified on the command line
  * 3.4. and the remote control server can be contacted
  * 3.5. then DrJava will open the files in an existing instance and quit
  * 
  * 4. DrJava.configureAndLoadDrJavaRoot determines if a restart is necessary:
  * 4.1. If MASTER_JVM_XMX is set
  * 4.2. or MASTER_JVM_ARGS is set
  * 4.3. then DrJava will attempt to restart itself with the specified JVM arguments
  * 4.4. Files that have arrived via Mac OS X's handleOpenFile event up to this point
  *      are included in the main arguments for the restarted DrJava.
  * 4.5. If that fails, DrJava will ask if the user wants to delete the settings in the .drjava file
  * 4.5.1. If the user says "yes", DrJava will attempt another restart. If that fails, DrJava gives up.
  * 4.5.2. If the user says "no", DrJava gives up.
  * 4.6. If additional files arrive via the handleOpenFile event, DrJava will
  *      attempt to use the remote control to open the files in the restarted DrJava.
  * 4.6.1. DrJava will perform NUM_REMOTE_CONTROL_RETRIES attempts to contact the
  *        remote control server, with WAIT_BEFORE_REMOTE_CONTROL_RETRY ms of sleep time in between.
  * 
  * 5. If neither the remote control was used nor a restart was necessary, DrJava will
  *    call DrJavaRoot.main.
  * 5.1. Files that have arrived via Mac OS X's handleOpenFile event up to this point
  *      are included in the arguments for DrJavaRoot.main.
  * 5.2. If additional files arrive via the handleOpenFile event, DrJava will
  *      MainFrame.handleRemoteOpenFile.
  * 
  * @version $Id: DrJava.java 5594 2012-06-21 11:23:40Z rcartwright $
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
  
  /** true if a new DrJava needs to be restarted to adjust parameters. */
  static volatile boolean _doRestart = false;
  
  /** true if DrJava has already launched the new instance. */
  static volatile boolean _alreadyRestarted = false;
  
  /** true if the restarted DrJava will use remote control, and we can try to
    * pass along files to open that arrived too late. */
  static volatile boolean _restartedDrJavaUsesRemoteControl = true;
  
  /** Time in millisecond before restarting DrJava to change the heap size, etc. is deemed a success. */
  private static final int WAIT_BEFORE_DECLARING_SUCCESS = 5000;

  /** Number of times we retry opening with the remote control. */
  private static final int NUM_REMOTE_CONTROL_RETRIES = 15;

  /** Time in millisecond that we wait before making another remote control attempt. */
  private static final int WAIT_BEFORE_REMOTE_CONTROL_RETRY = 500;

  /* Config objects can't be public static final, since we have to delay construction until we know the 
   * config file's location.  (Might be specified on command line.) Instead, use accessor methods to 
   * prevent others from assigning new values. */
  
  /** Default properties file used by the configuration object, i.e. ".drjava" in the user's home directory. */
  public static final File DEFAULT_PROPERTIES_FILE = new File(System.getProperty("user.home"), ".drjava");
  
  /** Properties file used by the configuration object. Defaults to DEFAULT_PROPERTIES_FILE. */
  private static volatile File _propertiesFile = DEFAULT_PROPERTIES_FILE;
  
  /** Configuration object with all customized and default values. 
    * Lazily initialized from _propertiesFile in getConfig() or handleCommandLineArgs(). */
  private static volatile FileConfiguration _config;
  
  /** Name of the resource bundle that controls whether options are editable or not. */
  public static final String RESOURCE_BUNDLE_NAME = "edu.rice.cs.drjava.config.options";
  
  /** Returns the properties file used by the configuration object. */
  public static File getPropertiesFile() { return _propertiesFile; }
  
  /** Returns the configuration object with all customized and default values. */
  public static synchronized FileConfiguration getConfig() {
    if (_config == null) {
      _config = _initConfig();  // read specified .djrava file into _config
    }
    return _config;
  }
  
  /** @return an array of the files that were passed on the command line. */
  public static synchronized String[] getFilesToOpen() { return _filesToOpen.toArray(new String[0]); }
  
  /** Add a file to the list of files to open. */
  public static synchronized void addFileToOpen(String s) {
    _filesToOpen.add(s);
    boolean isProjectFile =
      s.endsWith(OptionConstants.PROJECT_FILE_EXTENSION) ||
      s.endsWith(OptionConstants.PROJECT_FILE_EXTENSION2) ||
      s.endsWith(OptionConstants.OLD_PROJECT_FILE_EXTENSION);
    _forceNewInstance |= isProjectFile;
    if (_doRestart && _alreadyRestarted) {
      _log.log("addFileToOpen: already done the restart, trying to use remote control");
      // we already did the restart, try to use the remote control to open the file
      if (DrJava.getConfig().getSetting(OptionConstants.REMOTE_CONTROL_ENABLED)) {
        _log.log("\tremote control...");
        openWithRemoteControl(_filesToOpen,NUM_REMOTE_CONTROL_RETRIES );
        _log.log("\tclearing _filesToOpen");
        clearFilesToOpen();
      }
    }
  }
  
  /** Clear the list of files to open. */
  public static synchronized void clearFilesToOpen() {
    _filesToOpen.clear();
  }
  
  /** Open the specified files using the remote control. If the remote control server is not
    * running, numAttempts attempts will be made, with WAIT_BEFORE_REMOTE_CONTROL_RETRY
    * ms of sleep time in between.
    * @param files files to open with remote control
    * @param numAttempts of attempts to be made
    * @return true if successful
    */
  public static synchronized boolean openWithRemoteControl(ArrayList<String> files, int numAttempts) {
    if (! DrJava.getConfig().getSetting(OptionConstants.REMOTE_CONTROL_ENABLED) ||
        ! _restartedDrJavaUsesRemoteControl ||
        files.size() == 0) return false;
    
    ArrayList<String> fs = new ArrayList<String>(files);
    int failCount = 0;
    while(failCount<numAttempts) {
      try {
        RemoteControlClient.openFile(null);
        if (RemoteControlClient.isServerRunning()) {
          // existing instance is running and responding
          for (int i = 0; i < fs.size(); ++i) {
            _log.log("opening with remote control "+fs.get(i));
            RemoteControlClient.openFile(new File(fs.get(i)));
            files.remove(fs.get(i));
          }
          return true; // success
        }
        else {
          ++failCount;
          _log.log("Failed to open with remote control, attempt "+failCount+" of "+NUM_REMOTE_CONTROL_RETRIES);
          if (failCount>=numAttempts) return false; // failure
          try { Thread.sleep(WAIT_BEFORE_REMOTE_CONTROL_RETRY); }
          catch(InterruptedException ie) { /* just try again now */ }
        }
      }
      catch(IOException ioe) { ioe.printStackTrace(); }
    }
    return false; // failure
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
          DrJava.getConfig().getSetting(OptionConstants.REMOTE_CONTROL_ENABLED) &&
          (_filesToOpen.size() > 0)) {
        if (openWithRemoteControl(_filesToOpen,1)) System.exit(0); // files opened in existing instance, quit
      }
      
      // The code below is in a loop so that DrJava can retry launching itself
      // if it fails the first time after resetting the configuration file.
      // This helps for example when the main JVM heap size is too large, and
      // the JVM cannot be created.
      int failCount = 0;
      while(failCount < 2) {
        // Restart if there are custom JVM args
        String masterMemory = getConfig().getSetting(MASTER_JVM_XMX).trim();
        boolean _doRestart = (getConfig().getSetting(MASTER_JVM_ARGS).length() > 0)
          || (!"".equals(masterMemory) && !OptionConstants.heapSizeChoices.get(0).equals(masterMemory));
        _log.log("_doRestart: "+_doRestart);
        
        LinkedList<String> classArgs = new LinkedList<String>();
        
        // Add the parameters "-debugConsole" to classArgsList if _showDebugConsole is true
        if (_showDebugConsole) { classArgs.addFirst("-debugConsole"); }
        
        if (! _propertiesFile.equals(DEFAULT_PROPERTIES_FILE)) {
          // Placed in reversed order to get "-config filename"
          classArgs.addFirst(_propertiesFile.getAbsolutePath());
          classArgs.addFirst("-config");
        }
        
        synchronized(DrJava.class) {
          classArgs.addAll(_filesToOpen);
          clearFilesToOpen();
          _log.log("_filesToOpen copied into class arguments, clearing _filesToOpen");
        }
        
        if (_doRestart) {
          if (DrJava.getConfig().getSetting(OptionConstants.REMOTE_CONTROL_ENABLED)) {
            // at this time, OUR remote control server hasn't been started yet
            // if one is running, then we won't be able to contact the restarted DrJava
           _restartedDrJavaUsesRemoteControl = !RemoteControlClient.isServerRunning();
          } else {
            // no remote control
            _restartedDrJavaUsesRemoteControl = false;
          }
          
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
            _log.log("JVMBuilder: mainParams = "+classArgs);
            
            // start new DrJava
            Process p = jvmb.start(DrJavaRoot.class.getName(), classArgs);
            _alreadyRestarted = true;
            _log.log("_alreadyRestarted = true");
            DelayedInterrupter timeout = new DelayedInterrupter(WAIT_BEFORE_DECLARING_SUCCESS);
            try {
              int exitValue = p.waitFor();
              timeout.abort();
              failed = (exitValue != 0);
            }
            catch(InterruptedException e) { /* timeout was reached */ }
            _log.log("failed = "+failed);
            if (failed) {
              if (failCount > 0) {
                // 2nd time that spawning has failed, give up
                JOptionPane.showMessageDialog(null,
                                              "DrJava was unable to start, and resetting your configuration\n" + 
                                              "did not help. Please file a support request at\n" + 
                                              "https://sourceforge.net/projects/drjava/",
                                              "Could Not Start DrJava",
                                              JOptionPane.ERROR_MESSAGE);
                System.exit(1);
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
            else {
              // check if there are any files left in _filesToOpen
              _log.log("not failed, send remaining files via remote control: "+_filesToOpen);
              openWithRemoteControl(_filesToOpen, NUM_REMOTE_CONTROL_RETRIES);
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
          // when we return from here, DrJavaRoot._mainFrame has been initialized
          // but we may still have files in _filesToOpen that were not processed
          // do that now
          ArrayList<String> fs = new ArrayList<String>(_filesToOpen);
          for(String f: fs) {
            DrJavaRoot.handleRemoteOpenFile(new File(f), -1);
          }
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
    
    while(argIndex < len) {
      String arg = args[argIndex++];
      
      if (arg.equals("-config")) {
        if (len == argIndex) { 
          // config option is missing file name; should we generate an error?
          return true;
        }
        // arg.length > i+1 implying args list incudes config file name and perhaps files to open
        setPropertiesFile(args[argIndex++]);
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
      
      else if (arg.equals("-jll")) {
        String[] argsForJLL = new String[args.length-argIndex];
        System.arraycopy(args, argIndex, argsForJLL, 0, argsForJLL.length);
        edu.rice.cs.javalanglevels.LanguageLevelConverter.main(argsForJLL);
        System.exit(0);
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
    
    synchronized(DrJava.class) {
      _config = _initConfig();  // read specified .djrava file into _config
    }
    
    if ((!("".equals(getConfig().getSetting(MASTER_JVM_XMX)))) &&
        (!(OptionConstants.heapSizeChoices.get(0).equals(getConfig().getSetting(MASTER_JVM_XMX))))) { 
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
    System.out.println("  -jll [ARGS]           invoke the Java Language Level converter, specify files in ARGS");
  }
  
  /** Switches the config object to use a custom config file. Ensures that Java source files aren't 
    * accidentally used.
    */
  static void setPropertiesFile(String fileName) {
    if (!DrJavaFileUtils.isSourceFile(fileName))  _propertiesFile = new File(fileName);
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
    _config = new ResourceBundleConfiguration(RESOURCE_BUNDLE_NAME,config);
    return _config;
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
    clearFilesToOpen();
    _log.log("\t_filesToOpen cleared");
    _jvmArgs.clear();
    // Do not set _config or _propertiesFile to null because THEY ARE static
  }

  /** Warn if this system is Linux with Compiz. */
  public static boolean warnIfLinuxWithCompiz() {
    try {
      if (!System.getProperty("os.name").equals("Linux")) return false; // not Linux
      if (!DrJava.getConfig().getSetting(OptionConstants.WARN_IF_COMPIZ)) return false; // set to ignore
      
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
      
      final JavaVersion.FullVersion ver160_20 = JavaVersion.parseFullVersion("1.6.0_20");
      if (JavaVersion.CURRENT_FULL.compareTo(ver160_20)>=0) return false; // Java >= 1.6.0_20
      
      String[] options = new String[] { "Yes", "Yes, and ignore from now on", "No" };
      int res = javax.swing.JOptionPane.
        showOptionDialog(null,
                         "<html>DrJava has detected that you are using Compiz with a version<br>" +
                         "of Java that is older than " + ver160_20 + ".<br>" + 
                         "<br>" + 
                         "Compiz and older versions of Java are incompatible and can cause<br>" + 
                         "DrJava or your computer to crash.<br>" + 
                         "<br>" + 
                         "We recommend that you <b>update to " + ver160_20 + " or newer</b>,<br>" +
                         "or that you disable Compiz if you still experience problems.<br>" +
                         "On Ubuntu, go to System->Preferences->Appearence, display the<br>" +
                         "Visual Effects tab, and select 'None'.<br>" + 
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
          DrJava.getConfig().setSetting(OptionConstants.WARN_IF_COMPIZ, false);
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
