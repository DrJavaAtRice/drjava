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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import java.util.ArrayList;

/** The interface BraceReduction serves as the template for our reduced view of a java document, which stores only the
  * information necessary for parenthesis matching.
  * @version $Id$
  * @author JavaPLT
  */

public interface BraceReduction {

  /** @return the absolute character offset of the document represented by BraceReduction. */
  public int absOffset();

  /** Get the current token in the BraceReduction.
    * @return the current token
    */
  public ReducedToken currentToken();

  /** Get the state of the token at the current cursor position.
    * @return the current state
    */
  public ReducedModelState getStateAtCurrent();

  /** Insert a character into the BraceReduction.
   * @param ch the character to be inserted
   */
  public void insertChar(char ch);

  /** <P>Updates the BraceReduction to reflect cursor movement.
    * Negative values move left from the cursor, positive values move
    * right. </P>
    * @param count indicates the direction and magnitude of cursor movement
    */
  public void move(int count );

  /** <P>Update the BraceReduction to reflect text deletion.</P>
    * @param count indicates the size and direction of text deletion.
    * Negative values delete text to the left of the cursor, positive
    * values delete text to the right.
    */
  public void delete(int count );


  /** <P>Finds the closing brace that matches the next significant
    * brace iff that brace is an open brace.</P>
    * @return the distance until the matching closing brace.  On
    * failure, returns -1.
    * @see #balanceBackward()
    */
  public int balanceForward();

  /** <P>Finds the open brace that matches the previous significant
    * brace iff that brace is an closing brace.</P>
    * @return the distance until the matching open brace.  On
    * failure, returns -1.
    * @see #balanceForward()
    */
  public int balanceBackward();

//  /** Gets the distance to the enclosing brace. */
//  public IndentInfo getIndentInformation();

  /** 
   * @param relativeLoc the location to find the distance to
   * @return distance to enclosing new line 
   */
  public int getDistToStart(int relativeLoc);

  /** @return distance to next new line. */
  public int getDistToNextNewline();

  /** 
   * A simplified toString() method. 
   * @return a string representation of this
   */
  public String simpleString();

  /** Return all highlight status info for text between the current location 
   * and current location + length. This should collapse adjoining blocks with 
   * the same status into one.
   * @param start  The start location of the area being inspected. The reduced 
   *               model cursor is already located at this position, but the 
   *               parameter is needed to determine the absolute positions 
   *               needed in the HighlightStatus objects we return.
   * @param length  The length of the text area being inspected.
   * @return all highlight status info for the text between the current 
   *         location and current location + length
   */
  public ArrayList<HighlightStatus> getHighlightStatus(int start, int length);

  /** 
   * @param relLocation distance from walker to get state at.
   * @return the state at the relLocation, where relLocation is the location relative to the walker
   */
  public ReducedModelState moveWalkerGetState(int relLocation);

  /** Resets the location of the walker in the comment list to where the current cursor is. */
  public void resetLocation();
}
