/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

import java.awt.*;
import javax.swing.*;

/** Dialog that gets displayed when the program is processing data. */
public class ProcessingDialog extends JDialog {
  private Component _parent;
  private JProgressBar _pb;
  
  public ProcessingDialog(Frame parent, String title, String label) {
    this(parent, title, label, false);
  }

  public ProcessingDialog(Frame parent, String title, String label,
                          boolean modal) {
    super(parent, title, modal);
    setResizable(false);    
    _parent = parent;
    setSize(350, 150);
    if (_parent!=null) { Utilities.setPopupLoc(this, _parent); }
    JLabel waitLabel = new JLabel(label, SwingConstants.CENTER);
    getRootPane().setLayout(new BorderLayout());
    getRootPane().add(waitLabel, BorderLayout.CENTER);
    _pb = new JProgressBar(0, 100);
    _pb.setValue(0);
    _pb.setStringPainted(false);
    _pb.setIndeterminate(true);
    getRootPane().add(_pb, BorderLayout.SOUTH);
  }
  
  public JProgressBar getProgressBar() { return _pb; }
  
  public void setVisible(boolean vis) {
    if (_parent!=null) { Utilities.setPopupLoc(this, _parent); }
    super.setVisible(vis);
  }
}
