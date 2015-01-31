"use strict";

module.exports.load = () => localStorage.getItem("wiki") && JSON.parse(localStorage.getItem("wiki"));

module.exports.save = wiki => localStorage.setItem("wiki", JSON.stringify(wiki));

module.exports.init = () => new Promise(resolver => resolver());

module.exports.signOut = () => localStorage.clear();