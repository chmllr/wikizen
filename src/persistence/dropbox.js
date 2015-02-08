"use strict";

var client = new Dropbox.Client({key: "d141dh0xbwt9bxh"});

var store, wikiRecord;

module.exports.load = () => {
    var wikiTable = store.getTable('wikis');
    var rows = wikiTable.query();
    if(rows.length == 0) return;
    wikiRecord = rows[0];
    var data = wikiRecord.getFields();
    return {
        freeID: data.freeID,
        root: JSON.parse(data.root),
        deltas: store.getTable("deltas").query().map(item => {
            var delta = item.getFields();
            if(delta.property == "page") delta.value = JSON.parse(delta.value);
            delta.persisted = true;
            return delta;
        })
    };
};

module.exports.save = wiki => {
    if(!wikiRecord) {
        var wikiTable = store.getTable('wikis');
        wikiTable.insert({
            freeID: wiki.freeID,
            root: JSON.stringify(wiki.root)
        });
        module.exports.load();
    }
    var deltaTable = store.getTable("deltas");
    wiki.deltas.filter(delta => !delta.persisted).forEach(delta => {
            deltaTable.insert({
                timestamp: delta.timestamp,
                pageID: delta.pageID,
                property: delta.property,
                value: delta.property == "page" ? JSON.stringify(delta.value) : delta.value
            });
            delta.persisted = true;
        }
    );
    wikiRecord.set("freeID", wiki.freeID);
};

module.exports.init = () => new Promise((resolve, reject) => {
    console.time("datastore API initialization");
    if (client.isAuthenticated()) resolve();
    client.authenticate({interactive: false}, error => {
        if (error) reject('Authentication error: ' + error.description);
        else if (client.isAuthenticated()) {
            var datastoreManager = client.getDatastoreManager();
            datastoreManager.openDefaultDatastore((error, datastore) => {
                store = datastore;
                console.timeEnd("datastore API initialization");
                if (error) reject('Error opening default datastore: ' + error);
                else resolve();
            });
        } else client.authenticate();
    });

});

module.exports.signOut = () => client.signOut();
