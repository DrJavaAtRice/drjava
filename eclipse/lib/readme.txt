These jar files are a minimal subset of the DrJava dependencies: only those
that are required to compile the Eclipse plug-in (and transitively referenced
code) are included.  Refactoring to eliminate some of these dependencies,
thus minimizing the plug-in's disk and (potentially) runtime footprint,
would be a welcome improvement.

Contents:
*.jar: Binaries that should be part of the distribution
buildlib/*.jar: Binaries used in the build process, but that should not be part
                of the distribution.

VERSIONS:

asm-3.1.jar:              ASM 3.1 (http://asm.objectweb.org)
dynamicjava-base-15.jar:  dynamicjava-20080709
javalanglevels.jar:       javalanglevels-20080709
junit.jar:                JUnit 4.4
platform.jar:             platform-20070213-2003
plt.jar:                  plt-20080709-1847

buildlib/ant-contrib.jar:           ANT Contrib 1.0b3
buildlib/cenquatasks.jar:           Distributed with Clover 1.3.9
buildlib/findbugs-ant.jar:          FindBugs 1.3.1
buildlib/junit.jar:                 JUnit 3.8.1
buildlib/plt-ant.jar:               plt-ant-20070212-2001
