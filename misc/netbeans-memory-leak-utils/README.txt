See
http://blogs.sun.com/tor/entry/leak_unit_tests
for more information.

You want to include assertgc.jar and org-netbeans-insane.jar on your test classpath,
then call assertGc() and assertSize() as described in the above blog entry,
or here: http://performance.netbeans.org/insane/index.html

