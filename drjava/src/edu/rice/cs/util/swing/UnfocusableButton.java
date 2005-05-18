package edu.rice.cs.util.swing;

import javax.swing.*;

/** A JButton that cannot be given focus.
 *  @version $Id$
 */
public class UnfocusableButton extends JButton {
  /** Creates a new UnfocusableButton. */
  public UnfocusableButton() { super(); }

  /** Creates a new UnfocusableButton.
   *  @param a the action for this button to use
   */
  public UnfocusableButton(Action a) { super(a); }

  /** Creates a new UnfocusableButton.
   *  @param s the text for the button to display
   */
  public UnfocusableButton(String s) { super(s); }

  /** Creates a new UnfocusableButton.
   *  @param i the icon for the button to display
   */
  public UnfocusableButton(Icon i) { super(i); }

  /** Creates a new UnfocusableButton.
   *  @param s the text for the button to display
   *  @param i the icon for the button to display
   */
  public UnfocusableButton(String s, Icon i) { super(s, i); }

  /** Returns that this button cannot be given focus.
   *  @return <code>false</code>
   */
  public boolean isFocusTraversable() { return false; }

  /** Returns that this button cannot be given focus.
   *  @return <code>false</code>
   */
  public boolean isFocusable() { return false;
  }
}
