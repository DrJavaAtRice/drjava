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
 * This class represents a component of the bytecode 'ClassFile' format
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/05/28
 */

public abstract class BytecodeComponent {
    /**
     * The constant pool
     */
    protected ConstantPool constantPool;

    /**
     * The index of the name of this component
     */
    protected short nameIndex;

    /**
     * Initializes a new bytecode component
     * @param cp the constant pool
     * @param ni the name index in the constant pool
     */
    protected BytecodeComponent(ConstantPool cp, short ni) {
	constantPool = cp;
	nameIndex    = ni;
    }

    /**
     * Returns the constant pool
     */
    public ConstantPool getConstantPool() {
	return constantPool;
    }

    /**
     * Writes the class file to the given output stream
     */
    public void write(OutputStream out) throws IOException {
	write(new DataOutputStream(out));
    }

    /**
     * Writes the class file to the given output stream
     */
    public abstract void write(DataOutputStream out) throws IOException;

}
