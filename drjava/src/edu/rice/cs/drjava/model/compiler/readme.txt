@version $Id: readme.txt 2905 2005-03-04 22:49:13Z evlich $

About the compilers interface:

Several of the compiler adapter classes can only be compiled with a
particular version of JSR-14 (eg. v1.0 or v1.2).  These classes are kept
in a different source tree and are compiled separately into compilers.jar,
stored in the edu/rice/cs/lib/ directory, so that the rest of DrJava can
be compiled by any version of JSR-14.

Note:  it is strongly recommended at this time, April 2004, to use JSR14 v2.5
when compiling, as the modified version allows compilation into 1.3-compatible
class files.

The compilers.jar file in the lib directory *must* be on your classpath when
compiling, because classes in this directory extend classes defined in these
jar files.

The source for these classes is stored in the following directories:
src/src-jsr14v1_0/edu/rice/cs/drjava/model/compiler/
src/src-jsr14v1_2/edu/rice/cs/drjava/model/compiler/
src/src-jsr14v2_0/edu/rice/cs/drjava/model/compiler/

These classes do not normally need to be recompiled, but if you need to make
changes to them, put the appropriate version of JSR-14 on your classpath and
run either "ant compile-jsr14v1_0" or "ant compile-jsr14v1_2" or "ant
compile-jsr14v2_0" to compile and update the jars.
