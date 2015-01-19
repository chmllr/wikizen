"use strict";

var React = require('react');
//var EventBus = require('eventbus');
var engine = require('./engine');
var router = require('./router');

var defaultRootPage = engine.createPage("Main Page", "Hello world!");
var wiki = engine.createWiki("Test Wiki", defaultRootPage);
var runtimeArtifact = engine.assembleRuntimeWiki(wiki);

var Link = React.createClass({
    render: function () {
        var props = this.props;
        var param = props.param;
        return <a
            href={"#" + props.to + (param != undefined ? "=" + param : "")}>{props.label}</a>
    }
});

var EditingForm = React.createClass({
    applyChanges: function () {
        console.log(this.props.mode, this.props.pageID);
    },
    render: function () {
        var props = this.props;
        var mode = props.mode;
        return <div className="EditingForm">
            <input ref="title" type="text" placeholder="Title"/>
            <textarea ref="body"></textarea>
            <button onClick={this.applyChanges}>
                {mode == "NEW_PAGE" ? "Create New Page" : "Save Page"}
            </button>
        </div>
    }
});

var Page = React.createClass({
    render: function () {
        var page = this.props.page;
        return <div className="Page">
            <h1>{page.title}</h1>
            <div>{page.body}</div>
            <hr/>
            <h2>{page.children.length == 0 ? null : "Nested Pages"}</h2>
            <ul>{page.children.map(child => <li><Link to={"page=" + child.id}
                label={child.title} /></li>)}</ul>
            <hr/>
            <Link to="add" param={page.id} label="New Page" />
        </div>
    }
});

var renderComponent = component => React.render(component, document.body);

router.addHandler("page=:id", params =>
    renderComponent(<Page page={runtimeArtifact.index.pages[params.id]} />));

router.addHandler("add=:id", params =>
    renderComponent(<EditingForm mode="NEW_PAGE" pageID={params.id} />));
router.addHandler("edit=:id", params => console.log("page editor", params));
router.addHandler("delete=:id", params => console.log("page deleter", params));

self.onhashchange = router.dispatcher;

if (location.hash) self.onhashchange();
else location.href = "#page=0";