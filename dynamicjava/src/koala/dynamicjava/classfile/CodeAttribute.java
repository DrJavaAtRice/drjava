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
import java.util.*;

/**
 * This class represents a method code attribute
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/05/07
 */

public class CodeAttribute extends AttributeInfo {
  /**
   * The max depth of the operand stack
   */
  private short maxStack;
  
  /**
   * The max number of local variables
   */
  private short maxLocals;
  
  /**
   * The code
   */
  private byte[] code;
  
  /**
   * The exception table
   */
  private List exceptionTable;
  
  /**
   * The attributes
   */
  private List attributes;
  
  /**
   * Creates a new empty (not valid) code attribute
   * @param cp the constant pool
   */
  public CodeAttribute(ConstantPool cp) {
    super(cp, "Code");
    length         = 12;
    exceptionTable = new LinkedList();
    attributes     = new LinkedList();
  }
  
  /**
   * Writes the code info to the given output stream.
   */
  public void write(DataOutputStream out) throws IOException {
    out.writeShort(nameIndex);
    out.writeInt(length);
    out.writeShort(maxStack);
    out.writeShort(maxLocals);
    
    out.writeInt(code.length);
    out.write(code);
    
    out.writeShort(exceptionTable.size());
    
    Iterator it = exceptionTable.iterator();
    while (it.hasNext()) {
      ((ExceptionTableEntry)it.next()).write(out);
    }
    
    out.writeShort(attributes.size());
    it = attributes.iterator();
    while (it.hasNext()) {
      ((AttributeInfo)it.next()).write(out);
    }
  }
  
  /**
   * Sets the code for this code attribute
   * @param code the byte code array
   * @param nl   the number of local variables
   * @param ms   the max stack size
   */
  public void setCode(byte[] code, short nl, short ms) {
    maxLocals = nl;
    maxStack  = ms;
    this.code = code;
    length += code.length;
  }
  
  /**
   * Adds an exception entry in the exception table
   * @param spc the start of the try statement
   * @param epc the end   of the try statement
   * @param tpc the handler position
   * @param ex  the name of the exception
   */
  public void addExceptionTableEntry(short spc, short epc, short tpc, String ex) {
    String              n  = JVMUtilities.getName(ex);
    short               s  = constantPool.put(new ClassIdentifier(n));
    ExceptionTableEntry ee = new ExceptionTableEntry(spc, epc, tpc, s);
    
    exceptionTable.add(ee);
    length += ee.getLength();
  }
  
  class ExceptionTableEntry {
    /**
     * The 'try' block starting position
     */
    private short startPc;
    
    /**
     * The 'try' block end
     */
    private short endPc;
    
    /**
     * The index of the 'catch' statement
     */
    private short handlerPc;
    
    /**
     * The index of the name of the catched exception in the constant pool
     */
    private short catchType;
    
    /**
     * Creates a new exception table entry
     * @param spc the 'try' block starting position
     * @param epc the 'try' block end
     * @param hpc the index of the 'catch' statement
     * @param ct  the index of the name of the catched exception in the constant pool
     */
    public ExceptionTableEntry(short spc, short epc, short hpc, short ct) {
      startPc   = spc;
      endPc     = epc;
      handlerPc = hpc;
      catchType = ct;
    }
    
    /**
     * Returns the length of the entry
     */
    public short getLength() {
      return (short)8;
    }
    
    /**
     * Writes the field info to the given output stream
     */
    public void write(OutputStream out) throws IOException {
      write(new DataOutputStream(out));
    }
    
    /**
     * Writes the field info to the given output stream.
     */
    public void write(DataOutputStream out) throws IOException {
      out.writeShort(startPc);
      out.writeShort(endPc);
      out.writeShort(handlerPc);
      out.writeShort(catchType);
    }
  }
}
