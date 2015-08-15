/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.javadoc;

import java.io.File;
import edu.rice.cs.drjava.model.EventNotifier;
import edu.rice.cs.drjava.model.compiler.CompilerListener;

/**
 * Keeps track of all listeners to a ScaladocModel, and has the ability
 * to notify them of some event.
 * <p>
 *
 * This class has a specific role of managing ScaladocListeners.  Other
 * classes with similar names use similar code to perform the same function for
 * other interfaces, e.g. InteractionsEventNotifier and GlobalEventNotifier.
 * These classes implement the appropriate interface definition so that they
 * can be used transparently as composite packaging for a particular listener
 * interface.
 * <p>
 *
 * Components which might otherwise manage their own list of listeners use
 * EventNotifiers instead to simplify their internal implementation.  Notifiers
 * should therefore be considered a private implementation detail of the
 * components, and should not be used directly outside of the "host" component.
 * <p>
 *
 * All methods in this class must use the synchronization methods
 * provided by ReaderWriterLock.  This ensures that multiple notifications
 * (reads) can occur simultaneously, but only one thread can be adding
 * or removing listeners (writing) at a time, and no reads can occur
 * during a write.
 * <p>
 *
 * <i>No</i> methods on this class should be synchronized using traditional
 * Java synchronization!
 * <p>
 *
 * @version $Id: ScaladocEventNotifier.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
class ScaladocEventNotifier extends EventNotifier<ScaladocListener>
  implements ScaladocListener {
  
  /** Called after Scaladoc is started by the GlobalModel. */
  public void scaladocStarted() {
    _lock.startRead();
    try { for (ScaladocListener jl: _listeners) { jl.scaladocStarted(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called after Scaladoc is finished.
    * @param success whether the Scaladoc operation generated proper output
    * @param destDir  if (success == true) the location where the output was generated, otherwise undefined (null?)
    * @param allDocs Whether Scaladoc was run for all open documents
    */
  public void scaladocEnded(boolean success, File destDir, boolean allDocs) {
    _lock.startRead();
    try { for (ScaladocListener jl: _listeners) { jl.scaladocEnded(success, destDir, allDocs); } }
    finally { _lock.endRead();}
  }
  
  /** Asks the user if all files should be saved before running scaladoc (assuming the proper listener has been 
    * installed). Does not continue with scaladoc if the user fails to save!
    */
  public void saveBeforeScaladoc() {
    _lock.startRead();
    try { for (ScaladocListener jl: _listeners) { jl.saveBeforeScaladoc(); } }
    finally { _lock.endRead(); }
  }
  
  /** Asks the user if all files should be compiled before running scaladoc (assuming the proper listener has been 
    * installed). Does not continue with scaladoc if the user fails to save!
    */
  public void compileBeforeScaladoc(final CompilerListener afterCompile) {
    _lock.startRead();
    try { for (ScaladocListener jl: _listeners) { jl.compileBeforeScaladoc(afterCompile); } }
    finally { _lock.endRead(); }
  }
}

