#!/bin/sh

# Directory where Eclipse is installed
ECLIPSE=$HOME/eclipse

# Version of core Eclipse plugins
ECLIPSE_VERSION=2.1.0

# Platform used by Eclipse's SWT plugin
ECLIPSE_PLATFORM=motif

# Platform-specific path separator
SEP=:


export CLASSPATH=${CLASSPATH}${SEP}${ECLIPSE}/plugins/org.eclipse.core.runtime_${ECLIPSE_VERSION}/runtime.jar${SEP}${ECLIPSE}/plugins/org.eclipse.core.resources_${ECLIPSE_VERSION}/resources.jar${SEP}${ECLIPSE}/plugins/org.eclipse.ui_${ECLIPSE_VERSION}/ui.jar${SEP}${ECLIPSE}/plugins/org.eclipse.ui.workbench_${ECLIPSE_VERSION}/workbench.jar${SEP}${ECLIPSE}/plugins/org.eclipse.swt.${ECLIPSE_PLATFORM}_${ECLIPSE_VERSION}/ws/${ECLIPSE_PLATFORM}/swt.jar${SEP}${ECLIPSE}/plugins/org.eclipse.debug.ui_${ECLIPSE_VERSION}/dtui.jar${SEP}${ECLIPSE}/plugins/org.eclipse.jdt.ui_${ECLIPSE_VERSION}/jdt.jar${SEP}${ECLIPSE}/plugins/org.eclipse.search_${ECLIPSE_VERSION}/search.jar
