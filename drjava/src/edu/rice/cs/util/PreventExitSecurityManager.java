/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.util;

import java.security.*;

/** Not used anymore. <p>
 *  A security manager to prevent exiting the slaveVM indiscriminately.  This manager disallows System.exit 
 *  (unless you call {@link #exitVM}).  It also disallows setting a security manager, since this would override 
 *  the exit prevention!  If this security manager is enabled and an exit is attempted, either via System.exit 
 *  or via {@link #exitVM} when exiting is blocked, a {@link ExitingNotAllowedException} will be thrown.
 *
 * @version $Id$
 */
public class PreventExitSecurityManager extends SecurityManager {
  
  private static final Permission SET_MANAGER_PERM = new RuntimePermission("setSecurityManager");

  private final SecurityManager _parent;

  /** Has an unauthorized exit been attempted? */
  private boolean _exitAttempted = false;

  /** Is it time to exit, for real? */
  private boolean _timeToExit = false;

  /** Are we in exit blocking mode? */
  private boolean _blockExit = false;

  /** Is it time to unset this security manager? */
  private boolean _timeToDeactivate = false;

  /** Creates a PreventExitSecurityManager, delegating all permission checks except for exiting to the given parent 
   *  manager.
   *  @param parent SecurityManager to delegate permission to. This may be null, signifying to allow all.
   */
  private PreventExitSecurityManager(final SecurityManager parent) { _parent = parent;
    edu.rice.cs.util.Log log = new edu.rice.cs.util.Log("secman.txt",true);
    log.log("Creating new PreventExitSecurityManager");
  }

  /** Creates a new exit-preventing security manager, using the previous security manager to delegate to.
   */
  public static PreventExitSecurityManager activate() {
    SecurityManager currentMgr = System.getSecurityManager();
    if (currentMgr instanceof PreventExitSecurityManager) return (PreventExitSecurityManager) currentMgr;
    
    PreventExitSecurityManager mgr = new PreventExitSecurityManager(System.getSecurityManager());
    System.setSecurityManager(mgr);
    return mgr;
  }

  /** Removes this security manager. */
  public void deactivate() {
    _timeToDeactivate = true;
    System.setSecurityManager(_parent);
  }

  /** Exits the VM unless exiting is presently blocked. Blocking exit is used in test cases that want to see if we
   *  try to exit.
   */
  public void exitVM(int status) {
//    Utilities.showDebug("exitVm(" + status + ") called");
    if (! _blockExit) _timeToExit = true;
    System.exit(status);
  }

  /** Sets whether exiting the VM is unconditionally blocked or not. It's useful to block exiting to allow test cases
   *  to pretend to exit, just to make sure it would have exited under certain conditions.
   *  @param b If true, exiting will never be allowed until set false.
   */
  public void setBlockExit(boolean b) { _blockExit = b; }

  /** Returns whether a System.exit was attempted since the last time this method was called. */
  public boolean exitAttempted() {
    boolean old = _exitAttempted;
    _exitAttempted = false;
    return old;
  }

  /** Disallow setting security manager, but otherwise delegate to parent. */
  public void checkPermission(Permission perm) {
    if (perm.equals(SET_MANAGER_PERM)) {
      if (! _timeToDeactivate) throw new SecurityException("Can not reset security manager!");
    }
    else {
      if (_parent != null) _parent.checkPermission(perm);
    }
  }

  public void checkExit(int status) {
    if (! _timeToExit) {
      _exitAttempted = true;
      throw new ExitingNotAllowedException();
    }
  }
}


