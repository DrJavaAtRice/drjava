package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.model.*;

/**
 * Extension of {@link GlobalModelListener} for {@link SingleDisplayModel}s.
 *
 * @version $Id$
 */
public interface SingleDisplayModelListener extends GlobalModelListener {
  public void activeDocumentChanged(OpenDefinitionsDocument active);
}
