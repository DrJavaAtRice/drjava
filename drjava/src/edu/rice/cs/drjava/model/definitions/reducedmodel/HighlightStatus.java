/* $Id$ */

package edu.rice.cs.drjava;

public class HighlightStatus {
  public static final int NORMAL = 0;
  public static final int COMMENTED = 1;
  public static final int QUOTED = 2;
  public static final int KEYWORD = 3;

  private int _state;
  private int _location;
  private int _length;

  public HighlightStatus(int location, int length, int state) {
    _location = location;
    _length = length;
    _state = state;
  }

  public int getState() { return _state; }
  public int getLocation() { return _location; }
  public int getLength() { return _length; }
}
