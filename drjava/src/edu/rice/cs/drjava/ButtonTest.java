package edu.rice.cs.drjava;

import junit.framework.*;
import java.io.File;
import java.util.Vector;
import javax.swing.text.BadLocationException;
import junit.extensions.*;

public class ButtonTest extends TestCase
{
    
    MainFrame _m;

    public ButtonTest(String name)
    {
	super(name);
    }
	
    public void setUp()
    {
	_m = new MainFrame();
    }

    public static Test suite()
    {
	return new TestSuite(ButtonTest.class);
    }

    public void testSaveButtonInitiallyDisabled() {
	assertTrue(!_m._saveButton.isEnabled());
    }

    public void testCompileButtonInitiallyDisabled() {
	assertTrue(!_m._compileButton.isEnabled());
    }

    public void testSaveEnabledAfterModification() 
	throws BadLocationException {
	DefinitionsDocument d = _m.getDefView()._doc();
	d.insertString(0,"this is a test",null);
	assertTrue(_m._saveButton.isEnabled());
    }

    public void testCompileDisabledAfterModification() 
	throws BadLocationException {
	DefinitionsDocument d = _m.getDefView()._doc();
	d.insertString(0, "this is a test", null);
	assertTrue(!_m._compileButton.isEnabled());
    }

    public void testCompileEnabledAfterSave() 
	throws BadLocationException {
	DefinitionsView v = _m.getDefView();
	DefinitionsDocument d = v._doc();
	d.insertString(0,"this is a test",null);
	assertTrue(_m.saveToFile("button-test-file"));
	assertTrue(_m._compileButton.isEnabled());
	assertTrue(new File("button-test-file").delete());
    }

    public void testSaveDisabledAfterSave() 
	throws BadLocationException {
	DefinitionsView v = _m.getDefView();
	DefinitionsDocument d = v._doc();
	d.insertString(0,"this is a test",null);
	assertTrue(_m.saveToFile("button-test-file"));
	assertTrue(!_m._saveButton.isEnabled());
	assertTrue(new File("button-test-file").delete());
    }

	public void testCompileDisabledAfterCompile() 
	  throws BadLocationException {
	DefinitionsView v = _m.getDefView();
	DefinitionsDocument d = v._doc();
	d.insertString(0,"public class C{}",null);
	assertTrue(_m.saveToFile("C.java"));
	try {
	    _m.compile();
	}
	catch (NullPointerException ex) {
	    // A compilation will cause messages to be written to
	    // the compile-errors window, which doesn't exist in a
	    // barebones MainFrame.
	}
	assertTrue(!_m._compileButton.isEnabled());
	new File("C.java").delete();
	new File("C.class").delete();
	}
}

