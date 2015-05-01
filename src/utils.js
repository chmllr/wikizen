"use strict";

module.exports.getFile = url => {
    var request = window.XMLHttpRequest
        ? new XMLHttpRequest()
        : new ActiveXObject("Microsoft.XMLHTTP");
    if (request) {
        request.open("GET", url, false);
        request.send(null);
        return request.responseText;
    }
};