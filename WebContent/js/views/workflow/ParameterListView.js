//Filename: ParameterListView.js 

define([
	'jquery',
	'underscore',
	'logging',
	'BaseView',
	'vm',
	'text!templates/workflow/parameterListTemplate.html',
	'views/workflow/ParameterListItemView',
], function($,
	_,
	logging,
	BaseView,
	Vm,
	parameterListTemplate,
	ParameterListItemView) {

	var log = logging.getLogger("views.workflow.ParameterListView");

	return BaseView.extend({
		
		events : {
			"click button.add-parameter" : "addItem"
        },

		addItem : function() {
			console.log("param coll: %o", this.collection);
			var NewModel = this.collection.model;
			console.log("new model: %o", NewModel);
			var newInst = new NewModel();
			console.log("new instance: %o", newInst);
			newInst.setQN("rdfs:label", "CHANGE ME");
			this.collection.add(newInst);
//			console.log("parameters coll %o", that.collection);
		},

		initialize : function(options) {
			log.debug("Initialized ParameterListView.js");

			this.listClass = options.listClass;
			this.itemClass = options.itemClass;
            this.parentModel = options.parentModel;
            this.inputOrOutput = options.inputOrOutput;


			this.$el.addClass(this.listClass);

			this.listenTo(this.collection, "add", this.render);
			this.listenTo(this.collection, "remove", this.render);
		},

		render : function() {
			this.$el.html(parameterListTemplate);
			Vm.cleanupSubViews(this);
			_.each(this.collection.models, function(parameterModel) {
				var subview = Vm.createSubView(this, ParameterListItemView, {
					model : parameterModel,
					itemClass : this.itemClass,
                    parentModel : this.parentModel,
                    inputOrOutput : this.inputOrOutput,
				});
				this.appendHTML(subview, "div.list-container")
			}, this);
		}

	});
});
