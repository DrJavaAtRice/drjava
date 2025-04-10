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
package edu.rice.cs.drjava.model.javadoc;

import java.io.File;
import edu.rice.cs.drjava.model.EventNotifier;
import edu.rice.cs.drjava.model.compiler.CompilerListener;

/** * Keeps track of all listeners to a JavadocModel, and has the ability
 * to notify them of some event.
 * <p>
 *
 * This class has a specific role of managing JavadocListeners.  Other
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
 * All methods in this class should use the "copy-on-write" semantics of the _listeners colletion. This protocol
 * ensures that multiple notifications can occur simultaneously while other threads "atomically" modify the _listeners 
 * collection (each using a single method of the CopyOnWriteArrayList class.
 * <p>
 *
 * @version $Id$
 */
class JavadocEventNotifier extends EventNotifier<JavadocListener> implements JavadocListener {
  
  /** Called after Javadoc is started by the GlobalModel. */
  public void javadocStarted() {
    for (JavadocListener jl: _listeners) { jl.javadocStarted(); } 
  }
  
  /** Called after Javadoc is finished.
    * @param success whether the Javadoc operation generated proper output
    * @param destDir  if (success == true) the location where the output was generated, otherwise undefined (null?)
    * @param allDocs Whether Javadoc was run for all open documents
    */
  public void javadocEnded(boolean success, File destDir, boolean allDocs) {
    for (JavadocListener jl: _listeners) { jl.javadocEnded(success, destDir, allDocs); }
  }
  
  /** Asks the user if all files should be saved before running javadoc (assuming the proper listener has been 
    * installed). Does not continue with javadoc if the user fails to save!
    */
  public void saveBeforeJavadoc() {
    for (JavadocListener jl: _listeners) { jl.saveBeforeJavadoc(); }
  }
  
  /** Asks the user if all files should be compiled before running javadoc (assuming the proper listener has been 
    * installed). Does not continue with javadoc if the user fails to save!
    */
  public void compileBeforeJavadoc(final CompilerListener afterCompile) {
    for (JavadocListener jl: _listeners) { jl.compileBeforeJavadoc(afterCompile); }
  }
}

