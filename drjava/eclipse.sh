#!/bin/sh

# Directory where Eclipse is installed
ECLIPSE=$HOME/eclipse

# Version of core Eclipse plugins
ECLIPSE_VERSION=3.0.0

# Platform used by Eclipse's SWT plugin
#ECLIPSE_PLATFORM=win32
#ECLIPSE_PLATFORM=carbon
#ECLIPSE_PLATFORM=gtk
ECLIPSE_PLATFORM=motif

# OS and Architecture (directory)
#ECLIPSE_ARCH=macosx/ppc
ECLIPSE_ARCH=linux/x86

# Platform-specific path separator
SEP=:
#SEP=;

export CLASSPATH=${CLASSPATH}${SEP}\
${ECLIPSE}/plugins/org.eclipse.core.runtime_${ECLIPSE_VERSION}/runtime.jar${SEP}\
${ECLIPSE}/plugins/org.eclipse.core.boot_${ECLIPSE_VERSION}/boot.jar${SEP}\
${ECLIPSE}/plugins/org.eclipse.core.resources_${ECLIPSE_VERSION}/resources.jar${SEP}\
${ECLIPSE}/plugins/org.eclipse.ui_${ECLIPSE_VERSION}/ui.jar${SEP}\
${ECLIPSE}/plugins/org.eclipse.ui.console_${ECLIPSE_VERSION}/console.jar${SEP}\
${ECLIPSE}/plugins/org.eclipse.ui.workbench_${ECLIPSE_VERSION}/workbench.jar${SEP}\
${ECLIPSE}/plugins/org.eclipse.debug.ui_${ECLIPSE_VERSION}/dtui.jar${SEP}\
${ECLIPSE}/plugins/org.eclipse.swt.${ECLIPSE_PLATFORM}_${ECLIPSE_VERSION}/ws/${ECLIPSE_PLATFORM}/swt.jar${SEP}\
${ECLIPSE}/plugins/org.eclipse.swt.${ECLIPSE_PLATFORM}_${ECLIPSE_VERSION}/ws/${ECLIPSE_PLATFORM}/swt-pi.jar${SEP}\
${ECLIPSE}/plugins/org.eclipse.jdt.ui_${ECLIPSE_VERSION}/jdt.jar${SEP}\
${ECLIPSE}/plugins/org.eclipse.search_${ECLIPSE_VERSION}/search.jar${SEP}\
${ECLIPSE}/plugins/org.eclipse.jface_${ECLIPSE_VERSION}/jface.jar${SEP}\
${ECLIPSE}/plugins/org.eclipse.jdt.core_${ECLIPSE_VERSION}/jdtcore.jar${SEP}\
${ECLIPSE}/plugins/org.eclipse.osgi_${ECLIPSE_VERSION}/osgi.jar${SEP}\
plugins/eclipse/bin

export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}${SEP}\
${ECLIPSE}/plugins/org.eclipse.swt.${ECLIPSE_PLATFORM}_${ECLIPSE_VERSION}/os/${ECLIPSE_ARCH}
