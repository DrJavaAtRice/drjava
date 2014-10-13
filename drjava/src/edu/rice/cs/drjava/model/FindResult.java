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

package edu.rice.cs.drjava.model;

/** Returned to FindMachineDialog with the location of the found string
  * (or -1 if the string was not found) as well as a flag indicating
  * whether the machine wrapped around the end of the document.
  *
  * @version $Id: FindResult.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class FindResult {
  private OpenDefinitionsDocument _document;
  private int _foundoffset;
  private boolean _wrapped;
  private boolean _allWrapped;
  
  /** Constructor for a FindResult.
   * @param document the document where the found instance is located
   * @param foundoffset the offset of the instance found
   * @param wrapped {@code true} if the search wrapped to the beginning (or end) of the document
   * @param allWrapped {@code true} if the search wrapped to the start document
   */
  public FindResult(OpenDefinitionsDocument document, int foundoffset, boolean wrapped, boolean allWrapped) {
    _document = document;
    _foundoffset = foundoffset;
    _wrapped = wrapped;
    _allWrapped = allWrapped;
  }
  
  /** Intelligible toString method */
  public String toString() {
    return "FindResult(" + _document + ", " + _foundoffset + ", " + _wrapped + ", " + _allWrapped + ")";
  }
  
  /** Returns the document where the found instance is located */
  public OpenDefinitionsDocument getDocument() { return _document; }
  
  /** Returns the offset of the instance found */ 
  public int getFoundOffset() { return _foundoffset; }
  
  /** Returns true if the search wrapped to the beginning (or end) of the document */
  public boolean getWrapped() { return _wrapped; }
  
  /** Returns true if the search wrapped to the start document. */
  public boolean getAllWrapped() { return _allWrapped; }
}
