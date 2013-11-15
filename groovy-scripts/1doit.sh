#!/bin/bash

groovy_file="_doit.groovy"

cls=$1
echo "import ${cls}
${cls}.doit()" > $groovy_file
groovy $groovy_file
rm $groovy_file
