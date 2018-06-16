pub:
	git push
	NODE_ENV=production gulp build-js --prod
	gulp build-css --prod
	rm -rf app
	mkdir app
	cp -rf LANDING.md VERSION README.md Demo.md index.html wikizen.js style.css app/
	git checkout gh-pages
	git pull
	mv app/* .
	git commit -a -m "new version"
	git push
	git checkout master
	rm -rf app
