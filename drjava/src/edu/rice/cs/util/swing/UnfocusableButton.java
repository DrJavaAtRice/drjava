/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.swing;

import javax.swing.*;

/** A JButton that cannot be given focus.
  * @version $Id$
  */
public class UnfocusableButton extends JButton {
  /** Creates a new UnfocusableButton. */
  public UnfocusableButton() { super(); }
  
  /** Creates a new UnfocusableButton.
    * @param a the action for this button to use
    */
  public UnfocusableButton(Action a) { super(a); }
  
  /** Creates a new UnfocusableButton.
    * @param s the text for the button to display
    */
  public UnfocusableButton(String s) { super(s); }
  
  /** Creates a new UnfocusableButton.
    * @param i the icon for the button to display
    */
  public UnfocusableButton(Icon i) { super(i); }
  
  /** Creates a new UnfocusableButton.
    * @param s the text for the button to display
    * @param i the icon for the button to display
    */
  public UnfocusableButton(String s, Icon i) { super(s, i); }
  
  /** Returns that this button cannot be given focus.
    * @return <code>false</code>
    */
  @Deprecated
  public boolean isFocusTraversable() { return false; }
  
  /** Returns that this button cannot be given focus.
    * @return <code>false</code>
    */
  public boolean isFocusable() { return false; }
}
