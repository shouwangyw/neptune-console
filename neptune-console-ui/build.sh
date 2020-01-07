#!/bin/bash

rm -rf docker_build/dist

npm run build

mv dist docker_build/

cp -r assets/images docker_build/dist
cp -r assets/vendors docker_build/dist

