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
import java.util.*;

/**
 * This class represents an innerclasses entry in an innerclasses attribute
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/06/04
 * @see InnerClassesAttribute
 */

public class InnerClassesEntry{
    /**
     * The inner class info index
     */
    private short innerClassInfoIndex;

    /**
     * The outer class info index
     */
    private short outerClassInfoIndex;

    /**
     * The inner name index
     */
    private short innerNameIndex;

    /**
     * The inner class access flags
     */
    private short innerClassAccessFlags;

    /**
     * The constant pool used to store the constants
     */
    private ConstantPool constantPool;

    /**
     * Creates a new entry
     */
    public InnerClassesEntry(ConstantPool cp) {
	constantPool = cp;
    }

    /**
     * Writes the code represented by this object to the given output stream.
     */
    public void write(DataOutputStream out) throws IOException {
	out.writeShort(innerClassInfoIndex);
	out.writeShort(outerClassInfoIndex);
	out.writeShort(innerNameIndex);
	out.writeShort(innerClassAccessFlags);
    }

    /**
     * Sets the inner class info
     * @param cname the inner class name
     */
    public void setInnerClassInfo(String cname) {
	cname = JVMUtilities.getName(cname);
	innerClassInfoIndex = constantPool.put(new ClassIdentifier(cname));
    }

    /**
     * Sets the outer class info
     * @param cname the outer class name
     */
    public void setOuterClassInfo(String cname) {
	cname = JVMUtilities.getName(cname);
	outerClassInfoIndex = constantPool.put(new ClassIdentifier(cname));
    }

    /**
     * Sets the inner class name
     * @param the name of the inner class
     */
    public void setInnerName(String name) {
	innerNameIndex = constantPool.putUTF8(name);
    }

    /**
     *
     * @param af the access flags
     */
    public void setInnerClassAccessFlags(short af) {
	innerClassAccessFlags = af;
    }
}
