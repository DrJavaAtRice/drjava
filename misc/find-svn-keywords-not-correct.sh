#!/bin/bash

find . -type f -name \*.java -not -wholename '*/.svn/*'| xargs -n 1 -I {} -i bash -c 'if [ "`svn propget svn:keywords {}`" != "Author Date Id Revision" ]; then echo {}; fi'
