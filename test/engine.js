"use strict";

var engine = require('./builds/engine');
var _ = require("lodash");

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

var getTestWiki = function () {
    return _.clone({
        title: "Root Page",
        body: "This is the *page body*.",
        children: [
            {
                title: "Nested Page 1",
                body: "The __content__ of _nested_ page 1",
                children: [
                    { title: "Nested Page 1_1", body: "This _is_ a leaf text"},
                    { title: "Nested Page 1_2", body: "This _is_ another leaf text"}]
            },
            {
                title: "Nested Page 2",
                body: "The __content__ of _nested_ page 2",
                children: [{ title: "Nested Page 2_1", body: "This _is_ a leaf text"}]
            }
        ]
    });
};

exports.retrieving = function (test) {
    var page, testWiki = getTestWiki();
    page = engine.retrievePage(testWiki, []);
    test.equals(page, testWiki, "empty ref check");
    page = engine.retrievePage(testWiki, [0]);
    test.equals(page.title, "Nested Page 1", "1st child retrieval check");
    page = engine.retrievePage(testWiki, [1]);
    test.equals(page.title, "Nested Page 2", "2nd child retrieval check");
    page = engine.retrievePage(testWiki, [0, 0]);
    test.equals(page.title, "Nested Page 1_1", "page retrieval check");
    page = engine.retrievePage(testWiki, [0, 1]);
    test.equals(page.title, "Nested Page 1_2", "page retrieval check");
    page = engine.retrievePage(testWiki, [1, 0]);
    test.equals(page.title, "Nested Page 2_1", "page retrieval check");
    test.throws(function () { engine.retrievePage(testWiki, [2]) });
    test.throws(function () { engine.retrievePage(testWiki, [0, 0, 0]) });
    test.done();
};

exports.inserting = function (test) {
    var clone = getTestWiki();
    engine.insertPage(clone, [], engine.createPage("Title", "Body"));
    test.equal(engine.retrievePage(clone, [2]).title, "Title", "insertion check");
    clone = getTestWiki();
    engine.insertPage(clone, [0], engine.createPage("Title", "Body"));
    test.equal(engine.retrievePage(clone, [0, 2]).body, "Body", "insertion check");
    clone = getTestWiki();
    engine.insertPage(clone, [0, 0], engine.createPage("Title", "Body"));
    test.equal(engine.retrievePage(clone, [0, 0, 0]).title, "Title", "insertion check");
    test.done();
};

exports.deleting = function (test) {
    var clone = getTestWiki();
    test.throws(function (){ engine.deletePage(clone, []) });
    engine.insertPage(clone, [0, 0], engine.createPage("Title", "Body"));
    test.equal(engine.retrievePage(clone, [0, 0, 0]).title, "Title", "insertion check");
    test.deepEqual(engine.retrievePage(clone, [0, 0]).children, [engine.createPage("Title", "Body")]);
    engine.deletePage(clone, [0, 0, 0]);
    test.deepEqual(engine.retrievePage(clone, [0, 0]).children, []);
    test.throws(function (){ engine.deletePage(clone, [0,0,0]) });
    engine.deletePage(clone, [0, 0]);
    test.deepEqual(engine.retrievePage(clone, [0]).children, [{ title: "Nested Page 1_2",
        body: "This _is_ another leaf text"}]);
    engine.deletePage(clone, [0]);
    engine.deletePage(clone, [0, 0]);
    test.deepEqual(engine.retrievePage(clone, []).children, [{
        title: "Nested Page 2",
        body: "The __content__ of _nested_ page 2",
        children: []
    }]);
    test.throws(function (){ engine.deletePage(clone, [1]) });
    engine.deletePage(clone, [0]);
    test.deepEqual(engine.retrievePage(clone, []),
        {title: "Root Page", body: "This is the *page body*.", children: []});
    test.done()
};