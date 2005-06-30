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

package edu.rice.cs.drjava.model.definitions.indent;

import edu.rice.cs.drjava.model.AbstractDJDocument;

// TODO: Check synchronization.
import java.util.Vector;
import java.io.PrintStream;

/**
 * This class does almost all the work for keeping an indent tree trace.  IndentRuleQuestion
 * also does some of the work, and any subclass may substitute its own version of getRuleName()
 * Note: traceing is disabled by default
 * @version $Id$
 */
public abstract class IndentRuleWithTrace implements IndentRule{

  private static Vector<String> trace = null;
  private static boolean startOver = true;
  private static boolean ruleTraceEnabled = false;

  public static final String YES = "Yes";
  public static final String NO = "No";
  public static final String TERMINUS_RULE = "";

  /* This method prints the most recent trace through the indent tree */
  public static void printLastIndentTrace(PrintStream ps) {
    if (trace == null) {
      ps.println("No trace to print");
    } else {
      for (int x = 0; x < trace.size(); x++) {
        ps.println(trace.get(x));
      }
      ps.println("******************************");
    }
  }

  public static void setRuleTraceEnabled(boolean ruleTraceEnabled) {
    IndentRuleWithTrace.ruleTraceEnabled = ruleTraceEnabled;
  }

  static Vector<String> getTrace() {
    return trace;
  }

  /**
   * This rule just adds to the trace kept in trace
   */
  protected static void _addToIndentTrace(String ruleName, String direction, boolean terminus) {
    if (ruleTraceEnabled) {
      if (startOver) {
 trace = new Vector<String>();
      }
      startOver = terminus;
      trace.add(ruleName + " " + direction);
    }
  }


  /** Properly indents the line that the current position is on.
   *  Replaces all whitespace characters at the beginning of the
   *  line with the appropriate spacing or characters.
   *  @param doc AbstractDJDocument containing the line to be indented.
   *  @param pos ?
   *  @param reason ?
   *  @return true if the caller should update the current location itself,
   *          false if the indenter has already handled this
   */
  public boolean indentLine(AbstractDJDocument doc, int pos, int reason) {
    int oldPos = doc.getCurrentLocation();
    doc.setCurrentLocation(pos);
    indentLine(doc, reason);
    if (oldPos > doc.getLength()) oldPos = doc.getLength();
    doc.setCurrentLocation(oldPos);
    return false;
  }

  public boolean indentLine(AbstractDJDocument doc, int reason) {
    _addToIndentTrace(getRuleName(), TERMINUS_RULE, true);

    //Add the next line, and every time something is indented, the indent trace will be printed
    //printLastIndentTrace(System.out);
    return true;
  }

  /** The rule name to report to _addToIndentTrace */
  public String getRuleName() { return this.getClass().getName(); }
}
