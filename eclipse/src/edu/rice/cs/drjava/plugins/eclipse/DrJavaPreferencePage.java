/*BEGIN_COPYRIGHT_BLOCK*

DrJava Eclipse Plug-in BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of DrJava, the JavaPLT group, Rice University, nor the names of software 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.plugins.eclipse;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * A preference page that allows the user to customize settings for
 * the DrJava Plug-in for Eclipse.
 *
 * How to add a preference to the plug-in:<ul>
 * <li>Add a constant to DrJavaConstants</li>
 * <li>In DrJavaPreferencePage, add a field in createFieldEditors()</li>
 * <li>Add a default value to EclipsePlugin</li>
 * <li>Add an IPropertyChangeListener to the PreferenceStore
 *     ({@code EclipsePlugin.getDefault().getPreferenceStore()})</li>
 * <li>Specifically, in InteractionsController:<ul>
 *   <li>Add a variable to remember preference</li>
 *   <li>Make action dependent on variable</li>
 *   <li>Add a line to _updatePreferences</li>
 * </li>
 * </ul>
 *
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
    addField(new StringFieldEditor(DrJavaConstants.JVM_ARGS,
                                   "Arguments to the Interactions JVM",
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
