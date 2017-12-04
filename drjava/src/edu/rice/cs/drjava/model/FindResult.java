/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model;

/** This class implements a findResults tuple conttaining the location of the found string (or -1 if the string was not
  * found), the current document, the offset of the matching string, a flag indicating whether the machine wrapped 
  * around the end of the document (local search), and a flag indicating whether the search wrapped back to the start 
  * document (global search)
  * @version $Id$
  */
public class FindResult {
  private final OpenDefinitionsDocument _document;
  private final int _foundoffset;
  private final boolean _wrapped;
  private final boolean _allWrapped;
  
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
  
  /** @return the document where the found instance is located */
  public OpenDefinitionsDocument getDocument() { return _document; }
  
  /** @return the offset of the instance found */ 
  public int getFoundOffset() { return _foundoffset; }
  
  /** @return true if the search wrapped to the beginning (or end) of the document */
  public boolean isWrapped() { return _wrapped; }
  
  /** @return true if the search wrapped to the start document. */
  public boolean getAllWrapped() { return _allWrapped; }
}
