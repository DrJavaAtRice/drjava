/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.swing;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
/**
 * A JScrollPane without a traditional Swing border.  Uses its own
 * EtchedBorder instead, which improves the appearance of nested panes
 * on Mac OS X.
 * @version $Id: BorderlessScrollPane.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class BorderlessScrollPane extends JScrollPane {
  /** The default border for a "borderless" scroll pane.
   */
  private static final Border DEFAULT = new EtchedBorder();

  // note, I can't think of a way to guarantee superclass behavior without
  // overriding each superclass constructor and then calling setBorder().
  
  public BorderlessScrollPane() {
    super();
    setBorder(DEFAULT);
  }
  public BorderlessScrollPane(Component view) {
    super(view);
    setBorder(DEFAULT);
  }
  public BorderlessScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
    super(view,vsbPolicy,hsbPolicy);
    setBorder(DEFAULT);
  }
  public BorderlessScrollPane(int vsbPolicy, int hsbPolicy) {
    super(vsbPolicy,hsbPolicy);
    setBorder(DEFAULT);
  }
}
