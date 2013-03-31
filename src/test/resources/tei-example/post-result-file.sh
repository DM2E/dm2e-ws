#!/bin/zsh

source ../../../../curl_rest.sh

POST $SRV_MAIN/file/empty -F meta=@result.meta.ttl \
    2>&1 >/dev/null | grep Location | cut -d' ' -f3 | sed 's///'
