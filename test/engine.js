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