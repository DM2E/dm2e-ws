//Filename: filename

define([
	'jquery',
	'underscore',
	'logging',
	'BaseCollection',
	'models/',
], function($,
	_,
	logging,
	BaseCollection,
	TheModel
   ) {

	// var log = logging.getLogger("filename");


	return BaseCollection.extend({

	    model : TheModel,

    });
})
