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
 * This class represents a method exceptions attribute
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/05/21
 */

public class ExceptionsAttribute extends AttributeInfo {
    /**
     * The exception index table
     */
    private List exceptionIndexTable;

    /**
     * Creates a new empty (not valid) code attribute
     * @param cp the constant pool
     */
    public ExceptionsAttribute(ConstantPool cp) {
	super(cp, "Exceptions");
	length              = 2;
	exceptionIndexTable = new LinkedList();
    }

    /**
     * Writes the exception info to the given output stream.
     */
    public void write(DataOutputStream out) throws IOException {
	out.writeShort(nameIndex);
	out.writeInt(length);
	out.writeShort(exceptionIndexTable.size());
	Iterator it = exceptionIndexTable.iterator();
	while (it.hasNext()) {
	    out.writeShort(((Short)it.next()).shortValue());
	}
    }

    /**
     * Adds an exception to the attribute
     * @param name the name of the exception
     */
    public void addException(String name) {
        String n = JVMUtilities.getName(name);
	exceptionIndexTable.add(new Short(constantPool.put(new ClassIdentifier(n))));
	length += 2;
    }
}
