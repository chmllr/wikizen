"use strict";

var React = require('react');
var EventBus = require('eventbus');
var engine = require('./engine');

var id = x => x;

console.log("hello",id("world"));