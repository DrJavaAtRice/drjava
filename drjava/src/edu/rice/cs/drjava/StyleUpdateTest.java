package edu.rice.cs.drjava;

import java.awt.*;
import javax.swing.text.*;
import junit.framework.*;
import gj.util.Vector;
import junit.extensions.*;

public class StyleUpdateTest extends TestCase {

	
	
	protected DefinitionsDocument defModel;
	protected static Object FOREGROUND = StyleConstants.ColorConstants.Foreground;
		
	public StyleUpdateTest(String name)
		{
			super(name);
		}
	
	protected void setUp()
		{
			try{
				defModel = new DefinitionsDocument();
				this.defModel.insertString(0, "class C { /* comment */ }", null);
			}
			catch (BadLocationException ex) {
				throw new RuntimeException(ex.toString());
			}
		}
	
	public static Test suite()
		{
			return new TestSuite(StyleUpdateTest.class);
		}

	public void testInsertStringUpdate()
		{
			try {
				
				AttributeSet attributes = defModel.getCharacterElement(0).getAttributes();
				assertEquals(Color.black, attributes.getAttribute(FOREGROUND));
				this.defModel.insertString(0, "//", null);
				attributes = defModel.getCharacterElement(0).getAttributes();
				assertEquals(Color.blue, attributes.getAttribute(FOREGROUND));
			}
			catch (BadLocationException ex) {
				throw new RuntimeException(ex.toString());
			}
		}

	public void testRemoveStringUpdate()
		{
			try{
				AttributeSet attributes;
				this.defModel.remove(10,2);
				attributes = defModel.getCharacterElement(13).getAttributes();
				assertEquals(Color.black, attributes.getAttribute(FOREGROUND));
			}
			
			catch (BadLocationException ex) {
				throw new RuntimeException(ex.toString());
			}			
		}	
}







