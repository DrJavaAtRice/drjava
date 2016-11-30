package edu.rice.cs.drjava.model.repl;

import javarepl.ExpressionReader;
import javarepl.Repl;
import javarepl.client.JavaREPLClient;
import javarepl.internal.totallylazy.Mapper;
import javarepl.internal.totallylazy.Option;
import javarepl.internal.totallylazy.Sequence;

import java.io.IOException;

import static javarepl.internal.totallylazy.Callables.compose;
import static javarepl.internal.totallylazy.Option.none;
import static javarepl.internal.totallylazy.Option.some;
import static javarepl.internal.totallylazy.Sequences.sequence;
import static javarepl.internal.totallylazy.Strings.replaceAll;
import static javarepl.internal.totallylazy.Strings.startsWith;
import static javarepl.internal.totallylazy.numbers.Numbers.intValue;
import static javarepl.internal.totallylazy.numbers.Numbers.valueOf;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static javarepl.Utils.applicationVersion;
import static javarepl.Utils.randomServerPort;
import static javarepl.completion.CompletionCandidate.functions.candidateForms;
import static javarepl.completion.CompletionCandidate.functions.candidateValue;
import static javarepl.completion.CompletionResult.methods.fromJson;
import static javarepl.completion.CompletionResult.methods.toJson;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

/** Created by maladat on 1/21/16 by Jimmy Newman. */

public class DrJavaRepl {

    public JavaREPLClient client;

    private static Option<Process> process = none();

    public DrJavaRepl(String workDir) throws Exception {
        this.client = startNewLocalInstance(workDir);
    }

    private JavaREPLClient startNewLocalInstance(String workDir) throws Exception {
        if (getSystemJavaCompiler() == null) {
            System.exit(0);
        }

        int port = randomServerPort();

        ProcessBuilder builder = new ProcessBuilder("java", "-cp", System.getProperty("java.class.path") +
                System.getProperty("path.separator") + workDir, Repl.class.getCanonicalName(), "--port=" + port);
        builder.redirectErrorStream(true);

        process = some(builder.start());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                process.get().destroy();
            }
        });

        JavaREPLClient replClient = new JavaREPLClient("localhost", port);
        if (!waitUntilInstanceStarted(replClient)) {
            System.exit(0);
        }

        return replClient;
    }

    private static boolean waitUntilInstanceStarted(JavaREPLClient client) throws Exception {
        for (int i = 0; i < 500; i++) {
            Thread.sleep(10);
            if (client.status().isRunning())
                return true;
        }

        return false;
    }

    public void dispose() {
        process.get().destroy();
    }
}

