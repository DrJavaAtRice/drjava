package edu.rice.cs.drjava.model.repl.newjvm;

import java.io.*;
import scala.tools.nsc.interpreter.ILoop;
import scala.tools.nsc.Settings;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import edu.rice.cs.dynamicjava.interpreter.RuntimeBindings;
import edu.rice.cs.dynamicjava.interpreter.TypeContext;
import edu.rice.cs.dynamicjava.interpreter.InterpreterException;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.util.UnexpectedException;

import edu.rice.cs.util.swing.Utilities;

/**
 * Class for providing interpretation services in the Interactions pane. Code
 * submitted for interpretation (from the Interactions pane) is submitted to
 * a "DrScalaILoop" instance, which interprets the code and returns a String result.
 * 
 * Since stderr and stdout are redirected to point to the Interactions pane and
 * the DrJava console, "print" statements called from within ILoop, in the course
 * of interpretation, are routed correctly.  As such, all content *returned* from
 * ILoop can still be conveniently differentiated from content printed to the 
 * console.
 * 
 * IO between calls from the main slave JVM thread and ILoop, which runs in its 
 * own thread, is accomplished using a bounded buffer (ArrayBlockingQueue) in each 
 * direction.  Each line of code submitted for interpretation is added to the input 
 * queue, after which the output queue is immediately polled for ILoop's return 
 * content.
 *
 * TODO: 
 *       1)  error handling (I'm basically just printing the stack traces of any 
 *       exceptions caught, but we should probably be trying to restart calls in 
 *       some places -- particularly any interrupted blocking calls on blocking queues)
 */
public class DrScalaInterpreter implements Interpreter {

  /* producer: slave JVM's main thread sends code from the interactions pane for 
   *           interpretation, by calling "interpret"
   * consumer: ILoop calls "_iLoopReader.readLine", which blocks while this is empty
   */
  final ArrayBlockingQueue<String> inputStrings = new ArrayBlockingQueue<String>(100);

  /* producer: ILoop writes its output to this queue via _iLoopWriter
   * consumer: slave JVM's main thread waits for "interpret" to return, which in 
   *           turn contains blocking "take" calls on this queue
   */
  final ArrayBlockingQueue<String> outputStrings = new ArrayBlockingQueue<String>(100);

  /**
   * Used to catch scala interpreter commands.
   *
   * The Scala ILoop interpreter accepts colon command for a variety
   * of functions, such as ":paste", ":run", and ":quit".  The ":quit" command
   * can kill the interpreter, thus leaving the interactions pane unusable.
   * This regex is used to catch those colon commands so they can be ignored.
   */
  final private Pattern scalaColonCmd = Pattern.compile("^\\s*:.*$");

  /* Used to record whether the interpreter has been initialized */
  private volatile boolean _isInitialized = false;

  /* dummy Reader for _iLoopReader constructor -- these methods should NEVER be called! 
   * in addition to compilation requirements, the methods below are added for debugging
   * purposes -- _iLoopReader does not route any calls to dummyReader, so a call to any 
   * method in this object has "fallen through"
   */
  final Reader dummyReader = new Reader(){
    @Override public void close(){ System.out.println("uh-oh...close should NOT have been called on dummyReader"); }
    @Override public int read(char[] cbuf, int off, int len) throws IOException{ System.out.println("uh-oh...read should NOT have been called on dummyReader"); return -1; }
    @Override public int read(){ System.out.println("dummyReader.read() called"); return -1; }
    @Override public int read(char[] cbuf){ System.out.println("dummyReader.read(char[] cbuf) called with: " + cbuf); return -1; }
  };
  /*
   * 1) BufferedReader which overrides "readLine" (which is called by ILoop, via its internal "SimpleReader")
   * 2) "readLine" calls "take" on "inputStrings", blocking until the next line is submitted for interpretation
   */
  private final BufferedReader _iLoopReader = new BufferedReader(dummyReader){
    @Override
    public String readLine() throws IOException{
      try {
        return inputStrings.take(); 
      }
      catch (InterruptedException ie){
        ie.printStackTrace(); // TODO: probably need real error handling here (i.e. restart the blocking call)
      }
      return null;
    }
  };

  /* dummy OutStream for _iLoopWriter constructor -- these methods should NEVER be called! 
   * in addition to compilation requirements, the methods below are added for debugging
   * purposes -- _iLoopWriter does not route any calls to dummyOutStream, so a call to any 
   * method in this object has "fallen through"
   */
  final OutputStream dummyOutStream = new OutputStream() {
    @Override public void write(byte[] b) throws IOException { System.out.println("dummyOutStream.write(byte b) called"); }
    @Override public void write(byte[] b, int off, int len) throws IOException { System.out.println("dummyOutStream.write(byte[] b, int off, int len) called"); }
    @Override public void write(int b) throws IOException { System.out.println("dummyOutStream.write(int b) called"); }
    @Override public void close() { System.out.println("dummyOutStream.close() called"); }
    @Override public void flush() { System.out.println("dummyOutStream.flush() called"); }
  };

  /* PrintWriter used by ILoop to write return strings; _iLoopWriter simply
   * forwards every string it receives to the output queue, "outputStrings"
   * 
   * since scala print statements are sent to stdout, everything sent here is 
   * known to be part of the "returned" output from each interpretation call.
   * 
   * This field can ONLY by accessed by the Scala interpreter; operations on it
   * are NOT threadsafe.
   */
  private final PrintWriter _iLoopWriter = new PrintWriter(dummyOutStream){
    @Override
    public void print(String s) {
      outputStrings.add(s); 
    }
    /* this method added because ILoop/IMain calls it, probably for console print commands */
    @Override
    public void write(String s, int off, int len){ 
      outputStrings.add(s.substring(off, off + len));
    }
    @Override
    public void println(String s){
      outputStrings.add(s + "\n");
    }
    /* this is a no-op since the underlying output stream is just compiler candy */
    @Override public void flush(){}
  };

  /* thread in which ILoop runs */
  private final Thread _iLoopThread = new Thread(new Runnable(){
    public void run() {
      DrScalaILoop iLoop = new DrScalaILoop(_iLoopReader, _iLoopWriter);
      Settings s = new Settings();
      s.processArgumentString("-usejavacp");
      iLoop.process(s);
    }
  });

  public DrScalaInterpreter() {}

  /** method for adding a classpath element to the REPL classpath. */
  public synchronized void addCP(String pathType, String path) {
//    System.out.print(pathType + ": " + path);
    String res = this._interpret(":cp " + path, true);
    if (res.contains("doesn't seem to exist"))
      System.err.println("ERROR: unable to add cp, '" + path +
        "' to the Interpreter classpath.");
  }

  public synchronized void reset() {
    String res = this._interpret(":reset", true);
    System.err.println("result of reset cmd: " + res);
  }
  
  /** Initialize the interpreter for use in the interactions pane. */
  private void _init() {
  
    // Purge the "Welcome to scala ..." message from the interpreter output stream
    String s = null;
    try {
      /* The following code discards the initial "Welcome to scala ..." message which
       * ILoop sends over upon construction.
       */
      s = outputStrings.take();
      while (! s.equals("\nscala> ")) s = outputStrings.take();
    }
    catch(InterruptedException ie) { 
      /* should never happen. */
      throw new UnexpectedException(ie);
    }

    // Perform a trivial computation, forcing it to load most of its classes.
    inputStrings.add("val _$$$$$__$$$$$$_ = 2+2\n");
    /* this call blocks until the first line of the return has been received */
    try { outputStrings.take(); }
    catch(InterruptedException ie) {
      /* should never happen. */
      throw new UnexpectedException(ie);
    }
    
    // Record that initialization has been performed
    _isInitialized = true;
  }
  
  /* Initialize the interpreter for use in the interactions pane.  This method is called by InterpreterJVM after 
   * stderr/stdout are redirected */
  public void start() { 
    _iLoopThread.start();
    
    /* If not in test mode, initialize the interpreter for use in the interactions pane */
    if (! Utilities.TEST_MODE) _init();
  }

  /**
   * Public interface for the interpretation; this is separated from the internal 
   * implementation ('_interpret') because 'colon commands' are passed to that
   * method in order to augment the REPL classpath.
   */
  public String interpret(String input) throws InterpreterException {
    if (input.equals(":test-reset")){
      this.reset();
      return "";
    }
    Matcher match = scalaColonCmd.matcher(input);
    if (match.matches())
      return "Error:  Scala interpreter colon commands not accepted.\n";
    return this._interpret(input, false);
  }

  /** The primary method of the Scala Interpreter class: returns whatever String is returned 
    * by ILoop in response to input code, or "" if there is no return.
    * 
    * Note: this method returns an InterpretResult in vanilla DrJava, as does the wrapping 
    * caller in InterpreterJVM.  We may need to revert to that convention if 
    * diagnostic information is needed about interpretation results elsewhere -- 
    * e.g. for formatting multiline expressions.
    * 
    * This method is synchronized because there is a race condition between writing the initial 
    * welcome message into the ouput buffer and clearing the output buffer, which executes at the 
    * beginning of every interpretation.  In fact, without the synchronized qualifier,
    * there is a potential race condition on internal actions of this method if it is called from 
    * multiple threads.
    */
  private synchronized String _interpret(String input, boolean isCmd) {
    // Perform deferred initialization if necessary
    if (! _isInitialized) _init(); 

    try {
      /* clear out any leftovers -- there should never be any, however */
      outputStrings.clear();
      /* write the current line of code into the inputStrings queue */
      inputStrings.add(isCmd? input : (input + '\n'));
      /* this call blocks until the first line of the return has been received */
      String s = outputStrings.take();
      
      /* if the prompt or continuation string is returned, we're done */
      if (s.equals("\nscala> ")) return "";
      if (s.equals("     | "))   return s;
      
      /* 
       * otherwise, we keep taking strings from the return queue until
       * the prompt is encountered.
       * 
       * assumption: EVERY interpretation return which does not consist of
       * only the continuation string, "     | ", is followed by "\nscala> "
       * if this assumption is incorrect, the loop below could diverge
       */
      StringBuilder returnString = new StringBuilder(s);
      while (true){
        s = outputStrings.take();
        if (s.equals("\nscala> "))
          break;
        returnString.append(s);
      }
      return returnString.toString();
    }
    catch (InterruptedException ie){
      ie.printStackTrace(); // TODO: probably need real error handling here (i.e. restart the blocking call)
    }
    return null;
  }
}
