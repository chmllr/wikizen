"use strict";

var State = require('./state');
var Router = require('./router');
var UI = require('./ui');

if (window.navigator.standalone) {
    document.body.style.paddingTop = "25px";
    document.body.style.height = (window.innerHeight - "25") + "px";
}

var wikiZenURL = location.protocol + "//" + location.host + location.pathname;
self.onhashchange = Router.dispatcher;
var appState;

var openPage = id => location.hash = "#page=" + id;

Router.addHandler("page=:id", params => {
    var id = params.id;
    var page = appState.getPage(id);
    localStorage.setItem(appState.getProvider() + ".openedPage", id);
    if(page) UI.render.PAGE(page);
    else UI.render.MESSAGE("Unknown page ID.");
});

Router.addHandler("add=:id", params => UI.render.EDIT_FORM({ mode: "ADD", pageID: params.id }));

Router.addHandler("edit=:id", params => UI.render.EDIT_FORM({ mode: "EDIT", pageID: params.id }));

Router.addHandler("export", () => UI.render.EXPORT_PAGE(appState.getPage(0)));

Router.addHandler("print=:id", params => UI.render.PRINT_PAGE(appState.getPage(params.id)));

Router.addHandler("landing", () => UI.render.LANDING_PAGE());

Router.addHandler("signout", () => {
    appState.signOut();
    localStorage.clear();
    location.href = wikiZenURL;
});

Router.addHandler("delete=:id", params => {
    var response = confirm("Are you sure?");
    if (response) {
        var parent = appState.getParent(params.id);
        if(parent) {
            appState.deletePage(params.id);
            openPage(parent.id);
        }
    }
});

self.signIn = provider => {
    if(provider != "local") localStorage.loggedIn = true;
    appState = new State(provider);
    UI.setAppState(appState);
    UI.render.MESSAGE("Connecting...");
    console.log("initializing app state with provider:", provider);
    appState.init().then(() => {
            location.hash = "#";
            openPage(localStorage.getItem(appState.getProvider() + ".openedPage") || 0)
        },
        console.error);
};

if (location.hash == "#landing" || !localStorage.loggedIn) UI.render.LANDING_PAGE();
else self.signIn();