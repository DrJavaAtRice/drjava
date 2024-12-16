\# Concurrency Overview in DrJava

\#\# DrJava Architecture

DrJava is a java application that utilizes two Java Virtual Machines (JVMs): a MainJVM that supports the user interface, DrJava editor, and DrJava compilation; and an InterpreterJVM that runs the interpreter, as well as unit tests. DrJava utilizes the Java RMI library to communicate between the MainJVM and InterpreterJVM. 

\#\# JVM Threads

Each JVM has two base threads that are of import to DrJava: the main thread, that begins the program execution, and the event handling thread, which handles all input events. The main thread is typically sent to immediately terminate after initialization in Swing programs. Most code is run in the event thread, with some exceptions. Any large task (such as with the GUI) can stall out the event queue. The solution to this is to allow asynchronous execution of these tasks in a separate thread. The addition of these async threads changes the complexity of DrJava from a single threaded application to a multi threaded application, which requires additional attention.

\#\# The Async Threads

The aforementioned asynchronous threads do not run in the event thread, and therefore are exceptions to Swing’s rule of everything running synchronously in the event thread. Additional concurrency safeguards are required for this to work properly. Most commonly, locking and unlocking critical sections of code are used. 

Unfortunately, some portions of DrJava have been created under the assumption that they will exclusively be run in the event thread. This means that either they cannot utilize the async threads, or that they illegally use the async threads.

\#\# A note on invokeLater()

When you call SwingUtilities.invokeLater(), it places the task in the event queue, ensuring it runs asynchronously on the Event Dispatch Thread (EDT). This is particularly useful for UI updates, as the EDT is the only thread that should modify Swing components. If an event on the queue itself spawns a new thread, the event won’t necessarily go back into the event queue. Instead, it’s processed immediately by the thread that spawned it (the new thread). The task in the queue would run as expected, but any new tasks it spawns are handled separately and would not go through the event queue unless explicitly scheduled ( with SwingUtilities.invokeLater() or Utilities.invokeLater()). This is crucial to avoid a situation where threads block the event queue or perform actions that require UI updates directly from non-UI threads. As a result, tasks that modify the UI or trigger new tasks should be scheduled to run on the EDT rather than executed immediately by a worker thread.

\#\# State of Concurrency for Various EventNotifiers  
To improve concurrency in Event Notifiers and move away from the Swing-based model:  
One potential improvement is adopting parallel streams, available in Java 8, to process listener notifications concurrently, enhancing performance on multi-core systems. By replacing traditional for-loops with parallelStream(), event dispatching can be split across multiple threads, making the system more responsive without requiring manual thread management. Additionally, a CopyOnWriteArrayList can replace the current listener list to ensure thread-safe operations, particularly when reads significantly outnumber writes, without requiring explicit locking mechanisms. This allows listeners to be added or removed concurrently without disrupting ongoing notifications. Additionally, there exist cases where a removal spawns a new Thread, causing the listener to be removed in the arbitrary future, which poses potential issues with execution orders.

\#\# AsyncTaskLauncher   
First, using an ExecutorService with a fixed thread pool instead of creating a new thread for each task would optimize thread management, reduce overhead, and prevent resource exhaustion. Separating UI-related tasks, like disabling and enabling components, from task execution would improve maintainability and clarity. The lockUI mechanism in the AsyncTaskLauncher can be problematic because it locks the entire user interface, potentially leading to unresponsiveness or frustration for users, especially if the task takes a long time. Additionally, it can lead to deadlock situations or inconsistent behavior if other parts of the program need to interact with the UI while it's locked. A better approach would be to use SwingWorker, a built-in class in Java that manages background tasks and updates the UI in a non-blocking manner. SwingWorker handles tasks on a separate thread, ensuring the UI remains responsive, and updates the UI on the Event Dispatch Thread (EDT) when necessary. This removes the need for manually locking the UI and enables smooth cancellation handling and progress feedback. Moreover, AsyncTaskLauncher creates a runnable that is called in Utilities.invokeLater. This means that an asynchronous task needs to wait for the event queue to empty before running, slowing down the potential async call. The task is ultimately async, but the creation of the async task stalls for potentially too long. For further improvements, using a progress bar or modal dialog instead of locking the entire UI, along with providing proper task cancellation and timeout mechanisms, would enhance the user experience by keeping the interface interactive while managing long-running tasks efficiently.

\#\# Current JVM Communication  
\#\#\# Overview  
Communication between the MainJVM and InterpreterJVM are entirely done within the class definitions of each. In most cases, an operation will call a method in the MainJVM, which then communicates with the InterpreterJVM, rather than invoking the InterpreterJVM itself.

One exception is the SimpleInteractionsModel, which calls the Interpreter it created itself instead of relying on the MainJVM. The RMIInteractionsModel uses the MainJVM, and contains many of the calls that create communication between the MainJVM and the InterpreterJVM.

Any calls that might have been directly sent to the InterpreterJVM are instead funneled into the MainJVM, which then transfers the call into the InterpreterJVM how it sees fit. The MainJVM handles the different situations and conditions that the InterpreterJVM might be in with the different State classes. 

\#\#\# Structure  
The MainJVM does \*not\* hold any reference to the InterpreterJVM. Instead, everything is handled through a \_state field, which contains the current state of the JVMs. State types contain the current interpreter (not the InterpreterJVM), as well as different definitions for handling certain cases such as start and dispose. The MainJVM accesses the InterpreterJVM through a reference in the interpreter contained in the State. 

Most files do not know about the existence of an InterpreterJVM. As stated before, there are a few exceptions. Any calls they wish to make are called to the MainJVM, which then passes the call to the InterpreterJVM it knows about. Most calls simply use the same method in the InterpreterJVM, but some modify the call, do some preprocessing, or call a completely different method. One example of this is setting the package scope on line 450 of MainJVM.java, which calls the interpret function of the InterpreterJVM.

The InterpreterJVM is initialized once and held in a singleton ONLY. It contains a list of interpreters, and additionally categorizes them as active, default, or busy. The InterpreterJVM does less of the communication between the two JVMs.

Both JVMs need to be set up with a setup method before they are ready to be used. The MainJVM’s setup method creates the InterpreterJVM with the correct settings.

All communication methods must handle a RemoteException, in the cases where one JVM is unable to communicate for any reason.

\#\#\# MainJVM  
The MainJVM does most of the communicating. It can change many of the InterpreterJVM’s settings (lines 548-589). It can also change the internal structure of the interpreters within the InterpreterJVM, such as adding and removing interpreters, as well as designating an interpreter as active or default (lines 500 \- 541). There are similar, additional methods that change aspects of the InterpreterJVM such as classpath earlier in the MainJVM (lines 371-455). All of these are done by invoking the InterpreterJVM’s version of the method with one exception. The setPackageScope method (line 450\) uses the InterpreterJVM’s interpret method. The methods in the InterpreterJVM other than interpret are all using synchronized.

The MainJVM has an interpret method (line 353), which simply calls the interpret method of the InterpreterJVM. \*\*No\*\* explicit locking is used for this method in either JVM, but it is typically called in a Runnable that creates a thread that calls this function (which does \*\*\*NOT\*\*\* imply sequential execution). Because of this, there may be some concurrency issues in interpretation (as well as in setPackageScope).

The MainJVM has methods that handle the creation, deletion, and otherwise handling of the state of the InterpreterJVM (lines 150-195). These are the methods that notify the InterpreterJVM if it needs to stop.

The MainJVM contains methods that propagate tests to the InterpreterJVM and results to JUnit (lines 464-482).

\#\#\# InterpreterJVM  
The InterpreterJVM does less of the communicating, but still has some. At setup, the InterpreterJVM changes its standard input, output, and error to ask the MainJVM to conduct those tasks (lines 184-207). There is no locking.

The InterpreterJVM notifies the MainJVM of any testing, results, or errors and propagates that to the MainJVM (lines 644-703). There is no locking.

The InterpreterJVM constantly checks to make sure the MainJVM is still alive (AbstractSlaveJVM.java line 81). There is no locking.

\#\# Potential Lock Issues  
There are many instances where the documentation dictates that a method does not need a lock because it will be run in the event thread and therefore be implicitly sequential. One such case is the GlobalEventNotifier. It currently utilizes locking, when none are needed because notifiers are run in the event thread.

Additionally, lack of proper monitoring means that all fields are required to have final or volatile, which unnecessarily restricts optimization among other issues. 

There are currently three locking schemes in use. They are:  
\* Java’s synchronized()  
\* Swing’s methods along with the Event Thread  
\* Explicit Java locks  
A large amount of variety exists in when each of these three are used. It is largely unknown within DrJava where each method is being used, and usage is nonuniform.   
\<br/\>\<br/\>

Some issues with the current system include:  
\* Nonatomic reads, where different fields of an object are read without locking or similar, can lead to reading an inconsistent state if the fields were changed while reading  
\* Writing after a read can lead to writing incorrect information, if the value at the read is used and was changed after the read and before the write  
\* Testing is extremely difficult because the order of tasks in the Event Queue is unknown and not controlled  
\* Order of tests, which may be unknown, can also lead to difficulty testing

\# Suggestions for Future Work  
\#\# Annotations for Lock Checking  
Currently there does not exist a built-in way in Java to enforce locking requirements on objects. Primitives have a volatile keyword that allows for consistent concurrency handling, but there does not exist such a thing for objects that require an explicit or implicit locking mechanism. These mechanisms include ReadWriteLocks, synchronized(Object lock), and Swing’s synchronization methods. 

These methods are not used consistently or uniformly throughout DrJava, which makes checking for locking correctness harder. 

The idea behind using annotations is multi-faceted. Checking these annotations is a compile-time check and requires no additional work from the programmers other than correctly tagging variables and methods. This also means that there is no difference between compiled code using annotations and compiled code without the annotations. These annotations are strictly to verify that synchronization rules are being followed. Additionally, there are several pre-existing annotation packages that easily allow implementation and usage. They also include instructions on how to include the compile-time checking into an Ant build.

It is currently unknown whether these checkers satisfy the requirements for the variety of locking protocols used or if any checker is tractable, but it is worth investigating.

Some packages that were looked at include:  
\* \[Checker Framework\](https://checkerframework.org/manual/\#lock-checker)  
\* This framework was the most extensively researched and tested  
\* It does not check whether assignments were done under a lock  
\* Using the lock checking tags it may be possible to check that certain methods are called in the Event Thread only  
\* This framework definitely checks for dereferences under locks and synchronized  
\* \[Error Prone\](https://errorprone.info/)  
	\* This was not able to be tested due to local issues  
	\* It seems at a glance to be more strict but less flexible than Checker Framework  
	\* Assignments under locks is checked, according to examples and documentation  
\* \[Find Bugs\](https://sourceforge.net/projects/findbugs/)  
	\* This was not extensively researched or tested  
\* \[Loci\](https://www2.it.uu.se/research/upmarc/loci/)  
	\* This has not been researched at all, but was listed as a potential resource in Checker Framework  
	\* It might not be updated

None of these packages have been checked explicitly if they run on Java 8\.  
Additionally, open source and licensing compatibility were not checked.

\#\# Web-Hosted Structured Logs  
As a result of less synchronization and locking, there are several heisenbugs related to inconsistent internal state from concurrent operations \- particularly, in relation to lingering GUI bugs. In many applications, using logging for the purpose of catching concurrency problems introduces additional delays and can potentially mask concurrency bugs. However, for DrJava, utilizing logs to trace which functions are executing concurrently to find how shared variables become inconsistent state could be beneficial. Currently, invokeLater places Runnables at the end of the queue instead of relying on explicit synchronization to give a task’s dependencies a chance to complete beforehand (meaning concurrency bugs are already being masked), and logging is already being used for general debugging purposes. We suggest expanding logging capabilities to include fields such as time a Runnable was started/ended, any data dependencies of the Runnable, and keywords that would enable efficient searching and analysis of log files. 

Ideally, future developers of DrJava could filter the information contained in the log files collected when suspicious behavior occurs in a web application for easy use.

\#\# Weighted Priority Queues within Event Thread  
A  Weighted Priority Queue (WPQ) for event threads would prioritize the execution of notifications based on the importance or urgency of the events being processed. For instance, when multiple listeners need to be notified about various model changes (e.g., opening a project, modifying a file, or handling UI updates), the WPQ can assign weights to these events. Weights could be based on factors such as the type of event, its impact on the system or the timing requirements. For example, UI-related events might be given higher priority because they directly affect user experience over background tasks. Additionally, DrJava could create multiple WPQs based on estimated time the thread would take to execute, which could lead to more efficient modification and polling (perhaps using ConcurrentLinkedQueues, avoiding LinkedBlockingQueue to limit the amount of locking/synchronization necessary). With more efficient processing of Runnables on the event queue(s), developers could start pulling the miscellaneous threads spawned and running outside the event queue back to the event queue to streamline concurrency management in DrJava. See “Additional Synchronization Primitives and/or Atomic Operations” Section for more information on the Event Queue.

\#\# Additional Synchronization Primitives and/or Atomic Operations  
If we want to continue moving away from Swing’s approach to concurrency, a future project could attempt to implement Data Driven Futures to allow for strict thread monitoring and correctness guarantees at the cost of minimal synchronization. In the current implementation of DrJava, tasks are placed in the event thread to guarantee execution in a certain order, which is unnecessary in many cases. With futures, we could add greater flexibility in execution and guarantee the necessary result dependencies are fulfilled while executing a given task. Although SwingWorker technically implements the Future interface in Java 8, it does not utilize the flexibility of futures to their full potential, so we recommend extending CompletableFuture, CountedCompleter, and/or SwingWorker to implement some notion of priority inversion to rollback the executing Runnable on the event queue if it was waiting on a dependency to finish processing. To facilitate this, we could store rolled-back tasks in a set that, after being notified that all dependencies have finished processing using a Data Driven Future, could be removed from the set and placed at the front of the event thread to finish executing.  
While less sophisticated than Data Driven Futures, another potential solution for dependency monitoring is a counter in a parent task that a child task atomically increments upon completion. Once the counter increments to the necessary number to continue processing, the parent task would be free to read the result of the child task and continue executing (this approach is likely to introduce concurrency issues and probably not worth the additional complexity, though).  
Similarly, DrJava desperately needs atomic operations for updating the GUI. The current approach declaring all non-final shared variables as volatile prevents compiler optimization and does not guarantee correctness.

\#\# Modernize Main-Interpreter JVM Interactions

RMI is unnecessarily heavy-duty for the purpose of communicating between the two JVMs (and also a potential security risk for communicating sensitive test results back to the Main JVM over the Internet). Ideally, there would be another background thread where the two JVMs could pass messages (results and commands) to each other.

\#\# Use java.util.concurrent

\#\#\# Concurrent Data Structures

Currently, there are many data structures used that assume single thread execution. Over the years, some of these data structures have been haphazardly updated for a concurrent world. One of these is the listeners list in EventNotifier, which is implemented as an augmented linked list. Add and removal methods have been augmented to include a heavy lock or spawn a new thread that then removes the item in the arbitrary future. Instead, DrJava should use the concurrent versions of these data structures, which not only negates the need for manually augmenting each data structure, but also improves the functionality and execution of the methods.

\#\#\# ReadWriteLock

The locking mechanism currently used is with a custom ReaderWriterLocking class. In the time since DrJava was initially developed, a native locking mechanism in java.util.concurrent has been introduced. Using the ReadWriteLock from this library would be ideal, since it would be updated and supported, as well as work well with other concurrent classes that should be used. Additionally, using a single global lock for objects can reduce effectiveness. More fine-grained and local locking should be conducted.

