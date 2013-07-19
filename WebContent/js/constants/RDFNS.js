define([
    'jquery',
    'logging',
    'util/constantLoader'
], function ($, logging, loadConstants) {

    var rdfns = {};

//	var rdfnsRaw = loadConstants.from('api/constants/rdfns');
//	_.each(rdfnsRaw, function(urls,namespace) {
//		var base = urls.BASE;
//		var urlsNameReverse = {};
//		_.each(urls, function(url, name){
//			var pathSegment = url.replace(base, "");
//			urls[name] = function() { return pathSegment; };
//			urlsNameReverse[pathSegment] = name;
//		});
//		urls.inverted = urlsNameReverse;
//		urls.qname = function(path) {
//			var constantName = this.inverted[path];
//			if (! constantName) {
//				console.log(this.inverted);
//				throw "Unknown path " + path;
//			}
//			return this[constantName].call();
//		}
//		rdfnsElem = function() {
//			return urls;
//		};
//		rdfns[namespace] = rdfnsElem;
//
////		var rdfnsNselem = {}
////		rdfnsNselem.urls = urls;
////		console.log(rdfnsNselem);
////		rdfnsNselem.get = function(name) {
////			if (this.urls[name]) {
////				return this.urls[name];
////			}
////			throw "Unknown path " + name + " in " + namespace;
////		};
////		rdfns[namespace.toLowerCase()] = rdfnsNselem;
//	});
    var rdfnsRaw = loadConstants.from('api/constants/rdfns');
    _.each(rdfnsRaw, function (urls, namespace) {
        var urlsNameReverse = {};
        _.each(urls, function (url, name) {
            // OMNOM().PROP_FOO()
            urls[name] = function () {
                return url;
            };
            var pathSegment = url.replace(urls.BASE(), "");
            urlsNameReverse[pathSegment] = name;
        });
        urls.inverted = urlsNameReverse;
        urls.getQN = function (path) {
            var constantName = this.inverted[path];
            if (!constantName) {
                console.log(this.inverted);
                throw "Unknown path " + path;
            }
            return this[constantName].call();
        }
        var rdfnsElem = function () {
            return urls;
        };
        rdfns[namespace] = rdfnsElem;

    });

    rdfns.getQN = function (qname) {
        var tokens = qname.split(":");
        var namespace = tokens[0].toUpperCase();
        var path = tokens[1];
        if (!this[namespace]) {
            throw "Unknown namespace prefix " + namespace;
        }
        return this[namespace].call().getQN(path);
    };

    rdfns.rdf_attr = function (arg_obj, qname) {
        var obj = arg_obj || {};
        return obj[rdfns.getQN(qname)];
    };
//	console.log(rdfns.OMNOM().PROP_FILE_RETRIEVAL_URI());
//	console.log(rdfns.OMNOM().qname("fileRetrievalURI"));
//	console.log(rdfns.get("omnom:fileRetrievalURI"));
    return rdfns;
});