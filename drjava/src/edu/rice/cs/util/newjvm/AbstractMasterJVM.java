/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.newjvm;

import java.rmi.*;
import java.rmi.server.*;
import java.io.*;

/**
 * An abstract class implementing the logic to invoke and control, via
 * RMI, a second Java virtual machine.
 * This class is used by subclassing it.
 * (See package documentation for more details.)
 *
 * @version $Id$
 */
public abstract class AbstractMasterJVM/*<SlaveType extends SlaveRemote>*/
  implements MasterRemote/*<SlaveType>*/
{
  private static final String RUNNER = SlaveJVMRunner.class.getName();
  
  /** The slave JVM remote stub, if it's connected, or null if not. */
  private SlaveRemote _slave = null;

  /** Is slave JVM in the progress of starting up? */
  private boolean _startupInProgress = false;

  /**
   * This flag is set when a quit request is issued before the slave has even
   * finished starting up. In that case, immediately after starting up, we
   * quit it.
   */
  private boolean _quitOnStartup = false;

  /**
   * The current remote stub for this main JVM object.
   * This field is null except between the time the slave
   * JVM is first invoked and the time the slave registers itself.
   */
  private Remote _stub;

  /**
   * The file containing the serialized remote stub.
   * This field is null except between the time the slave
   * JVM is first invoked and the time the slave registers itself.
   */
  private File _stubFile;
  
  /** The fully-qualified name of the slave JVM class. */
  private final String _slaveClassName;

  /**
   * Sets up the master JVM object, but does not actually
   * invoke the slave JVM.
   * 
   * @param slaveClassName The fully-qualified class name of the 
   * class to start up in the second JVM. This class must implement
   * the interface specified by this class's type parameter, which
   * must be a subclass of {@link SlaveRemote}.
   */
  protected AbstractMasterJVM(String slaveClassName) {
    _slaveClassName = slaveClassName;
    
    // Make sure RMI doesn't use an IP address that might change
    System.setProperty("java.rmi.server.hostname", "127.0.0.1");
  }

  /**
   * Callback for when the slave JVM has connected, and the
   * bidirectional communications link has been established.
   * During this call, {@link #getSlave} is guaranteed to not
   * return null.
   */
  protected abstract void handleSlaveConnected();
  
  /**
   * Callback for when the slave JVM has quit.
   * During this call, {@link #getSlave} is guaranteed to
   * return null.
   * 
   * @param status The exit code returned by the slave JVM.
   */
  protected abstract void handleSlaveQuit(int status);
  
  /**
   * Invokes slave JVM without any JVM arguments.
   * @throws IllegalStateException if slave JVM already connected or
   * startup is in progress.
   */
  protected synchronized final void invokeSlave()
    throws IOException, RemoteException
  {
    invokeSlave(new String[0]);
  }
  
  /**
   * Invokes slave JVM.
   * @param jvmArgs Array of arguments to pass to the JVM on startup
   * @throws IllegalStateException if slave JVM already connected or
   * startup is in progress.
   */
  protected synchronized final void invokeSlave(String[] jvmArgs)
    throws IOException, RemoteException
  {
    if (_startupInProgress) { 
      throw new IllegalStateException("startup is in progress in invokeSlave");
    }
    
    if (_slave != null) {
      throw new IllegalStateException("slave nonnull in invoke: " + _slave);
    }
    
    _startupInProgress = true;
    Thread t = new Thread() {
      public void run() {
        synchronized(AbstractMasterJVM.this) {
          try {
            _stub = UnicastRemoteObject.exportObject(AbstractMasterJVM.this);
            
            // Debug: check that the IP address is 127.0.0.1
            //javax.swing.JOptionPane.showMessageDialog(null, _stub.toString());
          }
          catch (RemoteException re) {
            throw new edu.rice.cs.util.UnexpectedException(re);
          }
          AbstractMasterJVM.this.notify();
        }
      }
    };

    t.start();
    while (_stub == null) {
      try {
        wait();
      }
      catch (InterruptedException ie) {
        throw new edu.rice.cs.util.UnexpectedException(ie);
      }
    }

    _stubFile = File.createTempFile("DrJava-remote-stub", ".tmp");

    // serialize stub to _stubFile
    FileOutputStream fstream = new FileOutputStream(_stubFile);
    ObjectOutputStream ostream = new ObjectOutputStream(fstream);
    ostream.writeObject(_stub);
    ostream.flush();
    fstream.close();
    
    String[] args = new String[] { 
      _stubFile.getAbsolutePath(),
      _slaveClassName
    };
    
    final Process process = 
      ExecJVM.runJVMPropogateClassPath(RUNNER, args, jvmArgs);
    
    // Start a thread to wait for the slave to die
    // When it dies,
    Thread thread = new Thread() {
      public void run() {
        try {
          int status = process.waitFor();
          synchronized(AbstractMasterJVM.this) {
            _slave = null;
            UnicastRemoteObject.unexportObject(AbstractMasterJVM.this, true);
            handleSlaveQuit(status);
          }
        }
        catch (NoSuchObjectException e) {
          throw new edu.rice.cs.util.UnexpectedException(e);
        }
        catch (InterruptedException ie) {
          throw new edu.rice.cs.util.UnexpectedException(ie);
        }
      }
    };
    
    thread.start();
  }

  /**
   * No-op to prove that the master is still alive.
   */
  public void checkStillAlive() {}

  public synchronized void registerSlave(SlaveRemote slave)
    throws RemoteException
  {
    _slave = slave;
    _startupInProgress = false;
    _stubFile.delete();
    _stub = null;
    
    handleSlaveConnected();

    if (_quitOnStartup) {
      // quitSlave was called before the slave registered, so we now act on
      // the deferred quit request.
      _quitOnStartup = false;
      quitSlave();
    }
  }

  /**
   * Quits slave JVM.
   * @throws IllegalStateException if no slave JVM is connected
   */
  protected synchronized final void quitSlave() throws RemoteException {
    if (isStartupInProgress()) {
      // There is a slave to be quit, but we don't have a handle to it yet.
      // Instead we set this flag, which makes it quit immediately after it
      // registers in registerSlave.
      _quitOnStartup = true;
    }
    else if (_slave == null) {
      throw new IllegalStateException("tried to quit when no slave running" +
                                      " and startup not in progress");
    }
    else {
      _slave.quit();
    }
  }
  
  /** Returns slave remote instance, or null if not connected. */
  protected synchronized final SlaveRemote getSlave() {
    return _slave;
  }
  
  /** Returns true if the slave is in the process of starting. */
  protected synchronized boolean isStartupInProgress() {
    return _startupInProgress;
  }
}
