/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.debug.jpda;

import com.sun.jdi.StackFrame;
import edu.rice.cs.drjava.model.debug.DebugStackData;

/** Class for keeping track of a stack frame in the debugger.
  * @version $Id: JPDAStackData.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class JPDAStackData extends DebugStackData {
  /** Object for keeping track of a stack frame.
    * @param frame JPDA's reference to the stack frame
    */
  public JPDAStackData(StackFrame frame) {
    super(methodName(frame), frame.location().lineNumber());
  }
  
  /** Object for keeping track of a stack frame.
    * @param method method name as "myPackage.MyClass.myMethod"
    * @param lineNum line number
    */
  public JPDAStackData(String method, int lineNum) {
    super(method, lineNum);
  }
  
  /** Translate the type and method information from a JDI StackFrame to a "myPackage.MyClass.myMethod" method name string.
    * @param frame JDI stack frame
    * @return "myPackage.MyClass.myMethod" method name string
    */
  public static String methodName(StackFrame frame) {
    return frame.location().declaringType().name() + "." + frame.location().method().name();
  }
}
