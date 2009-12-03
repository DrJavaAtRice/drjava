package edu.rice.cs.dynamicjava.sourcechecker;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import koala.dynamicjava.interpreter.NodeProperties;
import koala.dynamicjava.interpreter.error.ExecutionError;
import koala.dynamicjava.parser.wrapper.JavaCCParser;
import koala.dynamicjava.parser.wrapper.ParseError;
import koala.dynamicjava.tree.CompilationUnit;
import koala.dynamicjava.tree.IdentifierToken;
import koala.dynamicjava.tree.Node;
import koala.dynamicjava.tree.SourceInfo;
import koala.dynamicjava.tree.TypeDeclaration;
import koala.dynamicjava.tree.visitor.DepthFirstVisitor;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.*;
import edu.rice.cs.dynamicjava.symbol.DJClass;
import edu.rice.cs.dynamicjava.symbol.ExtendedTypeSystem;
import edu.rice.cs.dynamicjava.symbol.Function;
import edu.rice.cs.dynamicjava.symbol.JLSTypeSystem;
import edu.rice.cs.dynamicjava.symbol.Library;
import edu.rice.cs.dynamicjava.symbol.SymbolUtil;
import edu.rice.cs.dynamicjava.symbol.TreeLibrary;
import edu.rice.cs.dynamicjava.symbol.TypeSystem;
import edu.rice.cs.dynamicjava.symbol.Variable;
import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.plt.collect.UnindexedRelation;
import edu.rice.cs.plt.collect.Relation;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.reflect.PathClassLoader;
import edu.rice.cs.plt.text.ArgumentParser;
import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Pair;

public class SourceChecker {

  private final Options _opt;
  private final boolean _quiet;
  private int _statusCount;
  private Iterable<CompilationUnit> _processed;
  
  public SourceChecker(Options opt, boolean quiet) {
    _opt = opt;
    _quiet = quiet;
    _statusCount = 0;
    _processed = IterUtil.empty();
  }
  
  public Iterable<CompilationUnit> processed() { return _processed; }
  
  public void check(File... sources) throws InterpreterException {
    check(IterUtil.asIterable(sources), IterUtil.<File>empty());
  }
  
  public void check(Iterable<? extends File> sources) throws InterpreterException {
    check(sources, IterUtil.<File>empty());
  }
  
  public void check(Iterable<? extends File> sources, Iterable<? extends File> classPath)
                      throws InterpreterException {
    Iterable<CompilationUnit> tree = parse(sources);
    _processed = IterUtil.compose(_processed, tree);
    TypeContext context = makeContext(tree, classPath);
    Relation<TypeDeclaration, ClassChecker> decls = extractDeclarations(tree, context);
    initializeClassSignatures(decls);
    checkSignatures(decls);
    checkBodies(decls);
  }
  
  private Iterable<CompilationUnit> parse(Iterable<? extends File> sources) throws InterpreterException {
    final List<CompilationUnit> result = new ArrayList<CompilationUnit>();
    Iterable<File> files = IterUtil.collapse(IterUtil.map(sources, new Lambda<File, Iterable<File>>() {
      private final FileFilter _filter = IOUtil.extensionFilePredicate("java");
      public Iterable<File> value(File f) { return IOUtil.listFilesRecursively(f, _filter); }
    }));
    new Phase<File>("Parsing") {
      protected void step(File source) throws InterpreterException {
        try {
          JavaCCParser parser = new JavaCCParser(new FileReader(source), source, _opt);
          result.add(parser.parseCompilationUnit());
        }
        catch (ParseError e) { throw new ParserException(e); }
        catch (FileNotFoundException e) { throw new SourceException(e); }
      }
      protected SourceInfo location(File f) { return SourceInfo.point(f, 0, 0); }
    }.run(files);
    return result;
  }
  
  private TypeContext makeContext(Iterable<CompilationUnit> sources, Iterable<? extends File> cp) {
    Library classLib = SymbolUtil.classLibrary(new PathClassLoader(null, cp));
    debug.logStart("creating TreeLibrary");
    Library sourceLib = new TreeLibrary(sources, classLib.classLoader(), _opt);
    debug.logEnd("creating TreeLibrary");
    return new ImportContext(new LibraryContext(new LibraryContext(classLib), sourceLib), _opt);
  }
  
  private Relation<TypeDeclaration, ClassChecker> extractDeclarations(Iterable<CompilationUnit> sources,
                                                                       TypeContext context)
                                                                       throws InterpreterException {
    final CompilationUnitChecker unitChecker = new CompilationUnitChecker(context, _opt);
    final Relation<TypeDeclaration, ClassChecker> checkers = UnindexedRelation.makeLinkedHashBased();
    new Phase<CompilationUnit>("Resolving imports") {
      protected void step(CompilationUnit u) throws InterpreterException {
        checkers.addAll(unitChecker.extractDeclarations(u));
      }
      protected SourceInfo location(CompilationUnit arg) { return arg.getSourceInfo(); }
    }.run(sources);
    return checkers;
  }

  private void initializeClassSignatures(Relation<TypeDeclaration, ClassChecker> decls) throws InterpreterException {
    new ClassCheckerPhase("Checking class signatures") {
      protected void step(TypeDeclaration ast, ClassChecker checker) { checker.initializeClassSignatures(ast); } 
    }.run(decls);
  }
  
  private void checkSignatures(Relation<TypeDeclaration, ClassChecker> decls) throws InterpreterException {
    new ClassCheckerPhase("Checking class member signatures") {
      protected void step(TypeDeclaration ast, ClassChecker checker) { checker.checkSignatures(ast); } 
    }.run(decls);
  }
  
  private void checkBodies(Relation<TypeDeclaration, ClassChecker> decls) throws InterpreterException {
    new ClassCheckerPhase("Checking class member bodies") {
      protected void step(TypeDeclaration ast, ClassChecker checker) { checker.checkBodies(ast); } 
    }.run(decls);
  }
  
  
  private void startStatus(String description) {
    _statusCount = 0;
    if (!_quiet) {
      String fullDesc = TextUtil.padRight(description + "...", ' ', 36);
      System.out.print(fullDesc);
      System.out.flush();
    }
  }
  
  private void incrementStatus() {
    _statusCount++;
    // arbitrarily chose 10 as the interval for status printouts
    if (!_quiet && (_statusCount % 10 == 0)) { System.out.print('*'); System.out.flush(); }
  }
  
  private void endStatus() {
    if (!_quiet) { System.out.println(); }
  }
  
  
  private abstract class Phase<T> {
    private final String _description;
    protected Phase(String description) { _description = description; }
    
    protected abstract void step(T arg) throws InterpreterException;
    protected abstract SourceInfo location(T arg);
    
    public void run(Iterable<? extends T> args) throws InterpreterException {
      List<InterpreterException> errors = new ArrayList<InterpreterException>();
      debug.logStart(_description);
      startStatus(_description);
      for (T arg : args) {
        debug.logStart("location", location(arg));
        try { step(arg); }
        catch (InterpreterException e) { errors.add(e); }
        catch (RuntimeException e) { errors.add(new InternalException(e, location(arg))); }
        incrementStatus();
        debug.logEnd();
      }
      endStatus();
      debug.logEnd(_description);
      if (!errors.isEmpty()) { throw CompositeException.make(errors); }
    }
  }
  
  private abstract class ClassCheckerPhase extends Phase<Pair<TypeDeclaration, ClassChecker>> {
    protected ClassCheckerPhase(String description) { super(description); }
    protected final void step(Pair<TypeDeclaration, ClassChecker> arg) throws InterpreterException {
      try { step(arg.first(), arg.second()); }
      catch (ExecutionError e) { throw extractErrors(arg.first()); }
    }
    protected final SourceInfo location(Pair<TypeDeclaration, ClassChecker> arg) {
      return arg.first().getSourceInfo();
    }
    protected abstract void step(TypeDeclaration ast, ClassChecker checker);
  }
  
  
  /** Get all ERROR values associated with the given AST. */
  private static InterpreterException extractErrors(Node ast) {
    // accumulate in a set to avoid duplicates from DAGs
    final Set<ExecutionError> result = new LinkedHashSet<ExecutionError>();
    new PropertiesDepthFirstVisitor() {
      public void run(Node node) {
        if (NodeProperties.hasError(node)) { result.add(NodeProperties.getError(node)); }
        super.run(node);
      }
    }.run(ast);
    return CompositeException.make(IterUtil.map(result, CheckerException.FACTORY));
  }
  
  /**
   * A DepthFirstVisitor extension that recurs on Node-typed properties.  Note that this may lead to
   * multiple invocations of the same node (because the properties are often used to create DAGs).
   */
  private static class PropertiesDepthFirstVisitor extends DepthFirstVisitor {
    public void run(Node node) {
        try { node.acceptVisitor(this); }
        catch (IllegalArgumentException e) { /* thrown by "empty" stub nodes -- ignore */ }
        if (NodeProperties.hasLeftExpression(node)) { recur(NodeProperties.getLeftExpression(node)); }
        if (NodeProperties.hasTranslation(node)) { recur(NodeProperties.getTranslation(node)); }
        if (NodeProperties.hasStatementTranslation(node)) { recur(NodeProperties.getTranslation(node)); }
    }
  }
 
  private static final Map<String, Options> _options;
  static {
    _options = new LinkedHashMap<String, Options>();
    _options.put("jls", new Options() {
      @Override protected Thunk<? extends TypeSystem> typeSystemFactory() {
        TypeSystem result = new JLSTypeSystem(this);
        return LambdaUtil.valueLambda(result);
      }
      @Override public boolean enforceAllAccess() { return true; }
      @Override public boolean prohibitUncheckedCasts() { return false; }
    });
    _options.put("ext", new Options() {
      @Override protected Thunk<? extends TypeSystem> typeSystemFactory() {
        TypeSystem result = new ExtendedTypeSystem(this);
        return LambdaUtil.valueLambda(result);
      }
      @Override public boolean enforceAllAccess() { return true; }
      @Override public boolean prohibitUncheckedCasts() { return false; }
    });
  }
  
  public static void main(String... args) {
    debug.logStart();

    ArgumentParser argParser = new ArgumentParser();
    argParser.supportOption("classpath", "");
    argParser.supportAlias("cp", "classpath");
    argParser.supportOption("opt", 1);
    argParser.requireParams(1);
    final ArgumentParser.Result parsedArgs = argParser.parse(args);
    Iterable<File> cp = IOUtil.parsePath(parsedArgs.getUnaryOption("classpath"));
    Iterable<File> sources = IterUtil.map(parsedArgs.params(), IOUtil.FILE_FACTORY);
    
    if (parsedArgs.hasOption("opt")) {
      Options opt = _options.get(parsedArgs.getUnaryOption("opt"));
      if (opt == null) { System.out.println("Unrecognized options name: " + parsedArgs.getUnaryOption("opt")); }
      else { processFiles(sources, cp, opt); }
    }
      
    else {
      Iterator<String> optNames = _options.keySet().iterator();
      String canonicalName = optNames.next();
      Iterable<CompilationUnit> canonical = processFiles(sources, cp, _options.get(canonicalName));
      Map<String, Iterable<CompilationUnit>> others = new LinkedHashMap<String, Iterable<CompilationUnit>>();
      while (optNames.hasNext()) {
        String n = optNames.next();
        others.put(n, processFiles(sources, cp, _options.get(n)));
      }
      NodeDiff diff = new NodeDiff();
      for (Map.Entry<String, Iterable<CompilationUnit>> e : others.entrySet()) {
        diff.compare(canonicalName, canonical, e.getKey(), e.getValue());
      }
    }
    
    debug.logEnd();
  }
  
  
  private static Iterable<CompilationUnit> processFiles(Iterable<File> sources, Iterable<File> cp, Options opt) {
    SourceChecker checker = new SourceChecker(opt, false);
    try {
      checker.check(sources, cp);
      System.out.println("Completed checking successfully.");
    }
    catch (InterpreterException e) {
      debug.log(e);
      e.printUserMessage(new PrintWriter(System.out, true));
    }
    return checker.processed();
  }
  
  
  static class NodeDiff {
    
    public void compare(String leftName, Iterable<CompilationUnit> left,
                          String rightName, Iterable<CompilationUnit> right) {
      System.out.println("\n**********************************************************");
      System.out.println("Comparing " + leftName + " with " + rightName);
      System.out.println("**********************************************************");
      if (IterUtil.sizeOf(left) != IterUtil.sizeOf(right)) {
        System.out.println("Can't compare results: mismatched CompilationUnit lists");
      }
      else {
        for (Pair<CompilationUnit, CompilationUnit> p : IterUtil.zip(left, right)) {
          compare(p.first(), p.second());
        }
      }
    }
    
    private void compare(Node left, Node right) {
      if (left.getClass().equals(right.getClass())) {
        Class<?> c = left.getClass();
        while (!c.equals(Node.class)) {
          compareDeclaredFields(c, left, right);
          c = c.getSuperclass();
        }
      }
      else {
        mismatch("Different node classes", left.getClass().getName(), left, right.getClass().getName(), right);
      }
      Field props;
      try { props = Node.class.getDeclaredField("properties"); }
      catch (NoSuchFieldException e) { throw new RuntimeException(e); }
      compareProperties((Map<?,?>) fieldValue(props, left), left, (Map<?,?>) fieldValue(props, right), right);
    }
    
    private void compareDeclaredFields(Class<?> c, Node left, Node right) {
      for (Field f : c.getDeclaredFields()) {
        String name = "field " + c.getName() + "." + f.getName();
        compareObjects(name, fieldValue(f, left), left, fieldValue(f, right), right);
      }
    }
    
    private void compareProperties(Map<?,?> leftProps, SourceInfo.Wrapper left,
                                     Map<?,?> rightProps, SourceInfo.Wrapper right) {
      Set<Object> keys = new HashSet<Object>(leftProps.keySet());
      keys.retainAll(rightProps.keySet());
      Set<Object> leftKeys = new HashSet<Object>(leftProps.keySet());
      leftKeys.removeAll(keys);
      Set<Object> rightKeys = new HashSet<Object>(rightProps.keySet());
      rightKeys.removeAll(keys);
      if (!leftKeys.isEmpty() || !rightKeys.isEmpty()) {
        mismatch("Extra properties", leftKeys.toString(), left, rightKeys.toString(), right);
      }
      for (Object k : keys) {
        compareObjects("property " + k, leftProps.get(k), left, rightProps.get(k), right);
      }
    }
    
    private void compareObjects(String name, Object leftVal, SourceInfo.Wrapper left,
                                 Object rightVal, SourceInfo.Wrapper right) {
      
      if (leftVal == null || rightVal == null) {
        if (leftVal != null || rightVal != null) {
          mismatch("Different " + name, ""+leftVal, left, ""+rightVal, right);
        }
      }
      
      else if (leftVal instanceof IdentifierToken && rightVal instanceof IdentifierToken) {
        String leftName = ((IdentifierToken) leftVal).image();
        String rightName = ((IdentifierToken) rightVal).image();
        if (!leftName.equals(rightName)) {
          mismatch("Different " + name, leftName, left, rightName, right);
        }
      }
      
      else if (leftVal instanceof Node && rightVal instanceof Node) {
        compare((Node) leftVal, (Node) rightVal);
      }
      
      else if (leftVal instanceof List<?> && rightVal instanceof List<?>) {
        List<?> leftList = (List<?>) leftVal;
        List<?> rightList = (List<?>) rightVal;
        if (leftList.size() == rightList.size()) {
          for (Pair<Object, Object> p : IterUtil.zip(leftList, rightList)) {
            compareObjects("element of " + name, p.first(), left, p.second(), right);
          }
        }
        else {
          mismatch("Different lengths of " + name, ""+leftList.size(), left, ""+rightList.size(), right);
        }
      }
      
      else if (leftVal instanceof Option<?> && rightVal instanceof Option<?>) {
        Option<?> leftOpt = (Option<?>) leftVal;
        Option<?> rightOpt = (Option<?>) rightVal;
        if (leftOpt.isSome() && rightOpt.isSome()) {
          compareObjects(name, leftOpt.unwrap(), left, rightOpt.unwrap(), right);
        }
        else if (!leftOpt.isNone() || !rightOpt.isNone()) {
          mismatch("Different " + name, leftVal.toString(), left, rightVal.toString(), right);
        }
      }
      
      else if (supportedObject(leftVal) && supportedObject(rightVal)) {
        if (!leftVal.equals(rightVal)) {
          mismatch("Different " + name, leftVal.toString(), left, rightVal.toString(), right);
        }
      }
      
      else {
        mismatch("Unsupported object type in " + name,
                 leftVal.getClass().getName(), left, rightVal.getClass().getName(), right);
      }
    }
    
    private boolean supportedObject(Object val) {
      return val instanceof String || val instanceof Number || val instanceof Boolean ||
              val instanceof Class<?> || val instanceof EnumSet<?> || val instanceof Enum<?> ||
              val instanceof DJClass || val instanceof Variable || val instanceof Function ||
              val instanceof Type;
    }
    
    private Object fieldValue(Field f, Object receiver) {
      try { f.setAccessible(true); }
      catch (SecurityException e) { /* ignore -- we can't relax accessibility */ }
      try { return f.get(receiver); }
      catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    
    private void mismatch(String description, String leftData, SourceInfo.Wrapper left,
                           String rightData, SourceInfo.Wrapper right) {
      System.out.println("*** " + description);
      System.out.println("Left (" + left.getSourceInfo() + "): " + leftData);
      System.out.println("Right (" + right.getSourceInfo() + "): " + rightData);
    }
    
  }
  
}
