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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

/**
 * A block that represents some information about the highlighting status in 
 * a particular section in the document.
 * @version $Id$
 */
public class HighlightStatus {
  public static final int NORMAL = 0;
  public static final int COMMENTED = 1;
  public static final int SINGLE_QUOTED = 2;
  public static final int DOUBLE_QUOTED = 3;
  public static final int KEYWORD = 4;
  public static final int NUMBER = 5;
  public static final int TYPE = 6;
  private int _state;
  private int _location;
  private int _length;

  /**
   * Constructor.
   * @param     int location the starting character offset of the block
   * @param     int length length of block
   * @param     int state coloring state of the block
   */
  public HighlightStatus(int location, int length, int state) {
    _location = location;
    _length = length;
    _state = state;
  }

  /**
   * Get the coloring state of this block.
   * @return an integer representing the color to paint the text
   * in the bounds of this block
   */
  public int getState() {
    return  _state;
  }

  /**
   * Get the starting location of this coloring block.
   * @return an integer offset
   */
  public int getLocation() {
    return  _location;
  }

  /**
   * Get the size of this coloring block.
   * @return the number of characters spanned by this block.
   */
  public int getLength() {
    return  _length;
  }
}



