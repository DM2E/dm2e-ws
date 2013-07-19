//Filename: ParamterListItemView.js

define([
	'jquery',
	'underscore',
	'logging',
	'backbone',
	'BaseView',
	'vm',
	'views/workflow/ParameterView',
	'text!templates/workflow/parameterListItemTemplate.html',
    'text!templates/workflow/parameterFormTemplate.html'
], function($,
	_,
	logging,
	Backbone,
	BaseView,
	Vm,
	ParameterView,
	viewTemplate,
	formTemplate
) {

	var log = logging.getLogger("ParamterListItemView.js");

	return ParameterView.extend({

		template : viewTemplate,

		events : {
			"ok" : "handleFormSave",
			"hidden" : "render",
			"click button.edit-parameter" : "showForm",
			"click button.remove-parameter" : "removeParam",
		},
		
		removeParam: function() {
			this.model.collection.remove(this.model);
		},

		handleFormSave : function() {
			_.each([ "rdfs:label", "omnom:defaultValue", "omnom:isRequired" ], function(qname) {
				this.model.setQN(qname, this.formView.$("input[name='" + qname + "']").val());
			}, this);
			this.render();
		},

		showForm : function() {
			var that = this;
			this.formView = Vm.createView(this, 'ModalView', BaseView.extend({
				model : this.model,
				template : formTemplate,
				initialize : function(options) {
					this.bind("ok", function() { that.handleFormSave(); });
				}
			}));
			this.modalView = new Backbone.BootstrapModal({
				content : this.formView,
//				okCloses : false,
			});
			this.modalView.open();
		}

	});
});
