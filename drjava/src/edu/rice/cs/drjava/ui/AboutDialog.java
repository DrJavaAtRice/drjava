/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.util.swing.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.Version;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.*;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/** About dialog.
  * @version $Id$
  */
public class AboutDialog extends JDialog implements ActionListener {

  private static ImageInfo CSLOGO = new ImageInfo("RiceCS.gif",new Color(0x423585)),
    SF = new ImageInfo("SourceForge.gif",Color.black),
    DRJAVA = new ImageInfo("DrJava.png",new Color(0xCCCCFF));

  private final JButton _okButton = new JButton("OK");

  /** the button that copies the system properties to the clipboard */
  private JButton _copyButton;
  
  /** the table with the System Properties information */
  private JTable _propertiesTable;

  /** index the System Properties tab, one of the tabs in _tabs */
  private int _propertiesTabIndex;
  
  /** the pane with tabs to select */
  private final JTabbedPane _tabs = new JTabbedPane();
  
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
  
  public void setVisible(boolean vis) {
    _tabs.remove(0);
    addTab(_tabs,"About",createCopyrightTab(), 0);
    _tabs.setSelectedIndex(0);
    
    if (vis) {
      // suggested from zaq@nosi.com, to keep the frame on the screen!
      //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      //Dimension frameSize = this.getSize();
      Utilities.setPopupLoc(this, getOwner());
    }
    super.setVisible(vis);
  }

  public void buildGUI(Container cp) {
    cp.setLayout(new BorderLayout());
    JLabel drjava = createImageLabel(DRJAVA,JLabel.LEFT);
    if (drjava != null) {
      drjava.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5), drjava.getBorder()));
      drjava.setCursor(new Cursor(Cursor.HAND_CURSOR));
      final String url = "http://drjava.org/";
      drjava.setToolTipText(url);
      drjava.addMouseListener(new MouseListener() {
        public void mousePressed(MouseEvent e) { }        
        public void mouseReleased(MouseEvent e) { }        
        public void mouseEntered(MouseEvent e) { }        
        public void mouseExited(MouseEvent e) { }        
        public void mouseClicked(MouseEvent e) {
          try {
            PlatformFactory.ONLY.openURL(new URL(url));
          } catch(Exception ex) { /* ignore, just not open web page */ }
        }
      });
      
      JPanel djPanel = new JPanel(new GridLayout(1,1));
      djPanel.add(drjava);
      djPanel.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5), new EtchedBorder()));
      cp.add(djPanel,BorderLayout.NORTH);
    }
    addTab(_tabs,"About",createCopyrightTab());
    LICENSE = getLicense();
    if (LICENSE != null) addTab(_tabs,"DrJava License",createTextScroller(LICENSE));
    
    addTab(_tabs,"DynamicJava License",createTextScroller(DYADE_LICENSE));
    addTab(_tabs,"System Properties",createSysPropTab());
    _propertiesTabIndex = _tabs.getTabCount()-1;
    cp.add(createBottomBar(),BorderLayout.SOUTH);
    cp.add(_tabs,BorderLayout.CENTER);
  }

  private JComponent createSysPropTab() {
    Properties props = System.getProperties();
    int size = props.size();
    String[][] rowData = new String[size][2];
    int rowNum = 0;
    for (Map.Entry<Object, Object> entry : props.entrySet()) {
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
    _propertiesTable = new JTable(model);
    JScrollPane scroller = new BorderlessScrollPane(_propertiesTable);
    wrapBorder(scroller,new EmptyBorder(5,0,0,0));
    JPanel propTab = new JPanel(new BorderLayout());
    propTab.add(new JLabel("Current system properties:"),BorderLayout.NORTH);
    propTab.add(scroller,BorderLayout.CENTER);
    return propTab;
  }

  private static void addTab(JTabbedPane tabs, String title, JComponent tab) {
    wrapBorder(tab,new EmptyBorder(5,6,6,5));
    tabs.addTab(title,tab);
  }                        

  private static void addTab(JTabbedPane tabs, String title, JComponent tab, int i) {
    wrapBorder(tab,new EmptyBorder(5,6,6,5));
    tabs.insertTab(title, null, tab, "", i);
  }                        
  
  public static JComponent createCopyrightTab() {
    final JPanel panel = new JPanel(new BorderLayout());

    final StringBuilder sb = new StringBuilder("DrJava Version : ");
    sb.append(Version.getVersionString());
    sb.append("\nDrJava Build Time: ");
    sb.append(Version.getBuildTimeString());
    sb.append("\n\nDrJava Configuration File: ");
    sb.append(DrJava.getPropertiesFile().getAbsolutePath());
    sb.append("\n\nUsed memory: about ");
    sb.append(StringOps.memSizeToString(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()));
    sb.append("\nFree memory: about ");
    sb.append(StringOps.memSizeToString(Runtime.getRuntime().freeMemory()));
    sb.append("\nTotal memory: about ");
    sb.append(StringOps.memSizeToString(Runtime.getRuntime().totalMemory()));
    sb.append("\nTotal memory can expand to: about ");
    sb.append(StringOps.memSizeToString(Runtime.getRuntime().maxMemory()));
    sb.append("\n\n");
    sb.append(COPYRIGHT);
    final JComponent copy = createTextScroller(sb.toString());
    wrapBorder(copy,new EmptyBorder(0,0,5,0));

    // deal with logos now (calibrate size)
    final LogoList logos = new LogoList();
    logos.addLogo(createBorderedLabel(CSLOGO,new EmptyBorder(5,5,5,5)), "http://compsci.rice.edu/");
    logos.addLogo(createBorderedLabel(SF,null), "http://sourceforge.net/projects/drjava/");
    logos.resizeLogos();

    // add to panel
    final JPanel logoPanel = new JPanel();
    logoPanel.setLayout(new BoxLayout(logoPanel,BoxLayout.X_AXIS));
    logoPanel.add(Box.createHorizontalGlue());
    for (JComponent l : logos) {
      logoPanel.add(l);
      l.setCursor(new Cursor(Cursor.HAND_CURSOR));
      final String url = (String)l.getClientProperty("url");
      if (url != null) {
        l.setToolTipText(url);
        l.addMouseListener(new MouseListener() {
          public void mousePressed(MouseEvent e) { }        
          public void mouseReleased(MouseEvent e) { }        
          public void mouseEntered(MouseEvent e) { }        
          public void mouseExited(MouseEvent e) { }        
          public void mouseClicked(MouseEvent e) {
            try { PlatformFactory.ONLY.openURL(new URL(url)); } 
            catch(Exception ex) { /* ignore, just not open web page */ }          
          }
        });
      }
      logoPanel.add(Box.createHorizontalGlue());
    }
    panel.add(logoPanel,BorderLayout.SOUTH);
    panel.add(copy,BorderLayout.CENTER);
    return panel;
  }

  private static class LogoList extends java.util.LinkedList<JPanel> implements Serializable {
    private int width = Integer.MIN_VALUE;
    private int height = Integer.MIN_VALUE;
    private void addLogo(JPanel logo, String url) {
      if (logo != null) {
        Dimension d = logo.getMinimumSize();
        width = Math.max(width,d.width);
        height = Math.max(height,d.height);
        add(logo);
        if (url != null) logo.putClientProperty("url", url);
      }
    }

    private void resizeLogos() {
      Dimension d = new Dimension(width,height);
      for (JComponent i : this) {
        i.setMinimumSize(d);
        i.setMaximumSize(d);
        i.setPreferredSize(d);
      }
    }
  }

  public static JPanel createBorderedLabel(ImageInfo info, EmptyBorder pad) {
    JLabel label = createImageLabel(info,JLabel.CENTER);
    if (label == null) return null;
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
    if (icon == null) return null;
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
    JPanel buttonPanel = new JPanel();
    _copyButton = new JButton(new AbstractAction("Copy System Properties") {
      public void actionPerformed(ActionEvent e) {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection contents = new StringSelection(DrJavaErrorWindow.getSystemAndDrJavaInfo());
        cb.setContents(contents, null);
      }
    });
    _tabs.addChangeListener(new ChangeListener() {
      // This method is called whenever the selected tab changes
      public void stateChanged(ChangeEvent evt) {
        _copyButton.setVisible(_tabs.getSelectedIndex()==_propertiesTabIndex);
      }
    });
    _copyButton.setVisible(_tabs.getSelectedIndex()==_propertiesTabIndex);
    _okButton.addActionListener(this);
    buttonPanel.add(_copyButton);
    buttonPanel.add(_okButton);
    panel.add(buttonPanel,BorderLayout.EAST);
    wrapBorder(panel,new EmptyBorder(5,5,5,5));
    return panel;
  }

  public void actionPerformed(ActionEvent e) {
    setVisible(false);
  }

  public static final String COPYRIGHT =
    "Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)\n"+
    "All rights reserved.\n\n"+
    "Redistribution and use in source and binary forms, with or without"+
    "modification, are permitted provided that the following conditions are met:\n"+
    "* Redistributions of source code must retain the above copyright"+
    "notice, this list of conditions and the following disclaimer.\n"+
    "* Redistributions in binary form must reproduce the above copyright"+
    "notice, this list of conditions and the following disclaimer in the"+
    "documentation and/or other materials provided with the distribution.\n"+
    "* Neither the names of DrJava, the JavaPLT group, Rice University, nor the"+
    "names of its contributors may be used to endorse or promote products"+
    "derived from this software without specific prior written permission.\n\n"+
    "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS"+
    "\"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT"+
    "LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR"+
    "A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR"+
    "CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,"+
    "EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,"+
    "PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR"+
    "PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF"+
    "LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING"+
    "NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS"+
    "SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n\n"+
    "This software is Open Source Initiative approved Open Source Software.\n"+
    "Open Source Initative Approved is a trademark of the Open Source Initiative.\n";
  private static String LICENSE;
  private static boolean initLicense = false;
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
    "Home Page: http://www.drjava.org\nPaper: http://drjava.sf.net/papers/drjava-paper.shtml";

  public static class ImageInfo {
    private final String name;
    private final Color color;
    public ImageInfo(String name, Color color) {
      this.name = name;
      this.color = color;
    }
  }

  public static String getLicense() {
    if (initLicense) return LICENSE;

    try {
      InputStream is = AboutDialog.class.getResourceAsStream("/edu/rice/cs/LICENSE");
      if (is != null) {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        try {
          
          final StringBuilder sb = new StringBuilder();
          for (String s = r.readLine(); s != null; s = r.readLine()) {
            int lastSig = s.length()-1; // the last char index
            
            while (lastSig >= 0 && Character.isWhitespace(s.charAt(lastSig))) lastSig--;
            if (lastSig < 0) sb.append("\n"); // empty line, so insert two newlines.
            else {
              sb.append(s.substring(0,lastSig+1));
              sb.append('\n');
            }
          }
          LICENSE = sb.toString();
          LICENSE = LICENSE.trim();
          if (LICENSE.length() == 0) LICENSE = null;
        }
        finally { 
          is.close();
          r.close();
        }
      }
    }
    catch(Exception e) { throw new UnexpectedException(e, StringOps.getStackTrace(e)); }

    initLicense = true;
    return LICENSE;
  }

  private static void wrapBorder(JComponent c, Border b) {
    c.setBorder(new CompoundBorder(b,c.getBorder()));
  }
}
