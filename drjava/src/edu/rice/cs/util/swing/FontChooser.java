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

package edu.rice.cs.util.swing;

import edu.rice.cs.util.swing.Utilities;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;

/**
 * FontChooser, adapted from NwFontChooserS by Noah Wairauch.
 * (see http:///forum.java.sun.com/thread.jsp?forum=57&thread=195067)
 *
 * @version $Id: FontChooser.java 5594 2012-06-21 11:23:40Z rcartwright $
 */

public class FontChooser extends JDialog {
  /** Available font styles.
   */
  private static final String[] STYLES =
      new String[] { "Plain", "Bold", "Italic", "Bold Italic" };

  /** Available font sizes.
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

  /** Constructs a new modal FontChooser for the given frame,
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
    //resize();
  }

  /** Method used to show the font chooser, and select a new font.
   *
   * @param parent The parent frame.
   * @param title  The title for this window.
   * @param font   The previously chosen font.
   * @return the newly chosen font.
   */
  public static Font showDialog(Frame parent, String title, Font font) {
    FontChooser fd = new FontChooser(parent, font);
    fd.setTitle(title);
    
    Utilities.setPopupLoc(fd, parent);
    fd.setVisible(true);

    Font chosenFont = null;
    if (fd.clickedOK()) {
      chosenFont = fd.getFont();
    }
    fd.dispose();
    return (chosenFont);
  }

  /** Shows the font chooser with a standard title ("Font Chooser").
   */
  public static Font showDialog(Frame parent, Font font) {
    return showDialog(parent, "Font Chooser", font);
  }

  private void initAll() {
    Container cp = getContentPane();
    GridBagLayout cpLayout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    cp.setLayout(cpLayout);

    // lists
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.weighty = 1.0;
    _fontList = new NwList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    cpLayout.setConstraints(_fontList, c);
    cp.add(_fontList);
//    JPanel fontListPanel = new JPanel();
//    fontListPanel.setBackground(Color.RED);
//    cpLayout.setConstraints(fontListPanel, c);
//    cp.add(fontListPanel);
    
    c.fill = GridBagConstraints.VERTICAL;
    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = GridBagConstraints.RELATIVE;
    c.gridheight = 1;
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 0.3;
    c.weighty = 1.0;
    _styleList = new NwList(STYLES);
    cpLayout.setConstraints(_styleList , c);
    cp.add(_styleList);
//    JPanel styleListPanel = new JPanel();
//    styleListPanel.setBackground(Color.GREEN);
//    cpLayout.setConstraints(styleListPanel, c);
//    cp.add(styleListPanel);
    
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = 1;
    c.gridx = 2;
    c.gridy = 0;
    c.weightx = 0.3;
    c.weighty = 1.0;
    _sizeList = new NwList(SIZES);
    cpLayout.setConstraints(_sizeList, c);
    cp.add(_sizeList);    

    // sample text
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.WEST;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = 1;
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.0;
    c.weighty = 0.0;
    _sampleText = new JLabel();
    _sampleText.setForeground(Color.black);
    cpLayout.setConstraints(_sampleText, c);
    cp.add(_sampleText);
    
    // buttons
    JPanel bottom = new JPanel();
    bottom.setBorder(new EmptyBorder(5,5,5,5));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
    
    _okButton = new JButton("OK");
    _cancelButton = new JButton("Cancel");
    bottom.add(_okButton);
    bottom.add(_cancelButton);
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
    bottom.add(Box.createHorizontalGlue());

    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.PAGE_END;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = 1;
    c.gridx = 0;
    c.gridy = 2;
    c.weightx = 0.0;
    c.weighty = 0.0;
    cpLayout.setConstraints(bottom, c);
    cp.add(bottom);
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
      }
    });
    
    setSize(425, 400);
  }

  private void showSample() {
    int g = 0;
    try { g = Integer.parseInt(_sizeList.getSelectedValue()); }
    catch (NumberFormatException nfe) { /* do nothing */ }
    String st = _styleList.getSelectedValue();
    int s = Font.PLAIN;
    if (st.equalsIgnoreCase("Bold")) s = Font.BOLD;
    if (st.equalsIgnoreCase("Italic")) s = Font.ITALIC;
    if (st.equalsIgnoreCase("Bold Italic")) s = Font.BOLD | Font.ITALIC;
    _sampleText.setFont(new Font(_fontList.getSelectedValue(), s, g));
    _sampleText.setText("The quick brown fox jumped over the lazy dog.");
    _sampleText.setVerticalAlignment(SwingConstants.TOP);
  }

  /** Returns whether the user clicked OK when the dialog was closed. (If false, the user clicked cancel.) */
  public boolean clickedOK() { return _clickedOK; }

  /** Returns the currently selected Font. */
  public Font getFont() { return _sampleText.getFont(); }

  /** Private inner class for a list which displays a list of options in addition to a label indicating the currently
    * selected item.
    */
  public class NwList extends JPanel {
    JList<String> jl;
    JScrollPane sp;
    JLabel jt;
    String si = " ";

    public NwList(String[] values) {
      GridBagLayout cpLayout = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();
      setLayout(cpLayout);
     
      jl = new JList<String>(values);
      sp = new JScrollPane(jl);
      jt = new JLabel();
      jt.setBackground(Color.white);
      jt.setForeground(Color.black);
      jt.setOpaque(true);
      jt.setBorder(new JTextField().getBorder());
      jt.setFont(getFont());
      jl.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          jt.setText(jl.getSelectedValue());
          si = jl.getSelectedValue();
          showSample();
        }
      });
      
      c.fill = GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.NORTH;
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.gridheight = 1;
      c.gridx = 0;
      c.gridy = 0;
      c.weightx = 1.0;
      c.weighty = 0.0;
      cpLayout.setConstraints(jt, c);
      add(jt);

      c.fill = GridBagConstraints.BOTH;
      c.anchor = GridBagConstraints.NORTH;
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.gridheight = 1;
      c.gridx = 0;
      c.gridy = 1;
      c.weightx = 1.0;
      c.weighty = 1.0;
      cpLayout.setConstraints(sp, c);
      add(sp);
    }

    public String getSelectedValue() { return (si); }

    public void setSelectedItem(String s) { jl.setSelectedValue(s, true);}
  }
}