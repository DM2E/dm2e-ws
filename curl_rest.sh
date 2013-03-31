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


alias POST="curl -v -X POST"
alias GET="curl -v -X GET"
alias GET_TURTLE="GET -H '$AC_TURTLE'"
alias GET_LOG="GET -H '$AC_LOG'"
alias PUT="curl -v -X PUT"
alias DELETE="curl -v -X DELETE"
alias PATCH="curl -v -X PATCH"
alias HEAD="curl -v -X HEAD"

SRV_MAIN="http://localhost:9998"
SRV_TASK="http://localhost:9110"
