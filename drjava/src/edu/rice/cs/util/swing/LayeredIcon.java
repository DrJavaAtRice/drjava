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

import javax.swing.*;
import java.awt.*;

public class LayeredIcon implements Icon {
  private Icon[] _layers;
  private int[] _xoffs;
  private int[] _yoffs;
  private int _w=0;
  private int _h=0;
  public LayeredIcon(Icon[] layers, int[] x, int[] y) {
    _layers = layers;
    _xoffs = x;
    _yoffs = y;
    if (layers.length != x.length || x.length != y.length) {
      throw new IllegalArgumentException("Array lengths don't match");
    }
    _w = 0; _h = 0;
    for (int i = 0; i < layers.length; i++) {
      if (layers[i] != null) {
        _w = Math.max(_w, layers[i].getIconWidth() + x[i]);
        _h = Math.max(_h, layers[i].getIconHeight() + x[i]);
      }
    }
  }
  public int getIconHeight(){
    return _h;
  }
  public int getIconWidth(){
    return _w;
  }
  public void paintIcon(Component c, Graphics g, int x, int y){
    for (int i = 0; i < _layers.length; i++) {
      Icon ico = _layers[i];
      if (ico != null) _layers[i].paintIcon(c,g, x+_xoffs[i], y+_yoffs[i]);
    }
  }
  
  public Icon[] getLayers(){
    return _layers;
  }
  public int[] getXOffsets() {
    return _xoffs;
  }
  public int[] getYOffsets(){
    return _xoffs;
  }
}