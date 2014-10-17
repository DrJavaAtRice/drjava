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

package edu.rice.cs.drjava.model.definitions.indent;

import javax.swing.text.*;
import java.util.Arrays;
import java.util.StringTokenizer;

import edu.rice.cs.util.UnexpectedException;

import edu.rice.cs.drjava.model.AbstractDJDocument;

/** Question rule in the indentation decision tree.  Determines if the current line starts with the specified string.
  * Some prefixes may be ignored.  Comments may or may be ignored.
  * @version $Id: QuestionCurrLineStartsWith.java 5709 2012-08-30 05:11:09Z rcartwright $
  */
public class QuestionCurrLineStartsWith extends IndentRuleQuestion {
  private final String _prefix;
  private final int _prefLen; // length of _prefix
  private final String[] _excludedSuffixes;  // SORTED array of excluded suffix strings
  private final boolean _acceptComments;
  private static final String SUFFIX_DELIMITERS = " \t\n\r{}()[]="; // not in sorted order
  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  
  /** Constructs a new rule for the given prefix string and excludedSuffixes.
    * @param prefix String to search for
    * @param excludedSuffixes is a SORTED array of excluded suffix strings
    * @param yesRule Rule to use if this rule holds
    * @param noRule Rule to use if this rule does not hold
    * @param acceptComments whether to include comment text in search
    */
  public QuestionCurrLineStartsWith(String prefix, String[] excludedSuffixes, IndentRule yesRule, IndentRule noRule, 
                                    boolean acceptComments) {
    super(yesRule, noRule);
    _prefix = prefix;
    _prefLen = (prefix == null) ? 0 : _prefix.length();
    _excludedSuffixes = excludedSuffixes;
    _acceptComments = acceptComments;
  }
  
  /** Constructs a new rule for the given prefix string and excludedSuffixes that ignores comments
    * @param prefix String to search for
    * @param excludedSuffixes is a SORTED array of excluded suffix strings
    * @param yesRule Rule to use if this rule holds
    * @param noRule Rule to use if this rule does not hold
    * @param acceptComments whether to include comment text in search
    */
  public static QuestionCurrLineStartsWith newQuestionWithSuffixesSkipComments(String prefix, String[] excludedSuffixes, 
                                                                               IndentRule yesRule, IndentRule noRule) {
   return new QuestionCurrLineStartsWith(prefix, excludedSuffixes, yesRule, noRule, false);
  }
  
  /** Constructs a new rule for the given prefix string that ignores comments
    * @param prefix String to search for
    * @param excludedSuffixes is a SORTED array of excluded suffix strings
    * @param yesRule Rule to use if this rule holds
    * @param noRule Rule to use if this rule does not hold
    * @param acceptComments whether to include comment text in search
    */
  public static QuestionCurrLineStartsWith newQuestionSkipComments(String prefix, IndentRule yesRule, IndentRule noRule) {
   return new QuestionCurrLineStartsWith(prefix,  EMPTY_STRING_ARRAY, yesRule, noRule, false);
  }
  
  /** Constructs a new rule for the given prefix string that processes comments. */
  public static QuestionCurrLineStartsWith newQuestion(String prefix, IndentRule yesRule, IndentRule noRule) {
    return new QuestionCurrLineStartsWith(prefix, EMPTY_STRING_ARRAY, yesRule, noRule, true);
  }
  
  /** Determines if the current line in the document starts with the specified prefix, ignoring whitespace and,
    * optionally, comments.
    * If the prefix is null or empty, returns true.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @param reason The reason that the indentation is being done
    * @return true if this node's rule holds.
    */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {
    System.err.println("***** QCLSW.applyRule called; _prefix = '" + _prefix + "' exclude = " + 
                       Arrays.toString(_excludedSuffixes) + " currentLocation = " + doc.getCurrentLocation());
    if (_prefLen == 0) return true;
    
    try {
      // Find start of line
      int lineStart = doc._getLineStartPos();
      
//      System.err.println("*** doc text from lineStart is: '" + doc.getText().substring(lineStart) + "'");
      
      int firstCharPos = doc.getFirstNonWSCharPos(lineStart, _acceptComments);
      int lineEndPos = doc._getLineEndPos(firstCharPos);
      
      System.err.println("  lineStart = " + lineStart + "  firstCharPos = " + firstCharPos + " lineEndPos = " + 
                         lineEndPos);

      // If prefix would run off the end of the line, return false
      if (firstCharPos + _prefLen > lineEndPos) {
        return false;
      }
      
      // Compare prefix
      int textLen = doc.getLength() - firstCharPos;
      
      assert textLen >= _prefLen;  // lineEndPos <= textLen => textLen > lineEndPos - firstCharPos
      
      if (textLen < _prefLen) return false;    // current line cannot possibly start with _prefix
      String text = doc.getText(firstCharPos, textLen);
      String actualPrefix = text.substring(0, _prefLen);
      System.err.println("  actualPrefix = " + actualPrefix + " equalsTest = " + _prefix.equals(actualPrefix));
      if (_prefix.equals(actualPrefix)) return (_excludedSuffixes.length == 0) || confirmExcludedSuffixes(text, textLen);
      return false;
    }
    catch (BadLocationException e) { return false; }  // no first NonWS char exists; hence non-empty _prefix cannot match
  }
  
  /** Given text starts with _prefix, confirms that the first word in the sequel (after _prefix) is not in 
    * _excludedSuffixes */
  private boolean confirmExcludedSuffixes(String text, int len) {
    if (len == _prefLen) return true;  // no chars follow the prefix
    char nextChar = text.substring(_prefLen, _prefLen + 1).charAt(0);
    if (! Character.isWhitespace(nextChar)) return false;  // if char following prefix is not whitespace, the prefix is bad

    String sequel = text.substring(_prefLen + 1, len - _prefLen - 1); // sequel is text following the whitespace char
    StringTokenizer tokenizer = new StringTokenizer(sequel, SUFFIX_DELIMITERS, true);
    
    if (! tokenizer.hasMoreTokens()) return true;          // if text following whitespace char is empty, condition is confirmed
    String suffix = tokenizer.nextToken();
//    System.err.println("Case suffix token = '" + suffix + "'");
    return Arrays.binarySearch(_excludedSuffixes, suffix) < 0;  // true when excluded suffix is NOT found
  }
}
