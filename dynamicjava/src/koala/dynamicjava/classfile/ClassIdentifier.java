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
 * This class represents a class in the JVM internal format
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/05/06
 */

public class ClassIdentifier {
    /**
     * The value of this class identifier
     */
    private String value;

    /**
     * Creates a new class identifier
     */
    public ClassIdentifier(String v) {
	value = v;
    }

    /**
     * Returns the value of this constant
     */
    public String getValue() {
	return value;
    }

    /**
     * Indicates whether some other object is equal to this one
     */
    public boolean equals(Object other) {
	if (other == null || !(other instanceof ClassIdentifier)) {
	    return false;
	}
	return value.equals(((ClassIdentifier)other).value);
    }

    /**
     * Returns a hash code value for this object
     */
    public int hashCode() {
	return "ClassIdentifier".hashCode()+value.hashCode();
    }
}
