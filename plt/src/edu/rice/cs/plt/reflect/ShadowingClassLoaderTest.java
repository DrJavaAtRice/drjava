package edu.rice.cs.plt.reflect;

public class ShadowingClassLoaderTest extends ClassLoaderTestCase {
  
  private static final ClassLoader BASE_LOADER = ShadowingClassLoaderTest.class.getClassLoader();
  
  public void testShadowedClassLoading() throws ClassNotFoundException {
    ShadowingClassLoader l = new ShadowingClassLoader(BASE_LOADER, "edu.rice.cs.plt.reflect");
    assertLoadsSameClass(BASE_LOADER, l, "edu.rice.cs.plt.iter.IterUtil");
    assertLoadsClass(BASE_LOADER, "edu.rice.cs.plt.reflect.ReflectUtil");
    assertDoesNotLoadClass(l, "edu.rice.cs.plt.reflect.ReflectUtil");

    ShadowingClassLoader l2 = new ShadowingClassLoader(BASE_LOADER, "edu.rice.cs.plt.refl");
    assertLoadsSameClass(BASE_LOADER, l2, "edu.rice.cs.plt.iter.IterUtil");
    assertLoadsSameClass(BASE_LOADER, l2, "edu.rice.cs.plt.reflect.ReflectUtil");
  }
  
  public void testResourceLoading() {
    ShadowingClassLoader l = new ShadowingClassLoader(BASE_LOADER, "edu.rice.cs.plt.reflect");
    assertHasSameResource(BASE_LOADER, l, "edu/rice/cs/plt/iter/IterUtil.class");
    assertHasResource(BASE_LOADER, "edu/rice/cs/plt/reflect/ShadowingClassLoaderTest.class");
    assertDoesNotHaveResource(l, "edu/rice/cs/plt/reflect/ShadowingClassLoaderTest.class");
  }
  
}
