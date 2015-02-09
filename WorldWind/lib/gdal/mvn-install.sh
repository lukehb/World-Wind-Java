#!/bin/sh

mvn install:install-file -Dfile=gdal-1.7.2.jar -DpomFile=gdal-1.7.2.pom
mvn install:install-file -Dfile=gdal-windows-i586-1.7.2.jar -DpomFile=gdal-windows-i586-1.7.2.pom
mvn install:install-file -Dfile=gdal-windows-amd64-1.7.2.jar -DpomFile=gdal-windows-amd64-1.7.2.pom
mvn install:install-file -Dfile=gdal-linux-i586-1.7.2.jar -DpomFile=gdal-linux-i586-1.7.2.pom
mvn install:install-file -Dfile=gdal-linux-amd64-1.7.2.jar -DpomFile=gdal-linux-amd64-1.7.2.pom
mvn install:install-file -Dfile=gdal-macosx-universal-1.7.2.jar -DpomFile=gdal-macosx-universal-1.7.2.pom
