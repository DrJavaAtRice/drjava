Note:

Several of the compiler adapter classes can only be compiled with a 
particular version of JSR-14 (eg. v1.0 or v1.2).  These classes are kept 
in a different source tree and are compiled separately into jar files 
stored in the edu/rice/cs/lib/ directory, so that the rest of DrJava can 
be compiled by any version of JSR-14.

The compilers-jsr14v1_0.jar and compilers-jsr14v1_2.jar files in the lib 
directory *must* be on your classpath when compiling, because classes in 
this directory extend classes defined in these jar files.

The source for these classes is stored in the following directorys:
src/src-jsr14v1_0/edu/rice/cs/drjava/model/compiler/
src/src-jsr14v1_2/edu/rice/cs/drjava/model/compiler/

These classes do not normally need to be recompiled, but if you need to 
make changes to them, put the appropriate version of JSR-14 on your 
classpath and run either "ant compile-jsr14v1_0" or "ant 
compile-jsr14v1_2" to compile and update the jars.

