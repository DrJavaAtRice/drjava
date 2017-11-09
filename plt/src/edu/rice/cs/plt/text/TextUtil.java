/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.text;

import java.io.Serializable;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.recur.RecurUtil;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.collect.OneToOneRelation;
import edu.rice.cs.plt.collect.IndexedOneToOneRelation;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.LazyThunk;

public final class TextUtil {
  
  /** The system-dependent "line.separator" property. */
  public static final String NEWLINE = System.getProperty("line.separator", "\n");
  
  /** A regex matching any line break: {@code \r\n}, {@code \n}, or {@code \r}. */
  public static final String NEWLINE_PATTERN = "\\r\\n|\\n|\\r";
  
  /** Prevents instance creation */
  private TextUtil() {}
  
  /**
   * Convert the given object to a string.  This method invokes {@link RecurUtil#safeToString(Object)}
   * to provide simple, safe handling of {@code null} values, arrays, and self-referential data structures
   * (with cooperation from the {@code toString()} method of the relevant class).
   */
  public static String toString(Object o) {
    return RecurUtil.safeToString(o);
  }
  
  /**
   * Break a string into a list of lines.  {@code "\n"}, {@code "\r"}, and {@code "\r\n"}
   * are considered line delimiters.  The empty string is taken to contain 0 lines.  An optional final
   * trailing newline will be ignored.
   */
  public static SizedIterable<String> getLines(String s) {
    SizedIterable<String> result = IterUtil.<String>empty();
    BufferedReader r = new BufferedReader(new StringReader(s));
    try {
      String line = r.readLine();
      while (line != null) {
        result = IterUtil.compose(result, line);
        line = r.readLine();
      }
    }
    catch (IOException e) {
      // Should not happen with a StringReader, but if it does, just ignore it
    }
    finally { 
      try { r.close(); }
      catch (IOException e) { /* ignore */ }
    }
    return result;
  }
  
  /** Produce a string by concatenating {@code copies} instances of {@code s} */
  public static String repeat(String s, int copies) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < copies; i++) { result.append(s); }
    return result.toString();
  }
  
  /** Produce a string by concatenating {@code copies} instances of {@code c} */
  public static String repeat(char c, int copies) {
    char[] result = new char[copies];
    Arrays.fill(result, c);
    return String.valueOf(result);
  }
  
  /** Create a string of (at least) the given length by filling in copies of {@code c} to the left of {@code s}. */
  public static String padLeft(String s, char c, int length) {
    StringBuilder result = new StringBuilder();
    int delta = length - s.length();
    for (int i = 0; i < delta; i++) { result.append(c); }
    result.append(s);
    return result.toString();
  }
  
  /** Create a string of (at least) the given length by filling in copies of {@code c} to the right of {@code s}. */
  public static String padRight(String s, char c, int length) {
    StringBuilder result = new StringBuilder();
    result.append(s);
    int delta = length - s.length();
    for (int i = 0; i < delta; i++) { result.append(c); }
    return result.toString();
  }
  
  // Here are subsequently, a javadoc bug requires referring to java.lang.String with a fully-qualified name
  
  /**
   * Determine if the given character occurs in {@code s}.  Defined in terms of
   * {@link java.lang.String#indexOf(int)}.
   */
  public static boolean contains(String s, int character) { return s.indexOf(character) >= 0; }
  
  /**
   * Determine if the given string occurs in {@code s}.  Defined in terms of {@link java.lang.String#indexOf(String)}.
   * This is also defined as {@link String#contains}, but is defined here for legacy support.
   */
  public static boolean contains(String s, String piece) { return s.indexOf(piece) >= 0; }
  
  /**
   * Determine if <em>any</em> of the given characters occurs in {@code s}.  Defined in terms of
   * {@link java.lang.String#indexOf(int)}.
   */
  public static boolean containsAny(String s, int... characters) {
    for (int c: characters) { if (contains(s, c)) { return true; } }
    return false;
  }
  
  /**
   * Determine if <em>any</em> of the given strings occurs in {@code s}.  Defined in terms of
   * {@link java.lang.String#indexOf(String)}.
   */
  public static boolean containsAny(String s, String... pieces) {
    for (String piece: pieces) { if (contains(s, piece)) { return true; } }
    return false;
  }
  
  /**
   * Determine if <em>all</em> of the given characters occur in {@code s}.  Defined in terms of
   * {@link java.lang.String#indexOf(int)}.
   */
  public static boolean containsAll(String s, int... characters) {
    for (int c: characters) { if (!contains(s, c)) { return false; } }
    return true;
  }
  
  /**
   * Determine if <em>all</em> of the given strings occur in {@code s}.  Defined in terms of
   * {@link java.lang.String#indexOf(String)}.
   */
  public static boolean containsAll(String s, String... pieces) {
    for (String piece: pieces) { if (!contains(s, piece)) { return false; } }
    return true;
  }
  
  /**
   * Determine if the given string occurs in {@code s}, ignoring differences in case.  Unlike 
   * {@link java.lang.String#equalsIgnoreCase}, this test only compares the lower-case conversion of
   * {@code s} to the lower-case conversion of {@code piece}.
   */
  public static boolean containsIgnoreCase(String s, String piece) {
    return s.toLowerCase().indexOf(piece.toLowerCase()) >= 0;
  }
  
  /**
   * Determine if <em>any</em> of the given strings occurs in {@code s}, ignoring differences in case.  Defined in 
   * terms of {@link #containsIgnoreCase}.
   */
  public static boolean containsAnyIgnoreCase(String s, String... pieces) {
    for (String piece: pieces) { if (contains(s, piece)) { return true; } }
    return false;
  }
  
  /**
   * Determine if <em>all</em> of the given strings occur in {@code s}, ignoring differences in case.  Defined in 
   * terms of {@link #containsIgnoreCase}.
   */
  public static boolean containsAllIgnoreCase(String s, String... pieces) {
    for (String piece: pieces) { if (!contains(s, piece)) { return false; } }
    return true;
  }
  
  /**
   * Determine if any of the given strings is a prefix of {@code s}.  Defined in terms of
   * {@link java.lang.String#startsWith}.
   */
  public static boolean startsWithAny(String s, String... prefixes) {
    for (String prefix : prefixes) { if (s.startsWith(prefix)) { return true; } }
    return false;
  }
  
  /**
   * Determine if any of the given strings is a suffix of {@code s}.  Defined in terms of
   * {@link java.lang.String#endsWith}.
   */
  public static boolean endsWithAny(String s, String... suffixes) {
    for (String suffix : suffixes) { if (s.endsWith(suffix)) { return true; } }
    return false;
  }
  
  /**
   * Find the first occurrence of any of the given characters in {@code s}.  If none are present, the result is 
   * {@code -1}.  Defined in terms of {@link java.lang.String#indexOf(int)}.
   */
  public static int indexOfFirst(String s, int... characters) {
    int result = -1;
    for (int c : characters) {
      int index = s.indexOf(c);
      if (index >= 0 && (result < 0 || index < result)) { result = index; }
    }
    return result;
  }
  
  /**
   * Find the first occurrence of any of the given strings in {@code s}.  If none are present, the result is 
   * {@code -1}.  Defined in terms of {@link java.lang.String#indexOf(String)}.
   */
  public static int indexOfFirst(String s, String... pieces) {
    int result = -1;
    for (String piece : pieces) {
      int index = s.indexOf(piece);
      if (index >= 0 && (result < 0 || index < result)) { result = index; }
    }
    return result;
  }
  
  /**
   * Extract the portion of {@code s} before the first occurrence of the given delimiter.  {@code s} if the
   * delimiter is not found.
   */
  public static String prefix(String s, int delim) {
    int index = s.indexOf(delim);
    return (index == -1) ? s : s.substring(0, index);
  }
  
  /**
   * Extract the portion of {@code s} after the first occurrence of the given delimiter.  {@code s} if the
   * delimiter is not found.
   */
  public static String removePrefix(String s, int delim) {
    int index = s.indexOf(delim);
    return (index == -1) ? s : s.substring(index+1);
  }
    
  /**
   * Extract the portion of {@code s} after the last occurrence of the given delimiter.  {@code s} if the
   * delimiter is not found.
   */
  public static String suffix(String s, int delim) {
    int index = s.lastIndexOf(delim);
    return (index == -1) ? s : s.substring(index+1);
  }
  
  /**
   * Extract the portion of {@code s} before the last occurrence of the given delimiter.  {@code s} if the
   * delimiter is not found.
   */
  public static String removeSuffix(String s, int delim) {
    int index = s.lastIndexOf(delim);
    return (index == -1) ? s : s.substring(0, index);
  }
  
  /**
   * An extended version of {@link String#split} that recognizes nested parentheses and only splits
   * where the delimiter occurs at the top level.  This convenience method sets {@code limit} to {@code 0}
   * (unlimited number of matches) and {@code brackets} to {@link Bracket#PARENTHESES}.  See
   * {@link #split(String, String, int, Bracket[])} for a full specification.
   */
  public static SplitString splitWithParens(String s, String delimRegex) {
    return new StringSplitter(s, delimRegex, 0, Bracket.PARENTHESES).split();
  }
  
  /**
   * An extended version of {@link String#split} that recognizes nested parentheses and only splits
   * where the delimiter occurs at the top level.  This convenience method sets {@code brackets} to 
   * {@link Bracket#PARENTHESES}.  See {@link #split(String, String, int, Bracket[])} for a full
   * specification.
   */
  public static SplitString splitWithParens(String s, String delimRegex, int limit) {
    return new StringSplitter(s, delimRegex, limit, Bracket.PARENTHESES).split();
  }
  
  /**
   * An extended version of {@link String#split} that recognizes nested matched brackets and only splits
   * where the delimiter occurs at the top level.  This convenience method sets {@code limit} to {@code 0}
   * (unlimited number of matches).  See {@link #split(String, String, int, Bracket[])} for a full
   * specification.
   */
  public static SplitString split(String s, String delimRegex, Bracket... brackets) {
    return new StringSplitter(s, delimRegex, 0, brackets).split();
  }
  
  /**
   * An extended version of {@link String#split} that recognizes nested matched brackets and only splits
   * where the delimiter occurs at the top level.  For convenience when the delimiter is a nontrivial
   * regular expression, the result includes both the split strings and the matched delimiters.  Ignoring
   * these extensions, the behavior is roughly equivalent: {@code s.split(delimRegex, limit)} is equivalent
   * to {@code TextUtil.split(s, delimRegex, limit).array()}, with the exception that trailing empty strings
   * (separated by delimiters) are never discarded here.
   * @param s  A string to split
   * @param delimRegex  A regular expression recognizing delimiters
   * @param limit  The number of non-delimiter pieces to produce.  Consistent with {@code String.split()},
   *               {@code limit-1} is the number of delimiters to search for.  If {@code 0} or negative, the
   *               search continues until the string is exhausted.  Unlike {@code String.split()}, trailing
   *               empty strings (separated by delimiters) are never discarded, even when {@code limit == 0}.
   * @param brackets  Bracket pairs that should be recognized.  A delimiter match that occurs within one of
   *                  these bracket pairs (at any nonzero nesting depth) is not considered a delimiter.
   *                  A left bracket increases the nesting level only if it is at the top level or follows
   *                  another left bracket that supports nesting; a right bracket reduces the nesting level
   *                  only if it matches the most recent left bracket.  If {@code delimRegex} recognizes part
   *                  of a valid bracket (e.g., {@code "*"} is the delimiter and {@code "/*"} is a bracket),
   *                  how relevant text is handled is unspecified (it would be nice, but difficult, to fix this).
   *                  If multiple brackets overlap, an expected right bracket will match before a left bracket,
   *                  and the first left bracket listed in {@code brackets} has priority over later left
   *                  brackets.
   */
  public static SplitString split(String s, String delimRegex, int limit, Bracket... brackets) {
    return new StringSplitter(s, delimRegex, limit, brackets).split();
  }
  
  /**
   * The result of a {@code split()} invocation.  The original string can be formed by concatenating
   * {@code splits()}, {@code delims()} (interleaved), and {@code rest()}.
   */
  public static class SplitString implements Serializable {
    private final List<String> _splits;
    private final List<String> _delims;
    private final String _rest;
    
    private SplitString(List<String> splits, List<String> delims, String rest) {
      _splits = Collections.unmodifiableList(splits);
      _delims = Collections.unmodifiableList(delims);
      _rest = rest;
    }
    
    /**
     * The sequence of strings that were followed by a recognized delimiter.
     * {@code splits().size() == delims().size()}.
     */
    public List<String> splits() { return _splits; }
    /**
     * The delimiters that followed the corresponding members of {@code splits()}.
     * {@code splits().size() == delims().size()}.
     */
    public List<String> delimiters() { return _delims; }
    /**
     * The tail portion of the input string.  Either this string contains no delimiters, or it was
     * left unsearched.
     */
    public String rest() { return _rest; }
    
    /**
     * Fill an array with the non-delimiter portions of the original string.  The array has the same
     * form as the result of {@code String#split}.  It always has length {@code >= 1} and
     * {@code <= limit}, if {@code limit} (a parameter of the {@code split} method) was positive.
     */
    public String[] array() {
      String[] result = new String[_splits.size() + 1];
      _splits.toArray(result);
      result[_splits.size()] = _rest;
      return result;
    }
    
    public String toString() {
      StringBuilder result = new StringBuilder();
      result.append("SplitString: ");
      for (Pair<String, String> pair : IterUtil.zip(_splits, _delims)) {
        result.append("(").append(pair.first()).append(") ");
        result.append("[").append(pair.second()).append("] ");
      }
      result.append("+ (").append(_rest).append(")");
      return result.toString();
    }
  }
  
  /** Implementation of the split algorithm. */
  private static class StringSplitter {
    private final List<String> _splits;
    private final List<String> _delims;
    private final String _s;
    
    private final Matcher _delim;
    private final Bracket[] _brackets;
    private final Matcher[] _lefts;
    private final Matcher[] _rights;
    private final LinkedList<Integer> _stack; // grows left -- use addFirst and removeFirst
    
    private int _remaining;
    
    public StringSplitter(String s, String delimRegex, int limit, Bracket... brackets) {
      if (limit > 0 && limit < 10) { // 10 is the specified default capacity
        _splits = new ArrayList<String>(limit);
        _delims = new ArrayList<String>(limit);
      }
      else {
        _splits = new ArrayList<String>();
        _delims = new ArrayList<String>();
      }
      _s = s;
      _delim = Pattern.compile(delimRegex).matcher(_s);
      _brackets = brackets;
      _lefts = new Matcher[_brackets.length];
      _rights = new Matcher[_brackets.length];
      for (int i = 0; i < _brackets.length; i++) {
        _lefts[i] = _brackets[i].left().matcher(_s);
        _rights[i] = _brackets[i].right().matcher(_s);
      }
      _stack = new LinkedList<Integer>();
      _remaining = limit;
    }
    
    public SplitString split() {
      int rest = 0; // text not yet added to _splits or _delims
      int cursor = 0; // current start location for search; >= rest
      while (_remaining != 1) {
        if (_delim.find()) {
          int dStart = _delim.start();
          int dEnd = _delim.end();
          processStack(cursor, dStart, false);
          if (_stack.isEmpty()) {
            _splits.add(_s.substring(rest, dStart));
            _delims.add(_s.substring(dStart, dEnd));
            if (_remaining > 1) { _remaining--; }
            rest = dEnd;
            cursor = dEnd;
          }
          else {
            cursor = processStack(dStart, _s.length(), true);
            _delim.region(cursor, _s.length()); // skip delimiter search ahead past right brackets
          }
        }
        else { _remaining = 1; /* end search */ }
      }
      return new SplitString(_splits, _delims, _s.substring(rest));
    }
    
    /**
     * Push and pop brackets on the stack until {@code rangeEnd} is reached or, if
     * {@code stopWhenEmpty}, the stack is empty.
     */
    private int processStack(int rangeStart, int rangeEnd, boolean stopWhenEmpty) {
      // Match doesn't have a state query method, so we have to keep track here
      // null -> haven't tried; true -> successful match; false -> no match
      Boolean[] leftMatches = new Boolean[_lefts.length];
      Boolean[] rightMatches = new Boolean[_rights.length];
      int cursor = rangeStart;
      boolean searchLefts = _stack.isEmpty() || _brackets[_stack.getFirst()].nests();
      
      while (cursor < rangeEnd && !(stopWhenEmpty && _stack.isEmpty())) {
        // possible next brackets are any rights in the stack and, if searchLefts, all lefts
        int first = rangeEnd;
        int firstIndex = -1;
        boolean firstIsLeft = false;
        if (!_stack.isEmpty()) { // look for a right bracket
          int i = _stack.getFirst();
          Matcher m = _rights[i];
          Boolean matched = rightMatches[i];
          if (matched == null || (matched && m.start() < cursor) || (!matched && m.regionEnd() < first)) {
            matched = m.region(cursor, first).find();
            rightMatches[i] = matched;
          }
          if (matched && m.start() < first) {
            first = m.start();
            firstIndex = i;
            firstIsLeft = false;
          }
        }
        if (searchLefts) { // rights take priority; earlier lefts take priority
          for (int i = 0; i < _lefts.length; i++) {
            Matcher m = _lefts[i];
            Boolean matched = leftMatches[i];
            if (matched == null || (matched && m.start() < cursor) || (!matched && m.regionEnd() < first)) {
              // minimize search region so we don't perform needless work (but this does impact behavior
              // where different brackets overlap)
              matched = m.region(cursor, first).find();
              leftMatches[i] = matched;
            }
            if (matched && m.start() < first) {
              first = m.start();
              firstIndex = i;
              firstIsLeft = true;
            }
          }
        }
        
        if (first < rangeEnd) { // at least one bracket was found
          if (firstIsLeft) {
            _stack.addFirst(firstIndex);
            cursor = _lefts[firstIndex].end();
            searchLefts = _brackets[firstIndex].nests();
          }
          else {
            _stack.removeFirst();
            cursor = _rights[firstIndex].end();
            searchLefts = true; // either the stack is empty or the top supports nesting
          }
        }
        else { cursor = rangeEnd; }
        
      }
      return cursor;
    }
    
  }

  /** Express a byte array as a sequence of unsigned hexadecimal bytes. */
  public static String toHexString(byte[] bs) {
    return toHexString(bs, 0, bs.length);
  }
  
  /** Express a byte array as a sequence of unsigned hexadecimal bytes. */
  public static String toHexString(byte[] bs, int offset, int length) {
    StringBuilder result = new StringBuilder();
    for (int i = offset; i < offset+length; i++) {
      if (i > offset) { result.append(' '); }
      byte b = bs[i];
      // Integer.toHexString pads results in range 128-255 with "ffff...",
      // and using it on (b & 0xff) excludes leading 0s.
      result.append(Character.forDigit((b & 0xf0) >> 4, 16));
      result.append(Character.forDigit(b & 0xf, 16));
    }
    return result.toString();
  }
  
  
  public static boolean isDecimalDigit(char c) { return c >= '0' && c <= '9'; }
  
  public static boolean isOctalDigit(char c) { return c >= '0' && c <= '7'; }
  
  public static boolean isHexDigit(char c) {
    return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }
  
  /** 
   * Abstract class for string translation algorithms.  Implementations are responsible for modifying
   * {@code _result} and {@code _changed}; each character in the original string will be passed
   * to {@code processChar()}, followed by a single invocation of {@code finish()}.
   */
  private static abstract class StringTranslator implements Lambda<String, String> {
    protected final StringBuilder _result;
    protected boolean _changed;
    
    StringTranslator() { _result = new StringBuilder(); _changed = false; }
    
    public final String value(String s) {
      int length = s.length();
      for (int i = 0; i < length; i++) { processChar(s.charAt(i)); }
      finish();
      return _changed ? _result.toString() : s;
    }
    
    protected abstract void processChar(char c);
    
    protected abstract void finish();
  }
  
  /** Shared code for Unicode escaping and unescaping algorithms */
  private static abstract class UnicodeTranslator extends StringTranslator {
    private static enum State { START, BACKSLASH, U, DIG1, DIG2, DIG3 };

    private State _state;
    private StringBuilder _buffer; // includes everything that follows a "\\u"
    
    UnicodeTranslator() { _state = State.START; _buffer = new StringBuilder(); }
    
    protected abstract void handleStandardChar(char c, boolean backslashed);
    protected abstract void handlePartialEscape(String escape);
    protected abstract void handleCompleteEscape(String escape);
    
    private void reset(char c) {
      handlePartialEscape(_buffer.toString());
      _buffer.delete(0, _buffer.length());
      _state = State.START;
      if (c == '\\') { _state = State.BACKSLASH; }
      else { handleStandardChar(c, false); }
    }
    
    protected final void processChar(char c) {
      switch (_state) {
        case START:
          if (c == '\\') { _state = State.BACKSLASH; }
          else { handleStandardChar(c, false); }
          break;
        case BACKSLASH:
          if (c == 'u') { _state = State.U; }
          else { handleStandardChar(c, true); _state = State.START; } // intentionally includes double-backslash
          break;
        case U:
          if (isHexDigit(c)) { _buffer.append(c); _state = State.DIG1; }
          else if (c != 'u') { reset(c); }
          break;
        case DIG1:
          if (isHexDigit(c)) { _buffer.append(c); _state = State.DIG2; }
          else { reset(c); }
          break;
        case DIG2:
          if (isHexDigit(c)) { _buffer.append(c); _state = State.DIG3; }
          else { reset(c); }
          break;
        case DIG3:
          if (isHexDigit(c)) {
            _buffer.append(c);
            handleCompleteEscape(_buffer.toString());
            _buffer.delete(0, _buffer.length());
            _state = State.START;
          }
          else { reset(c); }
          break;
      }
    }
    
    protected final void finish() {
      switch (_state) {
        case START: break;
        case BACKSLASH: handleStandardChar('\\', false); break;
        default: handlePartialEscape(_buffer.toString()); break;
      }
    }
  }

  /**
   * Convert all non-ASCII characters in the string to Unicode escapes, as specified by JLS 3.3.
   * As suggested by JLS, an additional {@code u} is added to existing escapes in the string;
   * instances of {@code \} that precede a non-ASCII character or a malformed Unicode escape will
   * be encoded as {@code &#92;u005c}.  The original string may be safely reconstructed with 
   * {@link #unicodeUnescapeOnce}; to safely interpret <em>all</em> Unicode escapes, including 
   * those in the original string, use {@link #unicodeUnescape} (in either case, this method
   * guarantees an absence of {@code IllegalArgumentException}s).
   */
  public static String unicodeEscape(String s) {
    return new UnicodeTranslator() {
      protected void handleStandardChar(char c, boolean backslashed) {
        if (c > '\u007f') {
          if (backslashed) { _result.append("\\u005c"); } // encoded '\'
          _result.append("\\u");
          _result.append(padLeft(Integer.toHexString(c), '0', 4));
          _changed = true;
        }
        else {
          if (backslashed) { _result.append('\\'); }
          _result.append(c);
        }
      }
      protected void handlePartialEscape(String escape) {
        _result.append("\\u005cu"); // encoded '\' plus 'u'
        _result.append(escape);
        _changed = true;
      }
      protected void handleCompleteEscape(String escape) {
        _result.append("\\uu"); // add a 'u'
        _result.append(escape);
        _changed = true;
      }
    }.value(s);
  }
  
  /**
   * Convert all one-level Unicode escapes in the string to their equivalent characters, as specified by JLS 3.3.
   * Higher-level escapes (containing multiple 'u' characters) will have a single 'u' removed.
   * @throws IllegalArgumentException  If a backslash-u escape in the string is not followed by 4 hex digits
   */
  public static String unicodeUnescapeOnce(String s) {
    return new UnicodeTranslator() {
      protected void handleStandardChar(char c, boolean backslashed) {
        if (backslashed) { _result.append('\\'); }
        _result.append(c);
      }
      protected void handlePartialEscape(String escape) {
        throw new IllegalArgumentException("Expected a hexadecimal digit after '\\u" + escape + "'");
      }
      protected void handleCompleteEscape(String escape) {
        if (escape.charAt(0) == 'u') {
          _result.append('\\');
          _result.append(escape); // skip the initial 'u'
        }
        else { _result.append((char) Integer.parseInt(escape, 16)); }
        _changed = true;
      }
    }.value(s);
  }

  /**
   * Convert all Unicode escapes in the string into their equivalent Unicode characters, as specified
   * by JLS 3.3.
   * @throws IllegalArgumentException  If a backslash-u escape in the string is not followed by 4 hex digits
   */
  public static String unicodeUnescape(String s) {
    return new UnicodeTranslator() {
      protected void handleStandardChar(char c, boolean backslashed) {
        if (backslashed) { _result.append('\\'); }
        _result.append(c);
      }
      protected void handlePartialEscape(String escape) {
        throw new IllegalArgumentException("Expected a hexadecimal digit after '\\u" + escape + "'");
      }
      protected void handleCompleteEscape(String escape) {
        int firstDigit = escape.lastIndexOf('u') + 1;
        _result.append((char) Integer.parseInt(escape.substring(firstDigit), 16));
        _changed = true;
      }
    }.value(s);
  }
  
  /**
   * Convert the given string to a form compatible with the Java language specification for character and string
   * literals (see JLS 3.10.6).  The characters {@code \}, {@code "}, and {@code '} are replaced with escape
   * sequences.  All control characters between {@code &#92;u0000} and {@code &#92;u001F}, along with
   * {@code &#92;u007F}, are replaced with mnemonic escape sequences (such as {@code "\n"}), or octal escape
   * sequences if no mnemonic exists.
   */
  public static String javaEscape(String s) {
    return new StringTranslator() {
      protected void processChar(char c) {
        switch (c) {
          case '\b': _result.append("\\b"); _changed = true; break;
          case '\t': _result.append("\\t"); _changed = true; break;
          case '\n': _result.append("\\n"); _changed = true; break;
          case '\f': _result.append("\\f"); _changed = true; break;
          case '\r': _result.append("\\r"); _changed = true; break;
          case '\"': _result.append("\\\""); _changed = true; break;
          case '\'': _result.append("\\\'"); _changed = true; break;
          case '\\': _result.append("\\\\"); _changed = true; break;
          default:
            if (c < ' ' || c == '\u007f') {
              // must use 3 digits so that unescaping doesn't consume too many chars ("\12" vs. "\0012")
              _result.append('\\');
              _result.append(padLeft(Integer.toOctalString(c), '0', 3));
              _changed = true;
            }
            else { _result.append(c); }
            break;
        }
      }
      protected void finish() {}
    }.value(s);
  }
  
  private static enum JState { START, BACKSLASH, DIG1, DIG2, DIG3 };

  /**
   * Convert a string potentially containing Java character escapes (as in {@link #javaEscape}) to its
   * unescaped equivalent.  Note that Unicode escapes are <em>not</em> interpreted (strings from Java source
   * code should first be processed by {@link #unicodeUnescape}).
   * @throws IllegalArgumentException  If the character {@code \} is followed by an invalid escape character
   *                                   or the end of the string.
   */
  public static String javaUnescape(String s) {
    return new StringTranslator() {
      
      private JState _state = JState.START;
      private final StringBuilder _buffer = new StringBuilder(); // contains octal digits
      
      private void reset(char c) {
        _result.append((char) Integer.parseInt(_buffer.toString(), 8));
        _buffer.delete(0, _buffer.length());
        _state = JState.START;
        if (c == '\\') { _state = JState.BACKSLASH; _changed = true; }
        else { _result.append(c); }
      }
      
      protected void processChar(char c) {
        switch (_state) {
          case START:
            if (c == '\\') { _state = JState.BACKSLASH; _changed = true; }
            else { _result.append(c); }
            break;
          case BACKSLASH:
            switch (c) {
              case 'b': _result.append('\b'); _state = JState.START; break;
              case 't': _result.append('\t'); _state = JState.START; break;
              case 'n': _result.append('\n'); _state = JState.START; break;
              case 'f': _result.append('\f'); _state = JState.START; break;
              case 'r': _result.append('\r'); _state = JState.START; break;
              case '\"': _result.append('\"'); _state = JState.START; break;
              case '\'': _result.append('\''); _state = JState.START; break;
              case '\\': _result.append('\\'); _state = JState.START; break;
              case '0':
              case '1':
              case '2':
              case '3':
                _buffer.append(c); _state = JState.DIG1; break;
              case '4':
              case '5':
              case '6':
              case '7':
                _buffer.append(c); _state = JState.DIG2; break;
              default:
                throw new IllegalArgumentException("'" + c + "' after '\\'");
            }
            break;
          case DIG1:
            if (isOctalDigit(c)) { _buffer.append(c); _state = JState.DIG2; }
            else { reset(c); }
            break;
          case DIG2:
            if (isOctalDigit(c)) { _buffer.append(c); _state = JState.DIG3; }
            else { reset(c); }
            break;
          case DIG3:
            reset(c);
            break;
        }
      }
      
      protected void finish() {
        switch (_state) {
          case START: break;
          case BACKSLASH: throw new IllegalArgumentException("Nothing after after '\\'");
          default: _result.append((char) Integer.parseInt(_buffer.toString(), 8)); break;
        }
      }
    }.value(s);
  }

  /**
   * <p>Produce a regular expression that matches the given string.  Backslash escape sequences are
   * used for all characters that potentially clash with regular expression syntax.  For simplicity,
   * escapes are applied to all control characters ({@code &#92;u0000} to {@code &#92;u001F} and 
   * {@code &#92;u007F}) and to all non-alphanumeric, non-space ASCII characters (in the range
   * {@code &#92;u0020} to {@code &#92;u007E}), including those that have no special meaning in
   * the regular expression syntax (such as {@code @}, {@code "}, and {@code ~}).  Where a
   * mnemonic escape for control characters exists, it is used; otherwise, the hexadecimal {@code \xhh}
   * notation is used.</p>
   * 
   * <p>Note: a similar method is available in Java 5: {@link Pattern#quote}.  It has the same basic 
   * contract &mdash; produce a regex to match the given string &mdash; but produces different (equivalent)
   * results.</p>
   */
  public static String regexEscape(String s) {
    return new StringTranslator() {
      protected void processChar(char c) {
        switch (c) {
          case '\t': _result.append("\\t"); _changed = true; break;
          case '\n': _result.append("\\n"); _changed = true; break;
          case '\r': _result.append("\\r"); _changed = true; break;
          case '\f': _result.append("\\f"); _changed = true; break;
          case '\u0007': _result.append("\\a"); _changed = true; break;
          case '\u001b': _result.append("\\e"); _changed = true; break;
          default:
            if (c < ' ' || c == '\u007f') {
              _result.append("\\x");
              _result.append(padLeft(Integer.toHexString(c), '0', 2));
              _changed = true;
            }
            else if ((c > ' ' && c < '0') || (c > '9' && c < 'A') ||
                     (c > 'Z' && c < 'a') || (c > 'z' && c < '\u007F')) {
              _result.append('\\');
              _result.append(c);
              _changed = true;
            }
            else { _result.append(c); }
            break;
        }
      }
      protected void finish() {}
    }.value(s);
  }

  /**
   * Convert the given string to a form containing SGML character entities.  All characters appearing in
   * {@code entities} will be translated to their corrresponding entity names; if {@code convertToAscii} is
   * {@code true}, all other non-ASCII characters will be converted to numeric references.
   */
  public static String sgmlEscape(String s, final Map<Character, String> entities, final boolean convertToAscii) {
    return new StringTranslator() {
      protected void processChar(char c) {
        String entity = entities.get(c);
        if (entity != null) {
          _result.append('&');
          _result.append(entity);
          _result.append(';');
          _changed = true;
        }
        else if (convertToAscii && c > '\u007F') {
          _result.append("&#");
          _result.append((int) c);
          _result.append(';');
          _changed = true;
        }
        else { _result.append(c); }
      }
      protected void finish() {}
    }.value(s);
  }

  private static enum SGMLState { START, AMP, NAME, NUM, HEX_DIGITS, DEC_DIGITS };
  
  /**
   * Interpret all SGML character entities in the given string according to the provided name-character mapping.
   * @throws  IllegalArgumentException  If the string contains a malformed or unrecognized character entity
   */
  public static String sgmlUnescape(String s, final Map<String, Character> entities) {
    return new StringTranslator() {

      private SGMLState _state = SGMLState.START;
      private final StringBuilder _buffer = new StringBuilder(); // contains name or digits
      
      private void reset() { _buffer.delete(0, _buffer.length()); _state = SGMLState.START; }
      
      protected void processChar(char c) {
        switch (_state) {
          case START:
            if (c == '&') { _state = SGMLState.AMP; _changed = true; }
            else { _result.append(c); }
            break;
          case AMP:
            if (c == '#') { _state = SGMLState.NUM; }
            else if (c == ';') { throw new IllegalArgumentException("Missing entity name"); }
            else { _state = SGMLState.NAME; _buffer.append(c); }
            break;
          case NAME:
            if (c == ';') {
              Character namedChar = entities.get(_buffer.toString());
              if (namedChar == null) {
                throw new IllegalArgumentException("Unrecognized entity name: '" + _buffer.toString() + "'");
              }
              else { _result.append((char) namedChar); reset(); }
            }
            else { _buffer.append(c); }
            break;
          case NUM:
            if (c == 'x') { _state = SGMLState.HEX_DIGITS; }
            else if (isDecimalDigit(c)) { _state = SGMLState.DEC_DIGITS; _buffer.append(c); }
            else { throw new IllegalArgumentException("Expected decimal digit: '" + c + "'"); }
            break;
          case HEX_DIGITS:
            if (c == ';') {
              if (_buffer.length() == 0) { throw new IllegalArgumentException("Expected hexadecimal digit: ';'"); }
              else { _result.append((char) Integer.parseInt(_buffer.toString(), 16)); reset(); }
            }
            else if (isHexDigit(c)) { _buffer.append(c); }
            else { throw new IllegalArgumentException("Expected hexadecimal digit: '" + c + "'"); }
            break;
          case DEC_DIGITS:
            if (c == ';') { _result.append((char) Integer.parseInt(_buffer.toString())); reset(); }
            else if (isDecimalDigit(c)) { _buffer.append(c); }
            else { throw new IllegalArgumentException("Expected decimal digit: '" + c + "'"); }
            break;
        }
      }
      
      protected void finish() {
        if (_state != SGMLState.START) { throw new IllegalArgumentException("Unfinished entity"); }
      }
    }.value(s);
  }

  /**
   * Convert the given string to an escaped form compatible with XML.  The standard XML named entities
   * ({@code "}, {@code &}, {@code '}, {@code <}, and {@code >}) will be replaced with named references
   * (such as {@code &quot;}), and all non-ASCII characters will be replaced with numeric references.
   */
  public static String xmlEscape(String s) {
    return sgmlEscape(s, XML_ENTITIES.value().functionMap(), true);
  }

 /**
  * Convert the given string to an escaped form compatible with XML.  The standard XML named entities
  * ({@code "}, {@code &}, {@code '}, {@code <}, and {@code >}) will be replaced with named references
  * (such as {@code &quot;}); if {@code convertToAscii} is {@code true}, all non-ASCII characters 
  * will be replaced with numeric references.
  */
  public static String xmlEscape(String s, boolean convertToAscii) {
    return sgmlEscape(s, XML_ENTITIES.value().functionMap(), convertToAscii);
  }

 /**
   * Interpret all XML character entities in the given string.
   * @throws  IllegalArgumentException  If the string contains a malformed or unrecognized character entity
  */
  public static String xmlUnescape(String s) {
    return sgmlUnescape(s, XML_ENTITIES.value().injectionMap());
  }

  /**
   * Convert the given string to an escaped form compatible with HTML.  All named entities
   * supported by HTML 4.0 will be replaced with named references, and all other non-ASCII
   * characters will be replaced with numeric references.  The {@code '} character will also
   * be replaced with a numeric refererence.
   */
  public static String htmlEscape(String s) {
    return sgmlEscape(s, HTML_ENTITIES.value().functionMap(), true);
  }

 /**
   * Interpret all HTML character entities in the given string.
   * @throws  IllegalArgumentException  If the string contains a malformed or unrecognized character entity
  */
  public static String htmlUnescape(String s) {
    return sgmlUnescape(s, HTML_ENTITIES.value().injectionMap());
  }

  
  /** Entity names for XML; declared lazily to prevent creation when it is not used */
  private static final Thunk<OneToOneRelation<Character, String>> XML_ENTITIES = 
    LazyThunk.make(new Thunk<OneToOneRelation<Character, String>>() {
    public OneToOneRelation<Character, String> value() {
      OneToOneRelation<Character, String> result = new IndexedOneToOneRelation<Character, String>();
      // Source: Wikipedia, "List of XML and HTML character entity references"
      result.add('"', "quot");
      result.add('&', "amp");
      result.add('\'', "apos");
      result.add('<', "lt");
      result.add('>', "gt");
      return result;
    }      
  });

  
  /** Entity names for HTML; declared lazily to prevent creation when it is not used */
  private static final Thunk<OneToOneRelation<Character, String>> HTML_ENTITIES = 
    LazyThunk.make(new Thunk<OneToOneRelation<Character, String>>() {
    public OneToOneRelation<Character, String> value() {
      OneToOneRelation<Character, String> result = new IndexedOneToOneRelation<Character, String>();
      // Source: Wikipedia, "List of XML and HTML character entity references"
      result.add('\'', "#39"); // no entity defined, but it's safer to escape it
      result.add('"', "quot");
      result.add('&', "amp");
      result.add('<', "lt");
      result.add('>', "gt");
      
      result.add('\u00A0', "nbsp");
      result.add('\u00A1', "iexcl");
      result.add('\u00A2', "cent");
      result.add('\u00A3', "pound");
      result.add('\u00A4', "curren");
      result.add('\u00A5', "yen");
      result.add('\u00A6', "brvbar");
      result.add('\u00A7', "sect");
      result.add('\u00A8', "uml");
      result.add('\u00A9', "copy");
      result.add('\u00AA', "ordf");
      result.add('\u00AB', "laquo");
      result.add('\u00AC', "not");
      result.add('\u00AD', "shy");
      result.add('\u00AE', "reg");
      result.add('\u00AF', "macr");
      result.add('\u00B0', "deg");
      result.add('\u00B1', "plusmn");
      result.add('\u00B2', "sup2");
      result.add('\u00B3', "sup3");
      result.add('\u00B4', "acute");
      result.add('\u00B5', "micro");
      result.add('\u00B6', "para");
      result.add('\u00B7', "middot");
      result.add('\u00B8', "cedil");
      result.add('\u00B9', "sup1");
      result.add('\u00BA', "ordm");
      result.add('\u00BB', "raquo");
      result.add('\u00BC', "frac14");
      result.add('\u00BD', "frac12");
      result.add('\u00BE', "frac34");
      result.add('\u00BF', "iquest");
      result.add('\u00C0', "Agrave");
      result.add('\u00C1', "Aacute");
      result.add('\u00C2', "Acirc");
      result.add('\u00C3', "Atilde");
      result.add('\u00C4', "Auml");
      result.add('\u00C5', "Aring");
      result.add('\u00C6', "AElig");
      result.add('\u00C7', "Ccedil");
      result.add('\u00C8', "Egrave");
      result.add('\u00C9', "Eacute");
      result.add('\u00CA', "Ecirc");
      result.add('\u00CB', "Euml");
      result.add('\u00CC', "Igrave");
      result.add('\u00CD', "Iacute");
      result.add('\u00CE', "Icirc");
      result.add('\u00CF', "Iuml");
      result.add('\u00D0', "ETH");
      result.add('\u00D1', "Ntilde");
      result.add('\u00D2', "Ograve");
      result.add('\u00D3', "Oacute");
      result.add('\u00D4', "Ocirc");
      result.add('\u00D5', "Otilde");
      result.add('\u00D6', "Ouml");
      result.add('\u00D7', "times");
      result.add('\u00D8', "Oslash");
      result.add('\u00D9', "Ugrave");
      result.add('\u00DA', "Uacute");
      result.add('\u00DB', "Ucirc");
      result.add('\u00DC', "Uuml");
      result.add('\u00DD', "Yacute");
      result.add('\u00DE', "THORN");
      result.add('\u00DF', "szlig");
      result.add('\u00E0', "agrave");
      result.add('\u00E1', "aacute");
      result.add('\u00E2', "acirc");
      result.add('\u00E3', "atilde");
      result.add('\u00E4', "auml");
      result.add('\u00E5', "aring");
      result.add('\u00E6', "aelig");
      result.add('\u00E7', "ccedil");
      result.add('\u00E8', "egrave");
      result.add('\u00E9', "eacute");
      result.add('\u00EA', "ecirc");
      result.add('\u00EB', "euml");
      result.add('\u00EC', "igrave");
      result.add('\u00ED', "iacute");
      result.add('\u00EE', "icirc");
      result.add('\u00EF', "iuml");
      result.add('\u00F0', "eth");
      result.add('\u00F1', "ntilde");
      result.add('\u00F2', "ograve");
      result.add('\u00F3', "oacute");
      result.add('\u00F4', "ocirc");
      result.add('\u00F5', "otilde");
      result.add('\u00F6', "ouml");
      result.add('\u00F7', "divide");
      result.add('\u00F8', "oslash");
      result.add('\u00F9', "ugrave");
      result.add('\u00FA', "uacute");
      result.add('\u00FB', "ucirc");
      result.add('\u00FC', "uuml");
      result.add('\u00FD', "yacute");
      result.add('\u00FE', "thorn");
      result.add('\u00FF', "yuml");
      
      result.add('\u0152', "OElig");
      result.add('\u0153', "oelig");
      result.add('\u0160', "Scaron");
      result.add('\u0161', "scaron");
      result.add('\u0178', "Yuml");
      result.add('\u0192', "fnof");
      
      result.add('\u02C6', "circ");
      result.add('\u02DC', "tilde");
      
      result.add('\u0391', "Alpha");
      result.add('\u0392', "Beta");
      result.add('\u0393', "Gamma");
      result.add('\u0394', "Delta");
      result.add('\u0395', "Epsilon");
      result.add('\u0396', "Zeta");
      result.add('\u0397', "Eta");
      result.add('\u0398', "Theta");
      result.add('\u0399', "Iota");
      result.add('\u039A', "Kappa");
      result.add('\u039B', "Lambda");
      result.add('\u039C', "Mu");
      result.add('\u039D', "Nu");
      result.add('\u039E', "Xi");
      result.add('\u039F', "Omicron");
      result.add('\u03A0', "Pi");
      result.add('\u03A1', "Rho");
      result.add('\u03A3', "Sigma");
      result.add('\u03A4', "Tau");
      result.add('\u03A5', "Upsilon");
      result.add('\u03A6', "Phi");
      result.add('\u03A7', "Chi");
      result.add('\u03A8', "Psi");
      result.add('\u03A9', "Omega");
      
      result.add('\u03B1', "alpha");
      result.add('\u03B2', "beta");
      result.add('\u03B3', "gamma");
      result.add('\u03B4', "delta");
      result.add('\u03B5', "epsilon");
      result.add('\u03B6', "zeta");
      result.add('\u03B7', "eta");
      result.add('\u03B8', "theta");
      result.add('\u03B9', "iota");
      result.add('\u03BA', "kappa");
      result.add('\u03BB', "lambda");
      result.add('\u03BC', "mu");
      result.add('\u03BD', "nu");
      result.add('\u03BE', "xi");
      result.add('\u03BF', "omicron");
      result.add('\u03C0', "pi");
      result.add('\u03C1', "rho");
      result.add('\u03C2', "sigmaf");
      result.add('\u03C3', "sigma");
      result.add('\u03C4', "tau");
      result.add('\u03C5', "upsilon");
      result.add('\u03C6', "phi");
      result.add('\u03C7', "chi");
      result.add('\u03C8', "psi");
      result.add('\u03C9', "omega");
      
      result.add('\u03D1', "thetasym");
      result.add('\u03D2', "upsih");
      result.add('\u03D6', "piv");
      
      result.add('\u2002', "ensp");
      result.add('\u2003', "emsp");
      result.add('\u2009', "thinsp");
      result.add('\u200C', "zwnj");
      result.add('\u200D', "zwj");
      result.add('\u200E', "lrm");
      result.add('\u200F', "rlm");
      result.add('\u2013', "ndash");
      result.add('\u2014', "mdash");
      result.add('\u2018', "lsquo");
      result.add('\u2019', "rsquo");
      result.add('\u201A', "sbquo");
      result.add('\u201C', "ldquo");
      result.add('\u201D', "rdquo");
      result.add('\u201E', "bdquo");
      result.add('\u2020', "dagger");
      result.add('\u2021', "Dagger");
      result.add('\u2022', "bull");
      result.add('\u2026', "hellip");
      result.add('\u2030', "permil");
      result.add('\u2032', "prime");
      result.add('\u2033', "Prime");
      result.add('\u2039', "lsaquo");
      result.add('\u203A', "rsaquo");
      result.add('\u203E', "oline");
      result.add('\u2044', "frasl");
      result.add('\u20AC', "euro");
      
      result.add('\u2111', "image");
      result.add('\u2118', "weierp");
      result.add('\u211C', "real");
      result.add('\u2122', "trade");
      result.add('\u2135', "alefsym");
      result.add('\u2190', "larr");
      result.add('\u2191', "uarr");
      result.add('\u2192', "rarr");
      result.add('\u2193', "darr");
      result.add('\u2194', "harr");
      result.add('\u21B5', "crarr");
      result.add('\u21D0', "lArr");
      result.add('\u21D1', "uArr");
      result.add('\u21D2', "rArr");
      result.add('\u21D3', "dArr");
      result.add('\u21D4', "hArr");
      
      result.add('\u2200', "forall");
      result.add('\u2202', "part");
      result.add('\u2203', "exist");
      result.add('\u2205', "empty");
      result.add('\u2207', "nabla");
      result.add('\u2208', "isin");
      result.add('\u2209', "notin");
      result.add('\u220B', "ni");
      result.add('\u220F', "prod");
      result.add('\u2211', "sum");
      result.add('\u2212', "minus");
      result.add('\u2217', "lowast");
      result.add('\u221A', "radic");
      result.add('\u221D', "prop");
      result.add('\u221E', "infin");
      result.add('\u2220', "ang");
      result.add('\u2227', "and");
      result.add('\u2228', "or");
      result.add('\u2229', "cap");
      result.add('\u222A', "cup");
      result.add('\u222B', "int");
      result.add('\u2234', "there4");
      result.add('\u223C', "sim");
      result.add('\u2245', "cong");
      result.add('\u2248', "asymp");
      result.add('\u2260', "ne");
      result.add('\u2261', "equiv");
      result.add('\u2264', "le");
      result.add('\u2265', "ge");
      result.add('\u2282', "sub");
      result.add('\u2283', "sup");
      result.add('\u2284', "nsub");
      result.add('\u2286', "sube");
      result.add('\u2287', "supe");
      result.add('\u2295', "oplus");
      result.add('\u2297', "otimes");
      result.add('\u22A5', "perp");
      result.add('\u22C5', "sdot");
      
      result.add('\u2308', "lceil");
      result.add('\u2309', "rceil");
      result.add('\u230A', "lfloor");
      result.add('\u230B', "rfloor");
      result.add('\u2329', "lang");
      result.add('\u232A', "rang");
      
      result.add('\u25CA', "loz");
      
      result.add('\u2660', "spades");
      result.add('\u2663', "clubs");
      result.add('\u2665', "hearts");
      result.add('\u2666', "diams");
      return result;
    }
  });
  
}
