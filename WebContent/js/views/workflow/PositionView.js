//Filename: js/views/workflow/PositionView.js

define([
	'jquery',
	'underscore',
	'logging',
	'BaseView',
	'vm',
	'constants/RDFNS',
	'views/workflow/ParameterInPositionView',
    'text!templates/workflow/positionTemplate.html'
], function($,
	_,
	logging,
	BaseView,
	Vm,
	NS,
	ParameterInPositionView,
    theTemplate
   ) {

	// var log = logging.getLogger("filename");

	return BaseView.extend({

		
		events: {
            "click button.set-first" : function() {
                var thisModel = this.model;
                var coll = thisModel.collection;
                var thisModelIndex = coll.indexOf(thisModel);
                coll.models.splice(0, 0, coll.models.splice(thisModelIndex, 1)[0]);
                coll.trigger("add");
            },
			"click button.remove-position" : function() {
				this.model.collection.remove(this.model);
			}
		},
		
		className: "boxed-item",

	    template : theTemplate,

	    render : function() {
            this.$el.html(this.createHTML(this.template, { model: this.model.toJSON() }));

            if (this.model.id)
                this.$el.attr("data-backbone-modelid", this.model.id);
            else
                this.$el.attr("data-backbone-modelid", this.model.cid);

			Vm.cleanupSubViews(this);

			console.log(this.model.getQN("omnom:webservice").getQN("omnom:inputParam"));
			_.each(this.model.getQN("omnom:webservice").getQN("omnom:inputParam").models, function(model) {
				var subview = Vm.createSubView(this, ParameterInPositionView, {
					parentType : NS.getQN("omnom:WorkflowPosition"),
                    inputOrOutput : 'input',
					parentModel : this.model,
					model : model
				});
				this.appendHTML(subview, ".input-params")
			}, this);

			_.each(this.model.getQN("omnom:webservice").getQN("omnom:outputParam").models, function(model) {
				var subview = Vm.createSubView(this, ParameterInPositionView, {
					parentType : NS.getQN("omnom:WorkflowPosition"),
					parentModel : this.model,
                    inputOrOutput : 'output',
					itemClass : "output-param",
					model : model
				});
				console.log(model);
				this.appendHTML(subview, ".output-params")
			}, this);
	    	return this;
	    }

    });
});
