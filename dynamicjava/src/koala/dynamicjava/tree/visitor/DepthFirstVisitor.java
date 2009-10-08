package koala.dynamicjava.tree.visitor;

import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Pair;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.EnumDeclaration.EnumConstant;
import koala.dynamicjava.tree.tiger.GenericClassDeclaration;
import koala.dynamicjava.tree.tiger.GenericInterfaceDeclaration;
import koala.dynamicjava.tree.tiger.GenericReferenceTypeName;
import koala.dynamicjava.tree.tiger.HookTypeName;
import koala.dynamicjava.tree.tiger.PolymorphicAnonymousAllocation;
import koala.dynamicjava.tree.tiger.PolymorphicAnonymousInnerAllocation;
import koala.dynamicjava.tree.tiger.PolymorphicConstructorDeclaration;
import koala.dynamicjava.tree.tiger.PolymorphicInnerAllocation;
import koala.dynamicjava.tree.tiger.PolymorphicMethodDeclaration;
import koala.dynamicjava.tree.tiger.PolymorphicObjectMethodCall;
import koala.dynamicjava.tree.tiger.PolymorphicSimpleAllocation;
import koala.dynamicjava.tree.tiger.PolymorphicStaticMethodCall;
import koala.dynamicjava.tree.tiger.PolymorphicSuperMethodCall;
import koala.dynamicjava.tree.tiger.TypeParameter;

/**
 * A visitor that traverses an AST in depth-first order.  Clients should invoke run() rather than
 * acceptVisitor() to invoke instances.
 * 
 * To design a visitor that may perform an action or prune the traversal for each node, override
 * run(), which by default just calls {@code n.acceptVisitor(this)}.  To customize behavior for
 * specific node types, override the appropriate visit() method, which by default simply invokes
 * a recur() method for each subtree. 
 * 
 * It is very important that changes to the AST class declarations be reflected here (otherwise, for example,
 * a newly-added field will be silently ignored by the traversal).
 */
public class DepthFirstVisitor implements Visitor<Void>, Runnable1<Node> {
  
  public void run(Node n) {
    n.acceptVisitor(this);
  }
  
  protected void recur(Node n) {
    if (n != null) { run(n); }
  }
  
  protected void recur(Node... ns) {
    if (ns != null) {
      for (Node n : ns) {
        if (n != null) { run(n); }
      }
    }
  }
  
  protected void recur(Iterable<? extends Node> l) {
    if (l != null) {
      for (Node n : l) {
        if (n != null) { run(n); }
      }
    }
  }

  protected void recur(Option<? extends Node> opt) {
    if (opt != null && opt.isSome()) {
      Node n = opt.unwrap();
      if (n != null) { run(n); }
    }
  }
  
  protected void recurOnLists(Iterable<? extends Iterable<? extends Node>> l) {
    if (l != null) {
      for (Iterable<? extends Node> nl : l) {
        if (nl != null) {
          for (Node n :nl) {
            if (n != null) { run(n); }
          }
        }
      }
    }
  }

  protected void recurOnPairSeconds(Iterable<? extends Pair<?, ? extends Node>> l) {
    if (l != null) {
      for (Pair<?, ? extends Node> p : l) {
        if (p != null) {
          Node n = p.second();
          if (n != null) { run(n); }
        }
      }
    }
  }

  public Void visit(CompilationUnit node) {
    recur(node.getPackage());
    recur(node.getImports());
    recur(node.getDeclarations());
    return null;
  }

  public Void visit(PackageDeclaration node) {
    recur(node.getModifiers());
    return null;
  }

  public Void visit(ImportDeclaration node) {
    return null;
  }

  public Void visit(EmptyStatement node) {
    return null;
  }

  public Void visit(ExpressionStatement node) {
    recur(node.getExpression());
    return null;
  }

  public Void visit(WhileStatement node) {
    recur(node.getCondition());
    recur(node.getBody());
    return null;
  }

  public Void visit(ForStatement node) {
    recur(node.getInitialization());
    recur(node.getCondition());
    recur(node.getUpdate());
    recur(node.getBody());
    return null;
  }

  public Void visit(ForEachStatement node) {
    recur(node.getParameter());
    recur(node.getCollection());
    recur(node.getBody());
    return null;
  }

  public Void visit(DoStatement node) {
    recur(node.getBody());
    recur(node.getCondition());
    return null;
  }

  public Void visit(SwitchStatement node) {
    recur(node.getSelector());
    recur(node.getBindings());
    return null;
  }

  public Void visit(SwitchBlock node) {
    recur(node.getExpression());
    recur(node.getStatements());
    return null;
  }

  public Void visit(LabeledStatement node) {
    recur(node.getStatement());
    return null;
  }

  public Void visit(BreakStatement node) {
    return null;
  }

  public Void visit(TryStatement node) {
    recur(node.getTryBlock());
    recur(node.getCatchStatements());
    recur(node.getFinallyBlock());
    return null;
  }

  public Void visit(CatchStatement node) {
    recur(node.getException());
    recur(node.getBlock());
    return null;
  }

  public Void visit(ThrowStatement node) {
    recur(node.getExpression());
    return null;
  }

  public Void visit(ReturnStatement node) {
    recur(node.getExpression());
    return null;
  }

  public Void visit(SynchronizedStatement node) {
    recur(node.getLock());
    recur(node.getBody());
    return null;
  }

  public Void visit(ContinueStatement node) {
    return null;
  }

  public Void visit(IfThenStatement node) {
    recur(node.getCondition());
    recur(node.getThenStatement());
    return null;
  }

  public Void visit(IfThenElseStatement node) {
    recur(node.getCondition());
    recur(node.getThenStatement());
    recur(node.getElseStatement());
    return null;
  }

  public Void visit(AssertStatement node) {
    recur(node.getCondition());
    recur(node.getFailString());
    return null;
  }

  public Void visit(Literal node) {
    return null;
  }

  public Void visit(ThisExpression node) {
    return null;
  }

  public Void visit(AmbiguousName node) {
    return null;
  }

  public Void visit(VariableAccess node) {
    return null;
  }

  public Void visit(SimpleFieldAccess node) {
    return null;
  }

  public Void visit(ObjectFieldAccess node) {
    recur(node.getExpression());
    return null;
  }

  public Void visit(StaticFieldAccess node) {
    recur(node.getFieldType());
    return null;
  }

  public Void visit(SuperFieldAccess node) {
    return null;
  }

  public Void visit(ArrayAccess node) {
    recur(node.getExpression());
    recur(node.getCellNumber());
    return null;
  }

  public Void visit(ObjectMethodCall node) {
    recur(node.getExpression());
    if (node instanceof PolymorphicObjectMethodCall) {
      recur(((PolymorphicObjectMethodCall) node).getTypeArguments());
    }
    recur(node.getArguments());
    return null;
  }

  public Void visit(SimpleMethodCall node) {
    recur(node.getArguments());
    return null;
  }

  public Void visit(StaticMethodCall node) {
    recur(node.getMethodType());
    if (node instanceof PolymorphicStaticMethodCall) {
      recur(((PolymorphicStaticMethodCall) node).getTypeArguments());
    }
    recur(node.getArguments());
    return null;
  }

  public Void visit(ConstructorCall node) {
    recur(node.getExpression());
    recur(node.getArguments());
    return null;
  }

  public Void visit(SuperMethodCall node) {
    if (node instanceof PolymorphicSuperMethodCall) {
      recur(((PolymorphicSuperMethodCall) node).getTypeArguments());
    }
    recur(node.getArguments());
    return null;
  }

  public Void visit(BooleanTypeName node) {
    return null;
  }

  public Void visit(ByteTypeName node) {
    return null;
  }

  public Void visit(ShortTypeName node) {
    return null;
  }

  public Void visit(CharTypeName node) {
    return null;
  }

  public Void visit(IntTypeName node) {
    return null;
  }

  public Void visit(LongTypeName node) {
    return null;
  }

  public Void visit(FloatTypeName node) {
    return null;
  }

  public Void visit(DoubleTypeName node) {
    return null;
  }

  public Void visit(VoidTypeName node) {
    return null;
  }

  public Void visit(ReferenceTypeName node) {
    if (node instanceof TypeParameter) {
      recur(((TypeParameter) node).getBound());
      recur(((TypeParameter) node).getInterfaceBounds());
    }
    return null;
  }

  public Void visit(GenericReferenceTypeName node) {
    recurOnLists(node.getTypeArguments());
    return null;
  }

  public Void visit(ArrayTypeName node) {
    recur(node.getElementType());
    return null;
  }

  public Void visit(HookTypeName node) {
    recur(node.getUpperBound());
    recur(node.getLowerBound());
    return null;
  }

  public Void visit(TypeExpression node) {
    recur(node.getType());
    return null;
  }

  public Void visit(PostIncrement node) {
    recur(node.getExpression());
    return null;
  }

  public Void visit(PostDecrement node) {
    recur(node.getExpression());
    return null;
  }

  public Void visit(PreIncrement node) {
    recur(node.getExpression());
    return null;
  }

  public Void visit(PreDecrement node) {
    recur(node.getExpression());
    return null;
  }

  public Void visit(ArrayInitializer node) {
    recur(node.getElementType());
    recur(node.getCells());
    return null;
  }

  public Void visit(ArrayAllocation node) {
    recur(node.getElementType());
    recur(node.getSizes());
    recur(node.getInitialization());
    return null;
  }

  public Void visit(SimpleAllocation node) {
    if (node instanceof PolymorphicSimpleAllocation) {
      recur(((PolymorphicSimpleAllocation) node).getTypeArguments());
    }
    recur(node.getCreationType());
    recur(node.getArguments());
    return null;
  }

  public Void visit(AnonymousAllocation node) {
    if (node instanceof PolymorphicAnonymousAllocation) {
      recur(((PolymorphicAnonymousAllocation) node).getTypeArguments());
    }
    recur(node.getCreationType());
    recur(node.getArguments());
    recur(node.getMembers());
    return null;
  }

  public Void visit(InnerAllocation node) {
    recur(node.getExpression());
    if (node instanceof PolymorphicInnerAllocation) {
      recur(((PolymorphicInnerAllocation) node).getTypeArguments());
    }
    recur(node.getClassTypeArguments());
    recur(node.getArguments());
    return null;
  }

  public Void visit(AnonymousInnerAllocation node) {
    recur(node.getExpression());
    if (node instanceof PolymorphicAnonymousInnerAllocation) {
      recur(((PolymorphicAnonymousInnerAllocation) node).getTypeArguments());
    }
    recur(node.getClassTypeArguments());
    recur(node.getArguments());
    recur(node.getMembers());
    return null;
  }

  public Void visit(CastExpression node) {
    recur(node.getTargetType());
    recur(node.getExpression());
    return null;
  }

  public Void visit(NotExpression node) {
    recur(node.getExpression());
    return null;
  }

  public Void visit(ComplementExpression node) {
    recur(node.getExpression());
    return null;
  }

  public Void visit(PlusExpression node) {
    recur(node.getExpression());
    return null;
  }

  public Void visit(MinusExpression node) {
    recur(node.getExpression());
    return null;
  }

  public Void visit(MultiplyExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(DivideExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(RemainderExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(AddExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(SubtractExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(ShiftLeftExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(ShiftRightExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(UnsignedShiftRightExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(LessExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(GreaterExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(LessOrEqualExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(GreaterOrEqualExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(InstanceOfExpression node) {
    recur(node.getExpression());
    recur(node.getReferenceType());
    return null;
  }

  public Void visit(EqualExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(NotEqualExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(BitAndExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(ExclusiveOrExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(BitOrExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(AndExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(OrExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(ConditionalExpression node) {
    recur(node.getConditionExpression());
    recur(node.getIfTrueExpression());
    recur(node.getIfFalseExpression());
    return null;
  }

  public Void visit(SimpleAssignExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(MultiplyAssignExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(DivideAssignExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(RemainderAssignExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(AddAssignExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(SubtractAssignExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(ShiftLeftAssignExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(ShiftRightAssignExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(UnsignedShiftRightAssignExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(BitAndAssignExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(ExclusiveOrAssignExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(BitOrAssignExpression node) {
    recur(node.getLeftExpression());
    recur(node.getRightExpression());
    return null;
  }

  public Void visit(BlockStatement node) {
    recur(node.getStatements());
    return null;
  }

  public Void visit(ClassDeclaration node) {
    recur(node.getModifiers());
    if (node instanceof GenericClassDeclaration) {
      recur(((GenericClassDeclaration) node).getTypeParameters());
    }
    recur(node.getSuperclass());
    recur(node.getInterfaces());
    recur(node.getMembers());
    return null;
  }

  public Void visit(InterfaceDeclaration node) {
    recur(node.getModifiers());
    if (node instanceof GenericInterfaceDeclaration) {
      recur(((GenericInterfaceDeclaration) node).getTypeParameters());
    }
    recur(node.getInterfaces());
    recur(node.getMembers());
    return null;
  }

  public Void visit(ConstructorDeclaration node) {
    recur(node.getModifiers());
    if (node instanceof PolymorphicConstructorDeclaration) {
      recur(((PolymorphicConstructorDeclaration) node).getTypeParameters());
    }
    recur(node.getParameters());
    recur(node.getExceptions());
    recur(node.getConstructorCall());
    recur(node.getStatements());
    return null;
  }

  public Void visit(MethodDeclaration node) {
    recur(node.getModifiers());
    if (node instanceof PolymorphicMethodDeclaration) {
      recur(((PolymorphicMethodDeclaration) node).getTypeParameters());
    }
    recur(node.getReturnType());
    recur(node.getParameters());
    recur(node.getExceptions());
    recur(node.getBody());
    return null;
  }

  public Void visit(FormalParameter node) {
    recur(node.getType());
    return null;
  }

  public Void visit(FieldDeclaration node) {
    recur(node.getModifiers());
    recur(node.getType());
    recur(node.getInitializer());
    return null;
  }

  public Void visit(VariableDeclaration node) {
    recur(node.getModifiers());
    recur(node.getType());
    recur(node.getInitializer());
    return null;
  }

  public Void visit(EnumConstant node) {
    recur(node.getModifiers());
    recur(node.getArguments());
    recur(node.getClassBody());
    return null;
  }

  public Void visit(ClassInitializer node) {
    recur(node.getBlock());
    return null;
  }

  public Void visit(InstanceInitializer node) {
    recur(node.getBlock());
    return null;
  }

  public Void visit(ModifierSet node) {
    recur(node.getAnnotations());
    return null;
  }

  public Void visit(Annotation node) {
    recur(node.getType());
    recurOnPairSeconds(node.getValues());
    return null;
  }

}
