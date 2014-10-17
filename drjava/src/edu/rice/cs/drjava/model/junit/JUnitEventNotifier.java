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

package edu.rice.cs.drjava.model.junit;

import edu.rice.cs.drjava.model.EventNotifier;
import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import java.util.List;

/**
 * Keeps track of all listeners to a JUnitModel, and has the ability
 * to notify them of some event.
 * <p>
 *
 * This class has a specific role of managing JUnitListeners.  Other
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
 * @version $Id: JUnitEventNotifier.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
class JUnitEventNotifier extends EventNotifier<JUnitListener> implements JUnitListener {
  
  public void addListener(JUnitListener jul) {
    super.addListener(jul);
//    Utilities.show("Adding listener " + jul + " to listener list in " + this);
  }
  
  /** Called when trying to test a non-TestCase class.
    * @param isTestAll whether or not it was a use of the test all button
    * @param didCompileFail whether or not a compile before this JUnit attempt failed
    */
  public void nonTestCase(boolean isTestAll, boolean didCompileFail) {
    _lock.startRead();
    try { for (JUnitListener jul : _listeners) { jul.nonTestCase(isTestAll, didCompileFail); } }
    finally { _lock.endRead(); }
  }
  
  public void classFileError(ClassFileError e) {
    _lock.startRead();
    try { for (JUnitListener jul : _listeners) { jul.classFileError(e); } }
    finally { _lock.endRead(); }
  }
  
  /** Called before JUnit is started by the DefaultJUnitModel. */
  public void compileBeforeJUnit(final CompilerListener cl, List<OpenDefinitionsDocument> outOfSync) {
    _lock.startRead();
    try { for (JUnitListener jul : _listeners) { jul.compileBeforeJUnit(cl, outOfSync); } }
    finally { _lock.endRead(); }
  }
  
  /** Called after junit/junitAll is started by the GlobalModel. */
  public void junitStarted() {
    _lock.startRead();
    try { for (JUnitListener jul : _listeners) { jul.junitStarted(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called after junitClasses is started by the GlobalModel. */
  public void junitClassesStarted() {
    _lock.startRead();
    try { for (JUnitListener jul : _listeners) { jul.junitClassesStarted(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called to indicate that a suite of tests has started running.
    * @param numTests The number of tests in the suite to be run.
    */
  public void junitSuiteStarted(int numTests) {
    _lock.startRead();
    try { for (JUnitListener jul : _listeners) { jul.junitSuiteStarted(numTests); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when a particular test is started.
    * @param name The name of the test being started.
    */
  public void junitTestStarted(String name) {
    _lock.startRead();
    try { for (JUnitListener jul : _listeners) { jul.junitTestStarted(name); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when a particular test has ended.
    * @param name The name of the test that has ended.
    * @param wasSuccessful Whether the test passed or not.
    * @param causedError If not successful, whether the test caused an error or simply failed.
    */
  public void junitTestEnded(String name, boolean wasSuccessful, boolean causedError) {
    _lock.startRead();
    try { for (JUnitListener jul : _listeners) { jul.junitTestEnded(name, wasSuccessful, causedError); } }
    finally { _lock.endRead(); }
  }
  
  /** Called after JUnit is finished running tests. */
  public void junitEnded() {
    _lock.startRead();
    try { for(JUnitListener jul : _listeners) { jul.junitEnded(); } }
    finally { _lock.endRead(); }
  }
}

