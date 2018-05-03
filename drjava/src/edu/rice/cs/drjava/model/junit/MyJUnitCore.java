package edu.rice.cs.drjava.model.junit;

import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

/**
 * This class is used to run test in parallel.
 * It uses ParallelComputer to parallel test method and test class 
 * @author zhexin
 *
 */
public class MyJUnitCore extends JUnitCore {

	private final RunNotifier runNotifier = new RunNotifier();

	/**
	 * method to run test classes in parallel 
	 * @param listener listener for these test case
	 * @param classes  test cases
	 * @return  result of running these test case
	 */
	public Result parallelRunClasses(RunListener listener, Class<?>[] classes) {
		Runner runner = Request.classes(new ParallelComputer(true, true), classes).getRunner();
		Result result = new Result();
		RunListener resultListener = result.createListener();
		runNotifier.addFirstListener(resultListener);
		runNotifier.addListener(listener);
		try {
			runNotifier.fireTestRunStarted(runner.getDescription());
			runner.run(runNotifier);
			runNotifier.fireTestRunFinished(result);
		} finally {
			removeListener(listener);
			removeListener(resultListener);

		}
		return result;

	}

}
