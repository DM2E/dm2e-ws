//Filename: WebserviceView.js

define([ 'jquery',
'underscore',
'BaseView',
'logging',
'text!templates/workflow/webserviceTemplate.html'
], function($,
	_,
	BaseView,
	logging,
	webserviceTemplate
	) {

	//noinspection JSUnusedLocalSymbols
    var log = logging.getLogger("WebserviceView.js");

	return BaseView.extend({

        template: webserviceTemplate,
		
		tagName: "div",

		className: "webservice boxed-item fat-border",
		
		initialize : function() {

			this.$el.data("model", this.model);
			
			this.$el.draggable({
				revert: "invalid",
				helper: "clone",
				delay: 0,
			});
		},

//        render : function() {
//            this.doRender();
//        }
	
	});
});