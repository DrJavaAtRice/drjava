package edu.rice.cs.drjava.model.repl;

import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.*;
import java.util.*;

import edu.rice.cs.util.UnexpectedException;

/**
 * This mediates the model and view of interactions.
 * @version $Id$
 */
public class InteractionsEditorKit extends StyledEditorKit {
  //private final HashMap _listenerMap = new HashMap();

  public Document createDefaultDocument() {
    return new InteractionsDocument();
  }

  /*
  public void install(JEditorPane e) {
    super.install(e);
    ClickListener l = new ClickListener();
    e.addMouseListener(l);
    _listenerMap.put(e, l);
  }

  public void deinstall(JEditorPane e) {
    super.deinstall(e);
    ClickListener l = (ClickListener) _listenerMap.get(e);
    _listenerMap.remove(e);
    e.removeMouseListener(l);
  }

  private static InteractionsDocument _document(JEditorPane e) {
    return (InteractionsDocument) e.getDocument();
  }

  private static class ClickListener extends MouseAdapter {
    public void mouseClicked(MouseEvent e) {
      JEditorPane pane = (JEditorPane) e.getComponent();
      int modelPos = pane.viewToModel(e.getPoint());

      String linkinfo = _document(pane).getLinkInfo(modelPos);


      JOptionPane.showMessageDialog(null, "linkinfo(" + modelPos + "): " + linkinfo);
    }
  }
  */
}
