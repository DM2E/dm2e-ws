//Filename: Webservice.js

define([
	'jquery',
	'underscore',
	'logging',
	'BaseModel',
	'RelationalModel',
	'models/workflow/ParameterModel',
	'constants/RDFNS'
], function($,
	_,
	logging,
	BaseModel,
	RelationalModel,
	ParameterModel,
	NS) {

	var log = logging.getLogger("Webservice.js");

	var theDefaults = {};
	theDefaults["rdf:type"] = NS.getQN("omnom:Webservice");
	// theDefaults[NS.getQN("omnom:inputParam")] = [];
	// theDefaults[NS.OMNOM().PROP_OUTPUT_PARAM()] = [];
	// // console.log(theDefaults);

	return RelationalModel.extend({

		defaults : theDefaults,

		toJSON : function() {
			var asJSON = _.clone(this.attributes);
			asJSON.uuid = this.cid;
			return asJSON;
		},
		relations : [ {
			type : Backbone.HasMany,
			key : NS.getQN("omnom:inputParam"),
			relatedModel : ParameterModel,
		},
		{
			type : Backbone.HasMany,
			key : NS.getQN("omnom:outputParam"),
			relatedModel : ParameterModel,
		} ],

	// initialize: function() {
	// console.log(RDFNS.OMNOM);
	// }
	});
});