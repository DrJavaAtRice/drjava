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

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.MatteBorder;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.awt.*;
import java.awt.print.*;
import java.awt.image.*;
import java.net.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.util.swing.SwingFrame;

/** DrJava's print preview window
  * @version $Id: PreviewFrame.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class PreviewFrame extends SwingFrame {
  
  protected final SingleDisplayModel _model;
  protected final MainFrame _mainFrame;
  protected final Pageable _print;
  protected volatile int _pageNumber;
  
  //zooming modification
  Dimension _screenSize = Toolkit.getDefaultToolkit().getScreenSize();
  JSlider _zoomSlider = new JSlider(JSlider.HORIZONTAL, 100, 300, 100);
  //zooming modification
  private JScrollPane _previewScroll;
  
//  private JTextField _pageTextField = new JTextField("" + (_pageNumber + 1), 2) {
//    public Dimension getMaximumSize() {
//      return getPreferredSize();
//    }
//  };
  
  private final PageChangerUpdater _pageChanger;
  
  private static abstract class PageChangerUpdater {
    abstract void update(int pageNumber) throws Exception;
    abstract JComponent getComponent();
  }
  
  private class JTextFieldChanger extends PageChangerUpdater {
    private final JTextField textfield;
    private JTextFieldChanger(JTextField tf) { textfield = tf; }
    void update(int pageNumber) throws Exception { textfield.setText(String.valueOf(pageNumber)); }
    JComponent getComponent() { return textfield; }
  }
  
  private class JSpinnerChanger extends PageChangerUpdater {
    private volatile JComponent spinner;
    private volatile Method setValueMethod;
    private final Object[] args = new Object[1];
    private JSpinnerChanger(Class<?> spinnerClass, JComponent spinnerObj) throws Exception {
      spinner = spinnerObj;
      setValueMethod = spinnerClass.getMethod("setValue", Object.class);
    }
    void update(int pageNumber) throws Exception {
      args[0] = Integer.valueOf(pageNumber);
      setValueMethod.invoke(spinner, args);
    }
    JComponent getComponent() { return spinner; }
  }
  
  // Print Preview Dimensions
  private final int PREVIEW_WIDTH;
  private final int PREVIEW_HEIGHT;
  private final int PREVIEW_PAGE_WIDTH;
  private final int PREVIEW_PAGE_HEIGHT;
  
  private static final double PAGE_ZOOM = 0.7;
  private static final int PAGE_BORDER = 20;
  private static final int TOOLBAR_HEIGHT = 65;
  private static final String ICON_PATH = "/edu/rice/cs/drjava/ui/icons/";
  
  // Components
  private JToolBar _toolBar;
  private PagePreview _pagePreview;
  
  // Actions
  /** Prints the current document. */
  private final ActionListener _printListener = new ActionListener() {
    public void actionPerformed(ActionEvent ae) {
      _print();
      _close();
    }
  };
  
  /** Prints the current document. */
  private final Action _closeAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent ae) { _close(); }
  };
  
  /** Displays the next page of the document. */
  private final Action _nextPageAction = new AbstractAction("Next Page") {
    public void actionPerformed(ActionEvent ae) { _nextPage(); }
  };
  
  /** Displays the previous page of the document. */
  private final Action _prevPageAction = new AbstractAction("Previous Page") {
    public void actionPerformed(ActionEvent ae) { _previousPage(); }
  };
  
  /** How Preview Pane responds to window events. */
  private final WindowListener _windowCloseListener = new WindowAdapter() {
    public void windowClosing(WindowEvent ev) { _close(); }
  };
  
  /** Contructs a new PreviewFrame using a parent model and a Pageable object print to show. Should only be called in 
    * event thread. 
    */
  public PreviewFrame(SingleDisplayModel model, MainFrame mainFrame, boolean interactions) 
    throws IllegalStateException {
    super("Print Preview");
    mainFrame.hourglassOn();
    _model = model;
    _mainFrame = mainFrame;
    _toolBar = new JToolBar();
    _print = setUpDocument(model, interactions);
    _pageChanger = createPageChanger();
    
    /* Initialize constants. */
    //zooming modification
    PageFormat first = _print.getPageFormat(0);
    
    PREVIEW_PAGE_WIDTH = (int) (PAGE_ZOOM * first.getWidth());
    PREVIEW_PAGE_HEIGHT = (int) (PAGE_ZOOM * first.getHeight());
    
    PREVIEW_WIDTH = PREVIEW_PAGE_WIDTH + (2 * PAGE_BORDER);
    PREVIEW_HEIGHT = PREVIEW_PAGE_HEIGHT + (2 * PAGE_BORDER) + TOOLBAR_HEIGHT;
    
    _setUpActions();
    _setUpToolBar();
    
    _pagePreview = new PagePreview(PREVIEW_PAGE_WIDTH, PREVIEW_PAGE_HEIGHT);
    _pageNumber = 0;
    
    //zooming modification
    //PagePreviewContainer ppc = new PagePreviewContainer();
    //ppc.add(_pagePreview);
    JPanel previewHolder = new JPanel(new BorderLayout());
    JPanel tbCont = new JPanel(new BorderLayout());
    JPanel cp = new JPanel(new BorderLayout());
    //zooming modification
    _previewScroll = new JScrollPane(previewHolder);
    //_previewScroll = new JScrollPane(cp);
    _previewScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    _previewScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    tbCont.add(_toolBar,BorderLayout.NORTH);
    tbCont.add(Box.createVerticalStrut(10),BorderLayout.SOUTH);
    tbCont.setBorder(new EmptyBorder(0,0,5,0));
    
    //zooming modification
    previewHolder.add(_pagePreview, BorderLayout.CENTER);
    cp.add(_previewScroll);
    
    setContentPane(cp);
    //setContentPane(_previewScroll);
    //cp.add(_pagePreview, BorderLayout.CENTER);
    cp.setBorder(new EmptyBorder(5,5,5,5));
    cp.add(tbCont, BorderLayout.NORTH);
    //zooming modification
    //cp.add(ppc, BorderLayout.SOUTH);
    addWindowListener(_windowCloseListener);
    
    showPage();
    _updateActions();
    //zooming modification
//    setExtendedState(Frame.MAXIMIZED_BOTH);
    //setSize(screenSize.width, screenSize.height);
    setSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    initDone(); // call mandated by SwingFrame contract
    
    setVisible(true);
  }
  
  //zooming modification
  public void setZoom(int percent, boolean fast){
    int h = (int)((PREVIEW_PAGE_HEIGHT *  percent) / 100.0);
    int w = (int)((PREVIEW_PAGE_WIDTH *  percent) / 100.0);
    _pagePreview.updateScaled(w, h, fast);
    repaint();
    if (!fast) {
      refreshScreen();
    }
  }
  
  public void refreshScreen() {
    int previewWidth = _pagePreview.getPreferredSize().width + (2 * PAGE_BORDER);
    int previewHeight = _pagePreview.getPreferredSize().height + (2 * PAGE_BORDER) + TOOLBAR_HEIGHT;
    if (previewWidth > _screenSize.width - TOOLBAR_HEIGHT) previewWidth = _screenSize.width - TOOLBAR_HEIGHT; 
    if (previewHeight > _screenSize.height - TOOLBAR_HEIGHT) previewHeight = _screenSize.height - TOOLBAR_HEIGHT; 
    setSize(previewWidth, previewHeight);
  }
  
  /** Prints the document being previewed */
  abstract protected void _print();
  
  /** Sets up the document to be displayed and returns the Pageable object that allows display by pages
    * @param model the current display model
    * @param interactions whether the document is an interactions document
    * @return a Pageable object that allows the document to be displayed by pages
    */
  abstract protected Pageable setUpDocument(SingleDisplayModel model, boolean interactions);
  
  private void _close() {
    dispose();
    _mainFrame.hourglassOff();
  }
  
  private void _nextPage() {
    _pageNumber++;
    _goToPage(_pageNumber);
  }
  
  private void _previousPage() {
    _pageNumber--;
    _goToPage(_pageNumber);
  }
  
  private void _goToPage(int pi) {
    _pageNumber = pi;
    showPage();
    _updateActions();
  }
  
  protected void _showError(Exception e, String title, String message) {
    JOptionPane.showMessageDialog(this, message + "\n" + e, title, JOptionPane.ERROR_MESSAGE);
  }
  
  /** Updates all of the buttons on the page to reflect the current state of the PreviewWindows. Enables/Disables the
    * page buttons, and updates the gotopage field.
    */
  private void _updateActions() {
    _nextPageAction.setEnabled(_print.getNumberOfPages() > (_pageNumber + 1));
    _prevPageAction.setEnabled(_pageNumber > 0);
    try { _pageChanger.update(_pageNumber + 1); }
    catch(Exception e) { /* ignore */ }
  }
  
  /** Initializes all action objects. Adds icons and descriptions to several of the actions. */
  private void _setUpActions() {
    //_printAction.putValue(Action.SHORT_DESCRIPTION, "Print");
    _closeAction.putValue(Action.SHORT_DESCRIPTION, "Close");
    //zooming modification
    //_zoomInAction.putValue(Action.SHORT_DESCRIPTION, "Zoom In");
    //_zoomOutAction.putValue(Action.SHORT_DESCRIPTION, "Zoom Out");
    //_zoomOutAction.setEnabled(false);
    //    _printAction.putValue(Action.SMALL_ICON, _getIcon("Print16.gif"));
    _nextPageAction.putValue(Action.SMALL_ICON, _getIcon("Forward16.gif"));
    _nextPageAction.putValue(Action.SHORT_DESCRIPTION, "Next Page");
    _prevPageAction.putValue(Action.SMALL_ICON, _getIcon("Back16.gif"));
    _prevPageAction.putValue(Action.SHORT_DESCRIPTION, "Previous Page");
  }
  
  private PageChangerUpdater createPageChanger() {
    //_pageTextField.setAction(_goToPageAction);
    // _goToPageAction.putValue(Action.SHORT_DESCRIPTION, "Goto Page");
    try {
      Class<?> spinnerClass = Class.forName("javax.swing.JSpinner");
      final JComponent spinner = (JComponent) spinnerClass.newInstance();
      final Method getter = spinnerClass.getMethod("getValue",new Class<?>[0]);
      Object model = callMethod(spinner, spinnerClass, "getModel",null,null);
      Class<?> modelClass = model.getClass();
      Class<?>[] ca = new Class<?>[] {Comparable.class};
      Object[] aa = new Object[] {Integer.valueOf(1)};
      callMethod(model,modelClass,"setMinimum",ca,aa);
      aa[0] = Integer.valueOf(_print.getNumberOfPages());
      callMethod(model,modelClass,"setMaximum",ca,aa);
      ca[0] = ChangeListener.class;
      aa[0] = new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          int num = _pageNumber;
          try {
            num = ((Number) getter.invoke(spinner,new Object[0])).intValue()-1;
            if ((num >= 0) && (num < _print.getNumberOfPages())) _goToPage(num);
            else _updateActions();
          }
          catch(IllegalAccessException ex) { _updateActions(); }
          catch(InvocationTargetException ex) { _updateActions(); }
        }
      };
      callMethod(spinner, spinnerClass,"addChangeListener",ca,aa);
      return new JSpinnerChanger(spinnerClass, spinner);
    } catch(Exception e) {
      /** Displays the previous page of the document. */
      final JTextField tf = new JTextField();
      tf.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          try {
            int pageToGoTo = Integer.parseInt(tf.getText()) - 1;
            if ((pageToGoTo < 0) || (pageToGoTo >= _print.getNumberOfPages())) { _updateActions(); } 
            else _goToPage(pageToGoTo); 
          } 
          catch (NumberFormatException e) { _updateActions(); }
        }
      });
      return new JTextFieldChanger(tf);
    }
  }
  
  private static Object callMethod(Object rec, Class<?> c, String name, Class<?>[] ca, Object[] args) throws Exception {
    Method m = c.getMethod(name,ca);
    return m.invoke(rec,args);
  }
  
  /** Mirrored from MainFrame, will later use the same Icon access code. */
  private ImageIcon _getIcon(String name) {
    URL url = PreviewFrame.class.getResource(ICON_PATH + name);
    if (url != null) return new ImageIcon(url);
    return null;
  }
  
  /** Sets up the toolbar with all of the necessary buttons. */
  private void _setUpToolBar() {
    _toolBar.setFloatable(false);
    
    // Print and Close buttons
    JButton printButton = new JButton("Print...",_getIcon("Print16.gif"));
    printButton.setToolTipText("Print this document");
    printButton.addActionListener(_printListener);
    _toolBar.add(printButton);
    _toolBar.addSeparator();
    _toolBar.add(_closeAction);
    
    // Horizontal Gap
    _toolBar.add(Box.createHorizontalGlue());
    
    //zooming modification
    //_toolBar.add(_zoomOutAction);
    //_toolBar.add(_zoomInAction);
    //zoomSlider.setMajorTickSpacing(10);
    //zoomSlider.setSnapToTicks(true);
    _zoomSlider.setPaintLabels(true);
    _zoomSlider.setLabelTable(_zoomSlider.createStandardLabels(100));
    
    _zoomSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        JSlider slider = (JSlider) evt.getSource();
        setZoom(slider.getValue(), slider.getValueIsAdjusting());
      }
    });
    _toolBar.add(_zoomSlider);
    
    // Navigation components
    _toolBar.add(_prevPageAction);
    _toolBar.add(_nextPageAction);
    _toolBar.addSeparator();
    
    JLabel gotop = new JLabel("Page");
    
    JLabel of = new JLabel(" of " + _print.getNumberOfPages());
    
    _toolBar.add(gotop);
    _toolBar.addSeparator();
    JComponent c = _pageChanger.getComponent();
    Dimension d = c.getPreferredSize();
    d = new Dimension(100,d.height);
    c.setMaximumSize(d);
    c.setPreferredSize(d);
    c.setMinimumSize(d);
    c.setToolTipText("Goto Page");
    _toolBar.add(c);
    _toolBar.add(of);
  }
  
  /** Generates an Image, prints to it, and then displays the image on the page. */
  private void showPage() {
    BufferedImage img = new BufferedImage((int) _model.getPageFormat().getWidth(),
                                          (int) _model.getPageFormat().getHeight(),
                                          BufferedImage.TYPE_INT_RGB);
    Graphics g = img.getGraphics();
    g.setColor(Color.white);
    g.fillRect(0, 0, (int) _model.getPageFormat().getWidth(), (int) _model.getPageFormat().getHeight());
    
    try {
      _print.getPrintable(_pageNumber).print(g, _model.getPageFormat(), _pageNumber);
      _pagePreview.setImage(img);
    } 
    catch (PrinterException e) { /* ignore */ }
  }
  
  /** Internal class which holds (and places) the PagePreview object. */
  class PagePreviewContainer extends JPanel {
    public Dimension getPreferredSize() { return getParent().getSize(); }
    
    /** Places the PagePreview component into the center of this object */
    public void doLayout() {
      Component cp = getComponent(0);
      
      Dimension dm = cp.getPreferredSize();
      int Hindent = (int) (getPreferredSize().getWidth() - dm.getWidth()) / 2;
      int Vindent = TOOLBAR_HEIGHT + (int) ((getPreferredSize().getHeight() - dm.getHeight() - TOOLBAR_HEIGHT) / 2);
      _pagePreview.setBounds(Hindent, Vindent, (int) dm.getWidth(), (int) dm.getHeight());
    }
  }
  
  /** Static inner class which displays the image on the screen, and holds the Image object. */
  static class PagePreview extends JPanel {
//    protected final int _width;
//    protected final int _height;
    //zooming modification
    //we need to change the variables from final to non-final because we need to enlarge the page preview area when we zoom in
    protected int _width;
    protected int _height;   
    protected volatile Image _source;
    protected volatile Image _image;
    
    /** Constructs a PagePreview object with given width and height. */
    public PagePreview(int width, int height) {
      super();
      _width = width;
      _height = height;
      setBorder(new MatteBorder(1, 1, 2, 2, Color.black));
      setBackground(Color.white);
    }
    
    /** Scales the interal image to the appropriate size. */
    protected void updateScaled() {
      _image = _source.getScaledInstance(_width, _height, Image.SCALE_SMOOTH);
      _image.flush();
    }
    
    //zooming modification
    protected void updateScaled(int newWidth, int newHeight, boolean fast) {
      _width = newWidth;
      _height = newHeight;
      _image = _source.getScaledInstance(newWidth, newHeight, fast?Image.SCALE_FAST:Image.SCALE_SMOOTH);
      _image.flush();
    }    
    
    /** Updates the image of this PagePreview.
      * @param i The Image to place and show.
      */
    public void setImage(Image i) {
      _source = i;
      updateScaled();
      repaint();
    }
    
    //zooming modification
    public int getHeight() {
      return _height;
    }
    public int getWidth() {
      return _width;
    }
    
    public Dimension getPreferredSize() { return new Dimension(_width, _height); }
    
    public Dimension getMaximumSize() { return getPreferredSize(); }
    
    public Dimension getMinimumSize() { return getPreferredSize(); }
    
    public void paint(Graphics g) {
      g.setColor(getBackground());
      g.fillRect(0, 0, _width, _height);
      g.drawImage(_image, 0, 0, this);
      paintBorder(g);
    }
  }
}
