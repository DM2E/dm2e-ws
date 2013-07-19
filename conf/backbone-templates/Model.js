//Filename: filename

define([
	'jquery',
	'underscore',
	'logging',
	'constants/RDFNS',
	'BaseModel',
], function($,
	_,
	logging,
	NS,
	BaseModel
   ) {

    var log = logging.getLogger("filename");

    var theDefaults = {};

	return BaseModel.extend({

	    defaults : theDefaults,

	    initialize : function () {
	        log.debug("Initialized filename");
        }

    });
})
