package edu.rice.cs.dynamicjava.interpreter;

import java.util.Map;
import java.util.HashMap;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.symbol.TreeClass;
import edu.rice.cs.plt.reflect.ShadowingClassLoader;
import edu.rice.cs.plt.reflect.ComposedClassLoader;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * A class loader with the additional ability of loading classes from their (type-checked)
 * AST representations.
 */
public class TreeClassLoader extends ClassLoader {
  
  private final Options _opt;
  private final Map<String, TreeCompiler.EvaluationAdapter> _adapters;
  
  public TreeClassLoader(ClassLoader parent, Options opt) {
    super(makeParent(parent));
    _opt = opt;
    _adapters = new HashMap<String, TreeCompiler.EvaluationAdapter>();
  }
  
  private static ClassLoader makeParent(ClassLoader p) {
    // Classes that must be loaded by the implementation's class loader
    // (the compiled tree classes need to be able to refer to these classes
    // and be talking about the ones that are loaded in the implementation code):
    Iterable<String> includes =
      IterUtil.make(Object.class.getName(),
                    String.class.getName(),
                    RuntimeBindings.class.getName(),
                    TreeClassLoader.class.getName(),
                    TreeCompiler.EvaluationAdapter.class.getName(),
                    TreeCompiler.BindingsFactory.class.getName());
    // For maximum flexibility, we let p load bootstrap classes
    // (except those listed above)
    ClassLoader implementationLoader =
      new ShadowingClassLoader(TreeClassLoader.class.getClassLoader(), false,
                               includes, true);
    return new ComposedClassLoader(implementationLoader, p);
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
