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

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.util.List;

import edu.rice.cs.drjava.model.EventNotifier;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * Keeps track of all listeners to a CompilerModel, and has the ability
 * to notify them of some event.
 * <p>
 *
 * This class has a specific role of managing CompilerListeners.  Other
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
 * @version $Id: CompilerEventNotifier.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
class CompilerEventNotifier extends EventNotifier<CompilerListener> implements CompilerListener {
  
  /** Called after a compile is started by the GlobalModel. */
  public void compileStarted() {
//    new ScrollableDialog(null, "CompilerEventNotifier.compileStarted() called for listeners " + _listeners, "", "").show();
    _lock.startRead();
    try { for (CompilerListener cl : _listeners) { cl.compileStarted(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when a compile has finished running. */
  public void compileEnded(File workDir, List<? extends File> excludedFiles) {
    _lock.startRead();
    try { for (CompilerListener cl : _listeners) { cl.compileEnded(workDir, excludedFiles); } }
    finally { _lock.endRead(); }
  }

  /** Called if the compile cannot be performed. By default, the Exception is an UnexpectedException containing an
    * explanatory message.
    */
  public void compileAborted(Exception e) {
    _lock.startRead();
    try { for (CompilerListener cl : _listeners) { cl.compileAborted(e); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when files are saved before compiling. It is up to the caller of this method to check if the 
    * documents have been saved, using IGetDocuments.hasModifiedDocuments().
    */
  public void saveBeforeCompile() {
    _lock.startRead();
    try { for (CompilerListener cl : _listeners) { cl.saveBeforeCompile(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when files are saved before compiling. It is up to the caller of this method to check if the 
    * documents have been saved, using IGetDocuments.hasModifiedDocuments().
    */
  public void saveUntitled() {
    _lock.startRead();
    try { for (CompilerListener cl : _listeners) { cl.saveUntitled(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called after the active compiler has been changed. */
  public void activeCompilerChanged() {
//    new ScrollableDialog(null, "CompilerEventNotifier.compileStarted() called for listeners " + _listeners, "", "").show();
    _lock.startRead();
    try { for (CompilerListener cl : _listeners) { cl.activeCompilerChanged(); } }
    finally { _lock.endRead(); }
  }
}
