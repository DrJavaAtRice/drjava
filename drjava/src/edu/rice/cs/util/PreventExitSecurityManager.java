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

package edu.rice.cs.util;

import java.security.*;

/**
 * A security manager to prevent exiting the VM indiscriminately.
 * This manager disallows System.exit (unless you call {@link #exitVM}).
 * It also disallows setting a security manager, since this would override
 * the exit prevention!
 *
 * If this security manager is enabled and an exit is attempted,
 * either via System.exit or via {@link #exitVM} when exiting is blocked,
 * a {@link ExitingNotAllowedException} will be thrown.
 *
 * @version $Id$
 */
public class PreventExitSecurityManager extends SecurityManager {
  private static final Permission SET_MANAGER_PERM
    = new RuntimePermission("setSecurityManager");

  private final SecurityManager _parent;

  /** Has an unauthorized exit been attempted? */
  private boolean _exitAttempted = false;

  /** Is it time to exit, for real? */
  private boolean _timeToExit = false;

  /** Are we in exit blocking mode? */
  private boolean _blockExit = false;

  /** Is it time to unset this security manager? */
  private boolean _timeToDeactivate = false;

  /**
   * Creates a PreventExitSecurityManager, delegating all permission checks
   * except for exiting to the given parent manager.
   *
   * @param parent SecurityManager to delegate permission to
   *               This may be null, signifying to allow all.
   */
  private PreventExitSecurityManager(final SecurityManager parent) {
    _parent = parent;
  }

  /**
   * Creates a new exit-preventing security manager, using the previous
   * security manager to delegate to.
   */
  public static PreventExitSecurityManager activate() {
    PreventExitSecurityManager mgr
      = new PreventExitSecurityManager(System.getSecurityManager());
    System.setSecurityManager(mgr);

    return mgr;
  }

  /** Removes this security manager. */
  public void deactivate() {
    _timeToDeactivate = true;
    System.setSecurityManager(_parent);
  }

  /**
   * Exits the VM unless exiting is presently blocked.
   * Blocking exit is used in test cases that want to see if we try to exit.
   */
  public void exitVM(int status) {
    if (! _blockExit) {
      _timeToExit = true;
    }

    System.exit(status);
  }

  /**
   * Sets whether exiting the VM is unconditionally blocked or not.
   * It's useful to block exiting to allow test cases to pretend to exit, just
   * to make sure it would have exited under certain conditions.
   *
   * @param b If true, exiting will never be allowed until set false.
   */
  public void setBlockExit(boolean b) {
    _blockExit = b;
  }

  /**
   * Returns whether a System.exit was attempted since the last time this
   * method was called.
   */
  public boolean exitAttempted() {
    boolean old = _exitAttempted;
    _exitAttempted = false;
    return old;
  }

  /**
   * Disallow setting security manager, but otherwise delegate to parent.
   */
  public void checkPermission(Permission perm) {
    if (perm.equals(SET_MANAGER_PERM)) {
      if (! _timeToDeactivate) {
        throw new SecurityException("Can not reset security manager!");
      }
    }
    else {
      if (_parent != null) {
        _parent.checkPermission(perm);
      }
    }
  }

  public void checkExit(int status) {
    if (! _timeToExit) {
      _exitAttempted = true;
      throw new ExitingNotAllowedException();
    }
  }
}


