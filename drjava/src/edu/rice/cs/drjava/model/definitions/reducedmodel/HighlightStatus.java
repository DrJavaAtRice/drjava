package  edu.rice.cs.drjava;

/**
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
   * put your documentation comment here
   * @param     int location
   * @param     int length
   * @param     int state
   */
  public HighlightStatus(int location, int length, int state) {
    _location = location;
    _length = length;
    _state = state;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public int getState() {
    return  _state;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public int getLocation() {
    return  _location;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public int getLength() {
    return  _length;
  }
}



