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

package edu.rice.cs.drjava.model.debug;

import com.sun.jdi.*;

/**
 * Class for keeping track of the currently running threads.
 * @version $Id$
 */
public class DebugThreadData {
  private ThreadReference _thread;
  private String _name;
  private String _status;
  
  /**
   * Object for keeping track of a thread in the debuggee JVM.
   * @param thread JPDA's reference to the thread
   */
  public DebugThreadData(ThreadReference thread) {
    _thread = thread;
    _name = _thread.name();
    String status = "(unknown)";
    switch (_thread.status()) {
      case ThreadReference.THREAD_STATUS_MONITOR: 
        status = "MONITOR"; break;
      case ThreadReference.THREAD_STATUS_NOT_STARTED:
        status = "NOT STARTED"; break;
      case ThreadReference.THREAD_STATUS_RUNNING:
        status = "RUNNING"; break;
      case ThreadReference.THREAD_STATUS_SLEEPING:
        status = "SLEEPING"; break;
      case ThreadReference.THREAD_STATUS_UNKNOWN:
        status = "UNKNOWN"; break;
      case ThreadReference.THREAD_STATUS_WAIT:
        status = "WAIT"; break;
      case ThreadReference.THREAD_STATUS_ZOMBIE:
        status = "ZOMBIE"; break;
    }
    _status = status;
  }
  
  /**
   * Returns the name of this thread.
   */
  public String getName() {
    return _name;
  }
  
  /**
   * Returns the status of this thread (at the time of this object's construction)
   */
  public String getStatus() {
    return _status;
  }
}