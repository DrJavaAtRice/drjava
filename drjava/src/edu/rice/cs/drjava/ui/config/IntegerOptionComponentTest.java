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
import java.awt.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.DrJava;

import junit.framework.*;
import junit.extensions.*;

/**
 * Tests functionality of this OptionComponent
 */
public class IntegerOptionComponentTest extends TestCase {
  
  private static IntegerOptionComponent _option;
 
  public IntegerOptionComponentTest(String name) {
    super(name);
  }
  
  protected void setUp() {
    _option = new IntegerOptionComponent( OptionConstants.INDENT_LEVEL, "Indent Level", new Frame());
    DrJava.CONFIG.setSetting(OptionConstants.INDENT_LEVEL, OptionConstants.INDENT_LEVEL.getDefault());
    
  }
  
  public void testCancelDoesNotChangeConfig() {

    Integer testInteger = new Integer (0);
    
    _option.setValue(testInteger);
    _option.resetToCurrent(); // should reset to the original.
    _option.updateConfig(); // should update with original values therefore no change.
  
    assertEquals("Cancel (resetToCurrent) should not change the config", 
                 OptionConstants.INDENT_LEVEL.getDefault(),
                 DrJava.CONFIG.getSetting(OptionConstants.INDENT_LEVEL));
    
  }
  
  public void testApplyDoesChangeConfig() {
    Integer testInteger = new Integer (10);
       
    _option.setValue(testInteger); 
    _option.updateConfig();
    
    assertEquals("Apply (updateConfig) should write change to file", 
                 testInteger,
                 DrJava.CONFIG.getSetting(OptionConstants.INDENT_LEVEL));
  }
  
  public void testApplyThenResetDefault() {
    Integer testInteger = new Integer (10);
    
    _option.setValue(testInteger);
    _option.updateConfig(); 
    _option.resetToDefault(); // resets to default
    _option.updateConfig();
    
    assertEquals("Apply (updateConfig) should write change to file", 
                 OptionConstants.INDENT_LEVEL.getDefault(),
                 DrJava.CONFIG.getSetting(OptionConstants.INDENT_LEVEL));
  }
    
}