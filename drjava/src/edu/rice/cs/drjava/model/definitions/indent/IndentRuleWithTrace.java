/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

import edu.rice.cs.drjava.model.AbstractDJDocument;

// TODO: Check synchronization.
import java.util.ArrayList;
import java.io.PrintStream;

/** This class does almost all the work for keeping an indent tree trace.  IndentRuleQuestion
  * also does some of the work, and any subclass may substitute its own version of getRuleName()
  * Note: traceing is disabled by default
  * @version $Id$
  */
public abstract class IndentRuleWithTrace implements IndentRule {

  private static ArrayList<String> trace = null;
  private static boolean startOver = true;
  private static boolean ruleTraceEnabled = false;

  public static final String YES = "Yes";
  public static final String NO = "No";
  public static final String TERMINUS_RULE = "";

  /* This method prints the most recent trace through the indent tree */
  public static void printLastIndentTrace(PrintStream ps) {
    if (trace == null) {
      ps.println("No trace to print");
    } 
    else {
      for (int x = 0; x < trace.size(); x++) {
        ps.println(trace.get(x));
      }
      ps.println("******************************");
    }
  }

  public static void setRuleTraceEnabled(boolean ruleTraceEnabled) {
    IndentRuleWithTrace.ruleTraceEnabled = ruleTraceEnabled;
  }

  static ArrayList<String> getTrace() { return trace; }

  /** This rule just adds to the trace kept in trace */
  protected static void _addToIndentTrace(String ruleName, String direction, boolean terminus) {
    if (ruleTraceEnabled) {
      if (startOver) trace = new ArrayList<String>();
      startOver = terminus;
      trace.add(ruleName + " " + direction);
    }
  }

  /** Properly indents the line identified by pos. Replaces all whitespace characters at the beginning of the line with
    * the appropriate spacing or characters.
    * @param doc  the AbstractDJDocument containing the line to be indented.
    * @param pos  the position identifying the line to be indented
    * @param reason  the reason that the indentation is taking place
    * @return true if the caller should update the current location itself, false if the indenter has already handled it
    */
  public void indentLine(AbstractDJDocument doc, int pos, Indenter.IndentReason reason) {
    int oldPos = doc.getCurrentLocation();
    doc.setCurrentLocation(pos);
    indentLine(doc, reason);
    if (oldPos > doc.getLength()) oldPos = doc.getLength();
    doc.setCurrentLocation(oldPos);
  }

  /** This method optionally traces indenting; it does not indent the current line! */
  public void indentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {
    _addToIndentTrace(getRuleName(), TERMINUS_RULE, true);;
  }
  
  /** Convenience method that wraps calls on indentLine in a write lock. Only used in testing. */
  public void testIndentLine(AbstractDJDocument doc, int pos, Indenter.IndentReason reason) {
    indentLine(doc, pos, reason); 
  }
  
  /** Convenience method that wraps calls on indentLine in a write lock. Only used in testing. */
   public void testIndentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {
     indentLine(doc, reason); 
   }

  /** The rule name to report to _addToIndentTrace */
  public String getRuleName() { return this.getClass().getName(); }
}
