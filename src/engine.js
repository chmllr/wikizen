"use strict";

var DiffMatchPatch = require('diff-match-patch');
var dmp = new DiffMatchPatch();
var _ = require('lodash');

module.exports.createPage = (title, body, children) => ({
    title: title,
    body: body,
    children: children
});

module.exports.createWiki = (rootPage, deltas) => ({
    root: rootPage,
    deltas: deltas
});

module.exports.getPatch = (A, B) => dmp.patch_toText(dmp.patch_make(A, B));
module.exports.applyPatch = (patch, text) => {
    var result = dmp.patch_apply(dmp.patch_fromText(patch), text);
    var status = result[1];
    if(_.every(status)) return result[0];
    console.error("Patch couldn't be applied");
};