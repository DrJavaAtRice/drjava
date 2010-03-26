package edu.rice.cs.dynamicjava.interpreter;

import java.util.Map;
import java.util.HashMap;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.symbol.TreeClass;
import edu.rice.cs.plt.reflect.AbstractClassLoader;
import edu.rice.cs.plt.reflect.ShadowingClassLoader;
import edu.rice.cs.plt.reflect.ComposedClassLoader;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * A class loader with the additional ability of loading classes from their (type-checked)
 * AST representations.
 */
public class TreeClassLoader extends AbstractClassLoader {
  
  private final Options _opt;
  // trees that have been declared but not yet loaded
  private final Map<String, TreeClass> _registeredTrees;
  private final Map<String, TreeCompiler.EvaluationAdapter> _adapters;
  
  public TreeClassLoader(ClassLoader parent, Options opt) {
    this(parent, opt, new HashMap<String, TreeClass>());
  }
  
  private TreeClassLoader(ClassLoader parent, Options opt, Map<String, TreeClass> registeredTrees) {
    super(makeParent(parent, registeredTrees.keySet()));
    _opt = opt;
    _registeredTrees = registeredTrees;
    _adapters = new HashMap<String, TreeCompiler.EvaluationAdapter>();
  }
  
  private static ClassLoader makeParent(ClassLoader p, Iterable<String> registeredNames) {
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
    // Allow shadowing by hiding parent classes redefined here
    ClassLoader parentLoader = new ShadowingClassLoader(p, true, registeredNames, true);
    return new ComposedClassLoader(implementationLoader, parentLoader);
  }
  
  public void registerTree(TreeClass treeClass) {
    _registeredTrees.put(treeClass.fullName(), treeClass);
  }

  protected Class<?> findClass(String name) throws ClassNotFoundException {
    TreeClass treeClass = _registeredTrees.get(name);
    if (treeClass == null) { throw new ClassNotFoundException(); }
    else {
      TreeCompiler compiler = new TreeCompiler(treeClass, _opt);
      byte[] bytes = compiler.bytecode();
      _adapters.put(name, compiler.evaluationAdapter());
      
      // define package
      definePackageForClass(name);
      
      Class<?> result = defineClass(name, bytes, 0, bytes.length);
      return result;
    }
  }
  
  public TreeCompiler.EvaluationAdapter getAdapter(String className) {
    return _adapters.get(className);
  }
    
}
