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

package koala.dynamicjava.interpreter;

import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.parser.wrapper.*;
import koala.dynamicjava.util.*;

/**
 * This exception is thrown when an error append while
 * interpreting a statement
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/11/14
 */

public class InterpreterException extends ThrownException {
    /**
     * The source code information
     */
    protected SourceInformation sourceInformation;

    /**
     * The detailed message
     */
    protected String message;

    /**
     * Constructs an <code>InterpreterException</code> from a ParseError
     */
    public InterpreterException(ParseError e) {
        super(e);
	if (e.getLine() != -1) {
	    sourceInformation = new SourceInformation(e.getFilename(),
						      e.getLine(),
						      e.getColumn());
	    message = "L"+e.getLine()+", C"+e.getColumn()+" ("+e.getFilename()+"):\n"+
		e.getMessage();
	} else {
	    message = e.getMessage();
	}
    }

    /**
     * Constructs an <code>InterpreterException</code> from a ExecutionError
     */
    public InterpreterException(ExecutionError e) {
        super(e);
	Node n = e.getNode();
	if (n != null && n.getFilename() != null) {
	    sourceInformation = new SourceInformation(n.getFilename(),
						      n.getBeginLine(),
						      n.getBeginColumn());
	    message = "L"+n.getBeginLine()+", C"+n.getBeginColumn()+
		" ("+n.getFilename()+"):\n";
	} else {
	    message = "";
	}
	if (e instanceof CatchedExceptionError) {
	    message += ((CatchedExceptionError)e).getException();
	} else if (e instanceof ThrownException) {
	    message += ((ThrownException)e).getException();
	} else {
	    message += e.getMessage();
	}
    }

    public Throwable getError() {
        return thrown;
    }
 
    /**
     * Returns the source code information if available, or null
     */
    public SourceInformation getSourceInformation() {
	return sourceInformation;
    }

    /**
     * To represent the source code informations
     */
    public static class SourceInformation {
	// The fields
	private String filename;
	private int    line;
	private int    column;

	/**
	 * Creates a source information
	 */
	public SourceInformation(String filename, int line, int column) {
	    this.filename = filename;
	    this.line     = line;
	    this.column   = column;
	}

	/**
	 * Returns the filename
	 */
	public String getFilename() {
	    return filename;
	}

	/**
	 * Returns the line where the error occurs
	 */
	public int getLine() {
	    return line;
	}

	/**
	 * Returns the column where the error occurs
	 */
	public int getColumn() {
	    return column;
	}
    }

    /**
     * Returns the detailed message
     */
    public String getMessage() {
	return message;
    }
}
