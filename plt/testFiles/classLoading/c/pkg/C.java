package pkg;

public class C implements IntBox {
  public static String NAME = "C";
  public static String getName() { return NAME; }
  public int get() { return new A().get() + 2; }
}
