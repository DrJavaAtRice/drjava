Contents:
*.jar: Binaries that should be part of the distribution
buildlib/*.jar: Binaries used in the build process, but that should not be part
                of the distribution.

VERSIONS:

bcel-5.1-pruned.jar: BCEL 5.1 with the following unneeded class files removed (to reduce size):
                     - org/apache/bcel/ExceptionConstants*.class (2 files)
                     - org/apache/bcel/classfile/*Visitor.class (2 files)
                     - org/apache/bcel/generic/* except for the files listed below (217 files)
                       - ArrayType.class
                       - BasicType.class
                       - ClassGenException.class
                       - ObjectType.class
                       - ReferenceType.class
                       - Type.class and its inner classes
                     - org/apache/bcel/util/* except for the files listed below (14 files)
                       - ByteSequence*.class
                       - ClassPath*.class
                       - ClassQueue.class
                       - ClassVector.class
                       - *Repository.class
                     - org/apache/bcel/verifier/* (65 files)
                     The list of required files was found by compiling *without* the jar file,
                     placing the BCEL source in the sourcepath instead.  When only the
                     edu/rice/cs/javalanglevels/**/*.java files are compiled, the compiler will
                     automatically compile BCEL files when needed, and ignore them otherwise.

retroweaver-rt-1.2.3.jar: Retroweaver 1.2.3
plt.jar: plt-20070207-2208
buildlib/ant-contrib.jar: ANT Contrib 1.0b2
buildlib/astgen.jar: astgen-20060227-1521
buildlib/cenquatasks.jar: Distributed with Clover 1.3.9
buildlib/junit.jar: JUnit 3.8.1
buildlib/retroweaver-all-1.2.3.jar: Retroweaver 1.2.3


