#!/bin/zsh

source ../../../../curl_rest.sh

PUT $SRV_TASK/service/xslt -H $AC_TURTLE -H $CT_TEXT -d $(zsh post-config.sh) \
    2>&1 >/dev/null | grep Location | cut -d' ' -f3|sed 's///'
