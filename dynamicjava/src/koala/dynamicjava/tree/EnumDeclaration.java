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
 * This class represents an enum declaration
 *
 * @author  Moez, Shankar
 */

public class EnumDeclaration extends ClassDeclaration {
  /**
   * Creates a new enum declaration
   * @param flags the access flags
   * @param name  the name of the enum to declare
   * @param impl  the list of implemented interfaces (a list of list of
   *              Token). Can be null.
   * @param body  the list of members declarations
   */
  public EnumDeclaration(int flags, String name, List<? extends ReferenceType> impl, List<Node> body) {
    this(flags, name, impl, body, null, 0, 0, 0, 0);
  }

  /**
   * Creates a new enum declaration
   * @param flags the access flags
   * @param name  the name of the enum to declare
   * @param impl  the list of implemented interfaces (a list of list of
   *              Token). Can be null.
   * @param body  the list of members declarations
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   */
  public EnumDeclaration(int flags, String name, List<? extends ReferenceType> impl, List<Node> body,
                          String fn, int bl, int bc, int el, int ec) {
    super(flags, name, new ReferenceType("java.lang.Enum"), impl, HandleConstructors(name, body), fn, bl, bc, el, ec);
  }
  
  static List<Node> HandleConstructors(String name, List<Node> body){
    Iterator<Node> it = body.listIterator();
    
    List<IdentifierToken> idnt1  = new LinkedList<IdentifierToken>();
    idnt1.add(new Identifier("$1"));
    List<IdentifierToken> idnt2  = new LinkedList<IdentifierToken>();
    idnt2.add(new Identifier("$2"));
    
    List<FormalParameter> addToConsDeclaration = new LinkedList<FormalParameter>();
    addToConsDeclaration.add(new FormalParameter(false, new ReferenceType("String"), "$1"));
    addToConsDeclaration.add(new FormalParameter(false, new IntType(),               "$2"));
    
    List<Expression> args = new LinkedList<Expression>();
    args.add(new QualifiedName(idnt1));
    args.add(new QualifiedName(idnt2));
    
    List<FormalParameter> consParams;
    boolean noConstructor = true;
      
    while(it.hasNext()) {
      Node current = it.next();
      if (current instanceof ConstructorDeclaration) {
        noConstructor = false;
        
        consParams = ((ConstructorDeclaration)current).getParameters();
        List<FormalParameter> newConsParam = new LinkedList<FormalParameter>();
        
        newConsParam.addAll(addToConsDeclaration);
        newConsParam.addAll(consParams);
        
        ((ConstructorDeclaration)current).setParameters(newConsParam);
        
        ((ConstructorDeclaration)current).setConstructorInvocation(new ConstructorInvocation(null, args, true));
      }
    }
    
    if (noConstructor) {
      body.add(new ConstructorDeclaration(java.lang.reflect.Modifier.PRIVATE, name, addToConsDeclaration, 
                                          new LinkedList<ReferenceType>(), 
                                          new ConstructorInvocation(null, args, true), 
                                          new LinkedList<Node>()));
    }
    return body;
  }
}
