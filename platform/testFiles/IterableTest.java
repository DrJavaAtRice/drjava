public class IterableTest {
  
  public static void method(java.util.Collection<String> iter) {
    for (String s : iter) { System.out.println(s); }
  }
    
  public static void method(java.lang.Iterable<String> iter) {
    for (String s : iter) { System.out.println(s); }
  }

}
