JSR-14 v1.2 Specific Classes ----

The src/src-jsr14v1_2/ directory contains all source files which can only 
be compiled using the JSR-14 version 1.2 compiler (and not the JSR-14 
version 1.0 compiler).  We need to maintain support for the v1.0 compiler 
for developers on platforms with Java 1.3 (eg. Mac OS X), so any classes 
which require version 1.2 should be placed in this directory.  The 
corresponding class files are included in the compilers-jsr14v1_2.jar file 
in the src/edu/rice/cs/lib/ directory, and do not need to be recompiled 
when building new versions of DrJava.

Specifically, this directory includes the compiler adapters that DrJava
uses to invoke the Java 1.4.1 and JSR-14 v1.2 compilers.  Since we call 
methods on these compilers programmatically, we have to compile the 
adapter classes using Sun's new Javac source tree, which each of the 
aforementioned compilers is built upon.

To compile:
 - Copy the JSR-14 v1.2 version of javac.jar to "jsr14.jar" in the
   src/edu/rice/cs/lib/ directory.
 - Run "ant compile-jsr14v1_2".  This will create the
   "compilers-jsr14v1_2.jar" file in the lib directory.
