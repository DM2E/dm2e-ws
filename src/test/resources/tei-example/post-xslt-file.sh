#!/bin/zsh

source ../../../../curl_rest.sh

POST $SRV_MAIN/file -F file=@dta.xsl -F meta=@dta.meta.ttl 2>&1 >/dev/null \
    | grep Location | cut -d' ' -f3 |sed 's///'
