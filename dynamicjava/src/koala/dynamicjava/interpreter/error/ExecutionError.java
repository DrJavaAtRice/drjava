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

package koala.dynamicjava.interpreter.error;

import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.util.*;

/**
 * This error is thrown when an unexpected error append while
 * interpreting a statement
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/04/30
 */

public class ExecutionError extends Error {
 
    protected Throwable thrown;
    
    /**
     * The resource bundle name
     */
    private final static String BUNDLE
	= "koala.dynamicjava.interpreter.resources.messages";

    public final static String SHOW_CAUSE_PROPERTY
        = "koala.dynamicjava.interpreter.showCause";

    public final static String SHOW_TRACE_PROPERTY
        = "koala.dynamicjava.interpreter.showTrace";

    /**
     * The message reader
     */
    private final static LocalizedMessageReader reader
	= new LocalizedMessageReader(BUNDLE);

    /**
     * The syntax tree node where the error occurs
     * @serial
     */
    private Node node;

    /**
     * The raw message
     */
    private String rawMessage;

    /**
     * Constructs an <code>ExecutionError</code> with no detail message. 
     */
    public ExecutionError() {
        this("");
    }

    /**
     * Constructs an <code>ExecutionError</code> with the specified 
     * detail message. 
     * @param s the detail message (a key in a resource file).
     */
    public ExecutionError(String s) {
        this(s, null);
    }
    
    /**
     * Constructs an <code>ExecutionError</code> with the specified 
     * detail message, filename, line and column. 
     * @param s  the detail message (a key in a resource file).
     * @param n  the syntax tree node where the error occurs
     */
    public ExecutionError(String s, Node n) {
	rawMessage = s;
	node       = n;
    }
    
    public ExecutionError(Throwable thrown) {
        this.thrown = thrown;
    }

    /**
     * Returns the syntax tree node where the error occurs
     */
    public Node getNode() {
	return node;
    }

    /**
     * Overridden to delegate to printStackTrace(PrintStream) to print nested
     * exception information.
     * @see #printStackTrace(PrintStream)
     */
    public void printStackTrace() {
        this.printStackTrace(System.err);
    }
 
    /**
     * Overridden to delegate to printStackTrace(PrintWriter) to print nested
     * exception information.
     * @see #printStackTrace(PrintWriter)
     */
    public void printStackTrace(java.io.PrintStream s) {
        this.printStackTrace(new java.io.PrintWriter(s, true));
    }

    /**
     * Handles all calls to printStackTrace(), printing
     * the stack trace of the current exception, and also that of its cause.
     */
    public void printStackTrace(java.io.PrintWriter w) {
        String trace = System.getProperty(SHOW_TRACE_PROPERTY);
        if (trace != null && !new Boolean(trace).booleanValue()) {
            w.println(this);
        } else {
            super.printStackTrace(w);
            String cause = System.getProperty(SHOW_CAUSE_PROPERTY);
            if (cause == null || new Boolean(cause).booleanValue()) {
                if (thrown != null) {
                    w.println("Caused by: ");
                    thrown.printStackTrace(w);
                }
            }
        }
    }
    
    /**
     * Returns the errort message string of this exception
     */
    public String getMessage() {
	return reader.getMessage(rawMessage,
				 node != null &&
				 node.hasProperty(NodeProperties.ERROR_STRINGS)
				 ? (String[])node.getProperty(NodeProperties.ERROR_STRINGS)
				 : null);
    }
}
