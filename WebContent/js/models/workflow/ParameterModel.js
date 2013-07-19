//Filename: ParameterModel.js

define([ 'jquery', // lib/jquery/jquery
'underscore', // lib/underscore/underscore
'RelationalModel', // lib/backbone/backbone
'logging', // logging
], function($,
	_,
	RelationalModel,
	logging) {

	var log = logging.getLogger("models.workflow.ParameterModel");

	return RelationalModel.extend({
		
		initialize: function() {
	    	var isWorkflowParam = /.*\/workflow\/.*param\/.*$/.test(this.id);
            // TODO FIXME this should be position not webservice
	    	this.paramType = isWorkflowParam ? "workflow" : "webservice";
	    	log.trace("%o is a workflowparam? : %o", this.id, this.isWorkflowParam);
//            console.error(this);
		},
		
		toJSON : function() {
			
			var asJSON = _.extend({},this.attributes);
			asJSON.uuid = this.cid;
			return asJSON;
		}
	
	});
});
