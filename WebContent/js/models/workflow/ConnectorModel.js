//Filename: ConnectorModel.js

define(
    [
        'jquery', // lib/jquery/jquery
        'underscore', // lib/underscore/underscore
        'RelationalModel', // lib/backbone/backbone
        'logging', // logging
        'models/workflow/WorkflowModel',
        'RDFNS',

    ],
    function ($,
              _,
              RelationalModel,
              logging,
              WorkflowModel,
              WebserviceModel,
              NS,
        ) {

        var log = logging.getLogger("ConnectorModel.js");

        return RelationalModel.extend({

            relations:
                [
                    {
                        type: Backbone.HasOne,
                        key: NS.getQN("omnom:fromWorkflow"),
                        relatedModel: WorkflowModel,
                        includeInJSON:
                            [
                                "id"
                            ],
                    }
                ]

        });
    });
