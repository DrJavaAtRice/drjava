/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.tree;

import java.util.*;

/**
 * This class contains a collection of utility methods for trees.
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/06/27
 */

public class TreeUtilities {
  /**
   * Transforms a list of token into a dot-separated name
   * @param l a list of token. l can be null.
   * @return "" if l is null.
   */
  public static String listToName(List<? extends IdentifierToken> l) {
    String   result = "";
    if (l != null) {
      Iterator<? extends IdentifierToken> it = l.iterator();
      if (it.hasNext()) {
        result += it.next().image();
      }
      while (it.hasNext()) {
        result += "." + it.next().image();
      }
    }
    return result;
  }
  
  /**
   * This class contains only static methods, so it is not useful
   * to create instances of it or to extend it.
   */
  private TreeUtilities() {
  }
}
