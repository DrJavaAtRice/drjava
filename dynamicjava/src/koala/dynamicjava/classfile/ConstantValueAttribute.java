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

import java.io.*;

/**
 * This class represents a constant field value
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/05/06
 */

public class ConstantValueAttribute extends AttributeInfo {
    /**
     * The index of this constant in the constant pool
     */
    private short index;

    /**
     * Creates a new constant value attribute
     * @param cp    the constant pool where constants are stored
     * @param value the value of this constant
     */
    public ConstantValueAttribute(ConstantPool cp, Integer value) {
	this(cp);
	index = constantPool.put(value);
    }
    
    /**
     * Creates a new constant value attribute
     * @param cp    the constant pool where constants are stored
     * @param value the value of this constant
     */
    public ConstantValueAttribute(ConstantPool cp, Long value) {
	this(cp);
	index = constantPool.put(value);
    }
    
    /**
     * Creates a new constant value attribute
     * @param cp    the constant pool where constants are stored
     * @param value the value of this constant
     */
    public ConstantValueAttribute(ConstantPool cp, Float value) {
	this(cp);
	index = constantPool.put(value);
    }
    
    /**
     * Creates a new constant value attribute
     * @param cp    the constant pool where constants are stored
     * @param value the value of this constant
     */
    public ConstantValueAttribute(ConstantPool cp, Double value) {
	this(cp);
	index = constantPool.put(value);
    }
    
    /**
     * Creates a new constant value attribute
     * @param cp    the constant pool where constants are stored
     * @param value the value of this constant
     */
    public ConstantValueAttribute(ConstantPool cp, String value) {
	this(cp);
	index = constantPool.put(new ConstantString(value));
    }
    
    /**
     * Initializes a new constant value attribute
     * @param cp    the constant pool where constants are stored
     */
    private ConstantValueAttribute(ConstantPool cp) {
	super(cp, "ConstantValue");
	length = 2;
    }
    
    /**
     * Writes the constant info to the given output stream.
     */
    public void write(DataOutputStream out) throws IOException {
	out.writeShort(nameIndex);
	out.writeInt(length);
	out.writeShort(index);
    }
}
