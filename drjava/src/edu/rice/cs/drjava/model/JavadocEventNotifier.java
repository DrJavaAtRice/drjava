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

package edu.rice.cs.drjava.model;

import java.io.File;

/**
 * Keeps track of all listeners to a JavadocModel, and has the ability
 * to notify them of some event.
 *
 * This class has a specific role of managing JavadocListeners.  Other
 * classes with similar names use similar code to perform the same function for
 * other interfaces, e.g. InteractionsEventNotifier and GlobalEventNotifier.
 * These classes implement the appropriate interface definition so that they
 * can be used transparently as composite packaging for a particular listener
 * interface.
 *
 * Components which might otherwise manage their own list of listeners use
 * EventNotifiers instead to simplify their internal implementation.  Notifiers
 * should therefore be considered a private implementation detail of the
 * components, and should not be used directly outside of the "host" component.
 *
 * All methods in this class must use the synchronization methods
 * provided by ReaderWriterLock.  This ensures that multiple notifications
 * (reads) can occur simultaneously, but only one thread can be adding
 * or removing listeners (writing) at a time, and no reads can occur
 * during a write.
 *
 * <i>No</i> methods on this class should be synchronized using traditional
 * Java synchronization!
 *
 * @version $Id$
 */
class JavadocEventNotifier extends EventNotifier<JavadocListener>
    implements JavadocListener {

  // -------------------- READER METHODS --------------------

  /**
   * Called after Javadoc is started by the GlobalModel.
   */
  public void javadocStarted() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).javadocStarted();
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called after Javadoc is finished.
   * @param success whether the Javadoc operation generated proper output
   * @param destDir if (success == true) the location where the output was
   *                generated, otherwise undefined (possibly null)
   * @param allDocs Whether Javadoc was run for all open documents
   */
  public void javadocEnded(boolean success, File destDir, boolean allDocs) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).javadocEnded(success, destDir, allDocs);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called before attempting Javadoc, to give users a chance to save.
   * Do not continue with Javadoc if the user doesn't save!
   */
  public void saveBeforeJavadoc() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).saveBeforeJavadoc();
      }
    }
    finally {
      _lock.endRead();
    }
  }
}

