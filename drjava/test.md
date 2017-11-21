# `ant test`

<!----------------------------------------------------------------------------->

## Vu's system

```
vu@Vu-Gazelle:~/github$ lsb_release -a
No LSB modules are available.
Distributor ID:	Ubuntu
Description:	Ubuntu 17.10
Release:	17.10
Codename:	artful
```

```
vu@Vu-Gazelle:~/github$ java -version
java version "1.8.0_151"
Java(TM) SE Runtime Environment (build 1.8.0_151-b12)
Java HotSpot(TM) 64-Bit Server VM (build 25.151-b12, mixed mode)
```

<!----------------------------------------------------------------------------->

## commit

```
vu@Vu-Gazelle:~/github/drjava/drjava$ git log
commit 0886cf16da57b40012734584f089b7b0f4f474da (HEAD -> master)
Merge: 6a479a389 046954c0a
Author: Vu <vuphan314@users.noreply.github.com>
Date:   Tue Nov 21 12:21:36 2017 -0600

    Merge branch master github.com/DrJavaAtRice/drjava
```

<!----------------------------------------------------------------------------->

## 4 failed tests

```
GlobalModelJUnitTest
GlobalModelOtherTest
SingleDisplayModelTest
JUnitErrorModelTest
```

<!----------------------------------------------------------------------------->

## Dr. Cartwright's `~/.drjava` file

```
vu@Vu-Gazelle:~/github/drjava/drjava$ date
Tue Nov 21 12:25:54 CST 2017
```

```
vu@Vu-Gazelle:~/github/drjava/drjava$ ant test
Buildfile: /home/vu/github/drjava/drjava/build.xml
     [echo] libs = asm-6.0.jar;docs.jar;dynamicjava-base.jar;ecj-4.5.1.jar;hamcrest-core.jar;jacocoagent.jar;jacocoant.jar;javalanglevels-base.jar;javarepl-dev.build.jar;jgoodies-common-1.8.1.jar;jgoodies-forms-1.9.0.jar;jgoodies-looks-2.7.0.jar;jsoup-1.8.1.jar;junit.jar;org.jacoco.agent-0.7.10.201707180856.jar;org.jacoco.ant-0.7.10.201707180856.jar;org.jacoco.core-0.7.10.201707180856.jar;org.jacoco.core-0.7.3.201409180205.jar;org.jacoco.report-0.7.10.201707180856.jar;org.jacoco.report-0.7.3.201409180205.jar;platform.jar;plt.jar;seaglasslookandfeel-0.2.jar
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
     [echo] libs = asm-6.0.jar;docs.jar;dynamicjava-base.jar;ecj-4.5.1.jar;hamcrest-core.jar;jacocoagent.jar;jacocoant.jar;javalanglevels-base.jar;javarepl-dev.build.jar;jgoodies-common-1.8.1.jar;jgoodies-forms-1.9.0.jar;jgoodies-looks-2.7.0.jar;jsoup-1.8.1.jar;junit.jar;org.jacoco.agent-0.7.10.201707180856.jar;org.jacoco.ant-0.7.10.201707180856.jar;org.jacoco.core-0.7.10.201707180856.jar;org.jacoco.core-0.7.3.201409180205.jar;org.jacoco.report-0.7.10.201707180856.jar;org.jacoco.report-0.7.3.201409180205.jar;platform.jar;plt.jar;seaglasslookandfeel-0.2.jar
     [echo] jrelibs = charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
     [echo] extlibs = cldrdata.jar;dnsns.jar;jaccess.jar;jfxrt.jar;localedata.jar;nashorn.jar;sunec.jar;sunjce_provider.jar;sunpkcs11.jar;zipfs.jar

check-generate-dir-from-dir:

do-unjar-libs:

compile:

resolve-current-tools:

test:
     [echo] libs = asm-6.0.jar;docs.jar;dynamicjava-base.jar;ecj-4.5.1.jar;hamcrest-core.jar;jacocoagent.jar;jacocoant.jar;javalanglevels-base.jar;javarepl-dev.build.jar;jgoodies-common-1.8.1.jar;jgoodies-forms-1.9.0.jar;jgoodies-looks-2.7.0.jar;jsoup-1.8.1.jar;junit.jar;org.jacoco.agent-0.7.10.201707180856.jar;org.jacoco.ant-0.7.10.201707180856.jar;org.jacoco.core-0.7.10.201707180856.jar;org.jacoco.core-0.7.3.201409180205.jar;org.jacoco.report-0.7.10.201707180856.jar;org.jacoco.report-0.7.3.201409180205.jar;platform.jar;plt.jar;seaglasslookandfeel-0.2.jar
     [echo] jrelibs = charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
     [echo] extlibs = cldrdata.jar;dnsns.jar;jaccess.jar;jfxrt.jar;localedata.jar;nashorn.jar;sunec.jar;sunjce_provider.jar;sunpkcs11.jar;zipfs.jar

resolve-test-formatter-class:

iterate-tests:
     [echo] Executing iterate-tests
     [echo] libs = asm-6.0.jar;docs.jar;dynamicjava-base.jar;ecj-4.5.1.jar;hamcrest-core.jar;jacocoagent.jar;jacocoant.jar;javalanglevels-base.jar;javarepl-dev.build.jar;jgoodies-common-1.8.1.jar;jgoodies-forms-1.9.0.jar;jgoodies-looks-2.7.0.jar;jsoup-1.8.1.jar;junit.jar;org.jacoco.agent-0.7.10.201707180856.jar;org.jacoco.ant-0.7.10.201707180856.jar;org.jacoco.core-0.7.10.201707180856.jar;org.jacoco.core-0.7.3.201409180205.jar;org.jacoco.report-0.7.10.201707180856.jar;org.jacoco.report-0.7.3.201409180205.jar;platform.jar;plt.jar;seaglasslookandfeel-0.2.jar
     [echo] jrelibs = charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
     [echo] extlibs = cldrdata.jar;dnsns.jar;jaccess.jar;jfxrt.jar;localedata.jar;nashorn.jar;sunec.jar;sunjce_provider.jar;sunpkcs11.jar;zipfs.jar

resolve-jvm-args:

do-test:
     [echo] Running all tests matching '*' with command 'java', using '${junit-jar}' and '/usr/lib/jvm/java-8-oracle/lib/tools.jar'
[jacoco:coverage] Enhancing junit with coverage
    [junit] CommandLineTest                              12.023 sec
    [junit] ConfigFileTest                               0.317 sec
    [junit] DependenciesTest                             0.258 sec
    [junit] BooleanOptionTest                            0.252 sec
    [junit] ColorOptionTest                              0.258 sec
    [junit] DrJavaPropertySetupTest                      0.438 sec
    [junit] FontOptionTest                               0.242 sec
    [junit] ForcedChoiceOptionTest                       0.258 sec
    [junit] IntegerOptionTest                            0.281 sec
    [junit] KeyStrokeOptionTest                          0.475 sec
    [junit] LongOptionTest                               0.261 sec
    [junit] NonNegativeIntegerOptionTest                 0.262 sec
    [junit] OptionMapLoaderTest                          0.278 sec
    [junit] RecursiveFileListPropertyTest                0.022 sec
    [junit] SavableConfigurationTest                     0.252 sec
    [junit] StringOptionTest                             0.245 sec
    [junit] VectorOptionTest                             0.243 sec
    [junit] AbstractDJDocumentTest                       0.27 sec
    [junit] ClassAndInterfaceFinderTest                  0.297 sec
    [junit] ClipboardHistoryModelTest                    0.007 sec
    [junit] DocumentRegionTest                           0.222 sec
    [junit] DummyGlobalModelTest                         0.252 sec
    [junit] DummyOpenDefDocTest                          0.292 sec
    [junit] EventNotifierTest                            0.292 sec
    [junit] FindReplaceMachineTest                       0.36 sec
    [junit] GlobalIndentTest                             8.895 sec
    [junit] GlobalModelCompileErrorsTest                 7.137 sec
    [junit] GlobalModelCompileIOTest                     7.697 sec
    [junit] GlobalModelCompileSuccessOptionsTest         4.096 sec
    [junit] GlobalModelCompileSuccessTest                3.998 sec
    [junit] GlobalModelCompileTest                       7.68 sec
    [junit] GlobalModelIOTest                            29.459 sec
    [junit] GlobalModelJUnitTest                         16.764 sec
    [junit] Testsuite: edu.rice.cs.drjava.model.GlobalModelJUnitTest
    [junit] Tests run: 14, Failures: 1, Errors: 13
    [junit] ------------- Standard Output ---------------
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5208410230188852640, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu7388951152607684174, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu7769125439560152442, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu6372520454848866301, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu8939100105748552602, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu4149294641460584434, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu8662682442315839695, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu7670909271631489375, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu7670909271631489375, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.access$200(DefaultJUnitModel.java:86)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel$1$2.run(DefaultJUnitModel.java:282)
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
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5655252296517299916, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5557801197185589957, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.access$200(DefaultJUnitModel.java:86)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel$1$2.run(DefaultJUnitModel.java:282)
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
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu854871758223487805, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu7285028187889493096, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu636122992828057108, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5095051210763798567, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] ------------- ---------------- ---------------
    [junit] ------------- Standard Error -----------------
    [junit] ********** Starting JUnit on NonPublic.java
    [junit] ********** Starting JUnit on JUnit4MultiTest.java
    [junit] ********** Starting JUnit on MonkeyTestError.java
    [junit] ********** Starting JUnit on MonkeyTestPass.java
    [junit] ********** Starting JUnit on JUnit4NoTest.java
    [junit] ********** Starting JUnit on JUnit4TwoMethod1Test.java
    [junit] ********** Starting JUnit on MonkeyTestPass.java
    [junit] ********** Starting JUnit on NonTestCase.java
    [junit] ********** Starting JUnit on MonkeyTestPass.java
    [junit] ********** Starting JUnit on Elspeth.java
    [junit] ********** Starting JUnit on JUnit4StyleTest.java
    [junit] ------------- ---------------- ---------------
    [junit] Testcase: testResultOfNonPublicTestCase_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testResultOfNonPublicTestCase_NOJOIN(GlobalModelJUnitTest.java:347)
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
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testJUnit4MultiTest_NOJOIN(GlobalModelJUnitTest.java:860)
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
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testRealError_NOJOIN(GlobalModelJUnitTest.java:282)
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
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testNoJUnitErrors_NOJOIN(GlobalModelJUnitTest.java:200)
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
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testJUnit4NoTest_NOJOIN(GlobalModelJUnitTest.java:894)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Testcase: testCorrectFilesAfterIncorrectChanges_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitDocs(DefaultJUnitModel.java:212)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitAll(DefaultJUnitModel.java:182)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1236)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testCorrectFilesAfterIncorrectChanges_NOJOIN(GlobalModelJUnitTest.java:782)
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
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testJUnit4TwoMethod1Test_NOJOIN(GlobalModelJUnitTest.java:929)
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
    [junit] Testcase: testNonTestCaseError_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testNonTestCaseError_NOJOIN(GlobalModelJUnitTest.java:310)
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
    [junit] Testcase: testOneJUnitError_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitDocs(DefaultJUnitModel.java:212)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitAll(DefaultJUnitModel.java:182)
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
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
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
    [junit] Testcase: testJUnit4StyleTestWorks_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:727)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1228)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testJUnit4StyleTestWorks_NOJOIN(GlobalModelJUnitTest.java:826)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Test edu.rice.cs.drjava.model.GlobalModelJUnitTest FAILED
    [junit] GlobalModelOtherTest                         159.469 sec
    [junit] Testsuite: edu.rice.cs.drjava.model.GlobalModelOtherTest
    [junit] Tests run: 16, Failures: 8, Errors: 0
    [junit] ------------- Standard Output ---------------
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu4836333457356647056, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5298963082238246029, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu6756678776365889517, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu2868473685167803011, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu2390525616628911596, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu8978450501082008473/dir1, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] ------------- ---------------- ---------------
    [junit] ------------- Standard Error -----------------
    [junit] undoableEditHappened(javax.swing.event.UndoableEditEvent[source=ddoc for (Untitled)]) called
    [junit] undoableEditHappened call propagated to listener
    [junit] ------------- ---------------- ---------------
    [junit] Testcase: testInteractionsLiveUpdateClassPath(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase.doCompile(GlobalModelTestCase.java:337)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testInteractionsLiveUpdateClassPath(GlobalModelOtherTest.java:498)
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
    [junit] Testcase: testExitInteractions(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testExitInteractions(GlobalModelOtherTest.java:137)
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
    [junit] Testcase: testInteractionsCanSeeCompiledClasses(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] Reset did not complete before timeout
    [junit] junit.framework.AssertionFailedError: Reset did not complete before timeout
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$InteractionListener.waitResetDone(GlobalModelTestCase.java:1046)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase.doCompile(GlobalModelTestCase.java:337)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testInteractionsCanSeeCompiledClasses(GlobalModelOtherTest.java:170)
    [junit]
    [junit]
    [junit] Testcase: testSwitchInterpreters(edu.rice.cs.drjava.model.GlobalModelOtherTest):	FAILED
    [junit] number of times interpreterChanged fired expected:<1> but was:<0>
    [junit] junit.framework.AssertionFailedError: number of times interpreterChanged fired expected:<1> but was:<0>
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$TestListener.assertInterpreterChangedCount(GlobalModelTestCase.java:792)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelOtherTest.testSwitchInterpreters(GlobalModelOtherTest.java:556)
    [junit]
    [junit]
    [junit] Test edu.rice.cs.drjava.model.GlobalModelOtherTest FAILED
    [junit] MultiThreadedTestCaseTest                    0.327 sec
    [junit] SingleDisplayModelTest                       9.393 sec
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
    [junit] TestDocGetterTest                            0.448 sec
    [junit] DocumentCacheTest                            8.303 sec
    [junit] CompilerErrorModelTest                       0.629 sec
    [junit] DebugWatchDataTest                           0.371 sec
    [junit] CommentTest                                  0.426 sec
    [junit] DefinitionsDocumentTest                      1.593 sec
    [junit] IndentHelperTest                             0.461 sec
    [junit] IndentTest                                   0.594 sec
    [junit] ActionBracePlusTest                          0.493 sec
    [junit] ActionDoNothingTest                          0.458 sec
    [junit] ActionStartPrevLinePlusMultilinePreserveTest 0.484 sec
    [junit] ActionStartPrevLinePlusTest                  0.427 sec
    [junit] ActionStartPrevStmtPlusTest                  0.446 sec
    [junit] ActionStartStmtOfBracePlusTest               0.41 sec
    [junit] IndentRuleWithTraceTest                      0.355 sec
    [junit] QuestionBraceIsCurlyTest                     0.418 sec
    [junit] QuestionBraceIsParenOrBracketTest            0.405 sec
    [junit] QuestionCurrLineEmptyOrEnterPressTest        0.435 sec
    [junit] QuestionCurrLineIsWingCommentTest            0.485 sec
    [junit] QuestionCurrLineStartsWithSkipCommentsTest   0.446 sec
    [junit] QuestionCurrLineStartsWithTest               0.409 sec
    [junit] QuestionExistsCharInStmtTest                 0.358 sec
    [junit] QuestionHasCharPrecedingOpenBraceTest        0.408 sec
    [junit] QuestionInsideCommentTest                    0.46 sec
    [junit] QuestionLineContainsTest                     0.413 sec
    [junit] QuestionNewParenPhraseTest                   0.526 sec
    [junit] QuestionPrevLineStartsCommentTest            0.467 sec
    [junit] QuestionPrevLineStartsWithTest               0.525 sec
    [junit] QuestionStartAfterOpenBraceTest              0.498 sec
    [junit] QuestionStartingNewStmtTest                  0.437 sec
    [junit] BackSlashTest                                0.361 sec
    [junit] BraceInfoTest                                0.388 sec
    [junit] BraceTest                                    0.32 sec
    [junit] GapTest                                      0.357 sec
    [junit] MixedQuoteTest                               0.357 sec
    [junit] ModelListTest                                0.479 sec
    [junit] ReducedModelDeleteTest                       0.415 sec
    [junit] ReducedModelTest                             0.391 sec
    [junit] SingleQuoteTest                              0.317 sec
    [junit] JavadocModelTest                             0.457 sec
    [junit] JUnitErrorModelTest                          5.041 sec
    [junit] Testsuite: edu.rice.cs.drjava.model.junit.JUnitErrorModelTest
    [junit] Tests run: 3, Failures: 0, Errors: 3
    [junit] ------------- Standard Output ---------------
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu2372916694468323627, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu28807314531154268, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5077522745457743467, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5077522745457743467, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5536340476683236821.jar]';  bootClassPath = 'null'
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
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
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
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
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
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
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
    [junit] DrJavaBookTest                               0.318 sec
    [junit] HistoryTest                                  0.443 sec
^Z
[1]+  Stopped                 ant test
```

```
vu@Vu-Gazelle:~/github/drjava/drjava$ date
Tue Nov 21 12:38:17 CST 2017
```
