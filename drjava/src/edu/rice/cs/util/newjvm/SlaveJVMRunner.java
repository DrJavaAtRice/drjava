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
  
  /**
   * The main method for invoking a slave JVM.
   * 
   * @param args Command-line parameters, of which there must be two.
   * The first is the absolute path to the file containing the serialized
   * MasterRemote stub, and the second is the fully-qualified class name
   * of the slave JVM implementation class.
   */
  public static void main(String[] args) {
    if (args.length != 2) System.exit(1);

    try {
      FileInputStream fstream = new FileInputStream(args[0]);
      ObjectInputStream ostream = new ObjectInputStream(fstream);
      MasterRemote remote = (MasterRemote) ostream.readObject();
      
      try {
        Class slaveClass = Class.forName(args[1]);
        SlaveRemote slave = (SlaveRemote) slaveClass.newInstance();
        
        // Must export slave object to RMI so we can pass stub to the master
        SlaveRemote stub= (SlaveRemote) UnicastRemoteObject.exportObject(slave);

        // start the slave and notify the master
        remote.registerSlave(slave);
        slave.start(remote);
      }
      catch (Exception e) {
        System.err.println("Exception while instantiation slave " + args[1]);
        e.printStackTrace();
        //javax.swing.JOptionPane.showMessageDialog(null, e);
        System.exit(3);
      }
    }
    catch (Exception e) {
      System.err.println("Exception while deserializing remote stub");
      e.printStackTrace();
      System.exit(2);
    }
  }
}
