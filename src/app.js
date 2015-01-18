"use strict";

var React = require('react');
//var EventBus = require('eventbus');
var engine = require('./engine');
var router = require('./router');

var defaultRootPage = engine.createPage("Main Page", "Hello world!");
var wiki = engine.createWiki("Test Wiki", defaultRootPage);
var runtimeArtifact = engine.assembleRuntimeWiki(wiki);
var container = document.getElementById("app");

var Link = React.createClass({
    render: function () {
        var props = this.props;
        return <a href={"#" + props.to}>{props.label}</a>
    }
});

var Page = React.createClass({
    render: function () {
        var page = this.props.page;
        return <div>
            <h1>{page.title}</h1>
            <div>{page.body}</div>
            <hr/>
            <h2>{page.children.length == 0 ? null : "Nested Pages"}</h2>
            <ul>{page.children.map(child => <li><Link to={"page=" + child.id}
                label={child.title} /></li>)}</ul>
        </div>
    }
});

router.addHandler("page=:id", params =>
    React.render(<Page page={runtimeArtifact.index.ids[params.id]} />, container));

router.addHandler("edit=:id", params => console.log("page editor", params));
router.addHandler("delete=:id", params => console.log("page deleter", params));

self.onhashchange = router.dispatcher;
location.href = "#page=0";