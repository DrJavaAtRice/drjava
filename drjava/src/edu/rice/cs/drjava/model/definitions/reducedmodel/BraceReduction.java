/* $Id$ */

package edu.rice.cs.drjava;

import gj.util.Vector;
/**
 * The interface BraceReduction serves as the template for our reduced
 * view of a java document, which stores only the information necessary
 * for parenthesis matching.
 * @author Corky Cartwright, Paul Graunke, Mike Yantosca
 */

public interface BraceReduction
{

	
/**
	 * Accessor for curson.
	 */
	public ModelList<ReducedToken>.Iterator getCursor();

	/**
	 * Accessor for block offset.
	 */
	public int getBlockOffset();

	/**
	 * Mutator for block offset.
	 */
	public void setBlockOffset(int n);

	/**
	 * Accessor for braces.
	 */
	public ModelList<ReducedToken> getBraces();

	/**
	 * Returns the current state of the cursor iterator.
	 */
	public int getStateAtCurrent();

	/**
	 * Returns a copy of the current curson.
	 */
	public ModelList<ReducedToken>.Iterator makeCopyCursor();
	
  /**
   * Inserts an open brace ({) into the reduced model.
   */
  public void insertOpenSquiggly();

	/**
   * Inserts a backslash (\) into the reduced model.
   */
  public void insertBackSlash();

  /**
   * Inserts a closed brace (}) into the reduced model.
   */
  public void insertClosedSquiggly();

  /**
   * Inserts an open parenthesis (() into the reduced model.
   */
  public void insertOpenParen();

  /**
   * Inserts a closed parenthesis ()) into the reduced model.
   */
  public void insertClosedParen();

  /**
   * Inserts an open bracket ([) into the reduced model.
   */
  public void insertOpenBracket();

  /**
   * Inserts a closed bracket (]) into the reduced model.
   */
  public void insertClosedBracket();

  /**
   * Inserts a star.
   */
  public void insertStar();

  /**
   * Inserts a slash.
   */
  public void insertSlash();

  /**
   * Inserts a new line character (\n) into the reduced model.
   */
  public void insertNewline();

  /**
   * Inserts a double quote (") into the reduced model.
   */
  public void insertQuote();
  
  /**
   * <P>Updates the BraceReduction to reflect the insertion of a
   * regular text string into the document.</P>
   * @param length the length of the inserted string
   */
  public void insertGap( int length );

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
   * <P>Finds the next significant brace.</P>
   * @return the distance to the next significant brace.
   */
  public int nextBrace();
  
  /**
   * <P>Finds the previous significant brace.</P>
   * @return the distance to the previous significant brace.
   */
  public int previousBrace();
  
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
	 * Returns true iff the brace immediately to the left is closed.
	 */
	public boolean closedBraceImmediatelyLeft();
}













