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
