A few classes to test with class loaders.  We follow the following scheme:

intbox/pkg/IntBox.java: An interface shared by all classes
a/pkg/A.java: A class that references IntBox
b/bpkg/B.java: A class that references IntBox
c/pkg/C.java: A class that references A and IntBox
d/D.java: A class that references A, B, C, and IntBox

Each class's "get" method should return a number corresponding to its position in the
alphabet.

For compatibility with running under 1.4 JVMs, tests should be compiled with the
options "-source 1.4 -target 1.4".

Example compile command:
javac -source 1.4 -target 1.4 -cp a:b:c:d:intbox d/d.java

