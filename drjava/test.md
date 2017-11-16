# `ant test`

`commit 51cd9811b88027eff799a471198ae4b96dbd78be`

```
vu@Vu-Gazelle:~/github/drjava/drjava$ ant test
Buildfile: /home/vu/github/drjava/drjava/build.xml
     [echo] libs = concutest-junit-4.7-withrt-nodep.jar;docs.jar;dynamicjava-base.jar;ecj-4.5.1.jar;jacocoagent.jar;jacocoant.jar;javalanglevels-base.jar;javarepl-dev.build.jar;jgoodies-common-1.8.1.jar;jgoodies-forms-1.9.0.jar;jgoodies-looks-2.7.0.jar;jsoup-1.8.1.jar;org.jacoco.agent-0.7.10.201707180856.jar;org.jacoco.ant-0.7.10.201707180856.jar;org.jacoco.core-0.7.10.201707180856.jar;org.jacoco.core-0.7.3.201409180205.jar;org.jacoco.report-0.7.10.201707180856.jar;org.jacoco.report-0.7.3.201409180205.jar;platform.jar;plt.jar;seaglasslookandfeel-0.2.jar
     [echo] jrelibs = charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
     [echo] extlibs = cldrdata.jar;dnsns.jar;jaccess.jar;jfxrt.jar;localedata.jar;nashorn.jar;sunec.jar;sunjce_provider.jar;sunpkcs11.jar;zipfs.jar

resolve-development-value:

resolve-version-tag:

generate-source:
     [echo] Processing src/edu/rice/cs/drjava/CodeStatus.orig
     [copy] Copying 1 file to /home/vu/github/drjava/drjava/src/edu/rice/cs/drjava
     [echo] Processing src/edu/rice/cs/drjava/Version.orig
     [copy] Copying 1 file to /home/vu/github/drjava/drjava/src/edu/rice/cs/drjava

resolve-java8-runtime:
     [echo] java8-runtime = /usr/lib/jvm/java-8-oracle/jre/lib/rt.jar

resolve-java8-tools:

do-compile:
     [echo] Compiling src directory to classes/base and classes/test with command 'javac'
    [mkdir] Created dir: /home/vu/github/drjava/drjava/classes/base
    [mkdir] Created dir: /home/vu/github/drjava/drjava/classes/test
     [echo] jrelibs=charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
    [javac] Compiling 615 source files to /home/vu/github/drjava/drjava/classes/base
     [move] Moving 498 files to /home/vu/github/drjava/drjava/classes/test

copy-resources:
     [copy] Copying 58 files to /home/vu/github/drjava/drjava/classes/base

unjar-libs:
     [echo] libs = concutest-junit-4.7-withrt-nodep.jar;docs.jar;dynamicjava-base.jar;ecj-4.5.1.jar;jacocoagent.jar;jacocoant.jar;javalanglevels-base.jar;javarepl-dev.build.jar;jgoodies-common-1.8.1.jar;jgoodies-forms-1.9.0.jar;jgoodies-looks-2.7.0.jar;jsoup-1.8.1.jar;org.jacoco.agent-0.7.10.201707180856.jar;org.jacoco.ant-0.7.10.201707180856.jar;org.jacoco.core-0.7.10.201707180856.jar;org.jacoco.core-0.7.3.201409180205.jar;org.jacoco.report-0.7.10.201707180856.jar;org.jacoco.report-0.7.3.201409180205.jar;platform.jar;plt.jar;seaglasslookandfeel-0.2.jar
     [echo] jrelibs = charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
     [echo] extlibs = cldrdata.jar;dnsns.jar;jaccess.jar;jfxrt.jar;localedata.jar;nashorn.jar;sunec.jar;sunjce_provider.jar;sunpkcs11.jar;zipfs.jar

check-generate-dir-from-dir:

do-unjar-libs:
     [echo] Unjarring jar files in the lib directory
    [mkdir] Created dir: /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/concutest-junit-4.7-withrt-nodep.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/docs.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/dynamicjava-base.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/ecj-4.5.1.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/jacocoagent.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/jacocoant.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/javalanglevels-base.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/javarepl-dev.build.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/jgoodies-common-1.8.1.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/jgoodies-forms-1.9.0.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/jgoodies-looks-2.7.0.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/jsoup-1.8.1.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/org.jacoco.agent-0.7.10.201707180856.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/org.jacoco.ant-0.7.10.201707180856.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/org.jacoco.core-0.7.10.201707180856.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/org.jacoco.core-0.7.3.201409180205.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/org.jacoco.report-0.7.10.201707180856.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/org.jacoco.report-0.7.3.201409180205.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/platform.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/plt.jar into /home/vu/github/drjava/drjava/classes/lib
    [unjar] Expanding: /home/vu/github/drjava/drjava/lib/seaglasslookandfeel-0.2.jar into /home/vu/github/drjava/drjava/classes/lib

compile:

resolve-current-tools:

test:
     [echo] libs = concutest-junit-4.7-withrt-nodep.jar;docs.jar;dynamicjava-base.jar;ecj-4.5.1.jar;jacocoagent.jar;jacocoant.jar;javalanglevels-base.jar;javarepl-dev.build.jar;jgoodies-common-1.8.1.jar;jgoodies-forms-1.9.0.jar;jgoodies-looks-2.7.0.jar;jsoup-1.8.1.jar;org.jacoco.agent-0.7.10.201707180856.jar;org.jacoco.ant-0.7.10.201707180856.jar;org.jacoco.core-0.7.10.201707180856.jar;org.jacoco.core-0.7.3.201409180205.jar;org.jacoco.report-0.7.10.201707180856.jar;org.jacoco.report-0.7.3.201409180205.jar;platform.jar;plt.jar;seaglasslookandfeel-0.2.jar
     [echo] jrelibs = charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
     [echo] extlibs = cldrdata.jar;dnsns.jar;jaccess.jar;jfxrt.jar;localedata.jar;nashorn.jar;sunec.jar;sunjce_provider.jar;sunpkcs11.jar;zipfs.jar

resolve-test-formatter-class:

iterate-tests:
     [echo] Executing iterate-tests
     [echo] libs = concutest-junit-4.7-withrt-nodep.jar;docs.jar;dynamicjava-base.jar;ecj-4.5.1.jar;jacocoagent.jar;jacocoant.jar;javalanglevels-base.jar;javarepl-dev.build.jar;jgoodies-common-1.8.1.jar;jgoodies-forms-1.9.0.jar;jgoodies-looks-2.7.0.jar;jsoup-1.8.1.jar;org.jacoco.agent-0.7.10.201707180856.jar;org.jacoco.ant-0.7.10.201707180856.jar;org.jacoco.core-0.7.10.201707180856.jar;org.jacoco.core-0.7.3.201409180205.jar;org.jacoco.report-0.7.10.201707180856.jar;org.jacoco.report-0.7.3.201409180205.jar;platform.jar;plt.jar;seaglasslookandfeel-0.2.jar
     [echo] jrelibs = charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
     [echo] extlibs = cldrdata.jar;dnsns.jar;jaccess.jar;jfxrt.jar;localedata.jar;nashorn.jar;sunec.jar;sunjce_provider.jar;sunpkcs11.jar;zipfs.jar

resolve-jvm-args:

resolve-junit-jar:

do-test:
     [echo] Running all tests matching '*' with command 'java', using '/home/vu/github/drjava/drjava/lib/buildlib/junit.jar' and '/usr/lib/jvm/java-8-oracle/lib/tools.jar'
[jacoco:coverage] Enhancing junit with coverage
    [junit] CommandLineTest                              15.699 sec
    [junit] ConfigFileTest                               0.346 sec
    [junit] DependenciesTest                             0.315 sec
    [junit] BooleanOptionTest                            0.365 sec
    [junit] ColorOptionTest                              0.333 sec
    [junit] DrJavaPropertySetupTest                      0.407 sec
    [junit] FontOptionTest                               0.357 sec
    [junit] ForcedChoiceOptionTest                       0.373 sec
    [junit] IntegerOptionTest                            0.29 sec
    [junit] KeyStrokeOptionTest                          0.676 sec
    [junit] LongOptionTest                               0.378 sec
    [junit] NonNegativeIntegerOptionTest                 0.355 sec
    [junit] OptionMapLoaderTest                          0.534 sec
    [junit] RecursiveFileListPropertyTest                0.024 sec
    [junit] SavableConfigurationTest                     0.356 sec
    [junit] StringOptionTest                             0.255 sec
    [junit] VectorOptionTest                             0.289 sec
    [junit] AbstractDJDocumentTest                       0.253 sec
    [junit] ClassAndInterfaceFinderTest                  0.324 sec
    [junit] ClipboardHistoryModelTest                    0.006 sec
    [junit] DocumentRegionTest                           0.337 sec
    [junit] DummyGlobalModelTest                         0.278 sec
    [junit] DummyOpenDefDocTest                          0.315 sec
    [junit] EventNotifierTest                            0.434 sec
    [junit] FindReplaceMachineTest                       0.42 sec
    [junit] GlobalIndentTest                             9.934 sec
    [junit] GlobalModelCompileErrorsTest                 8.131 sec
    [junit] GlobalModelCompileIOTest                     8.377 sec
    [junit] GlobalModelCompileSuccessOptionsTest         4.377 sec
    [junit] GlobalModelCompileSuccessTest                4.858 sec
    [junit] GlobalModelCompileTest                       8.396 sec
    [junit] GlobalModelIOTest                            30.769 sec
    [junit] GlobalModelJUnitTest                         17.629 sec
    [junit] Testsuite: edu.rice.cs.drjava.model.GlobalModelJUnitTest
    [junit] Tests run: 14, Failures: 1, Errors: 13
    [junit] ------------- Standard Output ---------------
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu6146396280504883384, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu8675823041767716088, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5314102656630318753, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5801185981911830909, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5426479687876637240, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5426479687876637240, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.access$200(DefaultJUnitModel.java:85)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel$1$2.run(DefaultJUnitModel.java:281)
    [junit] 	at java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:311)
    [junit] 	at java.awt.EventQueue.dispatchEventImpl(EventQueue.java:756)
    [junit] 	at java.awt.EventQueue.access$500(EventQueue.java:97)
    [junit] 	at java.awt.EventQueue$3.run(EventQueue.java:709)
    [junit] 	at java.awt.EventQueue$3.run(EventQueue.java:703)
    [junit] 	at java.security.AccessController.doPrivileged(Native Method)
    [junit] 	at java.security.ProtectionDomain$JavaSecurityAccessImpl.doIntersectionPrivilege(ProtectionDomain.java:80)
    [junit] 	at java.awt.EventQueue.dispatchEvent(EventQueue.java:726)
    [junit] 	at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:201)
    [junit] 	at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:116)
    [junit] 	at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:105)
    [junit] 	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:101)
    [junit] 	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:93)
    [junit] 	at java.awt.EventDispatchThread.run(EventDispatchThread.java:82)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit] 	... 16 more
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu4656113627208308277, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu6806694749173887417, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu643390457704609601, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu3597293966725309192, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu4197934678698336358, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.access$200(DefaultJUnitModel.java:85)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel$1$2.run(DefaultJUnitModel.java:281)
    [junit] 	at java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:311)
    [junit] 	at java.awt.EventQueue.dispatchEventImpl(EventQueue.java:756)
    [junit] 	at java.awt.EventQueue.access$500(EventQueue.java:97)
    [junit] 	at java.awt.EventQueue$3.run(EventQueue.java:709)
    [junit] 	at java.awt.EventQueue$3.run(EventQueue.java:703)
    [junit] 	at java.security.AccessController.doPrivileged(Native Method)
    [junit] 	at java.security.ProtectionDomain$JavaSecurityAccessImpl.doIntersectionPrivilege(ProtectionDomain.java:80)
    [junit] 	at java.awt.EventQueue.dispatchEvent(EventQueue.java:726)
    [junit] 	at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:201)
    [junit] 	at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:116)
    [junit] 	at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:105)
    [junit] 	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:101)
    [junit] 	at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:93)
    [junit] 	at java.awt.EventDispatchThread.run(EventDispatchThread.java:82)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit] 	... 16 more
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu8409018497781667492, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5335116754316078350, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu557434904922765212, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu1009588379428084543, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] ------------- ---------------- ---------------
    [junit] ------------- Standard Error -----------------
    [junit] ********** Starting JUnit on Elspeth.java
    [junit] ********** Starting JUnit on MonkeyTestError.java
    [junit] ********** Starting JUnit on NonTestCase.java
    [junit] ********** Starting JUnit on MonkeyTestPass.java
    [junit] ********** Starting JUnit on JUnit4StyleTest.java
    [junit] ********** Starting JUnit on JUnit4MultiTest.java
    [junit] ********** Starting JUnit on JUnit4NoTest.java
    [junit] ********** Starting JUnit on JUnit4TwoMethod1Test.java
    [junit] ********** Starting JUnit on MonkeyTestPass.java
    [junit] ********** Starting JUnit on MonkeyTestPass.java
    [junit] ********** Starting JUnit on NonPublic.java
    [junit] ------------- ---------------- ---------------
    [junit] Testcase: testElspethOneJUnitError_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:298)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:230)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testElspethOneJUnitError_NOJOIN(GlobalModelJUnitTest.java:256)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Testcase: testRealError_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:298)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:230)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testRealError_NOJOIN(GlobalModelJUnitTest.java:282)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Testcase: testNonTestCaseError_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:298)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:230)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testNonTestCaseError_NOJOIN(GlobalModelJUnitTest.java:310)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Testcase: testInfiniteLoop_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	FAILED
    [junit] Aborting unit testing runs recovery code in testing thread; no exception is thrown
    [junit] junit.framework.AssertionFailedError: Aborting unit testing runs recovery code in testing thread; no exception is thrown
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testInfiniteLoop_NOJOIN(GlobalModelJUnitTest.java:465)
    [junit]
    [junit]
    [junit] Testcase: testUnsavedAndUnCompiledChanges(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] null
    [junit] java.lang.InterruptedException
    [junit] 	at java.lang.Object.wait(Native Method)
    [junit] 	at java.lang.Object.wait(Object.java:502)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.waitJUnitDone(GlobalModelTestCase.java:1241)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1230)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testUnsavedAndUnCompiledChanges(GlobalModelJUnitTest.java:517)
    [junit]
    [junit]
    [junit] Testcase: testJUnit4StyleTestWorks_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:298)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:230)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testJUnit4StyleTestWorks_NOJOIN(GlobalModelJUnitTest.java:826)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Testcase: testJUnit4MultiTest_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:298)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:230)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testJUnit4MultiTest_NOJOIN(GlobalModelJUnitTest.java:860)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Testcase: testJUnit4NoTest_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:298)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:230)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testJUnit4NoTest_NOJOIN(GlobalModelJUnitTest.java:894)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Testcase: testJUnit4TwoMethod1Test_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:298)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:230)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testJUnit4TwoMethod1Test_NOJOIN(GlobalModelJUnitTest.java:929)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Testcase: testNoClassFile(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] null
    [junit] java.lang.InterruptedException
    [junit] 	at java.lang.Object.wait(Native Method)
    [junit] 	at java.lang.Object.wait(Object.java:502)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.waitJUnitDone(GlobalModelTestCase.java:1241)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1230)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testNoClassFile(GlobalModelJUnitTest.java:405)
    [junit]
    [junit]
    [junit] Testcase: testCorrectFilesAfterIncorrectChanges_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:298)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitDocs(DefaultJUnitModel.java:211)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitAll(DefaultJUnitModel.java:181)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1236)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testCorrectFilesAfterIncorrectChanges_NOJOIN(GlobalModelJUnitTest.java:782)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Testcase: testOneJUnitError_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:298)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitDocs(DefaultJUnitModel.java:211)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitAll(DefaultJUnitModel.java:181)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1236)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testOneJUnitError_NOJOIN(GlobalModelJUnitTest.java:231)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Testcase: testNoJUnitErrors_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:298)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:230)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testNoJUnitErrors_NOJOIN(GlobalModelJUnitTest.java:200)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Testcase: testResultOfNonPublicTestCase_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:298)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:230)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testResultOfNonPublicTestCase_NOJOIN(GlobalModelJUnitTest.java:347)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Test edu.rice.cs.drjava.model.GlobalModelJUnitTest FAILED
    [junit] GlobalModelOtherTest                         160.294 sec
    [junit] Testsuite: edu.rice.cs.drjava.model.GlobalModelOtherTest
    [junit] Tests run: 16, Failures: 8, Errors: 0
    [junit] ------------- Standard Output ---------------
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5265750846190219998, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu8522349027646795929, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu1846340106279082691, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu203011771160975240, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu3941263249714017111, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu2409835503592029321/dir1, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] ------------- ---------------- ---------------
    [junit] ------------- Standard Error -----------------
    [junit] undoableEditHappened(javax.swing.event.UndoableEditEvent[source=ddoc for (Untitled)]) called
    [junit] undoableEditHappened call propagated to listener
    [junit] ------------- ---------------- ---------------
    [junit] Testcase: testInteractionsDefineAnonymousInnerClass(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase.doCompile(GlobalModelTestCase.java:337)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testInteractionsDefineAnonymousInnerClass(GlobalModelOtherTest.java:269)
    [junit]
    [junit]
    [junit] Testcase: testInteractionsLiveUpdateClassPath(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase.doCompile(GlobalModelTestCase.java:337)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testInteractionsLiveUpdateClassPath(GlobalModelOtherTest.java:498)
    [junit]
    [junit]
    [junit] Testcase: testSwitchInterpreters(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] number of times interpreterChanged fired expected:<1> but was:<0>
    [junit] junit.framework.AssertionFailedError: number of times interpreterChanged fired expected:<1> but was:<0>
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$TestListener.assertInterpreterChangedCount(GlobalModelTestCase.java:792)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testSwitchInterpreters(GlobalModelOtherTest.java:556)
    [junit]
    [junit]
    [junit] Testcase: testRunMainMethod(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase.doCompile(GlobalModelTestCase.java:337)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase.doCompile(GlobalModelTestCase.java:300)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testRunMainMethod(GlobalModelOtherTest.java:569)
    [junit]
    [junit]
    [junit] Testcase: testInteractionsVariableWithLowercaseClassName(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase.doCompile(GlobalModelTestCase.java:337)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testInteractionsVariableWithLowercaseClassName(GlobalModelOtherTest.java:218)
    [junit]
    [junit]
    [junit] Testcase: testInteractionsCanSeeChangedClass(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase.doCompile(GlobalModelTestCase.java:337)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testInteractionsCanSeeChangedClass(GlobalModelOtherTest.java:245)
    [junit]
    [junit]
    [junit] Testcase: testInteractionsCanSeeCompiledClasses(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase.doCompile(GlobalModelTestCase.java:337)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testInteractionsCanSeeCompiledClasses(GlobalModelOtherTest.java:170)
    [junit]
    [junit]
    [junit] Testcase: testExitInteractions(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testExitInteractions(GlobalModelOtherTest.java:137)
    [junit]
    [junit]
    [junit] Test edu.rice.cs.drjava.model.GlobalModelOtherTest FAILED
    [junit] MultiThreadedTestCaseTest                    0.37 sec
    [junit] SingleDisplayModelTest                       9.624 sec
    [junit] Testsuite: edu.rice.cs.drjava.model.SingleDisplayModelTest
    [junit] Tests run: 7, Failures: 1, Errors: 0
    [junit] Testcase: testCloseFiles(edu.rice.cs.drjava.model.SingleDisplayModelTest):	FAILED
    [junit] number of times interpreterReady fired expected:<1> but was:<0>
    [junit] junit.framework.AssertionFailedError: number of times interpreterReady fired expected:<1> but was:<0>
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$TestListener.assertInterpreterReadyCount(GlobalModelTestCase.java:816)
    [junit] 	at edu.rice.cs.drjava.model.SingleDisplayModelTest.testCloseFiles(SingleDisplayModelTest.java:302)
    [junit]
    [junit]
    [junit] Test edu.rice.cs.drjava.model.SingleDisplayModelTest FAILED
    [junit] TestDocGetterTest                            0.372 sec
    [junit] DocumentCacheTest                            8.364 sec
    [junit] CompilerErrorModelTest                       0.631 sec
    [junit] DebugWatchDataTest                           0.403 sec
    [junit] CommentTest                                  0.444 sec
    [junit] DefinitionsDocumentTest                      1.654 sec
    [junit] IndentHelperTest                             0.582 sec
    [junit] IndentTest                                   0.823 sec
    [junit] ActionBracePlusTest                          0.51 sec
    [junit] ActionDoNothingTest                          0.464 sec
    [junit] ActionStartPrevLinePlusMultilinePreserveTest 0.503 sec
    [junit] ActionStartPrevLinePlusTest                  0.439 sec
    [junit] ActionStartPrevStmtPlusTest                  0.48 sec
    [junit] ActionStartStmtOfBracePlusTest               0.458 sec
    [junit] IndentRuleWithTraceTest                      0.498 sec
    [junit] QuestionBraceIsCurlyTest                     0.557 sec
    [junit] QuestionBraceIsParenOrBracketTest            0.467 sec
    [junit] QuestionCurrLineEmptyOrEnterPressTest        0.554 sec
    [junit] QuestionCurrLineIsWingCommentTest            0.366 sec
    [junit] QuestionCurrLineStartsWithSkipCommentsTest   0.408 sec
    [junit] QuestionCurrLineStartsWithTest               0.45 sec
    [junit] QuestionExistsCharInStmtTest                 0.465 sec
    [junit] QuestionHasCharPrecedingOpenBraceTest        0.493 sec
    [junit] QuestionInsideCommentTest                    0.573 sec
    [junit] QuestionLineContainsTest                     0.526 sec
    [junit] QuestionNewParenPhraseTest                   0.521 sec
    [junit] QuestionPrevLineStartsCommentTest            0.391 sec
    [junit] QuestionPrevLineStartsWithTest               0.427 sec
    [junit] QuestionStartAfterOpenBraceTest              0.414 sec
    [junit] QuestionStartingNewStmtTest                  0.466 sec
    [junit] BackSlashTest                                0.465 sec
    [junit] BraceInfoTest                                0.445 sec
    [junit] BraceTest                                    0.432 sec
    [junit] GapTest                                      0.41 sec
    [junit] MixedQuoteTest                               0.385 sec
    [junit] ModelListTest                                0.346 sec
    [junit] ReducedModelDeleteTest                       0.386 sec
    [junit] ReducedModelTest                             0.46 sec
    [junit] SingleQuoteTest                              0.485 sec
    [junit] JavadocModelTest                             0.622 sec
    [junit] JUnitErrorModelTest                          5.129 sec
    [junit] Testsuite: edu.rice.cs.drjava.model.junit.JUnitErrorModelTest
    [junit] Tests run: 3, Failures: 0, Errors: 3
    [junit] ------------- Standard Output ---------------
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu1554886394418237032, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu1554886394418237032, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu6201716654656148930, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu58426381494131300, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent3649252638076807421.jar]';  bootClassPath = 'null'
    [junit] ------------- ---------------- ---------------
    [junit] ------------- Standard Error -----------------
    [junit] ********** Starting JUnit on ABCTest.java
    [junit] ********** Starting JUnit on MonkeyTestFail.java
    [junit] ********** Starting JUnit on TestOne.java
    [junit] ------------- ---------------- ---------------
    [junit] Testcase: testVerifyErrorHandledCorrectly_NOJOIN(edu.rice.cs.drjava.model.junit.JUnitErrorModelTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:298)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:230)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.junit.JUnitErrorModelTest.testVerifyErrorHandledCorrectly_NOJOIN(JUnitErrorModelTest.java:295)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Testcase: testErrorsArrayInOrder_NOJOIN(edu.rice.cs.drjava.model.junit.JUnitErrorModelTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:298)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:230)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.junit.JUnitErrorModelTest.testErrorsArrayInOrder_NOJOIN(JUnitErrorModelTest.java:190)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Testcase: testErrorInSuperClass_NOJOIN(edu.rice.cs.drjava.model.junit.JUnitErrorModelTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:298)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:230)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.junit.JUnitErrorModelTest.testErrorInSuperClass_NOJOIN(JUnitErrorModelTest.java:387)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Test edu.rice.cs.drjava.model.junit.JUnitErrorModelTest FAILED
    [junit] DrJavaBookTest                               0.442 sec
    [junit] HistoryTest                                  0.554 sec
```

The next `[junit]` test stalled in 90 minutes.
