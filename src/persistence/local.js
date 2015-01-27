"use strict";

module.exports.load = () => localStorage.getItem("wiki") && JSON.parse(localStorage.getItem("wiki"));

module.exports.save = wiki => localStorage.setItem("wiki", JSON.stringify(wiki));