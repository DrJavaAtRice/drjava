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

package edu.rice.cs.drjava;

/** Main controller class for DrJava.  This class sets up all of the appropriate cross-references between components at 
  * runtime.  Almost all of the functionality of DrJava is hidden in components with extremely high-level interfaces.
  * This class ensures that each component is registered with all other components that need to call its methods.  In 
  * essence, this class manages component associations.  Any details more low-level than who talks to who should be
  * handled by components through their interfaces.
  * @version $Id: MainController.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class MainController {
  /* Things that MainController needs to manage:
   *   DJWindow [aka: MainFrame] (register view components)
   *   Menu Bar Manager (register with DJWindow [+ ViewerWindows + AboutWindow on Mac])
   *     Menus (register with MBM, add Actions)
   *       File, Edit, Tools, Debugger, View, Documents, Help
   *   Tool Bar (register with DJWindow, add Actions)
   *   Document List (register with DJWindow + View Menu + Documents Menu)
   *   Document Pane (register with DJWindow + DocumentList + Documents Menu + Debugger)
   *   Tab Manager (register with DJWindow + View Menu, add Tabs)
   *     Tabs (register with TM)
   *       Interactions, Console, Compiler, JUnit, Javadoc, FindBugs, Find/Replace
   *   Debugger (register with DJWindow [or maybe TM?])
   *   ViewerWindow
   *     Help, Javadoc HTML
   *   AboutWindow
   *   PreferencesWindow (to be refactored in another pass)
   *   Configuration
   * 
   *   All ErrorCaretListeners need to be aware of each other.
   * 
   * Behaviors that MainController will support:
   *   Lock edits
   *   Display Message (warning, error)
   *   Display File Dialog (open, save, select directory)
   */
}