package edu.rice.cs.drjava.model.definitions;

import javax.swing.text.*;
import java.awt.*;
import javax.swing.event.DocumentEvent;

import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/**
 * This is an editor kit for editing Java source files.
 * It functions as the controller in the MVC arrangement.
 * It implements a factory for new documents, and it also
 * has a factory for Views (the things that render the document).
 * @version $Id$
 */
public class DefinitionsEditorKit extends DefaultEditorKit {
  private static final ViewFactory _factory = new ViewFactory() {
    public View create(Element elem) {
      return new ColoringView(elem);
    }
  };

  /** Factory method to make this view create correct model objects. */
  public Document createDefaultDocument() {
    return  _createDefaultTypedDocument();
  }

  /**
   * Creates a new DefinitionsDocument.
   * @return a new DefinitionsDocument.
   */
  private static DefinitionsDocument _createDefaultTypedDocument() {
    return  new DefinitionsDocument();
  }

  /**
   * Get the MIME content type of the document.
   * @return "text/java"
   */
  public String getContentType() {
    return "text/java";
  }

  /**
   * We want to use our ColoringView to render text, so here we return
   * a factory that creates ColoringViews.
   */
  public final ViewFactory getViewFactory() {
    return _factory;
  }
}




