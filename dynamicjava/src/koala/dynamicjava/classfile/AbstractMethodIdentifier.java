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

package koala.dynamicjava.classfile;

/**
 * The classes derived from this one are used to represents a method
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/05/06
 */

public abstract class AbstractMethodIdentifier extends MemberIdentifier {
    /**
     * The parameters types
     */
    private String[] parameters;

    /**
     * Creates a new method identifier
     * @param dc the declaring class of this member
     * @param n  the name of this member
     * @param t  the type of this member in JVM format
     * @param p  the parameters types
     */
    public AbstractMethodIdentifier(String dc, String n, String t, String[] p) {
	super(dc, n, t);
	parameters = p;
    }

    /**
     * Returns the parameters types
     */
    public String[] getParameters() {
	return parameters;
    }

    /**
     * Indicates whether some other object is equal to this one
     */
    public boolean equals(Object other) {
	if (super.equals(other)) {
	    String[] p = ((AbstractMethodIdentifier)other).parameters;
	    if (parameters.length != p.length) {
		return false;
	    }
	    for (int i = 0; i < p.length; i++) {
		if (!parameters[i].equals(p[i])) {
		    return false;
		}
	    }
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Returns a hash code value for this object
     */
    public int hashCode() {
	int result = super.hashCode();
	for (int i = 0; i < parameters.length; i++) {
	    result += parameters[i].hashCode();
	}
	return result;
    }
}
