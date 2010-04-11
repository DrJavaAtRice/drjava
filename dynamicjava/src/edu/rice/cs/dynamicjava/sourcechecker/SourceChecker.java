package edu.rice.cs.dynamicjava.sourcechecker;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import koala.dynamicjava.interpreter.NodeProperties;
import koala.dynamicjava.interpreter.error.ExecutionError;
import koala.dynamicjava.parser.wrapper.JavaCCParser;
import koala.dynamicjava.parser.wrapper.ParseError;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.tiger.HookTypeName;
import koala.dynamicjava.tree.visitor.DepthFirstVisitor;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.*;
import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.*;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.collect.IndexedRelation;
import edu.rice.cs.plt.collect.PredicateSet;
import edu.rice.cs.plt.collect.UnindexedRelation;
import edu.rice.cs.plt.collect.Relation;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Box;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Lambda2;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.lambda.Predicate2;
import edu.rice.cs.plt.lambda.SimpleBox;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.recur.RecursionStack2;
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
        if (NodeProperties.hasStatementTranslation(node)) { recur(NodeProperties.getStatementTranslation(node)); }
    }
  }
 
  private static final Map<String, Options> _options;
  static {
    abstract class SourceCheckerOptions extends Options {
      protected abstract TypeSystem makeTypeSystem();
      @Override protected Thunk<? extends TypeSystem> typeSystemFactory() {
        return LambdaUtil.valueLambda(makeTypeSystem());
      }
      @Override public boolean enforceAllAccess() { return true; }
      @Override public boolean prohibitUncheckedCasts() { return false; }
    }
    
    _options = new LinkedHashMap<String, Options>();
    _options.put("jls", new SourceCheckerOptions() {
      public TypeSystem makeTypeSystem() { return new JLSTypeSystem(this, true, true, true, true, true, false); }
    });
    _options.put("ext", new SourceCheckerOptions() {
      public TypeSystem makeTypeSystem() { return new ExtendedTypeSystem(this, true, true, true, false); }
    });
    _options.put("jls-inferred", new SourceCheckerOptions() {
      public TypeSystem makeTypeSystem() { return new JLSTypeSystem(this, true, true, true, true, false, false); }
    });
    _options.put("ext-inferred", new SourceCheckerOptions() {
      public TypeSystem makeTypeSystem() { return new ExtendedTypeSystem(this, true, true, false, false); }
    });
  }
  
  public static void main(String... args) {
    debug.logStart();

    ArgumentParser argParser = new ArgumentParser();
    argParser.supportOption("classpath", "");
    argParser.supportAlias("cp", "classpath");
    argParser.supportOption("opt", 1);
    argParser.supportOption("verbose");
    argParser.requireParams(1);
    final ArgumentParser.Result parsedArgs = argParser.parse(args);
    Iterable<File> cp = IOUtil.parsePath(parsedArgs.getUnaryOption("classpath"));
    Iterable<File> sources = IterUtil.map(parsedArgs.params(), IOUtil.FILE_FACTORY);
    boolean verbose = parsedArgs.hasOption("verbose");
    
    if (parsedArgs.hasOption("opt")) {
      Options opt = _options.get(parsedArgs.getUnaryOption("opt"));
      if (opt == null) { System.out.println("Unrecognized options name: " + parsedArgs.getUnaryOption("opt")); }
      else { processFiles(sources, cp, opt); }
    }
      
    else {
      String canonical = IterUtil.first(_options.keySet());
      Map<String, Iterable<CompilationUnit>> results = new LinkedHashMap<String, Iterable<CompilationUnit>>();
      for (String n : _options.keySet()) {
        System.out.println("============ Checking with type system " + n + " ============");
        results.put(n, processFiles(sources, cp, _options.get(n)));
      }
      for (Map.Entry<String, Iterable<CompilationUnit>> e : results.entrySet()) {
        if (e.getKey().equals(canonical)) continue;
        String compareTo;
        if (e.getKey().endsWith("-inferred")) {
          compareTo = e.getKey().substring(0, e.getKey().length() - "-inferred".length());
        }
        else { compareTo = canonical; }
        
        NodeDiffLog log = new NodeDiffLog(compareTo, _options.get(compareTo).typeSystem(),
                                          e.getKey(), _options.get(e.getKey()).typeSystem(), verbose);
        new NodeDiff(log).compare(results.get(compareTo), e.getValue());
        
        String firstInferred = compareTo + "-inferred";
        String secondInferred = e.getKey() + "-inferred";
        if (results.containsKey(firstInferred) && results.containsKey(secondInferred)) {
          NodeDiffLog log2 = new NodeDiffLog(firstInferred, _options.get(firstInferred).typeSystem(),
                                             secondInferred, _options.get(secondInferred).typeSystem(), verbose);
          new NodeDiff(log2).compare(results.get(firstInferred), results.get(secondInferred));
        }
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
  
  static class NodeDiffLog {
    
    private final String _leftName;
    private final TypeSystem _leftTS;
    private final String _rightName;
    private final TypeSystem _rightTS;
    private final boolean _verbose;
    
    private final Relation<SourceInfo, Location> _commonErrors;
    private final Relation<SourceInfo, Location> _leftErrors;
    private final Relation<SourceInfo, Location> _rightErrors;
    private final Relation<SourceInfo, Location> _polymorphicDeclarations;
    private final Relation<SourceInfo, Location> _inferredInvocations;
    private final Relation<SourceInfo, Location> _explicitInvocations;
    private final Relation<SourceInfo, Location> _simpleWildcards;
    private final Relation<SourceInfo, Location> _extendsWildcards;
    private final Relation<SourceInfo, Location> _superWildcards;
    private final Relation<SourceInfo, MismatchedType> _mismatchedTypes;
    private final Relation<SourceInfo, Cast> _leftCasts;
    private final Relation<SourceInfo, Cast> _rightCasts;
    
    public NodeDiffLog(String leftName, TypeSystem leftTS, String rightName, TypeSystem rightTS, boolean verbose) {
      _leftName = leftName;
      _leftTS = leftTS;
      _rightName = rightName;
      _rightTS = rightTS;
      _verbose = verbose;
      // we use relations because two AST nodes may have the same SourceInfo
      Thunk<Map<SourceInfo, PredicateSet<MismatchedType>>> mapFactory1 = CollectUtil.treeMapFactory();
      Thunk<Map<SourceInfo, PredicateSet<Location>>> mapFactory2 = CollectUtil.treeMapFactory();
      Thunk<Map<SourceInfo, PredicateSet<Cast>>> mapFactory3 = CollectUtil.treeMapFactory();
      Thunk<Set<MismatchedType>> setFactory1 = CollectUtil.hashSetFactory();
      Thunk<Set<Location>> setFactory2 = CollectUtil.hashSetFactory();
      Thunk<Set<Cast>> setFactory3 = CollectUtil.hashSetFactory();
      _commonErrors = new IndexedRelation<SourceInfo, Location>(mapFactory2, setFactory2);
      _leftErrors = new IndexedRelation<SourceInfo, Location>(mapFactory2, setFactory2);
      _rightErrors = new IndexedRelation<SourceInfo, Location>(mapFactory2, setFactory2);
      _polymorphicDeclarations = new IndexedRelation<SourceInfo, Location>(mapFactory2, setFactory2);
      _inferredInvocations = new IndexedRelation<SourceInfo, Location>(mapFactory2, setFactory2);
      _explicitInvocations = new IndexedRelation<SourceInfo, Location>(mapFactory2, setFactory2);
      _simpleWildcards = new IndexedRelation<SourceInfo, Location>(mapFactory2, setFactory2);
      _extendsWildcards = new IndexedRelation<SourceInfo, Location>(mapFactory2, setFactory2);
      _superWildcards = new IndexedRelation<SourceInfo, Location>(mapFactory2, setFactory2);
      _mismatchedTypes = new IndexedRelation<SourceInfo, MismatchedType>(mapFactory1, setFactory1);
      _leftCasts = new IndexedRelation<SourceInfo, Cast>(mapFactory3, setFactory3);
      _rightCasts = new IndexedRelation<SourceInfo, Cast>(mapFactory3, setFactory3);
    }
    
    public void start() {
      System.out.println("\n**********************************************************");
      System.out.println("Comparing " + _leftName + " with " + _rightName);
      System.out.println("**********************************************************");
    }
    
    public void end() {
      System.out.println();
      System.out.println("Common statements with errors: " + sizeString(_commonErrors));
      if (_verbose) { dump(_commonErrors.secondSet()); }
      System.out.println("Left statements with errors: " + sizeString(_leftErrors));
      if (_verbose) { dump(_leftErrors.secondSet()); }
      System.out.println("Right statements with errors: " + sizeString(_rightErrors));
      if (_verbose) { dump(_rightErrors.secondSet()); }
      System.out.println("Polymorphic declarations: " + sizeString(_polymorphicDeclarations));
      if (_verbose) { dump(_polymorphicDeclarations.secondSet()); }
      System.out.println("Inferred polymorphic invocations: " + sizeString(_inferredInvocations));
      if (_verbose) { dump(_inferredInvocations.secondSet()); }
      System.out.println("Explicit polymorphic invocations: " + sizeString(_explicitInvocations));
      if (_verbose) { dump(_explicitInvocations.secondSet()); }
      System.out.println("Simple wildcards: " + sizeString(_simpleWildcards));
      if (_verbose) { dump(_simpleWildcards.secondSet()); }
      System.out.println("Upper-bounded wildcards: " + sizeString(_extendsWildcards));
      if (_verbose) { dump(_extendsWildcards.secondSet()); }
      System.out.println("Lower-bounded wildcards: " + sizeString(_superWildcards));
      if (_verbose) { dump(_superWildcards.secondSet()); }
      System.out.println("Mismatched types: " + sizeString(_mismatchedTypes));
      if (_verbose) { dump(_mismatchedTypes.secondSet()); }
      System.out.println("Left extra casts: " + sizeString(_leftCasts));
      if (_verbose) { dump(_leftCasts.secondSet()); }
      System.out.println("Right extra casts: " + sizeString(_rightCasts));
      if (_verbose) { dump(_rightCasts.secondSet()); }
      System.out.println();
    }
    
    private String sizeString(Relation<?,?> r) {
      int size = r.size();
      int unique = r.firstSet().size();
      if (size == unique) { return "" + size; }
      else { return size + " (" + unique + " unique)"; }    
    }
    
    private void dump(Set<?> s) {
      int counter = 0;
      for (Object obj: s) {
        counter++;
        if (counter > 100) { System.out.println("..."); break; }
        else { System.out.println(obj.toString()); }
      }
    }
    
    public void mismatchedCompilationUnits() {
      System.out.println("Can't compare results: mismatched CompilationUnit lists");
    }
    
    public void commonErrorStatement(String context, SourceInfo.Wrapper left, SourceInfo.Wrapper right) {
      _commonErrors.add(left.getSourceInfo(), new Location(context, left, right));
    }
    
    public void leftErrorStatement(String context, SourceInfo.Wrapper left, SourceInfo.Wrapper right) {
      _leftErrors.add(left.getSourceInfo(), new Location(context, left, right));
    }
    
    public void rightErrorStatement(String context, SourceInfo.Wrapper left, SourceInfo.Wrapper right) {
      _rightErrors.add(left.getSourceInfo(), new Location(context, left, right));
    }
    
    public void polymorphicDeclaration(String context, SourceInfo.Wrapper left, SourceInfo.Wrapper right) {
      _polymorphicDeclarations.add(left.getSourceInfo(), new Location(context, left, right));
    }
    
    public void polymorphicInvocation(String context, SourceInfo.Wrapper left, SourceInfo.Wrapper right,
                                        boolean inferred) {
      if (inferred) { _inferredInvocations.add(left.getSourceInfo(), new Location(context, left, right)); }
      else { _explicitInvocations.add(left.getSourceInfo(), new Location(context, left, right)); }
    }
    
    public void wildcard(String context, SourceInfo.Wrapper left, SourceInfo.Wrapper right,
                          boolean upper, boolean lower) {
      if (lower) { _superWildcards.add(left.getSourceInfo(), new Location(context, left, right)); }
      else if (upper) { _extendsWildcards.add(left.getSourceInfo(), new Location(context, left, right)); }
      else { _simpleWildcards.add(left.getSourceInfo(), new Location(context, left, right)); }
    }
    
    public void extraLeftCast(String context, Class<?> target, SourceInfo.Wrapper left, SourceInfo.Wrapper right) {
      _leftCasts.add(left.getSourceInfo(), new Cast(context, target, left, right));
    }

    public void extraRightCast(String context, Class<?> target, SourceInfo.Wrapper left, SourceInfo.Wrapper right) {
      _rightCasts.add(left.getSourceInfo(), new Cast(context, target, left, right));
    }
    
    public void mismatchedType(String context, Type leftType, SourceInfo.Wrapper left,
                                 Type rightType, SourceInfo.Wrapper right) {
      _mismatchedTypes.add(left.getSourceInfo(), new MismatchedType(context, leftType, left, rightType, right));
    }

    public void mismatch(String description, String context, String leftData, SourceInfo.Wrapper left,
                           String rightData, SourceInfo.Wrapper right) {
      System.out.println("*** " + description + " in " + context);
      System.out.println("Left (" + left.getSourceInfo() + "): " + leftData);
      System.out.println("Right (" + right.getSourceInfo() + "): " + rightData);
    }
    
    private static class Location {
      private final String _context;
      private final SourceInfo _left;
      private final SourceInfo _right;
      public Location(String context, SourceInfo.Wrapper left, SourceInfo.Wrapper right) {
        _context = context; _left = left.getSourceInfo(); _right = right.getSourceInfo();
      }
      public String toString() {
        if (_left.equals(_right)) { return _left + ": " + _context; }
        else { return _left + "/" + _right + ": " + _context; }
      }
    }
    
    private class MismatchedType {
      private final Location _location;
      private final Type _leftType;
      private final Type _rightType;
      public MismatchedType(String context, Type leftType, SourceInfo.Wrapper left,
                             Type rightType, SourceInfo.Wrapper right) {
        _location = new Location(context, left, right);
        _leftType = leftType;
        _rightType = rightType;
      }
      public String toString() {
        return _location + " has types " +
                _leftTS.typePrinter().print(_leftType) + "/" +
                _rightTS.typePrinter().print(_rightType);
      }
    }
    
    private class Cast {
      private final Location _location;
      private final Class<?> _target;
      public Cast(String context, Class<?> target, SourceInfo.Wrapper left, SourceInfo.Wrapper right) {
        _location = new Location(context, left, right);
        _target = target;
      }
      public String toString() {
        return _location + " cast to " + _target.getName();
      }
    }
    
  }
  
  static class NodeDiff {
    
    private final NodeDiffLog _log;
    
    public NodeDiff(NodeDiffLog log) { _log = log; }
    
    public void compare(Iterable<CompilationUnit> left, Iterable<CompilationUnit> right) {
      _log.start();
      if (IterUtil.sizeOf(left) != IterUtil.sizeOf(right)) {
        _log.mismatchedCompilationUnits();
      }
      else {
        for (Pair<CompilationUnit, CompilationUnit> p : IterUtil.zip(left, right)) {
          compare("Compilation unit", p.first(), p.second());
        }
      }
      _log.end();
    }
    
    private void compare(String context, Node left, Node right) {
      if (left.getClass().equals(right.getClass())) {
        if ((left instanceof Statement && !(left instanceof BlockStatement)) ||
             left instanceof VariableDeclaration ||
             left instanceof FieldDeclaration ||
             left instanceof Expression) {
          if (hasNestedError(left)) {
            if (hasNestedError(right)) { _log.commonErrorStatement(context, left, right); }
            else { _log.leftErrorStatement(context, left, right); }
            return;
          }
          else if (hasNestedError(right)) {
            _log.rightErrorStatement(context, left, right);
            return;
          }
        }
        if (NodeProperties.hasMethod(left)) {
          DJMethod m = NodeProperties.getMethod(left);
          if (left instanceof MethodCall && !IterUtil.isEmpty(m.typeParameters())) {
            _log.polymorphicInvocation(context, left, right, ((MethodCall) left).getTypeArgs().isNone());
          }
          else if (left instanceof MethodDeclaration && !IterUtil.isEmpty(m.typeParameters())) {
            _log.polymorphicDeclaration(context, left, right);
          }
        }
        if (NodeProperties.hasConstructor(left)) {
          DJConstructor k = NodeProperties.getConstructor(left);
          if (!IterUtil.isEmpty(k.typeParameters())) {
            Boolean inferred = null;
            if (left instanceof ConstructorCall) { inferred = true; } // doesn't support targs for now
            else if (left instanceof SimpleAllocation) { inferred = ((SimpleAllocation) left).getTypeArgs().isNone(); }
            else if (left instanceof InnerAllocation) { inferred = ((InnerAllocation) left).getTypeArgs().isNone(); }
            if (inferred != null) { _log.polymorphicInvocation(context, left, right, inferred); }
            if (left instanceof ConstructorDeclaration) { _log.polymorphicDeclaration(context, left, right); }
          }
        }
        if (left instanceof HookTypeName) {
          HookTypeName t = (HookTypeName) left;
          _log.wildcard(context, left, right, t.getUpperBound().isSome(), t.getLowerBound().isSome());
        }
        Class<?> c = left.getClass();
        while (!c.equals(Node.class)) {
          compareDeclaredFields(c, left, right);
          c = c.getSuperclass();
        }
        Field props;
        try { props = Node.class.getDeclaredField("properties"); }
        catch (NoSuchFieldException e) { throw new RuntimeException(e); }
        compareProperties(left.getClass(), (Map<?,?>) fieldValue(props, left), left,
                          (Map<?,?>) fieldValue(props, right), right);
      }
      else {
        _log.mismatch("Different classes", context, left.getClass().getName(), left,
                      right.getClass().getName(), right);
      }
    }
    
    private boolean hasNestedError(Node n) {
      final Box<Boolean> result = new SimpleBox<Boolean>(false);
      new PropertiesDepthFirstVisitor() {
        @Override public void run(Node n) {
          if (!result.value()) {
            if (NodeProperties.hasError(n)) { result.set(true); }
            else { super.run(n); }
          }
        }
      }.run(n);
      return result.value();
    }
    
    private void compareDeclaredFields(Class<?> c, Node left, Node right) {
      if (c.equals(ArrayAllocation.class)) {
        // special case -- must use accessors to compare private ArrayInitializers
        ArrayAllocation leftAlloc = (ArrayAllocation) left;
        ArrayAllocation rightAlloc = (ArrayAllocation) right;
        compareObjects("field ArrayAllocation.elementType",
                       leftAlloc.getElementType(), left, rightAlloc.getElementType(), right);
        compareObjects("field ArrayAllocation.typeDescriptor.dimension",
                       leftAlloc.getDimension(), left, rightAlloc.getDimension(), right);
        compareObjects("field ArrayAllocation.typeDescriptor.sizes",
                       leftAlloc.getSizes(), left, rightAlloc.getSizes(), right);
        compareObjects("field ArrayAllocation.typeDescriptor.initialization",
                       leftAlloc.getInitialization(), left, rightAlloc.getInitialization(), right);
      }
      else {
        for (Field f : c.getDeclaredFields()) {
          String name = "field " + c.getName() + "." + f.getName();
          compareObjects(name, fieldValue(f, left), left, fieldValue(f, right), right);
        }
      }
    }
    
    private void compareProperties(Class<?> c, Map<?,?> leftProps, SourceInfo.Wrapper left,
                                     Map<?,?> rightProps, SourceInfo.Wrapper right) {
      Set<Object> keys = new HashSet<Object>(leftProps.keySet());
      keys.retainAll(rightProps.keySet());
      Set<Object> leftKeys = new HashSet<Object>(leftProps.keySet());
      leftKeys.removeAll(keys);
      Set<Object> rightKeys = new HashSet<Object>(rightProps.keySet());
      rightKeys.removeAll(keys);
      for (Object k : keys) {
        compareObjects("property " + k + " of " + c.getName(), leftProps.get(k), left, rightProps.get(k), right);
      }
      if (leftKeys.contains("assertedType")) {
        // asserted on left, checked on right
        _log.extraRightCast(c.getName(), (Class<?>) ((Thunk<?>) leftProps.get("assertedType")).value(), left, right);
        leftKeys.remove("assertedType");
        rightKeys.remove("checkedType");
      }
      if (rightKeys.contains("assertedType")) {
        // asserted on right, checked on left
        _log.extraLeftCast(c.getName(), (Class<?>) ((Thunk<?>) rightProps.get("assertedType")).value(), left, right);
        leftKeys.remove("checkedType");
        rightKeys.remove("assertedType");
      }
      if (leftKeys.contains("checkedType")) {
        // incidental internal conversion
        leftKeys.remove("checkedType");
      }
      if (rightKeys.contains("checkedType")) {
        // incidental internal conversion
        rightKeys.remove("checkedType");
      }
      if (!leftKeys.isEmpty() || !rightKeys.isEmpty()) {
        _log.mismatch("Extra properties", c.getName(), leftKeys.toString(), left, rightKeys.toString(), right);
      }
    }
    
    private void compareObjects(String context, Object leftVal, SourceInfo.Wrapper left,
                                 Object rightVal, SourceInfo.Wrapper right) {
      
      if (leftVal == null || rightVal == null) {
        if (leftVal != null || rightVal != null) {
          _log.mismatch("Different value", context, ""+leftVal, left, ""+rightVal, right);
        }
      }
      
      else if (leftVal instanceof Object[] && rightVal instanceof Object[]) {
        compareObjects(context, Arrays.asList((Object[]) leftVal), left, Arrays.asList((Object[]) rightVal), right);
      }
      
      else if (leftVal instanceof Thunk<?> && rightVal instanceof Thunk<?> ||
                leftVal instanceof Lambda<?,?> && rightVal instanceof Lambda<?,?> ||
                leftVal instanceof Lambda2<?,?,?> && rightVal instanceof Lambda2<?,?,?>)  {} // ignore
      
      else if (leftVal instanceof IdentifierToken && rightVal instanceof IdentifierToken) {
        String leftName = ((IdentifierToken) leftVal).image();
        String rightName = ((IdentifierToken) rightVal).image();
        if (!leftName.equals(rightName)) {
          _log.mismatch("Different value", context, leftName, left, rightName, right);
        }
      }
      
      else if (leftVal instanceof Node && rightVal instanceof Node) {
        compare(context, (Node) leftVal, (Node) rightVal);
      }
      
      else if (leftVal instanceof List<?> && rightVal instanceof List<?>) {
        List<?> leftList = (List<?>) leftVal;
        List<?> rightList = (List<?>) rightVal;
        if (leftList.size() == rightList.size()) {
          for (Pair<Object, Object> p : IterUtil.zip(leftList, rightList)) {
            compareObjects("element of " + context, p.first(), left, p.second(), right);
          }
        }
        else {
          _log.mismatch("Different lengths", context, ""+leftList.size(), left, ""+rightList.size(), right);
        }
      }
      
      else if (leftVal instanceof Option<?> && rightVal instanceof Option<?>) {
        Option<?> leftOpt = (Option<?>) leftVal;
        Option<?> rightOpt = (Option<?>) rightVal;
        if (leftOpt.isSome() && rightOpt.isSome()) {
          compareObjects(context, leftOpt.unwrap(), left, rightOpt.unwrap(), right);
        }
        else if (!leftOpt.isNone() || !rightOpt.isNone()) {
          _log.mismatch("Different value", context, leftVal.toString(), left, rightVal.toString(), right);
        }
      }
      
      else if (leftVal instanceof Pair<?,?> && rightVal instanceof Pair<?,?>) {
        Pair<?,?> leftPair = (Pair<?,?>) leftVal;
        Pair<?,?> rightPair = (Pair<?,?>) rightVal;
        compareObjects(context, leftPair.first(), left, rightPair.first(), right);
        compareObjects(context, leftPair.second(), left, rightPair.second(), right);
      }
      
      else if (leftVal instanceof DJClass && rightVal instanceof DJClass) {
        if (!sameClass((DJClass) leftVal, (DJClass) rightVal)) {
          _log.mismatch("Different value", context, leftVal.toString(), left, rightVal.toString(), right);
        }
      }
      
      else if (leftVal instanceof Variable && rightVal instanceof Variable) {
        if (!sameVariable((Variable) leftVal, (Variable) rightVal)) {
          _log.mismatch("Different value", context, leftVal.toString(), left, rightVal.toString(), right);
        }
      }
      
      else if (leftVal instanceof Function && rightVal instanceof Function) {
        if (!sameFunction((Function) leftVal, (Function) rightVal)) {
          _log.mismatch("Different value", context, leftVal.toString(), left, rightVal.toString(), right);
        }
      }
      
      else if (leftVal instanceof Type && rightVal instanceof Type) {
        if (!sameType((Type) leftVal, (Type) rightVal)) {
          _log.mismatchedType(context, (Type) leftVal, left, (Type) rightVal, right);
        }
      }
      
      else if (supportedAtom(leftVal) && supportedAtom(rightVal)) {
        if (!leftVal.equals(rightVal)) {
          _log.mismatch("Different value", context, leftVal.toString(), left, rightVal.toString(), right);
        }
      }
      
      else {
        _log.mismatch("Unsupported object type", context,
                      leftVal.getClass().getName(), left, rightVal.getClass().getName(), right);
      }
    }
    
    private boolean supportedAtom(Object val) {
      return val instanceof String || val instanceof Number || val instanceof Boolean ||
              val instanceof Character || val instanceof Class<?> ||
              val instanceof EnumSet<?> || val instanceof Enum<?>;
    }
    
    private boolean sameClass(DJClass left, DJClass right) {
      return left.getClass().equals(right.getClass()) && left.fullName().equals(right.fullName());
    }
    
    private boolean sameVariable(Variable left, Variable right) {
      if (left.getClass().equals(right.getClass())) {
        boolean result = true;
        if (left instanceof DJField) {
          DJClass leftClass = ((DJField) left).declaringClass();
          DJClass rightClass = ((DJField) right).declaringClass();
          result &= (leftClass == rightClass) ||
                    (leftClass != null && rightClass != null && sameClass(leftClass, rightClass));
        }
        result &= left.declaredName().equals(right.declaredName());
        result &= sameType(left.type(), right.type());
        // doesn't handle shadowed variables, but that shouldn't be an issue in practice
        return result;
      }
      else { return false; }
    }
    
    private boolean sameFunction(Function left, Function right) {
      if (left.getClass().equals(right.getClass())) {
        boolean result = true;
        if (left instanceof DJMethod) {
          // used declared signatures; differences in substitution types are handled by type comparisons elsewhere
          left = ((DJMethod) left).declaredSignature();
          right = ((DJMethod) right).declaredSignature();
          result &= left.getClass().equals(right.getClass());
          DJClass leftClass = ((DJMethod) left).declaringClass();
          DJClass rightClass = ((DJMethod) right).declaringClass();
          result &= (leftClass == rightClass) ||
                    (leftClass != null && rightClass != null && sameClass(leftClass, rightClass));
        }
        result &= left.declaredName().equals(right.declaredName());
        result &= sameType(left.returnType(), right.returnType());
        result &= IterUtil.sizeOf(left.typeParameters()) == IterUtil.sizeOf(right.typeParameters());
        result &= IterUtil.sizeOf(left.parameters()) == IterUtil.sizeOf(right.parameters());
        if (result) {
          for (Pair<VariableType, VariableType> p : IterUtil.zip(left.typeParameters(), right.typeParameters())) {
            result &= sameType(p.first(), p.second());
          }
          for (Pair<LocalVariable, LocalVariable> p : IterUtil.zip(left.parameters(), right.parameters())) {
            result &= sameVariable(p.first(), p.second());
          }
        }
        return result;
      }
      else { return false; }
    }
    
    private boolean sameType(Type left, final Type right) {
      return new TypeStructComparer().contains(left, right);
    }
    
    private Object fieldValue(Field f, Object receiver) {
      try { f.setAccessible(true); }
      catch (SecurityException e) { /* ignore -- we can't relax accessibility */ }
      try { return f.get(receiver); }
      catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    
    private class TypeStructComparer implements Predicate2<Type, Type> {
      private final RecursionStack2<Type, Type> _stack = new RecursionStack2<Type, Type>();
      
      public boolean contains(Type left, final Type right) {
        if (left.getClass().equals(right.getClass())) {
          return left.apply(new TypeAbstractVisitor<Boolean>() {
            @Override public Boolean defaultCase(Type left) { return true; }
            @Override public Boolean forArrayType(ArrayType left) {
              return contains(left.ofType(), ((ArrayType) right).ofType());
            }
            @Override public Boolean forClassType(ClassType left) {
              return sameClass(left.ofClass(), ((ClassType) right).ofClass());
            }
            @Override public Boolean forParameterizedClassType(ParameterizedClassType left) {
              return forClassType(left) && compareList(left.typeArguments(),
                                                        ((ParameterizedClassType) right).typeArguments());
            }
            @Override public Boolean forBoundType(BoundType left) {
              return compareList(left.ofTypes(), ((BoundType) right).ofTypes());
            }
            @Override public Boolean forVariableType(VariableType left) {
              return handleBoundedSymbol(left.symbol(), ((VariableType) right).symbol());
            }
            @Override public Boolean forWildcard(Wildcard left) {
              return handleBoundedSymbol(left.symbol(), ((Wildcard) right).symbol());
            }
            
            private boolean handleBoundedSymbol(BoundedSymbol left, BoundedSymbol right) {
              boolean result = true;
              if (left.generated()) { result &= right.generated(); }
              else { result &= !right.generated() && left.name().equals(right.name()); }
              if (result) {
                Lambda2<Type, Type, Boolean> recur = LambdaUtil.asLambda(TypeStructComparer.this);
                result &= _stack.apply(recur, true, left.upperBound(), right.upperBound());
                result &= _stack.apply(recur, true, left.lowerBound(), right.lowerBound());
              }
              return result;
            }
            
          });
        }
        else { return false; }
      }
      
      private boolean compareList(Iterable<? extends Type> lefts, Iterable<? extends Type> rights) {
        return IterUtil.sizeOf(lefts) == IterUtil.sizeOf(rights) && IterUtil.and(lefts, rights, this);
      }
        
    }
    
  }
  
}
