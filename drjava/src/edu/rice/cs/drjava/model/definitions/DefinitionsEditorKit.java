/* $Id$
 *
 * File history:
 *
 * $Log$
 * Revision 1.2  2001/06/19 16:26:54  javaplt
 * Added CVS tags to comments (Id and Log).
 * Changed all uses of assert() in JUnit tests to assertTrue(). assert() has been
 * deprecated because it is a builtin keyword in Java 1.4.
 * Fixed build.xml to test correctly after compile and to add CVS targets.
 *
 */

package edu.rice.cs.drjava;

import javax.swing.text.Document;

import javax.swing.text.DefaultEditorKit;
import javax.swing.text.StyledEditorKit;

/** This is an editor kit for editing Java source files. */
public class DefinitionsEditorKit extends StyledEditorKit
{
  /** Factory method to make this view create correct model objects. */
  public Document createDefaultDocument()
  {
    return _createDefaultTypedDocument();
  }

  private static DefinitionsDocument _createDefaultTypedDocument()
  {
    return new DefinitionsDocument();
  }

  public String getContentType()
  {
    return "text/java";
  }
}
