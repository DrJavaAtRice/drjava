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
