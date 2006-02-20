/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.swing;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;

/**
 * FontChooser, adapted from NwFontChooserS by Noah Wairauch.
 * (see http:///forum.java.sun.com/thread.jsp?forum=57&thread=195067)
 *
 * @version $Id$
 */

public class FontChooser extends JDialog {
  /**
   * Available font styles.
   */
  private static final String[] STYLES =
      new String[] { "Plain", "Bold", "Italic", "Bold Italic" };

  /**
   * Available font sizes.
   */
  private static final String[] SIZES =
      new String[] { "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
                     "13", "14", "15", "16", "17", "18", "19", "20", "22",
                     "24", "27", "30", "34", "39", "45", "51", "60"};

  // Lists to display
  private NwList _styleList;
  private NwList _fontList;
  private NwList _sizeList;

  // Swing elements
  private JButton _okButton;
  private JButton _cancelButton;
  private JLabel _sampleText = new JLabel();

  private boolean _clickedOK = false;

  /**
   * Constructs a new modal FontChooser for the given frame,
   * using the specified font.
   */
  private FontChooser(Frame parent, Font font) {
    super(parent, true);
    initAll();
    if (font == null) font = _sampleText.getFont();
    _fontList.setSelectedItem(font.getName());
    _sizeList.setSelectedItem(font.getSize() + "");
    _styleList.setSelectedItem(STYLES[font.getStyle()]);
    //this.setResizable(false);
    resize();
  }

  /**
   * Method used to show the font chooser, and select a new font.
   *
   * @param parent The parent frame.
   * @param title  The title for this window.
   * @param font   The previously chosen font.
   * @return the newly chosen font.
   */
  public static Font showDialog(Frame parent, String title, Font font) {
    FontChooser fd = new FontChooser(parent, font);
    fd.setTitle(title);
    fd.setVisible(true);
    Font chosenFont = null;
    if (fd.clickedOK()) {
      chosenFont = fd.getFont();
    }
    fd.dispose();
    return (chosenFont);
  }

  /**
   * Shows the font chooser with a standard title ("Font Chooser").
   */
  public static Font showDialog(Frame parent, Font font) {
    return showDialog(parent, "Font Chooser", font);
  }

  private void initAll() {
    getContentPane().setLayout(null);
    setBounds(50, 50, 425, 400);
    _sampleText = new JLabel();
    addLists();
    addButtons();
    _sampleText.setForeground(Color.black);
    getContentPane().add(_sampleText);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
      }
    });
    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent evt) {
        resize();
      }
    });
  }

  private void resize() {
    int w = getWidth();
    int h = getHeight();
    double wf = (double) w / 425;
    int w2 = (int) (80 * wf);
    int w3 = (int) (50 * wf);
    if (w3 < 30) w3 = 30;
    int w1 = w - w2 - w3 - 25;
    _fontList.setBounds(5, 5, w1, h - 91);
    _styleList.setBounds(w1 + 10, 5, w2, h - 91);
    _sizeList.setBounds(w1 + w2 + 15, 5, w3, h - 91);
    _sampleText.setBounds(10, h - 78, w - 20, 45);
    _okButton.setBounds(w - 165, h - 55, 70, 20);
    _cancelButton.setBounds(w - 81, h - 55, 70, 20);
    validate();
  }

  private void addLists() {
    _fontList = new NwList(GraphicsEnvironment.getLocalGraphicsEnvironment()
                           .getAvailableFontFamilyNames());
    _styleList = new NwList(STYLES);
    _sizeList = new NwList(SIZES);
    getContentPane().add(_fontList);
    getContentPane().add(_styleList);
    getContentPane().add(_sizeList);
  }

  private void addButtons() {
    _okButton = new JButton("OK");
    _okButton.setMargin(new Insets(0, 0, 0, 0));
    _cancelButton = new JButton("Cancel");
    _cancelButton.setMargin(new Insets(0, 0, 0, 0));
    _okButton.setFont(new Font(" ", 1, 11));
    _cancelButton.setFont(new Font(" ", 1, 12));
    getContentPane().add(_okButton);
    getContentPane().add(_cancelButton);
    _okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        _clickedOK = true;
      }
    });
    _cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        _clickedOK = false;
      }
    });
  }

  private void showSample() {
    int g = 0;
    try {
      g = Integer.parseInt(_sizeList.getSelectedValue());
    }
    catch (NumberFormatException nfe) {
    }
    String st = _styleList.getSelectedValue();
    int s = Font.PLAIN;
    if (st.equalsIgnoreCase("Bold")) s = Font.BOLD;
    if (st.equalsIgnoreCase("Italic")) s = Font.ITALIC;
    if (st.equalsIgnoreCase("Bold Italic")) s = Font.BOLD | Font.ITALIC;
    _sampleText.setFont(new Font(_fontList.getSelectedValue(), s, g));
    _sampleText.setText("The quick brown fox jumped over the lazy dog.");
    _sampleText.setVerticalAlignment(SwingConstants.TOP);
  }

  /**
   * Returns whether the user clicked OK when the dialog was closed.
   * (If false, the user clicked cancel.)
   */
  public boolean clickedOK() {
    return _clickedOK;
  }

  /**
   * Returns the currently selected Font.
   */
  public Font getFont() {
    return _sampleText.getFont();
  }

  /**
   * Private inner class for a list which displays a list of options in addition to a label
   * indicating the currently selected item.
   */
  public class NwList extends JPanel {
    JList jl;
    JScrollPane sp;
    JLabel jt;
    String si = " ";

    public NwList(String[] values) {
      setLayout(null);
      jl = new JList(values);
      sp = new JScrollPane(jl);
      jt = new JLabel();
      jt.setBackground(Color.white);
      jt.setForeground(Color.black);
      jt.setOpaque(true);
      jt.setBorder(new JTextField().getBorder());
      jt.setFont(getFont());
      jl.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          jt.setText((String) jl.getSelectedValue());
          si = (String) jl.getSelectedValue();
          showSample();
        }
      });
      add(sp);
      add(jt);
    }

    public void setBounds(int x, int y, int w, int h) {
      super.setBounds(x, y, w, h);
      sp.setBounds(0, y + 16, w, h - 23);
      sp.revalidate();
      jt.setBounds(0, 0, w, 20);
    }

    public String getSelectedValue() {
      return (si);
    }

    public void setSelectedItem(String s) {
      jl.setSelectedValue(s, true);
    }

  }
}