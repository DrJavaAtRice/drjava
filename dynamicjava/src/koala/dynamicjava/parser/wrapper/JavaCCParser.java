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

import koala.dynamicjava.parser.impl.*;
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
  private final Parser _parser;
  private final File _f;
  
  public JavaCCParser(InputStream is, File f) {
    _parser = new Parser(is);
    _parser.setFile(f);
    _f = f;
  }
  
  public JavaCCParser(InputStream is) {
    _parser = new Parser(is);
    _f = null;
  }
  
  public JavaCCParser(Reader r, File f) {
    _parser = new Parser(r);
    _parser.setFile(f);
    _f = f;
  }
  
  public JavaCCParser(Reader r) {
    _parser = new Parser(r);
    _f = null;
  }
  
  /**
   * Parses top level statements
   * @return a list of nodes
   * @see koala.dynamicjava.tree.Node
   */
  public List<Node> parseStream() {
    try {
      return _parser.parseStream();
    }
    catch (ParseException e) {
      throw new ParseError(e, _f);
    }
    catch (TokenMgrError e) {
      throw new ParseError(e, SourceInfo.point(_f, 0, 0));
    }
    catch (Error e) {
      // JavaCharStream does not use a useful exception type for escape character errors
      String msg = e.getMessage();
      if (msg != null && msg.startsWith("Invalid escape character")) {
        throw new ParseError(e, SourceInfo.point(_f, 0, 0));
      }
      else { throw e; }
    }
  }
  
  /**
   * Parses a library file
   * @see koala.dynamicjava.tree.Node
   */
  public CompilationUnit parseCompilationUnit() {
    try {
      return _parser.parseCompilationUnit();
    }
    catch (ParseException e) {
      throw new ParseError(e, _f);
    }
    catch (TokenMgrError e) {
      throw new ParseError(e, SourceInfo.point(_f, 0, 0));
    }
    catch (Error e) {
      // JavaCharStream does not use a useful exception type for escape character errors
      String msg = e.getMessage();
      if (msg != null && msg.startsWith("Invalid escape character")) {
        throw new ParseError(e, SourceInfo.point(_f, 0, 0));
      }
      else { throw e; }
    }
  }
}
