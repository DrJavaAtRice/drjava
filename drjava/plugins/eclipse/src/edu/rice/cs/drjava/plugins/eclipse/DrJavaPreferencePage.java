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

package edu.rice.cs.drjava.plugins.eclipse;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A preference page that allows the user to customize settings for
 * the DrJava Plug-in for Eclipse.
 * @version $Id$
 */
public class DrJavaPreferencePage extends FieldEditorPreferencePage
  implements IWorkbenchPreferencePage {
  
  /**
   * Constructs a new preference page for DrJava's options.
   */
  public DrJavaPreferencePage() {
    super("DrJava", GRID);  // title, layout
  }
  
  /**
   * Initializes the preference page (does nothing).
   * (Method declared on IWorkbenchPreferencePage)
   */
  public void init(IWorkbench workbench) {
  }
  
  /**
   * Creates the fields to be edited on this page.
   */
  protected void createFieldEditors() {
    addField(new BooleanFieldEditor(DrJavaConstants.INTERACTIONS_RESET_PROMPT,
                                    "Prompt Before Resetting Interactions Pane",
                                    SWT.NONE, getFieldEditorParent()));
    addField(new BooleanFieldEditor(DrJavaConstants.ALLOW_PRIVATE_ACCESS,
                                    "Allow Access to Private and Protected Members of Classes",
                                    SWT.NONE, getFieldEditorParent()));
    addField(new BooleanFieldEditor(DrJavaConstants.INTERACTIONS_EXIT_PROMPT,
                                    "Prompt if Interactions Pane Exits Unexpectedly",
                                    SWT.NONE, getFieldEditorParent()));
    addField(new IntegerFieldEditor(DrJavaConstants.HISTORY_MAX_SIZE,
                                    "Size of Interactions History",
                                    getFieldEditorParent()));
  }

  /** 
   * Returns preference store that belongs to the DrJava plug-in.
   * This approach was taken by Eclipse's ReadmeTool example plug-in
   * to store preferences separately from the desktop.
   */
  protected IPreferenceStore doGetPreferenceStore() {
    return EclipsePlugin.getDefault().getPreferenceStore();
  }
}