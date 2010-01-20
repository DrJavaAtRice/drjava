/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedList;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.swing.DisplayManager;
import edu.rice.cs.util.swing.Utilities;

/** This class extends a Swing view class.  Hence it should only be accessed from the event-handling thread. */
public class RecentDocFrame extends JWindow {
  // MainFrame
  MainFrame _frame;
  
  // The manager that gives filenames and icons
  DisplayManager<OpenDefinitionsDocument> _displayManager = MainFrame.getOddDisplayManager30();
  
  // the label that shows the icon and filename
  JLabel _label;
  // the panel that holds the label and textpane
  JPanel _panel;
  // the pane that holds the sample of source
  JTextPane _textpane;
  // the scroller that holds the text
  JScrollPane _scroller;
  // the currently selected document
  int _current = 0;
  
  int _padding = 4;
  
  LinkedList<OpenDefinitionsDocument> _docs = new LinkedList<OpenDefinitionsDocument>();
  
  private OptionListener<Color> _colorListener = new OptionListener<Color>() {
    public void optionChanged(OptionEvent<Color> oce) { updateFontColor(); }
  };
  
  private OptionListener<Font> _fontListener = new OptionListener<Font>() {
    public void optionChanged(OptionEvent<Font> oce) { updateFontColor(); }
  };
  
  private OptionListener<Boolean> _antialiasListener = new OptionListener<Boolean>() {
    public void optionChanged(OptionEvent<Boolean> oce) { updateFontColor(); }
  };
  
  private OptionListener<Boolean> _showSourceListener = new OptionListener<Boolean>() {
    public void optionChanged(OptionEvent<Boolean> oce) { _showSource = oce.value; }
  };
  
  /* if the pane should antialias itself */
  boolean _antiAliasText = false;
  
  /* if we should show source code when switching */
  boolean _showSource;
  
  public RecentDocFrame(MainFrame f) {
    super();
    _frame = f;
    _current = 0;
    _label = new JLabel("...") {
      // Enable anti-aliased text by overriding paintComponent.
      protected void paintComponent(Graphics g) {
        if (_antiAliasText && g instanceof Graphics2D) {
          Graphics2D g2d = (Graphics2D)g;
          g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        super.paintComponent(g);
      }
    };
    _panel = new JPanel();
    _scroller = new JScrollPane();
    _textpane = new JTextPane() {
      // Enable anti-aliased text by overriding paintComponent.
      protected void paintComponent(Graphics g) {
        if (_antiAliasText && g instanceof Graphics2D) {
          Graphics2D g2d = (Graphics2D)g;
          g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        super.paintComponent(g);
      }
    };
    
    _textpane.setText("...");
    _scroller.getViewport().add(_textpane);
    _scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    _scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    _scroller.setMaximumSize(new Dimension(300,200));
    
    _panel.setLayout(new BorderLayout());
    _panel.add(_label, BorderLayout.NORTH);
    _panel.add(_scroller, BorderLayout.SOUTH);
    
    getContentPane().add(_panel);
    pack();
    updateFontColor();
    _showSource = DrJava.getConfig().getSetting(OptionConstants.SHOW_SOURCE_WHEN_SWITCHING);
    DrJava.getConfig().addOptionListener(OptionConstants.DEFINITIONS_BACKGROUND_COLOR, _colorListener);
    DrJava.getConfig().addOptionListener(OptionConstants.DEFINITIONS_NORMAL_COLOR, _colorListener);
    DrJava.getConfig().addOptionListener(OptionConstants.FONT_MAIN, _fontListener);
    DrJava.getConfig().addOptionListener(OptionConstants.TEXT_ANTIALIAS, _antialiasListener);
    DrJava.getConfig().addOptionListener(OptionConstants.SHOW_SOURCE_WHEN_SWITCHING, _showSourceListener);
  }
  
  private void updateFontColor() {
    Font  mainFont = DrJava.getConfig().getSetting(OptionConstants.FONT_MAIN);
    Color backColor = DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_BACKGROUND_COLOR);
    Color fontColor = DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_NORMAL_COLOR);
    /* make it bigger */
    Font titleFont = mainFont.deriveFont((float) (mainFont.getSize() + 3));
    _antiAliasText = DrJava.getConfig().getSetting(OptionConstants.TEXT_ANTIALIAS).booleanValue();
    
    _label.setForeground(fontColor);
    _panel.setBackground(backColor);
    _label.setFont(titleFont);
    _textpane.setForeground(fontColor);
    _textpane.setFont(mainFont);;
    _textpane.setBackground(backColor);
    _scroller.setBackground(backColor);
    _scroller.setBorder(new EmptyBorder(0,0,0,0));
    _panel.setBorder(new LineBorder(fontColor, 1));
  }
  /** Moves the document d to the beginning of the list if it's already in the list, or it adds it to the
    * beginning if its not already in the list.
    */
  public void pokeDocument(OpenDefinitionsDocument d) {
    if (_docs.contains(d)) {
      _current = _docs.indexOf(d);
      reset();
    }
    else _docs.addFirst(d);
  }
  
  /** Removes the document from the list. */
  public void closeDocument(OpenDefinitionsDocument d) { _docs.remove(d); }
  
  private void show(int _current) {
    OpenDefinitionsDocument doc = _docs.get(_current);
    
    String text = getTextFor(doc);
    
    _label.setText(_displayManager.getName(doc));
    _label.setIcon(_displayManager.getIcon(doc));
    
    if (text.length() > 0) {
      // as wide as the text area wants, but only 200px high
      _textpane.setText(text);
      _scroller.setPreferredSize(_textpane.getPreferredScrollableViewportSize());
      if (_scroller.getPreferredSize().getHeight() > 200)
        _scroller.setPreferredSize(new Dimension((int)_scroller.getPreferredSize().getWidth(), 200));
      
      _scroller.setVisible(_showSource);
    }
    else _scroller.setVisible(false);
    
    Dimension d = _label.getMinimumSize();
    d.setSize(d.getWidth() + _padding*2, d.getHeight() + _padding*2);
    _label.setPreferredSize(d);
    _label.setHorizontalAlignment(SwingConstants.CENTER);
    _label.setVerticalAlignment(SwingConstants.CENTER);
    pack();
    centerH();
  }
  
  /** Sets the current document to be the next document in the list. */
  public void next() {
    if (_docs.size() > 0) {
      _current++;
      if (_current >= _docs.size()) _current = 0;
      show(_current);
    }
  }
  
  /** Sets the current document to be the previous document in the list. */
  public void prev() {
    if (_docs.size() > 0) {
      _current--;
      if (_current < 0) _current = _docs.size() - 1;
      show(_current);
    }
  }
  
  private String getTextFor(OpenDefinitionsDocument doc) {
    DefinitionsPane pane = _frame.getDefPaneGivenODD(doc);
    String endl = "\n"; // was StringOps.EOL; but Swing uses '\n' for newLine
    int loc = pane.getCaretPosition();
    int start = loc;
    int end = loc;
    String text;
    text = doc.getText();
    
    /* get the starting point of 2 lines up... */
    for (int i = 0; i < 4; i++) {
      if (start > 0) start = text.lastIndexOf(endl, start-endl.length());
    }
    if (start == -1) start = 0;
    
    // skip the end line, if we're at one
//    if (doc.getLength() >= endl.length() && text.substring(start, start+endl.length()) == endl) 
//    start += endl.length();
    if (doc.getLength() >= endl.length() && text.substring(start, start+endl.length()).equals(endl)) 
      start += endl.length();
    /* get the ending point 2 lines down */
    int index;
    for (int i = 0; i < 4; i++) {
      if (end < doc.getLength()) {
        index = text.indexOf(endl, end + endl.length());
        if (index != -1) end = index;
      }
    }
    if (end < start) end = start;
    text = text.substring(start, end);
    return text;
  }
  
  /** Resets the frame to point to the first document in the list. */
  public void first() {
    _current = 0;
    next();
  }
  
  public void refreshColor() { }
  
  /** Sets this frame as visible only if _docs is non empty. Also resets the frame accordingly */
  public void setVisible(boolean v) {
    centerH();
    if (_docs.size() > 0) {
      if (v) { 
        centerV();
        refreshColor();
        first();
      }
      else reset();
      super.setVisible(v);
    }
  }
  
  /** Centers the frame in the screen. */
  private void centerH() { Utilities.setPopupLoc(this, _frame); }
  
  /** Centers the frame in the screen. */
  private void centerV() {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    setLocation((int)getLocation().getX(), (screenSize.height - frameSize.height) / 2);
  }
  
  /** Moves the selected document to the front of the list. */
  public void reset() {
    if (_current < _docs.size()) _docs.addFirst(_docs.remove(_current));
  }
  
  /** Returns null if the list is empty, or the currently prefered OpenDefinitionsDocument. */
  public OpenDefinitionsDocument getDocument() {
    if (_docs.size() > 0) return _docs.getFirst();
    return null;
  }
  
//  private ImageIcon _getIconResource(String name) {
//    URL url = RecentDocFrame.class.getResource("icons/" + name);
//    if (url != null) return new ImageIcon(url);
//    return null;
//  }
  
}
