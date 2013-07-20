//Filename: UserModel.js

define([
	'jquery',
	'underscore',
	'BaseModel',
	'RelationalModel',
	'logging',
	'constants/RDFNS',
	'models/workflow/WebserviceModel',
	'collections/workflow/WebserviceCollection'
], function($,
	_,
	BaseModel,
	RelationalModel,
	logging,
	NS,
	WebserviceModel,
	WebserviceCollection) {

	var log = logging.getLogger("models.user.UserModel");

	var theDefaults = {
		fileServices : [
            'api/file',
            'api/mint-file'
        ],
		configService : 'api/config',
		workflowService : 'api/workflow',
		serviceList : [
			'api/service/xslt',
			'api/service/xslt-zip',
			'api/publish',
			'api/service/demo'
		]
	};
//	theDefaults[NS.getQN("omnom:webservice")] = new WebserviceCollection();

	return RelationalModel.extend({

		defaults : theDefaults,
		relations : [
            {
                type : Backbone.HasMany,
                key : NS.getQN("omnom:webservice"),
                relatedModel : WebserviceModel,
            },

        ],
		initialize : function() {
			_.each(this.get("serviceList"), function(serviceURL) {
				var service = new WebserviceModel({id : serviceURL});
				service.url = serviceURL;
				service.fetch();
				this.getQN("omnom:webservice").add(service);
			}, this);
		}
	});
});