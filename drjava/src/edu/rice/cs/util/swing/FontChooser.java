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

package edu.rice.cs.util.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * FontChooser, adapted from NwFontChooserS by Noah Wairauch
 *  (see http:///forum.java.sun.com/thread.jsp?forum=57&thread=195067)
 * @version $Id$
 */

public class FontChooser extends JDialog
{
  private final String[] STYLES = new String[]
  {"Plain","Bold","Italic","Bold Italic"};
  private final String[] SIZES = new String[]
  {"3","4","5","6","7","8","9","10","11","12","13","14","15","16","17",
    "18","19","20","22","24","27","30","34","39","45","51","60"};
  private NwList _styleList;
  private NwList _fontList;
  private NwList _sizeList;
  
  private static JLabel _sampleText = new JLabel();
  private boolean _clickedOK = false;
  
  private FontChooser(Frame parent, Font font)
  {
    super (parent,true);
    initAll();
    if (font == null) font = _sampleText.getFont();
    _fontList.setSelectedItem(font.getName());
    _sizeList.setSelectedItem(font.getSize()+"");
    _styleList.setSelectedItem(STYLES[font.getStyle()]);
    this.setResizable(false);
  }
  
  /**
   * Method used to show the font chooser, and select a new font.
   * @param parent The parent frame.
   * @param title The title for this window.
   * @param font The previously chosen font.
   * @return the newly chosen font.
   */
  public static Font showDialog(Frame parent, String title, Font font)
  {
    FontChooser fd = new FontChooser(parent,font);
    fd.setTitle(title);
    fd.setVisible(true); 
    Font chosenFont = null;
    if (fd._clickedOK) {
      chosenFont = _sampleText.getFont();
    }
    fd.dispose(); 
    return(chosenFont); 
  }
  
  public static Font showDialog(Frame parent, Font font) {
    return showDialog(parent, "Font Chooser", font);
  }
  
  private void initAll()
  {
    getContentPane().setLayout(null);
    setBounds(50,50,425,400);
    addLists();
    addButtons();
    _sampleText.setBounds(10,320,415,25);
    _sampleText.setForeground(Color.black);
    getContentPane().add(_sampleText);
    addWindowListener(new WindowAdapter() { 
      public void windowClosing(java.awt.event.WindowEvent e) { 
        setVisible (false);}
    });
  }
  
  private void addLists() 
  {
    _fontList  = new NwList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    _styleList = new NwList(STYLES);
    _sizeList  = new NwList(SIZES);
    _fontList.setBounds(10,10,260,295);
    _styleList.setBounds(280,10,80,295);
    _sizeList.setBounds(370,10,40,295);
    getContentPane().add(_fontList);
    getContentPane().add(_styleList);
    getContentPane().add(_sizeList);
  }
  private void addButtons()
  {
    JButton ok = new JButton("OK");
    ok.setMargin(new Insets(0,0,0,0));
    JButton ca = new JButton("Cancel");
    ca.setMargin(new Insets(0,0,0,0));
    ok.setBounds(260,350,70,20);
    ok.setFont(new Font(" ",1,11));
    ca.setBounds(340,350,70,20);
    ca.setFont(new Font(" ",1,12));
    getContentPane().add(ok);
    getContentPane().add(ca);
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        _clickedOK = true;}
    });
    ca.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        _clickedOK = false;}
    });
  }

  private void showSample() 
  {
    int g = 0;
    try {g = Integer.parseInt(_sizeList.getSelectedValue());} 
    catch(NumberFormatException nfe){}
    String st = _styleList.getSelectedValue();
    int s  = Font.PLAIN;
    if (st.equalsIgnoreCase("Bold"))   s = Font.BOLD;
    if (st.equalsIgnoreCase("Italic")) s = Font.ITALIC;    
    if (st.equalsIgnoreCase("Bold Italic")) s = Font.BOLD | Font.ITALIC;
    _sampleText.setFont(new Font(_fontList.getSelectedValue(),s,g));
    _sampleText.setText("The quick brown fox jumped over the lazy dog.");
  }
  
  /**
   * Private inner class for a list which displays a list of options in addition to a label 
   * indicating the currently selected item.
   */
  public class NwList extends JPanel
  {
    JList       jl;
    JScrollPane sp;
    JLabel      jt;
    String      si = " ";
    
    public NwList(String[] values)
    {
      setLayout(null);
      jl = new JList(values);
      sp = new JScrollPane(jl);
      jt = new JLabel();
      jt.setBackground(Color.white);
      jt.setForeground(Color.black);
      jt.setOpaque(true);
      jt.setBorder(new JTextField().getBorder());
      jt.setFont(getFont());
      jl.setBounds(0,0,100,1000);
      jl.setBackground(Color.white);
      jl.addListSelectionListener(new ListSelectionListener()
                                    { public void valueChanged(ListSelectionEvent e)
        { jt.setText((String)jl.getSelectedValue());
                                      si = (String)jl.getSelectedValue();
                                      showSample();}});
                                      add(sp);
                                      add(jt);
    }
    public String getSelectedValue()
    {
      return(si);
    }
    public void setSelectedItem(String s)
    {
      jl.setSelectedValue(s,true);
    }
    public void setBounds(int x, int y, int w ,int h)
    {
      super.setBounds(x,y,w,h);
      sp.setBounds(0,y+12,w,h-23);
      sp.revalidate();
      jt.setBounds(0,0,w,20);
    } 
    
  }
}