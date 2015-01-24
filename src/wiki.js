"use strict";

var engine = require('./engine');

function getFile(url) {
    var request = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
    if (request) {
        request.open("GET", url, false);
        request.send(null);
        return request.responseText;
    }
}

function Wiki (wikiJSON) {
    var wiki = wikiJSON || engine.createWiki("Wiki",
            engine.createPage("Default Home Page", getFile("README.md")));
    var runtimeWiki;
    this.update = () => {
        runtimeWiki = engine.assembleRuntimeWiki(wiki);
        localStorage.setItem("wiki", JSON.stringify(wiki));
    };
    this.getPage = id => runtimeWiki.index.pages[id];
    this.getParent = id => runtimeWiki.index.parents[id];
    this.addPage = (id, title, body) => {
        var childID = engine.addPage(wiki, id, title, body);
        this.update();
        return childID;
    };
    this.editPage = (id, title, body) => {
        engine.editPage(wiki, id, title, body);
        this.update()
    };
    this.deletePage = id => {
        engine.deletePage(wiki, id);
        this.update();
    };
    this.undoDelta = () => {
        wiki.deltas.splice(wiki.deltas.length - 1, 1);
        this.update();
    };
    this.update();
}

module.exports = Wiki;