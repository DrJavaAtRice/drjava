JSR-14 v1.0 Specific Classes ----

The src/src-jsr14v1_0/ directory contains all source files which can only
be compiled using the JSR-14 version 1.0 compiler, which is no longer
distributed by Sun.  The corresponding class files are included in the
compilers-jsr14v1_0.jar file in the src/edu/rice/cs/lib/ directory, and do
not need to be recompiled when building new versions of DrJava.

Specifically, this directory includes the compiler adapters that DrJava
uses to invoke the Java 1.3, Java 1.4.0 and JSR-14 v1.0 compilers.  Since
we call methods on these compilers programmatically, we have to compile
the adapter classes using Sun's old Javac source tree, which each of the
aforementioned compilers is built upon.  The JSR-14 v1.2 and Java 1.4.1
(and later) compilers are built upon a different (incompatible) source 
tree, which forces us to make a distinction at compile time.

To compile:
 - Copy the JSR-14 v1.0 version of javac.jar to "jsr14.jar" in the
   src/edu/rice/cs/lib/ directory.
 - Run "ant compile-jsr14v1_0".  This will create the 
   "compilers-jsr14v1_0.jar" file in the lib directory.

