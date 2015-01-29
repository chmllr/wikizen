all:
	NODE_ENV=production make dev
	mv wikizen.js /tmp
	cat /tmp/wikizen.js | closure-compiler -W QUIET -O SIMPLE > wikizen.js

dev:
	browserify -t [ reactify --es6 --target es5 ] src/app.js > wikizen.js
	lessc -x style.less > style.css

tests:
	jsx --harmony src/ test/builds/
	nodeunit

deps:
	npm install diff-match-patch
	npm install reactify
	npm install react
	npm install marked
	npm install nodeunit

pub:
	make
	cp README.md index.html wikizen.js style.css ~/Dropbox/Public/wiki/
