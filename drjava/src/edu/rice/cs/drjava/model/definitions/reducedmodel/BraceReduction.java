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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * any Java compiler, even if it is provided in binary-only form, and distribute
 * linked combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than Java compilers.
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so.  If you do not wish to
 * do so, delete this exception statement from your version.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import gj.util.Vector;
/**
 * The interface BraceReduction serves as the template for our reduced
 * view of a java document, which stores only the information necessary
 * for parenthesis matching.
 * @version $Id$
 * @author JavaPLT
 */

public interface BraceReduction {

  /**
   * Get the absolute character offset of the document represented by BraceReduction.
   */
  public int absOffset();

  /**
   * Get the current token in the BraceReduction.
   * @return the current token
   */
  ReducedToken currentToken();

  /**
   * Get the state of the token at the current cursor position.
   * @return the current state
   */
  ReducedModelState getStateAtCurrent();

  /**
   * Insert a character into the BraceReduction.
   * @param ch the character to be inserted
   */
  public void insertChar(char ch);

  /**
   * <P>Updates the BraceReduction to reflect cursor movement.
   * Negative values move left from the cursor, positive values move
   * right. </P>
   * @param count indicates the direction and magnitude of cursor movement
   */
  public void move( int count );
  
  /**
   * <P>Update the BraceReduction to reflect text deletion.</P>
   * @param count indicates the size and direction of text deletion.
   * Negative values delete text to the left of the cursor, positive
   * values delete text to the right.
   */
  public void delete( int count );
  
  
  /**
   * <P>Finds the closing brace that matches the next significant
   * brace iff that brace is an open brace.</P>
   * @return the distance until the matching closing brace.  On
   * failure, returns -1.
   * @see #nextBrace()
   */
  public int balanceForward();
  
  /**
   * <P>Finds the open brace that matches the previous significant
   * brace iff that brace is an closing brace.</P>
   * @return the distance until the matching open brace.  On
   * failure, returns -1.
   * @see #previousBrace()
   */
  public int balanceBackward();

  /**
   *Gets the distance to the enclosing brace.
   */
  public IndentInfo getIndentInformation();

  /**
   * Gets distance to enclosing new line
   */
  public int getDistToPreviousNewline(int relativeLoc);

  /**
   * Gets distance to next new line.
   */
  public int getDistToNextNewline();

  /**
   * A simplified toString() method.
   */
  public String simpleString();

  /**
   * Return all highlight status info for text between the current
   * location and current location + end.
   * This should collapse adjoining blocks with the same status into one.
   * @param start The starting location of the area we want to get status of.
   *              The reduced model is already at this position, but the 
   *              parameter is needed to determine the absolute positions
   *              needed in the HighlightStatus objects we return.
   * @param length How far should we generate info for?             
   */
  public Vector<HighlightStatus> getHighlightStatus(int start, int length);
    
  /**
   *Returns the state at the relLocation, where relLocation is the location
   *relative to the walker
   *@param relLocation distance from walker to get state at.
   */
  public ReducedModelState stateAtRelLocation(int relLocation);
}
