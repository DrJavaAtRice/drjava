/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.util.swing.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.Version;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.FileConfiguration;
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
  * @version $Id: AboutDialog.java 5711 2012-09-11 19:42:33Z rcartwright $
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
    super(owner, "About DrScala", true); // (changed to non-modal for now)

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
    JLabel drjava = /* createImageLabel(DRJAVA,JLabel.LEFT) */ new JLabel("DrScala");
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
    if (LICENSE != null) addTab(_tabs,"DrScala License",createTextScroller(LICENSE));
    
    addTab(_tabs,"Scala License",createTextScroller(SCALA_LICENSE));
    addTab(_tabs,"DynamicJava License",createTextScroller(DYADE_LICENSE));
    addTab(_tabs,"Eclipse License",createTextScroller(ECLIPSE_LICENSE));
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

    final StringBuilder sb = new StringBuilder("DrScala Version : ");
    sb.append(Version.getVersionString());
    FileConfiguration config = DrJava.getConfig();
    if (config!=null) {
      String customDrJavaJarVersionSuffix = config.getSetting(OptionConstants.CUSTOM_DRJAVA_JAR_VERSION_SUFFIX);
      if (customDrJavaJarVersionSuffix.length()>0)  {
        sb.append(" with ");
        sb.append(customDrJavaJarVersionSuffix);
      }
    }
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
    _copyButton.setToolTipText("Copy information about your computer into the clipboard so it can be pasted.");
    // button always visible
//    _tabs.addChangeListener(new ChangeListener() {
//      // This method is called whenever the selected tab changes
//      public void stateChanged(ChangeEvent evt) {
//        _copyButton.setVisible(_tabs.getSelectedIndex() == _propertiesTabIndex);
//      }
//    });
//    _copyButton.setVisible(_tabs.getSelectedIndex() == _propertiesTabIndex);
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
    "Copyright (c) 2001-2012, JavaPLT group at Rice University (javaplt@rice.edu)\n" + 
    "All rights reserved.\n\n" + 
    "Redistribution and use in source and binary forms, with or without " + 
    "modification, are permitted provided that the following conditions are met:\n" + 
    "* Redistributions of source code must retain the above copyright " + 
    "notice, this list of conditions and the following disclaimer.\n" + 
    "* Redistributions in binary form must reproduce the above copyright " + 
    "notice, this list of conditions and the following disclaimer in the " + 
    "documentation and/or other materials provided with the distribution.\n" + 
    "* Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the " + 
    "names of its contributors may be used to endorse or promote products " + 
    "derived from this software without specific prior written permission.\n\n" + 
    "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS " + 
    "\"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT " + 
    "LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR " + 
    "A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR " + 
    "CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, " + 
    "EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, " + 
    "PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR " + 
    "PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF " + 
    "LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING " + 
    "NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS " + 
    "SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n\n" + 
    "This software is Open Source Initiative approved Open Source Software.\n" + 
    "Open Source Initative Approved is a trademark of the Open Source Initiative.\n";
  
  private static String LICENSE;
  private static boolean initLicense = false;
  
  public static final String SCALA_LICENSE =
    "SCALA LICENSE\n\n" +
    "Copyright (c) 2002-2011 EPFL, Lausanne, unless otherwise specified.\n" +
    "All rights reserved.\n\n" +
    "This software was developed by the Programming Methods Laboratory of the\n" +
    "Swiss Federal Institute of Technology (EPFL), Lausanne, Switzerland.\n\n" +
    "Permission to use, copy, modify, and distribute this software in source\n" +
    "or binary form for any purpose with or without fee is hereby granted,\n" +
    "provided that the following conditions are met:\n\n" +
    "   1. Redistributions of source code must retain the above copyright\n" +
    "      notice, this list of conditions and the following disclaimer.\n\n" +
    "   2. Redistributions in binary form must reproduce the above copyright\n" +
    "      notice, this list of conditions and the following disclaimer in the\n" +
    "      documentation and/or other materials provided with the distribution.\n" +
    
    "   3. Neither the name of the EPFL nor the names of its contributors\n" +
    "      may be used to endorse or promote products derived from this\n" +
    "      software without specific prior written permission.\n\n\n" +
    "THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND\n" +
    "ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE\n" +
    "IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE\n" +
    "ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE\n" +
    "FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL\n" +
    "DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR\n" +
    "SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER\n" +
    "CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT\n" +
    "LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY\n" +
    "OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF\n" +
    "SUCH DAMAGE.";

  public static final String DYADE_LICENSE =
    "DynamicJava - Copyright \u00a9 1999 Dyade\n\nPermission is hereby granted," + 
    " free of charge, to any person obtaining a copy of this software and associated" + 
    " documentation files (the \"Software\"), to deal in the Software without restriction," + 
    " including without limitation the rights to use, copy, modify, merge, publish, distribute," + 
    " sublicense, and/or sell copies of the Software, and to permit persons to whom the Software" + 
    " is furnished to do so, subject to the following conditions:\n\n" + 
    "The above copyright notice and this permission notice shall be included in all copies or" + 
    " substantial portions of the Software.\n\nTHE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY" + 
    " OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY," + 
    " FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM," + 
    " DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT" + 
    " OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.\n\n" + 
    "Except as contained in this notice, the name of Dyade shall not be used in advertising or otherwise" + 
    " to promote the sale, use or other dealings in this Software without prior written authorization from Dyade.";
  
  public static final String ECLIPSE_LICENSE =
    "Eclipse Public License - v 1.0\n"+
    "\n" +
    "THE ACCOMPANYING PROGRAM IS PROVIDED UNDER THE TERMS OF THIS ECLIPSE " +
    "PUBLIC LICENSE (\"AGREEMENT\"). ANY USE, REPRODUCTION OR DISTRIBUTION " +
    "OF THE PROGRAM CONSTITUTES RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT.\n" +
    "\n" +
    "1. DEFINITIONS\n" +
    "\n" +
    "\"Contribution\" means:\n" +
    "\n" +
    "a) in the case of the initial Contributor, the initial code and documentation distributed " +
    "under this Agreement, and\n" +
    "\n" +
    "b) in the case of each subsequent Contributor:\n" +
    "\n" +
    "i) changes to the Program, and\n" +
    "\n" +
    "ii) additions to the Program;\n" +
    "\n" +
    "where such changes and/or additions to the Program originate from and are distributed by " +
    "that particular Contributor. A Contribution 'originates' from a Contributor if it was added " +
    "to the Program by such Contributor itself or anyone acting on such Contributor's behalf. " +
    "Contributions do not include additions to the Program which: (i) are separate modules of " +
    "software distributed in conjunction with the Program under their own license agreement, " +
    "and (ii) are not derivative works of the Program.\n" +
    "\n" +
    "\"Contributor\" means any person or entity that distributes the Program.\n" +
    "\n" +
    "\"Licensed Patents\" mean patent claims licensable by a Contributor which are necessarily " +
    "infringed by the use or sale of its Contribution alone or when combined with the Program.\n" +
    "\n" +
    "\"Program\" means the Contributions distributed in accordance with this Agreement.\n" +
    "\n" +
    "\"Recipient\" means anyone who receives the Program under this Agreement, including all " +
    "Contributors.\n" +
    "\n" +
    "2. GRANT OF RIGHTS\n" +
    "\n" +
    "a) Subject to the terms of this Agreement, each Contributor hereby grants Recipient a " +
    "non-exclusive, worldwide, royalty-free copyright license to reproduce, prepare " +
    "derivative works of, publicly display, publicly perform, distribute and sublicense " +
    "the Contribution of such Contributor, if any, and such derivative works, in source " +
    "code and object code form.\n" +
    "\n" +
    "b) Subject to the terms of this Agreement, each Contributor hereby grants Recipient a " +
    "non-exclusive, worldwide, royalty-free patent license under Licensed Patents to make, " +
    "use, sell, offer to sell, import and otherwise transfer the Contribution of such " +
    "Contributor, if any, in source code and object code form. This patent license shall " +
    "apply to the combination of the Contribution and the Program if, at the time the " +
    "Contribution is added by the Contributor, such addition of the Contribution causes " +
    "such combination to be covered by the Licensed Patents. The patent license shall not " +
    "apply to any other combinations which include the Contribution. No hardware per se " +
    "is licensed hereunder.\n" +
    "\n" +
    "c) Recipient understands that although each Contributor grants the licenses to its " +
    "Contributions set forth herein, no assurances are provided by any Contributor that " +
    "the Program does not infringe the patent or other intellectual property rights of " +
    "any other entity. Each Contributor disclaims any liability to Recipient for claims " +
    "brought by any other entity based on infringement of intellectual property rights " +
    "or otherwise. As a condition to exercising the rights and licenses granted hereunder, " +
    "each Recipient hereby assumes sole responsibility to secure any other intellectual " +
    "property rights needed, if any. For example, if a third party patent license is " +
    "required to allow Recipient to distribute the Program, it is Recipient's responsibility " +
    "to acquire that license before distributing the Program.\n" +
    "\n" +
    "d) Each Contributor represents that to its knowledge it has sufficient copyright " +
    "rights in its Contribution, if any, to grant the copyright license set forth in " +
    "this Agreement.\n" +
    "\n" +
    "3. REQUIREMENTS\n" +
    "\n" +
    "A Contributor may choose to distribute the Program in object code form under its " +
    "own license agreement, provided that:\n" +
    "\n" +
    "a) it complies with the terms and conditions of this Agreement; and\n" +
    "\n" +
    "b) its license agreement:\n" +
    "\n" +
    "i) effectively disclaims on behalf of all Contributors all warranties and conditions, " +
    "express and implied, including warranties or conditions of title and non-infringement, " +
    "and implied warranties or conditions of merchantability and fitness for a particular " +
    "purpose;\n" +
    "\n" +
    "ii) effectively excludes on behalf of all Contributors all liability for damages, " +
    "including direct, indirect, special, incidental and consequential damages, such as " +
    "lost profits;\n" +
    "\n" +
    "iii) states that any provisions which differ from this Agreement are offered by " +
    "that Contributor alone and not by any other party; and\n" +
    "\n" +
    "iv) states that source code for the Program is available from such Contributor, and " +
    "informs licensees how to obtain it in a reasonable manner on or through a medium " +
    "customarily used for software exchange.\n" +
    "\n" +
    "When the Program is made available in source code form:\n" +
    "\n" +
    "a) it must be made available under this Agreement; and\n" +
    "\n" +
    "b) a copy of this Agreement must be included with each copy of the Program.\n" +
    "\n" +
    "Contributors may not remove or alter any copyright notices contained within the " +
    "Program.\n" +
    "\n" +
    "Each Contributor must identify itself as the originator of its Contribution, if " +
    "any, in a manner that reasonably allows subsequent Recipients to identify the " +
    "originator of the Contribution.\n" +
    "\n" +
    "4. COMMERCIAL DISTRIBUTION\n" +
    "\n" +
    "Commercial distributors of software may accept certain responsibilities with " +
    "respect to end users, business partners and the like. While this license is intended " +
    "to facilitate the commercial use of the Program, the Contributor who includes the " +
    "Program in a commercial product offering should do so in a manner which does not " +
    "create potential liability for other Contributors. Therefore, if a Contributor " +
    "includes the Program in a commercial product offering, such Contributor " +
    "(\"Commercial Contributor\") hereby agrees to defend and indemnify every other " +
    "Contributor (\"Indemnified Contributor\") against any losses, damages and costs " +
    "(collectively \"Losses\") arising from claims, lawsuits and other legal actions " +
    "brought by a third party against the Indemnified Contributor to the extent caused " +
    "by the acts or omissions of such Commercial Contributor in connection with its " +
    "distribution of the Program in a commercial product offering. The obligations in " +
    "this section do not apply to any claims or Losses relating to any actual or " +
    "alleged intellectual property infringement. In order to qualify, an Indemnified " +
    "Contributor must: a) promptly notify the Commercial Contributor in writing of " +
    "such claim, and b) allow the Commercial Contributor to control, and cooperate with " +
    "the Commercial Contributor in, the defense and any related settlement negotiations. " +
    "The Indemnified Contributor may participate in any such claim at its own expense.\n" +
    "\n" +
    "For example, a Contributor might include the Program in a commercial product " +
    "offering, Product X. That Contributor is then a Commercial Contributor. If that " +
    "Commercial Contributor then makes performance claims, or offers warranties related " +
    "to Product X, those performance claims and warranties are such Commercial " +
    "Contributor's responsibility alone. Under this section, the Commercial Contributor " +
    "would have to defend claims against the other Contributors related to those " +
    "performance claims and warranties, and if a court requires any other Contributor " +
    "to pay any damages as a result, the Commercial Contributor must pay those damages.\n" +
    "\n" +
    "5. NO WARRANTY\n" +
    "\n" +
    "EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVIDED ON AN " +
    "\"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR " +
    "IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR CONDITIONS OF TITLE, " +
    "NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Each " +
    "Recipient is solely responsible for determining the appropriateness of using and " +
    "distributing the Program and assumes all risks associated with its exercise of " +
    "rights under this Agreement , including but not limited to the risks and costs of " +
    "program errors, compliance with applicable laws, damage to or loss of data, " +
    "programs or equipment, and unavailability or interruption of operations.\n" +
    "\n" +
    "6. DISCLAIMER OF LIABILITY\n" +
    "\n" +
    "EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT NOR ANY " +
    "CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL, " +
    "SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION LOST " +
    "PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, " +
    "STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY " +
    "OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE EXERCISE OF ANY RIGHTS " +
    "GRANTED HEREUNDER, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.\n" +
    "\n" +
    "7. GENERAL\n" +
    "\n" +
    "If any provision of this Agreement is invalid or unenforceable under applicable " +
    "law, it shall not affect the validity or enforceability of the remainder of the " +
    "terms of this Agreement, and without further action by the parties hereto, such " +
    "provision shall be reformed to the minimum extent necessary to make such " +
    "provision valid and enforceable.\n" +
    "\n" +
    "If Recipient institutes patent litigation against any entity (including a " +
    "cross-claim or counterclaim in a lawsuit) alleging that the Program itself " +
    "(excluding combinations of the Program with other software or hardware) " +
    "infringes such Recipient's patent(s), then such Recipient's rights granted " +
    "under Section 2(b) shall terminate as of the date such litigation is filed.\n" +
    "\n" +
    "All Recipient's rights under this Agreement shall terminate if it fails to " +
    "comply with any of the material terms or conditions of this Agreement and does " +
    "not cure such failure in a reasonable period of time after becoming aware of " +
    "such noncompliance. If all Recipient's rights under this Agreement terminate, " +
    "Recipient agrees to cease use and distribution of the Program as soon as " +
    "reasonably practicable. However, Recipient's obligations under this Agreement " +
    "and any licenses granted by Recipient relating to the Program shall continue " +
    "and survive.\n" +
    "\n" +
    "Everyone is permitted to copy and distribute copies of this Agreement, but " +
    "in order to avoid inconsistency the Agreement is copyrighted and may only be " +
    "modified in the following manner. The Agreement Steward reserves the right to " +
    "publish new versions (including revisions) of this Agreement from time to " +
    "time. No one other than the Agreement Steward has the right to modify this " +
    "Agreement. The Eclipse Foundation is the initial Agreement Steward. The " +
    "Eclipse Foundation may assign the responsibility to serve as the Agreement " +
    "Steward to a suitable separate entity. Each new version of the Agreement " +
    "will be given a distinguishing version number. The Program (including " +
    "Contributions) may always be distributed subject to the version of the " +
    "Agreement under which it was received. In addition, after a new version of " +
    "the Agreement is published, Contributor may elect to distribute the Program " +
    "(including its Contributions) under the new version. Except as expressly " +
    "stated in Sections 2(a) and 2(b) above, Recipient receives no rights or " +
    "licenses to the intellectual property of any Contributor under this Agreement, " +
    "whether expressly, by implication, estoppel or otherwise. All rights in the " +
    "Program not expressly granted under this Agreement are reserved.\n" +
    "\n" +
    "This Agreement is governed by the laws of the State of New York and the " +
    "intellectual property laws of the United States of America. No party to this " +
    "Agreement will bring a legal action under this Agreement more than one year " +
    "after the cause of action arose. Each party waives its rights to a jury trial " +
    "in any resulting litigation.";

  public static final String INTRODUCTION =
    "DrScala is a pedagogic programming environment for Scala, intended to help students focus more on program" + 
    " design than on the features of a complicated development environment. It provides an Interactions" + 
    " window based on a \"read-eval-print loop\", which allows programmers to develop, test, and debug" + 
    " Java programs in an interactive, incremental fashion.\n\n" + 
    "Home Page: http://www.drscala.org\nPaper: http://drjava.sf.net/papers/drjava-paper.shtml";

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
