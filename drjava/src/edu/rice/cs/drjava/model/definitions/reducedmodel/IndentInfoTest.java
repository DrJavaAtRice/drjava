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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import  junit.framework.*;
import  junit.extensions.*;
import  javax.swing.text.BadLocationException;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.EventNotifier;

/**
 
 * @version $Id$
 */
public final class IndentInfoTest extends TestCase {
  
  private String _text;
  private DefinitionsDocument _document;
  //private BraceReduction _reduced;
  private IndentInfo _info;
  private EventNotifier _notifier;

  public IndentInfoTest(String name) {
    super(name);
  }

  public void setUp() {
    _notifier = new EventNotifier();
    _document = new DefinitionsDocument(_notifier);
  }

  private void _infoTestHelper(int location,
          String message,
          int expDistToPrevNewline,
          int expDistToBrace,
          int expDistToNewline,
          int expDistToBraceCurrent,
          int expDistToNewlineCurrent) {
      
    _document.setCurrentLocation(location);
    //_reduced = _document.getReduced();
    _info = _document.getIndentInformation();

    assertEquals(message + " -- distToPrevNewline", expDistToPrevNewline, _info.distToPrevNewline);
    assertEquals(message + " -- distToBrace", expDistToBrace, _info.distToBrace);
    assertEquals(message + " -- distToNewline", expDistToNewline, _info.distToNewline);
    assertEquals(message + " -- distToBraceCurrent", expDistToBraceCurrent, _info.distToBraceCurrent);
    assertEquals(message + " -- distToNewlineCurrent", expDistToNewlineCurrent, _info.distToNewlineCurrent);  
  }
          
  public void testFieldsForCurrentLocation() throws BadLocationException {
    
    _text = "foo {\nvoid m1(int a,\nint b) {\n}\n}";
    //       .   . ..   .  ..     . .    . . ... .
    //       |          |         |           |
    //       0          10        20          30

    _document.remove(0, _document.getLength());
    _document.insertString(0, _text, null);
  
    _infoTestHelper(0, "DOCSTART -- no brace or newline",     -1, -1, -1, -1, -1);
    _infoTestHelper(4, "Location has no brace or newline",    -1, -1, -1, -1, -1);
    _infoTestHelper(5, "Location has a brace but no newline", -1, -1, -1,  1, -1);
    _infoTestHelper(6, "Location has a brace and a newline",   0,  2, -1,  2, -1);
    _infoTestHelper(10, "Location has a brace and a newline",  4,  6, -1,  6, -1);
    _infoTestHelper(13, "Location has a brace and a newline",  7,  9, -1,  9, -1);
    _infoTestHelper(14, "Location has a brace and a newline",  8, 10, -1,  1,  8);
    _infoTestHelper(20, "At \\n within parens",               14, 16, -1,  7, 14);
    _infoTestHelper(21, "Second line within parens",           0,  8, 15,  8, 15);
    _infoTestHelper(26, "On close paren",                      5, 13, 20, 13, 20);
    _infoTestHelper(28, "On second open brace",                7, 15, 22, 24, -1);
    _infoTestHelper(29, "On \\n in second set of braces",      8, 16, 23,  1,  8);
    _infoTestHelper(30, "Close brace of method declaration",   0,  2,  9,  2,  9);
    _infoTestHelper(31, "Last \\n",                            1,  3, 10, 27, -1);
    _infoTestHelper(32, "Final close brace",                   0, 28, -1, 28, -1);
  }
}
