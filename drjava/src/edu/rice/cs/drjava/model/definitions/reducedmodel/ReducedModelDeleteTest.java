package  edu.rice.cs.drjava;

import  junit.framework.*;
import  java.util.Vector;
import  junit.extensions.*;


/**
 * @version $Id$
 */
public class ReducedModelDeleteTest extends TestCase implements ReducedModelStates {
  
  protected BraceReduction model0;
  protected BraceReduction model1;
  protected BraceReduction model2;

  /**
   * put your documentation comment here
   * @param   String name
   */
  public ReducedModelDeleteTest(String name) {
    super(name);
  }

  /**
   * put your documentation comment here
   */
  protected void setUp() {
    model0 = new ReducedModelControl();
    model1 = new ReducedModelControl();
    model2 = new ReducedModelControl();
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public static Test suite() {
    return  new TestSuite(ReducedModelDeleteTest.class);
  }

  protected void insertGap(BraceReduction model, int size) {
    for (int i = 0; i < size; i++)
      model.insertChar(' ');
  }

  /**
   * put your documentation comment here
   */
  public void testHalfLineComment() {
    model1.insertChar('/');
    model1.insertChar('/');
    model1.move(-2);
    assertEquals("#0.0", "//", model1.currentToken().getType());
    assertEquals("#0.1", 0, model1.absOffset());
    model1.delete(1);
    assertEquals("#1.0", "/", model1.currentToken().getType());
    assertEquals("#1.1", 0, model1.absOffset());
    model1.insertChar('/');
    model1.delete(1);           //This time delete the second slash
    assertEquals("#2.0", 1, model1.absOffset());
    model1.move(-1);
    assertEquals("#2.1", 0, model1.absOffset());
    assertEquals("#2.2", "/", model1.currentToken().getType());
    model1.delete(1);
    assertEquals("#3.0", 0, model1.absOffset());
  }

  /**
   * put your documentation comment here
   */
  public void testInnerGapDelete() {
    insertGap(model1, 8);
    assertEquals("#0.0", 8, model1.absOffset());
    model1.move(-6);
    assertEquals("#0.0", 2, model1.absOffset());
    model1.delete(3);
    assertEquals("#1.0", 2, model1.absOffset());
    assertEquals("#1.1", 5, model1.currentToken().getSize());
    model1.move(3);
    assertEquals("#2.0", 5, model1.absOffset());
  }

  /**
   * put your documentation comment here
   */
  public void testDeleteAndMergeTwoGaps() {
    insertGap(model1, 5);
    model1.insertChar('/');
    assertEquals("#1.0", 6, model1.absOffset());
    model1.insertChar('*');
    assertEquals("#2.0", 7, model1.absOffset());
    insertGap(model1, 6);
    assertEquals("#3.0", 13, model1.absOffset());
    model1.move(-9);
    assertEquals("#4.0", 4, model1.absOffset());
    assertTrue("#4.1", model1.currentToken().isGap());
    assertEquals("#4.2", 5, model1.currentToken().getSize());
    model1.move(2);
    assertEquals("#5.0", 6, model1.absOffset());
    assertEquals("#5.2", "/*", model1.currentToken().getType());
    model1.move(-2);
    model1.delete(5);
    assertEquals("#6.0", 4, model1.absOffset());
    assertTrue("#6.1", model1.currentToken().isGap());
    assertEquals("#6.2", 8, model1.currentToken().getSize());
  }

  /**
   * put your documentation comment here
   */
  public void testDeleteBlockCommentMakesLineComment() {
    model1.insertChar('/');
    assertEquals("#0.0", 1, model1.absOffset());
    assertEquals("#0.1", FREE, model1.getStateAtCurrent());
    model1.insertChar('*');
    assertEquals("#1.0", 2, model1.absOffset());
    assertEquals("#1.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    insertGap(model1, 2);
    assertEquals("#2.0", 4, model1.absOffset());
    assertEquals("#2.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.insertChar('*');
    assertEquals("#3.0", 5, model1.absOffset());
    assertEquals("#3.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.insertChar('/');
    assertEquals("#4.0", 6, model1.absOffset());
    assertEquals("#4.1", FREE, model1.getStateAtCurrent());
    insertGap(model1, 1);
    assertEquals("#5.0", 7, model1.absOffset());
    assertEquals("#5.1", FREE, model1.getStateAtCurrent());
    //  /*__*/_#
    model1.move(-6);
    assertEquals("#6.0", 1, model1.absOffset());
    model1.delete(4);
    assertEquals("#7.0", 1, model1.absOffset());
    assertEquals("#7.1", "//", model1.currentToken().getType());
    assertEquals("#7.3", FREE, model1.getStateAtCurrent());
    model1.move(1);
    assertEquals("#7.0", 2, model1.absOffset());
    assertTrue("#7.1", model1.currentToken().isGap());
    assertEquals("#7.2", 1, model1.currentToken().getSize());
    assertEquals("#7.3", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
  }

  /**
   * put your documentation comment here
   */
  public void testLineCommentStealsBlockCommentSlash() {
    model1.insertChar('/');
    assertEquals("#0.0", 1, model1.absOffset());
    insertGap(model1, 2);
    assertEquals("#1.0", 3, model1.absOffset());
    model1.insertChar('/');
    assertEquals("#2.0", 4, model1.absOffset());
    model1.insertChar('*');
    assertEquals("#3.0", 5, model1.absOffset());
    assertEquals("#3.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.move(-4);
    model1.delete(2);
    assertEquals("#4.0", 1, model1.absOffset());
    assertEquals("#4.1", "//", model1.currentToken().getType());
    assertEquals("#4.2", FREE, model1.getStateAtCurrent());
    model1.move(1);
    assertEquals("#5.0", 2, model1.absOffset());
    assertEquals("#5.1", "*", model1.currentToken().getType());
    assertEquals("#5.2", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    assertEquals("#5.3", INSIDE_LINE_COMMENT, model1.currentToken().getState());
  }

  /**
   * put your documentation comment here
   */
  public void testLineCommentStealsLineCommentSlash() {
    model1.insertChar('/');
    assertEquals("#0.0", 1, model1.absOffset());
    insertGap(model1, 2);
    assertEquals("#1.0", 3, model1.absOffset());
    model1.insertChar('/');
    assertEquals("#2.0", 4, model1.absOffset());
    model1.insertChar('/');
    assertEquals("#3.0", 5, model1.absOffset());
    assertEquals("#3.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    model1.move(-4);
    model1.delete(2);
    assertEquals("#4.0", 1, model1.absOffset());
    assertEquals("#4.1", "//", model1.currentToken().getType());
    assertEquals("#4.2", FREE, model1.getStateAtCurrent());
    model1.move(1);
    assertEquals("#5.0", 2, model1.absOffset());
    assertEquals("#5.1", "/", model1.currentToken().getType());
    assertEquals("#5.2", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    assertEquals("#5.3", INSIDE_LINE_COMMENT, model1.currentToken().getState());
  }

  /**
   * put your documentation comment here
   */
  public void testDeleteNewlineAndShadowBlockCommentStart() {
    model1.insertChar('/');
    assertEquals("#0.0", 1, model1.absOffset());
    assertEquals("#0.1", FREE, model1.getStateAtCurrent());
    model1.insertChar('/');
    assertEquals("#1.0", 2, model1.absOffset());
    assertEquals("#1.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    model1.insertChar('*');
    assertEquals("#2.0", 3, model1.absOffset());
    assertEquals("#2.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    model1.insertChar('\n');
    assertEquals("#3.0", 4, model1.absOffset());
    assertEquals("#3.1", FREE, model1.getStateAtCurrent());
    model1.insertChar('/');
    assertEquals("#4.0", 5, model1.absOffset());
    assertEquals("#4.1", FREE, model1.getStateAtCurrent());
    model1.insertChar('*');
    assertEquals("#5.0", 6, model1.absOffset());
    assertEquals("#5.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.move(-3);
    assertEquals("#6.0", 3, model1.absOffset());
    model1.delete(1);
    assertEquals("#7.0", 3, model1.absOffset());
    assertEquals("#7.1", "/", model1.currentToken().getType());
    assertEquals("#7.2", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    model1.move(-1);
    assertEquals("#8.0", 2, model1.absOffset());
    assertEquals("#8.1", "*", model1.currentToken().getType());
    assertEquals("#8.2", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    model1.move(2);
    assertEquals("#9.0", 4, model1.absOffset());
    assertEquals("#9.1", "*", model1.currentToken().getType());
    assertEquals("#9.2", INSIDE_LINE_COMMENT, model1.currentToken().getState());
  }

  /**
   * put your documentation comment here
   */
  public void testBlockCommentStartEatsEnd() {
    model1.insertChar('/');
    assertEquals("#0.0", 1, model1.absOffset());
    assertEquals("#0.1", FREE, model1.getStateAtCurrent());
    model1.insertChar('*');
    assertEquals("#1.0", 2, model1.absOffset());
    assertEquals("#1.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    insertGap(model1, 2);
    assertEquals("#2.0", 4, model1.absOffset());
    assertEquals("#2.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.insertChar('*');
    assertEquals("#3.0", 5, model1.absOffset());
    assertEquals("#3.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.insertChar('/');
    assertEquals("#4.0", 6, model1.absOffset());
    assertEquals("#4.1", FREE, model1.getStateAtCurrent());
    model1.move(-5);
    assertEquals("#4.0", 1, model1.absOffset());
    assertEquals("#4.1", "/*", model1.currentToken().getType());
    assertEquals("#4.1", FREE, model1.getStateAtCurrent());
    model1.delete(3);
    assertEquals("#5.0", 1, model1.absOffset());
    assertEquals("#5.1", "/*", model1.currentToken().getType());
    model1.move(1);
    assertEquals("#6.0", 2, model1.absOffset());
    assertEquals("#6.1", "/", model1.currentToken().getType());
    assertEquals("#6.2", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    assertEquals("#6.3", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#7.0", 3, model1.absOffset());
    assertEquals("#7.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
  }

  /**
   * put your documentation comment here
   */
  public void deleteLineCommentSlashOpensBlockComment() {
    model1.insertChar('/');
    model1.insertChar('/');
    model1.insertChar('*');
    model1.insertChar('\n');
    insertGap(model1, 2);
    model1.insertChar('(');
    model1.insertChar('*');
    model1.insertChar('/');
    assertEquals("#0.0", 9, model1.absOffset());
    assertEquals("#0.1", FREE, model1.getStateAtCurrent());
    model1.move(-1);
    assertEquals("#1.0", 8, model1.absOffset());
    assertEquals("#1.1", FREE, model1.getStateAtCurrent());
    assertEquals("#1.2", "/", model1.currentToken().getType());
    model1.move(-1);
    assertEquals("#2.0", 7, model1.absOffset());
    assertEquals("#2.1", FREE, model1.getStateAtCurrent());
    assertEquals("#2.2", "*", model1.currentToken().getType());
    model1.move(-1);
    assertEquals("#3.0", 6, model1.absOffset());
    assertEquals("#3.1", FREE, model1.getStateAtCurrent());
    assertEquals("#3.2", "(", model1.currentToken().getType());
    model1.move(-2);
    assertEquals("#4.0", 4, model1.absOffset());
    assertEquals("#4.1", FREE, model1.getStateAtCurrent());
    assertTrue("#4.2", model1.currentToken().isGap());
    assertEquals("#4.3", 2, model1.currentToken().getSize());
    model1.move(-1);
    assertEquals("#5.0", 3, model1.absOffset());
    assertEquals("#5.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    assertEquals("#5.2", "\n", model1.currentToken().getType());
    model1.move(-1);
    assertEquals("#6.0", 2, model1.absOffset());
    assertEquals("#6.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    assertEquals("#6.2", "*", model1.currentToken().getType());
    model1.move(-1);
    assertEquals("#7.0", 1, model1.absOffset());
    assertEquals("#7.1", FREE, model1.getStateAtCurrent());
    assertEquals("#7.2", "//", model1.currentToken().getType());
    model1.delete(-1);
    assertEquals("#8.0", 0, model1.absOffset());
    assertEquals("#8.1", FREE, model1.getStateAtCurrent());
    assertEquals("#8.2", "/*", model1.currentToken().getType());
    model1.move(7);
    assertEquals("#8.0", 7, model1.absOffset());
    assertEquals("#8.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    assertEquals("#8.2", "*/", model1.currentToken().getType());
  }

  /**
   * put your documentation comment here
   */
  public void testStartDeleteGap() {
    model1.insertChar('/');
    model1.insertChar('*');
    insertGap(model1, 2);
    model1.insertChar('*');
    model1.insertChar('/');
    model1.move(-4);
    model1.delete(2);
    assertEquals("#0.0", 2, model1.absOffset());
    assertEquals("#0.1", "*/", model1.currentToken().getType());
    assertEquals("#0.2", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.move(-2);
    assertEquals("#1.0", 0, model1.absOffset());
    assertEquals("#1.1", "/*", model1.currentToken().getType());
    assertEquals("#1.2", FREE, model1.getStateAtCurrent());
  }

  /**
   * put your documentation comment here
   */
  public void testDeleteFreesBlockCommentEnd() {
    model1.insertChar('/');
    model1.insertChar('*');
    insertGap(model1, 2);
    model1.insertChar('*');
    model1.insertChar('/');
    model1.move(-6);
    model1.delete(4);
    assertEquals("#0.0", 0, model1.absOffset());
    assertEquals("#0.1", "*", model1.currentToken().getType());
    assertEquals("#0.2", FREE, model1.currentToken().getState());
    assertEquals("#0.3", FREE, model1.getStateAtCurrent());
    model1.move(1);
    assertEquals("#1.0", 1, model1.absOffset());
    assertEquals("#1.1", "/", model1.currentToken().getType());
    assertEquals("#1.2", FREE, model1.currentToken().getState());
    assertEquals("#1.3", FREE, model1.getStateAtCurrent());
  }

  /**
   * put your documentation comment here
   */
  public void testUnmaskBlockCommentedLineComment() {
    model1.insertChar('/');
    model1.insertChar('*');
    insertGap(model1, 2);
    model1.insertChar('/');
    model1.insertChar('/');
    model1.move(-1);
    assertEquals("#0.0", 5, model1.absOffset());
    assertEquals("#0.1", "/", model1.currentToken().getType());
    assertEquals("#0.2", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    assertEquals("#0.3", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.move(-1);
    assertEquals("#0.0", 4, model1.absOffset());
    assertEquals("#0.1", "/", model1.currentToken().getType());
    assertEquals("#0.2", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    assertEquals("#0.3", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.move(-4);
    model1.delete(4);
    assertEquals("#2.0", 0, model1.absOffset());
    assertEquals("#2.1", "//", model1.currentToken().getType());
    assertEquals("#2.2", FREE, model1.currentToken().getState());
    assertEquals("#2.3", FREE, model1.getStateAtCurrent());
    model1.move(2);
    assertEquals("#3.0", 2, model1.absOffset());
    assertEquals("#3.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
  }

  /**
   * put your documentation comment here
   */
  public void testCrazyDelete() {
    model1.insertChar('/');
    model1.insertChar('/');
    model1.insertChar('*');
    insertGap(model1, 2);
    model1.insertChar('\n');
    model1.insertChar('/');
    model1.insertChar('/');
    assertEquals("#0.0", 8, model1.absOffset());
    assertEquals("#0.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    model1.move(-2);
    assertEquals("#1.0", 6, model1.absOffset());
    assertEquals("#1.1", FREE, model1.getStateAtCurrent());
    assertEquals("#1.2", "//", model1.currentToken().getType());
    assertEquals("#1.3", FREE, model1.currentToken().getState());
    model1.move(-4);
    model1.delete(4);
    assertEquals("#2.0", 2, model1.absOffset());
    assertEquals("#2.1", "/", model1.currentToken().getType());
    assertEquals("#2.2", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#3.0", 3, model1.absOffset());
    assertEquals("#3.1", "/", model1.currentToken().getType());
    assertEquals("#3.2", INSIDE_LINE_COMMENT, model1.currentToken().getState());
  }
}



