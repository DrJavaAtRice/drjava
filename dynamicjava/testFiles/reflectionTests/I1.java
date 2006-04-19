package reflectionTests;

public interface I1<T extends Comparable<T>> {
  public T method1();
  public T method2(int i, T other);
  public T method3(double d);
  public T method4(T other);
}
