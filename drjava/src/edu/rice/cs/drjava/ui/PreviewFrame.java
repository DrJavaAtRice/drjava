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
import java.lang.reflect.Method;
import edu.rice.cs.drjava.model.*;

/** DrJava's print preview window
 *  @version $Id$
 */
public abstract class PreviewFrame extends JFrame {

  protected SingleDisplayModel _model;
  protected MainFrame _mainFrame;
  protected Pageable _print;
  protected int _pageNumber;

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
    private JTextFieldChanger(JTextField tf) {
      textfield = tf;
    }
    void update(int pageNumber) throws Exception {
      textfield.setText(String.valueOf(pageNumber));
    }
    JComponent getComponent() { return textfield; }
  }

  private class JSpinnerChanger extends PageChangerUpdater {
    private final JComponent spinner;
    private final Method setValueMethod;
    private final Object[] args = new Object[1];
    private JSpinnerChanger(Class spinnerClass, JComponent spinnerObj)
      throws Exception {
      spinner = spinnerObj;
      setValueMethod = spinnerClass.getMethod("setValue", Object.class);
    }
    void update(int pageNumber) throws Exception {
      args[0] = new Integer(pageNumber);
      setValueMethod.invoke(spinner,args);
    }
    JComponent getComponent() { return spinner; }
  }

  // Print Preview Dimensions
  private int PREVIEW_WIDTH;
  private int PREVIEW_HEIGHT;
  private int PREVIEW_PAGE_WIDTH;
  private int PREVIEW_PAGE_HEIGHT;
  private double PAGE_ZOOM = 0.7;
  private static int PAGE_BORDER = 20;
  private int TOOLBAR_HEIGHT = 35;
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
  private Action _closeAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent ae) {
      _close();
    }
  };

  /** Displays the next page of the document. */
  private Action _nextPageAction = new AbstractAction("Next Page") {
    public void actionPerformed(ActionEvent ae) {
      _nextPage();
    }
  };

  /** Displays the previous page of the document. */
  private Action _prevPageAction = new AbstractAction("Previous Page") {
    public void actionPerformed(ActionEvent ae) {
      _previousPage();
    }
  };

  /** How Preview Pane responds to window events. */
  private WindowListener _windowCloseListener = new WindowAdapter() {
    public void windowClosing(WindowEvent ev) {
      _close();
    }
  };

  /**
   * Contructs a new PreviewFrame using a parent model and
   * a Pageable object print to show.
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

    _setUpActions();
    _setUpToolBar();
    _setUpConstants();

    _pagePreview = new PagePreview(PREVIEW_PAGE_WIDTH, PREVIEW_PAGE_HEIGHT);
    _pageNumber = 0;


    PagePreviewContainer ppc = new PagePreviewContainer();
    ppc.add(_pagePreview);
    JPanel tbCont = new JPanel(new BorderLayout());
    JPanel cp = new JPanel(new BorderLayout());
    tbCont.add(_toolBar,BorderLayout.NORTH);
    tbCont.add(Box.createVerticalStrut(10),BorderLayout.SOUTH);
    tbCont.setBorder(new EmptyBorder(0,0,5,0));
    setContentPane(cp);
    cp.setBorder(new EmptyBorder(5,5,5,5));
    cp.add(tbCont, BorderLayout.NORTH);
    cp.add(ppc, BorderLayout.SOUTH);

    addWindowListener(_windowCloseListener);

    showPage();
    _updateActions();

    setSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setVisible(true);
  }

  /** Prints the document being previewed */
  abstract protected void _print();
  
   /** Sets up the document to be displayed and returns the Pageable object that allows display by pages
   * 
   *  @param model the current display model
   *  @param interactions whether the document is an interactions document
   *  
   *  @return a Pageable object that allows the document to be displayed by pages
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
    JOptionPane.showMessageDialog(this,
                                  message + "\n" + e,
                                  title,
                                  JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Updates all of the buttons on the page to
   * reflect the current state of the PreviewWindows.
   * Enables/Disables the page buttons, and updates the
   * gotopage field.
   */
  private void _updateActions() {
    _nextPageAction.setEnabled(_print.getNumberOfPages() > (_pageNumber + 1));
    _prevPageAction.setEnabled(_pageNumber > 0);
    try {
      _pageChanger.update(_pageNumber + 1);
    } catch(Exception e) {
    }
  }


  /**
   * Initializes all of the constants for the
   * page size, etc...
   */
  private void _setUpConstants() {
    PageFormat first = _print.getPageFormat(0);

    PREVIEW_PAGE_WIDTH = (int) (PAGE_ZOOM * first.getWidth());
    PREVIEW_PAGE_HEIGHT = (int) (PAGE_ZOOM * first.getHeight());

    PREVIEW_WIDTH = PREVIEW_PAGE_WIDTH + (2 * PAGE_BORDER);
    PREVIEW_HEIGHT = PREVIEW_PAGE_HEIGHT + (2 * PAGE_BORDER) + TOOLBAR_HEIGHT;
  }

  /**
   * Initializes all action objects.
   * Adds icons and descriptions to several of the actions.
   */
  private void _setUpActions() {
    //_printAction.putValue(Action.SHORT_DESCRIPTION, "Print");
    _closeAction.putValue(Action.SHORT_DESCRIPTION, "Close");
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
      final Method getter = spinnerClass.getMethod("getValue",new Class[0]);
      Object model = callMethod(spinner,spinnerClass,"getModel",null,null);
      Class<?> modelClass = model.getClass();
      Class<?>[] ca = new Class<?>[] {Comparable.class};
      Object[] aa = new Object[] {new Integer(1)};
      callMethod(model,modelClass,"setMinimum",ca,aa);
      aa[0] = new Integer(_print.getNumberOfPages());
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
          catch(Exception ex) { _updateActions(); }
        }
      };
      callMethod(spinner,spinnerClass,"addChangeListener",ca,aa);
      return new JSpinnerChanger(spinnerClass,spinner);
    } catch(Exception e) {
      /** Displays the previous page of the document. */
      final JTextField tf = new JTextField();
      tf.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          try {
            int pageToGoTo = Integer.parseInt(tf.getText()) - 1;

            if ((pageToGoTo < 0) || (pageToGoTo >= _print.getNumberOfPages())) {
              _updateActions();
            } else {
              _goToPage(pageToGoTo);
            }
          } catch (NumberFormatException e) {
            _updateActions();
          }
        }
      });
      return new JTextFieldChanger(tf);
    }
  }

  private static Object callMethod(Object rec, Class<?> c, String name,
                                   Class<?>[] ca,
                                   Object[] args) throws Exception {
    Method m = c.getMethod(name,ca);
    return m.invoke(rec,args);
  }

  /**
   * Mirrored from MainFrame, will later use the same
   * Icon access code.
   */
  private ImageIcon _getIcon(String name) {
    URL url = PreviewFrame.class.getResource(ICON_PATH + name);
    if (url != null) {
      return new ImageIcon(url);
    } else {
      return null;
    }
  }

  /**
   * Sets up the toolbar with all of the necessary buttons.
   */
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

  /**
   * Generates an Image, prints to it, and then displays the image
   * on the page.
   */
  private void showPage() {
    BufferedImage img = new BufferedImage((int) _model.getPageFormat().getWidth(),
                                          (int) _model.getPageFormat().getHeight(),
                                          BufferedImage.TYPE_INT_RGB);
    Graphics g = img.getGraphics();
    g.setColor(Color.white);
    g.fillRect(0, 0, (int) _model.getPageFormat().getWidth(),
                     (int) _model.getPageFormat().getHeight());

    try {
      _print.getPrintable(_pageNumber).print(g, _model.getPageFormat(), _pageNumber);
      _pagePreview.setImage(img);
    } catch (PrinterException e) {
    }
  }

  /**
   * Internal class which holds (and places) the PagePreview
   * object.
   */
  class PagePreviewContainer extends JPanel {
    public Dimension getPreferredSize() {
      return getParent().getSize();
    }

    /**
     * Places the PagePreview component into the center of this
     * object
     */
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
    protected int _width;
    protected int _height;
    protected Image _source;
    protected Image _image;

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

    /** Updates the image of this PagePreview.
     *  @param i The Image to place and show.
     */
    public void setImage(Image i) {
      _source = i;
      updateScaled();
      repaint();
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