"use strict";

var client = new Dropbox.Client({key: "rix5irz7khzazoi"});

var datastoreManager;

module.exports.load = () => {
    debugger
};

module.exports.save = wiki => {};

module.exports.init = () => new Promise((resolve, reject) => {
    client.authenticate({interactive: false}, error => {
        if (error) reject('Authentication error: ' + error);
        else if(client.isAuthenticated()) {
            datastoreManager = client.getDatastoreManager();
            datastoreManager.openDefaultDatastore((error, datastore) => {
                if (error) reject('Error opening default datastore: ' + error);
                else resolve();
            });
        } else client.authenticate();
    });

});


