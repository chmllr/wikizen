"use strict";

var sandbox = require('nodeunit').utils.sandbox;
var boxModule = {exports: {}};
var box = sandbox('test/builds/engine.js', {
    module: boxModule,
    exports: boxModule.exports,
    require: require,
    console: console
});
var engine = box.exports;

exports.instantiations = function (test) {
    var title = "Hello World";
    var body = "This is the body";
    var page = engine.createPage(title, body, 1);
    test.ok(page, "page object instantiated successfully");
    test.equal(page.title, title, "title check");
    test.equal(page.body, body, "body check");
    test.equal(page.id, 1, "id check");
    var wiki = engine.createWiki("Test Wiki", page, []);
    test.ok(wiki, "wiki object instantiated successfully");
    test.equal(wiki.name, "Test Wiki", "Wiki name check");
    test.equal(wiki.root, page, "root page check");
    test.equal(wiki.freeID, page.id + 1, "free id check");
    test.done();
};

exports.patching = function (test) {
    var line1 = "When a man lies, he murders some part of the world";
    var line2 = "These are the pale deaths which men miscall their lives";
    var diff = box.getPatch(line1, line2);
    test.ok(diff, "diff is ok");
    test.equal(box.applyPatch(line1, diff)[0], line2, "applied patch produces the expected result");
    var wrongOutput = box.applyPatch(line2, diff)[1];
    test.ok(wrongOutput.indexOf(false) >= 0, "wrong patch application returns nil");
    test.done();
};

var getTestWiki = function () {
    var wiki = engine.createWiki("Test Wiki", engine.createPage("Root Page", "This is the *page body*."));
    var id = engine.addPage(wiki, 0, "Nested Page 1", "The __content__ of _nested_ page 1");
    engine.addPage(wiki, id, "Nested Page 1_1", "This _is_ a leaf text");
    engine.addPage(wiki, id, "Nested Page 1_2", "This _is_ another leaf text");
    id = engine.addPage(wiki, 0, "Nested Page 2", "The __content__ of _nested_ page 2");
    engine.addPage(wiki, id, "Nested Page 2_1", "This _is_ a leaf text");
    return wiki;
};

var testWikiResult = {
    id: 0,
    title: "Root Page",
    body: "This is the *page body*.",
    children: [
        {
            id: 1,
            title: "Nested Page 1",
            body: "The __content__ of _nested_ page 1",
            children: [
                { id: 2, title: "Nested Page 1_1", body: "This _is_ a leaf text", children: []},
                { id: 3, title: "Nested Page 1_2", body: "This _is_ another leaf text", children: []}]
        },
        {
            id: 4,
            title: "Nested Page 2",
            body: "The __content__ of _nested_ page 2",
            children: [{id: 5, title: "Nested Page 2_1", body: "This _is_ a leaf text", children: []}]
        }
    ]
};

exports.wikiAssemblingAndRetrieving = function (test) {
    var testWiki = getTestWiki();
    var wiki = engine.assembleRuntimeWiki(testWiki);
    test.equals(JSON.stringify(testWiki), JSON.stringify(testWiki), "assebmling is not modifying");
    test.equals(wiki.name, "Test Wiki");
    test.deepEqual(wiki.root, testWikiResult, "assembling works");
    var page, pages = wiki.index.pages;
    page = pages[0];
    test.equals(page.title, "Root Page", "root page title check");
    page = pages[1];
    test.equals(page.title, "Nested Page 1", "1st child retrieval check");
    page = pages[4];
    test.equals(page.title, "Nested Page 2", "2nd child retrieval check");
    page = pages[2];
    test.equals(page.title, "Nested Page 1_1", "page retrieval check");
    page = pages[3];
    test.equals(page.title, "Nested Page 1_2", "page retrieval check");
    page = pages[5];
    test.equals(page.title, "Nested Page 2_1", "page retrieval check");
    test.done();
};

exports.pageModifications = function (test) {
    var wiki = getTestWiki();
    var wikiSer = JSON.stringify(wiki);
    engine.assembleRuntimeWiki(wiki);
    test.equals(JSON.stringify(wiki), wikiSer);
    var id = engine.addPage(wiki, 0, "Title", "Body");
    var rta = engine.assembleRuntimeWiki(wiki);
    test.equal(rta.index.pages[id].title, "Title", "insertion check");
    id = engine.addPage(wiki, id, "New Title", "Body");
    var rta2 = engine.assembleRuntimeWiki(wiki);
    test.deepEqual(rta2.index.pages[id], { id: id,
        title: "New Title", body: "Body", children: [] }, "insertion check");
    test.notDeepEqual(rta, rta2);
    engine.editPage(wiki, id, "New Title", "Hello world");
    rta = engine.assembleRuntimeWiki(wiki);
    test.deepEqual(rta.index.pages[id], { id: id,
        title: "New Title", body: "Hello world", children: [] }, "insertion check");
    engine.editPage(wiki, id, "Test Title", "Hello world");
    rta = engine.assembleRuntimeWiki(wiki);
    test.deepEqual(rta.index.pages[id], { id: id,
        title: "Test Title", body: "Hello world", children: [] }, "insertion check");
    test.done();
};

exports.deleting = function (test) {
    var wiki = getTestWiki();
    var id = engine.addPage(wiki, 2, "Title", "Body");
    var rta = engine.assembleRuntimeWiki(wiki);
    test.deepEqual(rta.index.parents[id].children,
        [ { id: wiki.freeID - 1, title: 'Title', body: 'Body', children: [] } ],
        "added page has correct structure");
    engine.deletePage(wiki, id);
    test.deepEqual(engine.assembleRuntimeWiki(getTestWiki()).root, testWikiResult, "the same tree as before");
    var newRootPage = {
        id: 0,
            title: "Root Page",
        body: "This is the *page body*.",
        children: []
    };
    engine.deletePage(wiki, 1);
    engine.deletePage(wiki, 2);
    engine.deletePage(wiki, 3);
    engine.deletePage(wiki, 4);
    engine.deletePage(wiki, 5);
    test.deepEqual(engine.assembleRuntimeWiki(wiki).root, newRootPage, "only root page remains");
    test.done()
};