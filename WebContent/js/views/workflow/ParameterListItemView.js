//Filename: ParameterListItemView.js

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

	var log = logging.getLogger("ParameterListItemView.js");

	return ParameterView.extend({

		template : viewTemplate,

		events : {
			"click button.edit-parameter" : "showForm",
			"click button.remove-parameter" : "removeParam",
		},

        initialize : function(options) {
            var that = this;
            this.on("ok", function() { that.handleFormSave(); });
            this.on("hidden", function() { that.render(); });
            this.model.on("change", function() { that.render(); });
            this.doInitialize(options);
        },
		
		removeParam: function() {
			this.model.collection.remove(this.model);
		},

		handleFormSave : function() {
			_.each([ "rdfs:label", "omnom:defaultValue"], function(qname) {
				this.model.setQN(qname, this.formView.$("input[name='" + qname + "']").val());
			}, this);
            // FIXME TODO
            this.model.setQN("omnom:isRequired", this.formView.$("input[name='omnom:isRequired']:checked").val());
//            , "omnom:isRequired"
            this.trigger("parameter-was-saved");
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
