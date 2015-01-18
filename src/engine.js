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

var initializeIndex = () => ({ ids: {}, parents: {} });

module.exports.computeIndex = rootPage => {
    var index = initializeIndex();
    var walker = page => {
        index.ids[page.id] = page;
        if (page.children)
            page.children.forEach(child => {
                index.parents[child.id] = page;
                walker(child);
            });
    };
    walker(rootPage);
    return index;
};

var applySimpleDelta = (page, delta) => {
    if (delta.property == PAGE.TITLE)
        page.title = delta.value;
    else page.body = applyPatch(page.body, delta.value);
};

var assembleWiki = wiki => {
    var root = JSON.parse(JSON.stringify(wiki.root));
    var index = initializeIndex();
    index.ids[root.id] = root;
    wiki.deltas.forEach(delta => {
        var pageID = delta.pageID, parent;
        if (delta.property == DELTA.PAGE) {
            var value = delta.value;
            if (value) { // add page
                parent = index.ids[pageID];
                parent.children.push(value);
                index.parents[value.id] = parent;
            }
            else { // delete page
                parent = index.parents[pageID];
                parent.children = parent.children.filter(child => child.id != pageID);
            }
        } else applySimpleDelta(index[pageID], delta);
    });
    return root;
};

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