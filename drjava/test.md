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

## `objectweb`

```
vu@Vu-Gazelle:~/github/drjava/drjava$ jar -tvf drjava.jar | grep objectweb
     0 Sat Oct 12 09:49:02 CDT 2013 org/objectweb/
     0 Tue Nov 21 12:40:28 CST 2017 org/objectweb/asm/
     0 Tue Nov 21 12:40:28 CST 2017 org/objectweb/asm/commons/
     0 Sat Oct 12 09:49:02 CDT 2013 org/objectweb/asm/signature/
     0 Tue Nov 21 12:40:28 CST 2017 org/objectweb/asm/tree/
     0 Tue Nov 21 12:40:28 CST 2017 org/objectweb/asm/tree/analysis/
     0 Tue Nov 21 12:40:28 CST 2017 org/objectweb/asm/util/
     0 Tue Nov 21 12:40:28 CST 2017 org/objectweb/asm/xml/
   978 Sat Oct 12 09:48:56 CDT 2013 org/objectweb/asm/AnnotationVisitor.class
  3685 Sat Oct 12 09:48:56 CDT 2013 org/objectweb/asm/AnnotationWriter.class
  1545 Sat Oct 12 09:48:56 CDT 2013 org/objectweb/asm/Attribute.class
  1920 Sat Oct 12 09:48:56 CDT 2013 org/objectweb/asm/ByteVector.class
 18727 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/ClassReader.class
  1588 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/ClassVisitor.class
 12756 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/ClassWriter.class
   398 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/Context.class
   916 Sat Sep 23 06:29:08 CDT 2017 org/objectweb/asm/CurrentFrame.class
   232 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/Edge.class
   694 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/FieldVisitor.class
  2614 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/FieldWriter.class
  7025 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/Frame.class
  1062 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/Handle.class
   736 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/Handler.class
  1799 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/Item.class
  2525 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/Label.class
  3061 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/MethodVisitor.class
 19810 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/MethodWriter.class
  1854 Sat Sep 23 06:29:08 CDT 2017 org/objectweb/asm/ModuleVisitor.class
  4024 Sat Sep 23 06:29:08 CDT 2017 org/objectweb/asm/ModuleWriter.class
  6677 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/Opcodes.class
  6246 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/Type.class
  2197 Sat Sep 23 06:29:08 CDT 2017 org/objectweb/asm/TypePath.class
  3198 Sat Sep 23 06:29:08 CDT 2017 org/objectweb/asm/TypeReference.class
  6188 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/AdviceAdapter.class
  8466 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/AnalyzerAdapter.class
  2418 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/CodeSizeEvaluator.class
 12819 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/GeneratorAdapter.class
 11223 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/InstructionAdapter.class
  2382 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/JSRInlinerAdapter$Instantiation.class
  6229 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/JSRInlinerAdapter.class
  3395 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/LocalVariablesSorter.class
  3243 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/Method.class
  3172 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/Remapper.class
  1298 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/RemappingAnnotationAdapter.class
  3023 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/RemappingClassAdapter.class
   934 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/RemappingFieldAdapter.class
  3334 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/RemappingMethodAdapter.class
  2350 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/RemappingSignatureAdapter.class
   935 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/SerialVersionUIDAdder$Item.class
  3911 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/SerialVersionUIDAdder.class
  1059 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/SimpleRemapper.class
  1567 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/StaticInitMerger.class
   185 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/TableSwitchGenerator.class
  1049 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/TryCatchBlockSorter$1.class
   986 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/commons/TryCatchBlockSorter.class
  1892 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/signature/SignatureReader.class
  1173 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/signature/SignatureVisitor.class
  1870 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/signature/SignatureWriter.class
  1667 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/AbstractInsnNode.class
  1863 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/AnnotationNode.class
  3495 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/ClassNode.class
   749 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/FieldInsnNode.class
  1896 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/FieldNode.class
  1779 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/FrameNode.class
   564 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/IincInsnNode.class
   529 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/InnerClassNode.class
  1690 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/InsnList$InsnListIterator.class
  3127 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/InsnList.class
   492 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/InsnNode.class
   596 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/IntInsnNode.class
   809 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/InvokeDynamicInsnNode.class
   893 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/JumpInsnNode.class
   854 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/LabelNode.class
   556 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/LdcInsnNode.class
   864 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/LineNumberNode.class
   912 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/LocalVariableNode.class
  1626 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/LookupSwitchInsnNode.class
   751 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/MethodInsnNode.class
   455 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/MethodNode$1.class
  7870 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/MethodNode.class
   623 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/MultiANewArrayInsnNode.class
  1462 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/TableSwitchInsnNode.class
   834 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/TryCatchBlockNode.class
   633 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/TypeInsnNode.class
   592 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/VarInsnNode.class
  7728 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/analysis/Analyzer.class
   978 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/analysis/AnalyzerException.class
  7026 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/analysis/BasicInterpreter.class
  1508 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/analysis/BasicValue.class
  7187 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/analysis/BasicVerifier.class
  7812 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/analysis/Frame.class
  1536 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/analysis/Interpreter.class
  4897 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/analysis/SimpleVerifier.class
  1134 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/analysis/SmallSet.class
  5118 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/analysis/SourceInterpreter.class
   835 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/analysis/SourceValue.class
  1218 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/analysis/Subroutine.class
   113 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/tree/analysis/Value.class
   165 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/ASMifiable.class
 17648 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/ASMifier.class
  2136 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/CheckAnnotationAdapter.class
 10858 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/CheckClassAdapter.class
  1115 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/CheckFieldAdapter.class
  1796 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/CheckMethodAdapter$1.class
 14698 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/CheckMethodAdapter.class
  2940 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/CheckSignatureAdapter.class
  5651 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/Printer.class
   149 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/Textifiable.class
 17008 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/Textifier.class
  1339 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/TraceAnnotationVisitor.class
  2900 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/TraceClassVisitor.class
  1117 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/TraceFieldVisitor.class
  3760 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/TraceMethodVisitor.class
  3391 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/util/TraceSignatureVisitor.class
   853 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$AnnotationDefaultRule.class
  1222 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$AnnotationParameterRule.class
  1257 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$AnnotationRule.class
   979 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$AnnotationValueAnnotationRule.class
   933 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$AnnotationValueArrayRule.class
   797 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$AnnotationValueEnumRule.class
   897 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$AnnotationValueRule.class
  1088 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$ClassRule.class
   791 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$ExceptionRule.class
  1197 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$ExceptionsRule.class
  1289 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$FieldRule.class
  1703 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$FrameRule.class
  1041 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$FrameTypeRule.class
   884 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$InnerClassRule.class
   791 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$InterfaceRule.class
  1242 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$InterfacesRule.class
   848 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$InvokeDynamicBsmArgumentsRule.class
  1333 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$InvokeDynamicRule.class
   755 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$LabelRule.class
   862 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$LineNumberRule.class
  1037 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$LocalVarRule.class
   910 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$LookupSwitchLabelRule.class
  1635 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$LookupSwitchRule.class
   763 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$MaxRule.class
  1111 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$MethodRule.class
   235 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$Opcode.class
   458 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$OpcodeGroup.class
  2372 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$OpcodesRule.class
   800 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$OuterClassRule.class
  4675 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$Rule.class
  1306 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$RuleSet.class
   752 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$SourceRule.class
   867 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$TableSwitchLabelRule.class
  1598 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$TableSwitchRule.class
   913 Sat Oct 12 09:48:58 CDT 2013 org/objectweb/asm/xml/ASMContentHandler$TryCatchRule.class
  9480 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/ASMContentHandler.class
   959 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/Processor$ASMContentHandlerFactory$1.class
   668 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/Processor$ASMContentHandlerFactory.class
   169 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/Processor$ContentHandlerFactory.class
   246 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/Processor$EntryElement.class
  1574 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/Processor$InputSlicingHandler.class
  2326 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/Processor$OutputSlicingHandler.class
   532 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/Processor$ProtectedInputStream.class
  2917 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/Processor$SAXWriter.class
   499 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/Processor$SAXWriterFactory.class
   533 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/Processor$SingleDocElement.class
   429 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/Processor$SubdocumentHandlerFactory.class
  1220 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/Processor$TransformerHandlerFactory.class
   711 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/Processor$ZipEntryElement.class
  7872 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/Processor.class
  1281 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/SAXAdapter.class
  3302 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/SAXAnnotationAdapter.class
  4473 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/SAXClassAdapter.class
  6418 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/SAXCodeAdapter.class
   823 Sat Oct 12 09:49:00 CDT 2013 org/objectweb/asm/xml/SAXFieldAdapter.class
vu@Vu-Gazelle:~/github/drjava/drjava$
```

<!----------------------------------------------------------------------------->

## Dr. Cartwright's `~/.drjava` file

`commit 0886cf16da57b40012734584f089b7b0f4f474da`

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

<!----------------------------------------------------------------------------->

## Vu's `~/.drjava` file

`commit ed17cbaef63890495a2d72b97e019c5641a43834`

```
vu@Vu-Gazelle:~/github/drjava/drjava$ ant test
Buildfile: /home/vu/github/drjava/drjava/build.xml
     [echo] libs = asm-6.0.jar;docs.jar;dynamicjava-base.jar;ecj-4.5.1.jar;hamcrest-core.jar;jacocoagent.jar;jacocoant.jar;javalanglevels-base.jar;jgoodies-common-1.8.1.jar;jgoodies-forms-1.9.0.jar;jgoodies-looks-2.7.0.jar;jsoup-1.8.1.jar;junit.jar;org.jacoco.agent-0.7.10.201707180856.jar;org.jacoco.ant-0.7.10.201707180856.jar;org.jacoco.core-0.7.10.201707180856.jar;org.jacoco.report-0.7.10.201707180856.jar;platform.jar;plt.jar;seaglasslookandfeel-0.2.jar
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
     [move] Moving 498 files to /home/vu/github/drjava/drjava/classes/base
     [echo] jrelibs=charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
    [javac] Compiling 608 source files to /home/vu/github/drjava/drjava/classes/base
     [move] Moving 499 files to /home/vu/github/drjava/drjava/classes/test

copy-resources:
     [copy] Copying 58 files to /home/vu/github/drjava/drjava/classes/base

unjar-libs:
     [echo] libs = asm-6.0.jar;docs.jar;dynamicjava-base.jar;ecj-4.5.1.jar;hamcrest-core.jar;jacocoagent.jar;jacocoant.jar;javalanglevels-base.jar;jgoodies-common-1.8.1.jar;jgoodies-forms-1.9.0.jar;jgoodies-looks-2.7.0.jar;jsoup-1.8.1.jar;junit.jar;org.jacoco.agent-0.7.10.201707180856.jar;org.jacoco.ant-0.7.10.201707180856.jar;org.jacoco.core-0.7.10.201707180856.jar;org.jacoco.report-0.7.10.201707180856.jar;platform.jar;plt.jar;seaglasslookandfeel-0.2.jar
     [echo] jrelibs = charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
     [echo] extlibs = cldrdata.jar;dnsns.jar;jaccess.jar;jfxrt.jar;localedata.jar;nashorn.jar;sunec.jar;sunjce_provider.jar;sunpkcs11.jar;zipfs.jar

check-generate-dir-from-dir:

do-unjar-libs:

compile:

resolve-current-tools:

test:
     [echo] libs = asm-6.0.jar;docs.jar;dynamicjava-base.jar;ecj-4.5.1.jar;hamcrest-core.jar;jacocoagent.jar;jacocoant.jar;javalanglevels-base.jar;jgoodies-common-1.8.1.jar;jgoodies-forms-1.9.0.jar;jgoodies-looks-2.7.0.jar;jsoup-1.8.1.jar;junit.jar;org.jacoco.agent-0.7.10.201707180856.jar;org.jacoco.ant-0.7.10.201707180856.jar;org.jacoco.core-0.7.10.201707180856.jar;org.jacoco.report-0.7.10.201707180856.jar;platform.jar;plt.jar;seaglasslookandfeel-0.2.jar
     [echo] jrelibs = charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
     [echo] extlibs = cldrdata.jar;dnsns.jar;jaccess.jar;jfxrt.jar;localedata.jar;nashorn.jar;sunec.jar;sunjce_provider.jar;sunpkcs11.jar;zipfs.jar

resolve-test-formatter-class:

iterate-tests:
     [echo] Executing iterate-tests
     [echo] libs = asm-6.0.jar;docs.jar;dynamicjava-base.jar;ecj-4.5.1.jar;hamcrest-core.jar;jacocoagent.jar;jacocoant.jar;javalanglevels-base.jar;jgoodies-common-1.8.1.jar;jgoodies-forms-1.9.0.jar;jgoodies-looks-2.7.0.jar;jsoup-1.8.1.jar;junit.jar;org.jacoco.agent-0.7.10.201707180856.jar;org.jacoco.ant-0.7.10.201707180856.jar;org.jacoco.core-0.7.10.201707180856.jar;org.jacoco.report-0.7.10.201707180856.jar;platform.jar;plt.jar;seaglasslookandfeel-0.2.jar
     [echo] jrelibs = charsets.jar;deploy.jar;javaws.jar;jce.jar;jfr.jar;jfxswt.jar;jsse.jar;management-agent.jar;plugin.jar;resources.jar;rt.jar
     [echo] extlibs = cldrdata.jar;dnsns.jar;jaccess.jar;jfxrt.jar;localedata.jar;nashorn.jar;sunec.jar;sunjce_provider.jar;sunpkcs11.jar;zipfs.jar

resolve-jvm-args:

do-test:
     [echo] Running all tests matching '*' with command 'java', using '${junit-jar}' and '/usr/lib/jvm/java-8-oracle/lib/tools.jar'
[jacoco:coverage] Enhancing junit with coverage
    [junit] CommandLineTest                              10.127 sec
    [junit] ConfigFileTest                               0.322 sec
    [junit] DependenciesTest                             0.265 sec
    [junit] BooleanOptionTest                            0.259 sec
    [junit] ColorOptionTest                              0.282 sec
    [junit] DrJavaPropertySetupTest                      0.404 sec
    [junit] FontOptionTest                               0.252 sec
    [junit] ForcedChoiceOptionTest                       0.242 sec
    [junit] IntegerOptionTest                            0.248 sec
    [junit] KeyStrokeOptionTest                          0.479 sec
    [junit] LongOptionTest                               0.275 sec
    [junit] NonNegativeIntegerOptionTest                 0.274 sec
    [junit] OptionMapLoaderTest                          0.254 sec
    [junit] RecursiveFileListPropertyTest                0.018 sec
    [junit] SavableConfigurationTest                     0.268 sec
    [junit] StringOptionTest                             0.306 sec
    [junit] VectorOptionTest                             0.289 sec
    [junit] AbstractDJDocumentTest                       0.308 sec
    [junit] ClassAndInterfaceFinderTest                  0.266 sec
    [junit] ClipboardHistoryModelTest                    0.006 sec
    [junit] DocumentRegionTest                           0.218 sec
    [junit] DummyGlobalModelTest                         0.283 sec
    [junit] DummyOpenDefDocTest                          0.273 sec
    [junit] EventNotifierTest                            0.29 sec
    [junit] FindReplaceMachineTest                       0.351 sec
    [junit] GlobalIndentTest                             4.932 sec
    [junit] GlobalModelCompileErrorsTest                 4.431 sec
    [junit] GlobalModelCompileIOTest                     6.583 sec
    [junit] GlobalModelCompileSuccessOptionsTest         2.412 sec
    [junit] GlobalModelCompileSuccessTest                2.925 sec
    [junit] GlobalModelCompileTest                       4.373 sec
    [junit] GlobalModelIOTest                            16.913 sec
    [junit] GlobalModelJUnitTest                         9.325 sec
    [junit] Testsuite: edu.rice.cs.drjava.model.GlobalModelJUnitTest
    [junit] Tests run: 14, Failures: 1, Errors: 13
    [junit] ------------- Standard Output ---------------
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu1918369028699505376, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu7160294357563295171, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5044273328837132015, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu1441146025600376517, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu5002683804843107450, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu6327281938329492736, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu3192354518768567107, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu1483773352364003025, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu1483773352364003025, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
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
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu6774191600952707322, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu4173846329794562127, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
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
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu9099470852520677516, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu7480289164142289245, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu4349069243474249640, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu4767760733522007123, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] ------------- ---------------- ---------------
    [junit] Testcase: testResultOfNonPublicTestCase_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:726)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1279)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testResultOfNonPublicTestCase_NOJOIN(GlobalModelJUnitTest.java:344)
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
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:726)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1279)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testJUnit4MultiTest_NOJOIN(GlobalModelJUnitTest.java:857)
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
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:726)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1279)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testRealError_NOJOIN(GlobalModelJUnitTest.java:281)
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
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:726)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1279)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testNoJUnitErrors_NOJOIN(GlobalModelJUnitTest.java:199)
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
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:726)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1279)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testJUnit4NoTest_NOJOIN(GlobalModelJUnitTest.java:891)
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
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1287)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testCorrectFilesAfterIncorrectChanges_NOJOIN(GlobalModelJUnitTest.java:779)
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
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:726)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1279)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testJUnit4TwoMethod1Test_NOJOIN(GlobalModelJUnitTest.java:926)
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
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.waitJUnitDone(GlobalModelTestCase.java:1292)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1281)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testUnsavedAndUnCompiledChanges(GlobalModelJUnitTest.java:514)
    [junit]
    [junit]
    [junit] Testcase: testNonTestCaseError_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:726)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1279)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testNonTestCaseError_NOJOIN(GlobalModelJUnitTest.java:309)
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
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.waitJUnitDone(GlobalModelTestCase.java:1292)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1281)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testNoClassFile(GlobalModelJUnitTest.java:402)
    [junit]
    [junit]
    [junit] Testcase: testOneJUnitError_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitDocs(DefaultJUnitModel.java:212)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitAll(DefaultJUnitModel.java:182)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1287)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testOneJUnitError_NOJOIN(GlobalModelJUnitTest.java:230)
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
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:726)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1279)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testElspethOneJUnitError_NOJOIN(GlobalModelJUnitTest.java:255)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Testcase: testInfiniteLoop_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	FAILED
    [junit] Aborting unit testing runs recovery code in testing thread; no exception is thrown
    [junit] junit.framework.AssertionFailedError: Aborting unit testing runs recovery code in testing thread; no exception is thrown
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testInfiniteLoop_NOJOIN(GlobalModelJUnitTest.java:462)
    [junit]
    [junit]
    [junit] Testcase: testJUnit4StyleTestWorks_NOJOIN(edu.rice.cs.drjava.model.GlobalModelJUnitTest):	Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:726)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1279)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelJUnitTest.testJUnit4StyleTestWorks_NOJOIN(GlobalModelJUnitTest.java:823)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Test edu.rice.cs.drjava.model.GlobalModelJUnitTest FAILED
    [junit] GlobalModelOtherTest                         15.454 sec
    [junit] MultiThreadedTestCaseTest                    0.296 sec
    [junit] SingleDisplayModelTest                       3.27 sec
    [junit] TestDocGetterTest                            0.254 sec
    [junit] DocumentCacheTest                            3.429 sec
    [junit] CompilerErrorModelTest                       0.386 sec
    [junit] DebugWatchDataTest                           0.31 sec
    [junit] CommentTest                                  0.411 sec
    [junit] DefinitionsDocumentTest                      1.182 sec
    [junit] IndentHelperTest                             0.451 sec
    [junit] IndentTest                                   0.542 sec
    [junit] ActionBracePlusTest                          0.379 sec
    [junit] ActionDoNothingTest                          0.367 sec
    [junit] ActionStartPrevLinePlusMultilinePreserveTest 0.37 sec
    [junit] ActionStartPrevLinePlusTest                  0.361 sec
    [junit] ActionStartPrevStmtPlusTest                  0.391 sec
    [junit] ActionStartStmtOfBracePlusTest               0.363 sec
    [junit] IndentRuleWithTraceTest                      0.348 sec
    [junit] QuestionBraceIsCurlyTest                     0.324 sec
    [junit] QuestionBraceIsParenOrBracketTest            0.376 sec
    [junit] QuestionCurrLineEmptyOrEnterPressTest        0.377 sec
    [junit] QuestionCurrLineIsWingCommentTest            0.349 sec
    [junit] QuestionCurrLineStartsWithSkipCommentsTest   0.386 sec
    [junit] QuestionCurrLineStartsWithTest               0.425 sec
    [junit] QuestionExistsCharInStmtTest                 0.358 sec
    [junit] QuestionHasCharPrecedingOpenBraceTest        0.373 sec
    [junit] QuestionInsideCommentTest                    0.343 sec
    [junit] QuestionLineContainsTest                     0.361 sec
    [junit] QuestionNewParenPhraseTest                   0.383 sec
    [junit] QuestionPrevLineStartsCommentTest            0.45 sec
    [junit] QuestionPrevLineStartsWithTest               0.396 sec
    [junit] QuestionStartAfterOpenBraceTest              0.434 sec
    [junit] QuestionStartingNewStmtTest                  0.331 sec
    [junit] BackSlashTest                                0.294 sec
    [junit] BraceInfoTest                                0.355 sec
    [junit] BraceTest                                    0.278 sec
    [junit] GapTest                                      0.256 sec
    [junit] MixedQuoteTest                               0.324 sec
    [junit] ModelListTest                                0.302 sec
    [junit] ReducedModelDeleteTest                       0.413 sec
    [junit] ReducedModelTest                             0.42 sec
    [junit] SingleQuoteTest                              0.312 sec
    [junit] JavadocModelTest                             0.414 sec
    [junit] JUnitErrorModelTest                          2.928 sec
    [junit] Testsuite: edu.rice.cs.drjava.model.junit.JUnitErrorModelTest
    [junit] Tests run: 3, Failures: 0, Errors: 3
    [junit] ------------- Standard Output ---------------
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu1607756968196347723, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu2845014186205321158, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu1443630559772209437, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] Compiler is using classPath = '[/tmp/DrJava-test-vu1443630559772209437, /usr/lib/jvm/java-8-oracle/lib/tools.jar, /home/vu/github/drjava/drjava/lib/buildlib/plt-ant.jar, /home/vu/github/drjava/drjava/lib/buildlib/netbeans-memory-leak-utils.jar, /home/vu/github/drjava/drjava/classes/test, /home/vu/github/drjava/drjava/classes/base, /home/vu/github/drjava/drjava/classes/lib, /usr/share/ant/lib/junit.jar, /usr/share/java/ant-launcher-1.9.9.jar, /usr/share/ant/lib/ant.jar, /usr/share/ant/lib/ant-junit.jar, /usr/share/ant/lib/ant-junit4.jar, /tmp/jacocoagent5784881752619445294.jar]';  bootClassPath = 'null'
    [junit] ------------- ---------------- ---------------
    [junit] Testcase: testErrorsArrayInOrder_NOJOIN(edu.rice.cs.drjava.model.junit.JUnitErrorModelTest):Caused an ERROR
    [junit] java.lang.IllegalArgumentException
    [junit] edu.rice.cs.util.UnexpectedException: java.lang.IllegalArgumentException
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:471)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junitOpenDefDocs(DefaultJUnitModel.java:299)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel.junit(DefaultJUnitModel.java:231)
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:726)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1279)
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
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:726)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1279)
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
    [junit] 	at edu.rice.cs.drjava.model.DefaultGlobalModel$ConcreteOpenDefDoc.startJUnit(DefaultGlobalModel.java:726)
    [junit] 	at edu.rice.cs.drjava.model.GlobalModelTestCase$JUnitTestListener.runJUnit(GlobalModelTestCase.java:1279)
    [junit] 	at edu.rice.cs.drjava.model.junit.JUnitErrorModelTest.testVerifyErrorHandledCorrectly_NOJOIN(JUnitErrorModelTest.java:295)
    [junit] Caused by: java.lang.IllegalArgumentException
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at org.objectweb.asm.ClassReader.<init>(Unknown Source)
    [junit] 	at edu.rice.cs.drjava.model.junit.DefaultJUnitModel._rawJUnitOpenDefDocs(DefaultJUnitModel.java:414)
    [junit]
    [junit]
    [junit] Test edu.rice.cs.drjava.model.junit.JUnitErrorModelTest FAILED
    [junit] DrJavaBookTest                               0.331 sec
    [junit] HistoryTest                                  0.407 sec
    [junit] InteractionsDJDocumentTest                   4.27 sec
    [junit] InteractionsDocumentTest                     0.363 sec
    [junit] InteractionsModelErrorTest                   4.052 sec
    [junit] InteractionsModelTest                        0.628 sec
    [junit] JavaInterpreterTest                          0.718 sec
    [junit] NewJVMTest                                   0.915 sec
    [junit] ProjectTest                                  0.515 sec
    [junit] XMLProjectFileParserTest                     0.373 sec
    [junit] DefinitionsPaneMemoryLeakTest                3.235 sec
    [junit] DefinitionsPaneTest                          11.59 sec
    [junit] InteractionsPaneTest                         0.544 sec
    [junit] MainFrameTest                                16.255 sec
    [junit] NewJavaClassTest                             0.086 sec
    [junit] ProjectMenuTest                              9.298 sec
    [junit] RecentFileManagerTest                        0.363 sec
    [junit] BooleanOptionComponentTest                   0.336 sec
    [junit] ColorOptionComponentTest                     0.346 sec
    [junit] FileOptionComponentTest                      0.406 sec
    [junit] FontOptionComponentTest                      0.354 sec
    [junit] ForcedChoiceOptionComponentTest              0.348 sec
    [junit] IntegerOptionComponentTest                   0.327 sec
    [junit] KeyStrokeOptionComponentTest                 0.315 sec
    [junit] VectorFileOptionComponentTest                0.501 sec
    [junit] VectorKeyStrokeOptionComponentTest           0.517 sec
    [junit] PredictiveInputModelTest                     0.556 sec
    [junit] ArgumentTokenizerTest                        0.259 sec
    [junit] BalancingStreamTokenizerTest                 0.047 sec
    [junit] FileOpsTest                                  0.523 sec
    [junit] LogTest                                      0.45 sec
    [junit] ReaderWriterLockTest                         0.427 sec
    [junit] StreamRedirectorTest                         0.254 sec
    [junit] StringOpsTest                                0.425 sec
    [junit] XMLConfigTest                                0.103 sec
    [junit] JListSortNavigatorTest                       0.269 sec
    [junit] JTreeSortNavigatorTest                       0.348 sec
    [junit] JarCreationTest                              0.33 sec
    [junit] IntegratedMasterSlaveTest                    4.148 sec
    [junit] SExpParserTest                               0.329 sec
    [junit] TokensTest                                   0.005 sec
    [junit] HighlightManagerTest                         0.297 sec
    [junit] ScrollableListDialogTest                     0.235 sec
    [junit] ScrollableListSelectionDialogTest            0.252 sec
    [junit] UtilitiesTest                                1.081 sec
    [junit] ConsoleDocumentTest                          0.314 sec
    [junit] SwingDocumentTest                            0.294 sec

BUILD FAILED
/home/vu/github/drjava/drjava/build.xml:331: The following error occurred while executing this line:
/home/vu/github/drjava/drjava/build.xml:364: The following error occurred while executing this line:
/home/vu/github/drjava/drjava/build.xml:366: /home/vu/github/drjava/drjava/build.xml:367: The following error occurred while executing this line:
/home/vu/github/drjava/drjava/build.xml:462: One or more unit tests failed.

Total time: 4 minutes 2 seconds
```
