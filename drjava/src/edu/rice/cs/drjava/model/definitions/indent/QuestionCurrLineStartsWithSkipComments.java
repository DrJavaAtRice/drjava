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

package edu.rice.cs.drjava.model.definitions.indent;

import javax.swing.text.*;
import edu.rice.cs.util.UnexpectedException;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/**
 * Determines whether or not the current line in the document starts
 * with a specific character sequence, skipping over any comments on that line.
 * The character sequence is passed to the constructor of the class as a String
 * argument.
 *
 * @version $Id$
 */
public class QuestionCurrLineStartsWithSkipComments extends IndentRuleQuestion
{
  /**
   * The String to be matched. This String may not contain whitespace characters
   * or comment-delimiting characters.
   */
  private String _prefix;
  
  /**
   * @param yesRule The decision subtree for the case that this rule applies
   * in the current context.
   * @param noRule The decision subtree for the case that this rule does not
   * apply in the current context.
   */
  public QuestionCurrLineStartsWithSkipComments(String prefix, IndentRule yesRule, IndentRule noRule)
  {
    super(yesRule, noRule);
    _prefix = prefix;
  }
  
  /**
   * Determines whether or not the current line in the document starts
   * with the character sequence specified by the String field _prefix,
   * skipping over any comments on that line.
   * @param doc The DefinitionsDocument containing the current line.
   * @return True iff the current line in the document starts with the
   * character sequence specified by the String field _prefix.
   */
  boolean applyRule(DefinitionsDocument doc, int reason)
  {
    try
    {
      // Find the first non-whitespace character on the current line.
      
      int currentPos = doc.getCurrentLocation(),
        startPos   = doc.getLineFirstCharPos(currentPos),
        endPos     = doc.getLineEndPos(currentPos),
        lineLength = endPos - startPos;
      
      char currentChar, previousChar = '\0';
      String text = doc.getText(startPos, lineLength);
      
      for (int i = 0; i < lineLength; i++)
      {
        // Get state for walker position.
        //BraceReduction reduced = doc.getReduced();
        
        synchronized(doc){
          doc.move( startPos - currentPos + i);
          ReducedModelState state = doc.getStateAtCurrent();
          doc.move(-startPos + currentPos - i);
          
          
          currentChar = text.charAt(i);
          
          if (state.equals(ReducedModelState.INSIDE_LINE_COMMENT)) 
          {
            return false;
          }
          if (state.equals(ReducedModelState.INSIDE_BLOCK_COMMENT))
          {
            // Handle case: ...*/*
            previousChar = '\0'; continue;
          }
          if (state.equals(ReducedModelState.FREE))
          {
            // Can prefix still fit on the current line?
            if (_prefix.length() > lineLength - i)
            {
              return false;
            }
            else if (text.substring(i, i+_prefix.length()).equals(_prefix) && previousChar != '/')
            {
              // '/' is the only non-WS character that we consume without
              // immediately returning false. When we try to match the prefix,
              // we also need to reflect this implicit lookahead mechanism.
              return true;
            }
            else if (currentChar == '/')
            {
              if (previousChar == '/') { return false; }
            }
            else if (currentChar == ' ' || currentChar == '\t')
            {
            }
            else if (!(currentChar == '*' && previousChar == '/'))
            {
              return false;
            }
          }
        }
        if (previousChar == '/' && currentChar != '*')
        {
          return false;
        }
        previousChar = currentChar;
      }
      return false;
    }
    catch (BadLocationException e)
    {
      // Control flow should never reach this point!
      throw new UnexpectedException(new RuntimeException("Bug in QuestionCurrLineStartsWithSkipComments"));
    }
  }
}
