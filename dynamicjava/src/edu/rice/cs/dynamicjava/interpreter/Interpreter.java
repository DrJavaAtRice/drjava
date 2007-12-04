package edu.rice.cs.dynamicjava.interpreter;

import java.io.StringReader;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.lambda.WrappedException;

import koala.dynamicjava.tree.Node;
import koala.dynamicjava.tree.Expression;
import koala.dynamicjava.interpreter.error.ExecutionError;
import koala.dynamicjava.parser.wrapper.JavaCCParser;
import koala.dynamicjava.parser.wrapper.ParseError;
import edu.rice.cs.dynamicjava.Options;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * The external interface for the interpreter.
 */
public class Interpreter {

  private final Options _opt;
  private TypeContext _typeContext;
  private RuntimeBindings _bindings;
  
  public Interpreter(Options opt, TypeContext typeContext, RuntimeBindings bindings) {
    _opt = opt;
    _typeContext = typeContext;
    _bindings = bindings;
    // Force potentially expensive objects/classes to initialize now:
    _opt.typeSystem();
    new JavaCCParser(new StringReader(""), "").parseStream();
  }
  
  public Interpreter(Options opt) {
    this(opt, new TopLevelContext(Interpreter.class.getClassLoader()), RuntimeBindings.EMPTY);
  }
  
  public Interpreter(Options opt, ClassLoader loader) {
    this(opt, new TopLevelContext(loader), RuntimeBindings.EMPTY);
  }
  
  public Option<Object> interpret(String code) throws InterpreterException {
    Iterable<Node> tree = parse(code);
    debug.logValue("Parse result", tree);
    TypeContext tcResult = typeCheck(tree);
    debug.log("Static phase successful");
    Pair<RuntimeBindings, Option<Object>> evalResult = evaluate(tree);
    // We don't commit an environment change until evaluation has completed successfully.  This
    // helps to guarantee that _typeContext and _bindings are in sync.  Effects:
    // - If there's a static error in the entire tree, nothing runs.
    // - If evaluation halts halfway in, nothing (either previous or subsequent) gets defined.
    // - If evaluation halts halfway in, previous side effects (including mutation) *do* occur.
    // The alternative is to interpret the list of nodes incrementally, committing each change
    // before proceeding to the next.  In this case, static errors later in the tree would not
    // prevent execution of earlier code.
    _typeContext = tcResult;
    _bindings = evalResult.first();
    return evalResult.second();
  }
  
  private Iterable<Node> parse(String code) throws InterpreterException {
    try {
      return new JavaCCParser(new StringReader(code), "[string input]").parseStream();
    }
    catch (ParseError e) {
      throw new ParserException(e);
    }
  }
  
  private TypeContext typeCheck(Iterable<Node> tree) throws InterpreterException {
    try {
      TypeContext newContext = _typeContext;
      for (Node n : tree) {
        if (n instanceof Expression) { n.acceptVisitor(new ExpressionChecker(newContext, _opt)); }
        else { newContext = n.acceptVisitor(new StatementChecker(newContext, _opt)); }
      }
      return newContext;
    }
    catch (ExecutionError e) { throw new CheckerException(e); }
  }
  
  private Pair<RuntimeBindings, Option<Object>> evaluate(Iterable<Node> tree) throws InterpreterException {
    try {
      RuntimeBindings newBindings = _bindings;
      Option<Object> val = Option.none();
      for (Node n : tree) {
        if (n instanceof Expression) { val = Option.some(new ExpressionEvaluator(newBindings, _opt).value(n)); }
        else {
          StatementEvaluator.Result r = n.acceptVisitor(new StatementEvaluator(newBindings, _opt));
          newBindings = r.bindings();
          val = r.value();
        }
      }
      return Pair.make(newBindings, val);
    }
    catch (WrappedException e) {
      if (e.getCause() instanceof InterpreterException) { throw (InterpreterException) e.getCause(); }
      else { throw e; }
    }
  }
  
}
