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

package edu.rice.cs.drjava.ui.avail;

import edu.rice.cs.drjava.model.EventNotifier;

/**
 * Convenience methods for starting and finishing tasks that use multiple
 * components.
 *
 * <i>No</i> methods on this class should be synchronized using traditional
 * Java synchronization!
 * <p>
 *
 * @version $Id: DefaultGUIAvailabilityNotifier.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class DefaultGUIAvailabilityNotifier extends GUIAvailabilityNotifier {
  /** Set the availability of tasks needed for JUnit, i.e. JUNIT and COMPILER.
    * The availabilities can be nested.
    * 
    * JUNIT is the main component, so its availability is necessarily changed.
    * JUNIT may invoke the COMPILER, so its availability is changed too.
    * @param available true to make available, false to make unavailable */
  public void junit(boolean available) {
    // LOG.log("junit "+available, new RuntimeException());
    availabilityChanged(GUIAvailabilityListener.ComponentType.JUNIT, available);
    availabilityChanged(GUIAvailabilityListener.ComponentType.COMPILER, available);
    // availabilityChanged(GUIAvailabilityListener.ComponentType.INTERACTIONS, available);
  }

  /** Make the tasks needed for JUnit, i.e. JUNIT and COMPILER, unavailable. */ 
  public void junitStarted() { junit(false); }

  /** Make the tasks needed for JUnit, i.e. JUNIT and COMPILER, available. */ 
  public void junitFinished() { junit(true); }  

  /** Set the availability of tasks needed for Javadoc, i.e. JAVADOC and COMPILER.
    * The availabilities can be nested.
    * 
    * JAVADOC is the main component, so its availability is necessarily changed.
    * JAVADOC may invoke the COMPILER, so its availability is changed too.
    * @param available true to make available, false to make unavailable */
  public void javadoc(boolean available) {
    // LOG.log("javadoc "+available, new RuntimeException());
    availabilityChanged(GUIAvailabilityListener.ComponentType.JAVADOC, available);
    availabilityChanged(GUIAvailabilityListener.ComponentType.COMPILER, available);
  }

  /** Make the tasks needed for Javadoc, i.e. JAVADOC and COMPILER, unavailable. */ 
  public void javadocStarted() { javadoc(false); }

  /** Make the tasks needed for Javadoc, i.e. JAVADOC and COMPILER, unavailable. */ 
  public void javadocFinished() { javadoc(true); }  
}
