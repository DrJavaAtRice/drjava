package edu.rice.cs.drjava.ui;
import javax.swing.*;
import java.awt.*;
/**
 * DrJava's version of a JSplitPane, which initializes the border to null.
 */
public class BorderlessSplitPane extends JSplitPane {
  
  public BorderlessSplitPane() {
    super();
    setBorder(null);
  }
  public BorderlessSplitPane(int orient) { 
    super(orient);
    setBorder(null);
  }
  public BorderlessSplitPane(int orient, boolean layout) {
    super(orient,layout);
    setBorder(null);
  }
  public BorderlessSplitPane(int orient, boolean layout, Component left, Component right) {
    super(orient,layout,left,right);
    setBorder(null);
  }
  public BorderlessSplitPane(int orient, Component left, Component right) {
    super(orient,left,right);
    setBorder(null);
  }
}



