pub:
	git push
	NODE_ENV=production gulp build-js --prod
	gulp build-css --prod
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
