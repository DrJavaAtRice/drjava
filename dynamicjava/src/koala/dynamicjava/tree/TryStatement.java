/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.tree;

import java.util.*;

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents the try statement nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/26
 */

public class TryStatement extends Statement {
  /**
   * The try block
   */
  private Node tryBlock;
  
  /**
   * The catch statements
   */
  private List<CatchStatement> catchStatements;
  
  /**
   * The finally block
   */
  private Node finallyBlock;
  
  /**
   * Creates a new while statement
   * @param tryB   the try block
   * @param catchL the catch list
   * @param fin    the finally block
   * @param fn     the filename
   * @param bl     the begin line
   * @param bc     the begin column
   * @param el     the end line
   * @param ec     the end column
   */
  public TryStatement(Node tryB, List<CatchStatement> catchL, Node fin,
                      String fn, int bl, int bc, int el, int ec) {
    super(fn, bl, bc, el, ec);
    tryBlock        = tryB;
    catchStatements = catchL;
    finallyBlock    = fin;
  }
  
  /**
   * Gets the try block
   */
  public Node getTryBlock() {
    return tryBlock;
  }
  
  /**
   * Gets the catch statements
   */
  public List<CatchStatement> getCatchStatements() {
    return catchStatements;
  }
  
  /**
   * Gets the finally block
   */
  public Node getFinallyBlock() {
    return finallyBlock;
  }
  
  /**
   * Allows a visitor to traverse the tree
   * @param visitor the visitor to accept
   */
  public <T> T acceptVisitor(Visitor<T> visitor) {
    return visitor.visit(this);
  }
    /**
   * Implementation of toString for use in unit testing
   */
  public String toString() {
    return "("+getClass().getName()+": "+getTryBlock()+" "+getCatchStatements()+" "+getFinallyBlock()+")";
  }
}
