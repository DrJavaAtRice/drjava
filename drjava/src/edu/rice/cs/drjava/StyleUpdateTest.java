package edu.rice.cs.drjava;

import java.awt.*;
import javax.swing.text.*;
import junit.framework.*;
import gj.util.Vector;
import junit.extensions.*;

public class StyleUpdateTest extends TestCase implements Flag {

	
	
	protected DefinitionsDocument defModel;
	protected static Object FOREGROUND =
	  StyleConstants.ColorConstants.Foreground;
	private boolean _wasRaised, _wasLowered;
		
	public StyleUpdateTest(String name)
		{
			super(name);
		}
	
	protected void setUp()
		{
//			this._wasRaised = false;
//			this._wasLowered = false;
			defModel = new DefinitionsDocument();
//			defModel._taskCounter.setFlag(this);
		}
	
	public static Test suite()
		{
			return new TestSuite(StyleUpdateTest.class);
		}

	public void testInsertStringUpdate() throws InterruptedException
		{
//  			try {
//  				this.defModel.insertString(0, "class C { /* comment */ }", null);
				
//  				while (defModel._styleUpdater == null ||
//  							 ! _wasRaised ||
//  							 ! _wasLowered);
//  				AttributeSet attributes =
//  					defModel.getCharacterElement(0).getAttributes();
//  				assertEquals(Color.black, attributes.getAttribute(FOREGROUND));
				
//  				this._wasRaised = false;
//  				this._wasLowered = false;
//  				this.defModel.insertString(0, "//", null);
//  				while (defModel._styleUpdater == null ||
//  							 ! _wasRaised ||
//  							 ! _wasLowered);
//  				attributes = defModel.getCharacterElement(0).getAttributes();
//  				assertEquals(Color.blue, attributes.getAttribute(FOREGROUND));
//  			}
//  			catch (BadLocationException ex) {
//  				throw new RuntimeException(ex.toString());
//  			}
		}

	public void testInsertStringUpdate2()
		{
			try {
				this.defModel.insertString(0, "/", null);
				this.defModel.insertString(1, "*", null);
				this.defModel.insertString(2, "h", null);
				this.defModel.insertString(3, "e", null);
				this.defModel.insertString(4, "r", null);
				this.defModel.insertString(5, "e", null);
				this.defModel.insertString(6, "*", null);
				this.defModel.insertString(7, "/", null);
			}
			catch (BadLocationException ex) {
				throw new RuntimeException(ex.toString());
			}
		}

	public void testRemoveStringUpdate() throws InterruptedException
		{
//  			try{
//  				this.defModel.insertString(0, "class C { /* comment */ }", null);
//  				AttributeSet attributes;
//  				this.defModel.remove(10,2);
//  				while (defModel._styleUpdater == null ||
//  							 ! _wasRaised ||
//  							 ! _wasLowered);
//  				attributes = defModel.getCharacterElement(13).getAttributes();
//  				assertEquals(Color.black, attributes.getAttribute(FOREGROUND));
//  			}
			
//  			catch (BadLocationException ex) {
//  				throw new RuntimeException(ex.toString());
//  			}			
		}

	public void raise() {
		this._wasRaised = true;
	}

	public void lower() {
		this._wasLowered = true;
	}

	public void wave() {}
	
}







