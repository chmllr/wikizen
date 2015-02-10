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
	git push
	make
	mkdir app
	cp -rf Roadmap.md VERSION images README.md Demo.md index.html wikizen.js style.css app/
	git co gh-pages
	git pull
	rm -rf images/
	mv app/* .
	git ca -m "new version"
	git push
	git co master
	rm -rf app
