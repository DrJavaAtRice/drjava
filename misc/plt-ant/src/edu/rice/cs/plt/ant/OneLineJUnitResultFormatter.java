package edu.rice.cs.plt.ant;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;

/**
 * Prints out one-line summaries if tests succeed.
 *
 * @see FormatterElement
 * @see PlainJUnitResultFormatter
 */
public class OneLineJUnitResultFormatter implements JUnitResultFormatter {

    /**
     * Where to write the log to.
     */
    private OutputStream out;

    /**
     * Used for writing the results.
     */
    private PrintWriter output;

    /**
     * Used as part of formatting the results.
     */
    private StringWriter results;

    /**
     * Used for writing formatted results to.
     */
    private PrintWriter resultWriter;

    /**
     * Formatter for timings.
     */
    private NumberFormat numberFormat = NumberFormat.getInstance();

    /**
     * Output suite has written to System.out
     */
    private String systemOutput = null;

    /**
     * Output suite has written to System.err
     */
    private String systemError = null;

    /**
     * Constructor for OneLineJUnitResultFormatter.
     */
    public OneLineJUnitResultFormatter() {
        results = new StringWriter();
        resultWriter = new PrintWriter(results);
    }

    /**
     * Sets the stream the formatter is supposed to write its results to.
     * @param out the output stream to write to
     */
    public void setOutput(OutputStream out) {
        this.out = out;
        output = new PrintWriter(out);
    }

    /**
     * @see JUnitResultFormatter#setSystemOutput(String)
     */
    /** {@inheritDoc}. */
    public void setSystemOutput(String out) {
        systemOutput = out;
    }

    /**
     * @see JUnitResultFormatter#setSystemError(String)
     */
    /** {@inheritDoc}. */
    public void setSystemError(String err) {
        systemError = err;
    }

    /**
     * The whole testsuite started.
     * @param suite the test suite
     */
    public void startTestSuite(JUnitTest suite) {
 // no output
    }

    /**
     * The whole testsuite ended.
     * @param suite the test suite
     */
    public void endTestSuite(JUnitTest suite) {
        StringBuffer sb = new StringBuffer("Testsuite: ");
        sb.append(suite.getName());
        sb.append(StringUtils.LINE_SEP);
        sb.append("Tests run: ");
        sb.append(suite.runCount());
        sb.append(", Failures: ");
        sb.append(suite.failureCount());
        sb.append(", Errors: ");
        sb.append(suite.errorCount());
        sb.append(StringUtils.LINE_SEP);

 String simpleName = suite.getName();
 int dotIndex = simpleName.lastIndexOf('.');
 if (dotIndex>=0) {
     simpleName = simpleName.substring(dotIndex+1);
 }
        StringBuffer sbTime = new StringBuffer(simpleName);
 while(sbTime.toString().length()<45) { sbTime.append(' '); }
        sbTime.append(numberFormat.format(suite.getRunTime() / 1000.0));
        sbTime.append(" sec");
        sbTime.append(StringUtils.LINE_SEP);

        // append the err and output streams to the log
        if (systemOutput != null && systemOutput.length() > 0) {
            sb.append("------------- Standard Output ---------------")
                    .append(StringUtils.LINE_SEP)
                    .append(systemOutput)
                    .append("------------- ---------------- ---------------")
                    .append(StringUtils.LINE_SEP);
        }

        if (systemError != null && systemError.length() > 0) {
            sb.append("------------- Standard Error -----------------")
                    .append(StringUtils.LINE_SEP)
                    .append(systemError)
                    .append("------------- ---------------- ---------------")
                    .append(StringUtils.LINE_SEP);
        }

        if (output != null) {
     output.write(sbTime.toString());
     if ((suite.failureCount()!=0) || (suite.errorCount()!=0)) {
  try {
      output.write(sb.toString());
      resultWriter.close();
      output.write(results.toString());
  } finally {
      if (out != System.out && out != System.err) {
   FileUtils.close(out);
      }
  }
     }
     output.flush();
 }
    }

    /**
     * A test started.
     * @param test a test
     */
    public void startTest(Test test) {
    }

    /**
     * A test ended.
     * @param test a test
     */
    public void endTest(Test test) {
    }

    /**
     * Interface TestListener for JUnit &lt;= 3.4.
     *
     * <p>A Test failed.
     * @param test a test
     * @param t    the exception thrown by the test
     */
    public void addFailure(Test test, Throwable t) {
        formatError("\tFAILED", test, t);
    }

    /**
     * Interface TestListener for JUnit &gt; 3.4.
     *
     * <p>A Test failed.
     * @param test a test
     * @param t    the assertion failed by the test
     */
    public void addFailure(Test test, AssertionFailedError t) {
        addFailure(test, (Throwable) t);
    }

    /**
     * A test caused an error.
     * @param test  a test
     * @param error the error thrown by the test
     */
    public void addError(Test test, Throwable error) {
        formatError("\tCaused an ERROR", test, error);
    }

    /**
     * Format the test for printing..
     * @param test a test
     * @return the formatted testname
     */
    protected String formatTest(Test test) {
        if (test == null) {
            return "Null Test: ";
        } else {
            return "Testcase: " + test.toString() + ":";
        }
    }

    /**
     * Format an error and print it.
     * @param type the type of error
     * @param test the test that failed
     * @param error the exception that the test threw
     */
    protected synchronized void formatError(String type, Test test,
                                            Throwable error) {
        if (test != null) {
            endTest(test);
        }

        resultWriter.println(formatTest(test) + type);
        resultWriter.println(error.getMessage());
        String strace = JUnitTestRunner.getFilteredTrace(error);
        resultWriter.println(strace);
        resultWriter.println();
    }
}
