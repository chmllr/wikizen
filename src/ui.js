var React = require('react');
var marked = require('marked');
var appState;

var render = component => React.render(component, document.body);
var openPage = id => location.hash = "#page=" + id;
var editPage = id => location.hash = "#edit=" + id;
var addPage = id => location.hash = "#add=" + id;
var deletePage = id => location.hash = "#delete=" + id;

module.exports.setAppState = state => appState = state;

module.exports.render = {
    PAGE: payload => render(<Page {...payload} />),
    LANDING_PAGE: () => render(<LandingPage />),
    EDIT_FORM: payload => render(<EditingForm {...payload} />),
    EXPORT_PAGE: payload => render(<textarea className="Export">{payload}</textarea>),
    MESSAGE: payload => render(<div className="CenteredBox">{payload}</div>)
};

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
        var parent = appState.getParent(pageID);
        if(!parent) return [];
        var node = <Link key={parent.id} to="page" param={parent.id} label={parent.title} />;
        var rest = this.getPath(parent.id);
        rest.push(node);
        rest.push(<span key={"span" + rest.length}>&nbsp;&gt;&nbsp;</span>);
        return rest;
    },
    render: function () {
        var props = this.props;
        var path = this.getPath(props.id);
        path.push(props.title);
        return <nav className="Breadcrumb">{path}</nav>;
    }
});

var NestedPages = React.createClass({
    render: function () {
        var pages = this.props.pages;
        return pages.length == 0 ? null : <div><b>Nested Pages</b>
            <ol>{pages.map((page, i) =>
                <li key={i}><Link to="page" param={page.id} label={page.title} /></li>)}
            </ol>
        </div>;
    }
});

var WarningBox = React.createClass({
    getInitialState: function () {
        return { display: this.props.display }
    },
    render: function () {
        if (this.state.display)
            return <div className="Warning">
                <h3>{this.props.title}</h3>
                {this.props.text}
                <br/>
                <button onClick={() => this.setState({ display: false })}>CLOSE</button>
            </div>;
        else return null;
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
                onClick={() => openPage(appState.getParent(id).id)}>
                <span className="monospace">&lt; </span>Back</button>
            <div className="separator"></div>
            <button className="prime" onClick={() => addPage(id)}>Add Page</button>
            <button onClick={() => editPage(id)}>Edit Page</button>
            <button onClick={() => this.setState({ menuHidden: !this.state.menuHidden })}>
                <span className="monospace">{this.state.menuHidden ? "+" : "-"} </span>
                Menu</button>
            { this.state.menuHidden
                ? <div className="separator"></div>
                : <ul className="Menu">
                {isRoot ? null : <li><Link to="delete" param={id} label="Delete Page" /></li>}
                <li><Link to="export" label="Export Wiki" /></li>
                <li><Link to="signout" label="Sign Out" /></li>
            </ul>}
            {children.length == 0 || !page.body ? null : <NestedPages pages={page.children} />}
            <div className="filler"></div>
            <WarningBox
                title="DEMO MODE!"
                text="This wiki persists in your browser only."
                display={appState.getProvider() == "local"} />
            <footer>Powered by <Link to="landing" label="WikiZen"/></footer>
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
                    var parent = appState.getParent(page.id);
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
        var body = page.body;
        return <div className="Page">
            <Sidebar {...page}/>
            <main>
                <Breadcrumb {...page} />
                {body
                    ? <article key={0} dangerouslySetInnerHTML={{__html: marked(body || "") }}></article>
                    : <article key={1}><h1>{page.title}</h1><NestedPages pages={page.children} /></article>}
            </main>
        </div>
    }
});

var EditingForm = React.createClass({
    getInitialState: function () {
        var props = this.props;
        return props.mode == "EDIT" ? appState.getPage(props.pageID) : {};
    },
    applyChanges: function () {
        var pageID;
        var props = this.props;
        if (props.mode == "ADD")
            pageID = appState.addPage(props.pageID,
                this.refs.title.getDOMNode().value,
                this.refs.body.getDOMNode().value);
        else {
            pageID = props.pageID;
            var state = this.state;
            appState.editPage(pageID, state.title, state.body);
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
                    var page = props.mode == "EDIT" ? appState.getPage(id) : appState.getParent(id);
                    openPage(page && page.id || 0);
                    break;
                case "save":
                    if (event.metaKey || event.ctrlKey && !event.altKey) {
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
        return <div id="LandingPage" className="CenteredBox">
            <div className="Logo">
                <img src="images/wikizen.svg" />
                WikiZen
            </div>
            <p className="Lead">Simple Markdown Wiki in your Dropbox.</p>
            <div className="ButtonBar">
                <button onClick={() => signIn()}>Connect With Dropbox</button>
            &nbsp;
                <button onClick={() => signIn('local')}
                    className="prime">Give It 5 Minutes</button>
            </div>
            <a href="https://github.com/chmllr/WikiZen">
                <img style={{position: "absolute", top: 0, right: 0, border: 0}}
                    src="https://s3.amazonaws.com/github/ribbons/forkme_right_red_aa0000.png"
                    alt="Fork me on GitHub" />
            </a>
        </div>;
    }
});