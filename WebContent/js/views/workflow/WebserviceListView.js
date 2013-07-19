//Filename: WebserviceListView.js

define([ 'jquery', // lib/jquery/jquery
'underscore', // lib/underscore/underscore
'BaseView',
'logging', // logging
'vm',
'views/workflow/WebserviceView',
'text!templates/workflow/webserviceListTemplate.html'
], function($,
	_,
	BaseView,
	logging,
	Vm,
	WebserviceView,
	webserviceListTemplate
	) {

	var log = logging.getLogger("views.workflow.WebserviceListView");

	return BaseView.extend({

//        tagName : "div",

		initialize : function() {
			var that = this;
			console.log(this.collection);
			this.collection.on("add", function(model) { that.render(); });
		},
		
		render : function() {
			this.$el.html(webserviceListTemplate);
			log.debug("Rendering the webservice list");
//			Vm.cleanupSubViews(this);
//			_.each(this.collection.models, function(webserviceModel) {
//				var subview = Vm.createSubView(this, WebserviceView, {model : webserviceModel});
//				this.appendHTML(subview, "div.list-container")
//			}, this);
//            this.listSelector = this.el;
            this.renderCollection(WebserviceView);

		}
	
	});
});