package koala.dynamicjava.interpreter.context;

//import koala.dynamicjava.interpreter.context.VariableContext;

/**
 * To store the variables
 */
public abstract class AbstractVariable {
  /**
   * The constant name
   */
  public String name;
  
  /**
   * Sets the variable in the current scope
   */
  public abstract void set(VariableContext ctx, Object value);
  
  /**
   * Sets the variable in the current scope
   */
  public abstract Object get(VariableContext ctx);
  
  /**
   * Returns the hashCode
   */
  public int hashCode() {
    return name.hashCode();
  }
  
  public String toString(){
    return name;
  }
}

/**
 * To store the variables
 */
class Variable extends AbstractVariable {
  /**
   * Creates a new variable
   */
  public Variable(String s) {
    name = s;
  }
  
  /**
   * Sets the variable in the current scope
   */
  public void set(VariableContext ctx, Object value) {
    ctx.scope.put(name, value);
  }
  
  /**
   * Sets the variable in the current scope
   */
  public Object get(VariableContext ctx) {
    return ctx.scope.get(name);
  }
}

/**
 * To store the constants
 */
class Constant extends AbstractVariable {
  /**
   * Creates a new variable
   */
  public Constant(String s) {
    name = s;
  }
  
  /**
   * Sets the variable in the current scope
   */
  public void set(VariableContext ctx, Object value) {
    ctx.cscope.put(name, value);
  }
  
  /**
   * Sets the variable in the current scope
   */
  public Object get(VariableContext ctx) {
    return ctx.cscope.get(name);
  }
}

