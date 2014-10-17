/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

/** A block that represents information about the highlighting status of a particular section in the document.
 *  @version $Id: HighlightStatus.java 5594 2012-06-21 11:23:40Z rcartwright $
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

  /** Constructor.
   * @param location the starting character offset of the block
   * @param length length of block
   * @param state coloring state of the block
   */
  public HighlightStatus(int location, int length, int state) {
    _location = location;
    _length = length;
    _state = state;
  }

  /** Get the coloring state of this block.
   * @return an integer representing the color to paint the text
   * in the bounds of this block
   */
  public int getState() {
    return  _state;
  }

  /** Get the starting location of this coloring block.
   * @return an integer offset
   */
  public int getLocation() {
    return  _location;
  }

  /** Get the size of this coloring block.
   * @return the number of characters spanned by this block.
   */
  public int getLength() {
    return  _length;
  }
}



