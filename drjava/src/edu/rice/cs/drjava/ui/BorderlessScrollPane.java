package edu.rice.cs.drjava.ui;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.ui.CompilerErrorPanel.ErrorListPane;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.DelegatingAction;

/**
 * DrJava's version of a JScrollPane, which initializes the border to null.
 */
public class BorderlessScrollPane extends JScrollPane {
    public BorderlessScrollPane() {
	super();
	setBorder(null);
    }
    public BorderlessScrollPane(Component view) {
	super(view);
	setBorder(null);
    }
    public BorderlessScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
	super(view,vsbPolicy,hsbPolicy);
	setBorder(null);
    }
    public BorderlessScrollPane(int vsbPolicy, int hsbPolicy) {
	super(vsbPolicy,hsbPolicy);
	setBorder(null);
    }
}
