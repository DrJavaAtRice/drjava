/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/


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
 * This class allows the creation of JVM bytecode method format outputs
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/05/06
 */

public class MethodInfo extends AttributeOwnerComponent {
  /**
   * The descriptor index
   */
  private short descriptorIndex;
  
  /**
   * Creates a new method info
   * The type names must be fully qualified.
   * <p>The following strings are valid class names:
   * <ul>
   *   <li>"int"</li>
   *   <li>"Z"</li>
   *   <li>"java.lang.String"</li>
   *   <li>"java.lang.Object[][]"</li>
   *   <li>"Ljava/lang/String;"</li>
   *   <li>"[[Ljava/lang/Integer;"</li>
   * </ul>
   * @param cp the constant pool where constants are stored
   * @param rt the return type of this method
   * @param nm the name of this method
   * @param pt the parameters type names
   */
  public MethodInfo(ConstantPool cp, String rt, String nm, String[] pt) {
    constantPool = cp;
    nameIndex = constantPool.putUTF8(nm);
    setSignature(rt, pt);
  }
  
  /**
   * Writes the method info to the given output stream
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
   * Tests if the method is static
   */
  public boolean isStatic() {
    return (accessFlags & Modifier.STATIC) != 0;
  }
  
  /**
   * Tests if the method is abstract
   */
  public boolean isAbstract() {
    return (accessFlags & Modifier.ABSTRACT) != 0;
  }
  
  /**
   * Tests if the method is abstract
   */
  public boolean isVarArgs() {
    //throw new RuntimeException("Modifier.VARARGS not supported (yet) by the 1.5 beta");
    
    //    return (accessFlags & Modifier.VARARGS) != 0; /**/
    
    return (accessFlags & 0x00000080) != 0; // Modifier.VARARGS == 0x00000080
  }
  
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
   * Sets the synchronized flag for this class
   */
  public void setSynchronized() {
    accessFlags |= Modifier.SYNCHRONIZED;
  }
  
  /**
   * Sets the native flag for this class
   */
  public void setNative() {
    accessFlags |= Modifier.NATIVE;
  }
  
  /**
   * Sets the abstract flag for this class
   */
  public void setAbstract() {
    accessFlags |= Modifier.ABSTRACT;
  }
  
  /**
   * Sets the abstract flag for this class
   */
  public void setVarArgs() {
    //throw new RuntimeException("Modifier.VARARGS not supported (yet) by the 1.5 beta");
    //accessFlags |= Modifier.VARARGS; /**/
    accessFlags |= 0x00000080; // Modifier.VARARGS == 0x00000080
  }
  
  /**
   * Sets the strict flag for this class
   */
  public void setStrict() {
    accessFlags |= Modifier.STRICT;
  }
  
  // Name and type ////////////////////////////////////////////////////////////
  
  /**
   * Creates the exception attribute for this method
   */
  public ExceptionsAttribute createExceptionsAttribute() {
    ExceptionsAttribute result = new ExceptionsAttribute(constantPool);
    attributes.add(result);
    return result;
  }
  
  /**
   * Creates the code attribute for this method
   */
  public CodeAttribute createCodeAttribute() {
    CodeAttribute result = new CodeAttribute(constantPool);
    attributes.add(result);
    return result;
  }
  
  /**
   * Sets the signature of this method.
   * @param rt the return type name
   * @param pt the parameters type names
   */
  private void setSignature(String rt, String[] pt) {
    String   r = JVMUtilities.getReturnTypeName(rt);
    String[] p = new String[pt.length];
    for (int i = 0; i < pt.length; i++) {
      p[i] = JVMUtilities.getParameterTypeName(pt[i]);
    }
    String sig = JVMUtilities.createMethodDescriptor(r, p);
    descriptorIndex = constantPool.putUTF8(sig);
  }
}
