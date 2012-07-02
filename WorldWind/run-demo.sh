#!/bin/sh
# Shell script for Running a WorldWind Demo

echo Running $1

mvn exec:exec -Dexec.executable=java -pl ww-extensions -Dexec.args="-Djava.library.debug=y -javaagent:../lib/mx-native-loader/mx-native-loader-1.2.1.jar='-l jogl,gluegen-rt,gdal -e jogl_awt' -Djava.library.path=tmplib -classpath %classpath $*"
