/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 *
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
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
