#!/bin/zsh

source ../../../../curl_rest.sh

CONFIG_URI=$(zsh post-config.sh)

PUT $SRV_TASK/service/xslt/validate -H $CT_TURTLE -d $CONFIG_URI \
    2>&1 >/dev/null | grep '< HTTP/1.1' |grep -o '[0-9]\{3\}'
