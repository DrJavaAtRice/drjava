package edu.rice.cs.drjava.ui;

import java.awt.*;
import javax.swing.*;


public class Rule extends JComponent {
       
    public static int SIZE = 35;

    private int increment;
    protected FontMetrics _fm;
    protected DefinitionsPane _p;
    protected Font newFont;
    protected FontMetrics nfm;

    public Rule(DefinitionsPane p) {
        _p = p;
        _fm = _p.getFontMetrics(_p.getFont());
        increment = _fm.getHeight();
        newFont = _p.getFont().deriveFont( 8f );
        nfm = getFontMetrics(newFont);
        SIZE = nfm.stringWidth("99999");
    }

    /**
     * Return a new Dimension using our set width, and the height of the def. pane
     * return Dimension
     */
    public Dimension getPreferredSize() {
      return new Dimension( SIZE, (int)_p.getPreferredSize().getHeight());
    }
    
    public void paintComponent(Graphics g) {
        Rectangle drawHere = g.getClipBounds();

        g.setColor(new Color(255, 255, 255));
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);

        // Do the ruler labels in a small font that's black.
        g.setFont(newFont); 
        g.setColor(Color.black);

        // Some vars we need.
        int end = 0;
        int start = 1;
        int tickLength = 0;
        String text = null;
            
        // Use clipping bounds to calculate first tick and last tick location.
       
        start = (drawHere.y / increment) * increment;
        end = (((drawHere.y + drawHere.height) / increment) + 1)
          * increment;
        
        
        int baseline = (int) (( nfm.getAscent() + _fm.getHeight() - _fm.getDescent())/2.0 );
        
        // ticks and labels
        for (int i = start; i < end; i += increment) {
          tickLength = 10;
          text = Integer.toString(i/increment +1);
          
          
          int offset = SIZE - (nfm.stringWidth(text) + 1);
          
          //g.drawLine(SIZE-1, i, SIZE-tickLength-1, i);
          if (text != null)
            g.drawString(text, offset, i + baseline);
          
        }
    }
}

