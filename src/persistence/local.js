"use strict";

module.exports.load = () => localStorage.getItem("WikiZen") && JSON.parse(localStorage.getItem("WikiZen"));

module.exports.save = wiki => localStorage.setItem("WikiZen", JSON.stringify(wiki));

module.exports.init = () => new Promise(resolver => resolver());

module.exports.signOut = () => localStorage.clear();
