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
import java.text.*;

/**
 * The DrJavaBook class is DrJava's implementation of a Pageable object. It
 * serves as the control class for printing, and is responsible for
 * preparing the print job of previewing or printing given the String
 * representation of the document.
 *
 * @version $Id: DrJavaBook.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class DrJavaBook implements Pageable {
  
  private ArrayList<PagePrinter> _pagePrinters;
  private PageFormat _format;
  private String _fileName;
  
  public static final Font PRINT_FONT = new Font("Monospaced", Font.PLAIN, 9);
  public static final Font FOOTER_FONT = new Font("Monospaced", Font.PLAIN, 8);
  public static final Font LINE_FONT = new Font("Monospaced", Font.ITALIC, 8);
  public float LINE_NUM_WIDTH;
  
  private static FontRenderContext DEFAULT_FRC = new FontRenderContext(null, false, true);
  
  /** Constructs a DrJavaBook which a given content text, filename, and pageformat. */
  public DrJavaBook(String text, String fileName, PageFormat format) {
    _pagePrinters = new ArrayList<PagePrinter>();
    _format = format;
    _fileName = fileName;
    
    TextLayout textl = new TextLayout("XXX ", LINE_FONT, DEFAULT_FRC);
    LINE_NUM_WIDTH = textl.getAdvance();
    
    setUpPagePrinters(text);
  }
  
  /** Method which creates all of the individual Printable objects
   * given a String text.
   * @param text The text of the document.
   */
  private void setUpPagePrinters(String text) {
    int linenum = 0;
    int reallinenum = 1;
    String thisText;
    FontRenderContext frc = new FontRenderContext(null, false, true);
    
    // determine the number of lines per page
    TextLayout textl = new TextLayout("X", PRINT_FONT, frc);
    float lineHeight = textl.getLeading() + textl.getAscent();
    int linesPerPage = (int) (_format.getImageableHeight() / lineHeight) - 1;
    
    HashMap<TextAttribute,Object> map = new HashMap<TextAttribute,Object>(); // Added parameterization <TextAttribute, Object>.
    map.put(TextAttribute.FONT, PRINT_FONT);
    
    char[] carriageReturn = {(char) 10};
    String lineSeparator = new String(carriageReturn);
    
    try {
      thisText = text.substring(0, text.indexOf(lineSeparator));
      text = text.substring(text.indexOf(lineSeparator) + 1);
    }
    catch (StringIndexOutOfBoundsException e) {
      thisText = text;
      text = "";
    }
    
    int page = 0;
    PagePrinter thisPagePrinter = new PagePrinter(page, _fileName, this);
    _pagePrinters.add(thisPagePrinter);
    
    // loop over each of the *real* lines in the document
    while (! (thisText.equals("") && (text.equals("")))) {
      if (thisText.equals("")) thisText = " ";
      
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
          thisPagePrinter = new PagePrinter(page, _fileName, this);
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
  
  /** @return The number of pages in this print job. */
  public int getNumberOfPages() { return _pagePrinters.size(); }
  
  /** Returns the PageFormat for this print job.
    * @param pageIndex The page number
    * @return the PageFormat of this print job.
    */
  public PageFormat getPageFormat(int pageIndex) { return _format; }
  
  /** Returns the Printable object for a given page.
    * @param pageIndex The page number.
    * @return The Printable object for the given page.
    */
  public Printable getPrintable(int pageIndex) { return _pagePrinters.get(pageIndex); }
  
}
