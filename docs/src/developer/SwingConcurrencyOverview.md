# Java.Util.Concurrent Documentation

## Executors

Interfaces:

- Executor is an interface for defining custom thread-like subsystems (thread pools, async I/O, task frameworks). Depending on what concrete Executor class used, tasks may execute in a newly created thread, existing task-execution thread, or the thread calling execute. The task may execute sequentially or concurrently.  
- ExecutorService provides an async task execution framework that manages queuing and scheduling of tasks, which allows for controlled shutdown.The ScheduledExecutor subinterface adds support for delayed and periodic task execution.  
- Executor also provides methods for arranging the async execution of any function expressed as Callable (which is analogous to Runnable but bears a result).  
- Future returns the results of a function, allows determination of whether execution has completed, and provides the means to cancel execution.  
- RunnableFuture is a Future that possesses a run method that, upon execution, sets its results.

Implementations:

- ThreadPoolExecutor and ScheduledThreadPoolExecutor provide tunable, flexible thread pools.  
- Executors class provides factory methods for most common kinds and configurations of Executors.  
- FutureTask is an extensible implementation of Futures.  
- ExecutorCompletionService assists in coordinating processing groups of async tasks.  
- ForkJoinPool provides an Executor for processing ForkJoinTasks, which employ a work-stealing scheduler that attains high throughput for tasks conforming to restrictions that often hold in computation-intensive parallel processing.

## Queues

ConcurrentLinkedQueue and ConcurrentLinkedDeque supply efficient scalable thread-safe non-blocking FIFO queue/deque. There are 5 implementations in this package that support an extended BlockingQueue interface with blocking versions of put and take: LinkedBlockingQueue, ArrayBlockingQueue, SynchronousQueue, PriorityBlockingQueue, and DelayQueue. TransferQueue and LinkedTransferQueue have a synchronous transfer method where the producer can optionally block the awaiting consumer.

## Synchronizers

- Semaphore  
- CountDownLatch \- a common utility for blocking until a given number of signals, events, or conditions hold  
- CyclicBarrier \- a resettable multiway synchronization point that is useful in some styles of parallel programming  
- Phaser \- a flexible barrier used to control phased computation among multiple threads  
- Exchanger \- allows two threads to exchange objects at a rendezvous point (useful in pipelines)

## Concurrent Collections

Concurrent Collections include ConcurrentHashMap, ConcurrentSkipListMap, ConcurrentSkipListSet, CopyOnWriteArrayList, and CopyOnWriteArraySet. These concurrent collections are thread safe, but are governed by a single exclusion lock. Synchronized classes are useful to prevent all access to a collection via a single lock. However, unsynchronized collections are preferable when either collections are unshared or are accessible only when holding other locks.

Iterators and Spliterators of these classes provide weakly consistent vs fast-fail traversal. They may proceed concurrently with other operations and will never throw a ConcurrentModificationException. Additionally, they are guaranteed to traverse elements as they existed upon construction exactly once, but are not guaranteed to reflect modifications subsequent to construction.

# Concurrency

## Threads

Threads are lightweight processes that exist within a process. They share the process’ resources, including memory and open files. Every Java application has at least one thread (several if counting “system” threads for memory management and signal handling). Each thread is associated with an instance of the Thread class. Two strategies for using Threads are to:

1. Directly control thread creation and management by instantiating a Thread each time the application needs to initiate an asynchronous task.  
2. Abstract thread management from the rest of the application by passing the application’s task to an *executor.*  
   

If the application creates an instance of Thread, it must provide the code that will run in that thread by:

1. Providing a Runnable object. The Runnable interface defines a single method, run, containing the code executed in the thread (Runnable object passed to the Thread constructor). This approach is considered more general.  
2. Subclassing Thread, which will itself implement Runnable, meaning the subclass needs to redefine run. This approach is easier in simple applications, but is limited  because that task class must be a descendant of Thread.

Methods:

- Thread.start() starts the new thread.  
- Thread.sleep() suspends the current thread for a specified period. The sleep period can be terminated by interrupts. An interrupt is an indication to a thread that it should do something else. A thread sends an interrupt by invoking *interrupt* on the Thread object. A Thread can even interrupt itself by throwing and catching an InterruptedException. There are many methods that throw this exception that are designed to cancel their current operation and return immediately when an interrupt is received.  
- Thread.interrupt() sets the internal interrupt status flag. When the thread checks for an interrupt by invoking the static method Thread.interrupted, the interrupt status is cleared. The non-static isInterrupted method that is used by one thread to query the interrupt status of another does not change this flag.  
- The join method allows one thread to wait for completion of another. If the Thread object is currently executing, t.join() causes the current thread to pause execution until t’s thread terminates. Overloads of join allow to specify a waiting period.

Note that sleep and join are both dependent on the operating system for timing. Do not assume these methods will wait for the exact time specified. Also note that sleep and join both respond to interrupts with an InterruptedException.

## Synchronization

Threads primarily communicate by sharing access to fields and objects reference fields to prevent thread interference and memory consistency errors with synchronization.

Thread contention occurs when two or more threads try to access the same resource simultaneously and cause the Java runtime to execute one or more threads more slowly or suspend their execution (Ex: starvation and livelock)

Interference happens when two operations in different threads act on the same data and interleave access to the shared memory.

Memory consistency errors occur when different threads have inconsistent views of what should be the same data.

A crucial model for reasoning about concurrency in the Java Memory Model is the happens-before relationship: a guarantee that memory writes by one specific statement are visible to another specific statement. Synchronization creates a happens-before relationship in one of the following ways:

1. Each action in a thread happens-before every action in that thread that comes later in the program’s order.  
2. An unlock (synchronized block or method exit) of a monitor happens-before every subsequent lock of that same monitor. happens-before is **transitive** so all actions of a thread prior to unlocking happen-before all actions subsequent to any thread locking that monitor.  
3. A write to a volatile field happens-before every subsequent read of that field. **Note that writes and reads of volatile fields have similar memory consistency effects as entering and exiting monitors but have NO mutual exclusion locking.**  
4. A call to start on a thread happens-before any action in the started thread.  
5. All actions in a thread happen-before any other thread successfully returns from a join on that thread.

NOTE: Methods of all classes in java.util.concurrent and subpackages extend the above guarantees to higher-level synchronization:

1. Actions in a thread prior to placing an object into any concurrent collection happen-before actions after access or removal of that element from collection in another thread.  
2. Actions in a thread prior to submission of Runnable to Executor happen-before execution begins (similar for Callables to ExecutorServices).  
3. Actions prior to releasing synchronizer methods happen-before actions subsequent to successful acquiring method on the same synchronizer object in another thread.  
4. **For each pair of threads successfully exchanging objects via Exchanger, actions prior to the exchange() in each thread happen-before those subsequent to corresponding exchange() in another thread.**  
5. Actions prior to calling CyclicBarrier.await and Phaser.awaitAdvance happen-before actions performed by the barrier. Actions performed by barrier happen-before actions after successfully returning from corresponding await in other threads.  
   

To make a method synchronized, add the synchronized keyword to its declaration. Note that two invocations of synchronized methods on the same object cannot interleave. When one thread is executing a synchronized method for the same object, all other threads that invoke synchronized methods for the same object block until the first thread is done. When a synchronized method exists, the synchronized method establishes a happens-before relationship with any subsequent invocation of synchronized method for the same object (changes to state of object will be visible to all threads).

Also note that constructors cannot be synchronized. When constructing an object shared between threads, make sure reference to that object does not leak prematurely. Make sure the construction of the object is complete before any other thread can get access.

Synchronized methods are useful if an object is visible to more than one thread. All reads or writes to that object’s variables will be done through synchronized methods EXCEPT final fields, which cannot be modified after the object is constructed. Therefore, it is safe to use non-synchronized methods.  
Every object has an intrinsic lock. The thread needs to acquire the intrinsic lock before an access and release the lock once done. When the thread releases the intrinsic lock, a happens-before relationship is established between that action and any subsequent acquisition of the same lock.

When the thread invokes synchronized methods, it automatically acquires the intrinsic lock for that method’s object, and releases the lock once the method returns \- even if the return is caused by uncaught exceptions. If a static synchronized method is invoked, the thread acquires the intrinsic lock for the associated Class object (so access to static fields are controlled by a distinct lock from the instance of the class).

Synchronized statements must specify the object that provides the intrinsic lock. This is useful for fine grained synchronization. Instead of a synchronized method locking the whole object or locks with this, it is possible to create locks solely for locking behavior. B**e very careful to make sure it is safe to interleave access to affected fields.**

A thread can acquire a lock it already owns, so a thread can acquire the same lock more than once (reentrant synchronization). This happens when synchronized code directly or indirectly invokes methods that also contain synchronized code (both sets of code use the same lock). Otherwise, synchronized code would need to do other things to prevent self-blocking.

Atomic action happens effectively all at once and cannot be stopped in the middle of completion. It is possible to specify reads and writes of reference variables and most primitive variables except long and double as atomic. Similarly, it is possible to specify reads and writes as atomic for all variables declared volatile, including long and double variables.

However, memory consistency errors are still possible. Volatile reduces the risk of memory consistency because any write to a volatile variable establishes a happens-before relationship with subsequent reads of the same variable (i.e changes to a volatile variable are visible to other threads. When a thread reads a volatile variable, it sees the side effects of the code up to the change).

## Liveness

- Liveness: a concurrent application’s ability to execute in timely manner  
- Deadlock: two or more threads are blocked forever  
- Starvation: This is a situation where the thread is unable to gain regular access to shared resources and cannot make progress. This happens when resources are made unavailable by greedy threads.  
- Livelock: This condition occurs if the other thread’s action is also a response to the action of another thread and is not blocked, just too busy responding to each other to resume work.

## Guarded Blocks

A guarded block is a block in which threads coordinate actions. They begin by polling a condition that must be true before proceeding.  
It is possible to use Object.wait to suspend the current thread. As a result, the task will not return until another thread issues a notification that the desired condition has occurred. Always invoke wait inside a loop testing for condition being waited for (i.e. Don’t assume that the interrupt was for the particular awaited condition or that the condition is still true).

## Immutable Objects

The strategy for creating immutable objects is as follows:

1. Don’t provide setter methods.  
2. Make all fields final and private.  
3. Don’t allow subclasses to override the method (this can be accomplished by declaring the class as final or by making the constructor private and constructing instances in factory methods).  
4. If instance fields include references to mutable objects, don’t allow these objects to be changed. In other words, don’t provide methods that modify the mutable and don’t share references to mutable objects.

## High Level Concurrency Objects

See the java.util.concurrent package documentation above an overview, but the following is a more detailed description of the provided interfaces in the package:

- Locks: Only one thread can own a Lock at a time, which supports the wait/notify mechanism. tryLocks can back out of an attempt to acquire a lock.  
  - The lockInterruptibly method backs out if another thread sends an interrupt before the lock is acquired.  
- Executors: See above. Note that fork/join is a framework for taking advantage of multiple processors.  
  - Fixed thread pool has a specified number of threads terminated while in use. These tasks are submitted to the pool via an internal queue.   
  - newCachedThreadPool creates an executor with an expandable thread pool, which is good for applications that launch many short-lived tasks.  
- Fork/Join operates on the following principle: “If my portion of the work is small enough, do the work directly. Otherwise, split into two pieces, invoke the two pieces and wait for results”. The ForkJoinTask subclass can be customized by implementing RecursiveTask (which returns a result) or RecursiveAction. In other words, implement the abstract compute() method, which performs the action directly or splits it into smaller tasks.

# 

# 

# Swing

The objective of Swing is to create a user interface that never freezes. There are three types of threads to deal with:  
1\. Initial threads: execute initial application code  
2\. Event dispatch thread: all event-handling code executed here (most code with Swing framework must execute here)  
3\. Worker threads: time-consuming background tasks executed here

Note that the Swing program can create additional threads and thread pools.

## Initial Threads

The initial thread’s job is to create a Runnable object that initializes the GUI and to schedule the object for execution on event dispatch thread. Once the GUI has been created, the program is driven by GUI events (in event dispatch thread)

## Event Dispatch Thread

The Event Dispatch Thread handles most of the GUI events and all event handling code. Most Swing object methods must be run here because they are not thread safe. Some Swing components are thread safe (per API) and can be invoked from any thread. Other tasks can be scheduled to run in the event thread by application code using invokeLater or invokeAndWait. Tasks on the event dispatch must finish quickly, or they will cause GUI freeze.

## Worker Threads and SwingWorker

If the Swing program needs to execute a long-running task, it uses background worker threads to run. These are represented by the abstract SwingWorker class. The class implements the Future interface, so it can provide return value, cancel, discover whether finished or canceled. Important methods in this class are:

- done(): automatically invoked on event dispatch thread when finished  
- publish(): provides intermediate results \- causes SwingWorker.process to be invoked from event dispatch thread

**Swing’s Threading Policy:**  
In general Swing is not thread safe. All Swing components and related classes, unless otherwise documented, must be accessed on the event dispatching thread.

Typical Swing applications do processing in response to an event generated from a user gesture. For example, clicking on a `JButton` notifies all `ActionListeners` added to the `JButton`. As all events generated from a user gesture are dispatched on the event dispatching thread, most developers are not impacted by the restriction.

Where the impact lies, however, is in constructing and showing a Swing application. Calls to an application's `main` method, or methods in `Applet`, are not invoked on the event dispatching thread. As such, care must be taken to transfer control to the event dispatching thread when constructing and showing an application or applet. The preferred way to transfer control and begin working with Swing is to use `invokeLater`. The `invokeLater` method schedules a `Runnable` to be processed on the event dispatching thread.  
This restriction also applies to models attached to Swing components. For example, if a `TableModel` is attached to a `JTable`, the `TableModel` should only be modified on the event dispatching thread. If you modify the model on a separate thread you run the risk of exceptions and possible display corruption.

Although it is generally safe to make updates to the UI immediately, when executing on the event dispatch thread, there is an exception : if a model listener tries to further change the UI before the UI has been updated to reflect a pending change then the UI may render incorrectly. This can happen if an application installed listener needs to update the UI in response to an event which will cause a change in the model structure. It is important to first allow component installed listeners to process this change, since there is no guarantee of the order in which listeners may be called. The solution is for the application listener to make the change using [`SwingUtilities.invokeLater(Runnable)`](https://docs.oracle.com/en/java/javase/22/docs/api/java.desktop/javax/swing/SwingUtilities.html#invokeLater\(java.lang.Runnable\)) so that any changes to UI rendering will be done post processing all the model listeners installed by the component.

As all events are delivered on the event dispatching thread, care must be taken in event processing. In particular, a long running task, such as network io or computational intensive processing, executed on the event dispatching thread blocks the event dispatching thread from dispatching any other events. While the event dispatching thread is blocked the application is completely unresponsive to user input. Refer to [`SwingWorker`](https://docs.oracle.com/en/java/javase/22/docs/api/java.desktop/javax/swing/SwingWorker.html) for the preferred way to do such processing when working with Swing.

**NOTE: Serialized objects of any Swing class will not be compatible with future Swing releases. The current serialization support is appropriate for short term storage or RMI between applications running the same version of Swing. As of 1.4, support for long term storage of all JavaBeans added to java.beans package**

# Custom Swing in DrJava

The method invokeLater(Runnable r) takes a Runnable (the class used to represent the *command* design pattern in the Java libraries) and places that action at the end of the Java event queue. The action is *asynchronous*: the method returns as soon as the specified action has been queued. The action is executed sometime in the indefinite future by the event thread after all previously queued actions have been performed. Hence, if some code following a call on invokeLater depends on the execution of the requested action, we have a serious synchronization problem. How do we know when the requested action has been executed?

The method invokeAndWait(Runnable r) is intended to address this problem. It takes a Runnable, places that action at the end of the Java event queue, and waits for the event thread to execute the action. But invokeAndWait has an annoying feature: it throws an exception when it is called from within the event thread. In a complex application like DrJava, the same method can be called from the event thread and other threads. In general, it is very difficult to determine which threads call which methods.

In DrJava, we have worked around the limitations and inconveniences of the Java invokeLater/invokeAndWait methods in the Java libraries by defining our own versions of these methods in the class edu.rice.cs.util.swing.Utilities. Our versions first test to see if the executing thread is the event thread. If the answer is affirmative, then our methods immediately execute the run() method passed in the Runnable argument and return. Otherwise, our invokeXXX methods call the corresponding method in java.awt.EventQueue. Note that our versions of the invokeXXX methods are very efficient if they happen to be called in the event thread because no context switching is required to perform the requested action.

In some situations, the distinction between the DrJava versions and the Sun versions of these primitives is critically important. In particular, it may be essential to run some code in the event thread *after* all of the pending events in the event queue have been processed. In these situations, DrJava must use the Sun versions of these methods. On the other hand, if a call on invokeAndWait can be executed in the event thread as well as other threads, DrJava must use our version of invokeAndWait. Similarly, if DrJava must run some Swing code asynchronously in an arbitrary thread after all of the events already in the event queue have been processed (for example, the execution of some notified listeners that run Swing code in the event thread using invokeLater) then DrJava must use the Sun version of invokeLater. In the absence of this timing constraint, the DrJava version of invokeLater is preferable to the Sun version because it performs the requested action more quickly if the event thread is executed.

The DrJava Utilities class also includes the method void clearEventQueue() which, when executed in a thread other than the event thread, forces all of the events currently in the event queue to be processed before proceeding. If it is executed in the event thread, it immediately returns because the event thread cannot wait on the completion of processing a pending event\!

Note that some of this custom Swing behavior places some limitations on how tasks can be interleaved and should be improved in the future. See DrJavaConcurrency.md for more details.

# Sources and Further Reading

java.util.concurrency: [https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html\#MemoryVisibility](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html#MemoryVisibility)  
Futures: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html  
Concurrency: [https://docs.oracle.com/javase/tutorial/essential/concurrency/index.html](https://docs.oracle.com/javase/tutorial/essential/concurrency/index.html)  
Swing: [https://docs.oracle.com/javase/tutorial/uiswing/concurrency/index.html](https://docs.oracle.com/javase/tutorial/uiswing/concurrency/index.html)  
Java 9 Concurrency Textbook: [https://github.com/PacktPublishing/Mastering-Concurrency-Programming-with-Java-9-Second-Edition](https://github.com/PacktPublishing/Mastering-Concurrency-Programming-with-Java-9-Second-Edition)  
Java 22 Swing Documentation: https://docs.oracle.com/en/java/javase/22/docs/api/java.desktop/javax/swing/package-summary.html