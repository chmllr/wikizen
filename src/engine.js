"use strict";

var DiffMatchPatch = require('diff-match-patch');

module.exports.createPage = (title, body, children) => ({
    title: title,
    body: body,
    children: children
});

module.exports.createWiki = (rootPage, deltas) => ({
    root: rootPage,
    deltas: deltas
});
