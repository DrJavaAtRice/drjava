/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

/**
 * The abstract notion of a shadowing state.  We use shadowing to mean
 * the state of text as it is interpreted during compile.  Commented text
 * is ignored, and quoted text does not factor into the ASTs generated
 * by the compiler except as a text constant. This buys us a lot in
 * terms of correctness when highlighting, indenting, and performing
 * other editor functions.
 * @version $Id$
 */
public abstract class ReducedModelState implements ReducedModelStates {
  abstract ReducedModelState update(TokenList.Iterator copyCursor);

  /**
   * Combines the current and next braces if they match the given types.
   * If we have braces of first and second in immediate succession, and if
   * second's gap is 0, combine them into first+second.
   * The cursor remains on the same block after this method is called.
   * @param first the first half of a multiple char brace
   * @param second the second half of a multiple char brace
   * @return true if we combined two braces or false if not
   */
  boolean _combineCurrentAndNextIfFind(String first, String second,
                                       TokenList.Iterator copyCursor)
  {
    if (copyCursor.atStart() || copyCursor.atEnd() || copyCursor.atLastItem() ||
        !copyCursor.current().getType().equals(first))
    {
      return false;
    }
    copyCursor.next(); // move to second one to check if we can combine

    // The second one is eligible to combine if it exists (atLast is false),
    // if it has the right brace type, and if it has no gap.
    if (copyCursor.current().getType().equals(second)) {
      if ((copyCursor.current().getType().equals("")) &&
          (copyCursor.prevItem().getType().equals(""))) {
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
