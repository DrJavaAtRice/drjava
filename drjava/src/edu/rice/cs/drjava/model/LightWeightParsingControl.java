/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.util.List;

import edu.rice.cs.drjava.model.definitions.*;

/** Light-weight parsing control.
 *  @version $Id$
 */
public interface LightWeightParsingControl {
  /** Perform light-weight parsing on the document if the delay has been exceeded.
   *  @param doc the document to parse. */
  public void update(OpenDefinitionsDocument doc);
  
  /** Delay the next update. */
  public void delay();
  
  /** Start or stop automatic updates.
   *  @param b  {@code true} to start or {@code false} to stop automatic updates */
  public void setAutomaticUpdates(boolean b);

  /** Reset light-weight parsing. Forget everything. */
  public void reset();
  
  /** Return the last enclosing class name for the specified document, "" if not inside a class, or
   *  null if unknown.
   *  @param doc the document for which we want the information
   *  @return the enclosing class name
   */
  public String getEnclosingClassName(OpenDefinitionsDocument doc);
  
  /** Add the listener to this controller.
   *  @param l listener to add */
  public void addListener(LightWeightParsingListener l);
  
  /** Remove the listener from this controller. */
  public void removeListener(LightWeightParsingListener l);
  
  /** Remove all listeners from this controller. */
  public void removeAllListeners();
  
  /** @return a copy of the list of listeners. */
  public List<LightWeightParsingListener> getListeners();
}