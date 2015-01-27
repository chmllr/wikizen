"use strict";

var engine = require('./engine');

var stores = {
    local: require('./persistence/local')
};

function getFile(url) {
    var request = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
    if (request) {
        request.open("GET", url, false);
        request.send(null);
        return request.responseText;
    }
}

function State (persistence) {
    var store = stores[persistence] || stores.local, wiki, snapshot;
    var update = () => {
        snapshot = engine.assembleRuntimeWiki(wiki);
        store.save(wiki);
    };

    this.init = () => {
        return new Promise((resolver, rejecter) => {
            store.init().then(() => {
                wiki = store.load() || engine.createWiki("Wiki", engine.createPage("HOME", getFile("README.md")));
                snapshot = engine.assembleRuntimeWiki(wiki);
                resolver();
            }, rejecter);
        });
    };

    this.getPage = id => snapshot.index.pages[id];
    this.getParent = id => snapshot.index.parents[id];
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
}

module.exports = State;