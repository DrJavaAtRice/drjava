package edu.rice.cs.drjava.ui;
import javax.swing.*;
import java.awt.*;
/**
 * DrJava's version of a JScrollPane, which initializes the border to null.
 */
public class BorderlessScrollPane extends JScrollPane {
  public BorderlessScrollPane() {
    super();
    setBorder(null);
  }
  public BorderlessScrollPane(Component view) {
    super(view);
    setBorder(null);
  }
  public BorderlessScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
    super(view,vsbPolicy,hsbPolicy);
    setBorder(null);
  }
  public BorderlessScrollPane(int vsbPolicy, int hsbPolicy) {
    super(vsbPolicy,hsbPolicy);
    setBorder(null);
  }
}
