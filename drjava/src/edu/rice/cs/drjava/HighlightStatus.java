package  edu.rice.cs.drjava;

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



