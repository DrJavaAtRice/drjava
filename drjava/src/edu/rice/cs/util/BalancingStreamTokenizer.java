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

package edu.rice.cs.util;

import java.io.Reader;
import java.io.IOException;
import java.util.Stack;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.ArrayList;

import edu.rice.cs.plt.lambda.Lambda;

/** A tokenizer that splits a stream into string tokens while balancing quoting characters.
  * @author Mathias Ricken
  * @version $Id: BalancingStreamTokenizer.java 5668 2012-08-15 04:58:30Z rcartwright $
 */

public class BalancingStreamTokenizer {
  /** Input Reader. */
  protected Reader _reader;

  /** Stack of characters having been pushed back. */
  public Stack<Integer> _pushed = new Stack<Integer>();
  
  /** State of the tokenizer. */
  public static class State {
    /** Pairs of beginning and ending quote strings. */
    public HashMap<String,String> quotePairs = new HashMap<String,String>();
    
    /** Sets of quote beginnings to be parsed as one symbol. */
    public TreeSet<String> quotes = new TreeSet<String>();

    /** Sets of quote endings to be parsed as one symbol. */
    public TreeSet<String> quoteEnds = new TreeSet<String>();

    /** Sets of keywords to be parsed as one symbol. */
    public TreeSet<String> keywords = new TreeSet<String>();

    /** Whitespace characters. */
    public HashSet<Integer> whitespace = new HashSet<Integer>();
    
    /** Default constructor. */
    public State() { }
    
    /** Copy constructor. */
    public State(State o) {
      quotePairs = new HashMap<String,String>(o.quotePairs);
      keywords = new TreeSet<String>(o.keywords);
      quotes = new TreeSet<String>(o.quotes);
      quoteEnds = new TreeSet<String>(o.quoteEnds);
      whitespace = new HashSet<Integer>(o.whitespace);
    }
  }
    
  /** Current state of the tokenizer. */
  protected State _state = new State();

  /** Stack of previous states. */
  protected Stack<State> _stateStack = new Stack<State>();
  
  /** Escape character, if available. If this character is placed in front
    * of any quote or keyword, the quote or keyword is treated as normal text.
    * To get this character to exist alone, it has to be doubled up.
    * If this escape character appears alone where it does not precede another escape
    * character, whitespace, a quote or keyword, it is dropped.
    * The escape character CANNOT be declared whitespace.
    * The escape character CAN be part of a quote or keyword, but it has to be
    * doubled up in the string, and when the quotes or keywords are added,
    * the escape character is automatically doubled up if present.
    * If set to null, no escaping is possible. */
  protected Character _escape = null;
  
  /** The previous character was the escape character. */
  protected boolean _wasEscape = false;

  /** The current character is the escape character. */
  protected boolean _isEscape = false;

  /** Kind of tokens to be returned. */
  public enum Token { NONE, NORMAL, QUOTED, KEYWORD, END }

  public volatile Token _token = Token.NONE;
  
  /** Create a new balancing stream tokenizer.
   * @param r reader to tokenize
   */
  public BalancingStreamTokenizer(Reader r) {
    this(r,null);
  }
  
  /** Create a new balancing stream tokenizer.
   * @param r reader to tokenize
   * @param escape escape character or null
   */
  public BalancingStreamTokenizer(Reader r, Character escape) {
    _escape = escape;
    _reader = r;
  }
  
  /** Setup a tokenizer with just whitespace. */
  public void defaultWhitespaceSetup() {
    wordRange(0,255);
    whitespaceRange(0,32);
  }
  
  /** Setup a tokenizer that recognizes " and ' quotes. */
  public void defaultTwoQuoteSetup() {
    wordRange(0,255);
    whitespaceRange(0,32);
    addQuotes("\"", "\"");
    addQuotes("'", "'");
  }

  /** Setup a tokenizer that recognizes ", ' and ` quotes. */
  public void defaultThreeQuoteSetup() {
    wordRange(0,255);
    whitespaceRange(0,32);
    addQuotes("\"", "\"");
    addQuotes("'", "'");
    addQuotes("`", "`");
  }
  
  /** Setup a tokenizer that recognizes " and ' quotes and { } braces. */
  public void defaultTwoQuoteCurlySetup() {
    wordRange(0,255);
    whitespaceRange(0,32);
    addQuotes("\"", "\"");
    addQuotes("'", "'");
    addQuotes("{", "}");
  }
  
  /** Setup a tokenizer that recognizes ", ' and ` quotes and { } braces. */
  public void defaultThreeQuoteCurlySetup() {
    wordRange(0,255);
    whitespaceRange(0,32);
    addQuotes("\"", "\"");
    addQuotes("'", "'");
    addQuotes("`", "`");
    addQuotes("{", "}");
  }
  
  /** Setup a tokenizer that recognizes ", ' and ` quotes and ${ } braces. */
  public void defaultThreeQuoteDollarCurlySetup() {
    wordRange(0,255);
    whitespaceRange(0,32);
    addQuotes("\"", "\"");
    addQuotes("'", "'");
    addQuotes("`", "`");
    addQuotes("${", "}");
  }
  
  /** Return the next token from the reader, or from the stack if it isn't empty.
   * @return next token or -1 when end of stream
   */
  protected int nextToken() throws IOException {
    if (_pushed.empty()) {
      return _reader.read();
    }
    else {
      return _pushed.pop();
    }
  }
  
  /** Push a token back onto the stack.
   * @param token token to push back
   */
  protected void pushToken(int token) {
    _pushed.push(token);
  }
  
  /** Return a copy of the current state of the tokenizer.
   * @return copy of the state
   */
  public State getState() { return new State(_state); }
  
  /** Set the stream tokenizer the the state specified.
   * @param state state
   */
  public void setState(State state) { _state = state; }
  
  /** Push the current state onto the stack. */
  protected void pushState() { _stateStack.push(_state); }
  
  /** Pops the top of the state stack and makes it the current state. */
  protected void popState() { setState(_stateStack.pop()); }

  /** Returns the type of the current token. */
  public Token token() { return _token; }
  
  /** Specify a range characters as word characters.
   * @param lo the character beginning the word character range, inclusive
   * @param hi the character ending the word character range, inclusive
   */
  public void wordRange(int lo, int hi) {
    ArrayList<String> kwToRemove = new ArrayList<String>();
    ArrayList<String> qpToRemove = new ArrayList<String>();
    for(int i = lo; i <= hi; ++i) {
      // now remove all whitespace in that range
      if (_state.whitespace.contains(i)) {
        _state.whitespace.remove(i);
      }
      
      // now accumulate all keywords that begin with that character
      Iterator<String> kit = _state.keywords.iterator();
      while(kit.hasNext()) {
        String s = kit.next();
        if (s.charAt(0) == i) { kwToRemove.add(s); }
      }

      // now accumulate all quotes that begin with that character
      Iterator<String> qit = _state.quotes.iterator();
      while(qit.hasNext()) {
        String s = qit.next();
        if (s.charAt(0) == i) { qpToRemove.add(s); }
      }
    }
    // remove all accumulated keywords and quotes
    for(String s: kwToRemove) { _state.keywords.remove(s); }
    for(String s: qpToRemove) {
      _state.quotes.remove(s);
      _state.quoteEnds.remove(_state.quotePairs.get(s));
      _state.quotePairs.remove(s);
    }
  }

  /** Specify one or more characters as word characters.
   * @param c the character(s)
   */
  public void wordChars(int... c) {
    ArrayList<String> kwToRemove = new ArrayList<String>();
    ArrayList<String> qpToRemove = new ArrayList<String>();
    for(int i: c) {
      // now remove all whitespace in that range
      if (_state.whitespace.contains(i)) {
        _state.whitespace.remove(i);
      }
      
      // now accumulate all keywords that begin with that character
      Iterator<String> kit = _state.keywords.iterator();
      while(kit.hasNext()) {
        String s = kit.next();
        if (s.charAt(0) == i) { kwToRemove.add(s); }
      }
      
      // now accumulate all quotes that begin with that character
      Iterator<String> qit = _state.quotes.iterator();
      while(qit.hasNext()) {
        String s = qit.next();
        if (s.charAt(0) == i) { qpToRemove.add(s); }
      }
    }
    // remove all accumulated keywords and quotes
    for(String s: kwToRemove) { _state.keywords.remove(s); }
    for(String s: qpToRemove) {
      _state.quotes.remove(s);
      _state.quoteEnds.remove(_state.quotePairs.get(s));
      _state.quotePairs.remove(s);
    }
  }
  
  /** Specify a range characters as whitespace.
   * @param lo the character beginning the whitespace range, inclusive
   * @param hi the character ending the whitespace range, inclusive
   */
  public void whitespaceRange(int lo, int hi) {
    ArrayList<String> kwToRemove = new ArrayList<String>();
    ArrayList<String> qpToRemove = new ArrayList<String>();
    for(int i = lo; i <= hi; ++i) {
      if ((_escape != null) && (i == _escape)) { continue; }

      // set whitespace
      _state.whitespace.add(i);
      
      // now accumulate all keywords that begin with that character
      Iterator<String> kit = _state.keywords.iterator();
      while(kit.hasNext()) {
        String s = kit.next();
        if (s.charAt(0) == i) { kwToRemove.add(s); }
      }

      // now accumulate all quotes that begin with that character
      Iterator<String> qit = _state.quotes.iterator();
      while(qit.hasNext()) {
        String s = qit.next();
        if (s.charAt(0) == i) { qpToRemove.add(s); }
      }
    }
    // remove all accumulated keywords and quotes
    for(String s: kwToRemove) { _state.keywords.remove(s); }
    for(String s: qpToRemove) {
      _state.quotes.remove(s);
      _state.quoteEnds.remove(_state.quotePairs.get(s));
      _state.quotePairs.remove(s);
    }
  }

  /** Specify one or more characters as whitespace.
   * @param c the character(s)
   */
  public void whitespace(int... c) {
    ArrayList<String> kwToRemove = new ArrayList<String>();
    ArrayList<String> qpToRemove = new ArrayList<String>();
    for(int i: c) {
      if ((_escape != null) && (i == _escape)) { continue; }
      
      // set whitespace
      _state.whitespace.add(i);

      // now accumulate all keywords that begin with that character
      Iterator<String> kit = _state.keywords.iterator();
      while(kit.hasNext()) {
        String s = kit.next();
        if (s.charAt(0) == i) { kwToRemove.add(s); }
      }
      
      // now accumulate all quotes that begin with that character
      Iterator<String> qit = _state.quotes.iterator();
      while(qit.hasNext()) {
        String s = qit.next();
        if (s.charAt(0) == i) { qpToRemove.add(s); }
      }
    }
    // remove all accumulated keywords and quotes
    for(String s: kwToRemove) { _state.keywords.remove(s); }
    for(String s: qpToRemove) {
      _state.quotes.remove(s);
      _state.quoteEnds.remove(_state.quotePairs.get(s));
      _state.quotePairs.remove(s);
    }
  }

  /** Specify a pair of quotes.
   * @param begin the beginning quotation mark
   * @param end the ending quotation mark
   */
  public void addQuotes(String begin, String end) {
    begin = escape(begin);
    end = escape(end);
    
    // check if the first character of the beginning quotation mark is considered whitespace
    Iterator<Integer> wit = _state.whitespace.iterator();
    while(wit.hasNext()) {
      int c = wit.next();
      if (begin.charAt(0) == c) {
        throw new QuoteStartsWithWhitespaceException("Cannot add quote pair '" + 
                                                     begin + "'-'" + end + "' because the first character of the beginning has " + 
                                                     "already been marked as whitespace");
      }
    }
    // check that there is not already a quote pair that begins with this end string
    Iterator<String> qit = _state.quotes.iterator();
    while(qit.hasNext()) {
      String s = qit.next();
      if (s.equals(end)) {
        throw new QuoteStartsWithWhitespaceException("Cannot add quote pair '" + begin + "'-'" + end+
                                                     "' because the end is already used as beginning of another quote pair");
      }
    }

    // add or replace pair of quotation marks
    String b = null;
    qit = _state.quotes.iterator();
    while(qit.hasNext()) {
      b = qit.next();
      if (b.equals(begin)) { break; }
    }
    if ((b != null) && (qit.hasNext())) {
      _state.quotes.remove(b);
      _state.quoteEnds.remove(_state.quotePairs.get(b));
      _state.quotePairs.remove(b);
    }
    _state.quotes.add(begin);
    _state.quoteEnds.add(end);
    _state.quotePairs.put(begin,end);
    
    // now accumulate all keywords that begin with that character
    ArrayList<String> kwToRemove = new ArrayList<String>();
    Iterator<String> kit = _state.keywords.iterator();
    while(kit.hasNext()) {
      String s = kit.next();
      if (s.startsWith(begin)) { kwToRemove.add(s); }
    }
    // remove all accumulated keywords
    for(String s: kwToRemove) { _state.keywords.remove(s); }
  }
  
  /** Specify a new keyword.
   * @param kw the new keyword
   */
  public void addKeyword(String kw) {
    kw = escape(kw);

    // check if the first character of the beginning quotation mark is considered whitespace
    Iterator<Integer> wit = _state.whitespace.iterator();
    while(wit.hasNext()) {
      int c = wit.next();
      if (kw.charAt(0) == c) {
        throw new KeywordStartsWithWhitespaceException("Cannot add keyword '" + 
                                                       kw + "' because the first character of the beginning has " + 
                                                       "already been marked as whitespace");
      }
    }

    // check if the keyword is considered a beginning quotation mark
    Iterator<String> qit = _state.quotes.iterator();
    while(qit.hasNext()) {
      String s = qit.next();
      if (s.startsWith(kw)) {
        throw new KeywordStartsWithQuoteException("Cannot add keyword '" + 
                                                  kw + "' because it has the same beginning as the quote pair '" + 
                                                  s + "'-'" + _state.quotePairs.get(s) + "'");
      }
    }

    // add keyword
    _state.keywords.add(kw);
  }
  
  /** Return the next token, or null if the end of the stream has been reached.
   * @return next token, or null if end of stream has been reached.
   */
  public String getNextToken() throws IOException {
    StringBuilder buf = new StringBuilder();
    int c = nextToken();
    while (c != -1) {
      _isEscape = ((_escape != null) && (((char)c) == _escape));
      
      // see if this is whitespace
      if (_state.whitespace.contains(c)) {
        if (_wasEscape) {
          // there was a previous escape, do not count as whitespace
          buf.append(String.valueOf((char)c));
          _wasEscape = false;
        }
        else {
          if (buf.length() > 0) {
            _token = Token.NORMAL;
            return buf.toString();
          }
        }
        c = nextToken();
        continue;
      }

      if (!_wasEscape) {
        // see if it can be a quote
        String temp;
        temp = findMatch(c, _state.quotes, new Lambda<String,String>() {
          public String value(String in) {
            // we didn't find a match
            // push the tokens back, all except for the last one
            for(int i=in.length()-1; i > 0; --i) {
              pushToken(in.charAt(i));
            }
            return null;
          }
        });
        if (temp != null) {
          // we found the beginning of a quote
          if (buf.length() > 0) {
            // but we still have regular text to output
            // so we need to push all tokens back
            for(int i=temp.length()-1; i >= 0; --i) {
              pushToken(temp.charAt(i));
            }
            _token = Token.NORMAL;
            return buf.toString();
          }
          String begin = temp;
          Stack<String> quoteStack = new Stack<String>();
          quoteStack.add(begin);
          StringBuilder quoteBuf = new StringBuilder(unescape(begin));
          
          // push the state of the tokenizer and set up a new state:
          // - no whitespace, i.e. whitespace is not discarded
          // - scan for both ending and beginning quotes, but as keywords
          // - no quotes at all
          pushState();
          _state = new State();
          _state.whitespace.clear();
          _state.keywords.clear();
          _state.keywords.addAll(_stateStack.peek().quotes);
          _state.keywords.addAll(_stateStack.peek().quoteEnds);
          _state.quotes.clear();
          _state.quoteEnds.clear();
          _state.quotePairs.clear();
          
          while(quoteStack.size() > 0) {
            String s = getNextToken();
            if (s == null) { break; }
            if (_stateStack.peek().quoteEnds.contains(s)) {
              // ending quote
              String top = quoteStack.peek();
              if (_stateStack.peek().quotePairs.get(top).equals(s)) {
                // matches top of stack
                quoteBuf.append(unescape(s));
                quoteStack.pop();
              }
              else {
                // closing quote does not match top of stack
                // it may be an opening quote though
                if (_stateStack.peek().quotes.contains(s)) {
                  // beginning quote
                  quoteBuf.append(unescape(s));
                  quoteStack.add(s);
                }
                else {
                  // neither a matching closing brace nor an opening brace
                  quoteBuf.append(s);
                  break;
                }
              }
            }
            else if (_stateStack.peek().quotes.contains(s)) {
              // beginning quote
              quoteBuf.append(unescape(s));
              quoteStack.add(s);
            }
            else {
              quoteBuf.append(s);
            }
          }
          
          // restore the old state
          popState();
          _token = Token.QUOTED;
          return quoteBuf.toString();
        }
      }
      
      if (!_wasEscape) {
        // it wasn't a quote, see if it is a keyword
        String temp = findMatch(c, _state.keywords, new Lambda<String,String>() {
          public String value(String in) {
            // we didn't find a match
            // push the tokens back, all except for the last one
            for(int i=in.length()-1; i > 0; --i) {
              pushToken(in.charAt(i));
            }
            return null;
          }
        });
        if (temp != null) {
          // we found a keyword
          if (buf.length() > 0) {
            // but we still have regular text to output
            // so we need to push all tokens back
            for(int i=temp.length()-1; i >= 0; --i) {
              pushToken(temp.charAt(i));
            }
            _token = Token.NORMAL;
            return buf.toString();
          }
          _token = Token.KEYWORD;
          return unescape(temp);
        }
      }

      // it must be a regular word
      // append character to buffer
      if (_isEscape) {
        if (_wasEscape) {
          buf.append(String.valueOf(_escape));
          _isEscape = _wasEscape = false;
        }
        else {
          // there was an escape
          // see if whitespace or escape is coming up
          // System.err.println("There was an escape");
          int cnext = nextToken();
          if ((cnext!=(int)_escape) && (!_state.whitespace.contains(cnext))) {
            // System.err.println("But it's not an escape or whitespace");
            // see if a quote might be coming up
            String temp = findMatch(cnext, _state.quotes, new Lambda<String,String>() {
              public String value(String in) { 
                // push the tokens back
                for(int i=in.length()-1; i > 0; --i) {
                  pushToken(in.charAt(i));
                }
                return null;
              }
            });
            if (temp != null) {
              // push the tokens back
              for(int i=temp.length()-1; i > 0; --i) {
                pushToken(temp.charAt(i));
              }
              // System.err.println("It looks like a quote");
            }
            else {
              // System.err.println("But it's not a quote");
              // it wasn't a quote, see if it could be a keyword
              temp = findMatch(cnext, _state.keywords, new Lambda<String,String>() {
                public String value(String in) {
                  // push the tokens back
                  for(int i=in.length()-1; i > 0; --i) {
                    pushToken(in.charAt(i));
                  }
                  return null;
                }
              });
              if (temp != null) {
                // push the tokens back
                for(int i=temp.length()-1; i > 0; --i) {
                  pushToken(temp.charAt(i));
                }
                // System.err.println("It looks like a keyword");
              }
              else {
                // System.err.println("But it's not a keyword ==> lone escape");
                // neither a quote nor a keyword coming up
                // lone escape
                buf.append(String.valueOf(_escape));
                _isEscape = _wasEscape = false;
              }
            }
          }
          pushToken(cnext);
        }
      }
      else {
        buf.append(String.valueOf((char)c));
      }
      _wasEscape = _isEscape;
      c = nextToken();
    }
    if (_wasEscape) {
      // last thing we saw was a lone escape
      // generously append it
      buf.append(String.valueOf(_escape));
    }
    // end of stream, return remaining buffer as last token
    if (buf.length() > 0) {
      _token = Token.NORMAL;
      return buf.toString();
    }
    // or return null to represent the end of the stream
    _token = Token.END;
    return null;
  }
  
  /** Return the subset of the set whose entries begin with the prefix.
   * @param set parent set
   * @param prefix prefix string
   * @return subset of only those entries that begin with the prefix
   */
  public static TreeSet<String> prefixSet(Set<String> set, String prefix) {
    TreeSet<String> out = new TreeSet<String>();
    Iterator<String> it = set.iterator();
    while(it.hasNext()) {
      String s = it.next();
      if (s.startsWith(prefix)) { out.add(s); }
    }
    return out;
  }

  protected String findMatch(int c, TreeSet<String> choices, Lambda<String,String> notFoundLambda) throws IOException {
    StringBuilder buf = new StringBuilder(String.valueOf((char)c));
    SortedSet<String> prefixSet = prefixSet(choices,buf.toString());
    while(prefixSet.size()>1) { // while there is no definite answer, keep reading tokens
      c = nextToken();
      if (c!=-1) {
        // add character to the string, and narrow prefix set
        buf.append(String.valueOf((char)c));
        prefixSet = prefixSet(choices,buf.toString());
      }
      else {
        // end of stream reached without finding a match
        break;
      }
    }
    if ((c!=-1) && 
        (prefixSet.size() == 1) && 
        (choices.contains(prefixSet.first()))) {
      // there is only one match
      String match = prefixSet.first();
      // read tokens to make sure it actually is it
      while((c!=-1) && (buf.length()<match.length())) {
        c = nextToken();
        if (c!=-1) {
          // add character to the string, and narrow prefix set
          buf.append(String.valueOf((char)c));
        }
        else {
          // end of stream reached without finding a match
          break;
        }
      }
      if (buf.toString().equals(match)) { return buf.toString(); }
    }
    return notFoundLambda.value(buf.toString());
  }
  
  
  protected String escape(String s) {
    if (_escape == null) { return s; }
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < s.length(); ++i) {
      if (i == 0) { sb.append(s.charAt(0)); }
      else {
      if (s.charAt(i) == _escape) { sb.append(_escape); }
      sb.append(s.charAt(i));
    }
    }
    return sb.toString();
  }
  
  protected String unescape(String s) {
    if (_escape == null) { return s; }
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < s.length(); ++i) {
      if (i == 0) { sb.append(s.charAt(0)); }
      else {
      if (s.charAt(i) == _escape) {
        if ((i+1<s.length()) && (s.charAt(i+1) == _escape)) { ++i; }
      }
      sb.append(s.charAt(i));
    }
    }
    return sb.toString();
  }

  /** Setup exception. */
  public static class SetupException extends RuntimeException {
    public SetupException(String s) { super(s); }
  }
  
  /** Quote or keyword starts with whitespace exception. */
  public static class StartsWithWhitespaceException extends SetupException {
    public StartsWithWhitespaceException(String s) { super(s); }
  }
  
  /** Quote starts with whitespace exception. */
  public static class QuoteStartsWithWhitespaceException extends StartsWithWhitespaceException {
    public QuoteStartsWithWhitespaceException(String s) { super(s); }
  }
  
  /** Keyword starts with whitespace exception. */
  public static class KeywordStartsWithWhitespaceException extends StartsWithWhitespaceException {
    public KeywordStartsWithWhitespaceException(String s) { super(s); }
  }
  
  /** Keyword starts with quote exception. */
  public static class KeywordStartsWithQuoteException extends SetupException {
    public KeywordStartsWithQuoteException(String s) { super(s); }
  }
}
