/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import javax.swing.text.DefaultEditorKit;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.*;
import java.awt.*;
import javax.swing.event.DocumentEvent;

import gj.util.Vector;

/**
 * This is an editor kit for editing Java source files.
 * It functions as the controller in the MVC arrangement.
 * It implements a factory for new documents, and it also
 * has a factory for Views (the things that render the document).
 */
public class DefinitionsEditorKit extends DefaultEditorKit
{
  /** Factory method to make this view create correct model objects. */
  public Document createDefaultDocument()
  {
    return _createDefaultTypedDocument();
  }

  private static DefinitionsDocument _createDefaultTypedDocument()
  {
    return new DefinitionsDocument();
  }

  public String getContentType()
  {
    return "text/java";
  }

  /**
   * We want to use our ColoringView to render text, so here we return
   * a factory that creates ColoringViews.
   */
  public final ViewFactory getViewFactory() {
    return new ViewFactory() {
      public View create(Element elem) {
        return new ColoringView(elem);
        //return new WrappedPlainView(elem);
      }
    };
  }
}

/**
 * This view class renders text on the screen.
 * By extending WrappedPlainView, we only have to override the parts we want to.
 * Here we only override drawUnselectedText. We may want to override 
 * drawSelectedText at some point.
 */
class ColoringView extends WrappedPlainView {
  private DefinitionsDocument _doc;

  ColoringView(Element elem) {
    super(elem);
    _doc = (DefinitionsDocument) getDocument();
  }

  /**
   * Renders the given range in the model as normal unselected
   * text.
   * Note that this is text that's all on one line. The superclass deals
   * with breaking lines and such. So all we have to do here is draw the
   * text on [p0,p1) in the model. We have to start drawing at (x,y), and
   * the function returns the x coordinate when we're done.
   *
   * @param g the graphics context
   * @param x the starting X coordinate
   * @param y the starting Y coordinate
   * @param p0 the beginning position in the model
   * @param p1 the ending position in the model
   * @returns the x coordinate at the end of the range
   * @exception BadLocationException if the range is invalid
   */
  protected int drawUnselectedText(Graphics g,
                                   int x,
                                   int y,
                                   int p0,
                                   int p1) throws BadLocationException
  {
    /*
    DrJava.consoleErr().println("drawUnselected: " + p0 + "-" + p1 + 
                                " doclen=" + _doc.getLength() +" x="+x+" y="+y);
    */

    // If there's nothing to show, don't do anything!
    // For some reason I don't understand we tend to get called sometimes
    // to render a zero-length area.
    if (p0 == p1) {
      return x;
    }

    Vector<HighlightStatus> stats = _doc.getHighlightStatus(p0, p1);
    if (stats.size() < 1) {
      throw new RuntimeException("GetHighlightStatus returned nothing!");
    }

    for (int i = 0; i < stats.size(); i++) {
      HighlightStatus stat = stats.elementAt(i);
      setFormattingForState(g, stat.getState());

      // If this highlight status extends past p1, end at p1
      int length = stat.getLength();
      int location = stat.getLocation();
      if (location + length > p1) {
        length = p1 - stat.getLocation();
      }

      Segment text = getLineBuffer();

      /*
      DrJava.consoleErr().println("Highlight: loc=" + location + " len=" +
                                  length + " state=" + stat.getState() +
                                  " text=" + text);
      */

      _doc.getText(location, length, text);
      x = Utilities.drawTabbedText(text, x, y, g, this, location);
    }

    //DrJava.consoleErr().println("returning x: " + x);
    return x;
  }

  protected int drawSelectedText(Graphics g,
                                   int x,
                                   int y,
                                   int p0,
                                   int p1) throws BadLocationException
  {
    /*
    DrJava.consoleErr().println("drawSelected: " + p0 + "-" + p1 + 
                                " doclen=" + _doc.getLength() +" x="+x+" y="+y);
    */
    return super.drawSelectedText(g, x, y, p0, p1);
  }


  private void setFormattingForState(Graphics g, int state) {
    switch (state) {
      case HighlightStatus.NORMAL:
        g.setColor(Color.black);
        break;
      case HighlightStatus.COMMENTED:
        g.setColor(Color.green);
        break;
      case HighlightStatus.QUOTED:
        g.setColor(Color.red);
        break;
      case HighlightStatus.KEYWORD:
        g.setColor(Color.blue);
        break;
      default:
        throw new RuntimeException("Can't get color for invalid state: " + state);
    }
  }

  public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
    super.changedUpdate(changes, a, f);
    // Make sure we redraw since something changed in the formatting
    getContainer().repaint();
  }
}
