Windows Specific Classes ----

The Java source files under this directory must be compiled using a JDK on
Windows.  To facilitate this, an ant target has been created, called 
"compile-windows".  This target will compile the classes and place them 
into a jar file in the lib directory for distribution.  No tests will be 
included in the jar file.  Like all of the platform support classes, 
these will fail to compile on any platform other than the appropriate one.
