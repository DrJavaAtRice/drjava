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

/** Interface supported by all document regions used in search results, bookmarks, and breakpoints (e.g., region classes
  * other than DummyDocumentRegion and BrowserDocumentRegion).  OrderedDocumentRegions are presumed to contain three 
  * fields: an OpenDefinitionsDocument, a start Postion, and an end Position, where Positions refers to 
  * javax.swing.text.Position.  They are presumed to be ordered by first by document (using some form of lexicographic 
  * ordering on the name), then by start Position and then by end Position.
  * 
  * All implementations of this interface should be immutable (at the level of the field bindings in those classes), but
  * a given document Position can "move" when the associated document is modified.  As a result, conventional hashing on 
  * OrderedDocumentRegions will produce upredictable results (WILL NOT WORK) unless hashing on Positions works (which 
  * this design does NOT presume).  On the other hand, the relative ordering of Positions is nearly invariant;
  * formerly distinct Positions can be coalesced which in unusual corner cases can invert the ordering between
  * overlapping regions.  In addition, mutating the document backing an OrderedDocumentRegion can coarsen the compareTo
  * ordering, equating DocumentRegions that were previously unequal, but it rarely alters the weak ordering 
  * among regions. In the rare cases when the ordering between two DocumentRegions changes, their start Positions
  * are coalesced by the change.
  *
  * If {@literal a1 <= a2 <= .. .<= an}, this property typically remains invariant under mutation.  The rare cases
  * when ordering changes can be efficiently addressed by reordering all elements that have coalesced as a result
  * of a change.  In contrast, if a1 = a2 = ... = an, this property ALWAYS remains invariant.  As a result, searches 
  * in SortedSets of OrderedDocumentRegions work as long as the requisite reordering is atomically performed when 
  * the backing document is modified.  In most cases, no such reordering is necessary because no coalescing of start
  * Positions occurs. (Note the complements of the preceding two relations are NOT invariant as a document is modified.)
  */
public interface OrderedDocumentRegion extends IDocumentRegion, Comparable<OrderedDocumentRegion> {
  public int getLineStartOffset();
  public int getLineEndOffset();
  public String getString();
  public boolean isEmpty();
}

