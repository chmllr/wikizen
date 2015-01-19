"use strict";

var React = require('react');
var engine = require('./engine');
var router = require('./router');

var defaultRootPage = engine.createPage("Main Page", "Hello world!");
var wiki = engine.createWiki("Test Wiki", defaultRootPage);
var runtimeArtifact;
var updateRuntime = () => runtimeArtifact = engine.assembleRuntimeWiki(wiki);
updateRuntime();


var Link = React.createClass({
    render: function () {
        var props = this.props;
        var param = props.param;
        return <a
            href={"#" + props.to + (param != undefined ? "=" + param : "")}>{props.label}</a>
    }
});

var EditingForm = React.createClass({
    getInitialState: function () {
        var props = this.props;
        return props.mode == "EDIT" ? runtimeArtifact.index.pages[props.pageID] : {};
    },
    applyChanges: function () {
        var pageID;
        var props = this.props;
        if (props.mode == "ADD")
            pageID = engine.addPage(wiki,
                props.pageID,
                this.refs.title.getDOMNode().value,
                this.refs.body.getDOMNode().value);
        else {
            pageID = props.pageID;
            var state = this.state;
            engine.editPage(wiki, pageID, state.title, state.body);
        }
        updateRuntime();
        openPage(pageID);
    },
    handleChange: function (property, value) {
        var state = {};
        state[property] = value;
        this.setState(state);
    },
    render: function () {
        var state = this.state;
        return <div className="EditingForm">
            <input ref="title" type="text" placeholder="Title" value={state.title}
                onChange={event => this.handleChange("title", event.target.value)}/>
            <textarea ref="body" value={state.body}
                onChange={event => this.handleChange("body", event.target.value)}></textarea>
            <button onClick={this.applyChanges}>
                {this.props.mode == "EDIT" ? "Save Page" : "Create New Page"}
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
            <Link to="add" param={page.id} label="New Page" /> &nbsp;
            <Link to="edit" param={page.id} label="Edit Page" /> &nbsp;
            <Link to="delete" param={page.id} label="Delete Page" />
        </div>
    }
});

var renderComponent = component => React.render(component, document.body);
var openPage = id => location.hash = "#page=" + id;

router.addHandler("page=:id", params =>
    renderComponent(<Page page={runtimeArtifact.index.pages[params.id]} />));

router.addHandler("add=:id", params =>
    renderComponent(<EditingForm mode="ADD" pageID={params.id} />));

router.addHandler("edit=:id", params =>
    renderComponent(<EditingForm mode="EDIT" pageID={params.id} />));

router.addHandler("delete=:id", params => {
    var response = confirm("Are you sure?");
    if (response) {
        var parentID = runtimeArtifact.index.parents[params.id].id;
        engine.deletePage(wiki, params.id);
        updateRuntime();
        openPage(parentID);
    }
});

self.onhashchange = router.dispatcher;

if (location.hash) self.onhashchange();
else openPage(0);