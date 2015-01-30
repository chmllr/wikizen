function Router () {
    var handlers = {};
    this.addHandler = (pattern, handler) => {
        var list = handlers[pattern] || [];
        list.push(handler);
        handlers[pattern] = list;
    };
    this.dispatcher = () =>
        Object.keys(handlers)
            .map(pattern => ({
                identifiers: (pattern.match(/:\w+/g) || []).map(x => x.replace(":", "")),
                matches: location.hash
                    .replace("#", "")
                    .match(new RegExp(pattern.replace(/:\w+/g, "(.+)"))),
                handlers: handlers[pattern]
            }))
            .filter(object => object.matches)
            .forEach(object =>
                object.handlers.forEach(handler =>
                    handler(object.identifiers.reduce((acc, param, i) => {
                            acc[param] = object.matches[i + 1];
                            return acc
                        }, {}))));
}

module.exports = new Router();