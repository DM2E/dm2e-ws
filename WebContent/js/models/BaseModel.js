//Filename: BaseModel.js

define([
        'jquery', 'underscore', 'logging', 'backbone', 'constants/RDFNS'
], function($,
	_,
	logging,
	Backbone,
	NS
	) {

	var log = logging.getLogger("BaseModel.js");

	var theDefaults = {};

	return Backbone.Model.extend({

        absoluteURL : function() {
            var baseURL = (typeof this.url === 'function')
                ? this.url()
                : this.url;
            if (/^(https?|ftp):\/\/.*/.test(baseURL)) {
                return baseURL;
            }
            return window.location.protocol
                + "//"
                + window.location.host
                + (/^\/.*/.test(baseURL) ? "" : "/")
                + baseURL;
        },
		getQN : function(qname) {
			var url = NS.getQN(qname);
			if (! url) throw "Unknown QName " + qname;
			return this.get(url);
		},
		setQN : function(qname, val) {
			var url = NS.getQN(qname);
			if (! url) throw "Unknown QName " + qname;
			return this.set(url, val);
		},
	
	});
});