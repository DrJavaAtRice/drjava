package edu.rice.cs.drjava.model.repl;

import  junit.framework.*;
import  junit.extensions.*;


/**
 * Tests for ThreadedInterpreterWrapper.
 * 
 * @version $Id$
 */
public class ThreadedInterpreterWrapperTest extends TestCase {
  private ThreadedInterpreterWrapper _interpreter =
    new ThreadedInterpreterWrapper();

  /**
   * We need to interpret something once at startup to make sure
   * the interpreter has had a chance to fully load.
   * We use this variable to make sure it happens once.
   */
  private boolean _ranOnce = false;

  /** How long should we wait for normal operations to finish? */
  public static final int TIMEOUT_MS = 5000;

  /**
   * Constructor.
   * @param   String name
   */
  public ThreadedInterpreterWrapperTest(String name) {
    super(name);
  }

  /**
   * Set up the test case.
   */
  protected void setUp() {
    _interpreter.restart();

    if (!_ranOnce) {
      _ranOnce = true;

      // Interpret "0", and wait as long as it takes to finish.
      // If this never returns, the test case will never end.
      // But in that case something is very wrong.
      // Once this has been done, subsequent interpretations should be fast.
      synchronized(this) {
        _interpreter.interpretAsynchronously( new ExpectedToCompleteCommand() {
          public String getStringToInterpret() { return "0"; }

          public void interpretationSucceeded(Object result) {
            synchronized(ThreadedInterpreterWrapperTest.this) {
              ThreadedInterpreterWrapperTest.this.notify();
            }
          }
        });

        try {
          wait();
        }
        catch (InterruptedException e) {
          fail("InterruptedException in wait() in setUp!");
        }
      }
    }
  }

  /**
   * Create a test suite for JUnit to run.
   * @return a test suite based on this class
   */
  public static Test suite() {
    return new TestSuite(ThreadedInterpreterWrapperTest.class);
  }

  public void testConstant1() {
    Object ret = new ShouldNotTimeoutCommand("5").executeCommand();
    assertEquals(new Integer(5), ret);
  }

  public void testConstant2() {
    Object ret = new ShouldNotTimeoutCommand("null").executeCommand();
    assertNull(ret);
  }

  public void testWhileTrueNonEmptyBody() {
    new ExpectedToTimeoutCommand("int i=0; while(true) i++;").executeCommand();
  }

  public void testWhileTrueEmptyBody() {
    new ExpectedToTimeoutCommand("while(true) {}").executeCommand();
  }

  /** A command that is expected to complete successfully. */
  private abstract class ExpectedToCompleteCommand
    implements ThreadedInterpreterWrapper.Command
  {
    public void interpretationInterrupted(InterpreterInterruptedException e) {
      fail("Interpreter interrupted when not expected to: " + e);
    }
  }

  /**
   * A command that is expected to complete successfully before timing out.
   */
  private class ShouldNotTimeoutCommand extends ExpectedToCompleteCommand
    implements ThreadedInterpreterWrapper.Command
  {
    private String _toInterpret;
    private Object _result;
    private boolean _interpreterFinished;

    public ShouldNotTimeoutCommand(String toInterpret) {
      _toInterpret = toInterpret;
      _result = null;
      _interpreterFinished = false;
    }

    /**
     * Executes the command. Signals an error if the interpreter doesn't
     * finish in {@link #TIMEOUT_MS} ms.
     */
    public Object executeCommand() {
      synchronized(this) {
        _interpreter.interpretAsynchronously(this);
        try {
          wait(TIMEOUT_MS);
        }
        catch (InterruptedException e) {
          fail("InterruptedException unexpected during executeCommand!");
        }

        if (!_interpreterFinished) {
          fail("Interpreter timed out interpreting string: " + _toInterpret);
        }

        return _result;
      }
    }

    public String getStringToInterpret() { return _toInterpret; }

    public void interpretationSucceeded(Object result) {
      synchronized(this) {
        _result = result;
        _interpreterFinished = true;
        notify();
      }
    }
  }
  
  /**
   * A command that is expected to time out.
   * The command is expected to not be done after {@link #TIMEOUT_MS} ms,
   * but we also don't expect it to be abort()ed before then either.
   */
  private class ExpectedToTimeoutCommand extends ExpectedToCompleteCommand
    implements ThreadedInterpreterWrapper.Command
  {
    private String _toInterpret;
    private boolean _interpreterFinished;

    public ExpectedToTimeoutCommand(String toInterpret) {
      _toInterpret = toInterpret;
      _interpreterFinished = false;
    }

    /**
     * Executes the command.
     * Expects that after {@link #TIMEOUT_MS} ms, the command will not have
     * terminated. Then, it will abort() the command, and once again
     * wait up to {@link #TIMEOUT_MS} ms. If the interruption is recorded,
     * this command executed as expected.
     */
    public void executeCommand() {
      synchronized(this) {
        _interpreter.interpretAsynchronously(this);
        try {
          wait(TIMEOUT_MS);
        }
        catch (InterruptedException e) {
          fail("InterruptedException unexpected during executeCommand!");
        }

        assertTrue("Interpreter did not time out interpreting string: " +
                     _toInterpret,
                   ! _interpreterFinished);

        _interpreter.abort();
        try {
          wait(TIMEOUT_MS);
        }
        catch (InterruptedException e) {
          fail("InterruptedException unexpected during executeCommand!");
        }

        assertTrue("Interpreter did not successfully abort " +
                     "when executing string: " + _toInterpret,
                   _interpreterFinished);

      }
    }

    public String getStringToInterpret() { return _toInterpret; }

    public void interpretationSucceeded(Object result) {
      fail("Interpreter succeeded when not expected to. Returned: " + result);
    }

    public void interpretationInterrupted(InterpreterInterruptedException e) {
      synchronized(this) {
        _interpreterFinished = true;
        notify();
      }
    }
  }
  
}
