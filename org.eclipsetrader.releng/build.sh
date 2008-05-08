#!/bin/sh

ECLIPSE_HOME=$HOME/eclipse-3.3

PRODUCT=$PWD/eclipsetrader.product
MAPS=$PWD/maps
TIMESTAMP=$(date +%Y%m%d%H%M)

BUILD_LOCATION=$HOME/eclipse.build
BUILD_TYPE=N
BUILD_ID=$TIMESTAMP

# proces command line arguments
while [ $# -gt 0 ]
do
	case "$1" in
		-buildType) BUILD_TYPE="$2"; shift;;
		-buildId) BUILD_ID="$2"; shift;;
		-fetchTag) FETCH_TAG="$2"; shift;;
		-baseLocation) ECLIPSE_HOME="$2"; shift;;
	esac
	shift
done

PDE_SCRIPTS=$ECLIPSE_HOME/plugins/org.eclipse.pde.build_3.3.1.v20070828/scripts/productBuild

/opt/java/sun-jdk1.5.0/bin/java -jar $ECLIPSE_HOME/plugins/org.eclipse.equinox.launcher_1.0.1.R33x_v20070828.jar -application org.eclipse.ant.core.antRunner \
    -buildfile $PDE_SCRIPTS/productBuild.xml \
    -DbaseLocation=$ECLIPSE_HOME \
    -DallElementsFile=$PWD/allElements.xml \
    -DcustomTargets=$PWD/customTargets.xml \
    -Dproduct=$PRODUCT \
    -Dmaps=$MAPS \
    -DbuildType=$BUILD_TYPE \
    -DbuildId=$BUILD_ID \
    -Dtimestamp=$TIMESTAMP
