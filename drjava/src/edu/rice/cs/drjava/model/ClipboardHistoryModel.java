/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model;

import java.util.List;
import java.util.LinkedList;

/** Model class for clipboard history. */
public class ClipboardHistoryModel {
  /** Maximum size of the history. */
  private int _maxSize;
  
  /** The history of strings. The most recent string will be the last. */
  private LinkedList<String> _history = new LinkedList<String>();
  
  /** Singleton instance. */
  private static ClipboardHistoryModel ONLY = null;
  
  /** Singleton accessor. */
  public static synchronized ClipboardHistoryModel singleton() {
    if (ONLY == null) ONLY = new ClipboardHistoryModel(10);
    return ONLY;
  }
  
  /** Create a new clipboard history model.
    * @param maxSize maximum size of history */
  private ClipboardHistoryModel(int maxSize) {
    _maxSize = maxSize;
  }
  
  /** Sets the maximum size. May kick old strings out. */
  public void resize(int maxSize) {
    _maxSize = maxSize;
    while (_history.size()>_maxSize) { _history.removeFirst(); }
  }
  
  /** Add a string to the history. If it is already in the history, it will get
    * moved to the end, making it the most recent string. */
  public synchronized void put(String s) {
    _history.remove(s);
    _history.add(s);
    while (_history.size()>_maxSize) { _history.removeFirst(); }
  }
  
  /** Return a copy of the history of strings. */
  public synchronized List<String> getStrings() {
    return new LinkedList<String>(_history);
  }
  
  /** Return the most recent string, or null if nothing is in the history. */
  public synchronized String getMostRecent() {
    if (_history.size() == 0) { return null; }
    else { return _history.getLast(); }
  }
}
