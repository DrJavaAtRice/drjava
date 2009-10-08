/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

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
 * This class represents the foreach statement nodes of the syntax tree
 *
 */

public class ForEachStatement extends ForSlashEachStatement implements ContinueTarget {
  /**
   * The initialization statements
   */
  private FormalParameter parameter;
  
  /**
   * The update statements
   */
  private Expression collection;
  
  /**
   * The body of this statement
   */
  private Node body;
  
  /**
   * The labels
   */
  private List<String> labels;
  
  
  /*
   * The list of variables used for expansion of the foreach node.
   */
  private List<String> vars;
  
  /**
   * Creates a new for statement
   * @exception IllegalArgumentException if body is null
   */
  public ForEachStatement(FormalParameter para, Expression collection, Node body) {
    this(para, collection, body, SourceInfo.NONE);
  }
  
  /**
   * Creates a new for statement
   * @param body  the body
   * @exception IllegalArgumentException if body is null
   */
  public ForEachStatement(FormalParameter para, Expression coll, Node body,
                      SourceInfo si) {
    super(si);
    
    if (body == null) throw new IllegalArgumentException("body == null");
    
    parameter = para;
    collection     = coll;
    this.body      = body;
    labels         = new LinkedList<String>();
    vars = new LinkedList<String>();
  }
  
  
  public void addVar(String s){
    vars.add(s);
  }
  
  public List<String> getVars(){
    return vars;
  }
  
  /**
   * Gets the initialization statements
   */
  public FormalParameter getParameter() {
    return parameter;
  }
  
  /**
   * Sets the initialization statements
   */
  public void setParameter(FormalParameter l) {
    parameter = l;
  }
  
  /**
   * Gets the condition to evaluate at each loop
   */
  public Expression getCollection() {
    return collection;
  }
  
  /**
   * Sets the condition to evaluate
   */
  public void setCollection(Expression e) {
    collection = e;
  }
  
  /**
   * Returns the body of this statement
   */
  public Node getBody() {
    return body;
  }
  
  /**
   * Sets the body of this statement
   * @exception IllegalArgumentException if node is null
   */
  public void setBody(Node node) {
    if (node == null) throw new IllegalArgumentException("node == null");
    body = node;
  }
  
  /**
   * Adds a label to this statement
   * @param label the label to add
   * @exception IllegalArgumentException if label is null
   */
  public void addLabel(String label) {
    if (label == null) throw new IllegalArgumentException("label == null");
    
    labels.add(label);
  }
  
  /**
   * Test whether this statement has the given label
   * @return true if this statement has the given label
   */
  public boolean hasLabel(String label) {
    return labels.contains(label);
  }
  
  /**
   * Allows a visitor to traverse the tree
   * @param visitor the visitor to accept
   */
  public <T> T acceptVisitor(Visitor<T> visitor) {
    return visitor.visit(this);
  }
  
  public String toString(){
    return "("+getClass().getName()+": "+getParameter()+" "+getCollection()+" "+getBody()+")";
  }
}
