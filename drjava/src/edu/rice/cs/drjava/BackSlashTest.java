/* $Id$ */

package edu.rice.cs.drjava;

import junit.framework.*;
import java.util.Vector;
import junit.extensions.*;

public class BackSlashTest extends TestCase {

  private static final int INSIDE_QUOTE = ReducedToken.INSIDE_QUOTE;
  private static final int FREE = ReducedToken.FREE;

  protected ReducedModelControl model0;
  protected ReducedModelControl model1;
  protected ReducedModelControl model2;

  public BackSlashTest(String name) {
    super(name);
  }
  
  protected void setUp() {
    model0 = new ReducedModelControl();
    model1 = new ReducedModelControl();
    model2 = new ReducedModelControl();
  }
  
  public static Test suite() {
    return new TestSuite(BackSlashTest.class);
  }

  public void testInsideQuotePrevious() {
    model1.insertChar('\"');
    model1.insertChar('\\');
    model1.insertChar('\"');
    model1.move(-2);
    assertEquals("#0.0", "\\\"", model1.currentToken().getType());
    assertEquals("#0.1", INSIDE_QUOTE, stateOfCurrentToken(model1));

    model1.move(2);
    model1.insertChar('\"');
    model1.move(-1);
    assertEquals("#1.0", "\"", model1.currentToken().getType());
    assertEquals("#1.1", FREE, stateOfCurrentToken(model1));
    assertTrue("#1.2", model1.currentToken().isClosed());

    model1.move(1);
    model1.insertChar('\"');     
    model1.insertChar('\\');
    model1.insertChar('\\');
    model1.move(-2);
    assertEquals("#2.0", "\\\\", model1.currentToken().getType());
    assertEquals("#2.1", INSIDE_QUOTE, stateOfCurrentToken(model1));       

    model1.move(2);
    model1.insertChar('\\');
    model1.move(-1);
    assertEquals("#3.0", "\\", model1.currentToken().getType());
    assertEquals("#3.1", INSIDE_QUOTE, stateOfCurrentToken(model1));

    model1.move(1);
    model1.insertChar('\"');
    model1.move(-1);
    assertEquals("#4.0", "\\\"", model1.currentToken().getType());
    assertEquals("#4.1", INSIDE_QUOTE, stateOfCurrentToken(model1));
  }

  public void testInsideQuoteNext() {
    model1.insertChar('\"');
    model1.insertChar('\"');
    model1.move(-1);
    model1.insertChar('\\');
    assertEquals("#0.0", "\\\"", model1.currentToken().getType());
    assertEquals("#0.1", INSIDE_QUOTE, stateOfCurrentToken(model1));
    assertEquals("#0.2", 1, model1.getBlockOffset());

    model1.move(1);     
    model1.insertChar('\"');
    model1.move(-1);
    assertEquals("#1.0", "\"", model1.currentToken().getType());
    assertEquals("#1.1", FREE, stateOfCurrentToken(model1));
    assertTrue("#1.2", model1.currentToken().isClosed());

    model1.move(1);
    model1.insertChar('\"');     
    model1.insertChar('\\');
    model1.move(-1);
    model1.insertChar('\\');
    assertEquals("#2.0", "\\\\", model1.currentToken().getType());
    assertEquals("#2.1", INSIDE_QUOTE, stateOfCurrentToken(model1));
    assertEquals("#2.2", 6, model1.absOffset());

    model1.move(-2);
    model1.insertChar('{');
    model1.move(-1);
    assertEquals("#3.0", "{", model1.currentToken().getType());
    assertEquals("#3.1", FREE, stateOfCurrentToken(model1));

    model1.move(1);
    model1.move(3);
    model1.insertChar('\"');
    model1.move(-1);
    assertEquals("#4.0", "\"", model1.currentToken().getType());
    assertEquals("#4.1", FREE, stateOfCurrentToken(model1));
    assertTrue("#4.2", model1.currentToken().isClosed());

    model1.insertChar('\\');
    assertEquals("#5.0", "\\\"", model1.currentToken().getType());
    assertEquals("#5.1", INSIDE_QUOTE, stateOfCurrentToken(model1));
    assertEquals("#5.2", 1, model1.getBlockOffset());         
  }

  public void testBackSlashBeforeDoubleEscape() {
    model1.insertChar('\\');
    model1.insertChar('\\');
    model1.move(-2);
    model1.insertChar('\\');
    assertEquals("#0.0", "\\\\", model1.currentToken().getType());
    assertEquals("#0.1", 2, model1.currentToken().getSize());

    model1.move(1);
    assertEquals("#0.2", "\\", model1.currentToken().getType());

    model2.insertChar('\\');
    model2.insertChar('\"');
    model2.move(-2);
    model2.insertChar('\\');
    assertEquals("#1.0", "\\\\", model2.currentToken().getType());
    assertEquals("#1.1", 1, model2.absOffset());

    model2.move(1);
    assertEquals("#1.2", "\"", model2.currentToken().getType());    
  }

  public void testInsertBetweenDoubleEscape() {
    model1.insertChar('\\');
    model1.insertChar('\\');
    model1.move(-1);
    model1.insertChar('\\');
    model1.move(-2);
    assertEquals("#0.0", "\\\\", model1.currentToken().getType());

    model1.move(2);
    assertEquals("#0.1", "\\", model1.currentToken().getType());

    model2.insertChar('\\');
    model2.insertChar('\"');
    model2.move(-1);
    model2.insertChar('\\');
    model2.move(-2);
    assertEquals("#1.0", "\\\\", model2.currentToken().getType());

    model2.move(2);
    assertEquals("#1.1", "\"", model2.currentToken().getType());

    model0.insertChar('\\');
    model0.insertChar('\\');
    model0.move(-1);
    model0.insertChar(')');
    model0.move(-2);
    assertEquals("#2.0", "\\", model0.currentToken().getType());

    model0.move(1);
    assertEquals("#2.1", ")", model0.currentToken().getType());

    model0.move(1);
    assertEquals("#2.2", "\\", model0.currentToken().getType());

    model0.move(1);
    model0.delete(-3);
    model0.insertChar('\\');
    model0.insertChar('\"');
    model0.move(-1);
    model0.insertChar(')');
    model0.move(-2);
    assertEquals("#3.0", "\\", model0.currentToken().getType());

    model0.move(1);
    assertEquals("#3.1", ")", model0.currentToken().getType());

    model0.move(1);
    assertEquals("#3.2", "\"", model0.currentToken().getType());
  }

  public void testDeleteAndCombine() {
    model0.insertChar('\\');
    model0.insertGap(2);
    model0.insertChar('\"');
    model0.move(-1);
    assertEquals("#0.0", "\"", model0.currentToken().getType());

    model0.delete(-2);
    assertEquals("#1.0", "\\\"", model0.currentToken().getType());
    assertEquals("#1.1", 1, model0.absOffset());
    
    model0.delete(1);
    model0.insertGap(2);
    model0.insertChar('\\');
    model0.move(-1);
    assertEquals("#2.0", "\\", model0.currentToken().getType());

    model0.delete(-2);
    assertEquals("#3.0", "\\\\", model0.currentToken().getType());
    assertEquals("#3.1", 2, model0.currentToken().getSize());
  }

  public void testDeleteAndCombine2() {
    model0.insertChar('\\');
    model0.insertChar('\"');
    model0.move(-1);
    model0.delete(-1);
    assertEquals("#0.0", "\"", model0.currentToken().getType());
    assertEquals("#0.1", 
                 FREE,
                 model0.getStateAtCurrent());
    
    model1.insertChar('\\');
    model1.insertChar('\\');
    model1.delete(-1);
    model1.move(-1);
    assertEquals("#1.0", "\\", model1.currentToken().getType());

    model1.move(1);
    model1.insertChar('\\');
    model1.move(-1);
    model1.delete(-1);
    assertEquals("#2.0", "\\", model1.currentToken().getType());      
  }

  public void testDeleteAndCombine3() {
    model0.insertChar('\\');
    model0.insertChar('\\');
    model0.insertGap(3);
    model0.insertChar('\\');
    model0.move(-1);
    model0.delete(-4);
    assertEquals("#0.0", "\\\\", model0.currentToken().getType());
    assertEquals("#0.1", 1, model0.absOffset());

    model1.insertChar('\\');
    model1.insertGap(3);
    model1.insertChar('\\');
    model1.insertChar('\"');
    model1.move(-1);
    model1.delete(-4);
    assertEquals("#1.0", "\\\"", model1.currentToken().getType());
    assertEquals("#1.1", 1, model1.absOffset());
  }

  public void testChainEffect() {
    model0.insertChar('\"');
    model0.insertChar('\\');
    model0.insertChar('\"');
    model0.insertChar('\"');
    model0.insertChar('\"');
    model0.insertChar('\\');
    model0.insertChar('\"');
    model0.insertChar('\"');
    model0.insertChar('\"');
    model0.insertChar('\\');
    model0.insertChar('\"');
    model0.insertChar('\"');
    // "\"""\"""\""#
    model0.move(-1);
    assertEquals("#0.0", "\"", model0.currentToken().getType());
    assertTrue("#0.1", model0.currentToken().isClosed());

    model0.move(-2);
    // "\"""\"""#\""
    assertEquals("#1.0", "\\\"", model0.currentToken().getType());
    assertEquals("#1.1", INSIDE_QUOTE, stateOfCurrentToken(model0));

    model0.move(-1);
    assertEquals("#1.2", "\"", model0.currentToken().getType());
    assertEquals("#1.3", FREE, stateOfCurrentToken(model0));
    assertTrue("#1.4", model0.currentToken().isOpen());
    
    model0.move(1);
    model0.insertChar('\\');
    // "\"""\"""\#\""
    assertEquals("#2.0", "\\\\", model0.currentToken().getType());
    assertEquals("#2.1", INSIDE_QUOTE, stateOfCurrentToken(model0));
    assertEquals("#2.2", 10, model0.absOffset());

    model0.move(-2);
    assertEquals("#2.3", "\"", model0.currentToken().getType());
    assertEquals("#2.4", FREE, stateOfCurrentToken(model0));
    assertTrue("#2.5", model0.currentToken().isOpen());

    model0.move(3);
    assertEquals("#2.6", "\"", model0.currentToken().getType());
    assertEquals("#2.7", FREE, stateOfCurrentToken(model0));
    assertTrue("#2.8", model0.currentToken().isClosed());

    model0.move(-1);
    model0.insertChar('\"');
    // "\"""\"""\"#\""
    assertEquals("#3.0", "\\\"", model0.currentToken().getType());
    assertEquals("#3.1", INSIDE_QUOTE, stateOfCurrentToken(model0));
    assertEquals("#3.2", 11, model0.absOffset());

    model0.move(-2);
    assertEquals("#3.3", "\\\"", model0.currentToken().getType());
    assertEquals("#3.4", INSIDE_QUOTE, stateOfCurrentToken(model0));

    model0.move(4);
    assertEquals("#3.5", "\"", model0.currentToken().getType());
    assertEquals("#3.6", FREE, stateOfCurrentToken(model0));
    assertTrue("#3.7", model0.currentToken().isClosed());

    model0.move(-12);
    // "#\"""\"""\"\""
    model0.delete(1);
    // "#"""\"""\"\""
    model0.move(-1);
    // #""""\"""\"\""
    assertEquals("#4.0", "\"", model0.currentToken().getType());
    assertTrue("#4.1", model0.currentToken().isOpen());
    assertEquals("#4.2", FREE, stateOfCurrentToken(model0));

    model0.move(1);
    // "#"""\"""\"\""
    assertEquals("#4.3", "\"", model0.currentToken().getType());
    assertTrue("#4.4", model0.currentToken().isClosed());
    assertEquals("#4.5", FREE, stateOfCurrentToken(model0));

    model0.move(1);
    // ""#""\"""\"\""
    assertEquals("#5.0", "\"", model0.currentToken().getType());
    assertTrue("#5.1", model0.currentToken().isOpen());
    assertEquals("#5.2", FREE, stateOfCurrentToken(model0));
    model0.move(1);
    // """#"\"""\"\""
    assertEquals("#5.3", "\"", model0.currentToken().getType());
    assertTrue("#5.4", model0.currentToken().isClosed());
    assertEquals("#5.5", FREE, stateOfCurrentToken(model0));

    model0.move(1);
    // """"#\"""\"\""
    assertEquals("#5.6", "\\\"", model0.currentToken().getType());
    assertEquals("#5.7", FREE, stateOfCurrentToken(model0));
    
    model0.move(2);
    // """"\"#""\"\""
    assertEquals("#6.0", "\"", model0.currentToken().getType());
    assertTrue("#6.1", model0.currentToken().isOpen());
    assertEquals("#6.2", FREE, stateOfCurrentToken(model0));

    model0.move(1);
    // """"\""#"\"\""
    assertEquals("#6.3", "\"", model0.currentToken().getType());
    assertTrue("#6.4", model0.currentToken().isClosed());
    assertEquals("#6.5", FREE, stateOfCurrentToken(model0));

    model0.move(1);
    // """"\"""#\"\""
    assertEquals("#6.6", "\\\"", model0.currentToken().getType());
    assertEquals("#6.7", FREE, stateOfCurrentToken(model0));

    model0.move(2);
    // """"\"""\"#\""
    assertEquals("#6.0", "\\\"", model0.currentToken().getType());
    assertEquals("#6.1", FREE, stateOfCurrentToken(model0));

    model0.move(2);
    // """"\"""\"\"#"
    assertEquals("#6.2", "\"", model0.currentToken().getType());
    assertTrue("#6.3", model0.currentToken().isOpen());
    assertEquals("#6.4", FREE, stateOfCurrentToken(model0));
  }

  private int stateOfCurrentToken(ReducedModelControl rmc) {
    return rmc.currentToken().getState();
  }
}





