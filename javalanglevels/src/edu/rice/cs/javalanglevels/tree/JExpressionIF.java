package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

public interface JExpressionIF {
  public SourceInfo getSourceInfo();

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public void visit(JExpressionIFVisitor_void visitor);
  public void outputHelp(TabPrintWriter writer);
}
