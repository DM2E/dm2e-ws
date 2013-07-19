//Filename: BaseCollection.js

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

	return Backbone.Collection.extend({
		
		getQN : function(qname) {
			var url = NS.get(qname);
			if (url)
				return this[url].call();
		},
	
	});
});