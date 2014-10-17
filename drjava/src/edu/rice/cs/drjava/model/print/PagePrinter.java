/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.print;

import java.util.*;
import java.awt.print.*;
import java.awt.*;
import java.awt.font.*;

/**
 * Class which represents a Printable object for a given
 * page of the print job.
 *
 * @version $Id: PagePrinter.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class PagePrinter implements Printable {

  // private int _page;
  private ArrayList<TextLayout> _textLayouts;
  private ArrayList<TextLayout> _lineNumbers;
  private String _fileName;
  private DrJavaBook _parent;

  /** Constructs a PagePrinter for a given page number (which is ignored!), a
   * given filename, and parent.
   */
  public PagePrinter(int page, String fileName, DrJavaBook parent) {
    // _page = page;
    _textLayouts = new ArrayList<TextLayout>();
    _lineNumbers = new ArrayList<TextLayout>();
    _fileName = fileName;
    _parent = parent;
  }

  /** Method which adds a TextLayout (and lineNumber) to this
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

  /** Method to support the Printable interface. It prints the
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
    for (int i = 0; i < _textLayouts.size(); i++) {
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

  /** Method which prints the footer onto the document
   * @param g2d The Graphics2D object to print the footer to.
   * @param format The PageFormat to use.
   * @param page The page number to print.
   */
  private void printFooter(Graphics2D g2d, PageFormat format, int page) {
    TextLayout footerFile = new TextLayout(_fileName, DrJavaBook.FOOTER_FONT, g2d.getFontRenderContext());
    float footerPlace = (float) (format.getImageableWidth() - footerFile.getAdvance()) / 2;

    footerFile.draw(g2d, footerPlace, (float) format.getImageableHeight() - footerFile.getDescent());

    TextLayout footerPageNo = new TextLayout(page + "", DrJavaBook.FOOTER_FONT, g2d.getFontRenderContext());
    footerPageNo.draw(g2d,
                      (float) format.getImageableWidth() - footerPageNo.getAdvance(),
                      (float) format.getImageableHeight() - footerPageNo.getDescent());
  }

}
