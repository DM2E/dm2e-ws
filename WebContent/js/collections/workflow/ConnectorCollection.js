//Filename: ConnectorCollection.js

define([
	'jquery',
	'underscore',
	'logging',
	'BaseCollection',
	'models/workflow/ConnectorModel'
], function($,
	_,
	logging,
	BaseCollection,
	ConnectorModel) {

	var log = logging.getLogger("ConnectorCollection.js");

	var theDefaults = {};

	return BaseCollection.extend({

		model : ConnectorModel,

	});
});