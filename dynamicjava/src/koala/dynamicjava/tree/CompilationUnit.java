package koala.dynamicjava.tree;

import java.util.List;

import koala.dynamicjava.tree.visitor.Visitor;

public class CompilationUnit extends Node {
  
  private PackageDeclaration package_; // may be null
  private List<ImportDeclaration> imports;
  private List<Node> declarations;
  
  public CompilationUnit(PackageDeclaration pkg, List<ImportDeclaration> imp, List<Node> decls) {
    this(pkg, imp, decls, SourceInfo.NONE);
  }
  
  public CompilationUnit(PackageDeclaration pkg, List<ImportDeclaration> imp, List<Node> decls, SourceInfo si) {
    super(si);
    
    if (imp == null) throw new IllegalArgumentException("imp == null");
    if (decls == null) throw new IllegalArgumentException("decls == null");
    
    package_ = pkg;
    imports = imp;
    declarations = decls;
  }
  
  public PackageDeclaration getPackage() {
    return package_;
  }
  
  public List<ImportDeclaration> getImports() {
    return imports;
  }
  
  public List<Node> getDeclarations() {
    return declarations;
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
    return "("+getClass().getName()+": "+getPackage()+", " + getImports() + ", " + getDeclarations() + ")";
  }
}
