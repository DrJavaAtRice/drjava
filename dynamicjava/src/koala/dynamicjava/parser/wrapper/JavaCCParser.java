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

package koala.dynamicjava.parser.wrapper;

import java.io.*;
import java.util.*;

import koala.dynamicjava.parser.*;
import koala.dynamicjava.tree.*;

/**
 * The instances of this class represents a parser
 * generated with JavaCC.
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/06/12
 */

public class JavaCCParser implements SourceCodeParser {
  /**
   * The parser
   */
  private Parser parser;
  
  /**
   * Creates a new JavaCCParser
   * @param is    the input stream
   * @param fname the file name
   */
  public JavaCCParser(InputStream is, String fname) {
    parser = new Parser(is);
    parser.setFilename(fname);
  }
  
  /**
   * Creates a new JavaCCParser
   * @param r     the reader
   * @param fname the file name
   */
  public JavaCCParser(Reader r, String fname) {
    parser = new Parser(r);
    parser.setFilename(fname);
  }
  
  /**
   * Creates a new parser and returns it
   * @param is    the input stream
   * @param fname the file name
   */
  public SourceCodeParser createParser(InputStream is, String fname) {
    return new JavaCCParser(is, fname);
  }
  
  /**
   * Creates a new parser and returns it
   * @param r     the reader
   * @param fname the file name
   */
  public SourceCodeParser createParser(Reader r, String fname) {
    return new JavaCCParser(r, fname);
  }
  
  /**
   * Parses top level statements
   * @return a list of nodes
   * @see koala.dynamicjava.tree.Node
   */
  public List<Node> parseStream() {
    try {
      return parser.parseStream();
    } catch (ParseException e) {
      throw new ParseError(e.getMessage());
    }
  }
  
  /**
   * Parses a library file
   * @see koala.dynamicjava.tree.Node
   */
  public List<Node> parseCompilationUnit() {
    try {
      return parser.parseCompilationUnit();
    } catch (ParseException e) {
      throw new ParseError(e.getMessage());
    }
  }
}
