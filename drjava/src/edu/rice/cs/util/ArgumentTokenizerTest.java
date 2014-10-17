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

import edu.rice.cs.drjava.DrJavaTestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Tests that an ArgumentTokenizer can correctly divide up a string
 * into command line-style arguments.  Tries to follow the precedent
 * set by a Unix bash shell in most cases.
 * @version $Id: ArgumentTokenizerTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class ArgumentTokenizerTest extends DrJavaTestCase {

  /** Creates a new ArgumentTokenizer to be used in every test.
   */
  public ArgumentTokenizerTest(String name) {
    super(name);
  }

  /** Asserts that the given string is tokenized to become the
   * given array of string arguments.
   * @param typed A string containing all arguments (as typed by a user)
   * @param expected What the tokenizer should return
   */
  protected void _assertTokenized(String typed, String[] expected) {
    _assertTokenized(typed, expected, false);
  }

  /** Asserts that the given string is tokenized to become the
   * given array of string arguments.
   * @param typed A string containing all arguments (as typed by a user)
   * @param expected What the tokenizer should return
   * @param stringify Whether to format the resulting arguments to print
   * out as Strings.
   */
  protected void _assertTokenized(String typed, String[] expected,
                                  boolean stringify) {
    List<String> actual = ArgumentTokenizer.tokenize(typed, stringify);
    List<String> expectedList = Arrays.asList(expected);
    assertEquals("tokenized argument list should match expected",
                 expectedList, actual);
  }

  /** Tests that the argument tokenizer can split up a simple list of arguments.
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

  /** Tests that escaped characters just return the character itself.
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

  /** Tests that within a quote, everything is correctly escaped.
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

  /** Tests that single quotes can be used as argument delimiters.
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

  /** Tests that arguments can be "stringified" properly.
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
