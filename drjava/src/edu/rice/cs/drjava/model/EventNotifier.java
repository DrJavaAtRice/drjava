/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2019, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the names of its contributors may be used 
 *      to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software. Open Source Initative Approved is a trademark
 * of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/ or 
 * http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/
package edu.rice.cs.drjava.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Base class for all component-specific EventNotifiers.  This class provides common methods to 
  * manage listeners of a specific type.  T the type of the listener class to be managed.
  * @version $Id$
  */
public abstract class EventNotifier<T> {
  /** All T Listeners that are listening to the model.  Accesses to this collection are protected by the 
    * ReaderWriterLock. The collection relies on "copy-on-write" semantics for _listeners.
    */
  protected final List<T> _listeners = new CopyOnWriteArrayList<T>();
  
  /* Since the listener framework is now implemented using CopyOnWriteArrayList, no readers/writers locking is necessary. */
  
  /** Adds a listener to this notifier.
    * @param listener a listener that reacts on events
    */
  public void addListener(T listener) { _listeners.add(listener); }
  
  /** Removes a listener from this notifier. If the thread already holds the lock,
    * then the listener is removed later, but as soon as possible.
    * Note: It is NOT guaranteed that the listener will not be executed again.
    * @param listener a listener that reacts on events
    */
  public void removeListener(final T listener) { _listeners.remove(listener); }
  
  /** Removes all listeners from this notifier.  If the thread already holds the lock,
    * then the listener is removed later, but as soon as possible.
    * Note: It is NOT guaranteed that the listener will not be executed again. */
  public void removeAllListeners() { _listeners.clear(); }
}
