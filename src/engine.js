"use strict";

var DiffMatchPatch = require('diff-match-patch');
var dmp = new DiffMatchPatch();

var getPatch = (A, B) => dmp.patch_toText(dmp.patch_make(A, B));
module.exports.getPatch = getPatch;
var applyPatch = (patch, text) => {
    var result = dmp.patch_apply(dmp.patch_fromText(patch), text);
    var status = result[1];
    if(_.every(status)) return result[0];
    console.error("Patch couldn't be applied");
};
module.exports.applyPatch = applyPatch;

var createPage = (id, title, body) => ({
    id: id,
    title: title,
    body: body
});
module.exports.createPage = createPage;

module.exports.createWiki = (name, rootPage, deltas) => ({
    name: name,
    root: rootPage,
    deltas: deltas,
    freeId: 0
});

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

module.exports.createDelta = createDelta;

module.exports.addPage = (wiki, parentID, title, body) => {
    wiki.deltas.push(createDelta(parentID,
        DELTA.PAGE,
        createPage(++wiki.id, title, body)));
};

module.exports.deletePage = (wiki, pageID) => {
    wiki.deltas.push(createDelta(pageID, DELTA.PAGE, null));
};

module.exports.changePage = (wiki, pageID, property, value) => {
    var runtimeWiki = assembleWiki(wiki);
    var page = computeIndex(runtimeWiki.root)[pageID];
    wiki.deltas.push(property == DELTA.BODY
        ? createDelta(pageID, DELTA.BODY, getPatch(page.body, value))
        : createDelta(pageID, DELTA.TITLE, value));
};

module.exports.computeIndex = rootPage => {
    var index = {
        ids: {},
        parents: {}
    };
    var walker = page => {
        index.ids[page.id] = page.id;
        if (page.children)
            page.children.forEach(child => {
                index.parents[child.id] = page;
                walker(child);
            });
    };
    walker(rootPage);
    return index;
};
