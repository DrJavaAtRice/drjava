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

/**
 * Class which represents a Printable object for a given
 * page of the print job.
 *
 * @version $Id$
 */
public class PagePrinter implements Printable {

  private int _page;
  private ArrayList<TextLayout> _textLayouts;
  private ArrayList<TextLayout> _lineNumbers;
  private String _filename;
  private DrJavaBook _parent;

  /**
   * Constructs a PagePrinter for a given page number, a
   * given filename, and parent.
   */
  public PagePrinter(int page, String filename, DrJavaBook parent) {
    _page = page;
    _textLayouts = new ArrayList<TextLayout>();
    _lineNumbers = new ArrayList<TextLayout>();
    _filename = filename;
    _parent = parent;
  }

  /**
   * Method which adds a TextLayout (and lineNumber) to this
   * page.  This is designed to represent a physical line of
   * text to be printed on the document (as opposed to a
   * real line number.
   * @param text The text of the given line.
   * @param lineNumber The Text to write in the lineNumber location.
   */
  public void add(TextLayout text, TextLayout lineNumber) {
    _textLayouts.add(text);
    _lineNumbers.add(lineNumber);
  }

  /**
   * Method to support the Printable interface. It prints the
   * contents of this PagePrinter onto the Graphics object.
   * @param graphics The Graphics object to print to.
   * @param format The PageFormat to use.
   * @param pageIndex The page number to print.
   */
  public int print(Graphics graphics, PageFormat format, int pageIndex) {
    // Set up graphics object
    Graphics2D g2d = (Graphics2D) graphics;
    g2d.translate(format.getImageableX(), format.getImageableY());
    g2d.setPaint(Color.black);

    float y = 0;

    // loop over the TextLayouts, printing out each one and the line number
    for (int i=0; i<_textLayouts.size(); i++) {
      TextLayout layout = _textLayouts.get(i);
      TextLayout lineNumber = _lineNumbers.get(i);

      y += layout.getAscent();
      lineNumber.draw(g2d, 0, y);
      layout.draw(g2d, _parent.LINE_NUM_WIDTH, y);
      y += layout.getLeading();
    }

    // print the footer
    printFooter(g2d, format, pageIndex + 1);

    return PAGE_EXISTS;
  }

  /**
   * Method which prints the footer onto the document
   * @param g2d The Graphics2D object to print the footer to.
   * @param format The PageFormat to use.
   * @param page The page number to print.
   */
  private void printFooter(Graphics2D g2d, PageFormat format, int page) {
    TextLayout footerFile = new TextLayout(_filename, DrJavaBook.FOOTER_FONT, g2d.getFontRenderContext());
    float footerPlace = (float) (format.getImageableWidth() - footerFile.getAdvance()) / 2;

    footerFile.draw(g2d, footerPlace, (float) format.getImageableHeight() - footerFile.getDescent());

    TextLayout footerPageNo = new TextLayout(page + "", DrJavaBook.FOOTER_FONT, g2d.getFontRenderContext());
    footerPageNo.draw(g2d,
                      (float) format.getImageableWidth() - footerPageNo.getAdvance(),
                      (float) format.getImageableHeight() - footerPageNo.getDescent());
  }

}
