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
    timestamp: new Date() - 0,
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

var initializeIndex = () => ({ pages: {}, parents: {} });

var assembleRuntimeWiki = wiki => {
    var runtimeObject = {
        name: wiki.name,
        root: clone(wiki.root),
        index: initializeIndex()
    };
    var root = runtimeObject.root;
    var index = runtimeObject.index;
    var pages = index.pages;
    pages[root.id] = root;
    wiki.deltas.forEach(delta => {
        var pageID = delta.pageID, parent;
        if (delta.property == DELTA.PAGE) {
            var value = delta.value;
            if (value) { // add page
                value = clone(value);
                parent = pages[pageID];
                parent.children.push(value);
                pages[value.id] = value;
                index.parents[value.id] = parent;
            }
            else { // delete page
                parent = index.parents[pageID];
                parent.children = parent.children.filter(child => child.id != pageID);
                delete pages[pageID];
            }
        } else applySimpleDelta(pages[pageID], delta);
    });
    return runtimeObject;
};

/** PUBLIC METHODS */

module.exports.assembleRuntimeWiki = assembleRuntimeWiki;

module.exports.createWiki = (name, rootPage, deltas) => {
    rootPage.id = 0;
    return {
        name: name,
        root: rootPage,
        deltas: deltas || [],
        freeID: 1
    }};

module.exports.createPage = (title, body, id) => ({
    id: id,
    title: title,
    body: body,
    children: []
});

module.exports.addPage = (wiki, parentID, title, body) => {
    var id = wiki.freeID++;
    wiki.deltas.push(createDelta(parentID, DELTA.PAGE, module.exports.createPage(title, body, id)));
    return id;
};

module.exports.deletePage = (wiki, pageID) => {
    wiki.deltas.push(createDelta(pageID, DELTA.PAGE, null));
};

module.exports.editPage = (wiki, pageID, title, body) => {
    var page = assembleRuntimeWiki(wiki).index.pages[pageID];
    if(page.title != title)
        wiki.deltas.push(createDelta(pageID, DELTA.TITLE, title));
    if(page.body != body)
        wiki.deltas.push(createDelta(pageID, DELTA.BODY, getPatch(page.body, body)));
};