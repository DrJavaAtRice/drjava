package edu.rice.cs.drjava.ui.config;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import edu.rice.cs.drjava.config.*;

public class VectorClassnameOptionComponent extends VectorOptionComponent<String> {
  public VectorClassnameOptionComponent (VectorOption<String> opt, String text, Frame parent) {
    super(opt, text, parent);
  }

  /**
   * Constructor that allows for a tooltip description.
   */
  public VectorClassnameOptionComponent(VectorOption<String> opt, String text,
                                        Frame parent, String description) {
    this(opt, text, parent);
    setDescription(description);
  }

  protected Action _getAddAction() {
    return new AbstractAction("Add") {
      public void actionPerformed(ActionEvent e) {
        String input = JOptionPane.showInputDialog(_parent, "Please enter the class name:");
        if (input != null && !input.equals("")) {
          _listModel.addElement(input);
        }
      }
    };
  }
}