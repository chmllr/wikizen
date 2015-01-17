"use strict";

var DiffMatchPatch = require('diff-match-patch');
var dmp = new DiffMatchPatch();
var _ = require('lodash');

var applyPatch = (patch, text) => {
    var result = dmp.patch_apply(dmp.patch_fromText(patch), text);
    var status = result[1];
    if(_.every(status)) return result[0];
    console.error("Patch couldn't be applied");
};

var computeIndex = wiki => {
    var index = {};
    var walker = page => {
        index.ids[page.id] = page.id;
        if (page.children)
            page.children.forEach(walker);
    };
    walker(wiki.root);
    return index;
};

var retrievePage = (wiki, id) => {
    var walker = page => {
        if(id == page.id) return page;
        for(var i in page.children) {
            var result = walker(page.children[i]);
            if(result) {
                // ...
            }
        }
    }
};

var insertPage = (wiki, id, title, body) => {
    var child = {
        id: ++wiki.id,
        title: title,
        body: body
    };
    page.children.push(child);
};

var deletePage = (wiki, id) => {
    var index = computeIndex(wiki);
    let parent = index.parents[id];
    parent.children = parent.children.filter(child => child.id != id);
};

module.exports.applyDelta = (root, delta) => {
    var page = retrievePage(root, delta, ref);
    switch (delta.property) {
        case "title":
            page.title = delta.payload;
            break;
        case "body":
            page.body = applyPatch(delta.payload, page.body);
            break;
        case "page":
            _.assign(page, delta.payload);
            break;
        default:
            throw "Corrupted delta property: " + delta.property;
    }
};

module.exports.getPatch = (A, B) => dmp.patch_toText(dmp.patch_make(A, B));
module.exports.createWiki = (rootPage, deltas) => ({
    root: rootPage,
    freeId: 0,
    deltas: deltas
});
module.exports.deletePage = deletePage;
module.exports.insertPage = insertPage;
module.exports.computeIndex = computeIndex;
module.exports.applyPatch = applyPatch;
