JSR-14 v2.0 Specific Classes ----

*Changes: This branch must now be compiled with JSR-14 v2.2 because we've 
introduced the variable args construct into these files.  Also, this
branch now supports JSR-14 v2.0, 2.2, 2.3, 2.4, 2.5 and J2SE 1.5.0 beta.
We combined them all here simply because it wasn't necessary to create
new branches for each one.

The src/src-jsr14v2_0/ directory contains all source files which can only be
compiled using the JSR-14 version 2.0 compiler.  We need to maintain support
for JSR-14 v1.0 for developers on platforms with Java 1.3 (eg.  Mac OS X), so
any classes which require version 2.0 should be placed in this directory.  
The corresponding class files are included in the compilers.jar file in the
src/edu/rice/cs/lib/ directory, and do not need to be recompiled when
building new versions of DrJava.

Specifically, this directory includes the compiler adapters that DrJava uses
to invoke the JSR-14 v2.0 compiler.  Since we call methods on this compiler
programmatically, we have to compile the adapter classes using Sun's new
Javac source tree, which the new compiler uses.

Important to note is that while we compile the rest of DrJava with the 
-novariance flag, the JSR14v20Compiler must be compiled using variance.  
Thus, it uses a different ant call than the other branches.

To compile:
 - Copy the JSR-14 v2.0 version of gjc-rt.jar to "jsr14.jar" in the
   src/edu/rice/cs/lib/ directory.
 - Run "ant compile-jsr14v2_0".  This will update the "compilers.jar" file in
   the lib directory.

