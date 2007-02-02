package edu.rice.cs.plt.reflect;

public class PreemptingClassLoaderTest extends ClassLoaderTestCase {
  
  private static final ClassLoader BASE_LOADER = PreemptingClassLoaderTest.class.getClassLoader();
  
  public void testLoadsNonPreemptedClasses() throws ClassNotFoundException {
    PreemptingClassLoader l = new PreemptingClassLoader(BASE_LOADER, "edu.rice.cs.plt.reflect.ReflectUtil");
    assertLoadsSameClass(BASE_LOADER, l, "edu.rice.cs.plt.iter.IterUtil");
    assertLoadsSameClass(BASE_LOADER, l, "edu.rice.cs.plt.reflect.PreemptingClassLoaderTest");
  }
  
  public void testLoadsResources() throws ClassNotFoundException {
    PreemptingClassLoader l = new PreemptingClassLoader(BASE_LOADER, "edu.rice.cs.plt.reflect.ReflectUtil");
    assertHasSameResource(BASE_LOADER, l, "edu/rice/cs/plt/iter/IterUtil.class");
    assertHasSameResource(BASE_LOADER, l, "edu/rice/cs/plt/reflect/PreemptingClassLoaderTest.class");
    assertHasSameResource(BASE_LOADER, l, "edu/rice/cs/plt/reflect/ReflectUtil.class");
    assertHasSameResource(BASE_LOADER, l, "edu/rice/cs/plt/reflect/ReflectUtil$Version.class");
    assertHasSameResource(BASE_LOADER, l, "edu/rice/cs/plt/reflect/ReflectUtil$Version$1.class");
  }
  
  public void testLoadsPreemptedClasses1() throws ClassNotFoundException {
    PreemptingClassLoader l = new PreemptingClassLoader(BASE_LOADER, "edu.rice.cs.plt.reflect.ReflectUtil");
    String[] names =
    { "edu.rice.cs.plt.reflect.ReflectUtil",
      "edu.rice.cs.plt.reflect.ReflectUtil$Version",
      "edu.rice.cs.plt.reflect.ReflectUtil$Version$1" };
    
    for (String name : names) {
      assertLoadsClassAsLoader(l, name);
      assertLoadsDifferentClass(BASE_LOADER, l, name);
    }
  }
  
  public void testLoadsPreemptedClasses2() throws ClassNotFoundException {
    PreemptingClassLoader l = new PreemptingClassLoader(BASE_LOADER, "edu.rice.cs.plt.reflect.ReflectUtil$Version");
    
    assertLoadsSameClass(BASE_LOADER, l, "edu.rice.cs.plt.reflect.ReflectUtil");
    
    String[] names = {
      "edu.rice.cs.plt.reflect.ReflectUtil$Version", 
      "edu.rice.cs.plt.reflect.ReflectUtil$Version$1" };
    
    for (String name : names) {
      assertLoadsClassAsLoader(l, name);
      assertLoadsDifferentClass(BASE_LOADER, l, name);
    }
  }
  
}
