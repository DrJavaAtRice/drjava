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

package edu.rice.cs.util;

import junit.framework.TestCase;
import java.util.*;
import java.io.*;

/**
 * Tests that an ArgumentTokenizer can correctly divide up a string
 * into command line-style arguments.  Tries to follow the precedent
 * set by a Unix bash shell in most cases.
 * @version $Id$
 */
public class ArgumentTokenizerTest extends TestCase {
  
  /**
   * Creates a new ArgumentTokenizer to be used in every test.
   */
  public ArgumentTokenizerTest(String name) {
    super(name);
  }
  
  /**
   * Asserts that the given string is tokenized to become the
   * given array of string arguments.
   * @param typed A string containing all arguments (as typed by a user)
   * @param expected What the tokenizer should return
   */
  protected void _assertTokenized(String typed, String[] expected) {
    _assertTokenized(typed, expected, false);
  }
  
  /**
   * Asserts that the given string is tokenized to become the
   * given array of string arguments.
   * @param typed A string containing all arguments (as typed by a user)
   * @param expected What the tokenizer should return
   * @param stringify Whether to format the resulting arguments to print
   * out as Strings.
   */
  protected void _assertTokenized(String typed, String[] expected,
                                  boolean stringify) {
    List<String> actual = ArgumentTokenizer.tokenize(typed, stringify);
    List expectedList = Arrays.asList(expected);
    assertEquals("tokenized argument list should match expected",
                 expectedList, actual);
  }
  
  /**
   * Tests that the argument tokenizer can split up a simple list of arguments.
   */
  public void testTokenizeArguments() {
    // a b c
    // [a, b, c]
    _assertTokenized("a b c",
                     new String[]{"a","b","c"});
    // "a b c"
    // [a b c]
    _assertTokenized("\"a b c\"",
                     new String[]{"a b c"});
    
    // "a b"c d
    // [a bc, d]
    // This behavior seems unintuitive, but it's the way both DOS and Unix
    //  handle command-line arguments.
    _assertTokenized("\"a b\"c d",
                     new String[]{"a bc","d"});

    // 'a b'c d
    // [a bc, d]
    // This behavior seems unintuitive, but it's the way both DOS and Unix
    //  handle command-line arguments.
    _assertTokenized("'a b'c d",
                     new String[]{"a bc","d"});

    // a b"c d"
    // [a, bc d]
    // This behavior seems unintuitive, but it's the way both DOS and Unix
    //  handle command-line arguments.
    _assertTokenized("a b\"c d\"",
                     new String[]{"a","bc d"});

    // a b'c d'
    // [a, bc d]
    // This behavior seems unintuitive, but it's the way both DOS and Unix
    //  handle command-line arguments.
    _assertTokenized("a b'c d'",
                     new String[]{"a","bc d"});

    // a b'c d'"e f" g "hi "
    // [a, bc de f, g, hi ]
    _assertTokenized("a b'c d'\"e f\" g \"hi \"",
                     new String[]{"a","bc de f","g","hi "});

    // c:\\file.txt
    // [c:\file.txt]
    _assertTokenized("c:\\\\file.txt",
                     new String[]{"c:\\file.txt"});

    // /home/user/file
    // [/home/user/file]
    _assertTokenized("/home/user/file",
                     new String[]{"/home/user/file"});
    
    // "asdf
    // [asdf]
    _assertTokenized("\"asdf",
                     new String[]{"asdf"});
  }

  /**
   * Tests that escaped characters just return the character itself.
   * Escaped whitespace is considered a character, not a delimiter.
   * (This is how Unix behaves.)
   *
   * not currently enforcing any behavior for a simple implementation
   * using a StreamTokenizer
   */
  public void testTokenizeEscapedArgs() {
    // \j
    // [j]
    _assertTokenized("\\j",
                     new String[]{"j"});
    // \"
    // ["]
    _assertTokenized("\\\"",
                     new String[]{"\""});
    // \\
    // [\]
    _assertTokenized("\\\\",
                     new String[]{"\\"});
    // a\ b
    // [a b]
    _assertTokenized("a\\ b",
                     new String[]{"a b"});
  }
  
  /**
   * Tests that within a quote, everything is correctly escaped.
   * (Special characters are passed to the program correctly.)
   */
  public void testTokenizeQuotedEscapedArgs() {
    // "a \" b"
    // [a " b]
    _assertTokenized("\"a \\\" b\"",
                     new String[]{"a \" b"});
    // "\'"
    // [\']
    _assertTokenized("\"'\"",
                     new String[]{"'"});
    // "\\"
    // [\]
    _assertTokenized("\\\\",
                     new String[]{"\\"});
    // "\" \d"
    // [" \d]
    _assertTokenized("\"\\\" \\d\"",
                     new String[]{"\" \\d"});
    // "\n"
    // [\n]
    _assertTokenized("\"\\n\"",
                     new String[]{"\\n"});
    // "\t"
    // [\t]
    _assertTokenized("\"\\t\"",
                     new String[]{"\\t"});
    // "\r"
    // [\r]
    _assertTokenized("\"\\r\"",
                     new String[]{"\\r"});
    // "\f"
    // [\f]
    _assertTokenized("\"\\f\"",
                     new String[]{"\\f"});
    // "\b"
    // [\b]
    _assertTokenized("\"\\b\"",
                     new String[]{"\\b"});
  }

  /**
   * Tests that single quotes can be used as argument delimiters.
   * This is consistent with Unix, not with DOS.
   */
  public void testTokenizeSingleQuotedArgs() {
    // 'asdf'
    // [asdf]
    _assertTokenized("'asdf'",
                     new String[]{"asdf"});
    // 'a b c'
    // [a b c]
    _assertTokenized("'a b c'",
                     new String[]{"a b c"});
    // '\'
    // [\]
    _assertTokenized("'\\'",
                     new String[]{"\\"});
  }
  
  /**
   * Tests that arguments can be "stringified" properly.
   * (ie. formatted to be printed as a String)
   */
  public void testTokenizeAndStringify() {
    // a b c
    // ["a", "b", "c"]
    _assertTokenized("a b c",
                     new String[]{"\"a\"", "\"b\"", "\"c\""},
                     true);
    // \\
    // ["\\"]
    _assertTokenized("\\",
                     new String[]{"\"\\\\\""},
                     true);
    // \"
    // ["\""]
    _assertTokenized("\\\"",
                     new String[]{"\"\\\"\""},
                     true);
    // "\n"
    // ["\\n"]
    _assertTokenized("\"\\n\"",
                     new String[]{"\"\\\\n\""},
                     true);
  }
}
