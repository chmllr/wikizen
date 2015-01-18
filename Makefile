all:
	browserify -t [ reactify --es6 --target es5 ] src/app.js > wikizen.js

tests:
	jsx --harmony src/ test/builds/
	nodeunit

deps:
	npm install diff
	npm install reactify
	npm install react
