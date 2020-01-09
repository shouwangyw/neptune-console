#!/bin/bash

rm -rf neptune-console-ui/dist
rm -rf neptune-console-api/src/main/resources/static/*
rm -rf neptune-console-1.0

cd neptune-console-ui
npm run build
cp -r assets/images dist
cp -r assets/vendors dist
cd ..

mkdir neptune-console-api/src/main/resources/static/
cp -r neptune-console-ui/dist/ neptune-console-api/src/main/resources/static/

mvn clean install package -Dmaven.test.skip=true

mkdir neptune-console-1.0
cp -r neptune-console-api/target/neptune-console-api-1.0.0.jar neptune-console-1.0

cd neptune-console-1.0
mv neptune-console-api-1.0.0.jar neptune-console-dist-1.0.jar
cd ..