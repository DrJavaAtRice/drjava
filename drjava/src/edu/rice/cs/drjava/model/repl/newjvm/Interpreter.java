package edu.rice.cs.drjava.model.repl.newjvm;

import java.io.*;
import scala.tools.nsc.interpreter.ILoop;
import scala.tools.nsc.Settings;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import edu.rice.cs.dynamicjava.interpreter.RuntimeBindings;
import edu.rice.cs.dynamicjava.interpreter.TypeContext;
import edu.rice.cs.dynamicjava.interpreter.InterpreterException;
import edu.rice.cs.dynamicjava.Options;

/*
 * Class for providing interpretation services in the Interactions pane. Code
 * submitted for interpretation (from the Interactions pane) is submitted to
 * an "ILoop" instance, which interprets the code and returns a String result.
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
 * yes, the dummy Reader/OutputStream instances are very ugly in terms of readability
 * ...but there is nothing to read, since the behavior of each of these methods is 
 * identical: just to print out a message alerting us that it has been called. so all
 * of the dummy methods are defined in single lines.
 * 
 * TODO: 
 *       1)  error handling (I'm basically just printing the stack traces of any 
 *       exceptions caught, but we should probably be trying to restart calls in 
 *       some places -- particularly any interrupted blocking calls on blocking queues)
 * 
 *       2)  a) should we use LinkedBlockingQueue's instead of an ArrayBlockingQueue's,
 *           to avoid fixing the capacity of the input/outputStrings queues?
 *           
 *           b) if NOT, how do we determine a "good" capacity for an ArrayBlockingQueue?
 *           (personally, I am fine with just plucking the number 100 out of thin air, since
 *           the capacity of "inputStrings" should never exceed 1 and that of "outputStrings"
 *           would never exceed 3 or 4, as far as I can tell, and is emptied at the beginning
 *           of each call to "interpret" in any case -- and 100 pointers is not exactly going 
 *           to tax the heap)
 *
 */
public class Interpreter {

  /* 
   * producer: slave JVM's main thread sends code from the interactions pane for 
   *           interpretation, by calling "interpret"
   * consumer: ILoop calls "iLoopReader.readLine", which blocks while this is empty
   */
  final ArrayBlockingQueue<String> inputStrings = new ArrayBlockingQueue<String>(100);

  /*
   * producer: ILoop writes its output to this queue via iLoopWriter
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

  private boolean _isInitialized = false;

  /* 
   * dummy Reader for iLoopReader constructor -- these methods should NEVER be called! 
   * in addition to compilation requirements, the methods below are added for debugging
   * purposes -- iLoopReader does not route any calls to dummyReader, so a call to any 
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
  final BufferedReader iLoopReader = new BufferedReader(dummyReader){
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

  /* 
   * dummy OutStream for iLoopWriter constructor -- these methods should NEVER be called! 
   * in addition to compilation requirements, the methods below are added for debugging
   * purposes -- iLoopWriter does not route any calls to dummyOutStream, so a call to any 
   * method in this object has "fallen through"
   */
  final OutputStream dummyOutStream = new OutputStream(){
    @Override public void write(byte[] b) throws IOException { System.out.println("dummyOutStream.write(byte b) called"); }
    @Override public void write(byte[] b, int off, int len) throws IOException { System.out.println("dummyOutStream.write(byte[] b, int off, int len) called"); }
    @Override public void write(int b) throws IOException { System.out.println("dummyOutStream.write(int b) called"); }
    @Override public void close() { System.out.println("dummyOutStream.close() called"); }
    @Override public void flush() { System.out.println("dummyOutStream.flush() called"); }
  };

  /* 
   * PrintWriter used by ILoop to write return strings; iLoopWriter simply
   * forwards every string it receives to the output queue, "outputStrings"
   * 
   * since scala print statements are sent to stdout, everything sent here is 
   * known to be part of the "returned" output from each interpretation call
   */
  final PrintWriter iLoopWriter = new PrintWriter(dummyOutStream){
    @Override
    public void print(String s) {
      outputStrings.add(s); 
    }
    /* this method added because ILoop/IMain calls it (though from where, I can't say) */
    @Override
    public void write (String s, int off, int len){ 
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
  final Thread iLoopThread = new Thread(new Runnable(){
    public void run(){
      ILoop iLoop = new ILoop(iLoopReader, iLoopWriter);
      Settings s = new Settings();
      s.processArgumentString("-usejavacp");
      iLoop.process(s);
    }
  });
  
  /*
   * NOTE: currently, all of the construction takes place prior to the constructor call.
   *       however, we may need to use the other constructors for unit testing, etc (?)
   *       so we're just leaving all of the constructor calls in InterpreterJVM as they
   *       are for now.  BUT it may be possible to eliminate these alternate constructors
   *       altogether -- and if this is the case, then it may also be possible to pare down 
   *       InterpreterJVM a good deal
   */
  public Interpreter(Options opt) { this(); }
  public Interpreter(Options o,TypeContext typeC,RuntimeBindings b) { this(); }
  public Interpreter(Options opt, ClassLoader loader) { this(); }
  public Interpreter() {}
  
  /* this method is called by InterpreterJVM after stderr/stdout are redirected */
  public void start(){
    iLoopThread.start();
  }

  /* 
   * the class's primary public method: returns whatever String is returned 
   * by ILoop in response to input code, or "" if there is no return.
   * 
   * (note: this method used to return an "InterpretResult", as does the wrapping 
   * caller in InterpreterJVM. We may need to revert to that convention if 
   * diagnostic information is needed about interpretation results elsewhere -- 
   * e.g. for formatting multiline expressions)
   */
  public String interpret(String input) throws InterpreterException {

    /*
     * hack to avoid returning the initial "Welcome to scala message..." which
     * ILoop sends over upon construction.  This hack should never make any
     * difference if the first call to "interpret" is made by a real user; but
     * if it is invoked programatically (i.e. for testing purposes), then it
     * could arrive quickly enough that the initial call to "outputStrings.clear()",
     * below, might miss scala's welcome message.  Or, it could catch that message,
     * but then immediately return after reading the initial "scala> " prompt.
     * 
     * Essentially, anytime "interpret" is programatically invoked very shortly
     * (in machine terms) after an Interpreter instance has been constructed,
     * there is a race condition regarding the order in which the initial welcome
     * messages are written into the ouput buffer and the call to clear the output
     * buffer, which executes at the beginning of every interpretation, is made --
     * since this method's protocol is to poll the output buffer until it encounters
     * a "scala> " prompt, this hack should ensure that the rest of this method
     * executes ONLY once the scala repl's initial message and prompt have been
     * consumed.
     *
     */
    if (!_isInitialized){
      _isInitialized = true;
      interpret("");
    }

    Matcher match = scalaColonCmd.matcher(input);

    if (match.matches()) 
      return "Error:  Scala interpreter colon commands not accepted.\n";

    try{
      /* clear out any leftovers -- there should never be any, however */
      outputStrings.clear();
       /* write the current line of code into the inputStrings queue */
      inputStrings.add(input + '\n');
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
        if(s.equals("\nscala> "))
          break;
        returnString.append(s);
      }
      return returnString.toString();
    }
    catch(InterruptedException iE){
      iE.printStackTrace(); // this should probably be replaced with actual error handling where "take" calls are made -- i.e. attempting to restart these calls
    }
    return null;
  }
}
