#!/bin/sh

node -e "var fs=require('fs'),V='VERSION',v=fs.readFileSync(V,'utf8').split('.');v[2]++;fs.writeFile(V,v.join('.').trim())"
