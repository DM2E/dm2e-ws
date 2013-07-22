//Filename: JobModel

define([
    'jquery',
    'underscore',
    'logging',
    'constants/RDFNS',
    'BaseModel',
//    'RelationalModel',
], function($,
    _,
    logging,
    NS,
    BaseModel
    ) {

    var log = logging.getLogger("models.job.JobModel");

    var theDefaults = {};

    return BaseModel.extend({

        defaults : theDefaults,

        initialize : function () {
            log.debug("Initialized JobModel");
        }

    });
})
