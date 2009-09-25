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

package koala.dynamicjava.util;

import java.util.*;

/**
 * The instances of this class read localized messages in resource files.
 * The messages in the file are templates. Context specific strings are
 * inserted where '%n' patterns can be found. A '%' character is represented
 * with '%%'.
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/08/04
 */

public class LocalizedMessageReader {
  /**
   * The escape character
   */
  private final static char ESCAPE_CHAR = '%';
  
  /**
   * The resource bundle
   */
  private ResourceBundle bundle;
  
  /**
   * Creates a new message reader
   * @param name the name of the resource
   */
  public LocalizedMessageReader(String name) {
    bundle = ResourceBundle.getBundle(name);
  }
  
  /**
   * Gets a message
   * @param key the message key
   * @param strings the strings to insert in the message
   */
  public String getMessage(String key, String[] strings) {
    String rawMessage = bundle.getString(key);
    String result = "";
    
    if (rawMessage != null) {
      for (int i = 0; i < rawMessage.length(); i++) {
        char c = rawMessage.charAt(i);
        if (c == ESCAPE_CHAR) {
          c = rawMessage.charAt(++i);
          if (c == ESCAPE_CHAR) {
            result += c;
          } else {
            String numb = "";
            do {
              if (!Character.isDigit(c = rawMessage.charAt(i))) {
                i--;
                break;
              }
              numb += c;
            } while (++i < rawMessage.length());
            int n = Integer.parseInt(numb);
            result += strings[n];
          }
        } else {
          result += c;
        }
      }
    }
    return result;
  }
}
