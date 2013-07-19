//Filename: ParameterInPositionView.js

define([
	'jquery',
	'underscore',
	'logging',
	'BaseView',
	'constants/RDFNS',
	'views/workflow/ParameterView',
    'text!templates/workflow/parameterInPositionTemplate.html'
], function($,
	_,
	logging,
	BaseView,
	NS,
	ParameterView,
    theTemplate
   ) {

	var log = logging.getLogger("views.workflow.ParameterInPositionView");
	

	return ParameterView.extend({
		
		tagName: "li",

	    template : theTemplate,
		
//		render : function () {
//			console.log(this.model);
//			this.$el.html(this.model[NS.getQN("rdfs:label")]);
//			console.log(this.el);
//			return this;
//		}

    });
});
