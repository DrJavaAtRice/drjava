package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.model.*;

public interface SingleDisplayModelListener extends GlobalModelListener {
  public void activeDocumentChanged(OpenDefinitionsDocument active);
}
