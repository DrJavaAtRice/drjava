/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 *
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import gj.util.Vector;

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
  public static void printLastIndentTrace(PrintStream ps){
    if (trace == null){
      ps.println("No trace to print");
    } else {
      for (int x = 0; x < trace.size(); x++){
	ps.println(trace.elementAt(x));
      }
      ps.println("******************************");
    }
  }

  public static void setRuleTraceEnabled(boolean ruleTraceEnabled){
    IndentRuleWithTrace.ruleTraceEnabled = ruleTraceEnabled;
  }

  static Vector<String> getTrace(){
    return trace;
  }

  /**
   * This rule just adds to the trace kept in trace
   */
  protected static void _addToIndentTrace(String ruleName, String direction, boolean terminus){
    if (ruleTraceEnabled){
      if(startOver){
	trace = new Vector<String>();
      }
      startOver = terminus;
      trace.addElement(ruleName + " " + direction);
    }
  }


  /**
   * Properly indents the line that the current position is on.
   * Replaces all whitespace characters at the beginning of the
   * line with the appropriate spacing or characters.
   * @param doc DefinitionsDocument containing the line to be indented.
   */
  public void indentLine(DefinitionsDocument doc, int pos, int reason) {
    int oldPos = doc.getCurrentLocation();
    doc.setCurrentLocation(pos);
    indentLine(doc, reason);
    if (oldPos > doc.getLength()) {
      oldPos = doc.getLength();
    }
    doc.setCurrentLocation(oldPos);
  }

  public void indentLine(DefinitionsDocument doc, int reason){
    _addToIndentTrace(getRuleName(), TERMINUS_RULE, true);

    //Add the next line, and every time something is indented, the indent trace will be printed
    //printLastIndentTrace(System.out);
  }

  /**
   * The rule name to report to _addToIndentTrace
   */
  public String getRuleName(){
    return this.getClass().getName();
  }
}
