#!/bin/bash
# mvn -q -o exec:java \
    # -Dlogging.properties.main-file="logging.console.properties" \
    # -Dlogging.properties.main=/home/kb/Dropbox/workspace/dm2e-ws/target/classes/logging.properties \
mvn -q -o exec:java \
    -Dlogback.configurationFile="logback-console.xml" \
    -Ddm2e-ws.test.properties_file="dm2e-ws.test.properties" \
    -Dexec.mainClass.override="eu.dm2e.utils.GuiConsole"
