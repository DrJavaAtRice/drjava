/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava;

/**
 * Main controller class for DrJava.  This class sets up all of the appropriate
 * cross-references between components at runtime.  Almost all of the function-
 * -ality of DrJava is hidden in components with extremely high-level interfaces.
 * This class ensures that each component is registered with all other components
 * that need to call its methods.  In essence, this class manages component
 * associations.  Any details more low-level than who talks to who should be
 * handled by components through their interfaces.
 * @version $Id$
 */
public class MainController {
  /*
   * Things that MainController needs to manage:
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
   * Behaviors that MainController will support:
   *   Lock edits
   *   Display Message (warning, error)
   *   Display File Dialog (open, save, select directory)
   */
}