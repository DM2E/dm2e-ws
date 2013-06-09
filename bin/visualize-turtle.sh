#!/bin/sh
rapper -o dot -i turtle -I 'http://EXAMPLE' - | dot -Tsvg -o output.svg
    #|grep -v 'label="rdf:type"'\
echo "done"
