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

function State () {
    var wiki = localStorage.getItem("wiki") && JSON.parse(localStorage.getItem("wiki")) ||
        engine.createWiki("Wiki", engine.createPage("HOME", getFile("README.md")));
    var snapshot;
    var update = () => {
        snapshot = engine.assembleRuntimeWiki(wiki);
        localStorage.setItem("wiki", JSON.stringify(wiki));
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
    update();
}

module.exports = State;