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


import koala.dynamicjava.parser.ParseException;
  
/**
 * This error is thrown when an unexpected error append while
 * parsing a statement
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/05/03
 */

public class ParseError extends Error {
    /**
     * Constructs a ParseError with no detailed message
     * The ParseException that generated the ParseError or
     * null if generated for another reason
     */
    private ParseException pe = null;
    
    /**
     * The file name
     * @serial
     */
    private String filename;

    /**
     * The line in the source code where the error occured
     * @serial
     */
    private int line;

    /**
     * The column in the source code where the error occured
     * @serial
     */
    private int column;

    /**
     * Constructs a ParseError with no detail message. 
     */
    public ParseError() {
        this("");
    }

    /**
     * Constructs an <code>ExecutionError</code> with the specified 
     * detail message. 
     * @param s the detail message.
     */
    public ParseError(String s) {
        this(s, "", -1, -1);
    }
    
    /**
     * Constructs a ParseError with the specified 
     * detail message. 
     * @param e the ParseException.
     */
    public ParseError(ParseException e) {
        this(e.getShortMessage(), "", -1, -1);
        pe = e;
    }
    
    /**
     * Constructs an <code>ExecutionError</code> with the specified 
     * detail message, filename, line and column. 
     * @param s  the detail message.
     * @param fn the file name.
     * @param l  the line in the source code.
     * @param c  the column in the source code.
     */
    public ParseError(String s, String fn, int l, int c) {
        super(s);
        filename = fn;
        line     = l;
        column   = c;
    }
    
    /**
     * Returns the name of the source file
     */
    public String getFilename() {
      return filename;
    }

    /**
     * Returns the line in the source code where the error occured
     */
    public int getLine() {
      return line;
    }
    
    /**
     * Returns the column in the source code where the error occured
     */
    public int getColumn() {
      return column;
    }
    
    /**
     * Returns the ParseException
     */
    public ParseException getParseException() {
      return pe;
    }
}
