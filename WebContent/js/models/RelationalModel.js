//Filename: RelationalModel.js

define([
	'jquery',
	'underscore',
	'logging',
	'backbone',
	'constants/RDFNS',
	'BaseModel',
	'backbone_relational'
], function($,
	_,
	logging,
	Backbone,
	NS,
	BaseModel
	) {

	var log = logging.getLogger("RelationalModel.js");

	var theDefaults = {};

	return Backbone.RelationalModel.extend({

		getQN : function(qname) {
			var url = NS.getQN(qname);
			if (!url)
				throw "Unknown QName " + qname;
			return this.get(url);
		},
		setQN : function(qname,
			val) {
			var url = NS.getQN(qname);
			if (!url)
				throw "Unknown QName " + qname;
			return this.set(url, val);
		},

	});
});