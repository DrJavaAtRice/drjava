Platform-specific Classes

The sources here require certain platform-specific libraries in order to compile.  Their purposes and dependencies are as follows:

src-jdk14:  A compiler adapter for the javac compiler, version 1.4.  Must be compiled against the Sun compiler libraries, as distributed with the J2SE SDK version 1.4.3. (TODO: Are other versions, such as 1.4.1, sufficient?)

src-jdk15:  A compiler adapter for the javac compiler, version 1.5+, and the prototype JSR-14 compiler, version 2.0+.  Must be compiled against the Sun compiler libraries, as distributed with the JDK version 1.5.0+ (or a compatible JSR-14 distribution).

src-mac:  A platform adapter for Mac OS X with Java version 1.4 or later.  Must be compiled against the com.apple.eawt classes, as distributed with Apple's Java implementation, version 1.4 or later.

src-windows:  A platform adapter for Windows.  Currently has no platform-specific dependencies, but is placed here in case the need arises.
