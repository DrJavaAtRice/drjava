package  edu.rice.cs.drjava;

/**
 * Interface for repl interpreters.
 * @version $Id$
 */
public interface JavaInterpreter {
  public static final Object NO_RESULT = new Object();

  /**
   * put your documentation comment here
   * @param s
   * @return 
   */
  public Object interpret(String s);

  /**
   * put your documentation comment here
   * @param path
   */
  public void addClassPath(String path);
}



