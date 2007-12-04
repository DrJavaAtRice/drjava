package edu.rice.cs.dynamicjava;

import java.io.*;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.OptionVisitor;
import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.dynamicjava.interpreter.Interpreter;
import edu.rice.cs.dynamicjava.interpreter.InterpreterException;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

public final class DynamicJava {
  
  private DynamicJava() {}
  
  public static void main(String... args) throws IOException {
    debug.log();
    Interpreter i = new Interpreter(Options.DEFAULT);
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      System.out.print("> ");
      System.out.flush();
      String input = in.readLine();
      try {
        Option<Object> result = i.interpret(input);
        result.apply(new OptionVisitor<Object, Void>() {
          public Void forSome(Object o) { System.out.println(TextUtil.toString(o)); return null; }
          public Void forNone() { return null; }
        });
      }
      catch (InterpreterException e) { e.printUserMessage(); debug.log(e); }
      catch (RuntimeException e) {
        System.out.println("INTERNAL ERROR: Uncaught exception");
        e.printStackTrace();
      }
      System.out.println();
    }
  }
  
}
