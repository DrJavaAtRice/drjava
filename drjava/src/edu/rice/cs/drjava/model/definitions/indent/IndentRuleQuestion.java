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
import edu.rice.cs.drjava.model.definitions.reducedmodel.BraceReduction;

/**
 * A question node in the decision tree for the indentation system.
 * Calls to <code>indentLine</code> on an IndentRuleQuestion will
 * make a decision based on context and call the same method on
 * one of its children.  The leaves of the tree are represented
 * by IndentRuleAction objects.
 * @version $Id$
 */
public abstract class IndentRuleQuestion implements IndentRule {
  /**
   * Node in decision tree to use if the rule holds in this context.
   */
  private final IndentRule _yesRule;

  /**
   * Node in decision tree to use if the rule does not hold in this context.
   */
  private final IndentRule _noRule;
  
  /**
   * Constructs a new Question indent rule using the two given children.
   * @param yesRule Rule to use if this rule holds
   * @param noRule Rule to use if this rule does not hold
   */
  public IndentRuleQuestion(final IndentRule yesRule, final IndentRule noRule) {
    _yesRule = yesRule;
    _noRule = noRule;
  }

  /**
   * Determines if the given rule holds in this context.
   * @param doc DefinitionsDocument containing the line to be indented.
   * @return true if this node's rule holds.
   */
  abstract boolean applyRule(DefinitionsDocument doc);

  /**
   * Determines if the given rule holds in this context.
   * @param doc DefinitionsDocument containing the line to be indented.
   * @param pos Position within line to be indented.
   * @return true if this node's rule holds.
   */
  boolean applyRule(DefinitionsDocument doc, int pos) {
    int oldPos = doc.getCurrentLocation();
    doc.setCurrentLocation(pos);
    boolean result = applyRule(doc);
    if (oldPos > doc.getLength()) {
      oldPos = doc.getLength();
    }
    doc.setCurrentLocation(oldPos);
    return result;
  }

  /**
   * Determines if the given rule holds in this context and calls
   * the same method on one of its child nodes.
   * @param doc DefinitionsDocument containing the line to be indented.
   * @param reducedModel reduced model used by the document.
   */
  public void indentLine(DefinitionsDocument doc)
  {
    if (applyRule(doc)) {
      _yesRule.indentLine(doc);
    }
    else {
      _noRule.indentLine(doc);
    }
  }
  
  /**
   * Properly indents the line that the current position is on.
   * Replaces all whitespace characters at the beginning of the
   * line with the appropriate spacing or characters.
   * @param doc DefinitionsDocument containing the line to be indented.
   */
  public void indentLine(DefinitionsDocument doc, int pos) {
    int oldPos = doc.getCurrentLocation();
    doc.setCurrentLocation(pos);
    indentLine(doc);
    if (oldPos > doc.getLength()) {
      oldPos = doc.getLength();
    }
    doc.setCurrentLocation(oldPos);
  }
}





