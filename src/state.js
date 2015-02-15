"use strict";

var engine = require('./engine');
var utils = require('./utils');

var stores = {
    local: require('./persistence/local'),
    dropbox: require('./persistence/dropbox')
};

function State (provider) {
    var store = stores[provider] || stores.dropbox, wiki, state;
    var update = () => {
        state = engine.assembleRuntimeWiki(wiki);
        store.save(wiki);
    };
    var version = utils.getFile("VERSION");

    var search = (term, page) => {
        var regExp = new RegExp(term, "i");
        var results = [];
        if (page.title.match(regExp) || page.body.match(regExp))
            results.push({ id: page.id, title: page.title });
        page.children.forEach(child => results = results.concat(search(term, child)));
        return results;
    };

    this.init = () => {
        return new Promise((resolve, reject) => {
            store.init().then(() => {
                wiki = store.load() || engine.createWiki(engine.createPage("HOME", utils.getFile("README.md")));
                state = engine.assembleRuntimeWiki(wiki);
                if(wiki.freeID == 1) this.addPage(0, "Markdown Features", utils.getFile("Demo.md"));
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
    this.search = term => {
        return search(term, state.root);
    };
    this.signOut = () => store.signOut();
    this.getProvider = () => provider;
    this.getVersion = () => version;
}

module.exports = State;