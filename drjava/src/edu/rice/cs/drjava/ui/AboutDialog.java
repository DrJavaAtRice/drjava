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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.util.swing.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.Version;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;


import java.net.URL;
import java.io.*;
import java.util.Hashtable;
import java.util.Map;
/**
 * About dialog.
 *
 * @version $Id$
 */
public class AboutDialog extends JDialog implements ActionListener {

  private static ImageInfo CSLOGO = new ImageInfo("RiceCS.gif",new Color(0x423585)),
    SF = new ImageInfo("SourceForge.gif",Color.black),
    DRJAVA = new ImageInfo("DrJava.png",new Color(0xCCCCFF));

  private final JButton _okButton = new JButton("OK");

  public AboutDialog(JFrame owner) {
    super(owner, "About DrJava", true); // (changed to non-modal for now)

    buildGUI(getContentPane());
    getRootPane().setDefaultButton(_okButton);
    // pack();
    // setSize((int) (.8f*owner.getWidth()),(int) (.8f*owner.getHeight()));
    setSize(550, 400);
    // suggested from zaq@nosi.com, to keep the frame on the screen!
    //System.out.println("Dialog created...");
  }

  public void show() {
    // suggested from zaq@nosi.com, to keep the frame on the screen!
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = this.getSize();
    this.setLocation((screenSize.width - frameSize.width) / 2,
                     (screenSize.height - frameSize.height) / 2);
    super.show();
  }

  public void buildGUI(Container cp) {
    cp.setLayout(new BorderLayout());
    JLabel drjava = createImageLabel(DRJAVA,JLabel.LEFT);
    if (drjava != null) {
      drjava.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5),
                                          drjava.getBorder()));
      JPanel djPanel = new JPanel(new GridLayout(1,1));
      djPanel.add(drjava);
      djPanel.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5),
                                           new EtchedBorder()));
      cp.add(djPanel,BorderLayout.NORTH);
    }
    JTabbedPane tabs = new JTabbedPane();
    addTab(tabs,"About",createCopyrightTab());
    String gpl = getGPL();
    if(gpl!=null)
      addTab(tabs,"GNU Public License",createTextScroller(gpl));
    addTab(tabs,"DynamicJava License",createTextScroller(DYADE_LICENSE));
    addTab(tabs,"System Properties",createSysPropTab());
    cp.add(createBottomBar(),BorderLayout.SOUTH);
    cp.add(tabs,BorderLayout.CENTER);
  }

  private static JComponent createSysPropTab() {
    java.util.Properties props = System.getProperties();
    int size = props.size();
    String[][] rowData = new String[size][2];
    java.util.Iterator entries = props.entrySet().iterator();
    int rowNum = 0;
    while(entries.hasNext()) {
      Map.Entry entry = (Map.Entry) entries.next();
      rowData[rowNum][0] = (String) entry.getKey();
      rowData[rowNum][1] = (String) entry.getValue();
      rowNum++;
    }
    java.util.Arrays.sort(rowData,new java.util.Comparator<String[]>() {
      public int compare(String[] o1, String[] o2) {
        return o1[0].compareTo(o2[0]);
      }
    });
    String[] nvStrings = new String[]{"Name","Value"};
    UneditableTableModel model = new UneditableTableModel(rowData, nvStrings);
    JTable table = new JTable(model);
    JScrollPane scroller = new BorderlessScrollPane(table);
    wrapBorder(scroller,new EmptyBorder(5,0,0,0));
    JPanel propTab = new JPanel(new BorderLayout());
    propTab.add(new JLabel("Current system properties:"),BorderLayout.NORTH);
    propTab.add(scroller,BorderLayout.CENTER);
    return propTab;
  }

  private static void addTab(JTabbedPane tabs, String title,
                             JComponent tab) {
    wrapBorder(tab,new EmptyBorder(5,6,6,5));
    tabs.addTab(title,tab);
  }

  public static JComponent createCopyrightTab() {
    JPanel panel = new JPanel(new BorderLayout());

    StringBuffer sb = new StringBuffer("DrJava Version : ");
    sb.append(Version.getBuildTimeString());
    sb.append("\n\nDrJava Configuration file: ");
    sb.append(DrJava.getPropertiesFile().getAbsolutePath());
    sb.append("\n\n");
    sb.append(COPYRIGHT);
    JComponent copy = createTextScroller(sb.toString());
    wrapBorder(copy,new EmptyBorder(0,0,5,0));

    // deal with logos now (calibrate size)
    LogoList logos = new LogoList();
    logos.addLogo(createBorderedLabel(CSLOGO,new EmptyBorder(5,5,5,5)));
    logos.addLogo(createBorderedLabel(SF,null));
    logos.resizeLogos();

    // add to panel
    JPanel logoPanel = new JPanel();
    logoPanel.setLayout(new BoxLayout(logoPanel,BoxLayout.X_AXIS));
    logoPanel.add(Box.createHorizontalGlue());
    java.util.Iterator it = logos.iterator();
    while(it.hasNext()) {
      logoPanel.add((JComponent) it.next());
      logoPanel.add(Box.createHorizontalGlue());
    }
    panel.add(logoPanel,BorderLayout.SOUTH);
    panel.add(copy,BorderLayout.CENTER);
    return panel;
  }

  private static class LogoList extends java.util.LinkedList<JPanel> {
    private int width = Integer.MIN_VALUE;
    private int height = Integer.MIN_VALUE;
    private void addLogo(JPanel logo) {
      if(logo == null) return;
      Dimension d = logo.getMinimumSize();
      width = Math.max(width,d.width);
      height = Math.max(height,d.height);
      add(logo);
    }

    private void resizeLogos() {
      java.util.Iterator it = iterator();
      Dimension d = new Dimension(width,height);
      while(it.hasNext()) {
        JComponent i = (JComponent) it.next();
        i.setMinimumSize(d);
        i.setMaximumSize(d);
        i.setPreferredSize(d);
      }
    }
  }

  public static JPanel createBorderedLabel(ImageInfo info,
                                           EmptyBorder pad) {
    JLabel label = createImageLabel(info,JLabel.CENTER);
    if(label == null) return null;
    JPanel panel = new JPanel(new GridLayout(1,1));
    panel.setOpaque(true);
    panel.setBackground(info.color);
    panel.setBorder(pad);
    wrapBorder(panel,new EtchedBorder());
    panel.add(label);
    return panel;
  }

  public static JLabel createImageLabel(ImageInfo info, int align) {
    ImageIcon icon = MainFrame.getIcon(info.name);
    if(icon==null) return null;
    JLabel label = new JLabel(icon,align);
    label.setOpaque(true);
    label.setBackground(info.color);
    return label;
  }

  public static JTextArea createTextArea(String text) {
    JTextArea textArea = new JTextArea(text);
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setCaretPosition(0);
    return textArea;
  }

  public static JScrollPane createTextScroller(String text) {
    return new BorderlessScrollPane(createTextArea(text));
  }

  private JPanel createBottomBar() {
    JPanel panel = new JPanel(new BorderLayout());
    _okButton.addActionListener(this);
    panel.add(_okButton,BorderLayout.EAST);
    wrapBorder(panel,new EmptyBorder(5,5,5,5));
    return panel;
  }

  public void actionPerformed(ActionEvent e) {
    hide();
  }

  public static final String COPYRIGHT = "Copyright \u00a9 2001-2002 JavaPLT group"+
    " at Rice University (javaplt@rice.edu)\n\nSee http://drjava.sourceforge.net for"+
    " more information on DrJava or to obtain the latest version of the program or its source code.\n\n"+
    "DrJava is free software; you can redistribute it and/or modify it under the terms"+
    " of the GNU General Public License as published by the Free Software Foundation;"+
    " either version 2 of the License, or (at your option) any later version.\n\n"+
    "DrJava is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;"+
    " without even the implied warranty of MERCHANTABILITY or FITNESS FOR A"+
    " PARTICULAR PURPOSE.  See the GNU General Public License for more details.";
  private static String GPL;
  private static boolean initGPL = false;
  public static final String DYADE_LICENSE =
    "DynamicJava - Copyright \u00a9 1999 Dyade\n\nPermission is hereby granted,"+
    " free of charge, to any person obtaining a copy of this software and associated"+
    " documentation files (the \"Software\"), to deal in the Software without restriction,"+
    " including without limitation the rights to use, copy, modify, merge, publish, distribute,"+
    " sublicense, and/or sell copies of the Software, and to permit persons to whom the Software"+
    " is furnished to do so, subject to the following conditions:\n\n"+
    "The above copyright notice and this permission notice shall be included in all copies or"+
    " substantial portions of the Software.\n\nTHE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY"+
    " OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,"+
    " FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM,"+
    " DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT"+
    " OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.\n\n"+
    "Except as contained in this notice, the name of Dyade shall not be used in advertising or otherwise"+
    " to promote the sale, use or other dealings in this Software without prior written authorization from Dyade.";
  public static final String INTRODUCTION =
    "DrJava is a pedagogic programming environment for Java, intended to help students focus more on program"+
    " design than on the features of a complicated development environment. It provides an Interactions"+
    " window based on a \"read-eval-print loop\", which allows programmers to develop, test, and debug"+
    " Java programs in an interactive, incremental fashion.\n\n"+
    "Home Page: http://drjava.sourceforge.net\nPaper: http://drjava.sf.net/papers/drjava-paper.shtml";

  public static class ImageInfo {
    private final String name;
    private final Color color;
    public ImageInfo(String name, Color color) {
      this.name = name;
      this.color = color;
    }
  }

  public static String getGPL() {
    if(initGPL) return GPL;

    try {
      InputStream is = AboutDialog.class.getResourceAsStream("/edu/rice/cs/LICENSE");
      if(is!=null) {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        for(String s = r.readLine(); s != null; s = r.readLine()) {
          int lastSig = s.length()-1; // the last char index

          while(lastSig >= 0 && Character.isWhitespace(s.charAt(lastSig))) {
            lastSig--;
          }
          if(lastSig<0) {
            sb.append("\n\n"); // empty line, so insert two newlines.
          } else {
            sb.append(' ');
            sb.append(s.substring(0,lastSig+1));
          }
        }
        GPL = sb.toString();
        GPL = GPL.trim();
        if(GPL.length() == 0) GPL = null;
      }
    }
    catch(Exception e) {
      GPL = null;
    }

    initGPL = true;
    return GPL;
  }

  private static void wrapBorder(JComponent c, Border b) {
    c.setBorder(new CompoundBorder(b,c.getBorder()));
  }
}
