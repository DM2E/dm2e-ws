# Helper aliasses and variables to make testing REST easier
# Usage:
# $> source curl_rest.sh
# $> URL=http:localhost:9998/file
# $> POST $URL -H $CT_MULTI -H $AC_TURTLE -F file=@payload.xml -F meta=@metadata.ttl
# instead of
# $> curl -v -X POST -H "Content-Type: multipart/form-data" -H "Accept: Turtle" -F file=@payload.xml -F meta=@metadata.ttl $URL

CT_TEXT='Content-Type: text/plain'
AC_TEXT='Accept: text/plain'

CT_TURTLE='Content-Type: text/turtle'
AC_TURTLE='Accept: text/turtle'

CT_NTRIPLES='Content-Type: application/rdf-triples'
AC_NTRIPLES='Accept: application/rdf-triples'

CT_N3='Content-Type: text/rdf+n3'
AC_N3='Accept: text/rdf+n3'

CT_RDFXML='Content-Type: application/rdf+xml'
AC_RDFXML='Accept: application/rdf+xml'

CT_MULTI='Content-Type: multipart/form-data'
AC_MULTI='Accept: multipart/form-data'

CT_LOG='Content-Type: text/x-log'
AC_LOG='Accept: text/x-log'

CT_JSON='Content-Type: application/json'
AC_JSON='Accept: application/json'

CT_DOT='Content-Type: text/vnd.graphviz'
AC_DOT='Accept: text/vnd.graphviz'

alias POST="curl -v -X POST"
alias POSTT="curl -v -X POST -H $AC_TURTLE"
alias POSTJ="curl -v -X POST -H $AC_JSON"
alias GET="curl -v -X GET"
alias GETT="GET -H '$AC_TURTLE'"
alias GETTURTLE="GET -H '$AC_TURTLE'"
alias GET_TURTLE="GET -H '$AC_TURTLE'"
alias GET_LOG="GET -H '$AC_LOG'"
alias GETLOG="GET -H '$AC_LOG'"
alias GET_DOT="GET -H '$AC_DOT'"
alias GETDOT="GET -H '$AC_DOT'"
alias GETL="GET -H '$AC_LOG'"
alias GET_JSON="GET -H '$AC_JSON'"
alias GETJSON="GET -H '$AC_JSON'"
alias GETJ="GET -H '$AC_JSON'"
alias PUT="curl -v -X PUT"
alias DELETE="curl -v -X DELETE"
alias PATCH="curl -v -X PATCH"
alias HEAD="curl -v -X HEAD"

# SRV_MAIN="http://localhost:9998"
# SRV="http://localhost:9998"
# SRV="http://localhost:8080/dm2e-ws/api"
SRV="http://localhost:9998/api"
