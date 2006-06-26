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

package edu.rice.cs.util.newjvm;

import java.io.*;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

import java.util.Arrays;
  
import edu.rice.cs.util.Log;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.ScrollableDialog;
import edu.rice.cs.util.swing.Utilities;
//import edu.rice.cs.util.PreventExitSecurityManager;

/** This class is the root class for the Slave JVM.  The Master JVM invokes the {@link #main} method of this class, 
 *  which is never instantiated. See the {@link #main} method documentation for information on the command line 
 *  parameters this class requires.  If there is an error setting up the slave JVM before the RMI links can be 
 *  established, this JVM process will exit with an error code according to the following list:
 *  <DL>
 *  <DT>1</DT><DD>Invalid number of command line arguments.</DD>
 *  <DT>2</DT><DD>Error deserializing remote stub</DD>
 *  <DT>3</DT><DD>Error instantiating slave implementation class</DD>
 *  </DL>
 *  If the slave JVM completes successfully, it will exit with code 0.
 *
 *  @version $Id$
 */
public final class SlaveJVMRunner {

  /** Whether Swing dialogs should be displayed with diagnostic
   * information if the slave is unable to register or contact the
   * main JVM.  If false, the information will be printed to (the
   * usually invisible) System.err.
   * 
   * Note that the master JVM will always be notified if possible if
   * there is a problem instantiating or registering the slave, so
   * it can take appropriate action.  This flag only affects those
   * situations in which the master JVM cannot be contacted.
   */
  public static final boolean SHOW_DEBUG_DIALOGS = false;
  
  protected static final Log _log  = new Log("MasterSlave.txt", true);
  
  private static final long RMI_TIMEOUT = 5000L;
  
  private static Thread _main;
  
  private static volatile boolean _notDone;
 
  /** Private constructor to prevent instantiation. */
  private SlaveJVMRunner() { }

  private static AbstractSlaveJVM _getInstance(Class clazz) throws Exception {
    try { return (AbstractSlaveJVM) clazz.getField("ONLY").get(null); }
    catch (Throwable t) { 
      _log.log("SlaveRemote class does not have an ONLY field!");
      return (AbstractSlaveJVM) clazz.newInstance();  
    }
  }

  /** The main method for invoking a slave JVM.
   * 
   *  @param args Command-line parameters, of which there must be two or three. The first is the absolute path to the 
   *         file containing the serialized MasterRemote stub, and the second is the fully-qualified class name of the
   *         slave JVM implementation class.
   */
  public synchronized static void main(String[] args) {
    try {
      // Make sure RMI doesn't use an IP address that might change
      System.setProperty("java.rmi.server.hostname", "127.0.0.1");
      
      if (args.length != 3 && args.length != 2) System.exit(1);
      
      _notDone = true;
        
      _main = Thread.currentThread();
      

      // get the master remote
      final FileInputStream fstream = new FileInputStream(args[0]);
      final ObjectInputStream ostream = new ObjectInputStream(new BufferedInputStream(fstream));
      
      _log.log("Slave JVM reading master remote stub from file " + args[0] + " with " + 
                 fstream.getChannel().size() + " bytes");
      
/* The following code currently breaks unit tests and DrJava itself when it detects the hanging
 * of readObject(...).  It can be commented back if the calling code is revised to handle this form
 * of exit.  */
      
//      Thread timeout = new Thread("RMI Timeout Thread") {
//        public void run() {
//          _log.log("RMI timer started");
//          
//          final Object lock = new Object();
//          try { synchronized(lock) { lock.wait(RMI_TIMEOUT); } }
//          catch(InterruptedException e) { throw new UnexpectedException(e); }
//          // Abort starting this slave JVM if readObject has hung
//          if (_notDone) {
//            StackTraceElement[] trace = Thread.getAllStackTraces().get(_main);
//            _log.log("DUMP of hung deserializing thread:", trace);
//            System.exit(9);
//          }
//          else _log.log(this + " TERMINATED normally");
//        }
//      };
//      
//      timeout.setDaemon(true);
//      timeout.start();
      
//      // if we have a remote classloader to use, load it
//      if (args.length == 3) _installRemoteLoader(args[2]);
      
      // Loading the class that intermittently hangs first readObject(...) call below
      Class psi = Class.forName("java.net.PlainSocketImpl");
        
      final MasterRemote masterRemote = (MasterRemote) ostream.readObject();
      _notDone = false;
      _log.log("SlaveJVMRunner completed reading " + masterRemote);
      fstream.close();
      ostream.close();
      
      AbstractSlaveJVM slave = null;

      try {
        Class slaveClass = Class.forName(args[1]);
//        _log.log("Slave JVM created singleton of " + args[1]);
        slave = _getInstance(slaveClass);
        
        //Export slave object to RMI, passing stub to the master JVM (how does stub get there?  Transitivity?
//        _log.log("Slave JVM exporting " + slave + " to RMI");
        SlaveRemote slaveRemote = (SlaveRemote) UnicastRemoteObject.exportObject(slave);  
        _log.log("SlaveJVMRunner exported stub " + slaveRemote);

        // start the slave and then notify the master
//        _log.log("Slave JVM invoking the method " + slave + ".start(" + masterRemote + ")");
        slave.start(masterRemote);
        _log.log("SlaveJVMRunner invoking the method registerSlave(" + slave + ") in the Master JVM");
        masterRemote.registerSlave(slave);
      }
      catch (Exception e) {
        // Couldn't instantiate the slave.
        _log.log("SlaveJVMRunner could not instantiate and start slave class '" + slave + "'.  Threw exception: " + e);
        try {
          // Try to show the error properly, through the master
          masterRemote.errorStartingSlave(e);
        }
        catch (RemoteException re) {
          // Couldn't show the error properly, so use another approach
          String msg = "SlaveJVMRunner could not instantiate and register the slave.\n" +
            "  Also failed to display error through master JVM, because:\n" +
            StringOps.getStackTrace(re) + "\n";
          _showErrorMessage(msg, e);
        }
        System.exit(3);
      }
    }
    catch (Exception e) { // IOException, ClassNotFoundException
      // There's no master to display the error, so we'll do it ourselves
      _showErrorMessage("SlaveJVMRunner could not set up the Slave JVM.", e);
      _log.log("SlaveJVMRunner could not set up the Slave JVM. Calling System.exit(2) in response to: " + e);
      System.exit(2);
    }
  }

  /** Displays a graphical error message to notify the user of a problem encountered while starting the slave JVM.
   *  @param cause A message indicating where the error took place.
   *  @param t The Throwable which caused the error.
   */
  private static void _showErrorMessage(String cause, Throwable t) {
    String msg = "An error occurred in SlaveJVMRunner while starting the slave JVM:\n  " +
      cause + "\n\nOriginal error:\n" + StringOps.getStackTrace(t);
    
    _log.log("ERROR in SlaveJVMRunner: " + cause + "; threw " + t);

    if (SHOW_DEBUG_DIALOGS) new ScrollableDialog(null, "Error", "Error details:", msg).show();
    else if (! Utilities.TEST_MODE) System.out.println(msg);
  }
}
