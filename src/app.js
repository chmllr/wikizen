"use strict";

var React = require('react');
var Wiki = require('./wiki');
var Router = require('./router');
var marked = require('marked');

var wiki = new Wiki(localStorage.getItem("wiki") && JSON.parse(localStorage.getItem("wiki")));
var renderComponent = component => React.render(component, document.body);
var openPage = id => location.hash = "#page=" + id;
self.onhashchange = Router.dispatcher;

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
        return props.mode == "EDIT" ? wiki.getPage(props.pageID) : {};
    },
    applyChanges: function () {
        var pageID;
        var props = this.props;
        if (props.mode == "ADD")
            pageID = wiki.addPage(props.pageID,
                this.refs.title.getDOMNode().value,
                this.refs.body.getDOMNode().value);
        else {
            pageID = props.pageID;
            var state = this.state;
            wiki.editPage(pageID, state.title, state.body);
        }
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
        var page = this.props.page,
            children = page.children;
        return <div className="Page">
            <code>{page.title}</code>
            <div dangerouslySetInnerHTML={{__html: marked(page.body)}}></div>
            <hr/>
            <h2>{children.length == 0 ? null : "Nested Pages"}</h2>
            <ul>{children.map(child => <li><Link to={"page=" + child.id} label={child.title} /></li>)}</ul>
            <hr/>
            <Link to="add" param={page.id} label="New Page" /> &nbsp;
            <Link to="edit" param={page.id} label="Edit Page" /> &nbsp;
            <Link to="delete" param={page.id} label="Delete Page" />
        </div>
    }
});

Router.addHandler("page=:id", params =>
    renderComponent(<Page page={wiki.getPage(params.id)} />));

Router.addHandler("add=:id", params =>
    renderComponent(<EditingForm mode="ADD" pageID={params.id} />));

Router.addHandler("edit=:id", params =>
    renderComponent(<EditingForm mode="EDIT" pageID={params.id} />));

Router.addHandler("delete=:id", params => {
    var response = confirm("Are you sure?");
    if (response) {
        var parentID = wiki.getParent(params.id).id;
        wiki.deletePage(params.id);
        openPage(parentID);
    }
});

if (location.hash) self.onhashchange();
else openPage(0);
