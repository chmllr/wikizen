"use strict";

var React = require('react');
var Wiki = require('./wiki');
var Router = require('./router');
var marked = require('marked');

var wiki = new Wiki(localStorage.getItem("wiki") && JSON.parse(localStorage.getItem("wiki")));
var renderComponent = component => React.render(component, document.body);
var openPage = id => location.hash = "#page=" + id;
self.onhashchange = Router.dispatcher;
var keyMapping = {
    69: "edit",
    78: "new",
    68: "delete",
    37: "back",
    29: "escape",
    48: "home",
    49: 1,
    50: 2,
    51: 3,
    52: 4,
    53: 5,
    54: 6,
    55: 7,
    56: 8,
    57: 9
};

var Link = React.createClass({
    render: function () {
        var props = this.props;
        var param = props.param;
        return <a className={props.className}
            href={"#" + props.to + (param != undefined ? "=" + param : "")}>{props.label}</a>
    }
});

var Header = React.createClass({
    getPath: function (pageID) {
        var parent = wiki.getParent(pageID);
        if(!parent) return [];
        var node = <Link to="page" param={parent.id} label={parent.title} />;
        var rest = this.getPath(parent.id);
        rest.push(node);
        rest.push(<span>&nbsp;&gt;&nbsp;</span>);
        return rest;
    },
    render: function () {
        var props = this.props;
        var path = this.getPath(props.id);
        path.push(props.title);
        return <header>
            <nav className="Breadcrumb">{path}</nav>
            <div className="links">
                <Link to="add" param={props.id} label="New Page" className="prime" /> &middot;&nbsp;
                <Link to="edit" param={props.id} label="Edit Page" /> &middot;&nbsp;
                <Link to="delete" param={props.id} label="Delete Page" />
            </div>
        </header>
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
    componentDidMount: function () {
        this.refs.title.getDOMNode().focus();
        document.onkeyup = event => {
            var page = this.props;
            var code = keyMapping[event.keyCode];
            switch (code) {
                case "escape":
                    var parent = wiki.getParent(page.id);
                    if(parent) openPage(parent.id);
                    break;
                default:
                    break;
            }
        };
    },
    render: function () {
        var state = this.state;
        return <div className="EditingForm">
            <input className="TitleInput" ref="title" type="text" placeholder="Title" value={state.title}
                onChange={event => this.handleChange("title", event.target.value)}/>
            <textarea className="BodyInput" ref="body" value={state.body}
                onChange={event => this.handleChange("body", event.target.value)}></textarea>
            <div className="Scrollable">
                <article className="Main" dangerouslySetInnerHTML={{__html: marked(state.body || "")}}></article>
            </div>
            <div className="ButtonBar">
                <button onClick={() => window.history.back()}>Cancel</button>
                <button onClick={this.applyChanges}>
                {this.props.mode == "EDIT" ? "Save Page" : "Create New Page"}
                </button>
            </div>
        </div>
    }
});

var Footer = React.createClass({
    render: function () {
        return <footer>
            Powered by WikiZen.
        </footer>
    }
});

var Page = React.createClass({
    componentDidMount: function () {
        document.onkeyup = event => {
            var page = this.props;
            var code = keyMapping[event.keyCode];
            switch (code) {
                case "home":
                    openPage(0);
                    break;
                case "edit":
                    location.hash = "#edit=" + page.id;
                    break;
                case "new":
                    location.hash = "#add=" + page.id;
                    break;
                case "delete":
                    location.hash = "#delete=" + page.id;
                    break;
                case "back":
                    var parent = wiki.getParent(page.id);
                    if(parent) openPage(parent.id);
                    break;
                default:
                    var child = page.children[code-1];
                    if(child) openPage(child.id);
            }
        };
    },
    render: function () {
        var page = this.props,
            children = page.children;
        var nestedPages = <div className="NestedPages Main">
            <h3>Nested Pages</h3>
            <ol>{children.map(child => <li><Link to={"page=" + child.id} label={child.title} /></li>)}</ol>
        </div>;
        return <div ref="page" className="Page">
            <Header {...page} />
            <article className="Main" dangerouslySetInnerHTML={{__html: marked(page.body || "")}}></article>
            {children.length == 0 ? null : nestedPages}
            <Footer />
        </div>
    }
});

Router.addHandler("page=:id", params =>
    renderComponent(<Page {...wiki.getPage(params.id)} />));

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
