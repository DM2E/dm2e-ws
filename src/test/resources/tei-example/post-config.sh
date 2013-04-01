#!/bin/zsh

source ../../../../curl_rest.sh

# post xml
XML_SOURCE=$(zsh post-xml-file.sh)
# post xslt
XSLT_SOURCE=$(zsh post-xslt-file.sh)
# post empty file
RESULT_FILE=$(zsh post-result-file.sh)

# the config to be posted
CONFIG="@prefix dm2e: <http://onto.dm2e.eu/onto#> .
@prefix dm2e_types: <http://onto.dm2e.eu/types#> .
@prefix omnom: <http://omnom.dm2e.eu/> .
@prefix xslt_service: <http://omnom.dm2e.eu/service/xslt#> .
[]  a dm2e:WebServiceConfig ;
    xslt_service:xsltSource <$XSLT_SOURCE>  ;
    xslt_service:xmlSource <$XML_SOURCE> ;
    xslt_service:output <$RESULT_FILE> .
"

echo $CONFIG > tmp_config.ttl

POST $SRV_MAIN/data/configurations -d @tmp_config.ttl -H $CT_TURTLE \
    2>&1 >/dev/null | grep Location | cut -d' ' -f3 | sed 's///'
