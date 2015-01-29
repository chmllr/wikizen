"use strict";

var React = require('react');
var State = require('./state');
var Router = require('./router');
var marked = require('marked');

var runtime = new State(location.search.replace("?", ""));
var renderComponent = component => React.render(component, document.body);
var openPage = id => {
    location.hash = "#page=" + id;
    localStorage.openedPage = id;
};
var editPage = id => location.hash = "#edit=" + id;
var addPage = id => location.hash = "#add=" + id;
var deletePage = id => location.hash = "#delete=" + id;
self.onhashchange = Router.dispatcher;
var keyMapping = {
    69: "edit",
    78: "new",
    68: "delete",
    37: "back",
    27: "escape",
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

var Breadcrumb = React.createClass({
    getPath: function (pageID) {
        var parent = runtime.getParent(pageID);
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
        return <nav className="Breadcrumb">{path}</nav>;
    }
});

var Sidebar = React.createClass({
    render: function () {
        var page = this.props;
        var id = page.id;
        var children = page.children;
        var isRoot = id == 0;
        return <aside>
            {isRoot ? null : <button className="BackButton" onClick={() => openPage(runtime.getParent(id).id)}>&#8701; Back</button>}
            {isRoot ? null : <div className="separator"></div>}
            <button className="prime" onClick={() => addPage(id)}>Add Page</button>
            <button onClick={() => editPage(id)}>Edit Page</button>
            {isRoot ? null : <button onClick={() => deletePage(id)}>Delete Page</button>}
            <div className="separator"></div>
            {children.length == 0
                ? null
                : <div>Nested Pages:<ol>{page.children.map(child =>
                    <li><Link to="page" param={child.id} label={child.title} /></li>)}</ol></div>}
            <div className="filler"></div>
            <footer>Powered by WikiZen.</footer>
        </aside>
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
                    editPage(page.id);
                    break;
                case "new":
                    addPage(page.id);
                    break;
                case "delete":
                    deletePage(page.id);
                    break;
                case "back":
                    var parent = runtime.getParent(page.id);
                    if(parent) openPage(parent.id);
                    break;
                default:
                    var child = page.children[code-1];
                    if(child) openPage(child.id);
            }
        };
    },
    render: function () {
        var page = this.props;
        return <div className="Page">
            <Sidebar {...page}/>
            <main>
                <Breadcrumb {...page} />
                <article dangerouslySetInnerHTML={{__html: marked(page.body || "")}}></article>
            </main>
        </div>
    }
});

var EditingForm = React.createClass({
    getInitialState: function () {
        var props = this.props;
        return props.mode == "EDIT" ? runtime.getPage(props.pageID) : {};
    },
    applyChanges: function () {
        var pageID;
        var props = this.props;
        if (props.mode == "ADD")
            pageID = runtime.addPage(props.pageID,
                this.refs.title.getDOMNode().value,
                this.refs.body.getDOMNode().value);
        else {
            pageID = props.pageID;
            var state = this.state;
            runtime.editPage(pageID, state.title, state.body);
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
            var props = this.props;
            var id = props.pageID;
            var code = keyMapping[event.keyCode];
            switch (code) {
                case "escape":
                    var page = props.mode == "EDIT" ? runtime.getPage(id) : runtime.getParent(id);
                    openPage(page && page.id || 0);
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
            <main className="Scrollable">
                <article className="Main" dangerouslySetInnerHTML={{__html: marked(state.body || "")}}></article>
            </main>
            <div className="ButtonBar">
                <button onClick={() => window.history.back()}>Cancel</button>
                <button onClick={this.applyChanges}>
                {this.props.mode == "EDIT" ? "Save Page" : "Create New Page"}
                </button>
            </div>
        </div>
    }
});

Router.addHandler("page=:id", params =>
    renderComponent(<Page {...runtime.getPage(params.id)} />));

Router.addHandler("add=:id", params =>
    renderComponent(<EditingForm mode="ADD" pageID={params.id} />));

Router.addHandler("edit=:id", params =>
    renderComponent(<EditingForm mode="EDIT" pageID={params.id} />));

Router.addHandler("delete=:id", params => {
    var response = confirm("Are you sure?");
    if (response) {
        var parent = runtime.getParent(params.id);
        if(parent) {
            runtime.deletePage(params.id);
            openPage(parent.id);
        }
    }
});

runtime.init().then(() => {
        if (location.hash) self.onhashchange();
        else openPage(localStorage.openedPage || 0);
    },
    console.error
);