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

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

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
    bottom.addView("edu.rice.cs.drjava.plugins.eclipse.views.InteractionsView");
    bottom.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
    bottom.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
    
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
