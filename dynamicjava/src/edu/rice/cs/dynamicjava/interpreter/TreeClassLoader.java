package edu.rice.cs.dynamicjava.interpreter;

import java.util.Map;
import java.util.HashMap;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.symbol.TreeClass;

/**
 * A class loader with the additional ability of loading classes from their (type-checked)
 * AST representations.
 */
public class TreeClassLoader extends ClassLoader {
  
  private final Options _opt;
  private final Map<String, TreeCompiler.EvaluationAdapter> _adapters;
  
  public TreeClassLoader(ClassLoader parent, Options opt) {
    super(parent);
    _opt = opt;
    _adapters = new HashMap<String, TreeCompiler.EvaluationAdapter>();
  }
  
  public Class<?> loadTree(TreeClass treeClass) {
    TreeCompiler compiler = new TreeCompiler(treeClass, _opt);
    byte[] bytes = compiler.bytecode();
    _adapters.put(treeClass.fullName(), compiler.evaluationAdapter());
    return defineClass(treeClass.fullName(), bytes, 0, bytes.length);
  }
  
  public TreeCompiler.EvaluationAdapter getAdapter(String className) {
    return _adapters.get(className);
  }
    
}
