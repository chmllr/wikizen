"use strict";

var jsdiff = require('diff');

/* DIFFING */

var getPatch = (A, B) => jsdiff.createPatch("WikiZen", A, B);

var applyPatch = (text, patch) => jsdiff.applyPatch(text, patch);

/* DELTA MANAGEMENT */

var DELTA = {
    PAGE: "page",
    BODY: "body",
    TITLE: "title"
};

var createDelta = (pageID, property, value) => ({
    pageID: pageID,
    property: property,
    value: value
});

var applySimpleDelta = (page, delta) => {
    if (delta.property == DELTA.TITLE)
        page.title = delta.value;
    else {
        var result = applyPatch(page.body, delta.value);
        if (result === false) {
            console.log("Patch", delta.value, "could not be applied to value", page.body);
            throw "Patch " + delta.value + " could not be applied to value " + page.body;
        }
        page.body = result;
    }
};

/** RUNTIME ARTIFACT COMPUTATION */

var clone = object => JSON.parse(JSON.stringify(object));

var initializeIndex = () => ({ ids: {}, parents: {} });

var assembleRuntimeWiki = wiki => {
    var runtimeObject = {
        name: wiki.name,
        root: clone(wiki.root),
        index: initializeIndex()
    };
    var root = runtimeObject.root;
    var index = runtimeObject.index;
    index.ids[root.id] = root;
    wiki.deltas.forEach(delta => {
        var pageID = delta.pageID, parent;
        if (delta.property == DELTA.PAGE) {
            var value = delta.value;
            if (value) { // add page
                value = clone(value);
                parent = index.ids[pageID];
                parent.children.push(value);
                index.ids[value.id] = value;
                index.parents[value.id] = parent;
            }
            else { // delete page
                parent = index.parents[pageID];
                parent.children = parent.children.filter(child => child.id != pageID);
                delete index.ids[pageID];
            }
        } else applySimpleDelta(index.ids[pageID], delta);
    });
    return runtimeObject;
};

/** PUBLIC METHODS */

module.exports.assembleRuntimeWiki = assembleRuntimeWiki;

module.exports.createWiki = (name, rootPage, deltas) => ({
    name: name,
    root: rootPage,
    deltas: deltas || [],
    freeID: rootPage.id + 1
});

module.exports.createPage = (id, title, body) => ({
    id: id,
    title: title,
    body: body,
    children: []
});

module.exports.addPage = (wiki, parentID, title, body) => {
    var id = wiki.freeID++;
    wiki.deltas.push(createDelta(parentID, DELTA.PAGE, module.exports.createPage(id, title, body)));
    return id;
};

module.exports.deletePage = (wiki, pageID) => {
    wiki.deltas.push(createDelta(pageID, DELTA.PAGE, null));
};

module.exports.editPage = (wiki, pageID, title, body) => {
    var page = assembleRuntimeWiki(wiki).index.ids[pageID];
    if(page.title != title)
        wiki.deltas.push(createDelta(pageID, DELTA.TITLE, title));
    if(page.body != body)
        wiki.deltas.push(createDelta(pageID, DELTA.BODY, getPatch(page.body, body)));
};