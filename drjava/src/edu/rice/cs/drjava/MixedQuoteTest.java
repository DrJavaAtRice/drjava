package  edu.rice.cs.drjava;

import  junit.framework.*;
import  java.util.Vector;
import  junit.extensions.*;


/**
 * @version $Id$
 */
public class MixedQuoteTest extends BraceReductionTestCase 
  implements ReducedModelStates 
{

  protected ReducedModelControl model;

  /**
   * Constructor.
   * @param name a name for the test.
   */
  public MixedQuoteTest(String name) {
    super(name);
  }

  /**
   * Initializes the reduced models used in the tests.
   */
  protected void setUp() {
    model = new ReducedModelControl();
  }

  /**
   * Creates a test suite for JUnit to use.
   * @return a test suite for JUnit
   */
  public static Test suite() {
    return  new TestSuite(MixedQuoteTest.class);
  }

  /**
   * Convenience function to insert a number of non-special characters into a reduced model.
   * @param model the model being modified
   * @param size the number of characters being inserted
   */
  protected void insertGap(BraceReduction model, int size) {
    for (int i = 0; i < size; i++) {
      model.insertChar(' ');
    }
  }
  
  /**
   * Tests how a single quote can eclipse the effects of a double quote by inserting
   * the single quote before the double quote.  This test caught an error with 
   * getStateAtCurrent(): the check for double quote status checks if there is a double
   * quote immediately preceding, but it didn't make sure the double quote was FREE.
   * I fixed that, so now the test passes.
   */
  public void testSingleEclipsesDouble() {
    model.insertChar('\"');
    assertEquals("#0.0", INSIDE_DOUBLE_QUOTE, model.getStateAtCurrent());
    model.move(-1);
    assertEquals("#0.1", FREE, stateOfCurrentToken(model));
    model.move(1);
    model.insertChar('A');    
    model.move(-1);
    assertEquals("#1.0", INSIDE_DOUBLE_QUOTE, model.getStateAtCurrent());    
    assertEquals("#1.1", INSIDE_DOUBLE_QUOTE, stateOfCurrentToken(model));
    assertTrue("#1.2", model.currentToken().isGap());
    model.move(-1);
    model.insertChar('\'');
    assertEquals("#2.0", INSIDE_SINGLE_QUOTE, model.getStateAtCurrent());
    assertEquals("#2.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model));
    assertEquals("#2.2", "\"", model.currentToken().getType());
    model.move(1);
    assertEquals("#3.0", INSIDE_SINGLE_QUOTE, model.getStateAtCurrent());
    assertEquals("#3.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model));
    assertTrue("#3.2", model.currentToken().isGap());    
  }

  /**
   * Tests how a double quote can eclipse the effects of a single quote by inserting
   * the double quote before the single quote. 
   */
  public void testDoubleEclipsesSingle() {
    model.insertChar('\'');
    assertEquals("#0.0", INSIDE_SINGLE_QUOTE, model.getStateAtCurrent());
    model.move(-1);
    assertEquals("#0.1", FREE, stateOfCurrentToken(model));
    model.move(1);
    model.insertChar('A');    
    model.move(-1);
    assertEquals("#1.0", INSIDE_SINGLE_QUOTE, model.getStateAtCurrent());    
    assertEquals("#1.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(model));
    assertTrue("#1.2", model.currentToken().isGap());
    model.move(-1);
    model.insertChar('\"');
    assertEquals("#2.0", INSIDE_DOUBLE_QUOTE, model.getStateAtCurrent());
    assertEquals("#2.1", INSIDE_DOUBLE_QUOTE, stateOfCurrentToken(model));
    assertEquals("#2.2", "\'", model.currentToken().getType());
    model.move(1);
    assertEquals("#3.0", INSIDE_DOUBLE_QUOTE, model.getStateAtCurrent());
    assertEquals("#3.1", INSIDE_DOUBLE_QUOTE, stateOfCurrentToken(model));
    assertTrue("#3.2", model.currentToken().isGap());    
  }  
}
