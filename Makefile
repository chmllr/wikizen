all:
	NODE_ENV=production make dev

dev:
	browserify -t [ reactify --es6 --target es5 ] src/app.js > wikizen.js
	lessc style.less > style.css

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
