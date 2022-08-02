#!/bin/bash
set -e

echo "Running release"
mvn -B --settings maven-settings.xml deploy -Dmaven.wagon.http.pool=false