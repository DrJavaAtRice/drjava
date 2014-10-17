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

import static edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelStates.*;

/** Represents the abstract notion of a shadowing state.  The shadowing state of text is simply its interpretation during
  * compilation.  Commented text is ignored; quoted text is accumulated into string constants.  This classification
  * supports accurate highlighting, indenting, and other analyses of program text.
  * @version $Id: ReducedModelState.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class ReducedModelState {
  
  abstract ReducedModelState update(TokenList.Iterator copyCursor);

  /** Combines the current and next braces if they match the given types. If we have braces of first and second in
    * immediate succession, and if second's gap is 0, combine them into first+second.  The cursor remains on the same
    * block after this method is called.
    * @param first the first half of a multiple char brace
    * @param second the second half of a multiple char brace
    * @return true if we combined two braces or false if not
    */
  boolean _combineCurrentAndNextIfFind(String first, String second, TokenList.Iterator copyCursor) {
    if (copyCursor.atStart() || copyCursor.atEnd() || copyCursor.atLastItem() ||
        !copyCursor.current().getType().equals(first))
      return false;

    copyCursor.next(); // move to second one to check if we can combine

    // The second one is eligible to combine if it exists (atLast is false),
    // if it has the right brace type, and if it has no gap.
    if (copyCursor.current().getType().equals(second)) {
      if (copyCursor.current().getType().equals("") && copyCursor.prevItem().getType().equals("")) {
        // delete first Gap and augment the second
        copyCursor.prev();
        int growth = copyCursor.current().getSize();
        copyCursor.remove();
        copyCursor.current().grow(growth);
      }
      else if (copyCursor.current().getType().length() == 2) {
        String tail = copyCursor.current().getType().substring(1,2);
        String head = copyCursor.prevItem().getType() +
          copyCursor.current().getType().substring(0,1);
        copyCursor.current().setType(tail);
        copyCursor.prev();
        copyCursor.current().setType(head);
        copyCursor.current().setState(FREE);
      }
      else {
        // delete the first Brace and augment the second
        copyCursor.prev();
        copyCursor.remove();
        copyCursor.current().setType(first + second);
      }
      return true;
    }
    else {
      // we couldn't combine, so move back and return
      copyCursor.prev();
      return false;
    }
  }

  boolean _combineCurrentAndNextIfEscape(TokenList.Iterator copyCursor) {
    boolean combined = _combineCurrentAndNextIfFind("\\","\\",copyCursor);  // \-\
    combined = combined || _combineCurrentAndNextIfFind("\\","\'",copyCursor);  // \-'
    combined = combined || _combineCurrentAndNextIfFind("\\","\\'",copyCursor);// \-\'
    combined = combined || _combineCurrentAndNextIfFind("\\","\"",copyCursor);  // \-"
    combined = combined || _combineCurrentAndNextIfFind("\\","\\\"",copyCursor);// \-\"
    combined = combined || _combineCurrentAndNextIfFind("\\","\\\\",copyCursor);// \-\\
    return combined;
  }
}
