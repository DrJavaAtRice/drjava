/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/



package edu.rice.cs.drjava.model.definitions.indent;

/**
 * Test class for an indent rule with a really long name.  :-)
 * Inherits from ActionStartPrevLinePlusBackupTest, since this rule's 
 * functionality should be a strict extension of ActionStartPrevLinePlusBackup.
 * @version $Id$
 */
public final class ActionStartPrevLinePlusMultilineTest
    extends ActionStartPrevLinePlusBackupTest {
  
//  /**
//   * Factory to enable reuse of methods from ActionStartPrevLinePlusTest.
//   * This creates an action that should behave identically to an instance of
//   * ActionStartPrevLinePlus.
//   * @param suffix the text to be added by this rule after indent padding
//   * @see ActionStartPrevLinePlus#ActionStartPrevLinePlus(String)
//   */
//  private IndentRuleAction makeAction(String suffix) {
//    return new ActionStartPrevLinePlusMultiline(new String[] {suffix},
//                                                0, suffix.length());
//  }
  
//  /**
//   * Factory to enable reuse of methods from ActionStartPrevLinePlusBackupTest.
//   * This works similarly to makeAction(String).
//   * @param suffix the text to be added by this rule after indent padding
//   * @param position the character within the suffix string before which to
//   * place the cursor
//   * @see ActionStartPrevLinePlusBackup#ActionStartPrevLinePlusBackup(String, int)
//   */
//  private IndentRuleAction makeBackupAction(String suffix, int position) {
//    return new ActionStartPrevLinePlusMultiline(new String[] {suffix},
//                                                0, position);
//  }
  
  
}
