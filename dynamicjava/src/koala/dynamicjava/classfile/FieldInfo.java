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
import java.lang.reflect.*;
import java.util.*;

/**
 * This class allows the creation of JVM bytecode field format outputs
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/05/06
 */

public class FieldInfo extends AttributeOwnerComponent {
    /**
     * The descriptor index
     */
    private short descriptorIndex;

    /**
     * Creates a new field info
     * @param cp the constant pool where constants are stored
     * @param tp the type name.
     * The type name must be fully qualified.
     * <p>The following strings are valid class names:
     * <ul>
     *   <li>"int"</li>
     *   <li>"Z"</li>
     *   <li>"java.lang.String"</li>
     *   <li>"java.lang.Object[][]"</li>
     *   <li>"Ljava/lang/String;"</li>
     *   <li>"[[Ljava/lang/Integer;"</li>
     * </ul>
     * @param nm the name of the field
     */
    public FieldInfo(ConstantPool cp, String tp, String nm) {
	constantPool = cp;
	nameIndex = constantPool.putUTF8(nm);
	descriptorIndex = constantPool.putUTF8(JVMUtilities.getReturnTypeName(tp));
    }

    /**
     * Writes the field info to the given output stream
     */
    public void write(DataOutputStream out) throws IOException {
	out.writeShort(accessFlags);
	out.writeShort(nameIndex);
	out.writeShort(descriptorIndex);

	out.writeShort(attributes.size());
	Iterator it = attributes.iterator();
	while (it.hasNext()) {
	    ((AttributeInfo)it.next()).write(out);
	}
    }

    // Access flag settings ///////////////////////////////////////////////////

    /**
     * Sets the public flag for this class
     */
    public void setPublic() {
	accessFlags |= Modifier.PUBLIC;
    }

    /**
     * Sets the private flag for this class
     */
    public void setPrivate() {
	accessFlags |= Modifier.PRIVATE;
    }

    /**
     * Sets the protected flag for this class
     */
    public void setProtected() {
	accessFlags |= Modifier.PROTECTED;
    }

    /**
     * Sets the static flag for this class
     */
    public void setStatic() {
	accessFlags |= Modifier.STATIC;
    }

    /**
     * Sets the final flag for this class
     */
    public void setFinal() {
	accessFlags |= Modifier.FINAL;
    }

    /**
     * Sets the volatile flag for this class
     */
    public void setVolatile() {
	accessFlags |= Modifier.VOLATILE;
    }

    /**
     * Sets the transient flag for this class
     */
    public void setTransient() {
	accessFlags |= Modifier.TRANSIENT;
    }

    // Name and type ////////////////////////////////////////////////////////////

    /**
     * Sets the constant value attribute for this field to
     * an integer value.
     */
    public void setConstantValueAttribute(Integer value) {
	attributes.add(new ConstantValueAttribute(constantPool, value));
    }

    /**
     * Sets the constant value attribute for this field to
     * a long value.
     */
    public void setConstantValueAttribute(Long value) {
	attributes.add(new ConstantValueAttribute(constantPool, value));
    }

    /**
     * Sets the constant value attribute for this field to
     * a float value.
     */
    public void setConstantValueAttribute(Float value) {
	attributes.add(new ConstantValueAttribute(constantPool, value));
    }

    /**
     * Sets the constant value attribute for this field to
     * a double value.
     */
    public void setConstantValueAttribute(Double value) {
	attributes.add(new ConstantValueAttribute(constantPool, value));
    }

    /**
     * Sets the constant value attribute for this field to
     * a string value.
     */
    public void setConstantValueAttribute(String value) {
	attributes.add(new ConstantValueAttribute(constantPool, value));
    }

}
