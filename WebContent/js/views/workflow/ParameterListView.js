//Filename: ParameterListView.js 

define([
	'jquery',
	'underscore',
	'logging',
	'BaseView',
	'vm',
    'util/dialogs',
	'text!templates/workflow/parameterListTemplate.html',
	'views/workflow/ParameterListItemView',
], function($,
	_,
	logging,
	BaseView,
	Vm,
    dialogs,
	parameterListTemplate,
	ParameterListItemView) {

	var log = logging.getLogger("views.workflow.ParameterListView");

	return BaseView.extend({
		
		events : {
			"click button.add-parameter" : function() { this.addItem({}); }
        },

		addItem : function(preset) {
//			console.log("param coll: %o", this.collection);
			var NewModel = this.collection.model;
//			console.log("new model: %o", NewModel);
			var newInst = new NewModel(preset);
//			console.log("new instance: %o", newInst);
//			this.collection.add(newInst);
            var modalView = new ParameterListItemView({
                model : newInst,
                workflowOrPosition : 'workflow',
            });
            var that = this;
            modalView.on("parameter-was-saved", function() {
                that.collection.add( newInst )
            });
            modalView.showForm();
            return newInst;
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

            var parameterList = this;
            var workflow = this.parentModel;
            this.$(".add-parameter-dropzone").droppable({
                accept: function(draggable) {
                    console.error(workflow);
                    if (! draggable.hasClass("parameter"))
                        return false
                    var source = draggable.data("model");
                    var sourceParent = draggable.data("parentModel");
                    if (source.inputOrOutput !== parameterList.inputOrOutput)
                        return false;
                    if (sourceParent === workflow)
                        return false;
                    return true;
                },
                drop : function (event, ui) {
                    var source = ui.draggable.data("model");
                    console.log(source);
                    var sourceModelJSON = source.toJSON();
                    // delete the ID so backbone-relational is happy and we're forced to create a new, workflow-specific one
                    delete sourceModelJSON.id;
                    // Add parameter
                    var newParam = parameterList.addItem(sourceModelJSON);
                    // FIXME TODO Add connection
//                    var conn = {};
//                    newParam.addConnectionTo()
                },
                activeClass: "drop-active",
                hoverClass: "drop-hover",
            });
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
