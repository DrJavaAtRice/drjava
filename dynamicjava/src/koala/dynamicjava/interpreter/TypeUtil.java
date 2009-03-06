package koala.dynamicjava.interpreter;

import koala.dynamicjava.tree.Node;
import koala.dynamicjava.tree.Expression;
import koala.dynamicjava.tree.SourceInfo;
import koala.dynamicjava.tree.TypeName;
import koala.dynamicjava.tree.visitor.Visitor;

public class TypeUtil {
  
  
  /**
   * @return  An expression for representing arbitrary "imaginary" syntax at
   *          the given location.  Useful for invoking {@code TypeSystem} methods
   *          that require an expression.  The result cannot be visited,
   *          but it can have properties set and be used to build other {@code Node}s.
   */
  public static Expression makeEmptyExpression() {
    return new Expression(SourceInfo.NONE) {
      public <T> T acceptVisitor(Visitor<T> v) { 
        throw new IllegalArgumentException("Cannot visit an empty expression");
      }
    };
  }

  /**
   * @return  An expression for representing arbitrary "imaginary" syntax at
   *          the given location.  Useful for invoking {@code TypeSystem} methods
   *          that require an expression.  The result cannot be visited,
   *          but it can have properties set and be used to build other {@code Node}s.
   */
  public static Expression makeEmptyExpression(Node location) {
    return new Expression(location.getSourceInfo()) {
      public <T> T acceptVisitor(Visitor<T> v) { 
        throw new IllegalArgumentException("Cannot visit an empty expression");
      }
    };
  }
  /**
   * @return  A type name for representing arbitrary "imaginary" syntax at
   *          the given location.  The result cannot be visited,
   *          but it can have properties set and be used to build other {@code Node}s.
   */
  public static TypeName makeEmptyTypeName() {
    return new TypeName(SourceInfo.NONE) {
      public <T> T acceptVisitor(Visitor<T> v) { 
        throw new IllegalArgumentException("Cannot visit an empty type name");
      }
    };
  }

  /**
   * @return  A type name for representing arbitrary "imaginary" syntax at
   *          the given location.  The result cannot be visited,
   *          but it can have properties set and be used to build other {@code Node}s.
   */
  public static TypeName makeEmptyTypeName(Node location) {
    return new TypeName(location.getSourceInfo()) {
      public <T> T acceptVisitor(Visitor<T> v) { 
        throw new IllegalArgumentException("Cannot visit an empty type name");
      }
    };
  }

}
