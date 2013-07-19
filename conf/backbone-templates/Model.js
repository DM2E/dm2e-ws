//Filename: filename

define([
	'jquery',
	'underscore',
	'logging',
	'BaseModel',
], function($,
	_,
	logging,
	BaseView
   ) {

	// var log = logging.getLogger("filename");

    var theDefaults = {};

	return BaseModel.extend({

	    defaults : theDefaults,

	    initialize : function () {
        }

    });
})
