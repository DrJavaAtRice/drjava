Mac OS X JDK 1.3.* Specific Classes ----

The Java source files under this directory must be compiled using version
1.3 of the JDK on Mac OS X.  To facilitate this, an ant target has been 
created, called "compile-mac13".  This target will compile the classes 
and place them into a jar file in the lib directory for distribution.  
No tests will be included in the jar file.  Like all of the platform 
support classes, these will fail to compile on any platform other than the 
appropriate one.
