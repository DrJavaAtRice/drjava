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
 * Indent information block.
 * @version $Id$
 */
public class IndentInfo {
  public String braceType;      //the type of brace at the beginning of our line

  //the distance to the start of the line containing
  //the brace that encloses the start of our line.
  //____\n|_____
  public int distToNewline;

  //distance to the brace enclosing the start of our line  ____|{_____
  public int distToBrace;

  // type of brace at current position
  public String braceTypeCurrent;

  // distance to the start of the line containing the brace enclosing the current location
  public int distToNewlineCurrent;
  
  // distance to the brace enclosing the current location
  public int distToBraceCurrent;
  
  //the distance to the start of the current line
  public int distToPrevNewline;
  
  static public String noBrace = "";
  static public String openSquiggly = "{";
  static public String openParen = "(";
  static public String openBracket = "[";

  /**
   * put your documentation comment here
   */
  public IndentInfo() {
    braceType = noBrace;
    distToNewline = -1;
    distToBrace = -1;
    braceTypeCurrent = noBrace;
    distToNewlineCurrent = -1;
    distToBraceCurrent = -1;
  }

  /**
   * put your documentation comment here
   * @param     String _braceType
   * @param     int _distToNewline
   * @param     int _distToBrace
   * @param     int _distToPrevNewline
   */
  public IndentInfo(String _braceType, int _distToNewline, int _distToBrace, int _distToPrevNewline) {
    braceType = _braceType;
    distToNewline = _distToNewline;
    distToBrace = _distToBrace;
    distToPrevNewline = _distToPrevNewline;
  }
}



