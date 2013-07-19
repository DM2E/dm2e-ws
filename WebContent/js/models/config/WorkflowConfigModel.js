//Filename: WorkflowConfigModel.js
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

    var moduleName="WorkflowConfigModel";
	var log = logging.getLogger("models.config.WorkflowConfigModel");

    var theDefaults = {};
    theDefaults[NS.getQN("rdf:type")] = NS.getQN("omnom:WorkflowConfig");

	return BaseModel.extend({

	    defaults : theDefaults,

	    initialize : function () {
            log.debug("Initialized " + moduleName + ".")
        }

    });
})
