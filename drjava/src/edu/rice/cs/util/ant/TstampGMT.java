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

package edu.rice.cs.util.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

/**
 * A simple, inflexible replacement for Ant's TSTAMP task that
 * always uses GMT.
 * 
 * This simple task never allows parameters or nested elements.
 * It only sets the DSTAMP and TSTAMP variables.
 *
 * @version $Id$
*/
public class TstampGMT extends Task {
  private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
  private static final SimpleDateFormat DATE_FORMAT
    = new SimpleDateFormat ("yyyyMMdd");
  private static final SimpleDateFormat TIME_FORMAT
    = new SimpleDateFormat ("HHmm");

  {
    DATE_FORMAT.setTimeZone(GMT);
    TIME_FORMAT.setTimeZone(GMT);
  }

  public void execute() throws BuildException {
    try {
      Date d = new Date();
      project.setProperty("DSTAMP", DATE_FORMAT.format(d));
      project.setProperty("TSTAMP", TIME_FORMAT.format(d));
    }
    catch (Exception e) {
      throw new BuildException(e);
    }
  }
}


