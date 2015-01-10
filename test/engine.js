"use strict";

var engine = require('./builds/engine');

exports.instantiations = function (test) {
    var title = "Hello World";
    var body = "This is the body";
    var page = engine.createPage(title, body, []);
    test.ok(page, "page object instantiated successfully");
    test.equal(page.title, title, "title check");
    test.equal(page.body, body, "body check");
    var wiki = engine.createWiki(page, []);
    test.ok(wiki, "wiki object instantiated successfully");
    test.equal(wiki.root, page, "root page check");
    test.done();
};

exports.patching = function (test) {
    var line1 = "When a man lies, he murders some part of the world";
    var line2 = "These are the pale deaths which men miscall their lives";
    var diff = engine.getPatch(line1, line2);
    test.ok(diff, "diff is ok");
    test.equal(engine.applyPatch(diff, line1), line2);
    var wrongOutput = engine.applyPatch(diff, line2);
    test.equal(wrongOutput, undefined, "wrong patch application returns nil");
    test.done();
};