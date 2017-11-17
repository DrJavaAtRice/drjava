# `ant test`

`commit a76711319e5017ce5381a5ab0b39e48351f77faa`

```
vu@Vu-Gazelle:~/github/drjava/drjava$ date
Fri Nov 17 11:34:11 CST 2017
vu@Vu-Gazelle:~/github/drjava/drjava$ ant test
Buildfile: /home/vu/github/drjava/drjava/build.xml
     [echo] libs = concutest-junit-4.7-withrt-nodep.jar;docs.jar;dynamicjava-base.jar;ecj-4.5.1.jar;jacocoagent.jar;jacocoant.jar;javalanglevels-base.jar;javarepl-dev.build.jar;jgoodies-common-1.8.1.jar;jgoodies-forms-1.9.0.jar;jgoodies-looks-2.7.0.jar;jsoup-1.8.1.jar;org.jacoco.agent-0.7.10.201707180856.jar;org.jacoco.ant-0.7.10.201707180856.jar;org.jacoco.core-0.7.10.201707180856.jar;org.jacoco.core-0.7.3.201409180205.jar;org.jacoco.report-0.7.10.201707180856.jar;org.jacoco.report-0.7.3.201409180205.jar;platform.jar;plt.jar;seaglasslookandfeel-0.2.jar
     [echo] jrelibs = charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
     [echo] extlibs = cldrdata.jar;dnsns.jar;jaccess.jar;jfxrt.jar;localedata.jar;nashorn.jar;sunec.jar;sunjce_provider.jar;sunpkcs11.jar;zipfs.jar

resolve-development-value:

resolve-version-tag:

generate-source:
     [echo] Processing src/edu/rice/cs/drjava/CodeStatus.orig
     [echo] Processing src/edu/rice/cs/drjava/Version.orig

resolve-java8-runtime:
     [echo] java8-runtime = /usr/lib/jvm/java-8-oracle/jre/lib/rt.jar

resolve-java8-tools:

do-compile:
     [echo] Compiling src directory to classes/base and classes/test with command 'javac'
     [move] Moving 498 files to /home/vu/github/drjava/drjava/classes/base
     [echo] jrelibs=charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
    [javac] Compiling 3 source files to /home/vu/github/drjava/drjava/classes/base
     [move] Moving 498 files to /home/vu/github/drjava/drjava/classes/test

copy-resources:

unjar-libs:
     [echo] libs = concutest-junit-4.7-withrt-nodep.jar;docs.jar;dynamicjava-base.jar;ecj-4.5.1.jar;jacocoagent.jar;jacocoant.jar;javalanglevels-base.jar;javarepl-dev.build.jar;jgoodies-common-1.8.1.jar;jgoodies-forms-1.9.0.jar;jgoodies-looks-2.7.0.jar;jsoup-1.8.1.jar;org.jacoco.agent-0.7.10.201707180856.jar;org.jacoco.ant-0.7.10.201707180856.jar;org.jacoco.core-0.7.10.201707180856.jar;org.jacoco.core-0.7.3.201409180205.jar;org.jacoco.report-0.7.10.201707180856.jar;org.jacoco.report-0.7.3.201409180205.jar;platform.jar;plt.jar;seaglasslookandfeel-0.2.jar
     [echo] jrelibs = charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
     [echo] extlibs = cldrdata.jar;dnsns.jar;jaccess.jar;jfxrt.jar;localedata.jar;nashorn.jar;sunec.jar;sunjce_provider.jar;sunpkcs11.jar;zipfs.jar

check-generate-dir-from-dir:

do-unjar-libs:

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
    [junit] CommandLineTest                              13.18 sec
    [junit] Testsuite: edu.rice.cs.drjava.CommandLineTest
    [junit] Tests run: 8, Failures: 1, Errors: 0
    [junit] Testcase: testRelativePath(edu.rice.cs.drjava.CommandLineTest):	FAILED
    [junit] directory created OK
    [junit] junit.framework.AssertionFailedError: directory created OK
    [junit] 	at edu.rice.cs.drjava.CommandLineTest.mkTempDir(CommandLineTest.java:400)
    [junit] 	at edu.rice.cs.drjava.CommandLineTest.testRelativePath(CommandLineTest.java:356)
    [junit]
    [junit]
    [junit] Test edu.rice.cs.drjava.CommandLineTest FAILED
    [junit] ConfigFileTest                               0.316 sec
    [junit] DependenciesTest                             0.426 sec
    [junit] BooleanOptionTest                            0.341 sec
    [junit] ColorOptionTest                              0.425 sec
    [junit] DrJavaPropertySetupTest                      0.581 sec
    [junit] FontOptionTest                               0.283 sec
    [junit] ForcedChoiceOptionTest                       0.316 sec
    [junit] IntegerOptionTest                            0.364 sec
    [junit] KeyStrokeOptionTest                          0.512 sec
    [junit] LongOptionTest                               0.294 sec
    [junit] NonNegativeIntegerOptionTest                 0.284 sec
    [junit] OptionMapLoaderTest                          0.265 sec
    [junit] RecursiveFileListPropertyTest                0.019 sec
    [junit] SavableConfigurationTest                     0.264 sec
    [junit] StringOptionTest                             0.293 sec
    [junit] VectorOptionTest                             0.313 sec
    [junit] AbstractDJDocumentTest                       0.32 sec
    [junit] ClassAndInterfaceFinderTest                  0.301 sec
    [junit] ClipboardHistoryModelTest                    0.007 sec
    [junit] DocumentRegionTest                           0.23 sec
    [junit] DummyGlobalModelTest                         0.291 sec
    [junit] DummyOpenDefDocTest                          0.269 sec
    [junit] EventNotifierTest                            0.309 sec
    [junit] FindReplaceMachineTest                       0.385 sec
    [junit] GlobalIndentTest                             9.467 sec
    [junit] GlobalModelCompileErrorsTest                 7.531 sec
    [junit] GlobalModelCompileIOTest                     8.075 sec
    [junit] GlobalModelCompileSuccessOptionsTest         3.994 sec
    [junit] GlobalModelCompileSuccessTest                4.321 sec
    [junit] GlobalModelCompileTest                       8.111 sec
    [junit] GlobalModelIOTest                            31.803 sec
    [junit] GlobalModelJUnitTest                         17.116 sec
    [junit] Testsuite: edu.rice.cs.drjava.model.GlobalModelJUnitTest
    [junit] Tests run: 14, Failures: 1, Errors: 13
    [junit] ------------- Standard Output ---------------
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu8098523228617935860, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu1572662322165489500, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu8597499208378805970, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu6454564990463780437, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu6454564990463780437, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
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
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu6348455070479047343, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu3328990096390778949, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu8714047287802879199, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu6741892798188665482, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu7433045219176434795, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
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
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu8271560545126718634, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu362714273778123291, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu6736019150151620113, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu571423225449840886, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5968659425685073729, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] ------------- ---------------- ---------------
    [junit] ------------- Standard Error -----------------
    [junit] ********** Starting JUnit on NonTestCase.java
    [junit] ********** Starting JUnit on MonkeyTestError.java
    [junit] ********** Starting JUnit on NonPublic.java
    [junit] ********** Starting JUnit on MonkeyTestPass.java
    [junit] ********** Starting JUnit on JUnit4StyleTest.java
    [junit] ********** Starting JUnit on JUnit4MultiTest.java
    [junit] ********** Starting JUnit on JUnit4NoTest.java
    [junit] ********** Starting JUnit on JUnit4TwoMethod1Test.java
    [junit] ********** Starting JUnit on MonkeyTestPass.java
    [junit] ********** Starting JUnit on MonkeyTestPass.java
    [junit] ********** Starting JUnit on Elspeth.java
    [junit] ------------- ---------------- ---------------
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
    [junit] Testcase: testInfiniteLoop_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	FAILED
    [junit] Aborting unit testing runs recovery code in testing thread; no exception is thrown
    [junit] junit.framework.AssertionFailedError: Aborting unit testing runs recovery code in testing thread; no exception is thrown
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testInfiniteLoop_NOJOIN(GlobalModelJUnitTest.java:465)
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
    [junit] Test edu.rice.cs.drjava.model.GlobalModelJUnitTest FAILED
    [junit] GlobalModelOtherTest                         158.972 sec
    [junit] Testsuite: edu.rice.cs.drjava.model.GlobalModelOtherTest
    [junit] Tests run: 16, Failures: 8, Errors: 0
    [junit] ------------- Standard Output ---------------
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu4109318605671441139/dir1, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu4339338176540326179, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu6883026058947758928, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5657140172280584943, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu8539906421722259693, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu6311889815304002112, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] ------------- ---------------- ---------------
    [junit] ------------- Standard Error -----------------
    [junit] undoableEditHappened(javax.swing.event.UndoableEditEvent[source=ddoc for (Untitled)]) called
    [junit] undoableEditHappened call propagated to listener
    [junit] ------------- ---------------- ---------------
    [junit] Testcase: testInteractionsCanSeeCompiledClasses(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase.doCompile(GlobalModelTestCase.java:337)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testInteractionsCanSeeCompiledClasses(GlobalModelOtherTest.java:170)
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
    [junit] Testcase: testExitInteractions(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testExitInteractions(GlobalModelOtherTest.java:137)
    [junit]
    [junit]
    [junit] Testcase: testSwitchInterpreters(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] number of times interpreterChanged fired expected:<1> but was:<0>
    [junit] junit.framework.AssertionFailedError: number of times interpreterChanged fired expected:<1> but was:<0>
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$TestListener.assertInterpreterChangedCount(GlobalModelTestCase.java:792)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testSwitchInterpreters(GlobalModelOtherTest.java:556)
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
    [junit] Testcase: testRunMainMethod(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase.doCompile(GlobalModelTestCase.java:337)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase.doCompile(GlobalModelTestCase.java:300)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testRunMainMethod(GlobalModelOtherTest.java:569)
    [junit]
    [junit]
    [junit] Testcase: testInteractionsDefineAnonymousInnerClass(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase.doCompile(GlobalModelTestCase.java:337)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testInteractionsDefineAnonymousInnerClass(GlobalModelOtherTest.java:269)
    [junit]
    [junit]
    [junit] Testcase: testInteractionsCanSeeChangedClass(edu.rice.cs.drjava.model.GlobalModelOtherTest):FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase.doCompile(GlobalModelTestCase.java:337)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testInteractionsCanSeeChangedClass(GlobalModelOtherTest.java:245)
    [junit]
    [junit]
    [junit] Test edu.rice.cs.drjava.model.GlobalModelOtherTest FAILED
    [junit] MultiThreadedTestCaseTest                    0.327 sec
    [junit] SingleDisplayModelTest                       8.982 sec
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
    [junit] TestDocGetterTest                            0.33 sec
    [junit] DocumentCacheTest                            7.855 sec
    [junit] CompilerErrorModelTest                       0.418 sec
    [junit] DebugWatchDataTest                           0.314 sec
    [junit] CommentTest                                  0.53 sec
    [junit] DefinitionsDocumentTest                      1.385 sec
    [junit] IndentHelperTest                             0.386 sec
    [junit] IndentTest                                   0.631 sec
    [junit] ActionBracePlusTest                          0.389 sec
    [junit] ActionDoNothingTest                          0.386 sec
    [junit] ActionStartPrevLinePlusMultilinePreserveTest 0.397 sec
    [junit] ActionStartPrevLinePlusTest                  0.444 sec
    [junit] ActionStartPrevStmtPlusTest                  0.444 sec
    [junit] ActionStartStmtOfBracePlusTest               0.512 sec
    [junit] IndentRuleWithTraceTest                      0.422 sec
    [junit] QuestionBraceIsCurlyTest                     0.347 sec
    [junit] QuestionBraceIsParenOrBracketTest            0.416 sec
    [junit] QuestionCurrLineEmptyOrEnterPressTest        0.395 sec
    [junit] QuestionCurrLineIsWingCommentTest            0.37 sec
    [junit] QuestionCurrLineStartsWithSkipCommentsTest   0.382 sec
    [junit] QuestionCurrLineStartsWithTest               0.45 sec
    [junit] QuestionExistsCharInStmtTest                 0.508 sec
    [junit] QuestionHasCharPrecedingOpenBraceTest        0.485 sec
    [junit] QuestionInsideCommentTest                    0.518 sec
    [junit] QuestionLineContainsTest                     0.375 sec
    [junit] QuestionNewParenPhraseTest                   0.436 sec
    [junit] QuestionPrevLineStartsCommentTest            0.372 sec
    [junit] QuestionPrevLineStartsWithTest               0.408 sec
    [junit] QuestionStartAfterOpenBraceTest              0.409 sec
    [junit] QuestionStartingNewStmtTest                  0.451 sec
    [junit] BackSlashTest                                0.375 sec
    [junit] BraceInfoTest                                0.462 sec
    [junit] BraceTest                                    0.406 sec
    [junit] GapTest                                      0.369 sec
    [junit] MixedQuoteTest                               0.299 sec
    [junit] ModelListTest                                0.292 sec
    [junit] ReducedModelDeleteTest                       0.358 sec
    [junit] ReducedModelTest                             0.399 sec
    [junit] SingleQuoteTest                              0.396 sec
    [junit] JavadocModelTest                             0.483 sec
    [junit] JUnitErrorModelTest                          4.732 sec
    [junit] Testsuite: edu.rice.cs.drjava.model.junit.JUnitErrorModelTest
    [junit] Tests run: 3, Failures: 0, Errors: 3
    [junit] ------------- Standard Output ---------------
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu8700858667809322590, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu1459267652992427269, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu2503514664938832528, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu2503514664938832528, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/junit.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5213806959495094794.jar]';  bootClassPath = 'null'
    [junit] ------------- ---------------- ---------------
    [junit] ------------- Standard Error -----------------
    [junit] ********** Starting JUnit on MonkeyTestFail.java
    [junit] ********** Starting JUnit on TestOne.java
    [junit] ********** Starting JUnit on ABCTest.java
    [junit] ------------- ---------------- ---------------
    [junit] Testcase: testErrorsArrayInOrder_NOJOIN(edu.rice.cs.drjava.model.junit.JUnitErrorModelTest):Caused an ERROR
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
    [junit] Testcase: testErrorInSuperClass_NOJOIN(edu.rice.cs.drjava.model.junit.JUnitErrorModelTest):Caused an ERROR
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
    [junit] Test edu.rice.cs.drjava.model.junit.JUnitErrorModelTest FAILED
    [junit] DrJavaBookTest                               0.366 sec
    [junit] HistoryTest                                  0.504 sec
^Z
[1]+  Stopped                 ant test
vu@Vu-Gazelle:~/github/drjava/drjava$ date
Fri Nov 17 15:52:52 CST 2017
vu@Vu-Gazelle:~/github/drjava/drjava$
```

The next `[junit]` test stalled.
