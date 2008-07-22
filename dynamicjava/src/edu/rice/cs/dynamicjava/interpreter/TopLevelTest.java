package edu.rice.cs.dynamicjava.interpreter;

import java.util.zip.ZipFile;
import java.util.LinkedList;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.*;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.plt.reflect.PathClassLoader;
import edu.rice.cs.jlbench.*;
import edu.rice.cs.jlbench.JLBench.Benchmark;
import edu.rice.cs.jlbench.BenchmarkFile.Range;

import edu.rice.cs.dynamicjava.Options;

public class TopLevelTest {
  
  private static final boolean VERBOSE_FAILURES = true;
  
  private static String[] notYetSupported = {
    "java/ConstantExpressions.jlbench",
    "java/Literals.jlbench",
    "java/ExtremeLiterals.jlbench",
    "java5/UnsupportedFeatures.jlbench",
    "java5/ExplicitGenericMethods.jlbench"
  };
  
  public static Test suite() throws IOException {
    Iterable<String> excludes = IterUtil.asIterable(notYetSupported);
    ZipFile jlbenchJar = new ZipFile("lib/buildlib/jlbench.jar");
    LinkedList<Iterable<Benchmark>> benchmarks = new LinkedList<Iterable<Benchmark>>();
    benchmarks.add(JLBench.benchmarksForZipEntries(jlbenchJar, "edu/rice/cs/jlbench/benchmarks/java/", excludes));
    benchmarks.add(JLBench.benchmarksForZipEntries(jlbenchJar, "edu/rice/cs/jlbench/benchmarks/java5/", excludes));
    benchmarks.add(JLBench.benchmarksForFiles(new File("testFiles/jlbench/"), excludes));
    
    TestSuite result = new TestSuite();
    for (Benchmark b : IterUtil.collapse(benchmarks)) {
      result.addTest(new JLBenchTestWrapper(b, Options.DEFAULT));
    }
    return result;
  }
  
  private static class JLBenchTestWrapper implements Test {
    private final Benchmark _b;
    private final Options _opt;
    
    public JLBenchTestWrapper(Benchmark b, Options opt) { _b = b; _opt = opt; }
    
    public int countTestCases() { return _b.numberOfTests(); }
    
    public void run(TestResult result) {
      turnOffLogging(result);
      result.startTest(this);
      try {
        Iterable<Pair<Range, BenchmarkResult>> runResults = _b.run(new DynamicJavaBenchmarkRunner(_opt));
        for (Pair<Range, BenchmarkResult> p : runResults) {
          if (!p.second().successful()) {
            String message = p.first().toString();
            if (VERBOSE_FAILURES) {
              message += "\n" + TextUtil.repeat('=', 60) + "\n" + p.second();
            }
            result.addFailure(this, new AssertionFailedError(message));
          }
        }
      }
      catch (IOException e) { result.addError(this, e); }
      finally { result.endTest(this); }
    }
    
    public String toString() { return "<" + _b.name() + ">"; }
    
    private void turnOffLogging(TestResult result) {
      try {
        java.lang.reflect.Field listenersF = result.getClass().getDeclaredField("fListeners");
        listenersF.setAccessible(true);
        for (Object listener : (Iterable<?>) listenersF.get(result)) {
          Object antRunner = listener; // by default, the listener is the runner
          try {
            java.lang.reflect.Field antRunnerF = listener.getClass().getDeclaredField("this$0");
            antRunnerF.setAccessible(true);
            antRunner = antRunnerF.get(listener);
          }
          catch (Throwable t) { /* not an anonymous class */ }
          try {
            java.lang.reflect.Field logFlagF = antRunner.getClass().getDeclaredField("logTestListenerEvents");
            logFlagF.setAccessible(true);
            logFlagF.setBoolean(antRunner, false);
          }
          catch (Throwable t) { /* not an Ant test runner */ }
        }
      }
      catch (Throwable t) { /* not a standard JUnit TestResult */ }
    }
    
  }
  
  private static class DynamicJavaBenchmarkRunner extends CompiledBenchmarkRunner {
    
    private Options _opt;
    
    public DynamicJavaBenchmarkRunner(Options opt) throws IOException {
      super(new JavacCommandCompiler(), new Executor() {
        public BenchmarkResult execute(String className, Iterable<File> classPath) {
          throw new UnsupportedOperationException();
        }
      });
      _opt = opt;
    }
    
    protected BenchmarkResult doTest(PackageName p, String header, String body) {
      return interpret(p, header, body, false, false);
    }
    
    protected BenchmarkResult doStaticError(PackageName p, String header, String body) {
      return interpret(p, header, body, true, false);
    }
    
    protected BenchmarkResult doRuntimeError(PackageName p, String header, String body) {
      return interpret(p, header, body, false, true);
    }
    
    private BenchmarkResult interpret(PackageName p, String header, String body,
                                      boolean staticError, boolean runtimeError) {
      Interpreter i = new Interpreter(_opt, new PathClassLoader(_classRoot));
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOUtil.replaceSystemOut(out);
      ByteArrayOutputStream err = new ByteArrayOutputStream();
      IOUtil.replaceSystemErr(err);
      StringBuilder debugMessage = new StringBuilder();
      debugMessage.append(TextUtil.repeat('-', 60));
      debugMessage.append("\nInterpreted code:\n");
      debugMessage.append(header);
      debugMessage.append("\n");
      debugMessage.append(body);
      debugMessage.append("\n");
      debugMessage.append(TextUtil.repeat('-', 60));
      try {
        if (!p.isDefault()) { i.interpret("package " + p.toString()); }
        i.interpret("void assertTrue(boolean b) { if (!b) throw new Error(\"Assertion failed\"); }");
        i.interpret(header);
        i.interpret(body);
        recordOutAndErr(out, err, debugMessage);
        return new BenchmarkResult(!staticError && !runtimeError,
                                   "Interpreted without error" + debugMessage);
      }
      catch (InterpreterException e) {
        recordOutAndErr(out, err, debugMessage);
        if (e instanceof ParserException || e instanceof CheckerException) {
          return new BenchmarkResult(staticError, e.getUserMessage() + debugMessage);
        }
        else if (e instanceof EvaluatorException) {
          return new BenchmarkResult(runtimeError, e.getUserMessage() + debugMessage);
        }
        else { return BenchmarkResult.failure(e.getUserMessage() + debugMessage); }
      }
      catch (Throwable t) {
        recordOutAndErr(out, err, debugMessage);
        return BenchmarkResult.failure(t.toString() + "\n" + debugMessage);
      }
      finally {
        IOUtil.revertSystemOut();
        IOUtil.revertSystemErr();
      }
    }
    
    private void recordOutAndErr(ByteArrayOutputStream out, ByteArrayOutputStream err,
                                 StringBuilder debugMessage) {
      String outString = out.toString();
      if (outString.length() > 0) {
        debugMessage.append("\nStandard output:\n");
        debugMessage.append(outString);
        debugMessage.append("\n");
        debugMessage.append(TextUtil.repeat('-', 60));
      }
      String errString = err.toString();
      if (errString.length() > 0) {
        debugMessage.append("\nStandard error:\n");
        debugMessage.append(errString);
        debugMessage.append("\n");
        debugMessage.append(TextUtil.repeat('-', 60));
      }
    }
    
  }
  
}
