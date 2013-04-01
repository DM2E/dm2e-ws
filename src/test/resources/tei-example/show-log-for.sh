#!/bin/zsh

source ../../../../curl_rest.sh

JOB_URI=$1

GET $JOB_URI/log -H $AC_LOG
