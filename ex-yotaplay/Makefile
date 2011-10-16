all: iiPlay.crx

iiPlay.crx: manifest.json iiServiceApi.js iiPlayHook.js iiPlayExt.js when.js
	mkdir -p crx_src
	cp $^ crx_src
	./crxmake.sh crx_src debug.pem iiPlay
	rm -rf crx_src

%.js: %.coffee
	coffee -cp $? >$@

when.js: 
	curl https://raw.github.com/briancavalier/when.js/master/when.js > when.js

clean:
	rm -f *.js iiPlay.crx
	rm -rf crx_src

.PHONY: clean all
