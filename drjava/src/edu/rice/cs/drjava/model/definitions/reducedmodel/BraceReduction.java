package edu.rice.cs.drjava;

import gj.util.Vector;
/**
 * @version $Id$
 * The interface BraceReduction serves as the template for our reduced
 * view of a java document, which stores only the information necessary
 * for parenthesis matching.
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
