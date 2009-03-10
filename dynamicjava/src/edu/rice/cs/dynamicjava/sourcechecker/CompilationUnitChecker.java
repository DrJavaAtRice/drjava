package edu.rice.cs.dynamicjava.sourcechecker;

import java.util.LinkedList;
import java.util.List;

import koala.dynamicjava.interpreter.NodeProperties;
import koala.dynamicjava.interpreter.error.ExecutionError;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.tiger.*;
import koala.dynamicjava.tree.visitor.*;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.*;
import edu.rice.cs.dynamicjava.symbol.*;

public class CompilationUnitChecker {
  
  /** A context supporting packages and imports, and with access to all types that might be referenced. */
  private final TypeContext _context;
  private final Options _opt;
  
  public CompilationUnitChecker(TypeContext context, Options opt) {
    _context = context;
    _opt = opt;
  }
  
  public Iterable<BodyChecker> check(CompilationUnit u) throws CheckerException {
    try {
      TypeContext c = _context;
      PackageDeclaration pkg = u.getPackage();
      if (pkg != null) { c = c.setPackage(pkg.getName()); }
      for (ImportDeclaration imp : u.getImports()) {
        c = imp.acceptVisitor(new StatementChecker(c, _opt));
      }
      DeclarationVisitor dv = new DeclarationVisitor(c, _opt);
      List<BodyChecker> results = new LinkedList<BodyChecker>();
      for (Node decl : u.getDeclarations()) {
        results.add(decl.acceptVisitor(dv));
      }
      return results;
    }
    catch (ExecutionError e) { throw new CheckerException(e); }
  }
  
  public static class BodyChecker {
    private final ClassMemberChecker _checker;
    private final Iterable<Node> _members;
    private BodyChecker(ClassMemberChecker checker, Iterable<Node> members) { _checker = checker; _members = members; }
    public void check() throws CheckerException {
      try { _checker.checkBodies(_members); }
      catch (ExecutionError e) { throw new CheckerException(e); }
    }
  }
  
  private static class DeclarationVisitor extends AbstractVisitor<BodyChecker> {
    private final TypeContext _context;
    private final Options _opt;
    
    public DeclarationVisitor(TypeContext context, Options opt) {
      _context = context;
      _opt = opt;
    }

    @Override public BodyChecker visit(ClassDeclaration node) {
      return handleTypeDeclaration(node);
    }
    
    @Override public BodyChecker visit(InterfaceDeclaration node) {
      return handleTypeDeclaration(node);
    }
    
    private BodyChecker handleTypeDeclaration(TypeDeclaration node) {
      DJClass c = NodeProperties.getDJClass(node);
      
      TypeContext sigContext = new ClassSignatureContext(_context, c, _context.getClassLoader());
      TypeNameChecker sigChecker = new TypeNameChecker(sigContext, _opt);

      final TypeParameter[] tparams;
      if (node instanceof GenericClassDeclaration) {
        tparams = ((GenericClassDeclaration) node).getTypeParameters();
      }
      else if (node instanceof GenericInterfaceDeclaration) {
        tparams = ((GenericInterfaceDeclaration) node).getTypeParameters();
      }
      else { tparams = new TypeParameter[0]; }
      sigChecker.checkTypeParameters(tparams);

      if (node instanceof ClassDeclaration) {
        sigChecker.check(((ClassDeclaration) node).getSuperclass());
      }
      if (node.getInterfaces() != null) {
        for (TypeName tn : node.getInterfaces()) { sigChecker.check(tn); }
      }

      ClassMemberChecker classChecker = new ClassMemberChecker(new ClassContext(sigContext, c), _opt); 
      if (node instanceof InterfaceDeclaration) {
        classChecker.checkInterfaceSignatures(node.getMembers());
      }
      else {
        classChecker.checkClassSignatures(node.getMembers());
      }
      return new BodyChecker(classChecker, node.getMembers());
    }

  }

}
