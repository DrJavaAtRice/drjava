/* $Id$ */

package edu.rice.cs.drjava;

import gj.util.Stack;
import gj.util.Vector;

/**
 * This class provides an implementation of the BraceReduction
 * interface for brace matching.  In order to correctly match, this class
 * keeps track of what is commented (line and block) and what is inside
 * double quotes and keeps this in mind when matching.
 * To avoid unnecessary complication, this class maintains a few
 * invariants for its consistent states, i.e., between top-level
 * function calls.
 * <ol>
 * <li> The cursor offset is never at the end of a brace.  If movement
 * or insertion puts it there, the cursor is updated to point to the 0
 * offset of the next brace.
 * <li> Quoting information is invalid inside valid comments.  When part
 * of the document becomes uncommented, the reduced model must update the
 * quoting information linearly in the newly revealed code.
 * <li> Quote shadowing and comment shadowing are mutually exclusive.
 * <li> There is no nesting of comment open characters. If // is encountered
 *      in the middle of a comment, it is treated as two separate slashes.
 *      Similar for /*.
 * </ol>
 * @author Mike Yantosca, Jonathan Bannet
 */

public class ReducedModelControl implements BraceReduction
{
	ReducedModelBrace rmb;
	ReducedModelComment rmc;
	int _offset;
	ReducedModelControl()
		{
			rmb = new ReducedModelBrace(this);
			rmc = new ReducedModelComment();
		}
	
	public void insertChar(char ch)
		{
			rmb.insertChar(ch);
			rmc.insertChar(ch);
		}

	public void insertGap(int length)
		{
			rmb.insertGap(length);
			rmc.insertGap(length);
		}

  /**
   * <P>Updates the BraceReduction to reflect cursor movement.
   * Negative values move left from the cursor, positive values move
   * right. </P>
   * @param count indicates the direction and magnitude of cursor movement
   */
  public void move( int count )
		{
			rmb.move(count);
			rmc.move(count);
		}
  
  /**
   * <P>Update the BraceReduction to reflect text deletion.</P>
   * @param count indicates the size and direction of text deletion.
   * Negative values delete text to the left of the cursor, positive
   * values delete text to the right.
   */
  public void delete( int count )
		{
			rmb.delete(count);
			rmc.delete(count);
		}
  
    
  /**
   * <P>Finds the closing brace that matches the next significant
   * brace iff that brace is an open brace.</P>
   * @return the distance until the matching closing brace.  On
   * failure, returns -1.
   * @see #nextBrace()
   */
  public int balanceForward()
		{
			return rmb.balanceForward();
		}
  /**
   * <P>Finds the open brace that matches the previous significant
   * brace iff that brace is an closing brace.</P>
   * @return the distance until the matching open brace.  On
   * failure, returns -1.
   * @see #previousBrace()
   */
  public int balanceBackward()    
		{
			return rmb.balanceBackward();
		}

	/**
	 *This function returns the state at the relDistance, where relDistance
	 *is relative to the last time it was called. You can reset the last
	 *call to the current offset using resetLocation.
	 */
	public int stateAtRelLocation(int relDistance)
		{
			return rmc.stateAtRelLocation(relDistance);
		}

	/**
	 *This function resets the location of the walker in the comment list to
	 *where the current cursor is. This allows the walker to keep walking and
	 *using relative distance instead of having to rewalk the same distance
	 *every call to stateAtRelLocation. It is an optimization.
	 */
	void resetLocation()
		{
			rmc.resetLocation();
		}

	public ReducedToken currentToken()
		{
			ReducedToken rmcToken = rmc.current();
			if (!rmcToken.isGap())
				return rmcToken;
			ReducedToken rmbToken = rmb.current();
			if (!rmbToken.isGap()){
				rmbToken.setState(rmc.getStateAtCurrent());
				return rmbToken;
			}
			
			int size = getSize(rmbToken,rmcToken);
			return new Gap(size, rmc.getStateAtCurrent());
		}

	public int getStateAtCurrent()
		{
			return rmc.getStateAtCurrent();
		}

	String getType()
		{
			ReducedToken rmcToken = rmc.current();
			if (!rmcToken.isGap())
				return rmcToken.getType();

			ReducedToken rmbToken = rmb.current();
			if (!rmbToken.isGap())
				return rmbToken.getType();

			return ""; //a gap
		}
	
	int getSize()
		{
			return getSize(rmb.current(),rmc.current());
		}
	
	int getSize(ReducedToken rmbToken, ReducedToken rmcToken)
		{
			int rmb_offset = rmb._offset;
			int rmc_offset = rmc._offset;
			int rmb_size = rmbToken.getSize();
			int rmc_size = rmcToken.getSize();
			int size = 0;
			if (rmb_offset < rmc_offset){
				size = rmb_offset;
				_offset = size;
			}
			else{
				size = rmc_offset;
				_offset = size;
			}

			if (rmb_size - rmb_offset < rmc_size - rmc_offset)
				size += (rmb_size - rmb_offset);			
			else
				size += (rmc_size - rmc_offset);

			return size;
		}

	void next()
		{
			if (rmc._cursor.atStart()){
				rmc.next();
				rmb.next();
				return;
			}
			int size = getSize(rmb.current(),rmc.current());
			rmc.move(size - _offset);
			rmb.move(size - _offset);
		}

	void prev()
		{
			int size = 0;
			if (rmc._cursor.atEnd()){
				rmc.prev();
				rmb.prev();
				if (rmc._cursor.atStart()) //because in place now.
					return;
			 				
				if (rmc.current().getSize() < rmb.current().getSize())
					size = -rmc.current().getSize();
				else
					size = -rmb.current().getSize();
				rmc.next();
				rmb.next();
				move (size);
				return;
			}
			

			if (rmb._offset < rmc._offset) {
				rmb.prev();
				size = rmb.current().getSize() + rmb._offset;
				rmb.next();
				if (size < rmc._offset) 
					move(-size);
				else
					move(-rmc._offset);
			}
			else if (rmb._offset == rmc._offset) {
				rmb.prev();
				rmc.prev();
				rmb._offset = 0;
				rmc._offset = 0;
			}
			else {
				rmc.prev();
				size = rmc.current().getSize() + rmc._offset;
				rmc.next();
				if (size < rmb._offset) 
					move(-size);
				else
					move(-rmb._offset);
			}
				
		}

	public ReducedToken prevItem()
		{
			int rmbOffset = rmb._offset;
			int rmcOffset = rmc._offset;
			
			prev();
			ReducedToken temp = currentToken();
			next();

			rmb._offset = rmbOffset;
			rmc._offset = rmcOffset;
			return temp;
		}

	public ReducedToken nextItem()
		{
			int rmbOffset = rmb._offset;
			int rmcOffset = rmc._offset;
			next();
			ReducedToken temp = currentToken();
			prev();
			rmb._offset = rmbOffset;
			rmc._offset = rmcOffset;
			return temp;
		}

	boolean atEnd()
		{
			return (rmb._cursor.atEnd() || rmc._cursor.atEnd());			
		}

	boolean atStart()
		{
			return (rmb._cursor.atStart() || rmc._cursor.atStart());
		}

	int getBlockOffset()
		{
			if (rmb._offset < rmc._offset)
				return rmb._offset;
			return rmc._offset;
		}

	
	public int absOffset()
		{
			return rmc.absOffset();
		}


		public String simpleString()
		{
			return rmb.simpleString() + "\n\n\n" +rmc.simpleString();
		}

	/**
	 *Gets the distance to the enclosing brace.
	 */
	public IndentInfo getIndentInformation()
		{
			IndentInfo braceInfo = new IndentInfo();
			//get distance to the previous newline (in braceInfo.distToNewline)
			rmc.getDistToPreviousNewline(braceInfo);
			//get distance to the closing brace before that new line.
			rmb.getDistToEnclosingBrace(braceInfo);
			//get distance to newline before the previous, just mentioned, brace.
			rmc.getDistToIndentNewline(braceInfo);
			return braceInfo;
		}

	/**
	 *Gets distance to enclosing new line
	 */
	public int getDistToPreviousNewline(int relLoc)
		{
			return rmc.getDistToPreviousNewline(relLoc);
		}

	public int getDistToNextNewline()
		{
			return rmc.getDistToNextNewline();
		}

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
  public Vector<HighlightStatus> getHighlightStatus(final int start,
                                                    final int length)
  {
    Vector<HighlightStatus> vec = new Vector<HighlightStatus>();

    int curState;
    int curLocation;
    int curLength;

    ModelList<ReducedToken>.Iterator cursor = rmc._cursor.copy();
    curLocation = start;
    curLength = cursor.current().getSize() - rmc._offset;
    curState = cursor.current().getHighlightState();

    while ((curLocation + curLength) < (start + length)) {
      cursor.next();
      int nextState = cursor.current().getHighlightState();

      if (nextState == curState) {
        // add on and keep building
        curLength += cursor.current().getSize();
      }
      else {
        // add old one to the vector and start new one
        vec.addElement(new HighlightStatus(curLocation, curLength, curState));
        curLocation += curLength; // new block starts after previous one
        curLength = cursor.current().getSize();
        curState = nextState;
      }
    }

    // Make sure this token length doesn't extend past start+length.
    // This is because we guarantee that the returned vector only refers
    // to chars on [start, start+length).
    int requestEnd = start + length;
    if ((curLocation + curLength) > requestEnd) {
      curLength = requestEnd - curLocation;
    }

    // Add the last one, which has not been added yet
    vec.addElement(new HighlightStatus(curLocation, curLength, curState));

    cursor.dispose();

    return vec;
  }
}





