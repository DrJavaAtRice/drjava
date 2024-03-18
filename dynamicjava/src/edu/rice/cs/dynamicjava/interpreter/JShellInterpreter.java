import jdk.jshell.Diag;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
public class JShellInterpreter extends Interpreter {

    private final Options _opt;
    private TypeContext _typeContext;
    private RuntimeBindings _bindings;
    private JShell js;

    public Interpreter(Options opt, TypeContext typeContext, RuntimeBindings bindings) {
        super(opt, typeContext, bindings);
        makeShell();
    }

    public Interpreter(Options opt) {
        super(opt);
        makeShell();
    }

    public Interpreter(Options opt, ClassLoader loader) {
        super(opt, loader);
        makeShell();
    }

    private void makeShell() throws InterpreterException {
        try {
            js = JShell.create();
        } catch (IllegalStateException e) {
            //Potentially exit system or try to create a new JShell instance
            System.err.println("JShell is not available in this environment");
            throw new InterpreterException(e);
        }
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
            return new JavaCCParser(new StringReader(code), _opt).parseStream();
        }
        catch (ParseError e) {
            throw new ParserException(e);
        }
    }

    private TypeContext typeCheck(Iterable<Node> tree) throws InterpreterException {
        try { return new StatementChecker(_typeContext, _opt).checkList(tree); }
        catch (ExecutionError e) { throw new CheckerException(e); }
    }

    private Pair<RuntimeBindings, Option<Object>> evaluate(Iterable<Node> tree) throws InterpreterException {
        try {
            StatementEvaluator.Result r = new StatementEvaluator(_bindings, _opt).evaluateSequence(tree);
            return Pair.make(r.bindings(), r.value());
        }
        catch (WrappedException e) {
            if (e.getCause() instanceof InterpreterException) { throw (InterpreterException) e.getCause(); }
            else { throw e; }
        }
    }
}