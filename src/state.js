"use strict";

var engine = require('./engine');

var stores = {
    local: require('./persistence/local'),
    dropbox: require('./persistence/dropbox')
};

function getFile(url) {
    var request = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
    if (request) {
        request.open("GET", url, false);
        request.send(null);
        return request.responseText;
    }
}

function State (provider) {
    var store = stores[provider] || stores.dropbox, wiki, state;
    var update = () => {
        state = engine.assembleRuntimeWiki(wiki);
        store.save(wiki);
    };

    this.init = () => {
        return new Promise((resolve, reject) => {
            store.init().then(() => {
                wiki = store.load() || engine.createWiki(engine.createPage("HOME", getFile("README.md")));
                state = engine.assembleRuntimeWiki(wiki);
                if(wiki.freeID == 1) this.addPage(0, "Markdown Features", getFile("Demo.md"));
                resolve();
            }, reject);
        });
    };

    this.getPage = id => state.index.pages[id];
    this.getParent = id => state.index.parents[id];
    this.addPage = (id, title, body) => {
        var childID = engine.addPage(wiki, id, title, body);
        update();
        return childID;
    };
    this.editPage = (id, title, body) => {
        engine.editPage(wiki, id, title, body);
        update()
    };
    this.deletePage = id => {
        engine.deletePage(wiki, id);
        update();
    };
    this.undoDelta = () => {
        wiki.deltas.splice(wiki.deltas.length - 1, 1);
        update();
    };
    this.signOut = () => store.signOut();
    this.getProvider = () => provider;
}

module.exports = State;