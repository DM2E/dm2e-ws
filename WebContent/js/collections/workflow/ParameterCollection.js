//Filename: ParameterCollection.js

define([
	'jquery',
	'underscore',
	'logging',
	'BaseCollection',
	'models/workflow/ParameterModel'
], function($,
	_,
	logging,
	BaseCollection,
	ParameterModel) {

	var log = logging.getLogger("ParameterCollection.js");

	return BaseCollection.extend({

		model : ParameterModel,

	});
});
