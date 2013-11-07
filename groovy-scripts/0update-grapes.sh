#!/bin/bash
# LOC_GRAFEO="../../grafeo"

die() {
	echo $1 | boxes -d nuke
	exit 128
}

if [ "x$GRAPES_DIR" = "x" ];then
    die "Must set \$GRAPES_DIR [e.g. GRAPES_DIR=$HOME/.groovy/grapes]"
fi

remove_old_grapes() {
    groupId=$1
    artifactId=$2

    rm -vr $GRAPES_DIR/$groupId/$artifactId
}

update_maven_update_grapes() {
    groupId=$1
    artifactId=$2
    repo=$3
    cd $repo
    mvn -o -DskipTests -Djacoco.skip=false -Dmaven.test.skip=true install
    remove_old_grapes $groupId $artifactId
    cd $oldpwd
}


oldpwd=$PWD
update_maven_update_grapes eu.dm2e.grafeo grafeo ../../grafeo/
update_maven_update_grapes eu.dm2e.ws dm2e-ws ..
