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

import java.util.ArrayList;
import java.io.*;

/**
 * Utility class which can tokenize a String into a list of String arguments,
 * with behavior similar to parsing command line arguments to a program.
 * Quoted Strings are treated as single arguments, and escaped characters
 * are translated so that the tokenized arguments have the same meaning.
 * @version $Id$
 */
public class ArgumentTokenizer {
  private static final int NO_TOKEN_STATE = 0;
  private static final int NORMAL_TOKEN_STATE = 1;
  private static final int SINGLE_QUOTE_STATE = 2;
  private static final int DOUBLE_QUOTE_STATE = 3;
    
  /**
   * Creates an ArgumentTokenizer that can be used to tokenize many strings.
   */
  public ArgumentTokenizer() {
  }

  /**
   * Convenience method redirects to tokenize(arguments, false).
   * Tokenizes the given String into individual argument Strings.
   * @param arguments A String containing one or more command-line style 
   * arguments to be tokenized.
   * @return A list of parsed and properly escaped arguments.
   */
  public ArrayList<String> tokenize(String arguments) {
    return tokenize(arguments, false);
  }

  /**
   * Tokenizes the given String into individual argument Strings.
   * @param arguments A String containing one or more command-line style 
   * arguments to be tokenized.
   * @param stringify whether or not to include escape special characters
   * @return A list of parsed and properly escaped arguments.
   */
  public ArrayList<String> tokenize(String arguments, boolean stringify) {

    ArrayList<String> argList = new ArrayList<String>();
    StringBuffer currArg = new StringBuffer();
    boolean escaped = false;
    int state = NO_TOKEN_STATE;  // start in the NO_TOKEN_STATE
    int len = arguments.length();
    
    // Loop over each character in the string
    for (int i = 0; i < len; i++) {
      char c = arguments.charAt(i);
      if (escaped) {
        // Escaped state: just append the next character to the current arg.
        escaped = false;
        currArg.append(c);
      }
      else {
        switch(state) {
          case SINGLE_QUOTE_STATE:
            if (c == '\'') {
              // Seen the close quote; continue this arg until whitespace is seen
              state = NORMAL_TOKEN_STATE;
            }
            else {
              currArg.append(c);
            }
            break;
          case DOUBLE_QUOTE_STATE:
            if (c == '"') {
              // Seen the close quote; continue this arg until whitespace is seen
              state = NORMAL_TOKEN_STATE;
            }
            else if (c == '\\') {
              // Look ahead, and only escape quotes or backslashes
              i++;
              char next = arguments.charAt(i);
              if (next == '"' || next == '\\') {
                currArg.append(next);
              }
              else {
                currArg.append(c);
                currArg.append(next);
              }
            }
            else {
              currArg.append(c);
            }
            break;
//          case NORMAL_TOKEN_STATE:
//            if (Character.isWhitespace(c)) {
//              // Whitespace ends the token; start a new one
//              argList.add(currArg.toString());
//              currArg = new StringBuffer();
//              state = NO_TOKEN_STATE;
//            }
//            else if (c == '\\') {
//              // Backslash in a normal token: escape the next character
//              escaped = true;
//            }
//            else if (c == '\'') {
//              state = SINGLE_QUOTE_STATE;
//            }
//            else if (c == '"') {
//              state = DOUBLE_QUOTE_STATE;
//            }
//            else {
//              currArg.append(c);
//            }
//            break;
          case NO_TOKEN_STATE:
          case NORMAL_TOKEN_STATE:
            switch(c) {
              case '\\':
                escaped = true;
                state = NORMAL_TOKEN_STATE;
                break;
              case '\'':
                state = SINGLE_QUOTE_STATE;
                break;
              case '"':
                state = DOUBLE_QUOTE_STATE;
                break;
              default:
                if (!Character.isWhitespace(c)) {
                  currArg.append(c);
                  state = NORMAL_TOKEN_STATE;
                }
                else if (state == NORMAL_TOKEN_STATE) {
                  // Whitespace ends the token; start a new one
                  argList.add(currArg.toString());
                  currArg = new StringBuffer();
                  state = NO_TOKEN_STATE;
                }
              }
            break;
          default:
            throw new IllegalStateException("ArgumentTokenizer state " + state + " is invalid!");
        }
      }
    }
    
    // If we're still escaped, put in the backslash
    if (escaped) {
      currArg.append('\\');
      argList.add(currArg.toString());
    }
    // Close the last argument if we haven't yet
    else if (state != NO_TOKEN_STATE) {
      argList.add(currArg.toString());
    }
    // Format each argument if we've been told to stringify them
    if (stringify) {
      for (int i = 0; i < argList.size(); i++) {
        argList.set(i, "\"" + _escapeQuotesAndBackslashes(argList.get(i)) + "\"");
      }
    }
    return argList;
  }
  
  /**
   * Inserts backslashes before any occurrences of a backslash or
   * quote in the given string.  Also converts any special characters
   * appropriately.
   */
  protected static String _escapeQuotesAndBackslashes(String s) {
    StringBuffer buf = new StringBuffer(s);
    int lastIndex = 0;
    
    // Walk backwards, looking for quotes or backslashes.
    //  If we see any, insert an extra backslash into the buffer at
    //  the same index.  (By walking backwards, the index into the buffer
    //  will remain correct as we change the buffer.)
    for (int i = s.length()-1; i >= 0; i--) {
      char c = s.charAt(i);
      if ((c == '\\') || (c == '"')) {
        buf.insert(i, '\\');
      }
      // Replace any special characters with escaped versions
      else if (c == '\n') {
        buf.deleteCharAt(i);
        buf.insert(i, "\\n");
      }
      else if (c == '\t') {
        buf.deleteCharAt(i);
        buf.insert(i, "\\t");
      }
      else if (c == '\r') {
        buf.deleteCharAt(i);
        buf.insert(i, "\\r");
      }
      else if (c == '\b') {
        buf.deleteCharAt(i);
        buf.insert(i, "\\b");
      }
      else if (c == '\f') {
        buf.deleteCharAt(i);
        buf.insert(i, "\\f");
      }
    }
    return buf.toString();
  }
}