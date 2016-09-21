package reflectionTests;

public class C1 implements I1<String> {
  static String M1 = "method1";
  static String M2 = "method2";
  static String M3 = "method3";
  static String M4 = "method4";
  
  public String method1() { return M1; }
  public String method2(int i, String other) { return M2; }
  public String method3(double d) { return M3; }
  public String method4(String other) { return M4; }
}
