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

package edu.rice.cs.drjava.model.print;

import java.util.*;
import java.awt.print.*;
import java.awt.*;
import java.awt.font.*;
import java.text.*;

/**
 * The DrJavaBook class in DrJava's implementation of a Pageable object. It
 * serves as the control class for printing, and is responsible for
 * preparing the print job of previewing or printing given the String
 * representation of the document.
 *
 * @version $Id$
 */
public class DrJavaBook implements Pageable {

  private ArrayList<PagePrinter> _pagePrinters;
  private PageFormat _format;
  private String _filename;

  public static Font PRINT_FONT = new Font("Monospaced", Font.PLAIN, 9);
  public static Font FOOTER_FONT = new Font("Monospaced", Font.PLAIN, 8);
  public static Font LINE_FONT = new Font("Monospaced", Font.ITALIC, 8);
  public float LINE_NUMBER_WIDTH;

  private static FontRenderContext DEFAULT_FRC = new FontRenderContext(null, false, true);

  /**
   * Constructs a DrJavaBook which a given content text, filename, and
   * pageformat.
   */
  public DrJavaBook(String text, String filename, PageFormat format) {
    _pagePrinters = new ArrayList<PagePrinter>();
    _format = format;
    _filename = filename;

    TextLayout textl = new TextLayout("XXX ", LINE_FONT, DEFAULT_FRC);
    LINE_NUMBER_WIDTH = (float) textl.getAdvance();

    setUpPagePrinters(text);
  }

  /**
   * Method which creates all of the individual Printable objects
   * given a String text.
   * @param text The text of the document.
   */
  private void setUpPagePrinters(String text) {
    int linenum = 0;
    int reallinenum = 1;
    String thisText = "";
    FontRenderContext frc = new FontRenderContext(null, false, true);

    // determine the number of lines per page
    TextLayout textl = new TextLayout("X", PRINT_FONT, frc);
    float lineHeight = textl.getLeading() + textl.getAscent();
    int linesPerPage = (int) (_format.getImageableHeight() / lineHeight) - 1;

    HashMap<TextAttribute, Object> map = new HashMap<TextAttribute, Object>();
    map.put(TextAttribute.FONT, PRINT_FONT);

    char[] carraigeReturn = {(char) 10};
    String lineSeparator = new String(carraigeReturn);

    try {
      thisText = text.substring(0, text.indexOf(lineSeparator));
      text = text.substring(text.indexOf(lineSeparator) + 1);
    } catch (StringIndexOutOfBoundsException e) {
      thisText = text;
      text = "";
    }

    int page = 0;
    PagePrinter thisPagePrinter = new PagePrinter(page, _filename, this);
    _pagePrinters.add(thisPagePrinter);

    // loop over each of the *real* lines in the document
    while (! (thisText.equals("") && (text.equals("")))) {
      if (thisText.equals(""))
        thisText = " ";

      AttributedCharacterIterator charIterator = (new AttributedString(thisText, map)).getIterator();
      LineBreakMeasurer measurer = new LineBreakMeasurer(charIterator, frc);

      boolean isCarryLine = false;

      // loop over each of the broken lines in the real line
      while (measurer.getPosition() < charIterator.getEndIndex()) {
        TextLayout pageNumber = new TextLayout(" ", LINE_FONT, DEFAULT_FRC);

        if (! isCarryLine)
          pageNumber = new TextLayout("" + reallinenum, LINE_FONT, DEFAULT_FRC);

        // add this TextLayout to the PagePrinter
        thisPagePrinter.add(measurer.nextLayout((float) _format.getImageableWidth() - LINE_NUMBER_WIDTH), pageNumber);

        linenum++;
        // Create a new PagePrinter, if necessary
        if (linenum == (linesPerPage * (page+1)))
        {
          page++;
          thisPagePrinter = new PagePrinter(page, _filename, this);
          _pagePrinters.add(thisPagePrinter);
        }

        isCarryLine = true;
      }

      reallinenum++;

      // Get next *real* line
      try {
        thisText = text.substring(0, text.indexOf(lineSeparator));
        text = text.substring(text.indexOf(lineSeparator) + 1);
      } catch (StringIndexOutOfBoundsException e) {
        thisText = text;
        text = "";
      }
    }
  }

  /**
   * Method to comply with the Pageable interface
   * @return The number of pages in this print job.
   */
  public int getNumberOfPages() {
    return _pagePrinters.size();
  }

  /**
   * Method to comply with the Pageable interface
   * @param pageIndex The page number
   * @return the PageFormat of this print job.
   */
  public PageFormat getPageFormat(int pageIndex) {
    return _format;
  }

  /**
   * Method to comply with the Pageable interface, returns
   * the Printable object for a given page.
   * @param pageIndex The page number.
   * @return The Printable object for the given page.
   */
  public Printable getPrintable(int pageIndex) {
    return (Printable) _pagePrinters.get(pageIndex);
  }

}
