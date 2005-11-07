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

import java.rmi.*;
import edu.rice.cs.util.FileOps;
import java.io.*;
import java.net.URL;

/**
 * defines a classloader that can be used to override a system classloader via commandline
 * <br>
 * example jvm argument: "java -Djava.system.class.loader=edu.rice.cs.util.newjvm.CustomSystemClassLoader"
 * <br>
 * <br>
 * this class will forward all calls to the default system class loader until a setMasterRemote() is called.
 * then all classes that cannot be loaded locally will try to be loaded using the master remote loader.
 * if the master remote loader cannot load the class, then a ClassNotFoundException is thrown.
 * 
 */
public class CustomSystemClassLoader extends ClassLoader{
  /**
   * the remote classloader to use if we cannot load
   * the class locally
   */
  IRemoteClassLoader _master;
  
  /**
   * standard constructor
   * @param c the default system classloader
   */
  public CustomSystemClassLoader(ClassLoader c){
    super(c);
    _master = null;
  }
  
  /**
   * sets the remote class loader
   */
  public void setMasterRemote(IRemoteClassLoader m){
    _master = m;
  }
  
  
  /**
   * try to load a class
   * if the class is already loaded, or is a system class, load it locally.
   * otherwise, if the class cannot be found, try to load the class remotely
   * if we fail, throw a classnotfoundexception
   */
  public Class<?> loadClass(String name) throws ClassNotFoundException{
    Class c;
    // check if i already loaded it
    c = findLoadedClass(name);
    if (c!= null) {
      return c;
    }
    // try to load locally
    try{
      String fileName = name.replace('.', '/') + ".class";
      URL resource = getParent().getResource(fileName); // only dependency on newLoader!
      if (resource == null) {
        throw new ClassNotFoundException("Resource not found: " + fileName);
      }
      else if(fileName.startsWith("edu/rice/cs/util/newjvm/SlaveJVMRunner.class")){
        byte[] data = FileOps.readStreamAsBytes(resource.openStream());
        try { return defineClass(name, data, 0, data.length); }
        catch (Error t) { throw t; }
      }
      // the system couldn't find it, so let's try something else
      return getParent().loadClass(name);
    }
    catch(ClassNotFoundException e) { /* the system couldn't find it, so let's try something else */ }
    catch(IOException e) { /* the system couldn't find it, so let's try something else */ }
    
    // try to load remotely
    try {
      if (_master != null) {
        String fileName = name.replace('.', '/') + ".class";
        URL resource = _master.getRemoteResource(fileName); // only dependency on newLoader!
        if (resource == null) {
          throw new ClassNotFoundException("Resource not found: " + fileName);
        }
        else {
          byte[] data = FileOps.readStreamAsBytes(resource.openStream());
          try { return defineClass(name, data, 0, data.length); }
          catch (Error t) { throw t; }
        }
      }
      else throw new ClassNotFoundException();
    }
    catch(RemoteException e) { throw new ClassNotFoundException(); }
    catch(IOException e) { throw new ClassNotFoundException(); }
  }
}




