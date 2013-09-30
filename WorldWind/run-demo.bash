#!/bin/bash

#
# Copyright (C) 2012 United States Government as represented by the Administrator of the
# National Aeronautics and Space Administration.
# All Rights Reserved.
#

#
# Run a WorldWind Demo
#
# $Id$
#

echo Running $1
mvn exec:exec -Dexec.executable=java -pl ww-extensions -Dexec.args="-Djava.library.debug=y -javaagent:../lib/mx-native-loader/mx-native-loader-1.2.1.jar='-l jogl,gluegen-rt,gdal -e jogl_awt' -Djava.library.path=tmplib -classpath %classpath $*"
