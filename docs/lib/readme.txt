Contents:
*.jar: Binaries that should be part of the distribution
buildlib/*.jar: Binaries used in the build process, but that should not be part
                of the distribution.

VERSIONS:

buildlib/*.jar:  All jars bundled with FOP 0.95.  The following came with FOP,
                 but were excluded because they don't seem to be necessary:
                 serializer-2.7.0.jar; xalan-2.7.0.jar; xercesImpl-2.7.1.jar;
                 xml-apis-1.3.04.jar; xml-apis-ext-1.3.04.jar.  (Xalan would be
                 useful for platform-independent HTML generation, but I couldn't
                 get it to work correctly.  It failed to produce a table of
                 contents in the right places.)