package edu.rice.cs.plt.tuple;

import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.LambdaUtil;

/**
 * A wrapper for optional values.  This provides a strictly-typed alternative to using
 * {@code null} to represent the absence of a value.  Options have two variants: "some"
 * and "none."  The "some" case is represented by {@link Wrapper}s; the "none" case is
 * represented by the {@link Null} singleton.  {@code Option} values may be decomposed
 * using an {@link OptionVisitor}.
 */
public abstract class Option<T> extends Tuple {
  
  /** Calls the appropriate case in the visitor */
  public abstract <Ret> Ret apply(OptionVisitor<? super T, ? extends Ret> visitor);
  

  /** Create a "some" case wrapper for the given value */
  public static <T> Option<T> some(T val) { return new Wrapper<T>(val); }
  
  /** 
   * Return the "none" case singleton, cast (unsafe formally, but safe in practice) to the 
   * appropriate type
   */
  @SuppressWarnings("unchecked")
  public static <T> Option<T> none() {
    Option<Object> result = Null.INSTANCE;
    return (Option<T>) result;
  }
  
  /** 
   * Access the value in the given {@code Option}, or throw the given exception in the "none" case
   * @return  The value of {@code opt} if it is a "some"
   * @throws RuntimeException  If {@code opt} is a "none"
   */
  public static <T> T unwrap (Option<T> opt, RuntimeException forNone) {
    return unwrap(opt, LambdaUtil.valueThunk(forNone));
  }
  
  /**
   * Access the value in the given {@code Option}, or throw the exception produced by {@code forNone}
   * in the "none" case
   * @return  The value of {@code opt} if it is a "some"
   * @throws RuntimeException  If {@code opt} is a "none"
   */
  public static <T> T unwrap(Option<T> opt, final Thunk<? extends RuntimeException> forNone) {
    return opt.apply(new OptionVisitor<T, T>() {
      public T forSome(T value) { return value; }
      public T forNone() { throw forNone.value(); }
    });
  }
  
}
