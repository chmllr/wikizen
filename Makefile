all:
	NODE_ENV=production make dev
	mv wikizen.js /tmp
	cat /tmp/wikizen.js | closure-compiler -W QUIET -O SIMPLE > wikizen.js

dev:
	browserify -t [ reactify --es6 --target es5 ] src/app.js > wikizen.js
	lessc -x style.less > style.css

server:
	http-server

tests:
	jsx --harmony src/ test/builds/
	nodeunit

deps:
	npm install diff-match-patch
	npm install reactify
	npm install react
	npm install marked
	npm install nodeunit
	npm install -g http-server
	npm install -g browserify
	npm install -g less

pub:
	git push
	make
	rm -rf app
	mkdir app
	cp -rf *.png LANDING.md VERSION README.md Demo.md index.html wikizen.js style.css app/
	git co gh-pages
	git pull
	mv app/* .
	git ca -m "new version"
	git push
	git co master
	rm -rf app
