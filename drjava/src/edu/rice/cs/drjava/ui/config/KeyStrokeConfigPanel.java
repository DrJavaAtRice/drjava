/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui.config;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

import java.util.TreeSet;
import java.util.Iterator;

import gj.util.Vector;
import gj.util.Hashtable;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.KeyStrokeOption;
import edu.rice.cs.drjava.config.Option;

/**
 * Panel to display all KeyStrokeOptionComponents.
 * @version $Id$
 */
public class KeyStrokeConfigPanel extends ConfigPanel { 
  TreeSet _comps = null;

  public KeyStrokeConfigPanel(String title,TreeSet comps) {
    super(title);
    _comps = comps;
  }
  public KeyStrokeConfigPanel(String title) {
    super(title);
  }
  
  public void setKeyStrokeComponents(TreeSet comps) { _comps = comps; }
  
  /**
   * Tells each component in the vector to update() itself
   * @return whether update() of all the components succeeded 
   */ 
  public boolean update() {
    // shouldn't happen
    if (_comps == null) return false;
    
    /** 
     * Overrides update in order to perform some pre-processing. Need to 
     * set every modified key-binding's KeyStroke to null because in the 
     * KeyBindingManager, if two binding's KeyStrokes are swapped, the 
     * later key-binding will erase the earlier key-binding since its old
     * value's entry is removed from the hashtable.
     */
    Iterator iter = _comps.iterator();
    while (iter.hasNext()) {
      KeyStrokeOptionComponent x = (KeyStrokeOptionComponent) iter.next();
      if (!DrJava.getConfig().getSetting(x.getOption()).equals(x.getKeyStroke())) {
        DrJava.getConfig().setSetting((Option)x.getOption(), KeyStrokeOption.NULL_KEYSTROKE);
      }
          
    }
    return super.update();
  }
  
}