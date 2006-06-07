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

import java.net.URL;
import java.rmi.*;
import java.rmi.server.*;

import edu.rice.cs.util.Log;


/** Defines a classloader that can be used across jvm's. */
public class RemoteClassLoader extends ClassLoader implements IRemoteClassLoader {
  
  private final Log _log = new Log("MasterSlave.txt", false);
  
  /** @param c the "parent" classloader. This loader will be used to load any remote requests.  */
  public RemoteClassLoader(ClassLoader c) { 
    super(c); 
    _log.log(this + " constructed");
  }
  
  /** Handles a request to load a remote class. */
  public Class<?> loadRemoteClass(String name) throws ClassNotFoundException, RemoteException {
    return getParent().loadClass(name);
  }
  
  /** Handles a request to get a remote resource. */
  public URL getRemoteResource(String name) throws ClassNotFoundException, RemoteException {
    return getParent().getResource(name);
  }
  
  /** Overridden to support same semantics as UnicastRemoteObject */
  public boolean equals(Object o) {
    if (o.getClass() == getClass())  // o belongs to same class as this
      return o == this;
    if (o instanceof RemoteStub) {
      try {
        Remote stub = RemoteObject.toStub(this);
        return stub.equals(o);
      } 
      catch (NoSuchObjectException nsoe) { /* ignore */ }
    }
    return false;
  }
  
  /** Overridden to support same semantics as UnicastRemoteObject */
  public int hashCode() {
    try {
      Remote stub = RemoteObject.toStub(this);
      return stub.hashCode();
    }
    catch (NoSuchObjectException nsoe) { /* ignore */ }
    return super.hashCode();
  }
}


