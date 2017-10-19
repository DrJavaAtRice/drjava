package koala.dynamicjava.tree;

import java.util.List;

import edu.rice.cs.plt.tuple.Pair;
import koala.dynamicjava.tree.visitor.Visitor;

public class Annotation extends Expression {
  
  private final ReferenceTypeName type;
  private final List<Pair<String, Expression>> values;
  
  public Annotation(ReferenceTypeName t, List<Pair<String, Expression>> vals) {
    this(t, vals, SourceInfo.NONE);
  }
  
  public Annotation(ReferenceTypeName t, List<Pair<String, Expression>> vals, SourceInfo si) {
    super(si);
    if (t == null || vals == null) { throw new IllegalArgumentException(); }
    type = t;
    values = vals;
  }
  
  public ReferenceTypeName getType() { return type; }
  public List<Pair<String, Expression>> getValues() { return values; }
  
  @Override public <T> T acceptVisitor(Visitor<T> visitor) {
    return visitor.visit(this);
  }
  
  public String toString() {
    return "(" + getClass().getName() + ": " + type + ", " + values + ")";
  }
  
}
