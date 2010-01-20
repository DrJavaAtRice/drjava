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

import edu.rice.cs.drjava.ui.MainFrame;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.drjava.config.OptionConstants;

import java.io.*;
import java.net.*;

/** This class contains a server that monitors incoming datagrams on port 4444
  * (default; can be changed in OptionConstants.REMOTE_CONTROL_PORT).
  * These datagrams can contain commands to open additional files.
  * 
  * A client can query whether a server is running by sending QUERY_PREFIX.
  * If a server is running, it will respond with RESPONSE_PREFIX.
  * 
  * A client can tell a server to open a file by sending QUERY_PREFIX + " " + absoluteFileName.
  * The server will respond with RESPONSE_PREFIX, or RESPONSE_PREFIX + " " + error if an error occurred.
  * 
  * A client can tell a server to open a file and jump to a certain line number by sending QUERY_PREFIX + " " + 
  * absoluteFileName + File.pathSeparator + lineNumber.
  * The server will respond with RESPONSE_PREFIX, or RESPONSE_PREFIX + " " + error if an error occurred.
  * 
  * This class is declared final because it cannot be robustly subclassed because the constructor starts a thread.
  */
public final class RemoteControlServer {
  /** Prefix of a legitimate query by a client. */
  public static final String QUERY_PREFIX = "DrJava Remote Control?";
  
  /** Prefix of a legitimate response by this server. */
  public static final String RESPONSE_PREFIX = "DrJava Remote Control ";
  
  /** Prefix of a legitimate response by this server, including the user name. */
  public static final String RESPONSE_PREFIX_WITH_USER = RESPONSE_PREFIX+System.getProperty("user.name") + "!";
  
  /** Create a new remote control server, running in its own daemon thread.
    * @param frame main frame
    */
  public RemoteControlServer(MainFrame frame) throws IOException {
    RCServerThread rcsThread = new RCServerThread(frame);
    rcsThread.setDaemon(true);
    rcsThread.start();
  }
  
  /** Thread class for the server. */
  public static class RCServerThread extends Thread {
    /** Main frame access so the server can open files, etc. */
    protected MainFrame _frame;
    
    /** Socket used. */
    protected DatagramSocket socket = null;
    
    /** Create a new server thread.
      * @param frame main frame
      */
    public RCServerThread(MainFrame frame) throws IOException {
      this("RCServerThread", frame);
    }
    
    /**
     * Create a new server thread with a specified name.
     * @param name thread name
     * @param frame main frame
     */
    public RCServerThread(String name, MainFrame frame) throws IOException {
      super(name);
      _frame = frame;
      socket = new DatagramSocket(DrJava.getConfig().getSetting(OptionConstants.REMOTE_CONTROL_PORT));
    }

    /**
     * Main method of the thread. It loops indefinitely, waiting for queries.
     * Since this is a daemon thread, it will get shut down at the end.
     */
    public void run() {
      while (true) {
        try {
          byte[] buf = new byte[256];
          
          // receive request
          DatagramPacket packet = new DatagramPacket(buf, buf.length);
          socket.receive(packet);
          
          String request = new String(packet.getData(), 0, packet.getLength());
          
          // check if it was a legitimate query
          if (request.startsWith(QUERY_PREFIX)) {
            // construct response
            String dString = RESPONSE_PREFIX_WITH_USER;
            request = request.substring(QUERY_PREFIX.length());
            
            // check if a file was specified
            if ((request.length() > 0) && (request.charAt(0) == ' ')) {
              request = request.substring(1);
              
              // check if the request contained a line number
              int lineNo = -1;
              int pathSepIndex = request.indexOf(File.pathSeparatorChar);
              if (pathSepIndex >= 0) {
                try {
                  lineNo = Integer.valueOf(request.substring(pathSepIndex+1));
                }
                catch(NumberFormatException nfe) {
                  lineNo = -1;
                }
                request = request.substring(0,pathSepIndex);
              }
              
              final File f = new File(request);
              if (f.exists()) {
                DrJavaRoot.handleRemoteOpenFile(f, lineNo);
              }
            }
            else {
              dString = dString + " Cannot open file!";
            }
            
            buf = dString.getBytes();
            
            // send the response to the client at "address" and "port"
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            
            socket.send(packet);
          }
        }
        catch (SocketTimeoutException e) {
          // ignore
        }
        catch (IOException e) { e.printStackTrace(); }
      }
    }
    
    protected void finalize() { if (socket != null) socket.close(); }
  }
  
  /** Main method for test purposes. */
  public static void main(String[] args) {
    try {
      (new RCServerThread(null)).start();
    }
    catch(IOException ioe) {
      System.out.println(ioe);
      ioe.printStackTrace();
    }
  }
}
