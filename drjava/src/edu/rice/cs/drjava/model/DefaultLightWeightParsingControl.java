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

package edu.rice.cs.drjava.model;

import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import javax.swing.text.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.ui.DrJavaErrorHandler;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.swing.Utilities;

/** Default light-weight parsing control.  This class is declared final because it cannot be robustly subclassed because
  * the constructor starts a thread.
  * @version $Id: DefaultLightWeightParsingControl.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class DefaultLightWeightParsingControl implements LightWeightParsingControl {
  /** The model. */
  private AbstractGlobalModel _model;
  
  /** The time at which updates may be performed. */
  private long _beginUpdates;
  
  /** The time at which the last delay operation was performed. */
  private long _lastDelay = System.currentTimeMillis();
  
  /** Last updates for the documents. */
  private HashMap<OpenDefinitionsDocument, Long> _lastUpdates = new HashMap<OpenDefinitionsDocument, Long>();

  /** Enclosing class names for the documents. */
  private HashMap<OpenDefinitionsDocument, String> _enclosingClassNames = new HashMap<OpenDefinitionsDocument, String>();
  
  /** Flag to stop automatic updates. */
  private volatile boolean _running = false;
  
  /** Monitor to restart automatic updates. */
  private Object _restart = new Object();
  
  /** List of listeners. */
  private LinkedList<LightWeightParsingListener> _listeners = new LinkedList<LightWeightParsingListener>();
  
  /** Log file. */
  private static final Log _log = new Log("LightWeightParsing", false);
  
  /** Thread group for the updater. */
  private ThreadGroup _updaterThreadGroup = new ThreadGroup("Light-weight parsing updater thread group") {
    public void uncaughtException(Thread t, Throwable e) {
      _log.log("Uncaught exception in updater; disabled for rest of session", e);
      DrJavaErrorHandler.record(e);
    }
  };
  
  /** Thread to perform automatic updates. */
  private Thread _updater = new Thread(_updaterThreadGroup, new Runnable() {
    public void run() {
      while(true) { // this is ok, it's a daemon thread and will die when all other threads have died
        while (!_running) {
          _log.log("Waiting...");
          try { synchronized(_restart) { if (! _running)  _restart.wait(); } }
          catch(InterruptedException e) { }
        }
        long current = System.currentTimeMillis();
        long delta = (_beginUpdates-current);
        // _log.logTime("Begin updates at " + _beginUpdates + " (delta=" + delta + ")");
        if (current>=_beginUpdates) {
          OpenDefinitionsDocument doc = _model.getActiveDocument();
          Long last = _lastUpdates.get(doc);
          if ((last == null) || (last < _lastDelay)) {
            update(doc);
            // _log.logTime("Update done.");
          }
          else {
            // _log.logTime("Not updating, last update was at " + last);
          }
          delta = DrJava.getConfig().getSetting(OptionConstants.DIALOG_LIGHTWEIGHT_PARSING_DELAY).intValue();
        }
        // _log.logTime("Not updating, sleeping for " + delta);
        try {
          Thread.sleep(delta);
        }
        catch (InterruptedException e) { /* ignore, just wake up earlier and retry */ }
      }
    }
  });
  
  /** Create the default light-weight parsing control.  Starts a new thread.
    * @param model the model */
  public DefaultLightWeightParsingControl(AbstractGlobalModel model) {
    _model = model;
    _updater.setDaemon(true);
    _updater.start();
  }
  
  /** Perform light-weight parsing. */
  public synchronized void update(final OpenDefinitionsDocument doc) {
    _log.log("Update for " + doc);
    try {
      _lastUpdates.put(doc, System.currentTimeMillis());
      final String old = _enclosingClassNames.get(doc);
      final String updated = doc.getEnclosingClassName(doc.getCurrentLocation(), true);
      if ((old == null) || (!old.equals(updated))) {
        _enclosingClassNames.put(doc, updated);
        Utilities.invokeLater(new Runnable() {
          public void run() {
            List<LightWeightParsingListener> listeners = getListeners();
            for (LightWeightParsingListener l: listeners) { l.enclosingClassNameUpdated(doc, old, updated); }
          }
        });
      }
    }
    catch(BadLocationException e) { /* ignore */ }
    catch(ClassNameNotFoundException e) { /* ignore */ }
  }
  
  /** Start or stop automatic updates.
    * @param b  {@code true} to start or {@code false} to stop automatic updates
    */
  public void setAutomaticUpdates(boolean b) {
    _log.log("setAutomaticUpdates(" + b + ")");
    _running = b;
    if (b) {
      delay();
      synchronized(_restart) {
        _restart.notify();
      }
    }
  }
  
  /** Delay the next update. */
  public void delay() {
    _lastDelay = System.currentTimeMillis();
    _beginUpdates = _lastDelay + (DrJava.getConfig().getSetting(OptionConstants.DIALOG_LIGHTWEIGHT_PARSING_DELAY).intValue());
  }
  
  /** Reset light-weight parsing. Forget everything. */
  public synchronized void reset() {
    for(final OpenDefinitionsDocument doc: _enclosingClassNames.keySet()) {
      final String old = _enclosingClassNames.get(doc);
      Utilities.invokeLater(new Runnable() {
        public void run() {
          List<LightWeightParsingListener> listeners = getListeners();
          for (LightWeightParsingListener l: listeners) { l.enclosingClassNameUpdated(doc, old, null); }
        }
      });
    }
    _enclosingClassNames.clear();
    _lastUpdates.clear();
  }
  
  /** Return the last enclosing class name for the specified document, "" if not inside a class, or
    * null if unknown.
    * WARNING: In long source files and when contained in anonymous inner classes, this function might take a LONG time.
    * @param doc the document for which we want the information
    * @return the enclosing class name
    */
  public synchronized String getEnclosingClassName(OpenDefinitionsDocument doc) { return _enclosingClassNames.get(doc); }
  
  /** Add the listener to this controller.
    * @param l listener to add */
  public synchronized void addListener(LightWeightParsingListener l) { _listeners.add(l); }
  
  /** Remove the listener from this controller. */
  public synchronized void removeListener(LightWeightParsingListener l) { _listeners.remove(l); }
  
  /** Remove all listeners from this controller. */
  public synchronized void removeAllListeners() { _listeners.clear(); }  
  
  /** @return a copy of the list of listeners. */
  public synchronized List<LightWeightParsingListener> getListeners() {
    return new LinkedList<LightWeightParsingListener>(_listeners);
  }
}