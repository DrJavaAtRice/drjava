/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

import java.io.*;
import java.net.*;
import edu.rice.cs.drjava.config.OptionConstants;

/**
 * Client class to remote control an already running instance of DrJava.
 */
public class RemoteControlClient {
  /** true if a DrJava remote control server is running.
   */
  protected static Boolean _serverRunning = null;
  
  /** Contains the name of the user running the server, or is null if no server is running.
   */
  protected static String _serverUser = null;
  
  /** Time in ms until the client decides the server is not running.
   */
  public static final int REMOTE_CONTROL_TIMEOUT = 250;
  
  /** Return true if a DrJava remote control server is running.
   * @return true if running
   */
  public static synchronized boolean isServerRunning() {
    if (_serverRunning == null) {
      try {
        openFile(null);
      }
      catch(IOException e) { _serverRunning = false; }
    }
    return _serverRunning;
  }
  
  /** Return the name of the user running the server, or null if no server is running.
   * @return user name or null
   */
  public static String getServerUser() { return _serverUser; }
  
  /** Tell the existing DrJava instance to open a file.
   * @param f file, or null to just test if a server is running.
   * @return true if file could be opened
   */
  public static synchronized boolean openFile(File f) throws IOException {
    try {
      // get a datagram socket
      DatagramSocket socket = new DatagramSocket();
      socket.setSoTimeout(REMOTE_CONTROL_TIMEOUT);
      
      // send request
      String dString = RemoteControlServer.QUERY_PREFIX;
      if (f != null) {
        dString = dString + " " + f.getAbsolutePath();
      }
      byte[] buf = dString.getBytes();
      InetAddress address = InetAddress.getByName("127.0.0.1");
      DatagramPacket packet = new DatagramPacket(buf, buf.length, address,
                                                 DrJava.getConfig().getSetting(OptionConstants.REMOTE_CONTROL_PORT));
      socket.send(packet);
      
      // get response
      buf = new byte[512];
      packet = new DatagramPacket(buf, buf.length);
      socket.receive(packet);
      
      // display response
      String received = new String(packet.getData(), 0, packet.getLength());
      _serverRunning = received.startsWith(RemoteControlServer.RESPONSE_PREFIX);
      if (_serverRunning) {
        int pos = received.indexOf('!');
        _serverUser = received.substring(RemoteControlServer.RESPONSE_PREFIX.length(), pos);
      }
      else {
        _serverUser = null;
      }
      socket.close();
      
      return (received.equals(RemoteControlServer.RESPONSE_PREFIX_WITH_USER));
    }
    catch (SocketTimeoutException e) {
      _serverRunning = false;
      return false;
    }
  }
  
  /** Main method for test purposes.
   */
  public static void main(String[] args) {
    for (int i = 0; i < args.length; ++i) {
      try {
        boolean ret = openFile(new File(args[i]));
        System.out.println("openFile returned " + ret);
      }
      catch(IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }
}