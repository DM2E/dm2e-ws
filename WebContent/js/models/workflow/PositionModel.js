//Filename: PositionModel.js

define([ 'jquery', 
'underscore',
'BaseModel',
'RelationalModel', 
'logging', 
'constants/RDFNS',
'models/workflow/WebserviceModel',
'models/workflow/WorkflowModel'
], function($,
	_,
	BaseModel,
	RelationalModel,
	logging,
	NS,
	WebserviceModel,
	WorkflowModel
	) {

	var log = logging.getLogger("models.workflow.PositionModel");

	return RelationalModel.extend({

        initialize : function() {
            this.set("uuid", this.cid);
        },
		
		relations: [
            {
				type : Backbone.HasOne,
				key : NS.getQN("omnom:webservice"),
				relatedModel : WebserviceModel,
                includeInJSON : ["id", "uuid"],
			},
////            {
////				type : Backbone.HasOne,
////				key : NS.getQN("omnom:inWorkflow"),
////				relatedModel : WorkflowModel,
////			},
		],

	});
});