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
import java.rmi.server.UnicastRemoteObject;

/**
 * This class is used for its {@link #main} method, which is used
 * when a new slave JVM is invoked. See the main method documentation
 * for information on the command line parameters this class requires.
 * 
 * If there is an error setting up the slave JVM before the RMI
 * links can be established, this JVM process will exit with an error
 * code according to the following list:
 * <DL>
 * <DT>1</DT><DD>Invalid number of command line arguments.</DD>
 * <DT>2</DT><DD>Error deserializing remote stub</DD>
 * <DT>3</DT><DD>Error instantiating slave implementation class</DD>
 * </DL>
 * If the slave JVM completes successfully, it will exit with code 0.
 *
 * @version $Id$
 */
public final class SlaveJVMRunner {

  /** Private constructor to prevent instantiation. */
  private SlaveJVMRunner() {}

  private static SlaveRemote _getInstance(Class clazz) throws Exception {
    try {
      return (SlaveRemote) clazz.getField("ONLY").get(null);
    }
    catch (Throwable t) {
      return (SlaveRemote) clazz.newInstance();
    }
  }

  /**
   * The main method for invoking a slave JVM.
   * 
   * @param args Command-line parameters, of which there must be two.
   * The first is the absolute path to the file containing the serialized
   * MasterRemote stub, and the second is the fully-qualified class name
   * of the slave JVM implementation class.
   */
  public static void main(String[] args) {
    // Make sure RMI doesn't use an IP address that might change
    System.setProperty("java.rmi.server.hostname", "127.0.0.1");

    if (args.length != 2) System.exit(1);

    try {
      FileInputStream fstream = new FileInputStream(args[0]);
      ObjectInputStream ostream = new ObjectInputStream(fstream);
      MasterRemote remote = (MasterRemote) ostream.readObject();
      
      try {
        Class slaveClass = Class.forName(args[1]);
        SlaveRemote slave = _getInstance(slaveClass);
        
        // Must export slave object to RMI so we can pass stub to the master
        SlaveRemote stub= (SlaveRemote) UnicastRemoteObject.exportObject(slave);
        
        // Debug: check that the IP address is 127.0.0.1
        //javax.swing.JOptionPane.showMessageDialog(null, stub.toString());

        // start the slave and then notify the master
        slave.start(remote);
        remote.registerSlave(slave);
      }
      catch (Throwable e) {
        System.err.println("Exception while instantiating slave " + args[1]);
        e.printStackTrace();
        //javax.swing.JOptionPane.showMessageDialog(null, e.toString());
        System.exit(3);
      }
    }
    catch (Throwable e) {
      System.err.println("Exception while deserializing remote stub");
      e.printStackTrace();
      System.exit(2);
    }
  }
}
