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

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.MatteBorder;
import java.awt.event.*;
import java.awt.*;
import java.awt.print.*;
import java.awt.image.*;
import java.net.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;

/**
 * DrJava's print preview window
 * @version $Id$
 */
public class PreviewFrame extends JFrame {

  private SingleDisplayModel _model;
  private OpenDefinitionsDocument _document;
  private MainFrame _mainFrame;
  private Pageable _print;
  private int _pageNumber;

  private JTextField _pageTextField = new JTextField("" + (_pageNumber + 1), 2) {
      public Dimension getMaximumSize() {
        return getPreferredSize();
      }
    };

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
  private Action _printAction = new AbstractAction("Print...") {
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

  /** Displays the previous page of the document. */
  private Action _goToPageAction = new AbstractAction("Goto Page") {
    public void actionPerformed(ActionEvent ae) {
      try {
        int pageToGoTo = Integer.parseInt(_pageTextField.getText().toString()) - 1;

        if ((pageToGoTo < 0) || (pageToGoTo >= _print.getNumberOfPages())) {
          _updateActions();
        } else {
          _goToPage(pageToGoTo);
        }
      } catch (NumberFormatException e) {
        _updateActions();
      }
    }
  };

  /** How Preview Pane responds to window events. */
  private WindowListener _windowCloseListener = new WindowListener() {
    public void windowActivated(WindowEvent ev) {}
    public void windowClosed(WindowEvent ev) {}
    public void windowClosing(WindowEvent ev) {
      _close();
    }
    public void windowDeactivated(WindowEvent ev) {}
    public void windowDeiconified(WindowEvent ev) {}
    public void windowIconified(WindowEvent ev) {}
    public void windowOpened(WindowEvent ev) {}
  };

  /**
   * Contructs a new PreviewFrame using a parent model and
   * a Pageable object print to show.
   */
  public PreviewFrame(SingleDisplayModel model, MainFrame mainFrame)
    throws IllegalStateException {
    super("Print Preview");
    _model = model;
    _mainFrame = mainFrame;
    _document = model.getActiveDocument();
    _toolBar = new JToolBar();

    _mainFrame.hourglassOn();

    _print = _document.getPageable();

    _setUpActions();
    _setUpToolBar();
    _setUpConstants();

    _pagePreview = new PagePreview(PREVIEW_PAGE_WIDTH, PREVIEW_PAGE_HEIGHT);
    _pageNumber = 0;

    PagePreviewContainer ppc = new PagePreviewContainer();
    ppc.add(_pagePreview);

    getContentPane().add(_toolBar, BorderLayout.NORTH);
    getContentPane().add(ppc, BorderLayout.SOUTH);
    this.addWindowListener(_windowCloseListener);

    showPage();
    _updateActions();

    setSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setVisible(true);
  }

  private void _print() {
    try {
      _document.print();
    } catch (PrinterException e) {
      _showError(e, "Print Error", "An error occured while printing.");
    } catch (BadLocationException e) {
      _showError(e, "Print Error", "An error occured while printing.");
    }
  }

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

  private void _showError(Exception e, String title, String message) {
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
    _pageTextField.setText("" + (_pageNumber + 1));
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
    _printAction.putValue(Action.SHORT_DESCRIPTION, "Print");
    _closeAction.putValue(Action.SHORT_DESCRIPTION, "Close");

    _nextPageAction.putValue(Action.SMALL_ICON, _getIcon("Forward16.gif"));
    _nextPageAction.putValue(Action.SHORT_DESCRIPTION, "Next Page");
    _prevPageAction.putValue(Action.SMALL_ICON, _getIcon("Back16.gif"));
    _prevPageAction.putValue(Action.SHORT_DESCRIPTION, "Previous Page");

    _goToPageAction.putValue(Action.SHORT_DESCRIPTION, "Goto Page");

    _pageTextField.setAction(_goToPageAction);
  }

  /**
   * Mirrored from MainFrame, will later use the same
   * Icon access code.
   */
  private ImageIcon _getIcon(String name) {
    URL url = _model.getClass().getResource(ICON_PATH + name);
    if (url != null) {
      return new ImageIcon(url);
    }
    return null;
  }

  /**
   * Sets up the toolbar with all of the necessary buttons.
   */
  private void _setUpToolBar() {
    _toolBar.setFloatable(false);

    _toolBar.add(_printAction);
    _toolBar.addSeparator();

    _toolBar.add(_closeAction);
    _toolBar.addSeparator();

    _toolBar.add(_prevPageAction);
    _toolBar.add(_nextPageAction);
    _toolBar.addSeparator();

    JLabel gotop = new JLabel("Page");
    JLabel of = new JLabel(" of " + _print.getNumberOfPages());

    _toolBar.add(gotop);
    _toolBar.add(_pageTextField);
    _toolBar.add(of);

    _toolBar.addSeparator();

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
   * Interal class which holds (and places) the PagePreview
   * object.
   */
  class PagePreviewContainer extends JPanel
  {
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

  /**
   * Inner class which displays the image on the screen, and
   * holds the Image object.
   */
  class PagePreview extends JPanel
  {
    protected int _width;
    protected int _height;
    protected Image _source;
    protected Image _image;

    /**
     * Constructs a PagePreview object with given
     * width and height.
     */
    public PagePreview(int width, int height) {
      super();
      _width = width;
      _height = height;
      setBorder(new MatteBorder(1, 1, 2, 2, Color.black));
      setBackground(Color.white);
    }

    /**
     * Scales the interal image to the appropriate
     * size.
     */
    private void updateScaled() {
      _image = _source.getScaledInstance(_width, _height, Image.SCALE_SMOOTH);
      _image.flush();
    }

    /**
     * Updates the image of this PagePreview.
     * @param i The Image to place and show.
     */
    public void setImage(Image i) {
      _source = i;
      updateScaled();
      repaint();
    }

    public Dimension getPreferredSize() {
      return new Dimension(_width, _height);
    }

    public Dimension getMaximumSize() {
      return getPreferredSize();
    }

    public Dimension getMinimumSize() {
      return getPreferredSize();
    }

    public void paint(Graphics g) {
      g.setColor(getBackground());
      g.fillRect(0, 0, _width, _height);
      g.drawImage(_image, 0, 0, this);
      paintBorder(g);
    }
  }


}
