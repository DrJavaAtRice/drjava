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

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * A simplified perspective for Eclipse, providing a collection of views
 * that closely mimics the DrJava user interface.
 * 
 * @author Jonathan Mendez
 * @version $Id$
 */
public class DrJavaPerspective implements IPerspectiveFactory {
  
  /**
   * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(IPageLayout)
   */
  public void createInitialLayout(IPageLayout layout) {
    String editorArea = layout.getEditorArea();
    
    // bottom: interactions window, console, search, (JUnit?)
    IFolderLayout bottom =
      layout.createFolder("bottom", IPageLayout.BOTTOM, 0.75f, editorArea);
    bottom.addView("edu.rice.cs.drjava.InteractionsView");
    bottom.addPlaceholder(IDebugUIConstants.ID_CONSOLE_VIEW);
    bottom.addPlaceholder(SearchUI.SEARCH_RESULT_VIEW_ID);
    
    // left: same as Java perspective, need the package view for the files
    IFolderLayout left =
      layout.createFolder("left", IPageLayout.LEFT, 0.25f, editorArea);
    left.addView(JavaUI.ID_PACKAGES);
    //the next two are necessary for now, but if we can prevent the
    //behavior that causes them to be needed, that would be preferable
    left.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY);
    left.addPlaceholder(IPageLayout.ID_RES_NAV);
    
    // need to be able to launch programs and create new files
    // perhaps we could create our own more simplified action set
    layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
    layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
    
    // another place for new file functionality
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewPackageCreationWizard");
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewClassCreationWizard");
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard");
    // DrJava never claimed to be a general file editor, so we won't
    // include general file creation from here
  }
  
}
