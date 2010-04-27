#!/bin/bash

find . -type f -name \*.java -or -name \*.txt -or -name \*.xml -or -name \*.sh -not -wholename '*/.svn/*'| xargs -n 1 -I {} -i bash -c 'if [ "`svn propget svn:eol-style {}`" != "native" ]; then echo {}; fi'
