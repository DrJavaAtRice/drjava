Platform-specific Classes

The sources here require certain platform-specific libraries in order to compile.  Because they cannot all be built at once, the class files must be stored under source control along with the sources.  The location of required platform libraries is communicated to the Ant script via environment variables.  The specific purposes and dependencies of each set of classes is as follows:

jdk14:  A compiler adapter for the javac compiler, version 1.4.  Must be compiled against the Sun compiler libraries, as distributed with the J2SE SDK version 1.4.3. (TODO: Are other versions, such as 1.4.1, sufficient?)

jdk15:  A compiler adapter for the javac compiler, version 5, and the prototype JSR-14 compiler, version 2.0+.  Must be compiled against the Sun compiler libraries, as distributed with the JDK version 5.0+ (or a compatible JSR-14 distribution).

jdk16:  A compiler adapter for the javac compiler, version 6+.  Must be compiled against the Sun compiler libraries, as distributed with the JDK version 6.0+.

mac:  A platform adapter for Mac OS X with Java version 1.4 or later.  Must be compiled against the com.apple.eawt classes, as distributed with Apple's Java implementation, version 1.4 or later.

windows:  A platform adapter for Windows.  Currently has no platform-specific dependencies, but is placed here in case the need arises.
