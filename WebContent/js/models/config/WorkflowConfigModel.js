//Filename: WorkflowConfigModel.js
define([
	'jquery',
	'underscore',
	'logging',
    'constants/RDFNS',
	'RelationalModel',
    'models/config/ParameterAssignmentModel',
], function($,
	_,
	logging,
    NS,
	RelationalModel,
    ParameterAssignmentModel
   ) {

    var moduleName="WorkflowConfigModel";
	var log = logging.getLogger("models.config.WorkflowConfigModel");

    var theDefaults = {};
    theDefaults[NS.getQN("rdf:type")] = NS.getQN("omnom:WorkflowConfig");

	return RelationalModel.extend({

	    defaults : theDefaults,

        relations : [
            {
                type : Backbone.HasMany,
                key : NS.getQN("omnom:assignment"),
                relatedModel : ParameterAssignmentModel
//                collectionType: PositionCollection,

            }
        ],

	    initialize : function () {
            log.debug("Initialized " + moduleName + ".")
        }

    });
})
