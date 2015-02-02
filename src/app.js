"use strict";

var React = require('react');
var State = require('./state');
var Router = require('./router');
var marked = require('marked');

var wikiZenURL = location.protocol + "//" + location.host + location.pathname;

var runtime;
var renderComponent = component => React.render(component, document.body);
var openPage = id => location.hash = "#page=" + id;
var editPage = id => location.hash = "#edit=" + id;
var addPage = id => location.hash = "#add=" + id;
var deletePage = id => location.hash = "#delete=" + id;
self.onhashchange = Router.dispatcher;
var keyMapping = {
    69: "edit",
    65: "add",
    68: "delete",
    37: "back",
    27: "escape",
    48: "home",
    83: "save",
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
    getInitialState: function () {
        return { menuHidden: true }
    },
    render: function () {
        var page = this.props;
        var id = page.id;
        var children = page.children;
        var isRoot = id == 0;
        return <aside>
            <button className="BackButton"
                disabled={isRoot}
                onClick={() => openPage(runtime.getParent(id).id)}>
                <span className="monospace">&lt; </span>Back</button>
            <div className="separator"></div>
            <button className="prime" onClick={() => addPage(id)}>Add Page</button>
            <button onClick={() => editPage(id)}>Edit Page</button>
            <button onClick={() => this.setState({ menuHidden: !this.state.menuHidden })}>
                <span className="monospace">{this.state.menuHidden ? "+" : "-"} </span>
                Menu</button>
            { this.state.menuHidden
                ?             <div className="separator"></div>
                : <ul className="Menu">
                {isRoot ? null : <li><Link to="delete" param={id} label="Delete Page" /></li>}
                <li><Link to="export" label="Export Wiki" /></li>
                <li><Link to="signout" label="Sign Out" /></li>
            </ul>}
            {children.length == 0
                ? null
                : <div>Nested Pages:<ol>{page.children.map(child =>
                <li><Link to="page" param={child.id} label={child.title} /></li>)}</ol></div>}
            <div className="filler"></div>
            <footer>Powered by <Link to="signout" label="WikiZen" /></footer>
        </aside>
    }
});

var Page = React.createClass({
    componentDidMount: function () {
        document.onkeydown = event => {
            var page = this.props;
            var code = keyMapping[event.keyCode];
            switch (code) {
                case "home":
                    openPage(0);
                    break;
                case "edit":
                    editPage(page.id);
                    break;
                case "add":
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
        document.onkeydown = event => {
            var props = this.props;
            var id = props.pageID;
            var code = keyMapping[event.keyCode];
            switch (code) {
                case "escape":
                    var page = props.mode == "EDIT" ? runtime.getPage(id) : runtime.getParent(id);
                    openPage(page && page.id || 0);
                    break;
                case "save":
                    if (event.metaKey) {
                        event.preventDefault();
                        this.applyChanges();
                    }
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

var LandingPage = React.createClass({
    render: function () {
        return <div className="CenteredBox Banner">
            <div className="Logo">
                <svg width="80" height="80" viewBox="0 0 60 50"><g>
                    <path d="M40.05,31.667c-0.093-0.258-0.127-0.51-0.135-0.76c-1.351-0.215-2.558-0.695-3.269-1.779
		c-0.313,0.105-0.635,0.195-0.961,0.27c0.195,0.491,0.377,0.904,0.531,1.186c0.365,0.67,0.309,1.017,0.088,1.188
		c-0.562,0.438-2-0.535-2.922-1.271c-0.302-0.242-0.749-0.556-1.222-0.833h-0.209c-0.387,0.414-0.814,0.79-1.284,1.109
		c0.068,0.073,0.136,0.146,0.215,0.224c2.667,2.667,3.75,4.167,0.583,6.75c-4.93,3.364-6.103,5.957-6.363,7.226h12.469
		c-0.17-2.398,0.25-5.02,0.81-5.643C41.3,36.083,41.217,34.917,40.05,31.667z"/>
                    <path d="M26.417,16.917c3.678,0,6.75,2.632,7.437,6.111C33.948,23.505,34,23.996,34,24.5c0,0.016-0.002,0.031-0.002,0.047
		c-0.01,1.521-0.469,2.935-1.252,4.12h0.632c0.669,0,1.32-0.073,1.95-0.207c0.307-0.065,0.607-0.146,0.902-0.24
		c-0.157-0.488-0.252-1.05-0.252-1.719c0-0.447,0.049-0.838,0.121-1.202c0.6-3.024,3.528-3.345,6.24-3.377
		c0.237-0.823,0.371-1.689,0.371-2.588c0-5.155-4.179-9.333-9.333-9.333h-6.822c-4.796,0-8.743,3.619-9.269,8.274
		c1.227-0.854,2.714-1.358,4.318-1.358H26.417z"/>
                    <path d="M49.667,45.975H37.686H25.049H10.333c-1.657,0-2.03,0.928-0.834,2.075l4.235,4.06c1.196,1.146,3.509,2.076,5.166,2.076
		h22.2c1.657,0,3.971-0.93,5.166-2.076l4.235-4.06C51.697,46.904,51.324,45.975,49.667,45.975z"/>
                    <path d="M21.605,31.083h4.812c1.343,0,2.59-0.404,3.632-1.094c0.152-0.101,0.297-0.21,0.44-0.322
		c0.199-0.157,0.391-0.322,0.571-0.5c0.006-0.006,0.013-0.013,0.02-0.019c0.152-0.153,0.297-0.314,0.434-0.481
		C32.442,27.532,33,26.081,33,24.5c0-3.636-2.947-6.583-6.583-6.583h-4.812c-1.679,0-3.207,0.634-4.37,1.669
		c-0.167,0.148-0.325,0.305-0.476,0.469c-0.16,0.174-0.31,0.356-0.451,0.547c-0.805,1.092-1.287,2.437-1.287,3.898
		C15.022,28.136,17.969,31.083,21.605,31.083z"/>
                    <path d="M48.68,22.918c-0.102,0-0.203,0-0.306,0h-4.812c-0.103,0-0.203,0-0.306,0c-0.067,0-0.133,0-0.198,0
		c-0.18,0-0.357,0.001-0.533,0.002c-0.18,0-0.358,0.002-0.533,0.005c-2.824,0.056-4.928,0.471-5.006,3.411
		c-0.002,0.056-0.008,0.106-0.008,0.164c0,0.53,0.069,0.974,0.188,1.359c0.051,0.166,0.111,0.321,0.184,0.463
		c0.078,0.155,0.168,0.297,0.268,0.426c0.541,0.695,1.396,1.031,2.459,1.189c0.931,0.139,2.014,0.147,3.181,0.147
		c0.101,0,0.203,0,0.306,0h4.812c0.103,0,0.203,0,0.306,0c3.493,0,6.278-0.05,6.278-3.583C54.958,22.967,52.174,22.918,48.68,22.918
		z"/>
                </g></svg>
                WikiZen
            </div>
            <p className="Lead">Simple Markdown Wiki in your Dropbox.</p>
            <div className="ButtonBar">
                <button onClick={() => signIn()}
                    className="LandingButton">Connect With Dropbox</button>
            &nbsp;
                <button onClick={() => signIn('local')}
                    className="LandingButton prime">Give It 5 Minutes</button>
            </div>
            <a href="https://github.com/chmllr/WikiZen">
                <img style={{position: "absolute", top: 0, right: 0, border: 0}}
                    src="https://s3.amazonaws.com/github/ribbons/forkme_right_red_aa0000.png"
                    alt="Fork me on GitHub" />
            </a>
        </div>;
    }
});

Router.addHandler("page=:id", params => {
    var id = params.id;
    var page = runtime.getPage(id);
    localStorage.openedPage = id;
    renderComponent( page
        ? <Page {...page} />
        : <div className="CenteredBox">Unknown page ID.</div> )
});

Router.addHandler("add=:id", params =>
    renderComponent(<EditingForm mode="ADD" pageID={params.id} />));

Router.addHandler("edit=:id", params =>
    renderComponent(<EditingForm mode="EDIT" pageID={params.id} />));

Router.addHandler("export", () =>
    renderComponent(<textarea className="Export">{JSON.stringify(runtime.getPage(0), null, 2)}</textarea>));

Router.addHandler("signout", () => {
    runtime.signOut();
    localStorage.removeItem("active");
    location.href = wikiZenURL;
});

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

self.signIn = mode => {
    runtime = new State(mode);
    if(!mode) localStorage.active = true;
    renderComponent(<div className="CenteredBox">Loading...</div>);
    runtime.init().then(() => {
            if (location.hash) self.onhashchange();
            else openPage(localStorage.openedPage || 0);
        },
        console.error
    );
};

if (localStorage.active) self.signIn();
else renderComponent(<LandingPage />);