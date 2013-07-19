//Filename: WorkflowModel.js

define([ 'jquery', // lib/jquery/jquery
'underscore', // lib/underscore/underscore
'RelationalModel', // lib/backbone/backbone
'logging', // logging
'constants/RDFNS',
'models/workflow/PositionModel',
'models/workflow/ParameterModel',
'models/workflow/ConnectorModel',
'collections/workflow/PositionCollection',
'collections/workflow/ParameterCollection',
'collections/workflow/ConnectorCollection',
], function($,
	_,
	RelationalModel,
	logging,
	NS,
	PositionModel,
	ParameterModel,
	ConnectorModel,
	PositionCollection,
	ParameterCollection,
	ConnectorCollection
	) {

	var log = logging.getLogger("WorkflowModel.js");

	var theDefaults = {};

	console.log(NS.OMNOM());

	var model =  RelationalModel.extend({

//		urlRoot : 'api/workflow',

		relations: [
            {
				type : Backbone.HasMany,
				key : NS.OMNOM().PROP_WORKFLOW_POSITION(),
				relatedModel : PositionModel,
                collectionType: PositionCollection,
//				reverseRelation : {
//					key : NS.getQN("omnom:inWorkflow"),
//					includeInJSON : true
//				}
			},
		    {
				type : Backbone.HasMany,
				key : NS.getQN("omnom:outputParam"),
				relatedModel : ParameterModel,
				collectionType: ParameterCollection,
//				reverseRelation : {
//					key : NS.getQN("omnom:inWorkflow"),
////					keySource : NS.getQN("omnom:inWorkflow"),
////					keyDestination : NS.getQN("omnom:outputParam"),
//					includeInJSON : true
//				}
            },
		    {
				type : Backbone.HasMany,
				key : NS.getQN("omnom:inputParam"),
				relatedModel : ParameterModel,
				collectionType: ParameterCollection,
//                reverseRelation : {
//                    key : NS.getQN("omnom:inWorkflow"),
//                    includeInJSON : true
//                }
            },
	        {
				type : Backbone.HasMany,
				key : NS.getQN("omnom:parameterConnector"),
				relatedModel : ConnectorModel,
                collectionType: ConnectorCollection,
//                includeInJSON: ["id"],
//				reverseRelation : {
//					key : NS.getQN("omnom:inWorkflow"),
//					includeInJSON : true
//				}
			},
		],

		dumpToJSON : function(){

			var asJSON = this.toJSON();
//			
			var replacer = function(k,v) {
				return v;
			}
			console.log(asJSON);
			return JSON.stringify(asJSON, replacer, 2);
		}

		// TODO

	});
	model.setup();
	return model;
});
