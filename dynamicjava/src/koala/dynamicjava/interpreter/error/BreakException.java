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

/**
 * Thrown to indicate that a break statement has been reached
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/23
 */
public class BreakException extends RuntimeException {
    /**
     * The label
     * @serial
     */
    private String label;

    /**
     * Constructs an <code>BreakException</code> with the 
     * specified detail message. 
     * @param m the detail message.
     */
    public BreakException(String m) {
 super(m);
    }

    /**
     * Constructs an <code>BreakException</code> with the 
     * specified detail message and label.
     * @param m the detail message.
     * @param l the label
     */
    public BreakException(String m, String l) {
        super(m);
 label = l;
    }

    /**
     * Tests whether the statement was labeled
     */
    public boolean isLabeled() {
 return label != null;
    }

    /**
     * Returns the label of the statement that thrown the exception
     */
    public String getLabel() {
 return label;
    }
}
