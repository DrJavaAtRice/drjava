package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.util.UnexpectedException;

/**
 * A class to wrap up a JavaInterpreter, encapsulating a Thread which
 * allows interpretation to be done asynchronously.
 * 
 * @version $Id$
 */
public class ThreadedInterpreterWrapper {
  /** The actual interpreter. */
  private JavaInterpreter _realInterpreter;

  /** The thread to do the interpretation asynchronously. */
  private final Thread _thread;

  /**
   * The current command being interpreted (if busy) or
   * null if not busy now.
   * All accesses to this variable must be synchronized on
   * ThreadedInterpreterWrapper.this.
   */
  private Command _currentCommand;

  public ThreadedInterpreterWrapper() {
    _currentCommand = null;
    restart();
    _thread = new InterpreterThread();

    // Set to be daemon so the user can quit even if the interpreter
    // is busy. I think this is what we want.
    _thread.setDaemon(true);
    _thread.start();
  }

  /**
   * Interpret the given command.
   * If the interpreter is currently busy, this will
   * block until the interpreter is free.
   */
  public void interpretAsynchronously(final Command command) {
    // Acquire the lock, signifying it's ready for a command.
    //DrJava.consoleErr().println("begin interpAsynch");
    synchronized(this) {
      while (_currentCommand != null) {
        //DrJava.consoleErr().println("interpAsynch: not null, waiting");
        try {
          wait();
        }
        catch (InterruptedException e) {
          throw new UnexpectedException(e);
        }
      }

      //DrJava.consoleErr().println("interpAsynch: the wait is over");

      // We have the lock, and the command is null.
      _currentCommand = command;

      // Notify the interpreter thread that it has work to do!
      notify();
      //DrJava.consoleErr().println("interpAsynch: notified");
    }

    // That's all we have to do! The interpreter thread will
    // deal with the command we just gave it.
  }

  /**
   * Abort the current command, if any command is now running.
   */
  public void abort() {
    //DrJava.consoleErr().println("in abort(): before synch");
    synchronized(this) {
      if (_currentCommand != null) {
        // Interrupt the thread to kill the interpreter.
        //DrJava.consoleErr().println("in abort(): before interrupt");
        _thread.interrupt();
        //DrJava.consoleErr().println("in abort(): after interrupt. " +
                                    //"thread=" + _thread);
      }
    }
  }

  /**
   * Restart the interpreter.
   * This will first kill the current interpretation, if the interpreter
   * is busy right now.
   */
  public void restart() {
    synchronized(this) {
      if (_currentCommand != null) {
        abort();
      }

      // Keep the lock while we restart the interpreter.
      // Otherwise, someone could call interpretAsynchronously()
      // real quick and it'll interpret in the old interpreter.
      // But as long as we have the lock, interpret can't start.
      _realInterpreter = new DynamicJavaAdapter();
    }
  }

  /**
   * Adds the given path to the interpreter's classpath.
   * Forwards to {@link JavaInterpreter#addClassPath}.
   * @param path Path to add
   */
  public void addClassPath(final String path) {
    _realInterpreter.addClassPath(path);
  }

  /**
   * Set the scope for unqualified names to the given package.
   * Forwards to {@link JavaInterpreter#setPackageScope}.
   * @param packageName Package to assume scope of.
   */
  public void setPackageScope(final String packageName) {
    _realInterpreter.setPackageScope(packageName);
  }

  /**
   * An interpretation command. This specifies the text to be interpreted,
   * as well as the function to call when interpretation is over.
   */
  public interface Command {
    /** Specifies what text to interpret. */
    public String getStringToInterpret();

    /**
     * This method is called (from the interpreter thread) when
     * the interpretation is complete and it succeeded.
     *
     * @param result The result returned by the interpreter.
     */
    public void interpretationSucceeded(Object result);

    /**
     * This method is called (from the interpreter thread) when
     * the interpretation is interrupted before it can complete.
     *
     * @param e Exception with information about the status when
     *          the interpreter was interrupted.
     */
    public void interpretationInterrupted(InterpreterInterruptedException e);
  }
  
  /**
   * Infinite loop to consume _currentCommand and then
   * send events as appropriate.
   */
  private class InterpreterThread extends Thread {
    /** If set to false, it will end the loop. */
    volatile boolean keepRunning = true;

    public void run() {
      while (keepRunning) {
        // wait until we have a command.
        synchronized(ThreadedInterpreterWrapper.this) {
          while (_currentCommand == null) {
            //DrJava.consoleErr().println("in run(): null, waiting");
            try {
              ThreadedInterpreterWrapper.this.wait();
            }
            catch (InterruptedException e) {
              throw new UnexpectedException(e);
            }
          }
        }

        //DrJava.consoleErr().println("in run(): the wait is over");

        // Do the interpretation.
        try {
          String str = _currentCommand.getStringToInterpret();
          //DrJava.consoleErr().println("To interp: " + str);
          Object ret = _realInterpreter.interpret(str);
          //DrJava.consoleErr().println("interp ret: " + ret);
          _currentCommand.interpretationSucceeded(ret);
        }
        catch (InterpreterInterruptedException iie) {
          //DrJava.consoleErr().println("interp interrupt: " + iie);
          _currentCommand.interpretationInterrupted(iie);
        }

        // Now notify that the thread is now free.
        synchronized(ThreadedInterpreterWrapper.this) {
          _currentCommand = null;
          ThreadedInterpreterWrapper.this.notify();
        }
      }
    }
  }
}
