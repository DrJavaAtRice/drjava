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
  public float LINE_NUM_WIDTH;

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
    LINE_NUM_WIDTH = textl.getAdvance();

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

//    HashMap<TextAttribute, Object> map = new HashMap<TextAttribute, Object>();
    HashMap map = new HashMap();
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
        thisPagePrinter.add(measurer.nextLayout((float) _format.getImageableWidth() - LINE_NUM_WIDTH), pageNumber);

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
