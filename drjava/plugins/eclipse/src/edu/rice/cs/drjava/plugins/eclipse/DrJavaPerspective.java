package edu.rice.cs.drjava.plugins.eclipse;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * 
 * 
 * @author Jonathan Mendez  Copyright Feb 27, 2003 - All rights reserved.
 */
public class DrJavaPerspective implements IPerspectiveFactory {

    /**
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(IPageLayout)
     */
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();

        // bottom: interactions window, console, search, (JUnit?)
        IFolderLayout bottom =
            layout.createFolder("bottom", layout.BOTTOM, 0.75f, editorArea);
        bottom.addView(
            "edu.rice.cs.drjava.plugins.eclipse.views.InteractionsView");
        bottom.addPlaceholder(IDebugUIConstants.ID_CONSOLE_VIEW);
        bottom.addPlaceholder(SearchUI.SEARCH_RESULT_VIEW_ID);

        // left: same as Java perspective, need the package view for the files
        IFolderLayout left =
            layout.createFolder("left", layout.LEFT, 0.25f, editorArea);
        left.addView(JavaUI.ID_PACKAGES);
        //the next two are necessary for now, but if we can prevent the
        //behavior that causes them to be needed, that would be preferable
        left.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY);
        left.addPlaceholder(layout.ID_RES_NAV);

        // need to be able to launch programs and create new files
        // perhaps we could create our own more simplified action set
        layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
        layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);

        // another place for new file functionality
        layout.addNewWizardShortcut(
            "org.eclipse.jdt.ui.wizards.NewPackageCreationWizard");
        layout.addNewWizardShortcut(
            "org.eclipse.jdt.ui.wizards.NewClassCreationWizard");
        layout.addNewWizardShortcut(
            "org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard");
        // DrJava never claimed to be a general file editor, so we won't
        // include general file creation from here
    }

}
