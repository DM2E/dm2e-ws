//Filename: ParameterAssignmentModel.js

define([
    'jquery',
    'underscore',
    'logging',
    'constants/RDFNS',
    'RelationalModel',
], function ($, _, logging, NS, RelationalModel) {

    var log = logging.getLogger("ParameterAssignmentModel");

    var theDefaults = {};
    theDefaults[NS.getQN("rdf:type")] = NS.getQN("omnom:ParameterAssignment");

    return RelationalModel.extend({

        defaults: theDefaults,

        initialize: function () {
            log.debug("Initialized ParameterAssignmentModel");
        }

    });
})
