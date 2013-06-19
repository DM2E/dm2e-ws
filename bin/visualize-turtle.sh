#!/bin/sh
rapper -o dot -i turtle -I 'http://EXAMPLE' - | dot -Tsvg -o $1
    #|grep -v 'label="rdf:type"'\
echo "done"
