//Filename: ConnectorModel.js

define([
        'RelationalModel', // lib/backbone/backbone
        'logging', // logging
        'models/workflow/WorkflowModel',
        'models/workflow/WebserviceModel',
        'models/workflow/ParameterModel',
        'constants/RDFNS',
    ],
function (
      RelationalModel,
      logging,
      WorkflowModel,
      PositionModel,
      ParameterModel,
      NS) {

    var log = logging.getLogger("models.workflow.ConnectorModel");

    var theDefaults = {};
    theDefaults[NS.getQN("rdf:type")] = NS.getQN("omnom:ParameterConnector");

    var rdfRange = {
        "omnom:fromPosition" : "omnom:WorkflowPosition",
        "omnom:toPosition" : "omnom:WorkflowPosition",
        "omnom:fromWorkflow" : "omnom:Workflow",
        "omnom:toWorkflow" : "omnom:Workflow",
        "omnom:toParam" : "omnom:Parameter",
        "omnom:fromParam" : "omnom:Parameter",
    };

    // TODO fix the circular reference: Workflow-Connector-Workflow
    return RelationalModel.extend({

        defaults: theDefaults,

        toJSON : function() {
//            console.warn(this.getQN("omnom:fromWorkflow"));
            retJSON = {
                id : this.id,
                uuid : this.cid,
            };
            _.each([
                "omnom:fromWorkflow",
                "omnom:fromPosition",
                "omnom:fromParam",
                "omnom:toWorkflow",
                "omnom:toPosition",
                "omnom:toParam",
            ], function (qname) {
                if (!this.getQN(qname))
                    return;
//                console.error(this.getQN(qname));
                retJSON[NS.getQN(qname)] = {};
                retJSON[NS.getQN(qname)][NS.getQN("rdf:type")] = NS.getQN(rdfRange[qname]);
                retJSON[NS.getQN(qname)].id =  this.getQN(qname).id;

                // THIS IS THE HACK THAT MAKES IT AAAALL WORK
                if (this.getQN(qname).attributes)
                    retJSON[NS.getQN(qname)].uuid =  this.getQN(qname).attributes.cid;
            }, this)
            return retJSON;
        },

        relations: [
            {
                type: Backbone.HasOne,
                key: NS.getQN("omnom:fromWorkflow"),
                relatedModel: WorkflowModel,
                isAutoRelation: true,
                includeInJSON: [ "id", "uuid" ],
            },
            {
                type: Backbone.HasOne,
                key: NS.getQN("omnom:toWorkflow"),
                relatedModel: WorkflowModel,
                isAutoRelation: true,
                includeInJSON: [ "id", "uuid" ],
            },
            {
                type: Backbone.HasOne,
                key: NS.getQN("omnom:fromPosition"),
                relatedModel: PositionModel,
                includeInJSON: [ "id", "uuid" ],
            },
            {
                type: Backbone.HasOne,
                key: NS.getQN("omnom:toPosition"),
                relatedModel: PositionModel,
                includeInJSON: [ "id", "uuid" ],
            },
            {
                type: Backbone.HasOne,
                key: NS.getQN("omnom:fromParam"),
                relatedModel: ParameterModel,
                includeInJSON: [ "id", "uuid" ],
            },
            {
                type: Backbone.HasOne,
                key: NS.getQN("omnom:toParam"),
                relatedModel: ParameterModel,
                includeInJSON: [ "id", "uuid" ],
            }
        ]
    });
});
